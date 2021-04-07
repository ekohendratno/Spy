package com.example.spy.device.location;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.EasyBluetoothMod;
import github.nisrulz.easydeviceinfo.base.EasyLocationMod;

public class LocationState {
    private Map<String, String> info = new HashMap<>();

    public LocationState(Context context) {
        EasyLocationMod easyLocationMod = new EasyLocationMod(context);
        try {
            double[] l = easyLocationMod.getLatLong();

            info.put("LATITUDE", String.valueOf(l[0]));
            info.put("LONGITUDE", String.valueOf(l[1]));
        } catch (SecurityException e) {
            info.put("LATITUDE", String.valueOf(-1));
            info.put("LONGITUDE", String.valueOf(-1));
        }
    }

    public Map<String, String> getData() {
        return info;
    }
}
