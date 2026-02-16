package ca.uhn.fhir.jpa.dao.data;

import ca.uhn.fhir.jpa.model.entity.StatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IStatisticsDao extends JpaRepository<StatisticsEntity, Long>, IHapiFhirJpaRepository {
  
}
