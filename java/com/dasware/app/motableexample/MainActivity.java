package com.dasware.app.motableexample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 13579;
    private final int REQUEST_BT_PERMISSION = 12368;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_BT_PERMISSION);
        }else{
            checkBleActive();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_BT_PERMISSION){
            if (grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),"Habilite los permisos necesarios para usar la aplicación",Toast.LENGTH_LONG).show();
            }else{
                checkBleActive();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK) {
                   fragmentTransaction();
            }
            if (resultCode == RESULT_CANCELED) {
                 finish();
            }
        }

    }

    public void checkBleActive(){

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            fragmentTransaction();
        }
    }



    public void fragmentTransaction(){

        ScanListFragment myFragment = new ScanListFragment();
        FragmentTransaction mytransaction = getSupportFragmentManager().beginTransaction();
        mytransaction.replace(R.id.fragment_container,myFragment,"List");
        mytransaction.commit();


    }




}
