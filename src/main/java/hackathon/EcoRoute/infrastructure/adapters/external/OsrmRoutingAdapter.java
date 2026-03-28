package hackathon.EcoRoute.infrastructure.adapters.external;

import hackathon.EcoRoute.application.ports.out.RoutingPort;
import hackathon.EcoRoute.domain.model.Delivery;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OsrmRoutingAdapter implements RoutingPort {

    private final WebClient webClient = WebClient.builder().build();

    @Override
    public RouteResult optimizeAndGetRoute(List<Delivery> deliveries) {
        System.out.println("[OsrmRoutingAdapter] Solicitando matriz TSP al engine OSRM...");

        String waypointString = deliveries.stream()
                .map(d -> String.format(java.util.Locale.US, "%f,%f", d.getLongitude(), d.getLatitude()))
                .collect(Collectors.joining(";"));

        // Usamos /trip para resolver el Agente Viajero. Source=first mantiene la bodega como origen.
        String url = "https://router.project-osrm.org/trip/v1/driving/" + waypointString +
                "?roundtrip=false&source=first&overview=full&geometries=geojson";

        try {
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            return parseResponse(response, deliveries);
        } catch (Exception e) {
            throw new RuntimeException("Error comunicando con OSRM Engine: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private RouteResult parseResponse(Map<String, Object> response, List<Delivery> originalDeliveries) throws Exception {
        List<Map<String, Object>> trips = (List<Map<String, Object>>) response.get("trips");
        List<Map<String, Object>> waypoints = (List<Map<String, Object>>) response.get("waypoints");

        // 1. Extraer geometría real y distancia
        Map<String, Object> geometry = (Map<String, Object>) trips.get(0).get("geometry");
        List<List<Double>> coords = (List<List<Double>>) geometry.get("coordinates");
        double distanceKm = ((Number) trips.get(0).get("distance")).doubleValue() / 1000.0;
        String geometryJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(geometry);

        // 2. Reordenar entregas según el índice óptimo devuelto por OSRM
        Delivery[] orderedDeliveries = new Delivery[originalDeliveries.size()];
        for (int i = 0; i < waypoints.size(); i++) {
            int optimizedIndex = ((Number) waypoints.get(i).get("waypoint_index")).intValue();
            orderedDeliveries[optimizedIndex] = originalDeliveries.get(i);
        }

        return new RouteResult(List.of(orderedDeliveries), geometryJson, coords, distanceKm);
    }
}