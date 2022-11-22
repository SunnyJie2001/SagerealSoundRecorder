package com.sagereal.sagerealsoundrecorder.utils;

import android.os.Environment;

import java.io.File;

/**
 * @version 1.0
 * @anthur ssj
 * @date 2022/11/22 15:18
 */
public class SDCardUtils {
    private SDCardUtils(){

    }
    private static SDCardUtils sdCardUtils;
    public static SDCardUtils getInstance(){
        if (sdCardUtils ==null){
            synchronized (SDCardUtils.class){
                if (sdCardUtils ==null){
                    sdCardUtils = new SDCardUtils();
                }
            }
        }
        return sdCardUtils;
    }
    /**
     * @des 判断当前手机是否有SD卡
     */
    public boolean isHaveSDCard(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    /**
     * @des 创建项目的公共目录
     */
    public File createAppPublicDir(){
        if (isHaveSDCard()) {
            File sdDir = Environment.getExternalStorageDirectory();
            File addDir = new File(sdDir,IFieInter.APP_DIR);
            if (!addDir.exists()) {
                addDir.mkdir();
            }
            Contants.PATH_APP_DIR = addDir.getAbsolutePath();
            return addDir;
        }
        return null;
    }
    /**
     * @des 创建项目分支目录
     */
    public File createAppFetchDir(String dir){
        File publicDir = createAppPublicDir();
        if (publicDir!=null) {
            File fetchDir = new File(publicDir, dir);
            if (!fetchDir.exists()) {
                fetchDir.mkdirs();
            }
            return fetchDir;
        }
        return null;
    }
}
