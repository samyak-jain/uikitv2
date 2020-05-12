package io.agora.uikit.manager

import androidx.annotation.IdRes

/**
 * Contains the global state of the Application
 *
 * maxReference: Contains the resource id of the layout of the max view
 */
object StateManager {
    @JvmStatic
    @IdRes
    var maxReference: Int? = null
}