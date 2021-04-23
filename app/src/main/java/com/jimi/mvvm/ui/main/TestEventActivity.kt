package com.jimi.mvvm.ui.main


import com.jimi.mvvm.App
import com.jimi.mvvm.databinding.ActivityTestEventBinding
import com.jimi.mvvm.event.EventCode
import com.jimi.mvvm.event.EventMessage
import com.jimi.mvvm.ui.base.BaseActivity
import com.jimi.mvvm.ui.base.BaseViewModel
import com.jimi.mvvm.widget.clicks


class TestEventActivity : BaseActivity<BaseViewModel, ActivityTestEventBinding>() {


    override fun initView() {

    }

    override fun initClick() {
        v.btn.clicks {
            App.post(EventMessage(EventCode.REFRESH))
        }
    }

    override fun initData() {

    }

    override fun initVM() {

    }

}