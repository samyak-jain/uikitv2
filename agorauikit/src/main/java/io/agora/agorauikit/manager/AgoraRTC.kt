package io.agora.agorauikit.manager


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.util.Log
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.agorauikit.BuildConfig
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.agorauikit.manager.annotations.ChannelProfile
import io.agora.agorauikit.manager.annotations.ClientRole
import io.agora.agorauikit.manager.annotations.RenderMode
import io.agora.agorauikit.manager.annotations.StreamType
import io.agora.rtc.models.UserInfo


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


    /**
     * Function to automatically run all the required functions to quickly get started with the app
     *
     * @param context Activity context
     * @param appId The App ID issued to you by Agora
     * @param channel The unique channel name for the AgoraRTC session in the string format
     */
    override fun bootstrap(
        context: Context,
        appId: String,
        channel: String
    ) {
        requestPermissions(context)
        start(context, appId)
        joinChannel(channel)
    }

    /**
     * Creates the RtcEngine object for Agora
     *
     * @param context Activity Context
     * @param appId The App ID issued to you by Agora
     * @return RtcEngine Object
     */
    @Throws(Exception::class)
    override fun createSDK(context: Context?, appId: String?): RtcEngine {
        return RtcEngine.create(context, appId, eventHandler)
    }

    /**
     * Sets some default configurations for the RtcEngine regarding the resolution, frame rate,
     * FPS etc...
     */
    override fun configSdk() {
        if (BuildConfig.DEBUG) {
            _sdk!!.setParameters("{\"rtc.log_filter\": 65535}")
        }
        _sdk!!.enableAudio()
        _sdk!!.enableVideo()
        val config = VideoEncoderConfiguration(
            VideoEncoderConfiguration.VD_360x360,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE
        )
        _sdk!!.setVideoEncoderConfiguration(config)
    }

    /**
     * Method to join a particular channel
     * @param agoraChannel The unique channel name for the AgoraRTC session in the string format.
     */
    fun joinChannel(agoraChannel: String) {
        _sdk!!.joinChannel(null, agoraChannel, null, 0)
        channel = agoraChannel
    }

    /**
     * Method to join a channel with optionally a custom UID and token
     *
     * @param data A map containing uid, channel name and token
     */
    override fun joinChannel(data: Map<String?, String?>?) {
        data?.get(USER_ID)?.let { Integer.valueOf(it) }?.let {
            _sdk!!.joinChannel(
                data[TOKEN],
                data[CHANNEL_ID],
                null,
                it
            )
        }
    }

    /**
     * Method to leave the channel
     */
    override fun leaveChannel() {
        _sdk!!.leaveChannel()
    }

    /**
     * Destroys the RtcEngine
     */
    override fun destroySdk() {
        leaveChannel()
        RtcEngine.destroy()
    }

    /**
     * Add a new event listener
     *
     * @param listener Event Listener to add
     */
    fun registerListener(listener: IRtcEngineEventHandler) {
        listeners.add(listener)
    }

    /**
     * Detach event listener
     *
     * @param listener Event listener to remove
     */
    fun unregisterListener(listener: IRtcEngineEventHandler) {
        listeners.remove(listener)
    }

    /**
     * Sets the channel profile of the Agora RtcEngine.
     *
     * The Agora RtcEngine differentiates channel profiles and applies different optimization
     * algorithms accordingly. For example, it prioritizes smoothness and low latency for a video
     * call, and prioritizes video quality for a video broadcast.
     *
     * @param profile The channel profile of the Agora RtcEngine:
     *
     * - CHANNEL_PROFILE_COMMUNICATION(0): (Default) The Communication profile. Use this profile in one-on-one calls or group calls, where all users can talk freely.
     * - CHANNEL_PROFILE_LIVE_BROADCASTING(1): The Live-Broadcast profile. Users in a live-broadcast channel have a role as either broadcaster or audience. A broadcaster can both send and receive streams; an audience can only receive streams.
     * - CHANNEL_PROFILE_GAME(2): The Gaming profile. This profile uses a codec with a lower bitrate and consumes less power. Applies to the gaming scenario, where all game players can talk freely.
     *
     */
    fun setChannelProfile(@ChannelProfile profile: Int) {
        _sdk!!.setChannelProfile(profile)
    }

    /**
     * Sets the role of a user (Live Broadcast only).
     *
     * This method sets the role of a user, such as a host or an audience (default), before joining a channel.
     * This method can be used to switch the user role after a user joins a channel.
     * In the Live Broadcast profile, when a user switches user roles after joining a channel,
     * a successful setClientRole method call triggers the following callbacks:
     *
     * The local client: onClientRoleChanged.
     * The remote client: onUserJoined or onUserOffline(USER_OFFLINE_BECOME_AUDIENCE).
     *
     * @param role Sets the role of a user:
     *
     * - CLIENT_ROLE_BROADCASTER(1): Broadcaster. A broadcaster can both send and receive streams.
     * - CLIENT_ROLE_AUDIENCE(2): Audience, the default role. An audience can only receive streams.
     *
     */
    fun setClientRole(@ClientRole role: Int) {
        _sdk!!.setClientRole(role)
    }

    /**
     * Stops/Resumes sending the local audio stream.
     *
     * A successful muteLocalAudioStream method call triggers the onUserMuteAudio callback on the remote client.
     *
     * @param isMute Sets whether to send/stop sending the local audio stream:
     *
     * - true: Stop sending the local audio stream.
     * - false: (Default) Send the local audio stream.
     *
     */
    fun muteLocalAudioStream(isMute: Boolean) {
        _sdk!!.muteLocalAudioStream(isMute)
    }

    /**
     * Stops/Resumes sending the local video stream.
     * A successful muteLocalVideoStream method call triggers the onUserMuteVideo callback on the remote client.
     *
     * @param isMute Sets whether to send/stop sending the local video stream:
     *
     * - true: Stop sending the local video stream.
     * - false: (Default) Send the local video stream.

     */
    fun muteLocalVideoStream(isMute: Boolean) {
        _sdk!!.muteLocalVideoStream(isMute)
    }

    /**
     * Enables/Disables the dual video stream mode.
     * If dual-stream mode is enabled, the receiver can choose to receive the high stream (high-resolution high-bitrate video stream) or low stream (low-resolution low-bitrate video stream) video.
     *
     * @param enable Sets the stream mode:
     *
     * - true: Dual-stream mode.
     * - false: (Default) Single-stream mode.

     */
    fun enableDualStreamMode(enable: Boolean) {
        _sdk!!.setParameters(String.format("{\"che.audio.live_for_comm\":%b}", enable))
        _sdk!!.enableDualStreamMode(enable)
        _sdk!!.setRemoteDefaultVideoStreamType(if (enable) Constants.VIDEO_STREAM_LOW else Constants.VIDEO_STREAM_HIGH)
    }

    /**
     * Sets the stream type of the remote video.
     * Under limited network conditions, if the publisher has not disabled the dual-stream mode
     * using enableDualStreamMode(false), the receiver can choose to receive either the high-video
     * stream (the high resolution, and high bitrate video stream) or the low-video stream
     * (the low resolution, and low bitrate video stream). By default, users receive the
     * high-video stream. Call this method if you want to switch to the low-video stream.
     * This method allows the app to adjust the corresponding video stream type based on the
     * size of the video window to reduce the bandwidth and resources. The aspect ratio of the
     * low-video stream is the same as the high-video stream. Once the resolution of the high-video
     * stream is set, the system automatically sets the resolution, frame rate, and bitrate of the
     * low-video stream.
     * Thz SDK reports the result of calling this method in the onApiCallExecuted callback.
     *
     * @param uid ID of the remote user sending the video stream.
     * @param streamType Sets the video-stream type:
     *
     * - High-resolution, high-bitrate video: High-stream video(0)
     * - Low-resolution, low-bitrate video: Low-stream video(1)
     *
     */
    fun setRemoteVideoStreamType(uid: Int, @StreamType streamType: Int) {
        _sdk!!.setRemoteVideoStreamType(uid, streamType)
    }

    /**
     * Sets the default video-stream type of the remotely subscribed video stream when the remote user sends dual streams.
     *
     * @param streamType Sets the default video-stream type:
     *
     * - High-resolution, high-bitrate video: High-stream video(0)
     * - Low-resolution, low-bitrate video: Low-stream video(1)
     *
     */
    fun setRemoteDefaultVideoStreamType(@StreamType streamType: Int) {
        _sdk!!.setRemoteDefaultVideoStreamType(streamType)
    }

    /**
     * Creates the video renderer view.
     *
     * CreateRendererView returns the SurfaceView type. The operation and layout of the view are
     * managed by the app, and the Agora SDK renders the view provided by the app. The video
     * display view must be created using this method instead of directly calling SurfaceView.
     *
     * @param context Activity Context
     * @return
     */
    fun createRendererView(context: Context?): SurfaceView {
        return RtcEngine.CreateRendererView(context)
    }

    /**
     * Initializes the local video view.
     *
     * This method initializes the video view of the local stream on the local device. It affects only the video view that the local user sees, not the published local video stream.
     *
     * Call this method to bind the loca video stream to a video view and to set the rendering and mirror modes of the video view.
     *
     * @param view SurfaceView
     * @param renderMode The rendering mode of the video view:
     *
     * RENDER_MODE_HIDDEN(1): Uniformly scale the video until it fills the visible boundaries. One dimension of the video may have clipped contents.
     * RENDER_MODE_FIT(2): Uniformly scale the video until one of its dimension fits the boundary. Areas that are not filled due to the disparity in the aspect ratio are filled with black.
     * RENDER_MODE_ADAPTIVE(3): This mode is deprecated and Agora does not recommend using it.
     *
     */
    fun setupLocalVideo(view: SurfaceView?, @RenderMode renderMode: Int) {
        Log.d(TAG, "setupLocalVideo " + (view != null))
        val canvas = VideoCanvas(view, renderMode, 0)
        _sdk!!.setupLocalVideo(canvas)
    }

    /**
     * Starts the local video preview before joining a channel.
     *
     * Before calling this method, you must:
     *
     * Call the setupLocalVideo method to set the local preview window and configure the attributes
     * Call the enableVideo method to enable the video.
     */
    fun startPreview() {
        _sdk!!.startPreview()
    }

    /**
     * Initializes the video view of a remote user.
     * This method initializes the video view of a remote stream on the local device. It affects
     * only the video view that the local user sees. Call this method to bind the remote video
     * stream to a video view and to set the rendering and mirror modes of the video view.
     * Typically, the app specifies the uid of the remote user sending the video in the method call
     * before the remote user joins a channel. If the uid of the remote user is unknown to the app,
     * set the uid when the app receives the onUserJoined callback. If the Video Recording function
     * is enabled, the Video Recording Service joins the channel as a dummy client, causing other
     * clients to also receive the onUserJoined callback. Do not bind the dummy client to the app
     * view because the dummy client does not send any video streams. If your app does not
     * recognize the dummy client, bind the remote user to the view when the SDK triggers
     * the onFirstRemoteVideoDecoded callback.
     * To unbind the remote user from the view, set view in Video Canvas as null.
     * Once the remote user leaves the channel, the SDK unbinds the remote user.
     *
     * @param view SurfaceView
     * @param renderMode The rendering mode of the video view:
     *
     * RENDER_MODE_HIDDEN(1): Uniformly scale the video until it fills the visible boundaries. One dimension of the video may have clipped contents
     * RENDER_MODE_FIT(2): Uniformly scale the video until one of its dimension fits the boundary. Areas that are not filled due to the disparity in the aspect ratio are filled with black.
     * RENDER_MODE_ADAPTIVE(3): This mode is deprecated and Agora does not recommend using it.
     *
     * @param uid User ID.
     */
    fun setupRemoteVideo(view: SurfaceView?, @RenderMode renderMode: Int, uid: Int) {
        Log.d(TAG, "setupRemoteVideo " + (view != null) + " " + uid)
        val canvas = VideoCanvas(view, renderMode, uid)
        _sdk!!.setupRemoteVideo(canvas)
    }

    private var eventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onActiveSpeaker(p0: Int) {
            listeners.forEach {
                it.onActiveSpeaker(p0)
            }
        }

        override fun onLocalAudioStats(p0: LocalAudioStats?) {
            listeners.forEach {
                it.onLocalAudioStats(p0)
            }
        }

        override fun onRemoteSubscribeFallbackToAudioOnly(p0: Int, p1: Boolean) {
            listeners.forEach {
                it.onRemoteSubscribeFallbackToAudioOnly(p0, p1)
            }
        }

        override fun onAudioMixingStateChanged(p0: Int, p1: Int) {
            listeners.forEach {
                it.onAudioMixingStateChanged(p0, p1)
            }
        }

        override fun onRtcStats(p0: RtcStats?) {
            listeners.forEach {
                it.onRtcStats(p0)
            }
        }

        override fun onFirstRemoteAudioFrame(p0: Int, p1: Int) {
            listeners.forEach {
                it.onFirstRemoteAudioFrame(p0, p1)
            }
        }

        override fun onAudioRouteChanged(p0: Int) {
            listeners.forEach {
                it.onAudioRouteChanged(p0)
            }
        }

        override fun onLocalVideoStat(p0: Int, p1: Int) {
            listeners.forEach {
                it.onLocalVideoStat(p0, p1)
            }
        }

        override fun onAudioQuality(p0: Int, p1: Int, p2: Short, p3: Short) {
            listeners.forEach {
                it.onAudioQuality(p0, p1, p2, p3)
            }
        }

        override fun onNetworkTypeChanged(p0: Int) {
            listeners.forEach {
                it.onNetworkTypeChanged(p0)
            }
        }

        override fun onLocalAudioStateChanged(p0: Int, p1: Int) {
            listeners.forEach {
                it.onLocalAudioStateChanged(p0, p1)
            }
        }

        override fun onFirstRemoteVideoFrame(p0: Int, p1: Int, p2: Int, p3: Int) {
            listeners.forEach {
                it.onFirstRemoteVideoFrame(p0, p1, p2, p3)
            }
        }

        override fun onLastmileQuality(p0: Int) {
            listeners.forEach {
                it.onLastmileQuality(p0)
            }
        }

        override fun onCameraExposureAreaChanged(p0: Rect?) {
            listeners.forEach {
                it.onCameraExposureAreaChanged(p0)
            }
        }

        override fun onRemoteAudioTransportStats(p0: Int, p1: Int, p2: Int, p3: Int) {
            listeners.forEach {
                it.onRemoteAudioTransportStats(p0, p1, p2, p3)
            }
        }

        override fun onFirstRemoteVideoDecoded(p0: Int, p1: Int, p2: Int, p3: Int) {
            listeners.forEach {
                it.onFirstRemoteVideoDecoded(p0, p1, p2, p3)
            }
        }

        override fun onLocalVideoStateChanged(p0: Int, p1: Int) {
            listeners.forEach {
                it.onLocalVideoStateChanged(p0, p1)
            }
        }

        override fun onTranscodingUpdated() {
            listeners.forEach {
                it.onTranscodingUpdated()
            }
        }

        override fun onClientRoleChanged(p0: Int, p1: Int) {
            listeners.forEach {
                it.onClientRoleChanged(p0, p1)
            }
        }

        override fun onApiCallExecuted(p0: Int, p1: String?, p2: String?) {
            listeners.forEach {
                it.onApiCallExecuted(p0, p1, p2)
            }
        }

        override fun onFirstLocalAudioFrame(p0: Int) {
            listeners.forEach {
                it.onFirstLocalAudioFrame(p0)
            }
        }

        override fun onRemoteAudioStats(p0: RemoteAudioStats?) {
            listeners.forEach {
                it.onRemoteAudioStats(p0)
            }
        }

        override fun onRemoteVideoTransportStats(p0: Int, p1: Int, p2: Int, p3: Int) {
            listeners.forEach {
                it.onRemoteVideoTransportStats(p0, p1, p2, p3)
            }
        }

        override fun onStreamUnpublished(p0: String?) {
            listeners.forEach {
                it.onStreamUnpublished(p0)
            }
        }

        override fun onRejoinChannelSuccess(p0: String?, p1: Int, p2: Int) {
            listeners.forEach {
                it.onRejoinChannelSuccess(p0, p1, p2)
            }
        }

        override fun onVideoStopped() {
            listeners.forEach {
                it.onVideoStopped()
            }
        }

        override fun onLocalVideoStats(p0: LocalVideoStats?) {
            listeners.forEach {
                it.onLocalVideoStats(p0)
            }
        }

        override fun onStreamMessageError(p0: Int, p1: Int, p2: Int, p3: Int, p4: Int) {
            listeners.forEach {
                it.onStreamMessageError(p0, p1, p2, p3, p4)
            }
        }

        override fun onWarning(p0: Int) {
            listeners.forEach {
                it.onWarning(p0)
            }
        }

        override fun onLocalPublishFallbackToAudioOnly(p0: Boolean) {
            listeners.forEach {
                it.onLocalPublishFallbackToAudioOnly(p0)
            }
        }

        override fun onStreamPublished(p0: String?, p1: Int) {
            listeners.forEach {
                it.onStreamPublished(p0, p1)
            }
        }

        override fun onMediaEngineStartCallSuccess() {
            listeners.forEach {
                it.onMediaEngineStartCallSuccess()
            }
        }

        override fun onStreamInjectedStatus(p0: String?, p1: Int, p2: Int) {
            listeners.forEach {
                it.onStreamInjectedStatus(p0, p1, p2)
            }
        }

        override fun onUserMuteVideo(p0: Int, p1: Boolean) {
            listeners.forEach {
                it.onUserMuteVideo(p0, p1)
            }
        }

        override fun onJoinChannelSuccess(p0: String?, p1: Int, p2: Int) {
            listeners.forEach {
                it.onJoinChannelSuccess(p0, p1, p2)
            }
        }

        override fun onLeaveChannel(p0: RtcStats?) {
            listeners.forEach {
                it.onLeaveChannel(p0)
            }
        }

        override fun onConnectionStateChanged(p0: Int, p1: Int) {
            listeners.forEach {
                it.onConnectionStateChanged(p0, p1)
            }
        }

        override fun onMicrophoneEnabled(p0: Boolean) {
            listeners.forEach {
                it.onMicrophoneEnabled(p0)
            }
        }

        override fun onRemoteVideoStateChanged(p0: Int, p1: Int, p2: Int, p3: Int) {
            listeners.forEach {
                it.onRemoteVideoStateChanged(p0, p1, p2, p3)
            }
        }

        override fun onFacePositionChanged(
            p0: Int,
            p1: Int,
            p2: Array<out AgoraFacePositionInfo>?
        ) {
            listeners.forEach {
                it.onFacePositionChanged(p0, p1, p2)
            }
        }

        override fun onConnectionLost() {
            listeners.forEach {
                it.onConnectionLost()
            }
        }

        override fun onConnectionBanned() {
            listeners.forEach {
                it.onConnectionBanned()
            }
        }

        override fun onRemoteVideoStats(p0: RemoteVideoStats?) {
            listeners.forEach {
                it.onRemoteVideoStats(p0)
            }
        }

        override fun onFirstLocalVideoFrame(p0: Int, p1: Int, p2: Int) {
            listeners.forEach {
                it.onFirstLocalVideoFrame(p0, p1, p2)
            }
        }

        override fun onCameraReady() {
            listeners.forEach {
                it.onCameraReady()
            }
        }

        override fun onAudioEffectFinished(p0: Int) {
            listeners.forEach {
                it.onAudioEffectFinished(p0)
            }
        }

        override fun onStreamMessage(p0: Int, p1: Int, p2: ByteArray?) {
            listeners.forEach {
                it.onStreamMessage(p0, p1, p2)
            }
        }

        override fun onCameraFocusAreaChanged(p0: Rect?) {
            listeners.forEach {
                it.onCameraExposureAreaChanged(p0)
            }
        }

        override fun onMediaEngineLoadSuccess() {
            listeners.forEach {
                it.onMediaEngineLoadSuccess()
            }
        }

        override fun onChannelMediaRelayStateChanged(p0: Int, p1: Int) {
            listeners.forEach {
                it.onChannelMediaRelayStateChanged(p0, p1)
            }
        }

        override fun onRequestToken() {
            listeners.forEach {
                it.onRequestToken()
            }
        }

        override fun onUserEnableLocalVideo(p0: Int, p1: Boolean) {
            listeners.forEach {
                it.onUserEnableLocalVideo(p0, p1)
            }
        }

        override fun onConnectionInterrupted() {
            listeners.forEach {
                it.onConnectionInterrupted()
            }
        }

        override fun onRtmpStreamingStateChanged(p0: String?, p1: Int, p2: Int) {
            listeners.forEach {
                it.onRtmpStreamingStateChanged(p0, p1, p2)
            }
        }

        override fun onAudioVolumeIndication(p0: Array<out AudioVolumeInfo>?, p1: Int) {
            listeners.forEach {
                it.onAudioVolumeIndication(p0, p1)
            }
        }

        override fun onAudioMixingFinished() {
            listeners.forEach {
                it.onAudioMixingFinished()
            }
        }

        override fun onUserJoined(p0: Int, p1: Int) {
            listeners.forEach {
                it.onUserJoined(p0, p1)
            }
        }

        override fun onTokenPrivilegeWillExpire(p0: String?) {
            listeners.forEach {
                it.onTokenPrivilegeWillExpire(p0)
            }
        }

        override fun onUserOffline(p0: Int, p1: Int) {
            listeners.forEach {
                it.onUserOffline(p0, p1)
            }
        }

        override fun onNetworkQuality(p0: Int, p1: Int, p2: Int) {
            listeners.forEach {
                it.onNetworkQuality(p0, p1, p2)
            }
        }

        override fun onRemoteVideoStat(p0: Int, p1: Int, p2: Int, p3: Int) {
            listeners.forEach {
                it.onRemoteVideoStat(p0, p1, p2, p3)
            }
        }

        override fun onVideoSizeChanged(p0: Int, p1: Int, p2: Int, p3: Int) {
            listeners.forEach {
                it.onVideoSizeChanged(p0, p1, p2, p3)
            }
        }

        override fun onLastmileProbeResult(p0: LastmileProbeResult?) {
            listeners.forEach {
                it.onLastmileProbeResult(p0)
            }
        }

        override fun onChannelMediaRelayEvent(p0: Int) {
            listeners.forEach {
                it.onChannelMediaRelayEvent(p0)
            }
        }

        override fun onUserMuteAudio(p0: Int, p1: Boolean) {
            listeners.forEach {
                it.onUserMuteAudio(p0, p1)
            }
        }

        override fun onFirstRemoteAudioDecoded(p0: Int, p1: Int) {
            listeners.forEach {
                it.onFirstRemoteAudioDecoded(p0, p1)
            }
        }

        override fun onLocalUserRegistered(p0: Int, p1: String?) {
            listeners.forEach {
                it.onLocalUserRegistered(p0, p1)
            }
        }

        override fun onError(p0: Int) {
            listeners.forEach {
                it.onError(p0)
            }
        }

        override fun onUserEnableVideo(p0: Int, p1: Boolean) {
            listeners.forEach {
                it.onUserEnableVideo(p0, p1)
            }
        }

        override fun onUserInfoUpdated(p0: Int, p1: UserInfo?) {
            listeners.forEach {
                it.onUserInfoUpdated(p0, p1)
            }
        }

        override fun onRemoteAudioStateChanged(p0: Int, p1: Int, p2: Int, p3: Int) {
            listeners.forEach {
                it.onRemoteAudioStateChanged(p0, p1, p2, p3)
            }
        }
    }

    /**
     * Singleton Pattern
     */
    companion object {
        private var instance: AgoraRTC = AgoraRTC()

        @JvmStatic
        fun instance(): AgoraRTC {
            return instance
        }
    }


    /**
     * Requests a particular permission if not granted
     *
     * @param context Activity Context
     * @param permission Permission String
     * @return True if Permission is granted
     */
    private fun checkSelfPermission(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                REQUESTED_PERMISSIONS,
                PERMISSION_REQ_ID
            )
            return false
        }
        return true
    }

    /**
     * Request all relevant permissions
     *
     * @param context Activity Context
     * @return True if all the permissions were already granted
     */
    override fun requestPermissions(context: Context): Boolean {
        return checkSelfPermission(context, REQUESTED_PERMISSIONS[0]) &&
                checkSelfPermission(context, REQUESTED_PERMISSIONS[1]) &&
                checkSelfPermission(context, REQUESTED_PERMISSIONS[2])
    }
}
