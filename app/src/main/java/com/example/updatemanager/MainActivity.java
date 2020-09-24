package com.example.updatemanager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.liulishuo.okdownload.core.Util;
import com.tclibrary.updatemanager.CheckedResultCallback;
import com.tclibrary.updatemanager.IVersionChecker;
import com.tclibrary.updatemanager.UpdateManager;
import com.tclibrary.updatemanager.model.IVersion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void onClick(View v) {
//        startActivity(new Intent(this, SecondActivity.class));
        Util.enableConsoleLog();
        new UpdateManager.Builder(this)
                .setChecker(new IVersionChecker() {
                    @Override
                    public void check(@NonNull CheckedResultCallback callback) {
                        VersionInfo info = new VersionInfo();
                        info.setApkUrl("https://dldir1.qq.com/weixin/android/weixin7017android1720_arm64.apk");
                        info.setVersion("10.9.1");
                        info.setLog("1.解决已知问题；\n2.优化用户体验；\n3.视频号新增“浮评”功能，你可以在观看视频的同时看评论了；\n4.在聊天中，长按消息的菜单有了新样式。");
                        info.setHasNewVersion(true);
                        info.setMust(false);
                        callback.result(info);
                    }
                })
                .setCheckedResultCallback(new CheckedResultCallback() {
                    @Override
                    public void result(@NonNull IVersion info) {
                        if (!info.hasNewVersion())
                            Toast.makeText(MainActivity.this, "已是最新版", Toast.LENGTH_SHORT).show();
                    }
                })
                .showDialogInDownloading(true)
                .setShowIgnoreVersion(false)
                .showNotification(true)
                .setDefaultDialogTheme(R.mipmap.update_dialog_top_img, Color.parseColor("#E12B2B"))
                .build()
                .start();
                
    }
    
}