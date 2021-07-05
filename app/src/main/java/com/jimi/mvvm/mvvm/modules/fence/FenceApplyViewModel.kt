package com.jimi.app.mvvm.modules.fence

import androidx.lifecycle.MutableLiveData
import com.jimi.app.entitys.GEOBean
import com.jimi.app.mvvm.net.error.ErrorResult
import com.jimi.app.mvvm.net.retrofit.RetrofitClient
import com.jimi.app.mvvm.base.BaseViewModel
import okhttp3.FormBody

/**
 * Description:
 * Author: zengweidie
 * CreateDate: 2021/5/28 13:59
 * UpdateUser: 更新者
 * UpdateDate: 2021/5/28 13:59
 * UpdateRemark: 更新说明
 *
 */

class FenceApplyViewModel : BaseViewModel() {
    var geoList = MutableLiveData<List<GEOBean>>()
    var saveGeoTagFaile = MutableLiveData<ErrorResult>()  //编辑围栏失败
    var saveGeoTagSuccess = MutableLiveData<Boolean>()  //编辑围栏成功
    fun getGeoSetInfo(userId: String, imei: String, isShowLoading: Boolean) {
        val part = FormBody.Builder()
                .add("imei", imei)
                .add("userId", userId)
                .add("method", "getGeoSetInfo").build()
        launch({ RetrofitClient.api.getGeoSetInfo(part) }, geoList, isShowLoading)
    }

    fun saveGeo(args: Array<String>) {
        val part = FormBody.Builder()
                .add("method", "geoSave")
                .add("userId", args.get(0))
                .add("imei", args.get(1))
                .add("geoname", args.get(2))
                .add("mapType", args.get(3))
                .add("geom", args.get(4))
                .add("radius", args.get(5))
                .add("status", args.get(6))
                .add("scale", args.get(7))
                .add("geoId", args.get(8))
                .add("filterParameters", args.get(9))
                .add("delayIn", args.get(10))
                .add("delayOut", args.get(11))
                .add("userIds", args.get(12)).build()
        launch({ RetrofitClient.api.geoSave(part) }, null, isShowLoading = true, isShowError = true, successBlock = {saveGeoTagSuccess.value = true}, errorBlock = { errorResult ->saveGeoTagFaile.value = errorResult  })
    }
}