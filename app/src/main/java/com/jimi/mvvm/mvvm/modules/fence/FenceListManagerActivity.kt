package com.jimi.app.mvvm.modules.fence

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.jimi.app.R
import com.jimi.app.common.C
import com.jimi.app.common.LanguageHelper
import com.jimi.app.utils.LanguageUtil
import com.jimi.app.common.SharedPre
import com.jimi.app.databinding.DeviceFenceManagerActivityBinding
import com.jimi.app.entitys.GEOBean
import com.jimi.app.modules.device.RelevanceUserActivity
import com.jimi.app.mvvm.modules.fence.adapter.FenceListManagerAdapter
import com.jimi.app.mvvm.modules.fence.adapter.FenceListManagerAdapter.DeleteClick
import com.jimi.app.mvvm.base.BaseActivity
import com.jimi.app.mvvm.utils.SysUtils
import com.jimi.app.utils.BuryingPointUtils
import com.jimi.app.views.LoadingView
import com.jimi.app.views.NavigationView
import com.jimi.app.mvvm.event.EventCode
import com.jimi.app.mvvm.event.EventMessage
import kotlinx.android.synthetic.main.device_fence_manager_activity.*
import kotlinx.android.synthetic.main.fence_manger_list.view.*

/**
 *Created by LeeQiuuu on 2021/4/23.
 *Describe:引用的两个参数分别为ViewModel和databinding
viewModel不需要的时候使用EmptyModel,databinding会根据新建xml自动生成，智能提示输入即可找到
 */

class FenceListManagerActivity : BaseActivity<FenceListManagerViewModel, DeviceFenceManagerActivityBinding>(), LoadingView.onNetworkRetryListener {

    var list: ArrayList<GEOBean>? = null
    var adapter: FenceListManagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inLay.common_loadingview.visibility = View.VISIBLE
        inLay.common_loadingview.showLoadingView()
    }

    override fun initView() {
        inLay.common_loadingview.setNetworkRetryListener(this)
        list = ArrayList()
        inLay.mRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FenceListManagerAdapter(mContext, list!!)
        inLay.mRecyclerView.adapter = adapter
        getData()
        inLay.refreshLayout.setOnRefreshListener {//下拉刷新
            getData()
        }

    }

    override fun initClick() {
        adapter!!.clickListen = object : DeleteClick {
            override fun deleteClick(fenceId: String) {
                vm.deleteGeo(fenceId, true)
            }
        }
        adapter!!.itemClick(fun(it: Int) {
            // startActivity(Intent(mContext, TestEventActivity::class.java))
        })
    }

    override fun initData() {

    }

    override fun initVM() {
        vm.geoList.observe(this, Observer {
            inLay.refreshLayout.finishRefresh()
            list!!.clear()
            it?.let { it1 ->
                list!!.addAll(it1)
                inLay.common_loadingview.visibility = View.GONE
            }
            adapter!!.notifyDataSetChanged()
            if (it.isEmpty()) {
                inLay.common_loadingview.visibility = View.VISIBLE
                inLay.common_loadingview.showNoResultData()
            }
        })
        vm.isShowLoading.observe(this, Observer {
            if (it) {

            } else {

            }
        })
        vm.errorData.observe(this, Observer {
            showToast(it.errMsg)
            inLay.common_loadingview.visibility = View.VISIBLE
            inLay.common_loadingview.showNetworkError()
            inLay.refreshLayout.finishRefresh()
        })
        vm.addGeoTag.observe(this, Observer {
            if (it) showToast(R.string.device_add_success_added)
            else showToast(R.string.device_add_success_added)
            inLay.refreshLayout.autoRefresh()
        })
        vm.delGeoTag.observe(this, Observer {
            if (it) {
                showToast(R.string.delete_succes)
            } else {
                showToast(R.string.delete_fail)
            }
            inLay.refreshLayout.autoRefresh()
        })
    }

    /**
     * 接收消息
     */
    override fun handleEvent(msg: EventMessage) {
        super.handleEvent(msg)
        if (msg.code == EventCode.REFRESH) {
            showToast("主页：刷新")
            getData()
        }
    }

    override fun initNavigationBar(navigationView: NavigationView) {
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(SysUtils.dp2Px(this, 25f), SysUtils.dp2Px(this, 25f))
        navigationView.setShowBackButton(false)
        navigationView.setOnBackClickListener {
            BuryingPointUtils.onEvent(this, "c_app_tqzx_wd_wlgl_fhan")
            finish()
        }
        navigationView.setNavTitle(LanguageUtil.getInstance().getString(LanguageHelper.COMMON_FENCE_MANAGER_TEXT))
        navigationView.setShowRightImage(true)
        navigationView.setShowRightButton(false)
        navigationView.rightIv.layoutParams = params
        navigationView.rightIv.scaleType = ImageView.ScaleType.FIT_CENTER
        navigationView.rightIv.setImageResource(R.drawable.fence_new_create)
        navigationView.rightIv.setOnClickListener {
            BuryingPointUtils.onEvent(this, "c_app_tqzx_wd_wlgl_tjwltb")
            val vBundle = Bundle()
            vBundle.putString(C.key.ACTION_IMEI, "")
            vBundle.putInt("source", 1)
            val intent = Intent(this, FenceAddOrEditActivity::class.java)
            intent.putExtras(vBundle)
            startActivityForResult(intent, FenceAddOrEditActivity.REQUESTCODE)
        }

    }

    override fun onNetworkRetryEvent() {
        getData()
    }

    fun getData() {
        vm.getGeoSetInfo(SharedPre.mSharedPre.userID, "", false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FenceAddOrEditActivity.REQUESTCODE
                && resultCode == FenceAddOrEditActivity.RESULTCODE) {
            //更新数据
            inLay.refreshLayout.autoRefresh()
        } else if (requestCode == RelevanceUserActivity.REQUEST_RELEVANCE_CODE &&
                resultCode == RelevanceUserActivity.RESULT_RELEVANCE_CODE) {
            showProgressDialog(LanguageUtil.getInstance().getString(LanguageHelper.SAVING_HINT))
            data?.getStringExtra("fenceId")?.let { addUserOnFence(it, data.getStringExtra(C.key.ACTION_USERID)) }
        }
    }

    private fun addUserOnFence(geoId: String, userIds: String) {
        vm.addGeo(geoId, userIds, true)
    }
}