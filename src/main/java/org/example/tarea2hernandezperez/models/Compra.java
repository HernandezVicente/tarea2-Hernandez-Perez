package org.example.tarea2hernandezperez.models;

import java.time.LocalDate;

public class Compra {

    private int id;
    private String producto;
    private double monto;
    private String unidad;
    private LocalDate fecha;

    public Compra() {
    }

    public Compra(int id, String producto, double monto,
                  String unidad, LocalDate fecha) {
        this.id = id;
        this.producto = producto;
        this.monto = monto;
        this.unidad = unidad;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", producto='" + producto + '\'' +
                ", monto=" + monto +
                ", unidad='" + unidad + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}