package com.example.spy.device.fingerprint;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.EasyFingerprintMod;
import github.nisrulz.easydeviceinfo.base.EasyLocationMod;

public class FingerprintState {
    private Map<String, String> info = new HashMap<>();

    public FingerprintState(Context context) {
        EasyFingerprintMod easyFingerprintMod = new EasyFingerprintMod(context);
        try {
            info.put("IS_SENSOR_PRESENT", String.valueOf(easyFingerprintMod.isFingerprintSensorPresent()));
            info.put("IS_ENROLLED", String.valueOf(easyFingerprintMod.areFingerprintsEnrolled()));
        } catch (SecurityException e) {
            info.put("IS_SENSOR_PRESENT", "Undefined");
            info.put("IS_ENROLLED", "Undefined");
        }
    }

    public Map<String, String> getData() {
        return info;
    }
}
