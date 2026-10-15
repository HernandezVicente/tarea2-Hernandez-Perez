package org.example.tarea2hernandezperez.controllers;

import org.example.tarea2hernandezperez.models.Compra;
import org.example.tarea2hernandezperez.services.CompraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;

@RestController
@RequestMapping("/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @GetMapping("/{unidad}")
    public ResponseEntity<?> obtenerComprasPorUnidad(@PathVariable String unidad) {
        String unidadUpper = unidad.toUpperCase();
        if (!unidadUpper.equals("UF") && !unidadUpper.equals("USD") && !unidadUpper.equals("DOLAR")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unidad no válida. Use 'uf' o 'dolar'."));
        }

        // Si consultan dolar, normalizamos la salida a USD como pide el requerimiento
        String unidadRespuesta = unidadUpper.equals("DOLAR") ? "USD" : unidadUpper;
        String unidadBusqueda = unidadUpper.equals("DOLAR") ? "dolar" : "UF";

        List<Compra> todas = compraService.leerCsv();
        List<Map<String, Object>> comprasFiltradas = new ArrayList<>();
        long totalPesos = 0;

        for (Compra c : todas) {
            if (c.getUnidad().equalsIgnoreCase(unidadBusqueda) || (unidadBusqueda.equals("dolar") && c.getUnidad().equalsIgnoreCase("USD"))) {
                double valorUnidad = compraService.obtenerValorIndicador(c.getUnidad(), c.getFecha());
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
        respuesta.put("unidad", unidadRespuesta);
        respuesta.put("compras", comprasFiltradas);
        respuesta.put("total_pesos", totalPesos);

        return ResponseEntity.ok(respuesta);
    }
}