package com.aroncent.jpush

import android.content.Context
import android.content.Intent
import android.util.Log
import cn.jpush.android.api.CmdMessage
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.api.JPushMessage
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver
import com.aroncent.ble.BleTool
import com.aroncent.ble.DeviceConfig
import com.aroncent.event.ReadMsgEvent
import com.aroncent.utils.*
import com.blankj.utilcode.util.GsonUtils
import org.greenrobot.eventbus.EventBus

class PushMessageReceiver : JPushMessageReceiver() {
    private val TAG = "PushMessageReceiver"

    override fun onMessage(context: Context, customMessage: CustomMessage) {
        Log.e(TAG, "[onMessage] $customMessage")
        val intent = Intent("com.jiguang.demo.message")
        intent.putExtra("msg", customMessage.message)
        context.sendBroadcast(intent)
        val msgData = GsonUtils.fromJson(customMessage.extra,PushMsgBean::class.java)
        //这里有两种类型的指令，01和03,03指令的需要转换成01的指令
        when (msgData.key.infotype){
               PushInfoType.Bracelet->{
                   val str = msgData.key.morsecode
                   //示例： A5 AA AC 00 03 04 00 C5 CC CA  收到的03指令需要转01指令
                   val length = addZeroForNum((str.substring(10,12).toInt(16)*3).toString(16).uppercase(),2)
                   val content = str.substring(12,str.length-6)
                   var morseData = ""
                   BleTool.getInstructStringArray(content).forEach {
                       val char = toBinary(it!!,8).reversed()
                       morseData += char
                   }

                   morseData = morseData.substring(0,length.toInt(16))
                   //这里得到摩斯密码表示的长按和短按 eg: 010100
                   Log.e("morseData",morseData.substring(0,length.toInt(16)))

                   //组装01指令的数据域
                   var instructData = ""
                   morseData.forEach {
                       instructData += if (it.toString()=="0"){
                           getShortPressHex()
                       }else{
                           getLongPressHex()
                       }
                   }
                   //帧数长度
                   val frame_length = addZeroForNum((instructData.length/8).toString(16),2).uppercase()
                   val instruct = "A5AAAC" +
                           BleTool.getXOR("01$frame_length" + DeviceConfig.loop_number + instructData) +
                           "01$frame_length" + DeviceConfig.loop_number + instructData + "C5CCCA"

                   Log.e("03指令转成01指令：",instruct)
                   BleTool.sendInstruct(instruct)
               }
            PushInfoType.App->{
                val morseData = msgData.key.morsecode.replace(",","")
                //这里得到摩斯密码表示的长按和短按 eg: 010100
                Log.e("morseData",morseData)

                //组装01指令的数据域
                var instructData = ""
                morseData.forEach {
                    instructData += if (it.toString()=="0"){
                        getShortPressHex()
                    }else{
                        getLongPressHex()
                    }
                }
                //帧数长度
                val frame_length = addZeroForNum((instructData.length/8).toString(16),2).uppercase()
                val instruct = "A5AAAC" +
                        BleTool.getXOR("01$frame_length" + DeviceConfig.loop_number + instructData) +
                        "01$frame_length" + DeviceConfig.loop_number + instructData + "C5CCCA"

                Log.e("摩斯短语转01指令：",instruct)
                BleTool.sendInstruct(instruct)
            }
        }
        //标记消息已读
        EventBus.getDefault().post(ReadMsgEvent(msgData.key.infoid))
    }
    override fun onNotifyMessageOpened(context: Context, message: NotificationMessage) {
        Log.e(TAG, "[onNotifyMessageOpened] $message")

    }

    override fun onNotifyMessageArrived(context: Context?, message: NotificationMessage) {
        Log.e(TAG, "[onNotifyMessageArrived] $message")
//        示例： A5 AA AC F4 01 01 01 FF FF FF 0A C5 CC CA
//        BleTool.sendInstruct(message.notificationContent)
    }

    override fun onNotifyMessageDismiss(context: Context?, message: NotificationMessage) {
        Log.e(TAG, "[onNotifyMessageDismiss] $message")
    }

    override fun onRegister(context: Context, registrationId: String) {
        Log.e(TAG, "[onRegister] $registrationId")
        val intent = Intent("com.jiguang.demo.register")
        context.sendBroadcast(intent)
    }

    override fun onConnected(context: Context?, isConnected: Boolean) {
        Log.e(TAG, "[onConnected] $isConnected")
    }

    override fun onCommandResult(context: Context?, cmdMessage: CmdMessage) {
        Log.e(TAG, "[onCommandResult] $cmdMessage")
    }

    override fun onTagOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
        super.onTagOperatorResult(context, jPushMessage)
    }

    override fun onCheckTagOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
        super.onCheckTagOperatorResult(context, jPushMessage)
    }

    override fun onAliasOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
        super.onAliasOperatorResult(context, jPushMessage)
    }

    override fun onMobileNumberOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
        super.onMobileNumberOperatorResult(context, jPushMessage)
    }

    override fun onNotificationSettingsCheck(context: Context?, isOn: Boolean, source: Int) {
        super.onNotificationSettingsCheck(context, isOn, source)
        Log.e(TAG, "[onNotificationSettingsCheck] isOn:$isOn,source:$source")
    }
}