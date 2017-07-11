package com.dasware.app.motableexample;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import static android.content.ContentValues.TAG;
import static android.content.Context.BIND_AUTO_CREATE;

public class ConnectedFragment extends Fragment {

    BluetoothLeService mBluetoothLeService;
    ServiceConnection mServiceConnection;
    String MAC;
    SeekBar accelconf;
    SeekBar gyroconf;
    Button startBtn;
    TextView acceltv;
    AlertDialog myDialog;
    TextView gyrotv;
    boolean bound;
    byte[] conf = {1,0,0};
    BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action){
                case "conectado":
                    changeView();
                    break;
                case "Stop":
                    stopandsend();
                    break;
            }
        }
    };
    public ConnectedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MAC=(String)getArguments().get("MAC");

        IntentFilter myintentfilter = new IntentFilter();
        myintentfilter.addAction("conectado");
        myintentfilter.addAction("Stop");
        getContext().registerReceiver(myBroadcastReceiver, myintentfilter);
        Log.i("CF", "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("CF", "onCreateView");
        mServiceConnection= new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    getActivity().finish();
                }
                // Automatically connects to the device upon successful start-up initialization.
                Log.i("Service"," Conectando");
                mBluetoothLeService.connect(MAC);
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBluetoothLeService = null;
            }
        };

        Intent gattServiceIntent = new Intent(getContext(), BluetoothLeService.class);
        bound = getContext().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.i("Service bound:"," "+bound);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connecte, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("CF", "onViewCreated");
        deleteFile();

    }

    public void changeView(){

        LinearLayout ly = (LinearLayout)getView().findViewById(R.id.conectando);
        ly.setVisibility(View.GONE);

        RelativeLayout rl = (RelativeLayout)getView().findViewById(R.id.buttons);
        rl.setVisibility(View.VISIBLE);

        final ProgressBar mb = (ProgressBar)getView().findViewById(R.id.readingpb) ;

        accelconf = (SeekBar) getView().findViewById(R.id.accelSb);
        gyroconf = (SeekBar) getView().findViewById(R.id.accelSb);
        acceltv = (TextView) getView().findViewById(R.id.acceltv);
        gyrotv = (TextView) getView().findViewById(R.id.gyrotv);
        startBtn = (Button) getView().findViewById(R.id.startBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.conf(conf[0],conf[1],conf[2]);
                if(conf[0]==0){
                    conf[1]=1;
                    startBtn.setText("Start");
                    sendViaMail();
                    mb.setVisibility(View.GONE);

                }else{
                    conf[0]=0;
                    startBtn.setText("Stop");
                    mb.setVisibility(View.VISIBLE);

                }
            }
        });

        accelconf.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                conf[1]=(byte)progress;
                acceltv.setText("Accel Range: ±"+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        gyroconf.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                conf[2]=(byte)progress;
                gyrotv.setText("Gyro Range: ±"+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void sendViaMail(){

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, "TestmotaIMUDS.csv");
        if(file.exists()) {
            Uri path = Uri.fromFile(file);
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("vnd.android.cursor.dir/email");
            emailIntent.putExtra(Intent.EXTRA_STREAM, path);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "IMUDS");
            getContext().startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }


    }

    public void stopandsend(){
        mBluetoothLeService.conf(0,conf[1],conf[2]);

        sendViaMail();
    }

    public void deleteFile(){
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "TestmotaIMUDS.csv");

        file.delete();

    }


}
