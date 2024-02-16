//
// Source code recreated from a .class file by Quiltflower
//

package org.hl7.fhir.validation.instance;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.fhir.ucum.Decimal;
import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.PathEngineException;
import org.hl7.fhir.exceptions.TerminologyServiceException;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities.SourcedChildDefinitions;
import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.context.IWorkerContext.ValidationResult;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.JsonParser;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.ObjectConverter;
import org.hl7.fhir.r5.elementmodel.ParserBase;
import org.hl7.fhir.r5.elementmodel.ResourceParser;
import org.hl7.fhir.r5.elementmodel.XmlParser;
import org.hl7.fhir.r5.elementmodel.Element.SpecialElement;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.elementmodel.ParserBase.NamedElement;
import org.hl7.fhir.r5.elementmodel.ParserBase.ValidationPolicy;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Base64BinaryType;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.CanonicalResource;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.DataType;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.DateType;
import org.hl7.fhir.r5.model.DecimalType;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.Enumeration;
import org.hl7.fhir.r5.model.ExpressionNode;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.InstantType;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.OidType;
import org.hl7.fhir.r5.model.Period;
import org.hl7.fhir.r5.model.PrimitiveType;
import org.hl7.fhir.r5.model.Quantity;
import org.hl7.fhir.r5.model.Range;
import org.hl7.fhir.r5.model.Ratio;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.SampledData;
import org.hl7.fhir.r5.model.SearchParameter;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.TimeType;
import org.hl7.fhir.r5.model.Timing;
import org.hl7.fhir.r5.model.TypeDetails;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.UuidType;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.Base.ProfileSource;
import org.hl7.fhir.r5.model.Base.ValidationInfo;
import org.hl7.fhir.r5.model.Base.ValidationMode;
import org.hl7.fhir.r5.model.Base.ValidationReason;
import org.hl7.fhir.r5.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r5.model.ElementDefinition.AggregationMode;
import org.hl7.fhir.r5.model.ElementDefinition.ConstraintSeverity;
import org.hl7.fhir.r5.model.ElementDefinition.DiscriminatorType;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionBindingComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionConstraintComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionMappingComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionSlicingComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionSlicingDiscriminatorComponent;
import org.hl7.fhir.r5.model.ElementDefinition.PropertyRepresentation;
import org.hl7.fhir.r5.model.ElementDefinition.SlicingRules;
import org.hl7.fhir.r5.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r5.model.Enumerations.BindingStrength;
import org.hl7.fhir.r5.model.Enumerations.CodeSystemContentMode;
import org.hl7.fhir.r5.model.ExpressionNode.CollectionStatus;
import org.hl7.fhir.r5.model.ImplementationGuide.ImplementationGuideGlobalComponent;
import org.hl7.fhir.r5.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r5.model.StructureDefinition.ExtensionContextType;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionContextComponent;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionMappingComponent;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionSnapshotComponent;
import org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r5.renderers.DataRenderer;
import org.hl7.fhir.r5.terminologies.utilities.TerminologyServiceErrorClass;
import org.hl7.fhir.r5.utils.BuildExtensions;
import org.hl7.fhir.r5.utils.FHIRPathEngine;
import org.hl7.fhir.r5.utils.ResourceUtilities;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.r5.utils.XVerExtensionManager;
import org.hl7.fhir.r5.utils.FHIRLexer.FHIRLexerException;
import org.hl7.fhir.r5.utils.FHIRPathEngine.IEvaluationContext;
import org.hl7.fhir.r5.utils.FHIRPathUtilityClasses.FunctionDetails;
import org.hl7.fhir.r5.utils.FHIRPathUtilityClasses.TypedElementDefinition;
import org.hl7.fhir.r5.utils.validation.BundleValidationRule;
import org.hl7.fhir.r5.utils.validation.IResourceValidator;
import org.hl7.fhir.r5.utils.validation.IValidationPolicyAdvisor;
import org.hl7.fhir.r5.utils.validation.IValidationProfileUsageTracker;
import org.hl7.fhir.r5.utils.validation.IValidatorResourceFetcher;
import org.hl7.fhir.r5.utils.validation.constants.BestPracticeWarningLevel;
import org.hl7.fhir.r5.utils.validation.constants.BindingKind;
import org.hl7.fhir.r5.utils.validation.constants.CheckDisplayOption;
import org.hl7.fhir.r5.utils.validation.constants.CodedContentValidationPolicy;
import org.hl7.fhir.r5.utils.validation.constants.ContainedReferenceValidationPolicy;
import org.hl7.fhir.r5.utils.validation.constants.IdStatus;
import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.utilities.CommaSeparatedStringBuilder;
import org.hl7.fhir.utilities.MarkDownProcessor;
import org.hl7.fhir.utilities.SIDUtilities;
import org.hl7.fhir.utilities.UnicodeUtilities;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.Utilities.DecimalStatus;
import org.hl7.fhir.utilities.VersionUtilities.VersionURLInfo;
import org.hl7.fhir.utilities.json.model.JsonObject;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationOptions;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.validation.ValidationMessage.Source;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.validation.BaseValidator;
import org.hl7.fhir.validation.cli.model.HtmlInMarkdownCheck;
import org.hl7.fhir.validation.cli.utils.QuestionnaireMode;
import org.hl7.fhir.validation.codesystem.CodingsObserver;
import org.hl7.fhir.validation.instance.type.BundleValidator;
import org.hl7.fhir.validation.instance.type.CodeSystemValidator;
import org.hl7.fhir.validation.instance.type.ConceptMapValidator;
import org.hl7.fhir.validation.instance.type.MeasureValidator;
import org.hl7.fhir.validation.instance.type.QuestionnaireValidator;
import org.hl7.fhir.validation.instance.type.SearchParameterValidator;
import org.hl7.fhir.validation.instance.type.StructureDefinitionValidator;
import org.hl7.fhir.validation.instance.type.StructureMapValidator;
import org.hl7.fhir.validation.instance.type.ValueSetValidator;
import org.hl7.fhir.validation.instance.type.StructureMapValidator.VariableDefn;
import org.hl7.fhir.validation.instance.type.StructureMapValidator.VariableSet;
import org.hl7.fhir.validation.instance.utils.ChildIterator;
import org.hl7.fhir.validation.instance.utils.ElementInfo;
import org.hl7.fhir.validation.instance.utils.FHIRPathExpressionFixer;
import org.hl7.fhir.validation.instance.utils.IndexedElement;
import org.hl7.fhir.validation.instance.utils.NodeStack;
import org.hl7.fhir.validation.instance.utils.ResolvedReference;
import org.hl7.fhir.validation.instance.utils.ResourceValidationTracker;
import org.hl7.fhir.validation.instance.utils.ValidatorHostContext;
import org.w3c.dom.Document;

public class InstanceValidator extends BaseValidator implements IResourceValidator {
	private static final String EXECUTED_CONSTRAINT_LIST = "validator.executed.invariant.list";
	private static final String EXECUTION_ID = "validator.execution.id";
	private static final String HTML_FRAGMENT_REGEX = "[a-zA-Z]\\w*(((\\s+)(\\S)*)*)";
	private static final boolean STACK_TRACE = false;
	private static final boolean DEBUG_ELEMENT = false;
	private static final HashSet<String> NO_TX_SYSTEM_EXEMPT = new HashSet(
		Arrays.asList(
			"http://loinc.org",
			"http://unitsofmeasure.org",
			"http://hl7.org/fhir/sid/icd-9-cm",
			"http://snomed.info/sct",
			"http://www.nlm.nih.gov/research/umls/rxnorm"
		)
	);
	private static final HashSet<String> NO_HTTPS_LIST = new HashSet(
		Arrays.asList("https://loinc.org", "https://unitsofmeasure.org", "https://snomed.info/sct", "https://www.nlm.nih.gov/research/umls/rxnorm")
	);
	private static final HashSet<String> EXTENSION_CONTEXT_LIST = new HashSet(
		Arrays.asList("ElementDefinition.example.value", "ElementDefinition.pattern", "ElementDefinition.fixed")
	);
	private static final HashSet<String> ID_EXEMPT_LIST = new HashSet(Arrays.asList("id", "base64Binary", "markdown"));
	private static final HashSet<String> HTML_ELEMENTS = new HashSet(
		Arrays.asList(
			"p",
			"br",
			"div",
			"h1",
			"h2",
			"h3",
			"h4",
			"h5",
			"h6",
			"a",
			"span",
			"b",
			"em",
			"i",
			"strong",
			"small",
			"big",
			"tt",
			"small",
			"dfn",
			"q",
			"var",
			"abbr",
			"acronym",
			"cite",
			"blockquote",
			"hr",
			"address",
			"bdo",
			"kbd",
			"q",
			"sub",
			"sup",
			"ul",
			"ol",
			"li",
			"dl",
			"dt",
			"dd",
			"pre",
			"table",
			"caption",
			"colgroup",
			"col",
			"thead",
			"tr",
			"tfoot",
			"tbody",
			"th",
			"td",
			"code",
			"samp",
			"img",
			"map",
			"area"
		)
	);
	private static final HashSet<String> HTML_ATTRIBUTES = new HashSet(
		Arrays.asList(
			"title",
			"style",
			"class",
			"id",
			"lang",
			"xml:lang",
			"dir",
			"accesskey",
			"tabindex",
			"span",
			"width",
			"align",
			"valign",
			"char",
			"charoff",
			"abbr",
			"axis",
			"headers",
			"scope",
			"rowspan",
			"colspan"
		)
	);
	private static final HashSet<String> HTML_COMBO_LIST = new HashSet(
		Arrays.asList(
			"a.href",
			"a.name",
			"img.src",
			"img.border",
			"div.xmlns",
			"blockquote.cite",
			"q.cite",
			"a.charset",
			"a.type",
			"a.name",
			"a.href",
			"a.hreflang",
			"a.rel",
			"a.rev",
			"a.shape",
			"a.coords",
			"img.src",
			"img.alt",
			"img.longdesc",
			"img.height",
			"img.width",
			"img.usemap",
			"img.ismap",
			"map.name",
			"area.shape",
			"area.coords",
			"area.href",
			"area.nohref",
			"area.alt",
			"table.summary",
			"table.width",
			"table.border",
			"table.frame",
			"table.rules",
			"table.cellspacing",
			"table.cellpadding",
			"pre.space",
			"td.nowrap"
		)
	);
	private static final HashSet<String> HTML_BLOCK_LIST = new HashSet(Arrays.asList("div", "blockquote", "table", "ol", "ul", "p"));
	private static final HashSet<String> RESOURCE_X_POINTS = new HashSet(
		Arrays.asList(
			"Bundle.entry.resource",
			"Bundle.entry.response.outcome",
			"DomainResource.contained",
			"Parameters.parameter.resource",
			"Parameters.parameter.part.resource"
		)
	);
	private FHIRPathEngine fpe;
	private CheckDisplayOption checkDisplay;
	private boolean anyExtensionsAllowed;
	private boolean errorForUnknownProfiles;
	private boolean noInvariantChecks;
	private boolean wantInvariantInMessage;
	private boolean noTerminologyChecks;
	private boolean hintAboutNonMustSupport;
	private boolean showMessagesFromReferences;
	private BestPracticeWarningLevel bpWarnings;
	private String validationLanguage;
	private boolean baseOnly;
	private boolean noCheckAggregation;
	private boolean wantCheckSnapshotUnchanged;
	private boolean noUnicodeBiDiControlChars;
	private HtmlInMarkdownCheck htmlInMarkdownCheck;
	private boolean allowComments;
	private boolean allowDoubleQuotesInFHIRPath;
	private List<ImplementationGuide> igs = new ArrayList();
	private List<String> extensionDomains = new ArrayList();
	private IdStatus resourceIdRule;
	private boolean allowXsiLocation;
	private boolean suppressLoincSnomedMessages;
	private boolean noBindingMsgSuppressed;
	private Map<String, Element> fetchCache = new HashMap();
	private HashMap<Element, ResourceValidationTracker> resourceTracker = new HashMap();
	private IValidatorResourceFetcher fetcher;
	private IValidationPolicyAdvisor policyAdvisor;
	long time = 0L;
	long start = 0L;
	long lastlog = 0L;
	private IEvaluationContext externalHostServices;
	private boolean noExtensibleWarnings;
	private String serverBase;
	private EnableWhenEvaluator myEnableWhenEvaluator = new EnableWhenEvaluator();
	private String executionId;
	private IValidationProfileUsageTracker tracker;
	private InstanceValidator.ValidatorHostServices validatorServices;
	private boolean assumeValidRestReferences;
	private boolean securityChecks;
	private ProfileUtilities profileUtilities;
	private boolean crumbTrails;
	private List<BundleValidationRule> bundleValidationRules = new ArrayList();
	private boolean validateValueSetCodesOnTxServer = true;
	private QuestionnaireMode questionnaireMode;
	private ValidationOptions baseOptions = new ValidationOptions();
	private Map<String, InstanceValidator.CanonicalResourceLookupResult> crLookups = new HashMap();
	private boolean logProgress;
	private CodingsObserver codingObserver;

	public FHIRPathEngine getFHIRPathEngine() {
		return this.fpe;
	}

	public InstanceValidator(@Nonnull IWorkerContext theContext, @Nonnull IEvaluationContext hostServices, @Nonnull XVerExtensionManager xverManager) {
		super(theContext, xverManager, false);
		this.start = System.currentTimeMillis();
		this.externalHostServices = hostServices;
		this.profileUtilities = new ProfileUtilities(theContext, null, null);
		this.fpe = new FHIRPathEngine(this.context);
		this.validatorServices = new InstanceValidator.ValidatorHostServices();
		this.fpe.setHostServices(this.validatorServices);
		if (theContext.getVersion().startsWith("3.0") || theContext.getVersion().startsWith("1.0")) {
			this.fpe.setLegacyMode(true);
		}

		this.source = Source.InstanceValidator;
		this.fpe.setDoNotEnforceAsSingletonRule(!VersionUtilities.isR5VerOrLater(theContext.getVersion()));
		this.fpe.setAllowDoubleQuotes(this.allowDoubleQuotesInFHIRPath);
		this.codingObserver = new CodingsObserver(theContext, xverManager, this.debug);
	}

	public boolean isNoExtensibleWarnings() {
		return this.noExtensibleWarnings;
	}

	public IResourceValidator setNoExtensibleWarnings(boolean noExtensibleWarnings) {
		this.noExtensibleWarnings = noExtensibleWarnings;
		return this;
	}

	public boolean isShowMessagesFromReferences() {
		return this.showMessagesFromReferences;
	}

	public void setShowMessagesFromReferences(boolean showMessagesFromReferences) {
		this.showMessagesFromReferences = showMessagesFromReferences;
	}

	public boolean isNoInvariantChecks() {
		return this.noInvariantChecks;
	}

	public IResourceValidator setNoInvariantChecks(boolean value) {
		this.noInvariantChecks = value;
		return this;
	}

	public boolean isWantInvariantInMessage() {
		return this.wantInvariantInMessage;
	}

	public IResourceValidator setWantInvariantInMessage(boolean wantInvariantInMessage) {
		this.wantInvariantInMessage = wantInvariantInMessage;
		return this;
	}

	public IValidatorResourceFetcher getFetcher() {
		return this.fetcher;
	}

	public IResourceValidator setFetcher(IValidatorResourceFetcher value) {
		this.fetcher = value;
		return this;
	}

	public IValidationPolicyAdvisor getPolicyAdvisor() {
		return this.policyAdvisor;
	}

	public IResourceValidator setPolicyAdvisor(IValidationPolicyAdvisor advisor) {
		this.policyAdvisor = advisor;
		return this;
	}

	public IValidationProfileUsageTracker getTracker() {
		return this.tracker;
	}

	public IResourceValidator setTracker(IValidationProfileUsageTracker value) {
		this.tracker = value;
		return this;
	}

	public boolean isHintAboutNonMustSupport() {
		return this.hintAboutNonMustSupport;
	}

	public void setHintAboutNonMustSupport(boolean hintAboutNonMustSupport) {
		this.hintAboutNonMustSupport = hintAboutNonMustSupport;
	}

	public boolean isAssumeValidRestReferences() {
		return this.assumeValidRestReferences;
	}

	public void setAssumeValidRestReferences(boolean value) {
		this.assumeValidRestReferences = value;
	}

	public boolean isAllowComments() {
		return this.allowComments;
	}

	public void setAllowComments(boolean allowComments) {
		this.allowComments = allowComments;
	}

	public boolean isCrumbTrails() {
		return this.crumbTrails;
	}

	public void setCrumbTrails(boolean crumbTrails) {
		this.crumbTrails = crumbTrails;
	}

	public boolean isDoImplicitFHIRPathStringConversion() {
		return this.fpe.isDoImplicitStringConversion();
	}

	public void setDoImplicitFHIRPathStringConversion(boolean doImplicitFHIRPathStringConversion) {
		this.fpe.setDoImplicitStringConversion(doImplicitFHIRPathStringConversion);
	}

	private boolean allowUnknownExtension(String url) {
		if ((!this.allowExamples || !url.contains("example.org") && !url.contains("acme.com"))
			&& !url.contains("nema.org")
			&& !url.startsWith("http://hl7.org/fhir/tools/StructureDefinition/")
			&& !url.equals("http://hl7.org/fhir/StructureDefinition/structuredefinition-expression")) {
			for(String s : this.extensionDomains) {
				if (url.startsWith(s)) {
					return true;
				}
			}

			return this.anyExtensionsAllowed;
		} else {
			return true;
		}
	}

	private boolean isKnownExtension(String url) {
		if ((!this.allowExamples || !url.contains("example.org") && !url.contains("acme.com"))
			&& !url.contains("nema.org")
			&& !url.startsWith("http://hl7.org/fhir/tools/StructureDefinition/")
			&& !url.equals("http://hl7.org/fhir/StructureDefinition/structuredefinition-expression")
			&& !url.equals("http://hl7.org/fhir/StructureDefinition/codesystem-properties-mode")) {
			for(String s : this.extensionDomains) {
				if (url.startsWith(s)) {
					return true;
				}
			}

			return false;
		} else {
			return true;
		}
	}

	private boolean bpCheck(
		List<ValidationMessage> errors, IssueType invalid, int line, int col, String literalPath, boolean test, String message, Object... theMessageArguments
	) {
		if (this.bpWarnings != null) {
			switch(this.bpWarnings) {
				case Error:
					this.rule(errors, NO_RULE_DATE, invalid, line, col, literalPath, test, message, theMessageArguments);
					return test;
				case Warning:
					this.warning(errors, NO_RULE_DATE, invalid, line, col, literalPath, test, message, theMessageArguments);
					return true;
				case Hint:
					this.hint(errors, NO_RULE_DATE, invalid, line, col, literalPath, test, message, theMessageArguments);
					return true;
			}
		}

		return true;
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, InputStream stream, FhirFormat format) throws FHIRException {
		return this.validate(appContext, errors, stream, format, new ArrayList());
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, InputStream stream, FhirFormat format, String profile) throws FHIRException {
		ArrayList<StructureDefinition> profiles = new ArrayList();
		if (profile != null) {
			profiles.add(this.getSpecifiedProfile(profile));
		}

		return this.validate(appContext, errors, stream, format, profiles);
	}

	private StructureDefinition getSpecifiedProfile(String profile) {
		StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, profile);
		if (sd == null) {
			throw new FHIRException(this.context.formatMessage("Unable_to_locate_the_profile__in_order_to_validate_against_it", new Object[]{profile}));
		} else {
			return sd;
		}
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, InputStream stream, FhirFormat format, List<StructureDefinition> profiles) throws FHIRException {
		ParserBase parser = Manager.makeParser(this.context, format);
		List<StructureDefinition> logicals = new ArrayList();

		for(StructureDefinition sd : profiles) {
			if (sd.getKind() == StructureDefinitionKind.LOGICAL) {
				logicals.add(sd);
			}
		}

		if (logicals.size() > 0
			&& this.rulePlural(
			errors,
			NO_RULE_DATE,
			IssueType.BUSINESSRULE,
			"Configuration",
			logicals.size() == 1,
			logicals.size(),
			"MULTIPLE_LOGICAL_MODELS",
			new Object[]{ResourceUtilities.listUrls(logicals)}
		)) {
			parser.setLogical((StructureDefinition)logicals.get(0));
		}

		if (parser instanceof XmlParser) {
			((XmlParser)parser).setAllowXsiLocation(this.allowXsiLocation);
		}

		parser.setupValidation(ValidationPolicy.EVERYTHING, errors);
		if (parser instanceof XmlParser) {
			((XmlParser)parser).setAllowXsiLocation(this.allowXsiLocation);
		}

		if (parser instanceof JsonParser) {
			((JsonParser)parser).setAllowComments(this.allowComments);
		}

		long t = System.nanoTime();
		List<NamedElement> list = null;

		try {
			list = parser.parse(stream);
		} catch (IOException var14) {
			throw new FHIRException(var14);
		}

		this.timeTracker.load(t);
		if (list != null && !list.isEmpty()) {
			String url = parser.getImpliedProfile();
			if (url != null) {
				StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, url);
				if (sd == null) {
					this.rule(errors, NO_RULE_DATE, IssueType.NOTFOUND, "Payload", false, "Implied profile " + url + " not known to validator", new Object[0]);
				} else {
					profiles.add(sd);
				}
			}

			for(NamedElement ne : list) {
				this.validate(appContext, errors, ne.getName(), ne.getElement(), profiles);
			}
		}

		return list != null && !list.isEmpty() ? ((NamedElement)list.get(0)).getElement() : null;
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, Resource resource) throws FHIRException {
		return this.validate(appContext, errors, resource, new ArrayList());
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, Resource resource, String profile) throws FHIRException {
		ArrayList<StructureDefinition> profiles = new ArrayList();
		if (profile != null) {
			profiles.add(this.getSpecifiedProfile(profile));
		}

		return this.validate(appContext, errors, resource, profiles);
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, Resource resource, List<StructureDefinition> profiles) throws FHIRException {
		long t = System.nanoTime();
		Element e = new ResourceParser(this.context).parse(resource);
		this.timeTracker.load(t);
		this.validate(appContext, errors, null, e, profiles);
		return e;
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, org.w3c.dom.Element element) throws FHIRException {
		return this.validate(appContext, errors, element, new ArrayList());
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, org.w3c.dom.Element element, String profile) throws FHIRException {
		ArrayList<StructureDefinition> profiles = new ArrayList();
		if (profile != null) {
			profiles.add(this.getSpecifiedProfile(profile));
		}

		return this.validate(appContext, errors, element, profiles);
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, org.w3c.dom.Element element, List<StructureDefinition> profiles) throws FHIRException {
		XmlParser parser = new XmlParser(this.context);
		parser.setupValidation(ValidationPolicy.EVERYTHING, errors);
		long t = System.nanoTime();

		Element e;
		try {
			e = parser.parse(element);
		} catch (IOException var10) {
			throw new FHIRException(var10);
		}

		this.timeTracker.load(t);
		if (e != null) {
			this.validate(appContext, errors, null, e, profiles);
		}

		return e;
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, Document document) throws FHIRException {
		return this.validate(appContext, errors, document, new ArrayList());
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, Document document, String profile) throws FHIRException {
		ArrayList<StructureDefinition> profiles = new ArrayList();
		if (profile != null) {
			profiles.add(this.getSpecifiedProfile(profile));
		}

		return this.validate(appContext, errors, document, profiles);
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, Document document, List<StructureDefinition> profiles) throws FHIRException {
		XmlParser parser = new XmlParser(this.context);
		parser.setupValidation(ValidationPolicy.EVERYTHING, errors);
		long t = System.nanoTime();

		Element e;
		try {
			e = parser.parse(document);
		} catch (IOException var10) {
			throw new FHIRException(var10);
		}

		this.timeTracker.load(t);
		if (e != null) {
			this.validate(appContext, errors, null, e, profiles);
		}

		return e;
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, JsonObject object) throws FHIRException {
		return this.validate(appContext, errors, object, new ArrayList());
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, JsonObject object, String profile) throws FHIRException {
		ArrayList<StructureDefinition> profiles = new ArrayList();
		if (profile != null) {
			profiles.add(this.getSpecifiedProfile(profile));
		}

		return this.validate(appContext, errors, object, profiles);
	}

	public Element validate(Object appContext, List<ValidationMessage> errors, JsonObject object, List<StructureDefinition> profiles) throws FHIRException {
		JsonParser parser = new JsonParser(this.context, new ProfileUtilities(this.context, null, null, this.fpe));
		parser.setupValidation(ValidationPolicy.EVERYTHING, errors);
		long t = System.nanoTime();
		Element e = parser.parse(object);
		this.timeTracker.load(t);
		if (e != null) {
			this.validate(appContext, errors, null, e, profiles);
		}

		return e;
	}

	public void validate(Object appContext, List<ValidationMessage> errors, String initialPath, Element element) throws FHIRException {
		this.validate(appContext, errors, initialPath, element, new ArrayList());
	}

	public void validate(Object appContext, List<ValidationMessage> errors, String initialPath, Element element, String profile) throws FHIRException {
		ArrayList<StructureDefinition> profiles = new ArrayList();
		if (profile != null) {
			profiles.add(this.getSpecifiedProfile(profile));
		}

		this.validate(appContext, errors, initialPath, element, profiles);
	}

	public void validate(Object appContext, List<ValidationMessage> errors, String path, Element element, List<StructureDefinition> profiles) throws FHIRException {
		this.fetchCache.clear();
		this.fetchCache.put(element.fhirType() + "/" + element.getIdBase(), element);
		this.resourceTracker.clear();
		this.trackedMessages.clear();
		this.messagesToRemove.clear();
		this.executionId = UUID.randomUUID().toString();
		this.baseOnly = profiles.isEmpty();
		setParents(element);
		long t = System.nanoTime();
		NodeStack stack = new NodeStack(this.context, path, element, this.validationLanguage);
		if (profiles != null && !profiles.isEmpty()) {
			for(int i = 0; i < profiles.size(); ++i) {
				StructureDefinition sd = (StructureDefinition)profiles.get(i);
				if (sd.hasExtension("http://hl7.org/fhir/StructureDefinition/structuredefinition-imposeProfile")) {
					for(Extension ext : sd.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/structuredefinition-imposeProfile")) {
						StructureDefinition dep = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, ext.getValue().primitiveValue(), sd);
						if (dep == null) {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.BUSINESSRULE,
								element.line(),
								element.col(),
								stack.getLiteralPath(),
								false,
								"VALIDATION_VAL_PROFILE_DEPENDS_NOT_RESOLVED",
								new Object[]{ext.getValue().primitiveValue(), sd.getVersionedUrl()}
							);
						} else if (!profiles.contains(dep)) {
							profiles.add(dep);
						}
					}
				}
			}

			for(StructureDefinition defn : profiles) {
				this.validateResource(
					new ValidatorHostContext(appContext, element),
					errors,
					element,
					element,
					defn,
					this.resourceIdRule,
					stack.resetIds(),
					null,
					new ValidationMode(ValidationReason.Validation, ProfileSource.ConfigProfile)
				);
			}
		} else {
			this.validateResource(
				new ValidatorHostContext(appContext, element),
				errors,
				element,
				element,
				null,
				this.resourceIdRule,
				stack.resetIds(),
				null,
				new ValidationMode(ValidationReason.Validation, ProfileSource.BaseDefinition)
			);
		}

		if (this.hintAboutNonMustSupport) {
			this.checkElementUsage(errors, element, stack);
		}

		this.codingObserver.finish(errors, stack);
		errors.removeAll(this.messagesToRemove);
		this.timeTracker.overall(t);
	}

	private void checkElementUsage(List<ValidationMessage> errors, Element element, NodeStack stack) {
		String elementUsage = element.getUserString("elementSupported");
		this.hint(
			errors,
			NO_RULE_DATE,
			IssueType.INFORMATIONAL,
			element.line(),
			element.col(),
			stack.getLiteralPath(),
			elementUsage == null || elementUsage.equals("Y"),
			"MustSupport_VAL_MustSupport",
			new Object[]{element.getName(), element.getProperty().getStructure().getVersionedUrl()}
		);
		if (element.hasChildren()) {
			String prevName = "";
			int elementCount = 0;

			for(Element ce : element.getChildren()) {
				if (ce.getName().equals(prevName)) {
					++elementCount;
				} else {
					elementCount = 1;
					prevName = ce.getName();
				}

				this.checkElementUsage(errors, ce, stack.push(ce, elementCount, null, null));
			}
		}
	}

	private boolean check(String v1, String v2) {
		return v1 == null ? Utilities.noString(v1) : v1.equals(v2);
	}

	private boolean checkAddress(List<ValidationMessage> errors, String path, Element focus, Address fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".use", focus.getNamedChild("use"), fixed.getUseElement(), fixedSource, "use", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".text", focus.getNamedChild("text"), fixed.getTextElement(), fixedSource, "text", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".city", focus.getNamedChild("city"), fixed.getCityElement(), fixedSource, "city", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".state", focus.getNamedChild("state"), fixed.getStateElement(), fixedSource, "state", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".country", focus.getNamedChild("country"), fixed.getCountryElement(), fixedSource, "country", focus, pattern)
			&& ok;
		ok = this.checkFixedValue(errors, path + ".zip", focus.getNamedChild("zip"), fixed.getPostalCodeElement(), fixedSource, "postalCode", focus, pattern) && ok;
		List<Element> lines = new ArrayList();
		focus.getNamedChildren("line", lines);
		if (pattern) {
			boolean lineSizeCheck = lines.size() >= fixed.getLine().size();
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.VALUE,
				focus.line(),
				focus.col(),
				path,
				lineSizeCheck,
				"Fixed_Type_Checks_DT_Address_Line",
				new Object[]{Integer.toString(fixed.getLine().size()), Integer.toString(lines.size())}
			)) {
				for(int i = 0; i < fixed.getLine().size(); ++i) {
					StringType fixedLine = (StringType)fixed.getLine().get(i);
					boolean found = false;
					List<ValidationMessage> allErrorsFixed = new ArrayList();
					List<ValidationMessage> errorsFixed = null;

					for(int j = 0; j < lines.size() && !found; ++j) {
						errorsFixed = new ArrayList();
						this.checkFixedValue(errorsFixed, path + ".line", (Element)lines.get(j), fixedLine, fixedSource, "line", focus, pattern);
						if (!this.hasErrors(errorsFixed)) {
							found = true;
						} else {
							errorsFixed.stream().filter(t -> t.getLevel().ordinal() >= IssueSeverity.ERROR.ordinal()).forEach(t -> allErrorsFixed.add(t));
						}
					}

					if (!found) {
						ok = this.rule(
							errorsFixed,
							NO_RULE_DATE,
							IssueType.VALUE,
							focus.line(),
							focus.col(),
							path,
							false,
							"PATTERN_CHECK_STRING",
							new Object[]{fixedLine.getValue(), fixedSource, allErrorsFixed}
						)
							&& ok;
					}
				}
			}
		} else if (!pattern) {
			boolean lineSizeCheck = lines.size() == fixed.getLine().size();
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.VALUE,
				focus.line(),
				focus.col(),
				path,
				lineSizeCheck,
				"Fixed_Type_Checks_DT_Address_Line",
				new Object[]{Integer.toString(fixed.getLine().size()), Integer.toString(lines.size())}
			)) {
				for(int i = 0; i < lines.size(); ++i) {
					ok = this.checkFixedValue(
						errors, path + ".line", (Element)lines.get(i), (org.hl7.fhir.r5.model.Element)fixed.getLine().get(i), fixedSource, "line", focus, pattern
					)
						&& ok;
				}
			}
		}

		return ok;
	}

	private boolean checkAttachment(List<ValidationMessage> errors, String path, Element focus, Attachment fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(
			errors, path + ".contentType", focus.getNamedChild("contentType"), fixed.getContentTypeElement(), fixedSource, "contentType", focus, pattern
		)
			&& ok;
		ok = this.checkFixedValue(errors, path + ".language", focus.getNamedChild("language"), fixed.getLanguageElement(), fixedSource, "language", focus, pattern)
			&& ok;
		ok = this.checkFixedValue(errors, path + ".data", focus.getNamedChild("data"), fixed.getDataElement(), fixedSource, "data", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".url", focus.getNamedChild("url"), fixed.getUrlElement(), fixedSource, "url", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".size", focus.getNamedChild("size"), fixed.getSizeElement(), fixedSource, "size", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".hash", focus.getNamedChild("hash"), fixed.getHashElement(), fixedSource, "hash", focus, pattern) && ok;
		return this.checkFixedValue(errors, path + ".title", focus.getNamedChild("title"), fixed.getTitleElement(), fixedSource, "title", focus, pattern) && ok;
	}

	private boolean checkCode(
		List<ValidationMessage> errors,
		Element element,
		String path,
		String code,
		String system,
		String version,
		String display,
		boolean checkDisplay,
		NodeStack stack
	) throws TerminologyServiceException {
		long t = System.nanoTime();
		boolean ss = this.context.supportsSystem(system);
		this.timeTracker.tx(t, "ss " + system);
		if (ss) {
			t = System.nanoTime();
			ValidationResult s = this.checkCodeOnServer(stack, code, system, version, display, checkDisplay);
			this.timeTracker.tx(t, "vc " + system + "#" + code + " '" + display + "'");
			if (s == null) {
				return true;
			} else {
				if (s != null && s.isOk()) {
					for(OperationOutcomeIssueComponent iss : s.getIssues()) {
						this.txIssue(errors, "2023-08-19", s.getTxLink(), element.line(), element.col(), path, iss);
					}
				}

				if (s.isOk()) {
					if (s.getMessage() != null) {
						this.txWarning(
							errors,
							NO_RULE_DATE,
							s.getTxLink(),
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							s == null,
							"Terminology_PassThrough_TX_Message",
							new Object[]{s.getMessage(), system, code}
						);
					}

					return true;
				} else {
					if (s.getErrorClass() != null && s.getErrorClass().isInfrastructure()) {
						this.txWarning(
							errors, NO_RULE_DATE, s.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, s == null, s.getMessage(), new Object[0]
						);
					} else if (s.getSeverity() == IssueSeverity.INFORMATION) {
						this.txHint(
							errors, NO_RULE_DATE, s.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, s == null, s.getMessage(), new Object[0]
						);
					} else {
						if (s.getSeverity() != IssueSeverity.WARNING) {
							return this.txRule(
								errors,
								NO_RULE_DATE,
								s.getTxLink(),
								IssueType.CODEINVALID,
								element.line(),
								element.col(),
								path,
								s == null,
								"Terminology_PassThrough_TX_Message",
								new Object[]{s.getMessage(), system, code}
							);
						}

						this.txWarning(
							errors, NO_RULE_DATE, s.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, s == null, s.getMessage(), new Object[0]
						);
					}

					return true;
				}
			}
		} else if (system.startsWith("http://build.fhir.org") || system.startsWith("https://build.fhir.org")) {
			this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.CODEINVALID,
				element.line(),
				element.col(),
				path,
				false,
				"TERMINOLOGY_TX_SYSTEM_WRONG_BUILD",
				new Object[]{system, this.suggestSystemForBuild(system)}
			);
			return false;
		} else if (!system.startsWith("http://hl7.org/fhir")
			&& !system.startsWith("https://hl7.org/fhir")
			&& !system.startsWith("http://www.hl7.org/fhir")
			&& !system.startsWith("https://www.hl7.org/fhir")) {
			if (this.context.isNoTerminologyServer() && NO_TX_SYSTEM_EXEMPT.contains(system)) {
				return true;
			} else if (this.startsWithButIsNot(
				system, "http://snomed.info/sct", "http://loinc.org", "http://unitsofmeasure.org", "http://www.nlm.nih.gov/research/umls/rxnorm"
			)) {
				this.rule(
					errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, false, "Terminology_TX_System_Invalid", new Object[]{system}
				);
				return false;
			} else {
				try {
					if (this.context.fetchResourceWithException(ValueSet.class, system, element.getProperty().getStructure()) != null) {
						this.rule(
							errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, false, "Terminology_TX_System_ValueSet", new Object[]{system}
						);
					}

					boolean done = false;
					if (system.startsWith("https:") && system.length() > 7) {
						String ns = "http:" + system.substring(6);
						CodeSystem cs = this.getCodeSystem(ns);
						if (cs != null || NO_HTTPS_LIST.contains(system)) {
							this.rule(
								errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, false, "TERMINOLOGY_TX_SYSTEM_HTTPS", new Object[]{system}
							);
							done = true;
						}
					}

					if (!this.isAllowExamples() || !Utilities.startsWithInList(system, new String[]{"http://example.org", "https://example.org"})) {
						CodeSystem cs = this.context.fetchCodeSystem(system);
						if (cs == null) {
							this.hint(
								errors, NO_RULE_DATE, IssueType.UNKNOWN, element.line(), element.col(), path, done, "Terminology_TX_System_NotKnown", new Object[]{system}
							);
						} else if (this.hint(
							errors,
							NO_RULE_DATE,
							IssueType.UNKNOWN,
							element.line(),
							element.col(),
							path,
							cs.getContent() != CodeSystemContentMode.NOTPRESENT,
							"TERMINOLOGY_TX_SYSTEM_NOT_USABLE",
							new Object[]{system}
						)) {
							this.rule(
								errors,
								NO_RULE_DATE,
								IssueType.UNKNOWN,
								element.line(),
								element.col(),
								path,
								false,
								"Error - this should not happen? (Consult GG)",
								new Object[0]
							);
						}
					}

					return true;
				} catch (Exception var16) {
					return true;
				}
			}
		} else if (SIDUtilities.isknownCodeSystem(system)) {
			return true;
		} else if (system.startsWith("http://hl7.org/fhir/test")) {
			return true;
		} else if (system.endsWith(".html")) {
			this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.CODEINVALID,
				element.line(),
				element.col(),
				path,
				false,
				"TERMINOLOGY_TX_SYSTEM_WRONG_HTML",
				new Object[]{system, this.suggestSystemForPage(system)}
			);
			return false;
		} else {
			CodeSystem cs = this.getCodeSystem(system);
			if (this.rule(
				errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, cs != null, "Terminology_TX_System_Unknown", new Object[]{system}
			)) {
				ConceptDefinitionComponent def = this.getCodeDefinition(cs, code);
				if (this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					element.line(),
					element.col(),
					path,
					def != null,
					"Terminology_TX_Code_Unknown",
					new Object[]{system, code}
				)) {
					return this.warning(
						errors,
						NO_RULE_DATE,
						IssueType.CODEINVALID,
						element.line(),
						element.col(),
						path,
						display == null || display.equals(def.getDisplay()),
						"Terminology_TX_Display_Wrong",
						new Object[]{def.getDisplay()}
					);
				}
			}

			return false;
		}
	}

	private Object suggestSystemForPage(String system) {
		if (system.contains("/codesystem-")) {
			String s = system.substring(system.indexOf("/codesystem-") + 12);
			String url = "http://hl7.org/fhir/" + s.replace(".html", "");
			return this.context.fetchCodeSystem(url) != null ? url : "{unable to determine intended url}";
		} else if (system.contains("/valueset-")) {
			String s = system.substring(system.indexOf("/valueset-") + 8);
			String url = "http://hl7.org/fhir/" + s.replace(".html", "");
			return this.context.fetchCodeSystem(url) != null ? url : "{unable to determine intended url}";
		} else {
			return "{unable to determine intended url}";
		}
	}

	private Object suggestSystemForBuild(String system) {
		if (system.contains("/codesystem-")) {
			String s = system.substring(system.indexOf("/codesystem-") + 12);
			String url = "http://hl7.org/fhir/" + s.replace(".html", "");
			return this.context.fetchCodeSystem(url) != null ? url : "{unable to determine intended url}";
		} else if (system.contains("/valueset-")) {
			String s = system.substring(system.indexOf("/valueset-") + 8);
			String url = "http://hl7.org/fhir/" + s.replace(".html", "");
			return this.context.fetchCodeSystem(url) != null ? url : "{unable to determine intended url}";
		} else {
			system = system.replace("https://", "http://");
			if (system.length() < 22) {
				return "{unable to determine intended url}";
			} else {
				system = "http://hl7.org/fhir/" + system.substring(22).replace(".html", "");
				return this.context.fetchCodeSystem(system) != null ? system : "{unable to determine intended url}";
			}
		}
	}

	private boolean startsWithButIsNot(String system, String... uri) {
		for(String s : uri) {
			if (!system.equals(s) && system.startsWith(s)) {
				return true;
			}
		}

		return false;
	}

	private boolean hasErrors(List<ValidationMessage> errors) {
		if (errors != null) {
			for(ValidationMessage vm : errors) {
				if (vm.getLevel() == IssueSeverity.FATAL || vm.getLevel() == IssueSeverity.ERROR) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean checkCodeableConcept(List<ValidationMessage> errors, String path, Element focus, CodeableConcept fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".text", focus.getNamedChild("text"), fixed.getTextElement(), fixedSource, "text", focus, pattern) && ok;
		List<Element> codings = new ArrayList();
		focus.getNamedChildren("coding", codings);
		if (pattern) {
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.VALUE,
				focus.line(),
				focus.col(),
				path,
				codings.size() >= fixed.getCoding().size(),
				"Terminology_TX_Coding_Count",
				new Object[]{Integer.toString(fixed.getCoding().size()), Integer.toString(codings.size())}
			)) {
				for(int i = 0; i < fixed.getCoding().size(); ++i) {
					Coding fixedCoding = (Coding)fixed.getCoding().get(i);
					boolean found = false;
					List<ValidationMessage> allErrorsFixed = new ArrayList();

					for(int j = 0; j < codings.size() && !found; ++j) {
						List<ValidationMessage> errorsFixed = new ArrayList();
						ok = this.checkFixedValue(errorsFixed, path + ".coding", (Element)codings.get(j), fixedCoding, fixedSource, "coding", focus, pattern) && ok;
						if (!this.hasErrors(errorsFixed)) {
							found = true;
						} else {
							errorsFixed.stream().filter(t -> t.getLevel().ordinal() >= IssueSeverity.ERROR.ordinal()).forEach(t -> allErrorsFixed.add(t));
						}
					}

					if (!found) {
						if (fixedCoding.hasUserSelected()) {
							ok = this.rule(
								errors,
								NO_RULE_DATE,
								IssueType.VALUE,
								focus.line(),
								focus.col(),
								path,
								false,
								pattern ? "TYPE_CHECKS_PATTERN_CC_US" : "TYPE_CHECKS_FIXED_CC_US",
								new Object[]{
									fixedCoding.getSystemElement().asStringValue(),
									fixedCoding.getCodeElement().asStringValue(),
									fixedCoding.getDisplayElement().asStringValue(),
									fixedSource,
									allErrorsFixed,
									fixedCoding.getUserSelected()
								}
							)
								&& ok;
						} else {
							ok = this.rule(
								errors,
								NO_RULE_DATE,
								IssueType.VALUE,
								focus.line(),
								focus.col(),
								path,
								false,
								pattern ? "TYPE_CHECKS_PATTERN_CC" : "TYPE_CHECKS_FIXED_CC",
								new Object[]{
									fixedCoding.getSystemElement().asStringValue(),
									fixedCoding.getCodeElement().asStringValue(),
									fixedCoding.getDisplayElement().asStringValue(),
									fixedSource,
									allErrorsFixed
								}
							)
								&& ok;
						}
					}
				}
			} else {
				ok = false;
			}
		} else if (this.rule(
			errors,
			NO_RULE_DATE,
			IssueType.VALUE,
			focus.line(),
			focus.col(),
			path,
			codings.size() == fixed.getCoding().size(),
			"Terminology_TX_Coding_Count",
			new Object[]{Integer.toString(fixed.getCoding().size()), Integer.toString(codings.size())}
		)) {
			for(int i = 0; i < codings.size(); ++i) {
				ok = this.checkFixedValue(
					errors, path + ".coding", (Element)codings.get(i), (org.hl7.fhir.r5.model.Element)fixed.getCoding().get(i), fixedSource, "coding", focus, false
				)
					&& ok;
			}
		} else {
			ok = false;
		}

		return ok;
	}

	private boolean checkCodeableConcept(
		List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, ElementDefinition theElementCntext, NodeStack stack
	) {
		boolean res = true;
		if (!this.noTerminologyChecks && theElementCntext != null && theElementCntext.hasBinding()) {
			ElementDefinitionBindingComponent binding = theElementCntext.getBinding();
			if (this.warning(
				errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, binding != null, "Terminology_TX_Binding_Missing", new Object[]{path}
			)) {
				if (binding.hasValueSet()) {
					ValueSet valueset = this.resolveBindingReference(profile, binding.getValueSet(), profile.getUrl(), profile);
					if (valueset == null) {
						CodeSystem cs = this.context.fetchCodeSystem(binding.getValueSet());
						if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							cs == null,
							"Terminology_TX_ValueSet_NotFound_CS",
							new Object[]{this.describeReference(binding.getValueSet())}
						)) {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.CODEINVALID,
								element.line(),
								element.col(),
								path,
								valueset != null,
								"Terminology_TX_ValueSet_NotFound",
								new Object[]{this.describeReference(binding.getValueSet())}
							);
						}
					} else {
						try {
							CodeableConcept cc = ObjectConverter.readAsCodeableConcept(element);
							if (!cc.hasCoding()) {
								if (binding.getStrength() == BindingStrength.REQUIRED) {
									this.rule(
										errors,
										NO_RULE_DATE,
										IssueType.CODEINVALID,
										element.line(),
										element.col(),
										path,
										false,
										"Terminology_TX_Code_ValueSet",
										new Object[]{this.describeReference(binding.getValueSet(), valueset)}
									);
								} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
									if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
										this.rule(
											errors,
											NO_RULE_DATE,
											IssueType.CODEINVALID,
											element.line(),
											element.col(),
											path,
											false,
											"Terminology_TX_Code_ValueSetMax",
											new Object[]{
												this.describeReference(
													ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")
												),
												valueset.getVersionedUrl()
											}
										);
									} else {
										this.warning(
											errors,
											NO_RULE_DATE,
											IssueType.CODEINVALID,
											element.line(),
											element.col(),
											path,
											false,
											"Terminology_TX_Code_ValueSet_Ext",
											new Object[]{this.describeReference(binding.getValueSet(), valueset)}
										);
									}
								}
							} else {
								long t = System.nanoTime();
								boolean bindingsOk = true;
								if (binding.getStrength() != BindingStrength.EXAMPLE) {
									if (binding.getStrength() == BindingStrength.REQUIRED) {
										this.removeTrackedMessagesForLocation(errors, element, path);
									}

									boolean atLeastOneSystemIsSupported = false;

									for(Coding nextCoding : cc.getCoding()) {
										String nextSystem = nextCoding.getSystem();
										if (StringUtils.isNotBlank(nextSystem) && this.context.supportsSystem(nextSystem)) {
											atLeastOneSystemIsSupported = true;
											break;
										}
									}

									if (atLeastOneSystemIsSupported || binding.getStrength() != BindingStrength.EXAMPLE) {
										ValidationResult vr = this.checkCodeOnServer(stack, valueset, cc, true);
										if (vr != null && vr.isOk()) {
											for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
												this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
											}
										}

										if (vr.isOk()) {
											if (vr.getMessage() != null) {
												if (vr.getSeverity() == IssueSeverity.INFORMATION) {
													this.txHint(
														errors,
														"2023-07-03",
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														vr.getMessage(),
														new Object[0]
													);
												} else {
													res = false;
													this.txWarning(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														vr.getMessage(),
														new Object[0]
													);
												}
											} else {
												if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
													this.removeTrackedMessagesForLocation(errors, element, path);
												}

												res = false;
											}
										} else {
											bindingsOk = false;
											if (vr.getErrorClass() != null && vr.getErrorClass() == TerminologyServiceErrorClass.NOSERVICE) {
												if (binding.getStrength() != BindingStrength.REQUIRED
													&& (
													binding.getStrength() != BindingStrength.EXTENSIBLE
														|| !binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")
												)) {
													if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
														this.txHint(
															errors,
															NO_RULE_DATE,
															vr.getTxLink(),
															IssueType.CODEINVALID,
															element.line(),
															element.col(),
															path,
															false,
															"TERMINOLOGY_TX_NOSVC_BOUND_EXT",
															new Object[]{this.describeReference(binding.getValueSet())}
														);
													}
												} else {
													this.txHint(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"TERMINOLOGY_TX_NOSVC_BOUND_REQ",
														new Object[]{this.describeReference(binding.getValueSet())}
													);
												}
											} else if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure()) {
												if (binding.getStrength() == BindingStrength.REQUIRED) {
													this.txWarning(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_Confirm_1_CC",
														new Object[]{this.describeReference(binding.getValueSet()), vr.getErrorClass().toString()}
													);
												} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
													if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
														this.checkMaxValueSet(
															errors,
															path,
															element,
															profile,
															ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"),
															cc,
															stack
														);
													} else if (!this.noExtensibleWarnings) {
														this.txWarningForLaterRemoval(
															element,
															errors,
															NO_RULE_DATE,
															vr.getTxLink(),
															IssueType.CODEINVALID,
															element.line(),
															element.col(),
															path,
															false,
															"Terminology_TX_Confirm_2_CC",
															new Object[]{this.describeReference(binding.getValueSet()), vr.getErrorClass().toString()}
														);
													}
												} else if (binding.getStrength() == BindingStrength.PREFERRED && this.baseOnly) {
													this.txHint(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_Confirm_3_CC",
														new Object[]{this.describeReference(binding.getValueSet()), vr.getErrorClass().toString()}
													);
												}
											} else if (binding.getStrength() == BindingStrength.REQUIRED) {
												this.txRule(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_NoValid_1_CC",
													new Object[]{this.describeReference(binding.getValueSet(), valueset), this.ccSummary(cc)}
												);
											} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
												if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
													this.checkMaxValueSet(
														errors,
														path,
														element,
														profile,
														ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"),
														cc,
														stack
													);
												}

												if (!this.noExtensibleWarnings) {
													this.txWarningForLaterRemoval(
														element,
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_NoValid_2_CC",
														new Object[]{this.describeReference(binding.getValueSet(), valueset), this.ccSummary(cc)}
													);
												}
											} else if (binding.getStrength() == BindingStrength.PREFERRED && this.baseOnly) {
												this.txHint(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_NoValid_3_CC",
													new Object[]{this.describeReference(binding.getValueSet(), valueset), this.ccSummary(cc)}
												);
											}
										}
									}

									if (bindingsOk) {
										for(Coding nextCoding : cc.getCoding()) {
											this.checkBindings(errors, path, element, stack, valueset, nextCoding);
										}
									}

									this.timeTracker.tx(t, "vc " + cc.toString());
								}
							}
						} catch (Exception var18) {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.CODEINVALID,
								element.line(),
								element.col(),
								path,
								false,
								"Terminology_TX_Error_CodeableConcept",
								new Object[]{var18.getMessage()}
							);
						}
					}
				} else if (binding.hasValueSet()) {
					this.hint(errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, false, "Terminology_TX_Binding_CantCheck");
				} else if (!this.noBindingMsgSuppressed) {
					this.hint(
						errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, false, "Terminology_TX_Binding_NoSource", new Object[]{path}
					);
				}
			}
		}

		return res;
	}

	public void checkBindings(List<ValidationMessage> errors, String path, Element element, NodeStack stack, ValueSet valueset, Coding nextCoding) {
		if (StringUtils.isNotBlank(nextCoding.getCode()) && StringUtils.isNotBlank(nextCoding.getSystem()) && this.context.supportsSystem(nextCoding.getSystem())) {
			ValidationResult vr = this.checkCodeOnServer(stack, valueset, nextCoding, false);
			if (vr != null && vr.isOk()) {
				for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
					this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
				}
			}

			if (vr.getSeverity() != null) {
				if (vr.getSeverity() == IssueSeverity.INFORMATION) {
					this.txHint(errors, NO_RULE_DATE, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, vr.getMessage(), new Object[0]);
				} else if (vr.getSeverity() == IssueSeverity.WARNING) {
					this.txWarning(
						errors, NO_RULE_DATE, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, vr.getMessage(), new Object[0]
					);
				} else {
					this.txRule(errors, NO_RULE_DATE, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, vr.getMessage(), new Object[0]);
				}
			}
		}
	}

	private boolean checkTerminologyCodeableConcept(
		List<ValidationMessage> errors,
		String path,
		Element element,
		StructureDefinition profile,
		ElementDefinition theElementCntext,
		NodeStack stack,
		StructureDefinition logical
	) {
		boolean res = true;
		if (!this.noTerminologyChecks && theElementCntext != null && theElementCntext.hasBinding()) {
			ElementDefinitionBindingComponent binding = theElementCntext.getBinding();
			if (this.warning(
				errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, binding != null, "Terminology_TX_Binding_Missing", new Object[]{path}
			)) {
				if (binding.hasValueSet()) {
					ValueSet valueset = this.resolveBindingReference(profile, binding.getValueSet(), profile.getUrl(), profile);
					if (valueset == null) {
						CodeSystem cs = this.context.fetchCodeSystem(binding.getValueSet());
						if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							cs == null,
							"Terminology_TX_ValueSet_NotFound_CS",
							new Object[]{this.describeReference(binding.getValueSet())}
						)) {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.CODEINVALID,
								element.line(),
								element.col(),
								path,
								valueset != null,
								"Terminology_TX_ValueSet_NotFound",
								new Object[]{this.describeReference(binding.getValueSet())}
							);
						}
					} else {
						try {
							CodeableConcept cc = this.convertToCodeableConcept(element, logical);
							if (!cc.hasCoding()) {
								if (binding.getStrength() == BindingStrength.REQUIRED) {
									this.rule(
										errors,
										NO_RULE_DATE,
										IssueType.CODEINVALID,
										element.line(),
										element.col(),
										path,
										false,
										"No code provided, and a code is required from the value set "
											+ this.describeReference(binding.getValueSet())
											+ " ("
											+ valueset.getVersionedUrl(),
										new Object[0]
									);
								} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
									if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
										this.rule(
											errors,
											NO_RULE_DATE,
											IssueType.CODEINVALID,
											element.line(),
											element.col(),
											path,
											false,
											"Terminology_TX_Code_ValueSetMax",
											new Object[]{
												this.describeReference(
													ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")
												),
												valueset.getVersionedUrl()
											}
										);
									} else {
										this.warning(
											errors,
											NO_RULE_DATE,
											IssueType.CODEINVALID,
											element.line(),
											element.col(),
											path,
											false,
											"Terminology_TX_Code_ValueSet_Ext",
											new Object[]{this.describeReference(binding.getValueSet(), valueset)}
										);
									}
								}
							} else {
								long t = System.nanoTime();
								boolean bindingsOk = true;
								if (binding.getStrength() != BindingStrength.EXAMPLE) {
									if (binding.getStrength() == BindingStrength.REQUIRED) {
										this.removeTrackedMessagesForLocation(errors, element, path);
									}

									boolean atLeastOneSystemIsSupported = false;

									for(Coding nextCoding : cc.getCoding()) {
										String nextSystem = nextCoding.getSystem();
										if (StringUtils.isNotBlank(nextSystem) && this.context.supportsSystem(nextSystem)) {
											atLeastOneSystemIsSupported = true;
											break;
										}
									}

									if (atLeastOneSystemIsSupported || binding.getStrength() != BindingStrength.EXAMPLE) {
										ValidationResult vr = this.checkCodeOnServer(stack, valueset, cc, false);
										if (vr != null && vr.isOk()) {
											for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
												this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
											}
										}

										if (!vr.isOk()) {
											bindingsOk = false;
											if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure()) {
												if (binding.getStrength() == BindingStrength.REQUIRED) {
													this.txWarning(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_Confirm_1_CC",
														new Object[]{this.describeReference(binding.getValueSet()), vr.getErrorClass().toString()}
													);
												} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
													if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
														this.checkMaxValueSet(
															errors,
															path,
															element,
															profile,
															ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"),
															cc,
															stack
														);
													} else if (!this.noExtensibleWarnings) {
														this.txWarningForLaterRemoval(
															element,
															errors,
															NO_RULE_DATE,
															vr.getTxLink(),
															IssueType.CODEINVALID,
															element.line(),
															element.col(),
															path,
															false,
															"Terminology_TX_Confirm_2_CC",
															new Object[]{this.describeReference(binding.getValueSet()), vr.getErrorClass().toString()}
														);
													}
												} else if (binding.getStrength() == BindingStrength.PREFERRED && this.baseOnly) {
													this.txHint(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_Confirm_3_CC",
														new Object[]{this.describeReference(binding.getValueSet()), vr.getErrorClass().toString()}
													);
												}
											} else if (binding.getStrength() == BindingStrength.REQUIRED) {
												this.txRule(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_NoValid_1_CC",
													new Object[]{this.describeReference(binding.getValueSet()), valueset, this.ccSummary(cc)}
												);
											} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
												if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
													this.checkMaxValueSet(
														errors,
														path,
														element,
														profile,
														ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"),
														cc,
														stack
													);
												}

												if (!this.noExtensibleWarnings) {
													this.txWarningForLaterRemoval(
														element,
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_NoValid_2_CC",
														new Object[]{this.describeReference(binding.getValueSet(), valueset), this.ccSummary(cc)}
													);
												}
											} else if (binding.getStrength() == BindingStrength.PREFERRED && this.baseOnly) {
												this.txHint(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_NoValid_3_CC",
													new Object[]{this.describeReference(binding.getValueSet(), valueset), this.ccSummary(cc)}
												);
											}
										} else if (vr.getMessage() != null) {
											res = false;
											if (vr.getSeverity() == IssueSeverity.INFORMATION) {
												this.txHint(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													vr.getMessage(),
													new Object[0]
												);
											} else {
												this.txWarning(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													vr.getMessage(),
													new Object[0]
												);
											}
										} else {
											res = false;
										}
									}

									if (bindingsOk) {
										for(Coding nextCoding : cc.getCoding()) {
											String nextCode = nextCoding.getCode();
											String nextSystem = nextCoding.getSystem();
											String nextVersion = nextCoding.getVersion();
											if (StringUtils.isNotBlank(nextCode) && StringUtils.isNotBlank(nextSystem) && this.context.supportsSystem(nextSystem)) {
												ValidationResult vr = this.checkCodeOnServer(stack, nextCode, nextSystem, nextVersion, null, false);
												if (vr != null && vr.isOk()) {
													for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
														this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
													}
												}

												if (!vr.isOk()) {
													this.txWarning(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_Code_NotValid",
														new Object[]{nextCode, nextSystem}
													);
												}
											}
										}
									}

									this.timeTracker.tx(t, DataRenderer.display(this.context, cc));
								}
							}
						} catch (Exception var24) {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.CODEINVALID,
								element.line(),
								element.col(),
								path,
								false,
								"Terminology_TX_Error_CodeableConcept",
								new Object[]{var24.getMessage()}
							);
						}

						if (this.getMapping("http://hl7.org/fhir/terminology-pattern", logical, logical.getSnapshot().getElementFirstRep()).contains("Coding")) {
							this.checkTerminologyCoding(errors, path, element, profile, theElementCntext, true, true, stack, logical);
						}
					}
				} else if (binding.hasValueSet()) {
					this.hint(errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, false, "Terminology_TX_Binding_CantCheck");
				} else if (!this.noBindingMsgSuppressed) {
					this.hint(
						errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, false, "Terminology_TX_Binding_NoSource", new Object[]{path}
					);
				}
			}
		}

		return res;
	}

	private boolean checkTerminologyCoding(
		List<ValidationMessage> errors,
		String path,
		Element element,
		StructureDefinition profile,
		ElementDefinition theElementCntext,
		boolean inCodeableConcept,
		boolean checkDisplay,
		NodeStack stack,
		StructureDefinition logical
	) {
		boolean ok = false;
		Coding c = this.convertToCoding(element, logical);
		String code = c.getCode();
		String system = c.getSystem();
		String display = c.getDisplay();
		String version = c.getVersion();
		ok = this.rule(
			errors,
			NO_RULE_DATE,
			IssueType.CODEINVALID,
			element.line(),
			element.col(),
			path,
			system == null || this.isCodeSystemReferenceValid(system),
			"Terminology_TX_System_Relative",
			new Object[0]
		)
			&& ok;
		if (system != null && code != null && !this.noTerminologyChecks) {
			ok = this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.CODEINVALID,
				element.line(),
				element.col(),
				path,
				!this.isValueSet(system),
				"Terminology_TX_System_ValueSet2",
				new Object[]{system}
			)
				&& ok;

			try {
				if (this.checkCode(errors, element, path, code, system, version, display, checkDisplay, stack)) {
					if (theElementCntext != null && theElementCntext.hasBinding()) {
						ElementDefinitionBindingComponent binding = theElementCntext.getBinding();
						if (this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							binding != null,
							"Terminology_TX_Binding_Missing2",
							new Object[]{path}
						)) {
							if (binding.hasValueSet()) {
								ValueSet valueset = this.resolveBindingReference(profile, binding.getValueSet(), profile.getUrl(), profile);
								if (valueset == null) {
									CodeSystem cs = this.context.fetchCodeSystem(binding.getValueSet());
									if (this.rule(
										errors,
										NO_RULE_DATE,
										IssueType.CODEINVALID,
										element.line(),
										element.col(),
										path,
										cs == null,
										"Terminology_TX_ValueSet_NotFound_CS",
										new Object[]{this.describeReference(binding.getValueSet())}
									)) {
										this.warning(
											errors,
											NO_RULE_DATE,
											IssueType.CODEINVALID,
											element.line(),
											element.col(),
											path,
											valueset != null,
											"Terminology_TX_ValueSet_NotFound",
											new Object[]{this.describeReference(binding.getValueSet())}
										);
									} else {
										ok = false;
									}
								} else {
									try {
										long t = System.nanoTime();
										ValidationResult vr = null;
										if (binding.getStrength() != BindingStrength.EXAMPLE) {
											vr = this.checkCodeOnServer(stack, valueset, c, true);
										}

										if (binding.getStrength() == BindingStrength.REQUIRED) {
											this.removeTrackedMessagesForLocation(errors, element, path);
										}

										if (vr != null && vr.isOk()) {
											for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
												this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
											}
										}

										this.timeTracker.tx(t, "vc " + system + "#" + code + " '" + display + "'");
										if (vr != null && !vr.isOk()) {
											if (vr.IsNoService()) {
												this.txHint(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_Binding_NoServer",
													new Object[]{system + "#" + code}
												);
											} else if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure()) {
												if (binding.getStrength() == BindingStrength.REQUIRED) {
													this.txWarning(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_Confirm_4a",
														new Object[]{this.describeReference(binding.getValueSet(), valueset), vr.getMessage(), system + "#" + code}
													);
												} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
													if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
														this.checkMaxValueSet(
															errors,
															path,
															element,
															profile,
															ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"),
															c,
															stack
														);
													} else if (!this.noExtensibleWarnings) {
														this.txWarningForLaterRemoval(
															element,
															errors,
															NO_RULE_DATE,
															vr.getTxLink(),
															IssueType.CODEINVALID,
															element.line(),
															element.col(),
															path,
															false,
															"Terminology_TX_Confirm_5",
															new Object[]{this.describeReference(binding.getValueSet(), valueset)}
														);
													}
												} else if (binding.getStrength() == BindingStrength.PREFERRED && this.baseOnly) {
													this.txHint(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_Confirm_6",
														new Object[]{this.describeReference(binding.getValueSet(), valueset)}
													);
												}
											} else if (binding.getStrength() == BindingStrength.REQUIRED) {
												ok = this.txRule(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_NoValid_4",
													new Object[]{
														this.describeReference(binding.getValueSet(), valueset),
														vr.getMessage() != null ? " (error message = " + vr.getMessage() + ")" : "",
														system + "#" + code
													}
												)
													&& ok;
											} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
												if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
													ok = this.checkMaxValueSet(
														errors,
														path,
														element,
														profile,
														ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"),
														c,
														stack
													)
														&& ok;
												} else {
													this.txWarning(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_NoValid_5",
														new Object[]{
															this.describeReference(binding.getValueSet(), valueset),
															vr.getMessage() != null ? " (error message = " + vr.getMessage() + ")" : "",
															system + "#" + code
														}
													);
												}
											} else if (binding.getStrength() == BindingStrength.PREFERRED && this.baseOnly) {
												this.txHint(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_NoValid_6",
													new Object[]{
														this.describeReference(binding.getValueSet(), valueset),
														vr.getMessage() != null ? " (error message = " + vr.getMessage() + ")" : "",
														system + "#" + code
													}
												);
											}
										} else if (vr != null && vr.getMessage() != null) {
											if (vr.getSeverity() == IssueSeverity.INFORMATION) {
												this.txHint(
													errors,
													"2023-07-04",
													vr.getTxLink(),
													IssueType.INFORMATIONAL,
													element.line(),
													element.col(),
													path,
													false,
													"TERMINOLOGY_TX_HINT",
													new Object[]{code, vr.getMessage()}
												);
											} else {
												this.txWarning(
													errors,
													"2023-07-04",
													vr.getTxLink(),
													IssueType.INFORMATIONAL,
													element.line(),
													element.col(),
													path,
													false,
													"TERMINOLOGY_TX_WARNING",
													new Object[]{code, vr.getMessage()}
												);
											}
										}
									} catch (Exception var23) {
										this.warning(
											errors,
											NO_RULE_DATE,
											IssueType.CODEINVALID,
											element.line(),
											element.col(),
											path,
											false,
											"Terminology_TX_Error_Coding1",
											new Object[]{var23.getMessage()}
										);
									}
								}
							} else if (binding.hasValueSet()) {
								this.hint(errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, false, "Terminology_TX_Binding_CantCheck");
							} else if (!inCodeableConcept && !this.noBindingMsgSuppressed) {
								this.hint(
									errors,
									NO_RULE_DATE,
									IssueType.CODEINVALID,
									element.line(),
									element.col(),
									path,
									false,
									"Terminology_TX_Binding_NoSource",
									new Object[]{path}
								);
							}
						}
					}
				} else {
					ok = false;
				}
			} catch (Exception var24) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					element.line(),
					element.col(),
					path,
					false,
					"Terminology_TX_Error_Coding2",
					new Object[]{var24.getMessage(), var24.toString()}
				);
				ok = false;
			}
		}

		return ok;
	}

	private CodeableConcept convertToCodeableConcept(Element element, StructureDefinition logical) {
		CodeableConcept res = new CodeableConcept();

		for(ElementDefinition ed : logical.getSnapshot().getElement()) {
			if (Utilities.charCount(ed.getPath(), '.') == 1) {
				for(String m : this.getMapping("http://hl7.org/fhir/terminology-pattern", logical, ed)) {
					String name = this.tail(ed.getPath());
					List<Element> list = new ArrayList();
					element.getNamedChildren(name, list);
					if (!list.isEmpty()) {
						if ("Coding.code".equals(m)) {
							res.getCodingFirstRep().setCode(((Element)list.get(0)).primitiveValue());
						} else if ("Coding.system[fmt:OID]".equals(m)) {
							String oid = ((Element)list.get(0)).primitiveValue();
							String url = new ContextUtilities(this.context).oid2Uri(oid);
							if (url != null) {
								res.getCodingFirstRep().setSystem(url);
							} else {
								res.getCodingFirstRep().setSystem("urn:oid:" + oid);
							}
						} else if ("Coding.version".equals(m)) {
							res.getCodingFirstRep().setVersion(((Element)list.get(0)).primitiveValue());
						} else if ("Coding.display".equals(m)) {
							res.getCodingFirstRep().setDisplay(((Element)list.get(0)).primitiveValue());
						} else if ("CodeableConcept.text".equals(m)) {
							res.setText(((Element)list.get(0)).primitiveValue());
						} else if ("CodeableConcept.coding".equals(m)) {
							StructureDefinition c = this.context.fetchTypeDefinition(ed.getTypeFirstRep().getCode());

							for(Element e : list) {
								res.addCoding(this.convertToCoding(e, c));
							}
						}
					}
				}
			}
		}

		return res;
	}

	private Coding convertToCoding(Element element, StructureDefinition logical) {
		Coding res = new Coding();

		for(ElementDefinition ed : logical.getSnapshot().getElement()) {
			if (Utilities.charCount(ed.getPath(), '.') == 1) {
				for(String m : this.getMapping("http://hl7.org/fhir/terminology-pattern", logical, ed)) {
					String name = this.tail(ed.getPath());
					List<Element> list = new ArrayList();
					element.getNamedChildren(name, list);
					if (!list.isEmpty()) {
						if ("Coding.code".equals(m)) {
							res.setCode(((Element)list.get(0)).primitiveValue());
						} else if ("Coding.system[fmt:OID]".equals(m)) {
							String oid = ((Element)list.get(0)).primitiveValue();
							String url = new ContextUtilities(this.context).oid2Uri(oid);
							if (url != null) {
								res.setSystem(url);
							} else {
								res.setSystem("urn:oid:" + oid);
							}
						} else if ("Coding.version".equals(m)) {
							res.setVersion(((Element)list.get(0)).primitiveValue());
						} else if ("Coding.display".equals(m)) {
							res.setDisplay(((Element)list.get(0)).primitiveValue());
						}
					}
				}
			}
		}

		return res;
	}

	private void checkMaxValueSet(
		List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, String maxVSUrl, CodeableConcept cc, NodeStack stack
	) {
		ValueSet valueset = this.resolveBindingReference(profile, maxVSUrl, profile.getUrl(), profile);
		if (valueset == null) {
			CodeSystem cs = this.context.fetchCodeSystem(maxVSUrl);
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.CODEINVALID,
				element.line(),
				element.col(),
				path,
				cs == null,
				"Terminology_TX_ValueSet_NotFound_CS",
				new Object[]{this.describeReference(maxVSUrl)}
			)) {
				this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					element.line(),
					element.col(),
					path,
					valueset != null,
					"Terminology_TX_ValueSet_NotFound",
					new Object[]{this.describeReference(maxVSUrl)}
				);
			}
		} else {
			try {
				long t = System.nanoTime();
				ValidationResult vr = this.checkCodeOnServer(stack, valueset, cc, false);
				if (vr != null && vr.isOk()) {
					for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
						this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
					}
				}

				this.timeTracker.tx(t, "vc " + cc.toString());
				if (!vr.isOk()) {
					if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure()) {
						this.txWarning(
							errors,
							NO_RULE_DATE,
							vr.getTxLink(),
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							false,
							"Terminology_TX_NoValid_7",
							new Object[]{this.describeReference(maxVSUrl, valueset), vr.getMessage()}
						);
					} else {
						this.txRule(
							errors,
							NO_RULE_DATE,
							vr.getTxLink(),
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							false,
							"Terminology_TX_NoValid_8",
							new Object[]{this.describeReference(maxVSUrl, valueset), this.ccSummary(cc)}
						);
					}
				}
			} catch (Exception var14) {
				this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					element.line(),
					element.col(),
					path,
					false,
					"Terminology_TX_Error_CodeableConcept_Max",
					new Object[]{var14.getMessage()}
				);
			}
		}
	}

	private boolean checkMaxValueSet(
		List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, String maxVSUrl, Coding c, NodeStack stack
	) {
		boolean ok = true;
		ValueSet valueset = this.resolveBindingReference(profile, maxVSUrl, profile.getUrl(), profile);
		if (valueset == null) {
			CodeSystem cs = this.context.fetchCodeSystem(maxVSUrl);
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.CODEINVALID,
				element.line(),
				element.col(),
				path,
				cs == null,
				"Terminology_TX_ValueSet_NotFound_CS",
				new Object[]{this.describeReference(maxVSUrl)}
			)) {
				this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					element.line(),
					element.col(),
					path,
					valueset != null,
					"Terminology_TX_ValueSet_NotFound",
					new Object[]{this.describeReference(maxVSUrl)}
				);
			} else {
				ok = false;
			}
		} else {
			try {
				long t = System.nanoTime();
				ValidationResult vr = this.checkCodeOnServer(stack, valueset, c, true);
				if (vr != null && vr.isOk()) {
					for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
						this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
					}
				}

				this.timeTracker.tx(t, "vc " + c.getSystem() + "#" + c.getCode() + " '" + c.getDisplay() + "'");
				if (!vr.isOk()) {
					if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure()) {
						this.txWarning(
							errors,
							NO_RULE_DATE,
							vr.getTxLink(),
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							false,
							"Terminology_TX_NoValid_9",
							new Object[]{this.describeReference(maxVSUrl, valueset), vr.getMessage(), c.getSystem() + "#" + c.getCode()}
						);
					} else {
						ok = this.txRule(
							errors,
							NO_RULE_DATE,
							vr.getTxLink(),
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							false,
							"Terminology_TX_NoValid_10",
							new Object[]{this.describeReference(maxVSUrl, valueset), c.getSystem(), c.getCode()}
						)
							&& ok;
					}
				}
			} catch (Exception var15) {
				this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					element.line(),
					element.col(),
					path,
					false,
					"Terminology_TX_Error_CodeableConcept_Max",
					new Object[]{var15.getMessage()}
				);
			}
		}

		return ok;
	}

	private boolean checkMaxValueSet(
		List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, String maxVSUrl, String value, NodeStack stack
	) {
		boolean ok = true;
		ValueSet valueset = this.resolveBindingReference(profile, maxVSUrl, profile.getUrl(), profile);
		if (valueset == null) {
			CodeSystem cs = this.context.fetchCodeSystem(maxVSUrl);
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.CODEINVALID,
				element.line(),
				element.col(),
				path,
				cs == null,
				"Terminology_TX_ValueSet_NotFound_CS",
				new Object[]{this.describeReference(maxVSUrl)}
			)) {
				this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					element.line(),
					element.col(),
					path,
					valueset != null,
					"Terminology_TX_ValueSet_NotFound",
					new Object[]{this.describeReference(maxVSUrl)}
				);
			} else {
				ok = false;
			}
		} else {
			try {
				long t = System.nanoTime();
				ValidationResult vr = this.checkCodeOnServer(stack, valueset, value, this.baseOptions.withLanguage(stack.getWorkingLang()));
				if (vr != null && vr.isOk()) {
					for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
						this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
					}
				}

				this.timeTracker.tx(t, "vc " + value);
				if (!vr.isOk()) {
					if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure()) {
						this.txWarning(
							errors,
							NO_RULE_DATE,
							vr.getTxLink(),
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							false,
							"Terminology_TX_NoValid_9",
							new Object[]{this.describeReference(maxVSUrl, valueset), vr.getMessage(), value}
						);
					} else {
						ok = this.txRule(
							errors,
							NO_RULE_DATE,
							vr.getTxLink(),
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							false,
							"Terminology_TX_NoValid_11",
							new Object[]{this.describeReference(maxVSUrl, valueset), vr.getMessage()}
						)
							&& ok;
					}
				}
			} catch (Exception var15) {
				this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					element.line(),
					element.col(),
					path,
					false,
					"Terminology_TX_Error_CodeableConcept_Max",
					new Object[]{var15.getMessage()}
				);
			}
		}

		return ok;
	}

	private String ccSummary(CodeableConcept cc) {
		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

		for(Coding c : cc.getCoding()) {
			b.append(c.getSystem() + "#" + c.getCode());
		}

		return b.toString();
	}

	private boolean checkCoding(List<ValidationMessage> errors, String path, Element focus, Coding fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".system", focus.getNamedChild("system"), fixed.getSystemElement(), fixedSource, "system", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".version", focus.getNamedChild("version"), fixed.getVersionElement(), fixedSource, "version", focus, pattern)
			&& ok;
		ok = this.checkFixedValue(errors, path + ".code", focus.getNamedChild("code"), fixed.getCodeElement(), fixedSource, "code", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".display", focus.getNamedChild("display"), fixed.getDisplayElement(), fixedSource, "display", focus, pattern)
			&& ok;
		return this.checkFixedValue(
			errors, path + ".userSelected", focus.getNamedChild("userSelected"), fixed.getUserSelectedElement(), fixedSource, "userSelected", focus, pattern
		)
			&& ok;
	}

	private boolean checkCoding(
		List<ValidationMessage> errors,
		String path,
		Element element,
		StructureDefinition profile,
		ElementDefinition theElementCntext,
		boolean inCodeableConcept,
		boolean checkDisplay,
		NodeStack stack
	) {
		String code = element.getNamedChildValue("code");
		String system = element.getNamedChildValue("system");
		String version = element.getNamedChildValue("version");
		String display = element.getNamedChildValue("display");
		return this.checkCodedElement(errors, path, element, profile, theElementCntext, inCodeableConcept, checkDisplay, stack, code, system, version, display);
	}

	private boolean checkCodedElement(
		List<ValidationMessage> errors,
		String path,
		Element element,
		StructureDefinition profile,
		ElementDefinition theElementCntext,
		boolean inCodeableConcept,
		boolean checkDisplay,
		NodeStack stack,
		String theCode,
		String theSystem,
		String theVersion,
		String theDisplay
	) {
		boolean ok = true;
		ok = this.rule(
			errors,
			NO_RULE_DATE,
			IssueType.CODEINVALID,
			element.line(),
			element.col(),
			path,
			theSystem == null || this.isCodeSystemReferenceValid(theSystem),
			"Terminology_TX_System_Relative",
			new Object[0]
		)
			&& ok;
		this.warning(
			errors,
			NO_RULE_DATE,
			IssueType.CODEINVALID,
			element.line(),
			element.col(),
			path,
			Utilities.noString(theCode) || !Utilities.noString(theSystem),
			"TERMINOLOGY_TX_SYSTEM_NO_CODE",
			new Object[0]
		);
		if (theSystem != null && theCode != null && !this.noTerminologyChecks) {
			ok = this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.CODEINVALID,
				element.line(),
				element.col(),
				path,
				!this.isValueSet(theSystem),
				"Terminology_TX_System_ValueSet2",
				new Object[]{theSystem}
			)
				&& ok;

			try {
				if (this.checkCode(errors, element, path, theCode, theSystem, theVersion, theDisplay, checkDisplay, stack)) {
					if (theElementCntext != null && theElementCntext.hasBinding()) {
						ElementDefinitionBindingComponent binding = theElementCntext.getBinding();
						if (this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							binding != null,
							"Terminology_TX_Binding_Missing2",
							new Object[]{path}
						)) {
							if (binding.hasValueSet()) {
								ValueSet valueset = this.resolveBindingReference(profile, binding.getValueSet(), profile.getUrl(), profile);
								if (valueset == null) {
									CodeSystem cs = this.context.fetchCodeSystem(binding.getValueSet());
									if (this.rule(
										errors,
										NO_RULE_DATE,
										IssueType.CODEINVALID,
										element.line(),
										element.col(),
										path,
										cs == null,
										"Terminology_TX_ValueSet_NotFound_CS",
										new Object[]{this.describeReference(binding.getValueSet())}
									)) {
										this.warning(
											errors,
											NO_RULE_DATE,
											IssueType.CODEINVALID,
											element.line(),
											element.col(),
											path,
											valueset != null,
											"Terminology_TX_ValueSet_NotFound",
											new Object[]{this.describeReference(binding.getValueSet())}
										);
									} else {
										ok = false;
									}
								} else {
									try {
										Coding c = ObjectConverter.readAsCoding(element);
										long t = System.nanoTime();
										ValidationResult vr = null;
										if (binding.getStrength() != BindingStrength.EXAMPLE) {
											vr = this.checkCodeOnServer(stack, valueset, c, true);
										}

										if (vr != null && vr.isOk()) {
											for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
												this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
											}
										}

										this.timeTracker.tx(t, "vc " + c.getSystem() + "#" + c.getCode() + " '" + c.getDisplay() + "'");
										if (binding.getStrength() == BindingStrength.REQUIRED) {
											this.removeTrackedMessagesForLocation(errors, element, path);
										}

										if (vr != null && !vr.isOk()) {
											if (vr.IsNoService()) {
												this.txHint(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_Binding_NoServer",
													new Object[]{theSystem + "#" + theCode}
												);
											} else if (vr.getErrorClass() != null && !vr.getErrorClass().isInfrastructure()) {
												if (binding.getStrength() == BindingStrength.REQUIRED) {
													ok = this.txRule(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_Confirm_4a",
														new Object[]{this.describeReference(binding.getValueSet(), valueset), vr.getMessage(), theSystem + "#" + theCode}
													)
														&& ok;
												} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
													if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
														this.checkMaxValueSet(
															errors,
															path,
															element,
															profile,
															ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"),
															c,
															stack
														);
													} else if (!this.noExtensibleWarnings) {
														this.txWarningForLaterRemoval(
															element,
															errors,
															NO_RULE_DATE,
															vr.getTxLink(),
															IssueType.CODEINVALID,
															element.line(),
															element.col(),
															path,
															false,
															"Terminology_TX_Confirm_5",
															new Object[]{this.describeReference(binding.getValueSet(), valueset)}
														);
													}
												} else if (binding.getStrength() == BindingStrength.PREFERRED && this.baseOnly) {
													this.txHint(
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_Confirm_6",
														new Object[]{this.describeReference(binding.getValueSet(), valueset)}
													);
												}
											} else if (binding.getStrength() == BindingStrength.REQUIRED) {
												ok = this.txRule(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_NoValid_12",
													new Object[]{
														this.describeReference(binding.getValueSet(), valueset), this.getErrorMessage(vr.getMessage()), theSystem + "#" + theCode
													}
												)
													&& ok;
											} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
												if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
													ok = this.checkMaxValueSet(
														errors,
														path,
														element,
														profile,
														ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"),
														c,
														stack
													)
														&& ok;
												} else if (!this.noExtensibleWarnings) {
													this.txWarningForLaterRemoval(
														element,
														errors,
														NO_RULE_DATE,
														vr.getTxLink(),
														IssueType.CODEINVALID,
														element.line(),
														element.col(),
														path,
														false,
														"Terminology_TX_NoValid_13",
														new Object[]{
															this.describeReference(binding.getValueSet(), valueset), this.getErrorMessage(vr.getMessage()), c.getSystem() + "#" + c.getCode()
														}
													);
												}
											} else if (binding.getStrength() == BindingStrength.PREFERRED && this.baseOnly) {
												this.txHint(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													"Terminology_TX_NoValid_14",
													new Object[]{
														this.describeReference(binding.getValueSet(), valueset), this.getErrorMessage(vr.getMessage()), theSystem + "#" + theCode
													}
												);
											}
										} else if (vr != null && vr.getMessage() != null) {
											if (vr.getSeverity() == IssueSeverity.INFORMATION) {
												this.txHint(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													vr.getMessage(),
													new Object[0]
												);
											} else {
												this.txWarning(
													errors,
													NO_RULE_DATE,
													vr.getTxLink(),
													IssueType.CODEINVALID,
													element.line(),
													element.col(),
													path,
													false,
													vr.getMessage(),
													new Object[0]
												);
											}
										}
									} catch (Exception var22) {
										this.warning(
											errors,
											NO_RULE_DATE,
											IssueType.CODEINVALID,
											element.line(),
											element.col(),
											path,
											false,
											"Terminology_TX_Error_Coding1",
											new Object[]{var22.getMessage()}
										);
									}
								}
							} else if (binding.hasValueSet()) {
								this.hint(errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, false, "Terminology_TX_Binding_CantCheck");
							} else if (!inCodeableConcept && !this.noBindingMsgSuppressed) {
								this.hint(
									errors,
									NO_RULE_DATE,
									IssueType.CODEINVALID,
									element.line(),
									element.col(),
									path,
									false,
									"Terminology_TX_Binding_NoSource",
									new Object[]{path}
								);
							}
						}
					}
				} else {
					ok = false;
				}
			} catch (Exception var23) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					element.line(),
					element.col(),
					path,
					false,
					"Terminology_TX_Error_Coding2",
					new Object[]{var23.getMessage(), var23.toString()}
				);
				ok = false;
			}
		}

		return ok;
	}

	private boolean isValueSet(String url) {
		try {
			ValueSet vs = (ValueSet)this.context.fetchResourceWithException(ValueSet.class, url);
			return vs != null;
		} catch (Exception var3) {
			return false;
		}
	}

	private boolean checkContactPoint(List<ValidationMessage> errors, String path, Element focus, ContactPoint fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".system", focus.getNamedChild("system"), fixed.getSystemElement(), fixedSource, "system", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".value", focus.getNamedChild("value"), fixed.getValueElement(), fixedSource, "value", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".use", focus.getNamedChild("use"), fixed.getUseElement(), fixedSource, "use", focus, pattern) && ok;
		return this.checkFixedValue(errors, path + ".period", focus.getNamedChild("period"), fixed.getPeriod(), fixedSource, "period", focus, pattern) && ok;
	}

	private StructureDefinition checkExtension(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		String path,
		Element resource,
		Element container,
		Element element,
		ElementDefinition def,
		StructureDefinition profile,
		NodeStack stack,
		NodeStack containerStack,
		String extensionUrl,
		PercentageTracker pct,
		ValidationMode mode
	) throws FHIRException {
		String url = element.getNamedChildValue("url");
		String u = url.contains("|") ? url.substring(0, url.indexOf("|")) : url;
		boolean isModifier = element.getName().equals("modifierExtension");

		assert def.getIsModifier() == isModifier;

		long t = System.nanoTime();
		StructureDefinition ex = Utilities.isAbsoluteUrl(u) ? (StructureDefinition)this.context.fetchResource(StructureDefinition.class, u) : null;
		if (ex == null) {
			ex = this.getXverExt(errors, path, element, url);
		}

		if (url.contains("|")) {
			if (ex == null) {
				ex = Utilities.isAbsoluteUrl(url) ? (StructureDefinition)this.context.fetchResource(StructureDefinition.class, url) : null;
				if (ex == null) {
					this.warning(
						errors, "2022-12-17", IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, "EXT_VER_URL_NO_MATCH", new Object[0]
					);
				} else {
					this.rule(
						errors, "2022-12-17", IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, "EXT_VER_URL_IGNORE", new Object[0]
					);
				}
			} else if (url.equals(ex.getUrl())) {
				this.warning(
					errors, NO_RULE_DATE, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, "EXT_VER_URL_MISLEADING", new Object[0]
				);
			} else if (url.equals(ex.getVersionedUrl())) {
				this.rule(
					errors, NO_RULE_DATE, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, "EXT_VER_URL_NOT_ALLOWED", new Object[0]
				);
			} else {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path + "[url='" + url + "']",
					false,
					"EXT_VER_URL_REVERSION",
					new Object[]{ex.getVersion()}
				);
			}
		}

		this.timeTracker.sd(t);
		if (ex == null) {
			if (extensionUrl != null && !this.isAbsolute(url)) {
				if (extensionUrl.equals(profile.getUrl())) {
					this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						element.line(),
						element.col(),
						path + "[url='" + url + "']",
						this.hasExtensionSlice(profile, url),
						"Extension_EXT_SubExtension_Invalid",
						new Object[]{url, profile.getVersionedUrl()}
					);
				}
			} else if (SpecialExtensions.isKnownExtension(url)) {
				ex = SpecialExtensions.getDefinition(url);
			} else if (!BuildExtensions.allConsts().contains(url)
				&& this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.STRUCTURE,
				element.line(),
				element.col(),
				path,
				this.allowUnknownExtension(url),
				"Extension_EXT_Unknown_NotHere",
				new Object[]{url}
			)) {
				this.hint(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					this.isKnownExtension(url),
					"Extension_EXT_Unknown",
					new Object[]{url}
				);
			}
		}

		if (ex != null) {
			this.trackUsage(ex, hostContext, element);
			if (isModifier) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path + "[url='" + url + "']",
					def.getIsModifier() == isModifier,
					"Extension_EXT_Modifier_MismatchY",
					new Object[0]
				);
			} else {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path + "[url='" + url + "']",
					def.getIsModifier() == isModifier,
					"Extension_EXT_Modifier_MismatchN",
					new Object[0]
				);
			}

			this.checkExtensionContext(hostContext.getAppContext(), errors, resource, container, ex, containerStack, hostContext, isModifier);
			this.checkDefinitionStatus(errors, element, path, ex, profile, this.context.formatMessage("MSG_DEPENDS_ON_EXTENSION", new Object[0]));
			if (isModifier) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path + "[url='" + url + "']",
					((ElementDefinition)ex.getSnapshot().getElement().get(0)).getIsModifier(),
					"Extension_EXT_Modifier_Y",
					new Object[]{url}
				);
			} else {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path + "[url='" + url + "']",
					!((ElementDefinition)ex.getSnapshot().getElement().get(0)).getIsModifier(),
					"Extension_EXT_Modifier_N",
					new Object[]{url}
				);
			}

			Set<String> allowedTypes = this.listExtensionTypes(ex);
			String actualType = this.getExtensionType(element);
			if (actualType != null) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					allowedTypes.contains(actualType),
					"Extension_EXT_Type",
					new Object[]{url, allowedTypes.toString(), actualType}
				);
			} else if (element.hasChildren("extension")) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					allowedTypes.isEmpty(),
					"Extension_EXT_Simple_WRONG",
					new Object[]{url}
				);
			} else {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					allowedTypes.isEmpty(),
					"Extension_EXT_Simple_ABSENT",
					new Object[]{url}
				);
			}

			this.validateElement(
				hostContext,
				errors,
				ex,
				(ElementDefinition)ex.getSnapshot().getElement().get(0),
				null,
				null,
				resource,
				element,
				"Extension",
				stack,
				false,
				true,
				url,
				pct,
				mode
			);
		}

		return ex;
	}

	private boolean hasExtensionSlice(StructureDefinition profile, String sliceName) {
		for(ElementDefinition ed : profile.getSnapshot().getElement()) {
			if (ed.getPath().equals("Extension.extension.url") && ed.hasFixed() && sliceName.equals(ed.getFixed().primitiveValue())) {
				return true;
			}
		}

		return false;
	}

	private String getExtensionType(Element element) {
		for(Element e : element.getChildren()) {
			if (e.getName().startsWith("value")) {
				String tn = e.getName().substring(5);
				String ltn = Utilities.uncapitalize(tn);
				if (this.isPrimitiveType(ltn)) {
					return ltn;
				}

				return tn;
			}
		}

		return null;
	}

	private Set<String> listExtensionTypes(StructureDefinition ex) {
		ElementDefinition vd = null;

		for(ElementDefinition ed : ex.getSnapshot().getElement()) {
			if (ed.getPath().startsWith("Extension.value")) {
				vd = ed;
				break;
			}
		}

		Set<String> res = new HashSet();
		if (vd != null && !"0".equals(vd.getMax())) {
			for(TypeRefComponent tr : vd.getType()) {
				res.add(tr.getWorkingCode());
			}
		}

		if (ex.getUrl().equals("http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type")) {
			res.add("uri");
			res.add("url");
		}

		return res;
	}

	private boolean checkExtensionContext(
		Object appContext,
		List<ValidationMessage> errors,
		Element resource,
		Element container,
		StructureDefinition definition,
		NodeStack stack,
		ValidatorHostContext hostContext,
		boolean modifier
	) {
		String extUrl = definition.getUrl();
		boolean ok = false;
		CommaSeparatedStringBuilder contexts = new CommaSeparatedStringBuilder();
		List<String> plist = new ArrayList();
		plist.add(this.stripIndexes(stack.getLiteralPath()));

		for(String s : stack.getLogicalPaths()) {
			String p = this.stripIndexes(s);
			if (EXTENSION_CONTEXT_LIST.contains(p)) {
				return true;
			}

			plist.add(p);
		}

		Collections.sort(plist);

		for(StructureDefinitionContextComponent ctxt : this.fixContexts(extUrl, definition.getContext())) {
			if (ok) {
				break;
			}

			if (ctxt.getType() == ExtensionContextType.ELEMENT) {
				String en = ctxt.getExpression();
				contexts.append("e:" + en);
				String pu = null;
				if (en.contains("#")) {
					pu = en.substring(0, en.indexOf("#"));
					en = en.substring(en.indexOf("#") + 1);
				}

				if (Utilities.existsInList(en, new String[]{"Element", "Any"})) {
					ok = true;
				} else if (en.equals("Resource") && container.isResource()) {
					ok = true;
				} else if (en.equals("CanonicalResource") && VersionUtilities.getCanonicalResourceNames(this.context.getVersion()).contains(stack.getLiteralPath())) {
					ok = true;
				} else if (plist.contains(en) && pu == null) {
					ok = true;
				}

				if (!ok && this.checkConformsToProfile(appContext, errors, resource, container, stack, extUrl, ctxt.getExpression(), pu)) {
					for(String p : plist) {
						if (ok) {
							break;
						}

						if (p.equals(en)) {
							ok = true;
						} else {
							String pn = p;
							String pt = "";
							if (p.contains(".")) {
								pn = p.substring(0, p.indexOf("."));
								pt = p.substring(p.indexOf("."));
							}

							StructureDefinition sd = this.context.fetchTypeDefinition(pn);

							while(sd != null) {
								if ((sd.getType() + pt).equals(en)) {
									ok = true;
									break;
								}

								if (sd.getBaseDefinition() != null) {
									sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, sd.getBaseDefinition(), sd);
								} else {
									sd = null;
								}
							}
						}
					}
				}
			} else if (ctxt.getType() == ExtensionContextType.EXTENSION) {
				contexts.append("x:" + ctxt.getExpression());
				String ext = null;
				if (stack.getElement().getName().startsWith("value")) {
					NodeStack estack = stack.getParent();
					if (estack != null && estack.getElement().fhirType().equals("Extension")) {
						ext = estack.getElement().getNamedChildValue("url");
					}
				} else {
					ext = stack.getElement().getNamedChildValue("url");
				}

				if (ctxt.getExpression().equals(ext)) {
					ok = true;
				} else if (ext != null) {
					plist.add(ext);
				}
			} else {
				if (ctxt.getType() != ExtensionContextType.FHIRPATH) {
					throw new Error(this.context.formatMessage("Unrecognised_extension_context_", new Object[]{ctxt.getTypeElement().asStringValue()}));
				}

				contexts.append("p:" + ctxt.getExpression());
				List<Base> res = this.fpe.evaluate(hostContext, resource, hostContext.getRootResource(), resource, this.fpe.parse(ctxt.getExpression()));
				if (res.contains(container)) {
					ok = true;
				}
			}
		}

		if (!ok) {
			if (definition.hasUserData("XVER_EXT_MARKER")) {
				this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					container.line(),
					container.col(),
					stack.getLiteralPath(),
					false,
					modifier ? "EXTENSION_EXTM_CONTEXT_WRONG_XVER" : "EXTENSION_EXTP_CONTEXT_WRONG_XVER",
					new Object[]{extUrl, contexts.toString(), plist.toString()}
				);
			} else {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					container.line(),
					container.col(),
					stack.getLiteralPath(),
					false,
					modifier ? "Extension_EXTM_Context_Wrong" : "Extension_EXTP_Context_Wrong",
					new Object[]{extUrl, contexts.toString(), plist.toString()}
				);
			}

			return false;
		} else {
			if (definition.hasContextInvariant()) {
				for(StringType s : definition.getContextInvariant()) {
					if (!this.fpe.evaluateToBoolean(hostContext, resource, hostContext.getRootResource(), container, this.fpe.parse((String)s.getValue()))) {
						if (definition.hasUserData("XVER_EXT_MARKER")) {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.STRUCTURE,
								container.line(),
								container.col(),
								stack.getLiteralPath(),
								false,
								"Profile_EXT_Not_Here",
								new Object[]{extUrl, s.getValue()}
							);
							return true;
						}

						this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							container.line(),
							container.col(),
							stack.getLiteralPath(),
							false,
							"Profile_EXT_Not_Here",
							new Object[]{extUrl, s.getValue()}
						);
						return false;
					}
				}
			}

			return true;
		}
	}

	private boolean checkConformsToProfile(
		Object appContext, List<ValidationMessage> errors, Element resource, Element container, NodeStack stack, String extUrl, String expression, String pu
	) {
		if (pu == null) {
			return true;
		} else if (pu.equals("http://hl7.org/fhir/StructureDefinition/" + resource.fhirType())) {
			return true;
		} else {
			StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, pu);
			if (!this.rule(
				errors,
				"2023-07-03",
				IssueType.UNKNOWN,
				container.line(),
				container.col(),
				stack.getLiteralPath(),
				sd != null,
				"EXTENSION_CONTEXT_UNABLE_TO_FIND_PROFILE",
				new Object[]{extUrl, expression}
			)) {
				return false;
			} else if (!sd.getType().equals(resource.fhirType())) {
				this.warning(
					errors,
					"2023-07-03",
					IssueType.UNKNOWN,
					container.line(),
					container.col(),
					stack.getLiteralPath(),
					false,
					"EXTENSION_CONTEXT_UNABLE_TO_CHECK_PROFILE",
					new Object[]{extUrl, expression, pu}
				);
				return true;
			} else {
				List<ValidationMessage> valerrors = new ArrayList();
				ValidationMode mode = new ValidationMode(ValidationReason.Expression, ProfileSource.FromExpression);
				this.validateResource(
					new ValidatorHostContext(appContext, resource),
					valerrors,
					resource,
					resource,
					sd,
					IdStatus.OPTIONAL,
					new NodeStack(this.context, null, resource, this.validationLanguage),
					null,
					mode
				);
				boolean ok = true;
				List<ValidationMessage> record = new ArrayList();

				for(ValidationMessage v : valerrors) {
					ok = ok && !v.getLevel().isError();
					if (v.getLevel().isError() || v.isSlicingHint()) {
						record.add(v);
					}
				}

				return ok;
			}
		}
	}

	private List<StructureDefinitionContextComponent> fixContexts(String extUrl, List<StructureDefinitionContextComponent> list) {
		List<StructureDefinitionContextComponent> res = new ArrayList();

		for(StructureDefinitionContextComponent ctxt : list) {
			res.add(ctxt.copy());
		}

		if ("http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type".equals(extUrl)) {
			((StructureDefinitionContextComponent)list.get(0)).setExpression("ElementDefinition.type");
		}

		if ("http://hl7.org/fhir/StructureDefinition/regex".equals(extUrl)) {
			StructureDefinitionContextComponent e = new StructureDefinitionContextComponent();
			e.setExpression("ElementDefinition.type");
			e.setType(ExtensionContextType.ELEMENT);
			list.add(e);
		}

		if ("http://hl7.org/fhir/StructureDefinition/structuredefinition-normative-version".equals(extUrl)) {
			((StructureDefinitionContextComponent)list.get(0)).setExpression("Element");
		}

		if (!VersionUtilities.isThisOrLater("4.6", this.context.getVersion())
			&& Utilities.existsInList(
			extUrl,
			new String[]{
				"http://hl7.org/fhir/StructureDefinition/capabilitystatement-expectation", "http://hl7.org/fhir/StructureDefinition/capabilitystatement-prohibited"
			}
		)) {
			((StructureDefinitionContextComponent)list.get(0)).setExpression("Element");
		}

		return list;
	}

	private String stripIndexes(String path) {
		boolean skip = false;
		StringBuilder b = new StringBuilder();

		for(char c : path.toCharArray()) {
			if (skip) {
				if (c == ']') {
					skip = false;
				}
			} else if (c == '[') {
				skip = true;
			} else {
				b.append(c);
			}
		}

		return b.toString();
	}

	private boolean checkFixedValue(
		List<ValidationMessage> errors,
		String path,
		Element focus,
		org.hl7.fhir.r5.model.Element fixed,
		String fixedSource,
		String propName,
		Element parent,
		boolean pattern
	) {
		boolean ok = true;
		if (fixed != null && !fixed.isEmpty() || focus != null) {
			if ((fixed == null || fixed.isEmpty()) && focus != null) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.VALUE,
					focus.line(),
					focus.col(),
					path,
					pattern,
					"Profile_VAL_NotAllowed",
					new Object[]{focus.getName(), pattern ? "pattern" : "fixed value"}
				);
			} else if (fixed != null && !fixed.isEmpty() && focus == null) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.VALUE,
					parent == null ? -1 : parent.line(),
					parent == null ? -1 : parent.col(),
					path,
					false,
					"Profile_VAL_MissingElement",
					new Object[]{propName, fixedSource}
				);
			} else {
				String value = focus.primitiveValue();
				if (fixed instanceof BooleanType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check(((BooleanType)fixed).asStringValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((BooleanType)fixed).asStringValue()}
					);
				} else if (fixed instanceof IntegerType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check(((IntegerType)fixed).asStringValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((IntegerType)fixed).asStringValue()}
					);
				} else if (fixed instanceof DecimalType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check(((DecimalType)fixed).asStringValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((DecimalType)fixed).asStringValue()}
					);
				} else if (fixed instanceof Base64BinaryType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check(((Base64BinaryType)fixed).asStringValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((Base64BinaryType)fixed).asStringValue()}
					);
				} else if (fixed instanceof InstantType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check(((Date)((InstantType)fixed).getValue()).toString(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((InstantType)fixed).asStringValue()}
					);
				} else if (fixed instanceof CodeType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check((String)((CodeType)fixed).getValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((CodeType)fixed).getValue()}
					);
				} else if (fixed instanceof Enumeration) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check(((Enumeration)fixed).asStringValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((Enumeration)fixed).asStringValue()}
					);
				} else if (fixed instanceof StringType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check((String)((StringType)fixed).getValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((StringType)fixed).getValue()}
					);
				} else if (fixed instanceof UriType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check((String)((UriType)fixed).getValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((UriType)fixed).getValue()}
					);
				} else if (fixed instanceof DateType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check(((Date)((DateType)fixed).getValue()).toString(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((DateType)fixed).getValue()}
					);
				} else if (fixed instanceof DateTimeType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check(((Date)((DateTimeType)fixed).getValue()).toString(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((DateTimeType)fixed).getValue()}
					);
				} else if (fixed instanceof OidType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check((String)((OidType)fixed).getValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((OidType)fixed).getValue()}
					);
				} else if (fixed instanceof UuidType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check((String)((UuidType)fixed).getValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((UuidType)fixed).getValue()}
					);
				} else if (fixed instanceof IdType) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						this.check(((IdType)fixed).getValue(), value),
						"_DT_Fixed_Wrong",
						new Object[]{value, ((IdType)fixed).getValue()}
					);
				} else if (fixed instanceof Quantity) {
					this.checkQuantity(errors, path, focus, (Quantity)fixed, fixedSource, pattern);
				} else if (fixed instanceof Address) {
					ok = this.checkAddress(errors, path, focus, (Address)fixed, fixedSource, pattern);
				} else if (fixed instanceof ContactPoint) {
					ok = this.checkContactPoint(errors, path, focus, (ContactPoint)fixed, fixedSource, pattern);
				} else if (fixed instanceof Attachment) {
					ok = this.checkAttachment(errors, path, focus, (Attachment)fixed, fixedSource, pattern);
				} else if (fixed instanceof Identifier) {
					ok = this.checkIdentifier(errors, path, focus, (Identifier)fixed, fixedSource, pattern);
				} else if (fixed instanceof Coding) {
					ok = this.checkCoding(errors, path, focus, (Coding)fixed, fixedSource, pattern);
				} else if (fixed instanceof HumanName) {
					ok = this.checkHumanName(errors, path, focus, (HumanName)fixed, fixedSource, pattern);
				} else if (fixed instanceof CodeableConcept) {
					ok = this.checkCodeableConcept(errors, path, focus, (CodeableConcept)fixed, fixedSource, pattern);
				} else if (fixed instanceof Timing) {
					ok = this.checkTiming(errors, path, focus, (Timing)fixed, fixedSource, pattern);
				} else if (fixed instanceof Period) {
					ok = this.checkPeriod(errors, path, focus, (Period)fixed, fixedSource, pattern);
				} else if (fixed instanceof Range) {
					ok = this.checkRange(errors, path, focus, (Range)fixed, fixedSource, pattern);
				} else if (fixed instanceof Ratio) {
					ok = this.checkRatio(errors, path, focus, (Ratio)fixed, fixedSource, pattern);
				} else if (fixed instanceof SampledData) {
					ok = this.checkSampledData(errors, path, focus, (SampledData)fixed, fixedSource, pattern);
				} else if (fixed instanceof Reference) {
					ok = this.checkReference(errors, path, focus, (Reference)fixed, fixedSource, pattern);
				} else {
					ok = this.rule(
						errors, NO_RULE_DATE, IssueType.EXCEPTION, focus.line(), focus.col(), path, false, "Internal_INT_Bad_Type", new Object[]{fixed.fhirType()}
					);
				}

				List<Element> extensions = new ArrayList();
				focus.getNamedChildren("extension", extensions);
				if (fixed.getExtension().size() == 0) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.VALUE,
						focus.line(),
						focus.col(),
						path,
						extensions.size() == 0 || pattern,
						"Extension_EXT_Fixed_Banned",
						new Object[0]
					)
						&& ok;
				} else if (this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.VALUE,
					focus.line(),
					focus.col(),
					path,
					extensions.size() == fixed.getExtension().size(),
					"Extension_EXT_Count_Mismatch",
					new Object[]{Integer.toString(fixed.getExtension().size()), Integer.toString(extensions.size())}
				)) {
					for(Extension e : fixed.getExtension()) {
						Element ex = this.getExtensionByUrl(extensions, e.getUrl());
						if (this.rule(
							errors, NO_RULE_DATE, IssueType.VALUE, focus.line(), focus.col(), path, ex != null, "Extension_EXT_Count_NotFound", new Object[]{e.getUrl()}
						)) {
							ok = this.checkFixedValue(
								errors,
								path,
								ex.getNamedChild("extension").getNamedChild("value"),
								e.getValue(),
								fixedSource,
								"extension.value",
								ex.getNamedChild("extension"),
								false
							)
								&& ok;
						}
					}
				}
			}
		}

		return ok;
	}

	private boolean checkHumanName(List<ValidationMessage> errors, String path, Element focus, HumanName fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".use", focus.getNamedChild("use"), fixed.getUseElement(), fixedSource, "use", focus, pattern);
		ok = this.checkFixedValue(errors, path + ".text", focus.getNamedChild("text"), fixed.getTextElement(), fixedSource, "text", focus, pattern);
		ok = this.checkFixedValue(errors, path + ".period", focus.getNamedChild("period"), fixed.getPeriod(), fixedSource, "period", focus, pattern);
		List<Element> parts = new ArrayList();
		if (!pattern || fixed.hasFamily()) {
			focus.getNamedChildren("family", parts);
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.VALUE,
				focus.line(),
				focus.col(),
				path,
				parts.size() > 0 == fixed.hasFamily(),
				"Fixed_Type_Checks_DT_Name_Family",
				new Object[]{fixed.hasFamily() ? "1" : "0", Integer.toString(parts.size())}
			)) {
				for(int i = 0; i < parts.size(); ++i) {
					ok = this.checkFixedValue(errors, path + ".family", (Element)parts.get(i), fixed.getFamilyElement(), fixedSource, "family", focus, pattern) && ok;
				}
			}
		}

		if (!pattern || fixed.hasGiven()) {
			focus.getNamedChildren("given", parts);
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.VALUE,
				focus.line(),
				focus.col(),
				path,
				parts.size() == fixed.getGiven().size(),
				"Fixed_Type_Checks_DT_Name_Given",
				new Object[]{Integer.toString(fixed.getGiven().size()), Integer.toString(parts.size())}
			)) {
				for(int i = 0; i < parts.size(); ++i) {
					ok = this.checkFixedValue(
						errors, path + ".given", (Element)parts.get(i), (org.hl7.fhir.r5.model.Element)fixed.getGiven().get(i), fixedSource, "given", focus, pattern
					)
						&& ok;
				}
			}
		}

		if (!pattern || fixed.hasPrefix()) {
			focus.getNamedChildren("prefix", parts);
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.VALUE,
				focus.line(),
				focus.col(),
				path,
				parts.size() == fixed.getPrefix().size(),
				"Fixed_Type_Checks_DT_Name_Prefix",
				new Object[]{Integer.toString(fixed.getPrefix().size()), Integer.toString(parts.size())}
			)) {
				for(int i = 0; i < parts.size(); ++i) {
					ok = this.checkFixedValue(
						errors, path + ".prefix", (Element)parts.get(i), (org.hl7.fhir.r5.model.Element)fixed.getPrefix().get(i), fixedSource, "prefix", focus, pattern
					)
						&& ok;
				}
			}
		}

		if (!pattern || fixed.hasSuffix()) {
			focus.getNamedChildren("suffix", parts);
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.VALUE,
				focus.line(),
				focus.col(),
				path,
				parts.size() == fixed.getSuffix().size(),
				"Fixed_Type_Checks_DT_Name_Suffix",
				new Object[]{Integer.toString(fixed.getSuffix().size()), Integer.toString(parts.size())}
			)) {
				for(int i = 0; i < parts.size(); ++i) {
					ok = this.checkFixedValue(
						errors, path + ".suffix", (Element)parts.get(i), (org.hl7.fhir.r5.model.Element)fixed.getSuffix().get(i), fixedSource, "suffix", focus, pattern
					)
						&& ok;
				}
			}
		}

		return ok;
	}

	private boolean checkIdentifier(List<ValidationMessage> errors, String path, Element element, ElementDefinition context) {
		boolean ok = true;
		String system = element.getNamedChildValue("system");
		ok = this.rule(
			errors,
			NO_RULE_DATE,
			IssueType.CODEINVALID,
			element.line(),
			element.col(),
			path,
			system == null || this.isIdentifierSystemReferenceValid(system),
			"Type_Specific_Checks_DT_Identifier_System",
			new Object[0]
		)
			&& ok;
		if ("urn:ietf:rfc:3986".equals(system)) {
			String value = element.getNamedChildValue("value");
			ok = this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.CODEINVALID,
				element.line(),
				element.col(),
				path,
				value == null || this.isAbsolute(value),
				"TYPE_SPECIFIC_CHECKS_DT_IDENTIFIER_IETF_SYSTEM_VALUE",
				new Object[]{value}
			)
				&& ok;
		}

		return ok;
	}

	private boolean checkIdentifier(List<ValidationMessage> errors, String path, Element focus, Identifier fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".use", focus.getNamedChild("use"), fixed.getUseElement(), fixedSource, "use", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".type", focus.getNamedChild("type"), fixed.getType(), fixedSource, "type", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".system", focus.getNamedChild("system"), fixed.getSystemElement(), fixedSource, "system", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".value", focus.getNamedChild("value"), fixed.getValueElement(), fixedSource, "value", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".period", focus.getNamedChild("period"), fixed.getPeriod(), fixedSource, "period", focus, pattern) && ok;
		return this.checkFixedValue(errors, path + ".assigner", focus.getNamedChild("assigner"), fixed.getAssigner(), fixedSource, "assigner", focus, pattern)
			&& ok;
	}

	private boolean checkPeriod(List<ValidationMessage> errors, String path, Element focus, Period fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".start", focus.getNamedChild("start"), fixed.getStartElement(), fixedSource, "start", focus, pattern) && ok;
		return this.checkFixedValue(errors, path + ".end", focus.getNamedChild("end"), fixed.getEndElement(), fixedSource, "end", focus, pattern) && ok;
	}

	private boolean checkPrimitive(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		String path,
		String type,
		ElementDefinition context,
		Element e,
		StructureDefinition profile,
		NodeStack node
	) throws FHIRException {
		boolean ok = true;
		if (StringUtils.isBlank(e.primitiveValue())) {
			if (e.primitiveValue() == null) {
				ok = this.rule(
					errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, e.hasChildren(), "Type_Specific_Checks_DT_Primitive_ValueExt", new Object[0]
				)
					&& ok;
			} else if (e.primitiveValue().length() == 0) {
				ok = this.rule(
					errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, e.hasChildren(), "Type_Specific_Checks_DT_Primitive_NotEmpty", new Object[0]
				)
					&& ok;
			} else if (Utilities.isAllWhitespace(e.primitiveValue())) {
				this.warning(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, e.hasChildren(), "Type_Specific_Checks_DT_Primitive_WS", new Object[0]);
			}

			if (context.hasBinding()) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					e.line(),
					e.col(),
					path,
					context.getBinding().getStrength() != BindingStrength.REQUIRED,
					"Terminology_TX_Code_ValueSet_MISSING",
					new Object[0]
				)
					&& ok;
			}

			ok = this.rule(
				errors,
				"2023-06-18",
				IssueType.INVALID,
				e.line(),
				e.col(),
				path,
				!context.getMustHaveValue(),
				"PRIMITIVE_MUSTHAVEVALUE_MESSAGE",
				new Object[]{context.getId(), profile.getVersionedUrl()}
			)
				&& ok;
			if (context.hasValueAlternatives()) {
				boolean found = false;
				CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

				for(CanonicalType ct : context.getValueAlternatives()) {
					found = found || e.hasExtension((String)ct.getValue());
					b.append((String)ct.getValue());
				}

				ok = this.rulePlural(
					errors,
					"2023-06-18",
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					found,
					context.getValueAlternatives().size(),
					"PRIMITIVE_VALUE_ALTERNATIVES_MESSAGE",
					new Object[]{context.getId(), profile.getVersionedUrl(), b.toString()}
				)
					&& ok;
			}

			return ok;
		} else {
			boolean hasBiDiControls = UnicodeUtilities.hasBiDiChars(e.primitiveValue());
			if (hasBiDiControls) {
				if (this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.CODEINVALID,
					e.line(),
					e.col(),
					path,
					!this.noUnicodeBiDiControlChars,
					"UNICODE_BIDI_CONTROLS_CHARS_DISALLOWED",
					new Object[]{UnicodeUtilities.replaceBiDiChars(e.primitiveValue())}
				)) {
					String msg = UnicodeUtilities.checkUnicodeWellFormed(e.primitiveValue());
					this.warning(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, msg == null, "UNICODE_BIDI_CONTROLS_CHARS_MATCH", new Object[]{msg});
				} else {
					ok = false;
				}
			}

			Set<String> badChars = new HashSet();

			for(char ch : e.primitiveValue().toCharArray()) {
				if (ch < ' ' && ch != '\r' && ch != '\n' && ch != '\t') {
					badChars.add(Integer.toHexString(ch));
				}
			}

			this.warningPlural(
				errors,
				"2023-07-26",
				IssueType.INVALID,
				e.line(),
				e.col(),
				path,
				badChars.isEmpty(),
				badChars.size(),
				"UNICODE_XML_BAD_CHARS",
				new Object[]{badChars.toString()}
			);
			String regex = context.getExtensionString("http://hl7.org/fhir/StructureDefinition/regex");
			if (regex != null) {
				for(TypeRefComponent tr : context.getType()) {
					if (tr.hasExtension("http://hl7.org/fhir/StructureDefinition/regex")) {
						regex = tr.getExtensionString("http://hl7.org/fhir/StructureDefinition/regex");
						break;
					}
				}
			}

			if (regex != null) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					e.primitiveValue().matches(regex),
					"Type_Specific_Checks_DT_Primitive_Regex",
					new Object[]{e.primitiveValue(), regex}
				)
					&& ok;
			}

			if (!"xhtml".equals(type)) {
				if (this.securityChecks) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						!this.containsHtmlTags(e.primitiveValue()),
						"SECURITY_STRING_CONTENT_ERROR",
						new Object[0]
					)
						&& ok;
				} else if (!"markdown".equals(type)) {
					this.hint(
						errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, !this.containsHtmlTags(e.primitiveValue()), "SECURITY_STRING_CONTENT_WARNING"
					);
				}
			}

			if (type.equals("boolean")) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					"true".equals(e.primitiveValue()) || "false".equals(e.primitiveValue()),
					"Type_Specific_Checks_DT_Boolean_Value",
					new Object[0]
				)
					&& ok;
			}

			if (type.equals("uri") || type.equals("oid") || type.equals("uuid") || type.equals("url") || type.equals("canonical")) {
				String url = e.primitiveValue();
				ok = this.rule(
					errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, !url.startsWith("oid:"), "Type_Specific_Checks_DT_URI_OID", new Object[0]
				)
					&& ok;
				ok = this.rule(
					errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, !url.startsWith("uuid:"), "Type_Specific_Checks_DT_URI_UUID", new Object[0]
				)
					&& ok;
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					url.equals(Utilities.trimWS(url).replace(" ", ""))
						|| "http://www.acme.com/identifiers/patient or urn:ietf:rfc:3986 if the Identifier.value itself is a full uri".equals(url),
					"Type_Specific_Checks_DT_URI_WS",
					new Object[]{url}
				)
					&& ok;
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					!context.hasMaxLength() || context.getMaxLength() == 0 || url.length() <= context.getMaxLength(),
					"Type_Specific_Checks_DT_Primitive_Length",
					new Object[]{context.getMaxLength()}
				)
					&& ok;
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					!context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(),
					"Type_Specific_Checks_DT_Primitive_Length",
					new Object[]{context.getMaxLength()}
				)
					&& ok;
				if (type.equals("oid")) {
					ok = this.rule(
						errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, url.startsWith("urn:oid:"), "Type_Specific_Checks_DT_OID_Start", new Object[0]
					)
						&& ok;
				}

				if (type.equals("uuid")) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						url.startsWith("urn:uuid:"),
						"Type_Specific_Checks_DT_UUID_Strat",
						new Object[0]
					)
						&& ok;
				}

				if (type.equals("canonical")) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						url.startsWith("#") || Utilities.isAbsoluteUrl(url),
						"TYPE_SPECIFIC_CHECKS_CANONICAL_ABSOLUTE",
						new Object[]{url}
					)
						&& ok;
				}

				if (url != null && url.startsWith("urn:uuid:")) {
					String s = url.substring(9);
					if (s.contains("#")) {
						s = s.substring(0, s.indexOf("#"));
					}

					ok = this.rule(
						errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, Utilities.isValidUUID(s), "Type_Specific_Checks_DT_UUID_Valid", new Object[]{s}
					)
						&& ok;
				}

				if (url != null && url.startsWith("urn:oid:")) {
					String cc = url.substring(8);
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						Utilities.isOid(cc) && (cc.lastIndexOf(46) >= 5 || Utilities.existsInList(cc, new String[]{"1.3.160", "1.3.88"})),
						"Type_Specific_Checks_DT_OID_Valid",
						new Object[]{cc}
					)
						&& ok;
				}

				if (this.isCanonicalURLElement(e)) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						Utilities.isAbsoluteUrl(url),
						node.isContained() ? "TYPE_SPECIFIC_CHECKS_CANONICAL_CONTAINED" : "TYPE_SPECIFIC_CHECKS_CANONICAL_ABSOLUTE",
						new Object[]{url}
					)
						&& ok;
				} else if (!e.getProperty().getDefinition().getPath().equals("Bundle.entry.fullUrl")) {
					ok = this.validateReference(hostContext, errors, path, type, context, e, url) && ok;
				}
			}

			if (type.equals("id") && !"Resource.id".equals(context.getBase().getPath()) && !context.getPath().equals("ElementDefinition.id")) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					FormatUtilities.isValidId(e.primitiveValue()),
					"Type_Specific_Checks_DT_ID_Valid",
					new Object[]{e.primitiveValue()}
				)
					&& ok;
			}

			if (type.equalsIgnoreCase("string") && e.hasPrimitiveValue()) {
				if (this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					e.primitiveValue() == null || e.primitiveValue().length() > 0,
					"Type_Specific_Checks_DT_Primitive_NotEmpty",
					new Object[0]
				)) {
					if (this.warning(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						e.primitiveValue() == null || !Utilities.isAllWhitespace(e.primitiveValue()),
						"Type_Specific_Checks_DT_String_WS_ALL",
						new Object[]{this.prepWSPresentation(e.primitiveValue())}
					)) {
						this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							e.primitiveValue() == null || Utilities.trimWS(e.primitiveValue()).equals(e.primitiveValue()),
							"Type_Specific_Checks_DT_String_WS",
							new Object[]{this.prepWSPresentation(e.primitiveValue())}
						);
					}

					if (this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						e.primitiveValue().length() <= 1048576,
						"Type_Specific_Checks_DT_String_Length",
						new Object[0]
					)) {
						ok = this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							!context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(),
							"Type_Specific_Checks_DT_Primitive_Length",
							new Object[]{context.getMaxLength()}
						)
							&& ok;
					} else {
						ok = false;
					}
				} else {
					ok = false;
				}
			}

			if (type.equals("dateTime")) {
				boolean var26;
				boolean dok = var26 = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					e.primitiveValue()
						.matches(
							"([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\.[0-9]+)?(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00))?)?)?)?"
						),
					"Type_Specific_Checks_DT_DateTime_Valid",
					new Object[]{"'" + e.primitiveValue() + "' doesn't meet format requirements for dateTime"}
				)
					&& ok;
				boolean var40 = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					!this.hasTime(e.primitiveValue()) || this.hasTimeZone(e.primitiveValue()),
					"Type_Specific_Checks_DT_DateTime_TZ",
					new Object[0]
				)
					&& dok;
				boolean var41 = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					!context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(),
					"Type_Specific_Checks_DT_Primitive_Length",
					new Object[]{context.getMaxLength()}
				)
					&& var40;
				if (var41) {
					try {
						new DateTimeType(e.primitiveValue());
						this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							this.yearIsValid(e.primitiveValue()),
							"Type_Specific_Checks_DT_DateTime_Reasonable",
							new Object[]{e.primitiveValue()}
						);
					} catch (Exception var19) {
						this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							false,
							"Type_Specific_Checks_DT_DateTime_Valid",
							new Object[]{var19.getMessage()}
						);
						var41 = false;
					}
				}

				ok = var26 && var41;
			}

			if (type.equals("time")) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					e.primitiveValue().matches("([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)"),
					"Type_Specific_Checks_DT_Time_Valid",
					new Object[0]
				)
					&& ok;

				try {
					new TimeType(e.primitiveValue());
				} catch (Exception var18) {
					this.rule(
						errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, false, "Type_Specific_Checks_DT_Time_Valid", new Object[]{var18.getMessage()}
					);
					ok = false;
				}
			}

			if (type.equals("date")) {
				boolean dok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					e.primitiveValue().matches("([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1]))?)?"),
					"Type_Specific_Checks_DT_Date_Valid",
					new Object[]{"'" + e.primitiveValue() + "' doesn't meet format requirements for date"}
				);
				boolean var43 = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					!context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(),
					"Type_Specific_Checks_DT_Primitive_Length",
					new Object[]{context.getMaxLength()}
				)
					&& dok;
				if (var43) {
					try {
						new DateType(e.primitiveValue());
						this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							this.yearIsValid(e.primitiveValue()),
							"Type_Specific_Checks_DT_DateTime_Reasonable",
							new Object[]{e.primitiveValue()}
						);
					} catch (Exception var17) {
						this.rule(
							errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, false, "Type_Specific_Checks_DT_Date_Valid", new Object[]{var17.getMessage()}
						);
						var43 = false;
					}
				}

				ok = ok && var43;
			}

			if (type.equals("base64Binary")) {
				String encoded = e.primitiveValue();
				if (StringUtils.isNotBlank(encoded)) {
					boolean bok = this.isValidBase64(encoded);
					if (!bok) {
						String value = encoded.length() < 100 ? encoded : "(snip)";
						ok = this.rule(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, false, "Type_Specific_Checks_DT_Base64_Valid", new Object[]{value})
							&& ok;
					} else {
						boolean wsok = !this.base64HasWhitespace(encoded);
						if (!VersionUtilities.isR5VerOrLater(this.context.getVersion())) {
							this.warning(
								errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, wsok, "TYPE_SPECIFIC_CHECKS_DT_BASE64_NO_WS_WARNING", new Object[0]
							);
						} else {
							ok = this.rule(
								errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, wsok, "TYPE_SPECIFIC_CHECKS_DT_BASE64_NO_WS_ERROR", new Object[0]
							)
								&& ok;
						}
					}

					if (bok && context.hasExtension("http://hl7.org/fhir/StructureDefinition/maxSize")) {
						int size = this.countBase64DecodedBytes(encoded);
						long def = Long.parseLong(ToolingExtensions.readStringExtension(context, "http://hl7.org/fhir/StructureDefinition/maxSize"));
						ok = this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							e.line(),
							e.col(),
							path,
							(long)size <= def,
							"TYPE_SPECIFIC_CHECKS_DT_BASE64_TOO_LONG",
							new Object[]{size, def}
						)
							&& ok;
					}
				}
			}

			if (type.equals("integer") || type.equals("unsignedInt") || type.equals("positiveInt")) {
				if (this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					Utilities.isInteger(e.primitiveValue()),
					"Type_Specific_Checks_DT_Integer_Valid",
					new Object[]{e.primitiveValue()}
				)) {
					Integer v = new Integer(e.getValue());
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						!context.hasMaxValueIntegerType() || !context.getMaxValueIntegerType().hasValue() || context.getMaxValueIntegerType().getValue() >= v,
						"Type_Specific_Checks_DT_Integer_GT",
						new Object[]{context.hasMaxValueIntegerType() ? context.getMaxValueIntegerType() : ""}
					)
						&& ok;
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						!context.hasMinValueIntegerType() || !context.getMinValueIntegerType().hasValue() || context.getMinValueIntegerType().getValue() <= v,
						"Type_Specific_Checks_DT_Integer_LT",
						new Object[]{context.hasMinValueIntegerType() ? context.getMinValueIntegerType() : ""}
					)
						&& ok;
					if (type.equals("unsignedInt")) {
						ok = this.rule(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, v >= 0, "Type_Specific_Checks_DT_Integer_LT0", new Object[0])
							&& ok;
					}

					if (type.equals("positiveInt")) {
						ok = this.rule(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, v > 0, "Type_Specific_Checks_DT_Integer_LT1", new Object[0]) && ok;
					}
				} else {
					ok = false;
				}
			}

			if (type.equals("integer64")) {
				if (this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					Utilities.isLong(e.primitiveValue()),
					"Type_Specific_Checks_DT_Integer64_Valid",
					new Object[]{e.primitiveValue()}
				)) {
					Long v = new Long(e.getValue());
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						!context.hasMaxValueInteger64Type() || !context.getMaxValueInteger64Type().hasValue() || context.getMaxValueInteger64Type().getValue() >= v,
						"Type_Specific_Checks_DT_Integer_GT",
						new Object[]{context.hasMaxValueInteger64Type() ? context.getMaxValueInteger64Type() : ""}
					)
						&& ok;
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						!context.hasMinValueInteger64Type() || !context.getMinValueInteger64Type().hasValue() || context.getMinValueInteger64Type().getValue() <= v,
						"Type_Specific_Checks_DT_Integer_LT",
						new Object[]{context.hasMinValueInteger64Type() ? context.getMinValueInteger64Type() : ""}
					)
						&& ok;
					if (type.equals("unsignedInt")) {
						ok = this.rule(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, v >= 0L, "Type_Specific_Checks_DT_Integer_LT0", new Object[0])
							&& ok;
					}

					if (type.equals("positiveInt")) {
						ok = this.rule(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, v > 0L, "Type_Specific_Checks_DT_Integer_LT1", new Object[0])
							&& ok;
					}
				} else {
					ok = false;
				}
			}

			if (type.equals("decimal")) {
				if (e.primitiveValue() != null) {
					DecimalStatus ds = Utilities.checkDecimal(e.primitiveValue(), true, false);
					if (this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						ds == DecimalStatus.OK || ds == DecimalStatus.RANGE,
						"Type_Specific_Checks_DT_Decimal_Valid",
						new Object[]{e.primitiveValue()}
					)) {
						this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.VALUE,
							e.line(),
							e.col(),
							path,
							ds != DecimalStatus.RANGE,
							"Type_Specific_Checks_DT_Decimal_Range",
							new Object[]{e.primitiveValue()}
						);

						try {
							Decimal v = new Decimal(e.getValue());
							if (context.hasMaxValueDecimalType() && context.getMaxValueDecimalType().hasValue()) {
								ok = this.rule(
									errors,
									NO_RULE_DATE,
									IssueType.INVALID,
									e.line(),
									e.col(),
									path,
									this.checkDecimalMaxValue(v, (BigDecimal)context.getMaxValueDecimalType().getValue()),
									"Type_Specific_Checks_DT_Decimal_GT",
									new Object[]{context.getMaxValueDecimalType()}
								)
									&& ok;
							} else if (context.hasMaxValueIntegerType() && context.getMaxValueIntegerType().hasValue()) {
								ok = this.rule(
									errors,
									NO_RULE_DATE,
									IssueType.INVALID,
									e.line(),
									e.col(),
									path,
									this.checkDecimalMaxValue(v, new BigDecimal(context.getMaxValueIntegerType().getValue())),
									"Type_Specific_Checks_DT_Decimal_GT",
									new Object[]{context.getMaxValueIntegerType()}
								)
									&& ok;
							}

							if (context.hasMinValueDecimalType() && context.getMaxValueDecimalType().hasValue()) {
								ok = this.rule(
									errors,
									NO_RULE_DATE,
									IssueType.INVALID,
									e.line(),
									e.col(),
									path,
									this.checkDecimalMinValue(v, (BigDecimal)context.getMaxValueDecimalType().getValue()),
									"Type_Specific_Checks_DT_Decimal_LT",
									new Object[]{context.getMaxValueDecimalType()}
								)
									&& ok;
							} else if (context.hasMinValueIntegerType() && context.getMaxValueIntegerType().hasValue()) {
								ok = this.rule(
									errors,
									NO_RULE_DATE,
									IssueType.INVALID,
									e.line(),
									e.col(),
									path,
									this.checkDecimalMinValue(v, new BigDecimal(context.getMaxValueIntegerType().getValue())),
									"Type_Specific_Checks_DT_Decimal_LT",
									new Object[]{context.getMaxValueIntegerType()}
								)
									&& ok;
							}
						} catch (Exception var21) {
						}
					} else {
						ok = false;
					}
				}

				if (context.hasExtension("http://hl7.org/fhir/StructureDefinition/maxDecimalPlaces")) {
					int dp = e.primitiveValue().contains(".") ? e.primitiveValue().substring(e.primitiveValue().indexOf(".") + 1).length() : 0;
					int def = Integer.parseInt(ToolingExtensions.readStringExtension(context, "http://hl7.org/fhir/StructureDefinition/maxDecimalPlaces"));
					ok = this.rule(
						errors, NO_RULE_DATE, IssueType.STRUCTURE, e.line(), e.col(), path, dp <= def, "TYPE_SPECIFIC_CHECKS_DT_DECIMAL_CHARS", new Object[]{dp, def}
					)
						&& ok;
				}
			}

			if (type.equals("instant")) {
				boolean dok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					e.primitiveValue()
						.matches(
							"-?[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\.[0-9]+)?(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00))"
						),
					"Type_Specific_Checks_DT_DateTime_Regex",
					new Object[]{"'" + e.primitiveValue() + "' doesn't meet format requirements for instant)"}
				);
				if (dok) {
					try {
						new InstantType(e.primitiveValue());
						this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							this.yearIsValid(e.primitiveValue()),
							"Type_Specific_Checks_DT_DateTime_Reasonable",
							new Object[]{e.primitiveValue()}
						);
					} catch (Exception var16) {
						this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							false,
							"Type_Specific_Checks_DT_Instant_Valid",
							new Object[]{var16.getMessage()}
						);
						dok = false;
					}
				}

				ok = ok && dok;
			}

			if (type.equals("code") && e.primitiveValue() != null) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					this.passesCodeWhitespaceRules(e.primitiveValue()),
					"Type_Specific_Checks_DT_Code_WS",
					new Object[]{e.primitiveValue()}
				)
					&& ok;
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					!context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(),
					"Type_Specific_Checks_DT_Primitive_Length",
					new Object[]{context.getMaxLength()}
				)
					&& ok;
			}

			if (context.hasBinding() && e.primitiveValue() != null) {
				if ("StructureDefinition.type".equals(context.getPath()) && "http://hl7.org/fhir/StructureDefinition/StructureDefinition".equals(profile.getUrl())) {
					ok = this.checkTypeValue(errors, path, e, node.getElement());
				} else {
					ok = this.checkPrimitiveBinding(hostContext, errors, path, type, context, e, profile, node) && ok;
				}
			}

			if (type.equals("markdown") && this.htmlInMarkdownCheck != HtmlInMarkdownCheck.NONE) {
				String raw = e.primitiveValue();
				String processed = MarkDownProcessor.preProcess(raw);
				if (!raw.equals(processed)) {
					int i = 0;

					while(i < raw.length() && raw.charAt(1) == processed.charAt(i)) {
						++i;
					}

					if (i < raw.length() - 1) {
						ok = this.warningOrError(
							this.htmlInMarkdownCheck == HtmlInMarkdownCheck.ERROR,
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							false,
							"TYPE_SPECIFIC_CHECKS_DT_MARKDOWN_HTML",
							new Object[]{raw.subSequence(i, i + 2)}
						)
							&& ok;
					} else {
						ok = this.warningOrError(
							this.htmlInMarkdownCheck == HtmlInMarkdownCheck.ERROR,
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							false,
							"TYPE_SPECIFIC_CHECKS_DT_MARKDOWN_HTML",
							new Object[]{raw}
						)
							&& ok;
					}
				}
			}

			if (type.equals("xhtml")) {
				XhtmlNode xhtml = e.getXhtml();
				if (xhtml != null) {
					String ns = xhtml.getNsDecl();
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						"http://www.w3.org/1999/xhtml".equals(ns),
						"XHTML_XHTML_NS_InValid",
						new Object[]{ns, "http://www.w3.org/1999/xhtml"}
					)
						&& ok;
					this.checkInnerNS(errors, e, path, xhtml.getChildNodes());
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						e.line(),
						e.col(),
						path,
						"div".equals(xhtml.getName()),
						"XHTML_XHTML_Name_Invalid",
						new Object[]{xhtml.getName()}
					)
						&& ok;
					ok = this.checkInnerNames(errors, e, path, xhtml.getChildNodes(), false) && ok;
					ok = this.checkUrls(errors, e, path, xhtml.getChildNodes()) && ok;
				}
			}

			if (context.hasFixed()) {
				ok = this.checkFixedValue(errors, path, e, context.getFixed(), profile.getVersionedUrl(), context.getSliceName(), null, false) && ok;
			}

			if (context.hasPattern()) {
				ok = this.checkFixedValue(errors, path, e, context.getPattern(), profile.getVersionedUrl(), context.getSliceName(), null, true) && ok;
			}

			if (ok && !ID_EXEMPT_LIST.contains(e.fhirType())) {
				String regext = FHIRPathExpressionFixer.fixRegex(this.getRegexFromType(e.fhirType()));
				if (regext != null) {
					try {
						boolean matches = e.primitiveValue().matches(regext);
						if (!matches) {
							ok = this.rule(
								errors,
								NO_RULE_DATE,
								IssueType.INVALID,
								e.line(),
								e.col(),
								path,
								matches,
								"Type_Specific_Checks_DT_Primitive_Regex_Type",
								new Object[]{e.primitiveValue(), e.fhirType(), regext}
							)
								&& ok;
						}
					} catch (Throwable var20) {
						ok = this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							false,
							"TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_REGEX_EXCEPTION",
							new Object[]{regext, e.fhirType(), var20.getMessage()}
						)
							&& ok;
					}
				}
			}

			return ok;
		}
	}

	private String getRegexFromType(String fhirType) {
		StructureDefinition sd = this.context.fetchTypeDefinition(fhirType);
		if (sd != null) {
			for(ElementDefinition ed : sd.getSnapshot().getElement()) {
				if (ed.getPath().endsWith(".value")) {
					String regex = ed.getExtensionString("http://hl7.org/fhir/StructureDefinition/regex");
					if (regex != null) {
						return regex;
					}

					for(TypeRefComponent td : ed.getType()) {
						regex = td.getExtensionString("http://hl7.org/fhir/StructureDefinition/regex");
						if (regex != null) {
							return regex;
						}
					}
				}
			}
		}

		return null;
	}

	private boolean checkTypeValue(List<ValidationMessage> errors, String path, Element e, Element sd) {
		String v = e.primitiveValue();
		if (v == null) {
			return this.rule(errors, "2022-11-02", IssueType.INVALID, e.line(), e.col(), path, false, "SD_TYPE_MISSING", new Object[0]);
		} else {
			String url = sd.getChildValue("url");
			String d = sd.getChildValue("derivation");
			String k = sd.getChildValue("kind");
			if (!Utilities.isAbsoluteUrl(v)) {
				boolean tok = false;

				for(StructureDefinition t : this.context.fetchResourcesByType(StructureDefinition.class)) {
					if (t.hasSourcePackage() && t.getSourcePackage().getId().startsWith("hl7.fhir.r") && v.equals(t.getType())) {
						tok = true;
					}
				}

				if (!tok) {
					return this.rule(errors, "2022-11-02", IssueType.INVALID, e.line(), e.col(), path, tok, "SD_TYPE_NOT_LOCAL", new Object[]{v});
				} else {
					return !("http://hl7.org/fhir/StructureDefinition/" + v).equals(url)
						? this.rule(errors, "2022-11-02", IssueType.INVALID, e.line(), e.col(), path, "constraint".equals(d), "SD_TYPE_NOT_DERIVED", new Object[]{v})
						: true;
				}
			} else {
				this.warning(
					errors,
					"2022-11-02",
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					d.equals("constraint") || this.ns(v).equals(this.ns(url)) || this.ns(v).equals(this.ns(url).replace("StructureDefinition/", "")),
					"SD_TYPE_NOT_MATCH_NS",
					new Object[]{v, url}
				);
				return this.rule(errors, "2022-11-02", IssueType.INVALID, e.line(), e.col(), path, "logical".equals(k), "SD_TYPE_NOT_LOGICAL", new Object[]{v, k});
			}
		}
	}

	private String ns(String url) {
		return url.contains("/") ? url.substring(0, url.lastIndexOf("/")) : url;
	}

	private Object prepWSPresentation(String s) {
		return Utilities.noString(s) ? "" : Utilities.escapeJson(s);
	}

	public boolean validateReference(
		ValidatorHostContext hostContext, List<ValidationMessage> errors, String path, String type, ElementDefinition context, Element e, String url
	) {
		boolean ok = true;
		if (this.fetcher != null && !type.equals("uuid")) {
			boolean found;
			try {
				found = this.isDefinitionURL(url)
					|| this.allowExamples && (url.contains("example.org") || url.contains("acme.com"))
					|| url.contains("acme.org")
					|| SpecialExtensions.isKnownExtension(url)
					|| this.isXverUrl(url);
				if (!found) {
					found = this.fetcher.resolveURL(this, hostContext, path, url, type, type.equals("canonical"));
				}
			} catch (IOException var13) {
				found = false;
			}

			if (!found) {
				if (type.equals("canonical")) {
					ReferenceValidationPolicy rp = this.policyAdvisor == null
						? ReferenceValidationPolicy.CHECK_VALID
						: this.policyAdvisor.policyForReference(this, hostContext, path, url);
					if (rp != ReferenceValidationPolicy.CHECK_EXISTS && rp != ReferenceValidationPolicy.CHECK_EXISTS_AND_TYPE) {
						this.hint(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, false, "TYPE_SPECIFIC_CHECKS_DT_CANONICAL_RESOLVE", new Object[]{url});
					} else {
						ok = this.rule(
							errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, false, "TYPE_SPECIFIC_CHECKS_DT_CANONICAL_RESOLVE", new Object[]{url}
						)
							&& ok;
					}
				} else if (url.contains("hl7.org") || url.contains("fhir.org")) {
					ok = this.rule(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, false, "Type_Specific_Checks_DT_URL_Resolve", new Object[]{url})
						&& ok;
				} else if (!url.contains("example.org") && !url.contains("acme.com")) {
					this.warning(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, false, "Type_Specific_Checks_DT_URL_Resolve", new Object[]{url});
				} else {
					ok = this.rule(errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, false, "TYPE_SPECIFIC_CHECKS_DT_URL_EXAMPLE", new Object[]{url})
						&& ok;
				}
			} else if (type.equals("canonical")) {
				ReferenceValidationPolicy rp = this.policyAdvisor == null
					? ReferenceValidationPolicy.CHECK_VALID
					: this.policyAdvisor.policyForReference(this, hostContext, path, url);
				if (rp == ReferenceValidationPolicy.CHECK_EXISTS_AND_TYPE
					|| rp == ReferenceValidationPolicy.CHECK_TYPE_IF_EXISTS
					|| rp == ReferenceValidationPolicy.CHECK_VALID) {
					try {
						Resource r = null;
						if (url.startsWith("#")) {
							r = this.loadContainedResource(errors, path, hostContext.getRootResource(), url.substring(1), Resource.class);
						}

						if (r == null) {
							r = this.fetcher.fetchCanonicalResource(this, url);
						}

						if (r == null) {
							r = this.context.fetchResource(Resource.class, url);
						}

						if (r == null) {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.INVALID,
								e.line(),
								e.col(),
								path,
								rp != ReferenceValidationPolicy.CHECK_VALID,
								"TYPE_SPECIFIC_CHECKS_DT_CANONICAL_RESOLVE_NC",
								new Object[]{url}
							);
						} else if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							e.line(),
							e.col(),
							path,
							this.isCorrectCanonicalType(r, context),
							"TYPE_SPECIFIC_CHECKS_DT_CANONICAL_TYPE",
							new Object[]{url, r.fhirType(), this.listExpectedCanonicalTypes(context)}
						)
							&& rp == ReferenceValidationPolicy.CHECK_VALID) {
						}
					} catch (Exception var12) {
					}
				}
			}
		}

		return ok;
	}

	private Set<String> listExpectedCanonicalTypes(ElementDefinition context) {
		Set<String> res = new HashSet();
		TypeRefComponent tr = context.getType("canonical");
		if (tr != null) {
			for(CanonicalType p : tr.getTargetProfile()) {
				String url = (String)p.getValue();
				StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, url);
				if (sd != null) {
					res.add(sd.getType());
				} else if (url != null && url.startsWith("http://hl7.org/fhir/StructureDefinition/")) {
					res.add(url.substring("http://hl7.org/fhir/StructureDefinition/".length()));
				}
			}
		}

		return res;
	}

	private boolean isCorrectCanonicalType(Resource r, ElementDefinition context) {
		TypeRefComponent tr = context.getType("canonical");
		if (tr != null) {
			for(CanonicalType p : tr.getTargetProfile()) {
				if (this.isCorrectCanonicalType(r, p)) {
					return true;
				}
			}

			if (tr.getTargetProfile().isEmpty()) {
				return true;
			}
		}

		return false;
	}

	private boolean isCorrectCanonicalType(Resource r, CanonicalType p) {
		String url = (String)p.getValue();
		String t = null;
		StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, url);
		if (sd != null) {
			t = sd.getType();
		} else {
			if (!url.startsWith("http://hl7.org/fhir/StructureDefinition/")) {
				return false;
			}

			t = url.substring("http://hl7.org/fhir/StructureDefinition/".length());
		}

		return Utilities.existsInList(t, new String[]{"Resource", "CanonicalResource"}) || t.equals(r.fhirType());
	}

	private boolean isCanonicalURLElement(Element e) {
		if (e.getProperty() != null && e.getProperty().getDefinition() != null) {
			String path = e.getProperty().getDefinition().getBase().getPath();
			if (path == null) {
				return false;
			} else {
				String[] p = path.split("\\.");
				if (p.length != 2) {
					return false;
				} else {
					return !"url".equals(p[1]) ? false : VersionUtilities.getCanonicalResourceNames(this.context.getVersion()).contains(p[0]);
				}
			}
		} else {
			return false;
		}
	}

	private boolean containsHtmlTags(String cnt) {
		int i = cnt.indexOf("<");

		while(i > -1) {
			cnt = cnt.substring(i + 1);
			i = cnt.indexOf("<");
			int e = cnt.indexOf(">");
			if (e > -1 && e < i) {
				String s = cnt.substring(0, e);
				if (s.matches("[a-zA-Z]\\w*(((\\s+)(\\S)*)*)")) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isValidBase64(String theEncoded) {
		if (theEncoded == null) {
			return false;
		} else {
			int charCount = 0;
			boolean ok = true;

			for(int i = 0; i < theEncoded.length(); ++i) {
				char nextChar = theEncoded.charAt(i);
				if (!Utilities.isWhitespace(nextChar)) {
					if (Character.isLetterOrDigit(nextChar)) {
						++charCount;
					}

					if (nextChar == '/' || nextChar == '=' || nextChar == '+') {
						++charCount;
					}
				}
			}

			if (charCount > 0 && charCount % 4 != 0) {
				ok = false;
			}

			return ok;
		}
	}

	private boolean base64HasWhitespace(String theEncoded) {
		if (theEncoded == null) {
			return false;
		} else {
			for(int i = 0; i < theEncoded.length(); ++i) {
				char nextChar = theEncoded.charAt(i);
				if (Utilities.isWhitespace(nextChar)) {
					return true;
				}
			}

			return false;
		}
	}

	private int countBase64DecodedBytes(String theEncoded) {
		Base64InputStream inputStream = new Base64InputStream(new ByteArrayInputStream(theEncoded.getBytes(StandardCharsets.UTF_8)));

		try {
			int var4;
			try {
				int counter = 0;

				while(inputStream.read() != -1) {
					++counter;
				}

				var4 = counter;
			} finally {
				inputStream.close();
			}

			return var4;
		} catch (IOException var9) {
			throw new IllegalStateException(var9);
		}
	}

	private boolean isDefinitionURL(String url) {
		return Utilities.existsInList(
			url,
			new String[]{
				"http://hl7.org/fhirpath/System.Boolean",
				"http://hl7.org/fhirpath/System.String",
				"http://hl7.org/fhirpath/System.Integer",
				"http://hl7.org/fhirpath/System.Decimal",
				"http://hl7.org/fhirpath/System.Date",
				"http://hl7.org/fhirpath/System.Time",
				"http://hl7.org/fhirpath/System.DateTime",
				"http://hl7.org/fhirpath/System.Quantity"
			}
		);
	}

	private boolean checkInnerNames(List<ValidationMessage> errors, Element e, String path, List<XhtmlNode> list, boolean inPara) {
		boolean ok = true;

		for(XhtmlNode node : list) {
			if (node.getNodeType() == NodeType.Comment) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					!node.getContent().startsWith("DOCTYPE"),
					"XHTML_XHTML_DOCTYPE_ILLEGAL",
					new Object[0]
				);
			}

			if (node.getNodeType() == NodeType.Element) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					HTML_ELEMENTS.contains(node.getName()),
					"XHTML_XHTML_Element_Illegal",
					new Object[]{node.getName()}
				);

				for(String an : node.getAttributes().keySet()) {
					boolean bok = an.startsWith("xmlns") || HTML_ATTRIBUTES.contains(an) || HTML_COMBO_LIST.contains(node.getName() + "." + an);
					if (!bok) {
						this.rule(
							errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, false, "XHTML_XHTML_Attribute_Illegal", new Object[]{an, node.getName()}
						);
					}
				}

				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					!inPara || !HTML_BLOCK_LIST.contains(node.getName()),
					"XHTML_XHTML_ELEMENT_ILLEGAL_IN_PARA",
					new Object[]{node.getName()}
				)
					&& ok;
				ok = this.checkInnerNames(errors, e, path, node.getChildNodes(), inPara || "p".equals(node.getName())) && ok;
			}
		}

		return ok;
	}

	private boolean checkUrls(List<ValidationMessage> errors, Element e, String path, List<XhtmlNode> list) {
		boolean ok = true;

		for(XhtmlNode node : list) {
			if (node.getNodeType() == NodeType.Element) {
				if ("a".equals(node.getName())) {
					String msg = this.checkValidUrl(node.getAttribute("href"));
					ok = this.rule(
						errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, msg == null, "XHTML_URL_INVALID", new Object[]{node.getAttribute("href"), msg}
					)
						&& ok;
				} else if ("img".equals(node.getName())) {
					String msg = this.checkValidUrl(node.getAttribute("src"));
					ok = this.rule(
						errors, NO_RULE_DATE, IssueType.INVALID, e.line(), e.col(), path, msg == null, "XHTML_URL_INVALID", new Object[]{node.getAttribute("src"), msg}
					)
						&& ok;
				}

				ok = this.checkUrls(errors, e, path, node.getChildNodes()) && ok;
			}
		}

		return ok;
	}

	private String checkValidUrl(String value) {
		if (value == null) {
			return null;
		} else if (Utilities.noString(value)) {
			return this.context.formatMessage("XHTML_URL_EMPTY", new Object[0]);
		} else if (value.startsWith("data:")) {
			String[] p = value.substring(5).split("\\,");
			if (p.length < 2) {
				return this.context.formatMessage("XHTML_URL_DATA_NO_DATA", new Object[]{value});
			} else if (p.length > 2) {
				return this.context.formatMessage("XHTML_URL_DATA_DATA_INVALID_COMMA", new Object[]{value});
			} else if (p[0].endsWith(";base64") && this.isValidBase64(p[1])) {
				if (p[0].startsWith(" ")) {
					p[0] = Utilities.trimWS(p[0]);
				}

				String mMsg = this.checkValidMimeType(p[0].substring(0, p[0].lastIndexOf(";")));
				return mMsg != null ? this.context.formatMessage("XHTML_URL_DATA_MIMETYPE", new Object[]{value, mMsg}) : null;
			} else {
				return this.context.formatMessage("XHTML_URL_DATA_DATA_INVALID", new Object[]{value});
			}
		} else {
			Set<Character> invalidChars = new HashSet();
			int c = 0;

			for(char ch : value.toCharArray()) {
				if (!Character.isDigit(ch)
					&& !Character.isAlphabetic(ch)
					&& !Utilities.existsInList(ch, new int[]{59, 63, 58, 64, 38, 61, 43, 36, 46, 44, 47, 37, 45, 95, 126, 35, 91, 93, 33, 39, 40, 41, 42, 124})) {
					++c;
					invalidChars.add(ch);
				}
			}

			return invalidChars.isEmpty() ? null : this.context.formatMessagePlural(c, "XHTML_URL_INVALID_CHARS", new Object[]{invalidChars.toString()});
		}
	}

	private String checkValidMimeType(String mt) {
		return !mt.matches("^(\\w+|\\*)\\/(\\w+|\\*)((;\\s*(\\w+)=\\s*(\\S+))?)$") ? "Mime type invalid" : null;
	}

	private void checkInnerNS(List<ValidationMessage> errors, Element e, String path, List<XhtmlNode> list) {
		for(XhtmlNode node : list) {
			if (node.getNodeType() == NodeType.Element) {
				String ns = node.getNsDecl();
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					e.line(),
					e.col(),
					path,
					ns == null || "http://www.w3.org/1999/xhtml".equals(ns),
					"XHTML_XHTML_NS_InValid",
					new Object[]{ns, "http://www.w3.org/1999/xhtml"}
				);
				this.checkInnerNS(errors, e, path, node.getChildNodes());
			}
		}
	}

	private boolean checkPrimitiveBinding(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		String path,
		String type,
		ElementDefinition elementContext,
		Element element,
		StructureDefinition profile,
		NodeStack stack
	) {
		if (element.hasPrimitiveValue() && ("code".equals(type) || "string".equals(type) || "uri".equals(type) || "url".equals(type) || "canonical".equals(type))) {
			if (this.noTerminologyChecks) {
				return true;
			} else {
				boolean ok = true;
				String value = element.primitiveValue();
				ElementDefinitionBindingComponent binding = elementContext.getBinding();
				if (binding.hasValueSet()) {
					ValueSet vs = this.resolveBindingReference(profile, binding.getValueSet(), profile.getUrl(), profile);
					if (vs == null) {
						CodeSystem cs = this.context.fetchCodeSystem(binding.getValueSet());
						if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.CODEINVALID,
							element.line(),
							element.col(),
							path,
							cs == null,
							"Terminology_TX_ValueSet_NotFound_CS",
							new Object[]{this.describeReference(binding.getValueSet())}
						)) {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.CODEINVALID,
								element.line(),
								element.col(),
								path,
								vs != null,
								"Terminology_TX_ValueSet_NotFound",
								new Object[]{this.describeReference(binding.getValueSet())}
							);
						} else {
							ok = false;
						}
					} else {
						CodedContentValidationPolicy validationPolicy = this.getPolicyAdvisor() == null
							? CodedContentValidationPolicy.VALUESET
							: this.getPolicyAdvisor()
							.policyForCodedContent(this, hostContext, stack.getLiteralPath(), elementContext, profile, BindingKind.PRIMARY, vs, new ArrayList());
						if (validationPolicy != CodedContentValidationPolicy.IGNORE) {
							long t = System.nanoTime();
							ValidationResult vr = null;
							if (binding.getStrength() != BindingStrength.EXAMPLE) {
								ValidationOptions options = this.baseOptions.withLanguage(stack.getWorkingLang()).withGuessSystem();
								if (validationPolicy == CodedContentValidationPolicy.CODE) {
									options = options.withNoCheckValueSetMembership();
								}

								vr = this.checkCodeOnServer(stack, vs, value, options);
							}

							if (vr != null && vr.isOk()) {
								for(OperationOutcomeIssueComponent iss : vr.getIssues()) {
									this.txIssue(errors, "2023-08-19", vr.getTxLink(), element.line(), element.col(), path, iss);
								}
							}

							this.timeTracker.tx(t, "vc " + value);
							if (binding.getStrength() == BindingStrength.REQUIRED) {
								this.removeTrackedMessagesForLocation(errors, element, path);
							}

							if (vr != null && !vr.isOk()) {
								if (vr.IsNoService()) {
									this.txHint(
										errors,
										NO_RULE_DATE,
										vr.getTxLink(),
										IssueType.CODEINVALID,
										element.line(),
										element.col(),
										path,
										false,
										"Terminology_TX_NoValid_15",
										new Object[]{value}
									);
								} else if (vr.getErrorClass() != null && vr.getErrorClass() == TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED) {
									this.txWarning(
										errors,
										NO_RULE_DATE,
										vr.getTxLink(),
										IssueType.CODEINVALID,
										element.line(),
										element.col(),
										path,
										false,
										"Terminology_TX_NoValid_15A",
										new Object[]{value, vr.unknownSystems(), this.describeReference(binding.getValueSet())}
									);
								} else if (binding.getStrength() == BindingStrength.REQUIRED) {
									ok = this.txRule(
										errors,
										NO_RULE_DATE,
										vr.getTxLink(),
										IssueType.CODEINVALID,
										element.line(),
										element.col(),
										path,
										false,
										"Terminology_TX_NoValid_16",
										new Object[]{value, this.describeReference(binding.getValueSet(), vs), this.getErrorMessage(vr.getMessage())}
									)
										&& ok;
								} else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
									if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")) {
										ok = this.checkMaxValueSet(
											errors,
											path,
											element,
											profile,
											ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"),
											value,
											stack
										)
											&& ok;
									} else if (!this.noExtensibleWarnings && !this.isOkExtension(value, vs)) {
										this.txWarningForLaterRemoval(
											element,
											errors,
											NO_RULE_DATE,
											vr.getTxLink(),
											IssueType.CODEINVALID,
											element.line(),
											element.col(),
											path,
											false,
											"Terminology_TX_NoValid_17",
											new Object[]{value, this.describeReference(binding.getValueSet(), vs), this.getErrorMessage(vr.getMessage())}
										);
									}
								} else if (binding.getStrength() == BindingStrength.PREFERRED && this.baseOnly) {
									this.txHint(
										errors,
										NO_RULE_DATE,
										vr.getTxLink(),
										IssueType.CODEINVALID,
										element.line(),
										element.col(),
										path,
										false,
										"Terminology_TX_NoValid_18",
										new Object[]{value, this.describeReference(binding.getValueSet(), vs), this.getErrorMessage(vr.getMessage())}
									);
								}
							} else if (vr != null && vr.getMessage() != null) {
								if (vr.getSeverity() == IssueSeverity.INFORMATION) {
									this.txHint(
										errors,
										"2023-07-04",
										vr.getTxLink(),
										IssueType.INFORMATIONAL,
										element.line(),
										element.col(),
										path,
										false,
										"TERMINOLOGY_TX_HINT",
										new Object[]{value, vr.getMessage()}
									);
								} else {
									this.txWarning(
										errors,
										"2023-07-04",
										vr.getTxLink(),
										IssueType.INFORMATIONAL,
										element.line(),
										element.col(),
										path,
										false,
										"TERMINOLOGY_TX_WARNING",
										new Object[]{value, vr.getMessage()}
									);
								}
							}
						}
					}
				} else if (!this.noBindingMsgSuppressed) {
					this.hint(errors, NO_RULE_DATE, IssueType.CODEINVALID, element.line(), element.col(), path, !type.equals("code"), "Terminology_TX_Binding_NoSource2");
				}

				return ok;
			}
		} else {
			return true;
		}
	}

	private boolean isOkExtension(String value, ValueSet vs) {
		return "http://hl7.org/fhir/ValueSet/defined-types".equals(vs.getUrl()) ? value.startsWith("http://hl7.org/fhirpath/System.") : false;
	}

	private void checkQuantity(List<ValidationMessage> errors, String path, Element focus, Quantity fixed, String fixedSource, boolean pattern) {
		this.checkFixedValue(errors, path + ".value", focus.getNamedChild("value"), fixed.getValueElement(), fixedSource, "value", focus, pattern);
		this.checkFixedValue(
			errors, path + ".comparator", focus.getNamedChild("comparator"), fixed.getComparatorElement(), fixedSource, "comparator", focus, pattern
		);
		this.checkFixedValue(errors, path + ".unit", focus.getNamedChild("unit"), fixed.getUnitElement(), fixedSource, "unit", focus, pattern);
		this.checkFixedValue(errors, path + ".system", focus.getNamedChild("system"), fixed.getSystemElement(), fixedSource, "system", focus, pattern);
		this.checkFixedValue(errors, path + ".code", focus.getNamedChild("code"), fixed.getCodeElement(), fixedSource, "code", focus, pattern);
	}

	private boolean checkQuantity(
		List<ValidationMessage> errors, String path, Element element, StructureDefinition theProfile, ElementDefinition definition, NodeStack theStack
	) {
		boolean ok = true;
		String value = element.hasChild("value") ? element.getNamedChild("value").getValue() : null;
		String unit = element.hasChild("unit") ? element.getNamedChild("unit").getValue() : null;
		String system = element.hasChild("system") ? element.getNamedChild("system").getValue() : null;
		String code = element.hasChild("code") ? element.getNamedChild("code").getValue() : null;
		if (!Utilities.noString(value) && definition.hasExtension("http://hl7.org/fhir/StructureDefinition/maxDecimalPlaces")) {
			int dp = value.contains(".") ? value.substring(value.indexOf(".") + 1).length() : 0;
			int def = Integer.parseInt(ToolingExtensions.readStringExtension(definition, "http://hl7.org/fhir/StructureDefinition/maxDecimalPlaces"));
			ok = this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.STRUCTURE,
				element.line(),
				element.col(),
				path,
				dp <= def,
				"TYPE_SPECIFIC_CHECKS_DT_DECIMAL_CHARS",
				new Object[]{dp, def}
			)
				&& ok;
		}

		if (system != null || code != null) {
			ok = this.checkCodedElement(errors, path, element, theProfile, definition, false, false, theStack, code, system, null, unit) && ok;
		}

		if (code != null && "http://unitsofmeasure.org".equals(system)) {
			int b = code.indexOf("{");
			int e = code.indexOf("}");
			if (b >= 0 && e > 0 && b < e) {
				ok = this.bpCheck(
					errors,
					IssueType.BUSINESSRULE,
					element.line(),
					element.col(),
					path,
					!code.contains("{"),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_NO_ANNOTATIONS",
					code.substring(b, e + 1)
				)
					&& ok;
			}
		}

		if (definition.hasMinValue()
			&& this.warning(
			errors,
			NO_RULE_DATE,
			IssueType.INVALID,
			element.line(),
			element.col(),
			path,
			!Utilities.noString(value),
			"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_VALUE_NO_VALUE",
			new Object[0]
		)) {
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.INVALID,
				element.line(),
				element.col(),
				path,
				definition.getMinValue() instanceof Quantity,
				"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_NO_QTY",
				new Object[]{definition.getMinValue().fhirType()}
			)) {
				Quantity min = definition.getMinValueQuantity();
				if (this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					!Utilities.noString(min.getSystem()),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_MIN_NO_SYSTEM",
					new Object[0]
				)
					&& this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					!Utilities.noString(system),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_VALUE_NO_SYSTEM",
					new Object[0]
				)
					&& this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					system.equals(min.getSystem()),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_SYSTEM_MISMATCH",
					new Object[]{system, min.getSystem()}
				)
					&& this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					!Utilities.noString(min.getCode()),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_MIN_NO_CODE",
					new Object[0]
				)
					&& this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					!Utilities.noString(code),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_VALUE_NO_CODE",
					new Object[0]
				)) {
					if (this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						element.line(),
						element.col(),
						path,
						definition.getMinValueQuantity().hasValue(),
						"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_MIN_NO_VALUE",
						new Object[0]
					)) {
						if (code.equals(min.getCode())) {
							ok = this.rule(
								errors,
								NO_RULE_DATE,
								IssueType.INVALID,
								element.line(),
								element.col(),
								path,
								this.checkDecimalMinValue(value, min.getValue()),
								"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_VALUE_WRONG",
								new Object[]{value, min.getValue().toString()}
							)
								&& ok;
						} else if ("http://unitsofmeasure.org".equals(system)) {
							if (this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.INVALID,
								element.line(),
								element.col(),
								path,
								this.context.getUcumService() != null,
								"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_NO_UCUM_SVC",
								new Object[0]
							)) {
								Decimal v = this.convertUcumValue(value, code, min.getCode());
								if (this.rule(
									errors,
									NO_RULE_DATE,
									IssueType.INVALID,
									element.line(),
									element.col(),
									path,
									v != null,
									"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_MIN_NO_CONVERT",
									new Object[]{value, code, min.getCode()}
								)) {
									ok = this.rule(
										errors,
										NO_RULE_DATE,
										IssueType.INVALID,
										element.line(),
										element.col(),
										path,
										this.checkDecimalMinValue(v, min.getValue()),
										"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_VALUE_WRONG_UCUM",
										new Object[]{value, code, min.getValue().toString(), min.getCode()}
									)
										&& ok;
								} else {
									ok = false;
								}
							}
						} else {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.INVALID,
								element.line(),
								element.col(),
								path,
								false,
								"TYPE_SPECIFIC_CHECKS_DT_QTY_MIN_CODE_MISMATCH",
								new Object[]{code, min.getCode()}
							);
						}
					} else {
						ok = false;
					}
				}
			} else {
				ok = false;
			}
		}

		if (definition.hasMaxValue()
			&& this.warning(
			errors,
			NO_RULE_DATE,
			IssueType.INVALID,
			element.line(),
			element.col(),
			path,
			!Utilities.noString(value),
			"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_VALUE_NO_VALUE",
			new Object[0]
		)) {
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.INVALID,
				element.line(),
				element.col(),
				path,
				definition.getMaxValue() instanceof Quantity,
				"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_NO_QTY",
				new Object[]{definition.getMaxValue().fhirType()}
			)) {
				Quantity max = definition.getMaxValueQuantity();
				if (this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					!Utilities.noString(max.getSystem()),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_MIN_NO_SYSTEM",
					new Object[0]
				)
					&& this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					!Utilities.noString(system),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_VALUE_NO_SYSTEM",
					new Object[0]
				)
					&& this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					system.equals(max.getSystem()),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_SYSTEM_MISMATCH",
					new Object[]{system, max.getSystem()}
				)
					&& this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					!Utilities.noString(max.getCode()),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_MIN_NO_CODE",
					new Object[0]
				)
					&& this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					element.line(),
					element.col(),
					path,
					!Utilities.noString(code),
					"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_VALUE_NO_CODE",
					new Object[0]
				)) {
					if (this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						element.line(),
						element.col(),
						path,
						definition.getMaxValueQuantity().hasValue(),
						"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_MIN_NO_VALUE",
						new Object[0]
					)) {
						if (code.equals(max.getCode())) {
							ok = this.rule(
								errors,
								NO_RULE_DATE,
								IssueType.INVALID,
								element.line(),
								element.col(),
								path,
								this.checkDecimalMaxValue(value, max.getValue()),
								"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_VALUE_WRONG",
								new Object[]{value, max.getValue().toString()}
							)
								&& ok;
						} else if ("http://unitsofmeasure.org".equals(system)) {
							if (this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.INVALID,
								element.line(),
								element.col(),
								path,
								this.context.getUcumService() != null,
								"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_NO_UCUM_SVC",
								new Object[0]
							)) {
								Decimal v = this.convertUcumValue(value, code, max.getCode());
								if (this.rule(
									errors,
									NO_RULE_DATE,
									IssueType.INVALID,
									element.line(),
									element.col(),
									path,
									v != null,
									"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_MIN_NO_CONVERT",
									new Object[]{value, code, max.getCode()}
								)) {
									ok = this.rule(
										errors,
										NO_RULE_DATE,
										IssueType.INVALID,
										element.line(),
										element.col(),
										path,
										this.checkDecimalMaxValue(v, max.getValue()),
										"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_VALUE_WRONG_UCUM",
										new Object[]{value, code, max.getValue().toString(), max.getCode()}
									)
										&& ok;
								} else {
									ok = false;
								}
							}
						} else {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.INVALID,
								element.line(),
								element.col(),
								path,
								false,
								"TYPE_SPECIFIC_CHECKS_DT_QTY_MAX_CODE_MISMATCH",
								new Object[]{code, max.getCode()}
							);
						}
					} else {
						ok = false;
					}
				}
			} else {
				ok = false;
			}
		}

		return ok;
	}

	private Decimal convertUcumValue(String value, String code, String minCode) {
		try {
			Decimal v = new Decimal(value);
			return this.context.getUcumService().convert(v, code, minCode);
		} catch (Exception var5) {
			return null;
		}
	}

	private boolean checkDecimalMaxValue(Decimal value, BigDecimal min) {
		try {
			Decimal m = new Decimal(min.toString());
			return value.comparesTo(m) <= 0;
		} catch (Exception var4) {
			return false;
		}
	}

	private boolean checkDecimalMaxValue(String value, BigDecimal min) {
		try {
			BigDecimal v = new BigDecimal(value);
			return v.compareTo(min) <= 0;
		} catch (Exception var4) {
			return false;
		}
	}

	private boolean checkDecimalMinValue(Decimal value, BigDecimal min) {
		try {
			Decimal m = new Decimal(min.toString());
			return value.comparesTo(m) >= 0;
		} catch (Exception var4) {
			return false;
		}
	}

	private boolean checkDecimalMinValue(String value, BigDecimal min) {
		try {
			BigDecimal v = new BigDecimal(value);
			return v.compareTo(min) >= 0;
		} catch (Exception var4) {
			return false;
		}
	}

	private boolean checkAttachment(
		List<ValidationMessage> errors,
		String path,
		Element element,
		StructureDefinition theProfile,
		ElementDefinition definition,
		boolean theInCodeableConcept,
		boolean theCheckDisplayInContext,
		NodeStack theStack
	) {
		boolean ok = true;
		long size = -1L;
		String fetchError = null;
		if (element.hasChild("data")) {
			String b64 = element.getChildValue("data");
			boolean bok = this.isValidBase64(b64);
			if (bok && element.hasChild("size")) {
				size = (long)this.countBase64DecodedBytes(b64);
				String sz = element.getChildValue("size");
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					Long.toString(size).equals(sz),
					"TYPE_SPECIFIC_CHECKS_DT_ATT_SIZE_CORRECT",
					new Object[]{sz, size}
				)
					&& ok;
			}
		} else if (element.hasChild("size")) {
			String sz = element.getChildValue("size");
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.STRUCTURE,
				element.line(),
				element.col(),
				path,
				Utilities.isLong(sz),
				"TYPE_SPECIFIC_CHECKS_DT_ATT_SIZE_INVALID",
				new Object[]{sz}
			)) {
				size = Long.parseLong(sz);
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					size >= 0L,
					"TYPE_SPECIFIC_CHECKS_DT_ATT_SIZE_INVALID",
					new Object[]{sz}
				)
					&& ok;
			}
		} else if (element.hasChild("url")) {
			String url = element.getChildValue("url");
			if (definition.hasExtension("http://hl7.org/fhir/StructureDefinition/maxSize")) {
				try {
					if (!url.startsWith("http://") && !url.startsWith("https://")) {
						if (url.startsWith("file:")) {
							size = new File(url.substring(5)).length();
						} else {
							fetchError = this.context.formatMessage("TYPE_SPECIFIC_CHECKS_DT_ATT_UNKNOWN_URL_SCHEME", new Object[]{url});
						}
					} else if (this.fetcher == null) {
						fetchError = this.context.formatMessage("TYPE_SPECIFIC_CHECKS_DT_ATT_NO_FETCHER", new Object[]{url});
					} else {
						byte[] cnt = this.fetcher.fetchRaw(this, url);
						size = (long)cnt.length;
					}
				} catch (Exception var16) {
					fetchError = this.context.formatMessage("TYPE_SPECIFIC_CHECKS_DT_ATT_URL_ERROR", new Object[]{url, var16.getMessage()});
				}
			}
		}

		if (definition.hasExtension("http://hl7.org/fhir/StructureDefinition/maxSize")
			&& this.warning(errors, NO_RULE_DATE, IssueType.STRUCTURE, element.line(), element.col(), path, size >= 0L, fetchError, new Object[0])) {
			long def = Long.parseLong(ToolingExtensions.readStringExtension(definition, "http://hl7.org/fhir/StructureDefinition/maxSize"));
			ok = this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.STRUCTURE,
				element.line(),
				element.col(),
				path,
				size <= def,
				"TYPE_SPECIFIC_CHECKS_DT_ATT_TOO_LONG",
				new Object[]{size, def}
			)
				&& ok;
		}

		this.warning(
			errors,
			NO_RULE_DATE,
			IssueType.STRUCTURE,
			element.line(),
			element.col(),
			path,
			element.hasChild("data") || element.hasChild("url") || element.hasChild("contentType") || element.hasChild("language"),
			"TYPE_SPECIFIC_CHECKS_DT_ATT_NO_CONTENT",
			new Object[0]
		);
		return ok;
	}

	private boolean checkRange(List<ValidationMessage> errors, String path, Element focus, Range fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".low", focus.getNamedChild("low"), fixed.getLow(), fixedSource, "low", focus, pattern) && ok;
		return this.checkFixedValue(errors, path + ".high", focus.getNamedChild("high"), fixed.getHigh(), fixedSource, "high", focus, pattern) && ok;
	}

	private boolean checkRatio(List<ValidationMessage> errors, String path, Element focus, Ratio fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".numerator", focus.getNamedChild("numerator"), fixed.getNumerator(), fixedSource, "numerator", focus, pattern)
			&& ok;
		return this.checkFixedValue(
			errors, path + ".denominator", focus.getNamedChild("denominator"), fixed.getDenominator(), fixedSource, "denominator", focus, pattern
		)
			&& ok;
	}

	private boolean checkReference(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		String path,
		Element element,
		StructureDefinition profile,
		ElementDefinition container,
		String parentType,
		NodeStack stack,
		PercentageTracker pct,
		ValidationMode vmode
	) throws FHIRException {
		boolean ok = true;
		Reference reference = ObjectConverter.readAsReference(element);
		String ref = reference.getReference();
		if (Utilities.noString(ref)) {
			if (!path.contains("element.pattern")
				&& Utilities.noString(reference.getIdentifier().getSystem())
				&& Utilities.noString(reference.getIdentifier().getValue())) {
				this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					!Utilities.noString(element.getNamedChildValue("display")),
					"Reference_REF_NoDisplay",
					new Object[0]
				);
			}

			return true;
		} else if (Utilities.existsInList(ref, new String[]{"http://tools.ietf.org/html/bcp47"})) {
			return true;
		} else {
			this.warning(
				errors,
				NO_RULE_DATE,
				IssueType.STRUCTURE,
				element.line(),
				element.col(),
				path,
				!this.isSuspiciousReference(ref),
				"REFERENCE_REF_SUSPICIOUS",
				new Object[]{ref}
			);
			ResolvedReference we = this.localResolve(ref, stack, errors, path, hostContext.getRootResource(), hostContext.getGroupingResource(), element);
			String refType;
			if (ref.startsWith("#")) {
				refType = "contained";
			} else if (we == null) {
				refType = "remote";
			} else {
				refType = "bundled";
			}

			boolean conditional = ref.contains("?") && this.context.getResourceNamesAsSet().contains(ref.substring(0, ref.indexOf("?")));
			ReferenceValidationPolicy pol;
			if (refType.equals("contained") || refType.equals("bundled")) {
				pol = ReferenceValidationPolicy.CHECK_VALID;
			} else if (this.policyAdvisor == null) {
				pol = ReferenceValidationPolicy.IGNORE;
			} else {
				pol = this.policyAdvisor.policyForReference(this, hostContext.getAppContext(), path, ref);
			}

			if (conditional) {
				String query = ref.substring(ref.indexOf("?"));
				boolean test = !Utilities.noString(query) && query.matches("\\?([_a-zA-Z][_a-zA-Z0-9]*=[^=&]*)(&([_a-zA-Z][_a-zA-Z0-9]*=[^=&]*))*");
				ok = this.rule(errors, "2023-02-20", IssueType.INVALID, element.line(), element.col(), path, test, "REFERENCE_REF_QUERY_INVALID", new Object[]{ref})
					&& ok;
			} else if (pol.checkExists()) {
				if (we == null && !refType.equals("contained")) {
					if (this.fetcher == null) {
						throw new FHIRException(this.context.formatMessage("Resource_resolution_services_not_provided", new Object[0]));
					}

					Element ext = null;
					if (this.fetchCache.containsKey(ref)) {
						ext = (Element)this.fetchCache.get(ref);
					} else {
						try {
							ext = this.fetcher.fetch(this, hostContext.getAppContext(), ref);
						} catch (IOException var30) {
							throw new FHIRException(var30);
						}

						if (ext != null) {
							setParents(ext);
							this.fetchCache.put(ref, ext);
						}
					}

					we = ext == null ? null : this.makeExternalRef(ext, path);
				}

				boolean rok = this.allowExamples && (ref.contains("example.org") || ref.contains("acme.com"))
					|| we != null
					|| pol == ReferenceValidationPolicy.CHECK_TYPE_IF_EXISTS;
				ok = this.rule(errors, NO_RULE_DATE, IssueType.STRUCTURE, element.line(), element.col(), path, rok, "Reference_REF_CantResolve", new Object[]{ref})
					&& ok;
			}

			String ft;
			if (we != null) {
				ft = we.getType();
			} else {
				ft = this.tryParse(ref);
			}

			if (reference.hasType()) {
				String tu = this.isAbsolute(reference.getType()) ? reference.getType() : "http://hl7.org/fhir/StructureDefinition/" + reference.getType();
				TypeRefComponent containerType = container.getType("Reference");
				if (!containerType.hasTargetProfile(tu)
					&& !containerType.hasTargetProfile("http://hl7.org/fhir/StructureDefinition/Resource")
					&& !containerType.getTargetProfile().isEmpty()) {
					boolean matchingResource = false;

					for(CanonicalType target : containerType.getTargetProfile()) {
						StructureDefinition sd = this.resolveProfile(profile, target.asStringValue());
						if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.NOTFOUND,
							element.line(),
							element.col(),
							path,
							sd != null,
							"Reference_REF_CantResolveProfile",
							new Object[]{target.asStringValue()}
						)) {
							if (("http://hl7.org/fhir/StructureDefinition/" + sd.getType()).equals(tu)) {
								matchingResource = true;
								break;
							}
						} else {
							ok = false;
						}
					}

					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.STRUCTURE,
						element.line(),
						element.col(),
						path,
						matchingResource,
						"Reference_REF_WrongTarget",
						new Object[]{reference.getType(), container.getType("Reference").getTargetProfile()}
					)
						&& ok;
				}

				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					ft == null || ft.equals(reference.getType()),
					"Reference_REF_BadTargetType",
					new Object[]{reference.getType(), ft}
				)
					&& ok;
			}

			if (we != null
				&& pol.checkType()
				&& this.warning(errors, NO_RULE_DATE, IssueType.STRUCTURE, element.line(), element.col(), path, ft != null, "Reference_REF_NoType", new Object[0])) {
				boolean rok = false;
				TypeRefComponent type = this.getReferenceTypeRef(container.getType());
				if (type.hasTargetProfile() && !type.hasTargetProfile("http://hl7.org/fhir/StructureDefinition/Resource")) {
					Set<String> types = new HashSet();
					List<StructureDefinition> profiles = new ArrayList();

					for(UriType u : type.getTargetProfile()) {
						StructureDefinition sd = this.resolveProfile(profile, (String)u.getValue());
						if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							element.line(),
							element.col(),
							path,
							sd != null,
							"Reference_REF_CantResolveProfile",
							new Object[]{u.getValue()}
						)) {
							types.add(sd.getType());
							if (ft.equals(sd.getType())) {
								rok = true;
								profiles.add(sd);
							}
						} else {
							ok = false;
						}
					}

					if (!pol.checkValid()) {
						ok = this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							element.line(),
							element.col(),
							path,
							profiles.size() > 0,
							"Reference_REF_CantMatchType",
							new Object[]{ref, StringUtils.join(new Object[]{"; ", type.getTargetProfile()})}
						)
							&& ok;
					} else {
						Map<StructureDefinition, List<ValidationMessage>> badProfiles = new HashMap();
						Map<StructureDefinition, List<ValidationMessage>> goodProfiles = new HashMap();
						int goodCount = 0;

						for(StructureDefinition pr : profiles) {
							List<ValidationMessage> profileErrors = new ArrayList();
							this.validateResource(
								we.hostContext(hostContext, pr),
								profileErrors,
								we.getResource(),
								we.getFocus(),
								pr,
								IdStatus.OPTIONAL,
								we.getStack().resetIds(),
								pct,
								vmode.withReason(ValidationReason.MatchingSlice)
							);
							if (!this.hasErrors(profileErrors)) {
								++goodCount;
								goodProfiles.put(pr, profileErrors);
								this.trackUsage(pr, hostContext, element);
							} else {
								badProfiles.put(pr, profileErrors);
							}
						}

						if (goodCount == 1) {
							if (this.showMessagesFromReferences) {
								for(ValidationMessage vm : (List<ValidationMessage>)goodProfiles.values().iterator().next()) {
									if (!errors.contains(vm)) {
										errors.add(vm);
										ok = false;
									}
								}
							}
						} else if (goodProfiles.size() == 0) {
							if (!this.isShowMessagesFromReferences()) {
								ok = this.rule(
									errors,
									NO_RULE_DATE,
									IssueType.STRUCTURE,
									element.line(),
									element.col(),
									path,
									this.areAllBaseProfiles(profiles),
									"Reference_REF_CantMatchChoice",
									new Object[]{ref, this.asList(type.getTargetProfile())}
								)
									&& ok;

								for(StructureDefinition sd : badProfiles.keySet()) {
									this.slicingHint(
										errors,
										NO_RULE_DATE,
										IssueType.STRUCTURE,
										element.line(),
										element.col(),
										path,
										false,
										false,
										this.context.formatMessage("Details_for__matching_against_Profile_", new Object[]{ref, sd.getVersionedUrl()}),
										this.errorSummaryForSlicingAsHtml((List<ValidationMessage>)badProfiles.get(sd)),
										this.errorSummaryForSlicingAsText((List<ValidationMessage>)badProfiles.get(sd))
									);
								}
							} else {
								ok = this.rule(
									errors,
									NO_RULE_DATE,
									IssueType.STRUCTURE,
									element.line(),
									element.col(),
									path,
									profiles.size() == 1,
									"Reference_REF_CantMatchChoice",
									new Object[]{ref, this.asList(type.getTargetProfile())}
								)
									&& ok;

								for(List<ValidationMessage> messages : badProfiles.values()) {
									for(ValidationMessage vm : messages) {
										if (!errors.contains(vm)) {
											errors.add(vm);
											ok = false;
										}
									}
								}
							}
						} else if (!this.isShowMessagesFromReferences()) {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.STRUCTURE,
								element.line(),
								element.col(),
								path,
								false,
								"Reference_REF_MultipleMatches",
								new Object[]{ref, this.asListByUrl(goodProfiles.keySet())}
							);

							for(StructureDefinition sd : badProfiles.keySet()) {
								this.slicingHint(
									errors,
									NO_RULE_DATE,
									IssueType.STRUCTURE,
									element.line(),
									element.col(),
									path,
									false,
									false,
									this.context.formatMessage("Details_for__matching_against_Profile_", new Object[]{ref, sd.getVersionedUrl()}),
									this.errorSummaryForSlicingAsHtml((List<ValidationMessage>)badProfiles.get(sd)),
									this.errorSummaryForSlicingAsText((List<ValidationMessage>)badProfiles.get(sd))
								);
							}
						} else {
							this.warning(
								errors,
								NO_RULE_DATE,
								IssueType.STRUCTURE,
								element.line(),
								element.col(),
								path,
								false,
								"Reference_REF_MultipleMatches",
								new Object[]{ref, this.asListByUrl(goodProfiles.keySet())}
							);

							for(List<ValidationMessage> messages : goodProfiles.values()) {
								for(ValidationMessage vm : messages) {
									if (!errors.contains(vm)) {
										errors.add(vm);
										ok = false;
									}
								}
							}
						}
					}

					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.STRUCTURE,
						element.line(),
						element.col(),
						path,
						rok,
						"Reference_REF_BadTargetType",
						new Object[]{ft, types.toString()}
					)
						&& ok;
				}

				if (type.hasAggregation() && !this.noCheckAggregation) {
					boolean modeOk = false;
					CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

					for(Enumeration<AggregationMode> mode : type.getAggregation()) {
						b.append(mode.getCode());
						if (((AggregationMode)mode.getValue()).equals(AggregationMode.CONTAINED) && refType.equals("contained")) {
							modeOk = true;
						} else if (((AggregationMode)mode.getValue()).equals(AggregationMode.BUNDLED) && refType.equals("bundled")) {
							modeOk = true;
						} else if (((AggregationMode)mode.getValue()).equals(AggregationMode.REFERENCED) && (refType.equals("bundled") || refType.equals("remote"))) {
							modeOk = true;
						}
					}

					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.STRUCTURE,
						element.line(),
						element.col(),
						path,
						modeOk,
						"Reference_REF_Aggregation",
						new Object[]{refType, b.toString()}
					)
						&& ok;
				}
			}

			if (we == null) {
				TypeRefComponent type = this.getReferenceTypeRef(container.getType());
				boolean okToRef = !type.hasAggregation() || type.hasAggregation(AggregationMode.REFERENCED);
				ok = this.rule(errors, NO_RULE_DATE, IssueType.REQUIRED, -1, -1, path, okToRef, "Reference_REF_NotFound_Bundle", new Object[]{ref}) && ok;
			}

			if (we == null && ft != null && this.assumeValidRestReferences) {
				TypeRefComponent type = this.getReferenceTypeRef(container.getType());
				Set<String> types = new HashSet();
				StructureDefinition sdFT = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + ft);
				boolean rok = false;

				for(CanonicalType tp : type.getTargetProfile()) {
					StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, (String)tp.getValue(), profile);
					if (sd != null) {
						types.add(sd.getType());

						for(StructureDefinition sdF = sdFT;
							 sdF != null;
							 sdF = sdF.hasBaseDefinition() ? (StructureDefinition)this.context.fetchResource(StructureDefinition.class, sdF.getBaseDefinition(), sdF) : null
						) {
							if (sdF.getType().equals(sd.getType())) {
								rok = true;
								break;
							}
						}
					}
				}

				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					types.isEmpty() || rok,
					"Reference_REF_BadTargetType2",
					new Object[]{ft, ref, types}
				)
					&& ok;
			}

			if (pol == ReferenceValidationPolicy.CHECK_VALID) {
			}

			return ok;
		}
	}

	private boolean isSuspiciousReference(String url) {
		if (this.assumeValidRestReferences && url != null && !Utilities.isAbsoluteUrl(url) && !url.startsWith("#")) {
			String[] parts = url.split("\\/");
			if (parts.length == 2 && this.context.getResourceNames().contains(parts[0]) && Utilities.isValidId(parts[1])) {
				return false;
			} else {
				return parts.length != 4
					|| !this.context.getResourceNames().contains(parts[0])
					|| !Utilities.isValidId(parts[1])
					|| !"_history".equals(parts[2])
					|| !Utilities.isValidId(parts[3]);
			}
		} else {
			return false;
		}
	}

	private String asListByUrl(Collection<StructureDefinition> coll) {
		List<StructureDefinition> list = new ArrayList();
		list.addAll(coll);
		Collections.sort(list, new InstanceValidator.StructureDefinitionSorterByUrl());
		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

		for(StructureDefinition sd : list) {
			b.append(sd.getUrl());
		}

		return b.toString();
	}

	private String asList(Collection<CanonicalType> coll) {
		List<CanonicalType> list = new ArrayList();
		list.addAll(coll);
		Collections.sort(list, new InstanceValidator.CanonicalTypeSorter());
		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

		for(CanonicalType c : list) {
			b.append((String)c.getValue());
		}

		return b.toString();
	}

	private boolean areAllBaseProfiles(List<StructureDefinition> profiles) {
		for(StructureDefinition sd : profiles) {
			if (!sd.getUrl().startsWith("http://hl7.org/fhir/StructureDefinition/")) {
				return false;
			}
		}

		return true;
	}

	private String errorSummaryForSlicing(List<ValidationMessage> list) {
		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

		for(ValidationMessage vm : list) {
			if (vm.getLevel() == IssueSeverity.ERROR || vm.getLevel() == IssueSeverity.FATAL || vm.isSlicingHint()) {
				b.append(vm.getLocation() + ": " + vm.getMessage());
			}
		}

		return b.toString();
	}

	private String errorSummaryForSlicingAsHtml(List<ValidationMessage> list) {
		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

		for(ValidationMessage vm : list) {
			if (vm.isSlicingHint()) {
				b.append("<li>" + vm.getLocation() + ": " + vm.getSliceHtml() + "</li>");
			} else if (vm.getLevel() == IssueSeverity.ERROR || vm.getLevel() == IssueSeverity.FATAL) {
				b.append("<li>" + vm.getLocation() + ": " + vm.getHtml() + "</li>");
			}
		}

		return "<ul>" + b.toString() + "</ul>";
	}

	private boolean isCritical(List<ValidationMessage> list) {
		for(ValidationMessage vm : list) {
			if (vm.isSlicingHint() && vm.isCriticalSignpost()) {
				return true;
			}
		}

		return false;
	}

	private String[] errorSummaryForSlicingAsText(List<ValidationMessage> list) {
		List<String> res = new ArrayList();

		for(ValidationMessage vm : list) {
			if (vm.isSlicingHint()) {
				if (vm.sliceText != null) {
					for(String s : vm.sliceText) {
						res.add(vm.getLocation() + ": " + s);
					}
				} else {
					res.add(vm.getLocation() + ": " + vm.getMessage());
				}
			} else if (vm.getLevel() == IssueSeverity.ERROR || vm.getLevel() == IssueSeverity.FATAL) {
				res.add(vm.getLocation() + ": " + vm.getHtml());
			}
		}

		return (String[])res.toArray(new String[0]);
	}

	private TypeRefComponent getReferenceTypeRef(List<TypeRefComponent> types) {
		for(TypeRefComponent tr : types) {
			if ("Reference".equals(tr.getCode())) {
				return tr;
			}
		}

		return null;
	}

	private String checkResourceType(String type) {
		long t = System.nanoTime();

		String var4;
		try {
			if (this.context.fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + type) == null) {
				return null;
			}

			var4 = type;
		} finally {
			this.timeTracker.sd(t);
		}

		return var4;
	}

	private boolean checkSampledData(List<ValidationMessage> errors, String path, Element focus, SampledData fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".origin", focus.getNamedChild("origin"), fixed.getOrigin(), fixedSource, "origin", focus, pattern) && ok;
		if (VersionUtilities.isR5VerOrLater(this.context.getVersion())) {
			ok = this.checkFixedValue(errors, path + ".interval", focus.getNamedChild("period"), fixed.getIntervalElement(), fixedSource, "interval", focus, pattern)
				&& ok;
			ok = this.checkFixedValue(
				errors, path + ".intervalUnit", focus.getNamedChild("period"), fixed.getIntervalUnitElement(), fixedSource, "intervalUnit", focus, pattern
			)
				&& ok;
		} else {
			ok = this.checkFixedValue(errors, path + ".period", focus.getNamedChild("period"), fixed.getIntervalElement(), fixedSource, "period", focus, pattern)
				&& ok;
		}

		ok = this.checkFixedValue(errors, path + ".factor", focus.getNamedChild("factor"), fixed.getFactorElement(), fixedSource, "factor", focus, pattern) && ok;
		ok = this.checkFixedValue(
			errors, path + ".lowerLimit", focus.getNamedChild("lowerLimit"), fixed.getLowerLimitElement(), fixedSource, "lowerLimit", focus, pattern
		)
			&& ok;
		ok = this.checkFixedValue(
			errors, path + ".upperLimit", focus.getNamedChild("upperLimit"), fixed.getUpperLimitElement(), fixedSource, "upperLimit", focus, pattern
		)
			&& ok;
		ok = this.checkFixedValue(
			errors, path + ".dimensions", focus.getNamedChild("dimensions"), fixed.getDimensionsElement(), fixedSource, "dimensions", focus, pattern
		)
			&& ok;
		return this.checkFixedValue(errors, path + ".data", focus.getNamedChild("data"), fixed.getDataElement(), fixedSource, "data", focus, pattern) && ok;
	}

	private boolean checkReference(List<ValidationMessage> errors, String path, Element focus, Reference fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(
			errors, path + ".reference", focus.getNamedChild("reference"), fixed.getReferenceElement_(), fixedSource, "reference", focus, pattern
		)
			&& ok;
		ok = this.checkFixedValue(errors, path + ".type", focus.getNamedChild("type"), fixed.getTypeElement(), fixedSource, "type", focus, pattern) && ok;
		ok = this.checkFixedValue(errors, path + ".identifier", focus.getNamedChild("identifier"), fixed.getIdentifier(), fixedSource, "identifier", focus, pattern)
			&& ok;
		return this.checkFixedValue(errors, path + ".display", focus.getNamedChild("display"), fixed.getDisplayElement(), fixedSource, "display", focus, pattern)
			&& ok;
	}

	private boolean checkTiming(List<ValidationMessage> errors, String path, Element focus, Timing fixed, String fixedSource, boolean pattern) {
		boolean ok = true;
		ok = this.checkFixedValue(errors, path + ".repeat", focus.getNamedChild("repeat"), fixed.getRepeat(), fixedSource, "value", focus, pattern) && ok;
		List<Element> events = new ArrayList();
		focus.getNamedChildren("event", events);
		if (this.rule(
			errors,
			NO_RULE_DATE,
			IssueType.VALUE,
			focus.line(),
			focus.col(),
			path,
			events.size() == fixed.getEvent().size(),
			"Bundle_MSG_Event_Count",
			new Object[]{Integer.toString(fixed.getEvent().size()), Integer.toString(events.size())}
		)) {
			for(int i = 0; i < events.size(); ++i) {
				ok = this.checkFixedValue(
					errors, path + ".event", (Element)events.get(i), (org.hl7.fhir.r5.model.Element)fixed.getEvent().get(i), fixedSource, "event", focus, pattern
				)
					&& ok;
			}
		}

		return ok;
	}

	private boolean codeinExpansion(ValueSetExpansionContainsComponent cnt, String system, String code) {
		for(ValueSetExpansionContainsComponent c : cnt.getContains()) {
			if (code.equals(c.getCode()) && system.equals(c.getSystem().toString())) {
				return true;
			}

			if (this.codeinExpansion(c, system, code)) {
				return true;
			}
		}

		return false;
	}

	private boolean codeInExpansion(ValueSet vs, String system, String code) {
		for(ValueSetExpansionContainsComponent c : vs.getExpansion().getContains()) {
			if (code.equals(c.getCode()) && (system == null || system.equals(c.getSystem()))) {
				return true;
			}

			if (this.codeinExpansion(c, system, code)) {
				return true;
			}
		}

		return false;
	}

	private String describeReference(String reference, CanonicalResource target) {
		if (reference == null && target == null) {
			return "null";
		} else if (reference == null) {
			return target.getVersionedUrl();
		} else if (target == null) {
			return reference;
		} else {
			String uref = reference.contains("|") ? reference.substring(0, reference.lastIndexOf("|")) : reference;
			String vref = reference.contains("|") ? reference.substring(reference.lastIndexOf("|") + 1) : null;
			return !uref.equals(target.getUrl()) || vref != null && !vref.equals(target.getVersion())
				? reference + "(which actually refers to '" + target.present() + "' (" + target.getVersionedUrl() + "))"
				: "'" + target.present() + "' (" + target.getVersionedUrl() + ")";
		}
	}

	private String describeTypes(List<TypeRefComponent> types) {
		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

		for(TypeRefComponent t : types) {
			b.append(t.getWorkingCode());
		}

		return b.toString();
	}

	protected ElementDefinition findElement(StructureDefinition profile, String name) {
		for(ElementDefinition c : profile.getSnapshot().getElement()) {
			if (c.getPath().equals(name)) {
				return c;
			}
		}

		return null;
	}

	public BestPracticeWarningLevel getBestPracticeWarningLevel() {
		return this.bpWarnings;
	}

	public CheckDisplayOption getCheckDisplay() {
		return this.checkDisplay;
	}

	private ConceptDefinitionComponent getCodeDefinition(ConceptDefinitionComponent c, String code) {
		if (code.equals(c.getCode())) {
			return c;
		} else {
			for(ConceptDefinitionComponent g : c.getConcept()) {
				ConceptDefinitionComponent r = this.getCodeDefinition(g, code);
				if (r != null) {
					return r;
				}
			}

			return null;
		}
	}

	private ConceptDefinitionComponent getCodeDefinition(CodeSystem cs, String code) {
		for(ConceptDefinitionComponent c : cs.getConcept()) {
			ConceptDefinitionComponent r = this.getCodeDefinition(c, code);
			if (r != null) {
				return r;
			}
		}

		return null;
	}

	private IndexedElement getContainedById(Element container, String id) {
		List<Element> contained = new ArrayList();
		container.getNamedChildren("contained", contained);

		for(int i = 0; i < contained.size(); ++i) {
			Element we = (Element)contained.get(i);
			if (id.equals(we.getNamedChildValue("id"))) {
				return new IndexedElement(i, we, null);
			}
		}

		return null;
	}

	public IWorkerContext getContext() {
		return this.context;
	}

	private List<ElementDefinition> getCriteriaForDiscriminator(
		String path, ElementDefinition element, String discriminator, StructureDefinition profile, boolean removeResolve, StructureDefinition srcProfile
	) throws FHIRException {
		List<ElementDefinition> elements = new ArrayList();
		if ("value".equals(discriminator) && element.hasFixed()) {
			elements.add(element);
			return elements;
		} else {
			boolean dontFollowReference = false;
			if (removeResolve) {
				if (discriminator.equals("resolve()")) {
					elements.add(element);
					return elements;
				}

				if (discriminator.endsWith(".resolve()")) {
					discriminator = discriminator.substring(0, discriminator.length() - 10);
					dontFollowReference = true;
				}
			}

			TypedElementDefinition ted = null;
			String fp = FHIRPathExpressionFixer.fixExpr(discriminator, null, this.context.getVersion());
			ExpressionNode expr = null;

			try {
				expr = this.fpe.parse(fp);
			} catch (Exception var22) {
				throw new FHIRException(this.context.formatMessage("DISCRIMINATOR_BAD_PATH", new Object[]{var22.getMessage(), fp}), var22);
			}

			long t2 = System.nanoTime();
			ted = this.fpe.evaluateDefinition(expr, profile, new TypedElementDefinition(element), srcProfile, dontFollowReference);
			this.timeTracker.sd(t2);
			if (ted != null) {
				elements.add(ted.getElement());
			}

			for(TypeRefComponent type : element.getType()) {
				for(CanonicalType p : type.getProfile()) {
					String id = p.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-profile-element")
						? p.getExtensionString("http://hl7.org/fhir/StructureDefinition/elementdefinition-profile-element")
						: null;
					StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, (String)p.getValue(), profile);
					// PATCH: https://github.com/ahdis/matchbox/issues/138 try to check if it is a cross version extension
					if (sd == null) {
						sd = getXverExt(new ArrayList<ValidationMessage>(), "", null, p.getValue());
					}
					if (sd == null) {
						throw new DefinitionException(this.context.formatMessage("Unable_to_resolve_profile_", new Object[]{p}));
					}

					profile = sd;
					if (id == null) {
						element = sd.getSnapshot().getElementFirstRep();
					} else {
						element = null;

						for(ElementDefinition t : sd.getSnapshot().getElement()) {
							if (id.equals(t.getId())) {
								element = t;
							}
						}

						if (element == null) {
							throw new DefinitionException(this.context.formatMessage("Unable_to_resolve_element__in_profile_", new Object[]{id, p}));
						}
					}

					expr = this.fpe.parse(fp);
					t2 = System.nanoTime();
					ted = this.fpe.evaluateDefinition(expr, sd, new TypedElementDefinition(element), srcProfile, dontFollowReference);
					this.timeTracker.sd(t2);
					if (ted != null) {
						elements.add(ted.getElement());
					}
				}
			}

			return elements;
		}
	}

	private Element getExtensionByUrl(List<Element> extensions, String urlSimple) {
		for(Element e : extensions) {
			if (urlSimple.equals(e.getNamedChildValue("url"))) {
				return e;
			}
		}

		return null;
	}

	public List<String> getExtensionDomains() {
		return this.extensionDomains;
	}

	public List<ImplementationGuide> getImplementationGuides() {
		return this.igs;
	}

	private StructureDefinition getProfileForType(String type, List<TypeRefComponent> list, Resource src) {
		for(TypeRefComponent tr : list) {
			String url = tr.getWorkingCode();
			if (!Utilities.isAbsoluteUrl(url)) {
				url = "http://hl7.org/fhir/StructureDefinition/" + url;
			}

			long t = System.nanoTime();
			StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, url, src);
			this.timeTracker.sd(t);
			if (sd != null && (sd.getTypeTail().equals(type) || sd.getUrl().equals(type)) && sd.hasSnapshot()) {
				return sd;
			}

			if (sd.getAbstract()) {
				StructureDefinition sdt = this.context.fetchTypeDefinition(type);
				StructureDefinition tt = sdt;
        		// while (tt != null) { patch endless loop in CDA validation ahdis/matchbox#145
				if (tt != null) {
					if (tt.getBaseDefinition().equals(sd.getUrl())) {
						return sdt;
					}
				}
			}
		}

		return null;
	}

	private Element getValueForDiscriminator(
		Object appContext, List<ValidationMessage> errors, Element element, String discriminator, ElementDefinition criteria, NodeStack stack
	) throws FHIRException, IOException {
		String p = stack.getLiteralPath() + "." + element.getName();
		Element focus = element;
		String[] dlist = discriminator.split("\\.");

		for(String d : dlist) {
			if (focus.fhirType().equals("Reference") && d.equals("reference")) {
				String url = focus.getChildValue("reference");
				if (Utilities.noString(url)) {
					throw new FHIRException(
						this.context.formatMessage("No_reference_resolving_discriminator__from_", new Object[]{discriminator, element.getProperty().getName()})
					);
				}

				Element target = this.resolve(appContext, url, stack, errors, p);
				if (target == null) {
					throw new FHIRException(
						this.context
							.formatMessage(
								"Unable_to_find_resource__at__resolving_discriminator__from_", new Object[]{url, d, discriminator, element.getProperty().getName()}
							)
					);
				}

				focus = target;
			} else {
				if (d.equals("value") && focus.isPrimitive()) {
					return focus;
				}

				List<Element> children = focus.getChildren(d);
				if (children.isEmpty()) {
					throw new FHIRException(
						this.context.formatMessage("Unable_to_find__resolving_discriminator__from_", new Object[]{d, discriminator, element.getProperty().getName()})
					);
				}

				if (children.size() > 1) {
					throw new FHIRException(
						this.context
							.formatMessage(
								"Found__items_for__resolving_discriminator__from_",
								new Object[]{Integer.toString(children.size()), d, discriminator, element.getProperty().getName()}
							)
					);
				}

				focus = (Element)children.get(0);
				p = p + "." + d;
			}
		}

		return focus;
	}

	private CodeSystem getCodeSystem(String system) {
		long t = System.nanoTime();

		CodeSystem var4;
		try {
			var4 = this.context.fetchCodeSystem(system);
		} finally {
			this.timeTracker.tx(t, "cs " + system);
		}

		return var4;
	}

	private boolean hasTime(String fmt) {
		return fmt.contains("T");
	}

	private boolean hasTimeZone(String fmt) {
		return fmt.length() > 10 && (fmt.substring(10).contains("-") || fmt.substring(10).contains("+") || fmt.substring(10).contains("Z"));
	}

	private boolean isAbsolute(String uri) {
		String protocol = null;
		String tail = null;
		if (uri.contains(":")) {
			protocol = uri.substring(0, uri.indexOf(":"));
			tail = uri.substring(uri.indexOf(":") + 1);
		}

		if (Utilities.isToken(protocol)) {
			if (!"file".equals(protocol)) {
				return true;
			} else {
				return tail.startsWith("/") || tail.contains(":");
			}
		} else {
			return false;
		}
	}

	private boolean isCodeSystemReferenceValid(String uri) {
		return this.isSystemReferenceValid(uri);
	}

	private boolean isIdentifierSystemReferenceValid(String uri) {
		return this.isSystemReferenceValid(uri) || uri.startsWith("ldap:");
	}

	private boolean isSystemReferenceValid(String uri) {
		return uri.startsWith("http:") || uri.startsWith("https:") || uri.startsWith("urn:");
	}

	public boolean isAnyExtensionsAllowed() {
		return this.anyExtensionsAllowed;
	}

	public boolean isErrorForUnknownProfiles() {
		return this.errorForUnknownProfiles;
	}

	public void setErrorForUnknownProfiles(boolean errorForUnknownProfiles) {
		this.errorForUnknownProfiles = errorForUnknownProfiles;
	}

	private boolean isParametersEntry(String path) {
		String[] parts = path.split("\\.");
		return parts.length > 2
			&& parts[parts.length - 1].equals("resource")
			&& (pathEntryHasName(parts[parts.length - 2], "parameter") || pathEntryHasName(parts[parts.length - 2], "part"));
	}

	private boolean isBundleEntry(String path) {
		String[] parts = path.split("\\.");
		return parts.length > 2 && parts[parts.length - 1].equals("resource") && pathEntryHasName(parts[parts.length - 2], "entry");
	}

	private boolean isBundleOutcome(String path) {
		String[] parts = path.split("\\.");
		return parts.length > 2 && parts[parts.length - 1].equals("outcome") && pathEntryHasName(parts[parts.length - 2], "response");
	}

	private static boolean pathEntryHasName(String thePathEntry, String theName) {
		if (thePathEntry.equals(theName)) {
			return true;
		} else {
			return thePathEntry.length() >= theName.length() + 3 && thePathEntry.startsWith(theName) && thePathEntry.charAt(theName.length()) == '[';
		}
	}

	public boolean isPrimitiveType(String code) {
		StructureDefinition sd = this.context.fetchTypeDefinition(code);
		return sd != null && sd.getKind() == StructureDefinitionKind.PRIMITIVETYPE;
	}

	private String getErrorMessage(String message) {
		return message != null ? " (error message = " + message + ")" : "";
	}

	public boolean isSuppressLoincSnomedMessages() {
		return this.suppressLoincSnomedMessages;
	}

	private boolean nameMatches(String name, String tail) {
		return tail.endsWith("[x]") ? name.startsWith(tail.substring(0, tail.length() - 3)) : name.equals(tail);
	}

	private boolean passesCodeWhitespaceRules(String v) {
		if (!Utilities.trimWS(v).equals(v)) {
			return false;
		} else {
			boolean lastWasSpace = true;

			for(char c : v.toCharArray()) {
				if (c == ' ') {
					if (lastWasSpace) {
						return false;
					}

					lastWasSpace = true;
				} else {
					if (Utilities.isWhitespace(c)) {
						return false;
					}

					lastWasSpace = false;
				}
			}

			return true;
		}
	}

	private ResolvedReference localResolve(
		String ref, NodeStack stack, List<ValidationMessage> errors, String path, Element rootResource, Element groupingResource, Element source
	) {
		if (ref.startsWith("#")) {
			boolean wasContained = false;

			for(NodeStack nstack = stack; nstack != null && nstack.getElement() != null; nstack = nstack.getParent()) {
				if (nstack.getElement().getProperty().isResource()) {
					if (ref.equals("#") && nstack.getElement().getSpecial() != SpecialElement.CONTAINED && wasContained) {
						ResolvedReference rr = new ResolvedReference();
						rr.setResource(nstack.getElement());
						rr.setFocus(nstack.getElement());
						rr.setExternal(false);
						rr.setStack(nstack);
						return rr;
					}

					if (nstack.getElement().getSpecial() == SpecialElement.CONTAINED) {
						wasContained = true;
					}

					IndexedElement res = this.getContainedById(nstack.getElement(), ref.substring(1));
					if (res != null) {
						ResolvedReference rr = new ResolvedReference();
						rr.setResource(nstack.getElement());
						rr.setFocus(res.getMatch());
						rr.setExternal(false);
						rr.setStack(nstack.push(res.getMatch(), res.getIndex(), res.getMatch().getProperty().getDefinition(), res.getMatch().getProperty().getDefinition()));
						rr.getStack().pathComment(nstack.getElement().fhirType() + "/" + stack.getElement().getIdBase());
						return rr;
					}
				}

				if (nstack.getElement().getSpecial() == SpecialElement.BUNDLE_ENTRY || nstack.getElement().getSpecial() == SpecialElement.PARAMETER) {
					return null;
				}
			}

			if (ref.equals("#")) {
				for(Element e = stack.getElement(); e != null; e = e.getParentForValidator()) {
					if (e.getProperty().isResource() && e.getSpecial() != SpecialElement.CONTAINED) {
						ResolvedReference rr = new ResolvedReference();
						rr.setResource(e);
						rr.setFocus(e);
						rr.setExternal(false);
						rr.setStack(stack.push(e, -1, e.getProperty().getDefinition(), e.getProperty().getDefinition()));
						rr.getStack().pathComment(e.fhirType() + "/" + e.getIdBase());
						return rr;
					}
				}
			}

			return null;
		} else {
			for(String fullUrl = null; stack != null && stack.getElement() != null; stack = stack.getParent()) {
				if (stack.getElement().getSpecial() == SpecialElement.BUNDLE_ENTRY
					&& fullUrl == null
					&& stack.getParent() != null
					&& stack.getParent().getElement().getName().equals("entry")) {
					String type = stack.getParent().getParent().getElement().getChildValue("type");
					fullUrl = stack.getParent().getElement().getChildValue("fullUrl");
					if (fullUrl == null) {
						this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.REQUIRED,
							stack.getParent().getElement().line(),
							stack.getParent().getElement().col(),
							stack.getParent().getLiteralPath(),
							Utilities.existsInList(type, new String[]{"batch-response", "transaction-response"}) || fullUrl != null,
							"Bundle_BUNDLE_Entry_NoFullUrl",
							new Object[0]
						);
					}
				}

				if ("Bundle".equals(stack.getElement().getType())) {
					String type = stack.getElement().getChildValue("type");
					IndexedElement res = this.getFromBundle(stack.getElement(), ref, fullUrl, errors, path, type, "transaction".equals(type));
					if (res == null) {
						return null;
					}

					ResolvedReference rr = new ResolvedReference();
					rr.setResource(res.getMatch());
					rr.setFocus(res.getMatch());
					rr.setExternal(false);
					rr.setStack(
						stack.push(res.getEntry(), res.getIndex(), res.getEntry().getProperty().getDefinition(), res.getEntry().getProperty().getDefinition())
							.push(res.getMatch(), -1, res.getMatch().getProperty().getDefinition(), res.getMatch().getProperty().getDefinition())
					);
					rr.getStack().pathComment(rr.getResource().fhirType() + "/" + rr.getResource().getIdBase());
					return rr;
				}

				if (stack.getElement().getSpecial() == SpecialElement.PARAMETER && stack.getParent() != null) {
					NodeStack tgt = this.findInParams(stack.getParent().getParent(), ref);
					if (tgt != null) {
						ResolvedReference rr = new ResolvedReference();
						rr.setResource(tgt.getElement());
						rr.setFocus(tgt.getElement());
						rr.setExternal(false);
						rr.setStack(tgt);
						rr.getStack().pathComment(tgt.getElement().fhirType() + "/" + tgt.getElement().getIdBase());
						return rr;
					}
				}
			}

			if (groupingResource != null && "Bundle".equals(groupingResource.fhirType())) {
				String type = groupingResource.getChildValue("type");
				Element entry = this.getEntryForSource(groupingResource, source);
				String var13 = entry.getChildValue("fullUrl");
				IndexedElement res = this.getFromBundle(groupingResource, ref, var13, errors, path, type, "transaction".equals(type));
				if (res == null) {
					return null;
				} else {
					ResolvedReference rr = new ResolvedReference();
					rr.setResource(res.getMatch());
					rr.setFocus(res.getMatch());
					rr.setExternal(false);
					rr.setStack(
						new NodeStack(this.context, null, rootResource, this.validationLanguage)
							.push(res.getEntry(), res.getIndex(), res.getEntry().getProperty().getDefinition(), res.getEntry().getProperty().getDefinition())
							.push(res.getMatch(), -1, res.getMatch().getProperty().getDefinition(), res.getMatch().getProperty().getDefinition())
					);
					rr.getStack().pathComment(rr.getResource().fhirType() + "/" + rr.getResource().getIdBase());
					return rr;
				}
			} else {
				return null;
			}
		}
	}

	private NodeStack findInParams(NodeStack params, String ref) {
		int i = 0;

		for(Element child : params.getElement().getChildren("parameter")) {
			NodeStack p = params.push(child, i, child.getProperty().getDefinition(), child.getProperty().getDefinition());
			if (child.hasChild("resource")) {
				Element res = child.getNamedChild("resource");
				if ((res.fhirType() + "/" + res.getIdBase()).equals(ref)) {
					return p.push(res, -1, res.getProperty().getDefinition(), res.getProperty().getDefinition());
				}
			}

			NodeStack pc = this.findInParamParts(p, child, ref);
			if (pc != null) {
				return pc;
			}
		}

		return null;
	}

	private NodeStack findInParamParts(NodeStack pp, Element param, String ref) {
		int i = 0;

		for(Element child : param.getChildren("part")) {
			NodeStack p = pp.push(child, i, child.getProperty().getDefinition(), child.getProperty().getDefinition());
			if (child.hasChild("resource")) {
				Element res = child.getNamedChild("resource");
				if ((res.fhirType() + "/" + res.getIdBase()).equals(ref)) {
					return p.push(res, -1, res.getProperty().getDefinition(), res.getProperty().getDefinition());
				}
			}

			NodeStack pc = this.findInParamParts(p, child, ref);
			if (pc != null) {
				return pc;
			}
		}

		return null;
	}

	private Element getEntryForSource(Element bundle, Element element) {
		List<Element> entries = new ArrayList();
		bundle.getNamedChildren("entry", entries);

		for(Element entry : entries) {
			if (entry.hasDescendant(element)) {
				return entry;
			}
		}

		return null;
	}

	private ResolvedReference makeExternalRef(Element external, String path) {
		ResolvedReference res = new ResolvedReference();
		res.setResource(external);
		res.setFocus(external);
		res.setExternal(true);
		res.setStack(new NodeStack(this.context, external, path, this.validationLanguage));
		return res;
	}

	private Element resolve(Object appContext, String ref, NodeStack stack, List<ValidationMessage> errors, String path) throws IOException, FHIRException {
		Element local = this.localResolve(ref, stack, errors, path, null, null, null).getFocus();
		if (local != null) {
			return local;
		} else if (this.fetcher == null) {
			return null;
		} else if (this.fetchCache.containsKey(ref)) {
			return (Element)this.fetchCache.get(ref);
		} else {
			Element res = this.fetcher.fetch(this, appContext, ref);
			setParents(res);
			this.fetchCache.put(ref, res);
			return res;
		}
	}

	private ElementDefinition resolveNameReference(StructureDefinitionSnapshotComponent snapshot, String contentReference) {
		for(ElementDefinition ed : snapshot.getElement()) {
			if (contentReference.equals("#" + ed.getId())) {
				return ed;
			}
		}

		return null;
	}

	private StructureDefinition resolveProfile(StructureDefinition profile, String pr) {
		if (pr.startsWith("#")) {
			for(Resource r : profile.getContained()) {
				if (r.getId().equals(pr.substring(1)) && r instanceof StructureDefinition) {
					return (StructureDefinition)r;
				}
			}

			return null;
		} else {
			long t = System.nanoTime();
			StructureDefinition fr = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, pr, profile);
			this.timeTracker.sd(t);
			return fr;
		}
	}

	private ElementDefinition resolveType(String type, List<TypeRefComponent> list) {
		for(TypeRefComponent tr : list) {
			String url = tr.getWorkingCode();
			if (!Utilities.isAbsoluteUrl(url)) {
				url = "http://hl7.org/fhir/StructureDefinition/" + url;
			}

			long t = System.nanoTime();
			StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, url);
			this.timeTracker.sd(t);
			if (sd != null && (sd.getType().equals(type) || sd.getUrl().equals(type)) && sd.hasSnapshot()) {
				return (ElementDefinition)sd.getSnapshot().getElement().get(0);
			}
		}

		return null;
	}

	public void setAnyExtensionsAllowed(boolean anyExtensionsAllowed) {
		this.anyExtensionsAllowed = anyExtensionsAllowed;
	}

	public IResourceValidator setBestPracticeWarningLevel(BestPracticeWarningLevel value) {
		this.bpWarnings = value;
		return this;
	}

	public void setCheckDisplay(CheckDisplayOption checkDisplay) {
		this.checkDisplay = checkDisplay;
	}

	public void setSuppressLoincSnomedMessages(boolean suppressLoincSnomedMessages) {
		this.suppressLoincSnomedMessages = suppressLoincSnomedMessages;
	}

	public IdStatus getResourceIdRule() {
		return this.resourceIdRule;
	}

	public void setResourceIdRule(IdStatus resourceIdRule) {
		this.resourceIdRule = resourceIdRule;
	}

	public boolean isAllowXsiLocation() {
		return this.allowXsiLocation;
	}

	public void setAllowXsiLocation(boolean allowXsiLocation) {
		this.allowXsiLocation = allowXsiLocation;
	}

	private boolean sliceMatches(
		ValidatorHostContext hostContext,
		Element element,
		String path,
		ElementDefinition slicer,
		ElementDefinition ed,
		StructureDefinition profile,
		List<ValidationMessage> errors,
		List<ValidationMessage> sliceInfo,
		NodeStack stack,
		StructureDefinition srcProfile
	) throws DefinitionException, FHIRException {
		if (!slicer.getSlicing().hasDiscriminator()) {
			return false;
		} else {
			ExpressionNode n = (ExpressionNode)ed.getUserData("slice.expression.cache");
			if (n == null) {
				long t = System.nanoTime();
				StringBuilder expression = new StringBuilder("true");
				boolean anyFound = false;
				Set<String> discriminators = new HashSet();

				for(ElementDefinitionSlicingDiscriminatorComponent s : slicer.getSlicing().getDiscriminator()) {
					String discriminator = s.getPath();
					discriminators.add(discriminator);
					List<ElementDefinition> criteriaElements = this.getCriteriaForDiscriminator(
						path, ed, discriminator, profile, s.getType() == DiscriminatorType.PROFILE, srcProfile
					);
					boolean found = false;
					Iterator var22 = criteriaElements.iterator();

					while(true) {
						if (var22.hasNext()) {
							ElementDefinition criteriaElement = (ElementDefinition)var22.next();
							found = true;
							if ("0".equals(criteriaElement.getMax())) {
								expression.append(" and " + discriminator + ".empty()");
							} else if (s.getType() == DiscriminatorType.TYPE) {
								String type = null;
								if (!criteriaElement.getPath().contains("[") && discriminator.contains("[")) {
									discriminator = discriminator.substring(0, discriminator.indexOf(91));
									String lastNode = this.tail(discriminator);
									type = this.tail(criteriaElement.getPath()).substring(lastNode.length());
									type = type.substring(0, 1).toLowerCase() + type.substring(1);
								} else {
									if (criteriaElement.hasType() && criteriaElement.getType().size() != 1) {
										if (criteriaElement.getType().size() > 1) {
											throw new DefinitionException(
												this.context
													.formatMessagePlural(
														criteriaElement.getType().size(),
														"Discriminator__is_based_on_type_but_slice__in__has_multiple_types",
														new Object[]{discriminator, ed.getId(), profile.getVersionedUrl(), criteriaElement.typeSummary()}
													)
											);
										}

										throw new DefinitionException(
											this.context
												.formatMessage(
													"Discriminator__is_based_on_type_but_slice__in__has_no_types", new Object[]{discriminator, ed.getId(), profile.getVersionedUrl()}
												)
										);
									}

									if (discriminator.contains("[")) {
										discriminator = discriminator.substring(0, discriminator.indexOf(91));
									}

									if (criteriaElement.hasType()) {
										type = ((TypeRefComponent)criteriaElement.getType().get(0)).getWorkingCode();
									} else {
										if (criteriaElement.getPath().contains(".")) {
											throw new DefinitionException(
												this.context
													.formatMessage(
														"Discriminator__is_based_on_type_but_slice__in__has_no_types", new Object[]{discriminator, ed.getId(), profile.getVersionedUrl()}
													)
											);
										}

										type = criteriaElement.getPath();
									}
								}

								if (discriminator.isEmpty()) {
									expression.append(" and $this is " + type);
								} else {
									expression.append(" and " + discriminator + " is " + type);
								}
							} else if (s.getType() == DiscriminatorType.PROFILE) {
								if (criteriaElement.getType().size() == 0) {
									throw new DefinitionException(
										this.context
											.formatMessage(
												"Profile_based_discriminators_must_have_a_type__in_profile_", new Object[]{criteriaElement.getId(), profile.getVersionedUrl()}
											)
									);
								}

								if (criteriaElement.getType().size() != 1) {
									throw new DefinitionException(
										this.context
											.formatMessagePlural(
												criteriaElement.getType().size(),
												"Profile_based_discriminators_must_have_only_one_type__in_profile",
												new Object[]{criteriaElement.getId(), profile.getVersionedUrl()}
											)
									);
								}

								List<CanonicalType> list = !discriminator.endsWith(".resolve()") && !discriminator.equals("resolve()")
									? ((TypeRefComponent)criteriaElement.getType().get(0)).getProfile()
									: ((TypeRefComponent)criteriaElement.getType().get(0)).getTargetProfile();
								if (list.size() == 0) {
									throw new DefinitionException(
										this.context
											.formatMessage(
												"Profile_based_discriminators_must_have_a_type_with_a_profile__in_profile_",
												new Object[]{criteriaElement.getId(), profile.getVersionedUrl()}
											)
									);
								}

								if (list.size() <= 1) {
									expression.append(" and " + discriminator + ".conformsTo('" + (String)((CanonicalType)list.get(0)).getValue() + "')");
								} else {
									CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder(" or ");

									for(CanonicalType c : list) {
										b.append(discriminator + ".conformsTo('" + (String)c.getValue() + "')");
									}

									expression.append(" and (" + b + ")");
								}
							} else if (s.getType() == DiscriminatorType.EXISTS) {
								if (criteriaElement.hasMin() && criteriaElement.getMin() >= 1) {
									expression.append(" and (" + discriminator + ".exists())");
								} else {
									if (!criteriaElement.hasMax() || !criteriaElement.getMax().equals("0")) {
										throw new FHIRException(
											this.context
												.formatMessage(
													"Discriminator__is_based_on_element_existence_but_slice__neither_sets_min1_or_max0", new Object[]{discriminator, ed.getId()}
												)
										);
									}

									expression.append(" and (" + discriminator + ".exists().not())");
								}
							} else if (criteriaElement.hasFixed()) {
								this.buildFixedExpression(ed, expression, discriminator, criteriaElement);
							} else if (criteriaElement.hasPattern()) {
								this.buildPattternExpression(ed, expression, discriminator, criteriaElement);
							} else if (criteriaElement.hasBinding()
								&& criteriaElement.getBinding().hasStrength()
								&& criteriaElement.getBinding().getStrength().equals(BindingStrength.REQUIRED)
								&& criteriaElement.getBinding().hasValueSet()) {
								expression.append(" and (" + discriminator + " memberOf '" + criteriaElement.getBinding().getValueSet() + "')");
							} else {
								found = false;
							}

							if (!found) {
								continue;
							}
						}

						if (found) {
							anyFound = true;
						}
						break;
					}
				}

				if (!anyFound) {
					throw new DefinitionException(
						this.context
							.formatMessagePlural(
								slicer.getSlicing().getDiscriminator().size(),
								"Could_not_match_discriminator_for_slice_in_profile",
								new Object[]{discriminators, ed.getId(), profile.getVersionedUrl(), discriminators}
							)
					);
				}

				try {
					n = this.fpe.parse(FHIRPathExpressionFixer.fixExpr(expression.toString(), null, this.context.getVersion()));
				} catch (FHIRLexerException var28) {
					throw new FHIRException(
						this.context
							.formatMessage("Problem_processing_expression__in_profile__path__", new Object[]{expression, profile.getVersionedUrl(), path, var28.getMessage()})
					);
				}

				this.timeTracker.fpe(t);
				ed.setUserData("slice.expression.cache", n);
			}

			ValidatorHostContext shc = hostContext.forSlicing();
			boolean pass = this.evaluateSlicingExpression(shc, element, path, profile, n);
			if (!pass) {
				this.slicingHint(
					sliceInfo,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					path,
					false,
					this.isProfile(slicer),
					this.context.formatMessage("Does_not_match_slice_", new Object[]{ed.getSliceName(), n.toString().substring(8).trim()}),
					"discriminator = " + Utilities.escapeXml(n.toString()),
					null
				);

				for(String url : shc.getSliceRecords().keySet()) {
					StructureDefinition sdt = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, url);
					this.slicingHint(
						sliceInfo,
						NO_RULE_DATE,
						IssueType.STRUCTURE,
						element.line(),
						element.col(),
						path,
						false,
						this.isProfile(slicer),
						this.context
							.formatMessage("Details_for__matching_against_Profile_", new Object[]{stack.getLiteralPath(), sdt == null ? url : sdt.getVersionedUrl()}),
						this.context
							.formatMessage(
								"Profile__does_not_match_for__because_of_the_following_profile_issues__",
								new Object[]{url, stack.getLiteralPath(), this.errorSummaryForSlicingAsHtml((List<ValidationMessage>)shc.getSliceRecords().get(url))}
							),
						this.errorSummaryForSlicingAsText((List<ValidationMessage>)shc.getSliceRecords().get(url))
					);
				}
			}

			return pass;
		}
	}

	private boolean isBaseDefinition(String url) {
		return url.startsWith("http://hl7.org/fhir/") && !url.substring(40).contains("/");
	}

	private String descSD(String url) {
		StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, url);
		return sd == null ? url : sd.present();
	}

	private boolean isProfile(ElementDefinition slicer) {
		if (slicer != null && slicer.hasSlicing()) {
			for(ElementDefinitionSlicingDiscriminatorComponent t : slicer.getSlicing().getDiscriminator()) {
				if (t.getType() == DiscriminatorType.PROFILE) {
					return true;
				}
			}

			return false;
		} else {
			return false;
		}
	}

	public boolean evaluateSlicingExpression(ValidatorHostContext hostContext, Element element, String path, StructureDefinition profile, ExpressionNode n) throws FHIRException {
		try {
			long t = System.nanoTime();
			boolean ok = this.fpe.evaluateToBoolean(hostContext.forProfile(profile), hostContext.getResource(), hostContext.getRootResource(), element, n);
			this.timeTracker.fpe(t);
			String msg = this.fpe.forLog();
			return ok;
		} catch (Exception var10) {
			throw new FHIRException(
				this.context
					.formatMessage(
						"Problem_evaluating_slicing_expression_for_element_in_profile__path__fhirPath___",
						new Object[]{profile.getVersionedUrl(), path, n, var10.getMessage()}
					)
			);
		}
	}

	private void buildPattternExpression(ElementDefinition ed, StringBuilder expression, String discriminator, ElementDefinition criteriaElement) throws DefinitionException {
		DataType pattern = criteriaElement.getPattern();
		if (pattern instanceof CodeableConcept) {
			CodeableConcept cc = (CodeableConcept)pattern;
			expression.append(" and ");
			this.buildCodeableConceptExpression(ed, expression, discriminator, cc);
		} else if (pattern instanceof Coding) {
			Coding c = (Coding)pattern;
			expression.append(" and ");
			this.buildCodingExpression(ed, expression, discriminator, c);
		} else if (pattern instanceof BooleanType || pattern instanceof IntegerType || pattern instanceof DecimalType) {
			expression.append(" and ");
			this.buildPrimitiveExpression(ed, expression, discriminator, pattern, false);
		} else if (pattern instanceof PrimitiveType) {
			expression.append(" and ");
			this.buildPrimitiveExpression(ed, expression, discriminator, pattern, true);
		} else if (pattern instanceof Identifier) {
			Identifier ii = (Identifier)pattern;
			expression.append(" and ");
			this.buildIdentifierExpression(ed, expression, discriminator, ii);
		} else if (pattern instanceof HumanName) {
			HumanName name = (HumanName)pattern;
			expression.append(" and ");
			this.buildHumanNameExpression(ed, expression, discriminator, name);
		} else {
			if (!(pattern instanceof Address)) {
				throw new DefinitionException(
					this.context
						.formatMessage("Unsupported_fixed_pattern_type_for_discriminator_for_slice__", new Object[]{discriminator, ed.getId(), pattern.fhirType()})
				);
			}

			Address add = (Address)pattern;
			expression.append(" and ");
			this.buildAddressExpression(ed, expression, discriminator, add);
		}
	}

	private void buildIdentifierExpression(ElementDefinition ed, StringBuilder expression, String discriminator, Identifier ii) throws DefinitionException {
		if (ii.hasExtension()) {
			throw new DefinitionException(
				this.context
					.formatMessage("Unsupported_Identifier_pattern__extensions_are_not_allowed__for_discriminator_for_slice_", new Object[]{discriminator, ed.getId()})
			);
		} else {
			boolean first = true;
			expression.append(discriminator + ".where(");
			if (ii.hasSystem()) {
				first = false;
				expression.append("system = '" + ii.getSystem() + "'");
			}

			if (ii.hasValue()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("value = '" + ii.getValue() + "'");
			}

			if (ii.hasUse()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("use = '" + ii.getUse() + "'");
			}

			if (ii.hasType()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				this.buildCodeableConceptExpression(ed, expression, "type", ii.getType());
			}

			if (first) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_NO_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE", new Object[]{discriminator, ed.getId(), ii.fhirType()}
						)
				);
			} else {
				expression.append(").exists()");
			}
		}
	}

	private void buildHumanNameExpression(ElementDefinition ed, StringBuilder expression, String discriminator, HumanName name) throws DefinitionException {
		if (name.hasExtension()) {
			throw new DefinitionException(
				this.context
					.formatMessage("Unsupported_Identifier_pattern__extensions_are_not_allowed__for_discriminator_for_slice_", new Object[]{discriminator, ed.getId()})
			);
		} else {
			boolean first = true;
			expression.append(discriminator + ".where(");
			if (name.hasUse()) {
				first = false;
				expression.append("use = '" + name.getUse().toCode() + "'");
			}

			if (name.hasText()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("text = '" + name.getText() + "'");
			}

			if (name.hasFamily()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("family = '" + name.getFamily() + "'");
			}

			if (name.hasGiven()) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE",
							new Object[]{discriminator, ed.getId(), name.fhirType(), "given"}
						)
				);
			} else if (name.hasPrefix()) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE",
							new Object[]{discriminator, ed.getId(), name.fhirType(), "prefix"}
						)
				);
			} else if (name.hasSuffix()) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE",
							new Object[]{discriminator, ed.getId(), name.fhirType(), "suffix"}
						)
				);
			} else if (name.hasPeriod()) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE",
							new Object[]{discriminator, ed.getId(), name.fhirType(), "period"}
						)
				);
			} else if (first) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_NO_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE", new Object[]{discriminator, ed.getId(), name.fhirType()}
						)
				);
			} else {
				expression.append(").exists()");
			}
		}
	}

	private void buildAddressExpression(ElementDefinition ed, StringBuilder expression, String discriminator, Address add) throws DefinitionException {
		if (add.hasExtension()) {
			throw new DefinitionException(
				this.context
					.formatMessage("Unsupported_Identifier_pattern__extensions_are_not_allowed__for_discriminator_for_slice_", new Object[]{discriminator, ed.getId()})
			);
		} else {
			boolean first = true;
			expression.append(discriminator + ".where(");
			if (add.hasUse()) {
				first = false;
				expression.append("use = '" + add.getUse().toCode() + "'");
			}

			if (add.hasType()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("type = '" + add.getType().toCode() + "'");
			}

			if (add.hasText()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("text = '" + add.getText() + "'");
			}

			if (add.hasCity()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("city = '" + add.getCity() + "'");
			}

			if (add.hasDistrict()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("district = '" + add.getDistrict() + "'");
			}

			if (add.hasState()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("state = '" + add.getState() + "'");
			}

			if (add.hasPostalCode()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("postalCode = '" + add.getPostalCode() + "'");
			}

			if (add.hasCountry()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("country = '" + add.getCountry() + "'");
			}

			if (add.hasLine()) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE",
							new Object[]{discriminator, ed.getId(), add.fhirType(), "line"}
						)
				);
			} else if (add.hasPeriod()) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE",
							new Object[]{discriminator, ed.getId(), add.fhirType(), "period"}
						)
				);
			} else if (first) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_NO_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE", new Object[]{discriminator, ed.getId(), add.fhirType()}
						)
				);
			} else {
				expression.append(").exists()");
			}
		}
	}

	private void buildCodeableConceptExpression(ElementDefinition ed, StringBuilder expression, String discriminator, CodeableConcept cc) throws DefinitionException {
		if (cc.hasText()) {
			throw new DefinitionException(
				this.context.formatMessage("Unsupported_CodeableConcept_pattern__using_text__for_discriminator_for_slice_", new Object[]{discriminator, ed.getId()})
			);
		} else if (!cc.hasCoding()) {
			throw new DefinitionException(
				this.context
					.formatMessage(
						"Unsupported_CodeableConcept_pattern__must_have_at_least_one_coding__for_discriminator_for_slice_", new Object[]{discriminator, ed.getId()}
					)
			);
		} else if (cc.hasExtension()) {
			throw new DefinitionException(
				this.context
					.formatMessage(
						"Unsupported_CodeableConcept_pattern__extensions_are_not_allowed__for_discriminator_for_slice_", new Object[]{discriminator, ed.getId()}
					)
			);
		} else {
			boolean firstCoding = true;

			for(Coding c : cc.getCoding()) {
				if (c.hasExtension()) {
					throw new DefinitionException(
						this.context
							.formatMessage(
								"Unsupported_CodeableConcept_pattern__extensions_are_not_allowed__for_discriminator_for_slice_", new Object[]{discriminator, ed.getId()}
							)
					);
				}

				if (firstCoding) {
					firstCoding = false;
				} else {
					expression.append(" and ");
				}

				expression.append(discriminator + ".coding.where(");
				boolean first = true;
				if (c.hasSystem()) {
					first = false;
					expression.append("system = '" + c.getSystem() + "'");
				}

				if (c.hasVersion()) {
					if (first) {
						first = false;
					} else {
						expression.append(" and ");
					}

					expression.append("version = '" + c.getVersion() + "'");
				}

				if (c.hasCode()) {
					if (first) {
						first = false;
					} else {
						expression.append(" and ");
					}

					expression.append("code = '" + c.getCode() + "'");
				}

				if (c.hasDisplay()) {
					if (first) {
						first = false;
					} else {
						expression.append(" and ");
					}

					expression.append("display = '" + c.getDisplay() + "'");
				}

				if (first) {
					throw new DefinitionException(
						this.context
							.formatMessage(
								"UNSUPPORTED_IDENTIFIER_PATTERN_NO_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE", new Object[]{discriminator, ed.getId(), cc.fhirType()}
							)
					);
				}

				expression.append(").exists()");
			}
		}
	}

	private void buildCodingExpression(ElementDefinition ed, StringBuilder expression, String discriminator, Coding c) throws DefinitionException {
		if (c.hasExtension()) {
			throw new DefinitionException(
				this.context
					.formatMessage(
						"Unsupported_CodeableConcept_pattern__extensions_are_not_allowed__for_discriminator_for_slice_", new Object[]{discriminator, ed.getId()}
					)
			);
		} else {
			expression.append(discriminator + ".where(");
			boolean first = true;
			if (c.hasSystem()) {
				first = false;
				expression.append("system = '" + c.getSystem() + "'");
			}

			if (c.hasVersion()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("version = '" + c.getVersion() + "'");
			}

			if (c.hasCode()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("code = '" + c.getCode() + "'");
			}

			if (c.hasDisplay()) {
				if (first) {
					first = false;
				} else {
					expression.append(" and ");
				}

				expression.append("display = '" + c.getDisplay() + "'");
			}

			if (first) {
				throw new DefinitionException(
					this.context
						.formatMessage(
							"UNSUPPORTED_IDENTIFIER_PATTERN_NO_PROPERTY_NOT_SUPPORTED_FOR_DISCRIMINATOR_FOR_SLICE", new Object[]{discriminator, ed.getId(), c.fhirType()}
						)
				);
			} else {
				expression.append(").exists()");
			}
		}
	}

	private void buildPrimitiveExpression(ElementDefinition ed, StringBuilder expression, String discriminator, DataType p, boolean quotes) throws DefinitionException {
		if (p.hasExtension()) {
			throw new DefinitionException(
				this.context
					.formatMessage(
						"Unsupported_CodeableConcept_pattern__extensions_are_not_allowed__for_discriminator_for_slice_", new Object[]{discriminator, ed.getId()}
					)
			);
		} else {
			if (quotes) {
				expression.append(discriminator + ".where(value = '" + p.primitiveValue() + "'");
			} else {
				expression.append(discriminator + ".where(value = " + p.primitiveValue());
			}

			expression.append(").exists()");
		}
	}

	private void buildFixedExpression(ElementDefinition ed, StringBuilder expression, String discriminator, ElementDefinition criteriaElement) throws DefinitionException {
		DataType fixed = criteriaElement.getFixed();
		if (fixed instanceof CodeableConcept) {
			CodeableConcept cc = (CodeableConcept)fixed;
			expression.append(" and ");
			this.buildCodeableConceptExpression(ed, expression, discriminator, cc);
		} else if (fixed instanceof Identifier) {
			Identifier ii = (Identifier)fixed;
			expression.append(" and ");
			this.buildIdentifierExpression(ed, expression, discriminator, ii);
		} else if (fixed instanceof Coding) {
			Coding c = (Coding)fixed;
			expression.append(" and ");
			this.buildCodingExpression(ed, expression, discriminator, c);
		} else {
			expression.append(" and (");
			if (fixed instanceof StringType) {
				String es = Utilities.escapeJson(fixed.primitiveValue());
				expression.append("'" + es + "'");
			} else if (fixed instanceof UriType) {
				expression.append("'" + ((UriType)fixed).asStringValue() + "'");
			} else if (fixed instanceof IntegerType) {
				expression.append(((IntegerType)fixed).asStringValue());
			} else if (fixed instanceof DecimalType) {
				expression.append(((IntegerType)fixed).asStringValue());
			} else {
				if (!(fixed instanceof BooleanType)) {
					throw new DefinitionException(
						this.context
							.formatMessage("Unsupported_fixed_value_type_for_discriminator_for_slice__", new Object[]{discriminator, ed.getId(), fixed.getClass().getName()})
					);
				}

				expression.append(((BooleanType)fixed).asStringValue());
			}

			expression.append(" in " + discriminator + ")");
		}
	}

	private boolean start(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		Element resource,
		Element element,
		StructureDefinition defn,
		NodeStack stack,
		PercentageTracker pct,
		ValidationMode mode
	) throws FHIRException {
		boolean ok = true;
		this.checkLang(resource, stack);
		if (this.crumbTrails) {
			element.addMessage(
				this.signpost(
					errors,
					NO_RULE_DATE,
					IssueType.INFORMATIONAL,
					element.line(),
					element.col(),
					stack.getLiteralPath(),
					"VALIDATION_VAL_PROFILE_SIGNPOST",
					new Object[]{defn.getVersionedUrl()}
				)
			);
		}

		boolean pctOwned = false;
		if (pct == null) {
			pctOwned = true;
			pct = new PercentageTracker(resource.countDescendents() + 1, resource.fhirType(), defn.getVersionedUrl(), this.logProgress);
		}

		if ("Bundle".equals(element.fhirType())) {
			if (this.debug) {
				System.out.println("Resolve Bundle Entries " + this.time());
			}

			this.resolveBundleReferences(element, new ArrayList());
		}

		ok = this.startInner(hostContext, errors, resource, element, defn, stack, hostContext.isCheckSpecials(), pct, mode) && ok;
		if (pctOwned) {
			pct.done();
		}

		Element meta = element.getNamedChild("meta");
		if (meta != null) {
			List<Element> profiles = new ArrayList();
			meta.getNamedChildren("profile", profiles);
			int i = 0;

			for(Element profile : profiles) {
				StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, profile.primitiveValue());
				if (!defn.getUrl().equals(profile.primitiveValue())) {
					VersionURLInfo vu = VersionUtilities.parseVersionUrl(profile.primitiveValue());
					if (vu != null) {
						if (!VersionUtilities.versionsCompatible(vu.getVersion(), this.context.getVersion())) {
							this.hint(
								errors,
								NO_RULE_DATE,
								IssueType.STRUCTURE,
								element.line(),
								element.col(),
								stack.getLiteralPath() + ".meta.profile[" + i + "]",
								false,
								"VALIDATION_VAL_PROFILE_OTHER_VERSION",
								new Object[]{vu.getVersion()}
							);
						} else if (vu.getUrl().equals(defn.getUrl())) {
							this.hint(
								errors,
								NO_RULE_DATE,
								IssueType.STRUCTURE,
								element.line(),
								element.col(),
								stack.getLiteralPath() + ".meta.profile[" + i + "]",
								false,
								"VALIDATION_VAL_PROFILE_THIS_VERSION_OK"
							);
						} else {
							StructureDefinition sdt = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, vu.getUrl());
							ok = this.rule(
								errors,
								NO_RULE_DATE,
								IssueType.STRUCTURE,
								element.line(),
								element.col(),
								stack.getLiteralPath() + ".meta.profile[" + i + "]",
								false,
								"VALIDATION_VAL_PROFILE_THIS_VERSION_OTHER",
								new Object[]{sdt == null ? "null" : sdt.getType()}
							)
								&& ok;
						}
					} else {
						if (sd == null) {
							if (this.fetcher == null) {
								this.warning(
									errors,
									NO_RULE_DATE,
									IssueType.STRUCTURE,
									element.line(),
									element.col(),
									stack.getLiteralPath() + ".meta.profile[" + i + "]",
									false,
									"Validation_VAL_Profile_Unknown",
									new Object[]{profile.primitiveValue()}
								);
							} else if (!this.fetcher.fetchesCanonicalResource(this, profile.primitiveValue())) {
								this.warning(
									errors,
									NO_RULE_DATE,
									IssueType.STRUCTURE,
									element.line(),
									element.col(),
									stack.getLiteralPath() + ".meta.profile[" + i + "]",
									false,
									"VALIDATION_VAL_PROFILE_UNKNOWN_NOT_POLICY",
									new Object[]{profile.primitiveValue()}
								);
							} else {
								sd = this.lookupProfileReference(errors, element, stack, i, profile, sd);
							}
						}

						if (sd != null) {
							if (this.crumbTrails) {
								element.addMessage(
									this.signpost(
										errors,
										NO_RULE_DATE,
										IssueType.INFORMATIONAL,
										element.line(),
										element.col(),
										stack.getLiteralPath(),
										"VALIDATION_VAL_PROFILE_SIGNPOST_META",
										new Object[]{sd.getVersionedUrl()}
									)
								);
							}

							stack.resetIds();
							if (pctOwned) {
								pct = new PercentageTracker(resource.countDescendents(), resource.fhirType(), sd.getUrl(), this.logProgress);
							}

							ok = this.startInner(hostContext, errors, resource, element, sd, stack, false, pct, mode.withSource(ProfileSource.MetaProfile)) && ok;
							if (pctOwned) {
								pct.done();
							}

							if (sd.hasExtension("http://hl7.org/fhir/StructureDefinition/structuredefinition-imposeProfile")) {
								for(Extension ext : sd.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/structuredefinition-imposeProfile")) {
									StructureDefinition sdi = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, ext.getValue().primitiveValue());
									if (sdi == null) {
										this.warning(
											errors,
											NO_RULE_DATE,
											IssueType.BUSINESSRULE,
											element.line(),
											element.col(),
											stack.getLiteralPath() + ".meta.profile[" + i + "]",
											false,
											"VALIDATION_VAL_PROFILE_DEPENDS_NOT_RESOLVED",
											new Object[]{ext.getValue().primitiveValue(), sd.getVersionedUrl()}
										);
									} else {
										if (this.crumbTrails) {
											element.addMessage(
												this.signpost(
													errors,
													NO_RULE_DATE,
													IssueType.INFORMATIONAL,
													element.line(),
													element.col(),
													stack.getLiteralPath(),
													"VALIDATION_VAL_PROFILE_SIGNPOST_DEP",
													new Object[]{sdi.getUrl(), sd.getVersionedUrl()}
												)
											);
										}

										stack.resetIds();
										if (pctOwned) {
											pct = new PercentageTracker(resource.countDescendents(), resource.fhirType(), sdi.getUrl(), this.logProgress);
										}

										ok = this.startInner(hostContext, errors, resource, element, sdi, stack, false, pct, mode.withSource(ProfileSource.ProfileDependency))
											&& ok;
										if (pctOwned) {
											pct.done();
										}
									}
								}
							}
						}
					}
				}

				++i;
			}
		}

		String rt = element.fhirType();

		for(ImplementationGuide ig : this.igs) {
			for(ImplementationGuideGlobalComponent gl : ig.getGlobal()) {
				if (rt.equals(gl.getType())) {
					StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, gl.getProfile(), ig);
					if (this.warning(
						errors,
						NO_RULE_DATE,
						IssueType.STRUCTURE,
						element.line(),
						element.col(),
						stack.getLiteralPath(),
						sd != null,
						"VALIDATION_VAL_GLOBAL_PROFILE_UNKNOWN",
						new Object[]{gl.getProfile(), ig.getVersionedUrl()}
					)) {
						if (this.crumbTrails) {
							element.addMessage(
								this.signpost(
									errors,
									NO_RULE_DATE,
									IssueType.INFORMATIONAL,
									element.line(),
									element.col(),
									stack.getLiteralPath(),
									"VALIDATION_VAL_PROFILE_SIGNPOST_GLOBAL",
									new Object[]{sd.getVersionedUrl(), ig.getVersionedUrl()}
								)
							);
						}

						stack.resetIds();
						if (pctOwned) {
							pct = new PercentageTracker(resource.countDescendents(), resource.fhirType(), sd.getVersionedUrl(), this.logProgress);
						}

						ok = this.startInner(hostContext, errors, resource, element, sd, stack, false, pct, mode.withSource(ProfileSource.GlobalProfile)) && ok;
						if (pctOwned) {
							pct.done();
						}
					}
				}
			}
		}

		return ok;
	}

	private StructureDefinition lookupProfileReference(
		List<ValidationMessage> errors, Element element, NodeStack stack, int i, Element profile, StructureDefinition sd
	) {
		String url = profile.primitiveValue();
		InstanceValidator.CanonicalResourceLookupResult cr = (InstanceValidator.CanonicalResourceLookupResult)this.crLookups.get(url);
		if (cr != null) {
			if (cr.error != null) {
				this.warning(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					element.line(),
					element.col(),
					stack.getLiteralPath() + ".meta.profile[" + i + "]",
					false,
					"VALIDATION_VAL_PROFILE_UNKNOWN_ERROR",
					new Object[]{url, cr.error}
				);
			} else {
				sd = (StructureDefinition)cr.resource;
			}
		} else {
			try {
				sd = (StructureDefinition)this.fetcher.fetchCanonicalResource(this, url);
				this.crLookups.put(url, new InstanceValidator.CanonicalResourceLookupResult(sd));
			} catch (Exception var12) {
				this.crLookups.put(url, new InstanceValidator.CanonicalResourceLookupResult(var12.getMessage()));
				if (var12.getMessage() != null && var12.getMessage().startsWith("java.net.UnknownHostException:")) {
					try {
						this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							element.line(),
							element.col(),
							stack.getLiteralPath() + ".meta.profile[" + i + "]",
							false,
							"VALIDATION_VAL_PROFILE_UNKNOWN_ERROR_NETWORK",
							new Object[]{profile.primitiveValue(), new URI(url).getHost()}
						);
					} catch (URISyntaxException var11) {
						this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							element.line(),
							element.col(),
							stack.getLiteralPath() + ".meta.profile[" + i + "]",
							false,
							"VALIDATION_VAL_PROFILE_UNKNOWN_ERROR_NETWORK",
							new Object[]{profile.primitiveValue(), "??"}
						);
					}
				} else {
					this.warning(
						errors,
						NO_RULE_DATE,
						IssueType.STRUCTURE,
						element.line(),
						element.col(),
						stack.getLiteralPath() + ".meta.profile[" + i + "]",
						false,
						"VALIDATION_VAL_PROFILE_UNKNOWN_ERROR",
						new Object[]{profile.primitiveValue(), var12.getMessage()}
					);
				}
			}

			if (sd != null) {
				this.context.cacheResource(sd);
			}
		}

		return sd;
	}

	private void resolveBundleReferences(Element element, List<Element> bundles) {
		if (!element.hasUserData("validator.bundle.resolved")) {
			element.setUserData("validator.bundle.resolved", true);
			List<Element> list = new ArrayList();
			list.addAll(bundles);
			list.add(0, element);

			for(Element entry : element.getChildrenByName("entry")) {
				String fu = entry.getChildValue("fullUrl");
				Element r = entry.getNamedChild("resource");
				if (r != null) {
					this.resolveBundleReferencesInResource(list, r, fu);
				}
			}
		}
	}

	private void resolveBundleReferencesInResource(List<Element> bundles, Element r, String fu) {
		if ("Bundle".equals(r.fhirType())) {
			this.resolveBundleReferences(r, bundles);
		} else {
			for(Element child : r.getChildren()) {
				this.resolveBundleReferencesForElement(bundles, r, fu, child);
			}
		}
	}

	private void resolveBundleReferencesForElement(List<Element> bundles, Element resource, String fu, Element element) {
		if ("Reference".equals(element.fhirType())) {
			String ref = element.getChildValue("reference");
			if (!Utilities.noString(ref)) {
				for(Element bundle : bundles) {
					List<Element> entries = bundle.getChildren("entry");
					Element tgt = this.resolveInBundle(bundle, entries, ref, fu, resource.fhirType(), resource.getIdBase());
					if (tgt != null) {
						element.setUserData("validator.bundle.resolution", tgt.getNamedChild("resource"));
						return;
					}
				}
			}
		} else {
			for(Element child : element.getChildren()) {
				this.resolveBundleReferencesForElement(bundles, resource, fu, child);
			}
		}
	}

	public boolean startInner(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		Element resource,
		Element element,
		StructureDefinition defn,
		NodeStack stack,
		boolean checkSpecials,
		PercentageTracker pct,
		ValidationMode mode
	) {
		boolean ok = false;
		ResourceValidationTracker resTracker = this.getResourceTracker(element);
		List<ValidationMessage> cachedErrors = resTracker.getOutcomes(defn);
		if (cachedErrors != null) {
			for(ValidationMessage vm : cachedErrors) {
				if (!errors.contains(vm)) {
					errors.add(vm);
					ok = ok && vm.getLevel() != IssueSeverity.ERROR && vm.getLevel() != IssueSeverity.FATAL;
				}
			}

			return ok;
		} else {
			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.STRUCTURE,
				element.line(),
				element.col(),
				stack.getLiteralPath(),
				defn.hasSnapshot(),
				"Validation_VAL_Profile_NoSnapshot",
				new Object[]{defn.getVersionedUrl()}
			)) {
				List<ValidationMessage> localErrors = new ArrayList();
				resTracker.startValidating(defn);
				this.trackUsage(defn, hostContext, element);
				ok = this.validateElement(
					hostContext,
					localErrors,
					defn,
					(ElementDefinition)defn.getSnapshot().getElement().get(0),
					null,
					null,
					resource,
					element,
					element.getName(),
					stack,
					false,
					true,
					null,
					pct,
					mode
				)
					&& ok;
				resTracker.storeOutcomes(defn, localErrors);

				for(ValidationMessage vm : localErrors) {
					if (!errors.contains(vm)) {
						errors.add(vm);
					}
				}
			} else {
				ok = false;
			}

			if (checkSpecials) {
				ok = this.checkSpecials(hostContext, errors, element, stack, checkSpecials, pct, mode) && ok;
				ok = this.validateResourceRules(errors, element, stack) && ok;
			}

			return ok;
		}
	}

	public boolean checkSpecials(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		Element element,
		NodeStack stack,
		boolean checkSpecials,
		PercentageTracker pct,
		ValidationMode mode
	) {
		long t = System.nanoTime();

		boolean var16;
		try {
			if (VersionUtilities.getCanonicalResourceNames(this.context.getVersion()).contains(element.getType())) {
				Base base = element.getExtensionValue("http://hl7.org/fhir/StructureDefinition/structuredefinition-standards-status");
				String standardsStatus = base != null && base.isPrimitive() ? base.primitiveValue() : null;
				String status = element.getNamedChildValue("status");
				if (!Utilities.noString(status)
					&& !Utilities.noString(standardsStatus)
					&& this.warning(
					errors,
					"2023-08-14",
					IssueType.BUSINESSRULE,
					element.line(),
					element.col(),
					stack.getLiteralPath(),
					this.statusCodesConsistent(status, standardsStatus),
					"VALIDATION_VAL_STATUS_INCONSISTENT",
					new Object[]{status, standardsStatus}
				)) {
					this.hint(
						errors,
						"2023-08-14",
						IssueType.BUSINESSRULE,
						element.line(),
						element.col(),
						stack.getLiteralPath(),
						this.statusCodesDeeplyConsistent(status, standardsStatus),
						"VALIDATION_VAL_STATUS_INCONSISTENT_HINT",
						new Object[]{status, standardsStatus}
					);
				}
			}

			if (element.getType().equals("Bundle")) {
				return new BundleValidator(this, this.serverBase).validateBundle(errors, element, stack, checkSpecials, hostContext, pct, mode);
			}

			if (element.getType().equals("Observation")) {
				return this.validateObservation(errors, element, stack);
			}

			if (element.getType().equals("Questionnaire")) {
				return new QuestionnaireValidator(this, this.myEnableWhenEvaluator, this.fpe, this.questionnaireMode)
					.validateQuestionannaire(errors, element, element, stack);
			}

			if (element.getType().equals("QuestionnaireResponse")) {
				return new QuestionnaireValidator(this, this.myEnableWhenEvaluator, this.fpe, this.questionnaireMode)
					.validateQuestionannaireResponse(hostContext, errors, element, stack);
			}

			if (element.getType().equals("Measure")) {
				return new MeasureValidator(this).validateMeasure(hostContext, errors, element, stack);
			}

			if (element.getType().equals("MeasureReport")) {
				return new MeasureValidator(this).validateMeasureReport(hostContext, errors, element, stack);
			}

			if (element.getType().equals("CapabilityStatement")) {
				return this.validateCapabilityStatement(errors, element, stack);
			}

			if (element.getType().equals("CodeSystem")) {
				return new CodeSystemValidator(this).validateCodeSystem(errors, element, stack, this.baseOptions.withLanguage(stack.getWorkingLang()));
			}

			if (element.getType().equals("ConceptMap")) {
				return new ConceptMapValidator(this).validateConceptMap(errors, element, stack, this.baseOptions.withLanguage(stack.getWorkingLang()));
			}

			if (element.getType().equals("SearchParameter")) {
				return new SearchParameterValidator(this, this.fpe).validateSearchParameter(errors, element, stack);
			}

			if (element.getType().equals("StructureDefinition")) {
				return new StructureDefinitionValidator(this, this.fpe, this.wantCheckSnapshotUnchanged).validateStructureDefinition(errors, element, stack);
			}

			if (element.getType().equals("StructureMap")) {
				return new StructureMapValidator(this, this.fpe, this.profileUtilities).validateStructureMap(errors, element, stack);
			}

			if (!element.getType().equals("ValueSet")) {
				return true;
			}

			var16 = new ValueSetValidator(this).validateValueSet(errors, element, stack);
		} finally {
			this.timeTracker.spec(t);
		}

		return var16;
	}

	private boolean statusCodesConsistent(String status, String standardsStatus) {
		switch(standardsStatus) {
			case "draft":
				return Utilities.existsInList(status, new String[]{"draft"});
			case "normative":
				return Utilities.existsInList(status, new String[]{"active"});
			case "trial-use":
				return Utilities.existsInList(status, new String[]{"draft", "active"});
			case "informative":
				return Utilities.existsInList(status, new String[]{"draft", "active", "retired"});
			case "deprecated":
				return Utilities.existsInList(status, new String[]{"retired"});
			case "withdrawn":
				return Utilities.existsInList(status, new String[]{"retired"});
			case "external":
				return Utilities.existsInList(status, new String[]{"draft", "active", "retired"});
			default:
				return true;
		}
	}

	private boolean statusCodesDeeplyConsistent(String status, String standardsStatus) {
		switch(standardsStatus) {
			case "draft":
				return Utilities.existsInList(status, new String[]{"draft"});
			case "normative":
				return Utilities.existsInList(status, new String[]{"active"});
			case "trial-use":
				return Utilities.existsInList(status, new String[]{"active"});
			case "informative":
				return Utilities.existsInList(status, new String[]{"draft", "active"});
			case "deprecated":
				return Utilities.existsInList(status, new String[]{"retired"});
			case "withdrawn":
				return Utilities.existsInList(status, new String[]{"retired"});
			case "external":
				return Utilities.existsInList(status, new String[]{"draft", "active"});
			default:
				return true;
		}
	}

	private ResourceValidationTracker getResourceTracker(Element element) {
		ResourceValidationTracker res = (ResourceValidationTracker)this.resourceTracker.get(element);
		if (res == null) {
			res = new ResourceValidationTracker();
			this.resourceTracker.put(element, res);
		}

		return res;
	}

	private void checkLang(Element resource, NodeStack stack) {
		String lang = resource.getNamedChildValue("language");
		if (!Utilities.noString(lang)) {
			stack.setWorkingLang(lang);
		}
	}

	private boolean validateResourceRules(List<ValidationMessage> errors, Element element, NodeStack stack) {
		boolean ok = true;
		String lang = element.getNamedChildValue("language");
		Element text = element.getNamedChild("text");
		if (text != null) {
			Element div = text.getNamedChild("div");
			if (lang != null && div != null) {
				XhtmlNode xhtml = div.getXhtml();
				String l = xhtml.getAttribute("lang");
				String xl = xhtml.getAttribute("xml:lang");
				if (l == null && xl == null) {
					this.warning(
						errors, NO_RULE_DATE, IssueType.BUSINESSRULE, div.line(), div.col(), stack.getLiteralPath(), false, "Language_XHTML_Lang_Missing1", new Object[0]
					);
				} else {
					if (l == null) {
						this.warning(
							errors, NO_RULE_DATE, IssueType.BUSINESSRULE, div.line(), div.col(), stack.getLiteralPath(), false, "Language_XHTML_Lang_Missing2", new Object[0]
						);
					} else if (!l.equals(lang)) {
						this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.BUSINESSRULE,
							div.line(),
							div.col(),
							stack.getLiteralPath(),
							false,
							"Language_XHTML_Lang_Different1",
							new Object[]{lang, l}
						);
					}

					if (xl == null) {
						this.warning(
							errors, NO_RULE_DATE, IssueType.BUSINESSRULE, div.line(), div.col(), stack.getLiteralPath(), false, "Language_XHTML_Lang_Missing3", new Object[0]
						);
					} else if (!xl.equals(lang)) {
						this.warning(
							errors,
							NO_RULE_DATE,
							IssueType.BUSINESSRULE,
							div.line(),
							div.col(),
							stack.getLiteralPath(),
							false,
							"Language_XHTML_Lang_Different2",
							new Object[]{lang, xl}
						);
					}
				}
			}
		}

		Element meta = element.getNamedChild("meta");
		if (meta != null) {
			Set<String> tags = new HashSet();
			List<Element> list = new ArrayList();
			meta.getNamedChildren("security", list);
			int i = 0;

			for(Element e : list) {
				String s = e.getNamedChildValue("system") + "#" + e.getNamedChildValue("code");
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.BUSINESSRULE,
					e.line(),
					e.col(),
					stack.getLiteralPath() + ".meta.profile[" + Integer.toString(i) + "]",
					!tags.contains(s),
					"Meta_RES_Security_Duplicate",
					new Object[]{s}
				)
					&& ok;
				tags.add(s);
				++i;
			}
		}

		return ok;
	}

	private boolean validateCapabilityStatement(List<ValidationMessage> errors, Element cs, NodeStack stack) {
		boolean ok = true;
		int iRest = 0;

		for(Element rest : cs.getChildrenByName("rest")) {
			int iResource = 0;

			for(Element resource : rest.getChildrenByName("resource")) {
				int iSP = 0;

				for(Element searchParam : resource.getChildrenByName("searchParam")) {
					String ref = searchParam.getChildValue("definition");
					String type = searchParam.getChildValue("type");
					if (!Utilities.noString(ref)) {
						SearchParameter sp = (SearchParameter)this.context.fetchResource(SearchParameter.class, ref);
						if (sp != null) {
							ok = this.rule(
								errors,
								NO_RULE_DATE,
								IssueType.INVALID,
								searchParam.line(),
								searchParam.col(),
								stack.getLiteralPath() + ".rest[" + iRest + "].resource[" + iResource + "].searchParam[" + iSP + "]",
								sp.getType().toCode().equals(type),
								"CapabalityStatement_CS_SP_WrongType",
								new Object[]{sp.getVersionedUrl(), sp.getType().toCode(), type}
							)
								&& ok;
						}
					}

					++iSP;
				}

				++iResource;
			}

			++iRest;
		}

		return ok;
	}

	private boolean validateContains(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		String path,
		ElementDefinition child,
		ElementDefinition context,
		Element resource,
		Element element,
		NodeStack stack,
		IdStatus idstatus,
		StructureDefinition parentProfile,
		PercentageTracker pct,
		ValidationMode mode
	) throws FHIRException {
		boolean ok = true;
		if (element.isNull()) {
			if (!this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.INVALID,
				element.line(),
				element.col(),
				stack.getLiteralPath(),
				ToolingExtensions.readBooleanExtension(child, "http://hl7.org/fhir/tools/StructureDefinition/json-nullable"),
				"ELEMENT_CANNOT_BE_NULL",
				new Object[0]
			)) {
				ok = false;
			}
		} else {
			SpecialElement special = element.getSpecial();
			ContainedReferenceValidationPolicy containedValidationPolicy = this.getPolicyAdvisor() == null
				? ContainedReferenceValidationPolicy.CHECK_VALID
				: this.getPolicyAdvisor().policyForContained(this, hostContext, context.fhirType(), context.getId(), special, path, parentProfile.getUrl());
			if (containedValidationPolicy.ignore()) {
				return ok;
			}

			String resourceName = element.getType();
			TypeRefComponent typeForResource = null;
			CommaSeparatedStringBuilder bt = new CommaSeparatedStringBuilder();

			for(TypeRefComponent type : child.getType()) {
				bt.append(type.getCode());
				if (type.getCode().equals("Resource") || type.getCode().equals(resourceName)) {
					typeForResource = type;
					break;
				}
			}

			stack.pathComment(resourceName + "/" + element.getIdBase());
			if (typeForResource == null) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INFORMATIONAL,
					element.line(),
					element.col(),
					stack.getLiteralPath(),
					false,
					"Bundle_BUNDLE_Entry_Type",
					new Object[]{resourceName, bt.toString()}
				)
					&& ok;
			} else if (this.isValidResourceType(resourceName, typeForResource)) {
				if (containedValidationPolicy.checkValid()) {
					ValidatorHostContext hc = null;
					if (special != SpecialElement.BUNDLE_ENTRY
						&& special != SpecialElement.BUNDLE_OUTCOME
						&& special != SpecialElement.BUNDLE_ISSUES
						&& special != SpecialElement.PARAMETER) {
						hc = hostContext.forContained(element);
					} else {
						resource = element;

						assert Utilities.existsInList(hostContext.getResource().fhirType(), new String[]{"Bundle", "Parameters"}) : "Containing Resource is "
							+ hostContext.getResource().fhirType()
							+ ", expected Bundle or Parameters at "
							+ stack.getLiteralPath();

						hc = hostContext.forEntry(element, hostContext.getResource());
					}

					stack.resetIds();
					if (special != null) {
						switch(special) {
							case BUNDLE_ENTRY:
							case BUNDLE_OUTCOME:
							case PARAMETER:
								idstatus = IdStatus.OPTIONAL;
								break;
							case CONTAINED:
								stack.setContained(true);
								idstatus = IdStatus.REQUIRED;
						}
					}

					if (typeForResource.getProfile().size() == 1) {
						long t = System.nanoTime();
						StructureDefinition profile = (StructureDefinition)this.context
							.fetchResource(StructureDefinition.class, ((CanonicalType)typeForResource.getProfile().get(0)).asStringValue(), parentProfile);
						this.timeTracker.sd(t);
						this.trackUsage(profile, hostContext, element);
						if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							element.line(),
							element.col(),
							stack.getLiteralPath(),
							profile != null,
							"Bundle_BUNDLE_Entry_NoProfile_EXPL",
							new Object[]{special.toHuman(), resourceName, ((CanonicalType)typeForResource.getProfile().get(0)).asStringValue()}
						)) {
							ok = this.validateResource(hc, errors, resource, element, profile, idstatus, stack, pct, mode) && ok;
						} else {
							ok = false;
						}
					} else if (typeForResource.getProfile().isEmpty()) {
						long t = System.nanoTime();
						StructureDefinition profile = (StructureDefinition)this.context
							.fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + resourceName);
						this.timeTracker.sd(t);
						this.trackUsage(profile, hostContext, element);
						if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							element.line(),
							element.col(),
							stack.getLiteralPath(),
							profile != null,
							"Bundle_BUNDLE_Entry_NoProfile_TYPE",
							new Object[]{special == null ? "??" : special.toHuman(), resourceName}
						)) {
							ok = this.validateResource(hc, errors, resource, element, profile, idstatus, stack, pct, mode) && ok;
						} else {
							ok = false;
						}
					} else {
						CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

						for(CanonicalType u : typeForResource.getProfile()) {
							b.append(u.asStringValue());
						}

						ok = this.rulePlural(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							element.line(),
							element.col(),
							stack.getLiteralPath(),
							false,
							typeForResource.getProfile().size(),
							"BUNDLE_BUNDLE_ENTRY_MULTIPLE_PROFILES",
							new Object[]{special.toHuman(), typeForResource.getCode(), b.toString()}
						)
							&& ok;
					}
				}
			} else {
				List<String> types = new ArrayList();

				for(UriType u : typeForResource.getProfile()) {
					StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, (String)u.getValue(), parentProfile);
					if (sd != null && !types.contains(sd.getType())) {
						types.add(sd.getType());
					}
				}

				if (types.size() == 1) {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INFORMATIONAL,
						element.line(),
						element.col(),
						stack.getLiteralPath(),
						false,
						"Bundle_BUNDLE_Entry_Type2",
						new Object[]{resourceName, types.get(0)}
					)
						&& ok;
				} else {
					ok = this.rulePlural(
						errors,
						NO_RULE_DATE,
						IssueType.INFORMATIONAL,
						element.line(),
						element.col(),
						stack.getLiteralPath(),
						false,
						types.size(),
						"Bundle_BUNDLE_Entry_Type3",
						new Object[]{resourceName, types}
					)
						&& ok;
				}
			}
		}

		return ok;
	}

	private boolean isValidResourceType(String type, TypeRefComponent def) {
		if (!def.hasProfile() && def.getCode().equals("Resource")) {
			return true;
		} else if (def.getCode().equals(type)) {
			return true;
		} else {
			List<StructureDefinition> list = new ArrayList();

			for(UriType u : def.getProfile()) {
				StructureDefinition sdt = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, (String)u.getValue());
				if (sdt != null) {
					list.add(sdt);
				}
			}

			for(StructureDefinition sdt = this.context.fetchTypeDefinition(type);
				 sdt != null;
				 sdt = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, sdt.getBaseDefinition(), sdt)
			) {
				if (def.getWorkingCode().equals("Resource")) {
					for(StructureDefinition sd : list) {
						if (sd.getUrl().equals(sdt.getUrl())) {
							return true;
						}

						if (sd.getType().equals(sdt.getType())) {
							return true;
						}
					}
				}
			}

			return false;
		}
	}

	private boolean validateElement(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		StructureDefinition profile,
		ElementDefinition definition,
		StructureDefinition cprofile,
		ElementDefinition context,
		Element resource,
		Element element,
		String actualType,
		NodeStack stack,
		boolean inCodeableConcept,
		boolean checkDisplayInContext,
		String extensionUrl,
		PercentageTracker pct,
		ValidationMode mode
	) throws FHIRException {
		pct.seeElement(element);
		String id = element.getChildValue("id");
		if (!Utilities.noString(id)) {
			if (stack.getIds().containsKey(id) && stack.getIds().get(id) != element) {
				this.rule(errors, NO_RULE_DATE, IssueType.BUSINESSRULE, element.line(), element.col(), stack.getLiteralPath(), false, "DUPLICATE_ID", new Object[]{id});
			}

			if (!stack.isResetPoint()) {
				stack.getIds().put(id, element);
			}
		}

		if (definition.getPath().equals("StructureDefinition.snapshot")) {
			stack.resetIds();
		}

		ValidationInfo vi = element.addDefinition(profile, definition, mode);
		boolean ok = true;
		ok = this.checkInvariants(hostContext, errors, profile, definition, resource, element, stack, false) & ok;
		if (definition.getFixed() != null) {
			ok = this.checkFixedValue(
				errors, stack.getLiteralPath(), element, definition.getFixed(), profile.getVersionedUrl(), definition.getSliceName(), null, false
			)
				&& ok;
		}

		if (definition.getPattern() != null) {
			ok = this.checkFixedValue(
				errors, stack.getLiteralPath(), element, definition.getPattern(), profile.getVersionedUrl(), definition.getSliceName(), null, true
			)
				&& ok;
		}

		SourcedChildDefinitions childDefinitions = this.profileUtilities.getChildMap(profile, definition);
		if (childDefinitions.getList().isEmpty()) {
			if (actualType == null) {
				vi.setValid(false);
				return false;
			}

			childDefinitions = this.getActualTypeChildren(hostContext, element, actualType);
		} else if (definition.getType().size() > 1) {
			if (actualType == null) {
				vi.setValid(false);
				return false;
			}

			SourcedChildDefinitions typeChildDefinitions = this.getActualTypeChildren(hostContext, element, actualType);
			childDefinitions = this.mergeChildLists(childDefinitions, typeChildDefinitions, definition.getPath(), actualType);
		}

		List<ElementInfo> children = this.listChildren(element, stack);
		List<String> problematicPaths = this.assignChildren(hostContext, errors, profile, resource, stack, childDefinitions, children);
		ok = this.checkCardinalities(errors, profile, element, stack, childDefinitions, children, problematicPaths) && ok;

		for(ElementInfo ei : children) {
			ok = this.checkChild(
				hostContext, errors, profile, definition, resource, element, actualType, stack, inCodeableConcept, checkDisplayInContext, ei, extensionUrl, pct, mode
			)
				&& ok;
		}

		vi.setValid(ok);
		return ok;
	}

	private SourcedChildDefinitions mergeChildLists(SourcedChildDefinitions source, SourcedChildDefinitions additional, String masterPath, String typePath) {
		SourcedChildDefinitions res = new SourcedChildDefinitions(additional.getSource(), new ArrayList());
		res.getList().addAll(source.getList());

		for(ElementDefinition ed : additional.getList()) {
			boolean inMaster = false;

			for(ElementDefinition t : source.getList()) {
				String tp = masterPath + ed.getPath().substring(typePath.length());
				if (t.getPath().equals(tp)) {
					inMaster = true;
				}
			}

			if (!inMaster) {
				res.getList().add(ed);
			}
		}

		return res;
	}

	public SourcedChildDefinitions getActualTypeChildren(ValidatorHostContext hostContext, Element element, String actualType) {
		StructureDefinition dt = null;
		if (this.isAbsolute(actualType)) {
			dt = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, actualType);
		} else {
			dt = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + actualType);
		}

		if (dt == null) {
			throw new DefinitionException(this.context.formatMessage("Unable_to_resolve_actual_type_", new Object[]{actualType}));
		} else {
			this.trackUsage(dt, hostContext, element);
			return this.profileUtilities.getChildMap(dt, (ElementDefinition)dt.getSnapshot().getElement().get(0));
		}
	}

	public boolean checkChild(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		StructureDefinition profile,
		ElementDefinition definition,
		Element resource,
		Element element,
		String actualType,
		NodeStack stack,
		boolean inCodeableConcept,
		boolean checkDisplayInContext,
		ElementInfo ei,
		String extensionUrl,
		PercentageTracker pct,
		ValidationMode mode
	) throws FHIRException, DefinitionException {
		boolean ok = true;
		if (this.debug && ei.definition != null && ei.slice != null) {
			System.out
				.println(Utilities.padLeft("", ' ', stack.depth()) + "Check " + ei.getPath() + " against both " + ei.definition.getId() + " and " + ei.slice.getId());
		}

		if (ei.definition != null) {
			if (this.debug) {
				System.out
					.println(
						Utilities.padLeft("", ' ', stack.depth())
							+ "Check "
							+ ei.getPath()
							+ " against defn "
							+ ei.definition.getId()
							+ " from "
							+ profile.getVersionedUrl()
							+ this.time()
					);
			}

			ok = this.checkChildByDefinition(
				hostContext,
				errors,
				profile,
				definition,
				resource,
				element,
				actualType,
				stack,
				inCodeableConcept,
				checkDisplayInContext,
				ei,
				extensionUrl,
				ei.definition,
				false,
				pct,
				mode
			)
				&& ok;
		}

		if (ei.slice != null) {
			if (this.debug) {
				System.out.println(Utilities.padLeft("", ' ', stack.depth()) + "Check " + ei.getPath() + " against slice " + ei.slice.getId() + this.time());
			}

			ok = this.checkChildByDefinition(
				hostContext,
				errors,
				profile,
				definition,
				resource,
				element,
				actualType,
				stack,
				inCodeableConcept,
				checkDisplayInContext,
				ei,
				extensionUrl,
				ei.slice,
				true,
				pct,
				mode
			)
				&& ok;
		}

		return ok;
	}

	private String time() {
		long t = System.currentTimeMillis();
		String s = " " + (t - this.start) + "ms";
		this.start = t;
		return s;
	}

	public boolean checkChildByDefinition(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		StructureDefinition profile,
		ElementDefinition definition,
		Element resource,
		Element element,
		String actualType,
		NodeStack stack,
		boolean inCodeableConcept,
		boolean checkDisplayInContext,
		ElementInfo ei,
		String extensionUrl,
		ElementDefinition checkDefn,
		boolean isSlice,
		PercentageTracker pct,
		ValidationMode mode
	) {
		boolean ok = true;
		List<String> profiles = new ArrayList();
		String type = null;
		ElementDefinition typeDefn = null;
		this.checkMustSupport(profile, ei);
		long s = System.currentTimeMillis();
		if (checkDefn.getType().size() == 1
			&& !"*".equals(((TypeRefComponent)checkDefn.getType().get(0)).getWorkingCode())
			&& !"Element".equals(((TypeRefComponent)checkDefn.getType().get(0)).getWorkingCode())
			&& !"BackboneElement".equals(((TypeRefComponent)checkDefn.getType().get(0)).getWorkingCode())) {
			type = ((TypeRefComponent)checkDefn.getType().get(0)).getWorkingCode();
			String stype = ei.getElement().fhirType();
			if (!stype.equals(type)) {
				if (checkDefn.isChoice()) {
					if (extensionUrl != null && !this.isAbsolute(extensionUrl)) {
						ok = this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							element.line(),
							element.col(),
							ei.getPath(),
							false,
							"Extension_PROF_Type",
							new Object[]{profile.getVersionedUrl(), type, stype}
						)
							&& ok;
					} else if (!this.isAbstractType(type) && !"Extension".equals(profile.getType())) {
						ok = this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							element.line(),
							element.col(),
							ei.getPath(),
							stype.equals(type),
							"Extension_PROF_Type",
							new Object[]{profile.getVersionedUrl(), type, stype}
						)
							&& ok;
					}
				} else if (this.isAbstractType(type)) {
					if (!this.isResource(type)) {
						type = stype;
					}
				} else {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.STRUCTURE,
						element.line(),
						element.col(),
						ei.getPath(),
						stype.equals(type) || Utilities.existsInList(type, new String[]{"string", "id"}) && Utilities.existsInList(stype, new String[]{"string", "id"}),
						"Extension_PROF_Type",
						new Object[]{profile.getVersionedUrl(), type, stype}
					)
						&& ok;
				}
			}

			if (((TypeRefComponent)checkDefn.getType().get(0)).hasProfile()) {
				for(CanonicalType p : ((TypeRefComponent)checkDefn.getType().get(0)).getProfile()) {
					profiles.add((String)p.getValue());
				}
			}
		} else if (checkDefn.getType().size() == 1 && "*".equals(((TypeRefComponent)checkDefn.getType().get(0)).getWorkingCode())) {
			String prefix = this.tail(checkDefn.getPath());

			assert prefix.endsWith("[x]");

			type = ei.getName().substring(prefix.length() - 3);
			if (this.isPrimitiveType(type)) {
				type = Utilities.uncapitalize(type);
			}

			if (((TypeRefComponent)checkDefn.getType().get(0)).hasProfile()) {
				for(CanonicalType p : ((TypeRefComponent)checkDefn.getType().get(0)).getProfile()) {
					profiles.add((String)p.getValue());
				}
			}
		} else if (checkDefn.getType().size() > 1) {
			String prefix = this.tail(checkDefn.getPath());

			assert this.typesAreAllReference(checkDefn.getType())
				|| checkDefn.hasRepresentation(PropertyRepresentation.TYPEATTR)
				|| prefix.endsWith("[x]")
				|| this.isResourceAndTypes(checkDefn) : "Multiple Types allowed, but name is wrong @ " + checkDefn.getPath() + ": " + checkDefn.typeSummaryVB();

			if (checkDefn.hasRepresentation(PropertyRepresentation.TYPEATTR)) {
				type = ei.getElement().getType();
			} else if (ei.getElement().isResource()) {
				type = ei.getElement().fhirType();
			} else {
				prefix = prefix.substring(0, prefix.length() - 3);

				for(TypeRefComponent t : checkDefn.getType()) {
					if ((prefix + Utilities.capitalize(t.getWorkingCode())).equals(ei.getName())) {
						type = t.getWorkingCode();
						if (t.hasProfile() && !type.equals("Reference")) {
							profiles.add((String)((CanonicalType)t.getProfile().get(0)).getValue());
						}
					}
				}
			}

			if (type == null) {
				TypeRefComponent trc = (TypeRefComponent)checkDefn.getType().get(0);
				if (trc.getWorkingCode().equals("Reference")) {
					type = "Reference";
				} else {
					ok = this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.STRUCTURE,
						ei.line(),
						ei.col(),
						stack.getLiteralPath(),
						false,
						"Validation_VAL_Profile_NoType",
						new Object[]{ei.getName(), this.describeTypes(checkDefn.getType())}
					)
						&& ok;
				}
			}
		} else if (checkDefn.getContentReference() != null) {
			typeDefn = this.resolveNameReference(profile.getSnapshot(), checkDefn.getContentReference());
		} else if (checkDefn.getType().size() == 1
			&& (
			"Element".equals(((TypeRefComponent)checkDefn.getType().get(0)).getWorkingCode())
				|| "BackboneElement".equals(((TypeRefComponent)checkDefn.getType().get(0)).getWorkingCode())
		)
			&& ((TypeRefComponent)checkDefn.getType().get(0)).hasProfile()) {
			CanonicalType pu = (CanonicalType)((TypeRefComponent)checkDefn.getType().get(0)).getProfile().get(0);
			if (pu.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-profile-element")) {
				profiles.add((String)pu.getValue() + "#" + pu.getExtensionString("http://hl7.org/fhir/StructureDefinition/elementdefinition-profile-element"));
			} else {
				profiles.add((String)pu.getValue());
			}
		}

		if (type != null && type.startsWith("@")) {
			checkDefn = this.findElement(profile, type.substring(1));
			if (isSlice) {
				ei.slice = ei.definition;
			} else {
				ei.definition = ei.definition;
			}

			type = null;
		}

		NodeStack localStack = stack.push(
			ei.getElement(),
			"*".equals(ei.getDefinition().getBase().getMax()) && ei.count == -1 ? 0 : ei.count,
			checkDefn,
			type == null ? typeDefn : this.resolveType(type, checkDefn.getType())
		);
		if (this.debug) {
			System.out
				.println("  check " + localStack.getLiteralPath() + " against " + ei.getDefinition().getId() + " in profile " + profile.getVersionedUrl() + this.time());
		}

		String localStackLiteralPath = localStack.getLiteralPath();
		String eiPath = ei.getPath();

		assert eiPath.equals(localStackLiteralPath) || eiPath.equals(localStackLiteralPath) : "ei.path: "
			+ ei.getPath()
			+ "  -  localStack.getLiteralPath: "
			+ localStackLiteralPath;

		boolean thisIsCodeableConcept = false;
		String thisExtension = null;
		boolean checkDisplay = true;
		SpecialElement special = ei.getElement().getSpecial();
		ok = this.checkInvariants(hostContext, errors, profile, typeDefn != null ? typeDefn : checkDefn, resource, ei.getElement(), localStack, false) && ok;
		ei.getElement().markValidation(profile, checkDefn);
		boolean elementValidated = false;
		if (type != null) {
			if (this.isPrimitiveType(type)) {
				ok = this.checkPrimitive(hostContext, errors, ei.getPath(), type, checkDefn, ei.getElement(), profile, stack) && ok;
			} else {
				if (checkDefn.hasFixed()) {
					ok = this.checkFixedValue(
						errors, ei.getPath(), ei.getElement(), checkDefn.getFixed(), profile.getVersionedUrl(), checkDefn.getSliceName(), null, false
					)
						&& ok;
				}

				if (checkDefn.hasPattern()) {
					ok = this.checkFixedValue(
						errors, ei.getPath(), ei.getElement(), checkDefn.getPattern(), profile.getVersionedUrl(), checkDefn.getSliceName(), null, true
					)
						&& ok;
				}
			}

			if (type.equals("Identifier")) {
				ok = this.checkIdentifier(errors, ei.getPath(), ei.getElement(), checkDefn);
			} else if (type.equals("Coding")) {
				ok = this.checkCoding(errors, ei.getPath(), ei.getElement(), profile, checkDefn, inCodeableConcept, checkDisplayInContext, stack);
			} else if (type.equals("Quantity")) {
				ok = this.checkQuantity(errors, ei.getPath(), ei.getElement(), profile, checkDefn, stack);
			} else if (type.equals("Attachment")) {
				ok = this.checkAttachment(errors, ei.getPath(), ei.getElement(), profile, checkDefn, inCodeableConcept, checkDisplayInContext, stack);
			} else if (type.equals("CodeableConcept")) {
				ok = checkDisplay = this.checkCodeableConcept(errors, ei.getPath(), ei.getElement(), profile, checkDefn, stack);
				thisIsCodeableConcept = true;
			} else if (type.equals("Reference")) {
				ok = this.checkReference(hostContext, errors, ei.getPath(), ei.getElement(), profile, checkDefn, actualType, localStack, pct, mode) && ok;
			} else if (type.equals("Extension")) {
				Element eurl = ei.getElement().getNamedChild("url");
				if (this.rule(errors, NO_RULE_DATE, IssueType.INVALID, ei.getPath(), eurl != null, "Extension_EXT_Url_NotFound", new Object[0])) {
					String url = eurl.primitiveValue();
					thisExtension = url;
					if (this.rule(errors, NO_RULE_DATE, IssueType.INVALID, ei.getPath(), !Utilities.noString(url), "Extension_EXT_Url_NotFound", new Object[0])) {
						if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							ei.getPath(),
							extensionUrl != null || Utilities.isAbsoluteUrl(url),
							"Extension_EXT_URL_Absolute",
							new Object[0]
						)) {
							this.checkExtension(
								hostContext, errors, ei.getPath(), resource, element, ei.getElement(), checkDefn, profile, localStack, stack, extensionUrl, pct, mode
							);
						} else {
							ok = false;
						}
					} else {
						ok = false;
					}
				}
			} else if (type.equals("Resource") || this.isResource(type)) {
				ok = this.validateContains(
					hostContext,
					errors,
					ei.getPath(),
					checkDefn,
					definition,
					resource,
					ei.getElement(),
					localStack,
					this.idStatusForEntry(element, ei),
					profile,
					pct,
					mode
				)
					&& ok;
				elementValidated = true;
			} else if (Utilities.isAbsoluteUrl(type)) {
				StructureDefinition defn = this.context.fetchTypeDefinition(type);
				if (defn != null && this.hasMapping("http://hl7.org/fhir/terminology-pattern", defn, defn.getSnapshot().getElementFirstRep())) {
					List<String> txtype = this.getMapping("http://hl7.org/fhir/terminology-pattern", defn, defn.getSnapshot().getElementFirstRep());
					if (txtype.contains("CodeableConcept")) {
						ok = this.checkTerminologyCodeableConcept(errors, ei.getPath(), ei.getElement(), profile, checkDefn, stack, defn) && ok;
						thisIsCodeableConcept = true;
					} else if (txtype.contains("Coding")) {
						ok = this.checkTerminologyCoding(errors, ei.getPath(), ei.getElement(), profile, checkDefn, inCodeableConcept, checkDisplayInContext, stack, defn)
							&& ok;
					}
				}
			}
		} else if (this.rule(
			errors,
			NO_RULE_DATE,
			IssueType.STRUCTURE,
			ei.line(),
			ei.col(),
			stack.getLiteralPath(),
			checkDefn != null,
			"Validation_VAL_Content_Unknown",
			new Object[]{ei.getName()}
		)) {
			ok = this.validateElement(hostContext, errors, profile, checkDefn, null, null, resource, ei.getElement(), type, localStack, false, true, null, pct, mode)
				&& ok;
		} else {
			ok = false;
		}

		StructureDefinition p = null;
		String tail = null;
		if (profiles.isEmpty()) {
			if (type != null) {
				p = this.getProfileForType(type, checkDefn.getType(), profile);
				ok = this.rule(errors, NO_RULE_DATE, IssueType.STRUCTURE, ei.line(), ei.col(), ei.getPath(), p != null, "Validation_VAL_NoType", new Object[]{type})
					&& ok;
			}
		} else if (profiles.size() == 1) {
			String url = (String)profiles.get(0);
			if (url.contains("#")) {
				tail = url.substring(url.indexOf("#") + 1);
				url = url.substring(0, url.indexOf("#"));
			}

			p = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, url, profile);
			ok = this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.STRUCTURE,
				ei.line(),
				ei.col(),
				ei.getPath(),
				p != null,
				"Validation_VAL_Unknown_Profile",
				new Object[]{profiles.get(0)}
			)
				&& ok;
		} else {
			elementValidated = true;
			HashMap<String, List<ValidationMessage>> goodProfiles = new HashMap();
			HashMap<String, List<ValidationMessage>> badProfiles = new HashMap();

			for(String typeProfile : profiles) {
				tail = null;
				if (typeProfile.contains("#")) {
					tail = typeProfile.substring(typeProfile.indexOf("#") + 1);
					String url = typeProfile.substring(0, typeProfile.indexOf("#"));
				}

				p = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, typeProfile);
				if (this.rule(
					errors, NO_RULE_DATE, IssueType.STRUCTURE, ei.line(), ei.col(), ei.getPath(), p != null, "Validation_VAL_Unknown_Profile", new Object[]{typeProfile}
				)) {
					List<ValidationMessage> profileErrors = new ArrayList();
					ok = this.validateElement(
						hostContext,
						profileErrors,
						p,
						this.getElementByTail(p, tail),
						profile,
						checkDefn,
						resource,
						ei.getElement(),
						type,
						localStack,
						thisIsCodeableConcept,
						checkDisplay,
						thisExtension,
						pct,
						mode
					)
						&& ok;
					if (this.hasErrors(profileErrors)) {
						badProfiles.put(typeProfile, profileErrors);
					} else {
						goodProfiles.put(typeProfile, profileErrors);
					}
				}
			}

			if (goodProfiles.size() == 1) {
				errors.addAll((Collection)goodProfiles.values().iterator().next());
			} else if (goodProfiles.size() == 0) {
				ok = this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					ei.line(),
					ei.col(),
					ei.getPath(),
					false,
					"Validation_VAL_Profile_NoMatch",
					new Object[]{StringUtils.join(new Object[]{"; ", profiles})}
				)
					&& ok;

				for(String m : badProfiles.keySet()) {
					p = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, m);

					for(ValidationMessage message : (List<ValidationMessage>)badProfiles.get(m)) {
						message.setMessage(
							message.getMessage() + " (validating against " + p.getUrl() + (p.hasVersion() ? "|" + p.getVersion() : "") + " [" + p.getName() + "])"
						);
						errors.add(message);
					}
				}
			} else {
				this.warningPlural(
					errors,
					NO_RULE_DATE,
					IssueType.STRUCTURE,
					ei.line(),
					ei.col(),
					ei.getPath(),
					false,
					goodProfiles.size(),
					"Validation_VAL_Profile_MultipleMatches",
					new Object[]{ResourceUtilities.listStrings(goodProfiles.keySet())}
				);

				for(String m : goodProfiles.keySet()) {
					p = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, m);

					for(ValidationMessage message : (List<ValidationMessage>)goodProfiles.get(m)) {
						message.setMessage(
							message.getMessage() + " (validating against " + p.getUrl() + (p.hasVersion() ? "|" + p.getVersion() : "") + " [" + p.getName() + "])"
						);
						errors.add(message);
					}
				}
			}
		}

		if (p != null) {
			this.trackUsage(p, hostContext, element);
			if (!elementValidated) {
				if (ei.getElement().getSpecial() != SpecialElement.BUNDLE_ENTRY
					&& ei.getElement().getSpecial() != SpecialElement.BUNDLE_OUTCOME
					&& ei.getElement().getSpecial() != SpecialElement.PARAMETER) {
					ok = this.validateElement(
						hostContext,
						errors,
						p,
						this.getElementByTail(p, tail),
						profile,
						checkDefn,
						resource,
						ei.getElement(),
						type,
						localStack,
						thisIsCodeableConcept,
						checkDisplay,
						thisExtension,
						pct,
						mode
					)
						&& ok;
				} else {
					ok = this.validateElement(
						hostContext,
						errors,
						p,
						this.getElementByTail(p, tail),
						profile,
						checkDefn,
						ei.getElement(),
						ei.getElement(),
						type,
						localStack.resetIds(),
						thisIsCodeableConcept,
						checkDisplay,
						thisExtension,
						pct,
						mode
					)
						&& ok;
				}
			}

			int index = profile.getSnapshot().getElement().indexOf(checkDefn);
			if (index < profile.getSnapshot().getElement().size() - 1) {
				String nextPath = ((ElementDefinition)profile.getSnapshot().getElement().get(index + 1)).getPath();
				if (!nextPath.equals(checkDefn.getPath()) && nextPath.startsWith(checkDefn.getPath())) {
					if (ei.getElement().getSpecial() == SpecialElement.BUNDLE_ENTRY
						|| ei.getElement().getSpecial() == SpecialElement.BUNDLE_OUTCOME
						|| ei.getElement().getSpecial() == SpecialElement.PARAMETER) {
						ok = this.validateElement(
							hostContext.forEntry(ei.getElement(), null),
							errors,
							profile,
							checkDefn,
							null,
							null,
							ei.getElement(),
							ei.getElement(),
							type,
							localStack,
							thisIsCodeableConcept,
							checkDisplay,
							thisExtension,
							pct,
							mode
						)
							&& ok;
					} else if (ei.getElement().getSpecial() == SpecialElement.CONTAINED) {
						ok = this.validateElement(
							hostContext.forContained(ei.getElement()),
							errors,
							profile,
							checkDefn,
							null,
							null,
							ei.getElement(),
							ei.getElement(),
							type,
							localStack,
							thisIsCodeableConcept,
							checkDisplay,
							thisExtension,
							pct,
							mode
						)
							&& ok;
					} else {
						ok = this.validateElement(
							hostContext,
							errors,
							profile,
							checkDefn,
							null,
							null,
							resource,
							ei.getElement(),
							type,
							localStack,
							thisIsCodeableConcept,
							checkDisplay,
							thisExtension,
							pct,
							mode
						)
							&& ok;
					}
				}
			}
		}

		return ok;
	}

	private boolean isAbstractType(String type) {
		StructureDefinition sd = this.context.fetchTypeDefinition(type);
		return sd != null && sd.getAbstract();
	}

	private boolean isResourceAndTypes(ElementDefinition ed) {
		if (!RESOURCE_X_POINTS.contains(ed.getBase().getPath())) {
			return false;
		} else {
			for(TypeRefComponent tr : ed.getType()) {
				if (!this.isResource(tr.getCode())) {
					return false;
				}
			}

			return true;
		}
	}

	private boolean isResource(String type) {
		StructureDefinition sd = this.context.fetchTypeDefinition(type);
		return sd != null && sd.getKind().equals(StructureDefinitionKind.RESOURCE);
	}

	private void trackUsage(StructureDefinition profile, ValidatorHostContext hostContext, Element element) {
		if (this.tracker != null) {
			this.tracker.recordProfileUsage(profile, hostContext.getAppContext(), element);
		}
	}

	private boolean hasMapping(String url, StructureDefinition defn, ElementDefinition elem) {
		String id = null;

		for(StructureDefinitionMappingComponent m : defn.getMapping()) {
			if (url.equals(m.getUri())) {
				id = m.getIdentity();
				break;
			}
		}

		if (id != null) {
			for(ElementDefinitionMappingComponent m : elem.getMapping()) {
				if (id.equals(m.getIdentity())) {
					return true;
				}
			}
		}

		return false;
	}

	private List<String> getMapping(String url, StructureDefinition defn, ElementDefinition elem) {
		List<String> res = new ArrayList();
		String id = null;

		for(StructureDefinitionMappingComponent m : defn.getMapping()) {
			if (url.equals(m.getUri())) {
				id = m.getIdentity();
				break;
			}
		}

		if (id != null) {
			for(ElementDefinitionMappingComponent m : elem.getMapping()) {
				if (id.equals(m.getIdentity())) {
					res.add(m.getMap());
				}
			}
		}

		return res;
	}

	public void checkMustSupport(StructureDefinition profile, ElementInfo ei) {
		String usesMustSupport = profile.getUserString("usesMustSupport");
		if (usesMustSupport == null) {
			usesMustSupport = "N";

			for(ElementDefinition pe : profile.getSnapshot().getElement()) {
				if (pe.getMustSupport()) {
					usesMustSupport = "Y";
					break;
				}
			}

			profile.setUserData("usesMustSupport", usesMustSupport);
		}

		if (usesMustSupport.equals("Y")) {
			String elementSupported = ei.getElement().getUserString("elementSupported");
			if ((elementSupported == null || ei.definition.getMustSupport()) && ei.definition.getMustSupport()) {
				ei.getElement().setUserData("elementSupported", "Y");
			}
		}
	}

	public boolean checkCardinalities(
		List<ValidationMessage> errors,
		StructureDefinition profile,
		Element element,
		NodeStack stack,
		SourcedChildDefinitions childDefinitions,
		List<ElementInfo> children,
		List<String> problematicPaths
	) throws DefinitionException {
		boolean ok = true;

		for(ElementDefinition ed : childDefinitions.getList()) {
			if (ed.getRepresentation().isEmpty()) {
				int count = 0;
				List<ElementDefinition> slices = null;
				if (ed.hasSlicing()) {
					slices = this.profileUtilities.getSliceList(profile, ed);
				}

				for(ElementInfo ei : children) {
					if (ei.definition == ed) {
						++count;
					} else if (slices != null) {
						for(ElementDefinition sed : slices) {
							if (ei.definition == sed) {
								++count;
								break;
							}
						}
					}
				}

				if (ed.getMin() > 0) {
					if (problematicPaths.contains(ed.getPath())) {
						this.hintPlural(
							errors,
							NO_RULE_DATE,
							IssueType.NOTSUPPORTED,
							element.line(),
							element.col(),
							stack.getLiteralPath(),
							count >= ed.getMin(),
							count,
							"Validation_VAL_Profile_NoCheckMin",
							new Object[]{
								profile.getVersionedUrl(), ed.getPath(), ed.getId(), ed.getSliceName(), ed.getLabel(), stack.getLiteralPath(), Integer.toString(ed.getMin())
							}
						);
					} else if (count < ed.getMin()) {
						ok = this.rulePlural(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							element.line(),
							element.col(),
							stack.getLiteralPath(),
							false,
							count,
							"Validation_VAL_Profile_Minimum",
							new Object[]{
								profile.getVersionedUrl(), ed.getPath(), ed.getId(), ed.getSliceName(), ed.getLabel(), stack.getLiteralPath(), Integer.toString(ed.getMin())
							}
						)
							&& ok;
					}
				}

				if (ed.hasMax() && !ed.getMax().equals("*")) {
					if (problematicPaths.contains(ed.getPath())) {
						this.hintPlural(
							errors,
							NO_RULE_DATE,
							IssueType.NOTSUPPORTED,
							element.line(),
							element.col(),
							stack.getLiteralPath(),
							count <= Integer.parseInt(ed.getMax()),
							count,
							"Validation_VAL_Profile_NoCheckMax",
							new Object[]{profile.getVersionedUrl(), ed.getPath(), ed.getId(), ed.getSliceName(), ed.getLabel(), stack.getLiteralPath(), ed.getMax()}
						);
					} else if (count > Integer.parseInt(ed.getMax())) {
						ok = this.rulePlural(
							errors,
							NO_RULE_DATE,
							IssueType.STRUCTURE,
							element.line(),
							element.col(),
							stack.getLiteralPath(),
							false,
							count,
							"Validation_VAL_Profile_Maximum",
							new Object[]{
								profile.getVersionedUrl(),
								ed.getPath(),
								ed.getId(),
								ed.getSliceName(),
								ed.getLabel(),
								stack.getLiteralPath(),
								ed.getMax(),
								Integer.toString(count)
							}
						)
							&& ok;
					}
				}
			}
		}

		return ok;
	}

	public List<String> assignChildren(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		StructureDefinition profile,
		Element resource,
		NodeStack stack,
		SourcedChildDefinitions childDefinitions,
		List<ElementInfo> children
	) throws DefinitionException {
		ElementDefinition slicer = null;
		boolean unsupportedSlicing = false;
		List<String> problematicPaths = new ArrayList();
		String slicingPath = null;
		int sliceOffset = 0;

		for(int i = 0; i < childDefinitions.getList().size(); ++i) {
			ElementDefinition ed = (ElementDefinition)childDefinitions.getList().get(i);
			boolean childUnsupportedSlicing = false;
			boolean process = true;
			if (ed.hasSlicing() && !ed.getSlicing().getOrdered()) {
				slicingPath = ed.getPath();
			} else if ((slicingPath == null || !ed.getPath().equals(slicingPath)) && slicingPath != null && !ed.getPath().startsWith(slicingPath)) {
				slicingPath = null;
			}

			if (ed.hasSlicing()) {
				if (slicer != null && slicer.getPath().equals(ed.getPath())) {
					String errorContext = "profile " + profile.getVersionedUrl();
					if (resource.hasChild("id") && !resource.getChildValue("id").isEmpty()) {
						errorContext = errorContext + "; instance " + resource.getChildValue("id");
					}

					throw new DefinitionException(
						this.context.formatMessage("Slice_encountered_midway_through_set_path___id___", new Object[]{slicer.getPath(), slicer.getId(), errorContext})
					);
				}

				slicer = ed;
				process = false;
				sliceOffset = i;
			} else if (slicer != null && !slicer.getPath().equals(ed.getPath())) {
				slicer = null;
			}

			for(ElementInfo ei : children) {
				if (ei.sliceInfo == null) {
					ei.sliceInfo = new ArrayList();
				}

				unsupportedSlicing = this.matchSlice(
					hostContext, errors, ei.sliceInfo, profile, stack, slicer, unsupportedSlicing, problematicPaths, sliceOffset, i, ed, childUnsupportedSlicing, ei
				);
			}
		}

		int last = -1;
		ElementInfo lastei = null;
		int lastSlice = -1;

		for(ElementInfo ei : children) {
			String sliceInfo = "";
			if (slicer != null) {
				sliceInfo = " (slice: " + slicer.getPath() + ")";
			}

			if (!unsupportedSlicing) {
				if (ei.additionalSlice && ei.definition != null) {
					if (ei.definition.getSlicing().getRules().equals(SlicingRules.OPEN) || ei.definition.getSlicing().getRules().equals(SlicingRules.OPENATEND)) {
						this.slicingHint(
							errors,
							NO_RULE_DATE,
							IssueType.INFORMATIONAL,
							ei.line(),
							ei.col(),
							ei.getPath(),
							false,
							this.isProfile(slicer) || this.isCritical(ei.sliceInfo),
							this.context
								.formatMessage(
									"This_element_does_not_match_any_known_slice_", new Object[]{profile == null ? "" : "defined in the profile " + profile.getVersionedUrl()}
								),
							this.context
								.formatMessage(
									"This_element_does_not_match_any_known_slice_",
									new Object[]{profile == null ? "" : this.context.formatMessage("defined_in_the_profile", new Object[0]) + " " + profile.getVersionedUrl()}
								)
								+ this.errorSummaryForSlicingAsHtml(ei.sliceInfo),
							this.errorSummaryForSlicingAsText(ei.sliceInfo)
						);
					} else if (ei.definition.getSlicing().getRules().equals(SlicingRules.CLOSED)) {
						this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							ei.line(),
							ei.col(),
							ei.getPath(),
							false,
							"Validation_VAL_Profile_NotSlice",
							new Object[]{profile == null ? "" : " defined in the profile " + profile.getVersionedUrl(), this.errorSummaryForSlicing(ei.sliceInfo)}
						);
					}
				} else if (!childDefinitions.getSource().getAbstract()) {
					this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.NOTSUPPORTED,
						ei.line(),
						ei.col(),
						ei.getPath(),
						ei.definition != null,
						"Validation_VAL_Profile_NotAllowed",
						new Object[]{profile.getVersionedUrl()}
					);
				}
			}

			boolean isXmlAttr = false;
			if (ei.definition != null) {
				for(Enumeration<PropertyRepresentation> r : ei.definition.getRepresentation()) {
					if (r.getValue() == PropertyRepresentation.XMLATTR) {
						isXmlAttr = true;
						break;
					}
				}
			}

			if (!ToolingExtensions.readBoolExtension(profile, "http://hl7.org/fhir/StructureDefinition/structuredefinition-xml-no-order")) {
				boolean ok = ei.definition == null || ei.index >= last || isXmlAttr || ei.getElement().isIgnorePropertyOrder();
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					ei.line(),
					ei.col(),
					ei.getPath(),
					ok,
					"Validation_VAL_Profile_OutOfOrder",
					new Object[]{profile.getVersionedUrl(), ei.getName(), lastei == null ? "(null)" : lastei.getName()}
				);
			}

			if (ei.slice != null && ei.index == last && ei.slice.getSlicing().getOrdered()) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVALID,
					ei.line(),
					ei.col(),
					ei.getPath(),
					ei.definition == null || ei.sliceindex >= lastSlice || isXmlAttr,
					"Validation_VAL_Profile_SliceOrder",
					new Object[]{profile.getVersionedUrl(), ei.getName()}
				);
			}

			if (ei.definition == null || !isXmlAttr) {
				last = ei.index;
				lastei = ei;
			}

			if (ei.slice != null) {
				lastSlice = ei.sliceindex;
			} else {
				lastSlice = -1;
			}
		}

		return problematicPaths;
	}

	public List<ElementInfo> listChildren(Element element, NodeStack stack) {
		List<ElementInfo> children = new ArrayList();
		ChildIterator iter = new ChildIterator(this, stack.getLiteralPath(), element);

		while(iter.next()) {
			children.add(new ElementInfo(iter.name(), iter.element(), iter.path(), iter.count()));
		}

		return children;
	}

	public boolean checkInvariants(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		StructureDefinition profile,
		ElementDefinition definition,
		Element resource,
		Element element,
		NodeStack stack,
		boolean onlyNonInherited
	) throws FHIRException {
		return this.checkInvariants(hostContext, errors, stack.getLiteralPath(), profile, definition, null, null, resource, element, onlyNonInherited);
	}

	public boolean matchSlice(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		List<ValidationMessage> sliceInfo,
		StructureDefinition profile,
		NodeStack stack,
		ElementDefinition slicer,
		boolean unsupportedSlicing,
		List<String> problematicPaths,
		int sliceOffset,
		int i,
		ElementDefinition ed,
		boolean childUnsupportedSlicing,
		ElementInfo ei
	) {
		boolean match = false;
		if (slicer != null && slicer != ed) {
			if (this.nameMatches(ei.getName(), this.tail(ed.getPath()))) {
				try {
					match = this.sliceMatches(hostContext, ei.getElement(), ei.getPath(), slicer, ed, profile, errors, sliceInfo, stack, profile);
					if (match) {
						ei.slice = slicer;
						ei.additionalSlice = false;
					} else if (ei.slice == null) {
						ei.additionalSlice = true;
					}
				} catch (FHIRException var19) {
					this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.PROCESSING,
						ei.line(),
						ei.col(),
						ei.getPath(),
						false,
						"SLICING_CANNOT_BE_EVALUATED",
						new Object[]{var19.getMessage()}
					);
					unsupportedSlicing = true;
					childUnsupportedSlicing = true;
				}
			}
		} else {
			match = this.nameMatches(ei.getName(), this.tail(ed.getPath()));
		}

		if (match) {
			boolean update = true;
			boolean isOk = ei.definition == null
				|| ei.definition == slicer
				|| ei.definition.getPath().endsWith("[x]") && ed.getPath().startsWith(ei.definition.getPath().replace("[x]", ""));
			if (!isOk) {
				String existingName = ei.definition != null && ei.definition.hasSliceName() ? ei.definition.getSliceName() : null;
				String matchingName = ed.hasSliceName() ? ed.getSliceName() : null;
				if (existingName != null && matchingName != null) {
					if (matchingName.startsWith(existingName + "/")) {
						isOk = true;
					} else if (existingName.startsWith(matchingName + "/")) {
						update = false;
						isOk = true;
					}
				}
			}

			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.INVALID,
				ei.line(),
				ei.col(),
				ei.getPath(),
				isOk,
				"Validation_VAL_Profile_MatchMultiple",
				new Object[]{
					profile.getVersionedUrl(),
					ei.definition != null && ei.definition.hasSliceName() ? ei.definition.getSliceName() : "",
					ed.hasSliceName() ? ed.getSliceName() : ""
				}
			)
				&& update) {
				ei.definition = ed;
				if (ei.slice == null) {
					ei.index = i;
				} else {
					ei.index = sliceOffset;
					ei.sliceindex = i - (sliceOffset + 1);
				}
			}
		} else if (childUnsupportedSlicing) {
			problematicPaths.add(ed.getPath());
		}

		return unsupportedSlicing;
	}

	private String slicingSummary(ElementDefinitionSlicingComponent slicing) {
		StringBuilder b = new StringBuilder();
		b.append('[');
		boolean first = true;

		for(ElementDefinitionSlicingDiscriminatorComponent t : slicing.getDiscriminator()) {
			if (first) {
				first = false;
			} else {
				b.append(",");
			}

			b.append(t.getType().toCode());
			b.append(":");
			b.append(t.getPath());
		}

		b.append(']');
		b.append(slicing.getOrdered() ? ";ordered" : "");
		b.append(slicing.getRules().toString());
		return b.toString();
	}

	private ElementDefinition getElementByTail(StructureDefinition p, String tail) throws DefinitionException {
		if (tail == null) {
			return (ElementDefinition)p.getSnapshot().getElement().get(0);
		} else {
			for(ElementDefinition t : p.getSnapshot().getElement()) {
				if (tail.equals(t.getId())) {
					return t;
				}
			}

			throw new DefinitionException(this.context.formatMessage("Unable_to_find_element_with_id_", new Object[]{tail}));
		}
	}

	private IdStatus idStatusForEntry(Element ep, ElementInfo ei) {
		if (ei.getDefinition().hasExtension("http://hl7.org/fhir/tools/StructureDefinition/id-expectation")) {
			return IdStatus.fromCode(ToolingExtensions.readStringExtension(ei.getDefinition(), "http://hl7.org/fhir/tools/StructureDefinition/id-expectation"));
		} else if (this.isBundleEntry(ei.getPath())) {
			Element req = ep.getNamedChild("request");
			Element resp = ep.getNamedChild("response");
			Element fullUrl = ep.getNamedChild("fullUrl");
			Element method = null;
			Element url = null;
			if (req != null) {
				method = req.getNamedChild("method");
				url = req.getNamedChild("url");
			}

			if (resp != null) {
				return IdStatus.OPTIONAL;
			} else if (method == null) {
				if (fullUrl == null) {
					return IdStatus.REQUIRED;
				} else {
					return !fullUrl.primitiveValue().startsWith("urn:uuid:") && !fullUrl.primitiveValue().startsWith("urn:oid:") ? IdStatus.REQUIRED : IdStatus.OPTIONAL;
				}
			} else {
				String s = method.primitiveValue();
				if (s.equals("PUT")) {
					return url == null ? IdStatus.REQUIRED : IdStatus.OPTIONAL;
				} else {
					return s.equals("POST") ? IdStatus.OPTIONAL : IdStatus.OPTIONAL;
				}
			}
		} else {
			return !this.isParametersEntry(ei.getPath()) && !this.isBundleOutcome(ei.getPath()) ? IdStatus.REQUIRED : IdStatus.OPTIONAL;
		}
	}

	private boolean checkInvariants(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		String path,
		StructureDefinition profile,
		ElementDefinition ed,
		String typename,
		String typeProfile,
		Element resource,
		Element element,
		boolean onlyNonInherited
	) throws FHIRException, FHIRException {
		if (this.noInvariantChecks) {
			return true;
		} else {
			boolean ok = true;

			for(ElementDefinitionConstraintComponent inv : ed.getConstraint()) {
				if (inv.hasExpression()
					&& (
					!onlyNonInherited
						|| !inv.hasSource()
						|| !this.isInheritedProfile(profile, inv.getSource()) && !this.isInheritedProfile(ed.getType(), inv.getSource())
				)) {
					Map<String, List<ValidationMessage>> invMap = this.executionId.equals(element.getUserString("validator.execution.id"))
						? (Map)element.getUserData("validator.executed.invariant.list")
						: null;
					if (invMap == null) {
						invMap = new HashMap();
						element.setUserData("validator.executed.invariant.list", invMap);
						element.setUserData("validator.execution.id", this.executionId);
					}

					List<ValidationMessage> invErrors = null;
					String key = FHIRPathExpressionFixer.fixExpr(inv.getExpression(), inv.getKey(), this.context.getVersion());
					ArrayList var17;
					if (!invMap.keySet().contains(key)) {
						var17 = new ArrayList();
						invMap.put(key, var17);
						ok = this.checkInvariant(hostContext, var17, path, profile, resource, element, inv) && ok;
					} else {
						var17 = (ArrayList)invMap.get(key);
					}

					errors.addAll(var17);
				}
			}

			return ok;
		}
	}

	private boolean isInheritedProfile(List<TypeRefComponent> types, String source) {
		for(TypeRefComponent type : types) {
			for(CanonicalType c : type.getProfile()) {
				StructureDefinition sd = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, c.asStringValue());
				if (sd != null) {
					if (sd.getUrl().equals(source)) {
						return true;
					}

					if (this.isInheritedProfile(sd, source)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean isInheritedProfile(StructureDefinition profile, String source) {
		if (source.equals(profile.getUrl())) {
			return false;
		} else {
			while(profile != null) {
				profile = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, profile.getBaseDefinition(), profile);
				if (profile != null && source.equals(profile.getUrl())) {
					return true;
				}
			}

			return false;
		}
	}

	public boolean checkInvariant(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		String path,
		StructureDefinition profile,
		Element resource,
		Element element,
		ElementDefinitionConstraintComponent inv
	) throws FHIRException {
		if (this.debug) {
			System.out.println("inv " + inv.getKey() + " on " + path + " in " + resource.fhirType() + " {{ " + inv.getExpression() + " }}" + this.time());
		}

		ExpressionNode n = (ExpressionNode)inv.getUserData("validator.expression.cache");
		if (n == null) {
			long t = System.nanoTime();

			try {
				String expr = FHIRPathExpressionFixer.fixExpr(inv.getExpression(), inv.getKey(), this.context.getVersion());
				n = this.fpe.parse(expr);
			} catch (FHIRException var14) {
				this.rule(
					errors,
					NO_RULE_DATE,
					IssueType.INVARIANT,
					element.line(),
					element.col(),
					path,
					false,
					"Problem_processing_expression__in_profile__path__",
					new Object[]{inv.getExpression(), profile.getVersionedUrl(), path, var14.getMessage()}
				);
				return false;
			}

			this.timeTracker.fpe(t);
			inv.setUserData("validator.expression.cache", n);
		}

		boolean ok;
		String msg;
		try {
			long t = System.nanoTime();
			ok = this.fpe.evaluateToBoolean(hostContext, resource, hostContext.getRootResource(), element, n);
			this.timeTracker.fpe(t);
			msg = this.fpe.forLog();
		} catch (Exception var13) {
			ok = false;
			msg = var13.getClass().getName() + ": " + var13.getMessage();
			var13.printStackTrace();
		}

		if (!ok) {
			if (this.wantInvariantInMessage) {
				msg = msg + " (inv = " + n.toString() + ")";
			}

			if (!Utilities.noString(msg)) {
				msg = msg + " (log: " + msg + ")";
			}

			msg = this.context.formatMessage("INV_FAILED", new Object[]{inv.getKey() + ": '" + inv.getHuman() + "'"}) + msg;
			if (!inv.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-bestpractice")
				|| !ToolingExtensions.readBooleanExtension(inv, "http://hl7.org/fhir/StructureDefinition/elementdefinition-bestpractice")) {
				if (inv.getSeverity() == ConstraintSeverity.ERROR) {
					this.rule(errors, NO_RULE_DATE, IssueType.INVARIANT, element.line(), element.col(), path, ok, msg, new Object[0]);
				} else if (inv.getSeverity() == ConstraintSeverity.WARNING) {
					this.warning(errors, NO_RULE_DATE, IssueType.INVARIANT, element.line(), element.col(), path, ok, msg, new Object[0]);
				}
			} else if (this.bpWarnings == BestPracticeWarningLevel.Hint) {
				this.hint(errors, NO_RULE_DATE, IssueType.INVARIANT, element.line(), element.col(), path, ok, msg);
			} else if (this.bpWarnings == BestPracticeWarningLevel.Warning) {
				this.warning(errors, NO_RULE_DATE, IssueType.INVARIANT, element.line(), element.col(), path, ok, msg, new Object[0]);
			} else if (this.bpWarnings == BestPracticeWarningLevel.Error) {
				this.rule(errors, NO_RULE_DATE, IssueType.INVARIANT, element.line(), element.col(), path, ok, msg, new Object[0]);
			}
		}

		return ok;
	}

	private boolean validateObservation(List<ValidationMessage> errors, Element element, NodeStack stack) {
		boolean ok = true;
		ok = this.bpCheck(
			errors,
			IssueType.INVALID,
			element.line(),
			element.col(),
			stack.getLiteralPath(),
			element.getNamedChild("subject") != null,
			"All_observations_should_have_a_subject"
		)
			&& ok;
		List<Element> performers = new ArrayList();
		element.getNamedChildren("performer", performers);
		ok = this.bpCheck(
			errors, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), performers.size() > 0, "All_observations_should_have_a_performer"
		)
			&& ok;
		return this.bpCheck(
			errors,
			IssueType.INVALID,
			element.line(),
			element.col(),
			stack.getLiteralPath(),
			element.getNamedChild("effectiveDateTime") != null || element.getNamedChild("effectivePeriod") != null,
			"All_observations_should_have_an_effectiveDateTime_or_an_effectivePeriod"
		)
			&& ok;
	}

	private boolean validateResource(
		ValidatorHostContext hostContext,
		List<ValidationMessage> errors,
		Element resource,
		Element element,
		StructureDefinition defn,
		IdStatus idstatus,
		NodeStack stack,
		PercentageTracker pct,
		ValidationMode mode
	) throws FHIRException {
		boolean ok = true;

		assert stack != null;

		assert resource != null;

		boolean rok = true;
		String resourceName = element.getType();
		if (defn == null) {
			long t = System.nanoTime();
			defn = element.getProperty().getStructure();
			if (defn == null) {
				defn = (StructureDefinition)this.context.fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + resourceName);
			}

			this.timeTracker.sd(t);
			rok = this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.INVALID,
				element.line(),
				element.col(),
				stack.addToLiteralPath(new String[]{resourceName}),
				defn != null,
				"Validation_VAL_Profile_NoDefinition",
				new Object[]{resourceName}
			);
			ok = rok && ok;
		}

		if (!this.typeMatchesDefn(resourceName, defn) && resourceName.equals("Bundle")) {
			NodeStack first = this.getFirstEntry(stack);
			if (first != null && this.typeMatchesDefn(first.getElement().getType(), defn)) {
				element = first.getElement();
				stack = first;
				resourceName = element.getType();
				idstatus = IdStatus.OPTIONAL;
			}
		}

		if (rok) {
			if (idstatus == IdStatus.REQUIRED && element.getNamedChild("id") == null) {
				ok = this.rule(
					errors, NO_RULE_DATE, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), false, "Resource_RES_ID_Missing", new Object[0]
				)
					&& ok;
			} else if (idstatus == IdStatus.PROHIBITED && element.getNamedChild("id") != null) {
				ok = this.rule(
					errors, NO_RULE_DATE, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), false, "Resource_RES_ID_Prohibited", new Object[0]
				)
					&& ok;
			}

			if (element.getNamedChild("id") != null) {
				Element eid = element.getNamedChild("id");
				if (eid.getProperty() != null
					&& eid.getProperty().getDefinition() != null
					&& eid.getProperty().getDefinition().getBase().getPath().equals("Resource.id")) {
					NodeStack ns = stack.push(eid, -1, eid.getProperty().getDefinition(), null);
					if (eid.primitiveValue() != null && eid.primitiveValue().length() > 64) {
						if (this.rule(
							errors,
							NO_RULE_DATE,
							IssueType.INVALID,
							eid.line(),
							eid.col(),
							ns.getLiteralPath(),
							false,
							"Resource_RES_ID_Malformed_Length",
							new Object[]{eid.primitiveValue().length()}
						)
							&& ok) {
							boolean var19 = true;
						} else {
							boolean var18 = false;
						}
					} else if (this.rule(
						errors,
						NO_RULE_DATE,
						IssueType.INVALID,
						eid.line(),
						eid.col(),
						ns.getLiteralPath(),
						FormatUtilities.isValidId(eid.primitiveValue()),
						"Resource_RES_ID_Malformed_Chars",
						new Object[]{eid.primitiveValue()}
					)
						&& ok) {
						boolean var17 = true;
					} else {
						boolean var10000 = false;
					}
				}
			}

			if (this.rule(
				errors,
				NO_RULE_DATE,
				IssueType.INVALID,
				element.line(),
				element.col(),
				stack.getLiteralPath(),
				resourceName.equals(defn.getType()) || resourceName.equals(defn.getTypeTail()),
				"Validation_VAL_Profile_WrongType",
				new Object[]{defn.getType(), resourceName, defn.getVersionedUrl()}
			)) {
				ok = this.start(hostContext, errors, element, element, defn, stack, pct, mode);
			} else {
				ok = false;
			}
		}

		return ok;
	}

	private boolean typeMatchesDefn(String name, StructureDefinition defn) {
		if (defn.getKind() != StructureDefinitionKind.LOGICAL) {
			return name.matches(defn.getType());
		} else {
			return name.equals(defn.getType()) || name.equals(defn.getName()) || name.equals(defn.getId());
		}
	}

	private NodeStack getFirstEntry(NodeStack bundle) {
		List<Element> list = new ArrayList();
		bundle.getElement().getNamedChildren("entry", list);
		if (list.isEmpty()) {
			return null;
		} else {
			Element resource = ((Element)list.get(0)).getNamedChild("resource");
			if (resource == null) {
				return null;
			} else {
				NodeStack entry = bundle.push(
					(Element)list.get(0), 0, ((Element)list.get(0)).getProperty().getDefinition(), ((Element)list.get(0)).getProperty().getDefinition()
				);
				return entry.push(
					resource, -1, resource.getProperty().getDefinition(), this.context.fetchTypeDefinition(resource.fhirType()).getSnapshot().getElementFirstRep()
				);
			}
		}
	}

	private boolean valueMatchesCriteria(Element value, ElementDefinition criteria, StructureDefinition profile) throws FHIRException {
		if (criteria.hasFixed()) {
			List<ValidationMessage> msgs = new ArrayList();
			this.checkFixedValue(msgs, "{virtual}", value, criteria.getFixed(), profile.getVersionedUrl(), "value", null, false);
			return msgs.size() == 0;
		} else if (criteria.hasBinding() && criteria.getBinding().getStrength() == BindingStrength.REQUIRED && criteria.getBinding().hasValueSet()) {
			throw new FHIRException(this.context.formatMessage("Unable_to_resolve_slice_matching__slice_matching_by_value_set_not_done", new Object[0]));
		} else {
			throw new FHIRException(this.context.formatMessage("Unable_to_resolve_slice_matching__no_fixed_value_or_required_value_set", new Object[0]));
		}
	}

	private boolean yearIsValid(String v) {
		if (v == null) {
			return false;
		} else {
			try {
				int i = Integer.parseInt(v.substring(0, Math.min(4, v.length())));
				return i >= 1800 && i <= this.thisYear() + 80;
			} catch (NumberFormatException var3) {
				return false;
			}
		}
	}

	private int thisYear() {
		return Calendar.getInstance().get(1);
	}

	public String reportTimes() {
		String s = String.format(
			"Times (ms): overall = %d:4, tx = %d, sd = %d, load = %d, fpe = %d, spec = %d",
			this.timeTracker.getOverall() / 1000000L,
			this.timeTracker.getTxTime() / 1000000L,
			this.timeTracker.getSdTime() / 1000000L,
			this.timeTracker.getLoadTime() / 1000000L,
			this.timeTracker.getFpeTime() / 1000000L,
			this.timeTracker.getSpecTime() / 1000000L
		);
		this.timeTracker.reset();
		return s;
	}

	public boolean isNoBindingMsgSuppressed() {
		return this.noBindingMsgSuppressed;
	}

	public IResourceValidator setNoBindingMsgSuppressed(boolean noBindingMsgSuppressed) {
		this.noBindingMsgSuppressed = noBindingMsgSuppressed;
		return this;
	}

	public boolean isNoTerminologyChecks() {
		return this.noTerminologyChecks;
	}

	public IResourceValidator setNoTerminologyChecks(boolean noTerminologyChecks) {
		this.noTerminologyChecks = noTerminologyChecks;
		return this;
	}

	public void checkAllInvariants() {
		for(StructureDefinition sd : new ContextUtilities(this.context).allStructures()) {
			if (sd.getDerivation() == TypeDerivationRule.SPECIALIZATION) {
				for(ElementDefinition ed : sd.getSnapshot().getElement()) {
					for(ElementDefinitionConstraintComponent inv : ed.getConstraint()) {
						if (inv.hasExpression()) {
							try {
								ExpressionNode n = (ExpressionNode)inv.getUserData("validator.expression.cache");
								if (n == null) {
									n = this.fpe.parse(FHIRPathExpressionFixer.fixExpr(inv.getExpression(), inv.getKey(), this.context.getVersion()));
									inv.setUserData("validator.expression.cache", n);
								}

								this.fpe.check(null, sd.getKind() == StructureDefinitionKind.RESOURCE ? sd.getType() : "DomainResource", ed.getPath(), n);
							} catch (Exception var8) {
								System.out
									.println(
										"Error processing structure ["
											+ sd.getId()
											+ "] path "
											+ ed.getPath()
											+ ":"
											+ inv.getKey()
											+ " ('"
											+ inv.getExpression()
											+ "'): "
											+ var8.getMessage()
									);
							}
						}
					}
				}
			}
		}
	}

	public IEvaluationContext getExternalHostServices() {
		return this.externalHostServices;
	}

	public String getValidationLanguage() {
		return this.validationLanguage;
	}

	public void setValidationLanguage(String validationLanguage) {
		this.validationLanguage = validationLanguage;
	}

	private String tail(String path) {
		return path.substring(path.lastIndexOf(".") + 1);
	}

	private String tryParse(String ref) {
		String[] parts = ref.split("\\/");
		switch(parts.length) {
			case 1:
				return null;
			case 2:
				return this.checkResourceType(parts[0]);
			default:
				return parts[parts.length - 2].equals("_history") && parts.length >= 4
					? this.checkResourceType(parts[parts.length - 4])
					: this.checkResourceType(parts[parts.length - 2]);
		}
	}

	private boolean typesAreAllReference(List<TypeRefComponent> theType) {
		for(TypeRefComponent typeRefComponent : theType) {
			if (!typeRefComponent.getCode().equals("Reference")) {
				return false;
			}
		}

		return true;
	}

	public ValidationResult checkCodeOnServer(NodeStack stack, ValueSet vs, String value, ValidationOptions options) {
		return this.checkForInctive(this.context.validateCode(options, value, vs));
	}

	public ValidationResult checkCodeOnServer(NodeStack stack, String code, String system, String version, String display, boolean checkDisplay) {
		String lang = stack.getWorkingLang();
		if (lang == null) {
			lang = this.validationLanguage;
		}

		this.codingObserver.seeCode(stack, system, version, code, display);
		return this.checkForInctive(this.context.validateCode(this.baseOptions.withLanguage(lang), system, version, code, checkDisplay ? display : null));
	}

	public ValidationResult checkCodeOnServer(NodeStack stack, ValueSet valueset, Coding c, boolean checkMembership) {
		this.codingObserver.seeCode(stack, c);
		return checkMembership
			? this.checkForInctive(this.context.validateCode(this.baseOptions.withLanguage(stack.getWorkingLang()).withCheckValueSetOnly(), c, valueset))
			: this.checkForInctive(this.context.validateCode(this.baseOptions.withLanguage(stack.getWorkingLang()).withNoCheckValueSetMembership(), c, valueset));
	}

	public ValidationResult checkCodeOnServer(NodeStack stack, ValueSet valueset, CodeableConcept cc, boolean vsOnly) {
		this.codingObserver.seeCode(stack, cc);
		return vsOnly
			? this.checkForInctive(this.context.validateCode(this.baseOptions.withLanguage(stack.getWorkingLang()).withCheckValueSetOnly(), cc, valueset))
			: this.checkForInctive(this.context.validateCode(this.baseOptions.withLanguage(stack.getWorkingLang()), cc, valueset));
	}

	private ValidationResult checkForInctive(ValidationResult res) {
		if (res == null) {
			return null;
		} else if (!res.isInactive()) {
			return res;
		} else {
			org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity lvl = org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.INFORMATION;
			String status = "not active";
			if (res.getStatus() != null) {
				status = res.getStatus();
			}

			String code = res.getCode();
			OperationOutcomeIssueComponent op = new OperationOutcomeIssueComponent(lvl, org.hl7.fhir.r5.model.OperationOutcome.IssueType.INVALID);
			String msgId = null;
			if (code != null) {
				msgId = res.isOk() ? "STATUS_CODE_WARNING_CODE" : "STATUS_CODE_HINT_CODE";
			} else {
				msgId = res.isOk() ? "STATUS_CODE_WARNING" : "STATUS_CODE_HINT";
			}

			op.getDetails().setText(this.context.formatMessage(msgId, new Object[]{status, code}));
			res.getIssues().add(op);
			return res;
		}
	}

	public boolean isSecurityChecks() {
		return this.securityChecks;
	}

	public void setSecurityChecks(boolean securityChecks) {
		this.securityChecks = securityChecks;
	}

	public List<BundleValidationRule> getBundleValidationRules() {
		return this.bundleValidationRules;
	}

	public boolean isValidateValueSetCodesOnTxServer() {
		return this.validateValueSetCodesOnTxServer;
	}

	public void setValidateValueSetCodesOnTxServer(boolean value) {
		this.validateValueSetCodesOnTxServer = value;
	}

	public boolean isNoCheckAggregation() {
		return this.noCheckAggregation;
	}

	public void setNoCheckAggregation(boolean noCheckAggregation) {
		this.noCheckAggregation = noCheckAggregation;
	}

	public boolean isAllowDoubleQuotesInFHIRPath() {
		return this.allowDoubleQuotesInFHIRPath;
	}

	public void setAllowDoubleQuotesInFHIRPath(boolean allowDoubleQuotesInFHIRPath) {
		this.allowDoubleQuotesInFHIRPath = allowDoubleQuotesInFHIRPath;
	}

	public static Element setParents(Element element) {
		if (element != null && !element.hasParentForValidator()) {
			element.setParentForValidator(null);
			setParentsInner(element);
		}

		return element;
	}

	public static Base setParentsBase(Base element) {
		if (element instanceof Element) {
			setParents((Element)element);
		}

		return element;
	}

	public static void setParentsInner(Element element) {
		for(Element child : element.getChildren()) {
			child.setParentForValidator(element);
			setParentsInner(child);
		}
	}

	public void setQuestionnaireMode(QuestionnaireMode questionnaireMode) {
		this.questionnaireMode = questionnaireMode;
	}

	public QuestionnaireMode getQuestionnaireMode() {
		return this.questionnaireMode;
	}

	public boolean isWantCheckSnapshotUnchanged() {
		return this.wantCheckSnapshotUnchanged;
	}

	public void setWantCheckSnapshotUnchanged(boolean wantCheckSnapshotUnchanged) {
		this.wantCheckSnapshotUnchanged = wantCheckSnapshotUnchanged;
	}

	public ValidationOptions getBaseOptions() {
		return this.baseOptions;
	}

	public void setBaseOptions(ValidationOptions baseOptions) {
		this.baseOptions = baseOptions;
	}

	public boolean isNoUnicodeBiDiControlChars() {
		return this.noUnicodeBiDiControlChars;
	}

	public void setNoUnicodeBiDiControlChars(boolean noUnicodeBiDiControlChars) {
		this.noUnicodeBiDiControlChars = noUnicodeBiDiControlChars;
	}

	public HtmlInMarkdownCheck getHtmlInMarkdownCheck() {
		return this.htmlInMarkdownCheck;
	}

	public void setHtmlInMarkdownCheck(HtmlInMarkdownCheck htmlInMarkdownCheck) {
		this.htmlInMarkdownCheck = htmlInMarkdownCheck;
	}

	public Coding getJurisdiction() {
		return this.jurisdiction;
	}

	public IResourceValidator setJurisdiction(Coding jurisdiction) {
		this.jurisdiction = jurisdiction;
		return this;
	}

	public boolean isLogProgress() {
		return this.logProgress;
	}

	public void setLogProgress(boolean logProgress) {
		this.logProgress = logProgress;
	}

	public boolean isDisplayWarnings() {
		return this.baseOptions.isDisplayWarningMode();
	}

	public void setDisplayWarnings(boolean displayWarnings) {
		this.baseOptions.setDisplayWarningMode(displayWarnings);
	}

	public boolean isCheckIPSCodes() {
		return this.codingObserver.isCheckIPSCodes();
	}

	public void setCheckIPSCodes(boolean checkIPSCodes) {
		this.codingObserver.setCheckIPSCodes(checkIPSCodes);
	}

	public InstanceValidator setForPublication(boolean forPublication) {
		this.forPublication = forPublication;
		if (forPublication) {
			this.warnOnDraftOrExperimental = true;
		}

		return this;
	}

	public boolean isWarnOnDraftOrExperimental() {
		return this.warnOnDraftOrExperimental;
	}

	public InstanceValidator setWarnOnDraftOrExperimental(boolean warnOnDraftOrExperimental) {
		this.warnOnDraftOrExperimental = warnOnDraftOrExperimental;
		return this;
	}

	public class CanonicalResourceLookupResult {
		private CanonicalResource resource;
		private String error;

		public CanonicalResourceLookupResult(CanonicalResource resource) {
			this.resource = resource;
		}

		public CanonicalResourceLookupResult(String error) {
			this.error = error;
		}
	}

	public class CanonicalTypeSorter implements Comparator<CanonicalType> {
		public CanonicalTypeSorter() {
		}

		public int compare(CanonicalType o1, CanonicalType o2) {
			return ((String)o1.getValue()).compareTo((String)o2.getValue());
		}
	}

	public class StructureDefinitionSorterByUrl implements Comparator<StructureDefinition> {
		public StructureDefinitionSorterByUrl() {
		}

		public int compare(StructureDefinition o1, StructureDefinition o2) {
			return o1.getUrl().compareTo(o2.getUrl());
		}
	}

	private class ValidatorHostServices implements IEvaluationContext {
		private ValidatorHostServices() {
		}

		public List<Base> resolveConstant(Object appContext, String name, boolean beforeContext) throws PathEngineException {
			ValidatorHostContext c = (ValidatorHostContext)appContext;
			return (List<Base>)(InstanceValidator.this.externalHostServices != null
				? InstanceValidator.this.externalHostServices.resolveConstant(c.getAppContext(), name, beforeContext)
				: new ArrayList());
		}

		public TypeDetails resolveConstantType(Object appContext, String name) throws PathEngineException {
			if (appContext instanceof VariableSet) {
				VariableSet vars = (VariableSet)appContext;
				VariableDefn v = vars.getVariable(name.substring(1));
				return v != null && v.hasTypeInfo() ? new TypeDetails(CollectionStatus.SINGLETON, new String[]{v.getWorkingType()}) : null;
			} else {
				ValidatorHostContext c = (ValidatorHostContext)appContext;
				return InstanceValidator.this.externalHostServices != null
					? InstanceValidator.this.externalHostServices.resolveConstantType(c.getAppContext(), name)
					: null;
			}
		}

		public boolean log(String argument, List<Base> focus) {
			return InstanceValidator.this.externalHostServices != null ? InstanceValidator.this.externalHostServices.log(argument, focus) : false;
		}

		public FunctionDetails resolveFunction(String functionName) {
			throw new FHIRException(InstanceValidator.this.context.formatMessage("Not_done_yet_ValidatorHostServicesresolveFunction_", new Object[]{functionName}));
		}

		public TypeDetails checkFunction(Object appContext, String functionName, List<TypeDetails> parameters) throws PathEngineException {
			throw new Error(InstanceValidator.this.context.formatMessage("Not_done_yet_ValidatorHostServicescheckFunction", new Object[0]));
		}

		public List<Base> executeFunction(Object appContext, List<Base> focus, String functionName, List<List<Base>> parameters) {
			throw new Error(InstanceValidator.this.context.formatMessage("Not_done_yet_ValidatorHostServicesexecuteFunction", new Object[0]));
		}

		public Base resolveReference(Object appContext, String url, Base refContext) throws FHIRException {
			ValidatorHostContext c = (ValidatorHostContext)appContext;
			if (refContext != null && refContext.hasUserData("validator.bundle.resolution")) {
				return (Base)refContext.getUserData("validator.bundle.resolution");
			} else {
				if (c.getAppContext() instanceof Element) {
					for(Element element = (Element)c.getAppContext(); element != null; element = element.getParentForValidator()) {
						Base res = InstanceValidator.this.resolveInBundle(url, element);
						if (res != null) {
							return res;
						}
					}
				}

				Base res = InstanceValidator.this.resolveInBundle(url, c.getResource());
				if (res != null) {
					return res;
				} else {
					for(Element element = c.getRootResource(); element != null; element = element.getParentForValidator()) {
						res = InstanceValidator.this.resolveInBundle(url, element);
						if (res != null) {
							return res;
						}
					}

					if (InstanceValidator.this.externalHostServices != null) {
						return InstanceValidator.setParentsBase(InstanceValidator.this.externalHostServices.resolveReference(c.getAppContext(), url, refContext));
					} else if (InstanceValidator.this.fetcher != null) {
						try {
							return InstanceValidator.setParents(InstanceValidator.this.fetcher.fetch(InstanceValidator.this, c.getAppContext(), url));
						} catch (IOException var8) {
							throw new FHIRException(var8);
						}
					} else {
						throw new Error(InstanceValidator.this.context.formatMessage("Not_done_yet__resolve__locally_2", new Object[]{url}));
					}
				}
			}
		}

		public boolean conformsToProfile(Object appContext, Base item, String url) throws FHIRException {
			ValidatorHostContext ctxt = (ValidatorHostContext)appContext;
			StructureDefinition sd = (StructureDefinition)InstanceValidator.this.context.fetchResource(StructureDefinition.class, url);
			if (sd == null) {
				throw new FHIRException(InstanceValidator.this.context.formatMessage("Unable_to_resolve_", new Object[]{url}));
			} else {
				InstanceValidator self = InstanceValidator.this;
				List<ValidationMessage> valerrors = new ArrayList();
				ValidationMode mode = new ValidationMode(ValidationReason.Expression, ProfileSource.FromExpression);
				if (item instanceof Resource) {
					try {
						Element e = new ObjectConverter(InstanceValidator.this.context).convert((Resource)item);
						InstanceValidator.setParents(e);
						self.validateResource(
							new ValidatorHostContext(ctxt.getAppContext(), e),
							valerrors,
							e,
							e,
							sd,
							IdStatus.OPTIONAL,
							new NodeStack(InstanceValidator.this.context, null, e, InstanceValidator.this.validationLanguage),
							null,
							mode
						);
					} catch (IOException var13) {
						throw new FHIRException(var13);
					}
				} else {
					if (!(item instanceof Element)) {
						throw new NotImplementedException(
							InstanceValidator.this.context.formatMessage("Not_done_yet_ValidatorHostServicesconformsToProfile_when_item_is_not_an_element", new Object[0])
						);
					}

					Element e = (Element)item;
					if (e.getSpecial() == SpecialElement.CONTAINED) {
						self.validateResource(
							new ValidatorHostContext(ctxt.getAppContext(), e, ctxt.getRootResource(), ctxt.getGroupingResource()),
							valerrors,
							e,
							e,
							sd,
							IdStatus.OPTIONAL,
							new NodeStack(InstanceValidator.this.context, null, e, InstanceValidator.this.validationLanguage),
							null,
							mode
						);
					} else if (e.getSpecial() != null) {
						self.validateResource(
							new ValidatorHostContext(ctxt.getAppContext(), e, e, ctxt.getRootResource()),
							valerrors,
							e,
							e,
							sd,
							IdStatus.OPTIONAL,
							new NodeStack(InstanceValidator.this.context, null, e, InstanceValidator.this.validationLanguage),
							null,
							mode
						);
					} else {
						self.validateResource(
							new ValidatorHostContext(ctxt.getAppContext(), e),
							valerrors,
							e,
							e,
							sd,
							IdStatus.OPTIONAL,
							new NodeStack(InstanceValidator.this.context, null, e, InstanceValidator.this.validationLanguage),
							null,
							mode
						);
					}
				}

				boolean ok = true;
				List<ValidationMessage> record = new ArrayList();

				for(ValidationMessage v : valerrors) {
					ok = ok && !v.getLevel().isError();
					if (v.getLevel().isError() || v.isSlicingHint()) {
						record.add(v);
					}
				}

				if (!ok && !record.isEmpty()) {
					ctxt.sliceNotes(url, record);
				}

				return ok;
			}
		}

		public ValueSet resolveValueSet(Object appContext, String url) {
			ValidatorHostContext c = (ValidatorHostContext)appContext;
			if (c.getProfile() != null && url.startsWith("#")) {
				for(Resource r : c.getProfile().getContained()) {
					if (r.getId().equals(url.substring(1))) {
						if (r instanceof ValueSet) {
							return (ValueSet)r;
						}

						throw new FHIRException(InstanceValidator.this.context.formatMessage("Reference__refers_to_a__not_a_ValueSet", new Object[]{url, r.fhirType()}));
					}
				}

				return null;
			} else {
				return (ValueSet)InstanceValidator.this.context.fetchResource(ValueSet.class, url);
			}
		}
	}
}
