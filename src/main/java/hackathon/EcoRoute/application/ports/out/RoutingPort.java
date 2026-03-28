package hackathon.EcoRoute.application.ports.out;

import hackathon.EcoRoute.domain.model.Delivery;

import java.util.List;

public interface RoutingPort {
    RouteResult optimizeAndGetRoute(List<Delivery> deliveries);

    // Record interno para transferir datos de forma limpia e inmutable
    record RouteResult(
            List<Delivery> optimizedDeliveries,
            String geometryGeoJson,
            List<List<Double>> coordinates,
            double totalDistanceKm
    ) {}
}
