// En: src/main/java/hackathon/EcoRoute/application/service/RouteOptimizationService.java
package hackathon.EcoRoute.application.service;

import hackathon.EcoRoute.application.ports.in.OptimizeRouteUseCase;
import hackathon.EcoRoute.application.ports.out.RoutingPort;
import hackathon.EcoRoute.application.ports.out.TollRepositoryPort;
import hackathon.EcoRoute.domain.model.Delivery;
import hackathon.EcoRoute.domain.model.Route;
import hackathon.EcoRoute.domain.model.RouteCost;
import hackathon.EcoRoute.domain.model.VehicleCategory;
import hackathon.EcoRoute.domain.service.DistanceCalculator;
import hackathon.EcoRoute.domain.service.LocationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService implements OptimizeRouteUseCase {

    private final TollRepositoryPort tollRepositoryPort;
    private final RoutingPort routingPort;
    private final DistanceCalculator distanceCalculator;
    private final LocationValidator locationValidator;

    @Override
    public Route execute(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) return new Route();
        deliveries.forEach(locationValidator::validateDelivery);

        // 1. Distancia sin optimizar (Ajustada con factor de tortuosidad colombiana 1.4)
        double originalDistance = calculateOriginalHaversineDistance(deliveries);

        // 2. MAGIA: OSRM Engine calcula la matriz TSP y el orden perfecto por carretera
        RoutingPort.RouteResult osrmResult = routingPort.optimizeAndGetRoute(deliveries);
        List<Delivery> optimizedList = osrmResult.optimizedDeliveries();
        double totalDistance = osrmResult.totalDistanceKm();

        // 3. Métricas Financieras
        VehicleCategory category = VehicleCategory.fromWeight(optimizedList.get(0).getWeightKg());
        double fuelCost = totalDistance * category.getFuelPricePerKm();

        // 4. Peajes (Pasamos la geometría para no recalcularla)
        RouteCost cost = tollRepositoryPort.calculateTollsForRoute(
                optimizedList, osrmResult.coordinates(), osrmResult.geometryGeoJson()
        );
        cost.setFuelCost(fuelCost);
        cost.calculateTotal();

        return buildRouteResponse(optimizedList, cost, totalDistance, originalDistance);
    }

    private double calculateOriginalHaversineDistance(List<Delivery> list) {
        double total = 0.0;
        for (int i = 0; i < list.size() - 1; i++) {
            total += distanceCalculator.calculateHaversine(list.get(i), list.get(i + 1));
        }
        return total * 1.4; // Ajuste asimilando montañas en lugar de línea recta pura
    }

    private Route buildRouteResponse(List<Delivery> deliveries, RouteCost cost, double totalDist, double originalDist) {
        double savings = originalDist > totalDist ? Math.round(((originalDist - totalDist) / originalDist) * 100.0) : 0;

        Route route = new Route();
        route.setId(UUID.randomUUID().toString());
        route.setDeliveries(deliveries);
        route.setCost(cost);
        route.setTotalDistanceKm(totalDist);
        route.setRouteGeometry(cost.getRouteGeometry());
        route.setSavingsPercent(savings);
        route.setOriginalDistanceKm(originalDist);
        return route;
    }
}