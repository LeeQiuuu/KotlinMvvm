package com.jimi.app.mvvm.modules.fence

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.jimi.app.common.C
import com.jimi.app.R
import com.jimi.app.common.*
import com.jimi.app.common.Functions.dip2px
import com.jimi.app.databinding.ActivityFenceApplyMvvmBinding
import com.jimi.app.entitys.GEOBean
import com.jimi.app.entitys.PackageModel
import com.jimi.app.mvvm.modules.fence.adapter.FenceApplyAdapter
import com.jimi.app.mvvm.base.BaseActivity
import com.jimi.app.utils.BuryingPointUtils
import com.jimi.app.views.LoadingView
import com.jimi.app.views.NavigationView
import com.jimi.app.mvvm.event.EventCode
import com.jimi.app.mvvm.event.EventMessage
import com.jimi.app.utils.LanguageUtil
import kotlinx.android.synthetic.main.activity_fence_apply_mvvm.*


/**
 * Description：更多页面设备围栏
 * Author: zengweidie
 * CreateDate: 2021/5/28 13:55
 * UpdateUser: 更新者
 * UpdateDate: 2021/5/28 13:55
 * UpdateRemark: 更新说明
 *
 */

class FenceApplyActivity : BaseActivity<FenceApplyViewModel, ActivityFenceApplyMvvmBinding>() ,FenceApplyAdapter.OnFenceApplyClickListener, LoadingView.onNetworkRetryListener{
    var list: ArrayList<GEOBean>? = null
    var adapter: FenceApplyAdapter? = null
    val imei: String ="868120244040629"


    override fun initView() {
        common_loadingview.visibility = View.VISIBLE
        common_loadingview.showLoadingView()
        common_loadingview.setNetworkRetryListener(this)
        list = ArrayList()
        adapter = FenceApplyAdapter(mContext, list!!,"1","1",imei)//宗旨那边的f1
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerView.adapter = adapter

        vm.getGeoSetInfo(SharedPre.mSharedPre.userID, imei,false)

        v.refreshLayout.setOnRefreshListener {//下拉刷新
            vm.getGeoSetInfo(SharedPre.mSharedPre.userID, imei,false)
        }
    }

    override fun initClick() {
    }

    override fun initData() {
    }

    override fun initVM() {
        vm.geoList.observe(this, Observer {
            v.refreshLayout.finishRefresh()
            v.refreshLayout.finishLoadMore()
            list!!.clear()
            if(it.isEmpty()){
                common_loadingview.visibility = View.VISIBLE
                common_loadingview.showNoResultData()
            }else{
                it?.let { it1 -> list!!.addAll(it1) }
                adapter!!.setData(it)
                adapter!!.notifyDataSetChanged()
                common_loadingview.visibility=View.GONE
            }

        })
        vm.isShowLoading.observe(this,Observer{
            if (it){

            }else{

            }
        })
    }

    /**
     * 接收消息
     */
    override fun handleEvent(msg: EventMessage) {
        super.handleEvent(msg)
        if (msg.code == EventCode.REFRESH) {
            showToast("主页：刷新")
            vm.getGeoSetInfo(SharedPre.mSharedPre.userID, imei,false)
        }
    }

    override fun initNavigationBar(navigationView: NavigationView) {
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(dip2px(this,25f), dip2px(this,25f))
        navigationView.visibility = View.VISIBLE
        navigationView.setShowBackButton(false)
        navigationView.setNavTitle(LanguageUtil.getInstance().getString(LanguageHelper.COMMON_FENCE))
        navigationView.setShowRightButton(false)
        //getNavigation().getRightButton().setText(R.string.creat);
        navigationView.setShowRightImage(true)
        navigationView.rightIv.layoutParams = params
        navigationView.rightIv.scaleType = ImageView.ScaleType.FIT_CENTER
        navigationView.rightIv.setImageResource(R.drawable.fence_new_create)
        navigationView.rightIv.setOnClickListener {
            BuryingPointUtils.onEvent(this@FenceApplyActivity, "c_app_tqzx_sbczxq_wlym_tjwlan")
            val bundle = Bundle()
            bundle.putInt("source", 4)
            bundle.putString(C.key.ACTION_IMEI,"868120244040629")
            val intent = Intent(this,FenceAddOrEditActivity::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, FenceAddOrEditActivity.REQUESTCODE,bundle)
        }
    }

    fun succCallBack(pModel: PackageModel<*>, geoid: String?, flag: String?, out: Boolean, `in`: Boolean) {
        if (pModel.code != 0) {
            adapter!!.onFailReturnCB(geoid, flag, out, `in`)
            if (pModel.code == 5019) {
                showToast(LanguageUtil.getInstance().getString(LanguageHelper.FENCE_ALARM_SET_FAIL), 5000)
                return
            }
        }
        showToast(RetCode.getCodeMsg(this, pModel.code))
    }

    fun failCallBack(geoid: String?, flag: String?, out: Boolean, `in`: Boolean) {
        showToast(LanguageUtil.getInstance().getString(LanguageHelper.COMMON_NETWORK_ERROR1))
        adapter!!.onFailReturnCB(geoid, flag, out, `in`)
    }

    override fun onFenceApply(isShow: Boolean) {
        if (isShow) {
            showProgressDialog("")
        } else {
            closeProgressDialog()
        }
    }

    override fun onNetworkRetryEvent() {
        vm.getGeoSetInfo(SharedPre.mSharedPre.userID, imei,false)
    }
}