package com.sagereal.sagerealsoundrecorder.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import com.sagereal.sagerealsoundrecorder.R;
import com.sagereal.sagerealsoundrecorder.bean.AudioBean;
import com.sagereal.sagerealsoundrecorder.databinding.ActivityAudioListBinding;
import com.sagereal.sagerealsoundrecorder.databinding.ActivityMainBinding;
import com.sagereal.sagerealsoundrecorder.recoder.RecorderActivity;
import com.sagereal.sagerealsoundrecorder.utils.AudioInfoDialog;
import com.sagereal.sagerealsoundrecorder.utils.AudioInfoUtils;
import com.sagereal.sagerealsoundrecorder.utils.Contants;
import com.sagereal.sagerealsoundrecorder.utils.DialogUtils;
import com.sagereal.sagerealsoundrecorder.utils.RenameDialog;
import com.sagereal.sagerealsoundrecorder.utils.StartSystemPageUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AudioListActivity extends AppCompatActivity {
    private ActivityAudioListBinding binding;
    private List<AudioBean> mDatas;
    private AudioListAdapter adapter;
    private AudioService audioService;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder audioBinder = (AudioService.AudioBinder)service;
            audioService = audioBinder.getService();
            audioService.setOnPlayChangeListener(playChangeListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    AudioService.OnPlayChangeListener playChangeListener = new AudioService.OnPlayChangeListener() {
        @Override
        public void playChange(int changPos) {
            adapter.notifyDataSetChanged();
        }
    };


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = new Intent(this,AudioService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);

        //为ListView设置数据源和适配器
        mDatas = new ArrayList<>();
        adapter = new AudioListAdapter(this,mDatas);
        binding.audioLv.setAdapter(adapter);
        //将音频对象集合保存到全局变量
        Contants.setsAudioList(mDatas);
        //加载数据
        loadDatas();
        //设置监听时间
        setEvents();
    }

    /**
     * 解绑服务
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断点击了返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            StartSystemPageUtils.goToHomePage(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置监听
     */
    private void setEvents() {
        adapter.setOnItemPlayClickListener(playClickListener);
        binding.audioLv.setOnItemLongClickListener(longClickListener);
        binding.audioIb.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //1、关闭音乐
            audioService.closeMusic();
            //2、跳转到录音界面
            startActivity(new Intent(AudioListActivity.this, RecorderActivity.class));
            //3、销毁当前的activity
            finish();
        }
    };

    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            showPopMenu(view,position);
            audioService.closeMusic();
            return false;
        }
    };

    //点击每一个播放按钮会回调的方法
    AudioListAdapter.OnItemPlayClickListener playClickListener = new AudioListAdapter.OnItemPlayClickListener() {
        @Override
        public void onItemPlayClick(AudioListAdapter adapter, View convertView, View playView, int position) {
            for(int i =0;i< mDatas.size();i++){
                if(i==position){
                    continue;
                }
                AudioBean bean = mDatas.get(i);
                bean.setPlaying(false);
            }
            //获取当前条目录播放状态
            boolean playing = mDatas.get(position).isPlaying();
            mDatas.get(position).setPlaying(!playing);
            adapter.notifyDataSetChanged();
            audioService.cutMusicOrPause(position);
        }
    };

    /**
     * 长按每一项item能够弹出menu窗口
     */
    private void showPopMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.RIGHT);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.audio_menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_info:
                        showFileInfoDialog(position);
                        break;
                    case R.id.menu_del:
                        deleteFileByPos(position);
                        break;
                    case R.id.menu_rename:
                        showRenameDialog(position);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    /**
     * 显示文件详情的对话框
     * @param position
     */
    private void showFileInfoDialog(int position) {
        AudioBean bean =mDatas.get(position);
        AudioInfoDialog dialog = new AudioInfoDialog(this);
        dialog.show();
        dialog.setDialogWidth();
        dialog.setInfo(bean);
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 显示重命名对话框
     * @param position
     */
    private void showRenameDialog(int position) {
        AudioBean bean = mDatas.get(position);
        String title = bean.getTitle();
        RenameDialog dialog = new RenameDialog(this);
        dialog.show();
        dialog.setDialogWidth();
        dialog.setTipText(title);
        dialog.setOnEnsureListener(new RenameDialog.OnEnsureListener() {
            @Override
            public void onEnsure(String msg) {
                renameByPosition(msg,position);
            }
        });
    }

    /**
     * 对于指定位置的文件重新命名
     * @param msg
     * @param position
     */
    private void renameByPosition(String msg, int position) {
        AudioBean audioBean = mDatas.get(position);
        if(audioBean.getTitle().equals(msg)){
            return;
        }
        String path = audioBean.getPath();
        String fileSuffix = audioBean.getFileSuffix();
        File srcFile = new File(path); //原来的文件

        //获取修改路径
        String destPath = srcFile.getParent()+File.separator+msg+fileSuffix;
        File destFile = new File(destPath);

        //进行重命名物理操作
        srcFile.renameTo(destFile);
        //从内存中修改
        audioBean.setTitle(msg);
        audioBean.setPath(destPath);
        adapter.notifyDataSetChanged();
    }

    /**
     * 删除指定位置的文件
     * @param position
     */
    private void deleteFileByPos(int position) {
        AudioBean bean = mDatas.get(position);
        String title = bean.getTitle();
        String path = bean.getPath();
        DialogUtils.showNormalDialog(this, "提示信息", "删除文件后将无法回复，是否确定删除指定文件？"
                , "确定", new DialogUtils.OnLeftClickListener() {
                    @Override
                    public void onLeftClick() {
                        File file = new File(path);
                        file.getAbsoluteFile().delete();//物理删除文件，文件彻底消息
                        mDatas.remove(bean);
                        adapter.notifyDataSetChanged();
                    }
                }, "取消", null);
    }



    private void loadDatas(){
        //1.获取指定路径下的音频文件
        File fetchFile = new File(Contants.PATH_FETCH_DIR_RECORD);
        File[] listFiles = fetchFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (new File(dir,name).isDirectory()) {
                    return false;
                }
                if (name.endsWith(".mp3") || name.endsWith(".amr")||name.endsWith(".wav")||name.endsWith(".aac")) {
                    return true;
                }
                return false;
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        AudioInfoUtils audioInfoUtils = AudioInfoUtils.getInstance();

        //2.遍历数组中的文件，依次得到文件信息
        for (int i = 0; i < listFiles.length; i++) {
            File audioFile  = listFiles[i];
            String fname = audioFile.getName();  //文件名带后缀
            String title = fname.substring(0,fname.lastIndexOf("."));
            String  suffix = fname.substring(fname.lastIndexOf("."));
            //获取文件的最后修改时间
            long flastMod = audioFile.lastModified();
            String time = sdf.format(flastMod);//转换成固定格式的时间字符串
            //获取文件字节数
            long flength = audioFile.length();
            //获取文件路径
            String audioPath = audioFile.getAbsolutePath();
            long duration = audioInfoUtils.getAudioFileDuration(audioPath);
            String formatDuration = audioInfoUtils.getAudioFileFormatDuration(duration);
            AudioBean audioBean = new AudioBean(i + "", title, time, formatDuration, audioPath, duration, flastMod
                    , suffix, flength);
            mDatas.add(audioBean);
        }
        audioInfoUtils.releseRetriever(); //释放多媒体资料的资源对象
        //将集合中的元素重新排序，按照时间的先后顺序
        Collections.sort(mDatas, new Comparator<AudioBean>() {
            @Override
            public int compare(AudioBean o1, AudioBean o2) {
                if (o1.getLastModified()<o2.getLastModified()) {
                    return 1;
                }else if (o1.getLastModified()==o2.getLastModified()){
                    return 0;
                }
                return -1;
            }
        });
        adapter.notifyDataSetChanged();
    }
}