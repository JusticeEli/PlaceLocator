package com.justice.placelocator;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
    private LocationManager mLocationManager;
    public static Map currentLocation;

    public MyLocationListener(LocationListenerCallbacks listenerCallbacks, LocationManager mLocationManager) {
        this.listenerCallbacks = listenerCallbacks;
        this.mLocationManager = mLocationManager;
    }


    @Override
    public void onLocationChanged(final Location location) {
        onLocationChangedUserDefined(location);

    }

    public void onLocationChangedUserDefined(final Location location) {

        Geocoder geocoder = new Geocoder((Context) listenerCallbacks, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> finalMap = new HashMap<>();

            map.put("address", address);
            map.put("city", city);
            map.put("country", country);
            map.put("latitude", location.getLatitude());
            map.put("longitude", location.getLongitude());

            listenerCallbacks.setTextView(map.toString());
            map.put("timeStamp", FieldValue.serverTimestamp());
            if (MainActivity.checkIn) {
                finalMap.put("checkIn", true);
                finalMap.put("checkInLocation", map);
                finalMap.put("checkInTime", FieldValue.serverTimestamp());
                listenerCallbacks.setTextView("You are currently checked in ,\n Current Location is" + map.toString());

            } else {
                finalMap.put("checkIn", false);
                finalMap.put("checkOutLocation", map);
                finalMap.put("checkOutTime", FieldValue.serverTimestamp());
                listenerCallbacks.setTextView("You are currently checked out ,\n Current Location is" + map.toString());

            }
            currentLocation = map;
            listenerCallbacks.sendLocationDataToDatabase(finalMap);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        listenerCallbacks.setTextView("GPS Enable,please wait as we fetch current location...");

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
