package io.agora.sampleapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.agora.agorauikit.manager.AgoraRTC;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AgoraRTC.instance().bootstrap(this, "appid", "channelname");
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AgoraRTC.instance().destroySdk();
    }
}
