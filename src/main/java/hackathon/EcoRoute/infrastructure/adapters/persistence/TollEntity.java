package hackathon.EcoRoute.infrastructure.adapters.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "peajes")
@Getter
@Setter
public class TollEntity {
    @Id
    @Column(name = "id_peaje")
    private Integer id;

    @Column(name = "nombre")
    private String name;

    @Column(name = "lat")
    private double latitude;

    @Column(name = "lon")
    private double longitude;

    @Column(name = "estado")
    private String estado;
}
