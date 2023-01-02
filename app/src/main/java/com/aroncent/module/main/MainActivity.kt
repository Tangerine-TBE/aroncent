package com.aroncent.module.main

import com.aroncent.R
import com.aroncent.module.login.LoginActivity
import com.aroncent.utils.startActivity
import com.tencent.mmkv.MMKV
import com.xlitebt.base.BaseActivity

class MainActivity : BaseActivity() {
    override fun layoutId(): Int {
        return  R.layout.activity_main
    }

    override fun initData() {
    }

    override fun initView() {
        if (MMKV.defaultMMKV().getString("token","")==""){
            startActivity(LoginActivity::class.java)
        }
    }

    override fun initListener() {
    }

    override fun start() {
    }

}