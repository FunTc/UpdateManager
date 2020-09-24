package com.tclibrary.updatemanager;

import com.tclibrary.updatemanager.model.IVersion;

import androidx.annotation.NonNull;

/**
 * Created by FunTc on 2020/08/06.
 */
public interface CheckedResultCallback {
    
    void result(@NonNull IVersion info);
    
}
