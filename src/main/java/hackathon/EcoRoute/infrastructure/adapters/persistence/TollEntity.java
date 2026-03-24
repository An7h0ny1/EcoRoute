package hackathon.EcoRoute.infrastructure.adapters.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tolls")
@Getter
@Setter
public class TollEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;
    private double latitude;
    private double longitude;
}
