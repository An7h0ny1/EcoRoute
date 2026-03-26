package hackathon.EcoRoute.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetectedToll {
    private String name;
    private double latitude;
    private double longitude;
    private double cost;
}
