package hackathon.EcoRoute.infrastructure.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface JpaTollRepository extends JpaRepository<TollEntity, Integer> {

    // LEFT JOIN garantiza que el peaje sale sí o sí.
    // Eliminamos GROUP BY para que todos los peajes que cumplan las condiciones aparezcan.
    // COALESCE para que si no hay tarifa, el precio sea 0.
    @Query(value = "SELECT p.id_peaje as id, p.nombre as name, p.lat as latitude, p.lon as longitude, " +
            "COALESCE(MAX(t.valor_2026), 0) as price " +
            "FROM peajes p " +
            "LEFT JOIN tarifas t ON p.id_peaje = t.id_peaje AND t.id_categoria IN (:catIds) " +
            "WHERE p.lat BETWEEN :minLat AND :maxLat " +
            "AND p.lon BETWEEN :minLon AND :maxLon " +
            "GROUP BY p.id_peaje, p.nombre, p.lat, p.lon",
            nativeQuery = true)
    List<Map<String, Object>> findTollsWithPriceNative(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon,
            @Param("catIds") List<String> catIds);
}