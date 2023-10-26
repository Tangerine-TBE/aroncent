package com.aroncent.module.login

import android.content.Intent
import com.aroncent.R
import com.xlitebt.base.BaseActivity
import kotlinx.android.synthetic.main.act_register_2.*

class RegisterActivity2 : BaseActivity() {
    private var email =""

    override fun layoutId(): Int {
        return R.layout.act_register_2
    }

    override fun initData() {
    }

    override fun initView() {
        email = intent.getStringExtra("email")!!
    }

    override fun initListener() {
        tv_ok.setOnClickListener {
            startActivity(Intent(this,RegisterActivity3::class.java)
                .putExtra("email",email)
                .putExtra("smsCode",et_email_code.text.toString())
            )
        }
    }

    override fun start() {
    }
}