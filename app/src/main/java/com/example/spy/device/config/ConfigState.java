package com.example.spy.device.config;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.EasyConfigMod;
import github.nisrulz.easydeviceinfo.base.EasyLocationMod;
import github.nisrulz.easydeviceinfo.base.RingerMode;

public class ConfigState {
    private Map<String, String> info = new HashMap<>();

    public ConfigState(Context context) {
        EasyConfigMod easyConfigMod = new EasyConfigMod(context);

        String deviceRingerMode;
        switch (easyConfigMod.getDeviceRingerMode()) {
            case RingerMode.NORMAL: deviceRingerMode ="NORMAL"; break;
            case RingerMode.SILENT: deviceRingerMode ="SILENT"; break;
            case RingerMode.VIBRATE: deviceRingerMode ="VIBRATE"; break;
            default: deviceRingerMode = "Unknown"; break;
        }

        info.put("RINGER_MODE", deviceRingerMode);
        info.put("HAS_SD", String.valueOf(easyConfigMod.hasSdCard()));
    }

    public Map<String, String> getData() {
        return info;
    }
}
