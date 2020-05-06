package io.agora.uikit.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import io.agora.uikit.manager.AgoraRTC

class AgoraChannelTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        text = AgoraRTC.instance()?.channel
    }

}