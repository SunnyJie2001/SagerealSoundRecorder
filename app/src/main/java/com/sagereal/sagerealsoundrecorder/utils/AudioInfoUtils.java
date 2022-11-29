package com.sagereal.sagerealsoundrecorder.utils;
import android.media.MediaMetadataRetriever;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @version 1.0
 * @anthur ssj
 * @date 2022/11/29 14:35
 * 多媒体音频文件音频数据获取的工具类
 */
public class AudioInfoUtils {
    //获取文件相关内容的工具类
    private MediaMetadataRetriever mediaMetadataRetriever;
    private AudioInfoUtils(){}
    private static AudioInfoUtils utils;
    public static AudioInfoUtils getInstance(){
        if (utils==null) {
            synchronized (AudioInfoUtils.class){
                if (utils==null) {
                    utils = new AudioInfoUtils();
                }
            }
        }
        return utils;
    }

    public long getAudioFileDuration(String filePath){
        long  duration = 0;
        if (mediaMetadataRetriever ==null) {
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }
        mediaMetadataRetriever.setDataSource(filePath);
        String s = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Long.parseLong(s);
        return  duration;
    }

    public String getAudioFileFormatDuration(String format,long durlong){
        durlong-=8*3600*1000;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(durlong));
    }

    /**
     * 转换成固定类型的时长
     * HH:mm:ss
     *
     */
    public String getAudioFileFormatDuration(long durlong){
        return getAudioFileFormatDuration("HH:mm:ss",durlong);
    }

    /**
     * 获取多媒体文件的作者
     */
    public String getAudioFileArtist(String filepath){
        if (mediaMetadataRetriever ==null) {
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }
        mediaMetadataRetriever.setDataSource(filepath);
        String artist =mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);;
        return artist;
    }

    public void releseRetriever(){
        if (mediaMetadataRetriever!=null) {
            mediaMetadataRetriever.release();
            mediaMetadataRetriever = null;
        }
    }
}
