package com.example.prj_control_financiero.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prj_control_financiero.ui.presupuesto.PresupuestoActivity;
import com.example.prj_control_financiero.R;
import com.example.prj_control_financiero.ui.movimientos.RegistrarMovimientoActivity;
import com.example.prj_control_financiero.ui.movimientos.ResumenMensualActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {


    private TextView txtGastos, txtIngresos, txtDisponible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dashboard);

        txtGastos = findViewById(R.id.txtview_Gastos);
        txtDisponible = findViewById(R.id.txtview_Balance);
        txtIngresos = findViewById(R.id.txtview_dash_ingresos);

        cargarResumen();
        cargarMovimientos();

        Button btnRegistrar = findViewById(R.id.btnRegistrarMovimiento);
        Button btnPresupuesto = findViewById(R.id.btnVerPresupuesto);
        Button btnResumen = findViewById(R.id.btnResumenMensual);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, RegistrarMovimientoActivity.class));
            }
        });

        btnPresupuesto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, PresupuestoActivity.class));
            }
        });

        btnResumen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, ResumenMensualActivity.class));
            }
        });
    }

    private void cargarGraficoCategorias(JSONArray listaMovimientos) {
        try {
            Map<String, Float> totales = new HashMap<>();

            for (int i = 0; i < listaMovimientos.length(); i++) {
                JSONObject mov = listaMovimientos.getJSONObject(i);

                if (mov.getString("tipo").equals("Gasto")) {
                    String cat = mov.getString("categoria");
                    float monto = (float) mov.getDouble("monto");

                    totales.put(cat, totales.getOrDefault(cat, 0f) + monto);
                }
            }


            ArrayList<PieEntry> entries = new ArrayList<>();
            float totalGastos = 0f;

            for (float valor : totales.values()) {
                totalGastos += valor;
            }

            for (String cat : totales.keySet()) {
                float monto = totales.get(cat);
                entries.add(new PieEntry(monto, cat));
            }

            PieDataSet dataSet = new PieDataSet(entries, "Gastos por categoría");
            dataSet.setSliceSpace(2f);
            dataSet.setValueTextSize(12f);
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

            PieData data = new PieData(dataSet);

            PieChart pieChart = findViewById(R.id.pieChartCategorias);
            pieChart.setData(data);
            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleRadius(40f);
            pieChart.setTransparentCircleRadius(45f);
            pieChart.animateY(1000);

            pieChart.invalidate();


            LinearLayout layout = findViewById(R.id.layoutPorcentajes);
            layout.removeAllViews();

            for (String cat : totales.keySet()) {
                float monto = totales.get(cat);
                float porcentaje = (monto / totalGastos) * 100f;

                TextView tv = new TextView(this);
                tv.setText(cat + ": " + String.format("%.1f", porcentaje) + "%");
                tv.setTextSize(16);
                layout.addView(tv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void cargarResumen() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8000/resumen");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();


                JSONObject responseObject = new JSONObject(response.toString());

                double ingresos = responseObject.getDouble("ingresos");
                double gastos = responseObject.getDouble("gastos");
                double disponible = responseObject.getDouble("disponible");


                runOnUiThread(() -> {
                    try {
                        txtIngresos.setText("Ingresos: S/ " + ingresos);
                        txtGastos.setText("Gastos: S/ " + gastos);
                        txtDisponible.setText("Saldo Restante: S/ " + disponible);


                        JSONArray movimientos = responseObject.getJSONArray("movimientos");
                        cargarGraficoCategorias(movimientos);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void cargarMovimientos() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8000/movimientos"); // ← endpoint que devuelve lista
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                JSONArray movimientos = json.getJSONArray("movimientos");

                runOnUiThread(() -> {
                    cargarGraficoCategorias(movimientos);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        cargarResumen();
    }


}