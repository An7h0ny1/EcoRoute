package hackathon.EcoRoute.infrastructure.adapters.util;

import hackathon.EcoRoute.domain.model.Delivery;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CsvParser {
    public List<Delivery> parse(MultipartFile file) throws Exception {
        List<Delivery> deliveries = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            // Saltamos la cabecera si el CSV la tiene
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4) {
                    // Validar coordenadas
                    double lat, lon, weight = 0.0;
                    try {
                        lat = Double.parseDouble(values[2].trim());
                        lon = Double.parseDouble(values[3].trim());
                        
                        // Validar rangos de Colombia
                        if (lat < -4.5 || lat > 13.0 || lon < -79.0 || lon > -66.0) {
                            System.err.println("Coordenadas fuera de rango de Colombia: " + lat + ", " + lon);
                            continue;
                        }
                        
                        // Peso opcional
                        if (values.length > 4) {
                            weight = Double.parseDouble(values[4].trim());
                            if (weight < 0 || weight > 50000) { // límite razonable
                                weight = 1000.0; // valor por defecto
                            }
                        }
                        
                    } catch (NumberFormatException e) {
                        System.err.println("Error parseando coordenadas en línea: " + line);
                        continue;
                    }
                    
                    Delivery delivery = new Delivery();
                    delivery.setId(UUID.randomUUID().toString());
                    delivery.setCustomerName(values[0].trim());
                    delivery.setAddress(values[1].trim());
                    delivery.setLatitude(lat);
                    delivery.setLongitude(lon);
                    delivery.setWeightKg(weight);

                    deliveries.add(delivery);
                } else {
                    System.err.println("Línea con formato inválido (menos de 4 campos): " + line);
                }
            }
        }
        
        if (deliveries.isEmpty()) {
            throw new Exception("No se pudieron procesar entregas válidas del CSV");
        }
        
        return deliveries;
    }
}
