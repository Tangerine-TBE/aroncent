package com.aroncent.module.login

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.widget.RadioButton
import cn.jpush.android.api.JPushInterface
import com.aroncent.BuildConfig
import com.aroncent.R
import com.aroncent.api.RetrofitManager
import com.aroncent.base.RxSubscriber
import com.aroncent.module.main.MainActivity
import com.aroncent.utils.setUserInfoToSp
import com.aroncent.utils.showToast
import com.aroncent.utils.startActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.RegexUtils
import com.onesignal.OneSignal
import com.onesignal.OneSignalAPIClient
import com.onesignal.OneSignalNotificationManager
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_register_4.*
import java.util.*


class RegisterActivity4 : BaseActivity() {
    private var countryId = ""
    private var smsCode = ""
    private var email = ""

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

        tv_age.setOnClickListener {
//            val picker = DatePicker(this)
//            val wheelLayout = picker.wheelLayout
//            picker.cancelView.text = "Cancel"
//            picker.okView.text = "Confirm"
//            wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
//            wheelLayout.setDateFormatter(object : DateFormatter{
//                override fun formatYear(year: Int): String {
//                    return  year.toString()
//                }
//                override fun formatMonth(month: Int): String {
//                    return month.toString()+"mth"
//                }
//                override fun formatDay(day: Int): String {
//                    return day.toString()
//                }
//            })
//            wheelLayout.setRange(DateEntity.target(1949, 1, 1),DateEntity.today(),DateEntity.today())
//            picker.wheelLayout.setResetWhenLinkage(false)
//            picker.setOnDatePickedListener { year, month, day ->
//                tv_age.text = "$year-${if(month.toString().length<2) "0$month" else month.toString()}-${if (day.toString().length<2) "0$day" else day.toString()}"
//            }
//            picker.show()

            val ca: Calendar = Calendar.getInstance()
            val mYear: Int = ca.get(Calendar.YEAR)
            val mMonth: Int = ca.get(Calendar.MONTH)
            val mDay: Int = ca.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this, { _, year, month, dayOfMonth ->
                    val txtMon = if (month + 1 < 10) "0" + (month + 1) else (month + 1).toString()
                    val txtDay = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                    tv_age.text = "$year-$txtMon-$txtDay"
                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
        }
        layout_previous.setOnClickListener {
            onBackPressed()
        }
    }

    private fun register() {
        if (et_name.text.toString() == "" || tv_age.text.toString() == "" || et_password.text.toString() == "") {
            showToast("Please fill in all information")
            return
        }
        if (et_password.text.toString().trim().length < 6) {
            showToast("The password shall not be less than 6 digits")
            return
        }
        if (et_password.text.toString() != et_re_pwd.text.toString()) {
            showToast("Inconsistent passwords entered")
            return
        }
        val map = hashMapOf<String, String>()
        map["email"] = email
        map["code"] = smsCode
        map["sex"] = findViewById<RadioButton>(rg_sex.checkedRadioButtonId).text.toString()
        map["username"] = et_name.text.toString()
        map["birthday"] = tv_age.text.toString()
        map["country"] = countryId
        map["password"] = et_password.text.toString()
        map["jg_pushid"] = JPushInterface.getRegistrationID(this)
        map["onesignal_pushid"] = OneSignal.getDeviceState()?.userId ?: ""

        RetrofitManager.service.register(map).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<RequestUserInfoBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: RequestUserInfoBean?) {
                    t?.let {
                        if (t.code == 200) {
                            setUserInfoToSp(t.data.userInfo)
                            ActivityUtils.finishAllActivities()
                            startActivity(MainActivity::class.java)
                        } else {
                            showToast(t.msg)
                        }
                    }
                }
            })

    }

    override fun start() {
    }
}