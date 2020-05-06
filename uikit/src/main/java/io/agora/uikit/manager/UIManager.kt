package io.agora.uikit.manager

import android.content.Context
import android.view.ContextThemeWrapper
import io.agora.uikit.R

class UIManager private constructor(base: Context) {
    val buttonLayout = ContextThemeWrapper(base.applicationContext, R.style.ButtonStyle)

    companion object {
        private var instance: UIManager? = null
        fun instance(base: Context): UIManager? {
            if (instance == null) {
                synchronized(
                    UIManager::class.java
                ) {
                    if (instance == null) instance = UIManager(base)
                }
            }

            return instance
        }
    }
}