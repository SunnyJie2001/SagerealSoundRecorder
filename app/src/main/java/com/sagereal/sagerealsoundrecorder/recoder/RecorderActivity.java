package com.sagereal.sagerealsoundrecorder.recoder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sagereal.sagerealsoundrecorder.R;
import com.sagereal.sagerealsoundrecorder.audio.AudioListActivity;
import com.sagereal.sagerealsoundrecorder.bean.AudioBean;
import com.sagereal.sagerealsoundrecorder.databinding.ActivityRecoderBinding;
import com.sagereal.sagerealsoundrecorder.utils.AudioInfoUtils;
import com.sagereal.sagerealsoundrecorder.utils.Contants;
import com.sagereal.sagerealsoundrecorder.utils.RenameDialog;
import com.sagereal.sagerealsoundrecorder.utils.StartSystemPageUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecorderActivity extends AppCompatActivity {

    private ActivityRecoderBinding binding;
    private RecorderService recorderService;
    RecorderService.OnRefreshUIThreadListener refreshUIListener = new RecorderService.OnRefreshUIThreadListener() {
        @Override
        public void onRefresh(int fenbei, String time) {
            binding.voiceLine.setVolume(fenbei);
            binding.tvDuration.setText(time);
        }
    };
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.RecorderBinder binder = (RecorderService.RecorderBinder) service;
            recorderService = binder.getService();
            recorderService.startRecorder();
            recorderService.setOnRefreshUIThreadListener(refreshUIListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecoderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = new Intent(this,RecorderService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
        setSupportActionBar( binding.audioTbar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch(view.getId()){
//            case R.id.iv_back:
//                StartSystemPageUtils.goToHomePage(this);
//                break;
            case R.id.iv_stop:
                recorderService.stopRecorder();
                boolean rename = getSharedPreferences("config", MODE_PRIVATE).getBoolean("rename", false);
                if (rename) {
                    AudioBean bean = loadDatas().get(0);
                    String title = bean.getTitle();
                    RenameDialog dialog = new RenameDialog(this);
                    dialog.show();
                    dialog.setDialogWidth();
                    dialog.setTipText(title);
                    dialog.setOnEnsureListener(new RenameDialog.OnEnsureListener() {
                        @Override
                        public void onEnsure(String msg) {
                            renameByPosition(msg, bean);
                            Intent intent = new Intent(RecorderActivity.this, AudioListActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onUnsure(String msg) {
                            File file = new File(bean.getPath());
                            file.getAbsoluteFile().delete();
                            Toast.makeText(getApplicationContext(), "??????" + bean.getTitle() + "????????????", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RecorderActivity.this, AudioListActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    });
                } else {
                    Intent intent = new Intent(this, AudioListActivity.class);
                    startActivity(intent);
                    finish();
                }

                break;

        }
    }

    private void renameByPosition(String msg, AudioBean audioBean) {
        if (audioBean.getTitle().equals(msg)) {
            return;
        }
        String path = audioBean.getPath();
        String fileSuffix = audioBean.getFileSuffix();
        File srcFile = new File(path);
        String destPath = srcFile.getParent() + File.separator + msg + fileSuffix;
        File destFile = new File(destPath);
        srcFile.renameTo(destFile);
        audioBean.setTitle(msg);
        audioBean.setPath(destPath);
    }


    private List<AudioBean> loadDatas() {
        List<AudioBean> mDatas = new ArrayList<>();
//1.????????????????????????????????????
        File fetchFile = new File(Contants.PATH_FETCH_DIR_RECORD);
        File[] listFiles = fetchFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (new File(dir, name).isDirectory()) {
                    return false;
                }
                if (name.endsWith(".mp3") || name.endsWith(".amr") || name.endsWith(".m4a") || name.endsWith(".aac")) {
                    return true;
                }
                return false;
            }
        });
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        AudioInfoUtils audioInfoUtils = AudioInfoUtils.getInstance();
        //2.??????????????????????????????????????????????????????
        for (int i = 0; i < listFiles.length; i++) {
            File audioFile = listFiles[i];
            String fname = audioFile.getName();
            String title = fname.substring(0, fname.lastIndexOf("."));
            String suffix = fname.substring(fname.lastIndexOf("."));
            //??????????????????????????????
            long flastMod = audioFile.lastModified();
            String time = sdf.format(flastMod);
            //????????????????????????
            long flength = audioFile.length();
            //??????????????????
            String audioPath = audioFile.getAbsolutePath();
            long duration = audioInfoUtils.getAudioFileDuration(audioPath);
            String formatDuration = audioInfoUtils.getAudioFileFormatDuration(duration);
            AudioBean audioBean = new AudioBean(i + "", title, time, formatDuration, audioPath,
                    duration, flastMod, suffix, flength);
            mDatas.add(audioBean);
        }
        audioInfoUtils.releseRetriever();//????????????????????????????????????
        //?????????????????????????????????
        Collections.sort(mDatas, new Comparator<AudioBean>() {
            @Override
            public int compare(AudioBean o1, AudioBean o2) {
                if (o1.getLastModified() < o2.getLastModified()) {
                    return 1;
                } else if (o1.getLastModified() == o2.getLastModified()) {
                    return 0;
                }
                return -1;
            }
        });

        return mDatas;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //????????????
        if (connection!=null){
            unbindService(connection);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //???????????????????????????
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            StartSystemPageUtils.goToHomePage(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}