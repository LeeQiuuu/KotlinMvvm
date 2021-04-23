package com.jimi.mvvm.api

import com.jimi.mvvm.api.response.BaseResult
import com.jimi.mvvm.ui.common.model.TestModel
import com.jimi.mvvm.ui.main.model.ArticleListBean
import retrofit2.http.*


interface ApiService {

    @GET("test")
    suspend fun test(@QueryMap options: HashMap<String, String?>): BaseResult<TestModel>

    @GET("article/listproject/{page}/json")
    suspend fun getArticleList(@Path("page") page: Int): BaseResult<ArticleListBean>


}