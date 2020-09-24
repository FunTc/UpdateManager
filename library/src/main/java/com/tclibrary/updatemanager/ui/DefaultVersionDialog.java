package com.tclibrary.updatemanager.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.tclibrary.updatemanager.Constant;
import com.tclibrary.updatemanager.R;
import com.tclibrary.updatemanager.UpdateManager;
import com.tclibrary.updatemanager.model.ConfigParams;
import com.tclibrary.updatemanager.model.IVersion;
import com.tclibrary.updatemanager.utils.Utils;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by FunTc on 2020/08/07.
 */
public class DefaultVersionDialog extends AbsVersionDialog implements View.OnClickListener {
    
    protected ImageView mIvTopImg;
    protected TextView mTvTitle;
    protected TextView mTvUpdateLog;
    protected TextView mBtnUpdate;
    protected TextView mBtnIgnore;
    protected View mBtnClose;
    protected NumberProgressBar mProgressBar;
    protected TextView mBtnCancelDownload;
    protected TextView mBtnDownloadInBg;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.UMDialog);
        
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_update_version_default, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIvTopImg = view.findViewById(R.id.ivTopImg);
        mTvTitle = view.findViewById(R.id.tvTitle);
        mTvUpdateLog = view.findViewById(R.id.tvUpdateLog);
        mBtnUpdate = view.findViewById(R.id.btnUpdate);
        mBtnIgnore = view.findViewById(R.id.btnIgnore);
        mBtnClose = view.findViewById(R.id.ivClose);
        mProgressBar = view.findViewById(R.id.progressBar);
        mBtnCancelDownload = view.findViewById(R.id.btnCancelDownload);
        mBtnDownloadInBg = view.findViewById(R.id.btnDownloadInBg);

        mBtnUpdate.setOnClickListener(this);
        mBtnIgnore.setOnClickListener(this);
        mBtnClose.setOnClickListener(this);
        mBtnCancelDownload.setOnClickListener(this);
        mBtnDownloadInBg.setOnClickListener(this);
        
        initView();
        initTheme();
    }
    
    @SuppressLint("SetTextI18n")
    protected void initView() {
        IVersion info = UpdateManager.getVersionInfo();
        mTvTitle.setText("发现新版本（" + info.getVersion() + "）");
        mTvUpdateLog.setText(info.getLog());
        if (info.isMust()) {
            mBtnClose.setVisibility(View.GONE);
        } else if (mDialogUIConfig.isShowIgnoreVersion) {
            mBtnIgnore.setVisibility(View.VISIBLE);
        }
    }
    
    protected void initTheme() {
        ConfigParams params = requireArguments().getParcelable(Constant.KEY_CONFIG_PARAMS);
        int topImg = 0;
        int color = 0;
        if (params != null) {
            topImg = params.defaultDialogTopImage;
            color = params.defaultDialogThemeColor;
        }
        if (topImg != 0) {
            mIvTopImg.setImageResource(topImg);
        }
        if (color != 0) {
            mBtnUpdate.setBackground(Utils.createSolidButtonDrawable(color, Utils.dp2px(20)));
            mBtnIgnore.setBackground(Utils.createStrokeButtonDrawable(color, Utils.dp2px(1), Utils.dp2px(20)));
            mBtnIgnore.setTextColor(color);
            mProgressBar.setProgressTextColor(color);
            mProgressBar.setReachedBarColor(color);
            mBtnDownloadInBg.setTextColor(color);
            mBtnCancelDownload.setTextColor(color);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnUpdate) {
            if (mPreparedApk != null) {
                mEventController.clickInstall();
            } else {
                mEventController.clickUpdate();
            }
        } else if (v == mBtnIgnore) {
            mEventController.clickIgnore();
        } else if (v == mBtnClose) {
            mEventController.clickClose();
        } else if (v == mBtnCancelDownload) {
            mEventController.clickCancelDownload();
        } else if (v == mBtnDownloadInBg) {
            mEventController.clickDownloadInBackground();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false);
        }
    }

    @Override
    protected void onDownloadStart() {
        super.onDownloadStart();
        if (mDialogUIConfig.isShowDialogInDownloading || mVersionInfo.isMust()) {
            mBtnUpdate.setVisibility(View.GONE);
            mBtnIgnore.setVisibility(View.GONE);
            mBtnClose.setVisibility(View.GONE);
            if (mDialogUIConfig.isShowDownloadProgress) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            if (!mVersionInfo.isMust() && mDialogUIConfig.isShowBackgroundDownload) {
                mBtnDownloadInBg.setVisibility(View.VISIBLE);
            }
            mBtnCancelDownload.setVisibility(View.VISIBLE);
        } else {
            dismiss();
        }
    }

    @Override
    protected void onDownloadProgress(int progress, long current, long total) {
        super.onDownloadProgress(progress, current, total);
        if (mDialogUIConfig.isShowDownloadProgress) {
            mProgressBar.setProgress(progress);
        }
    }

    @Override
    protected void onDownloadFailed(Throwable t, boolean isCancel) {
        super.onDownloadFailed(t, isCancel);
        String msg;
        if (isCancel) {
            msg = "下载取消";
        } else {
            msg = "下载失败";
        }
        Toast.makeText(requireContext().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Override
    protected void onDownloadFinish(File file) {
        super.onDownloadFinish(file);
        if (mBtnUpdate != null) {
            mProgressBar.setVisibility(View.GONE);
            mBtnCancelDownload.setVisibility(View.GONE);
            mBtnDownloadInBg.setVisibility(View.GONE);
            mBtnUpdate.setText("安装");
            mBtnUpdate.setVisibility(View.VISIBLE);
        }
    }
}
