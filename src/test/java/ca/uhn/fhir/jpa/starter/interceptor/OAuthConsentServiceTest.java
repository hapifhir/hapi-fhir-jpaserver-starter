package ca.uhn.fhir.jpa.starter.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.OAuth2Helper;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.IPreResourceAccessDetails;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentInterceptor;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOutcome;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;

class OAuthConsentServiceTest {

	private OAuthConsentService myConsentService;
	private DaoRegistry myDaoRegistry;
	private IFhirResourceDao<IBaseResource> myResourceDao;
	private AppProperties myAppProperties;
	private RequestDetails myRequestDetails;
	private IConsentContextServices myConsentContextServices;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		myDaoRegistry = mock(DaoRegistry.class);
		myResourceDao = mock(IFhirResourceDao.class);
		myAppProperties = new AppProperties();
		myAppProperties.getOauth().setEnabled(true);
		myRequestDetails = mock(RequestDetails.class);
		myConsentContextServices = mock(IConsentContextServices.class);
		myConsentService = new OAuthConsentService(myDaoRegistry, myAppProperties);

		when(myRequestDetails.getResourceName()).thenReturn("Task");
		when(myRequestDetails.getId()).thenReturn(new IdType("Task/1"));
		when(myDaoRegistry.getResourceDao("Task")).thenReturn(myResourceDao);
	}

	@Test
	void canSeeResource_oauthDisabled_proceeds() {
		myAppProperties.getOauth().setEnabled(false);

		ConsentOutcome outcome = myConsentService.canSeeResource(
			myRequestDetails,
			createTaskForPatient("123"),
			myConsentContextServices
		);

		assertEquals(ConsentOutcome.PROCEED, outcome);
	}

	@Test
	void canSeeResource_matchingPatientClaim_proceeds() {
		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.canSeeResource(
				myRequestDetails,
				createTaskForPatient("123"),
				myConsentContextServices
			);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void canSeeResource_noToken_proceeds() {
		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(false);

			ConsentOutcome outcome = myConsentService.canSeeResource(
				myRequestDetails,
				createTaskForPatient("123"),
				myConsentContextServices
			);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void canSeeResource_nonTaskRequest_proceeds() {
		when(myRequestDetails.getResourceName()).thenReturn("Patient");
		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);

			ConsentOutcome outcome = myConsentService.canSeeResource(
				myRequestDetails,
				createTaskForPatient("123"),
				myConsentContextServices
			);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void canSeeResource_noPatientClaim_proceeds() {
		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn(null);

			ConsentOutcome outcome = myConsentService.canSeeResource(
				myRequestDetails,
				createTaskForPatient("123"),
				myConsentContextServices
			);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void canSeeResource_nullResource_rejects() {
		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.canSeeResource(
				myRequestDetails,
				null,
				myConsentContextServices
			);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	@Test
	void canSeeResource_taskWithoutForReference_rejects() {
		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.canSeeResource(
				myRequestDetails,
				new Task(),
				myConsentContextServices
			);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	@Test
	void canSeeResource_nonMatchingPatientClaim_rejects() {
		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.canSeeResource(
				myRequestDetails,
				createTaskForPatient("999"),
				myConsentContextServices
			);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	@Test
	void startOperation_post_matchingPatient_proceeds() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.POST);
		when(myRequestDetails.getResource()).thenReturn(createTaskForPatient("123"));

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void startOperation_post_nonMatchingPatient_rejects() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.POST);
		when(myRequestDetails.getResource()).thenReturn(createTaskForPatient("999"));

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	@Test
	void startOperation_put_bothMatchingPatients_proceeds() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PUT);
		when(myRequestDetails.getResource()).thenReturn(createTaskForPatient("123"));
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenReturn(createTaskForPatient("123"));

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void startOperation_put_persistentNonMatchingPatient_rejects() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PUT);
		when(myRequestDetails.getResource()).thenReturn(createTaskForPatient("123"));
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenReturn(createTaskForPatient("999"));

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	@Test
	void startOperation_patch_validJsonPatch_proceeds() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PATCH);
		when(myRequestDetails.getHeader("content-type")).thenReturn("application/json-patch+json");
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenReturn(createTaskForPatient("123"));
		when(myRequestDetails.loadRequestContents())
			.thenReturn("[{\"op\":\"replace\",\"path\":\"/for/reference\",\"value\":\"Patient/123\"}]".getBytes());

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void startOperation_patch_jsonPatchInvalidReference_rejects() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PATCH);
		when(myRequestDetails.getHeader("content-type")).thenReturn("application/json-patch+json");
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenReturn(createTaskForPatient("123"));
		when(myRequestDetails.loadRequestContents())
			.thenReturn("[{\"op\":\"replace\",\"path\":\"/for/reference\",\"value\":\"Patient/999\"}]".getBytes());

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	@Test
	void startOperation_patch_malformedJson_rejects() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PATCH);
		when(myRequestDetails.getHeader("content-type")).thenReturn("application/json-patch+json");
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenReturn(createTaskForPatient("123"));
		when(myRequestDetails.loadRequestContents()).thenReturn("not-json".getBytes());

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	@Test
	void startOperation_patch_invalidContentType_rejects() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PATCH);
		when(myRequestDetails.getHeader("content-type")).thenReturn("application/fhir+json");
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenReturn(createTaskForPatient("123"));

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	@Test
	void startOperation_delete_matchingPersistentPatient_proceeds() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.DELETE);
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenReturn(createTaskForPatient("123"));

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void startOperation_delete_nonMatchingPersistentPatient_rejects() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.DELETE);
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenReturn(createTaskForPatient("999"));

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	@Test
	void startOperation_oauthDisabled_proceeds() {
		myAppProperties.getOauth().setEnabled(false);
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PUT);

		ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
		assertEquals(ConsentOutcome.PROCEED, outcome);
	}

	@Test
	void startOperation_nonTaskRequest_proceeds() {
		when(myRequestDetails.getResourceName()).thenReturn("Patient");
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PUT);

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void startOperation_noPatientClaim_proceeds() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PUT);

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn(null);

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void startOperation_getRequestType_proceeds() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.GET);

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void startOperation_nullResourceName_proceeds() {
		when(myRequestDetails.getResourceName()).thenReturn(null);
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.DELETE);

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.PROCEED, outcome);
		}
	}

	@Test
	void startOperation_persistentReadThrows_runtimeExceptionPropagates() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PUT);
		when(myRequestDetails.getResource()).thenReturn(createTaskForPatient("123"));
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenThrow(new RuntimeException("db fail"));

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			assertThrows(RuntimeException.class, () -> myConsentService.startOperation(myRequestDetails, myConsentContextServices));
		}
	}

	@Test
	void startOperation_patch_missingContentType_rejects() {
		when(myRequestDetails.getRequestType()).thenReturn(RequestTypeEnum.PATCH);
		when(myRequestDetails.getHeader("content-type")).thenReturn(null);
		when(myResourceDao.read(any(), any(RequestDetails.class))).thenReturn(createTaskForPatient("123"));

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			ConsentOutcome outcome = myConsentService.startOperation(myRequestDetails, myConsentContextServices);
			assertEquals(ConsentOutcome.REJECT, outcome);
		}
	}

	/**
	 * The failure on deployed servers is an IndexOutOfBoundsException inside
	 * JpaPreResourceAccessDetails.getResource, reached from ConsentInterceptor.interceptPreAccess:
	 * the search result page reports a resource that it cannot produce. Without
	 * shouldProcessCanSeeResource, the interface default of true makes the interceptor walk that
	 * page on every search, including resource types this service never filters.
	 */
	@Test
	void interceptPreAccess_nonTaskRequest_leavesTheResultPageAlone() {
		when(myRequestDetails.getResourceName()).thenReturn("Patient");
		when(myRequestDetails.getUserData()).thenReturn(new HashMap<>());

		IPreResourceAccessDetails accessDetails = mock(IPreResourceAccessDetails.class);
		when(accessDetails.size()).thenReturn(1);
		when(accessDetails.getResource(0))
			.thenThrow(new IndexOutOfBoundsException("Index 0 out of bounds for length 0"));

		ConsentInterceptor interceptor = new ConsentInterceptor(myConsentService);

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			interceptor.interceptPreAccess(myRequestDetails, accessDetails);
		}

		verify(accessDetails, never()).getResource(anyInt());
	}

	/**
	 * The counterpart to the above: a Task request carrying a patient claim is the case this
	 * service exists for, so the interceptor must still load the page and filter it.
	 */
	@Test
	void interceptPreAccess_taskRequestWithPatientClaim_stillFiltersTheResultPage() {
		when(myRequestDetails.getUserData()).thenReturn(new HashMap<>());

		IPreResourceAccessDetails accessDetails = mock(IPreResourceAccessDetails.class);
		when(accessDetails.size()).thenReturn(1);
		when(accessDetails.getResource(0)).thenReturn(createTaskForPatient("999"));

		ConsentInterceptor interceptor = new ConsentInterceptor(myConsentService);

		try (MockedStatic<OAuth2Helper> helperMock = mockStatic(OAuth2Helper.class)) {
			helperMock.when(() -> OAuth2Helper.hasToken(myRequestDetails)).thenReturn(true);
			helperMock.when(() -> OAuth2Helper.getClaimAsString(myRequestDetails, "patient")).thenReturn("123");

			interceptor.interceptPreAccess(myRequestDetails, accessDetails);
		}

		verify(accessDetails).getResource(0);
		verify(accessDetails).setDontReturnResourceAtIndex(0);
	}

	private Task createTaskForPatient(String patientId) {
		Task task = new Task();
		task.setFor(new Reference("Patient/" + patientId));
		return task;
	}
}
