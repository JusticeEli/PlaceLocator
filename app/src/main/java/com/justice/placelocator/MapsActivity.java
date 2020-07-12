package com.justice.placelocator;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private AppointmentData appointmentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        appointmentData = MainActivity.documentSnapshot.toObject(AppointmentData.class);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng latLng = new LatLng(appointmentData.getCheckInLocation().getLatitude(), appointmentData.getCheckInLocation().getLongitude());
        String title = appointmentData.getCheckInLocation().getAddress();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Checked in at " + title));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        LatLng latLng2 = new LatLng(appointmentData.getCheckOutLocation().getLatitude(), appointmentData.getCheckOutLocation().getLongitude());
        String title2 = appointmentData.getCheckOutLocation().getAddress();
        mMap.addMarker(new MarkerOptions().position(latLng2).title("Checked out at " + title2));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng2));


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }
}