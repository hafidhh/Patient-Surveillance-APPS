package com.hafidhh.geofencing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class LogsAdapter extends ArrayAdapter {

    private ArrayList<String> Waktu;
    private ArrayList<String> Koordinat;
    Context context;

    public LogsAdapter(@NonNull Context context, ArrayList<String> waktu, ArrayList<String> koordinat) {
        super(context, R.layout.list_koordinat, waktu);
        this.Waktu = waktu;
        this.Koordinat = koordinat;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.list_koordinat, null, true);
        TextView waktu = view.findViewById(R.id.tv_Waktu);
        TextView koordinat = view.findViewById(R.id.tv_Koordinat);

        waktu.setText(Waktu.get(position));
        koordinat.setText(Koordinat.get(position));
        return view;
    }
}
