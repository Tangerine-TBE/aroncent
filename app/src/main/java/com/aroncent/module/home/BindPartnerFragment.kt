package com.aroncent.module.home

import android.annotation.SuppressLint
import android.view.View
import com.aroncent.R
import com.aroncent.app.KVKey
import com.aroncent.base.BaseBean
import com.aroncent.base.BaseFragment
import com.aroncent.base.RxSubscriber
import com.aroncent.utils.showToast
import com.blankj.utilcode.util.ClickUtils
import com.aroncent.api.RetrofitManager
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_bind_partner.*

class BindPartnerFragment : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.frag_bind_partner
    }

    override fun initView() {
        if (MMKV.defaultMMKV().getString(KVKey.partnerStatus,"") == "4"){
            tv_tip.visibility = View.VISIBLE
            ll_bind.visibility = View.GONE
        }
    }

    override fun lazyLoad() {
    }

    override fun initListener() {
        ClickUtils.applySingleDebouncing(tv_bind, 500) {
            bind()
        }
    }

   private fun bind() {
        if (et_bind_email.text.toString()==""){
            showToast("Please fill in the email")
            return
        }
        val map = hashMapOf<String, String>()
        map["partner_email"] = et_bind_email.text.toString()
        RetrofitManager.service.addPartner(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(requireContext(), true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        showToast(t.msg)
                        if (t.code == 200) {
                            tv_tip.visibility = View.VISIBLE
                            ll_bind.visibility = View.GONE
                        }
                    }
                }
            })
    }
}