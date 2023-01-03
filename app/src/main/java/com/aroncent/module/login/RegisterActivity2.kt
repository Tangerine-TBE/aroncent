package com.aroncent.module.login

import android.content.Intent
import com.aroncent.R
import com.xlitebt.base.BaseActivity
import kotlinx.android.synthetic.main.act_register_2.*

class RegisterActivity2 : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.act_register_2
    }

    override fun initData() {
    }

    override fun initView() {
    }

    override fun initListener() {
        tv_ok.setOnClickListener {
            startActivity(Intent(this,RegisterActivity3::class.java))
        }
    }

    override fun start() {
    }
}