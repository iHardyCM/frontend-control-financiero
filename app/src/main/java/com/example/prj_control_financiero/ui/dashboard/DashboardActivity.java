package com.example.prj_control_financiero.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private PieChart pieChart;
    private int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dashboard);

        txtGastos = findViewById(R.id.txtview_Gastos);
        txtDisponible = findViewById(R.id.txtview_Balance);
        txtIngresos = findViewById(R.id.txtview_dash_ingresos);
        pieChart = findViewById(R.id.pieChartCategorias);

        idUsuario = getSharedPreferences("session", MODE_PRIVATE)
                .getInt("id_usuario", 0);


        configurarPieChart();
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

        Log.d("DASH", "ID USUARIO RECIBIDO = " + idUsuario);

    }

    private void configurarPieChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawEntryLabels(true);
        pieChart.setCenterText("Gastos del mes");
        pieChart.setCenterTextSize(14f);
        pieChart.setNoDataText("No hay gastos este mes");
    }


    private void cargarGraficoCategorias(Map<String, Float> totales) {
        pieChart.clear();
        pieChart.setNoDataText("No hay gastos este mes");

        if (totales.isEmpty()) {
            pieChart.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Float> item : totales.entrySet()) {
            entries.add(new PieEntry(item.getValue(), item.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Gastos por categoría");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setValueTextSize(11f);
        pieChart.setEntryLabelColor(android.graphics.Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.animateY(800);
        pieChart.invalidate();

    }


    private void cargarMovimientos() {
        new Thread(() -> {
            try {

                URL url = new URL("http://10.0.2.2:8000/movimientos_mes/" + idUsuario);
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

                reader.close();

                JSONObject json = new JSONObject(result.toString());
                JSONArray movimientos = json.getJSONArray("movimientos");

                Log.d("DASH", "Movimientos recibidos = " + movimientos.length());

                runOnUiThread(() -> {
                    procesarDashboard(movimientos);
                });


                runOnUiThread(() -> {
                    procesarDashboard(movimientos);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    txtIngresos.setText("Ingresos: S/ 0.00");
                    txtGastos.setText("Gastos: S/ 0.00");
                    txtDisponible.setText("Saldo Restante: S/ 0.00");
                    pieChart.clear();
                    pieChart.invalidate();
                });
            }
        }).start();
    }

    private void procesarDashboard(JSONArray movimientos) {

        double totalIngresos = 0;
        double totalGastos = 0;

        Map<String, Float> gastosPorCategoria = new HashMap<>();

        for (int i = 0; i < movimientos.length(); i++) {
            try {
                JSONObject mov = movimientos.getJSONObject(i);

                int idCategoria = mov.getInt("id_categoria");
                double monto = mov.getDouble("monto");

                Log.d("DASH",
                        "idCategoria=" + idCategoria +
                                " monto=" + monto +
                                " esIngreso=" + esIngreso(idCategoria)
                );


                if (esIngreso(idCategoria)) {
                    totalIngresos += monto;
                } else {
                    totalGastos += monto;

                    String categoria = nombreCategoria(idCategoria);
                    gastosPorCategoria.put(
                            categoria,
                            gastosPorCategoria.getOrDefault(categoria, 0f) + (float) monto
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double saldo = totalIngresos - totalGastos;

        txtIngresos.setText("Ingresos: S/ " + String.format("%.2f", totalIngresos));
        txtGastos.setText("Gastos: S/ " + String.format("%.2f", totalGastos));
        txtDisponible.setText("Saldo Restante: S/ " + String.format("%.2f", saldo));

        cargarGraficoCategorias(gastosPorCategoria);
    }

    private boolean esIngreso(int idCategoria) {
        return idCategoria == 7;
    }

    private String nombreCategoria(int idCategoria) {
        switch (idCategoria) {
            case 1: return "Hogar";
            case 2: return "Transporte";
            case 3: return "Alimentos";
            case 4: return "Salud";
            case 5: return "Entretenimiento";
            case 6: return "Otros";
            default: return "Ingreso";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMovimientos();
    }


}