package com.tclibrary.updatemanager.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RestrictTo;

/**
 * Created by FunTc on 2020/08/12.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DialogUIConfig implements Parcelable {

    public boolean isShowIgnoreVersion;
    public boolean isShowDownloadProgress = true;
    public boolean isShowDialogInDownloading = true;
    public boolean isShowBackgroundDownload;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isShowIgnoreVersion ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowDownloadProgress ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowDialogInDownloading ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowBackgroundDownload ? (byte) 1 : (byte) 0);
    }

    public DialogUIConfig() {
    }

    protected DialogUIConfig(Parcel in) {
        this.isShowIgnoreVersion = in.readByte() != 0;
        this.isShowDownloadProgress = in.readByte() != 0;
        this.isShowDialogInDownloading = in.readByte() != 0;
        this.isShowBackgroundDownload = in.readByte() != 0;
    }

    public static final Creator<DialogUIConfig> CREATOR = new Creator<DialogUIConfig>() {
        @Override
        public DialogUIConfig createFromParcel(Parcel source) {
            return new DialogUIConfig(source);
        }

        @Override
        public DialogUIConfig[] newArray(int size) {
            return new DialogUIConfig[size];
        }
    };
}
