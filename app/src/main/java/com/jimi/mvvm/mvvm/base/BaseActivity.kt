package com.jimi.app.mvvm.base

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import com.jimi.app.MainApplication
import com.jimi.app.R
import com.jimi.app.utils.LanguageUtil
import com.jimi.app.mvvm.net.error.ErrorResult
import com.jimi.app.mvvm.utils.ToastUtil
import com.jimi.app.views.NavigationView
import com.jimi.app.views.WaitProgressDialog
import com.jimi.app.views.swipbacklayout.SwipeBackActivity
import com.jimi.app.views.swipbacklayout.SwipeBackLayout
import com.jimi.app.mvvm.event.EventCode
import com.jimi.app.mvvm.event.EventMessage
import com.jimi.app.utils.LogUtil
import com.luck.picture.lib.rxbus2.Subscribe
import com.umeng.analytics.MobclickAgent
import de.greenrobot.event.EventBus
import java.lang.reflect.ParameterizedType

/**
 *Created by LeeQiuuu on 2021/4/23.
 *Describe:Activity基础类
 */

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : SwipeBackActivity() {
    lateinit var mContext: FragmentActivity
    lateinit var vm: VM
    lateinit var v: VB
    protected var mNavigation: NavigationView? = null

    private var loadingDialog: ProgressDialog? = null
    private var languageUtil: LanguageUtil = LanguageUtil()
    protected var mProgressDialog: WaitProgressDialog? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //统一右滑返回
        setSwipeBackEnable(true)
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)
        MainApplication.getInstance().addActivity(this)
        //获取viewModel实例
        val type = javaClass.genericSuperclass as ParameterizedType
        val clazz1 = type.actualTypeArguments[0] as Class<VM>
        vm = ViewModelProvider(this).get(clazz1)
        //获取viewbinding实例
        val clazz2 = type.actualTypeArguments[1] as Class<VB>
        val method = clazz2.getMethod("inflate", LayoutInflater::class.java)
        v = method.invoke(null, layoutInflater) as VB

        setContentView(v.root)
        mNavigation = findViewById(R.id.nav_bar)
        mNavigation?.let {
            it.setOnBackClickListener { finish() }
            initNavigationBar(it)
            setStatusBarView(it.getmStatusBar())
        }
        setBarScreen()
        mContext = this
        init()
        initView()
        initClick()
        initData()
        initVM()
//        LogUtil.e(getClassName())
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this) //统计时长
        classLog("onResume")
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
        classLog("onPause")
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    //事件传递
    @Subscribe
    fun onEventMainThread(msg: EventMessage) {
        handleEvent(msg)
    }

    abstract fun initView()

    abstract fun initClick()

    abstract fun initData()

    abstract fun initVM()

    private fun init() {
        EventBus.getDefault().register(this)
        //loading
        vm.isShowLoading.observe(this, Observer {
            if (it) showProgressDialog("") else closeProgressDialog()
        })
        //错误信息
        vm.errorData.observe(this, Observer {
            if (it.show) showToast(it.errMsg)
            errorResult(it)
        })
        //空布局
        vm.isShowEmpty.observe(this, Observer {

        })
    }

    fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = ProgressDialog(this)
        }
        loadingDialog!!.show()
    }

    fun dismissLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    /**
     * 消息、事件接收回调
     */
    open fun handleEvent(msg: EventMessage) {
        if (msg.code == EventCode.LOGIN_OUT) {
            finish()
        }
    }

    /**
     * 接口请求错误回调
     */
    open fun errorResult(errorResult: ErrorResult) {}

    open fun classLog(pMsg: String?) {
        LogUtil.e(this.javaClass.simpleName, pMsg)
    }

    open fun getNavigation(): NavigationView? {
        return mNavigation
    }

    /**
     * Activity可以自定义初始化导航条
     */
    open fun initNavigationBar(navigationView: NavigationView) {

    }

    /**
     * 适配view顶到状态栏添加高度
     */
    open fun setStatusBarView(view: View?) {
        ImmersionBar.setStatusBarView(this, view)
    }

    /**
     * 状态栏的字体是否变色
     */
    protected open fun isBarDarkFont(): Boolean {
        return true
    }

    /**
     * 全屏显示
     */
    protected open fun setBarScreen() {
        val immersionBar = ImmersionBar.with(this)
                .fitsSystemWindows(false)
                .transparentStatusBar()
        if (isBarDarkFont()) immersionBar.statusBarDarkFont(true, 0.2f)
        immersionBar.init()
    }

    open fun showToast(msg: String?) {
        showToast(msg, 1000)
    }

    open fun showToast(resId: Int) {
        showToast(resId, 1000)
    }

    open fun showToast(msg: String?, duration: Int) {
        msg?.let {
            ToastUtil.show(msg, duration)
        }
    }

    open fun showToast(resId: Int, duration: Int) {
        ToastUtil.show(getString(resId), duration)
    }

    open fun showProgressDialog(pMsg: String) {
        showProgressDialog(pMsg, true)
    }

    /**
     * 显示进度对话框
     *
     * @param pMsg         进度框显示的文字
     * @param isCancelable 是否可以按返回键关闭进度框
     */
    open fun showProgressDialog(pMsg: String, isCancelable: Boolean) {
        var pMsg = pMsg
        closeProgressDialog()
        if (pMsg == "") pMsg = languageUtil.getString("common_sending_request")
        mProgressDialog = WaitProgressDialog()
        mProgressDialog?.show(this, pMsg, isCancelable)
    }

    open fun showProgressDialog(pResId: Int) {
        val vMsg = getString(pResId)
        showProgressDialog(vMsg, true)
    }

    open fun showProgressDialog(pResId: Int, isCancelable: Boolean) {
        val vMsg = getString(pResId)
        showProgressDialog(vMsg, isCancelable)
    }

    /**
     * 关闭进度对话框
     */
    open fun closeProgressDialog() {
        mProgressDialog?.dismiss()
    }
}