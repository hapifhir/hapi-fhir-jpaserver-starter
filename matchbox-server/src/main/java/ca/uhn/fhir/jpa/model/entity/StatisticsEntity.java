package ca.uhn.fhir.jpa.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "MATCHBOX_STATISTICS")
public class StatisticsEntity {
  
  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  
  @Setter
  @Getter
  @Column(name = "PROFILE_URL", length = 255, nullable = false)
  private String profile;

  @Setter
  @Getter
  @Column(name = "PACKAGES", length = 16535, nullable = false)
  private String packages;

  @Setter
  @Getter
  @Column(name = "NUMBER_OF_INFORMATIONS", nullable = false)
  private Integer numberOfInfos;

  @Setter
  @Getter
  @Column(name = "NUMBER_OF_WARNINGS", nullable = false)
  private Integer numberOfWarnings;

  @Setter
  @Getter
  @Column(name = "NUMBER_OF_ERRORS", nullable = false)
  private Integer numberOfErrors;

  @Setter
  @Getter
  @Column(name = "NUMBER_OF_FATALS", nullable = false)
  private Integer numberOfFatals;

  @Setter
  @Getter
  @Column(name = "TIMESTAMP", nullable = false)
  private LocalDateTime timestamp;

  @Setter
  @Getter
  @Column(name = "DURATION", nullable = false)
  private Long durationMillis;

  @Setter
  @Getter
  @Column(name = "AI_USED", nullable = false)
  private Boolean aiUsed;
  
  @Setter
  @Getter
  @Column(name = "SUCCESS", nullable = false)
  private Boolean success;

}
