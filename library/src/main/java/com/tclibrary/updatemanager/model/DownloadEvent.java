package com.tclibrary.updatemanager.model;

import java.io.File;

import androidx.annotation.RestrictTo;

/**
 * Created by FunTc on 2020/08/14.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DownloadEvent {
    
    public static final int START       = 0;
    public static final int FINISH      = 1;
    public static final int FAILED      = 2;
    public static final int PROGRESS    = 3;
    
    private int event;
    
    private File downloadFile;
    private Throwable exception;
    private boolean isCancel;
    
    private int progress;
    private long currBytes;
    private long totalBytes;

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public File getDownloadFile() {
        return downloadFile;
    }

    public void setDownloadFile(File downloadFile) {
        this.downloadFile = downloadFile;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getCurrBytes() {
        return currBytes;
    }

    public void setCurrBytes(long currBytes) {
        this.currBytes = currBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }
}
