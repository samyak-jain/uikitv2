package io.agora.uikit.ui.mincontainer

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.view.setPadding
import androidx.core.widget.TextViewCompat
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.uikit.R
import io.agora.uikit.manager.AgoraRTC
import io.agora.uikit.ui.DPToPx

class RemoteCountHolder @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    var count = 0
    val listener = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            count++
            text = "+$count"
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            count--
            text = "+$count"
        }
    }

    init {
        background = context.getDrawable(R.drawable.remote_count_holder_background)
        text = "-"
        gravity = Gravity.CENTER
        TextViewCompat.setAutoSizeTextTypeWithDefaults(this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
        setTextColor(Color.parseColor("#d7cec8"))
        setPadding(DPToPx(context, 13))
        AgoraRTC.instance()?.registerListener(listener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        AgoraRTC.instance()?.unregisterListener(listener)
    }
}