package io.agora.uikit.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceView
import android.widget.FrameLayout
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoCanvas.RENDER_MODE_FIT
import io.agora.rtc.video.VideoCanvas.RENDER_MODE_HIDDEN
import io.agora.uikit.R
import io.agora.uikit.manager.AgoraRTC
import io.agora.uikit.manager.StateManager
import io.agora.uikit.manager.annotations.RenderMode
import io.agora.uikit.ui.mincontainer.AgoraRecyclerView


class AgoraView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AgoraViewContainer(context, attrs, defStyleAttr) {

    private var surface: SurfaceView? = RtcEngine.CreateRendererView(context)
    var canvas: VideoCanvas
    var mUID: Int = 0

    init {
        canvas = VideoCanvas(surface)
        addView(surface)

        val tAttributes = context.obtainStyledAttributes(attrs, R.styleable.AgoraView)
        try {
            val local = tAttributes.getBoolean(R.styleable.AgoraView_local, false)
            if (local) {
                setUID(0)
            }
        } finally {
            tAttributes.recycle()
        }
    }

    fun setZOrderMediaOverlay(isMediaOverlay: Boolean) {
        try {
            removeView(surface)
            surface?.setZOrderMediaOverlay(isMediaOverlay)
            addView(surface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setZOrderOnTop(onTop: Boolean) {
        try {
            removeView(surface)
            surface?.setZOrderOnTop(onTop)
            addView(surface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setRenderMode(@RenderMode renderMode: Int) {
        canvas.renderMode = renderMode
        if (canvas.uid == 0) {
            AgoraRTC.instance().sdk?.setLocalRenderMode(canvas.renderMode, canvas.mirrorMode)
        } else {
            AgoraRTC.instance().sdk?.setRemoteRenderMode(canvas.uid, canvas.renderMode, canvas.mirrorMode)
        }
    }

    fun setChannelId(channelId: String) {
        canvas.channelId = channelId
        if (canvas.uid == 0) {
            AgoraRTC.instance().sdk?.setupLocalVideo(canvas)
        } else {
            AgoraRTC.instance().sdk?.setupRemoteVideo(canvas)
        }
    }

    fun setMirrorMode(mirrorMode: Int) {
        canvas.mirrorMode = mirrorMode
        if (canvas.uid == 0) {
            AgoraRTC.instance().sdk?.setLocalRenderMode(canvas.renderMode, canvas.mirrorMode)
        } else {
            AgoraRTC.instance().sdk?.setRemoteRenderMode(canvas.uid, canvas.renderMode, canvas.mirrorMode)
        }
    }

    fun reCreateSurface() {
        removeView(surface)
        surface = RtcEngine.CreateRendererView(context)
        addView(surface)
    }


    fun setUID(uid: Int) {
        mUID = uid
        if (uid == 0) {
            AgoraRTC.instance().sdk?.setupLocalVideo(VideoCanvas(surface, RENDER_MODE_HIDDEN, 0))
        } else {
            AgoraRTC.instance().sdk?.setupRemoteVideo(VideoCanvas(surface, RENDER_MODE_HIDDEN, uid))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int = MeasureSpec.getSize(widthMeasureSpec)
        val height: Int = MeasureSpec.getSize(heightMeasureSpec)
        surface?.layout(0, 0, width, height)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}