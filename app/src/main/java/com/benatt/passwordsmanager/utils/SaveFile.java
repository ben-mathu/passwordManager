package com.benatt.passwordsmanager.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveFile {

    public static void saveFile(byte[] bytes, String path) throws IOException {
        File file = new File(path);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, false));

        bos.write(bytes);
        bos.flush();
        bos.close();
    }
}
