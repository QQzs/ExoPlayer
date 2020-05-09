package com.zs.exoplayer.exo.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

/**
 * @Author: zs
 * @Date: 2020-05-02 16:50
 * @Description:
 */
public class FileUtils {
    /**
     * 获取App根目录
     *
     * @return
     */
    public static String getAppRootPath(String rootPathName) {
        return getRootPath(rootPathName, false);
    }

    public static String getRootPath(String name, boolean hasNoMedia) {
        String path = null;
        if (checkSDCard()) {
            path = Environment.getExternalStorageDirectory().toString()
                    + File.separator
                    + name
                    + File.separator;
        } else {

            File dataDir = ContextUtils.getContext().getFilesDir();
            if (dataDir != null) {
                path = dataDir + File.separator
                        + name
                        + File.separator;
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                    file.setExecutable(true, false);
                    file.setReadable(true, false);
                    file.setWritable(true, false);
                }
            } else {
                path = Environment.getDataDirectory().toString() + File.separator
                        + name
                        + File.separator;
            }
        }

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        if (hasNoMedia) {
            createNoMediaFile(path);
        }
        return path;
    }

    /**
     * 创建nomedia文件
     *
     * @param dirPath
     */
    public static void createNoMediaFile(String dirPath) {
        String filename = ".nomedia";
        File file = new File(dirPath + filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return
     * @Description 判断存储卡是否存在
     */
    public static boolean checkSDCard() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(ContextUtils.getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                return false;
            }

            if (ContextCompat.checkSelfPermission(ContextUtils.getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }

        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

}
