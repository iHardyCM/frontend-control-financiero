package com.example.prj_control_financiero.data.models;

public class UsuarioRequest {

    private String username;
    private String email;
    private String pin;

    public UsuarioRequest(String username, String email, String pin){
        this.username = username;
        this.email = email;
        this.pin = pin;
    }

}
