package com.cpd.soundbook.MongoDB;

import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component(value = "mongoDAO")
public interface MongoDBInter {
    void saveFile(File file);

    GridFSDBFile getFile(String filename);

    void deleteFile(String filename);

    void saveBackgroundMusic(File file,int level);

    List<GridFSDBFile> getBackgroundMusic(int level);

    void saveSoundEffectFile(File file);

    GridFSDBFile getSoundEffectFile(String filename);
}
