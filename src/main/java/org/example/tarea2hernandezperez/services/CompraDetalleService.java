package org.example.tarea2hernandezperez.services;

import org.example.tarea2hernandezperez.models.Compra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CompraDetalleService {

    private static final Logger log =
            LoggerFactory.getLogger(CompraDetalleService.class);

    private static final DateTimeFormatter FORMATO_API =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final Path rutaCsv;
    private final RestClient cliente;

    public CompraDetalleService(
            @Value("${CSV_PATH:compras.csv}") String rutaCsv) {

        this.rutaCsv = Path.of(rutaCsv);

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(15));
        factory.setReadTimeout(Duration.ofSeconds(120));

        this.cliente = RestClient.builder()
                .baseUrl("https://www.mindicador.cl/api")
                .requestFactory(factory)
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Map<String, Object> obtenerDetalle(int id) {
        Compra compra = buscarCompra(id);

        double valorUnidad = consultarIndicador(
                compra.getUnidad(),
                compra.getFecha());

        long montoPesos = Math.round(
                compra.getMonto() * valorUnidad);

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("id", compra.getId());
        respuesta.put("producto", compra.getProducto());
        respuesta.put("monto", compra.getMonto());
        respuesta.put("unidad", compra.getUnidad());
        respuesta.put("fecha", compra.getFecha().toString());
        respuesta.put("valor_unidad", valorUnidad);
        respuesta.put("monto_pesos", montoPesos);

        return respuesta;
    }

    private Compra buscarCompra(int idBuscado) {
        try (BufferedReader lector = Files.newBufferedReader(
                rutaCsv, StandardCharsets.UTF_8)) {

            // El CSV entregado tiene una primera fila de encabezados.
            String encabezado = lector.readLine();

            if (encabezado == null) {
                throw new IllegalStateException("El CSV está vacío");
            }

            String linea;
            int numeroLinea = 1;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;

                if (linea.isBlank()) {
                    continue;
                }

                Compra compra = convertirLinea(linea, numeroLinea);

                if (compra.getId() == idBuscado) {
                    return compra;
                }
            }

        } catch (IOException | IllegalStateException e) {
            log.error(
                    "No se pudo procesar el CSV: {}",
                    rutaCsv.toAbsolutePath(),
                    e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo leer o interpretar compras.csv",
                    e);
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No existe una compra con id " + idBuscado);
    }

    private Compra convertirLinea(String linea, int numeroLinea) {
        String[] campos = linea.split(",", -1);

        if (campos.length != 5) {
            throw new IllegalStateException(
                    "Se esperaban 5 columnas en la línea " + numeroLinea);
        }

        try {
            int id = Integer.parseInt(campos[0].trim());
            String producto = campos[1].trim();
            double monto = Double.parseDouble(campos[2].trim());

            String unidad = campos[3]
                    .trim()
                    .toUpperCase(Locale.ROOT);

            LocalDate fecha = LocalDate.parse(campos[4].trim());

            if (!Double.isFinite(monto) || monto < 0) {
                throw new IllegalStateException(
                        "Monto inválido en la línea " + numeroLinea);
            }

            if (!unidad.equals("UF") && !unidad.equals("USD")) {
                throw new IllegalStateException(
                        "Unidad inválida en la línea " + numeroLinea);
            }

            return new Compra(id, producto, monto, unidad, fecha);

        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalStateException(
                    "Formato incorrecto en la línea " + numeroLinea,
                    e);
        }
    }

    private double consultarIndicador(String unidad, LocalDate fecha) {
        String indicador = unidad.equals("UF") ? "uf" : "dolar";
        String fechaConsulta = fecha.format(FORMATO_API);

        Map<?, ?> respuesta;

        try {
            respuesta = cliente.get()
                    .uri("/{indicador}/{fecha}",
                            indicador, fechaConsulta)
                    .retrieve()
                    .body(Map.class);

        } catch (RestClientException e) {
            log.error(
                    "Error consultando mindicador: indicador={}, fecha={}",
                    indicador,
                    fechaConsulta,
                    e);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo consultar mindicador.cl",
                    e);
        }

        if (respuesta == null
                || !(respuesta.get("serie") instanceof List<?> serie)
                || serie.isEmpty()) {

            log.warn(
                    "Sin datos para indicador={}, fecha={}",
                    indicador,
                    fechaConsulta);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No hay valor disponible para "
                            + unidad + " en la fecha " + fecha);
        }

        if (!(serie.get(0) instanceof Map<?, ?> dato)
                || !(dato.get("valor") instanceof Number valor)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "La API devolvió una respuesta inválida");
        }

        double valorUnidad = valor.doubleValue();

        if (!Double.isFinite(valorUnidad) || valorUnidad <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El valor del indicador no es válido");
        }

        return valorUnidad;
    }
}