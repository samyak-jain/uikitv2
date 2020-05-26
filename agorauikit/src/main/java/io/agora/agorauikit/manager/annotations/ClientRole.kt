package io.agora.agorauikit.manager.annotations

import androidx.annotation.IntDef
import io.agora.rtc.Constants


@IntDef(Constants.CLIENT_ROLE_BROADCASTER, Constants.CLIENT_ROLE_AUDIENCE)
@Retention(AnnotationRetention.SOURCE)
annotation class ClientRole