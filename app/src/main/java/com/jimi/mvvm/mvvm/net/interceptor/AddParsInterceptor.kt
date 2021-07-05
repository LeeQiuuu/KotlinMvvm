package com.jimi.app.mvvm.net.interceptor

import com.jimi.app.MainApplication
import com.jimi.app.common.C
import com.jimi.app.mvvm.utils.DateUtil
import com.jimi.httpcrypt.utils.HttpCrypto
import okhttp3.*
import okio.Buffer
import java.io.IOException

/**
 *Created by LeeQiuuu on 2021/5/25.
 *Describe:公共参数添加
 */

class AddParsInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest: Request = chain.request()
        var newRequestBuild: Request.Builder? = null
        val method: String = oldRequest.method()
        var postBodyString = ""
        if ("POST" == method) {
            when (val oldBody: RequestBody? = oldRequest.body()) {
                is FormBody -> {
                    var mapBody: HashMap<String, String> = HashMap()
                    for (index in 0 until oldBody.size()) {
                        mapBody = HttpCrypto.getInstance(MainApplication.mInstance).toMap(mapBody, oldBody.name(index), oldBody.value(index))
                    }
                    mapBody["ver"] = "4";
                    mapBody["timestamp"] = DateUtil.getDate(System.currentTimeMillis())
                    mapBody["app_key"] = C.key.JIMIHTTPCRYPTO_APPKEY

                    val formBodyBuilder: FormBody.Builder = FormBody.Builder()
                    formBodyBuilder.add("ver", "4")
                    formBodyBuilder.add("timestamp", mapBody["timestamp"])
                    formBodyBuilder.add("app_key", C.key.JIMIHTTPCRYPTO_APPKEY)
                    formBodyBuilder.add("sign", HttpCrypto.getInstance(MainApplication.mInstance).signTopRequest2(mapBody))

                    newRequestBuild = oldRequest.newBuilder()
                    val formBody: RequestBody = formBodyBuilder.build()
                    postBodyString = bodyToString(oldRequest.body()!!)
                    postBodyString += (if (postBodyString.isNotEmpty()) "&" else "") + bodyToString(formBody)
                    newRequestBuild.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), postBodyString))
                }
                is MultipartBody -> {
                    val oldBodyMultipart: MultipartBody = oldBody as MultipartBody
                    val oldPartList: List<MultipartBody.Part> = oldBodyMultipart.parts()
                    val builder: MultipartBody.Builder = MultipartBody.Builder()
                    builder.setType(MultipartBody.FORM)
                    val mapBody = getMap(oldBodyMultipart)
                    mapBody["timestamp"] = DateUtil.getDate(System.currentTimeMillis())
                    mapBody["app_key"] = C.key.JIMIHTTPCRYPTO_APPKEY

                    val requestBody1: RequestBody = RequestBody.create(MediaType.parse("text/plain"), DateUtil.getDate(System.currentTimeMillis()))
                    val requestBody2: RequestBody = RequestBody.create(MediaType.parse("text/plain"), C.key.JIMIHTTPCRYPTO_APPKEY)
                    val requestBody3: RequestBody = RequestBody.create(MediaType.parse("text/plain"), HttpCrypto.getInstance(MainApplication.mInstance).signTopRequest2(mapBody))
                    for (part in oldPartList) {
                        builder.addPart(part)
                        postBodyString += """
                            ${bodyToString(part.body())}
                            
                            """.trimIndent()
                    }
                    postBodyString += """
                        ${bodyToString(requestBody1)}
                        
                        """.trimIndent()
                    postBodyString += """
                        ${bodyToString(requestBody2)}
                        
                        """.trimIndent()
                    postBodyString += """
                        ${bodyToString(requestBody3)}
                        
                        """.trimIndent()
                    //              builder.addPart(oldBody);  //不能用这个方法，因为不知道oldBody的类型，可能是PartMap过来的，也可能是多个Part过来的，所以需要重新逐个加载进去
                    builder.addPart(requestBody1)
                    builder.addPart(requestBody2)
                    builder.addPart(requestBody3)
                    newRequestBuild = oldRequest.newBuilder()
                    newRequestBuild.post(builder.build())
                    //Log.e(TAG, "MultipartBody," + oldRequest.url())
                }
                else -> {
                    newRequestBuild = oldRequest.newBuilder()
                }
            }
        } else {
            // 添加新的参数
            /*  var mapBody :HashMap<String,String> = HashMap()
              for (index in 0 until oldBody.size()){
                  mapBody = HttpCrypto.getInstance(MainApplication.mInstance).toMap(mapBody,oldBody.name(index),oldBody.value(index))
              }
              mapBody.put("timestamp", DateToStringUtils.timeStamp2Date(System.currentTimeMillis(), null))
              mapBody.put("app_key", C.key.JIMIHTTPCRYPTO_APPKEY)

              oldRequest.body().*/
            val commonParamsUrlBuilder: HttpUrl.Builder = oldRequest.url()
                    .newBuilder()
                    .scheme(oldRequest.url().scheme())
                    .host(oldRequest.url().host())
            /* .addQueryParameter("timestamp", DateToStringUtils.timeStamp2Date(System.currentTimeMillis(), null))
             .addQueryParameter("app_key", C.key.JIMIHTTPCRYPTO_APPKEY)
             .addQueryParameter("sign", HttpCrypto.getInstance(MainApplication.mInstance).signTopRequest2(formBodyBuilder))*/
            newRequestBuild = oldRequest.newBuilder()
                    .method(oldRequest.method(), oldRequest.body())
                    .url(commonParamsUrlBuilder.build())
        }
        val newRequest: Request = newRequestBuild
                .addHeader("Accept", "application/json")
                .addHeader("Accept-Language", "zh")
                .build()
        val startTime = System.currentTimeMillis()
        val response: Response = chain.proceed(newRequest)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        val mediaType = response.body()!!.contentType()
        val content = response.body()!!.string()
        val httpStatus = response.code()
        val logSB = StringBuilder()
        logSB.append("-------start:$method|")
        logSB.append("$newRequest\n|")
        logSB.append(if (method.equals("POST", ignoreCase = true)) "post参数{$postBodyString}\n|" else "")
        logSB.append("httpCode=$httpStatus;Response:$content\n|")
        logSB.append("----------End:" + duration + "毫秒----------")
        //   Log.d(TAG, logSB.toString())
        return response.newBuilder()
                .body(ResponseBody.create(mediaType, content))
                .build()
    }

    companion object {
        var TAG = "LogInterceptor"
        private fun bodyToString(request: RequestBody?): String {
            return try {
                val copy: RequestBody? = request
                val buffer = Buffer()
                if (copy != null) copy.writeTo(buffer) else return ""
                buffer.readUtf8()
            } catch (e: IOException) {
                "did not work"
            }
        }
    }

    fun getMap(requestBody: MultipartBody): HashMap<String, String> {
        val params = HashMap<String, String>()
        requestBody.parts().forEach { it ->
            val body = it.body()
            it.headers()?.let {
                val header = it.value(0)
                val split = header.replace(" ", "").replace("\"", "").split(";")
                when (split.size) {
                    2 -> {
                        //文本参数
                        val keys = split[1].split("=")
                        if (keys.size > 1 && body.contentLength() < 1024) {
                            val key = keys[1]
                            val buffer = Buffer()
                            body.writeTo(buffer)
                            val value = buffer.readUtf8()
                            params[key] = value
                        }
                    }
                }
            }
        }
        return params
    }
}