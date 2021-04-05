package com.example.spy.device.sim;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.EasyLocationMod;
import github.nisrulz.easydeviceinfo.base.EasySimMod;

public class SimState {
    private Map<String, String> info = new HashMap<>();

    SimState(Context context) {
        EasySimMod easySimMod = new EasySimMod(context);
        try {
            info.put("IMSI", easySimMod.getIMSI());
            info.put("SERIAL", easySimMod.getSIMSerial());
            info.put("INFO", String.valueOf(easySimMod.getActiveMultiSimInfo()));
            info.put("IS_MULTI_SIM", String.valueOf(easySimMod.isMultiSim()));
            info.put("NUMBER_OF_ACTIVE", String.valueOf(easySimMod.getNumberOfActiveSim()));
        } catch (SecurityException e) {
            info.put("IMSI", "Undefined");
            info.put("SERIAL", "Undefined");
            info.put("INFO", "Undefined");
            info.put("IS_MULTI_SIM", "Undefined");
            info.put("NUMBER_OF_ACTIVE", "Undefined");
        }
        info.put("COUNTRY", easySimMod.getCountry());
        info.put("CARRIER", easySimMod.getCarrier());
        info.put("IS_LOCKED", String.valueOf(easySimMod.isSimNetworkLocked()));
    }

    public Map<String, String> getData() {
        return info;
    }
}
