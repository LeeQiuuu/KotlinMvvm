package com.jimi.mvvm.ui.main

import androidx.lifecycle.MutableLiveData
import com.jimi.mvvm.ui.base.BaseViewModel
import com.jimi.mvvm.ui.main.model.ArticleListBean

class MainViewModel : BaseViewModel() {

    var articlesData = MutableLiveData<ArticleListBean>()

    fun getArticleList(page: Int, isShowLoading: Boolean) {
        launch({ httpUtil.getArticleList(page) }, articlesData, isShowLoading)
    }

}