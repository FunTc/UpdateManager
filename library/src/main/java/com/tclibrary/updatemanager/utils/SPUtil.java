package com.tclibrary.updatemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.RestrictTo;

/**
 * Created by FunTc on 2020/08/17.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class SPUtil {
    
    private final static String SP_NAME     = "update_manager_sp";
    
    private static SharedPreferences getSp(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }
    
    public static void saveDownloadApkPath(Context context, String apkPath) {
        getSp(context).edit().putString("apk_path", apkPath).apply();
    }
    
    public static String getDownloadApkPath(Context context) {
        return getSp(context).getString("apk_path", null);
    }
    
    public static void saveApkVerName(Context context, String verName) {
        getSp(context).edit().putString("apk_version", verName).apply();
    }

    public static String getApkVerName(Context context) {
        return getSp(context).getString("apk_version", null);
    }
    
    public static void setIgnoreVersion(Context context) {
        getSp(context).edit().putBoolean("ignore_version", true).apply();
    }
    
    public static boolean isIgnoreVersion(Context context) {
        return getSp(context).getBoolean("ignore_version", false);
    }
    
}
