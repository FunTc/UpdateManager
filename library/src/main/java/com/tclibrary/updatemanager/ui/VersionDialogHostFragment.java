package com.tclibrary.updatemanager.ui;

import android.os.Bundle;
import android.util.Log;

import com.tclibrary.updatemanager.Constant;
import com.tclibrary.updatemanager.UpdateManager;
import com.tclibrary.updatemanager.model.ConfigParams;
import com.tclibrary.updatemanager.model.IVersion;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by TianCheng on 2020/09/24.
 */
public class VersionDialogHostFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() == null) return;
        
        if (savedInstanceState != null) {
            UpdateManager.setVersionInfo((IVersion) getArguments().getSerializable(Constant.KEY_VERSION_INFO));
        }

        ConfigParams params = getArguments().getParcelable(Constant.KEY_CONFIG_PARAMS);
        AbsVersionDialog dialog = null;
        if (params != null) {
            try {
                dialog = params.versionDialogClz.newInstance();
            } catch (IllegalAccessException | java.lang.InstantiationException ignored) { }
        }

        if (dialog == null) {
            dialog = new DefaultVersionDialog();
        }
        dialog.setArguments(getArguments());
        dialog.setOnDialogClosedListener(needExit -> {
            if (needExit) {
                requireActivity().finishAffinity();
            } else {
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .remove(this)
                        .commitAllowingStateLoss();
            }
        });
        dialog.show(getChildFragmentManager(), "VersionDialog");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("tian", "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("tian", "onPause");
        
    }

    @Override
    public void onDestroy() {
        Log.e("tian", "onDestroy");
        super.onDestroy();
    }
}
