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
            return MMKV.defaultMMKV().decodeString(KVKey.long_shake,"0.0")
        }

    @JvmStatic
    val short_shake : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.short_shake,"0.0")
        }

    @JvmStatic
    val long_flash : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.long_flash,"0.0")
        }

    @JvmStatic
    val short_flash : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.short_flash,"0.0")
        }

    @JvmStatic
    val shaking_levels : String
        get() {
            return MMKV.defaultMMKV().decodeString(KVKey.shaking_levels,"0")
        }
}