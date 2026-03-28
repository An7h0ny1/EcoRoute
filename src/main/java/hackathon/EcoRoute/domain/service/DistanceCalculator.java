package hackathon.EcoRoute.domain.service;

import hackathon.EcoRoute.domain.model.Delivery;
import org.springframework.stereotype.Service;

@Service
public class DistanceCalculator {
    private static final double EARTH_RADIUS_KM = 6371.0;

    public double calculateHaversine(Delivery d1, Delivery d2) {
        return calculateHaversine(d1.getLatitude(), d1.getLongitude(), d2.getLatitude(), d2.getLongitude());
    }

    public double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
