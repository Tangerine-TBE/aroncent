package com.aroncent.module.history

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.aroncent.R
import com.aroncent.base.BaseFragment
import com.aroncent.base.RxSubscriber
import com.aroncent.ble.BleTool
import com.aroncent.event.ConnectStatusEvent
import com.aroncent.event.GetHistoryEvent
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.clj.fastble.BleManager
import com.aroncent.api.RetrofitManager
import com.aroncent.app.KVKey
import com.aroncent.module.main.BatteryBean
import com.aroncent.module.main.UpdateHeadPicEvent
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_history.*
import kotlinx.android.synthetic.main.frag_history.left_pic
import kotlinx.android.synthetic.main.item_history_left.view.item_history_code
import kotlinx.android.synthetic.main.item_history_left.view.item_history_content
import kotlinx.android.synthetic.main.item_history_left.view.item_history_time
import kotlinx.android.synthetic.main.top_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HistoryFragment : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.frag_history
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMsg(msg: GetHistoryEvent) {
        getHistory()
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
    fun onReceiveMsg(msg: ConnectStatusEvent) {
        tv_connected.visibility = if (msg.type == 1) View.VISIBLE else View.GONE
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: UpdateHeadPicEvent) {
        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(left_pic)

        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.partner_avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(right_pic)

    }
    override fun initView() {
        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(left_pic)
        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.partner_avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(right_pic)
        EventBus.getDefault().register(this)
        rv_history.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun lazyLoad() {
       getHistory()
        if (BleManager.getInstance().isConnected(BleTool.mBleDevice)){
            BleTool.sendInstruct("A5AAACFA05FFC5CCCA")
            tv_connected.visibility = View.VISIBLE
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
    inner class HistoryAdapter(data: MutableList<HistoryListBean.DataBean.ListBean>) :
        BaseMultiItemQuickAdapter<HistoryListBean.DataBean.ListBean, BaseViewHolder>(data) {
        init {
            addItemType(1, R.layout.item_history_left) //对方消息
            addItemType(0, R.layout.item_history_right) //自己消息
        }

        override fun convert(helper: BaseViewHolder, item: HistoryListBean.DataBean.ListBean) {
            val itemView = helper.itemView
            itemView.item_history_content.text = item.content
            var time = TimeUtils.millis2String(item.createtime.toLong()*1000,"yyyy/MM/dd HH:mm")
            time = if (item.isread==1){
                "$time [Read]"
            }else{
                "$time [Unread]"
            }
            var code = ""
            if (item.morsecode != null) {
                item.morsecode.forEach {
                    if (it.toString() == "0") {
                        code = "$code•"
                    }
                    if (it.toString() == "1") {
                        code += "一"
                    }
                }
            }
            itemView.item_history_time.text = time
            itemView.item_history_code.text = code

        }
    }

    private fun getHistory(){
        RetrofitManager.service.getHistory(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<HistoryListBean?>(requireContext(), true) {
                override fun _onError(message: String?) {
                    tv_heartbeat.text = "0"
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: HistoryListBean?) {
                    t?.let {
                        if (t.code==200){
                            if (t.data.list.isNotEmpty()){
                                val adapter = HistoryAdapter(t.data.list)
                                tv_heartbeat.text = t.data.list.size.toString()
                                rv_history.adapter = adapter
                            }
                        }
                    }
                }
            })
    }
    override fun initListener() {

    }
}