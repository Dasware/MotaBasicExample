package com.dasware.app.motableexample;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();
    public MotaBle myMota;
    CSVWriter csvWrite;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("Service/BleCallBack", "STATE_DISCONNECTED");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                myMota.suscribe();
            } else {
                Log.i("Service/BleCallBack", "Something went wrong at Service Discovering");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i("CB", "Received data...");
            parsedata(characteristic.getValue());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (characteristic.getUuid().equals(MotaBle.Conf_GattChar)){
               // Toast.makeText(getApplicationContext(),"Conf set succesfully!!",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            getApplicationContext().sendBroadcast(new Intent("conectado"));
        }
    };


    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {

        myMota = new MotaBle(getApplicationContext());
        return myMota.init();
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        return myMota.connect(mGattCallback,address);
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        myMota.disconnect();
    }

    public void conf(int a, int b, int c){
        myMota.conf(a,b,c);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {


        myMota.close();
        myMota=null;
    }
    public void parsedata(byte[] data){
        long vals[]=new long[7];
        double fvals[]=new double[7];
        double adiv=1;
        double gdiv=1;

        //accel
        vals[0]=parseAccel(data[0],data[1]);
        vals[1]=parseAccel(data[2],data[3]);
        vals[2]=parseAccel(data[4],data[5]);
        //temp
        vals[3]= parseAccel(data[6],data[7]);
        //gyro
        vals[4]=parseAccel(data[8],data[9]);
        vals[5]=parseAccel(data[10],data[11]);
        vals[6]=parseAccel(data[12],data[13]);

        switch (myMota.conf[1]){

            case 0:
                adiv=16384;
                break;
            case 1:
                adiv=8192;
                break;
            case 2:
                adiv=4096;
                break;
            case 3:
                adiv=2048;
                break;



        }

        switch (myMota.conf[2]){
            case 0:
                gdiv=131;
                break;
            case 1:
                gdiv=65.5;
                break;
            case 2:
                gdiv=32.8;
                break;
            case 3:
                gdiv=16;
                break;


        }

        //accel
        fvals[0]=vals[0]/adiv;
        fvals[1]=vals[1]/adiv;
        fvals[2]=vals[2]/adiv;
        //temp
        fvals[3]=vals[3]/340+36.53;
        //gyro
        fvals[4]=vals[3]/gdiv;
        fvals[5]=vals[4]/gdiv;
        fvals[6]=vals[5]/gdiv;




        addtofile(fvals);
    }

    public long parseAccel(byte a, byte b){

        long val = ((a&0xFF)<<8)|((b&0xFF));

        if (val >= 0x8000){
            val= -((65535 - val) + 1);
        }

        return val;
    }


    private void addtofile(double values[]) {

        String output[]=new String[values.length];
        for(int k = 0;k<values.length;k++){
            output[k]=Double.toString(values[k]);

        }
        // Log.i("AData:", output[0]+"/"+output[1]+"/"+output[2]);
        // Log.i("GData:", output[3]+"/"+output[4]+"/"+output[5]);
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "TestmotaIMUDS.csv");

        if(file.exists()){
            try {
                csvWrite = new CSVWriter(new FileWriter(file,true), ';');
                csvWrite.writeNext(output,true);
                csvWrite.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            try {
                file.createNewFile();
                csvWrite = new CSVWriter(new FileWriter(file,true), ';');
                csvWrite.writeNext(output,true);
                csvWrite.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }






}
