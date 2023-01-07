package com.aroncent.module.login

import android.annotation.SuppressLint
import android.widget.RadioButton
import com.aroncent.R
import com.aroncent.base.RxSubscriber
import com.aroncent.module.main.MainActivity
import com.aroncent.utils.setUserInfoToSp
import com.aroncent.utils.showToast
import com.aroncent.utils.startActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.RegexUtils
import com.ltwoo.estep.api.RetrofitManager
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_register_4.*
import kotlinx.android.synthetic.main.act_register_4.tv_ok

class RegisterActivity4 : BaseActivity() {
    private var countryId =""
    private var smsCode =""
    private var email =""

    override fun layoutId(): Int {
        return R.layout.act_register_4
    }

    override fun initData() {
    }

    override fun initView() {
        email = intent.getStringExtra("email")!!
        smsCode = intent.getStringExtra("smsCode")!!
        countryId = intent.getStringExtra("countryId")!!
    }

    override fun initListener() {
        tv_ok.setOnClickListener {
            register()
        }
    }

    private fun register(){
        if (et_name.text.toString() == "" || et_age.text.toString()=="" ||et_password.text.toString()==""){
            showToast("Please fill in all information")
            return
        }
        if (!RegexUtils.isMatch("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}\$", et_password.text.toString())){
            showToast("The password must contain upper and lower case and numbers, and not less than 8 digits")
            return
        }
        val map = hashMapOf<String,String>()
        map["email"] = email
        map["code"] = smsCode
        map["sex"] = findViewById<RadioButton>(rg_sex.checkedRadioButtonId).text.toString()
        map["username"] = et_name.text.toString()
        map["age"] = et_age.text.toString()
        map["country"] = countryId
        map["password"] = et_password.text.toString()
        RetrofitManager.service.register(map)
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
                            setUserInfoToSp(t.data.userinfo)
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