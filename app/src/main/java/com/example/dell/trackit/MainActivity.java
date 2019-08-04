package com.example.dell.trackit;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    SmsManager smsManager = null;
    FirebaseDatabase trackItDatabase;
    DatabaseReference databaseReference;
    BroadcastReceiver sentReceiver = null;
    BroadcastReceiver deliveryReceiver = null;
    PendingIntent sentIntent;
    PendingIntent deliveryIntent;
    Location location;
    boolean gsmStatus = false;
    String phoneNumber = "";

    @Bind(R.id.bGetLocation)
    Button buttonGetLocation;
    @Bind(R.id.bShowOnMap)
    Button buttonShowOnMap;
    @Bind(R.id.tPositionDate)
    TextView textPositionDate;
    @Bind(R.id.tQueryDate)
    TextView textQueryDate;
    @Bind(R.id.tGpsConnection)
    TextView textGpsConnection;
    @Bind(R.id.tGsmConnection)
    TextView textGsmConnection;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.etPhoneNumber)
    EditText editTextNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        location = new Location();
        getLocation();
        smsBroadcastRecInit();
        phoneNumber = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("PhoneNumber", phoneNumber);
        editTextNumber.setText(phoneNumber);
    }

    @Override
    protected void onResume() {
        super.onResume();
        smsBroadcastRecInit();
        phoneNumber = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("PhoneNumber", phoneNumber);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        smsBroadcastRecInit();
        phoneNumber = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("PhoneNumber", phoneNumber);
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
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("PhoneNumber", editTextNumber.getText().toString()).apply();
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
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("PhoneNumber", editTextNumber.getText().toString()).apply();
    }

    @OnClick(R.id.bGetLocation)
    void sendSms() {
        progressBar.setVisibility(View.VISIBLE);
        if (checkNumber(editTextNumber.getText().toString())) {
            progressBar.setVisibility(View.VISIBLE);
            String[] permissions = {Manifest.permission.SEND_SMS};
            String rationale = "Funkcjonowanie aplikacji jest niemożliwe bez udzielenia zgody na wysyłanie SMS.";
            Permissions.Options options = new Permissions.Options()
                    .setRationaleDialogTitle("Uwaga")
                    .setSettingsDialogTitle("Uwaga");

            Permissions.check(this, permissions, rationale, options, new PermissionHandler() {
                @Override
                public void onGranted() {
                    smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(editTextNumber.getText().toString() /*730225319*/, null, "gps1", sentIntent, deliveryIntent);
                }

                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    Toast toast = Toast.makeText(context, "Uprawnienia nie udzielone", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        } else {
            editTextNumber.setError("Wprowadź poprawny numer sim");
        }
    }

    boolean checkNumber(String number) {
        Pattern pattern = Pattern.compile("[0-9]{9}");
        Matcher matcher = pattern.matcher(number);
        return (matcher.find() && matcher.group().equals(number));
    }

    @OnClick(R.id.bShowOnMap)
    void showOnMap() {
        Intent intent = new Intent(this, LocationDetailsActivity.class);
        intent.putExtra("location", location);
        intent.putExtra("phoneNumber", phoneNumber);
        startActivity(intent);
    }

    void getLocation() {
        trackItDatabase = FirebaseDatabase.getInstance();
        databaseReference = trackItDatabase.getReference().child("TrackIt").child("devices").child("731536061");
        databaseReference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                        location.setLatitude(dataSnapshot.child("latitude").getValue(Double.class));
                                                        location.setLongitude(dataSnapshot.child("longitude").getValue(Double.class));
                                                        location.setUpdateTime(dataSnapshot.child("updateTime").getValue().toString());
                                                        location.setQueryTime(dataSnapshot.child("queryTime").getValue().toString());
                                                        location.setGpsStatus(dataSnapshot.child("validGps").getValue(Integer.class));
                                                        textQueryDate.setText(location.getQueryTime());
                                                        textPositionDate.setText(location.getUpdateTime());
                                                        if ((gsmStatus == true) && ((textPositionDate.getText()).equals(textQueryDate.getText()))) {
                                                            textGpsConnection.setText("GPS status: OK");
                                                            textGpsConnection.setTextColor(Color.GREEN);
                                                        }
                                                        if (location.getGpsStatus() == 0) {
                                                            textGpsConnection.setText("GPS status: BRAK POŁĄCZENIA");
                                                            textGpsConnection.setTextColor(Color.BLACK);
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
                            progressBar.setVisibility(View.INVISIBLE);
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
                            textGsmConnection.setText("GSM status: OK");
                            textGsmConnection.setTextColor(Color.GREEN);
                            gsmStatus = true;
                            progressBar.setVisibility(View.INVISIBLE);
                            break;

                        case Activity.RESULT_CANCELED:
                            Toast.makeText(getBaseContext(), "SMS niedoręczony", Toast.LENGTH_SHORT).show();
                            textGsmConnection.setText("GSM status: BRAK POŁĄCZENIA");
                            textGsmConnection.setTextColor(Color.BLACK);
                            gsmStatus = false;
                            progressBar.setVisibility(View.INVISIBLE);
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
