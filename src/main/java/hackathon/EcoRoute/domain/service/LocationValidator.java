package hackathon.EcoRoute.domain.service;

import hackathon.EcoRoute.domain.model.Delivery;
import org.springframework.stereotype.Service;

@Service
public class LocationValidator {
    // Definimos los límites de operación (Colombia)
    private static final double MIN_LAT = -4.5;
    private static final double MAX_LAT = 13.0;
    private static final double MIN_LON = -79.0;
    private static final double MAX_LON = -66.0;

    public boolean isValidColombiaLocation(double lat, double lon) {
        return lat >= MIN_LAT && lat <= MAX_LAT && lon >= MIN_LON && lon <= MAX_LON;
    }

    public void validateDelivery(Delivery delivery) throws IllegalArgumentException {
        if (!isValidColombiaLocation(delivery.getLatitude(), delivery.getLongitude())) {
            throw new IllegalArgumentException("La ubicación para " + delivery.getCustomerName() + " está fuera de la zona de cobertura (Colombia).");
        }
        if (delivery.getWeightKg() < 0 || delivery.getWeightKg() > 52000) {
            throw new IllegalArgumentException("El peso de la carga es inválido para el transporte terrestre.");
        }
    }
}
