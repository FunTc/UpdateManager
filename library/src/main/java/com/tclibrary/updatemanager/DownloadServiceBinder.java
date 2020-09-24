package com.tclibrary.updatemanager;

import android.os.Binder;

import com.liulishuo.okdownload.DownloadTask;

import androidx.annotation.RestrictTo;

/**
 * Created by FunTc on 2020/08/11.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DownloadServiceBinder extends Binder {

    private DownloadService downloadService;
    private DownloadTask downloadTask;

    DownloadServiceBinder(DownloadService downloadService) {
        this.downloadService = downloadService;
    }
    
    public DownloadService getDownloadService() {
        return downloadService;
    }
    
    public void setDownloadTask(DownloadTask task) {
        downloadTask = task;
    }
    
    public DownloadTask getDownloadTask() {
        return downloadTask;
    }
    
    
}
