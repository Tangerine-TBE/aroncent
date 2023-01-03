package com.aroncent.module.login

import android.content.Intent
import com.aroncent.R
import com.blankj.utilcode.util.RegexUtils
import com.xlitebt.base.BaseActivity
import kotlinx.android.synthetic.main.act_register_1.*

class RegisterActivity1 : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.act_register_1
    }

    override fun initData() {
    }

    override fun initView() {
        RegexUtils.isMatch("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}\$", "Ha123456")
    }

    override fun initListener() {
        tv_ok.setOnClickListener {
            startActivity(Intent(this,RegisterActivity2::class.java))
        }
    }

    override fun start() {
    }
}