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

        String waypointString = deliveries.stream()
                .map(d -> String.format(java.util.Locale.US, "%f,%f", d.getLongitude(), d.getLatitude()))
                .collect(Collectors.joining(";"));

        //URL para optimizar (TSP)
        String tripUrl = "https://router.project-osrm.org/trip/v1/driving/" + waypointString +
                "?roundtrip=false&source=first&overview=full&geometries=geojson";

        // URL para la ruta original (tal cual viene en el CSV, por carretera)
        String routeUrl = "https://router.project-osrm.org/route/v1/driving/" + waypointString +
                "?overview=full&geometries=geojson";

        try {
            Map<String, Object> tripResponse = webClient.get().uri(tripUrl).retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}).block();

            Map<String, Object> routeResponse = webClient.get().uri(routeUrl).retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}).block();

            return parseResponse(tripResponse, routeResponse, deliveries);
        } catch (Exception e) {
            throw new RuntimeException("Error comunicando con OSRM Engine: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private RouteResult parseResponse(Map<String, Object> tripResponse, Map<String, Object> routeResponse, List<Delivery> originalDeliveries) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        //Extraer datos optimizados
        List<Map<String, Object>> trips = (List<Map<String, Object>>) tripResponse.get("trips");
        List<Map<String, Object>> waypoints = (List<Map<String, Object>>) tripResponse.get("waypoints");
        Map<String, Object> geometry = (Map<String, Object>) trips.get(0).get("geometry");
        List<List<Double>> coords = (List<List<Double>>) geometry.get("coordinates");
        double distanceKm = ((Number) trips.get(0).get("distance")).doubleValue() / 1000.0;
        String geometryJson = mapper.writeValueAsString(geometry);

        //Extraer datos originales por carretera
        List<Map<String, Object>> routes = (List<Map<String, Object>>) routeResponse.get("routes");
        Map<String, Object> origGeometry = (Map<String, Object>) routes.get(0).get("geometry");
        double originalDistanceKm = ((Number) routes.get(0).get("distance")).doubleValue() / 1000.0;
        String originalGeometryJson = mapper.writeValueAsString(origGeometry);

        //Reordenar entregas
        Delivery[] orderedDeliveries = new Delivery[originalDeliveries.size()];
        for (int i = 0; i < waypoints.size(); i++) {
            int optimizedIndex = ((Number) waypoints.get(i).get("waypoint_index")).intValue();
            orderedDeliveries[optimizedIndex] = originalDeliveries.get(i);
        }

        return new RouteResult(List.of(orderedDeliveries), geometryJson, coords, distanceKm, originalGeometryJson, originalDistanceKm);
    }
}