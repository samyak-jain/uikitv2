package io.agora.agorauikit.ui.buttons

import android.content.Context
import android.util.AttributeSet
import io.agora.agorauikit.R
import io.agora.agorauikit.manager.AgoraRTC

class AudioButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AgoraButton(context, attrs, defStyleAttr) {

    private var buttonPressed: Boolean = false

    init {
        setImageResource(R.drawable.ic_mute_1)

        setOnClickListener {

            // Toggle mute local audio functionality
            AgoraRTC.instance().muteLocalAudioStream(!buttonPressed)
            buttonPressed = !buttonPressed
        }
    }
}