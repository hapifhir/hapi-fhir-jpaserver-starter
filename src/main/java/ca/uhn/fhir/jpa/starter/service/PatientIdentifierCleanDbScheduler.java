package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.model.EncounterIdEntity;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.iprd.fhir.utils.DateUtilityHelper;
import com.iprd.fhir.utils.FhirUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PatientIdentifierCleanDbScheduler {
//	private static final Logger logger = LoggerFactory.getLogger(ResourceMapperService.class);
	private static final long DELAY = 10 * 60000;

	/**
	 * Delete entries from the PatientIdentifierEntity Table whose status is DELETE and lastUpdated is 3 months old.
	 */
	@Scheduled(fixedDelay = DELAY, initialDelay = DELAY)
	public void deleteOldEntries() {

	}
}
