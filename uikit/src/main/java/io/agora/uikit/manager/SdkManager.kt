package io.agora.uikit.manager

import android.content.Context

abstract class SdkManager<Sdk> {
    var sdk: Sdk? = null
    var channel: String? = null

    abstract fun bootstrap(context: Context, appId: String, channel: String)

    fun start(context: Context?, appId: String?) {
        sdk = try {
            createSDK(context, appId)
        } catch (e: Exception) {
            throw RuntimeException(e.message)
        }
        configSdk()
    }

    @Throws(java.lang.RuntimeException::class)
    protected abstract fun createSDK(context: Context?, appId: String?): Sdk
    protected abstract fun configSdk()
    abstract fun joinChannel(data: Map<String?, String?>?)
    abstract fun leaveChannel()
    abstract fun requestPermissions(context: Context): Boolean
    abstract fun destroySdk()
    fun release() {
        leaveChannel()
        destroySdk()
        sdk = null
    }

    companion object {
        const val TOKEN = "token"
        const val CHANNEL_ID = "channelId"
        const val USER_ID = "userId"
    }
}