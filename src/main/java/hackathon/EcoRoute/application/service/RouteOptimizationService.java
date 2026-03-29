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

        List<Delivery> originalDeliveries = List.copyOf(deliveries);

        RoutingPort.RouteResult osrmResult = routingPort.optimizeAndGetRoute(deliveries);
        List<Delivery> optimizedList = osrmResult.optimizedDeliveries();
        double totalDistance = osrmResult.totalDistanceKm();
        double originalDistance = osrmResult.realOriginalDistanceKm();

        VehicleCategory category = VehicleCategory.fromWeight(optimizedList.get(0).getWeightKg());
        double fuelCost = totalDistance * category.getFuelPricePerKm();

        RouteCost cost = tollRepositoryPort.calculateTollsForRoute(
                optimizedList, osrmResult.coordinates(), osrmResult.geometryGeoJson()
        );
        cost.setFuelCost(fuelCost);
        cost.calculateTotal();

        return buildRouteResponse(originalDeliveries, optimizedList, cost, totalDistance, originalDistance, osrmResult.originalGeometryGeoJson());
    }

    private Route buildRouteResponse(List<Delivery> originalDeliveries, List<Delivery> optimizedDeliveries, RouteCost cost, double totalDist, double originalDist, String originalGeometry) {
        double savings = originalDist > totalDist ? Math.round(((originalDist - totalDist) / originalDist) * 100.0) : 0;

        Route route = new Route();
        route.setId(UUID.randomUUID().toString());
        route.setDeliveries(optimizedDeliveries);
        route.setOriginalDeliveries(originalDeliveries);
        route.setCost(cost);
        route.setTotalDistanceKm(totalDist);
        route.setRouteGeometry(cost.getRouteGeometry());
        route.setOriginalRouteGeometry(originalGeometry); // NUEVO
        route.setSavingsPercent(savings);
        route.setOriginalDistanceKm(originalDist);
        return route;
    }
}