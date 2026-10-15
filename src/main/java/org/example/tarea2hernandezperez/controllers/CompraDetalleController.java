package org.example.tarea2hernandezperez.controllers;

import org.example.tarea2hernandezperez.services.CompraDetalleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/compras")
public class CompraDetalleController {

    private final CompraDetalleService compraDetalleService;

    public CompraDetalleController(
            CompraDetalleService compraDetalleService) {

        this.compraDetalleService = compraDetalleService;
    }

    @GetMapping("/detalle/{id}")
    public ResponseEntity<Map<String, Object>> obtenerDetalle(
            @PathVariable("id") int id) {

        return ResponseEntity.ok(
                compraDetalleService.obtenerDetalle(id));
    }
}