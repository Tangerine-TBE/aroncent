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
import com.ltwoo.estep.api.RetrofitManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_history.*
import kotlinx.android.synthetic.main.item_history_left.view.*
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
    fun onReceiveMsg(msg: ConnectStatusEvent) {
        tv_connected.visibility = if (msg.type == 1) View.VISIBLE else View.GONE
    }
    override fun initView() {
        EventBus.getDefault().register(this)
        rv_history.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun lazyLoad() {
       getHistory()
        if (BleManager.getInstance().isConnected(BleTool.mBleDevice)){
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

            when (helper.itemViewType) {
                0 -> {

                }
                1 -> {

                }
            }
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
                        if (t.data.list.isNotEmpty()){
                            val adapter = HistoryAdapter(t.data.list)
                            tv_heartbeat.text = t.data.list.size.toString()
                            rv_history.adapter = adapter
                        }
                    }
                }
            })
    }
    override fun initListener() {

    }
}