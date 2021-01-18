package com.example.bustapgps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PassengerActivity extends AppCompatActivity {

    private Button passengerLogin;
    private Button passengerSignUp;
    private EditText emailPassenger;
    private EditText passPassenger;
    private ProgressDialog loadingBar;
    private EditText passengerName;
    private EditText passengerMobile;
    private EditText passengerConfirmPass;
    private TextView passengerLink;
    private TextView passengerStatus;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRefPassenger;
    private String passengerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        mAuth = FirebaseAuth.getInstance();

        passengerLogin = (Button)findViewById(R.id.btnLoginPassenger);
        passengerSignUp = (Button)findViewById(R.id.btnSignUpPassenger);
        passengerLink = (TextView)findViewById(R.id.txtViewCreateAccountPassenger);
        passengerStatus = (TextView)findViewById(R.id.textViewPassengerLogin);
        passengerName = (EditText)findViewById(R.id.txtNamePassenger);
        passengerMobile = (EditText)findViewById(R.id.txtNumberPassenger);
        passengerConfirmPass = (EditText)findViewById(R.id.txtConfirmPassPassenger);

        emailPassenger = (EditText)findViewById(R.id.txtEmailPassenger);
        passPassenger = (EditText)findViewById(R.id.txtPassPassenger);

        passengerName.setVisibility(View.INVISIBLE);
        passengerName.setEnabled(false);

        passengerMobile.setVisibility(View.INVISIBLE);
        passengerMobile.setEnabled(false);

        passengerConfirmPass.setVisibility(View.INVISIBLE);
        passengerConfirmPass.setEnabled(false);

        passengerSignUp.setVisibility(View.INVISIBLE);
        passengerSignUp.setEnabled(false);

        loadingBar = new ProgressDialog(this);

        passengerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passengerLogin.setVisibility(View.INVISIBLE);
                passengerLink.setVisibility(View.INVISIBLE);
                passengerStatus.setText("Passenger Sign Up");

                passengerName.setVisibility(View.VISIBLE);
                passengerName.setEnabled(true);

                passengerMobile.setVisibility(View.VISIBLE);
                passengerMobile.setEnabled(true);

                passengerConfirmPass.setVisibility(View.VISIBLE);
                passengerConfirmPass.setEnabled(true);

                passengerSignUp.setVisibility(View.VISIBLE);
                passengerSignUp.setEnabled(true);
            }
        });

        passengerSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailPassenger.getText().toString();
                String password = passPassenger.getText().toString();

                SignUpPassenger(email, password);
            }
        });

        passengerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailPassenger.getText().toString();
                String password = passPassenger.getText().toString();

                signinPassenger(email, password);
            }
        });
    }
    private void signinPassenger(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(PassengerActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(PassengerActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
        }else{

            loadingBar.setTitle("Passenger Login.");
            loadingBar.setMessage("Please wait.");
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener((task) -> {
                            if(task.isSuccessful()) {

                                Intent intentPassenger = new Intent(PassengerActivity.this, PassengerMapActivity.class);
                                startActivity(intentPassenger);

                                Toast.makeText(PassengerActivity.this, "Passenger account successfully logged in.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else {
                                Toast.makeText(PassengerActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                    });
        }
    }
    private void SignUpPassenger(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(PassengerActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(PassengerActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
        }else{

            loadingBar.setTitle("Passenger Sign Up.");
            loadingBar.setMessage("Please wait.");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                passengerID = mAuth.getCurrentUser().getUid();
                                dbRefPassenger = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(passengerID);
                                dbRefPassenger.setValue(true);

                                Intent intent = new Intent(PassengerActivity.this, PassengerMapActivity.class);
                                startActivity(intent);

                                Toast.makeText(PassengerActivity.this, "Passenger account created successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                Toast.makeText(PassengerActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
}