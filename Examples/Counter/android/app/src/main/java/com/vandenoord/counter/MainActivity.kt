package com.vandenoord.counter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.strohmNative.Strohm

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun reload(src: android.view.View) {
        Strohm.getInstance().reload()
    }
}