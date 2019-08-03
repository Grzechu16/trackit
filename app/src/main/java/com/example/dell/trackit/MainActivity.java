package com.example.dell.trackit;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
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
    Location location;
    BroadcastReceiver sentReceiver = null;
    BroadcastReceiver deliveryReceiver = null;
    PendingIntent sentIntent;
    PendingIntent deliveryIntent;

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
        getLocation();
        smsBroadcastRecInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        smsBroadcastRecInit();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        smsBroadcastRecInit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(sentReceiver);
            unregisterReceiver(deliveryReceiver);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(sentReceiver);
            unregisterReceiver(deliveryReceiver);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
                smsManager.sendTextMessage("730225319", null, "gps1", sentIntent, deliveryIntent);
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                Toast toast = Toast.makeText(context, "Uprawnienia nie udzielone", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        if (location.getGpsStatus() == 1) {
            textIsPositionUpdated.setVisibility(View.VISIBLE);
            buttonShowOnMap.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.bShowOnMap)
    void showOnMap() {
        Intent intent = new Intent(this, LocationDetailsActivity.class);
        intent.putExtra("location", location);
        startActivity(intent);
    }

    void getLocation() {
        trackItDatabase = FirebaseDatabase.getInstance();
        databaseReference = trackItDatabase.getReference().child("Tracker").child("devices").child("731536061");
        databaseReference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        location = new Location();
                                                        location.setLatitude(dataSnapshot.child("latitude").getValue(Double.class));
                                                        location.setLongitude(dataSnapshot.child("longitude").getValue(Double.class));
                                                        location.setUpdateTime(dataSnapshot.child("updateTime").getValue().toString());
                                                        location.setQueryTime(dataSnapshot.child("queryTime").getValue().toString());
                                                        location.setGpsStatus(dataSnapshot.child("validGps").getValue(Integer.class));
                                                        textPositionDate.setText(location.getUpdateTime());
                                                        if (location.getGpsStatus() != 1) {
                                                            Toast toast = Toast.makeText(MainActivity.this, "Brak aktualnej pozycji w bazie", Toast.LENGTH_SHORT);
                                                            toast.show();
                                                        } else {
                                                            textIsPositionUpdated.setTextColor(Color.parseColor("#228B22"));
                                                            textIsPositionUpdated.setText("Współrzędne dostępne");
                                                            buttonShowOnMap.setEnabled(true);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                }
        );
    }

    void smsBroadcastRecInit() {
        String smsSent = "SMS_SENT";
        String smsDelivered = "SMS_DELIVERED";
        sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(smsSent), 0);
        deliveryIntent = PendingIntent.getBroadcast(this, 0, new Intent(smsDelivered), 0);

        if ((sentReceiver == null) && deliveryReceiver == null) {
            sentReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS wysłany", Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(getBaseContext(), "Błąd wysyłania sms", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };
            deliveryReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS doręczony", Toast.LENGTH_SHORT).show();
                            break;

                        case Activity.RESULT_CANCELED:
                            Toast.makeText(getBaseContext(), "SMS niedoręczony", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };
        } else {
            registerReceiver(sentReceiver, new IntentFilter(smsSent));
            registerReceiver(deliveryReceiver, new IntentFilter(smsDelivered));
        }


    }
}
