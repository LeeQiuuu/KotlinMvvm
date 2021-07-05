package com.jimi.app.mvvm.widget

import android.os.SystemClock
import android.view.View
import com.jimi.app.mvvm.widget.ViewClickDelay.SPACE_TIME
import com.jimi.app.mvvm.widget.ViewClickDelay.hash
import com.jimi.app.mvvm.widget.ViewClickDelay.lastClickTime

object ViewClickDelay {
    var hash: Int = 0
    var lastClickTime: Long = 0
    var SPACE_TIME: Long = 1000
}

infix fun View.clicks(clickAction: () -> Unit) {
    this.setOnClickListener {
        if (this.hashCode() != hash) {
            hash = this.hashCode()
            lastClickTime = SystemClock.uptimeMillis()
            clickAction()
        } else {
            val currentTime = SystemClock.uptimeMillis()
            if (currentTime - lastClickTime > SPACE_TIME) {
                lastClickTime = SystemClock.uptimeMillis()
                clickAction()
            }
        }
    }
}