package com.dasware.app.motableexample;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by SamuelPS on 17/11/2016.
 */

public class ScanListAdapter extends BaseAdapter {



    LayoutInflater inflater;
    ArrayList<BluetoothDevice> lista;

    public ScanListAdapter(Context a){
        inflater=(LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lista=new ArrayList<>();
    }

    public ArrayList getList(){
        return lista;
    }

    public boolean add(BluetoothDevice b){
        lista.add(b);
        this.notifyDataSetChanged();
        return true;
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView == null){
            vi=inflater.inflate(R.layout.scan_item,null);
        }

        TextView NAME = (TextView) vi.findViewById(R.id.scan_name);
        NAME.setText(lista.get(position).getName());

        TextView MAC = (TextView) vi.findViewById(R.id.scan_MAC);
        MAC.setText(lista.get(position).getAddress());
        return vi;
    }
}
