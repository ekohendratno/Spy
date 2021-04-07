package com.example.spy.device.network;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.EasyLocationMod;
import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;
import github.nisrulz.easydeviceinfo.base.NetworkType;

public class NetworkState {
    private Map<String, String> info = new HashMap<>();

    public NetworkState(Context context) {
        EasyNetworkMod easyNetworkState = new EasyNetworkMod(context);

        String networktype;
        switch(easyNetworkState.getNetworkType()) {
            case NetworkType.CELLULAR_2G : networktype ="2G"; break;
            case NetworkType.CELLULAR_3G : networktype ="3G"; break;
            case NetworkType.CELLULAR_4G : networktype ="4G"; break;
            case NetworkType.CELLULAR_UNIDENTIFIED_GEN : networktype ="Undefined"; break;
            case NetworkType.CELLULAR_UNKNOWN : networktype ="Unknown"; break;
            case NetworkType.WIFI_WIFIMAX : networktype ="WIFI Max"; break;
            default: networktype ="Unknown"; break;
        }


        info.put("IPV4_ADDRESS", easyNetworkState.getIPv4Address());
        info.put("IPV6_ADDRESS", easyNetworkState.getIPv6Address());
        info.put("WIFI_AVAILABLE", String.valueOf(easyNetworkState.isNetworkAvailable()));
        info.put("WIFI_ENABLED", String.valueOf(easyNetworkState.isWifiEnabled()));
        info.put("WIFI_SSID", easyNetworkState.getWifiSSID());
        info.put("WIFI_BSSID", easyNetworkState.getWifiBSSID());
        info.put("WIFI_SPEAD", easyNetworkState.getWifiLinkSpeed());
        info.put("WIFI_MAC", easyNetworkState.getWifiMAC());
        info.put("NETWORK_TYPE", networktype);
    }

    public Map<String, String> getData() {
        return info;
    }
}
