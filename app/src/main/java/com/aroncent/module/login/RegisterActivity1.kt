package com.aroncent.module.login

import android.util.Log
import com.aroncent.R
import com.blankj.utilcode.util.RegexUtils
import com.xlitebt.base.BaseActivity

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

    }

    override fun start() {
    }
}