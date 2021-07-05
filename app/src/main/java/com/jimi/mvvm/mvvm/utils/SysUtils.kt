package com.jimi.app.mvvm.utils

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.pm.PackageInfoCompat
import com.jimi.app.R
import com.jimi.app.mvvm.App
import java.io.File


object SysUtils {

    @JvmStatic
    fun dp2Px(context: Context, dp: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    @JvmStatic
    fun px2Dp(context: Context, px: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (px / scale + 0.5f).toInt()
    }

    // 获取当前APP名称
    @Deprecated("不推荐使用，推荐使用context.getString(R.string.app_name)")
    fun getAppName(context: Context): String {
        val packageManager = context.packageManager
        val applicationInfo: ApplicationInfo
        applicationInfo = try {
            packageManager.getApplicationInfo(context.packageName, 0)
        } catch (e: java.lang.Exception) {
            return context.resources.getString(R.string.app_name)
        }
        return packageManager.getApplicationLabel(applicationInfo).toString()
    }

    @Deprecated("不推荐使用，推荐使用BuildConfig.VERSION_NAME")
    fun getAppVersion(): String? {
        val context: Context = App.instance
        val manager: PackageManager = context.packageManager
        return try {
            val info: PackageInfo = manager.getPackageInfo(context.packageName, 0)
            info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "1.0.0"
        }
    }

    @Deprecated("不推荐使用，推荐使用BuildConfig.VERSION_CODE")
    fun getAppVersionCode(): Long {
        val context: Context = App.instance
        val manager: PackageManager = context.packageManager
        return try {
            val info: PackageInfo = manager.getPackageInfo(context.packageName, 0)
            //Android P 之后，versionCode过时。改用getLongVersionCode
            PackageInfoCompat.getLongVersionCode(info)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            1
        }
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    fun getSystemModel() = Build.MODEL

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    fun getDeviceBrand() = Build.BRAND

    @Deprecated("谷歌要求使用getExternalFilesDir()，并且在android 10上，Environment.getExternalStorageDirectory()过时失效，有可能会返回空")
    fun initFiles() {
        var file = File(Environment.getExternalStorageDirectory(), "MVVM/data")
        if (!file.exists()) file.mkdirs()
        file = File(Environment.getExternalStorageDirectory(), "MVVM/images")
        if (!file.exists()) file.mkdirs()
        file = File(Environment.getExternalStorageDirectory(), "MVVM/download")
        if (!file.exists()) file.mkdirs()
    }

    @Deprecated("方法复杂且已经过时")
    fun getScreenWidth(activity: Activity): Int {
        var width = 0
        val windowManager = activity.windowManager
        val display = windowManager.defaultDisplay
        width = display.width
        return width
    }

    @Deprecated("方法复杂且已经过时")
    fun getScreenHeight(activity: Activity): Int {
        var height = 0
        val windowManager = activity.windowManager
        val display = windowManager.defaultDisplay
        height = display.height
        return height
    }

    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels;
    }

    @JvmStatic
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels;
    }
}