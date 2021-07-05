package com.jimi.app.mvvm.net.retrofit

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

open class NetConverterFactory  constructor(private val gson: Gson) : Converter.Factory() {
    override fun responseBodyConverter(type: Type,
                                       annotations: Array<Annotation>,
                                       retrofit: Retrofit): Converter<ResponseBody, *> {
        return getAdapter(TypeToken.get(type))
    }

    /**
     * 解析辅助类
     * @param type TypeToken
     * @param <T>  解析类型
     * @return 解析辅助类
    </T> */
    protected fun <T> getAdapter(type: TypeToken<T>?): NetResponseBodyConverter<T> {
        return NetResponseBodyConverter<T>(gson, gson.getAdapter(type))
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<Annotation>,
                                      methodAnnotations: Array<Annotation>,
                                      retrofit: Retrofit): Converter<*, RequestBody> {
        val adapter = gson.getAdapter(TypeToken.get(type))
        return NetRequestBodyConverter(gson, adapter)
    }

    companion object {
        /**
         * Create an instance using `gson` for conversion. Encoding to JSON and
         * decoding from JSON (when no charset is specified by a header) will use UTF-8.
         */
        /**
         * Create an instance using a default [Gson] instance for conversion. Encoding to JSON and
         * decoding from JSON (when no charset is specified by a header) will use UTF-8.
         */
        @JvmOverloads  // Guarding public API nullability.
        fun create(gson: Gson? = Gson()): NetConverterFactory {
            if (gson == null) throw NullPointerException("gson == null")
            return NetConverterFactory(gson)
        }
    }

}