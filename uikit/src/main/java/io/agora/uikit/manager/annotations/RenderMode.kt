package io.agora.uikit.manager.annotations

import androidx.annotation.IntDef
import io.agora.rtc.video.VideoCanvas


@IntDef(VideoCanvas.RENDER_MODE_HIDDEN, VideoCanvas.RENDER_MODE_FIT)
@Retention(AnnotationRetention.SOURCE)
annotation class RenderMode