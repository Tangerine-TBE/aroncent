package com.aroncent.module.history

import android.annotation.SuppressLint
import androidx.recyclerview.widget.LinearLayoutManager
import com.aroncent.R
import com.aroncent.base.BaseFragment
import com.aroncent.base.RxSubscriber
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ltwoo.estep.api.RetrofitManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_history.*
import kotlinx.android.synthetic.main.item_history_left.view.*

class HistoryFragment : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.frag_history
    }

    override fun initView() {
        rv_history.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun lazyLoad() {
       getHistory()
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