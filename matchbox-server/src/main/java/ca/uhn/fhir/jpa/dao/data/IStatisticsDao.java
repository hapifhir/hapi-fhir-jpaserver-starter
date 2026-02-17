package ca.uhn.fhir.jpa.dao.data;

import ca.uhn.fhir.jpa.model.entity.StatisticsEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@ConditionalOnProperty(prefix = "matchbox.validation", name = "save-statistics", havingValue = "true")
public interface IStatisticsDao extends JpaRepository<StatisticsEntity, Long>, IHapiFhirJpaRepository { 
  
  @Query("SELECT e FROM StatisticsEntity e WHERE e.timestamp >= :from AND e.timestamp <= :to")
  List<StatisticsEntity> searchByDate(@Param("from") Instant from, @Param("to") Instant to);
}
