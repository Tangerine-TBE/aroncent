package com.xlitebt.ble

import java.util.*

/**
 * Created by quan on 2017/11/16.
 */
class BleDefinedUUIDs {
    object Service {
        val NORDIC_UART = UUID.fromString("00001820-0000-1000-8000-00805f9b34fb")
        val BatteryService = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    }

    object Characteristic {
        val CHARACTERISTIC = UUID.fromString("00002a20-0000-1000-8000-00805f9b34fb")
        val BatteryCHARACTERISTIC = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
    }
}