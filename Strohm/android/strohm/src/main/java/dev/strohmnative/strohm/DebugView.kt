package dev.strohmnative.strohm

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout

class DebugView : FrameLayout {
    private var strohm: Strohm

    constructor(context: Context) : super(context) {
        strohm = Strohm.getInstance(context)

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