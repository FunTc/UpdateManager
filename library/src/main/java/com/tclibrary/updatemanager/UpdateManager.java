package com.tclibrary.updatemanager;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.tclibrary.updatemanager.model.ConfigParams;
import com.tclibrary.updatemanager.model.IVersion;
import com.tclibrary.updatemanager.ui.AbsVersionDialog;
import com.tclibrary.updatemanager.ui.DefaultVersionDialog;
import com.tclibrary.updatemanager.utils.SPUtil;
import com.tclibrary.updatemanager.utils.Utils;

import java.io.File;
import java.util.NoSuchElementException;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Created by FunTc on 2020/08/02.
 */
public final class UpdateManager {
    
    private static IVersion VERSION_INFO;
    
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void setVersionInfo(IVersion versionInfo) {
        VERSION_INFO = versionInfo;
    }
    
    public static IVersion getVersionInfo() {
        return VERSION_INFO;
    }
    
    public static boolean isNeedUpdate() {
        return VERSION_INFO != null && VERSION_INFO.hasNewVersion();
    }


    private Context context;
    private IVersionChecker checker;
    private CheckedResultCallback resultCallback;
    private ConfigParams params;

    public UpdateManager(@NonNull Context context, @NonNull ConfigParams params, 
                         @NonNull IVersionChecker checker, CheckedResultCallback resultCallback) {
        this.context = context;
        this.checker = checker;
        this.resultCallback = resultCallback;
        this.params = params;
        if (this.params.versionDialogClz == null) {
            this.params.versionDialogClz = DefaultVersionDialog.class;
        }
    }

    public void start() {
        checker.check(this::handleResult);
    }
    
    private void handleResult(IVersion info) {
        VERSION_INFO = info;
        if (resultCallback != null) resultCallback.result(info);
        String ignoreVer = SPUtil.getApkVerName(context);
        if (!info.hasNewVersion() 
                || SPUtil.isIgnoreVersion(context)
                && TextUtils.equals(ignoreVer, info.getVersion())) return;
        checkDownloadPath();
        if (isCanSilentDownload()) {
            startSilentDownload();            
        } else {
            params.isSilentDownload = false;
            Utils.showVersionInfoDialog(context, info, params);
        }
    }
    
    private void checkDownloadPath() {
        if (params.downloadPath == null || params.downloadPath.length() == 0) {
            File dirFile = context.getExternalFilesDir(null);
            if (dirFile == null) {
                dirFile = context.getExternalFilesDir(null);
                if (dirFile == null) {
                    String path = "Android/data/" + context.getPackageName() + "/files";
                    dirFile = new File(Environment.getExternalStorageDirectory(), path);
                }
            }
            params.downloadPath = dirFile.getAbsolutePath();
        }
    }
    
    private boolean isCanSilentDownload() {
        return params.isSilentDownload 
                && Utils.hasStoragePermission(context)
                && (!params.isSilentDownloadWifiRequired || Utils.isWifiConnected(context));
    }
    
    private void startSilentDownload() {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constant.KEY_CONFIG_PARAMS, params);
        intent.putExtra(Constant.KEY_VERSION_INFO, VERSION_INFO);
        context.startService(intent);
    }

    
    public static class Builder {
        private final Context context;
        private IVersionChecker checker;
        private CheckedResultCallback resultCallback;
        private final ConfigParams params;
        
        public Builder(@NonNull Context context) {
            this.context = context;
            params = new ConfigParams();
        }

        /**
         * 设置检查版本信息的具体实现
         * @param checker IVersionChecker
         */
        public Builder setChecker(IVersionChecker checker) {
            this.checker = checker;
            return this;
        }
        
        /**
         * 默认路径为：/storage/emulated/0/Android/data/{packageName}/files/{fileName}.apk
         * @param path 下载的具体路径
         */
        public Builder setDownloadPath(String path) {
            params.downloadPath = path;
            return this;
        }
        
        public Builder setDownloadPath(File pathFile) {
            params.downloadPath = pathFile == null ? null : pathFile.getAbsolutePath();
            return this;
        }

        /**
         * 设置下载的时候是否显示通知栏，默认不显示
         */
        public Builder showNotification(boolean isShow) {
            params.isShowNotification = isShow;
            return this;
        }

        /**
         * 设置是否静默下载apk，是否能静默下载请看{@link UpdateManager#isCanSilentDownload()}
         */
        public Builder setSilentDownload(boolean isSilentDownload, boolean wifiRequired) {
            params.isSilentDownload = isSilentDownload;
            params.isSilentDownloadWifiRequired = wifiRequired;
            return this;
        }

        /**
         * @param isShow Dialog是否显示忽略此版本
         */
        public Builder setShowIgnoreVersion(boolean isShow) {
            params.dialogUIConfig.isShowIgnoreVersion = isShow;
            return this;
        }

        /**
         * 下载中是否显示dialog，与{@link #showNotification}都为false时互斥，则会显示Notification
         */
        public Builder showDialogInDownloading(boolean isShow) {
            params.dialogUIConfig.isShowDialogInDownloading = isShow;
            return this;
        }

        /**
         * 在下载的时候是否显示下载进度
         */
        public Builder showDownloadProgress(boolean isShow) {
            params.dialogUIConfig.isShowDownloadProgress = isShow;
            return this;
        }

        public Builder showBackgroundDownload(boolean isShow) {
            params.dialogUIConfig.isShowBackgroundDownload = isShow;
            return this;
        }

        /**
         * 当app位于前台，并且下载中显示弹窗，下载完成后是否自动跳转到安装页面
         * @param isJump 是否自动跳转
         */
        public Builder setAutoJumpToInstallInForeground(boolean isJump) {
            params.isAutoJumpToInstallInForeground = isJump;
            return this;
        }

        /**
         * 当app位于前台，并且下载中不显示弹窗，只显示通知，下载完成后是否自动跳转到安装页面
         * @param isJump 是否自动跳转
         */
        public Builder setAutoJumpToInstallWithoutDialog(boolean isJump) {
            params.isAutoJumpToInstallWithoutDialog = isJump;
            return this;
        }

        /**
         * 设置自定义的弹窗样式
         */
        public Builder setCustomDialog(Class<? extends AbsVersionDialog> dialogClz) {
            params.versionDialogClz = dialogClz;
            return this;
        }
        
        public Builder setDefaultDialogTheme(@DrawableRes int topImg, @ColorInt int themeColor) {
            params.defaultDialogTopImage = topImg;
            params.defaultDialogThemeColor = themeColor;
            return this;
        }

        /**
         * 设置检查后获取到版本信息的回调 
         */
        public Builder setCheckedResultCallback(CheckedResultCallback callback) {
            resultCallback = callback;
            return this;
        }
        
        public UpdateManager build() {
            if (checker == null) {
                throw new NoSuchElementException("you must invoke the method 'setChecker'");
            }
            return new UpdateManager(context, params, checker, resultCallback);
        }
        
    }
    
}
