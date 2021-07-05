package com.jimi.app.mvvm.event

import de.greenrobot.event.EventBus


object Event {
    fun getInstance(): EventBus {
        return EventBus.getDefault()
    }
}