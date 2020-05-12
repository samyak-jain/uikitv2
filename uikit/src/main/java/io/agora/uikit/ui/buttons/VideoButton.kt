package io.agora.uikit.ui.buttons

import android.content.Context
import android.util.AttributeSet
import io.agora.uikit.R
import io.agora.uikit.manager.AgoraRTC

class VideoButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AgoraButton(context, attrs, defStyleAttr) {

    private var buttonPressed: Boolean = false

    init {
        setImageResource(R.drawable.ic_video_off)

        setOnClickListener {

            // Toggle mute local Video Stream
            AgoraRTC.instance().muteLocalVideoStream(!buttonPressed)
            buttonPressed = !buttonPressed
        }
    }
}