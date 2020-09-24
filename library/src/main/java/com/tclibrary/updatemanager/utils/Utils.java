package com.tclibrary.updatemanager.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.tclibrary.updatemanager.Constant;
import com.tclibrary.updatemanager.model.ConfigParams;
import com.tclibrary.updatemanager.model.IVersion;
import com.tclibrary.updatemanager.ui.VersionDialogHostActivity;
import com.tclibrary.updatemanager.ui.VersionDialogHostFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.annotation.RestrictTo;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

/**
 * Created by FunTc on 2020/08/07.
 */
public final class Utils {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void showVersionInfoDialog(Context context, IVersion version, ConfigParams params) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.KEY_CONFIG_PARAMS, params);
        bundle.putSerializable(Constant.KEY_VERSION_INFO, version);
        if (context instanceof FragmentActivity) {
            VersionDialogHostFragment fragment = new VersionDialogHostFragment();
            fragment.setArguments(bundle);
            ((FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .add(fragment, VersionDialogHostFragment.class.getSimpleName())
                    .commitAllowingStateLoss();
        } else {
            Intent intent = new Intent(context, VersionDialogHostActivity.class);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(intent);
        }
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isWifiConnected(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivity == null) {
            return false;
        } else {
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }
    }

    public static Bitmap getAppIcon(Context context) {
        try {
            Drawable drawable = context.getPackageManager().getApplicationIcon(context.getPackageName());
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isAppForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningAppProcessInfo aInfo : info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (aInfo.processName.equals(context.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Intent getInstallAppIntent(Context context, File apkFile) {
        if (apkFile == null || !apkFile.exists()) return null;
        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(apkFile);
        } else {
            String authority = context.getPackageName() + ".fileProvider";
            uri = FileProvider.getUriForFile(context, authority, apkFile);
        }

        if (uri == null) return null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(uri, type);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    
    public static void installApp(Context context, File apkFile) {
        Intent intent = Utils.getInstallAppIntent(context, apkFile);
        if (intent != null) {
            context.startActivity(intent);
//            android.os.Process.killProcess(android.os.Process.myPid());  //如果没有这句 最后不会提示完成、打开
        }
    }

    public static String getFileMD5(String filePath) {
        return getFileMD5((filePath == null || filePath.length() == 0) ? null : new File(filePath));
    }
    
    public static String getFileMD5(final File file) {
        if (file == null) return "";
        DigestInputStream dis = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(fis, md);
            byte[] buffer = new byte[1024 * 256];
            while (true) {
                if (!(dis.read(buffer) > 0)) break;
            }
            md = dis.getMessageDigest();
            return bytes2HexString(md.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static String bytes2HexString(final byte[] bytes) {
        if (bytes == null) return "";
        int len = bytes.length;
        if (len <= 0) return "";
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = HEX_DIGITS[bytes[i] >> 4 & 0x0f];
            ret[j++] = HEX_DIGITS[bytes[i] & 0x0f];
        }
        return new String(ret);
    }
    
    public static boolean hasStoragePermission(@NonNull Context context) {
        return hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean hasPermissions(@NonNull Context context, @Size(min = 1) @NonNull String... perms) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    public static int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static Drawable createSolidButtonDrawable(int color, int cornerRadius) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        float ratio = 0.8F;
        red = (int) (red * ratio);
        green = (int) (green * ratio);
        blue = (int) (blue * ratio);
        int deepColor = Color.argb(alpha, red, green, blue);
        return createStatePressedDrawable(
                createSolidRectDrawable(color, cornerRadius),
                createSolidRectDrawable(deepColor, cornerRadius)
        );
    }

    public static Drawable createStrokeButtonDrawable(int strokeColor, int strokeWidth, int cornerRadius) {
        int solidColor = ColorUtils.setAlphaComponent(strokeColor, 37);
        return createStrokeButtonDrawable(solidColor, strokeColor, strokeWidth, cornerRadius);
    }

    public static Drawable createStrokeButtonDrawable(int solidColor, int strokeColor, int strokeWidth, int cornerRadius) {
        return createStatePressedDrawable(
                createStrokeRectDrawable(Color.TRANSPARENT, strokeColor, strokeWidth, cornerRadius),
                createStrokeRectDrawable(solidColor, strokeColor, strokeWidth, cornerRadius)
        );
    }

    public static Drawable createSolidRectDrawable(int color, int cornerRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(cornerRadius);
        drawable.setColor(color);
        return drawable;
    }

    public static GradientDrawable createStrokeRectDrawable(int solidColor, int strokeColor, int strokeWidth, int cornerRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setStroke(strokeWidth, strokeColor);
        drawable.setColor(solidColor);
        drawable.setCornerRadius(cornerRadius);
        return drawable;
    }

    public static Drawable createStatePressedDrawable(Drawable normal, Drawable pressed) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed}, pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, normal);
        return drawable;
    }

}
