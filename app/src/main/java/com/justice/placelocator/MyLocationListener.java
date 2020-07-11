package com.justice.placelocator;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyLocationListener implements LocationListener {
    private LocationListenerCallbacks listenerCallbacks;

    public MyLocationListener(LocationListenerCallbacks listenerCallbacks) {
        this.listenerCallbacks = listenerCallbacks;
    }

    @Override
    public void onLocationChanged(final Location location) {
        Geocoder geocoder = new Geocoder((Context) listenerCallbacks, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            Map<String, Object> map = new HashMap<>();
            map.put("address", address);
            map.put("city", city);
            map.put("country", country);
            map.put("latitude", location.getLatitude());
            map.put("longitude", location.getLongitude());

            listenerCallbacks.setTextView(map.toString());
            map.put("timeStamp", FieldValue.serverTimestamp());

            listenerCallbacks.sendLocationDataToDatabase(map);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        listenerCallbacks.setTextView("GPS Enable,fetching data...");

    }

    @Override
    public void onProviderDisabled(String provider) {
      listenerCallbacks.onProviderDisabled();

    }

    public interface LocationListenerCallbacks {
        void setTextView(String text);

        void showToast(String text);

        void sendLocationDataToDatabase(Map<String, Object> map);
        void onProviderDisabled();

    }
}
