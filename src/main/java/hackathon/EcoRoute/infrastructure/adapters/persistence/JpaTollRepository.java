package hackathon.EcoRoute.infrastructure.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTollRepository extends JpaRepository<TollEntity, Long> {
    // Aquí podrías añadir métodos de búsqueda por cercanía geográfica si fuera necesario
}
