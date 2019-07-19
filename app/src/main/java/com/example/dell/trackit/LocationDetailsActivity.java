package com.example.dell.trackit;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LocationDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Bind(R.id.tPositionDate)
    TextView textPositionDate;
    @Bind(R.id.tLongitude)
    TextView textLongitude;
    @Bind(R.id.tLatitude)
    TextView textLatitude;
    @Bind(R.id.tDetails)
    TextView textDetails;

    private GoogleMap mMap;
    Location location;
    List<Address> addresses = null;

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
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        LatLng latLng = new LatLng(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()));
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

}
