package com.example.mapasapigoogle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private static final String TAG = "CustomInfoWindowAdapter";
    private LayoutInflater inflater;

    public CustomInfoWindowAdapter(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public View getInfoContents(final Marker m) {
        //Carga layout personalizado.
        View v = inflater.inflate(R.layout.info, null);
        String[] info = m.getTitle().split("&");
        String url = m.getSnippet();
        ((TextView)v.findViewById(R.id.info_window_nombre)).setText(m.getTitle());
        ((TextView)v.findViewById(R.id.info_window_estado)).setText("Estado: Activo");
        System.out.println("Esta es la URL: " + url);
        return v;
    }

    @Override
    public View getInfoWindow(Marker m) {
        return null;
    }
}
