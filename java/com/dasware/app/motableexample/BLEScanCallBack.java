package com.dasware.app.motableexample;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import java.util.ArrayList;

/**
 * Created by SamuelPS on 18/05/2017.
 */

public class BLEScanCallBack extends ScanCallback {

    ScanListAdapter myadapter;
    public static ArrayList<String> mydevicesMAC= new ArrayList<>();

    public BLEScanCallBack(ScanListAdapter adapter){
        this.myadapter=adapter;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        if(!mydevicesMAC.contains(result.getDevice().getAddress())){
            myadapter.add(result.getDevice());
            mydevicesMAC.add(result.getDevice().getAddress());
        }


    }



}
