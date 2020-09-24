package com.tclibrary.updatemanager.model;

import java.io.Serializable;

import androidx.annotation.Nullable;

/**
 * Created by FunTc on 2020/08/02.
 */
public interface IVersion extends Serializable {

    /** 是否有新版本 */
    boolean hasNewVersion();

    /** 版本号 */
    @Nullable String getVersion();

    /** 版本信息 */
    @Nullable String getLog();

    /** 是否需要强制更新 */
    boolean isMust();

    String getApkUrl();

    @Nullable String getApkSize();

    @Nullable String getApkMD5();
    
}
