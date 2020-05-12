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


    /**
     * Function to automatically run all the required functions to quickly get started with the app
     *
     * @param context Activity context
     * @param appId The App ID issued to you by Agora
     * @param channel The unique channel name for the AgoraRTC session in the string format
     */
    override fun bootstrap(context: Context, appId: String, channel: String) {
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

    /**
     * Method to join a particular channel
     * @param agoraChannel The unique channel name for the AgoraRTC session in the string format.
     */
    fun joinChannel(agoraChannel: String) {
        sdk!!.joinChannel(null, agoraChannel, null, 0)
        channel = agoraChannel
    }

    /**
     * Method to join a channel with optionally a custom UID and token
     *
     * @param data A map containing uid, channel name and token
     */
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

    /**
     * Method to leave the channel
     */
    override fun leaveChannel() {
        sdk!!.leaveChannel()
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
        sdk!!.setChannelProfile(profile)
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
        sdk!!.setClientRole(role)
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
        sdk!!.muteLocalAudioStream(isMute)
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
        sdk!!.muteLocalVideoStream(isMute)
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
        sdk!!.setParameters(String.format("{\"che.audio.live_for_comm\":%b}", enable))
        sdk!!.enableDualStreamMode(enable)
        sdk!!.setRemoteDefaultVideoStreamType(if (enable) Constants.VIDEO_STREAM_LOW else Constants.VIDEO_STREAM_HIGH)
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
        sdk!!.setRemoteVideoStreamType(uid, streamType)
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
        sdk!!.setRemoteDefaultVideoStreamType(streamType)
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
        sdk!!.setupLocalVideo(canvas)
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
        sdk!!.startPreview()
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
        sdk!!.setupRemoteVideo(canvas)
    }

    private val eventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        /**
         * Occurs when the local user joins a specified channel.
         *
         * The channel name assignment is based on channelName specified in the joinChannel method.
         *
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         *
         * @param channel Channel name.
         * @param uid User ID.
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered.
         */
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

        /**
         * Reports the statistics of the RtcEngine once every two seconds.
         *
         * @param stats RTC engine statistics: RtcStats.
         */
        override fun onRtcStats(stats: RtcStats) {
            for (listener in listeners) {
                listener.onRtcStats(stats)
            }
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) joins the channel.
         *
         * Communication profile: This callback notifies the app when another user joins the channel. If other users are already in the channel, the SDK also reports to the app on the existing users.
         * Live Broadcast profile: This callback notifies the app when the host joins the channel. If other hosts are already in the channel, the SDK also reports to the app on the existing hosts. We recommend having at most 17 hosts in a channel
         *
         * The SDK triggers this callback under one of the following circumstances:
         *
         * A remote user/host joins the channel by calling the joinChannel method.
         * A remote user switches the user role to the host by calling the setClientRole method after joining the channel
         * A remote user/host rejoins the channel after a network interruption.
         * The host injects an online media stream into the channel by calling the addInjectStreamUrl method.
         *
         * @param uid ID of the user or host who joins the channel.
         * @param elapsed Time delay (ms) from the local user calling joinChannel/setClientRole until this callback is triggered.
         */
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.i(TAG, "onUserJoined $uid")
            for (listener in listeners) {
                listener.onUserJoined(uid, elapsed)
            }
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         * Leave the channel: When the user/host leaves the channel, the user/host sends a goodbye message. When this message is received, the SDK determines that the user/host leaves the channel.
         * Drop offline: When no data packet of the user or host is received for a certain period of time (20 seconds for the communication profile, and more for the live broadcast profile), the SDK assumes that the user/host drops offline. A poor network connection may lead to false detections, so we recommend using the Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who leaves the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         * - USER_OFFLINE_QUIT(0): The user left the current channel.
         * - USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         * - USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         *
         */
        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, "onUserOffline $uid")
            for (listener in listeners) {
                listener.onUserOffline(uid, reason)
            }
        }

        /**
         * Reports the statistics of the audio stream from each remote user/host.
         *
         * The SDK triggers this callback once every two seconds for each remote user/host. If a channel includes multiple remote users, the SDK triggers this callback as many times.
         * Schemes such as FEC (Forward Error Correction) or retransmission counter the frame loss rate. Hence, users may find the overall audio quality acceptable even when the packet loss rate is high.
         *
         * @param stats Statistics of the received remote audio streams: RemoteAudioStats.
         */
        override fun onRemoteAudioStats(stats: RemoteAudioStats) {
            super.onRemoteAudioStats(stats)
        }

        /**
         * Occurs when the remote audio state changes.
         * This callback indicates the state change of the remote audio stream.
         *
         * @param uid ID of the user whose audio state changes.
         * @param state State of the remote audio:
         *
         * - REMOTE_AUDIO_STATE_STOPPED(0): The remote audio is in the default state, probably due to REMOTE_AUDIO_REASON_LOCAL_MUTED(3), REMOTE_AUDIO_REASON_REMOTE_MUTED(5), or REMOTE_AUDIO_REASON_REMOTE_OFFLINE(7).
         * - REMOTE_AUDIO_STATE_STARTING(1): The first remote audio packet is received.
         * - REMOTE_AUDIO_STATE_DECODING(2): The remote audio stream is decoded and plays normally, probably due to REMOTE_AUDIO_REASON_NETWORK_RECOVERY(2), REMOTE_AUDIO_REASON_LOCAL_UNMUTED(4) or REMOTE_AUDIO_REASON_REMOTE_UNMUTED(6).
         * - REMOTE_AUDIO_STATE_FROZEN(3): The remote audio is frozen, probably due to REMOTE_AUDIO_REASON_NETWORK_CONGESTION(1).
         * - REMOTE_AUDIO_STATE_FAILED(4): The remote audio fails to start, probably due to REMOTE_AUDIO_REASON_INTERNAL(0)
         *
         * @param reason The reason of the remote audio state change.
         *
         * - REMOTE_AUDIO_REASON_INTERNAL(0): Internal reasons
         * - REMOTE_AUDIO_REASON_NETWORK_CONGESTION(1): Network congestion.
         * - REMOTE_AUDIO_REASON_NETWORK_RECOVERY(2): Network recovery.
         * - REMOTE_AUDIO_REASON_LOCAL_MUTED(3): The local user stops receiving the remote audio stream or disables the audio module.
         * - REMOTE_AUDIO_REASON_LOCAL_UNMUTED(4): The local user resumes receiving the remote audio stream or enables the audio module.
         * - REMOTE_AUDIO_REASON_REMOTE_MUTED(5): The remote user stops sending the audio stream or disables the audio module.
         * - REMOTE_AUDIO_REASON_REMOTE_UNMUTED(6): The remote user resumes sending the audio stream or enables the audio module.
         * - REMOTE_AUDIO_REASON_REMOTE_OFFLINE(7): The remote user leaves the channel.
         *
         * @param elapsed
         */
        override fun onRemoteAudioStateChanged(
            uid: Int,
            state: Int,
            reason: Int,
            elapsed: Int
        ) {
            super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
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
            ActivityCompat.requestPermissions(context as Activity, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
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