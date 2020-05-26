package io.agora.agorauikit.ui.buttons

import android.content.Context
import android.util.AttributeSet
import io.agora.agorauikit.R
import io.agora.agorauikit.manager.AgoraRTC

class SwitchCameraButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AgoraButton(context, attrs, defStyleAttr) {

    init {
        this.setImageResource(R.drawable.switch_camera)

        this.setOnClickListener {
            AgoraRTC.instance()._sdk?.switchCamera()
        }
    }

}