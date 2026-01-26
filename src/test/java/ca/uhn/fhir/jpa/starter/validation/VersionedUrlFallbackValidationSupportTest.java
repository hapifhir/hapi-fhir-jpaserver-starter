package ca.uhn.fhir.jpa.starter.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionedUrlFallbackValidationSupportTest {

    private static final String BASE_FHIR_SD_PREFIX = "http://hl7.org/fhir/StructureDefinition/";
    private static final String ORGANIZATION_URL = BASE_FHIR_SD_PREFIX + "Organization";
    private static final String ORGANIZATION_URL_VERSIONED = ORGANIZATION_URL + "|4.0.1";
    private static final String ORGANIZATION_URL_MAJOR_MINOR = ORGANIZATION_URL + "|4.0";

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
    void testFallbackToNonVersionedUrl_WhenMajorMinorNotFound() {
        // Setup: major.minor returns null, non-versioned returns a resource
        StructureDefinition sd = new StructureDefinition();
        sd.setUrl(ORGANIZATION_URL);

        when(myChain.fetchStructureDefinition(ORGANIZATION_URL_MAJOR_MINOR)).thenReturn(null);
        when(myChain.fetchStructureDefinition(ORGANIZATION_URL)).thenReturn(sd);

        // Execute
        var result = mySvc.fetchStructureDefinition(ORGANIZATION_URL_VERSIONED);

        // Verify: fallback to non-versioned succeeds
        assertNotNull(result);
        assertSame(sd, result);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL_MAJOR_MINOR);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL);
    }

    @Test
    void testFallbackToMajorMinorVersion_WhenAvailable() {
        // Setup: major.minor version exists
        StructureDefinition sd = new StructureDefinition();
        sd.setUrl(ORGANIZATION_URL);
        sd.setVersion("4.0");

        when(myChain.fetchStructureDefinition(ORGANIZATION_URL_MAJOR_MINOR)).thenReturn(sd);

        // Execute
        var result = mySvc.fetchStructureDefinition(ORGANIZATION_URL_VERSIONED);

        // Verify: returns major.minor match, doesn't try non-versioned
        assertNotNull(result);
        assertSame(sd, result);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL_MAJOR_MINOR);
        verify(myChain, never()).fetchStructureDefinition(ORGANIZATION_URL);
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

        when(myChain.fetchStructureDefinition(CUSTOM_SD_URL + "|1.0")).thenReturn(null);
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

        when(myChain.fetchStructureDefinition(CUSTOM_SD_URL + "|1.0")).thenReturn(null);
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

        when(myChain.fetchResource(StructureDefinition.class, ORGANIZATION_URL_MAJOR_MINOR)).thenReturn(null);
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
    void testNoMajorMinorFallback_WhenOnlyMajorVersion() {
        // Setup: version is just "4" (no minor), so no major.minor fallback possible
        String urlWithMajorOnly = ORGANIZATION_URL + "|4";

        // Only non-versioned lookup should happen (no major.minor to extract)
        when(myChain.fetchStructureDefinition(ORGANIZATION_URL)).thenReturn(null);

        // Execute
        var result = mySvc.fetchStructureDefinition(urlWithMajorOnly);

        // Verify: should try non-versioned only (no major.minor since version has no minor part)
        assertNull(result);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL);
        verifyNoMoreInteractions(myChain);
    }

    @Test
    void testReturnsNull_WhenNoFallbackSucceeds() {
        // Setup: nothing found in any fallback
        when(myChain.fetchStructureDefinition(ORGANIZATION_URL_MAJOR_MINOR)).thenReturn(null);
        when(myChain.fetchStructureDefinition(ORGANIZATION_URL)).thenReturn(null);

        // Execute
        var result = mySvc.fetchStructureDefinition(ORGANIZATION_URL_VERSIONED);

        // Verify
        assertNull(result);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL_MAJOR_MINOR);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL);
    }

    @Test
    void testNoFallback_WhenMajorMinorSameAsVersion() {
        // Setup: version is "4.0" which equals major.minor, so only non-versioned fallback
        String urlWithMajorMinorOnly = ORGANIZATION_URL + "|4.0";

        when(myChain.fetchStructureDefinition(ORGANIZATION_URL)).thenReturn(null);

        // Execute
        var result = mySvc.fetchStructureDefinition(urlWithMajorMinorOnly);

        // Verify: should skip major.minor fallback (same as requested) and try non-versioned
        assertNull(result);
        verify(myChain).fetchStructureDefinition(ORGANIZATION_URL);
        verifyNoMoreInteractions(myChain);
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
                VersionedUrlFallbackValidationSupport.DEFAULT_URL_PREFIX);
    }
}
