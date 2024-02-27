package com.aroncent.onesignal

import android.content.Context
import android.util.Log
import com.aroncent.ble.BleTool
import com.aroncent.ble.DeviceConfig
import com.aroncent.event.ReadMsgEvent
import com.aroncent.jpush.PushInfoType
import com.aroncent.jpush.PushMsgBean
import com.aroncent.utils.addZeroForNum
import com.aroncent.utils.getLongPressHex
import com.aroncent.utils.getShortPressHex
import com.aroncent.utils.toBinary
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal
import org.greenrobot.eventbus.EventBus

@SuppressWarnings("unused")
class MyNotificationHandler :OneSignal.OSRemoteNotificationReceivedHandler {
    private val TAG = "MsgReceiveOneSignal"

    override fun remoteNotificationReceived(context: Context?, notificationReceivedEvent: OSNotificationReceivedEvent?) {
        val notification = notificationReceivedEvent!!.notification

        val data = notification.additionalData

        Log.e(TAG, "收到来自Onesignal的通知")
//        Log.e("MsgReceivedOnesignal", "notificationReceivedEvent："+notificationReceivedEvent.notification.toJSONObject().toString())
        Log.e(TAG, "Received Notification Data: $data")

        val msgData = GsonUtils.fromJson(data.toString(), PushMsgBean::class.java)
        //这里有两种类型的指令，01和03,03指令的需要转换成01的指令
        when (msgData.key.infotype){
            PushInfoType.Bracelet -> {
                val str = msgData.key.morsecode
                //示例： A5 AA AC 00 03 04 00 C5 CC CA  收到的03指令需要转01指令
                //                             12                str.length-6
                //A5AAAC8D   03        0B      0104810101010102020100    C5CCCA
                //          03指令    length         content
                val length =
                    addZeroForNum((str.substring(10, 12).toInt(16)).toString().uppercase(), 2)
                val content = str.substring(12, str.length - 6)/*这里需要额外建立一个记录时间的数组*/
                var morseData = ""
                val morseDelay = arrayOfNulls<String>(length.toInt())
                BleTool.getInstructStringArray(content).forEachIndexed { index, it ->
                    if (it != null) {
                        val unKnowNum = it.toInt(16)
                        if (unKnowNum < 128) {
                            morseDelay[index] = it.toInt(16).toString()
                            morseData += '0'
                            LogUtils.e("短按间隔-$it-${it.toInt(16)}-${it.toInt(16) * 0.1}秒")
                        } else if (unKnowNum == 128) {
                            morseDelay[index] = 1.toString()
                            morseData += '1'
                            LogUtils.e("长按间隔-$it-${it.toInt(16)}-0.1秒")
                        } else {
                            morseDelay[index] = (unKnowNum - 128).toString()
                            morseData += '1'
                            LogUtils.e("长按间隔-$it-${it.toInt(16)}-${(unKnowNum - 128) * 0.1}秒")
                        }
                    }

                }
                //这里得到摩斯密码表示的长按和短按 eg: 010100
                Log.e("JPush morseData", morseData)
                //组装01指令的数据域
                var instructData = ""
                morseData.forEachIndexed { index,it->
                    instructData += if (it.toString() == "0") {
                        getShortPressHex(morseDelay[index])
                    } else {
                        getLongPressHex(morseDelay[index])
                    }
                }
                //帧数长度
                val frame_length =
                    addZeroForNum((instructData.length / 8).toString(16), 2).uppercase()
                val instruct =
                    "A5AAAC" + BleTool.getXOR("01$frame_length" + DeviceConfig.loop_number + instructData) + "01$frame_length" + DeviceConfig.loop_number + instructData + "C5CCCA"

                Log.e("JPush 03指令转成01指令：", instruct)
                Thread.sleep(2000)
                BleTool.sendInstruct(instruct)
            }

            PushInfoType.App->{
                val morseData = msgData.key.morsecode.replace(",","")
                //这里得到摩斯密码表示的长按和短按 eg: 010100
                Log.e("OPush morseData",morseData)

                //组装01指令的数据域
                var instructData = ""
                morseData.forEach {
                    instructData += if (it.toString()=="0"){
                        getShortPressHex(null)
                    }else{
                        getLongPressHex(null)
                    }
                }
                //帧数长度
                val frame_length = addZeroForNum((instructData.length/8).toString(16),2).uppercase()
                val instruct = "A5AAAC" +
                        BleTool.getXOR("01$frame_length" + DeviceConfig.loop_number + instructData) +
                        "01$frame_length" + DeviceConfig.loop_number + instructData + "C5CCCA"

                Log.e("OPush 摩斯短语转01指令：",instruct)
                Thread.sleep(2000)
                BleTool.sendInstruct(instruct)
            }
        }
        //标记消息已读
        EventBus.getDefault().post(ReadMsgEvent(msgData.key.infoid,msgData.key.morsecode))

        notificationReceivedEvent.complete(null)
    }

}