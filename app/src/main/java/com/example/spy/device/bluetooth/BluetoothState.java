package com.example.spy.device.bluetooth;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.BatteryHealth;
import github.nisrulz.easydeviceinfo.base.ChargingVia;
import github.nisrulz.easydeviceinfo.base.EasyBatteryMod;
import github.nisrulz.easydeviceinfo.base.EasyBluetoothMod;

public class BluetoothState {
    private Map<String, String> info = new HashMap<>();

    public BluetoothState(Context context, String bluetoothName) {
        EasyBluetoothMod easyBluetoothState = new EasyBluetoothMod(context);
        try {
            info.put("MAC", easyBluetoothState.getBluetoothMAC());
            info.put("DISPLAYED_NAME", bluetoothName);
        } catch (Exception e) {

        }
    }

    public Map<String, String> getData() {
        return info;
    }
}
