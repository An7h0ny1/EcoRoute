package hackathon.EcoRoute.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteCost {
    private double fuelCost;
    private double tollCost;
    private double totalCost;

    public void calculateTotal() {
        this.totalCost = this.fuelCost + this.tollCost;
    }
}
