package com.example.neurostimulationpatientaccess;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.NumberUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.common.util.NumberUtils.*;

public class CreateRegimenActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private static final String TAG = "CreateRegimenActivity";

    private Button btnAddRegimen;
    private TextView wave1Name, wave2Name, wave3Name;
    private Spinner wave1Spinner, wave2Spinner, wave3Spinner;
    private EditText textRegimenName, textWave1Freq, textWave1Amp, textWave2Freq, textWave2Amp, textWave3Freq, textWave3Amp;
    private EditText textDuration, textOffset;
    private ProgressBar progressBar;

    public Regimen newRegimen;
    public String regimenName;
    public String wave1Freq, wave1Amp, wave2Freq, wave2Amp, wave3Freq, wave3Amp;
    public String wave1Category, wave2Category, wave3Category;
    public String duration, offset;
    public Pair wave1Info, wave2Info, wave3Info;
    public Pair wave1FreqAmp, wave2FreqAmp, wave3FreqAmp;

    public ArrayList<Pair<String, Pair<Double, Double>>> regimenWaves;

    private JSONObject newRegimenJson;
    private String FILE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_regimen);

        FILE_NAME = "local_regimens.json";

        btnAddRegimen = (Button) findViewById(R.id.btnAddRegimen);

        wave1Name = (TextView) findViewById(R.id.wave1_name);
        wave2Name = (TextView) findViewById(R.id.wave2_name);
        wave3Name = (TextView) findViewById(R.id.wave3_name);

        textRegimenName = (EditText) findViewById(R.id.regimen_name);

        textWave1Freq = (EditText) findViewById(R.id.wave1_frequency);
        textWave2Freq = (EditText) findViewById(R.id.wave2_frequency);
        textWave3Freq = (EditText) findViewById(R.id.wave3_frequency);

        textWave1Amp = (EditText) findViewById(R.id.wave1_amplitude);
        textWave2Amp = (EditText) findViewById(R.id.wave2_amplitude);
        textWave3Amp = (EditText) findViewById(R.id.wave3_amplitude);

        textDuration = (EditText) findViewById(R.id.regimen_duration);
        textOffset = (EditText) findViewById(R.id.regimen_offset);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        wave1Spinner = (Spinner) findViewById(R.id.spinner_wave1);
        wave2Spinner = (Spinner) findViewById(R.id.spinner_wave2);
        wave3Spinner = (Spinner) findViewById(R.id.spinner_wave3);

        // Spinner click listener
        wave1Spinner.setOnItemSelectedListener(this);
        wave2Spinner.setOnItemSelectedListener(this);
        wave3Spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<>();
        categories.add("square");
        categories.add("sine");
        categories.add("triangle");
        categories.add("random");
        categories.add("offset");

        //https://www.tutorialspoint.com/android/android_spinner_control.htm
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        wave1Spinner.setAdapter(dataAdapter);
        wave2Spinner.setAdapter(dataAdapter);
        wave3Spinner.setAdapter(dataAdapter);
        //newRegimen = new Regimen("", new ArrayList<Pair<String, Pair<Double, Double>>>(3), 0);
        newRegimenJson = new JSONObject();
    }

    public void addRegimen(View view) {
        //Log.d(TAG, "addRegimen: " + newRegimen.commands);
        /*
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String doctor_email = editTextDoctorEmail.getText().toString().trim();
         */
        regimenName = textRegimenName.getText().toString().trim();
        wave1Freq = textWave1Freq.getText().toString().trim();
        wave2Freq = textWave2Freq.getText().toString().trim();
        wave3Freq = textWave3Freq.getText().toString().trim();
        wave1Amp = textWave1Amp.getText().toString().trim();
        wave2Amp = textWave2Amp.getText().toString().trim();
        wave3Amp = textWave3Amp.getText().toString().trim();
        duration = textDuration.getText().toString().trim();
        offset = textOffset.getText().toString().trim();
        /*
        if (fullName.isEmpty()) {
            editTextFullName.setError("Please enter your name!");
            editTextFullName.requestFocus();
            return;
        }*/
        if (wave1Category.isEmpty()) {
           wave1Spinner.requestFocus();
            showToast("Please select Wave 1 category!");
            return;
        }
        if (wave2Category.isEmpty()) {
            wave2Spinner.requestFocus();
            showToast("Please select Wave 2 category!");
            return;
        }
        if (wave3Category.isEmpty()) {
            wave3Spinner.requestFocus();
            return;
        }
        if (regimenName.isEmpty()) {
            textRegimenName.setError("Please enter regimen name!");
            textRegimenName.requestFocus();
            return;
        }
        if (wave1Freq.isEmpty()) {
            textWave1Freq.setError("Please enter Wave 1 frequency!");
            textWave1Freq.requestFocus();
            return;
        } else {
            //https://stackabuse.com/java-check-if-string-is-a-number/
            if(!isNumeric(wave1Freq)) {
                textWave1Freq.setError("Please enter a number!");
                textWave1Freq.requestFocus();
                return;
            } else if (Double.parseDouble(wave1Freq) < 0) {
                textWave1Freq.setError("Please enter a positive number!");
                textWave1Freq.requestFocus();
                return;
            }
        }
        if (wave2Freq.isEmpty()) {
            textWave2Freq.setError("Please enter Wave 2 frequency!");
            textWave2Freq.requestFocus();
            return;
        } else {
            //https://stackabuse.com/java-check-if-string-is-a-number/
            if(!isNumeric(wave2Freq)) {
                textWave2Freq.setError("Please enter a number!");
                textWave2Freq.requestFocus();
                return;
            } else if (Double.parseDouble(wave2Freq) < 0) {
                textWave2Freq.setError("Please enter a positive number!");
                textWave2Freq.requestFocus();
                return;
            }
        }
        if (wave3Freq.isEmpty()) {
            textWave3Freq.setError("Please enter Wave 3 frequency!");
            textWave3Freq.requestFocus();
            return;
        } else {
            //https://stackabuse.com/java-check-if-string-is-a-number/
            if(!isNumeric(wave3Freq)) {
                textWave3Freq.setError("Please enter a number!");
                textWave3Freq.requestFocus();
                return;
            } else if (Double.parseDouble(wave3Freq) < 0) {
                textWave3Freq.setError("Please enter a positive number!");
                textWave3Freq.requestFocus();
                return;
            }
        }
        if (wave1Amp.isEmpty()) {
            textWave1Amp.setError("Please enter Wave 1 amplitude!");
            textWave1Amp.requestFocus();
            return;
        } else {
            //https://stackabuse.com/java-check-if-string-is-a-number/
            if(!isNumeric(wave1Amp)) {
                textWave1Amp.setError("Please enter a number!");
                textWave1Amp.requestFocus();
                return;
            } else if (Double.parseDouble(wave1Amp) < 0) {
                textWave1Amp.setError("Please enter a positive number!");
                textWave1Amp.requestFocus();
                return;
            }
        }
        if (wave2Amp.isEmpty()) {
            textWave2Amp.setError("Please enter Wave 2 amplitude!");
            textWave2Amp.requestFocus();
            return;
        } else {
            //https://stackabuse.com/java-check-if-string-is-a-number/
            if(!isNumeric(wave2Amp)) {
                textWave2Amp.setError("Please enter a number!");
                textWave2Amp.requestFocus();
                return;
            } else if (Double.parseDouble(wave2Amp) < 0) {
                textWave2Amp.setError("Please enter a positive number!");
                textWave2Amp.requestFocus();
                return;
            }
        }
        if (wave3Amp.isEmpty()) {
            textWave3Amp.setError("Please enter Wave 3 amplitude!");
            textWave3Amp.requestFocus();
            return;
        } else {
            //https://stackabuse.com/java-check-if-string-is-a-number/
            if(!isNumeric(wave3Amp)) {
                textWave3Amp.setError("Please enter a number!");
                textWave3Amp.requestFocus();
                return;
            } else if (Double.parseDouble(wave3Amp) < 0) {
                textWave3Amp.setError("Please enter a positive number!");
                textWave3Amp.requestFocus();
                return;
            }
        }
        if (duration.isEmpty()) {
            textDuration.setError("Please enter duration!");
            textDuration.requestFocus();
            return;
        } else {
            //https://stackabuse.com/java-check-if-string-is-a-number/
            if(!isNumeric(duration)) {
                textDuration.setError("Please enter a number!");
                textDuration.requestFocus();
                return;
            } else if (Double.parseDouble(duration) < 0) {
                textDuration.setError("Please enter a positive number!");
                textDuration.requestFocus();
                return;
            }
        }
        if (offset.isEmpty()) {
            textOffset.setError("Please enter offset!");
            textOffset.requestFocus();
            return;
        } else {
            //https://stackabuse.com/java-check-if-string-is-a-number/
            if(!isNumeric(offset)) {
                textOffset.setError("Please enter a number!");
                textOffset.requestFocus();
                return;
            } else if (Double.parseDouble(offset) < 0) {
                textOffset.setError("Please enter a positive number!");
                textOffset.requestFocus();
                return;
            }
        }

        if (Double.parseDouble(wave1Amp) + Double.parseDouble(wave2Amp) + Double.parseDouble(wave3Amp) + Double.parseDouble(offset) > 1) {
            textOffset.requestFocus();
            textWave1Amp.requestFocus();
            textWave2Amp.requestFocus();
            textWave3Amp.requestFocus();
            showToast("Amplitudes and offset must not sum to more than 1.");
        }

        wave1FreqAmp = new Pair(Double.parseDouble(wave1Freq), Double.parseDouble(wave1Amp));
        wave2FreqAmp = new Pair(Double.parseDouble(wave2Freq), Double.parseDouble(wave2Amp));
        wave3FreqAmp = new Pair(Double.parseDouble(wave3Freq), Double.parseDouble(wave3Amp));
        wave1Info = new Pair(wave1Category, wave1FreqAmp);
        wave2Info = new Pair(wave2Category, wave2FreqAmp);
        wave3Info = new Pair(wave3Category, wave3FreqAmp);

        regimenWaves = new ArrayList<>();
        regimenWaves.add(wave1Info);
        regimenWaves.add(wave2Info);
        regimenWaves.add(wave3Info);

        newRegimen = new Regimen(regimenName, regimenWaves, Double.parseDouble(duration), Double.parseDouble(offset));
        Log.d(TAG, "addRegimen: added newRegimen \n" + newRegimen.commands);
        //save to backend
        JSONArray newRegimenWaves = new JSONArray();
        try {
            newRegimenJson.put("name", newRegimen.regimenName);
            newRegimenJson.put("duration", newRegimen.duration);
            newRegimenJson.put("offset", newRegimen.offset);

            for (int i = 0; i < newRegimen.regimenWaves.size(); i++) {
                JSONObject newWave = new JSONObject();
                newWave.put("name", newRegimen.regimenWaves.get(i).first);
                if (newRegimen.regimenWaves.get(i).first.equals("random")) {
                    newWave.put("frequency", 0.0);
                } else {
                    newWave.put("frequency", newRegimen.regimenWaves.get(i).second.first);
                }
                newWave.put("amplitude", newRegimen.regimenWaves.get(i).second.second);
                newRegimenWaves.put(newWave);
            }
            //place waves object
            newRegimenJson.put("waves", newRegimenWaves);
            Log.d(TAG, "addRegimen: placed regimen in JSON");

            //https://medium.com/@nayantala259/android-how-to-read-and-write-parse-data-from-json-file-226f821e957a
            // Convert JsonObject to String Format
            //String regimenString = newRegimenJson.toString();
            // Define the File Path and its Name
            File file = new File(getFilesDir(), FILE_NAME);
            JsonParser parser = new JsonParser();
            JsonArray allRegs = (JsonArray) parser.parseReader(new FileReader(file));   // reading the file and creating a json array of it.
            //JsonArray allRegs = new JsonArray();
            allRegs.add(newRegimenJson.toString());   // adding your created object into the array
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(allRegs.toString());
            bufferedWriter.close();
            Log.d(TAG, "addRegimen: Directory is: " + getFilesDir().toString());
            showToast("Regimen Added!");
            //go back to class
            startActivity(new Intent(getApplicationContext(), RegimensActivity.class));
        } catch (JSONException | IOException e) {
            Log.d(TAG, "addRegimen: failed to place objects in JSON.");
            showToast("Adding regimen failed!");
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemSelected: selected spinner item");
        if (adapterView.getId() == R.id.spinner_wave1) {
            Log.d(TAG, "onItemSelected: selected wave1 spinner item");
            wave1Category = adapterView.getItemAtPosition(i).toString();
        } else if (adapterView.getId() == R.id.spinner_wave2) {
            Log.d(TAG, "onItemSelected: selected wave2 spinner item");
            wave2Category = adapterView.getItemAtPosition(i).toString();
        } else if (adapterView.getId() == R.id.spinner_wave3) {
            Log.d(TAG, "onItemSelected: selected wave3 spinner item");
            wave3Category = adapterView.getItemAtPosition(i).toString();
        } else {
            Log.d(TAG, "onItemSelected: this should never be reached");
        }
        // On selecting a spinner item
        //String item = adapterView.getItemAtPosition(i).toString();

        // Showing selected spinner item
        //Toast.makeText(adapterView.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //Do nothing.
    }

    public void showToast(String message) {
        Toast.makeText(CreateRegimenActivity.this, message, Toast.LENGTH_LONG).show();
    }

    public static boolean isNumeric(String string) {
        //https://stackabuse.com/java-check-if-string-is-a-number/
        double doubleValue;

        Log.d(TAG, "isNumeric: Parsing string: " + string);

        if(string == null || string.equals("")) {
            Log.d(TAG, "isNumeric: String cannot be parsed, it is null or empty.");
            return false;
        }

        try {
            doubleValue = Double.parseDouble(string);
            Log.d(TAG, "isNumeric: Input String can be parsed to Double");
            return true;
        } catch (NumberFormatException e) {
            Log.d(TAG, "isNumeric: Input String cannot be parsed to Double.");
        }
        return false;
    }
}