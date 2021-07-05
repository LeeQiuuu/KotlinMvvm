package com.jimi.app.mvvm.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimi.app.mvvm.net.error.ErrorResult
import com.jimi.app.mvvm.net.error.ErrorUtil
import com.jimi.app.mvvm.net.retrofit.AppResponseResult
import com.jimi.app.mvvm.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


open class BaseViewModel : ViewModel() {


    //  val api by lazy {  RetrofitClient.api }

    var isShowLoading = MutableLiveData<Boolean>()//是否显示loading
    var errorData = MutableLiveData<ErrorResult>()//错误信息
    var isShowEmpty = MutableLiveData<Boolean>()//是否显示空布局

    private fun showLoading() {
        isShowLoading.value = true
    }

    private fun dismissLoading() {
        isShowLoading.value = false
    }

    private fun showError(error: ErrorResult) {
        errorData.value = error
    }


    /**
     * 请求接口，可定制是否显示loading和错误提示
     */
    fun <T> launch(
            block: suspend CoroutineScope.() -> AppResponseResult<T>,//请求接口方法，T表示data实体泛型，调用时可将data对应的bean传入即可
            liveData: MutableLiveData<T>? = null,
            isShowLoading: Boolean = false,
            isShowError: Boolean = true,
            successBlock: (T?) -> Unit = {},
            errorBlock: (ErrorResult) -> Unit = {}
    ) {
        if (isShowLoading) showLoading()
        viewModelScope.launch {
            try {
                val result = block()
                if (result.code == AppResponseResult.SUCCESS) {//请求成功
                    liveData?.let {
                        it.value = result.data
                    }
                    successBlock(result.data)
                } else {
                    LogUtil.e("请求错误>>" + result.msg)
                    showError(ErrorResult(result.code, result.msg, isShowError))
                    errorBlock(ErrorResult(result.code, result.msg, isShowError))
                }
            } catch (e: Throwable) {//接口请求失败
                LogUtil.e("请求异常>>" + e.message)
                val errorResult = ErrorUtil.getError(e)
                errorResult.show = isShowError
                showError(errorResult)
            } finally {//请求结束
                dismissLoading()
            }
        }
    }

}
