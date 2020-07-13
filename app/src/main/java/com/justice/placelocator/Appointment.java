package com.justice.placelocator;

public class Appointment {
    private String id;
    private String email;
    private long numberOfAppointments;

    public Appointment(String email, long numberOfAppointments) {
        this.email = email;
        this.numberOfAppointments = numberOfAppointments;
    }

    public Appointment() {
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

    public long getNumberOfAppointments() {
        return numberOfAppointments;
    }

    public void setNumberOfAppointments(long numberOfAppointments) {
        this.numberOfAppointments = numberOfAppointments;
    }
}
