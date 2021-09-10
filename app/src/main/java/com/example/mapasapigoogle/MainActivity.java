package com.example.mapasapigoogle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //
    Spinner spinner;
    Button btnShear;
    SupportMapFragment smf;
    GoogleMap map;
    FusedLocationProviderClient clm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //vary
        spinner = findViewById(R.id.spinner);
        btnShear = findViewById(R.id.btnbuscar);
        smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView2);
        String[] placeTypeList = {"Bank", "Hospital", "Movie_theater", "Restaurant"};
        String[] placeNameList = {"Bank", "Hopital", "Movie Tharter", "Restaurant"};

        //
        spinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, placeNameList));
        //
        clm = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
        btnShear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get
                int i = spinner.getSelectedItemPosition();
                //initi
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + currentLat + "," + currentLong +
                        "&radius=5000" +
                        "&types" + placeTypeList[i] +
                        "&sensor=true" +
                        "&key" + getResources().getString(R.string.google_map_key);
                new PlaceTask().execute(url);
            }
        });
    }

    double currentLat = 0, currentLong = 0;

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        Task<Location> task;
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
        task = clm.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();

                    smf.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //
                            map = googleMap;
                            //
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(currentLat,currentLong),10
                            ));
                        }
                    });
                 }

            }
        });
    }

    private class PlaceTask extends
            AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... strings){
            String data = null;
            try{
                data = downloadUrl(strings[0]);
            }catch(IOException e){
                e.printStackTrace();
            }
            return data;
        }
    }
    private String downloadUrl(String string)throws IOException{
        URL url = new URL(string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line = "";
        while((line = reader.readLine())!=null){
            builder.append(line);
        }
        String data = builder.toString();
        reader.close();
        return data;
    }


    protected void onPostExecute(String s){
        new ParserTask().execute(s);
    }

    private class ParserTask extends AsyncTask<String,Integer, List<HashMap<String,String>>>{

        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JsonParser jsonParser = new JsonParser();
            List<HashMap<String,String>>mapList=null;
            JSONObject object = null;
            try{
                object = new JSONObject(strings[0]);
                mapList = jsonParser.parseResult(object);
            }catch (JSONException e){
                e.printStackTrace();
            }
            return mapList;
        }
        @Override
        protected  void onPostExecute(List<HashMap<String,String>>hashMaps){
            map.clear();
            for (int i=0; i<hashMaps.size();i++){
                HashMap<String,String>hashMapList = hashMaps.get(i);
                double lat = Double.parseDouble(hashMapList.get("lat"));
                double lng=Double.parseDouble(hashMapList.get("lng"));
                String name = hashMapList.get("name");
                LatLng latLng = new LatLng(lat,lng);
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.title(name);
                map.addMarker(options);


            }
        }
    }
}