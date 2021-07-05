package com.jimi.mvvm.ui.main

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.chrisbanes.photoview.PhotoView
import com.jimi.mvvm.databinding.ActivityMainBinding
import com.jimi.mvvm.event.EventCode
import com.jimi.mvvm.event.EventMessage
import com.jimi.mvvm.ui.base.BaseActivity
import com.jimi.mvvm.ui.main.adapter.ArticleListAdapter
import com.jimi.mvvm.ui.main.model.ArticleBean
import com.jimi.mvvm.utils.ToastUtil
import org.intellij.lang.annotations.Flow

/**
 *Created by LeeQiuuu on 2021/4/23.
 *Describe:引用的两个参数分别为ViewModel和databinding
viewModel不需要的时候使用EmptyModel,databinding会根据新建xml自动生成，智能提示输入即可找到
 */

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    var adapter: ArticleListAdapter? = null
    var list: ArrayList<ArticleBean>? = null
    var page: Int = 0


    override fun initView() {
        list = ArrayList()
        adapter = ArticleListAdapter(mContext, list!!)
        adapter!!.itemClick {
            startActivity(Intent(mContext, TestEventActivity::class.java))
        }
        v.mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        v.mRecyclerView.adapter = adapter

        vm.getArticleList(page, true)

        v.refreshLayout.setOnRefreshListener {//下拉刷新
            page = 0
            vm.getArticleList(page, false)
        }
        v.refreshLayout.setOnLoadMoreListener {//上拉加载
            vm.getArticleList(++page, false)
        }
    }

    override fun initClick() {

    }

    override fun initData() {

    }

    override fun initVM() {
        vm.articlesData.observe(this, Observer {
            v.refreshLayout.finishRefresh()
            v.refreshLayout.finishLoadMore()
            if (page == 0) list!!.clear()
            it.datas?.let { it1 -> list!!.addAll(it1) }
            adapter!!.notifyDataSetChanged()
        })
    }

    /**
     * 接收消息
     */
    override fun handleEvent(msg: EventMessage) {
        super.handleEvent(msg)
        if (msg.code == EventCode.REFRESH) {
            ToastUtil.showToast(mContext, "主页：刷新")
            page = 0
            vm.getArticleList(page, false)
        }
    }
}