package com.techtown.taxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.io.IOException;
import java.security.acl.Permission;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    SupportMapFragment mapFragment;
    GoogleMap map;
    MarkerOptions myLocationMarker;
    TextView textView;
    double latitude;
    double longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView3);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                Log.d("Map","지도 준비됨!.");
                map = googleMap;
            }
        });
        try{
            MapsInitializer.initialize(this);
        }catch(Exception e){
            e.printStackTrace();
        }

        AndPermission.with(this)
                .runtime()
                .permission(com.yanzhenjie.permission.runtime.Permission.ACCESS_FINE_LOCATION,
                        com.yanzhenjie.permission.runtime.Permission.ACCESS_COARSE_LOCATION)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("허용된 권한 갯수 : " + permissions.size());
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("거부된 권한 갯수 : " + permissions.size());
                    }
                })
                .start();

        startLocationService();

    }
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    public void startLocationService(){
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                 latitude = location.getLatitude();
                 longitude = location.getLongitude();
                String message = "최근 위치 -> Latitude : " + latitude + "\nLongitude:" + longitude;

                String add = getCurrentAddress(latitude,longitude);
                textView.setText("Location : " + add);
            }

            GPSListener gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime, minDistance, gpsListener);

            Toast.makeText(getApplicationContext(), "내 위치확인 요청함",
                    Toast.LENGTH_SHORT).show();


        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }
    public void onButton3Clicked(View v) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:010-1000-1000"));
        startActivity(myIntent);
    }
    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location){
             latitude = location.getLatitude();
             longitude = location.getLongitude();
            String message = "내 위치 -> Latitude : " + latitude + ", Longitude : " + longitude;

            showCurrentLocation(latitude,longitude);
            String add = getCurrentAddress(latitude,longitude);
            textView.setText("Location : " + add);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    }
    private void showCurrentLocation(Double latitude, Double longitude) {
        LatLng curPoint = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

        showMyLocationMarker(curPoint);
    }

    private void showMyLocationMarker(LatLng curPoint) {
        if (myLocationMarker == null) {
            myLocationMarker = new MarkerOptions();
            myLocationMarker.position(curPoint);
            myLocationMarker.title("● 내 위치\n");
            myLocationMarker.snippet("● GPS로 확인한 위치");
            myLocationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation));
            map.addMarker(myLocationMarker);
        } else {
            myLocationMarker.position(curPoint);
        }
    }
    public String getCurrentAddress(double latitude, double longitude){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try{
            addresses = geocoder.getFromLocation(latitude, longitude,7);

        }catch(IOException ioException){
            Toast.makeText(this, "negative", Toast.LENGTH_LONG).show();
            return "negative";
        }catch(IllegalArgumentException illegalArgumentException){
            Toast.makeText(this, "negative",Toast.LENGTH_LONG).show();
            return "negative";
        }
        if(addresses == null || addresses.size()==0){
            Toast.makeText(this, "negative", Toast.LENGTH_LONG).show();
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }
}
