package hackathon.EcoRoute.infrastructure.adapters.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hackathon.EcoRoute.application.ports.out.RoutingPort;
import hackathon.EcoRoute.domain.model.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsrmRoutingAdapter implements RoutingPort {

    @Value("${ors.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String ORS_BASE_URL = "https://api.openrouteservice.org";

    @Override
    public RouteResult optimizeAndGetRoute(List<Delivery> deliveries, String profile) {
        try {
            // OBTENER ORDEN OPTIMIZADO (VROOM)
            List<Integer> optimizedIndices = getOptimizedSequence(deliveries, profile);

            List<Delivery> optimizedList = new ArrayList<>();
            for (Integer idx : optimizedIndices) {
                optimizedList.add(deliveries.get(idx));
            }

            // OBTENER GEOMETRÍA RUTA OPTIMIZADA
            Map<String, Object> optimizedData = getRouteData(optimizedList, profile);

            // OBTENER GEOMETRÍA RUTA ORIGINAL
            Map<String, Object> originalData = getRouteData(deliveries, profile);

            return new RouteResult(
                    optimizedList,
                    (String) optimizedData.get("geometry"),
                    (List<List<Double>>) optimizedData.get("coords"),
                    (Double) optimizedData.get("distance"),
                    (String) originalData.get("geometry"),
                    (List<List<Double>>) originalData.get("coords"),
                    (Double) originalData.get("distance")
            );

        } catch (Exception e) {
            log.error("Error en OpenRouteService: {}", e.getMessage());
            throw new RuntimeException("Error al calcular la ruta optimizada con ORS.");
        }
    }

    private List<Integer> getOptimizedSequence(List<Delivery> deliveries, String profile) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode vehicles = root.putArray("vehicles");
        ObjectNode vehicle = vehicles.addObject();
        vehicle.put("id", 1);
        vehicle.put("profile", profile); // DINÁMICO
        ArrayNode start = vehicle.putArray("start");
        start.add(deliveries.get(0).getLongitude()).add(deliveries.get(0).getLatitude());

        ArrayNode jobs = root.putArray("jobs");
        for (int i = 1; i < deliveries.size(); i++) {
            ObjectNode job = jobs.addObject();
            job.put("id", i);
            ArrayNode loc = job.putArray("location");
            loc.add(deliveries.get(i).getLongitude()).add(deliveries.get(i).getLatitude());
        }

        JsonNode response = postToOrs("/optimization", root);
        List<Integer> sequence = new ArrayList<>();
        sequence.add(0);

        JsonNode steps = response.get("routes").get(0).get("steps");
        for (JsonNode step : steps) {
            if (step.get("type").asText().equals("job")) {
                sequence.add(step.get("id").asInt());
            }
        }
        return sequence;
    }

    private Map<String, Object> getRouteData(List<Delivery> list, String profile) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode coords = root.putArray("coordinates");
        for (Delivery d : list) {
            coords.addArray().add(d.getLongitude()).add(d.getLatitude());
        }

        JsonNode response = postToOrs("/v2/directions/" + profile + "/geojson", root);
        JsonNode feature = response.get("features").get(0);

        double distance = feature.get("properties").get("summary").get("distance").asDouble() / 1000.0;
        JsonNode geometry = feature.get("geometry");

        List<List<Double>> coordsList = new ArrayList<>();
        geometry.get("coordinates").forEach(c -> coordsList.add(List.of(c.get(0).asDouble(), c.get(1).asDouble())));

        return Map.of(
                "distance", distance,
                "geometry", mapper.writeValueAsString(geometry),
                "coords", coordsList
        );
    }

    private JsonNode postToOrs(String endpoint, ObjectNode body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
        String responseString = restTemplate.postForObject(ORS_BASE_URL + endpoint, entity, String.class);

        try {
            return mapper.readTree(responseString);
        } catch (Exception e) {
            log.error("Error al parsear respuesta JSON de ORS: {}", e.getMessage());
            throw new RuntimeException("Respuesta de ORS malformada");
        }
    }
}