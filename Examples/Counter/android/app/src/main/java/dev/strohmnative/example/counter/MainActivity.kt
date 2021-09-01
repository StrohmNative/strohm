package dev.strohmnative.example.counter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dev.strohmnative.example.counter.databinding.ActivityMainBinding
import dev.strohmnative.StatusChangeListener
import dev.strohmnative.StrohmNative
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var subscription: UUID? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.txtCounter.setOnEditorActionListener { v, _, _ -> onEnterCount(v) }
    }

    override fun onStart() {
        super.onStart()
        // Setup StrohmNative in onStart so that the status can be displayed using the bindings.
        // In onCreate the bindings don't work yet. Normally you can do this in onCreate.
        StrohmNative.getInstance(applicationContext, onStatusChange)
    }

    private val onStatusChange: StatusChangeListener = { _, _, new ->
        runOnUiThread {
            binding.txtStrohmStatus.text = new.rawValue
        }
    }

    fun decrement(@Suppress("UNUSED_PARAMETER") src: View) {
        StrohmNative.getInstance().dispatch("decrement")
    }

    fun increment(@Suppress("UNUSED_PARAMETER") src: View) {
        StrohmNative.getInstance().dispatch("increment")
    }

    private fun setCounter(count: Int) {
        StrohmNative.getInstance().dispatch("setCounter", mapOf("count" to count))
    }

    fun reload(@Suppress("UNUSED_PARAMETER") src: View) {
        StrohmNative.getInstance().reload()
        subscription = null
        binding.txtCounter.setText("0")
        binding.txtSubscribed.text = getString(R.string.strFalse)
    }

    fun subscribe(@Suppress("UNUSED_PARAMETER") src: View) {
        StrohmNative.getInstance().subscribe(mapOf("count" to listOf()),
            handler = { props ->
                val count = (props["count"] as Number).toInt()
                runOnUiThread {
                    binding.txtCounter.setText("$count")
                }
            },
            completion = { subscription ->
                this.subscription = subscription
                binding.txtSubscribed.text = getString(R.string.strTrue)
            })
    }

    fun unsubscribe(@Suppress("UNUSED_PARAMETER") src: View) {
        subscription?.let { StrohmNative.getInstance().unsubscribe(it) }
        subscription = null
        binding.txtSubscribed.text = getString(R.string.strFalse)
    }

    private fun onEnterCount(v: TextView?): Boolean {
        Log.d("app", "onEditorAction")
        hideSoftKeyBoard()
        v?.clearFocus()
        if (v?.text != null) {
            val count = Integer.valueOf(v.text.toString())
            setCounter(count.toInt())
        }
        return true
    }

    private fun hideSoftKeyBoard() {
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) {
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}
