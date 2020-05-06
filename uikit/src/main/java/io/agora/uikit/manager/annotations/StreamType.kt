package io.agora.uikit.manager.annotations

import androidx.annotation.IntDef
import io.agora.rtc.Constants


@IntDef(Constants.VIDEO_STREAM_HIGH, Constants.VIDEO_STREAM_LOW)
@Retention(AnnotationRetention.SOURCE)
annotation class StreamType