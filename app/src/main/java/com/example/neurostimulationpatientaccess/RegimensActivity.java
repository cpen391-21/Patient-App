package com.example.neurostimulationpatientaccess;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

public class RegimensActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "RegimensActivity";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    BluetoothAdapter mBluetoothAdapter;
    public BluetoothDevice currentDevice;

    private Button btnDeleteRegimen, btnCreateRegimen, btnStartRegimen, btnStopRegimen, btnResumeRegimen, btnPauseRegimen;
    public ArrayList<BluetoothDevice> mPairedDevices = new ArrayList<>(); //taken care of as set
    public ArrayList<Regimen> mRegimenList = new ArrayList<>();
    public DeviceListAdapter mPairedListAdapter;
    public RegimenListAdapter mRegimensListAdapter;

    public Regimen currentRegimen;

    private TextView currentDeviceName, currentDeviceAddress, currentRegimenText, noCurrentDeviceName, noCurrentDeviceAddress, noCurrentRegimens;
    private ListView btDevices, currentRegimens;

    //message if there are no paired devices available
    String noDeviceNames, noDeviceAddresses, noCurrentDevice, noCurrentAddress;

    //messages if there are no regimens available
    String noRegimens, noRegimenName;

    JSONObject userRegimens = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regimens);

        noDeviceNames = "No devices available.";
        noDeviceAddresses = "Please check BLUETOOTH!";
        noCurrentDevice = "No device selected.";
        noCurrentAddress = "Please select device!";

        noRegimens = "No regimens available.";
        noRegimenName = "No current regimen.";

        btnDeleteRegimen = (Button) findViewById(R.id.delete_regimen_btn);
        btnCreateRegimen = (Button) findViewById(R.id.create_regimen_btn);
        btnStartRegimen = (Button) findViewById(R.id.start_regimen_btn);
        btnStopRegimen = (Button) findViewById(R.id.stop_regimen_btn);
        btnResumeRegimen = (Button) findViewById(R.id.resume_regimen_btn);
        btnPauseRegimen = (Button) findViewById(R.id.pause_regimen_btn);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btDevices = (ListView) findViewById(R.id.pairedDevices);
        currentDeviceName = (TextView) findViewById(R.id.currentDeviceName);
        currentDeviceAddress = (TextView) findViewById(R.id.currentDeviceAddress);

        currentRegimens = (ListView) findViewById(R.id.regimensList);
        currentRegimenText = (TextView) findViewById(R.id.current_regimen);
        noCurrentRegimens = (TextView) findViewById(R.id.emptyRegimensList);

        noCurrentDeviceName = (TextView) findViewById(R.id.noCurrentDeviceName);
        noCurrentDeviceAddress = (TextView) findViewById(R.id.noCurrentDeviceAddress);

        btDevices.setOnItemClickListener(RegimensActivity.this);
        currentRegimens.setOnItemClickListener(RegimensActivity.this);

        //fill device list
        if (mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "btnPairedDevices: Cancelling discovery.");
            }
            //set of devices
            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

            if (devices.isEmpty()) {
                Log.d(TAG, "onCreate: No paired devices.");
                //mPairedListAdapter = new DeviceListAdapter(RegimensActivity.this, R.layout.device_adapter_view, mPairedDevices);
                //clear devices
                btDevices.setAdapter(new DeviceListAdapter(RegimensActivity.this, R.layout.device_adapter_view, new ArrayList<>()));
                //add devices
                //btDevices.setAdapter(mPairedListAdapter);
                //set empty text
                noCurrentDeviceName.setText(noDeviceNames);
                noCurrentDeviceAddress.setText(noDeviceAddresses);
            } else {
                Log.d(TAG, "onCreate: Display paired");
                mPairedDevices.clear();
                //no block
                noCurrentDeviceName.setText("");
                noCurrentDeviceAddress.setText("");
                for (BluetoothDevice device : devices) {
                    mPairedDevices.add(device);
                }
                mPairedListAdapter = new DeviceListAdapter(RegimensActivity.this, R.layout.device_adapter_view, mPairedDevices);
                //clear devices
                btDevices.setAdapter(new DeviceListAdapter(RegimensActivity.this, R.layout.device_adapter_view, new ArrayList<>()));
                //add devices
                btDevices.setAdapter(mPairedListAdapter);
            }
        } else {
            Log.d(TAG, "onCreate: No paired devices.");
            mPairedDevices.clear();
            //mPairedListAdapter = new DeviceListAdapter(RegimensActivity.this, R.layout.device_adapter_view, mPairedDevices);
            //clear devices
            btDevices.setAdapter(new DeviceListAdapter(RegimensActivity.this, R.layout.device_adapter_view, new ArrayList<>()));
            //add devices
            //btDevices.setAdapter(mPairedListAdapter);
            //set empty text
            noCurrentDeviceName.setText(noDeviceNames);
            noCurrentDeviceAddress.setText(noDeviceAddresses);
        }

        //fill current device
        if (currentDevice == null) {
            Log.d(TAG, "onCreate: No current device.");
            currentDeviceName.setText(noCurrentDevice);
            currentDeviceAddress.setText(noCurrentAddress);
        } else {
            Log.d(TAG, "onCreate: Display current device.");
            currentDeviceName.setText(currentDevice.getName());
            currentDeviceAddress.setText(currentDevice.getAddress());
        }

        //fill current regimen
        if (currentRegimen == null) {
            Log.d(TAG, "onCreate: No current regimen.");
            currentRegimenText.setText(noRegimenName);
        } else {
            Log.d(TAG, "onCreate: Display current regimen.");
            currentRegimenText.setText(currentRegimen.regimenName);
        }

        //fill regimen list
        if (mRegimenList == null || mRegimenList.isEmpty()) {
            Log.d(TAG, "onCreate: No regimens to display.");
            //mRegimensListAdapter = new RegimenListAdapter(RegimensActivity.this, R.layout.regimens_adapter_view, mRegimenList);
            currentRegimens.setAdapter(new DeviceListAdapter(RegimensActivity.this, R.layout.regimens_adapter_view, new ArrayList<>()));
            noCurrentRegimens.setText(noRegimens);
        } else {
            Log.d(TAG, "onCreate: Display regimens.");
            noCurrentRegimens.setText("");
            mRegimensListAdapter = new RegimenListAdapter(RegimensActivity.this, R.layout.regimens_adapter_view, mRegimenList);
            currentRegimens.setAdapter(mRegimensListAdapter);
        }

        btnCreateRegimen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateRegimenActivity.class));
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //check which list
        //devices list
        Log.d(TAG, "onItemClick: Clicked list item");
        if (adapterView.getId() == R.id.pairedDevices) {
            Log.d(TAG, "onItemClick: clicked device list item");
            currentDevice = mPairedDevices.get(i);
            Log.d(TAG, "onItemClick: Display current device.");
            currentDeviceName.setText(currentDevice.getName());
            currentDeviceAddress.setText(currentDevice.getAddress());
            //connect device

        }
        //regimens list
        else if (adapterView.getId() == R.id.regimensList) {
            Log.d(TAG, "onItemClick: clicked regimen list item");
            currentRegimen = mRegimenList.get(i);
            Log.d(TAG, "onItemClick: Display current regimen.");
            currentRegimenText.setText(currentRegimen.regimenName);
        }
        else {
            Log.d(TAG, "onItemClick: error clicking list");
            showToast("Error in clicking list.");
        }
    }

    public void btnStartRegimen(View view) {
        if (currentDevice == null) {
            Log.d(TAG, "btnStartRegimen: No device selected.");
            showToast("No device selected to run regimen.");
        } else {
            Log.d(TAG, "btnStartRegimen: Running regimen on device " + currentDevice.getName());
        }
    }

    public void showToast(String message) {
        Toast.makeText(RegimensActivity.this, message, Toast.LENGTH_LONG).show();
    }
}