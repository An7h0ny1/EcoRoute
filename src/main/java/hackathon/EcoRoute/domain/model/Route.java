package hackathon.EcoRoute.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Route {
    private String id;
    private List<Delivery> deliveries;
    private List<Delivery> originalDeliveries;
    private RouteCost cost;
    private double totalDistanceKm;
    private String routeGeometry;
    private double savingsPercent;
    private double originalDistanceKm;
    private String originalRouteGeometry;
    private String vehicleType;
    private double totalSavingsCop;
    private double originalTotalCost;
}
