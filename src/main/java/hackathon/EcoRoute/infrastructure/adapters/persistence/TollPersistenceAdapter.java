package hackathon.EcoRoute.infrastructure.adapters.persistence;

import hackathon.EcoRoute.application.ports.out.TollRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TollPersistenceAdapter implements TollRepositoryPort {

    private final JpaTollRepository jpaTollRepository;

    @Override
    public double calculateTollsForRoute(double startLat, double startLon, double endLat, double endLon) {
        // Por ahora, para el MVP, sumamos el precio de todos los peajes
        // registrados en la base de datos para simular el costo.
        // En el Paso 9 lo haremos más preciso por coordenadas.
        return jpaTollRepository.findAll()
                .stream()
                .mapToDouble(TollEntity::getPrice)
                .sum();
    }
}
