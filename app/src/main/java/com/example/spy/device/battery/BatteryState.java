package com.example.spy.device.battery;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.BatteryHealth;
import github.nisrulz.easydeviceinfo.base.ChargingVia;
import github.nisrulz.easydeviceinfo.base.EasyBatteryMod;

public class BatteryState {
    private Map<String, String> info = new HashMap<>();

    BatteryState(Context context) {
        String batteryHealth;
        EasyBatteryMod easyBatteryMod = new EasyBatteryMod(context);
        switch (easyBatteryMod.getBatteryHealth()) {
            case BatteryHealth.GOOD:
                batteryHealth = "Good";
                break;
            case BatteryHealth.HAVING_ISSUES:
                batteryHealth = "Having issues";
                break;
            default:
                batteryHealth = "Unknown";
                break;
        }

        String chargingSource = "Not charge";
        if(easyBatteryMod.isDeviceCharging()){
            switch (easyBatteryMod.getBatteryHealth()) {
                case ChargingVia.AC:
                    chargingSource = "AC";
                    break;
                case ChargingVia.UNKNOWN_SOURCE:
                    chargingSource = "Unknown";
                    break;
                case ChargingVia.USB:
                    chargingSource = "USB";
                    break;
                case ChargingVia.WIRELESS:
                    chargingSource = "Wireless";
                    break;
                default:
                    chargingSource = "Unknown";
                    break;
            }
        }

        info.put("PERCENTAGE", String.valueOf(easyBatteryMod.getBatteryPercentage()));
        info.put("TEMPERATURE_IN_C", String.valueOf(easyBatteryMod.getBatteryTemperature()));
        info.put("VOLTAGE_IN_mV", String.valueOf(easyBatteryMod.getBatteryVoltage()));
        info.put("TECHNOLOGY", easyBatteryMod.getBatteryTechnology());
        info.put("IS_CHARGING", String.valueOf(easyBatteryMod.isDeviceCharging()));
        info.put("CHARGING_SOURCE", chargingSource);
        info.put("HEALTH", batteryHealth);
    }

    public Map<String, String> getData() {
        return info;
    }
}
