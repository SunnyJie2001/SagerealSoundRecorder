package com.sagereal.sagerealsoundrecorder.utils;

import com.sagereal.sagerealsoundrecorder.bean.AudioBean;

import java.util.List;

/**
 * @version 1.0
 * @anthur ssj
 * @date 2022/11/22 15:39
 */
public class Contants {

    /**
     * 存放文件的目录
     */
    public static String PATH_APP_DIR;
    public static String PATH_FETCH_DIR_RECORD;

    private static List<AudioBean> sAudioList;
    public static void setsAudioList(List<AudioBean>audioList){
        if(audioList!=null){
            Contants.sAudioList = audioList;
        }
    }
    public static List<AudioBean> getsAudioList(){
        return sAudioList;
    }
}
