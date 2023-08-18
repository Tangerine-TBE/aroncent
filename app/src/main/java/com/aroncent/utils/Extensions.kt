package com.aroncent.utils

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aroncent.app.KVKey
import com.aroncent.ble.DeviceConfig
import com.aroncent.module.login.UserinfoBean
import com.blankj.utilcode.util.NumberUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.onesignal.OneSignal
import com.tencent.mmkv.MMKV
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions


fun Fragment.showToast(content: String) {
    ToastUtils.make()
        .setDurationIsLong(false)
        .show(content)
}

private var toast: Toast? = null
fun Context.showToast(content: String) {
    ToastUtils.make()
        .setDurationIsLong(false)
        .show(content)
}
fun Context.showToastLong(content: String) {
    ToastUtils.make()
        .setGravity(Gravity.CENTER, 0, 0)
        .setDurationIsLong(true)
        .show(content)
}

fun getUserToken():String{
    return MMKV.defaultMMKV().decodeString(KVKey.TOKEN,"")
}

fun setUserInfoToSp(data : UserinfoBean){
    MMKV.defaultMMKV().encode(KVKey.TOKEN, data.token)
    MMKV.defaultMMKV().encode(KVKey.avatar,data.avatar)
    MMKV.defaultMMKV().encode(KVKey.username,data.username)
    MMKV.defaultMMKV().encode(KVKey.nickname,data.nickname)
    MMKV.defaultMMKV().encode(KVKey.partnerStatus,data.partnerstatus)
    MMKV.defaultMMKV().encode(KVKey.user_id,data.user_id)
    OneSignal.setExternalUserId(data.user_id.toString(),object : OneSignal.OSExternalUserIdUpdateCompletionHandler{
        override fun onSuccess(p0: JSONObject?) {
            Log.e("OneSignal","setExternalUserId success: ${p0?.toString()}")
        }

        override fun onFailure(p0: OneSignal.ExternalIdError?) {
            Log.e("OneSignal","setExternalUserId error: ${p0?.message}")
        }
    })
    if (data.partnerstatus.toInt() >= 3){
        MMKV.defaultMMKV().encode(KVKey.isBind,true)
    }else{
        MMKV.defaultMMKV().encode(KVKey.isBind,false)
    }
}

fun stringToMorseCode(str:String) :String{
    var morseStr = ""
    str.forEach {
        val char = if (it.toString() == "0") "• " else "▬ "
        morseStr += char
    }
    return morseStr
}

fun ImageView.setGlideResource(context: Context, resource: Int) {
    Glide.with(context).load(resource).into(this)
}
fun ImageView.setGlideResource(context: Context, url: String) {
    Glide.with(context).load(url).into(this)
}


fun Context.startActivity(className:Class<*>){
    startActivity(Intent(this,className))
}

fun View.dip2px(dipValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (dipValue * scale + 0.5f).toInt()
}

fun View.px2dip(pxValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}


fun isAndroid12() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
fun isAndroid11() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

fun hasPermission(context: Context,permission: String): Boolean {
    return EasyPermissions.hasPermissions(context, permission)
}
/*
*数字不足位数左补0
*
* @param str
* @param strLength
*/
fun addZeroForNum(string: String, strLength: Int): String {
    var str = string
    var strLen = str.length
    if (strLen < strLength) {
        while (strLen < strLength) {
            val sb = StringBuffer()
            sb.append("0").append(str) //左补0
            // sb.append(str).append("0");//右补0
            str = sb.toString()
            strLen = str.length
        }
    }
    return str
}

/**
 * 16进制转 length位数二进制
 * length :多少位
 * */
fun toBinary(num: String,length:Int) : String{
    return addZeroForNum(num.toInt(16).toString(2),length)
}

/**
 * 二进制转16进制
 * */
fun binaryToHexString(num: String):String{
    return addZeroForNum(num.toInt(2).toString(16),2)
}

/**
 * 获取短按的16进制字符（用于03指令转01指令）
 * */
fun getShortPressHex():String{
    when{
        //这个是短闪时长大于短震时长的情况
        DeviceConfig.short_flash.toFloat() - DeviceConfig.short_shake.toFloat() > 0 ->{
            //第一帧的时长为偏短的那个时长，所以用short_shake
            val first_frame = DeviceConfig.lightColor + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((DeviceConfig.short_shake.toFloat()*10).toInt().toString(16),5)),2)

            //第二帧的时长
            val second_frame_time = NumberUtils.format(DeviceConfig.short_flash.toFloat() - DeviceConfig.short_shake.toFloat(),1).toFloat()
            val second_frame = DeviceConfig.lightColor + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((second_frame_time*10).toInt().toString(16),5)),2)

            //第三帧，为间隔时间，默认0.3s
            val third_frame = "00000003"
            Log.e("ShortPressHex",first_frame+second_frame+third_frame)
            return (first_frame+second_frame+third_frame).uppercase()
        }

        //这个是短闪时长小于短震时长的情况
        DeviceConfig.short_flash.toFloat() - DeviceConfig.short_shake.toFloat() < 0 ->{
            //第一帧的时长为偏短的那个时长，short_flash
            val first_frame = DeviceConfig.lightColor + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((DeviceConfig.short_flash.toFloat()*10).toInt().toString(16),5)),2)

            //第二帧的时长
            val second_frame_time = NumberUtils.format(DeviceConfig.short_shake.toFloat() - DeviceConfig.short_flash.toFloat(),1).toFloat()
            val second_frame = "000000" + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((second_frame_time*10).toInt().toString(16),5)),2)

            //第三帧，为间隔时间，默认0.3s
            val third_frame = "00000003"
            Log.e("ShortPressHex",first_frame+second_frame+third_frame)
            return (first_frame+second_frame+third_frame).uppercase()
        }

        else->{//这个是短震跟短闪时长一样的情况

            //第一帧
            val first_frame = DeviceConfig.lightColor + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((DeviceConfig.short_flash.toFloat()*10).toInt().toString(16),5)),2)

            //第二帧，为间隔时间，默认0.3s
            val second_frame = "00000003"
            Log.e("ShortPressHex",first_frame+second_frame)
            return (first_frame+second_frame).uppercase()
        }
    }
}

/**
 * 获取长按的16进制字符（用于03指令转01指令）
 * */
fun getLongPressHex():String{
    when{
        //这个是长闪时长大于长震时长的情况
        DeviceConfig.long_flash.toFloat() - DeviceConfig.long_shake.toFloat() > 0 ->{
            //第一帧的时长为偏短的那个时长，所以用short_shake
            val first_frame = DeviceConfig.lightColor + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((DeviceConfig.long_shake.toFloat()*10).toInt().toString(16),5)),2)

            //第二帧的时长
            val second_frame_time = NumberUtils.format(DeviceConfig.long_flash.toFloat() - DeviceConfig.long_shake.toFloat(),1).toFloat()
            val second_frame = DeviceConfig.lightColor + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((second_frame_time*10).toInt().toString(16),5)),2)

            //第三帧，为间隔时间，默认0.3s
            val third_frame = "00000003"
            Log.e("LongPressHex",first_frame+second_frame+third_frame)
            return (first_frame+second_frame+third_frame).uppercase()
        }

        //这个是长闪时长小于长震时长的情况
        DeviceConfig.long_flash.toFloat() - DeviceConfig.long_shake.toFloat() < 0 ->{
            //第一帧的时长为偏短的那个时长，short_flash
            val first_frame = DeviceConfig.lightColor + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((DeviceConfig.long_flash.toFloat()*10).toInt().toString(16),5)),2)

            //第二帧的时长
            val second_frame_time = NumberUtils.format(DeviceConfig.long_shake.toFloat() - DeviceConfig.long_flash.toFloat(),1).toFloat()
            val second_frame = "000000" + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((second_frame_time*10).toInt().toString(16),5)),2)

            //第三帧，为间隔时间，默认0.3s
            val third_frame = "00000003"
            Log.e("LongPressHex",first_frame+second_frame+third_frame)
            return (first_frame+second_frame+third_frame).uppercase()
        }

        else->{//这个是长震跟长闪时长一样的情况

            //第一帧
            val first_frame = DeviceConfig.lightColor + addZeroForNum(binaryToHexString(toBinary(DeviceConfig.shaking_levels,3)
                    + toBinary((DeviceConfig.long_flash.toFloat()*10).toInt().toString(16),5)),2)

            //第二帧，为间隔时间，默认0.3s
            val second_frame = "00000003"
            Log.e("LongPressHex",first_frame+second_frame)
            return (first_frame+second_frame).uppercase()
        }
    }
}

/**
 * 手机是否开启位置服务，如果没有开启那么所有app将不能使用定位功能
 */
fun isLocServiceEnable(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    return gps || network
}
















