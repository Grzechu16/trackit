package com.example.dell.trackit;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocationDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Bind(R.id.tPositionDate)
    TextView textPositionDate;
    @Bind(R.id.tLongitude)
    TextView textLongitude;
    @Bind(R.id.tLatitude)
    TextView textLatitude;
    @Bind(R.id.tDetails)
    TextView textDetails;
    @Bind(R.id.b1)
    Button bB1;
    @Bind(R.id.b10)
    Button bB10;
    @Bind(R.id.b30)
    Button bB30;
    @Bind(R.id.b60)
    Button bB60;
    @Bind(R.id.bMapFull)
    Button bMapFull;

    private GoogleMap mMap;
    Location location;
    List<Address> addresses = null;
    FirebaseDatabase trackItDatabase;
    DatabaseReference databaseReference;
    LatLng latLng = null;
    SmsManager smsManager = null;
    String phoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        location = intent.getParcelableExtra("location");
        phoneNumber = intent.getStringExtra("phoneNumber");
        try {
            getAddressDetails(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
        textPositionDate.setText(Html.fromHtml("<b>Data lokalizacji: </b>" + location.getUpdateTime()));
        textLongitude.setText(Html.fromHtml("<b>Długość: </b>" + location.getLongitude()));
        textLatitude.setText(Html.fromHtml("<b>Szerokość: </b>" + location.getLatitude()));
        textDetails.setText(Html.fromHtml("<b>Adres: </b>" + location.getAddress()));

        refreshLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        latLng = new LatLng(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()));
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(latLng, 17.0f ) );
    }

    public void getAddressDetails(Location location) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        location.setCity(addresses.get(0).getLocality());
        location.setState(addresses.get(0).getAdminArea());
        location.setZip(addresses.get(0).getPostalCode());
        location.setCountry(addresses.get(0).getCountryName());
        location.setKnownName(addresses.get(0).getFeatureName());
        location.setAddress(addresses.get(0).getAddressLine(0));
    }

    void refreshLocation() {
        trackItDatabase = FirebaseDatabase.getInstance();
        databaseReference = trackItDatabase.getReference().child("TrackIt").child("devices").child("731536061");
        databaseReference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        location = new Location();
                                                        location.setLatitude(dataSnapshot.child("latitude").getValue(Double.class));
                                                        location.setLongitude(dataSnapshot.child("longitude").getValue(Double.class));
                                                        location.setUpdateTime(dataSnapshot.child("updateTime").getValue().toString());
                                                        location.setQueryTime(dataSnapshot.child("queryTime").getValue().toString());

                                                        latLng = new LatLng(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()));
                                                        mMap.addMarker(new MarkerOptions().position(latLng).title("Current location"));
                                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                                        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(latLng, 15.0f ) );

                                                        try {
                                                            getAddressDetails(location);
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                        textPositionDate.setText(Html.fromHtml("<b>Data lokalizacji: </b>" + location.getUpdateTime()));
                                                        textLongitude.setText(Html.fromHtml("<b>Długość: </b>" + location.getLongitude()));
                                                        textLatitude.setText(Html.fromHtml("<b>Szerokość: </b>" + location.getLatitude()));
                                                        textDetails.setText(Html.fromHtml("<b>Adres: </b>" + location.getAddress()));
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                }
        );
    }

    @OnClick(R.id.bMapFull)
    void showFullMap(){
        Intent fullMapIntent = new Intent(this, MapsActivity.class);
        fullMapIntent.putExtra("mapLocation", location);
        startActivity(fullMapIntent);
    }


    @OnClick({R.id.b1, R.id.b10, R.id.b30, R.id.b60})
    void sendSms(View view) {
        if(view==bB1){
            smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "gps1", null, null);
        } else if(view==bB10){
            smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "gps10", null, null);
        } else if(view==bB30){
            smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "gps30", null, null);
        } else if(view==bB60){
            smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "gps60", null, null);
        }

    }
}
