package com.jimi.app.mvvm.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.lang.ref.WeakReference


object ToastUtil {
    private lateinit var mToast: Toast
    private lateinit var weakReference: WeakReference<Context>
    private lateinit var myHandler: Handler


    fun init(context: Context) {
        weakReference = WeakReference(context)
        myHandler = Handler(Looper.getMainLooper())
    }

    fun show(message: String, duration: Int) {
        cancel()
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            weakReference.get()?.let {
                mToast = Toast.makeText(it, message, duration)
                mToast.show()
            }
        } else {
            myHandler.post {
                Runnable {
                    mToast = Toast.makeText(weakReference.get(), message, duration)
                    mToast.show()
                }
            }
        }
    }

    fun showShort(message: String?) {
        message?.let { show(message, Toast.LENGTH_SHORT) }
    }

    fun showLong(message: String?) {
        message?.let { show(message, Toast.LENGTH_LONG) }
    }


    fun cancel() {
        if (::mToast.isInitialized) {
            mToast.cancel()
        }
    }
}