package com.tclibrary.updatemanager.ui;

import android.os.Bundle;

import com.tclibrary.updatemanager.Constant;
import com.tclibrary.updatemanager.UpdateManager;
import com.tclibrary.updatemanager.model.ConfigParams;
import com.tclibrary.updatemanager.model.IVersion;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

/**
 * Created by FunTc on 2020/08/07.
 */
public class VersionDialogHostActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 此时说明app进程重启，该页面重建，UpdateManager中的静态变量已被回收为null，需要再次赋值 */
        if (savedInstanceState != null) {
            UpdateManager.setVersionInfo((IVersion) getIntent().getSerializableExtra(Constant.KEY_VERSION_INFO));
        }
        
        ConfigParams params = getIntent().getParcelableExtra(Constant.KEY_CONFIG_PARAMS);
        AbsVersionDialog dialog = null;
        if (params != null) {
            try {
                dialog = params.versionDialogClz.newInstance();
            } catch (IllegalAccessException | InstantiationException ignored) { }
        }
        
        if (dialog == null) {
            dialog = new DefaultVersionDialog();
        }
        dialog.setArguments(getIntent().getExtras());
        dialog.setOnDialogClosedListener(needExit -> {
            if (needExit) {
                finishAffinity();
            } else {
                finish();
            }
        });
        dialog.show(getSupportFragmentManager(), "VersionDialog");
    }
    
}
