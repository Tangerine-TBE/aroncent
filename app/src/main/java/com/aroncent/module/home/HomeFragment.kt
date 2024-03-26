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
import com.aroncent.event.ReadMsgEvent
import com.aroncent.module.history.HistoryFragment
import com.aroncent.module.history.HistoryListBean
import com.aroncent.module.main.BatteryBean
import com.aroncent.module.main.UpdateHeadPicEvent
import com.aroncent.module.phrase.AddPhraseActivity
import com.bumptech.glide.Glide
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_history.refresh
import kotlinx.android.synthetic.main.frag_history.rv_history
import kotlinx.android.synthetic.main.frag_history.tv_heartbeat
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
    fun onReceiveMsg(msg: BatteryBean) {
        tv_connected.visibility = View.VISIBLE
        tv_battery.visibility = View.VISIBLE
        iv_battery.visibility = View.VISIBLE
        when(msg.value.toInt()){
            in 0..20->{
                iv_battery.setImageResource(R.drawable.b_20)
            }
            in 21..50->{
                iv_battery.setImageResource(R.drawable.b_50)
            }
            in 51..70->{
                iv_battery.setImageResource(R.drawable.b_70)
            }
            in 71..90->{
                iv_battery.setImageResource(R.drawable.b_90)
            }
            in 91..100->{
                iv_battery.setImageResource(R.drawable.b_100)
            }
            else->{
                iv_battery.setImageResource(R.drawable.b_100)
            }
        }
        tv_battery.text = msg.value+"%"
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: UpdateHeadPicEvent) {
        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(left_pic)
        tv_left_name.text = MMKV.defaultMMKV().decodeString(KVKey.nickname,"")

        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.partner_avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(right_pic)
        tv_right_name.text = MMKV.defaultMMKV().decodeString(KVKey.partner_nickname,"")

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMsg(msg: ReadMsgEvent) {
        tv_new_msg.text = "Receive the new massage from "+tv_right_name.text
        tv_new_msg2.visibility = View.VISIBLE

        RetrofitManager.service.getHistory(hashMapOf(Pair("page","1"))).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<HistoryListBean?>(requireContext(), true) {
                override fun _onError(message: String?) {
                    tv_heartbeat.text = "0"
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: HistoryListBean?) {
                    t?.let {
                        if (t.code == 200) {
                            if (t.data.list.isNotEmpty()) {
                              val listBean =   t.data.list[0]
                                tv_new_msg2.text = listBean.content
                            }
                        }
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                }
            })
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
            itemView.tv_content.text = "${(helper.position+1)}.${item.content}"
            itemView.tv_real_morse_code.text = item.morseword
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
            .subscribe(object : RxSubscriber<SendPhraseBean?>(requireContext(), true) {
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
        tv_my_template.setOnClickListener {
            startActivity(Intent(requireContext(), AddPhraseActivity::class.java))
        }
    }
}