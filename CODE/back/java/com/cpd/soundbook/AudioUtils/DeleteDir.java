package com.cpd.soundbook.AudioUtils;


import org.springframework.stereotype.Component;

import java.io.*;


@Component(value = "deleteDir")
public class DeleteDir {

    //删除临时目录
    public void deleteAllFilesOfDir(File tempDir) {
        if (!tempDir.exists())
            return;
        if (tempDir.isFile()) {
            tempDir.delete();
            return;
        }
        File[] files = tempDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteAllFilesOfDir(files[i]);
        }
        tempDir.delete();
    }
}
