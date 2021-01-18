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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class PassengerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private LatLng passengerPickUpPoint;
    private int radius = 1;
    private Boolean busFound = false;
    private String busFoundID;

    private Button btnSettings;
    private Button btnLogout;
    private Button btnBook;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String passengerID;
    private DatabaseReference passengerDbRef;
    private DatabaseReference busAvailDbRef;
    private DatabaseReference busRef;
    private DatabaseReference busLocationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_map);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        btnSettings = (Button)findViewById(R.id.btnSettingsPassenger);
        btnLogout = (Button)findViewById(R.id.btnLogoutPassenger);
        btnBook = (Button)findViewById(R.id.btnBook);
        passengerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        passengerDbRef = FirebaseDatabase.getInstance().getReference().child("Passengers Requests");
        busAvailDbRef = FirebaseDatabase.getInstance().getReference().child("Bus is Available");
        busLocationRef = FirebaseDatabase.getInstance().getReference().child("Bus Working");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                auth.signOut();
                logoutPassenger();
            }
        });
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoFire geoFire = new GeoFire(passengerDbRef);
                geoFire.setLocation(passengerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                passengerPickUpPoint = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(passengerPickUpPoint).title("Pick Me Up!"));

                btnBook.setText("Getting Near Bus...");
                getNearBus();
            }
        });
    }

    private void getNearBus() {
        GeoFire geoFire = new GeoFire(busAvailDbRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(passengerPickUpPoint.latitude, passengerPickUpPoint.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!busFound){
                    busFound = true;
                    busFoundID = key;

                    busRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Bus").child(busFoundID);
                    HashMap hashMap = new HashMap();
                    hashMap.put("PassengersID", passengerID);
                    busRef.updateChildren(hashMap);

                    getBusLocation();
                    btnBook.setText("Looking for bus");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!busFound){
                    radius = radius + 1;
                    getNearBus();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getBusLocation() {
        busLocationRef.child(busFoundID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DataSnapshot dataSnapshot = null;
                        if(dataSnapshot.exists()){
                            List<Object> busLocationMap = (List<Object>)dataSnapshot.getValue();
                            double locationLat = 0;
                            double locationLng = 0;
                            btnBook.setText("Bus Found");
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
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20));


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

    }
    private void logoutPassenger() {
        Intent intent = new Intent(PassengerMapActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}