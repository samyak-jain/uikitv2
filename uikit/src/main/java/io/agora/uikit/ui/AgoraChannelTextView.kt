package io.agora.uikit.ui

import android.content.Context
import android.util.AttributeSet
import io.agora.uikit.manager.AgoraRTC

class AgoraChannelTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        // Set the content of the TextView to the name of the current Channel
        text = AgoraRTC.instance().channel
    }

}