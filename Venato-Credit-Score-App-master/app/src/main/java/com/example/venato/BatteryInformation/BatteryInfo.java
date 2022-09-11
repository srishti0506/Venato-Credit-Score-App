package com.example.venato.BatteryInformation;

import static android.content.Context.BATTERY_SERVICE;

import android.content.Context;
import android.os.BatteryManager;

public class BatteryInfo {

    Context context;
    public BatteryInfo(Context _context){
        context=_context;
    }

    public double calBattery(){

        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return (double)batLevel;
    }
}
