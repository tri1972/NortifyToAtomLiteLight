package com.example.nortifytoatomlitelight.BleHelper;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.util.Log;

import com.example.nortifytoatomlitelight.SecondFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class BleScanCallback  extends ScanCallback {
    private Set<ScanResult> mResults = new HashSet<>();
    private List<ScanResult> mBatchScanResults = new ArrayList<>();

    IBleHelper  mParent;

    public BleScanCallback(IBleHelper  parent){
        mParent=parent;
    }
    /**
     * 内部データ取得用のgetter
     * @return
     */
    public Set<ScanResult> getmResults(){
        return  mResults;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
            mResults.add(result);
            mParent.AccessBleScanCallback(result);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        // In case onBatchScanResults are called due to buffer full, we want to collect all
        // scan results.
        mBatchScanResults.addAll(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        //super.onScanFailed(errorCode);
        Log.d(TAG, "onScanFailed: ");
    }
}
