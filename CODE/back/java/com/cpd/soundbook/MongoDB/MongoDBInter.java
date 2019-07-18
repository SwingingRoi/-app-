package com.cpd.soundbook.MongoDB;

import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.stereotype.Component;

import java.io.File;

@Component(value = "mongoDAO")
public interface MongoDBInter {
    void saveFile(File file);

    GridFSDBFile getFileByName(String filename);

    void deleteFile(String filename);

    GridFSDBFile getBGMByLevel(int level);

    GridFSDBFile getBGMByName(String name);

    GridFSDBFile getEffectByName(String name);
}
