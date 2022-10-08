package com.example.threadpooldemo.Utils;

import android.content.Context;
import android.net.Uri;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    public static final String TAG = "FileUtil";

    public static void cpAssertToLocalPath(Context context, String assetsName, String targetPath) throws FileNotFoundException {
        try {
            InputStream myInput = null;
            OutputStream myOutput = (OutputStream)(new FileOutputStream(targetPath));
            myInput = context.getAssets().open(assetsName);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过uri拷贝外部存储的文件到自己包名的目录下
     * @param uri
     * @param destFile
     */
    public static final void copyFieUriToInnerStorage(Context context, Uri uri, File destFile) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (destFile.exists()) {
                destFile.delete();
            }
            fileOutputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[4096];
            int readCount;
            while ((readCount = inputStream.read(buffer)) >= 0) {
                fileOutputStream.write(buffer, 0, readCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.getFD().sync();
                    fileOutputStream.close();
                }
                inputStream.close();
            } catch (Exception e) {
            }
        }

    }


}