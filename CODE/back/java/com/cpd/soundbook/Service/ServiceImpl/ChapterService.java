package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.AudioUtils.*;
import com.cpd.soundbook.AudioUtils.CutAudio;
import com.cpd.soundbook.DAO.DAOInterface.ChapterDAO;
import com.cpd.soundbook.DAO.DAOInterface.DraftDAO;
import com.cpd.soundbook.Entity.Chapter;
import com.cpd.soundbook.MongoDB.MongoDBInter;
import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChapterService implements com.cpd.soundbook.Service.ServiceInterface.ChapterService {
    @Autowired
    private ChapterDAO chapterDAO;

    @Autowired
    private DraftDAO draftDAO;

    @Autowired
    private AddEffect addEffect;

    @Autowired
    private MongoDBInter mongoDAO;

    @Autowired
    private AudioToText audioToText;

    @Autowired
    private TextToSpeech tts;

    @Autowired
    private FfmpegUtils ffmpegUtils;

    @Autowired
    private DeleteDir deleteDir;

    @Autowired
    private CutAudio cutAudio;

    final private int INFINITE = 99999999;

    @Override
    public void storeChapter(JSONObject chapter) {
        try{
            Chapter c = new Chapter();
            c.setBookid(chapter.getInt("bookid"));
            c.setTitle(chapter.getString("title"));
            c.setContent(chapter.getString("content"));
            c.setSpeechPath(chapter.getString("speechPath"));
            c.setTime(chapter.getString("length"));
            c.setBgmpath(chapter.getString("bgmPath"));
            c.setType(chapter.getBoolean("type"));
            chapterDAO.storeChapter(c);
            draftDAO.deleteDraftByBookid(chapter.getInt("bookid"));//删除上次的草稿
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public JSONArray getChapters(int bookid, int from, int size) {
        JSONArray result = new JSONArray();
        List<Chapter> chapters = chapterDAO.getChapters(bookid, from, size);

        try {
                for (Chapter chapter : chapters) {
                    JSONObject object = new JSONObject();
                    object.put("id", chapter.getId());
                    object.put("bookid", chapter.getBookid());
                    object.put("title", chapter.getTitle());
                    object.put("length",chapter.getTime());
                    result.put(object);
                }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void deleteChapters(JSONObject ids) {
        try {
            int bookid = ids.getInt("bookid");
            JSONArray idArray = ids.getJSONArray("ids");

            for(int i=0;i<idArray.length();i++){
                Chapter chapter = chapterDAO.getChapterByID(idArray.getJSONObject(i).getInt("id"));
                mongoDAO.deleteFile(chapter.getSpeechPath());
            }//删除音频文件

            chapterDAO.deleteChapters(bookid,idArray);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject getChapterByID(int id) {
        Chapter chapter = chapterDAO.getChapterByID(id);
        JSONObject result = new JSONObject();
        try{
            result.put("id",id);
            result.put("title",chapter.getTitle());
            result.put("content",chapter.getContent());
            result.put("speechPath",chapter.getSpeechPath());
            result.put("bgmPath",chapter.getBgmpath());
            result.put("length",chapter.getTime());
            result.put("type",chapter.isType());
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void modifyChapter(JSONObject chapter) {
        try{
            chapterDAO.modifyChapter(chapter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String matchBGMByText(String text){
        return mongoDAO.getBGMByLevel(computeFeelLevel(text)).getFilename();
    }

    @Override
    public GridFSDBFile getBGM(String filename) {
        return mongoDAO.getBGMByName(filename);
    }

    //分析文本情感
    private int computeFeelLevel(String text){
        int result = 0;
        int preindex1 = 0;
        List<Integer> pLevels = new ArrayList<>();

        System.out.println("begin compute feel level");
        for(int i = 0;i<text.length();i++){
            if(text.charAt(i) == '\n' || i == text.length() - 1){
                String paragraph = text.substring(preindex1,i);

                List<Integer> sLevels = new ArrayList<>();
                int preindex2 = 0;
                for(int j=0;j< paragraph.length();j++){
                    if(paragraph.charAt(j) == '。' || paragraph.charAt(j) == '，' || j == paragraph.length() - 1){
                        String sentence = paragraph.substring(preindex2,j + 1);

                        try {

                            Sentiment sentiment = new Sentiment();
                            int level = sentiment.feelLevel(sentence);
                            sLevels.add(level);
                            preindex2 = j + 1;
                        }catch (Exception e){
                            j--;
                        }
                    }
                }

                int pLevel = 0;
                for(int level : sLevels){
                    pLevel += level;
                }

                pLevel = pLevel / sLevels.size();
                pLevels.add(pLevel);
                preindex1 = i;
            }
        }

        for(int pLevel : pLevels){
            result += pLevel;
        }

        if(pLevels.size() == 0) return 0;
        System.out.println("Level: " + result / pLevels.size());
        return result / pLevels.size();

    }

    //返回添加音效后的语音在mongoDB中的存储路径
    @Override
    public String textToSpeech(String text) {
        String result ="";
        RandomName randomName = new RandomName();
        File resultFile;
        PrintWriter printWriter;

        //逐句转化为语音并添加音效
        //System.getProperty("user.dir") + "\\mp3\\tts\\" + randomName.randomName() + "\\"
        //"/audiobook/mp3/tts/" + randomName.randomName() + "/"
        String tempDir = "/audiobook/mp3/tts/" + randomName.randomName() + "/";//临时文件夹
        File tempD = new File(tempDir);
        if(!tempD.exists()) tempD.mkdir();

        String filenamePath = tempDir + randomName.randomName() + ".txt";
        String resultPath = tempDir + randomName.randomName() + ".mp3";

        System.out.println("text to speech begin");
        try {
            printWriter = new PrintWriter(filenamePath);
            int preIndex = 0;

            for(int i=0;i<text.length();i++) {
                String textNow = "";
                if(text.charAt(i) == ',' || text.charAt(i) == '，' || text.charAt(i) == '.' || text.charAt(i) == '。' || i==text.length() - 1){
                    textNow = text.substring(preIndex,i+1);
                    preIndex = preIndex + textNow.length();
                    File srcFile = tts.translate(textNow,tempDir);
                    //System.out.println("srcFile: " + srcFile);

                    File file = addEffect.addEffect(srcFile,textNow,tempDir);
                    //System.out.println("file :" + file.getAbsolutePath());

                    printWriter.write("file " + "\'" + file.getAbsolutePath() + "\'" + System.getProperty("line.separator"));
                    printWriter.flush();
                }
            }
            printWriter.close();

            resultFile = ffmpegUtils.concat(filenamePath,resultPath);//执行ffmepg的拼接命令
            mongoDAO.saveFile(resultFile);
            result = resultFile.getName();
            //System.out.println("reultFile: " + resultFile.getAbsolutePath());

            deleteDir.deleteAllFilesOfDir(new File(tempDir));//删除临时文件
            System.out.println("text to speech done");

        }catch (Exception e){
            e.printStackTrace();
            deleteDir.deleteAllFilesOfDir(new File(tempDir));//删除临时文件
        }
        return result;
    }

    @Override
    public String storeSpeech(File speech) {
        String result = "";
        try{
            mongoDAO.saveFile(speech);
            result = speech.getName();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public GridFSDBFile getSpeech(String path) {
        return mongoDAO.getFileByName(path);
    }

    @Override
    public String updateSpeech(String oldpath, File newspeech) {
        mongoDAO.deleteFile(oldpath);
        mongoDAO.saveFile(newspeech);
        return newspeech.getName();
    }

    @Override
    public JSONArray getChapterIDs(int bookid) {
        JSONArray result = new JSONArray();
        try{
            List<Chapter> chapters = chapterDAO.getChapters(bookid,0,INFINITE);
            for(Chapter chapter : chapters){
                JSONObject object = new JSONObject();
                object.put("id",chapter.getId());
                result.put(object);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /*语音转文本,返回一个JSONObject
    内容为：
    {'speechPath':添加音效后的语音在mongoDB中的存储路径,
    'text':转化后的文本
    }
     */
    @Override
    public JSONObject speechToText(File srcFile) {
        JSONObject result = new JSONObject();//返回结果
        RandomName randomName = new RandomName();
        PrintWriter printWriter;
        File resultFile;//添加音效后的语音文件
        String text = "";//转化后的文本

        System.out.println("speech to text begin");
        //存储裁剪文件的临时目录
        //System.getProperty("user.dir") + "\\mp3\\stt\\" + randomName.randomName() + "\\"
        //"/audiobook/mp3/stt/" + randomName.randomName() + "/"
        String tempDir = "/audiobook/mp3/stt/" + randomName.randomName() + "/";
        File tempD = new File(tempDir);
        if(!tempD.exists()) tempD.mkdirs();
        //cuttedResult:裁剪后的文件的路径
        List<String> cuttedResult = cutAudio.cutAudio(srcFile.getAbsolutePath(),tempDir);


        //System.out.println("cut mp3 done");
        //System.out.println("cutted result: ");

        //if(cuttedResult.size() == 0) return null;
        /*
        filenamePath:执行ffmpeg concat命令所需txt文件
        resultPath:存放最终结果的文件
         */

        String filenamePath = tempDir + randomName.randomName() + ".txt";
        String resultPath = tempDir + randomName.randomName() + ".mp3";


        try {
            printWriter = new PrintWriter(filenamePath);
            for (int i = 0;i<cuttedResult.size();i++) {
                String pathI = cuttedResult.get(i);
                //将切割后的mp3文件转化为pcm文件，以便调用百度接口
                File mp3FileI = new File(pathI);


                /*
                ffmpegUtils.transformFormat(pathI,
                        tempDir + i + ".pcm");
                File pcmFileI = new File(tempDir + i + ".pcm");
                System.out.println("pcmFile" + i + " " + pcmFileI.getAbsolutePath());
*/
                String tempText = audioToText.audioToText(mp3FileI);
                text += tempText;
                if(tempText == null) {
                    deleteDir.deleteAllFilesOfDir(new File(tempDir));
                    return null;
                }
                //System.out.println("text" + i + ": " + text);
                //System.out.println("srcFile: " + mp3FileI.getAbsolutePath());
                File tempFile = addEffect.addEffect(mp3FileI, tempText,tempDir);
                //System.out.println("tempFile" + i + " " + tempFile.getAbsolutePath());
                //System.out.println("temp text:" + tempText);

                //将要合并的文件名写入filenamePath
                printWriter.write("file " + "\'" + tempFile.getAbsolutePath() + "\'" + System.getProperty("line.separator"));
                printWriter.flush();
            }
            printWriter.close();

            //System.out.println("concat file begin");
            resultFile = ffmpegUtils.concat(filenamePath,resultPath);
            //System.out.println("concat file done");
            mongoDAO.saveFile(resultFile);

            System.out.println("speech to text done");
            result.put("speechPath",resultFile.getName());
            result.put("text",text);
            //System.out.println("resultFile: " + resultFile.getAbsolutePath());

            deleteDir.deleteAllFilesOfDir(new File(tempDir));
            //删除临时文件
        }catch (Exception e){
            deleteDir.deleteAllFilesOfDir(new File(tempDir));
            e.printStackTrace();
            return null;
        }
        return result;
    }

    @Override
    public void deletSpeech(String path) {
        mongoDAO.deleteFile(path);
    }
}
