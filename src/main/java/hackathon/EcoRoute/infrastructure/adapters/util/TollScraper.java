package hackathon.EcoRoute.infrastructure.adapters.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TollScraper {

    private final WebClient webClient;

    public TollScraper() {
        this.webClient = WebClient.builder().build();
    }

    private static final String ARCGIS_BASE_URL = "https://services7.arcgis.com/8o2hyYSY6GDe4YX6/arcgis/rest/services/PeajesColombia/FeatureServer/0/query";

    public List<Map<String, Object>> getLiveTollsForBoundingBox(double minLat, double minLon, double maxLat, double maxLon) {
        System.out.println("=== INICIO getLiveTollsForBoundingBox ===");

        String dynamicUrl = null;
        try {
            // Usamos .fromHttpUrl directamente (es un método estático)
            dynamicUrl = UriComponentsBuilder.fromUriString(ARCGIS_BASE_URL)
                    .queryParam("where", "1=1")
                    .queryParam("outFields", "F_cat_1_,F_cat_2_,F_cat_3_,F_cat_4_,F_cat_5_,F_cat_6_,F_cat_7_,F_cat_8_,F_cat_9_,F_ubicacion_,LATITUDE,LONGITUDE")
                    // Importante: ArcGIS espera Lon,Lat,Lon,Lat (x,y,x,y)
                    .queryParam("geometry", minLon + "," + minLat + "," + maxLon + "," + maxLat)
                    .queryParam("geometryType", "esriGeometryEnvelope")
                    .queryParam("inSR", "4326")
                    .queryParam("spatialRel", "esriSpatialRelIntersects")
                    .queryParam("returnGeometry", "true")
                    .queryParam("f", "json")
                    .build()
                    .toUriString();

            System.out.println("URL Generada: " + dynamicUrl);

            Map<String, Object> response = webClient.get()
                    .uri(dynamicUrl)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(60))
                    .block();

            if (response != null && response.containsKey("features")) {
                List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");
                System.out.println("Peajes obtenidos: " + features.size());
                return features;
            } else {
                System.out.println("Response no contiene 'features'. Error: " + (response != null ? response.get("error") : "null"));
            }
        } catch (Exception e) {
            System.err.println("Error consumiendo ArcGIS: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}