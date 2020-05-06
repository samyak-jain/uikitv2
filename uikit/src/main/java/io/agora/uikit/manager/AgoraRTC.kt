package io.agora.uikit.manager


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc.BuildConfig
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.uikit.manager.annotations.ChannelProfile
import io.agora.uikit.manager.annotations.ClientRole
import io.agora.uikit.manager.annotations.RenderMode
import io.agora.uikit.manager.annotations.StreamType


class AgoraRTC private constructor() : SdkManager<RtcEngine?>() {
    private val TAG: String = AgoraRTC::class.java.name
    private val listeners: MutableList<IRtcEngineEventHandler>
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val PERMISSION_REQ_ID = 22

    init {
        listeners = ArrayList()
    }

    override fun bootstrap(context: Context, appId: String, channel: String) {
        requestPermissions(context)
        start(context, appId)
        joinChannel(channel)
    }

    @Throws(Exception::class)
    override fun createSDK(context: Context?, appId: String?): RtcEngine {
        return RtcEngine.create(context, appId, eventHandler)
    }

    override fun configSdk() {
        if (BuildConfig.DEBUG) {
            sdk!!.setParameters("{\"rtc.log_filter\": 65535}")
        }
        sdk!!.enableAudio()
        sdk!!.enableVideo()
        val config = VideoEncoderConfiguration(
            VideoEncoderConfiguration.VD_360x360,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE
        )
        sdk!!.setVideoEncoderConfiguration(config)
    }

    fun joinChannel(agoraChannel: String) {
        sdk!!.joinChannel(null, agoraChannel, null, 0)
        channel = agoraChannel
    }

    override fun joinChannel(data: Map<String?, String?>?) {
        data?.get(USER_ID)?.let { Integer.valueOf(it) }?.let {
            sdk!!.joinChannel(
                data[TOKEN],
                data[CHANNEL_ID],
                null,
                it
            )
        }
    }

    override fun leaveChannel() {
        sdk!!.leaveChannel()
    }

    override fun destroySdk() {
        leaveChannel()
        RtcEngine.destroy()
    }

    fun registerListener(listener: IRtcEngineEventHandler) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: IRtcEngineEventHandler) {
        listeners.remove(listener)
    }

    fun setChannelProfile(@ChannelProfile profile: Int) {
        sdk!!.setChannelProfile(profile)
    }

    fun setClientRole(@ClientRole role: Int) {
        sdk!!.setClientRole(role)
    }

    fun muteLocalAudioStream(isMute: Boolean) {
        sdk!!.muteLocalAudioStream(isMute)
    }

    fun muteLocalVideoStream(isMute: Boolean) {
        sdk!!.muteLocalVideoStream(isMute)
    }

    fun enableDualStreamMode(enable: Boolean) {
        sdk!!.setParameters(String.format("{\"che.audio.live_for_comm\":%b}", enable))
        sdk!!.enableDualStreamMode(enable)
        sdk!!.setRemoteDefaultVideoStreamType(if (enable) Constants.VIDEO_STREAM_LOW else Constants.VIDEO_STREAM_HIGH)
    }

    fun setRemoteVideoStreamType(uid: Int, @StreamType streamType: Int) {
        sdk!!.setRemoteVideoStreamType(uid, streamType)
    }

    fun setRemoteDefaultVideoStreamType(@StreamType streamType: Int) {
        sdk!!.setRemoteDefaultVideoStreamType(streamType)
    }

    fun createRendererView(context: Context?): SurfaceView {
        return RtcEngine.CreateRendererView(context)
    }

    fun setupLocalVideo(view: SurfaceView?, @RenderMode renderMode: Int) {
        Log.d(TAG, "setupLocalVideo " + (view != null))
        val canvas = VideoCanvas(view, renderMode, 0)
        sdk!!.setupLocalVideo(canvas)
    }

    fun startPreview() {
        sdk!!.startPreview()
    }

    fun setupRemoteVideo(view: SurfaceView?, @RenderMode renderMode: Int, uid: Int) {
        Log.d(TAG, "setupRemoteVideo " + (view != null) + " " + uid)
        val canvas = VideoCanvas(view, renderMode, uid)
        sdk!!.setupRemoteVideo(canvas)
    }

    private val eventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(
            channel: String,
            uid: Int,
            elapsed: Int
        ) {
            Log.i(TAG, "onJoinChannelSuccess $channel $uid")
            for (listener in listeners) {
                listener.onJoinChannelSuccess(channel, uid, elapsed)
            }
        }

        override fun onRtcStats(stats: RtcStats) {
            for (listener in listeners) {
                listener.onRtcStats(stats)
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.i(TAG, "onUserJoined $uid")
            for (listener in listeners) {
                listener.onUserJoined(uid, elapsed)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, "onUserOffline $uid")
            for (listener in listeners) {
                listener.onUserOffline(uid, reason)
            }
        }

        override fun onRemoteAudioStats(stats: RemoteAudioStats) {
            super.onRemoteAudioStats(stats)
        }

        override fun onRemoteAudioStateChanged(
            uid: Int,
            state: Int,
            reason: Int,
            elapsed: Int
        ) {
            super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
        }
    }

    companion object {
        private var instance: AgoraRTC = AgoraRTC()

        @JvmStatic
        fun instance(): AgoraRTC {
            return instance
        }
    }


    private fun checkSelfPermission(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(context as Activity, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
            return false
        }
        return true
    }

    override fun requestPermissions(context: Context): Boolean {
        return checkSelfPermission(context, REQUESTED_PERMISSIONS[0]) &&
            checkSelfPermission(context, REQUESTED_PERMISSIONS[1]) &&
            checkSelfPermission(context, REQUESTED_PERMISSIONS[2])
    }
}