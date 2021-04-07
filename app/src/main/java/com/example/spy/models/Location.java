package com.example.spy.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Location {

    public String latitude;
    public String longtitude;
    public String tanggal;

    public Location(String latitude, String longtitude, String tanggal) {
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.tanggal = tanggal;
    }

}
