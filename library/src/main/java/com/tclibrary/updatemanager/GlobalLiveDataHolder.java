package com.tclibrary.updatemanager;

import com.tclibrary.updatemanager.model.DownloadEvent;

import androidx.annotation.RestrictTo;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by FunTc on 2020/08/14.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class GlobalLiveDataHolder {

    private static class InstanceHolder {
        private static final GlobalLiveDataHolder INSTANCE = new GlobalLiveDataHolder();
    }

    private GlobalLiveDataHolder() {}

    public static GlobalLiveDataHolder instance() {
        return GlobalLiveDataHolder.InstanceHolder.INSTANCE;
    }


    private SingleLiveEvent<DownloadEvent> downloadEvent;

    public MutableLiveData<DownloadEvent> getDownloadEvent() {
        if (downloadEvent == null) {
            downloadEvent = new SingleLiveEvent<>();
        }
        return downloadEvent;
    }

}
