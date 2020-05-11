package io.agora.uikit;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.agora.uikit.manager.AgoraRTC;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AgoraRTC.instance().bootstrap(this, "appid", "channelname");
        setContentView(UITemplates.GROUP_CALL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AgoraRTC.instance().destroySdk();
    }
}
