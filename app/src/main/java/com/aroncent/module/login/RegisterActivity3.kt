package com.aroncent.module.login

import android.annotation.SuppressLint
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.aroncent.R
import com.aroncent.base.RxSubscriber
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ltwoo.estep.api.RetrofitManager
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_register_3.*
import kotlinx.android.synthetic.main.item_country.view.*

class RegisterActivity3 : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.act_register_3
    }

    override fun initData() {
    }

    override fun initView() {
        rv_country.layoutManager = GridLayoutManager(this,3)
        getCountryList()
    }

    fun getCountryList(){
        RetrofitManager.service.getCountryList(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<CountryListBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: CountryListBean?) {
                    t?.let {
                        if (t.code == 1) {
                            if (t.data.isNotEmpty()) {
                                val adapter = CountryAdapter(t.data)
                                rv_country.adapter = adapter
                            }
                        }
                    }
                }
            })
    }

    inner class CountryAdapter(data: MutableList<CountryListBean.DataBean>) :
        BaseQuickAdapter<CountryListBean.DataBean, BaseViewHolder>(R.layout.item_country, data) {
        override fun convert(helper: BaseViewHolder, item: CountryListBean.DataBean) {
            val itemView = helper.itemView
            itemView.tv_country.text = item.country
            if (item.isCheck){
                itemView.tv_country.setTextColor(ContextCompat.getColor(this@RegisterActivity3,R.color.textCountry))
                itemView.tv_country.setBackgroundColor(ContextCompat.getColor(this@RegisterActivity3,R.color.white))
            }else{
                itemView.tv_country.setTextColor(ContextCompat.getColor(this@RegisterActivity3,R.color.white))
                itemView.tv_country.setBackgroundResource(R.drawable.country_shape)
            }
            itemView.tv_country.setOnClickListener {
                data.forEach {
                    it.isCheck = false
                }
                item.isCheck = true
                notifyDataSetChanged()
            }
        }
    }
    override fun initListener() {
    }

    override fun start() {
    }
}