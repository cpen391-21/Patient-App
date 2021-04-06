package com.example.neurostimulationpatientaccess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class RegimenListAdapter extends ArrayAdapter<Regimen> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<Regimen> mRegimens;
    private int mViewResourcesId;


    public RegimenListAdapter(@NonNull Context context, int tvResourceId, @NonNull ArrayList<Regimen> regimens) {
        super(context, tvResourceId, regimens);
        this.mRegimens = regimens;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourcesId = tvResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourcesId, null);

        String regimenName = mRegimens.get(position).regimenName;

        if (regimenName != null) {
            TextView tvRegimenName = (TextView) convertView.findViewById(R.id.tvRegimenName);

            if (tvRegimenName != null) {
                tvRegimenName.setText(regimenName);
            }
        }
        return convertView;
    }
}