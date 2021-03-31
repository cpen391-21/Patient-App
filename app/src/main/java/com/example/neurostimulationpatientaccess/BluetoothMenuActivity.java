package com.example.neurostimulationpatientaccess;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BluetoothMenuActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTooth, mPairedBluetooth;
    ImageView mBlueTooth;
    Button mOnBtn, mOffBtn, mDiscoverBtn, mPairedBtn;

    BluetoothAdapter mBlueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_menu);

        mStatusBlueTooth = findViewById(R.id.bluetooth_status);
        mPairedBluetooth = findViewById(R.id.bluetooth_paired);
        mBlueTooth = findViewById(R.id.bluetooth_image);
        mOnBtn = findViewById(R.id.turn_on_bluetooth);
        mOffBtn = findViewById(R.id.turn_off_bluetooth);
        mDiscoverBtn = findViewById(R.id.discover_bluetooth_btn);
        mPairedBtn = findViewById(R.id.get_paired_btn);

        //bluetooth adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        //check bluetooth availability
        if (mBlueAdapter == null) {
            mStatusBlueTooth.setText("Bluetooth is not available.");
        } else {
            mStatusBlueTooth.setText("Bluetooth is available.");
        }

        if (mBlueAdapter.isEnabled()) {
            mBlueTooth.setImageResource(R.drawable.ic_action_on);
        } else {
            mBlueTooth.setImageResource(R.drawable.ic_action_off);
        }

        //turn on bluetooth button
        mOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBlueAdapter.isEnabled()) {
                    showToast("Turning on Bluetooth...");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    showToast("Bluetooth is already enabled.");
                }

            }
        });

        //turn off bluetooth button
        mOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    mBlueAdapter.disable();
                    showToast("Turning off Bluetooth...");
                    mBlueTooth.setImageResource(R.drawable.ic_action_off);
                } else {
                    showToast("Bluetooth is already off.");
                }
            }
        });

        //discover devices
        mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBlueAdapter.isDiscovering()) {
                    showToast("Make your device discoverable.");
                    Intent intent  = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });

        //get paired devices
        mPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    mPairedBluetooth.setText("Paired Devices");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for (BluetoothDevice device: devices) {
                        mPairedBluetooth.append("\nDevice: " + device.getName() + "," + device);
                    }
                } else {
                    showToast("Turn on Bluetooth to get paired devices.");
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    mBlueTooth.setImageResource(R.drawable.ic_action_on);
                    showToast("Bluetooth is on.");
                } else {
                    showToast("Could not turn on Bluetooth.");
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //toast message
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}