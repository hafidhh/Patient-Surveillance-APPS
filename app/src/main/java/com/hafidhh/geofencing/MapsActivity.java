package com.hafidhh.geofencing;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.hafidhh.geofencing.databinding.ActivityMapsBinding;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private ActivityMapsBinding binding;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDataBaseRef;

    private float GEOFENCE_RADIUS = 20;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";

    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    ArrayList<String> MacAddress = new ArrayList<>();

    Marker marker = null;
    Circle circle;
    final Double[] hlat = {-5.377196567687361};
    final Double[] hlon = {105.05275188101263};

    Boolean MapsTracingVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance();
        mDataBaseRef = mDatabase.getReference();

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.tracing:
                        startActivity(new Intent(getApplicationContext(), Tracing.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        if (MapsTracingVal == true) {
                            startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("MapsTracingVal", false));
                            overridePendingTransition(0,0);
                        }
                        return true;
                }
                return false;
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        //Notifikasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    //MAPS
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mDatabase = FirebaseDatabase.getInstance();
        mDataBaseRef = mDatabase.getReference();

        enableUserLocation();

        Intent intent = getIntent();
        MapsTracingVal = intent.getBooleanExtra("MapsTracingVal", false);

        if (MapsTracingVal == true) {
            mMap.clear();
            ArrayList<String> Logs = (ArrayList<String>) getIntent().getSerializableExtra("Logs");
            ArrayList<String> Jam = (ArrayList<String>) getIntent().getSerializableExtra("Jam");
            for (int i = 0; i < Logs.size(); i++) {
                String pos = Logs.get(i);
                String jam = Jam.get(i);
                String[] Splitterpos = pos.split(",");
                double lat = Double.parseDouble(Splitterpos[0]);
                double lon = Double.parseDouble(Splitterpos[1]);
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(jam));
            }
            String pos = Logs.get(Logs.size()-1);
            String[] Splitterpos = pos.split(",");
            double lat = Double.parseDouble(Splitterpos[0]);
            double lon = Double.parseDouble(Splitterpos[1]);
            LatLng Lokasikarantina1 = new LatLng(lat, lon);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Lokasikarantina1, 20));
        }
        if (MapsTracingVal == false){
            //Firebase
            mDataBaseRef.addValueEventListener(new ValueEventListener() {
                String title = "";

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    MacAddress.clear();

                    Map<String, Object> geofencedata = (Map<String, Object>) snapshot.getValue();
                    String geofence = (String) geofencedata.get("geofence");
                    String[] Splittergeofence = geofence.split(",");
                    Double lat = Double.parseDouble(Splittergeofence[0]);
                    Double lon = Double.parseDouble(Splittergeofence[1]);
                    long GEOFENCE_RADIUS = Long.parseLong(Splittergeofence[2]);

                    //Move Camera to ...
                    if (!hlat[0].equals(lat) || !hlon[0].equals(lon)){
                        if (circle != null) {
                            circle.remove();
                        }
                        LatLng Lokasikarantina1 = new LatLng(lat, lon);
                        addCircle(Lokasikarantina1, GEOFENCE_RADIUS);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Lokasikarantina1, 20));
                        hlat[0] = lat;
                        hlon[0] = lon;
                    }

                    for (DataSnapshot ds : snapshot.child("ActiveUsers").getChildren()) {
                        if (ds.exists()) {
                            Boolean value = (Boolean) ds.getValue();
                            if (value == true)
                                MacAddress.add(ds.getKey());
                            Log.d(TAG, "onDataChange: "+MacAddress);
                        }
                    }

                    for (int i = 0; i < MacAddress.size(); i++) {

                        if (i==0) {
                            if (marker != null) {
                                marker.remove();
                            }
                        }

                        String message = "";
                        String message2 = "";
                        String message3 = "";
                        String fb = snapshot.child("Users").child(MacAddress.get(i)).child("latitude, longitude").getValue().toString();
                        String name = snapshot.child("Users").child(MacAddress.get(i)).child("name").getValue().toString();
                        String nik = snapshot.child("Users").child(MacAddress.get(i)).child("nik").getValue().toString();
                        Boolean gtrig = (Boolean) snapshot.child("Users").child(MacAddress.get(i)).child("gtrig").getValue();
                        Boolean htrig = (Boolean) snapshot.child("Users").child(MacAddress.get(i)).child("htrig").getValue();
                        int battery = Integer.parseInt(snapshot.child("Users").child(MacAddress.get(i)).child("battery").getValue().toString());

                        String[] Splitterfb = fb.split(",");

                        double olat = Double.parseDouble(Splitterfb[0]);
                        double olon = Double.parseDouble(Splitterfb[1]);

                        LatLng koordinat = new LatLng(olat, olon);
                        marker = mMap.addMarker(new MarkerOptions().position(koordinat).title(name));
                        Log.d(TAG, "onDataChange: "+koordinat.toString());

                        //Notifikasi
                        if (gtrig == true || htrig == true || battery < 16) {
                            Log.d(TAG, "onDataChange: Notifitkasi");

                            if (gtrig == true) {
                                title = nik;
                                message3 = "berada di luar batas ";
                            }

                            if (htrig == true) {
                                title = nik;
                                message = "device dilepas ";
                            }

                            if (battery < 16) {
                                title = nik;
                                message2 = "baterai lemah ";
                            }

                            notif(nik ,title, message, message2, message3, i);
//                            grupNotif(title, message, message2, message3);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG,"Failed to read value.", error.toException());
                }
            });
        }
    }

    //GPS
    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //ASk for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission ...
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    //GPS Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Have permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
            } else {
                //dont have

            }
        }
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addGeofence(LatLng latLng, float radius) {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getgeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorSting(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    }
                });
    }

    //Add circle on Maps (Geofence)
    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255,0, 0));
        circleOptions.strokeWidth(4);
        circle = mMap.addCircle(circleOptions);
    }

    void notif(String nik,String Title, String Message, String Message2, String Message3, int i) {
        Notification notification1 = new NotificationCompat.Builder(MapsActivity.this, "My Notification")
                .setSmallIcon(R.drawable.ic_geofence)
                .setStyle(new NotificationCompat.InboxStyle()
                    .setBigContentTitle(Title)
                    .addLine(Message)
                    .addLine(Message2)
                    .addLine(Message3)
                    )
                .setContentTitle(Title)
                .setContentText(Message+Message2+Message3)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setGroup("notification_group")
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        notificationManager.notify(i, notification1);
    }

    void grupNotif(String Title, String Message, String Message2, String Message3) {
        Notification summeryNotification = new NotificationCompat.Builder(MapsActivity.this, "My Notification")
                .setSmallIcon(R.drawable.ic_geofence)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(Message)
                        .addLine(Message2)
                        .addLine(Message3)
                        .setBigContentTitle(Title)
                        )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup("notification_group")
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .setGroupSummary(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        notificationManager.notify(0, summeryNotification);
    }
}