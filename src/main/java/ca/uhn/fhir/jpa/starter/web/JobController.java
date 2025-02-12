package ca.uhn.fhir.jpa.starter.web;

import ca.uhn.fhir.batch2.api.IJobCoordinator;
import ca.uhn.fhir.batch2.api.JobOperationResultJson;
import ca.uhn.fhir.batch2.model.JobInstance;
import ca.uhn.fhir.batch2.model.StatusEnum;
import ca.uhn.fhir.batch2.models.JobInstanceFetchRequest;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("control")
public class JobController {
	private final IJobCoordinator theJobCoordinator;

	public JobController(IJobCoordinator theJobCoordinator) {
		this.theJobCoordinator = theJobCoordinator;
	}

	@RequestMapping(value = JobController.JOBS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<JobInstance> getAllJobs(@RequestParam(name = "pageStart") @Min(0) int pageStart, @RequestParam(name = "batchSize") int batchSize, @RequestParam(name = "jobStatus", required = false) StatusEnum jobStatus) {
		JobInstanceFetchRequest jobInstanceFetchRequest = new JobInstanceFetchRequest();
		jobInstanceFetchRequest.setPageStart(pageStart);
		jobInstanceFetchRequest.setBatchSize(batchSize);
		jobInstanceFetchRequest.setJobStatus(jobStatus != null ? jobStatus.toString() : "");
		jobInstanceFetchRequest.setSort(Sort.by(Sort.Direction.DESC, JobController.MY_CREATE_TIME));

		return theJobCoordinator.fetchAllJobInstances(jobInstanceFetchRequest).getContent();
	}

	@RequestMapping(value = JobController.JOBS, method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JobOperationResultJson cancelInstance(@RequestParam(name = "instanceId") String instanceId) {
		return theJobCoordinator.cancelInstance(instanceId);
	}

	public static final String JOBS = "jobs";
	public static final String MY_CREATE_TIME = "myCreateTime";
}
