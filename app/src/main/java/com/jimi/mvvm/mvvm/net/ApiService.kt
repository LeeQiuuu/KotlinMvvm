package com.jimi.app.mvvm.net

import com.jimi.app.entitys.GEOBean
import com.jimi.app.mvvm.net.response.EmptyBean
import com.jimi.app.mvvm.net.retrofit.AppResponseResult
import okhttp3.FormBody
import retrofit2.http.Body
import retrofit2.http.POST

//接口名都在参数中体现-FormBody
interface ApiService {

    //获取围栏列表
    @POST("api/")
    suspend fun getGeoSetInfo(@Body param: FormBody): AppResponseResult<List<GEOBean>>

    //添加删除围栏
    @POST("api/")
    suspend fun deleteGeozone(@Body param: FormBody): AppResponseResult<EmptyBean>

    //添加删除围栏
    @POST("api/")
    suspend fun geoSave(@Body param: FormBody): AppResponseResult<String>

}