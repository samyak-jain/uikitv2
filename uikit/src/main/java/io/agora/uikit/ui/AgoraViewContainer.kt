package io.agora.uikit.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.util.AttributeSet
import android.view.GestureDetector
import android.widget.FrameLayout
import io.agora.uikit.R

open class AgoraViewContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var cornerRadius: Float = 0F
    private val stencilPath: Path = Path()

    init {
        val tAttributes = context.obtainStyledAttributes(attrs, R.styleable.AgoraViewContainer)
        try {
            cornerRadius = tAttributes.getDimension(R.styleable.AgoraViewContainer_corner_radius, 0F)
        } finally {
            tAttributes.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        stencilPath.reset()
        stencilPath.addRoundRect(0F, 0F, w.toFloat(), h.toFloat(), cornerRadius, cornerRadius, Path.Direction.CW)
        stencilPath.close()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        val savedCanvas = canvas?.save()
        canvas?.clipPath(stencilPath)
        super.dispatchDraw(canvas)
        savedCanvas?.let { canvas.restoreToCount(it) }
    }

    fun setDoubleTapListener(gd: GestureDetector) {
        setOnTouchListener {
                _, event -> gd.onTouchEvent(event)
        }
    }

}