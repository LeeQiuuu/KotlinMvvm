package com.jimi.mvvm.ui.main.adapter

import android.app.Activity
import com.bumptech.glide.Glide
import com.jimi.mvvm.databinding.ItemArticleBinding
import com.jimi.mvvm.ui.base.BaseAdapter
import com.jimi.mvvm.ui.main.model.ArticleBean


class ArticleListAdapter(context: Activity, listDatas: ArrayList<ArticleBean>) :
    BaseAdapter<ItemArticleBinding, ArticleBean>(context, listDatas) {

    override fun convert(v: ItemArticleBinding, t: ArticleBean, position: Int) {
        Glide.with(mContext).load(t.envelopePic).into(v.ivCover)
        v.tvTitle.text = t.title
        v.tvDes.text = t.desc
    }

}
