package com.justice.placelocator;

import java.util.Date;

public class AppointmentData {
    private String id;
    private String email;
    private Date checkInTime;
    private Date expectedFromTime;
    private Date checkOutTime;
    private Date expectToTime;
    private LocationData checkInLocation;
    private LocationData checkOutLocation;
    private String destinationLocation;
    private boolean checkIn;

    public AppointmentData(Date checkInTime, Date expectedFromTime, Date checkOutTime, Date expectToTime, LocationData checkInLocation, LocationData checkOutLocation, String destinationLocation) {
        this.checkInTime = checkInTime;
        this.expectedFromTime = expectedFromTime;
        this.checkOutTime = checkOutTime;
        this.expectToTime = expectToTime;
        this.checkInLocation = checkInLocation;
        this.checkOutLocation = checkOutLocation;
        this.destinationLocation = destinationLocation;
    }

    public AppointmentData() {
    }

    public boolean isCheckIn() {
        return checkIn;
    }

    public void setCheckIn(boolean checkIn) {
        this.checkIn = checkIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Date getExpectedFromTime() {
        return expectedFromTime;
    }

    public void setExpectedFromTime(Date expectedFromTime) {
        this.expectedFromTime = expectedFromTime;
    }

    public Date getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Date checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Date getExpectToTime() {
        return expectToTime;
    }

    public void setExpectToTime(Date expectToTime) {
        this.expectToTime = expectToTime;
    }

    public LocationData getCheckInLocation() {
        return checkInLocation;
    }

    public void setCheckInLocation(LocationData checkInLocation) {
        this.checkInLocation = checkInLocation;
    }

    public LocationData getCheckOutLocation() {
        return checkOutLocation;
    }

    public void setCheckOutLocation(LocationData checkOutLocation) {
        this.checkOutLocation = checkOutLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }
}
