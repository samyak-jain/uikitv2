package io.agora.agorauikit.ui.mincontainer

import android.app.Activity
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import io.agora.agorauikit.manager.StateManager
import io.agora.agorauikit.ui.AgoraView
import io.agora.agorauikit.ui.DPToPx


class AgoraRecyclerViewAdapter(private var uidList: ArrayList<Int>): RecyclerView.Adapter<AgoraRecyclerViewAdapter.AgoraViewHolder>() {

    class AgoraViewHolder(val agoraView: AgoraView): RecyclerView.ViewHolder(agoraView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgoraViewHolder {
        val agoraView = AgoraView(parent.context)
        val size = DPToPx(parent.context, 4)
        val lp = FrameLayout.LayoutParams(parent.height - size, parent.height - size)
        lp.setMargins(size)
        agoraView.layoutParams = lp
        agoraView.cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, parent.context.resources.displayMetrics)
        agoraView.setDoubleTapListener(GestureDetector(parent.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                StateManager.maxReference?.let {
                    val av = parent.rootView.findViewById<AgoraView>(it)
                    val oldUID = agoraView.mUID
                    val newUID = av.mUID

                    val position = uidList.indexOf(oldUID)
                    uidList[position] = newUID

                    (parent.context as Activity).runOnUiThread {
                        notifyDataSetChanged()
                        av.reCreateSurface()
                        av.setUID(oldUID)
                    }

                }

                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }
        }))

        return AgoraViewHolder(
            agoraView
        )
    }

    fun setDataset(newUidList: ArrayList<Int>) {
        uidList = newUidList
    }

    override fun getItemCount() = uidList.size

    override fun onBindViewHolder(holder: AgoraViewHolder, position: Int) {
        holder.agoraView.setUID(uidList[position])
    }

}