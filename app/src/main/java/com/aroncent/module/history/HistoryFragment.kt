package com.aroncent.module.history

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
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
import com.aroncent.api.SendNoRsp
import com.aroncent.app.KVKey
import com.aroncent.app.MyApplication
import com.aroncent.base.BaseBean
import com.aroncent.db.MsgData
import com.aroncent.module.home.MorseCodeListBean
import com.aroncent.module.home.weight.MyLinearLayoutManager
import com.aroncent.module.main.BatteryBean
import com.aroncent.module.main.UpdateHeadPicEvent
import com.aroncent.utils.showToast
import com.blankj.utilcode.util.ColorUtils.getColor
import com.bumptech.glide.Glide
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_history.*
import kotlinx.android.synthetic.main.frag_history.left_pic
import kotlinx.android.synthetic.main.frag_history.view.refresh
import kotlinx.android.synthetic.main.item_history_left.view.item_edit_info
import kotlinx.android.synthetic.main.item_history_left.view.item_history_code
import kotlinx.android.synthetic.main.item_history_left.view.item_history_content
import kotlinx.android.synthetic.main.item_history_left.view.item_history_content1
import kotlinx.android.synthetic.main.item_history_left.view.item_history_time
import kotlinx.android.synthetic.main.item_history_right.view.btn_edit
import kotlinx.android.synthetic.main.item_history_right.view.btn_send
import kotlinx.android.synthetic.main.top_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.HashMap

class HistoryFragment : BaseFragment(), OnRefreshListener, OnLoadMoreListener {
    private var list = arrayListOf<MorseCodeListBean.DataBean>()
    private var currentPage = "1"
    private fun loadMoreSize(): String {
        currentPage = (currentPage.toInt() + 1).toString()
        return currentPage
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_history
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMsg(msg: GetHistoryEvent) {
        getMorseCodeList(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMsg(msg: BatteryBean) {
        tv_connected.visibility = View.VISIBLE
        tv_battery.visibility = View.VISIBLE
        iv_battery.visibility = View.VISIBLE
        when (msg.value.toInt()) {
            in 0..20 -> {
                iv_battery.setImageResource(R.drawable.b_20)
            }

            in 21..50 -> {
                iv_battery.setImageResource(R.drawable.b_50)
            }

            in 51..70 -> {
                iv_battery.setImageResource(R.drawable.b_70)
            }

            in 71..90 -> {
                iv_battery.setImageResource(R.drawable.b_90)
            }

            in 91..100 -> {
                iv_battery.setImageResource(R.drawable.b_100)
            }

            else -> {
                iv_battery.setImageResource(R.drawable.b_100)
            }
        }
        tv_battery.text = msg.value + "%"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMsg(msg: ConnectStatusEvent) {
        tv_connected.visibility = if (msg.type == 1) View.VISIBLE else View.GONE
    }

    private fun getMorseCodeList(isRefresh: Boolean) {
        RetrofitManager.service.getMorseCodeList(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<MorseCodeListBean?>(requireContext(), true) {
                override fun _onError(message: String?) {

                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: MorseCodeListBean?) {
                    if (t!!.data.isNotEmpty()) {
                        list.addAll(t.data)
                        getHistory(isRefresh)
//                        getUserPhraseList()
                    }
                }
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: UpdateHeadPicEvent) {
        Glide.with(this).load(MMKV.defaultMMKV().decodeString(KVKey.avatar, "")).circleCrop()
            .error(R.drawable.head_default_pic).into(left_pic)

        Glide.with(this).load(MMKV.defaultMMKV().decodeString(KVKey.partner_avatar, ""))
            .circleCrop().error(R.drawable.head_default_pic).into(right_pic)

    }

    override fun initView() {
        Glide.with(this).load(MMKV.defaultMMKV().decodeString(KVKey.avatar, "")).circleCrop()
            .error(R.drawable.head_default_pic).into(left_pic)
        Glide.with(this).load(MMKV.defaultMMKV().decodeString(KVKey.partner_avatar, ""))
            .circleCrop().error(R.drawable.head_default_pic).into(right_pic)
        EventBus.getDefault().register(this)
        rv_history.layoutManager = MyLinearLayoutManager(requireContext())
        val adapter = HistoryAdapter(mutableListOf())
        rv_history.adapter = adapter

    }

    override fun lazyLoad() {
        refresh.autoRefresh()
//       getHistory()
        if (BleManager.getInstance().isConnected(BleTool.mBleDevice)) {
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

        fun refreshData(values: MutableList<HistoryListBean.DataBean.ListBean>) {
            data.clear()
            data.addAll(values)
            notifyItemRangeChanged(0, data.size)

        }

        fun loadMoreData(values: MutableList<HistoryListBean.DataBean.ListBean>) {
            val originSize = data.size
            data.addAll(values)
            notifyItemRangeChanged(originSize - 1, data.size)
        }

        override fun convert(helper: BaseViewHolder, item: HistoryListBean.DataBean.ListBean) {
            val itemView = helper.itemView
            itemView.item_history_content.text = item.content
            itemView.item_history_content1.text = item.morseword
            val time: String = if (item.isread == 1) {
                "${item.formatted_createtime} [Read]"
            } else {
                "${item.formatted_createtime} [Unread]"
            }
            var code = ""
            if (!TextUtils.isEmpty(item.morseword)) {
                var shake: String
                item.morseword.forEachIndexed { index, c ->
                    val value = c.toString()
                    for (i in list) {
                        if (i.code == value) {
                            shake = i.shake
                            shake.forEach {
                                if (it.toString() == "0") {
                                    code = "$code•"
                                }
                                if (it.toString() == "1") {
                                    code += "一"
                                }
                            }
                            if (index != item.morseword.length - 1) {
                                code += "    "
                            }
                            break
                        }
                    }
                }
            }
            itemView.item_edit_info.text = item.remark
            itemView.item_history_time.text = time
            itemView.item_history_code.text = code
            if (itemView.btn_send != null) {
                itemView.btn_send.setOnClickListener {
                    CustomDialog.build()
                        .setMaskColor(requireContext().getColor(R.color.dialogMaskColor))
                        .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_tips) {
                            override fun onBind(dialog: CustomDialog, v: View) {
                                v.let {
                                    val tip = v.findViewById<TextView>(R.id.tv_tip)
                                    tip.text = "Resend the msg ?"
                                    val confirm = v.findViewById<TextView>(R.id.tv_confirm)
                                    val cancel = v.findViewById<TextView>(R.id.tv_cancel)
                                    cancel.setOnClickListener {

                                        dialog.dismiss()
                                    }
                                    confirm.setOnClickListener {


                                        dialog.dismiss()
                                        sendPhrase(item.id)
                                    }
                                }
                            }
                        }).show()
                }
            }
            if (itemView.btn_edit != null) {
                itemView.btn_edit.setOnClickListener {
                    CustomDialog
                        .build()
                        .setMaskColor(getColor(R.color.dialogMaskColor))
                        .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_edit) {
                            override fun onBind(dialog: CustomDialog?, v: View?) {
                                v!!.let {
                                    val tip = v.findViewById<EditText>(R.id.tv_tip)
                                    tip.setText(itemView.item_edit_info.text)
                                    val confirm = v.findViewById<TextView>(R.id.tv_confirm)
                                    val cancel = v.findViewById<TextView>(R.id.tv_cancel)
                                    cancel.setOnClickListener {
                                        dialog!!.dismiss()
                                    }
                                    confirm.setOnClickListener {
                                        val map = hashMapOf<String, String>()
                                        map["Id"] = item.id.toString()
                                        map["Content"] = item.content
                                        map["remark"] = tip.text.toString()
                                        RetrofitManager.service.updateHistory(map).subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(object : RxSubscriber<BaseBean?>(requireContext(), true) {
                                                override fun _onError(message: String?) {
                                                }

                                                override fun onSubscribe(d: Disposable) {

                                                }

                                                @SuppressLint("SetTextI18n")
                                                override fun _onNext(t: BaseBean?) {
                                                    if (t!!.code == 200) {
                                                        showToast("Success")
                                                        refresh.autoRefresh()
                                                    }
                                                }
                                            })
                                        dialog!!.dismiss()
                                    }
                                }
                            }
                        })
                        .show()
                }
            }
        }

        private fun sendPhrase(id: Int) {
            val map = hashMapOf<String, String>()
            map["id"] = id.toString()
            RetrofitManager.service.historySend(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : RxSubscriber<SendNoRsp?>(requireContext(), true) {
                    override fun _onError(message: String?) {
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    @SuppressLint("SetTextI18n")
                    override fun _onNext(t: SendNoRsp?) {
                        if (t!!.code == 200) {
                            showToast("Success")
                            getMorseCodeList(true)
                        }
                    }
                })
        }

    }

    private fun getHistory(isRefresh: Boolean) {
        RetrofitManager.service.getHistory(
            hashMapOf(
                Pair(
                    "page",
                    if (isRefresh) "1" else loadMoreSize()
                )
            )
        )
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
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
                                tv_heartbeat.text = t.data.list.size.toString()
                                if (isRefresh) {
                                    (rv_history.adapter as HistoryAdapter).refreshData(t.data.list)
                                    rv_history.scrollToPosition(0)
                                    currentPage = "1"
                                } else {
                                    (rv_history.adapter as HistoryAdapter).loadMoreData(t.data.list)
                                }
                            }
                        }
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    if (refresh.isRefreshing) {
                        refresh.finishRefresh()

                    }
                    if (refresh.isLoading) {
                        refresh.finishLoadMore()
                    }
                }
            })
    }


    override fun initListener() {
        refresh.setOnRefreshListener(this)
        refresh.setOnLoadMoreListener(this)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        getMorseCodeList(true)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        getMorseCodeList(false)
    }
}