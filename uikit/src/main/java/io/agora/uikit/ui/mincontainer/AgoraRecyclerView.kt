package io.agora.uikit.ui.mincontainer

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.IdRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.uikit.R
import io.agora.uikit.manager.AgoraRTC
import io.agora.uikit.manager.StateManager

class AgoraRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    val uidList: ArrayList<Int> = ArrayList()
    private val agoraAdapter: AgoraRecyclerViewAdapter = AgoraRecyclerViewAdapter(uidList)

    @IdRes
    private var maxReference: Int? = null

    // Event listener that will add/remove items from recycler view
    private var listener: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            uidList.add(uid)
            Log.d("AgoraRecyclerView", "Added to list $uidList")
            (context as Activity).runOnUiThread {
                agoraAdapter.setDataset(uidList)
                agoraAdapter.notifyItemInserted(agoraAdapter.itemCount - 1)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            val position: Int = uidList.indexOf(uid)
            uidList.remove(uid)
            (context as Activity).runOnUiThread {
                agoraAdapter.setDataset(uidList)
                agoraAdapter.notifyItemRemoved(position)
            }
        }
    }

    init {
        AgoraRTC.instance().registerListener(listener)

        val tAttributes = context.obtainStyledAttributes(attrs, R.styleable.AgoraRecyclerView)
        try {
            /*
             Get reference to the Max View in the layout with which the video will switch when user
             double taps on any min view
             */
            maxReference = tAttributes.getResourceId(R.styleable.AgoraRecyclerView_maxViewID, -1)
        } finally {
            tAttributes.recycle()
        }

        this.apply {
            layoutManager=AgoraLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter=agoraAdapter
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (maxReference != -1) {
            StateManager.maxReference = maxReference
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        AgoraRTC.instance().unregisterListener(listener)
    }
}