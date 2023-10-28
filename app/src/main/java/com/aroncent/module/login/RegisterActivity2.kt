package com.aroncent.module.login

import android.annotation.SuppressLint
import android.content.Intent
import com.aroncent.R
import com.aroncent.api.RetrofitManager
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.utils.showToast
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_register_1.et_email
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
        layout_previous.setOnClickListener {
            RetrofitManager.service.sendEms(hashMapOf("email" to email))
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
                            showToast(t.msg)
                        }
                    }
                })
        }
    }

    override fun start() {
    }
}