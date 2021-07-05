package com.jimi.app.mvvm.base

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.jimi.app.mvvm.widget.clicks
import com.jimi.app.utils.LanguageUtil
import java.lang.reflect.ParameterizedType

/**
 *Created by LeeQiuuu on 2021/4/23.
 * 通过传入ViewBinding，不再需要写具体xml资源，省略onBindViewHolder中findviewById
 * 注意点：item的最外层布局高度要设为wrap_content，
 * 如果item有需求要设置为固定宽高，可以在子类的convert方法里，通过代码设置
 */
abstract class BaseAdapter<VB : ViewBinding, T>(
        var mContext: Activity,
        var listDatas: ArrayList<T>
) : RecyclerView.Adapter<BaseViewHolder>() {

    protected var languageUtil: LanguageUtil = LanguageUtil()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val type = javaClass.genericSuperclass as ParameterizedType
        val clazz = type.actualTypeArguments[0] as Class<VB>
        val method = clazz.getMethod("inflate", LayoutInflater::class.java)
        var vb = method.invoke(null, LayoutInflater.from(mContext)) as VB
        vb.root.layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        )
        return BaseViewHolder(vb, vb.root)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        //点击事件和长按事件通过传入方法体作为参数进行回调，只需要在activity中给itemClick赋值方法体
        holder.itemView.clicks {
            itemClick?.let { it(position) }
        }
        holder.itemView.setOnLongClickListener {
            itemLongClick?.let { it1 -> it1(position) }
            true
        }

        convert(holder.v as VB, listDatas[position], position)
    }

    abstract fun convert(v: VB, t: T, position: Int)

    override fun getItemCount(): Int {
        return listDatas.size
    }


    private var itemClick: ((Int) -> Unit)? = null
    private var itemLongClick: ((Int) -> Unit)? = null


    fun itemClick(itemClick: (Int) -> Unit) {
        this.itemClick = itemClick
    }

    fun itemLongClick(itemLongClick: (Int) -> Unit) {
        this.itemLongClick = itemLongClick
    }
}
