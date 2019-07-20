package com.example.dell.trackit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int SEND_SMS_PERMISSION = 1;
    SmsManager smsManager = null;
    FirebaseDatabase trackItDatabase;
    DatabaseReference databaseReference;
    boolean smsSend = false;
    Location location;

    @Bind(R.id.bGetLocation)
    Button buttonGetLocation;
    @Bind(R.id.bShowOnMap)
    Button buttonShowOnMap;
    @Bind(R.id.tPositionDate)
    TextView textPositionDate;
    @Bind(R.id.tIsPositionUpdated)
    TextView textIsPositionUpdated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.bGetLocation)
    void sendSms() {
        String[] permissions = {Manifest.permission.SEND_SMS};
        String rationale = "Funkcjonowanie aplikacji jest niemożliwe bez udzielenia zgody na wysyłanie SMS.";
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle("Info")
                .setSettingsDialogTitle("Info");

        Permissions.check(this, permissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("667794502" /*"730225319"*/, null, "gps", null, null);
                smsSend = true;
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                Toast toast = Toast.makeText(context, "Uprawnienia nie udzielone", Toast.LENGTH_SHORT);
                toast.show();
                smsSend = true;
            }
        });
        if (smsSend) {
            Toast toast = Toast.makeText(this, "Sms wysłany", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @OnClick(R.id.bShowCurrentPosition)
    void getLocation() {
        trackItDatabase = FirebaseDatabase.getInstance();
        databaseReference = trackItDatabase.getReference().child("Tracker").child("devices").child("731536061");
        databaseReference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        location = new Location();
                                                        location.setLatitude(dataSnapshot.child("latitude").getValue(Double.class));
                                                        location.setLongitude(dataSnapshot.child("longitude").getValue(Double.class));
                                                        location.setUpdateTime(dataSnapshot.child("lastUpdated").getValue().toString());
                                                        location.setGpsStatus(dataSnapshot.child("validGps").getValue(Integer.class));
                                                        textPositionDate.setText(location.getUpdateTime());
                                                        if (location.getGpsStatus() == 1) {
                                                            textIsPositionUpdated.setTextColor(Color.parseColor("#228B22"));
                                                            textIsPositionUpdated.setText("Pozycja aktualna");
                                                            buttonShowOnMap.setEnabled(true);
                                                        } else {
                                                            Toast toast = Toast.makeText(MainActivity.this, "Brak aktualnej pozycji w bazie", Toast.LENGTH_SHORT);
                                                            toast.show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                }
        );
    }

    @OnClick(R.id.bShowOnMap)
    void showOnMap() {
        Intent intent = new Intent(this, LocationDetailsActivity.class);
        //intent.putExtra("Longitude", String.valueOf(location.getLongitude()));
        //intent.putExtra("Latitude", String.valueOf(location.getLatitude()));
        intent.putExtra("location", location);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSms();
                } else {
                    Toast toast = Toast.makeText(this, "Uprawnienia nie udzielone, brak możliwości wysłania sms!", Toast.LENGTH_SHORT);
                    toast.show();

                }
                return;
            }
        }
    }
}
