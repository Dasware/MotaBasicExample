package com.dasware.app.motableexample;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanListFragment extends Fragment {


    ScanListAdapter myadapter;
    BluetoothAdapter myBLEAdapter;
    BLEScanCallBack stopcallback;

    BroadcastReceiver myBr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("BackPressed")) {
                OnBackPressed();
            }
        }
    };

    BluetoothAdapter.LeScanCallback mLeScanCallBack = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                myadapter.add(device);
        }
    };

    public ScanListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getContext().registerReceiver(myBr, new IntentFilter("BackPressed"));

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        myBLEAdapter = bluetoothManager.getAdapter();


        ListView mylist = (ListView) view.findViewById(R.id.scan_results);
        myadapter = new ScanListAdapter(getContext());
        mylist.setAdapter(myadapter);

        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              changeFragment(myadapter.getItem(position).getAddress());
            }
        });

        startScan();

    }

    public void OnBackPressed() {
        getContext().unregisterReceiver(myBr);
        stopScan();
        getActivity().finish();
    }

    public void startScan() {

         // SCANNER PARA MARSHMALLOW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // ScanCallback
            final BLEScanCallBack scanCallbackM = new BLEScanCallBack(myadapter);

            // ScanFilters
            ScanFilter.Builder scanFilterBuilderM = new ScanFilter.Builder();

            // List of ScanFilters
            List<ScanFilter> filtersM = new ArrayList<>();

            // ScanSettings builder
            ScanSettings.Builder scanSettingsBuilderM = new ScanSettings.Builder();
            ScanFilter scanFilterM = scanFilterBuilderM.build();
            filtersM.add(scanFilterM);

            ScanSettings scanSettingsM = scanSettingsBuilderM
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .setReportDelay(0)
                    .setMatchMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                    .build();

            Log.i("startScanBleDevices", "sdk M");
            myBLEAdapter.getBluetoothLeScanner().startScan(filtersM, scanSettingsM, scanCallbackM);
            stopcallback = scanCallbackM;
        }
        // SCANNER PARA LOLLIPOP
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Log.i("BluetoothLeService", "Escaneando version Lollipop");

            // ScanCallback
            final BLEScanCallBack scanCallbackL = new BLEScanCallBack(myadapter);

            // ScanFilters
            ScanFilter.Builder scanFilterBuilderL = new ScanFilter.Builder();

            // List of ScanFilters
            List<ScanFilter> filtersL = new ArrayList<>();

            // ScanSettings builder
            ScanSettings.Builder scanSettingsBuilderL = new ScanSettings.Builder();
            ScanFilter scanFilterL = scanFilterBuilderL.build();
            filtersL.add(scanFilterL);

            ScanSettings scanSettingsL = scanSettingsBuilderL
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .build();

            myBLEAdapter.getBluetoothLeScanner().startScan(filtersL, scanSettingsL, scanCallbackL);
            stopcallback = scanCallbackL;
        }

        // SCANNER PARA JELLYBEAN AND KITKAT
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            Log.i("BluetoothLeService", "Escaneando version JellyBean");
            myBLEAdapter.startLeScan(mLeScanCallBack);
        }
    }

    public void stopScan() {

        BLEScanCallBack.mydevicesMAC.clear();
        // SCANNER PARA MARSHMALLOW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            myBLEAdapter.getBluetoothLeScanner().stopScan(stopcallback);
        }
        // SCANNER PARA LOLLIPOP
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myBLEAdapter.getBluetoothLeScanner().stopScan(stopcallback);
        }
        // SCANNER PARA JELLYBEAN AND KITKAT
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (myBLEAdapter.isDiscovering()) myBLEAdapter.stopLeScan(mLeScanCallBack);
        }
    }

    public void changeFragment(String s) {
        stopScan();
        getContext().unregisterReceiver(myBr);
        ConnectedFragment myFragment = new ConnectedFragment();
        Bundle args = new Bundle();
        args.putString("MAC", s);
        myFragment.setArguments(args);
        FragmentTransaction mytransaction = getActivity().getSupportFragmentManager().beginTransaction();
        mytransaction.replace(R.id.fragment_container, myFragment, "Test");
        mytransaction.commit();
    }



}
