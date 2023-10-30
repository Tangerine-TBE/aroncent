package com.aroncent.ble

import com.aroncent.app.KVKey
import com.tencent.mmkv.MMKV

object DeviceConfig {
    @JvmStatic
    val lightColor : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.light_color,"FFFFFF")
        }

    @JvmStatic
    val long_shake : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.long_shake,"0.3")
        }

    @JvmStatic
    val short_shake : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.short_shake,"0.1")
        }

    @JvmStatic
    val long_flash : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.long_flash,"0.3")
        }

    @JvmStatic
    val short_flash : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.short_flash,"0.1")
        }

    @JvmStatic
    val shaking_levels : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.shaking_levels,"1")
        }

    @JvmStatic
    val loop_number : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.loop_number,"01")
        }

    @JvmStatic
    val interval_time : String
        get() {
            return "0.3"
        }
}