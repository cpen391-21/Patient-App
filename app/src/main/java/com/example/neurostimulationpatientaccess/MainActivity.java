package com.example.neurostimulationpatientaccess;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button signin_button;
    private Button signup_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signin_button = (Button) findViewById(R.id.signin_button);
        signup_button = (Button) findViewById(R.id.signup_button);

        signin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toSignIn();
            }
        });

        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toSignUp();
            }
        });
    }

    public void toSignIn() {
        Intent toSignIn = new Intent(this, SignInActivity.class);
        startActivity(toSignIn);
    }

    public void toSignUp() {
        Intent toSignUp = new Intent(this, SignUpActivity.class);
        startActivity(toSignUp);
    }
}