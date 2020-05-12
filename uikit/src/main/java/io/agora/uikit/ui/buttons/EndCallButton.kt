package io.agora.uikit.ui.buttons

import android.content.Context
import android.util.AttributeSet
import io.agora.rtc.RtcEngine
import io.agora.uikit.R
import io.agora.uikit.manager.AgoraRTC

class EndCallButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AgoraButton(context, attrs, defStyleAttr) {

    init {
        this.setImageResource(R.drawable.end_call)
        background = context.getDrawable(R.drawable.end_button_background)

        this.setOnClickListener {

            // Leave the channel and destroy the RtcEngine
            AgoraRTC.instance().leaveChannel()
            RtcEngine.destroy()
        }
    }
}