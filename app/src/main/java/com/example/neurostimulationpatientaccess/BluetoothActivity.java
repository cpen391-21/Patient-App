package com.example.neurostimulationpatientaccess;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "BluetoothActivity";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    BluetoothAdapter mBluetoothAdapter;

    ImageView mBlueTooth;
    Button turnOnOffBluetooth, btnEnableDisableDiscovery, btnPairedDevices;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mPairedDevices = new ArrayList<>(); //taken care of as set
    public DeviceListAdapter mDeviceListAdapter, mPairedListAdapter;
    ListView lvNewDevices;

    //BoadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //when discovery finds a device
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        mBlueTooth.setImageResource(R.drawable.ic_action_off);
                        //Toast.makeText(BluetoothActivity.this, "BLUETOOTH IS OFF", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        //Toast.makeText(BluetoothActivity.this, "BLUETOOTH IS TURNING OFF", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        mBlueTooth.setImageResource(R.drawable.ic_action_on);
                        //Toast.makeText(BluetoothActivity.this, "BLUETOOTH IS ON", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        //Toast.makeText(BluetoothActivity.this, "BLUETOOTH IS TURNING ON", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    };

    //BoadcastReceiver for SCAN_MODE_CHANGED
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //when discovery finds a device
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled");
                        Toast.makeText(BluetoothActivity.this, "Device discoverable for 600 seconds.", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled. Able to receive connection.");
                        Toast.makeText(BluetoothActivity.this, "Discoverability Enabled. Able to receive connection.", Toast.LENGTH_LONG).show();
                        //Toast.makeText(BluetoothActivity.this, "DISCOVERABILITY IS ENABLED", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connection.");
                        Toast.makeText(BluetoothActivity.this, "Discoverability Disabled. Not able to receive connection.", Toast.LENGTH_LONG).show();
                        //Toast.makeText(BluetoothActivity.this, "DISCOVERABILITY IS DISABLED", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                        Toast.makeText(BluetoothActivity.this, "Connecting...", Toast.LENGTH_LONG).show();
                        //Toast.makeText(BluetoothActivity.this, "Connecting...", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected");
                        Toast.makeText(BluetoothActivity.this, "CONNECTED", Toast.LENGTH_LONG).show();
                        //Toast.makeText(BluetoothActivity.this, "CONNECTED", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND, mBroadcastReceiver3");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mBTDevices.contains(device)) {
                    mBTDevices.add(device);
                }
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                //clear devices
                lvNewDevices.setAdapter(new DeviceListAdapter(context, R.layout.device_adapter_view, new ArrayList<>()));
                //add devices
                lvNewDevices.setAdapter(mDeviceListAdapter);
            } else {
                lvNewDevices.setAdapter(new DeviceListAdapter(context, R.layout.device_adapter_view, new ArrayList<>()));
                Toast.makeText(BluetoothActivity.this, "Turn on Bluetooth to get devices.", Toast.LENGTH_LONG).show();
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases
                //case 1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
                }
                //case 2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING");
                }
                //case 3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE");
                }
            }
        }
    };

    /*
    private BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ACTION FOUND, mBroadcastReceiver5");
            if (mBluetoothAdapter.isEnabled()) {
                mPairedDevices = (ArrayList<BluetoothDevice>) mBluetoothAdapter.getBondedDevices();
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mPairedDevices);
                //clear devices
                lvNewDevices.setAdapter(new DeviceListAdapter(context, R.layout.device_adapter_view, new ArrayList<>()));
                //add devices
                lvNewDevices.setAdapter(mDeviceListAdapter);
            } else {
                lvNewDevices.setAdapter(new DeviceListAdapter(context, R.layout.device_adapter_view, new ArrayList<>()));
                Toast.makeText(BluetoothActivity.this, "Turn on Bluetooth to get paired devices.", Toast.LENGTH_LONG).show();
            }
        }
    };*/

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: CALLED");
        super.onDestroy();
        try {
            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(mBroadcastReceiver1);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        try {
            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(mBroadcastReceiver2);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        try {
            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(mBroadcastReceiver3);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        try {
            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(mBroadcastReceiver4);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        turnOnOffBluetooth = (Button) findViewById(R.id.turn_on_off_bluetooth);
        btnEnableDisableDiscovery = (Button) findViewById(R.id.enable_discover);
        btnPairedDevices = (Button) findViewById(R.id.get_paired_devices);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBlueTooth = findViewById(R.id.bluetooth_image);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(BluetoothActivity.this);

        if (mBluetoothAdapter.isEnabled()) {
            mBlueTooth.setImageResource(R.drawable.ic_action_on);
        } else {
            mBlueTooth.setImageResource(R.drawable.ic_action_off);
        }

        turnOnOffBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ENABLING/DISABLING BLUETOOTH");
                enableDisableBT();
            }
        });

        btnEnableDisableDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnEnableDisableDiscovery: Request for making device discoverable for 10 minutes.");
                //600 seconds = 10 minutes

                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);

                discoverableIntent.putExtra("discover", REQUEST_DISCOVER_BT);
                startActivityForResult(discoverableIntent, REQUEST_DISCOVER_BT);

                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(mBroadcastReceiver2, intentFilter);
            }
        });

        btnPairedDevices.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onReceive: ACTION FOUND, display paired");
                if (mBluetoothAdapter.isEnabled()) {
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                        Log.d(TAG, "btnPairedDevices: Cancelling discovery.");
                    }
                    //set of devices
                    Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                    mBTDevices.clear();
                    mPairedDevices.clear();
                    for (BluetoothDevice device: devices) {
                        mPairedDevices.add(device);
                    }
                    mPairedListAdapter = new DeviceListAdapter(BluetoothActivity.this, R.layout.device_adapter_view, mPairedDevices);
                    //clear devices
                    lvNewDevices.setAdapter(new DeviceListAdapter(BluetoothActivity.this, R.layout.device_adapter_view, new ArrayList<>()));
                    //add devices
                    lvNewDevices.setAdapter(mPairedListAdapter);
                } else {
                    lvNewDevices.setAdapter(new DeviceListAdapter(BluetoothActivity.this, R.layout.device_adapter_view, new ArrayList<>()));
                    Toast.makeText(BluetoothActivity.this, "Turn on Bluetooth to get paired devices.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void enableDisableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
            mBlueTooth.setImageResource(R.drawable.ic_action_off);
            System.out.println(TAG + " enableDisableBT: Does not have BT capabilities.");
            Toast.makeText(BluetoothActivity.this, "BT capabilities are unavailable!", Toast.LENGTH_LONG).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "onClick: ENABLING BLUETOOTH");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBTIntent.putExtra("enable", REQUEST_ENABLE_BT);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "onClick: DISABLING BLUETOOTH");
            mBluetoothAdapter.disable();
            mBlueTooth.setImageResource(R.drawable.ic_action_off);
            Toast.makeText(BluetoothActivity.this, "BLUETOOTH IS OFF", Toast.LENGTH_LONG).show();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    mBlueTooth.setImageResource(R.drawable.ic_action_on);
                    Toast.makeText(BluetoothActivity.this, "BLUETOOTH IS ON", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BluetoothActivity.this, "Could not connect to BLUETOOTH.", Toast.LENGTH_LONG).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if (mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "btnDiscover: Cancelling discovery.");

                //check BT permissions
                checkBTPermissions();

                mBluetoothAdapter.startDiscovery();
                IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDeviceIntent);
            }
            if (!mBluetoothAdapter.isDiscovering()) {
                //check BT permissions in manifest
                checkBTPermissions();
                mBluetoothAdapter.startDiscovery();
                IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDeviceIntent);
            }
        } else {
            Toast.makeText(BluetoothActivity.this, "Bluetooth is not connected.", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //any number
            } else {
                Log.d(TAG, "checkBTPermission: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because it is very memory intensive
        if (!mBTDevices.isEmpty()) {
            mBluetoothAdapter.cancelDiscovery();

            Log.d(TAG, "onItemClick: You clicked on a device.");
            String deviceName = mBTDevices.get(i).getName();
            String deviceAddress = mBTDevices.get(i).getAddress();

            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

            //create the bond
            //NOTE: Requires API 17+? I think this is JellyBean
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.d(TAG, "Trying to pair with " + deviceName + " " + deviceAddress);
                mBTDevices.get(i).createBond();
            }
        } else {
            Log.d(TAG, "onItemClick: You clicked on a paired device.");
            String deviceName = mPairedDevices.get(i).getName();
            String deviceAddress = mPairedDevices.get(i).getAddress();
            //create the bond
            //NOTE: Requires API 17+? I think this is JellyBean
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.d(TAG, "Trying to pair with " + deviceName + " " + deviceAddress);
                if (mPairedDevices.get(i).getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(BluetoothActivity.this, "Device is already bonded.", Toast.LENGTH_LONG).show();
                } else {
                    mPairedDevices.get(i).createBond();
                }
            }
        }
    }
}