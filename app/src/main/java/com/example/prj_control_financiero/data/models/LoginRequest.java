package com.example.prj_control_financiero.data.models;

public class LoginRequest {

    private String username;
    private String pin;

    public LoginRequest(String username, String pin){
        this.username = username;
        this.pin = pin;
    }
}
