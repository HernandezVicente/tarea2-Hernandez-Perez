package org.example.tarea2hernandezperez.services;

import org.example.tarea2hernandezperez.models.Compra;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CompraService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Obtiene la ruta del CSV desde la variable de entorno CSV_PATH, o usa "compras.csv" por defecto en la raíz
    private String getCsvPath() {
        String path = System.getenv("CSV_PATH");
        return (path != null && !path.isEmpty()) ? path : "compras.csv";
    }

    // Lee todas las compras del archivo CSV manteniendo el orden original
    public List<Compra> leerCsv() {
        List<Compra> compras = new ArrayList<>();
        String archivo = getCsvPath();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true; // Para saltar la cabecera si la tiene
            while ((linea = br.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    // Si tu CSV tiene cabecera y la quieres saltar, descomenta esta línea.
                    // Si no tiene cabecera, bórrala.
                    // O puedes validar si la primera línea contiene letras en el ID.
                    if (linea.toLowerCase().contains("id")) continue;
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
        } catch (IOException e) {
            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
        }
        return compras;
    }

    // Consulta el valor de la UF o Dólar para una fecha específica en mindicador.cl
    public double obtenerValorIndicador(String unidad, LocalDate fecha) {
        String tipo = unidad.equalsIgnoreCase("uf") ? "uf" : "dolar";
        String fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String url = "https://mindicador.cl/api/" + tipo + "/" + fechaFormateada;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey(tipo)) {
                Map<String, Object> detalle = (Map<String, Object>) response.get(tipo);
                if (detalle != null && detalle.containsKey("valor")) {
                    return Double.parseDouble(detalle.get("valor").toString());
                }
            }
        } catch (Exception e) {
            // Si falla la API o no hay datos para ese día
        }
        return 0.0;
    }
}