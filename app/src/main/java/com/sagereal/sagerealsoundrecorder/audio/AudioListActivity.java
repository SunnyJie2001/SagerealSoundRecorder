package com.sagereal.sagerealsoundrecorder.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import com.sagereal.sagerealsoundrecorder.R;
import com.sagereal.sagerealsoundrecorder.bean.AudioBean;
import com.sagereal.sagerealsoundrecorder.databinding.ActivityAudioListBinding;
import com.sagereal.sagerealsoundrecorder.databinding.ActivityMainBinding;
import com.sagereal.sagerealsoundrecorder.utils.AudioInfoUtils;
import com.sagereal.sagerealsoundrecorder.utils.Contants;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //为ListView设置数据源和适配器
        mDatas = new ArrayList<>();
        adapter = new AudioListAdapter(this,mDatas);
        binding.audioLv.setAdapter(adapter);
        //加载数据
        loadDatas();

        //设置监听时间
        setEvents();
    }

    /**
     * 设置监听
     */
    private void setEvents() {
        adapter.setOnItemPlayClickListener(playClickListener);
        binding.audioLv.setOnItemLongClickListener(longClickListener);
    }
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            showPopMenu(view,position);
            return false;
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
                        break;
                    case R.id.menu_del:
                        break;
                    case R.id.menu_rename:
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    //点击每一个播放按钮会回调的方法
    AudioListAdapter.OnItemPlayClickListener playClickListener = new AudioListAdapter.OnItemPlayClickListener() {
        @Override
        public void onItemPlayClick(AudioListAdapter adapter, View convertView, View playView, int position) {
        }
    };

    private void loadDatas(){
        //1.获取指定路径下的音频文件
        File fetchFile = new File(Contants.PATH_FETCH_DIR_RECORD);
        File[] listFiles = fetchFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (new File(dir,name).isDirectory()) {
                    return false;
                }
                if (name.endsWith(".mp3") || name.endsWith(".amr")) {
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