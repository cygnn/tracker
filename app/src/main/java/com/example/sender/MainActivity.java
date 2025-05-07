package com.example.sender;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mapView = null;
    private MyLocationNewOverlay locationOverlay;
    private CompassOverlay compassOverlay;
    private TextView keyword;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().setOsmdroidBasePath(ctx.getFilesDir());
        Configuration.getInstance().setOsmdroidTileCache(ctx.getCacheDir());
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        keyword = findViewById(R.id.keywordText);
        btn = findViewById(R.id.sendButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CLICK", "Send button is clicked");
                sendSms(keyword.getText().toString());
                keyword.setText("");
            }
        });


        // Request permissions
        requestPermissionsIfNecessary(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
        });

        // Initialize the map
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        // Set initial map position and zoom level
        mapView.getController().setZoom(20.0);
        GeoPoint startPoint = new GeoPoint(10.297111026064464, 123.89681419994184); // New York City (example)
        mapView.getController().setCenter(startPoint);

        // Add location overlay (shows your current location)
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);


        // Example: Add a marker
        addMarker(startPoint, "UC MAIN", "This is an example marker");

        Intent intent =getIntent();
        String latitudeStr = intent.getStringExtra("latitude");
        String longitudeStr = intent.getStringExtra("longitude");

        if (latitudeStr != null && longitudeStr != null) {
            try {
                double lat = Double.parseDouble(latitudeStr);
                double lon = Double.parseDouble(longitudeStr);
                GeoPoint receivedPoint = new GeoPoint(lat, lon);

                // Center map and add marker
                mapView.getController().setCenter(receivedPoint);
                addMarker(receivedPoint, "Received Location", "From SMS");
            } catch (NumberFormatException e) {
                Log.e("MainActivity", "Invalid coordinates received.");
            }
        }


    }

    // Method to add a marker to the map
    private void addMarker(GeoPoint point, String title, String snippet) {
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker); //Removes previous marker
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setSnippet(snippet);
        mapView.getOverlays().add(marker);
        mapView.invalidate(); //Refreshes markers
    }

    @Override
    public void onResume() {
        super.onResume();
        // This is needed for the map to work properly
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // This is needed for the map to work properly
        mapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permissions[i]);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]), // Same fix here
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]), // Fix is here
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public void sendSms(String message){

        SmsManager sender =SmsManager.getDefault();
        Log.d("SEND", "Trying to send message");
        //Change this number to your receiver
        sender.sendTextMessage("+639055114206", null,message ,null,null);
    }
}