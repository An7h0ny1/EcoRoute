package hackathon.EcoRoute.infrastructure.adapters.persistence;

import hackathon.EcoRoute.application.ports.out.TollRepositoryPort;
import hackathon.EcoRoute.domain.model.Delivery;
import hackathon.EcoRoute.domain.model.DetectedToll;
import hackathon.EcoRoute.domain.model.RouteCost;
import hackathon.EcoRoute.infrastructure.adapters.util.TollScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TollPersistenceAdapter implements TollRepositoryPort {

    private final TollScraper tollScraper;

    private final WebClient webClient = WebClient.builder()
            .defaultHeader("User-Agent", "EcoRoute/1.0")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();

    @Override
    public RouteCost calculateTollsForRoute(List<Delivery> deliveries) {
        System.out.println("=== INICIO calculateTollsForRoute ===");
        System.out.println("Número de entregas: " + (deliveries != null ? deliveries.size() : "null"));

        if (deliveries == null || deliveries.size() < 2) return new RouteCost(0, 0, 0);

        // Calcular bounding box de las entregas
        System.out.println("=== CALCULANDO BOUNDING BOX ===");
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (Delivery d : deliveries) {
            minLat = Math.min(minLat, d.getLatitude());
            maxLat = Math.max(maxLat, d.getLatitude());
            minLon = Math.min(minLon, d.getLongitude());
            maxLon = Math.max(maxLon, d.getLongitude());
        }

        System.out.println("Bounding box calculado: minLat=" + minLat + " maxLat=" + maxLat + " minLon=" + minLon + " maxLon=" + maxLon);

        // Obtener peajes solo para el bounding box
        System.out.println("=== OBTENIENDO PEAJES DE ARCGIS ===");
        List<Map<String, Object>> liveTolls = tollScraper.getLiveTollsForBoundingBox(minLat, minLon, maxLat, maxLon);
        System.out.println("Peajes obtenidos de ArcGIS: " + liveTolls.size());

        // Construir String de coordenadas: lon,lat;lon,lat;lon,lat...
        System.out.println("=== CONSTRUYENDO STRING DE COORDENADAS ===");
        StringBuilder pointsBuilder = new StringBuilder();
        for (Delivery d : deliveries) {
            if (pointsBuilder.length() > 0) pointsBuilder.append(";");
            pointsBuilder.append(String.format(Locale.US, "%f,%f", d.getLongitude(), d.getLatitude()));
        }
        System.out.println("Entregas procesadas: " + deliveries.size());

        // UNA SOLA llamada a OSRM para toda la ruta
        System.out.println("=== LLAMANDO A OSRM ===");
        String osrmUrl = "https://router.project-osrm.org/route/v1/driving/"
                + pointsBuilder.toString()
                + "?overview=full&geometries=geojson";
        System.out.println("Llamando a OSRM con " + deliveries.size() + " entregas");

        try {
            System.out.println("=== PROCESANDO RESPUESTA OSRM ===");
            Map<String, Object> response = webClient.get()
                    .uri(osrmUrl)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(60))
                    .block();

            System.out.println("Respuesta OSRM recibida: " + (response != null ? "NO NULL" : "NULL"));

            if (response != null && response.containsKey("routes")) {
                System.out.println("Respuesta OSRM contiene 'routes', procesando...");
                return processTollMatching(response, liveTolls, deliveries);
            } else {
                System.out.println("Respuesta OSRM no contiene 'routes'");
            }
        } catch (Exception e) {
            System.err.println("Error en Batch OSRM: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Retornando 0.0 - error o sin rutas");
        System.out.println("=== FIN calculateTollsForRoute ===");
        return new RouteCost(0, 0, 0);
    }

    private RouteCost processTollMatching(Map<String, Object> osrmResponse, List<Map<String, Object>> features, List<Delivery> deliveries) {
        double totalCost = 0.0;
        int peajesProcesados = 0;
        int peajesCoincidentes = 0;
        List<DetectedToll> detectedTolls = new ArrayList<>();

        try {
            List<Map<String, Object>> routes = (List<Map<String, Object>>) osrmResponse.get("routes");
            Map<String, Object> geometry = (Map<String, Object>) routes.get(0).get("geometry");
            List<List<Double>> coordinates = (List<List<Double>>) geometry.get("coordinates");

            System.out.println("Coordenadas de ruta OSRM: " + coordinates.size());
            System.out.println("=== PROCESANDO PEAJES ===");

            for (Map<String, Object> feature : features) {
                peajesProcesados++;
                Map<String, Object> attr = (Map<String, Object>) feature.get("attributes");
                Map<String, Object> geom = (Map<String, Object>) feature.get("geometry");

                double tollLon = Double.parseDouble(geom.get("x").toString());
                double tollLat = Double.parseDouble(geom.get("y").toString());

                if (peajesProcesados % 10 == 0 || peajesProcesados == features.size()) {
                    System.out.println("Procesando peaje " + peajesProcesados + "/" + features.size());
                }

                // Pre-filtro: reducir puntos OSRM al área cercana al peaje (±0.05 grados ~ 5km)
                List<List<Double>> nearbyCoords = coordinates.stream()
                        .filter(coord -> Math.abs(coord.get(1) - tollLat) < 0.05 && Math.abs(coord.get(0) - tollLon) < 0.05)
                        .collect(java.util.stream.Collectors.toList());

                // OPTIMIZACIÓN: Pre-filtro con distancia euclidiana antes de Haversine
                if (nearbyCoords.isEmpty()) continue;

                List<List<Double>> candidateCoords = nearbyCoords.stream()
                        .filter(coord -> {
                            double latDiffKm = Math.abs(coord.get(1) - tollLat) * 111;
                            double lonDiffKm = Math.abs(coord.get(0) - tollLon) * 111 * Math.cos(Math.toRadians(tollLat));
                            return (latDiffKm * latDiffKm + lonDiffKm * lonDiffKm) < 1.0;
                        })
                        .collect(java.util.stream.Collectors.toList());

                if (candidateCoords.isEmpty()) continue;

                for (List<Double> coord : candidateCoords) {
                    double pLon = coord.get(0);
                    double pLat = coord.get(1);

                    if (haversine(pLat, pLon, tollLat, tollLon) < 0.5) {
                        String tollCategory = getTollCategory(deliveries.get(0).getWeightKg());
                        String priceField = "F_cat_" + tollCategory + "_";

                        Object priceObj = attr.get(priceField);
                        if (priceObj == null) priceObj = attr.get("F_cat_1_");

                        DetectedToll detected = new DetectedToll(
                                attr.get("F_ubicacion_") != null ? attr.get("F_ubicacion_").toString() : "Peaje",
                                tollLat,
                                tollLon,
                                priceObj != null ? Double.parseDouble(priceObj.toString()) : 0.0
                        );
                        detectedTolls.add(detected);
                        totalCost += detected.getCost();
                        peajesCoincidentes++;
                        break;
                    }
                }
            }

            System.out.println("Resultado: " + peajesCoincidentes + "/" + peajesProcesados + " peajes - Costo: $" + String.format(Locale.US, "%.2f", totalCost));

        } catch (Exception e) {
            System.err.println("Error en matching: " + e.getMessage());
            e.printStackTrace();
        }

        // Extraer geometría GeoJSON para el mapa
        String geometry = "";
        try {
            List<Map<String, Object>> routes = (List<Map<String, Object>>) osrmResponse.get("routes");
            Map<String, Object> routeGeom = (Map<String, Object>) routes.get(0).get("geometry");
            geometry = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(routeGeom);
        } catch (Exception e) {
            System.err.println("Error extrayendo geometría: " + e.getMessage());
        }

        RouteCost result = new RouteCost(0, totalCost, 0);
        result.setDetectedTolls(detectedTolls);
        result.setRouteGeometry(geometry);
        return result;
    }

    // Cálculo matemático real (Fórmula de Haversine)
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radio de la Tierra en KM
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return distance;
    }

    // Determinar categoría de peaje según peso del vehículo
    private String getTollCategory(double weightKg) {
        String category;
        if (weightKg <= 3500) category = "1";
        else if (weightKg <= 8500) category = "2";
        else if (weightKg <= 12500) category = "3";
        else if (weightKg <= 20500) category = "4";
        else if (weightKg <= 26500) category = "5";
        else if (weightKg <= 32500) category = "6";
        else if (weightKg <= 38500) category = "7";
        else if (weightKg <= 44500) category = "8";
        else category = "9";
        return category;
    }
}