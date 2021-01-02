package com.vandenoord.counter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dev.strohmNative.Strohm

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val txtCounter = findViewById<EditText>(R.id.txtCounter)
        txtCounter.setOnEditorActionListener { v, _, _ -> onEnterCount(v) }
    }

    fun decrement(src: View) {
        Strohm.getInstance().dispatch("decrement")
    }

    fun increment(src: View) {
        Strohm.getInstance().dispatch("increment")
    }

    fun setCounter(count: Int) {
        Strohm.getInstance().dispatch("setCounter", mapOf("count" to count))
    }

    fun reload(src: View) {
        Strohm.getInstance().reload()
    }

    fun subscribe(src: View) {
    }

    fun unsubscribe(src: View) {

    }

    private fun onEnterCount(v: TextView?): Boolean {
        Log.d("app", "onEditorAction")
        hideSoftKeyBoard()
        v?.clearFocus()
        if (v?.text != null) {
            val count = Integer.valueOf(v.text as String)
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