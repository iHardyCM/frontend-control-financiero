package com.example.prj_control_financiero.data.api;

import com.example.prj_control_financiero.data.models.LoginRequest;
import com.example.prj_control_financiero.data.models.UsuarioRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("usuario")
    Call<Map<String, Object>> registrarUsuario(@Body UsuarioRequest request);

    @POST("login")
    Call<Map<String, Object>> login(@Body LoginRequest request);


}
