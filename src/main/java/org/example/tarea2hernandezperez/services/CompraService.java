package org.example.tarea2hernandezperez.services;

import org.example.tarea2hernandezperez.models.Compra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CompraService {

    @Value("${csv.path:compras.csv}")
    private String csvPath;

    private final RestClient restClient = RestClient.create();

    public List<Compra> leerCsv() {
        List<Compra> compras = new ArrayList<>();
        Path path = Path.of(csvPath);

        if (!Files.exists(path)) {
            return compras;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false; // Saltar cabecera si la tiene
                    // Si tu CSV no tiene cabecera, elimina esta validación
                    // O si la primera línea es datos, coméntalo. Depende de tu CSV.
                }

                String[] partes = linea.split(",");
                if (partes.length >= 5) {
                    int id = Integer.parseInt(partes[0].trim());
                    String producto = partes[1].trim();
                    double monto = Double.parseDouble(partes[2].trim());
                    String unidad = partes[3].trim();
                    LocalDate fecha = LocalDate.parse(partes[4].trim());

                    compras.add(new Compra(id, producto, monto, unidad, fecha));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return compras;
    }

    public double obtenerValorIndicador(String unidad, LocalDate fecha) {
        String fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        // mindicador usa 'dolar' y 'uf' en minúscula en su API
        String indicador = unidad.equalsIgnoreCase("USD") || unidad.equalsIgnoreCase("DOLAR") ? "dolar" : "uf";
        String url = "https://mindicador.cl/api/" + indicador + "/" + fechaFormateada;

        try {
            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("serie")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> serie = (List<Map<String, Object>>) response.get("serie");
                if (serie != null && !serie.isEmpty()) {
                    Object valor = serie.get(0).get("valor");
                    if (valor instanceof Number) {
                        return ((Number) valor).doubleValue();
                    }
                }
            }
        } catch (Exception e) {
            // Manejo de errores de conexión o feriados sin valor
        }
        return 0.0;
    }
}