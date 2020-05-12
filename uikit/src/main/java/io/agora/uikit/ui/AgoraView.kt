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
            // Get attribute which tell us if this instance is for the local view or the remote view
            val local = tAttributes.getBoolean(R.styleable.AgoraView_local, false)
            if (local) {
                // For local, we set the UID as 0
                setUID(0)
            }
        } finally {
            tAttributes.recycle()
        }
    }

    /**
     * Control whether the surface view's surface is placed on top of another regular surface view
     * in the window (but still behind the window itself). This is typically used to place
     * overlays on top of an underlying media surface view.
     *
     * Note that this must be set before the surface view's containing window is attached to the window manager.
     *
     * Calling this overrides any previous call to setZOrderOnTop(boolean).
     *
     * @param isMediaOverlay
     */
    fun setZOrderMediaOverlay(isMediaOverlay: Boolean) {
        try {
            removeView(surface)
            surface?.setZOrderMediaOverlay(isMediaOverlay)
            addView(surface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Control whether the surface view's surface is placed on top of its window.
     * Normally it is placed behind the window, to allow it to (for the most part) appear to
     * composite with the views in the hierarchy. By setting this, you cause it to be placed
     * above the window. This means that none of the contents of the window this SurfaceView is
     * in will be visible on top of its surface.
     *
     * Note that this must be set before the surface view's containing window is attached to the
     * window manager. If you target Build.VERSION_CODES#R the Z ordering can be changed
     * dynamically if the backing surface is created, otherwise it would be applied at surface
     * construction time.
     *
     * Calling this overrides any previous call to setZOrderMediaOverlay(boolean)
     *
     * @param onTop
     */
    fun setZOrderOnTop(onTop: Boolean) {
        try {
            removeView(surface)
            surface?.setZOrderOnTop(onTop)
            addView(surface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     *
     * Updates the display mode of this video view
     *
     * @param renderMode Sets the remote video display mode:
     *
     * - RENDER_MODE_HIDDEN(1): Uniformly scale the video until it fills the visible boundaries (cropped). One dimension of the video may have clipped contents.
     * - RENDER_MODE_FIT(2): Uniformly scale the video until one of its dimension fits the boundary (zoomed to fit). Areas that are not filled due to the disparity in the aspect ratio are filled with black.
     * - RENDER_MODE_ADAPTIVE(3): This mode is deprecated and Agora does not recommend using this mode.
     *
     */
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

    /**
     * Removes the surface view and adds it again.
     */
    fun reCreateSurface() {
        removeView(surface)
        surface = RtcEngine.CreateRendererView(context)
        addView(surface)
    }


    /**
     * Render the video feed of the specified UID
     *
     * @param uid User ID.
     */
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