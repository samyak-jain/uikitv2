package io.agora.agorauikit.manager

import android.content.Context

abstract class SdkManager<Sdk> {
    var _sdk: Sdk? = null
    var channel: String? = null

    abstract fun bootstrap(context: Context, appId: String, channel: String)

    fun start(context: Context?, appId: String?) {
        _sdk = try {
            createSDK(context, appId)
        } catch (e: Exception) {
            throw RuntimeException(e.message)
        }
        configSdk()
    }

    fun getSDK(): Sdk {
        return _sdk!!
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
        _sdk = null
    }

    companion object {
        const val TOKEN = "token"
        const val CHANNEL_ID = "channelId"
        const val USER_ID = "userId"
    }
}