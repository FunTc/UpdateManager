package com.tclibrary.updatemanager;

import androidx.annotation.NonNull;

/**
 * Created by FunTc on 2020/08/07.
 */
public interface IVersionChecker {
    
    void check(@NonNull CheckedResultCallback callback);
    
}
