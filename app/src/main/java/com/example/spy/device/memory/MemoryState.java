package com.example.spy.device.memory;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.easydeviceinfo.base.EasyLocationMod;
import github.nisrulz.easydeviceinfo.base.EasyMemoryMod;

public class MemoryState {
    private Map<String, String> info = new HashMap<>();

    public MemoryState(Context context) {
        EasyMemoryMod easyMemoryMod = new EasyMemoryMod(context);
        info.put("TOTAL_RAM_MB", String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getTotalRAM())));
        info.put("TOTAL_INTERNAL_MEMORY_MB", String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getTotalInternalMemorySize())));
        info.put("TOTAL_EXTERNAL_MEMORY_MB", String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getTotalExternalMemorySize())));
        info.put("AVAILABLE_INTERNAL_MEMORY_MB", String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getAvailableInternalMemorySize())));
        info.put("AVAILABLE_EXTERNAL_MEMORY_MB", String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getAvailableExternalMemorySize())));
    }

    public Map<String, String> getData() {
        return info;
    }
}
