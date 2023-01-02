package com.aroncent.module.login

import com.aroncent.R
import com.aroncent.utils.startActivity
import com.xlitebt.base.BaseActivity
import kotlinx.android.synthetic.main.act_login.*

class LoginActivity : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.act_login
    }

    override fun initData() {
    }

    override fun initView() {
    }

    override fun initListener() {
        tv_register.setOnClickListener {
            startActivity(RegisterActivity1::class.java)
        }
    }

    override fun start() {
    }
}