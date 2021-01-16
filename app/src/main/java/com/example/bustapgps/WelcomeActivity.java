package com.example.bustapgps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    private Button busButton;
    private Button passengerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        passengerButton = (Button)findViewById(R.id.btnPassenger);
        busButton = (Button)findViewById(R.id.btnBus);

        passengerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        });
        busButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, BusActivity.class);
                startActivity(intent);
            }
        });
    }

}