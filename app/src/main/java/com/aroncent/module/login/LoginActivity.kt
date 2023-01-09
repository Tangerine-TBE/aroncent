package com.aroncent.module.login

import android.annotation.SuppressLint
import com.aroncent.R
import com.aroncent.base.RxSubscriber
import com.aroncent.module.main.MainActivity
import com.aroncent.utils.setUserInfoToSp
import com.aroncent.utils.showToast
import com.aroncent.utils.startActivity
import com.blankj.utilcode.util.ActivityUtils
import com.ltwoo.estep.api.RetrofitManager
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
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
        tv_login.setOnClickListener { login() }
    }
    private fun login(){
        val map = hashMapOf<String,String>()
        map["email"] = et_account.text.toString()
        map["password"] = et_password.text.toString()
        RetrofitManager.service.login(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<RequestUserInfoBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: RequestUserInfoBean?) {
                    t?.let {
                        if (t.code == 1) {
                            setUserInfoToSp(t.data.userInfo)
                            ActivityUtils.finishAllActivities()
                            startActivity(MainActivity::class.java)
                        }else{
                            showToast(t.msg)
                        }
                    }
                }
            })
    }

    override fun start() {
    }
}