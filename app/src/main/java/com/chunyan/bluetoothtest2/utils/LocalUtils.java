package com.chunyan.bluetoothtest2.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LocalUtils {

    //申请权限
    public static final int permission_LocationCode = 101;
    //打开gps定位
    public static final int open_GPSCode = 102;
    static String[] permissionsIndex;
    /**
     * 此方法用来检查gps和定位权限，先检查gps是否打开，在检查是否有定位权限
     * @param activity 上下文对象
     * @param permissions 权限的名称
     * @return
     */
    public static boolean checkLocalPermissiion(Activity activity, String[] permissions) {
        permissionsIndex = permissions;
        if (checkGPSIsOpen(activity)) {
            return checkPermissions(activity);
        } else {
            Toast.makeText(activity, "需要打开GPS", Toast.LENGTH_SHORT).show();
            goToOpenGPS(activity);
        }
        return false;
    }

    /**
     * 检查GPS是否打开
     *
     */
    public static boolean checkGPSIsOpen(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    /**
     * 检查权限并申请权限
     */
    public static boolean checkPermissions(final Activity activity) {
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissionsIndex) {
            int permissionCheck = ContextCompat.checkSelfPermission(activity, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(activity, deniedPermissions, permission_LocationCode);
        }
        return false;
    }


    /**
     * 去手机设置打开GPS
     *
     * @param activity
     */
    public static void goToOpenGPS(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(intent, open_GPSCode);

    }
}
