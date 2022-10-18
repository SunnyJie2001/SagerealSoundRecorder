package com.sagereal.sagerealsoundrecorder;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    //内存中唯一的选项
    private static PermissionUtils permissionUtils;
    private PermissionUtils(){}
    private final int mRequestCode = 100; //权限请求码
    public interface OnPermissionCallbackListener{
        void onGranted(); //授权将执行该方法
        void onDenied(List<String> deniedPermissions);
    }
    private OnPermissionCallbackListener mListener;
    public static PermissionUtils getInstance(){
        if (permissionUtils ==null) {
            //保证线程安全
            synchronized (PermissionUtils.class){
                if (permissionUtils == null) {
                    permissionUtils = new PermissionUtils();
                }
            }
        }
        return permissionUtils;
    }

    public void onRequestPermission(Activity context,String[]permissions,OnPermissionCallbackListener listener){
        mListener=listener;
        //判读手机版本 6.0以上需要申请权限
        if (Build.VERSION.SDK_INT >= 23) {
            //创建一个集合，将用户之前没有授予的权限放到集合当中统一管理
            List<String> mPermissionList = new ArrayList<>();
             //逐个判断权限是否通过授权
            for (int i = 0; i < permissions.length; i++) {
                int res = ContextCompat.checkSelfPermission(context, permissions[i]);
                if (res != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            //申请权限,
            if (mPermissionList.size()>0) {
                String[] permission_arr = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(context,permissions,mRequestCode);
            }else {
                //权限通过
                mListener.onGranted();
            }
        }
    }
    public void onRequestPermissionsResult(Activity context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == mRequestCode) {
            List<String> deniedPermissions = new ArrayList<>();
            if (grantResults.length>0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i]);
                    }
                }
            }
            if (deniedPermissions.size()==0) {
                mListener.onGranted();
            }else {
                mListener.onDenied(deniedPermissions);
            }
        }else {
            //接受所有权限
            mListener.onGranted();
        }
    }

    /*
     * 提示用户去应用设置页面手动开启权限
     */
    public void showDialogTipUserGotoAppSetting(Activity context){
        DialogUtils.showNormalDialog(context, "提示信息", "已禁用权限，请手动开启", "取消", new DialogUtils.OnLeftCliclListener() {
            @Override
            public void onLeftClick() {
                context.finish();
            }
        }, "确定", new DialogUtils.OnRightCliclListener() {
            @Override
            public void onRightClick() {
                StartSystemPageUtils.goToAppSetting(context);
                context.finish();
            }
        });
    }
}
