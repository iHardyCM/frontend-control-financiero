package com.example.prj_control_financiero.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prj_control_financiero.ui.dashboard.DashboardActivity;
import com.example.prj_control_financiero.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText login_username;
    private EditText editPin;
    private Button btnIngresar;
    private TextView usarHuella;
    private TextView registrarUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        login_username = findViewById(R.id.edt_login_username);
        editPin = findViewById(R.id.edit_pin);
        btnIngresar = findViewById(R.id.btnIngresar);
        usarHuella = findViewById(R.id.usarHuella);
        registrarUsuario = findViewById(R.id.registrarUsuario);

        Button b0 = findViewById(R.id.boton0);
        Button b1 = findViewById(R.id.boton1);
        Button b2 = findViewById(R.id.boton2);
        Button b3 = findViewById(R.id.boton3);
        Button b4 = findViewById(R.id.boton4);
        Button b5 = findViewById(R.id.boton5);
        Button b6 = findViewById(R.id.boton6);
        Button b7 = findViewById(R.id.boton7);
        Button b8 = findViewById(R.id.boton8);
        Button b9 = findViewById(R.id.boton9);

        View.OnClickListener listenerNumeros = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                editPin.append(b.getText().toString());
            }
        };

        b0.setOnClickListener(listenerNumeros);
        b1.setOnClickListener(listenerNumeros);
        b2.setOnClickListener(listenerNumeros);
        b3.setOnClickListener(listenerNumeros);
        b4.setOnClickListener(listenerNumeros);
        b5.setOnClickListener(listenerNumeros);
        b6.setOnClickListener(listenerNumeros);
        b7.setOnClickListener(listenerNumeros);
        b8.setOnClickListener(listenerNumeros);
        b9.setOnClickListener(listenerNumeros);

        btnIngresar.setOnClickListener(v -> {
            String pinIngresado = editPin.getText().toString().trim();
            String username = login_username.getText().toString().trim();

            if (username.isEmpty()) {
                Toast.makeText(this, "Ingresa tu usuario", Toast.LENGTH_SHORT).show();
                return;
            }


            if (pinIngresado.isEmpty()) {
                Toast.makeText(this, "Ingrese PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                int idUsuario = login(username, pinIngresado);

                runOnUiThread(() -> {
                    if (idUsuario > 0) {

                        // 🔑 GUARDAR SESIÓN
                        getSharedPreferences("session", MODE_PRIVATE)
                                .edit()
                                .putInt("id_usuario", idUsuario)
                                .apply();

                        Toast.makeText(this,
                                "Inicio de sesión exitoso",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(
                                MainActivity.this,
                                DashboardActivity.class
                        ));
                        finish();

                    } else {
                        Toast.makeText(this,
                                "Credenciales incorrectas",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }).start();
        });

        usarHuella.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, BiometricActivity.class))
        );

        registrarUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistroUsuarioActivity.class);
            startActivity(intent);
        });

    }

    private int login(String username, String pin) {
        try {
            URL url = new URL("http://10.0.2.2:8000/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            String jsonInput =
                    "{ \"username\": \"" + username + "\", " +
                            "\"pin\": \"" + pin + "\" }";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes("utf-8"));
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8")
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }

            String jsonResponse = response.toString();
            System.out.println("RESPUESTA LOGIN -> " + jsonResponse);

            if (jsonResponse.contains("\"success\":true")) {
                // 🔑 extraer id_usuario
                String idStr = jsonResponse
                        .replaceAll(".*\"id_usuario\":", "")
                        .replaceAll("[^0-9].*", "");

                return Integer.parseInt(idStr);
            }

            return 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}