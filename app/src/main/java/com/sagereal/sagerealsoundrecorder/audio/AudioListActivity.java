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
import com.sagereal.sagerealsoundrecorder.setting.SettingActivity;
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

        //???ListView???????????????????????????
        mDatas = new ArrayList<>();
        adapter = new AudioListAdapter(this,mDatas);
        binding.audioLv.setAdapter(adapter);
        //??????????????????????????????????????????
        Contants.setsAudioList(mDatas);
        //????????????
        loadDatas();
        //??????????????????
        setEvents();

        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AudioListActivity.this, SettingActivity.class));
            }
        });
    }

    /**
     * ????????????
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
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

    /**
     * ????????????
     */
    private void setEvents() {
        adapter.setOnItemPlayClickListener(playClickListener);
        binding.audioLv.setOnItemLongClickListener(longClickListener);
        binding.audioIb.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //1???????????????
            audioService.closeMusic();
            //2????????????????????????
            startActivity(new Intent(AudioListActivity.this, RecorderActivity.class));
            //3??????????????????activity
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

    //?????????????????????????????????????????????
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
            //?????????????????????????????????
            boolean playing = mDatas.get(position).isPlaying();
            mDatas.get(position).setPlaying(!playing);
            adapter.notifyDataSetChanged();
            audioService.cutMusicOrPause(position);
        }
    };

    /**
     * ???????????????item????????????menu??????
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
     * ??????????????????????????????
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
     * ????????????????????????
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

            @Override
            public void onUnsure(String msg) {

            }
        });
    }

    /**
     * ???????????????????????????????????????
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
        File srcFile = new File(path); //???????????????

        //??????????????????
        String destPath = srcFile.getParent()+File.separator+msg+fileSuffix;
        File destFile = new File(destPath);

        //???????????????????????????
        srcFile.renameTo(destFile);
        //??????????????????
        audioBean.setTitle(msg);
        audioBean.setPath(destPath);
        adapter.notifyDataSetChanged();
    }

    /**
     * ???????????????????????????
     * @param position
     */
    private void deleteFileByPos(int position) {
        AudioBean bean = mDatas.get(position);
        String title = bean.getTitle();
        String path = bean.getPath();
        DialogUtils.showNormalDialog(this, "????????????", "??????????????????????????????????????????????????????????????????"
                , "??????", new DialogUtils.OnLeftClickListener() {
                    @Override
                    public void onLeftClick() {
                        File file = new File(path);
                        file.getAbsoluteFile().delete();//???????????????????????????????????????
                        mDatas.remove(bean);
                        adapter.notifyDataSetChanged();
                    }
                }, "??????", null);
    }



    private void loadDatas(){
        //1.????????????????????????????????????
        File fetchFile = new File(Contants.PATH_FETCH_DIR_RECORD);
        File[] listFiles = fetchFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (new File(dir,name).isDirectory()) {
                    return false;
                }
                if (name.endsWith(".mp3") || name.endsWith(".amr")||name.endsWith(".m4a")||name.endsWith(".aac")) {
                    return true;
                }
                return false;
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        AudioInfoUtils audioInfoUtils = AudioInfoUtils.getInstance();

        //2.???????????????????????????????????????????????????
        for (int i = 0; i < listFiles.length; i++) {
            File audioFile  = listFiles[i];
            String fname = audioFile.getName();  //??????????????????
            String title = fname.substring(0,fname.lastIndexOf("."));
            String  suffix = fname.substring(fname.lastIndexOf("."));
            //?????????????????????????????????
            long flastMod = audioFile.lastModified();
            String time = sdf.format(flastMod);//???????????????????????????????????????
            //?????????????????????
            long flength = audioFile.length();
            //??????????????????
            String audioPath = audioFile.getAbsolutePath();
            long duration = audioInfoUtils.getAudioFileDuration(audioPath);
            String formatDuration = audioInfoUtils.getAudioFileFormatDuration(duration);
            AudioBean audioBean = new AudioBean(i + "", title, time, formatDuration, audioPath, duration, flastMod
                    , suffix, flength);
            mDatas.add(audioBean);
        }
        audioInfoUtils.releseRetriever(); //????????????????????????????????????
        //???????????????????????????????????????????????????????????????
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