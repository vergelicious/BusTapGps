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

public class BusActivity extends AppCompatActivity {

    private EditText busName;
    private EditText busMobile;
    private EditText busConfirmPass;
    private Button busLogin;
    private Button busSignUp;
    private TextView busLink;
    private TextView busStatus;
    private EditText emailBus;
    private EditText passBus;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRefBus;
    private String busID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);

        mAuth = FirebaseAuth.getInstance();

        busLogin = (Button)findViewById(R.id.btnLoginBus);
        busSignUp = (Button)findViewById(R.id.btnSignUpBus);
        busLink = (TextView)findViewById(R.id.textViewCreateAccountBus);
        busStatus = (TextView)findViewById(R.id.textViewBusLogin);
        busName = (EditText)findViewById(R.id.txtNameBus);
        busMobile = (EditText)findViewById(R.id.txtNumberBus);
        busConfirmPass = (EditText)findViewById(R.id.txtConfirmPassBus);

        emailBus = (EditText)findViewById(R.id.txtEmailBus);
        passBus = (EditText)findViewById(R.id.txtPassBus);

        busName.setVisibility(View.INVISIBLE);
        busName.setEnabled(false);

        busMobile.setVisibility(View.INVISIBLE);
        busMobile.setEnabled(false);

        busConfirmPass.setVisibility(View.INVISIBLE);
        busConfirmPass.setEnabled(false);

        busSignUp.setVisibility(View.INVISIBLE);
        busSignUp.setEnabled(false);

        loadingBar = new ProgressDialog(this);

        busLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busLogin.setVisibility(View.INVISIBLE);
                busLink.setVisibility(View.INVISIBLE);
                busStatus.setText("Bus Sign Up");

                busName.setVisibility(View.VISIBLE);
                busName.setEnabled(true);

                busMobile.setVisibility(View.VISIBLE);
                busMobile.setEnabled(true);

                busConfirmPass.setVisibility(View.VISIBLE);
                busConfirmPass.setEnabled(true);

                busSignUp.setVisibility(View.VISIBLE);
                busSignUp.setEnabled(true);
            }
        });
        busSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailBus.getText().toString();
                String password = passBus.getText().toString();

                SignUpPassenger(email, password);
            }
        });
        busLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailBus.getText().toString();
                String password = passBus.getText().toString();

                signinBus(email, password);
            }
        });
    }

    private void signinBus(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(BusActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(BusActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
        }else{

            loadingBar.setTitle("Bus Login.");
            loadingBar.setMessage("Please wait.");
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener((task) -> {
                            if(task.isSuccessful()) {

                                Intent intent = new Intent(BusActivity.this, BusMapActivity.class);
                                startActivity(intent);

                                Toast.makeText(BusActivity.this, "Bus account successfully logged in.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                Toast.makeText(BusActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        });
        }
    }

    private void SignUpPassenger(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(BusActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(BusActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
        }else{

            loadingBar.setTitle("Bus Sign Up.");
            loadingBar.setMessage("Please wait.");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                busID = mAuth.getCurrentUser().getUid();
                                dbRefBus = FirebaseDatabase.getInstance().getReference().child("Users").child("Bus").child(busID);
                                dbRefBus.setValue(true);

                                Intent intent = new Intent(BusActivity.this, BusMapActivity.class);
                                startActivity(intent);

                                Toast.makeText(BusActivity.this, "Bus account created successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                Toast.makeText(BusActivity.this, "An error occured. Please try again.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
}