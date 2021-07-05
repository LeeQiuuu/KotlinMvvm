package com.jimi.app.mvvm.modules.fence.adapter

import android.app.Activity
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jimi.app.R
import com.jimi.app.common.*
import com.jimi.app.databinding.DeviceFenceApplicationItemBinding
import com.jimi.app.entitys.GEOBean
import com.jimi.app.entitys.GeomPoint
import com.jimi.app.entitys.PackageModel
import com.jimi.app.modules.device.DelayAlarmSettingDialog
import com.jimi.app.modules.device.DelayAlarmSettingDialog.OnDelayAlarmSettingListener
import com.jimi.app.mvvm.base.BaseAdapter
import com.jimi.app.protocol.ConnectServiceImpl
import com.jimi.app.protocol.TObserver
import com.jimi.app.utils.*
import com.jimi.map.MyLatLng
import java.util.*
import kotlin.collections.HashSet

/**
 * Description:
 * Author: zengweidie
 * CreateDate: 2021/5/28 14:02
 * UpdateUser: 更新者
 * UpdateDate: 2021/5/28 14:02
 * UpdateRemark: 更新说明
 *
 */

class FenceApplyAdapter(context: Activity, listDatas: ArrayList<GEOBean>, pExpireFlag: String, pActivationFlag: String, mImei: String) :
        BaseAdapter<DeviceFenceApplicationItemBinding, GEOBean>(context, listDatas), OnDelayAlarmSettingListener {

    private var inSet = HashSet<String>()
    private var outSet = HashSet<String>()
    private var delayAlarmSettingDialog: DelayAlarmSettingDialog? = null
    var mImei: String
    private val pActivationFlag: String
    private val pExpireFlag: String

    var onFenceApplyClickListener: OnFenceApplyClickListener? = null

    init {
        this.mImei = mImei
        this.pActivationFlag = pActivationFlag
        this.pExpireFlag = pExpireFlag
        /* this.inSet= HashSet()
         this.outSet= HashSet()*/
        delayAlarmSettingDialog = DelayAlarmSettingDialog(mContext)
        delayAlarmSettingDialog!!.setOnDelayAlarmSettingListener(this)
    }

    fun setData(datas: List<GEOBean>?) {
        if (datas != null && datas[0].configureList != null && datas[0].configureList
                        .size > 0) {
            mImei = datas[0].configureList[0].imei
        }
        addFlag(datas!!)
    }

    /**
     * 为进出添加本地标记，防止显示错乱
     *
     * @param datas
     */
    private fun addFlag(datas: List<GEOBean>) {
        inSet.clear()
        outSet.clear()
        for (bean in datas) {
            if (bean.configureList != null && bean.configureList.size > 0 && bean.configureList[0] != null) {
                val vStatus = bean.configureList[0].status
                val vFenceId = bean.fenceId
                if (vStatus.contains("in")) {
                    inSet.add(vFenceId)
                }
                if (vStatus.contains("out")) {
                    outSet.add(vFenceId)
                }
                bean.delayIn = bean.configureList[0].delayIn
                bean.delayOut = bean.configureList[0].delayOut
            } else {
                bean.delayIn = "0"
                bean.delayOut = "0"
            }
        }
    }

    override fun convert(v: DeviceFenceApplicationItemBinding, t: GEOBean, position: Int) {
        v.deviceApplicationListItemHistory.text = LanguageUtil.getInstance().getString(LanguageHelper.COMMON_ALARM_IN_OUT_TEXT)
        if (t.getFenceType()) {
            v.layout.deviceListItemCarImg.setImageResource(R.drawable.device_list_item_car_user)
        } else {
            v.layout.deviceListItemCarImg.setImageResource(R.drawable.device_list_item_car)
        }
        v.layout.deviceListItemCarName.text = t.fenName
        v.layout.deviceListItemCarLocation.text = (LanguageUtil.getInstance().getString("check_fence_radiu") + t.radius + LanguageUtil.getInstance().getString("fence_meter"))
        if ("polygon".equals(t.type, ignoreCase = true)) {
            v.layout.deviceListItemCarLocation.visibility = View.GONE
        } else {
            v.layout.deviceListItemCarLocation.visibility = View.VISIBLE
        }
        //避免每次刷新都请求地址解析
        if (t.addr == null || t.addr.isEmpty()) {
            if (t.points != null) {
                //根据坐标的获取地址
                val vPoint: GeomPoint = t.points[0]
                val vMyLatLng = MyLatLng(vPoint.lat, vPoint.lng)
                if (Functions.getNetworkState(mContext) != Functions.NETWORN_NONE) {
                    val latlng = vMyLatLng.latitude.toString() + "," + vMyLatLng.longitude
                    ConnectServiceImpl.geocoderForBaiDu(latlng) { pAddress, latlng ->
                        if (pAddress.isNotEmpty()) {
                            v.layout.deviceListItemCarImei.text = pAddress
                            t.addr = pAddress
                        }
                    }
                }
            }
        } else {
            v.layout.deviceListItemCarImei.text = t.addr

        }
        val vFenceId = t.fenceId
        initCheckbox(v, vFenceId)

        v.layout.commonListItemCheckedIv.setOnClickListener(View.OnClickListener {
            if (t.getFenceType()) {
                ToastUtil.showToast(mContext, mContext.getString(R.string.apply_fence_error_hint))
                v.layout.commonListItemCheckedIv.isChecked = !v.layout.commonListItemCheckedIv.isChecked
                return@OnClickListener
            }
            if (pActivationFlag.equals("0", ignoreCase = true)) {
                ToastUtil.showToast(mContext, languageUtil.getString("device_unactivated"))
                v.layout.commonListItemCheckedIv.isChecked = !v.layout.commonListItemCheckedIv.isChecked
                return@OnClickListener
            } else if (pExpireFlag.equals("0", ignoreCase = true)) {
                ToastUtil.showToast(mContext, languageUtil.getString("device_expired"))
                v.layout.commonListItemCheckedIv.isChecked = !v.layout.commonListItemCheckedIv.isChecked
                return@OnClickListener
            }
            val status = ""
            var out = false
            var `in` = false
            if (inSet.contains(vFenceId) || outSet.contains(vFenceId)) {
                if (outSet.contains(vFenceId)) {
                    out = true
                    outSet.remove(vFenceId)
                    v.deviceApplicationListItemMore.isChecked = false
                }
                if (inSet.contains(vFenceId)) {
                    `in` = true
                    inSet.remove(vFenceId)
                    v.deviceListItemCommand.isChecked = false
                }
            } else {
                outSet.add(vFenceId)
                inSet.add(vFenceId)
                v.deviceListItemCommand.isChecked = true
                v.deviceApplicationListItemMore.isChecked = true
            }
            val delayIn = if (v.deviceListItemCommand.isChecked) t.getDelayIn() else 0
            val delayOut = if (v.deviceApplicationListItemMore.isChecked) t.getDelayOut() else 0
            setInOut(status, vFenceId, "all", out, `in`, t, delayIn, delayOut)
        })

        v.delayAlarmSetting.setOnClickListener(View.OnClickListener {
            if (pActivationFlag.equals("0", ignoreCase = true)) {
                ToastUtil.showToast(mContext, languageUtil.getString("device_unactivated"))
                return@OnClickListener
            } else if (pExpireFlag.equals("0", ignoreCase = true)) {
                ToastUtil.showToast(mContext, languageUtil.getString("device_expired"))
                return@OnClickListener
            }
            val delayIn = if (v.deviceListItemCommand.isChecked) t.getDelayIn() else 0
            val delayOut = if (v.deviceApplicationListItemMore.isChecked) t.getDelayOut() else 0
            delayAlarmSettingDialog!!.setFenceDelay(delayIn, delayOut)
            delayAlarmSettingDialog!!.setFenceStatus(v.deviceListItemCommand.isChecked, v.deviceApplicationListItemMore.isChecked)
            delayAlarmSettingDialog!!.setUserFence(t.getFenceType())
            delayAlarmSettingDialog!!.geoBean = t
            delayAlarmSettingDialog!!.vFenceId = vFenceId
            delayAlarmSettingDialog!!.inCheck = v.deviceListItemCommand
            delayAlarmSettingDialog!!.outCheck = v.deviceApplicationListItemMore
            delayAlarmSettingDialog!!.allCheck = v.layout.commonListItemCheckedIv
            if (!delayAlarmSettingDialog!!.isShowing) {
                delayAlarmSettingDialog!!.show()
            }
        })
        v.deviceListItemCommand.setOnClickListener(object : View.OnClickListener {
            var status = ""
            override fun onClick(view: View) {
                BuryingPointUtils.onEvent(mContext, "c_app_tqzx_sbczxq_wlym_jgx")
                if (t.getFenceType()) {
                    ToastUtil.showToast(mContext, mContext.getString(R.string.apply_fence_error_hint))
                    v.deviceListItemCommand.isChecked = !v.deviceListItemCommand.isChecked
                    return
                }
                if (pActivationFlag.equals("0", ignoreCase = true)) {
                    ToastUtil.showToast(mContext, languageUtil.getString("device_unactivated"))
                    v.deviceListItemCommand.isChecked = !v.deviceListItemCommand.isChecked
                    return
                } else if (pExpireFlag.equals("0", ignoreCase = true)) {
                    ToastUtil.showToast(mContext, languageUtil.getString("device_expired"))
                    v.deviceListItemCommand.isChecked = !v.deviceListItemCommand.isChecked
                    return
                }
                if (!inSet.contains(vFenceId)) {
                    inSet.add(vFenceId)
                    v.layout.commonListItemCheckedIv.isChecked = true
                } else {
                    inSet.remove(vFenceId)
                    if (!outSet.contains(vFenceId)) {
                        v.layout.commonListItemCheckedIv.isChecked = false
                    }
                }
                val delayIn = if (v.deviceListItemCommand.isChecked) t.getDelayIn() else 0
                val delayOut = if (v.deviceApplicationListItemMore.isChecked) t.getDelayOut() else 0
                setInOut(status, vFenceId, "in", out = false, `in` = false, geoBean = t, delayIn = delayIn, delayOut = delayOut)
            }
        })
        v.deviceApplicationListItemMore.setOnClickListener(object : View.OnClickListener {
            var status = ""
            override fun onClick(view: View) {
                BuryingPointUtils.onEvent(mContext, "c_app_tqzx_sbczxq_wlym_cgx")
                if (t.getFenceType()) {
                    ToastUtil.showToast(mContext, mContext.getString(R.string.apply_fence_error_hint))
                    v.deviceApplicationListItemMore.isChecked = !v.deviceApplicationListItemMore.isChecked
                    return
                }
                if (pActivationFlag.equals("0", ignoreCase = true)) {
                    ToastUtil.showToast(mContext, languageUtil.getString("device_unactivated"))
                    v.deviceApplicationListItemMore.isChecked = !v.deviceApplicationListItemMore.isChecked
                    return
                } else if (pExpireFlag.equals("0", ignoreCase = true)) {
                    ToastUtil.showToast(mContext, languageUtil.getString("device_expired"))
                    v.deviceApplicationListItemMore.isChecked = !v.deviceApplicationListItemMore.isChecked
                    return
                }
                if (!outSet.contains(vFenceId)) {
                    outSet.add(vFenceId)
                    v.layout.commonListItemCheckedIv.isChecked = true
                } else {
                    outSet.remove(vFenceId)
                    if (!inSet.contains(vFenceId)) {
                        v.layout.commonListItemCheckedIv.isChecked = false
                    }
                }
                val delayIn = if (v.deviceListItemCommand.isChecked) t.getDelayIn() else 0
                val delayOut = if (v.deviceApplicationListItemMore.isChecked) t.getDelayOut() else 0
                setInOut(status, vFenceId, "out", out = false, `in` = false, geoBean = t, delayIn = delayIn, delayOut = delayOut)
            }
        })
    }


    /**
     * 设置进出告警方法
     *
     * @param pStatus
     * @param pFenceId
     */
    private fun setInOut(pStatus: String, pFenceId: String, flag: String, out: Boolean, `in`: Boolean, geoBean: GEOBean,
                         delayIn: Int, delayOut: Int) {
        var mStatus = pStatus
        if (flag == "default") {
            if (out && `in`) {
                mStatus = "in,out"
            } else if (`in`) {
                mStatus = "in"
            } else if (out) {
                mStatus = "out"
            }
        } else {
            if (inSet.contains(pFenceId) && outSet.contains(pFenceId)) mStatus = "in,out"
            else if (inSet.contains(pFenceId) && !outSet.contains(pFenceId)) mStatus = "in"
            else if (!inSet.contains(pFenceId) && outSet.contains(pFenceId)) mStatus = "out"
            else if (!inSet.contains(pFenceId) && !outSet.contains(pFenceId)) mStatus = ""
        }
        //        mSProxy.Method(ServiceApi.addOrDeleteGeozoneAlarm, mImei, pFenceId, pStatus);
        onFenceApplyClickListener?.onFenceApply(true)
        requestWeb(pFenceId, mStatus, flag, out, `in`, geoBean, delayIn, delayOut)
    }

    private fun requestWeb(geoid: String, status: String, flag: String?, out: Boolean, `in`: Boolean,
                           geoBean: GEOBean?, delayIn: Int, delayOut: Int) {
        val vActivity = mContext as com.jimi.app.mvvm.modules.fence.FenceApplyActivity
        val map: MutableMap<String, String> = HashMap()
        map["url"] = Constant.API_HOST + (if (Constant.API_HOST.endsWith("/")) "" else "/") + "api?ver=2&method=addOrDeleteGeozoneAlarm&imei=" +
                mImei + "&geoId=" + geoid + "&status=" + status + "&delayIn=" + delayIn + "&delayOut=" + delayOut
        request(map, vActivity, geoid, flag!!, out, `in`, geoBean!!, delayIn, delayOut)
    }


    private fun initCheckbox(v: DeviceFenceApplicationItemBinding, pFenceId: String) {
        v.deviceListItemCommand.isChecked = inSet.contains(pFenceId)
        v.deviceApplicationListItemMore.isChecked = outSet.contains(pFenceId)
        v.layout.commonListItemCheckedIv.isChecked = !(!inSet.contains(pFenceId) && !outSet.contains(pFenceId))
    }

    private fun request(map: Map<String, String>, vActivity: com.jimi.app.mvvm.modules.fence.FenceApplyActivity, geoid: String,
                        flag: String, out: Boolean, `in`: Boolean, geoBean: GEOBean, delayIn: Int, delayOut: Int) {
        ConnectServiceImpl.DynamicUrl(map, object : TObserver(map) {
            override fun tNext(json: String) {
                LogUtil.e(json)
                val gson = Gson()
                val packageModel: PackageModel<*> = gson.fromJson(json, object : TypeToken<PackageModel<*>?>() {}.type)
                geoBean.delayIn = delayIn.toString()
                geoBean.delayOut = delayOut.toString()
                onFenceApplyClickListener?.onFenceApply(false)
                if (flag == "default") {
                    defaultSucc(out, `in`)
                }
                vActivity.succCallBack(packageModel, geoid, flag, out, `in`)
            }

            override fun reload(pMap: Map<String, String>) {
                request(pMap, vActivity, geoid, flag, out, `in`, geoBean, delayIn, delayOut)
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
                onFenceApplyClickListener?.onFenceApply(false)
                vActivity.failCallBack(geoid, flag, out, `in`)
            }
        })
    }

    private fun defaultSucc(out: Boolean, `in`: Boolean) {
        delayAlarmSettingDialog!!.outCheck.isChecked = out
        delayAlarmSettingDialog!!.inCheck.isChecked = `in`
        delayAlarmSettingDialog!!.allCheck.isChecked = out || `in`
        if (outSet.contains(delayAlarmSettingDialog!!.vFenceId) && !out) {
            outSet.remove(delayAlarmSettingDialog!!.vFenceId)
        } else if (!outSet.contains(delayAlarmSettingDialog!!.vFenceId) && out) {
            outSet.add(delayAlarmSettingDialog!!.vFenceId)
        }
        if (inSet.contains(delayAlarmSettingDialog!!.vFenceId) && !`in`) {
            inSet.remove(delayAlarmSettingDialog!!.vFenceId)
        } else if (!inSet.contains(delayAlarmSettingDialog!!.vFenceId) && `in`) {
            inSet.add(delayAlarmSettingDialog!!.vFenceId)
        }
    }

    override fun onDelayAlarmSettiongClick(delayIn: Int, delayOut: Int, inStatus: Boolean, outStatus: Boolean) {
        setInOut("", delayAlarmSettingDialog!!.vFenceId, "default",
                outStatus, inStatus, delayAlarmSettingDialog!!.geoBean, delayIn, delayOut)
    }

    //功能：当设置失败或无网络时cb返回为原来的状态
    fun onFailReturnCB(geoid: String?, flag: String?, out: Boolean, `in`: Boolean) {
        when (flag) {
            "in" -> if (inSet.contains(geoid)) {
                inSet.remove(geoid)
            } else {
                inSet.add(geoid!!)
            }
            "out" -> if (outSet.contains(geoid)) {
                outSet.remove(geoid)
            } else {
                outSet.add(geoid!!)
            }
            "all" -> {
                if (inSet.contains(geoid) and !`in`) {
                    inSet.remove(geoid)
                } else if (!inSet.contains(geoid) and `in`) {
                    inSet.add(geoid!!)
                }
                if (outSet.contains(geoid) and !out) {
                    outSet.remove(geoid)
                } else if (!outSet.contains(geoid) and out) {
                    outSet.add(geoid!!)
                }
            }
            "default" -> {
            }
        }
        notifyDataSetChanged()
    }

    interface OnFenceApplyClickListener {
        fun onFenceApply(isShow: Boolean)
    }
}
