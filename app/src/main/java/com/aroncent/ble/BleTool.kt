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


object BleTool {
    var mBleDevice : BleDevice? = null

    fun setBleDevice(mBle : BleDevice){
        mBleDevice = mBle
    }

    val mBleWriteCallback = object :BleWriteCallback() {
        override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
            LogUtils.eTag("OnSuccessWhite", HexUtil.formatHexString(justWrite, true).uppercase())

        }

        override fun onWriteFailure(exception: BleException) {

        }
    }

    fun sendInstruct(instructions: String,callback: BleWriteCallback = mBleWriteCallback) {//发送指令
        if (BleManager.getInstance().isConnected(mBleDevice)){
            BleManager.getInstance().write(
                mBleDevice,
                BleDefinedUUIDs.SERVICE.toString(),
                BleDefinedUUIDs.CHARACTERISTIC_WRITE.toString(),
                HexUtil.hexStringToBytes(instructions), callback)
        }
    }

    /**
     * 计算指令的XOR,注：指令长度需要为偶数！！
     * */
    fun getXOR(instruct: String):String{
        val arr = arrayOfNulls<String>(instruct.length / 2)
        var str1 = ""
        for (i in instruct.indices) {
            str1 += instruct[i]
            if ((i + 1) % 2 == 0) {
                arr[i / 2] = str1
                str1 = ""
            }
        }
        var xorValue = 0
        arr.forEach {
            xorValue = xorValue xor it!!.toInt(16)
        }
        return xorValue.toString(16)
    }

}