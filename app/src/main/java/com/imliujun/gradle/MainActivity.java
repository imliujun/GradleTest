package com.imliujun.gradle;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //可以去 BuildConfig 中查看Gradle中定义的常量
        Log.i("tag", BuildConfig.DOMAIN_NAME);
        //获取渠道名
        String channel = getMetaDataInApp("UMENG_CHANNEL");
        Log.i("channel", channel);
    }
    
    public String getMetaDataInApp(@NonNull final String key) {
        String value = "";
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            value = String.valueOf(ai.metaData.get(key));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }
}
