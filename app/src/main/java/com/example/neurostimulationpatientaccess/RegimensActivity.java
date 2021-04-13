package com.example.neurostimulationpatientaccess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.android.ui.IconGenerator;

public class RegimensActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "RegimensActivity";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    BluetoothAdapter mBluetoothAdapter;
    public BluetoothDevice currentDevice;

    private Button btnDeleteRegimen, btnCreateRegimen, btnStartRegimen, btnStopRegimen, btnResumeRegimen, btnPauseRegimen;
    public ArrayList<BluetoothDevice> mPairedDevices = new ArrayList<>(); //taken care of as set
    public ArrayList<Regimen> mRegimenList = new ArrayList<>();
    public ArrayList<Regimen> mServerRegimenList = new ArrayList<>();
    public ArrayList<Regimen> mLocalRegimenList = new ArrayList<>();
    public DeviceListAdapter mPairedListAdapter;
    public RegimenListAdapter mRegimensListAdapter;

    public Regimen currentRegimen;

    private TextView currentDeviceName, currentDeviceAddress, currentRegimenText, noCurrentDeviceName, noCurrentDeviceAddress, noCurrentRegimens;
    private ListView btDevices, currentRegimens;

    //message if there are no paired devices available
    String noDeviceNames, noDeviceAddresses, noCurrentDevice, noCurrentAddress;

    //messages if there are no regimens available
    String noRegimens, noRegimenName;

    //URLs for server
    private String post_URL = "";
    private String get_URL = "";
    private String FILE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regimens);

        FILE_NAME = "local_regimens.json";

        //if we ever need to run a clear
        /*
        try {
            File file = new File(getFilesDir(), FILE_NAME);
            JsonParser parser = new JsonParser();
            JsonArray allRegs = new JsonArray();   // reading the file and creating a json array of it.
            //JsonArray allRegs = new JsonArray();
            //allRegs.add(newRegimenJson.toString());   // adding your created object into the array
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(allRegs.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            Log.d(TAG, "addRegimen: failed to place objects in JSON.");
            e.printStackTrace();
        }*/

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

        final RequestQueue postQueue = Volley.newRequestQueue(this);
        final RequestQueue getQueue = Volley.newRequestQueue(this);
        final JSONObject jsnReq = new JSONObject();

        try {
            jsnReq.put("token", FirebaseAuth.getInstance().getCurrentUser().getIdToken(true));
            /*
            jsnReq.put("token", FirebaseAuth.getInstance().getAccessToken(true));
            jsnReq.put("token", FirebaseInstanceId.getInstance().getId());*/
        } catch (JSONException e) {
            Log.d(TAG, "Error with token.");
            e.printStackTrace();
        }
        /*
        final JsonObjectRequest json_obj = new JsonObjectRequest(Request.Method.POST, URL, jsnReq,
                new Response.Listener<JSONObject> (){*/
        //send request
        Log.d(TAG, "onCreate: JSON server stuff start");
        final JsonObjectRequest json_obj = new JsonObjectRequest(Request.Method.POST, post_URL, jsnReq, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean successVal = (boolean) response.get("success");
                    if (successVal) {
                        //GET JSON data
                        final JsonArrayRequest allActivities = new JsonArrayRequest(Request.Method.GET, get_URL,null,
                                new Response.Listener<JSONArray> (){
                                    @Override
                                    public void onResponse(JSONArray response){

                                        /**Get all the activities JSONArray and then parse the array to read and display the required info*/
                                        try {
                                            for(int i = 0; i < response.length(); i++){
                                                JSONObject jsonObject = response.getJSONObject(i);

                                                String regimenName = jsonObject.getString("name");
                                                double duration = jsonObject.getDouble("duration");
                                                double offset = jsonObject.getDouble("offset");

                                                ArrayList<Pair<String, Pair<Double, Double>>> regimenWaves = new ArrayList<>();

                                                JSONArray jsonWaves = jsonObject.getJSONArray("waves");
                                                for (int j = 0; j < jsonWaves.length(); j++) {
                                                    JSONObject waveJson = jsonWaves.getJSONObject(j);
                                                    Pair<String, Pair<Double, Double>> wave;
                                                    if (waveJson.getString("name").equals("random")) {
                                                        wave = Pair.create(waveJson.getString("name"), Pair.create(0.0, waveJson.getDouble("amplitude")));
                                                    } else {
                                                        wave = Pair.create(waveJson.getString("name"), Pair.create(waveJson.getDouble("frequency"), waveJson.getDouble("amplitude")));
                                                    }
                                                    regimenWaves.add(wave);
                                                }
                                                Log.d(TAG, "Regimen Name: " + regimenName);
                                                Log.d(TAG, "Regimen Waves: " + regimenWaves);
                                                Log.d(TAG, "Regimen Duration: " + duration);
                                                Log.d(TAG, "Regimen Offset: " + offset);
                                                Log.d(TAG, "Adding regimen to list.");
                                                mServerRegimenList.add(new Regimen(regimenName, regimenWaves, duration, offset));
                                                /*
                                                String name = jsonObject.getString("name");

                                                IconGenerator mGenerator = new IconGenerator(MainMapsActivity.this);
                                                Bitmap iconBitmap = mGenerator.makeIcon(name);

                                                LatLng marker = new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("long"));

                                                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).position(marker).title(jsonObject.getString("aid")).draggable(true));
                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker,14.0f));*/
                                            }
                                        } catch (JSONException e) {
                                            Log.d(TAG, "Error getting regimen data.");
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error connecting to regimens database and getting regimen data.");
                                error.printStackTrace();
                            }
                        });
                        //requestQueue.add(allActivities);
                        //mMap.setOnMarkerClickListener(this);
                        getQueue.add(allActivities);
                    } else {
                        Log.d(TAG, "Error connecting to regimens database.");
                        Toast.makeText(RegimensActivity.this, "ERROR connecting to regimens database.", Toast.LENGTH_SHORT).show();
                    }
                    String stat = response.get("status").toString();
                    Log.d(TAG, stat);
                } catch (JSONException e) {
                    Log.d(TAG, "Error connecting to regimens database.");
                    e.printStackTrace();
                }
            }
        }, error -> {
            /*
            if(error.networkResponse.statusCode == 401)
                Toast.makeText(RegimensActivity.this, "Incomplete connection.", Toast.LENGTH_SHORT).show();
            else if(error.networkResponse.statusCode == 402)
                Toast.makeText(RegimensActivity.this, "Error in connection.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(RegimensActivity.this, "Unable to send data to server.", Toast.LENGTH_SHORT).show();
            */
            Log.d(TAG, "ERROR: No response from server.");
            error.printStackTrace();
        });
        postQueue.add(json_obj);
        Log.d(TAG, "onCreate: JSON server stuff ends");

        try {
            //ArrayList<JSONObject> json=new ArrayList<JSONObject>();
            //JSONObject obj;
            File file = new File(getFilesDir(), FILE_NAME);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            Log.d(TAG, "onCreate: obtained line " + line);
            while (line != null) {
                //obj = (JSONObject) new JsonParser().parse(line);
                //json.add(obj);
                stringBuilder.append(line);
                line = bufferedReader.readLine();
                Log.d(TAG, "onCreate: obtained line " + line);
            }
            bufferedReader.close();
            // This response will have Json Format String
            String response = stringBuilder.toString();

            Log.d(TAG, "onCreate: got response " + response);
            JsonArray jsonRegimenArray = new JsonParser().parseString(response).getAsJsonArray();
            //jsonRegimenStuff.add(response);
            //JSONObject jsonRegimenStuff = new JSONObject(response);
            Log.d(TAG, "onCreate: got JSON Array");
            //JsonParser
            //JSONArray jsonRegimenArray = jsonRegimenStuff.getJSONArray()

            //add local regimens
            for(int k = 0; k < jsonRegimenArray.size(); k++){
                //JsonParser parser2 = new JsonParser();
                JsonElement jsonElementReg = new JsonParser().parseString(jsonRegimenArray.getAsJsonArray().get(k).toString());
                Log.d(TAG, "onCreate: got JSON Element " + jsonElementReg.toString());
                JsonObject gson = new JsonParser().parse(jsonElementReg.getAsString()).getAsJsonObject();
                JSONObject jsonObject = new JSONObject(gson.toString());
                // jsonObject = new JSONObject(new jsonElementReg.toString());
                Log.d(TAG, "onCreate: got JSON Object " + k);
                String regimenName = jsonObject.getString("name");
                double duration = jsonObject.getDouble("duration");
                double offset = jsonObject.getDouble("offset");

                ArrayList<Pair<String, Pair<Double, Double>>> regimenWaves = new ArrayList<>();

                JSONArray jsonWaves = jsonObject.getJSONArray("waves");
                for (int l = 0; l < jsonWaves.length(); l++) {
                    JSONObject waveJson = jsonWaves.getJSONObject(l);
                    Pair<String, Pair<Double, Double>> wave;
                    if (waveJson.get("name").equals("random")) {
                        wave = Pair.create(waveJson.getString("name"), Pair.create(0.0, waveJson.getDouble("amplitude")));
                    } else {
                        wave = Pair.create(waveJson.getString("name"), Pair.create(waveJson.getDouble("frequency"), waveJson.getDouble("amplitude")));
                    }
                    regimenWaves.add(wave);
                }
                Log.d(TAG, "Regimen Name: " + regimenName);
                Log.d(TAG, "Regimen Waves: " + regimenWaves);
                Log.d(TAG, "Regimen Duration: " + duration);
                Log.d(TAG, "Regimen Offset: " + offset);
                Log.d(TAG, "Adding local regimen to list.");
                mLocalRegimenList.add(new Regimen(regimenName, regimenWaves, duration, offset));
            }
            /*
            //Java Object
            JavaObject javaObject =
                    new JavaObject(jsonObject.get("name").toString(),
                            jsonObject.get("enroll_no").toString(),
                            jsonObject.get("mobile").toString(),
                            jsonObject.get("address").toString(),
                            jsonObject.get("branch").toString());
            return javaObject;*/
        } catch (IOException | JSONException e) {
            Log.d(TAG, "onCreate: failed to obtain objects from JSON.");
            e.printStackTrace();
        }

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

        //combine lists
        mRegimenList.addAll(mServerRegimenList);
        mRegimenList.addAll(mLocalRegimenList);

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

        //travel to create regimen activity
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
            Log.d(TAG, "onItemClick: Current Regimen " + currentRegimen.commands);
            currentRegimenText.setText(currentRegimen.regimenName);
        }
        else {
            Log.d(TAG, "onItemClick: error clicking list");
            showToast("Error in clicking list.");
        }
    }

    public void btnDeleteRegimen(View view) {
        //String currentRegimenName = currentRegimen.regimenName;
        if (currentRegimen == null) {
            Log.d(TAG, "btnDeleteRegimen: No current regimen available to delete");
            showToast("No current regimen available to delete!");
        } else if (!mLocalRegimenList.contains(currentRegimen)) {
            Log.d(TAG, "btnDeleteRegimen: No current regimen available to delete in local list.");
            showToast("Regimen is not local. Cannot be removed by patient. Consult your doctor.");
        } else {
            mRegimenList.remove(currentRegimen);
            mLocalRegimenList.remove(currentRegimen);

            try {
                // Define the File Path and its Name
                File file = new File(getFilesDir(), FILE_NAME);
                //JsonParser parser = new JsonParser();
                JsonArray allRegs = new JsonArray();   // reading the file and creating a json array of it.
                //JsonArray allRegs = new JsonArray();
                for (int j = 0; j<mLocalRegimenList.size(); j++) {
                    JSONArray newRegimenWaves = new JSONArray();
                    JSONObject newRegimenJson = new JSONObject();
                    newRegimenJson.put("name", mLocalRegimenList.get(j).regimenName);
                    newRegimenJson.put("duration", mLocalRegimenList.get(j).duration);
                    newRegimenJson.put("offset", mLocalRegimenList.get(j).offset);

                    for (int i = 0; i < mLocalRegimenList.get(j).regimenWaves.size(); i++) {
                        JSONObject newWave = new JSONObject();
                        newWave.put("name", mLocalRegimenList.get(j).regimenWaves.get(i).first);
                        if (mLocalRegimenList.get(j).regimenWaves.get(i).first.equals("random")) {
                            newWave.put("frequency", 0.0);
                        } else {
                            newWave.put("frequency", mLocalRegimenList.get(j).regimenWaves.get(i).second.first);
                        }
                        newWave.put("amplitude", mLocalRegimenList.get(j).regimenWaves.get(i).second.second);
                        newRegimenWaves.put(newWave);
                    }
                    //place waves object
                    newRegimenJson.put("waves", newRegimenWaves);
                    Log.d(TAG, "btnDeleteRegimen: placed regimen in JSON");

                    //https://medium.com/@nayantala259/android-how-to-read-and-write-parse-data-from-json-file-226f821e957a
                    // Convert JsonObject to String Format
                    //String regimenString = newRegimenJson.toString();
                    allRegs.add(newRegimenJson.toString());   // adding your created object into the array
                }
                mRegimensListAdapter.notifyDataSetChanged();
                currentRegimens.setAdapter(mRegimensListAdapter);
                currentRegimenText.setText(noRegimenName);
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(allRegs.toString());
                bufferedWriter.close();
                Log.d(TAG, "btnDeleteRegimen: Directory is: " + getFilesDir().toString());
            } catch (JSONException | IOException e) {
                Log.d(TAG, "btnDeleteRegimen: failed to place objects in JSON.");
                e.printStackTrace();
            }
            //lastly make current regimen null
            if (mRegimenList.isEmpty()) {
                Log.d(TAG, "btnDeleteRegimen: No current regimen.");
                currentRegimenText.setText(noRegimenName);
            }
            currentRegimen = null;
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
    //https://stackoverflow.com/questions/19945411/how-can-i-parse-a-local-json-file-from-assets-folder-into-a-listview
    //https://developer.android.com/training/data-storage/app-specific
}