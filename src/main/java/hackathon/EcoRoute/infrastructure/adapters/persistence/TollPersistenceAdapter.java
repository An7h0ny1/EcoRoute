package hackathon.EcoRoute.infrastructure.adapters.persistence;

import hackathon.EcoRoute.application.ports.out.TollRepositoryPort;
import hackathon.EcoRoute.domain.model.Delivery;
import hackathon.EcoRoute.domain.model.DetectedToll;
import hackathon.EcoRoute.domain.model.RouteCost;
import hackathon.EcoRoute.domain.model.VehicleCategory;
import hackathon.EcoRoute.domain.service.TollMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TollPersistenceAdapter implements TollRepositoryPort {

    private final JpaTollRepository jpaTollRepository;
    private final TollMatcher tollMatcher;

    @Override
    public RouteCost calculateTollsForRoute(List<Delivery> deliveries, List<List<Double>> routeCoords, String geometryGeoJson) {
        System.out.println("[TollPersistenceAdapter] START calculateTollsForRoute");
        
        if (deliveries == null || deliveries.isEmpty() || routeCoords == null) {
            System.out.println("[TollPersistenceAdapter] Invalid inputs, returning empty RouteCost");
            return new RouteCost(0, 0, 0);
        }

        System.out.println("[TollPersistenceAdapter] Deliveries count: " + deliveries.size());
        System.out.println("[TollPersistenceAdapter] Route coordinates count: " + routeCoords.size());

        VehicleCategory category = VehicleCategory.fromWeight(deliveries.get(0).getWeightKg());
        System.out.println("[TollPersistenceAdapter] Vehicle weight: " + deliveries.get(0).getWeightKg() + " kg");
        System.out.println("[TollPersistenceAdapter] Vehicle category: " + category);

        System.out.println("[TollPersistenceAdapter] Finding candidate tolls by route...");
        List<TollWithPriceDTO> candidates = findCandidateTollsByRoute(routeCoords, category);
        System.out.println("[TollPersistenceAdapter] Candidates found: " + candidates.size());

        System.out.println("[TollPersistenceAdapter] Performing toll matching on route...");
        List<DetectedToll> detected = tollMatcher.findTollsOnRoute(routeCoords, candidates);
        System.out.println("[TollPersistenceAdapter] Detected tolls: " + detected.size());

        for (DetectedToll toll : detected) {
            System.out.println("[TollPersistenceAdapter] Detected toll: " + toll.getName() + " - Cost: " + toll.getCost());
        }

        RouteCost cost = new RouteCost();
        double totalCost = detected.stream().mapToDouble(DetectedToll::getCost).sum();
        cost.setTollCost(totalCost);
        cost.setDetectedTolls(detected);
        cost.setRouteGeometry(geometryGeoJson); // Geometría inyectada, no calculada aquí

        System.out.println("[TollPersistenceAdapter] Total toll cost: " + totalCost);
        System.out.println("[TollPersistenceAdapter] END calculateTollsForRoute");

        return cost;
    }

    private List<TollWithPriceDTO> findCandidateTollsByRoute(List<List<Double>> routeCoords, VehicleCategory category) {
        System.out.println("[TollPersistenceAdapter] findCandidateTollsByRoute - Calculating bounding box...");

        double minLat = routeCoords.stream().mapToDouble(c -> c.get(1)).min().orElse(0) - 0.2;
        double maxLat = routeCoords.stream().mapToDouble(c -> c.get(1)).max().orElse(0) + 0.2;
        double minLon = routeCoords.stream().mapToDouble(c -> c.get(0)).min().orElse(0) - 0.2;
        double maxLon = routeCoords.stream().mapToDouble(c -> c.get(0)).max().orElse(0) + 0.2;

        System.out.println("[TollPersistenceAdapter] Bounding box: minLat=" + minLat + ", maxLat=" + maxLat + ", minLon=" + minLon + ", maxLon=" + maxLon);
        System.out.println("[TollPersistenceAdapter] Compatible codes: " + category.getCompatibleCodes());

        System.out.println("[TollPersistenceAdapter] Executing native query...");
        List<Map<String, Object>> result = jpaTollRepository.findTollsWithPriceNative(
                minLat, maxLat, minLon, maxLon, category.getCompatibleCodes()
        );
        System.out.println("[TollPersistenceAdapter] Query returned " + result.size() + " rows");

        System.out.println("[TollPersistenceAdapter] Mapping results to DTOs...");
        return result.stream().map(row -> new TollWithPriceDTO(
                (Integer) row.get("id"), (String) row.get("name"),
                ((Number) row.get("latitude")).doubleValue(), ((Number) row.get("longitude")).doubleValue(),
                ((Number) row.get("price")).doubleValue()
        )).collect(Collectors.toList());
    }
}