package com.example.spy.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Command {

    public String command;
    public String arg1;
    public String arg2;
    public String key;
    public String status;
    public String tanggal;

    public void setKey(String key) {
        this.key = key;
    }

    public Command(){}

    public Command(String command, String arg1, String arg2, String status, String tanggal) {
        this.command = command;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.status = status;
        this.tanggal = tanggal;
    }

}
