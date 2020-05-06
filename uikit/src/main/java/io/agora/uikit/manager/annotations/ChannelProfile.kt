package io.agora.uikit.manager.annotations

import androidx.annotation.IntDef
import io.agora.rtc.Constants


@IntDef(
    Constants.CHANNEL_PROFILE_COMMUNICATION,
    Constants.CHANNEL_PROFILE_LIVE_BROADCASTING,
    Constants.CHANNEL_PROFILE_GAME
)
@Retention(AnnotationRetention.SOURCE)
annotation class ChannelProfile