package com.example.prj_control_financiero.data.models;

public class MovimientoRequest {

    public String tipo;
    public String categoria;
    public double monto;
    public String fecha;

    public MovimientoRequest (String tipo, String categoria, double monto, String fecha){
        this.tipo = tipo;
        this.categoria = categoria;
        this.monto = monto;
        this.fecha = fecha;
    }
}
