package hackathon.EcoRoute.application.service;

import hackathon.EcoRoute.application.ports.in.OptimizeRouteUseCase;
import hackathon.EcoRoute.application.ports.out.TollRepositoryPort;
import hackathon.EcoRoute.domain.model.Delivery;
import hackathon.EcoRoute.domain.model.Route;
import hackathon.EcoRoute.domain.model.RouteCost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService implements OptimizeRouteUseCase {

    private final TollRepositoryPort tollRepositoryPort;
    private static final double FUEL_PRICE_PER_KM = 500.0; // Valor base en COP

    @Override
    public Route execute(List<Delivery> deliveries) {
        // En este paso del MVP, asumimos que el orden recibido es el inicial
        // Más adelante implementaremos el algoritmo de ordenamiento

        double totalDistance = calculateTotalDistance(deliveries);
        double fuelCost = totalDistance * FUEL_PRICE_PER_KM;

        // Simulación inicial de peajes usando el puerto de salida
        double tollCost = 0;
        if (deliveries.size() > 1) {
            Delivery first = deliveries.get(0);
            Delivery last = deliveries.get(deliveries.size() - 1);
            tollCost = tollRepositoryPort.calculateTollsForRoute(
                    first.getLatitude(), first.getLongitude(),
                    last.getLatitude(), last.getLongitude()
            );
        }

        RouteCost cost = new RouteCost(fuelCost, tollCost, 0);
        cost.calculateTotal();

        Route route = new Route();
        route.setId(UUID.randomUUID().toString());
        route.setDeliveries(deliveries);
        route.setCost(cost);
        route.setTotalDistanceKm(totalDistance);

        return route;
    }

    private double calculateTotalDistance(List<Delivery> deliveries) {
        // Lógica simple de distancia acumulada para el MVP inicial
        return deliveries.size() * 5.0; // Simulación: 5km por entrega
    }
}
