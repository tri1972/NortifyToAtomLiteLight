package com.example.nortifytoatomlitelight;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nortifytoatomlitelight.BleHelper.BleConnectionAtomLiteLight;
import com.example.nortifytoatomlitelight.BleHelper.IBleHelper;
import com.example.nortifytoatomlitelight.BleHelper.pairingBroadcastReceiverCallback;

import java.security.spec.ECField;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class SecondFragment extends Fragment implements IBleHelper {

    private static final int REQUEST_ENABLE_BT = 1;

    private TextView textview;
    private StringBuilder viewTextData;
    private ScrollView scrollView;
    private Handler handler = new Handler();
    private pairingBroadcastReceiverCallback receiver;
    private BluetoothGattCharacteristic characteristic;
    private BleConnectionAtomLiteLight bleAtomLite;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final IBleHelper parent=this;
        this.bleAtomLite=new BleConnectionAtomLiteLight();
        textview = view.findViewById(R.id.textView_Bluetooth_Log);
        scrollView = view.findViewById(R.id.ScrollView);

        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
        view.findViewById(R.id.button_BLE_Scan_Start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bleAtomLite.ScanLeDevice2(parent);
            }
        });

        view.findViewById(R.id.button_BLE_Scan_Stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAtomLite.ScanStop(v.getContext());
            }
        });
        view.findViewById(R.id.button_BLE_Connect_Start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAtomLite.ConnectStart(v.getContext(),parent);
            }
        });
        view.findViewById(R.id.button_BLE_Connect_Stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAtomLite.ConnectStop(v.getContext());
            }
        });
        view.findViewById(R.id.button_BLE_Pearling_Start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAtomLite.RequestPearing(v.getContext(),parent);
            }
        });
        view.findViewById(R.id.button_BLE_Calling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAtomLite.SendCalling(v.getContext());
            }
        });
        view.findViewById(R.id.button_BLE_Called).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAtomLite.SendCalled(v.getContext());
            }
        });
        view.findViewById(R.id.button_BLE_Attention).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAtomLite.SendAttention(v.getContext());
            }
        });

        this.bleAtomLite.IdentificationBLE(view.getContext());
        viewTextData = new StringBuilder();

        this.receiver=new pairingBroadcastReceiverCallback();
        this.pairingRequestIntentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        view.getContext().registerReceiver(this.receiver,this.pairingRequestIntentFilter);
    }

    private IntentFilter pairingRequestIntentFilter =new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST){
    };

    @Override
    public void onResume() {
        super.onResume();
        switch (this.bleAtomLite.IdentificationBLE(this.getContext())){
            case BluetoothAdapterDisable:
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                break;
            case BluetoothNotSupported:
                Toast.makeText(this.getContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * BleScaCallbackクラスよりScanデータが取得されれば呼ばれます
     *
     * @param data BleScanデータ
     */
    public void AccessBleScanCallback(ScanResult data) {
        int iLine, iPixel, iHeight, i;

        String tmp =
                "DeviceName:" + (data.getScanRecord().getDeviceName() != null ? data.getScanRecord().getDeviceName() : "null") + " "
                        + "TxPowerLevel:" + (data.getScanRecord().getTxPowerLevel()) + " "
                        + "Device:" + data.getDevice().toString() + " "
                        + "describeContents:" + data.describeContents();
        if (data.getScanRecord().getDeviceName() != null) {
            Log.d(TAG, "Scan Device Name: " + data.getScanRecord().getDeviceName());
        }
        this.viewTextData.append(tmp + "\r");

        //UIに値を設定するためUIスレッドに対してPostする
        handler.post(new Runnable() {
            @Override
            public void run() {
                textview.setText(viewTextData);
            }
        });

        iLine = this.textview.getLineCount();
        iLine /= 2;
        iPixel = this.textview.getLineHeight();
        iHeight = this.textview.getHeight();
        i = iLine * iPixel;
        i -= iHeight / 2;
        scrollView.smoothScrollBy(0, i);
    }

    @Override
    public void OnGetConnectionChangeNewStateAction(final Integer status) {
        final Context mContext=this.getContext();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext,
                        mContext.getString(R.string.message_Gatt_Connection_Status_Change)
                                + bleAtomLite.GetGattConnectionStatusToString(mContext,status),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public BluetoothGattCharacteristic GetCharacteristic() {
        return this.characteristic;
    }

    /**
     * gattServer側のServiceデータを取得すると実行されます
     * @param services
     */
    public  void OnGetService(List<BluetoothGattService> services){
        this.viewTextData.append("Get GattServices UUID \n");
        for(BluetoothGattService service:services){
            this.viewTextData.append( service.getUuid().toString()+"\n");
        }
        //UIに値を設定するためUIスレッドに対してPostする
        handler.post(new Runnable() {
            @Override
            public void run() {
                textview.setText(viewTextData);
            }
        });
    }

    /**
     * gattServer側のCharacteristicデータを取得すると実行されます
     * @param characteristics
     */
    public void OnGetCharacteristic(List<BluetoothGattCharacteristic> characteristics ){
        this.characteristic=characteristics.get(0);//このcharacteristicをBleConnectionAtomLiteLightクラスに持っていきステータス変動送信に使う
        this.viewTextData.append("Get GattCharacteristic UUID \n");
        for(BluetoothGattCharacteristic characteristic:characteristics){
            this.viewTextData.append( characteristic.getUuid().toString()+"\n");
        }
        //UIに値を設定するためUIスレッドに対してPostする
        handler.post(new Runnable() {
            @Override
            public void run() {
                textview.setText(viewTextData);
            }
        });
    }
}