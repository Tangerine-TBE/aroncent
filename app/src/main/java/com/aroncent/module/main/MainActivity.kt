package com.aroncent.module.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.Orientation
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import cn.jpush.android.api.JPushInterface
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.aroncent.R
import com.aroncent.api.RetrofitManager
import com.aroncent.app.KVKey
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.ble.BleAnswerEvent
import com.aroncent.ble.BleDefinedUUIDs
import com.aroncent.ble.BleTool
import com.aroncent.ble.ByteTransformUtil
import com.aroncent.ble.DeviceConfig
import com.aroncent.event.ConnectStatusEvent
import com.aroncent.event.GetHistoryEvent
import com.aroncent.event.ReConnectEvent
import com.aroncent.event.ReadMsgEvent
import com.aroncent.jpush.PushInfoType
import com.aroncent.module.add_partner.PartnerAddBean
import com.aroncent.module.history.HistoryFragment
import com.aroncent.module.home.BindPartnerFragment
import com.aroncent.module.home.HomeFragment
import com.aroncent.module.login.LoginActivity
import com.aroncent.module.login.RequestUserInfoBean
import com.aroncent.module.mine.MineFragment
import com.aroncent.observe.MainViewModel
import com.aroncent.utils.*
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleScanAndConnectCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.facebook.CallbackManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import com.onesignal.OneSignal
import com.tencent.mmkv.MMKV
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Thread.sleep
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.concurrent.thread


class MainActivity : BaseActivity() {
    val TAG = "MainActivity"

    private var mTab1: Fragment? = null
    private var mTab2: Fragment? = null
    private var mTab3: Fragment? = null
    private lateinit var deviceConfirmDialog: CustomDialog;
    private lateinit var mainViewModel: MainViewModel
    public lateinit var callbackManager : CallbackManager

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
    private val requestLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                //扫描蓝牙
                connectBle()
            } else {
                showToast(getString(R.string.opening_location_permission))
                finish()
            }
        }

    override fun layoutId(): Int {
        return R.layout.activity_main
    }

    fun getkeyhash() {
        try {
            val info = packageManager.getPackageInfo("com.aroncent", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash", "KeyHash:" + Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }

    override fun initData() {
        if (getUserToken() != "") {
            updateRid()
            getPartnerRequest()
            getUserInfo()
        }
    }

    private fun updateRid() {
        val map = hashMapOf<String, String>()
        map["jg_pushid"] = JPushInterface.getRegistrationID(this)
        map["onesignal_pushid"] = OneSignal.getDeviceState()?.userId ?: ""
        RetrofitManager.service.updaterid(map).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {

                    }
                }
            })
    }

    fun resetBg() {
        Glide.with(this).load(R.drawable.tab_home).into(iv_main_tab_1)
        Glide.with(this).load(R.drawable.tab_history).into(iv_main_tab_2)
        Glide.with(this).load(R.drawable.tab_my).into(iv_main_tab_3)
        tv_tab_1.setTextColor(ContextCompat.getColor(this, R.color.tabTextNormal))
        tv_tab_2.setTextColor(ContextCompat.getColor(this, R.color.tabTextNormal))
        tv_tab_3.setTextColor(ContextCompat.getColor(this, R.color.tabTextNormal))
    }

    public fun setSelect(i: Int) {
        resetBg()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        hideFragment(transaction)
        when (i) {
            0 -> {
                if (mTab1 == null) {
                    mTab1 = if (MMKV.defaultMMKV().getBoolean(KVKey.isBind, false)) {
                        HomeFragment()
                    } else {
                        BindPartnerFragment()
                    }
                    transaction.add(R.id.view_stub_main, mTab1!!)
                } else {
                    transaction.show(mTab1!!)
                }
                Glide.with(this).load(R.drawable.tab_home_h).into(iv_main_tab_1)
                tv_tab_1.setTextColor(ContextCompat.getColor(this, R.color.white))
            }

            1 -> {
                if (mTab2 == null) {
                    mTab2 = HistoryFragment()
                    transaction.add(R.id.view_stub_main, mTab2!!)
                } else {
                    transaction.show(mTab2!!)
                }
                Glide.with(this).load(R.drawable.tab_history_h).into(iv_main_tab_2)
                tv_tab_2.setTextColor(ContextCompat.getColor(this, R.color.white))
            }

            2 -> {
                if (mTab3 == null) {
                    mTab3 = MineFragment()
                    transaction.add(R.id.view_stub_main, mTab3!!)
                } else {
                    transaction.show(mTab3!!)
                }
                Glide.with(this).load(R.drawable.tab_my_h).into(iv_main_tab_3)
                tv_tab_3.setTextColor(ContextCompat.getColor(this, R.color.white))
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
        getkeyhash()
        callbackManager = CallbackManager.Factory.create();
        if (getUserToken() == "") {
            finish()
            startActivity(LoginActivity::class.java)
        } else {
            EventBus.getDefault().register(this)
            setSelect(0)
        }
    }

    private fun test1() {
        val str = "A5AAAC0103030F0E00C5CCCA"
        val length = addZeroForNum((str.substring(10, 12).toInt(16)).toString().uppercase(), 2)
        val content = str.substring(12, str.length - 6)
        var morseData = ""
        val morseDelay = arrayOfNulls<String>(length.toInt())
        BleTool.getInstructStringArray(content).forEachIndexed { index, it ->
            if (it != null) {
                val unKnowNum = it.toInt(16)
                if (unKnowNum < 128) {
                    morseDelay[index] = it.toInt(16).toString()
                    LogUtils.e("短按间隔-$it-${it.toInt(16)}-${it.toInt(16) * 0.1}秒")
                } else if (unKnowNum == 128) {
                    morseDelay[index] = 1.toString()
                    LogUtils.e("长按间隔-$it-${it.toInt(16)}-0.1秒")
                } else {
                    morseDelay[index] = (unKnowNum - 128).toString()
                    LogUtils.e("长按间隔-$it-${it.toInt(16)}-${(unKnowNum - 128) * 0.1}秒")
                }
                val char = it.let { it1 -> toBinary(it1, 8).reversed() }
                morseData += char
            }

        }

        morseData = morseData.substring(0, length.toInt())
        //这里得到摩斯密码表示的长按和短按 eg: 010100
        Log.e("JPush morseData", morseData.substring(0, length.toInt()))

        //组装01指令的数据域
        var instructData = ""
        morseData.forEachIndexed { index, it ->
            instructData += if (it.toString() == "0") {
                getShortPressHex(morseDelay[index])
            } else {
                getLongPressHex(morseDelay[index])
            }
        }
        //帧数长度
        val frame_length = addZeroForNum((instructData.length / 8).toString(16), 2).uppercase()
        val instruct =
            "A5AAAC" + BleTool.getXOR("01$frame_length" + DeviceConfig.loop_number + instructData) + "01$frame_length" + DeviceConfig.loop_number + instructData + "C5CCCA"

        Log.e("JPush 03指令转成01指令：", instruct)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMsg(msg: ReadMsgEvent) {
        readMsg(msg.msgId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReconnect(msg: ReConnectEvent) {
        connectBle()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    /** getUserInfo when the data response then would go inside  the connectBle */
    private fun connectBle() {
        if (!isLocServiceEnable(this)) {
            showToast(getString(R.string.open_the_location_service))
            showDisconnectDialog()
            return
        }
        if (!BleManager.getInstance().isBlueEnable) {
            showDisconnectDialog()
            showToast(getString(R.string.enable_bluetooth_hint))
            return
        }
        val scanRuleConfig =
            BleScanRuleConfig.Builder()
                .setDeviceName(true, "aroncent-")// 只扫描指定广播名的设备，可选,true:模糊查询
                .setScanTimeOut(8000) // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
                .build()

        BleManager.getInstance().initScanRule(scanRuleConfig)
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
            }

            override fun onScanning(bleDevice: BleDevice) {
                val bondedDevice = MMKV.defaultMMKV().decodeString(KVKey.equipment, "")
                if ("" == bondedDevice) {
                    if (!TextUtils.isEmpty(bleDevice.mac)) {
                        BleManager.getInstance().cancelScan()
                        deviceConfirmDialog = CustomDialog.build()
                            .setMaskColor(getColor(R.color.dialogMaskColor))
                            .setCustomView(object :
                                OnBindView<CustomDialog>(R.layout.dialog_confirm_connect) {
                                override fun onBind(
                                    dialog: CustomDialog?,
                                    v: View
                                ) {
                                    val changeTextLength = bleDevice.name.length
                                    val deviceTitleString =
                                        "Arrow ${bleDevice.name} to Receive Your telphone Notifications?"
                                    val deviceContentString =
                                        "when connected,all notifications you receive on you telphne will alsobe sent to ${bleDevice.name} and may be shown n its display"
                                    val titleSpanString = SpannableStringBuilder(deviceTitleString)
                                    val contentSpanString =
                                        SpannableStringBuilder(deviceContentString)
                                    val titleSpanForeground = ForegroundColorSpan(
                                        Color.YELLOW
                                    )
                                    titleSpanString.setSpan(
                                        titleSpanForeground,
                                        6,
                                        (6 + changeTextLength),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    contentSpanString.setSpan(
                                        titleSpanForeground,
                                        80,
                                        (80 + changeTextLength),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    val tvDeviceTitle =
                                        v.findViewById<TextView>(R.id.tv_device_title)
                                    val tvDeviceContent =
                                        v.findViewById<TextView>(R.id.tv_device_content)
                                    val btn_cancel = v.findViewById<Button>(R.id.btn_cancel)
                                    val btn_confirm = v.findViewById<Button>(R.id.btn_confirm)
                                    btn_cancel.setOnClickListener {
                                        dialog?.dismiss()
                                    }
                                    btn_confirm.setOnClickListener {
                                        BleManager.getInstance()
                                            .connect(bleDevice, object : BleGattCallback() {
                                                override fun onStartConnect() {
                                                    Log.e(TAG, "onStartConnect")
                                                }

                                                override fun onConnectFail(
                                                    bleDevice: BleDevice?,
                                                    exception: BleException?
                                                ) {
                                                    Log.e(
                                                        TAG,
                                                        "onConnectFail" + exception.toString()
                                                    )
                                                    deviceConfirmDialog.dismiss()
                                                    showToast(getString(R.string.connect_fail))
                                                    BleTool.mBleDevice = null
                                                }

                                                override fun onConnectSuccess(
                                                    bleDevice: BleDevice,
                                                    gatt: BluetoothGatt?,
                                                    status: Int
                                                ) {
                                                    Log.e(TAG, "onConnectSuccess")
                                                    setEquipment(bleDevice.mac)
                                                    deviceConfirmDialog.dismiss()
                                                    showToast("Connection succeeded")
                                                    BleTool.setBleDevice(bleDevice)
                                                    EventBus.getDefault()
                                                        .post(ConnectStatusEvent(1))

                                                    ThreadUtils.runOnUiThreadDelayed({
                                                        openBleNotify(bleDevice)
                                                    }, 100)
                                                }

                                                override fun onDisConnected(
                                                    isActiveDisConnected: Boolean,
                                                    device: BleDevice?,
                                                    gatt: BluetoothGatt?,
                                                    status: Int
                                                ) {
                                                    Log.e(TAG, "onDisConnected")
                                                    EventBus.getDefault()
                                                        .post(ConnectStatusEvent(0))
                                                    showToast(getString(R.string.device_disconnected))
                                                    if (!isActiveDisConnected) {
                                                        showDisconnectDialog()
                                                    }
                                                }
                                            })
                                    }

                                    tvDeviceTitle.text = titleSpanString
                                    tvDeviceContent.text = contentSpanString
                                }
                            })
                            .setFullScreen(true).setEnterAnimResId(R.anim.anim_custom_pop_enter)
                            .setExitAnimResId(R.anim.anim_custom_pop_exit)
                            .setAlignBaseViewGravity(Gravity.BOTTOM).setCancelable(false).show()
                    }

                } else {
                    if (!TextUtils.isEmpty(bleDevice.mac) && bondedDevice == bleDevice.mac) {
                        BleTool.setBleDevice(bleDevice)
                        BleManager.getInstance().cancelScan()
                        BleManager.getInstance()
                            .connect(bleDevice, object : BleGattCallback() {
                                override fun onStartConnect() {
                                    Log.e(TAG, "onStartConnect")
                                }

                                override fun onConnectFail(
                                    bleDevice: BleDevice?,
                                    exception: BleException?
                                ) {
                                    Log.e(
                                        TAG,
                                        "onConnectFail" + exception.toString()
                                    )
                                    showToast(getString(R.string.connect_fail))
                                    BleTool.mBleDevice = null
                                }

                                override fun onConnectSuccess(
                                    bleDevice: BleDevice,
                                    gatt: BluetoothGatt?,
                                    status: Int
                                ) {
                                    Log.e(TAG, "onConnectSuccess")
                                    setEquipment(bleDevice.mac)
                                    showToast("Connection succeeded")
                                    EventBus.getDefault()
                                        .post(ConnectStatusEvent(1))
                                    ThreadUtils.runOnUiThreadDelayed({
                                        openBleNotify(bleDevice)
                                    }, 100)
                                }

                                override fun onDisConnected(
                                    isActiveDisConnected: Boolean,
                                    device: BleDevice?,
                                    gatt: BluetoothGatt?,
                                    status: Int
                                ) {
                                    Log.e(TAG, "onDisConnected")
                                    EventBus.getDefault()
                                        .post(ConnectStatusEvent(0))
                                    showToast(getString(R.string.device_disconnected))
                                    showDisconnectDialog()
                                }
                            })
                    }
                }
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                if (scanResultList == null) {
                    /**附近没有匹配的蓝牙*/
                    showDisconnectDialog()
                } else {
                    if (scanResultList.size == 0) {
                        /**附近没有匹配的蓝牙*/
                        showDisconnectDialog()
                        return
                    }
                    /**附近有匹配的蓝牙*/
                    val bondedDevice = MMKV.defaultMMKV().decodeString(KVKey.equipment, "")
                    if (!TextUtils.isEmpty(bondedDevice)) {
                        /**在有绑定的情况下,绑定的蓝牙与附近蓝牙不匹配*/
                        if (BleTool.mBleDevice == null) {
                            showDisconnectDialog()
                        }
                    }

                }
            }


        })
    }

    //打开蓝牙通知
    private fun openBleNotify(mBleDevice: BleDevice) {
        //操作通知
        BleManager.getInstance().notify(mBleDevice,
            BleDefinedUUIDs.SERVICE.toString(),
            BleDefinedUUIDs.CHARACTERISTIC_NOTIFY.toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    LogUtils.eTag(TAG, "notify success")
                    //测试指令

                    thread {
                        while (true){
                            ThreadUtils.runOnUiThreadDelayed({
                                BleTool.sendInstruct("A5AAACFA05FFC5CCCA")
                            }, 100)
                            sleep(5000)
                        }
                    }
                    ThreadUtils.runOnUiThreadDelayed({
                        BleTool.sendInstruct("A5AAAC1706AABBCCDDEEFFC5CCCA")
                    }, 1000)
//                    test1()

                }

                override fun onNotifyFailure(exception: BleException?) {
                }

                @SuppressLint("SetTextI18n")
                override fun onCharacteristicChanged(data: ByteArray?) {
                    ViewUtils.runOnUiThread {
                        try {
                            LogUtils.eTag("$TAG Answer", ByteTransformUtil.bytesToHex(data).uppercase())
                            //03指令示例： A5AAAC00030400C5CCCA
                            val str = ByteTransformUtil.bytesToHex(data).uppercase()
                            if (str.contains("0241434B") || str.contains("024E41434B")) {
                                EventBus.getDefault().post(BleAnswerEvent("notify", str))
                            }
                            else if (str.substring(8, 10) == "03") {
                                //发送通知给对方 eg:A5AAAC00030304C5CCCA
                                //比如摩斯密码 （  –  –   – – – ）
                                //A5 AA AC 88 03 09 05 84 04 84 07 05 82 83 80 C5 CC CA
                                //其中：下划线部分代表摩斯密码与间隔时间。
                                //05   表示””，且与下一个摩斯密码间隔0.5s（计算方式为05h<128,所以短按）
                                //84   表示”–”，且与下一个摩斯密码间隔0.4s（84h>128,所以长按，时间为（84h-128）*0.1=0.4s）
                                //A5AAAC8003 08010B018203010000C5CCCA
                                LogUtils.e("接收到的信息为:$str")
                                sendMorseCode(str)
                            }
                            else if (str.substring(8, 10) == "05") {
                              val batteryType: BATTERY
                                val type =  str.substring(12,14).toInt(16)
                                batteryType = when (type) {
                                    0 -> {
                                        BATTERY.unCharging;
                                    }
                                    1 -> {
                                        BATTERY.charging;
                                    }
                                    else -> {
                                        BATTERY.fullBattery
                                    }
                                }
                                EventBus.getDefault()
                                    .post(BatteryBean(str.substring(10, 12).toInt(16).toString(),batteryType))
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                        }

                    }
                }
            })
    }

    private fun sendMorseCode(instructions: String) {
        RetrofitManager.service.sendMorseCode(
            hashMapOf(
                "morsecode" to instructions, "infotype" to PushInfoType.Bracelet
            )
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<SendMorseCodeBean?>(this, false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: SendMorseCodeBean?) {
                    if (t!!.code == 200) {
                        EventBus.getDefault().post(GetHistoryEvent()) //刷新历史记录
                        //告知硬件对方开始显示
                        val xorStr = BleTool.getXOR("0401")
                        BleTool.sendInstruct("A5AAAC" + xorStr + "0401C5CCCA")
                    }
                }
            })
    }

    private fun setEquipment(name: String) {
        RetrofitManager.service.setEquipment(hashMapOf("equipment" to name))
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    if (t!!.code == 200) {
                    }
                }
            })
    }

    override fun initListener() {
        //设置图标点击缩放
        ClickUtils.applyPressedViewScale(
            arrayOf(LL_tab_1, LL_tab_2, LL_tab_3), floatArrayOf(-0.2f, -0.2f, -0.2f)
        )

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

    /**
     * 检查是否有需要审核好友请求
     * */
    private fun getPartnerRequest() {
        RetrofitManager.service.getPartnerRequest(hashMapOf()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<PartnerAddBean?>(this, false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: PartnerAddBean?) {
                    t?.let {
                        if (t.code == 200) {
                            showToast(t.msg)
                            CustomDialog.build().setMaskColor(getColor(R.color.dialogMaskColor))
                                .setCustomView(object :
                                    OnBindView<CustomDialog>(R.layout.dialog_partner_add) {
                                    override fun onBind(dialog: CustomDialog?, v: View?) {
                                        v!!.let {
                                            val refuse = v.findViewById<TextView>(R.id.tv_cancel)
                                            val agree = v.findViewById<TextView>(R.id.tv_confirm)
                                            val img = v.findViewById<ImageView>(R.id.iv_head)
                                            val name = v.findViewById<TextView>(R.id.tv_name)
                                            name.text = t.data.username
                                            Glide.with(this@MainActivity).load(t.data.avatar)
                                                .circleCrop().into(img)
                                            refuse.setOnClickListener {
                                                dialog!!.dismiss()
                                                confirmEmail("0")
                                            }
                                            agree.setOnClickListener {
                                                dialog!!.dismiss()
                                                confirmEmail("3", t.data.videopath)
                                            }
                                        }
                                    }
                                }).setCancelable(false).show()
                        }
                    }
                }
            })
    }

    private fun getSettings() {
        RetrofitManager.service.getsettings(hashMapOf()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<SettingBean?>(this, false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: SettingBean?) {
                    t?.let {
                        if (t.code == 200) {
                            if (t.data != null) {
                                MMKV.defaultMMKV().encode(
                                    KVKey.long_shake,
                                    if (t.data.long_shake == "0") "0.3" else t.data.long_shake
                                )
                                MMKV.defaultMMKV().encode(
                                    KVKey.short_shake,
                                    if (t.data.short_shake == "0") "0.1" else t.data.short_shake
                                )
                                MMKV.defaultMMKV().encode(KVKey.light_color, t.data.lightcolor)
                                MMKV.defaultMMKV().encode(
                                    KVKey.long_flash,
                                    if (t.data.long_light == "0") "0.3" else t.data.long_light
                                )
                                MMKV.defaultMMKV().encode(
                                    KVKey.short_flash,
                                    if (t.data.short_light == "0") "0.1" else t.data.short_light
                                )
                                MMKV.defaultMMKV().encode(KVKey.equipment, t.data.equipment)
                                if (!BleManager.getInstance().isConnected(BleTool.mBleDevice)) {
                                    if (isAndroid12()) {
                                        //检查是否有BLUETOOTH_CONNECT权限
                                        if (hasPermission(
                                                this@MainActivity,
                                                Manifest.permission.BLUETOOTH_CONNECT
                                            )
                                        ) {
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
                }
            })
    }

    private fun getPartnerInfo() {
        RetrofitManager.service.getPartnerInfo(hashMapOf()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<PartnerInfoBean?>(this, false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: PartnerInfoBean?) {
                    t?.let {
                        if (t.code == 200) {
                            MMKV.defaultMMKV().encode(KVKey.partner_avatar, t.data.avatar)
                            MMKV.defaultMMKV().encode(KVKey.partner_nickname, t.data.nickname)
                            EventBus.getDefault().post(UpdateHeadPicEvent())
                        }
                    }
                }
            })
    }

    private fun getUserInfo() {
        RetrofitManager.service.getUserInfo(hashMapOf()).subscribeOn(Schedulers.io())
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
                            if (t.data != null) {
                                setUserInfoToSp(t.data.userInfo)
                                if (t.data.userInfo.partnerstatus == "5") {
                                    CustomDialog.build()
                                        .setMaskColor(getColor(R.color.dialogMaskColor))
                                        .setCustomView(object :
                                            OnBindView<CustomDialog>(R.layout.dialog_tips) {
                                            override fun onBind(dialog: CustomDialog?, v: View?) {
                                                v!!.let {
                                                    val tip = v.findViewById<TextView>(R.id.tv_tip)
                                                    tip.text =
                                                        "The other party wants to unbind you!"
                                                    val confirm =
                                                        v.findViewById<TextView>(R.id.tv_confirm)
                                                    val cancel =
                                                        v.findViewById<TextView>(R.id.tv_cancel)
                                                    cancel.setOnClickListener {
                                                        dialog!!.dismiss()
                                                    }
                                                    confirm.setOnClickListener {
                                                        dialog!!.dismiss()
                                                        confirmDelPartner()
                                                    }
                                                }
                                            }
                                        }).setCancelable(false).show()
                                } else {
                                    //获取用户设置
                                    getSettings()
                                    if (t.data.userInfo.partnerstatus.toInt() >= 3) {
                                        getPartnerInfo()
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun confirmEmail(status: String, videoUrl: String = "") {
        RetrofitManager.service.confirmEmail(hashMapOf("status" to status))
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        showToast(t.msg)
                        if (status == "3") {
                            getUserInfo()
                            //播放情侣视频
                            showVideoDialog(videoUrl)
                        }
                    }
                }
            })
    }

    private fun showVideoDialog(url: String) {
        CustomDialog.build().setMaskColor(getColor(R.color.dialogMaskColor))
            .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_play_video) {
                override fun onBind(dialog: CustomDialog?, v: View?) {
                    v!!.let {
                        val jz = v.findViewById<JzvdStd>(R.id.jz_video)
                        val close = v.findViewById<ImageView>(R.id.iv_close)
                        if (url != "") {
                            jz.fullscreenButton.visibility = View.GONE
                            jz.replayTextView.text = "To RePlay"
                            jz.setUp(url, "")
                            jz.startVideoAfterPreloading()
                        }
                        close.setOnClickListener {
                            dialog!!.dismiss()
                        }
                    }
                }
            }).setDialogLifecycleCallback(object : DialogLifecycleCallback<CustomDialog>() {
                override fun onDismiss(dialog: CustomDialog?) {
                    super.onDismiss(dialog)
                    mTab1 = HomeFragment()
                    replaceFragment(mTab1 as HomeFragment)
                }
            }).show()
    }

    private var exitTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event!!.action == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                if (WaitDialog.getInstance().isShow) {
                    WaitDialog.dismiss()
                } else {
                    showToast("Press again to exit")
                    exitTime = System.currentTimeMillis()
                }
            } else {
                AppUtils.exitApp()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }

    /***
     * 删除情侣
     */
    private fun confirmDelPartner() {
        RetrofitManager.service.deletePartnerConfirm(hashMapOf()).subscribeOn(Schedulers.io())
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


    private fun readMsg(id: String) {
        RetrofitManager.service.readMsg(hashMapOf("sendno" to id)).subscribeOn(Schedulers.io())
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

    private fun showDisconnectDialog() {
        CustomDialog.build().setMaskColor(getColor(R.color.dialogMaskColor))
            .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_device_disconnect) {
                override fun onBind(dialog: CustomDialog?, v: View?) {
                    v!!.let {
                        val confirm = v.findViewById<TextView>(R.id.tv_confirm)
                        confirm.setOnClickListener {
                            if (isAndroid12()) {
                                //检查是否有BLUETOOTH_CONNECT权限
                                if (hasPermission(
                                        this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT
                                    )
                                ) {
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
                            dialog!!.dismiss()
                        }
                    }
                }
            }).setCancelable(false).show()
    }

    //切换不同的fragment
    private fun replaceFragment(fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = manager.beginTransaction()
        fragmentTransaction.replace(R.id.view_stub_main, fragment)
        fragmentTransaction.commit()
    }

    /**初始化viewModel 做为 观察者*/
    override fun start() {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)


    }

    interface OnDeviceItemClicked {
        fun onItemClicked(bleDevice: BleDevice)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    inner class DeviceItemAdapter(private val clickListener: OnDeviceItemClicked) :
        RecyclerView.Adapter<DeviceItemAdapter.ViewHolder>() {
        private val deviceList = ArrayList<BleDevice>()

        public fun addDevice(device: BleDevice) {
            deviceList.add(device)
            notifyItemInserted(deviceList.size - 1)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val deviceName: TextView = itemView.findViewById<TextView>(R.id.tv_name)
            val deviceMac: TextView = itemView.findViewById<TextView>(R.id.tv_mac)
            val deviceConnect: TextView = itemView.findViewById<TextView>(R.id.tv_connect)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return deviceList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val bleDevice = deviceList[position]
            holder.deviceMac.text =
                if (!TextUtils.isEmpty(bleDevice.mac)) bleDevice.mac else "unKnow mac"
            holder.deviceName.text =
                if (!TextUtils.isEmpty(bleDevice.name)) bleDevice.name else "unKnow name"
            holder.deviceConnect.setOnClickListener {
                clickListener.onItemClicked(bleDevice)
            }

        }

    }


}
