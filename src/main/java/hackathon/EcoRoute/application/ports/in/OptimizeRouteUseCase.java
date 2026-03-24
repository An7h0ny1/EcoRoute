package hackathon.EcoRoute.application.ports.in;

import hackathon.EcoRoute.domain.model.Delivery;
import hackathon.EcoRoute.domain.model.Route;

import java.util.List;

public interface OptimizeRouteUseCase {
    Route execute(List<Delivery> deliveries);
}
