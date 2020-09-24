package com.tclibrary.updatemanager.ui;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tclibrary.updatemanager.Constant;
import com.tclibrary.updatemanager.DownloadService;
import com.tclibrary.updatemanager.DownloadServiceBinder;
import com.tclibrary.updatemanager.GlobalLiveDataHolder;
import com.tclibrary.updatemanager.R;
import com.tclibrary.updatemanager.model.ConfigParams;
import com.tclibrary.updatemanager.model.DialogUIConfig;
import com.tclibrary.updatemanager.model.DownloadEvent;
import com.tclibrary.updatemanager.model.IVersion;
import com.tclibrary.updatemanager.utils.SPUtil;
import com.tclibrary.updatemanager.utils.Utils;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by FunTc on 2020/08/13.
 */
public abstract class AbsVersionDialog extends DialogFragment {

    private static final int REQUEST_PERMISSION_CODE = 123;
    
    protected IVersion mVersionInfo;
    protected DialogUIConfig mDialogUIConfig;
    private ServiceConnection mServiceConnection;
    private DownloadServiceBinder mServiceBinder;
    protected File mPreparedApk;
    private OnDialogClosedListener mListener;

    protected DialogEventController mEventController = new DialogEventController() {

        @Override
        public void clickUpdate() {
            startUpdate();
        }

        @Override
        public void clickInstall() {
            startInstall();
        }

        @Override
        public void clickClose() {
            dismiss();
        }

        @Override
        public void clickIgnore() {
            SPUtil.saveApkVerName(requireContext(), mVersionInfo.getVersion());
            SPUtil.setIgnoreVersion(requireContext());
            clickClose();
        }

        @Override
        public void clickCancelDownload() {
            stopDownload();
        }

        @Override
        public void clickDownloadInBackground() {
            downloadInBackground();
        }
    };
    
    public AbsVersionDialog() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = requireArguments();
        mVersionInfo = (IVersion) data.getSerializable(Constant.KEY_VERSION_INFO);
        ConfigParams params = data.getParcelable(Constant.KEY_CONFIG_PARAMS);
        if (params != null) {
            mDialogUIConfig = params.dialogUIConfig;
        } else {
            mDialogUIConfig = new DialogUIConfig();
        }
        mPreparedApk = getPreparedApk();
    }

    @Override
    public void onDestroy() {
        if (mServiceConnection != null) {
            requireContext().unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        mListener = null;
        super.onDestroy();
    }

    private void startDownloadService() {
        Intent intent = new Intent(requireContext(), DownloadService.class);
        ConfigParams params = requireArguments().getParcelable(Constant.KEY_CONFIG_PARAMS);
        Log.e("tian", params == null ? "null" : "not null");
        intent.putExtras(requireArguments());
        requireContext().startService(intent);
    }
    
    private void bindDownloadService() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServiceBinder = (DownloadServiceBinder) service;
                mServiceBinder.getDownloadService().startDownload();
                if (mDialogUIConfig.isShowDialogInDownloading || mVersionInfo.isMust()) {
                    setDownloadEventCallback();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceBinder = null;
            }
        };
        Intent intent = new Intent(requireContext(), DownloadService.class);
        requireContext().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }
    
    private void setDownloadEventCallback() {
        GlobalLiveDataHolder.instance().getDownloadEvent().observe(this, downloadEvent -> {
            int event = downloadEvent.getEvent();
            if (event == DownloadEvent.PROGRESS) {
                onDownloadProgress(downloadEvent.getProgress(), downloadEvent.getCurrBytes(), downloadEvent.getTotalBytes());
            } else if (event == DownloadEvent.START) {
                onDownloadStart();
            } else if (event == DownloadEvent.FINISH) {
                onDownloadFinish(downloadEvent.getDownloadFile());
            } else if (event == DownloadEvent.FAILED) {
                onDownloadFailed(downloadEvent.getException(), downloadEvent.isCancel());
            }
        });
    }

    private void startUpdate() {
        if (Utils.hasStoragePermission(requireContext())) {
            startDownloadService();
            bindDownloadService();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }
    
    private void startInstall() {
        if (mServiceBinder != null) {
            mServiceBinder.getDownloadService().clearNotification();
        }
        if (mPreparedApk != null) {
            Utils.installApp(requireContext(), mPreparedApk);
        }
    }
    
    private void stopDownload() {
        if (mServiceBinder != null) {
            mServiceBinder.getDownloadService().cancelDownload();
        }
    }
    
    private void downloadInBackground() {
        if (mServiceBinder != null) {
            mServiceBinder.getDownloadService().downloadInBackground();
        }
        dismiss();
    }
    

    /**
     * @return apk文件是否已经准备好（新版文件已经下载好，MD5验证通过）
     */
    protected File getPreparedApk() {
        String newVerName = mVersionInfo.getVersion();
        String newApkMD5 = mVersionInfo.getApkMD5();
        if (!TextUtils.equals(newVerName, SPUtil.getApkVerName(requireContext()))
                || TextUtils.isEmpty(newApkMD5)) return null;
        String apkPath = SPUtil.getDownloadApkPath(requireContext());
        if (TextUtils.isEmpty(apkPath)) return null;
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) return null;
        String apkMD5 = Utils.getFileMD5(apkFile);
        return TextUtils.equals(newApkMD5.toUpperCase(), apkMD5) ? apkFile : null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) mListener.onClosed(mVersionInfo.isMust());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                grantedStoragePermission();
            } else {
                deniedStoragePermission();
            }
        }
    }
    
    private void grantedStoragePermission() {
        startDownloadService();
        bindDownloadService();
    }
    
    protected void deniedStoragePermission() {
        Toast.makeText(requireContext(), R.string.um_delay_storage_permission_toast, Toast.LENGTH_LONG).show();
    }
    
    
    public void setOnDialogClosedListener(OnDialogClosedListener listener) {
        mListener = listener;
    }

    protected void onDownloadStart() { }

    protected void onDownloadFinish(File file) {
        mPreparedApk = file;
    }

    protected void onDownloadFailed(Throwable t, boolean isCancel) { }

    protected void onDownloadProgress(int progress, long current, long total) {}
    
    
}
