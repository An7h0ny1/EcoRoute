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
        
        if (deliveries == null || deliveries.isEmpty() || routeCoords == null) {
            return new RouteCost(0, 0, 0);
        }

        VehicleCategory category = VehicleCategory.fromWeight(deliveries.get(0).getWeightKg());

        List<TollWithPriceDTO> candidates = findCandidateTollsByRoute(routeCoords, category);

        List<DetectedToll> detected = tollMatcher.findTollsOnRoute(routeCoords, candidates);

        RouteCost cost = new RouteCost();
        double totalCost = detected.stream().mapToDouble(DetectedToll::getCost).sum();
        cost.setTollCost(totalCost);
        cost.setDetectedTolls(detected);
        cost.setRouteGeometry(geometryGeoJson);

        return cost;
    }

    private List<TollWithPriceDTO> findCandidateTollsByRoute(List<List<Double>> routeCoords, VehicleCategory category) {

        double minLat = routeCoords.stream().mapToDouble(c -> c.get(1)).min().orElse(0) - 0.2;
        double maxLat = routeCoords.stream().mapToDouble(c -> c.get(1)).max().orElse(0) + 0.2;
        double minLon = routeCoords.stream().mapToDouble(c -> c.get(0)).min().orElse(0) - 0.2;
        double maxLon = routeCoords.stream().mapToDouble(c -> c.get(0)).max().orElse(0) + 0.2;

        List<Map<String, Object>> result = jpaTollRepository.findTollsWithPriceNative(
                minLat, maxLat, minLon, maxLon, category.getCompatibleCodes()
        );
        return result.stream().map(row -> new TollWithPriceDTO(
                (Integer) row.get("id"), (String) row.get("name"),
                ((Number) row.get("latitude")).doubleValue(), ((Number) row.get("longitude")).doubleValue(),
                ((Number) row.get("price")).doubleValue()
        )).collect(Collectors.toList());
    }
}