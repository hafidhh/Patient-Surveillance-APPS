package com.hafidhh.geofencing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class Tracing extends AppCompatActivity {

    private DatabaseReference mDataBaseRef;

    public ListView mListView;
    public int pilihUserID, pilihTahun, pilihBulan, pilihTanggal;

    ArrayList<String> UserID = new ArrayList<>();
    ArrayList<String> Key = new ArrayList<>();
    public static ArrayList<String> Logs = new ArrayList<>();
    ArrayList<String> Tahun = new ArrayList<>();
    ArrayList<String> Bulan = new ArrayList<>();
    ArrayList<String> Tanggal = new ArrayList<>();
    ArrayList<String> Jam = new ArrayList<>();
    ArrayList<String> MacAddress = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracing);

        UserID.add("UserID");
        Tahun.add("Tahun");
        Bulan.add("Bulan");
        Tanggal.add("Tanggal");

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mDataBaseRef = mDatabase.getReference();

////dari sini
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
        Spinner spinner4 = (Spinner) findViewById(R.id.spinner4);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.tracing);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (pilihTanggal != 0) {
                    Tracing.this.startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("MapsTracingVal",true).putExtra("Logs",Logs).putExtra("Jam", Jam));
                }
            }
        });


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("MapsTracingVal", false));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.tracing:
                        return true;
                }
                return false;
            }
        });

        // Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, UserID);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);

        // Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Tahun);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner2.setAdapter(spinnerArrayAdapter2);

        // Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter3 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Bulan);
        spinnerArrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner3.setAdapter(spinnerArrayAdapter3);

        // Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter4 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Tanggal);
        spinnerArrayAdapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner4.setAdapter(spinnerArrayAdapter4);

        mDataBaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserID.clear();
                UserID.add("UserID");
                Key.clear();
                Key.add("Key");

                for (DataSnapshot ds : snapshot.child("ActiveUsers").getChildren()) {
                    if (ds.exists()) {
                        Boolean value = (Boolean) ds.getValue();
                        if (value == true)
                            MacAddress.add(ds.getKey());
                        Log.d("TAG", "onDataChange: "+MacAddress);
                    }
                }

                for (int i = 0; i < MacAddress.size(); i++) {
                    String nik = Objects.requireNonNull(snapshot.child("Users").child(MacAddress.get(i)).child("nik").getValue()).toString();
                    String key = snapshot.child("Users").child(MacAddress.get(i)).getKey();
                    UserID.add(nik);
                    Log.d("TAG", "onDataChange: "+UserID);
                    Key.add(key);
                }

//                for (DataSnapshot ds : snapshot.child("Users").getChildren()) {
//                    if (ds.exists()) {
//                        String nik = Objects.requireNonNull(ds.child("nik").getValue()).toString();
//                        String key = ds.getKey();
//                        UserID.add(nik);
//                        Log.d("TAG", "onDataChange: "+UserID);
//                        Key.add(key);
//                    }
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pilihUserID = UserID.indexOf(UserID.get(position));
                Log.d("TAG", "onItemSelected: "+ UserID.indexOf(UserID.get(position)));
                if (position!=0)
                    spin2(Key.get(pilihUserID));
                if (position == 0){
                    Tahun.clear();
                    Tahun.add("Tahun");
                }
                spinner2.setSelection(0);
//                if (position != position){
//                    spinner2.setSelection(0);
//                }
                Log.d("TAG", "onItemSelected: "+ position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pilihTahun = Tahun.indexOf(Tahun.get(position));
                Log.d("TAG", "onItemSelected: "+ Tahun.indexOf(Tahun.get(position)));
                if (position!=0)
                    spin3(Key.get(pilihUserID), Tahun.get(pilihTahun));
                if (position==0) {
                    Bulan.clear();
                    Bulan.add("Bulan");
                }
                spinner3.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pilihBulan = Bulan.indexOf(Bulan.get(position));
                Log.d("TAG", "onItemSelected: "+ Bulan.indexOf(Bulan.get(position)));
                if (position!=0)
                    spin4(Key.get(pilihUserID), Tahun.get(pilihTahun), Bulan.get(pilihBulan));
                if (position==0) {
                    Tanggal.clear();
                    Tanggal.add("Tanggal");
                }
                spinner4.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pilihTanggal = Tanggal.indexOf(Tanggal.get(position));
                Log.d("TAG", "onItemSelected: "+ Tanggal.indexOf(Tanggal.get(position)));
                if (position!=0)
                    spin5(Key.get(pilihUserID), Tahun.get(pilihTahun), Bulan.get(pilihBulan), Tanggal.get(pilihTanggal));
                if (position==0) {
                    Logs.clear();
                    Jam.clear();
                    mListView = findViewById(R.id.lv_Logs);
                    LogsAdapter logsAdapter = new LogsAdapter(Tracing.this, Jam, Logs);
                    mListView.setAdapter(logsAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    void spin2(String Key) {
        mDataBaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Tahun.clear();
                Tahun.add("Tahun");
                for (DataSnapshot ds : snapshot.child("Users").child(Key).child("logs").getChildren()) {
                    if (ds.exists()) {
                        String tahun = ds.getKey();
                        Tahun.add(tahun);
                        Log.d("TAG", "onDataChange: "+Tahun);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void spin3 (String Key, String Tahun) {
        mDataBaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Bulan.clear();
                Bulan.add("Bulan");
                for (DataSnapshot ds : snapshot.child("Users").child(Key).child("logs").child(Tahun).getChildren()) {
                    if (ds.exists()) {
                        String bulan = ds.getKey();
                        Bulan.add(bulan);
                        Log.d("TAG", "onDataChange: " + Bulan);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void spin4 (String Key, String Tahun, String Bulan) {
        mDataBaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Tanggal.clear();
                Tanggal.add("Tanggal");
                for (DataSnapshot ds : snapshot.child("Users").child(Key).child("logs").child(Tahun).child(Bulan).getChildren()) {
                    if (ds.exists()) {
                        String tanggal = ds.getKey();
                        Tanggal.add(tanggal);
                        Log.d("TAG", "onDataChange: " + Tanggal);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void spin5 (String Key, String Tahun, String Bulan, String Tanggal) {
        mDataBaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Logs.clear();
                Jam.clear();
                for (DataSnapshot ds : snapshot.child("Users").child(Key).child("logs").child(Tahun).child(Bulan).child(Tanggal).getChildren()) {
                    if (ds.exists()) {
                        String jam = ds.getKey();
                        String lokasi = Objects.requireNonNull(ds.getValue()).toString();
                        Jam.add(jam);
                        Logs.add(lokasi);
                    }
                }
                Log.d("TAG", "onDataChange: " + Jam);
                Log.d("TAG", "onDataChange: " + Logs);
                mListView = findViewById(R.id.lv_Logs);
                LogsAdapter logsAdapter = new LogsAdapter(Tracing.this, Jam, Logs);
                mListView.setAdapter(logsAdapter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Uri gmmIntentUri = Uri.parse("geo:"+Logs.get(position)+"?q="+Logs.get(position));
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                startActivity(mapIntent);
                            }
                        }, 1000);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}