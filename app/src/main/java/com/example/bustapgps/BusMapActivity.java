package com.example.bustapgps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class BusMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button buttonLogout, buttonSetting;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Boolean currentLogout = false;
    private DatabaseReference assignedPassengerRef, assignedPassengerPickUpPointRef;
    private String busID,passengerID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_map);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        busID = auth.getCurrentUser().getUid();

        buttonLogout = (Button)findViewById(R.id.btnLogoutBus);
        buttonSetting = (Button)findViewById(R.id.btnSettingsBus);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLogout = true;
                disconnectBus();

                auth.signOut();
                logoutBus();
            }
        });
        getPassengerRequest();

    }

    private void getPassengerRequest() {
        assignedPassengerRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Bus").child(busID).child("PassengersID");
        assignedPassengerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    passengerID = snapshot.getValue().toString();

                    getPassengerPickUpPoint();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPassengerPickUpPoint() {
        assignedPassengerPickUpPointRef = FirebaseDatabase.getInstance().getReference().child("Passengers Requests").child(passengerID).child("l");

        assignedPassengerPickUpPointRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<Object> passengerLocationMap = (List<Object>)snapshot.getValue();

                    double locationLat = 0;
                    double locationLng = 0;

                    if(passengerLocationMap.get(0) != null) {
                        locationLat = Double.parseDouble(passengerLocationMap.get(0).toString());
                    }
                    if(passengerLocationMap.get(1) != null) {
                        locationLng = Double.parseDouble(passengerLocationMap.get(1).toString());
                    }
                    LatLng busLatLng = new LatLng(locationLat, locationLng);
                    mMap.addMarker(new MarkerOptions().position(busLatLng).title("Pick Up location"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext() != null){
            lastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference busAvailRef = FirebaseDatabase.getInstance().getReference().child("Bus is Available");
            GeoFire geoFireAvail = new GeoFire(busAvailRef);

            DatabaseReference busWorkingRef =  FirebaseDatabase.getInstance().getReference().child("Bus Working");
            GeoFire geoFireWorking = new GeoFire(busWorkingRef);

            switch (passengerID){
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvail.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvail.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
//
//        if(!currentLogout){
//            disconnectBus();
//        }

    }
    private void disconnectBus() {
//        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Bus is Available");
//
//        GeoFire geoFire = new GeoFire(databaseReference);
//        geoFire.removeLocation(userID);
    }
    private void logoutBus() {
        Intent intent = new Intent(BusMapActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}