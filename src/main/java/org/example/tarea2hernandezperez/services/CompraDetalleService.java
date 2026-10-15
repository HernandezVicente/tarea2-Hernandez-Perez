package org.example.tarea2hernandezperez.services;

import org.example.tarea2hernandezperez.models.Compra;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class CompraDetalleService {

    private final CompraService compraService;

    public CompraDetalleService(CompraService compraService) {
        this.compraService = compraService;
    }

    public Map<String, Object> obtenerDetalle(int id) {
        List<Compra> todas = compraService.leerCsv();
        for (Compra c : todas) {
            if (c.getId() == id) {
                double valorUnidad = compraService.obtenerValorIndicador(c.getUnidad(), c.getFecha());
                long montoPesos = Math.round(c.getMonto() * valorUnidad);

                Map<String, Object> resultado = new HashMap<>();
                resultado.put("id", c.getId());
                resultado.put("producto", c.getProducto());
                resultado.put("monto", c.getMonto());
                resultado.put("unidad", c.getUnidad());
                resultado.put("fecha", c.getFecha().toString());
                resultado.put("valor_unidad", valorUnidad);
                resultado.put("monto_pesos", montoPesos);
                return resultado;
            }
        }
        return null; // Retornará null si no se encuentra
    }
}