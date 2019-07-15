/*
<dependency>
<groupId>com.hankcs</groupId>
<artifactId>hanlp</artifactId>
<version>portable-1.6.1</version>
</dependency>
*/

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetEffectKey {

    private MongoClient mongoClient = new MongoClient("localhost", 27017);
    private DB db = mongoClient.getDB("soundbook");
    private GridFS gridFS = new GridFS(db, "sound");

    public List<Term> getAllNounList(String text){
        List<Term> nounList = new ArrayList<Term>();
        List<Term> termList = IndexTokenizer.segment(text);

        //System.out.println(termList);
        String key;
        for(Term term : termList){
            key = term.nature.toString();
            if(key.substring(0,1).equals("n")){
                nounList.add(term);
            }
        }
        return nounList;
    }

    public List<String> getAllVerbList(String text){
        List<String> verbList = new ArrayList<String>();
        List<Term> termList = IndexTokenizer.segment(text);

        //System.out.println(termList);
        String key;
        for(Term term : termList){
            key = term.nature.toString();
            if(key.substring(0,1).equals("v")){
                verbList.add(term.word);
            }
        }
        return verbList;
    }

    public List<Term> getNounList(String text){

        List<Term> nounList = getAllNounList(text);
        //System.out.println(nounList);
        List<String> verbList = getAllVerbList(text);
        //System.out.println(verbList);
        List<Term> targetList = new ArrayList<Term>();

        //for each noun in the text
        for(Term term:nounList){

            //noun match in the database
            GridFSDBFile gridFSDBFile = gridFS.findOne(term.word);
            if(gridFSDBFile != null){
                String jsonStr = gridFSDBFile.toString();
                //System.out.println(gridFSDBFile);
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONArray verbs = jsonObject.getJSONArray("verb");
                //System.out.println(verbs);
                for(int i = 0; i < verbs.length(); ++i){
                    String verb = (String)verbs.get(i);
                    //System.out.println(verb);
                    if(verbList.contains(verb)){
                        //System.out.println("exist:" + verb);
                        targetList.add(term);
                        System.out.println(targetList);
                    }
                }
                //System.out.println(verbs.get(0));
                //System.out.println(jsonObject.getString("filename"));
            }
        }
        return targetList;
    }

}
