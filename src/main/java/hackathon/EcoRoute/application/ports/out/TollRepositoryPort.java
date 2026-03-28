package hackathon.EcoRoute.application.ports.out;

import hackathon.EcoRoute.domain.model.Delivery;
import hackathon.EcoRoute.domain.model.RouteCost;

import java.util.List;


public interface TollRepositoryPort {
    RouteCost calculateTollsForRoute(List<Delivery> deliveries, List<List<Double>> routeCoordinates, String geometryGeoJson);
}
