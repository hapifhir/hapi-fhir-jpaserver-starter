package org.hl7.fhir.r5.hapi.ctx;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.sl.cache.Cache;
import ca.uhn.fhir.sl.cache.CacheFactory;
import ca.uhn.fhir.system.HapiSystemProperties;
import org.apache.commons.lang3.Validate;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.TerminologyServiceException;
import org.hl7.fhir.r5.context.ExpansionOptions;
import org.hl7.fhir.r5.context.IContextResourceLoader;
import org.hl7.fhir.r5.context.IOIDServices.OIDSummary;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.context.IWorkerContextManager;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionBindingComponent;
import org.hl7.fhir.r5.model.NamingSystem;
import org.hl7.fhir.r5.model.PackageInformation;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.ResourceType;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r5.profilemodel.PEBuilder;
import org.hl7.fhir.r5.terminologies.expansion.ValueSetExpansionOutcome;
import org.hl7.fhir.r5.terminologies.utilities.CodingValidationRequest;
import org.hl7.fhir.r5.terminologies.utilities.ValidationResult;
import org.hl7.fhir.r5.utils.validation.IResourceValidator;
import org.hl7.fhir.r5.utils.validation.ValidationContextCarrier;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.i18n.I18nBase;
import org.hl7.fhir.utilities.npm.BasePackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationOptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class HapiWorkerContext extends I18nBase implements IWorkerContext {
	private final FhirContext myCtx;
	private final Cache<String, Resource> myFetchedResourceCache;
	private final IValidationSupport myValidationSupport;
	private Parameters myExpansionProfile;
	private String myOverrideVersionNs;

	public HapiWorkerContext(FhirContext theCtx, IValidationSupport theValidationSupport) {
		Validate.notNull(theCtx, "theCtx must not be null");
		Validate.notNull(theValidationSupport, "theValidationSupport must not be null");
		myCtx = theCtx;
		myValidationSupport = theValidationSupport;

		long timeoutMillis = HapiSystemProperties.getValidationResourceCacheTimeoutMillis();

		myFetchedResourceCache = CacheFactory.build(timeoutMillis);

		// Set a default locale
		setValidationMessageLanguage(getLocale());
	}


	public CodeSystem fetchCodeSystem(String theSystem) {
		if (myValidationSupport == null) {
			return null;
		} else {
			return (CodeSystem) myValidationSupport.fetchCodeSystem(theSystem);
		}
	}


	public CodeSystem fetchCodeSystem(String theSystem, String version) {
		if (myValidationSupport == null) {
			return null;
		} else {
			return (CodeSystem) myValidationSupport.fetchCodeSystem(theSystem);
		}
	}


	public CodeSystem fetchCodeSystem(String system, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2456));
	}


	public CodeSystem fetchCodeSystem(String system, String version, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2457));
	}

	// matchbox patch: methods added for org.hl7.fhir.core 6.9.1 VersionResolutionRules API
	public CodeSystem fetchCodeSystem(String system, IWorkerContext.VersionResolutionRules rules) {
		return fetchCodeSystem(system);
	}

	public CodeSystem fetchCodeSystem(String system, IWorkerContext.VersionResolutionRules rules, String version, Resource sourceOfReference) {
		return fetchCodeSystem(system, version);
	}

	public CodeSystem fetchCodeSystem(String system, IWorkerContext.VersionResolutionRules rules, String version, Resource sourceOfReference, boolean useSupplements) {
		return fetchCodeSystem(system, version);
	}

	public CodeSystem fetchSupplementedCodeSystem(String theS) {
		return null;
	}


	public CodeSystem fetchSupplementedCodeSystem(String theS, String theS1) {
		return null;
	}


	public CodeSystem fetchSupplementedCodeSystem(String system, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2458));
	}


	public CodeSystem fetchSupplementedCodeSystem(String system, String version, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2459));
	}

	public CodeSystem fetchSupplementedCodeSystem(String system, IWorkerContext.VersionResolutionRules rules) {
		throw new UnsupportedOperationException(Msg.code(2459));
	}

	public CodeSystem fetchSupplementedCodeSystem(String system, IWorkerContext.VersionResolutionRules rules, String version, List<String> supplements, Resource sourceOfReference) {
		throw new UnsupportedOperationException(Msg.code(2459));
	}


	public List<String> getResourceNames() {
		List<String> result = new ArrayList<>();
		for (ResourceType next : ResourceType.values()) {
			result.add(next.name());
		}
		Collections.sort(result);
		return result;
	}


	public List<String> getResourceNames(FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2460));
	}


	public IResourceValidator newValidator() {
		throw new UnsupportedOperationException(Msg.code(206));
	}


	public Map<String, NamingSystem> getNSUrlMap() {
		throw new UnsupportedOperationException(Msg.code(2241));
	}


	public boolean supportsSystem(String theSystem) {
		if (myValidationSupport == null) {
			return false;
		} else {
			return myValidationSupport.isCodeSystemSupported(
					new ValidationSupportContext(myValidationSupport), theSystem);
		}
	}


	public boolean supportsSystem(String system, FhirPublication fhirVersion) throws TerminologyServiceException {
		if (!fhirVersion.equals(FhirPublication.R5)) {
			throw new UnsupportedOperationException(Msg.code(2461));
		}
		return supportsSystem(system);
	}


	public ValidationResult validateCode(ValidationOptions theOptions, CodeableConcept theCode, ValueSet theVs) {
		for (Coding next : theCode.getCoding()) {
			ValidationResult retVal = validateCode(theOptions, next, theVs);
			if (retVal.isOk()) {
				return retVal;
			}
		}

		return new ValidationResult(IssueSeverity.ERROR, null, null);
	}


	public ValidationResult validateCode(ValidationOptions theOptions, Coding theCode, ValueSet theVs) {
		String system = theCode.getSystem();
		String code = theCode.getCode();
		String display = theCode.getDisplay();
		return validateCode(theOptions, system, null, code, display, theVs);
	}


	public ValidationResult validateCode(
			ValidationOptions options, Coding code, ValueSet vs, ValidationContextCarrier ctxt) {
		return validateCode(options, code, vs);
	}


	public void validateCodeBatch(
			ValidationOptions options, List<? extends CodingValidationRequest> codes, ValueSet vs, boolean isGuess) {
		throw new UnsupportedOperationException(Msg.code(209));
	}


	public void validateCodeBatchByRef(
			ValidationOptions validationOptions, List<? extends CodingValidationRequest> list, String s) {
		throw new UnsupportedOperationException(Msg.code(2430));
	}


	public ValueSetExpansionOutcome expandVS(
			ValueSet theValueSet, boolean cacheOk, boolean heiarchical, boolean incompleteOk) {
		return null;
	}

	public ValueSetExpansionOutcome expandVS(ExpansionOptions options, String url) {
		throw new UnsupportedOperationException(Msg.code(2128));
	}

	public ValueSetExpansionOutcome expandVS(ValueSet theValueSet, boolean cacheOk, boolean hierarchical, int limit) {
		throw new UnsupportedOperationException(Msg.code(2128));
	}

	public ValueSetExpansionOutcome expandVS(ExpansionOptions options, ValueSet theValueSet) {
		throw new UnsupportedOperationException(Msg.code(2128));
	}


	public ValidationResult validateCode(
			ValidationOptions theOptions, String theSystem, String theVersion, String theCode, String theDisplay) {
		IValidationSupport.CodeValidationResult result = myValidationSupport.validateCode(
				new ValidationSupportContext(myValidationSupport),
				convertConceptValidationOptions(theOptions),
				theSystem,
				theCode,
				theDisplay,
				null);
		if (result == null) {
			return null;
		}
		IssueSeverity severity = null;
		if (result.getSeverity() != null) {
			severity = IssueSeverity.fromCode(result.getSeverityCode());
		}
		ConceptDefinitionComponent definition = new ConceptDefinitionComponent().setCode(result.getCode());
		return new ValidationResult(severity, result.getMessage(), theSystem, theVersion, definition, null, null);
	}


	public ValidationResult validateCode(
			ValidationOptions theOptions,
			String theSystem,
			String theVersion,
			String theCode,
			String theDisplay,
			ValueSet theVs) {
		IValidationSupport.CodeValidationResult outcome;
		if (isNotBlank(theVs.getUrl())) {
			outcome = myValidationSupport.validateCode(
					new ValidationSupportContext(myValidationSupport),
					convertConceptValidationOptions(theOptions),
					theSystem,
					theCode,
					theDisplay,
					theVs.getUrl());
		} else {
			outcome = myValidationSupport.validateCodeInValueSet(
					new ValidationSupportContext(myValidationSupport),
					convertConceptValidationOptions(theOptions),
					theSystem,
					theCode,
					theDisplay,
					theVs);
		}

		if (outcome != null && outcome.isOk()) {
			ConceptDefinitionComponent definition = new ConceptDefinitionComponent();
			definition.setCode(theCode);
			definition.setDisplay(outcome.getDisplay());
			return new ValidationResult(theSystem, theVersion, definition, null);
		}

		return new ValidationResult(
				IssueSeverity.ERROR,
				"Unknown code[" + theCode + "] in system[" + Constants.codeSystemWithDefaultDescription(theSystem)
						+ "]",
				null);
	}


	public ValidationResult validateCode(ValidationOptions theOptions, String code, ValueSet vs) {
		return validateCode(theOptions, null, null, code, null, vs);
	}


	public Parameters getExpansionParameters() {
		return myExpansionProfile;
	}


	public void setExpansionParameters(Parameters expParameters) {
		setExpansionProfile(expParameters);
	}

	public void setExpansionProfile(Parameters theExpParameters) {
		myExpansionProfile = theExpParameters;
	}


	public ValueSetExpansionOutcome expandVS(ValueSet theSource, boolean theCacheOk, boolean theHierarchical) {
		throw new UnsupportedOperationException(Msg.code(2128));
	}


	public ValueSetExpansionOutcome expandVS(ConceptSetComponent theInc, boolean theHierarchical, boolean theNoInactive)
			throws TerminologyServiceException {
		ValueSet input = new ValueSet();
		input.getCompose().setInactive(!theNoInactive); // TODO GGG/DO is this valid?
		input.getCompose().addInclude(theInc);
		IValidationSupport.ValueSetExpansionOutcome output =
				myValidationSupport.expandValueSet(new ValidationSupportContext(myValidationSupport), null, input);
		return new ValueSetExpansionOutcome(
				(ValueSet) output.getValueSet(), output.getError(), null, output.getErrorIsFromServer());
	}


	public Locale getLocale() {
		return Locale.getDefault();
	}


	public void setLocale(Locale locale) {
		// ignore
	}


	public org.hl7.fhir.r5.context.ILoggingService getLogger() {
		throw new UnsupportedOperationException(Msg.code(213));
	}


	public void setLogger(org.hl7.fhir.r5.context.ILoggingService theLogger) {
		throw new UnsupportedOperationException(Msg.code(214));
	}


	public String getVersion() {
		return myCtx.getVersion().getVersion().getFhirVersionString();
	}


	public UcumService getUcumService() {
		throw new UnsupportedOperationException(Msg.code(216));
	}


	public void setUcumService(UcumService ucumService) {
		throw new UnsupportedOperationException(Msg.code(217));
	}


	public boolean isNoTerminologyServer() {
		return false;
	}


	public Set<String> getCodeSystemsUsed() {
		throw new UnsupportedOperationException(Msg.code(218));
	}


	public StructureDefinition fetchTypeDefinition(String typeName) {
		return fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + typeName);
	}


	public boolean isPrimitiveType(String s) {
		throw new UnsupportedOperationException(Msg.code(2462));
	}


	public boolean isDataType(String s) {
		throw new UnsupportedOperationException(Msg.code(2463));
	}


	public StructureDefinition fetchTypeDefinition(String typeName, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2464));
	}


	public List<StructureDefinition> fetchTypeDefinitions(String n) {
		throw new UnsupportedOperationException(Msg.code(234));
	}


	public List<StructureDefinition> fetchTypeDefinitions(String n, FhirPublication fhirPublication) {
		throw new UnsupportedOperationException(Msg.code(2465));
	}


	public <T extends Resource> T fetchResourceRaw(Class<T> class_, String uri) {
		return fetchResource(class_, uri);
	}

	public <T extends Resource> T fetchResourceRaw(Class<T> class_, String uri, IWorkerContext.VersionResolutionRules rules) {
		return fetchResource(class_, uri);
	}


	public <T extends org.hl7.fhir.r5.model.Resource> T fetchResource(Class<T> theClass, String theUri) {
		if (myValidationSupport == null || theUri == null) {
			return null;
		} else {
			@SuppressWarnings("unchecked")
			T retVal = (T) myFetchedResourceCache.get(theUri, t -> myValidationSupport.fetchResource(theClass, theUri));
			return retVal;
		}
	}

	public <T extends Resource> T fetchResource(Class<T> class_, String uri, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2466));
	}

	public <T extends Resource> T fetchResource(Class<T> class_, String uri, IWorkerContext.VersionResolutionRules rules) {
		return fetchResource(class_, uri);
	}

	public <T extends Resource> T fetchResource(Class<T> class_, String uri, IWorkerContext.VersionResolutionRules rules, String version, Resource sourceOfReference) {
		return fetchResource(class_, uri);
	}


	public <T extends org.hl7.fhir.r5.model.Resource> T fetchResourceWithException(Class<T> theClass, String theUri)
			throws FHIRException {
		T retVal = fetchResource(theClass, theUri);
		if (retVal == null) {
			throw new FHIRException(Msg.code(224) + "Could not find resource: " + theUri);
		}
		return retVal;
	}


	public <T extends Resource> T fetchResourceWithException(Class<T> theClass, String uri, Resource sourceOfReference)
			throws FHIRException {
		throw new UnsupportedOperationException(Msg.code(2213));
	}

	public <T extends Resource> T fetchResourceWithException(Class<T> theClass, String uri, IWorkerContext.VersionResolutionRules rules)
			throws FHIRException {
		return fetchResourceWithException(theClass, uri);
	}

	public <T extends Resource> T fetchResourceWithException(Class<T> theClass, String uri, IWorkerContext.VersionResolutionRules rules, String version, Resource sourceOfReference)
			throws FHIRException {
		return fetchResourceWithException(theClass, uri);
	}


	public <T extends Resource> T fetchResource(Class<T> theClass, String theUri, String theVersion) {
		return fetchResource(theClass, theUri + "|" + theVersion);
	}


	public <T extends Resource> T fetchResource(
			Class<T> class_, String uri, String version, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2467));
	}


	public <T extends Resource> T fetchResource(Class<T> class_, String uri, Resource canonicalForSource) {
		return fetchResource(class_, uri);
	}


	public <T extends Resource> List<T> fetchResourcesByType(Class<T> class_, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2468));
	}


	public org.hl7.fhir.r5.model.Resource fetchResourceById(String theType, String theUri) {
		throw new UnsupportedOperationException(Msg.code(226));
	}


	public Resource fetchResourceById(String type, String uri, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2469));
	}


	public <T extends org.hl7.fhir.r5.model.Resource> boolean hasResource(Class<T> theClass_, String theUri) {
		throw new UnsupportedOperationException(Msg.code(227));
	}


	public <T extends Resource> boolean hasResource(Class<T> class_, String uri, Resource sourceOfReference) {
		throw new UnsupportedOperationException(Msg.code(2470));
	}

	public <T extends Resource> boolean hasResource(Class<T> class_, String uri, String version, Resource sourceOfReference) {
		throw new UnsupportedOperationException(Msg.code(2470));
	}

	public <T extends Resource> List<T> fetchResourceVersions(Class<T> class_, String url) {
		throw new UnsupportedOperationException(Msg.code(2470));
	}


	public <T extends Resource> boolean hasResource(Class<T> class_, String uri, FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2471));
	}


	public void cacheResource(org.hl7.fhir.r5.model.Resource theRes) throws FHIRException {
		throw new UnsupportedOperationException(Msg.code(228));
	}


	public void cacheResourceFromPackage(Resource res, PackageInformation packageDetails) throws FHIRException {
		throw new UnsupportedOperationException(Msg.code(229));
	}


	public void cachePackage(PackageInformation packageInformation) {}


	public Set<String> getResourceNamesAsSet() {
		return myCtx.getResourceTypes();
	}

	public Set<String> getResourceNamesAsSet(FhirPublication fhirVersion) {
		throw new UnsupportedOperationException(Msg.code(2472));
	}


	public ValueSetExpansionOutcome expandVS(
			Resource src, ElementDefinitionBindingComponent theBinding, boolean theCacheOk, boolean theHierarchical)
			throws FHIRException {
		throw new UnsupportedOperationException(Msg.code(230));
	}


	public Set<String> getBinaryKeysAsSet() {
		throw new UnsupportedOperationException(Msg.code(2115));
	}


	public boolean hasBinaryKey(String s) {
		throw new UnsupportedOperationException(Msg.code(2129));
	}


	public byte[] getBinaryForKey(String s) {
		throw new UnsupportedOperationException(Msg.code(2199));
	}

	public int loadFromPackage(NpmPackage pi, IContextResourceLoader loader) throws FHIRException {
		throw new UnsupportedOperationException(Msg.code(233));
	}

	public int loadFromPackage(NpmPackage pi, IContextResourceLoader loader, List<String> types)
			throws FileNotFoundException, IOException, FHIRException {
		throw new UnsupportedOperationException(Msg.code(2328));
	}

	public int loadFromPackageAndDependencies(NpmPackage pi, IContextResourceLoader loader, BasePackageCacheManager pcm)
			throws FHIRException {
		throw new UnsupportedOperationException(Msg.code(235));
	}


	public boolean hasPackage(String id, String ver) {
		throw new UnsupportedOperationException(Msg.code(236));
	}


	public boolean hasPackage(PackageInformation packageVersion) {
		return false;
	}


	public PackageInformation getPackage(String id, String ver) {
		return null;
	}


	public int getClientRetryCount() {
		throw new UnsupportedOperationException(Msg.code(237));
	}


	public IWorkerContext setClientRetryCount(int value) {
		throw new UnsupportedOperationException(Msg.code(238));
	}


	public TimeTracker clock() {
		return null;
	}


	public IWorkerContextManager.IPackageLoadingTracker getPackageTracker() {
		throw new UnsupportedOperationException(Msg.code(2112));
	}


	public PackageInformation getPackageForUrl(String s) {
		return null;
	}

	public static ConceptValidationOptions convertConceptValidationOptions(ValidationOptions theOptions) {
		ConceptValidationOptions retVal = new ConceptValidationOptions();
		if (theOptions.isGuessSystem()) {
			retVal = retVal.setInferSystem(true);
		}
		return retVal;
	}


	public <T extends Resource> List<T> fetchResourcesByType(Class<T> theClass) {
		if (theClass.equals(StructureDefinition.class)) {
			return myValidationSupport.fetchAllStructureDefinitions();
		}

		throw new UnsupportedOperationException(Msg.code(2113) + "Can't fetch all resources of type: " + theClass);
	}

	public <T extends Resource> List<T> fetchResourcesByUrl(Class<T> class_, String url) {
		throw new UnsupportedOperationException(Msg.code(2508) + "Can't fetch all resources of url: " + url);
	}


	public IWorkerContext setPackageTracker(IWorkerContextManager.IPackageLoadingTracker theIPackageLoadingTracker) {
		throw new UnsupportedOperationException(Msg.code(220));
	}


	public String getSpecUrl() {
		return "";
	}


	public PEBuilder getProfiledElementBuilder(
			PEBuilder.PEElementPropertiesPolicy thePEElementPropertiesPolicy, boolean theB) {
		throw new UnsupportedOperationException(Msg.code(2261));
	}


	public boolean isForPublication() {
		return false;
	}


	public void setForPublication(boolean b) {
		throw new UnsupportedOperationException(Msg.code(2350));
	}

	public OIDSummary urlsForOid(String oid, String resourceType) {
		throw new UnsupportedOperationException(Msg.code(2473));
	}


	public <T extends Resource> T findTxResource(Class<T> class_, String canonical, IWorkerContext.VersionResolutionRules rules) {
		throw new UnsupportedOperationException(Msg.code(2491));
	}


	public <T extends Resource> T findTxResource(Class<T> class_, String canonical, IWorkerContext.VersionResolutionRules rules, String version, Resource sourceOfReference) {
		throw new UnsupportedOperationException(Msg.code(2493));
	}


	public Boolean subsumes(ValidationOptions optionsArg, Coding parent, Coding child) {
		throw new UnsupportedOperationException(Msg.code(2488));
	}

	public long getDefinitionsVersion() {
		return 0;
	}

	public org.hl7.fhir.r5.context.IOIDServices oidServices() {
		return null;
	}

	public IWorkerContextManager getManager() {
		throw new UnsupportedOperationException(Msg.code(2488));
	}

	// matchbox patch: methods added in org.hl7.fhir.core 6.9.1
	public IWorkerContext.SystemSupportInformation getTxSupportInfo(String system, String version) {
		throw new UnsupportedOperationException(Msg.code(2488));
	}

	public org.hl7.fhir.r5.model.OperationOutcome validateTxResource(ValidationOptions options, Resource resource) {
		throw new UnsupportedOperationException(Msg.code(2488));
	}

	private final java.util.Map<String, Object> analyses = new java.util.HashMap<>();


	public void storeAnalysis(Class className, Object analysis) {
		analyses.put(className.getName(), analysis);
	}


	public Object retrieveAnalysis(Class className) {
		return analyses.get(className.getName());
	}
}
