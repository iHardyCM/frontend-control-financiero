package com.example.prj_control_financiero.ui.presupuesto;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prj_control_financiero.R;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

public class EditarPresupuestoActivity extends AppCompatActivity {

    private EditText e1,e2,e3,e4,e5,e6;
    private int idUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_presupuesto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        e1 = findViewById(R.id.edtHogar);
        e2 = findViewById(R.id.edtTransporte);
        e3 = findViewById(R.id.edtAlimentos);
        e4 = findViewById(R.id.edtSalud);
        e5 = findViewById(R.id.edtEntretenimiento);
        e6 = findViewById(R.id.edtOtros);

        idUsuario = getSharedPreferences("session", MODE_PRIVATE)
                .getInt("id_usuario", 0);

        Button btnGuardar = findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        new Thread(() -> {
            try {

                int idFecha = obtenerIdFechaMesActual(); // 👈 CLAVE

                enviarPresupuesto(1, val(e1), idFecha); // Hogar
                enviarPresupuesto(2, val(e2), idFecha); // Transporte
                enviarPresupuesto(3, val(e3), idFecha); // Alimentos
                enviarPresupuesto(4, val(e4), idFecha); // Salud
                enviarPresupuesto(5, val(e5), idFecha); // Entretenimiento
                enviarPresupuesto(6, val(e6), idFecha); // Otros

                runOnUiThread(() -> {
                    Toast.makeText(this, "Presupuesto guardado", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void enviarPresupuesto(int idCategoria, double monto, int idFecha) throws Exception {

        if (monto <= 0) return; // no guardar ceros

        URL url = new URL("http://10.0.2.2:8000/presupuesto");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);

        String json = "{"
                + "\"id_usuario\":" + idUsuario + ","
                + "\"id_categoria\":" + idCategoria + ","
                + "\"id_fecha\":" + idFecha + ","
                + "\"monto\":" + monto
                + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes("utf-8"));
        }

        conn.getInputStream(); // fuerza ejecución
    }

    private int obtenerIdFechaMesActual() {
        LocalDate hoy = LocalDate.now();
        return hoy.getYear() * 100 + hoy.getMonthValue(); // ej: 202601
    }


    private double val(EditText e){
        if(e.getText().toString().isEmpty()) return 0;
        return Double.parseDouble(e.getText().toString());
    }
}