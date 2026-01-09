package com.example.prj_control_financiero.ui.movimientos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prj_control_financiero.R;
import com.example.prj_control_financiero.data.models.MovimientoRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class RegistrarMovimientoActivity extends AppCompatActivity {

    private String categoriaSeleccionada = "";
    private EditText edtMonto;
    private RadioGroup rgTipo;
    private RadioButton rbGasto, rbIngreso;
    private String fechaSeleccionada = "";
    private String tipoSeleccionado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_registrar_movimiento);

        ImageView catHogar = findViewById(R.id.cat_hogar);
        ImageView catTransporte = findViewById(R.id.cat_transporte);
        ImageView catAlimentos = findViewById(R.id.cat_alimentos);
        ImageView catSalud = findViewById(R.id.cat_salud);
        ImageView catEntretenimiento = findViewById(R.id.cat_entretenimiento);
        ImageView catOtros = findViewById(R.id.cat_otros);

        edtMonto = findViewById(R.id.edt_text_monto);
        rgTipo = findViewById(R.id.rg_Tipo);
        rbGasto = findViewById(R.id.rb_Gasto);
        rbIngreso = findViewById(R.id.rb_Ingreso);

        View.OnClickListener listenerCategoria = v -> {

            resetSeleccionCategorias();
            v.setBackgroundResource(R.drawable.bg_categoria_seleccionada);

            int id = v.getId();

            if (id == R.id.cat_hogar) {
                categoriaSeleccionada = "Hogar";
            }
            else if (id == R.id.cat_transporte) {
                categoriaSeleccionada = "Transporte";
            }
            else if (id == R.id.cat_alimentos) {
                categoriaSeleccionada = "Alimentos";
            }
            else if (id == R.id.cat_salud) {
                categoriaSeleccionada = "Salud";
            }
            else if (id == R.id.cat_entretenimiento) {
                categoriaSeleccionada = "Entretenimiento";
            }
            else if (id == R.id.cat_otros) {
                categoriaSeleccionada = "Otros";
            }

            System.out.println("CATEGORIA SELECCIONADA -> " + categoriaSeleccionada);
        };


        catHogar.setOnClickListener(listenerCategoria);
        catTransporte.setOnClickListener(listenerCategoria);
        catAlimentos.setOnClickListener(listenerCategoria);
        catSalud.setOnClickListener(listenerCategoria);
        catEntretenimiento.setOnClickListener(listenerCategoria);
        catOtros.setOnClickListener(listenerCategoria);

        Button btnGuardar = findViewById(R.id.btn_GuardarMovimiento);
        btnGuardar.setOnClickListener(v -> {

            String tipo = rbGasto.isChecked() ? "Gasto" : "Ingreso";

            // VALIDACIÓN DE CATEGORÍA SOLO PARA GASTOS
            if (tipo.equals("Gasto") && categoriaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Seleccione una categoría", Toast.LENGTH_SHORT).show();
                return;
            }

            // SI ES INGRESO, DEFINIMOS CATEGORÍA AUTOMÁTICA
            if (tipo.equals("Ingreso")) {
                categoriaSeleccionada = "Ingreso";
            }

            String montoTxt = edtMonto.getText().toString().trim();
            if (montoTxt.isEmpty()) {
                Toast.makeText(this, "Ingrese un monto", Toast.LENGTH_SHORT).show();
                return;
            }

            if (fechaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Seleccione una fecha", Toast.LENGTH_SHORT).show();
                return;
            }

            guardarMovimiento();
        });


        Button btnFecha = findViewById(R.id.btn_SeleccionarFecha);
        btnFecha.setOnClickListener(v -> {
            seleccionarFecha();
        });



        rgTipo.setOnCheckedChangeListener((group, checkedID) -> {
            if (checkedID == R.id.rb_Ingreso){
                ocultarCategorias();
                btnGuardar.setText("Guardar Ingreso");
            } else {
                mostrarCategorias();
                btnGuardar.setText("Guardar Movimiento");
                categoriaSeleccionada = "";
            }
        });
    }

    private void resetSeleccionCategorias() {
        int[] ids = {
                R.id.cat_hogar, R.id.cat_transporte, R.id.cat_alimentos,
                R.id.cat_salud, R.id.cat_entretenimiento, R.id.cat_otros
        };

        for (int id : ids) {
            findViewById(id).setBackgroundResource(R.drawable.bg_categoria_normal);
        }
    }

    private void guardarMovimiento() {
        float monto = Float.parseFloat(edtMonto.getText().toString());
        String tipo = rbGasto.isChecked() ? "Gasto": "Ingreso";

        enviarMovimiento(monto,tipo,categoriaSeleccionada,fechaSeleccionada);
    }

    private void seleccionarFecha() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // enero = 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    selectedMonth += 1;

                    fechaSeleccionada = String.format(
                            "%04d-%02d-%02d",
                            selectedYear, selectedMonth, selectedDay
                    );

                    Toast.makeText(
                            this,
                            "Fecha: " + fechaSeleccionada,
                            Toast.LENGTH_SHORT
                    ).show();
                },
                year, month, day
        );

        datePicker.show();
    }





    private void enviarMovimiento(float monto, String tipo, String categoria, String fecha){

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8000/movimiento");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                int idUsuario = getSharedPreferences("session", MODE_PRIVATE)
                        .getInt("id_usuario", 0);
                System.out.println("ID USUARIO -> " + idUsuario);

                int idCuenta = 1;
                int idCategoria = obtenerIdCategoria(categoria);
                int idFecha = 1; // MVP

                System.out.println("CATEGORIA RECIBIDA -> [" + categoria + "]");
                System.out.println("ID CATEGORIA -> " + idCategoria);

                if (idUsuario == 0 || idCategoria == 0) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                String json = "{"
                        + "\"id_usuario\":" + idUsuario + ","
                        + "\"id_cuenta\":" + idCuenta + ","
                        + "\"id_categoria\":" + idCategoria + ","
                        + "\"fecha\":\"" + fecha + "\","
                        + "\"monto\":" + monto + ","
                        + "\"descripcion\":\"" + categoria + "\""
                        + "}";



                System.out.println("JSON -> " + json);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }

                int responseCode = conn.getResponseCode();
                InputStream errorStream = conn.getErrorStream();
                String backendError = "";

                if (errorStream != null) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(errorStream)
                    );
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    backendError = sb.toString();
                }

                String finalBackendError = backendError;

                runOnUiThread(() ->{
                    if (responseCode == 200 || responseCode == 201){
                        Toast.makeText(this, "Movimiento guardado correctamente", Toast.LENGTH_SHORT).show();
                        limpiarFormulario();
                    } else {
                        Toast.makeText(this,
                                "Error (" + responseCode + "): " + finalBackendError,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e){
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private int obtenerIdCategoria(String categoria) {
        if (categoria == null) return 0;

        switch (categoria.trim().toLowerCase()) {
            case "hogar": return 1;
            case "transporte": return 2;
            case "alimentos": return 3;
            case "salud": return 4;
            case "entretenimiento": return 5;
            case "otros": return 6;
            case "ingreso": return 7;
            default: return 0;
        }
    }


    private void limpiarFormulario(){
        edtMonto.setText("");
        rgTipo.clearCheck();
        resetSeleccionCategorias();
        categoriaSeleccionada = "";
        fechaSeleccionada = "";
    }

    private void ocultarCategorias() {
        findViewById(R.id.layoutFila1).setVisibility(View.GONE);
        findViewById(R.id.layoutFila2).setVisibility(View.GONE);
    }

    private void mostrarCategorias() {
        findViewById(R.id.layoutFila1).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutFila2).setVisibility(View.VISIBLE);
    }


}