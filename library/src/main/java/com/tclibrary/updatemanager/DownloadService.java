package com.tclibrary.updatemanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;
import com.tclibrary.updatemanager.model.ConfigParams;
import com.tclibrary.updatemanager.model.DownloadEvent;
import com.tclibrary.updatemanager.model.IVersion;
import com.tclibrary.updatemanager.utils.SPUtil;
import com.tclibrary.updatemanager.utils.Utils;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/** 
 * Created by FunTc on 2020/08/10.
 */
public class DownloadService extends Service {

    private static final int NOTIFY_ID              = 13213;
    private static final String CHANNEL_ID          = "app_update_channel_id";
    private static final String CHANNEL_NAME        = "AppUpdate";
    private static final String NOTIFY_TITLE        = "下载新版本";
    
    
    private DownloadServiceBinder mBinder;
    private DownloadEvent mDownloadEvent;
    private IVersion mVersionInfo;
    private boolean mIsShowNotification;
    private DownloadTask mDownloadTask;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private ConfigParams mParams;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        mBinder = new DownloadServiceBinder(this);
        mDownloadEvent = new DownloadEvent();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mVersionInfo = (IVersion) intent.getSerializableExtra(Constant.KEY_VERSION_INFO);
        mParams = intent.getParcelableExtra(Constant.KEY_CONFIG_PARAMS);
        if (mParams == null) {
            mParams = new ConfigParams();
        }
        mIsShowNotification = mParams.isShowNotification || !mParams.dialogUIConfig.isShowDialogInDownloading;
        
        if (mParams.isSilentDownload) {
            startDownload();
        }
        
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mBinder = null;
        super.onDestroy();
    }
    

    public void startDownload() {
        if (mDownloadTask != null && StatusUtil.isCompleted(mDownloadTask)) {
            File destFile = mDownloadTask.getFile();
            Utils.installApp(this, destFile);
        } else {
            download();
        }
    }
    
    private void download() {
        File destFile = new File(mParams.downloadPath);
        DownloadTask.Builder builder;
        if (destFile.isFile()) {
            builder = new DownloadTask.Builder(mVersionInfo.getApkUrl(), destFile);
        } else {
            builder = new DownloadTask.Builder(mVersionInfo.getApkUrl(), mParams.downloadPath, null);
        }
        mDownloadTask = builder.setMinIntervalMillisCallbackProcess(200)
                /*
                  在android10的设备上会报异常（The current offset on block-info isn't update correct,）导致下载无法正常完成 
                  按照别人的解决方法在 MultiPointOutputStream.close(int blockIndex) 添加 noSyncLengthMap.remove(blockIndex) 后异常不会必现，但偶尔还是会出现
                  最终解决方法：https://github.com/lingochamp/okdownload/issues/385
                */
                .setConnectionCount(1)
                .build();
        mDownloadTask.enqueue(mDownloadListener);
    }
    
    public void cancelDownload() {
        if (mDownloadTask != null && !StatusUtil.isCompleted(mDownloadTask)) {
            mDownloadTask.cancel();
            clearNotification();
            stopSelf();
        }
    }
    
    public void downloadInBackground() {
        if (mNotificationBuilder == null) {
            mIsShowNotification = true;
            createNotification(true);
        }
        mParams.dialogUIConfig.isShowDialogInDownloading = false;
    }
    
    public void clearNotification() {
        if (mNotificationBuilder != null) {
            mNotificationManager.cancel(NOTIFY_ID);
        }
    }
    
    
    private DownloadListener1 mDownloadListener = new DownloadListener1() {
        @Override
        public void taskStart(@NonNull DownloadTask task, @NonNull Listener1Assist.Listener1Model model) {
            if (!mParams.isSilentDownload) {
                createNotification(false);
                
                mDownloadEvent.setEvent(DownloadEvent.START);
                GlobalLiveDataHolder.instance().getDownloadEvent().setValue(mDownloadEvent);
            }
        }

        @Override
        public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) { }

        @Override
        public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) { }

        @Override
        public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
            int progress = (int) (currentOffset * 100 / totalLength);
            if (!mParams.isSilentDownload) {
                notifyDownloadProgress(progress);
                
                mDownloadEvent.setEvent(DownloadEvent.PROGRESS);
                mDownloadEvent.setProgress(progress);
                mDownloadEvent.setCurrBytes(currentOffset);
                mDownloadEvent.setTotalBytes(totalLength);
                GlobalLiveDataHolder.instance().getDownloadEvent().setValue(mDownloadEvent);
            }
        }

        @Override
        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {
            File downloadFile = task.getFile();
            if (mParams.isSilentDownload) {
                if (cause == EndCause.COMPLETED && downloadFile != null) {
                    SPUtil.saveDownloadApkPath(DownloadService.this, downloadFile.getPath());
                    SPUtil.saveApkVerName(DownloadService.this, mVersionInfo.getVersion());
                    Utils.showVersionInfoDialog(DownloadService.this, mVersionInfo, mParams);
                }
                stopSelf();
            } else {
                if (cause == EndCause.COMPLETED && downloadFile != null) {
                    notifyDownloadComplete(downloadFile);

                    SPUtil.saveDownloadApkPath(DownloadService.this, downloadFile.getPath());
                    SPUtil.saveApkVerName(DownloadService.this, mVersionInfo.getVersion());

                    mDownloadEvent.setEvent(DownloadEvent.FINISH);
                    mDownloadEvent.setDownloadFile(downloadFile);
                } else if (cause == EndCause.CANCELED) {
                    notifyDownloadError(null, true);
                    mDownloadEvent.setEvent(DownloadEvent.FAILED);
                    mDownloadEvent.setCancel(true);
                } else {
                    notifyDownloadError("下载出错", false);
                    mDownloadEvent.setEvent(DownloadEvent.FAILED);
                    mDownloadEvent.setCancel(false);
                    mDownloadEvent.setException(realCause);
                }
                GlobalLiveDataHolder.instance().getDownloadEvent().setValue(mDownloadEvent);
//                if (!mIsShowNotification) {
//                }
                stopSelf();
            }
        }
    };
    
    private void createNotification(boolean isStartBackground) {
        if (!mIsShowNotification) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            channel.enableLights(false);
            mNotificationManager.createNotificationChannel(channel);
        }
        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        mNotificationBuilder.setContentTitle(NOTIFY_TITLE)
                .setContentText(isStartBackground ? "后台下载中" : "等待下载中")
                .setSmallIcon(R.mipmap.ic_notification_download)
                .setLargeIcon(Utils.getAppIcon(this))
                .setOngoing(true)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        mNotificationManager.notify(NOTIFY_ID, mNotificationBuilder.build());
    }
    
    private void notifyDownloadError(String msg, boolean isCancel) {
        if (mNotificationBuilder != null) {
            if (isCancel) {
                mNotificationManager.cancel(NOTIFY_ID);
            } else {
                mNotificationBuilder.setContentTitle(NOTIFY_TITLE).setContentText(msg);
                Notification notification = mNotificationBuilder.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(NOTIFY_ID, notification);
            }
        }
    }
    
    private void notifyDownloadProgress(int progress) {
        if (mNotificationBuilder != null) {
            mNotificationBuilder.setContentTitle(NOTIFY_TITLE)
                    .setContentText(progress + "%")
                    .setProgress(100, progress, false)
                    .setWhen(System.currentTimeMillis());
            Notification notification = mNotificationBuilder.build();
            notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONLY_ALERT_ONCE;
            mNotificationManager.notify(NOTIFY_ID, notification);
        }
    }
    
    private void notifyDownloadComplete(File apkFile) {
        if (Utils.isAppForeground(this)) {
            /* 有弹窗显示，就清除通知 */
            if (mIsShowNotification && mParams.dialogUIConfig.isShowDialogInDownloading) {
                mNotificationManager.cancel(NOTIFY_ID);
            }

            if (mParams.isAutoJumpToInstallInForeground && mParams.dialogUIConfig.isShowDialogInDownloading) {
                mNotificationManager.cancel(NOTIFY_ID);
                /* 有弹窗的时候，需要自动跳转到安装 */
                Utils.installApp(this, apkFile);
            } else if (mParams.isAutoJumpToInstallWithoutDialog && mIsShowNotification) {
                mNotificationManager.cancel(NOTIFY_ID);
                /* 只有通知的时候，需要自动跳转到安装 */
                Utils.installApp(this, apkFile);
            } else if (!mParams.isAutoJumpToInstallWithoutDialog && !mParams.dialogUIConfig.isShowDialogInDownloading) {
                setDownloadCompleteNotification(apkFile);
            }
        } else if (mNotificationBuilder != null) {
            setDownloadCompleteNotification(apkFile);
        }
    }
    
    private void setDownloadCompleteNotification(File apkFile) {
        Intent installAppIntent = Utils.getInstallAppIntent(this, apkFile);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, installAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(contentIntent)
                .setContentTitle(NOTIFY_TITLE)
                .setContentText("下载完成，请点击安装")
                .setProgress(0, 0, false)
                .setDefaults((Notification.DEFAULT_ALL));
        Notification notification = mNotificationBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFY_ID, notification);
    }
    
    
    
}
