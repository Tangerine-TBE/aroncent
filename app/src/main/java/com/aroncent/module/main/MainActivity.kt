package com.aroncent.module.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.aroncent.R
import com.aroncent.app.KVKey
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.ble.BleAnswerEvent
import com.aroncent.ble.BleDefinedUUIDs
import com.aroncent.ble.BleTool
import com.aroncent.ble.ByteTransformUtil
import com.aroncent.event.GetHistoryEvent
import com.aroncent.event.ReadMsgEvent
import com.aroncent.module.history.HistoryFragment
import com.aroncent.module.home.BindPartnerFragment
import com.aroncent.module.home.HomeFragment
import com.aroncent.module.login.LoginActivity
import com.aroncent.module.login.RequestUserInfoBean
import com.aroncent.module.mine.MineFragment
import com.aroncent.utils.*
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ViewUtils
import com.bumptech.glide.Glide
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleScanAndConnectCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.ltwoo.estep.api.RetrofitManager
import com.tencent.mmkv.MMKV
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : BaseActivity() {
    val TAG = "MainActivity"

    private var mTab1: Fragment? = null
    private var mTab2: Fragment? = null
    private var mTab3: Fragment? = null

    //请求BLUETOOTH_CONNECT权限意图
    private val requestBluetoothConnect =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                //打开蓝牙
                enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                showToast(getString(R.string.android_12_ble_permission_hint))
                finish()
            }
        }

    //请求BLUETOOTH_SCAN权限意图
    private val requestBluetoothScan =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                //获得蓝牙权限后再获取位置权限
                if (!hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    connectBle()
                }
            } else {
                showToast(getString(R.string.android_12_ble_permission_hint))
                finish()
            }
        }

    //打开蓝牙意图
    private val enableBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (isAndroid12()) {
                    if (!hasPermission(this, Manifest.permission.BLUETOOTH_SCAN)) {
                        //请求权限
                        requestBluetoothScan.launch(Manifest.permission.BLUETOOTH_SCAN)
                    } else {
                        connectBle()
                    }
                } else {
                    if (!hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        requestLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        connectBle()
                    }
                }
            } else {
                showToast(getString(R.string.enable_bluetooth_hint))
                finish()
            }
        }

    //请求定位权限意图
    private val requestLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            //扫描蓝牙
            connectBle()
        } else {
            showToast(getString(R.string.opening_location_permission))
            finish()
        }
    }
    override fun layoutId(): Int {
        return  R.layout.activity_main
    }


    override fun initData() {
        if (getUserToken()!=""){
            getPartnerRequest()
            getSettings()
            getUserInfo()
        }
    }
    fun resetBg() {
        Glide.with(this)
            .load(R.drawable.tab_home)
            .into(iv_main_tab_1)
        Glide.with(this)
            .load(R.drawable.tab_history)
            .into(iv_main_tab_2)
        Glide.with(this)
            .load(R.drawable.tab_my)
            .into(iv_main_tab_3)
        tv_tab_1.setTextColor(ContextCompat.getColor(this,R.color.tabTextNormal))
        tv_tab_2.setTextColor(ContextCompat.getColor(this,R.color.tabTextNormal))
        tv_tab_3.setTextColor(ContextCompat.getColor(this,R.color.tabTextNormal))
    }
    private fun setSelect(i: Int) {
        resetBg()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        hideFragment(transaction)
        when (i) {
            0 -> {
                if (mTab1 == null) {
                    mTab1 = if (MMKV.defaultMMKV().getBoolean(KVKey.isBind,false)){
                        HomeFragment()
                    }else{
                        BindPartnerFragment()
                    }
                    transaction.add(R.id.view_stub_main, mTab1!!)
                } else {
                    transaction.show(mTab1!!)
                }
                Glide.with(this)
                    .load(R.drawable.tab_home_h)
                    .into(iv_main_tab_1)
                tv_tab_1.setTextColor(ContextCompat.getColor(this,R.color.white))
            }
            1 -> {
                if (mTab2 == null) {
                    mTab2 = HistoryFragment()
                    transaction.add(R.id.view_stub_main, mTab2!!)

                } else {
                    transaction.show(mTab2!!)
                }
                Glide.with(this)
                    .load(R.drawable.tab_history_h)
                    .into(iv_main_tab_2)
                tv_tab_2.setTextColor(ContextCompat.getColor(this,R.color.white))
            }
            2 -> {
                if (mTab3 == null) {
                    mTab3 = MineFragment()
                    transaction.add(R.id.view_stub_main, mTab3!!)
                } else {
                    transaction.show(mTab3!!)
                }
                Glide.with(this)
                    .load(R.drawable.tab_my_h)
                    .into(iv_main_tab_3)
                tv_tab_3.setTextColor(ContextCompat.getColor(this,R.color.white))
            }
        }
        transaction.commitAllowingStateLoss()
    }

    private fun hideFragment(transaction: FragmentTransaction) {
        if (mTab1 != null) {
            transaction.hide(mTab1!!)
        }
        if (mTab2 != null) {
            transaction.hide(mTab2!!)
        }
        if (mTab3 != null) {
            transaction.hide(mTab3!!)
        }
    }

    override fun initView() {
        if (getUserToken()==""){
            finish()
            startActivity(LoginActivity::class.java)
        } else {
            EventBus.getDefault().register(this)
            setSelect(0)
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMsg(msg: ReadMsgEvent) {
        readMsg(msg.msgId)
    }
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun connectBle(){
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setDeviceName(true, "RY_BLE") // 只扫描指定广播名的设备，可选,true:模糊查询
            .setScanTimeOut(8000) // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
            .build()

        BleManager.getInstance().initScanRule(scanRuleConfig)
        BleManager.getInstance().scanAndConnect(object : BleScanAndConnectCallback() {
            override fun onStartConnect() {

            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                Log.e(TAG, "onConnectFail"+exception.toString())
                WaitDialog.dismiss()
                showToast(getString(R.string.connect_fail))
                BleTool.mBleDevice = null
            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt?, status: Int) {
                Log.e(TAG, "onConnectSuccess")
                setEquipment(bleDevice.name)
                WaitDialog.dismiss()
                showToast("Connection succeeded")
                BleTool.setBleDevice(bleDevice)
                ThreadUtils.runOnUiThreadDelayed({
                    openBleNotify(bleDevice)
                },100)
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean, device: BleDevice?,
                gatt: BluetoothGatt?, status: Int
            ) {
                Log.e(TAG, "onDisConnected")
                showToast(getString(R.string.device_disconnected))
            }

            override fun onScanStarted(success: Boolean) {
                WaitDialog.show("Device Connecting...")
            }

            override fun onScanning(bleDevice: BleDevice?) {
            }

            override fun onScanFinished(scanResult: BleDevice?) {
                if (scanResult==null){
                    WaitDialog.dismiss()
                }
            }
        })
    }
    //打开蓝牙通知
    private fun openBleNotify(mBleDevice: BleDevice) {
        //操作通知
        BleManager.getInstance().notify(
            mBleDevice,
            BleDefinedUUIDs.SERVICE.toString(),
            BleDefinedUUIDs.CHARACTERISTIC_NOTIFY.toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    LogUtils.eTag(TAG, "notify success")
//                    //测试指令
//                    val xorStr = BleTool.getXOR("01"+"0101FFFFFF0A")
//                    BleTool.sendInstruct("A5AAAC"+xorStr+"01"+"0101FFFFFF0A"+"C5CCCA")
                }

                override fun onNotifyFailure(exception: BleException?) {
                    WaitDialog.dismiss()
                }

                @SuppressLint("SetTextI18n")
                override fun onCharacteristicChanged(data: ByteArray?) {
                    ViewUtils.runOnUiThread {
                        LogUtils.eTag("$TAG Answer", ByteTransformUtil.bytesToHex(data).uppercase())
                        //03指令示例： A5AAAC00030400C5CCCA
                        val str = ByteTransformUtil.bytesToHex(data).uppercase()
                        when{
                            str.contains("0241434B")->{
                                EventBus.getDefault().post(BleAnswerEvent("notify", str))
                            }
                            str.substring(8,10)=="03"->{
                               //发送通知给对方
                                sendMorseCode(str)
                            }
                        }
                    }
                }
            })
    }

    private fun sendMorseCode(instructions:String){
        RetrofitManager.service.sendMorseCode(hashMapOf("morsecode" to instructions))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    if (t!!.code==200){
                        //告知硬件对方开始显示
                        val xorStr = BleTool.getXOR("0401")
                        BleTool.sendInstruct("A5AAAC"+xorStr+"0401C5CCCA")
                        EventBus.getDefault().post(GetHistoryEvent()) //刷新历史记录
                    }
                }
            })
    }

    private fun setEquipment(name:String){
        RetrofitManager.service.setEquipment(hashMapOf("equipment" to name))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    if (t!!.code==200){

                    }
                }
            })
    }
    override fun initListener() {
        //设置图标点击缩放
        ClickUtils.applyPressedViewScale(arrayOf(LL_tab_1,LL_tab_2,LL_tab_3),
            floatArrayOf(-0.2f,-0.2f,-0.2f))

        ClickUtils.applySingleDebouncing(LL_tab_1, 300) {
            setSelect(0)
        }
        ClickUtils.applySingleDebouncing(LL_tab_2, 300) {
            setSelect(1)
        }
        ClickUtils.applySingleDebouncing(LL_tab_3, 300) {
            setSelect(2)
        }
    }

    private fun getPartnerRequest(){
        RetrofitManager.service.getPartnerRequest(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        if (t.code == 200) {
                            showToast(t.msg)
                            CustomDialog
                                .build()
                                .setMaskColor(getColor(R.color.dialogMaskColor))
                                .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_partner_add) {
                                    override fun onBind(dialog: CustomDialog?, v: View?) {
                                        v!!.let {
                                            val refuse = v.findViewById<TextView>(R.id.tv_cancel)
                                            val agree = v.findViewById<TextView>(R.id.tv_confirm)
                                            refuse.setOnClickListener {
                                                dialog!!.dismiss()
                                                confirmEmail("0")
                                            }
                                            agree.setOnClickListener {
                                                dialog!!.dismiss()
                                                confirmEmail("3")
                                            }
                                        }
                                    }
                                })
                                .setCancelable(false)
                                .show()
                        }
                    }
                }
            })
    }
    private fun getSettings(){
        RetrofitManager.service.getsettings(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<SettingBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: SettingBean?) {
                    t?.let {
                        if (t.code == 200) {
                            if (t.data!=null){
                                MMKV.defaultMMKV().encode(KVKey.long_shake,t.data.long_shake)
                                MMKV.defaultMMKV().encode(KVKey.short_shake,t.data.short_shake)
                                MMKV.defaultMMKV().encode(KVKey.light_color,t.data.lightcolor)
                                MMKV.defaultMMKV().encode(KVKey.long_flash,t.data.long_light)
                                MMKV.defaultMMKV().encode(KVKey.short_flash,t.data.short_light)
                                MMKV.defaultMMKV().encode(KVKey.equipment,t.data.equipment)
                                if (isAndroid12()) {
                                    //检查是否有BLUETOOTH_CONNECT权限
                                    if (hasPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT)) {
                                        //打开蓝牙
                                        enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                                    } else {
                                        //请求权限
                                        requestBluetoothConnect.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                    }
                                } else {
                                    //不是Android12 直接打开蓝牙
                                    enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                                }
                            }
                        }
                    }
                }
            })
    }
    private fun getUserInfo(){
        RetrofitManager.service.getUserInfo(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<RequestUserInfoBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: RequestUserInfoBean?) {
                    t?.let {
                        if (t.code == 200) {
                            if (t.data!=null){
                               setUserInfoToSp(t.data.userInfo)
                            }
                        }
                    }
                }
            })
    }


    private fun confirmEmail(status:String){
        RetrofitManager.service.confirmEmail(hashMapOf("status" to status))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        showToast(t.msg)
                    }
                }
            })
    }

    private fun readMsg(id:Int){
        RetrofitManager.service.readMsg(hashMapOf("id" to id.toString()))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {

                }
            })
    }

    override fun start() {
    }

}