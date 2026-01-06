package com.example.prj_control_financiero.ui.movimientos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prj_control_financiero.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResumenMensualActivity extends AppCompatActivity {

    private int idUsuario;
    private int idFecha;

    private TextView txtMayor, txtMenor, txtTotalGastos, txtTotalIngresos;
    private FrameLayout frameGrafico;
    private Button btnCompartir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_resumen_mensual);

        TextView titulo = findViewById(R.id.txtview_resumen_TituloResumen);

        String mes = LocalDate.now()
                .getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL,
                        new java.util.Locale("es"));

        titulo.setText("Resumen Mensual – " + mes);


        idUsuario = getSharedPreferences("session", MODE_PRIVATE)
                .getInt("id_usuario", 0);

        LocalDate hoy = LocalDate.now();
        idFecha = hoy.getYear() * 100 + hoy.getMonthValue();

        txtMayor = findViewById(R.id.txtview_resumen_MasGastado);
        txtMenor = findViewById(R.id.txtview_resumen_MenosGastado);
        txtTotalGastos = findViewById(R.id.txtview_resumen_TotalGastos);
        txtTotalIngresos = findViewById(R.id.txtview_resumen_TotalIngresos);
        frameGrafico = findViewById(R.id.frame_grafico);
        btnCompartir = findViewById(R.id.btnCompartir);

        cargarResumen();

        btnCompartir.setOnClickListener(v -> compartir());
    }

    private void cargarResumen() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8000/movimientos_mes/" + idUsuario);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONArray arr = new JSONObject(sb.toString()).getJSONArray("movimientos");

                double totalGastos = 0;
                double totalIngresos = 0;

                Map<Integer, Double> gastosPorCategoria = new HashMap<>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    int cat = o.getInt("id_categoria");
                    double monto = o.getDouble("monto");

                    if (cat == 7) {
                        totalIngresos += monto;
                    } else {
                        totalGastos += monto;
                        gastosPorCategoria.put(
                                cat,
                                gastosPorCategoria.getOrDefault(cat, 0.0) + monto
                        );
                    }
                }

                int catMayor = -1, catMenor = -1;
                double max = 0, min = Double.MAX_VALUE;

                for (Map.Entry<Integer, Double> e : gastosPorCategoria.entrySet()) {
                    if (e.getValue() > max) {
                        max = e.getValue();
                        catMayor = e.getKey();
                    }
                    if (e.getValue() < min) {
                        min = e.getValue();
                        catMenor = e.getKey();
                    }
                }

                double tg = totalGastos;
                double ti = totalIngresos;
                final int fCatMayor = catMayor;
                final int fCatMenor = catMenor;
                final double fMax = max;
                final double fMin = min;
                final double fTotalGastos = totalGastos;
                final double fTotalIngresos = totalIngresos;

                runOnUiThread(() -> {

                    txtTotalGastos.setText(
                            "Total de gastos: S/ " + String.format("%.2f", fTotalGastos)
                    );

                    txtTotalIngresos.setText(
                            "Total de ingresos: S/ " + String.format("%.2f", fTotalIngresos)
                    );

                    if (fTotalGastos > fTotalIngresos) {
                        txtTotalGastos.setTextColor(android.graphics.Color.RED);
                    } else {
                        txtTotalGastos.setTextColor(
                                android.graphics.Color.parseColor("#4CAF50")
                        );
                    }

                    txtMayor.setText(
                            "Categoría con mayor gasto: "
                                    + nombreCategoria(fCatMayor)
                                    + " S/ " + String.format("%.2f", fMax)
                    );

                    txtMenor.setText(
                            "Categoría con menor gasto: "
                                    + nombreCategoria(fCatMenor)
                                    + " S/ " + String.format("%.2f", fMin)
                    );

                    pintarGrafico(gastosPorCategoria);
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void pintarGrafico(Map<Integer, Double> data) {
        frameGrafico.removeAllViews();

        BarChart chart = new BarChart(this);
        chart.getDescription().setEnabled(false);

        ArrayList<BarEntry> entries = new ArrayList<>();
        int index = 0;

        List<Map.Entry<Integer, Double>> lista =
                new ArrayList<>(data.entrySet());

        lista.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (Map.Entry<Integer, Double> e : lista) {
            entries.add(new BarEntry(index++, e.getValue().floatValue()));
        }


        BarDataSet set = new BarDataSet(entries, "Gastos por categoría");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextSize(12f);
        set.setValueTextColor(android.graphics.Color.BLACK);

        BarData barData = new BarData(set);
        chart.setData(barData);
        set.setValueTextColor(android.graphics.Color.BLACK);
        chart.invalidate();

        frameGrafico.addView(chart);

    }

    private void compartir() {
        String texto =
                "📊 Resumen Mensual\n\n" +
                        txtTotalIngresos.getText() + "\n" +
                        txtTotalGastos.getText() + "\n" +
                        "Ahorro: S/ " +
                        String.format("%.2f",
                                Double.parseDouble(
                                        txtTotalIngresos.getText().toString()
                                                .replaceAll("[^0-9.]", "")
                                )
                                        -
                                        Double.parseDouble(
                                                txtTotalGastos.getText().toString()
                                                        .replaceAll("[^0-9.]", "")
                                        )
                        ) +
                        "\n\n" +
                        txtMayor.getText() + "\n" +
                        txtMenor.getText();


        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, texto);
        startActivity(Intent.createChooser(intent, "Compartir reporte"));
    }

    private String nombreCategoria(int id) {
        switch (id) {
            case 1: return "Hogar";
            case 2: return "Transporte";
            case 3: return "Alimentos";
            case 4: return "Salud";
            case 5: return "Entretenimiento";
            case 6: return "Otros";
            default: return "-";
        }
    }
}