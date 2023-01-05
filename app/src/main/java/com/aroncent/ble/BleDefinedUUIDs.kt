package com.aroncent.ble

import java.util.*

/**
 * Created by quan on 2017/11/16.
 */
object BleDefinedUUIDs {
        val SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_NOTIFY = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_WRITE = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
}