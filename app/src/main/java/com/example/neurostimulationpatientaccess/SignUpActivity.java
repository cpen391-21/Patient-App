package com.example.neurostimulationpatientaccess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView registerUser;
    private EditText editTextFullName, editTextEmail, editTextPassword, editTextDoctorEmail;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        registerUser = (Button) findViewById(R.id.signUp);
        registerUser.setOnClickListener(this);

        editTextFullName = (EditText) findViewById(R.id.name);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextDoctorEmail = (EditText) findViewById(R.id.doctor_email);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUp:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String doctor_email = editTextDoctorEmail.getText().toString().trim();

        if (fullName.isEmpty()) {
            editTextFullName.setError("Please enter your name!");
            editTextFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Please enter your email!");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email is invalid. Provide valid email!");
            editTextEmail.requestFocus();
            return;
        }

        if (doctor_email.isEmpty()) {
            editTextDoctorEmail.setError("Please enter your doctor's email!");
            editTextDoctorEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(doctor_email).matches()) {
            editTextDoctorEmail.setError("Doctor email is invalid. Provide valid email!");
            editTextDoctorEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Please enter a password!");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be longer than 6 characters!");
            editTextPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Patient patient = new Patient(fullName, email, doctor_email);

                        }
                    }
                });
    }
}