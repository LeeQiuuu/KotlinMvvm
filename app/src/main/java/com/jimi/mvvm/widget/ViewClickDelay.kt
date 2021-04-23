package com.jimi.mvvm.widget

import android.view.View
import com.jimi.mvvm.widget.ViewClickDelay.SPACE_TIME
import com.jimi.mvvm.widget.ViewClickDelay.hash
import com.jimi.mvvm.widget.ViewClickDelay.lastClickTime

object ViewClickDelay {
    var hash: Int = 0
    var lastClickTime: Long = 0
    var SPACE_TIME: Long = 1000
}

infix fun View.clicks(clickAction: () -> Unit) {
    this.setOnClickListener {
        if (this.hashCode() != hash) {
            hash = this.hashCode()
            lastClickTime = System.currentTimeMillis()
            clickAction()
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > SPACE_TIME) {
                lastClickTime = System.currentTimeMillis()
                clickAction()
            }
        }
    }
}