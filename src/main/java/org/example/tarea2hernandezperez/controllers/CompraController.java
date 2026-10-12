package org.example.tarea2hernandezperez.controllers;

import org.example.tarea2hernandezperez.models.Compra;
import org.example.tarea2hernandezperez.services.CompraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    // Desafío 1: GET /compras/{unidad}
    @GetMapping("/{unidad}")
    public ResponseEntity<?> obtenerComprasPorUnidad(@PathVariable String unidad) {
        // Validar que la unidad sea válida (uf o dolar, insensible a mayúsculas)
        String unidadUpper = unidad.toUpperCase();
        if (!unidadUpper.equals("UF") && !unidadUpper.equals("USD") && !unidadUpper.equals("DOLAR")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unidad no válida. Use 'uf' o 'dolar'."));
        }

        // Normalizar a formato estándar si ingresan dolar
        String unidadBusqueda = unidadUpper.equals("DOLAR") ? "USD" : unidadUpper;

        List<Compra> todas = compraService.leerCsv();
        List<Map<String, Object>> comprasFiltradas = new java.util.ArrayList<>();
        long totalPesos = 0;

        for (Compra c : todas) {
            if (c.getUnidad().equalsIgnoreCase(unidadBusqueda)) {
                double valorUnidad = compraService.obtenerValorIndicador(unidadBusqueda, c.getFecha());
                long montoPesos = Math.round(c.getMonto() * valorUnidad);
                totalPesos += montoPesos;

                Map<String, Object> mapCompra = new HashMap<>();
                mapCompra.put("id", c.getId());
                mapCompra.put("producto", c.getProducto());
                mapCompra.put("monto", c.getMonto());
                mapCompra.put("fecha", c.getFecha().toString());
                mapCompra.put("valor_unidad", valorUnidad);
                mapCompra.put("monto_pesos", montoPesos);

                comprasFiltradas.add(mapCompra);
            }
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("unidad", unidadBusqueda);
        respuesta.put("compras", comprasFiltradas);
        respuesta.put("total_pesos", totalPesos);

        return ResponseEntity.ok(respuesta);
    }
}