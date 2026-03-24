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
                    Delivery delivery = new Delivery();
                    delivery.setId(UUID.randomUUID().toString());
                    delivery.setCustomerName(values[0].trim());
                    delivery.setAddress(values[1].trim());
                    delivery.setLatitude(Double.parseDouble(values[2].trim()));
                    delivery.setLongitude(Double.parseDouble(values[3].trim()));
                    // El peso es opcional en este MVP, por ahora 0.0 o el valor si existe
                    delivery.setWeightKg(values.length > 4 ? Double.parseDouble(values[4].trim()) : 0.0);

                    deliveries.add(delivery);
                }
            }
        }
        return deliveries;
    }
}
