package com.jimi.app.mvvm.modules.fence

import androidx.lifecycle.MutableLiveData
import com.jimi.app.entitys.GEOBean
import com.jimi.app.mvvm.net.retrofit.RetrofitClient
import com.jimi.app.mvvm.base.BaseViewModel
import okhttp3.FormBody

class FenceListManagerViewModel : BaseViewModel() {

    lateinit var userId: String
    var geoList = MutableLiveData<List<GEOBean>>()
    var addGeoTag = MutableLiveData<Boolean>()  //添加围栏成功或失败
    var delGeoTag = MutableLiveData<Boolean>()  //删除围栏成功或失败
    fun getGeoSetInfo(userId: String, imei: String, isShowLoading: Boolean) {
        this.userId = userId
        val part = FormBody.Builder()
                .add("imei", imei)
                .add("userId", userId)
                .add("method", "getGeoSetInfo").build()
        launch({ RetrofitClient.api.getGeoSetInfo(part) }, geoList, isShowLoading)
    }

    fun deleteGeo(geoId: String, isShowLoading: Boolean) {
        val part = FormBody.Builder()
                .add("geoId", geoId)
                .add("method", "deleteGeozone").build()
        launch({ RetrofitClient.api.deleteGeozone(part) }, null, isShowLoading, false, { delGeoTag.value = true}, { delGeoTag.value = false})
    }

    fun addGeo(geoId: String, userId: String, isShowLoading: Boolean) {
        val part = FormBody.Builder()
                .add("geoId", geoId)
                .add("userIds", userId)
                .add("method", "addUserIdOnFence").build()
        launch({ RetrofitClient.api.deleteGeozone(part) }, null, isShowLoading, false, { addGeoTag.value = true}, { addGeoTag.value = false})
    }
}