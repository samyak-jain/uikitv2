package io.agora.sampleapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.agora.agorauikit.UITemplates;
import io.agora.agorauikit.manager.AgoraRTC;
import io.agora.rtc.IRtcEngineEventHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AgoraRTC.instance().bootstrap(this, "appid", "channelname");
        AgoraRTC.instance().registerListener(new IRtcEngineEventHandler() {
            @Override
            public void onJoinChannelSuccess(String s, int i, int i1) {
                super.onJoinChannelSuccess(s, i, i1);
            }
        });
        setContentView(UITemplates.GROUP_CALL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AgoraRTC.instance().destroySdk();
    }
}
