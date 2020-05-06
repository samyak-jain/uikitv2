package io.agora.uikit.ui.buttons

import android.content.Context
import android.util.AttributeSet
import io.agora.uikit.R
import io.agora.uikit.manager.AgoraRTC

class AudioButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AgoraButton(context, attrs, defStyleAttr) {

    private var buttonPressed: Boolean = false

    init {
        setImageResource(R.drawable.ic_mute_1)

        setOnClickListener {
            AgoraRTC.instance()?.muteLocalAudioStream(!buttonPressed)
            buttonPressed = !buttonPressed
        }
    }
}