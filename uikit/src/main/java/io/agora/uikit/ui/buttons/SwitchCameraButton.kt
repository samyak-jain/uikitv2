package io.agora.uikit.ui.buttons

import android.content.Context
import android.util.AttributeSet
import io.agora.uikit.R
import io.agora.uikit.manager.AgoraRTC

class SwitchCameraButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AgoraButton(context, attrs, defStyleAttr) {

    init {
        this.setImageResource(R.drawable.switch_camera)

        this.setOnClickListener {
            AgoraRTC.instance().sdk?.switchCamera()
        }
    }

}