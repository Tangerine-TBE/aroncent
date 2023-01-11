package com.aroncent.module.home

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.aroncent.R
import com.aroncent.base.BaseFragment
import com.aroncent.base.RxSubscriber
import com.aroncent.event.ConnectStatusEvent
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ltwoo.estep.api.RetrofitManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_home.*
import kotlinx.android.synthetic.main.item_phrase_model.view.*
import kotlinx.android.synthetic.main.top_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.frag_home
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMsg(msg: ConnectStatusEvent) {
        tv_connected.visibility = if (msg.type == 1) View.VISIBLE else View.GONE
    }
    override fun initView() {
        EventBus.getDefault().register(this)
        rv_phrase.layoutManager = LinearLayoutManager(requireContext())
        getMorseCodeList()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    inner class PhraseAdapter(data: MutableList<MorseCodeListBean.DataBean>) :
        BaseQuickAdapter<MorseCodeListBean.DataBean, BaseViewHolder>(R.layout.item_phrase_model, data) {
        override fun convert(helper: BaseViewHolder, item: MorseCodeListBean.DataBean) {
            val itemView = helper.itemView
            itemView.tv_content.text = item.code
            itemView.tv_morse_code.text = "•一•一••一一一"

            itemView.tv_send.setOnClickListener {

            }
            itemView.tv_edit.setOnClickListener {

            }
            itemView.tv_light.setOnClickListener {

            }
        }
    }


    private fun getMorseCodeList() {
        RetrofitManager.service.getMorseCodeList(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<MorseCodeListBean?>(requireContext(), false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: MorseCodeListBean?) {
                        if (t!!.data.isNotEmpty()) {
                            val adapter = PhraseAdapter(t.data)
                            rv_phrase.adapter = adapter
                        }
                }
            })
    }

    override fun lazyLoad() {
    }

    override fun initListener() {
    }
}