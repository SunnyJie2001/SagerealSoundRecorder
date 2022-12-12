package com.sagereal.sagerealsoundrecorder.recoder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.sagereal.sagerealsoundrecorder.R;
import com.sagereal.sagerealsoundrecorder.utils.Contants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class   RecorderService extends Service {
    private MediaRecorder recorder;
    private boolean isAlive = false;
    private String recorderDirpath;//存放录音文件的公共目录
    private SimpleDateFormat sdf,calSdf;
    private int time;
    private RemoteViews remoteView;
    private NotificationManager manager;
    private Notification notification;
    private int NOTIFY_ID_RECORDER = 102;

    @Override
    public void onCreate() {
        super.onCreate();
        sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        calSdf = new SimpleDateFormat("HH:mm:ss");
        recorderDirpath = Contants.PATH_FETCH_DIR_RECORD;
        initRemoteView();
        initNotification();
    }

    /**
     * 初始化通知对象
     */
    private void initNotification() {
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.icon_voice)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.icon_voice))
                .setContent(remoteView)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(Notification.PRIORITY_HIGH);
        notification = builder.build();
    }

    /**
     * 更新发送通知的函数
     */
    private void updateNotification(String calTime){
        remoteView.setTextViewText(R.id.ny_time,calTime);
        manager.notify(NOTIFY_ID_RECORDER,notification);
    }

    /**
     * 关闭通知
     */
    private void closeNotification(){
        manager.cancel(NOTIFY_ID_RECORDER);
    }

    private void initRemoteView() {
        remoteView = new RemoteViews(getPackageName(), R.layout.notify_recorder);
        Intent intent = new Intent(this,RecorderActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_layout,pi);
    }

    /**
     * 设置更新Activity的UI界面的回调接口
     **/
    public interface OnRefreshUIThreadListener{
        void onRefresh(int fenbei,String time);
    }
    private OnRefreshUIThreadListener onRefreshUIThreadListener;

    public void setOnRefreshUIThreadListener(OnRefreshUIThreadListener onRefreshUIThreadListener) {
        this.onRefreshUIThreadListener = onRefreshUIThreadListener;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (recorder == null) return false;
            double ratio = (double) recorder.getMaxAmplitude() / 100;
            double db = 0;// 分贝
            //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
            //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
            //同时，也可以配置灵敏度sensibility
            if (ratio > 1){
                db = 20 * Math.log10(ratio);
            }
            time += 1000;//ms
            if (onRefreshUIThreadListener!=null) {
                String timeStr = calTime(time);
                onRefreshUIThreadListener.onRefresh((int)db,timeStr);
                updateNotification(timeStr);
            }
            return false;
        }
    });

    /**
     * 时间换算成指定格式
     * @param mSecond
     * @return
     */
    private String calTime(int mSecond) {
        mSecond-=8*60*60*1000;
        String format = calSdf.format(new Date(mSecond));
        return format;
    }
    /**
    开启子线程，实时获取音量，以及当前录制的时间，反馈给主线程
    **/
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (isAlive){
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    });
    /**
    开启录音
    **/
    public void startRecorder(){
        if (recorder == null){
            recorder = new MediaRecorder();
        }
        isAlive = true;
        recorder.reset();
        //设置录音对象参数
        setRecorder();
        try {
            recorder.prepare();
            recorder.start();
            thread.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 停止录音
     */
    public void stopRecorder(){
        if (recorder!=null)
            recorder.stop();
        recorder = null;
        time = 0;
        closeNotification();
        isAlive = false;  //停止线程
    }

    /**
     * 设置录音对象参数
     */
    private void setRecorder() {
        //设置获取麦克风的声音；
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        String type = getSharedPreferences("config", MODE_PRIVATE).getString("voiceType", "amr");
        File file;

        //设置录制的输出文件
        String time = sdf.format(new Date());
        if(type.equals("aac")){
            //设置输出格式
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            //设置编码格式
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            file = new File(recorderDirpath,time+".aac");
        }else if(type.equals("mp3")){
            //设置输出格式
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //设置编码格式
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            file = new File(recorderDirpath,time+".mp3");
        }else{
            //设置输出格式
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            //设置编码格式
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            file = new File(recorderDirpath,time+".amr");
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        recorder.setOutputFile(file.getAbsolutePath());
        //设置最多录制的时间，最多录制10分钟
        recorder.setMaxDuration(10*60*1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RecorderBinder();
    }
    public class RecorderBinder extends Binder {
        public RecorderService getService(){
            return RecorderService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecorder();
    }
}