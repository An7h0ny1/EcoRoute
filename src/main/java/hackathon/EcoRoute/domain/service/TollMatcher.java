package hackathon.EcoRoute.domain.service;

import hackathon.EcoRoute.domain.model.DetectedToll;
import hackathon.EcoRoute.infrastructure.adapters.persistence.TollWithPriceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TollMatcher {
    private final DistanceCalculator distanceCalculator;

    // Radio de Alta Precisión: 150 metros.
    private static final double DETECTION_RADIUS_KM = 0.15;
    private static final double FAST_BOUNDING_BOX_DEG = 0.015;

    public List<DetectedToll> findTollsOnRoute(List<List<Double>> coordinates, List<TollWithPriceDTO> candidates) {
        List<DetectedToll> detected = new ArrayList<>();

        for (TollWithPriceDTO toll : candidates) {
            //Filtrar peajes sin costo
            if (toll.getPrice2026() <= 0) {
                continue;
            }

            if (isTollNearRouteExact(toll, coordinates)) {
                detected.add(new DetectedToll(
                        toll.getName(),
                        toll.getLatitude(),
                        toll.getLongitude(),
                        toll.getPrice2026()
                ));
            }
        }
        return detected;
    }

    private boolean isTollNearRouteExact(TollWithPriceDTO toll, List<List<Double>> routePoints) {
        return routePoints.stream()
                .parallel()
                .anyMatch(point -> {
                    double pointLon = point.get(0);
                    double pointLat = point.get(1);

                    // Pre-filtro matemático (ahorra CPU)
                    if (Math.abs(pointLat - toll.getLatitude()) > FAST_BOUNDING_BOX_DEG ||
                            Math.abs(pointLon - toll.getLongitude()) > FAST_BOUNDING_BOX_DEG) {
                        return false;
                    }

                    // Cálculo Haversine de precisión
                    double distance = distanceCalculator.calculateHaversine(
                            pointLat, pointLon, toll.getLatitude(), toll.getLongitude());

                    return distance <= DETECTION_RADIUS_KM;
                });
    }
}