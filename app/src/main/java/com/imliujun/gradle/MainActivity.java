package com.imliujun.gradle;

import android.os.Bundle;
import android.util.Log;

import com.leon.channel.helper.ChannelReaderUtil;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //可以去 BuildConfig 中查看Gradle中定义的常量
        Log.i("tag", BuildConfig.DOMAIN_NAME);
        //获取渠道名
        String channel = ChannelReaderUtil.getChannel(this);
        Log.i("channel", channel);
    }
}
