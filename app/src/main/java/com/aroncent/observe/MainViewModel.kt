package com.aroncent.observe

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clj.fastble.data.BleDevice

class MainViewModel :ViewModel() {
    var device = MutableLiveData<BleDevice>(null)

}