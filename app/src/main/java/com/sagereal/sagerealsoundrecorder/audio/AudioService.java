package com.sagereal.sagerealsoundrecorder.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.sagereal.sagerealsoundrecorder.R;
import com.sagereal.sagerealsoundrecorder.bean.AudioBean;
import com.sagereal.sagerealsoundrecorder.utils.Contants;

import java.util.List;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener{
    private MediaPlayer mediaPlayer = null;
    private List<AudioBean> mList;  //播放列表
    private int playPosition = -1;//记录当前播放位置
    private RemoteViews remoteView;//通知对应的自定义布局
    private AudioReceiver receiver;
    private NotificationManager manager;
    private final int NOTIFY_ID_MUSIC = 100;

    /**
     * 接收通知发出的广播action
     */
    private final String PRE_ACTION_LAST = "com.ztree.last";
    private final String PRE_ACTION_PLAY = "com.ztree.play";
    private final String PRE_ACTION_NEXT = "com.ztree.next";
    private final String PRE_ACTION_CLOSE = "com.ztree.close";
    private Notification notification;


    public AudioService() {}

    /**
     * 创建通知对象和远程View对象
     */
    @Override
    public void onCreate() {
        super.onCreate();
        initRegisterReceiver();
        initRemoteView();
        initNotification();
    }

    /**
     * 注册广播接收者
     */
    class AudioReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            notifyUIControl(action);
        }
    }

    private void notifyUIControl(String action) {
        switch (action){
            case PRE_ACTION_LAST:
                previousMusic();
                break;
            case PRE_ACTION_NEXT:
                nextMusic();
                break;
            case PRE_ACTION_PLAY:
                pauseOrContinueMusic();
                break;
            case PRE_ACTION_CLOSE:
                closeNotification();
                break;
        }
    }
    /**
    关闭通知栏，停止音乐播放
    **/
    private void closeNotification() {
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            mList.get(playPosition).setPlaying(false);
        }
        notifyActivityRefreshUI();
        manager.cancel(NOTIFY_ID_MUSIC);
    }

    /**
     * 接受用户点击通知通知栏按钮信息
     */
    private void initRegisterReceiver() {
        receiver = new AudioReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PRE_ACTION_LAST);
        filter.addAction(PRE_ACTION_PLAY);
        filter.addAction(PRE_ACTION_NEXT);
        filter.addAction(PRE_ACTION_CLOSE);
        registerReceiver(receiver,filter);
    }

    /**
     * 设置通知栏显示效果以及图片点击事件
     */
    private void initRemoteView() {
        remoteView = new RemoteViews(getPackageName(), R.layout.notify_audio);
        PendingIntent lastPI = PendingIntent
                .getBroadcast(this,1,new Intent(PRE_ACTION_LAST),PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_last,lastPI);
        PendingIntent nextPI = PendingIntent
                .getBroadcast(this,1,new Intent(PRE_ACTION_NEXT),PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_next,nextPI);
        PendingIntent playPI = PendingIntent
                .getBroadcast(this,1,new Intent(PRE_ACTION_PLAY),PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_play,playPI);
        PendingIntent closePI = PendingIntent
                .getBroadcast(this,1,new Intent(PRE_ACTION_CLOSE),PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_close,closePI);
    }

    /**
     * 初始化通知栏
     */
    private void initNotification() {
        manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.icon_app_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.icon_app_logo))
                .setContent(remoteView)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(Notification.PRIORITY_HIGH);
        notification = builder.build();
    }

    /**
    更新通知栏信息的函数
    **/

    private void updateNotification(int position){
        //根据多媒体的播放状态显示图片
        if (mediaPlayer.isPlaying()){
            remoteView.setImageViewResource(R.id.ny_iv_play,R.mipmap.red_pause);
        }else {
            remoteView.setImageViewResource(R.id.ny_iv_play,R.mipmap.red_play);
        }
        remoteView.setTextViewText(R.id.ny_tv_title,mList.get(position).getTitle());
        remoteView.setTextViewText(R.id.ny_tv_duration,mList.get(position).getDuration());
        //发送通知
        manager.notify(NOTIFY_ID_MUSIC,notification);
    }

    public interface OnPlayChangeListener{
        public void playChange(int changPos);
    }

    private OnPlayChangeListener onPlayChangeListener;

    public void setOnPlayChangeListener(OnPlayChangeListener onPlayChangeListener) {
        this.onPlayChangeListener = onPlayChangeListener;
    }

    /**
     * 多媒体服务改变，提示Acvitity刷新ui
     */
    public void notifyActivityRefreshUI(){
        if(onPlayChangeListener!=null){
            onPlayChangeListener.playChange(playPosition);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextMusic();//当前播放完成，直接播放下一个音乐
    }

    public class AudioBinder extends Binder {
        public AudioService getService(){
            return AudioService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AudioBinder();
    }

    /**
     * 播放按钮有两种可能性
     * 1、不是当前播放的位置被点击了，就进行切歌操作
     * 2.当前播放的位置被点击了，就进行暂停或者继续的操作
     **/
    public void cutMusicOrPause(int position){
        int playPosition = this.playPosition;
        if(position!=playPosition){
            //判断是否正在播放，如果切歌。把上一曲改为false
            if(playPosition!=-1){
                mList.get(playPosition).setPlaying(false);
            }
            play(position);
            return;
        }
        pauseOrContinueMusic();
    }

    /**
     * 播放音乐，点击切歌
     * @param position
     */
    public void play(int position){
        if(mediaPlayer==null){
            mediaPlayer = new MediaPlayer();
            //设置监听音乐
            mediaPlayer.setOnCompletionListener(this);
        }
        //播放时获取当前歌曲列表，判断是否有歌曲
        mList = Contants.getsAudioList();
        if (mList.size()<=0){
            return;
        }
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        try {
            //切歌之前重置，释放掉原来的资源
            mediaPlayer.reset();
            playPosition = position;
            //设置播放音频的资源路径
            mediaPlayer.setDataSource(mList.get(position).getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            //设置当前位置正在播放
            mList.get(position).setPlaying(true);
            notifyActivityRefreshUI();
            setFlagControlThread(true);
            updateProgress();
            updateNotification(position);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 暂停/ 继续播放音乐
     */
    public void pauseOrContinueMusic(){
        int playPosition = this.playPosition;
        AudioBean audioBean = mList.get(playPosition);
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            audioBean.setPlaying(false);
        }else {
            mediaPlayer.start();
            audioBean.setPlaying(true);
        }
        notifyActivityRefreshUI();
        updateNotification(playPosition);
    }

    /**
     * 下一曲
     */
    private void nextMusic() {
        mList.get(playPosition).setPlaying(false);
        if(playPosition>=mList.size()-1){
            playPosition = 0;
        }else {
            playPosition++;
        }
        mList.get(playPosition).setPlaying(true);
        play(playPosition);
    }

    /**
     *播放上一曲的音乐
     */
    private void previousMusic() {
        mList.get(playPosition).setPlaying(false);
        if(playPosition==0){
            playPosition = mList.size()-1;
        }else {
            playPosition--;
        }
        mList.get(playPosition).setPlaying(true);
        play(playPosition);
    }
    /**
    停止音乐
    **/
    public void closeMusic(){
        if(mediaPlayer!=null){
            setFlagControlThread(false);
            closeNotification();
            mediaPlayer.stop();
            playPosition = -1;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
        closeMusic();
    }

    /**
    更新播放进度的方法
    * */
    private boolean flag = true;
    private final int PROGRESS_ID = 1;
    private final int INTERMINATE_TIME = 1000;
    public void setFlagControlThread(boolean flag){this.flag = flag;}
    public void updateProgress(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag){
                    //获取总时长
                    long total = mList.get(playPosition).getDurationLong();
                    //获取当前播放的位置
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    //计算播放进度
                    int progress = (int)(currentPosition*100/total);
                    mList.get(playPosition).setCurrentProgress(progress);
                    handler.sendEmptyMessageDelayed(PROGRESS_ID,INTERMINATE_TIME);
                    try {
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == PROGRESS_ID){
                notifyActivityRefreshUI();
            }
            return true;
        }
    });
}