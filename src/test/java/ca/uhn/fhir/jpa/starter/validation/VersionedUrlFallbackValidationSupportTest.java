package ca.uhn.fhir.jpa.starter.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static ca.uhn.fhir.context.support.IValidationSupport.URL_PREFIX_STRUCTURE_DEFINITION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionedUrlFallbackValidationSupportTest {

    private static final String BASE_FHIR_SD_PREFIX = "http://hl7.org/fhir/StructureDefinition/";
    private static final String ORGANIZATION_URL = BASE_FHIR_SD_PREFIX + "Organization";
    private static final String ORGANIZATION_URL_VERSIONED = ORGANIZATION_URL + "|4.0.1";

    private static final String CUSTOM_SD_URL = "http://example.com/StructureDefinition/MyProfile";
    private static final String CUSTOM_SD_URL_VERSIONED = CUSTOM_SD_URL + "|1.0.0";

    private FhirContext myFhirContext;

    @Mock
    private IValidationSupport myChain;

    private VersionedUrlFallbackValidationSupport mySvc;

    @BeforeEach
    void setUp() {
        myFhirContext = FhirContext.forR4Cached();
        mySvc = new VersionedUrlFallbackValidationSupport(myFhirContext, myChain);
    }

    @Test
    void testExactVersionedUrl_ReturnedWithoutFallback() {
        // Setup: exact versioned URL is available
        StructureDefinition sd = new StructureDefinition();
        sd.setUrl(ORGANIZATION_URL);
        sd.setVersion("4.0.1");

        when(myChain.fetchStructureDefinition(ORGANIZATION_URL_VERSIONED)).thenReturn(sd);

        // Execute
        var result = mySvc.fetchStructureDefinition(ORGANIZATION_URL_VERSIONED);

        // Verify: returns exact match, no fallback attempted
        assertNotNull(result);
        assertSame(sd, result);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL_VERSIONED);
        verify(myChain, never()).fetchStructureDefinition(ORGANIZATION_URL);
    }

    @Test
    void testFallbackToNonVersionedUrl() {
        // Setup: exact versioned URL not found, non-versioned returns a resource
        StructureDefinition sd = new StructureDefinition();
        sd.setUrl(ORGANIZATION_URL);

        when(myChain.fetchStructureDefinition(ORGANIZATION_URL_VERSIONED)).thenReturn(null);
        when(myChain.fetchStructureDefinition(ORGANIZATION_URL)).thenReturn(sd);

        // Execute
        var result = mySvc.fetchStructureDefinition(ORGANIZATION_URL_VERSIONED);

        // Verify: fallback to non-versioned succeeds
        assertNotNull(result);
        assertSame(sd, result);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL_VERSIONED);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL);
    }

    @Test
    void testNoFallback_ForNonVersionedUrl() {
        // Execute: non-versioned URL should pass through without any chain calls
        var result = mySvc.fetchStructureDefinition(ORGANIZATION_URL);

        // Verify: returns null immediately, lets other chain supports handle it
        assertNull(result);
        verifyNoInteractions(myChain);
    }

    @Test
    void testNoFallback_ForCustomUrlNotMatchingDefaultPrefix() {
        // Execute: custom URL doesn't match the default prefix filter
        var result = mySvc.fetchStructureDefinition(CUSTOM_SD_URL_VERSIONED);

        // Verify: returns null, doesn't attempt fallback (not in prefix list)
        assertNull(result);
        verifyNoInteractions(myChain);
    }

    @Test
    void testFallback_ForCustomUrl_WhenPrefixConfigured() {
        // Setup: configure to also handle custom URLs
        mySvc = new VersionedUrlFallbackValidationSupport(myFhirContext, myChain,
                Set.of(BASE_FHIR_SD_PREFIX, "http://example.com/StructureDefinition/"));

        StructureDefinition sd = new StructureDefinition();
        sd.setUrl(CUSTOM_SD_URL);

        when(myChain.fetchStructureDefinition(CUSTOM_SD_URL_VERSIONED)).thenReturn(null);
        when(myChain.fetchStructureDefinition(CUSTOM_SD_URL)).thenReturn(sd);

        // Execute
        var result = mySvc.fetchStructureDefinition(CUSTOM_SD_URL_VERSIONED);

        // Verify
        assertNotNull(result);
        assertSame(sd, result);
    }

    @Test
    void testFallback_ForAllUrls_WhenEmptyPrefixSet() {
        // Setup: empty prefix set means apply to all URLs
        mySvc = new VersionedUrlFallbackValidationSupport(myFhirContext, myChain, Set.of());

        StructureDefinition sd = new StructureDefinition();
        sd.setUrl(CUSTOM_SD_URL);

        when(myChain.fetchStructureDefinition(CUSTOM_SD_URL_VERSIONED)).thenReturn(null);
        when(myChain.fetchStructureDefinition(CUSTOM_SD_URL)).thenReturn(sd);

        // Execute
        var result = mySvc.fetchStructureDefinition(CUSTOM_SD_URL_VERSIONED);

        // Verify
        assertNotNull(result);
        assertSame(sd, result);
    }

    @Test
    void testFetchResource_FallbackToNonVersioned() {
        // Setup
        StructureDefinition sd = new StructureDefinition();
        sd.setUrl(ORGANIZATION_URL);

        when(myChain.fetchResource(StructureDefinition.class, ORGANIZATION_URL_VERSIONED)).thenReturn(null);
        when(myChain.fetchResource(StructureDefinition.class, ORGANIZATION_URL)).thenReturn(sd);

        // Execute
        var result = mySvc.fetchResource(StructureDefinition.class, ORGANIZATION_URL_VERSIONED);

        // Verify
        assertNotNull(result);
        assertSame(sd, result);
    }

    @Test
    void testFetchResource_NoFallbackForNonMatchingPrefix() {
        // Execute
        var result = mySvc.fetchResource(StructureDefinition.class, CUSTOM_SD_URL_VERSIONED);

        // Verify
        assertNull(result);
        verifyNoInteractions(myChain);
    }

    @Test
    void testFetchResource_NoFallbackForNonVersionedUrl() {
        // Execute
        var result = mySvc.fetchResource(StructureDefinition.class, ORGANIZATION_URL);

        // Verify
        assertNull(result);
        verifyNoInteractions(myChain);
    }

    @Test
    void testReturnsNull_WhenNoFallbackSucceeds() {
        // Setup: nothing found in any lookup
        when(myChain.fetchStructureDefinition(ORGANIZATION_URL_VERSIONED)).thenReturn(null);
        when(myChain.fetchStructureDefinition(ORGANIZATION_URL)).thenReturn(null);

        // Execute
        var result = mySvc.fetchStructureDefinition(ORGANIZATION_URL_VERSIONED);

        // Verify
        assertNull(result);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL_VERSIONED);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL);
    }

    @Test
    void testGetName() {
        assertEquals("VersionedUrlFallbackValidationSupport", mySvc.getName());
    }

    @Test
    void testGetFhirContext() {
        assertSame(myFhirContext, mySvc.getFhirContext());
    }

    @Test
    void testDefaultUrlPrefix() {
        assertEquals("http://hl7.org/fhir/StructureDefinition/",
			  URL_PREFIX_STRUCTURE_DEFINITION);
    }

    /**
     * Integration tests using real DefaultProfileValidationSupport instead of mocks.
     * This tests the actual fallback behavior with FHIR's built-in profiles.
     */
    @Nested
    class WithRealValidationChain {

        private FhirContext myFhirContext;
        private ValidationSupportChain myValidationChain;
        private VersionedUrlFallbackValidationSupport mySvc;

        @BeforeEach
        void setUp() {
            myFhirContext = FhirContext.forR4Cached();

            // Create a validation chain with the real DefaultProfileValidationSupport
            // which contains all built-in FHIR R4 StructureDefinitions
            myValidationChain = new ValidationSupportChain(new DefaultProfileValidationSupport(myFhirContext));

            // Wrap the chain with our fallback support, similar to production setup
            mySvc = new VersionedUrlFallbackValidationSupport(myFhirContext, myValidationChain);
        }

        @Test
        void testFallbackToNonVersionedUrl_WithRealDefaultProfile() {
            // The DefaultProfileValidationSupport has Organization without version suffix.
            // When we request versioned URL, it should fall back and find it.
            String versionedUrl = "http://hl7.org/fhir/StructureDefinition/Organization|4.0.1";

            var result = mySvc.fetchStructureDefinition(versionedUrl);

            assertNotNull(result, "Should find Organization via fallback to non-versioned URL");
            assertInstanceOf(StructureDefinition.class, result);
            StructureDefinition sd = (StructureDefinition) result;
            assertEquals("http://hl7.org/fhir/StructureDefinition/Organization", sd.getUrl());
            assertEquals("Organization", sd.getName());
        }

        @Test
        void testFallbackForPatient_WithRealDefaultProfile() {
            String versionedUrl = "http://hl7.org/fhir/StructureDefinition/Patient|4.0.1";

            var result = mySvc.fetchStructureDefinition(versionedUrl);

            assertNotNull(result, "Should find Patient via fallback");
            assertInstanceOf(StructureDefinition.class, result);
            StructureDefinition sd = (StructureDefinition) result;
            assertEquals("http://hl7.org/fhir/StructureDefinition/Patient", sd.getUrl());
        }

        @Test
        void testFetchResource_WithRealDefaultProfile() {
            String versionedUrl = "http://hl7.org/fhir/StructureDefinition/Observation|4.0.1";

            var result = mySvc.fetchResource(StructureDefinition.class, versionedUrl);

            assertNotNull(result, "Should find Observation via fetchResource fallback");
            assertEquals("http://hl7.org/fhir/StructureDefinition/Observation", result.getUrl());
        }

        @Test
        void testNonExistentResource_ReturnsNull() {
            String versionedUrl = "http://hl7.org/fhir/StructureDefinition/NonExistentResource|1.0.0";

            var result = mySvc.fetchStructureDefinition(versionedUrl);

            assertNull(result, "Should return null for non-existent resource");
        }

        @Test
        void testNonVersionedUrl_PassesThrough() {
            // Non-versioned URLs should return null from the fallback support
            // (they're handled by DefaultProfileValidationSupport directly in a real chain)
            String nonVersionedUrl = "http://hl7.org/fhir/StructureDefinition/Patient";

            var result = mySvc.fetchStructureDefinition(nonVersionedUrl);

            // The fallback support returns null for non-versioned URLs
            // In a real setup, the chain would handle this
            assertNull(result);
        }

        @Test
        void testDataTypeProfiles_WithRealDefaultProfile() {
            // Test that data type StructureDefinitions also work
            String versionedUrl = "http://hl7.org/fhir/StructureDefinition/HumanName|4.0.1";

            var result = mySvc.fetchStructureDefinition(versionedUrl);

            assertNotNull(result, "Should find HumanName data type via fallback");
            assertInstanceOf(StructureDefinition.class, result);
            assertEquals("http://hl7.org/fhir/StructureDefinition/HumanName",
                    ((StructureDefinition) result).getUrl());
        }
    }
}
