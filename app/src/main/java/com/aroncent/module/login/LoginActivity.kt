package com.aroncent.module.login

import android.annotation.SuppressLint
import android.widget.RadioButton
import com.aroncent.R
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.module.main.MainActivity
import com.aroncent.utils.showToast
import com.aroncent.utils.startActivity
import com.blankj.utilcode.util.ActivityUtils
import com.ltwoo.estep.api.RetrofitManager
import com.tencent.mmkv.MMKV
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_login.*
import kotlinx.android.synthetic.main.act_register_1.*

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
            .subscribe(object : RxSubscriber<BaseBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        if (t.code == 1) {
//                            MMKV.defaultMMKV().putString("token",t.data.userinfo.token)
//                            MMKV.defaultMMKV().putString("avatar",t.data.userinfo.avatar)
//                            MMKV.defaultMMKV().putString("username",t.data.userinfo.username)
//                            MMKV.defaultMMKV().putString("nickname",t.data.userinfo.nickname)
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