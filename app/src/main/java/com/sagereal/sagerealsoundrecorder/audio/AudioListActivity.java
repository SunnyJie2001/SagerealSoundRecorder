package com.sagereal.sagerealsoundrecorder.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.sagereal.sagerealsoundrecorder.bean.AudioBean;
import com.sagereal.sagerealsoundrecorder.databinding.ActivityAudioListBinding;
import com.sagereal.sagerealsoundrecorder.databinding.ActivityMainBinding;

import java.util.ArrayList;
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
    }
}