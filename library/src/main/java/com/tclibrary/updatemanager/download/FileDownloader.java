package com.tclibrary.updatemanager.download;

import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;

/**
 * Created by FunTc on 2020/08/10.
 */
public final class FileDownloader {

    private static final int TIMEOUT = 30000;
    private static final int BUFFERED_READER_SIZE = 8192;

    private final String url;
    private File destFile;
    private final FileDownloadListener listener;
    private final Handler mainHandler;
    private Future<?> downloadTask;
    private AtomicBoolean cancelFlag;


    public static FileDownloader download(String url, File destFile, FileDownloadListener listener) {
        return new FileDownloader(url, destFile, listener);
    }
    
    private FileDownloader(@NonNull String url, @NonNull File destFile, FileDownloadListener listener) {
        this.url = url;
        this.destFile = destFile;
        this.listener = listener;
        mainHandler = new Handler();
        cancelFlag = new AtomicBoolean();
    }
    
    private boolean checkFile() throws IOException {
        if (destFile.exists()) {
            if (destFile.isFile()) {
                return true;
            } else {
                destFile = new File(destFile, "updateApp.apk");
                return checkFile();
            }
        } else {
            File parentDirFile = destFile.getParentFile();
            if (parentDirFile != null && parentDirFile.mkdirs()) {
                return destFile.createNewFile();
            }
            return destFile.createNewFile();
        }
    }

    public FileDownloader start() {
        try {
            if (!checkFile()) {
                throw new IOException("Create file failed");
            }
        } catch (Exception e) {
            notifyFailed(e, false);
            return this;
        }
        notifyStart();
        downloadTask = Executors.newSingleThreadExecutor().submit((Runnable) this::download);
        return this;
    }
    
    private void download() {
        HttpURLConnection client = null;
        InputStream responseIs = null;
        FileOutputStream fos = null;
        int statusCode;
        boolean success;
        long contentLen;

        try {
            client = (HttpURLConnection) new URL(url).openConnection();
            client.setConnectTimeout(TIMEOUT);
            client.setReadTimeout(TIMEOUT);
            client.setDoInput(true);
            client.setRequestMethod("GET");

            statusCode = client.getResponseCode();
            success = client.getResponseCode() == HttpURLConnection.HTTP_OK;
            
            if (cancelFlag.get()) {
                client.disconnect();
                mainHandler.post(() -> listener.onFailed(null, true));
                return;
            }

            if (success) {
                contentLen = client.getContentLength();
                if (!isExternalStorageSpaceEnough(destFile.getPath(), contentLen)) {
                    client.disconnect();
                    notifyFailed(new IOException("external storage space is not enough"), false);
                    return;
                }

                responseIs = client.getInputStream();
                byte[] buffer = new byte[BUFFERED_READER_SIZE];
                fos = new FileOutputStream(destFile);
                long readLen = 0;
                int lastProgress = 0;
                int length;
                final ProgressData progressData = new ProgressData();
                Runnable progressRunnable = () -> 
                        listener.onProgress(progressData.progress, progressData.current, progressData.total);
                while ((length = responseIs.read(buffer)) != -1 && !cancelFlag.get()) {
                    fos.write(buffer, 0, length);
                    readLen += length;
                    int progress = (int) (readLen * 100 / contentLen);
                    if (listener != null && progress != lastProgress) {
                        progressData.setData(progress, readLen, contentLen);
                        mainHandler.post(progressRunnable);
                        lastProgress = progress;
                    }
                }
                
                if (cancelFlag.get()) {
                    fos.close();
                    responseIs.close();
                    notifyFailed(null, true);
                    return;
                }
                
                fos.flush();
                if (listener != null) {
                    mainHandler.post(() -> {
                        listener.onProgress(100, contentLen, contentLen);
                        listener.onSuccess(destFile);
                    });
                }
            } else {
                throw new Exception("http status exception. code = " + statusCode);
            }
        } catch (Exception e) {
            notifyFailed(e, false);
        } finally {
            try {
                if (fos != null) fos.close();
                if (responseIs != null) responseIs.close();
                if (client != null) client.disconnect();
            } catch (IOException e) {
                notifyFailed(e, false);
            }
        }
    }

    public boolean isDownloading() {
        if (downloadTask == null) return false;
        return !downloadTask.isDone();
    }

    public void cancel() {
        if (isDownloading()) {
            cancelFlag.set(true);
            downloadTask.cancel(true);
        }
    }

    private boolean isMainThread() {
        return Looper.getMainLooper() == mainHandler.getLooper();
    }

    private void notifyFailed(Throwable t, boolean isCancel) {
        if (listener == null) return;
        if (isMainThread()) {
            listener.onFailed(t, isCancel);
        } else {
            mainHandler.post(() -> listener.onFailed(t, isCancel));
        }
    }
    
    private void notifyStart() {
        if (listener == null) return;
        if (isMainThread()) {
            listener.onStart();
        } else {
            mainHandler.post(listener::onStart);
        }
    }

    private static boolean isExternalStorageSpaceEnough(String dirPath, long fileSize) {
        StatFs statFs = new StatFs(dirPath);
        return statFs.getAvailableBytes() > fileSize;
    }
    
    private static class ProgressData {
        int progress;
        long current;
        long total;
        
        void setData(int progress, long current, long total) {
            this.progress = progress;
            this.current = current;
            this.total = total;
        }
    }
    
}
