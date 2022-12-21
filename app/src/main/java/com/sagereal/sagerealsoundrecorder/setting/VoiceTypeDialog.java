package com.sagereal.sagerealsoundrecorder.setting;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.sagereal.sagerealsoundrecorder.R;
import com.sagereal.sagerealsoundrecorder.databinding.DialogVoicetypeBinding;

/**
 * @version 1.0
 * @anthur ssj
 * @date 2022/12/12 22:06
 */
public class VoiceTypeDialog extends Dialog implements View.OnClickListener{
    public VoiceTypeDialog(@NonNull Context context) {
        super(context);
    }

    DialogVoicetypeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogVoicetypeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvMc.setOnClickListener(this);
        binding.tvSj.setOnClickListener(this);
        binding.tvDx.setOnClickListener(this);
    }
    public void setDialogWidth(){
        //获取当前窗口对象
        Window window = getWindow();
        //获取窗口信息
        WindowManager.LayoutParams wlp = window.getAttributes();
        //获取屏幕宽度
        Display display = window.getWindowManager().getDefaultDisplay();
        wlp.width = display.getWidth()-30;
        wlp.gravity = Gravity.BOTTOM;
        //设置窗口背景透明
        window.setBackgroundDrawableResource(android.R.color.transparent);
        //设置窗口参数
        window.setAttributes(wlp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_mc:
                getContext().getSharedPreferences("config", MODE_PRIVATE).edit().putString("voiceType", "aac").commit();
                break;
            case R.id.tv_sj:
                getContext().getSharedPreferences("config", MODE_PRIVATE).edit().putString("voiceType", "mp3").commit();
                break;
            case R.id.tv_dx:
                getContext().getSharedPreferences("config", MODE_PRIVATE).edit().putString("voiceType", "amr").commit();
                break;
        }
        Intent intent = new Intent(getContext(), SettingActivity.class);
        getContext().startActivity(intent);
        dismiss();
    }
}
