package com.aroncent.module.login

import android.annotation.SuppressLint
import android.content.Intent
import com.aroncent.R
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.blankj.utilcode.util.RegexUtils
import com.ltwoo.estep.api.RetrofitManager
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_register_1.*
import kotlinx.android.synthetic.main.act_register_1.tv_ok
import kotlinx.android.synthetic.main.act_register_3.*

class RegisterActivity1 : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.act_register_1
    }

    override fun initData() {
    }

    override fun initView() {

    }

    override fun initListener() {
        tv_ok.setOnClickListener {
            sendCode()
        }
    }

    private fun sendCode(){
        RetrofitManager.service.sendEms(hashMapOf("email" to et_email.text.toString()))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        if (t.code == 1) {
                            startActivity(Intent(this@RegisterActivity1,RegisterActivity2::class.java)
                                .putExtra("email",et_email.text.toString()))
                        }
                    }
                }
            })
    }

    override fun start() {
    }
}