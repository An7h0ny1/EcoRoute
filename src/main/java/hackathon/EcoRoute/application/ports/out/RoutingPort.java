package hackathon.EcoRoute.application.ports.out;

import hackathon.EcoRoute.domain.model.Delivery;

import java.util.List;

public interface RoutingPort {
    RouteResult optimizeAndGetRoute(List<Delivery> deliveries, String profile);

    record RouteResult(
            List<Delivery> optimizedDeliveries,
            String geometryGeoJson,
            List<List<Double>> coordinates,
            double totalDistanceKm,
            String originalGeometryGeoJson,
            List<List<Double>> originalCoordinates,
            double realOriginalDistanceKm
    ) {}
}
