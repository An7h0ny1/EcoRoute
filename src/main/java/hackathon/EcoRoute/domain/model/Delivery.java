package hackathon.EcoRoute.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {
    private String id;
    private String address;
    private double latitude;
    private double longitude;
    private String customerName;
    private double weightKg;
}
