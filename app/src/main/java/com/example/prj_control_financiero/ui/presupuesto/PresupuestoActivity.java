package com.example.prj_control_financiero.ui.presupuesto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prj_control_financiero.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PresupuestoActivity extends AppCompatActivity {

    private int idUsuario;
    private int idFecha;
    private ProgressBar pHogar, pTransporte, pAlimentos, pSalud, pEntretenimiento, pOtros;
    private TextView
            txtTotal,
            txtPorcHogar,
            txtPorcTransporte,
            txtPorcAlimentos,
            txtPorcSalud,
            txtPorcEntretenimiento,
            txtPorcOtros;

    private Map<Integer, Double> presupuesto = new HashMap<>();
    private Map<Integer, Double> gastos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_presupuesto);

        idUsuario = getSharedPreferences("session", MODE_PRIVATE)
                .getInt("id_usuario", 0);

        LocalDate hoy = LocalDate.now();
        idFecha = hoy.getYear() * 100 + hoy.getMonthValue(); // 202601

        txtTotal = findViewById(R.id.txtview_PresupuestoTotal);

        pHogar = findViewById(R.id.progressHogar);
        pTransporte = findViewById(R.id.progressTransporte);
        pAlimentos = findViewById(R.id.progressAlimentos);
        pSalud = findViewById(R.id.progressSalud);
        pEntretenimiento = findViewById(R.id.progressEntretenimiento);
        pOtros = findViewById(R.id.progressOtros);

        txtPorcHogar = findViewById(R.id.txtHogarPct);
        txtPorcTransporte = findViewById(R.id.txtTransportePct);
        txtPorcAlimentos = findViewById(R.id.txtAlimentosPct);
        txtPorcSalud = findViewById(R.id.txtSaludPct);
        txtPorcEntretenimiento = findViewById(R.id.txtEntretenimientoPct);
        txtPorcOtros = findViewById(R.id.txtOtrosPct);


        Button btnEditar = findViewById(R.id.btn_EditarPresupuesto);
        btnEditar.setOnClickListener(v ->
                startActivity(new Intent(this, EditarPresupuestoActivity.class))
        );

        cargarPresupuesto();
    }

    private void cargarPresupuesto() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8000/presupuesto/" + idUsuario + "/" + idFecha);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONArray arr = new JSONObject(sb.toString()).getJSONArray("presupuesto");

                presupuesto.clear();
                double total = 0;

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    int cat = o.getInt("id_categoria");
                    double monto = o.getDouble("monto");
                    presupuesto.put(cat, monto);
                    total += monto;
                }

                double totalFinal = total;

                runOnUiThread(() -> {
                    txtTotal.setText("Presupuesto Total: S/ " + String.format("%.2f", totalFinal));
                    cargarGastos();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cargarGastos() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8000/movimientos/" + idUsuario);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONArray arr = new JSONObject(sb.toString()).getJSONArray("movimientos");

                gastos.clear();
                Log.d("PRESU", arr.toString());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);

                    if (!o.has("yyyymm")) {
                        Log.e("PRESU", "Movimiento sin yyyymm: " + o.toString());
                        continue;
                    }

                    int cat = o.getInt("id_categoria");
                    int yyyymm = o.getInt("yyyymm");
                    double monto = o.getDouble("monto");

                    if (yyyymm == idFecha && cat != 7) {
                        gastos.put(cat, gastos.getOrDefault(cat, 0.0) + monto);
                        Log.d("PRESU", "cat=" + cat + " monto=" + monto);
                    }
                }

                runOnUiThread(this::pintar);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void pintar() {
        pintarBarra(pHogar, txtPorcHogar, 1);
        pintarBarra(pTransporte, txtPorcTransporte, 2);
        pintarBarra(pAlimentos, txtPorcAlimentos, 3);
        pintarBarra(pSalud, txtPorcSalud, 4);
        pintarBarra(pEntretenimiento, txtPorcEntretenimiento, 5);
        pintarBarra(pOtros, txtPorcOtros, 6);

    }

    private void pintarBarra(ProgressBar bar, TextView txt, int cat) {
        double p = presupuesto.getOrDefault(cat, 0.0);
        double g = gastos.getOrDefault(cat, 0.0);

        int porcentaje = (p == 0) ? 0 : (int) Math.min((g / p) * 100, 100);

        bar.setProgress(porcentaje);
        txt.setText(porcentaje + "%");
    }



    @Override
    protected void onResume() {
        super.onResume();
        cargarPresupuesto();
    }
}