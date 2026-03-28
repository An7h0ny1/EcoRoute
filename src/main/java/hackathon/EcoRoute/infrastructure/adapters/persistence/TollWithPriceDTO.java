package hackathon.EcoRoute.infrastructure.adapters.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TollWithPriceDTO {
    private Integer id;
    private String name;
    private double latitude;
    private double longitude;
    private double price2026;
}
