package hackathon.EcoRoute.application.service;

import hackathon.EcoRoute.application.ports.in.OptimizeRouteUseCase;
import hackathon.EcoRoute.application.ports.out.TollRepositoryPort;
import hackathon.EcoRoute.domain.model.Delivery;
import hackathon.EcoRoute.domain.model.Route;
import hackathon.EcoRoute.domain.model.RouteCost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService implements OptimizeRouteUseCase {

    private final TollRepositoryPort tollRepositoryPort;
    private static final Map<String, Double> FUEL_PRICE_PER_KM_BY_CATEGORY = Map.of(
            "1", 900.0,   // Automóviles, motos, camionetas (hasta 3.500 kg)
            "2", 1400.0,  // Buses, camiones pequeños (hasta 8.500 kg)
            "3", 1900.0,  // Camiones medianos (hasta 12.500 kg)
            "4", 2400.0,  // Camiones grandes y tractocamiones (más de 12.500 kg)
            "5", 2400.0,
            "6", 2400.0,
            "7", 2400.0,
            "8", 2400.0,
            "9", 2400.0
    );

    @Override
    public Route execute(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) return new Route();

        double originalDistance = calculateTotalDistance(deliveries);
        // 1. Algoritmo Greedy para ordenar paradas por cercanía (Tu lógica actual)
        List<Delivery> optimizedList = new ArrayList<>();
        List<Delivery> remaining = new ArrayList<>(deliveries);
        Delivery current = remaining.remove(0);
        optimizedList.add(current);

        while (!remaining.isEmpty()) {
            Delivery next = findClosest(current, remaining);
            optimizedList.add(next);
            remaining.remove(next);
            current = next;
        }

        // 2. Cálculo de Distancia Real (Aquí podrías sumar distancias de OSRM luego)
        double totalDistance = calculateTotalDistance(optimizedList);
        String vehicleCategory = getTollCategory(optimizedList.get(0).getWeightKg());
        double fuelPricePerKm = FUEL_PRICE_PER_KM_BY_CATEGORY.getOrDefault(vehicleCategory, 900.0);
        double fuelCost = totalDistance * fuelPricePerKm;

        // 3. ¡EL MOMENTO DE LA VERDAD!: Cálculo de peajes con datos de ArcGIS y OSRM
        // Pasamos la lista OPTIMIZADA para que el adaptador analice el trayecto real
        RouteCost cost = tollRepositoryPort.calculateTollsForRoute(optimizedList);

        // 4. Construcción del objeto de costo
        cost.setFuelCost(fuelCost);
        cost.calculateTotal();

        Route route = new Route();
        route.setId(UUID.randomUUID().toString());
        route.setDeliveries(optimizedList);
        route.setCost(cost);
        route.setTotalDistanceKm(totalDistance);
        route.setRouteGeometry(cost.getRouteGeometry());

        return route;
    }

    // 1. Distancia Real Acumulada (Ya no es size * 5.0)
    private double calculateTotalDistance(List<Delivery> deliveries) {
        double total = 0.0;
        for (int i = 0; i < deliveries.size() - 1; i++) {
            total += calculateHaversine(deliveries.get(i), deliveries.get(i + 1));
        }
        return total;
    }

    // 2. Buscador del más cercano usando Haversine real
    private Delivery findClosest(Delivery current, List<Delivery> others) {
        return others.stream()
                .min((d1, d2) -> Double.compare(
                        calculateHaversine(current, d1),
                        calculateHaversine(current, d2)))
                .get();
    }

    // 3. Fórmula de Haversine (Matemática real, cero inventos)
    private double calculateHaversine(Delivery d1, Delivery d2) {
        double R = 6371; // Radio de la Tierra en km
        double dLat = Math.toRadians(d2.getLatitude() - d1.getLatitude());
        double dLon = Math.toRadians(d2.getLongitude() - d1.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(d1.getLatitude())) * Math.cos(Math.toRadians(d2.getLatitude())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String getTollCategory(double weightKg) {
        System.out.println("Determinando categoría de peaje para peso: " + weightKg + " kg");

        String category;
        if (weightKg <= 3500) category = "1";          // Automóviles, motos, camionetas
        else if (weightKg <= 8500) category = "2";      // Buses, camiones pequeños
        else if (weightKg <= 12500) category = "3";     // Camiones medianos
        else if (weightKg <= 20500) category = "4";     // Camiones grandes
        else if (weightKg <= 26500) category = "5";     // Tractocamiones 2 ejes
        else if (weightKg <= 32500) category = "6";     // Tractocamiones 3 ejes
        else if (weightKg <= 38500) category = "7";     // Tractocamiones 4 ejes
        else if (weightKg <= 44500) category = "8";     // Tractocamiones 5 ejes
        else category = "9";                            // Tractocamiones 6+ ejes

        System.out.println("Categoría asignada: " + category);
        return category;
    }
}
