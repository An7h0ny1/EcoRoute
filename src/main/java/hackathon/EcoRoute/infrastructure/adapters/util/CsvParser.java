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
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] v = line.split(",");
                // Necesitamos al menos: nombre, dirección, lat, lon (4 cols mínimo)
                if (v.length < 4) continue;

                // Leemos desde el FINAL para ser resilientes a comas en la dirección
                // Última col: peso (opcional), penúltima: longitud, antepenúltima: latitud
                try {
                    double weightKg = 1000.0;
                    int latIndex, lonIndex;

                    // Detectar si la última columna es el peso (no parece coordenada)
                    double lastVal = Double.parseDouble(v[v.length - 1].trim());
                    double secondLastVal = Double.parseDouble(v[v.length - 2].trim());
                    double thirdLastVal = Double.parseDouble(v[v.length - 3].trim());

                    // Las coordenadas de Colombia: lat ~ 4, lon ~ -74
                    // Si thirdLastVal y secondLastVal parecen coordenadas, lastVal es el peso
                    if (Math.abs(thirdLastVal) < 20 && Math.abs(secondLastVal) < 90) {
                        latIndex = v.length - 3;
                        lonIndex = v.length - 2;
                        weightKg = lastVal;
                    } else {
                        // Solo 4 cols útiles: sin peso
                        latIndex = v.length - 2;
                        lonIndex = v.length - 1;
                    }

                    double lat = Double.parseDouble(v[latIndex].trim());
                    double lon = Double.parseDouble(v[lonIndex].trim());

                    // Nombre = primera columna, dirección = todo lo del medio
                    String customerName = v[0].trim();
                    StringBuilder address = new StringBuilder();
                    for (int i = 1; i < latIndex; i++) {
                        if (i > 1) address.append(", ");
                        address.append(v[i].trim());
                    }

                    Delivery d = new Delivery();
                    d.setId(UUID.randomUUID().toString());
                    d.setCustomerName(customerName);
                    d.setAddress(address.toString());
                    d.setLatitude(lat);
                    d.setLongitude(lon);
                    d.setWeightKg(weightKg);
                    deliveries.add(d);

                } catch (NumberFormatException e) {
                    continue; // Línea malformada, ignorar
                }
            }
        }
        if (deliveries.isEmpty()) throw new Exception("El archivo no contiene datos válidos.");
        return deliveries;
    }
}