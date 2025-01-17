package com.aroncent.module.phrase

import android.annotation.SuppressLint
import androidx.recyclerview.widget.GridLayoutManager
import com.aroncent.R
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.event.GetUserPhraseEvent
import com.aroncent.module.home.MorseCodeListBean
import com.aroncent.utils.showToast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.aroncent.api.RetrofitManager
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_add_phrase.*
import kotlinx.android.synthetic.main.act_edit_phrase.nested_scroll_view
import kotlinx.android.synthetic.main.act_edit_phrase.tv_morse_code
import kotlinx.android.synthetic.main.item_morse_code.view.*
import kotlinx.android.synthetic.main.item_select_morse_code.view.*
import kotlinx.android.synthetic.main.top_bar.iv_back
import org.greenrobot.eventbus.EventBus

class AddPhraseActivity : BaseActivity() {

    val selectCode = arrayListOf<MorseCodeListBean.DataBean>()
    val selectCodeArray = arrayListOf<MorseCodeListBean.DataBean>()
    override fun layoutId(): Int {
        return R.layout.act_add_phrase
    }

    override fun initData() {
        rv_morse_code.layoutManager = GridLayoutManager(this, 3)
        rv_select_code.layoutManager = GridLayoutManager(this, 7)
        for (i in 1..7){
            selectCodeArray.add(MorseCodeListBean.DataBean())
        }
        rv_select_code.adapter = SelectMorseCodeAdapter(selectCodeArray)
        getMorseCodeList()
    }

    override fun initView() {
    }

    override fun initListener() {
        iv_back.setOnClickListener { finish() }
        tv_save.setOnClickListener {
            addPhrase()
        }
        iv_del_code.setOnClickListener {
            if (selectCode.isNotEmpty()){
                selectCode.removeLast()
                refreshSelectCode()
            }
        }
    }

    inner class MorseCodeAdapter(data: MutableList<MorseCodeListBean.DataBean>) :
        BaseQuickAdapter<MorseCodeListBean.DataBean, BaseViewHolder>(
            R.layout.item_morse_code,
            data
        ) {
        override fun convert(helper: BaseViewHolder, item: MorseCodeListBean.DataBean) {
            val itemView = helper.itemView
            var text = item.code
            item.shake.forEach {
                if (it.toString()=="0"){
                    text += "•"
                }
                if (it.toString()=="1"){
                    text += "一"
                }
            }
            itemView.item_morse_code.text = text
            itemView.setOnClickListener {
                if (selectCode.size < 7){
                    selectCode.add(item)
                    refreshSelectCode()
                }
            }
        }
    }

    inner class SelectMorseCodeAdapter(data: MutableList<MorseCodeListBean.DataBean>) :
        BaseQuickAdapter<MorseCodeListBean.DataBean, BaseViewHolder>(
            R.layout.item_select_morse_code,
            data
        ) {
        override fun convert(helper: BaseViewHolder, item: MorseCodeListBean.DataBean) {
            val itemView = helper.itemView
            itemView.item_select_morse_code.text = item.code
        }
    }

    private fun refreshSelectCode() {
        var text = ""
        selectCodeArray.forEach {
            it.code = ""
        }
        selectCode.forEachIndexed { index, dataBean ->
            dataBean.shake.forEach { char->
                if (char.toString()=="0"){
                    text += "•"
                }
                if (char.toString()=="1"){
                    text += "一"
                }

            }
            if(index < selectCode.size - 1){
                text +="   "
            }
            selectCodeArray[index].code = dataBean.code
        }
        tv_morse_code.text = text
        rv_select_code.adapter = SelectMorseCodeAdapter(selectCodeArray)
    }


    private fun getMorseCodeList() {
        RetrofitManager.service.getMorseCodeList(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<MorseCodeListBean?>(this, true) {
                override fun _onError(message: String?) {

                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: MorseCodeListBean?) {
                    if (t!!.data.isNotEmpty()) {
                        val adapter = MorseCodeAdapter(t.data)
                        rv_morse_code.adapter = adapter
                    }
                }
            })
    }

    private fun addPhrase(){
        if (et_content.text.toString()==""){
            showToast("Please enter content")
            return
        }
        if (selectCode.isEmpty()){
            showToast("Please select Morse Code")
            return
        }
        var shake = ""
        var moreword = ""
        selectCode.forEach {
            it.shake.forEach { char->
                if (char.toString()=="0"){
                    shake = "$shake,0"
                }
                if (char.toString()=="1"){
                    shake = "$shake,1"
                }
            }
            moreword += it.code
        }
        val map = hashMapOf<String,String>()
        map["content"] = et_content.text.toString()
        map["shake"] = shake.substring(1)
        map["morseword"] = moreword
        RetrofitManager.service.addPhrase(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    if (t!!.code==200) {
                        EventBus.getDefault().post(GetUserPhraseEvent())
                        showToast("Success")
                        finish()
                    }
                }
            })
    }
    override fun start() {
    }
}