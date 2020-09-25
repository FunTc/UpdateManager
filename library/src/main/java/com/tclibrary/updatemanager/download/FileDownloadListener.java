package com.tclibrary.updatemanager.download;

import java.io.File;

/**
 * Created by FunTc on 2020/08/10.
 */
public interface FileDownloadListener {
    
    void onSuccess(File file);
    void onFailed(Throwable t, boolean isCancel);
    void onProgress(int progress, long current, long total);
    void onStart();
}
