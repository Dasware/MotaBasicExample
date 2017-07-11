package com.dasware.app.motableexample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

/**
 * Created by SamuelPS on 18/05/2017.
 */

public class MotaBle {

    Context context;
    BluetoothGatt motaGatt;
    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    String mBluetoothDeviceAddress;
    public boolean init;
    public byte[] conf=new byte[3];

    public static final UUID Service = UUID.fromString("cc31c41a-f835-4af5-ab48-81bb6baba638");
    public static final UUID Conf_GattChar = UUID.fromString("ca5fd17a-c934-4118-96bc-f3e3892196f6");
    public static final UUID Data_GattChar = UUID.fromString("ca5fd17a-c934-4118-96bc-f3e3892196f8");

    public static final int START=1;
    public static final int STOP=0;

    public static final int ACCEL_RANGE_2=0;
    public static final int ACCEL_RANGE_4=1;
    public static final int ACCEL_RANGE_8=2;
    public static final int ACCEL_RANGE_16=3;

    public static final int GYRO_RANGE_250=0;
    public static final int GYRO_RANGE_500=1;
    public static final int GYRO_RANGE_1000=2;
    public static final int GYRO_RANGE_2000=3;

    public MotaBle(Context ctx){
        context=ctx;
        init=false;
    }


    /**
     * Inicializamos bluetooth related
     * @return
     */
    public boolean init(){
        Log.i("Init", "Inicializando...");
        conf[0]=0;
        conf[1]=0;
        conf[2]=0;
        mBluetoothDeviceAddress=null;
        init=true;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

            return true;

    }

    /**
     * Conectamos a la mota
     * @param mblecallback
     * @param address
     * @return
     */

    public boolean connect(final BluetoothGattCallback mblecallback, final String address){

            //Si existe conexion, reconectamos.
           if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                   && motaGatt != null) {
               if (motaGatt.connect()) {
                   return true;
               } else {
                   return false;
               }
           }

            //Comenzamos connexion
            mBluetoothDeviceAddress=address;
            BluetoothDevice device= mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
            motaGatt = device.connectGatt(context,false,mblecallback);
            return true;
    }

    public void disconnect(){
        if (mBluetoothAdapter == null || motaGatt == null) {
            return;
        }
        motaGatt.disconnect();
    }

    /**
     * Escribimos caracateristica de configuración
     * @param startstop
     * @param accelrange
     * @param gyrorange
     * @return
     */
    public boolean conf(int startstop, int accelrange, int gyrorange){
        conf[0]=(byte)startstop;
        conf[1]=(byte)accelrange;
        conf[2]=(byte)gyrorange;

        BluetoothGattCharacteristic confChar = motaGatt.getService(Service).getCharacteristic(Conf_GattChar);
        confChar.setValue(conf);
        return motaGatt.writeCharacteristic(confChar);
    }

    /**
     * Nos suscribimos a la caracteristica de lectura
     */
    public boolean suscribe(){
        BluetoothGattCharacteristic mychar = motaGatt.getService(Service).getCharacteristic(Data_GattChar);
        motaGatt.setCharacteristicNotification(mychar,true);
        UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor descriptor = mychar.getDescriptor(uuid);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        return motaGatt.writeDescriptor(descriptor);
    }

    public void close(){
        if (motaGatt == null) {
            return;
        }
        motaGatt.close();
        motaGatt = null;
    }


}
