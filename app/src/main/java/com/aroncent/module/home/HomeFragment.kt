package com.aroncent.module.home

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.aroncent.R
import com.aroncent.base.BaseFragment
import com.aroncent.base.RxSubscriber
import com.aroncent.event.ConnectStatusEvent
import com.aroncent.event.GetHistoryEvent
import com.aroncent.event.GetUserPhraseEvent
import com.aroncent.jpush.PushInfoType
import com.aroncent.module.phrase.EditPhraseActivity
import com.aroncent.utils.showToast
import com.blankj.utilcode.util.ClickUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.aroncent.api.RetrofitManager
import com.aroncent.app.KVKey
import com.aroncent.module.main.UpdateHeadPicEvent
import com.bumptech.glide.Glide
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_home.*
import kotlinx.android.synthetic.main.frag_mine.*
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMsg(msg: GetUserPhraseEvent) {
        getUserPhraseList()
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: UpdateHeadPicEvent) {
        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(left_pic)
    }
    override fun initView() {
        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(left_pic)
        EventBus.getDefault().register(this)
        rv_phrase.layoutManager = LinearLayoutManager(requireContext())
        getUserPhraseList()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    inner class PhraseAdapter(data: MutableList<UserPhraseListBean.DataBean>) :
        BaseQuickAdapter<UserPhraseListBean.DataBean, BaseViewHolder>(R.layout.item_phrase_model, data) {
        override fun convert(helper: BaseViewHolder, item: UserPhraseListBean.DataBean) {
            val itemView = helper.itemView
            itemView.tv_content.text = item.content
            var code = ""
            if (item.shake != null) {
                item.shake.forEach {
                    if (it.toString() == "0") {
                        code = "$code•"
                    }
                    if (it.toString() == "1") {
                        code += "一"
                    }
                }
            }
            itemView.tv_morse_code.text = code

            ClickUtils.applySingleDebouncing(itemView.tv_send,500){
                sendPhrase(item.id)
            }

            itemView.tv_edit.setOnClickListener {
                startActivity(Intent(requireContext(), EditPhraseActivity::class.java)
                    .putExtra("id",item.id.toString())
                    .putExtra("content",item.content)
                )
            }
            itemView.tv_light.setOnClickListener {

            }
        }
    }

    private fun sendPhrase(id:Int){
        val map = hashMapOf<String,String>()
        map["id"] = id.toString()
        map["infotype"] = PushInfoType.App
        RetrofitManager.service.sendPhrase(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<SendPhraseBean?>(requireContext(), false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: SendPhraseBean?) {
                    if (t!!.code == 200) {
                        showToast("Success")
                        EventBus.getDefault().post(GetHistoryEvent()) //刷新历史记录
                    }
                }
            })
    }

    private fun getUserPhraseList() {
        RetrofitManager.service.getUserPhraseList(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<UserPhraseListBean?>(requireContext(), false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: UserPhraseListBean?) {
                    if (t!!.code == 200) {
                        val adapter = PhraseAdapter(t.data)
                        rv_phrase.adapter = adapter
                    } else {
                        val adapter = PhraseAdapter(mutableListOf())
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