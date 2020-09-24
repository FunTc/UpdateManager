package com.tclibrary.updatemanager.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.tclibrary.updatemanager.ui.AbsVersionDialog;

import androidx.annotation.RestrictTo;

/**
 * Created by FunTc on 2020/08/18.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ConfigParams implements Parcelable{

    public String downloadPath;
    public boolean isShowNotification;
    public boolean isSilentDownload;
    public boolean isSilentDownloadWifiRequired;
    public DialogUIConfig dialogUIConfig;
    public Class<? extends AbsVersionDialog> versionDialogClz;
    public boolean isAutoJumpToInstallInForeground;
    public boolean isAutoJumpToInstallWithoutDialog;

    public int defaultDialogTopImage;
    public int defaultDialogThemeColor;
    
    public ConfigParams() {
        dialogUIConfig = new DialogUIConfig();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.downloadPath);
        dest.writeByte(this.isShowNotification ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isSilentDownload ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isSilentDownloadWifiRequired ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.dialogUIConfig, flags);
        dest.writeSerializable(this.versionDialogClz);
        dest.writeByte(this.isAutoJumpToInstallInForeground ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isAutoJumpToInstallWithoutDialog ? (byte) 1 : (byte) 0);
        dest.writeInt(this.defaultDialogTopImage);
        dest.writeInt(this.defaultDialogThemeColor);
    }

    protected ConfigParams(Parcel in) {
        this.downloadPath = in.readString();
        this.isShowNotification = in.readByte() != 0;
        this.isSilentDownload = in.readByte() != 0;
        this.isSilentDownloadWifiRequired = in.readByte() != 0;
        this.dialogUIConfig = in.readParcelable(DialogUIConfig.class.getClassLoader());
        this.versionDialogClz = (Class<? extends AbsVersionDialog>) in.readSerializable();
        this.isAutoJumpToInstallInForeground = in.readByte() != 0;
        this.isAutoJumpToInstallWithoutDialog = in.readByte() != 0;
        this.defaultDialogTopImage = in.readInt();
        this.defaultDialogThemeColor = in.readInt();
    }

    public static final Creator<ConfigParams> CREATOR = new Creator<ConfigParams>() {
        @Override
        public ConfigParams createFromParcel(Parcel source) {
            return new ConfigParams(source);
        }

        @Override
        public ConfigParams[] newArray(int size) {
            return new ConfigParams[size];
        }
    };
}
