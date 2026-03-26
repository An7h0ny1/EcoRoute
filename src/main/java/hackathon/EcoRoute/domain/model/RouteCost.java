package hackathon.EcoRoute.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteCost {
    private double fuelCost;
    private double tollCost;
    private double total;
    private List<DetectedToll> detectedTolls = new ArrayList<>();
    private String routeGeometry; // GeoJSON para el mapa

    public RouteCost(double fuelCost, double tollCost, double ignored) {
        this.fuelCost = fuelCost;
        this.tollCost = tollCost;
        this.detectedTolls = new ArrayList<>();
    }

    public void calculateTotal() {
        this.total = this.fuelCost + this.tollCost;
    }
}
