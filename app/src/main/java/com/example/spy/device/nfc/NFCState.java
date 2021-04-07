package com.example.spy.device.nfc;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;
import github.nisrulz.easydeviceinfo.base.EasyNfcMod;
import github.nisrulz.easydeviceinfo.base.NetworkType;

public class NFCState {
    private Map<String, String> info = new HashMap<>();

    public NFCState(Context context) {
        EasyNfcMod easyNFCState = new EasyNfcMod(context);
        info.put("IS_PRESENT", String.valueOf(easyNFCState.isNfcPresent()));
        info.put("IS_ENABLED", String.valueOf(easyNFCState.isNfcEnabled()));
    }

    public Map<String, String> getData() {
        return info;
    }
}
