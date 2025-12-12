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

    private EditText editPin;
    private Button btnIngresar;
    private TextView usarHuella;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editPin = findViewById(R.id.edit_pin);
        btnIngresar = findViewById(R.id.btnIngresar);
        usarHuella = findViewById(R.id.usarHuella);

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

            if (pinIngresado.isEmpty()) {
                Toast.makeText(this, "Ingrese PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                boolean acceso = login(pinIngresado);

                runOnUiThread(() -> {
                    if (acceso) {

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "PIN incorrecto", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        usarHuella.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, BiometricActivity.class))
        );
    }

    private boolean login(String pin){
        try{
            URL url = new URL("http://10.0.2.2:8000/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            String jsonInput = "{\"pin\":\"" + pin + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8")
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }

            System.out.println("RESPUESTA DEL SERVIDOR: " + response.toString());

            return response.toString().contains("\"success\":true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}