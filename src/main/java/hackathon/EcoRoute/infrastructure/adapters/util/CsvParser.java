package hackathon.EcoRoute.infrastructure.adapters.util;

import hackathon.EcoRoute.domain.model.Delivery;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CsvParser {

    public List<Delivery> parse(MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new Exception("El archivo está vacío.");

        List<Delivery> deliveries = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) throw new Exception("El archivo no tiene encabezados.");

            List<String> headers = Arrays.stream(headerLine.split(","))
                    .map(h -> h.trim().toLowerCase().replaceAll("[^a-z]", ""))
                    .collect(Collectors.toList());

            int idxName = headers.indexOf("nombre");
            int idxAddress = headers.indexOf("direccion");
            int idxLat = headers.indexOf("latitud");
            int idxLon = headers.indexOf("longitud");
            int idxWeight = headers.indexOf("pesokg");

            if (idxName == -1 || idxLat == -1 || idxLon == -1) {
                throw new Exception("Formato inválido. El CSV debe tener columnas: Nombre, Latitud, Longitud.");
            }

            String line;
            int rowNum = 1;
            while ((line = br.readLine()) != null) {
                rowNum++;
                if (line.trim().isEmpty()) continue;

                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Regex para respetar comas dentro de comillas

                try {
                    String name = columns[idxName].replace("\"", "").trim();
                    String address = (idxAddress != -1 && idxAddress < columns.length) ? columns[idxAddress].replace("\"", "").trim() : "Dirección no especificada";
                    double lat = Double.parseDouble(columns[idxLat].trim());
                    double lon = Double.parseDouble(columns[idxLon].trim());
                    double weight = (idxWeight != -1 && idxWeight < columns.length) ? Double.parseDouble(columns[idxWeight].trim()) : 1000.0;

                    deliveries.add(new Delivery(UUID.randomUUID().toString(), address, lat, lon, name, weight));
                } catch (Exception e) {
                    throw new Exception("Error en la fila " + rowNum + ": Verifique que las coordenadas y el peso sean números válidos.");
                }
            }
        }

        if (deliveries.size() < 2) throw new Exception("Se necesitan al menos 2 puntos (Origen + 1 Entrega) para optimizar.");

        return deliveries;
    }
}