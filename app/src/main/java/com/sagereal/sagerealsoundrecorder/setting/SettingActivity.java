package com.sagereal.sagerealsoundrecorder.setting;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sagereal.sagerealsoundrecorder.databinding.ActSettingBinding;


/**
 * @version 1.0
 * @anthur ssj
 * @date 2022/12/12 22:05
 */
public class SettingActivity extends AppCompatActivity {
    private ActSettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        boolean soundMode = getSharedPreferences("config", MODE_PRIVATE).getBoolean("soundMode", false);
        binding.swTT.setChecked(soundMode);
        binding.swTT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("config", MODE_PRIVATE).edit().putBoolean("soundMode", isChecked).commit();
            }
        });
        boolean rename = getSharedPreferences("config", MODE_PRIVATE).getBoolean("rename", false);
        binding.swName.setChecked(rename);
        binding.swName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("config", MODE_PRIVATE).edit().putBoolean("rename", isChecked).commit();
            }
        });
        binding.versi.setText(getapkname());

        String voiceType = getSharedPreferences("config", MODE_PRIVATE).getString("voiceType", "amr");

        binding.type.setText(voiceType);

        binding.rlVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoiceTypeDialog voiceTypeDialog = new VoiceTypeDialog(SettingActivity.this);
                voiceTypeDialog.setDialogWidth();
                voiceTypeDialog.show();
            }
        });
        setSupportActionBar( binding.audioTbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String voiceType = getSharedPreferences("config", MODE_PRIVATE).getString("voiceType", "amr");

        binding.type.setText(voiceType);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String voiceType = getSharedPreferences("config", MODE_PRIVATE).getString("voiceType", "amr");

        binding.type.setText(voiceType);
    }

    private String getapkname() {

        PackageManager packagemanager = getPackageManager();


        PackageInfo packinfo = null;
        try {
            packinfo = packagemanager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String version = packinfo.versionName;

        return "V" + version;

    }
}
