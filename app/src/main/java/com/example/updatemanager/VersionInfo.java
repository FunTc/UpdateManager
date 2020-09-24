package com.example.updatemanager;

import com.tclibrary.updatemanager.model.IVersion;

import androidx.annotation.NonNull;

/**
 * Created by FunTc on 2020/08/02.
 */
public class VersionInfo implements IVersion {

    /** 是否有新版本 */
    private boolean hasNewVersion;
    /** 版本号 */
    private String version;
    /** 版本信息 */
    private String log;
    /** 是否需要强制更新 */
    private boolean isMust;
    private String apkUrl;
    private String apkSize;
    private String apkMD5;


    @Override
    public boolean hasNewVersion() {
        return hasNewVersion;
    }

    public void setHasNewVersion(boolean hasNewVersion) {
        this.hasNewVersion = hasNewVersion;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public boolean isMust() {
        return isMust;
    }

    public void setMust(boolean must) {
        isMust = must;
    }

    @Override
    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    @Override
    public String getApkSize() {
        return apkSize;
    }

    public void setApkSize(String apkSize) {
        this.apkSize = apkSize;
    }

    @Override
    public String getApkMD5() {
        return apkMD5;
    }

    public void setApkMD5(String apkMD5) {
        this.apkMD5 = apkMD5;
    }

    @NonNull
    @Override
    public String toString() {
        return "VersionInfo{" +
                "hasNewVersion=" + hasNewVersion +
                ", version='" + version + '\'' +
                ", log='" + log + '\'' +
                ", isMust=" + isMust +
                ", apkUrl='" + apkUrl + '\'' +
                ", apkSize='" + apkSize + '\'' +
                ", apkMD5='" + apkMD5 + '\'' +
                '}';
    }
}
