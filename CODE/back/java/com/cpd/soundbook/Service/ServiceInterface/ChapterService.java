package com.cpd.soundbook.Service.ServiceInterface;

import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;

@Component(value = "chapterService")
public interface ChapterService {
    void storeChapter(JSONObject chapter);

    JSONArray getChapters(int bookid,int from,int size);

    void deleteChapters(JSONObject ids);

    JSONObject getChapterByID(int id);

    void modifyChapter(JSONObject chapter);

    String matchBGM(String text);

    GridFSDBFile getBGM(String filename);

    File textToSpeech(String text);

    void storeSpeech(File speech);

    GridFSDBFile getSpeech(String path);

    void updateSpeech(String oldpath,File newspeech);
}
