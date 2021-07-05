package com.jimi.app.mvvm.modules.fence.adapter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.jimi.app.R
import com.jimi.app.common.C
import com.jimi.app.common.Functions
import com.jimi.app.common.LanguageHelper
import com.jimi.app.utils.LanguageUtil
import com.jimi.app.databinding.DeviceFenceAddItemBinding
import com.jimi.app.entitys.GEOBean
import com.jimi.app.entitys.GeomPoint
import com.jimi.app.modules.device.DeviceAddedActivity
import com.jimi.app.modules.device.RelevanceUserActivity
import com.jimi.app.mvvm.modules.fence.FenceAddOrEditActivity
import com.jimi.app.mvvm.modules.fence.FenceListManagerActivity
import com.jimi.app.mvvm.base.BaseAdapter
import com.jimi.app.protocol.ConnectServiceImpl
import com.jimi.app.utils.BuryingPointUtils
import com.jimi.app.views.AlertDialog
import com.jimi.map.MyLatLng


class FenceListManagerAdapter(context: Activity, listDatas: ArrayList<GEOBean>) :
        BaseAdapter<DeviceFenceAddItemBinding, GEOBean>(context, listDatas) {

    var clickListen: DeleteClick? = null


    override fun convert(v: DeviceFenceAddItemBinding, pGEOBean: GEOBean, position: Int) {
        v.deviceAddListItemEdit.text = LanguageUtil.getInstance().getString(LanguageHelper.COMMON_EDIT_TEXT)
        v.deviceAddListItemDel.text = LanguageUtil.getInstance().getString(LanguageHelper.COMMON_DELETE_ADD)

        if (pGEOBean.getFenceType()) {
            v.deviceAddListItemAdd.text = mContext.getString(R.string.user_add)
            v.deviceAddListItemAdd.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.user_guanlian, 0, 0)
            v.deviceAddListItemFamilyImg.setImageResource(R.drawable.device_list_item_car_user)
        } else {
            v.deviceAddListItemAdd.text = LanguageUtil.getInstance().getString(LanguageHelper.DEVICE_ADD)
            v.deviceAddListItemAdd.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.device_add_item_normal, 0, 0)
            v.deviceAddListItemFamilyImg.setImageResource(R.drawable.device_list_item_car)
        }

        v.deviceAddListItemFamilyName.text = pGEOBean.fenName
        v.deviceAddListItemFamilyTime.text = (LanguageUtil.getInstance().getString("check_fence_radiu") + pGEOBean.radius + LanguageUtil.getInstance().getString("fence_meter"))
        if (pGEOBean.addr == null || pGEOBean.addr.isEmpty()) {
            if (pGEOBean.points != null) {
                //根据坐标的获取地址
                val vPoint: GeomPoint = pGEOBean.points[0]
                val vMyLatLng = MyLatLng(vPoint.lat, vPoint.lng)
                if (Functions.getNetworkState(mContext) != Functions.NETWORN_NONE) {
                    val mLatLng = vMyLatLng.latitude.toString() + "," + vMyLatLng.longitude
                    ConnectServiceImpl.geocoderForBaiDu(mLatLng) { pAddress, latlng ->
                        if (pAddress.isNotEmpty()) {
                            v.deviceAddListItemFamilyImei.text = pAddress
                            pGEOBean.addr = pAddress
                        }
                    }
                }
            }
        } else {
            v.deviceAddListItemFamilyImei.text = pGEOBean.addr
        }

        if ("polygon".equals(pGEOBean.type, ignoreCase = true)) {
            v.deviceAddListItemFamilyTime.visibility = View.GONE
        } else {
            v.deviceAddListItemFamilyTime.visibility = View.VISIBLE
        }

        v.deviceAddListItemEdit.isEnabled = !"polygon".equals(pGEOBean.type, ignoreCase = true)

        v.run {
            deviceAddListItemEdit.setOnClickListener {
                BuryingPointUtils.onEvent(mContext, "c_app_tqzx_wd_wlgl_bjwlan")
                //跳转到编辑围栏页面
                val vIntent = Intent(mContext, FenceAddOrEditActivity::class.java)
                val vBundle = Bundle()
                vBundle.putString(C.key.ACTION_IMEI, "")
                vBundle.putSerializable(C.key.ACTION_GEOBEAN, pGEOBean)
                vBundle.putInt("source", 3)
                vIntent.putExtras(vBundle)
                (mContext as FenceListManagerActivity).startActivityForResult(vIntent, FenceAddOrEditActivity.REQUESTCODE)
            }
            deviceAddListItemAdd.setOnClickListener {
                if (pGEOBean.getFenceType()) {
                    val vIntent = Intent(mContext, RelevanceUserActivity::class.java)
                    vIntent.putExtra("fenceId", pGEOBean.fenceId)
                    (mContext as FenceListManagerActivity).startActivityForResult(vIntent, RelevanceUserActivity.REQUEST_RELEVANCE_CODE)
                } else {
                    //跳转到添加设备页面
                    val vIntent = Intent(mContext, DeviceAddedActivity::class.java)
                    vIntent.putExtra("geobean", pGEOBean)
                    vIntent.putExtra("isQueryNoExpired", true)
                    vIntent.putExtra("source", 2)
                    (mContext as FenceListManagerActivity).startActivityForResult(vIntent, FenceAddOrEditActivity.REQUESTCODE)
                }
            }
            deviceAddListItemDel.setOnClickListener {
                BuryingPointUtils.onEvent(mContext, "c_app_tqzx_wd_wlgl_scwlan")
                val alertDialog = AlertDialog(mContext as Activity?)
                alertDialog.createDialog()
                alertDialog.setMsg(LanguageUtil.getInstance().getString("sure_to_delete_fence"))
                alertDialog.setButtonOkTextColor(ContextCompat.getColor(mContext, R.color.common_blue))
                alertDialog.setWarnIconVisibity(true)
                alertDialog.setOkOnClickListener {
                    BuryingPointUtils.onEvent(mContext, "c_app_tqzx_wd_scwl_qran")
                    alertDialog.dismiss()
                    clickListen?.deleteClick(pGEOBean.fenceId)
                }
                alertDialog.setCancelOnClickListener {
                    BuryingPointUtils.onEvent(mContext, "c_app_tqzx_wd_scwl_qxan")
                    alertDialog.dismiss()
                }
                alertDialog.show()
            }
        }
    }

    interface DeleteClick {
        fun deleteClick(fenceId: String)
    }
}
