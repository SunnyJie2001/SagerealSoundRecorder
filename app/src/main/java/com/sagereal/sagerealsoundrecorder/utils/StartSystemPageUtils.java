package com.sagereal.sagerealsoundrecorder.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import java.net.URI;

/**
 * @version 1.0
 * @anthur ssj
 * @date 2022/10/18 23:39
 * @des 跳转系统的相关页面
 */
public class StartSystemPageUtils {
    public static void goToAppSetting(Activity context){
        /**
         *跳转到系统当前应用的设置界面
         **/
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent .setData(uri);
        context.startActivity(intent);
    }
    /**
     * 跳转到手机的Home页面
     */
    public static void goToHomePage(Activity context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);
    }
}
