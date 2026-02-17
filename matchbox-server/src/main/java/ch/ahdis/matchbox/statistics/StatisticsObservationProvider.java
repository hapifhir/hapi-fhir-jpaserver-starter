package ch.ahdis.matchbox.statistics;

import ca.uhn.fhir.jpa.dao.data.IStatisticsDao;
import ca.uhn.fhir.jpa.model.entity.StatisticsEntity;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class StatisticsObservationProvider implements IResourceProvider {

	private final IStatisticsDao statisticsDao;

	public StatisticsObservationProvider(final IStatisticsDao statisticsDao) {
		this.statisticsDao = statisticsDao;
	}

	// GET /fhir/Observation?date=lt2027&date=gt2025
	// https://hl7.org/fhir/R4/search.html#date
	@Search
	@Transactional
	public List<Observation> searchStatistics(@RequiredParam(name = "date") final DateRangeParam dateRange) {
		try {
			return this.searchObservations(dateRange);
		} catch (final Exception e) {
			OperationOutcome operationOutcome = new OperationOutcome();
			operationOutcome.addIssue()
				.setSeverity(OperationOutcome.IssueSeverity.ERROR)
				.setCode(OperationOutcome.IssueType.EXCEPTION)
				.setDiagnostics(e.getMessage());

			throw new InternalErrorException("message", operationOutcome);

		}
	}


	private List<Observation> searchObservations(final DateRangeParam dateRange) {
		// initialize from and to variables with default values
		Instant from = Instant.parse("2000-01-01T00:00:00Z");
		Instant to = Instant.now();

		// assign lower and upper bounds if they exist
		if (dateRange.getLowerBoundAsInstant() != null) {
			from = dateRange.getLowerBoundAsInstant().toInstant();
		}
		if (dateRange.getUpperBoundAsInstant() != null) {
			to = dateRange.getUpperBoundAsInstant().toInstant();
		}

		// use searchByDate function in IStatisticsDao
		List<StatisticsEntity> filteredStatistics = this.statisticsDao.searchByDate(from, to);

		// initialize Observation list
		List<Observation> observations = new ArrayList<>();

		// map each entity to an observation and add to observation list
		for (StatisticsEntity entity : filteredStatistics) {
			Observation observation = new Observation();
			observation.setId(UUID.randomUUID().toString());

			observation.setCode(new CodeableConcept().setText("Validation statistic"));
			observation.setEffective(new InstantType(Date.from(entity.getTimestamp())));
			observation.setStatus(Observation.ObservationStatus.FINAL);

			observation.addComponent()
					.setCode(new CodeableConcept().setText("Success"))
					.setValue(new BooleanType(entity.getSuccess()));



			observations.add(observation);
		}
		// map to Observation: to be discussed


		// return observation list
		return observations;
	}

	public Class<? extends IBaseResource> getResourceType() {
		return Observation.class;
	}
}
