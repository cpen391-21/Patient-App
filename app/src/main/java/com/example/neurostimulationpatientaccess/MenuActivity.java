package com.example.neurostimulationpatientaccess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity {

    private Button email_doctor;
    private Button sign_out;
    private Button bluetooth_btn;
    private Button access_regimens;

    private FirebaseUser patient;
    private DatabaseReference reference;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        email_doctor = (Button) findViewById(R.id.email_doctor);
        sign_out = (Button) findViewById(R.id.signout_button);
        bluetooth_btn = (Button) findViewById(R.id.bluetooth_button);
        access_regimens = (Button) findViewById(R.id.regimens_button);

        patient = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Patients");
        userID = patient.getUid();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Patient patientProfile = snapshot.getValue(Patient.class);

                if (patientProfile != null) {
                    String doctor_email = patientProfile.doctorEmail;
                    email_doctor.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent email_intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", doctor_email, null));
                            startActivity(Intent.createChooser(email_intent, "Choose Email client: "));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MenuActivity.this, "Error occurred!", Toast.LENGTH_LONG).show();
            }
        });


        bluetooth_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), BluetoothMenuActivity.class));
                startActivity(new Intent(getApplicationContext(), BluetoothActivity.class));
            }
        });

        access_regimens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get activity
                startActivity(new Intent(getApplicationContext(), RegimensActivity.class));
            }
        });

        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();;
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}