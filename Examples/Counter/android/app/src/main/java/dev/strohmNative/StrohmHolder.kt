package dev.strohmNative

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

class StrohmHolder : FrameLayout {
    private var strohm: Strohm

    constructor(context: Context) : super(context) {
        strohm = Strohm.getInstance(context)
        strohm.install("main.js", 8080)

        addView(strohm.webView)

        val layoutParams = strohm.webView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        strohm.webView.layoutParams = layoutParams
        strohm.webView.requestLayout()
    }

    @Suppress("UNUSED_PARAMETER")
    constructor(context: Context, attrs: AttributeSet): this(context)
}