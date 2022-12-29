package com.aroncent.ble

import com.aroncent.utils.addZeroForNum
import com.aroncent.utils.binaryToHexString
import com.aroncent.utils.toBinary
import com.blankj.utilcode.util.LogUtils
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.xlitebt.ble.BleDefinedUUIDs


object BleTool {
    var mBleDevice : BleDevice? = null

    fun setBleDevice(mBle : BleDevice){
        mBleDevice = mBle
    }

    var name = "" //暂时没用上这个变量
    var bleKey = ""
    var mac = ""

    /**
     * 设置bleTool里面的 key跟name
     * */
    fun setBleKeyAndName(key: String, bleName: String,bleMac: String) {
        bleKey = key
        name = bleName
        mac = bleMac
    }

    var lightState = "1000" //灯光开1000  关0000
    var lockMode = "00" //手动01   自动00模式
    var lockState = "00" //解锁00  锁定01
    var findCarState = "00" //寻车01   停止寻车00
    var brakeState = "11" //刹车感应开11 关10
    var ledLight = "00000000" //LED亮度 00->FF
    var ledMode = "0000" //LED模式 00->03
    var fctMode = "00000000" //FCT模式
    var musicType = "0000" //报警音乐0,1,2

    val mBleWriteCallback = object :BleWriteCallback() {
        override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
            LogUtils.eTag("OnSuccessWhite", HexUtil.formatHexString(justWrite, true))

        }

        override fun onWriteFailure(exception: BleException) {

        }
    }
    fun setCurrentInstructions(instructions : String){
        //eg: 01234567 8403 3E01 0000 80 00
        bleKey = instructions.substring(0,8)
        lightState = toBinary(instructions.substring(8,9),4)
        lockMode = toBinary(instructions.substring(9,10),4).substring(0,2)
        lockState = toBinary(instructions.substring(9,10),4).substring(2,4)
        brakeState = toBinary(instructions.substring(10,12),4).substring(2,4)
        findCarState = toBinary(instructions.substring(10,12),4).substring(0,2)
        ledLight = toBinary(instructions.substring(12,14),8)
        ledMode = toBinary(instructions.substring(14,16),4)
        fctMode = toBinary(instructions.substring(16,18),8)
        musicType = toBinary(instructions.substring(21,22),4)
    }

    //构建发送的指令
    fun createInstructions() : String{
        return (bleKey + binaryToHexString(lightState)+
                binaryToHexString(lockMode + lockState)+
                addZeroForNum(binaryToHexString(findCarState + brakeState),2)+
                addZeroForNum(binaryToHexString(ledLight),2)+
                addZeroForNum(binaryToHexString(ledMode),2)+
                addZeroForNum(binaryToHexString(fctMode),2)+
                "00"+ //音频控制的字节位置
                addZeroForNum(binaryToHexString(musicType),2)).uppercase()
    }

    fun sendInstruct(mBleDevice: BleDevice,callback: BleWriteCallback = mBleWriteCallback) {//发送指令
        if (BleManager.getInstance().isConnected(mBleDevice) && bleKey.isNotEmpty()){
            BleManager.getInstance().write(
                mBleDevice,
                BleDefinedUUIDs.Service.NORDIC_UART.toString(),
                BleDefinedUUIDs.Characteristic.CHARACTERISTIC.toString(),
                HexUtil.hexStringToBytes(createInstructions()), callback)
        }
    }

    fun return2Factory(mBleDevice: BleDevice) {//发送指令
        if (BleManager.getInstance().isConnected(mBleDevice) && bleKey.isNotEmpty()){
            BleManager.getInstance().write(
                mBleDevice,
                BleDefinedUUIDs.Service.NORDIC_UART.toString(),
                BleDefinedUUIDs.Characteristic.CHARACTERISTIC.toString(),
                HexUtil.hexStringToBytes(createInstructions()),object :BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {}
                    override fun onWriteFailure(exception: BleException) {}
                })
        }
    }



}