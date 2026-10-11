package org.example.tarea2hernandezperez.models;

public class Compra {
    private int id;
    private String producto;
    private double monto;
    private String unidad;
    private String fecha;

    public int getId() {
        return id;
    }

    public String getProducto() {
        return producto;
    }

    public double getMonto() {
        return monto;
    }

    public String getUnidad() {
        return unidad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", producto='" + producto + '\'' +
                ", monto=" + monto +
                ", unidad='" + unidad + '\'' +
                ", fecha='" + fecha + '\'' +
                '}';
    }
}
