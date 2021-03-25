package com.example.neurostimulationpatientaccess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView registerUser;
    private EditText editTextFullName, editTextEmail, editTextPassword, editTextDoctorEmail;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerUser = (Button) findViewById(R.id.signUp);
        registerUser.setOnClickListener(this);

        editTextFullName = (EditText) findViewById(R.id.name);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextDoctorEmail = (EditText) findViewById(R.id.doctor_email);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUp:
                registerUser();
                break;
        }
    }

    static void setTrueBoolean(boolean doctorExists) {
        doctorExists = true;
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

        db.collection("emails").document("patient_emails")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot pat = task.getResult();
                if (pat != null) {
                    Map<String, Object> patientEmails = pat.getData();
                    if (pat.getData().get(email) == null) {
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

                        db.collection("emails").document("doctor_emails")
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot doc = task.getResult();
                                if (doc != null) {
                                    Map<String, Object> doctorEmails = doc.getData();
                                    if (doc.getData().get(doctor_email) != null) {
                                        System.out.println("Doctor Exists!");
                                        //next condition
                                        progressBar.setVisibility(View.VISIBLE);
                                        mAuth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            Patient patient = new Patient(fullName, email, doctor_email);
                                                            FirebaseDatabase.getInstance().getReference("Patients")
                                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .setValue(patient).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(SignUpActivity.this, "Patient registration successful!", Toast.LENGTH_LONG).show();
                                                                        progressBar.setVisibility(View.GONE);
                                                                        backToStart();
                                                                    } else {
                                                                        Toast.makeText(SignUpActivity.this, "Patient registration failed!", Toast.LENGTH_LONG).show();
                                                                        progressBar.setVisibility(View.GONE);
                                                                    }
                                                                }
                                                            });

                                                            Map<String, Object> patientEmail = new HashMap<>();
                                                            patientEmail.put(email, email);
                                                            db.collection("emails").document("patient_emails").set(patientEmail, SetOptions.merge())
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            System.out.println("Writing successful!");
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            System.out.println("Writing failed!");
                                                                        }
                                                                    });
                                                        } else {
                                                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                            Toast.makeText(SignUpActivity.this, "Patient registration failed!", Toast.LENGTH_LONG).show();
                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    }
                                                });
                                    } else {
                                        System.out.println("Doctor does not exist." + task.getException());
                                        editTextDoctorEmail.setError("Doctor is not in database. Provide valid doctor email!");
                                        editTextDoctorEmail.requestFocus();
                                        return;
                                    }
                                } else {
                                    System.out.println("No Document" + task.getException());
                                }
                            }
                        });
                    } else {
                        editTextEmail.setError("Email is taken. Provide another email!");
                        editTextEmail.requestFocus();
                        return;
                    }
                } else {
                    System.out.println("No Document" + task.getException());
                }
            }
        });

        /*
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

        db.collection("emails").document("doctor_emails")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                if (doc != null) {
                    Map<String, Object> doctorEmails = doc.getData();
                    if (doc.getData().get(doctor_email) != null) {
                        System.out.println("Doctor Exists!");
                        //next condition
                        progressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Patient patient = new Patient(fullName, email, doctor_email);
                                            FirebaseDatabase.getInstance().getReference("Patients")
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .setValue(patient).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(SignUpActivity.this, "Patient registration successful!", Toast.LENGTH_LONG).show();
                                                        progressBar.setVisibility(View.GONE);
                                                        backToStart();
                                                    } else {
                                                        Toast.makeText(SignUpActivity.this, "Patient registration failed!", Toast.LENGTH_LONG).show();
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            });

                                            Map<String, Object> patientEmail = new HashMap<>();
                                            patientEmail.put(email, email);
                                            db.collection("emails").document("patient_emails").set(patientEmail, SetOptions.merge())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            System.out.println("Writing successful!");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            System.out.println("Writing failed!");
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            Toast.makeText(SignUpActivity.this, "Patient registration failed!", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    } else {
                        System.out.println("Doctor does not exist." + task.getException());
                        editTextDoctorEmail.setError("Doctor is not in database. Provide valid doctor email!");
                        editTextDoctorEmail.requestFocus();
                        return;
                    }
                } else {
                    System.out.println("No Document" + task.getException());
                }
            }
        });

        if (true) {
            editTextDoctorEmail.setError("Doctor is not in database. Provide valid doctor email!");
            editTextDoctorEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Patient patient = new Patient(fullName, email, doctor_email);
                            FirebaseDatabase.getInstance().getReference("Patients")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(patient).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "Patient registration successful!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                        backToStart();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Patient registration failed!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });

                            Map<String, Object> patientEmail = new HashMap<>();
                            patientEmail.put(email, email);
                            db.collection("emails").document("patient_emails").set(patientEmail, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            System.out.println("Writing successful!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println("Writing failed!");
                                        }
                                    });
                        } else {
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Toast.makeText(SignUpActivity.this, "Patient registration failed!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
         */
    }

    public void backToStart() {
        Intent backToStart = new Intent(this, MainActivity.class);
        startActivity(backToStart);
    }

}