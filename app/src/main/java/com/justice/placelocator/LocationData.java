package com.justice.placelocator;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;

public class LocationData {
    @DocumentId
    private String id;
    private String address;
    private String city;
    private String country;
    private double latitude;
    private double longitude;
    private Date timeStamp;

    public LocationData() {
    }

    public LocationData(String address, String city, String country, double latitude, double longitude, Date timeStamp) {
        this.address = address;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
