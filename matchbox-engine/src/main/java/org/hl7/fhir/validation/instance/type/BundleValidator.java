package org.hl7.fhir.validation.instance.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.model.Base.ValidationMode;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Constants;
import org.hl7.fhir.r5.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.utils.XVerExtensionManager;
import org.hl7.fhir.r5.utils.validation.BundleValidationRule;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.i18n.I18nConstants;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.validation.BaseValidator;
import org.hl7.fhir.validation.instance.InstanceValidator;
import org.hl7.fhir.validation.instance.PercentageTracker;
import org.hl7.fhir.validation.instance.type.BundleValidator.StringWithSource;
import org.hl7.fhir.validation.instance.utils.EntrySummary;
import org.hl7.fhir.validation.instance.utils.IndexedElement;
import org.hl7.fhir.validation.instance.utils.NodeStack;
import org.hl7.fhir.validation.instance.utils.ValidationContext;

public class BundleValidator extends BaseValidator {
  public class StringWithSource {

    private String reference;
    private Element source;
    private boolean warning;
    private boolean nlLink;

    public StringWithSource(String reference, Element source, boolean warning, boolean nlLink) {
      this.reference = reference;
      this.source = source;
      this.warning = warning;
      this.nlLink = nlLink;
    }

    public String getReference() {
      return reference;
    }

    public Element getSource() {
      return source;
    }

    public boolean isWarning() {
      return warning;
    }

    public boolean isNlLink() {
      return nlLink;
    }

  }


  public final static String URI_REGEX3 = "((http|https)://([A-Za-z0-9\\\\\\.\\:\\%\\$]*\\/)*)?(Account|ActivityDefinition|AllergyIntolerance|AdverseEvent|Appointment|AppointmentResponse|AuditEvent|Basic|Binary|BodySite|Bundle|CapabilityStatement|CarePlan|CareTeam|ChargeItem|Claim|ClaimResponse|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap|Condition (aka Problem)|Consent|Contract|Coverage|DataElement|DetectedIssue|Device|DeviceComponent|DeviceMetric|DeviceRequest|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EligibilityRequest|EligibilityResponse|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|ExpansionProfile|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingManifest|ImagingStudy|Immunization|ImmunizationRecommendation|ImplementationGuide|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense|MedicationRequest|MedicationStatement|MessageDefinition|MessageHeader|NamingSystem|NutritionOrder|Observation|OperationDefinition|OperationOutcome|Organization|Parameters|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole|Procedure|ProcedureRequest|ProcessRequest|ProcessResponse|Provenance|Questionnaire|QuestionnaireResponse|ReferralRequest|RelatedPerson|RequestGroup|ResearchStudy|ResearchSubject|RiskAssessment|Schedule|SearchParameter|Sequence|ServiceDefinition|Slot|Specimen|StructureDefinition|StructureMap|Subscription|Substance|SupplyDelivery|SupplyRequest|Task|TestScript|TestReport|ValueSet|VisionPrescription)\\/[A-Za-z0-9\\-\\.]{1,64}(\\/_history\\/[A-Za-z0-9\\-\\.]{1,64})?";
  private String serverBase;

  public BundleValidator(BaseValidator parent, String serverBase) {
    super(parent);
    this.serverBase = serverBase;
  }

  public boolean validateBundle(List<ValidationMessage> errors, Element bundle, NodeStack stack, boolean checkSpecials, ValidationContext hostContext, PercentageTracker pct, ValidationMode mode) {
    boolean ok = true;
    
    String type = bundle.getNamedChildValue(TYPE, false);
    type = StringUtils.defaultString(type);
    List<Element> entries = new ArrayList<Element>();
    bundle.getNamedChildren(ENTRY, entries);    
    
    List<Element> links = new ArrayList<Element>();
    bundle.getNamedChildren(LINK, links);
    if (links.size() > 0) {
      int i = 0;
      for (Element l : links) {
        ok = validateLink(errors, bundle, links, l, stack.push(l, i++, null, null), type, entries) && ok;
      }
    }

    if (entries.size() == 0) {
      ok = rule(errors, NO_RULE_DATE, IssueType.INVALID, stack.getLiteralPath(), !(type.equals(DOCUMENT) || type.equals(MESSAGE)), I18nConstants.BUNDLE_BUNDLE_ENTRY_NOFIRST) && ok;
    } else {
      // Get the first entry, the MessageHeader or Document
      Element firstEntry = entries.get(0);
      // Get the stack of the first entry
      NodeStack firstStack = stack.push(firstEntry, 1, null, null);

      String fullUrl = firstEntry.getNamedChildValue(FULL_URL, false);

      if (type.equals(DOCUMENT)) {
        Element resource = firstEntry.getNamedChild(RESOURCE, false);
        if (rule(errors, NO_RULE_DATE, IssueType.INVALID, firstEntry.line(), firstEntry.col(), stack.addToLiteralPath(ENTRY, PATH_ARG), resource != null, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOFIRSTRESOURCE)) {
          String id = resource.getNamedChildValue(ID, false);
          ok = validateDocument(errors, bundle, entries, resource, firstStack.push(resource, -1, null, null), fullUrl, id) && ok;
          if (validator().getBundleValidationRules().size()==0) {
	          // matchbox patch #348
            StructureDefinition sdProfile = hostContext.getProfile();
            if (sdProfile != null) {
              Element res = resource;
              // NodeStack rstack = estack.push(res, -1, null, null);
              NodeStack rstack = stack.push(res, -1, null, null);
              String profilesCommaSep = validator().getFHIRPathEngine().evaluateToString(sdProfile,
                  "snapshot.element.where(min=1 and max='1' and path='Bundle.entry.resource' and type.where(code='Composition').exists()).first().type.where(code='Composition').first().profile");
              if (profilesCommaSep != null) {
                String profiles[] = profilesCommaSep.split(",");
                for (String profile : profiles) {
                  StructureDefinition defn = context.fetchResource(StructureDefinition.class, profile);
                  if (defn != null) {
//                    if (validator().isCrumbTrails()) {
                      res.addMessage(signpost(errors, NO_RULE_DATE, IssueType.INFORMATIONAL, res.line(), res.col(),
                          stack.getLiteralPath(), I18nConstants.VALIDATION_VAL_PROFILE_SIGNPOST_BUNDLE_PARAM,
                          defn.getUrl()));
//                    }
                    stack.resetIds();
                    ok = validator().startInner(hostContext, errors, res, res, defn, rstack, false, pct, mode, false)
                        && ok;
                  }
                  // also, while we're here, check the specials, since this doesn't happen
                  // anywhere else
                  ((InstanceValidator) parent).checkSpecials(hostContext, errors, res, rstack, true, pct, mode, true,
                      ok);
                }
              }
            }
          }
        }
        if (!VersionUtilities.isThisOrLater(FHIRVersion._4_0_1.getDisplay(), bundle.getProperty().getStructure().getFhirVersion().getDisplay())) {
          ok = handleSpecialCaseForLastUpdated(bundle, errors, stack) && ok;
        }
        ok = checkAllInterlinked(errors, entries, stack, bundle, false) && ok;
      } else if (type.equals(MESSAGE)) {
        Element resource = firstEntry.getNamedChild(RESOURCE, false);
        if (rule(errors, NO_RULE_DATE, IssueType.INVALID, firstEntry.line(), firstEntry.col(), stack.addToLiteralPath(ENTRY, PATH_ARG), resource != null, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOFIRSTRESOURCE)) {
          String id = resource.getNamedChildValue(ID, false);
          ok = validateMessage(errors, entries, resource, firstStack.push(resource, -1, null, null), fullUrl, id) && ok;
          ok = checkAllInterlinked(errors, entries, stack, bundle, true) && ok;
        }
      } else if (type.equals(SEARCHSET)) {
        ok = checkSearchSet(errors, bundle, entries, stack) && ok;
      }
      // We do not yet have rules requiring that the id and fullUrl match when dealing with messaging Bundles
      //      validateResourceIds(errors, UNKNOWN_DATE_TIME, entries, stack);
    }

    int count = 0;
    Map<String, Integer> counter = new HashMap<>(); 

    boolean fullUrlOptional = Utilities.existsInList(type, "transaction", "transaction-response", "batch", "batch-response");
    
    for (Element entry : entries) {
      NodeStack estack = stack.push(entry, count, null, null);
      String fullUrl = entry.getNamedChildValue(FULL_URL, false);
      String url = getCanonicalURLForEntry(entry);
      String id = getIdForEntry(entry);
      String rtype = getTypeForEntry(entry);
      
      if (!Utilities.noString(fullUrl)) {
        if (Utilities.isAbsoluteUrl(fullUrl)) {
          if (rtype != null &&  fullUrl.matches(urlRegex)) {
            if (rule(errors, "2023-11-13", IssueType.INVALID, entry.line(), entry.col(), stack.addToLiteralPath(ENTRY, PATH_ARG), id != null, I18nConstants.BUNDLE_ENTRY_URL_MATCHES_NO_ID, fullUrl)) {
              ok = rule(errors, "2023-11-13", IssueType.INVALID, entry.line(), entry.col(), stack.addToLiteralPath(ENTRY, PATH_ARG), fullUrl.endsWith("/"+rtype+"/"+id), I18nConstants.BUNDLE_ENTRY_URL_MATCHES_TYPE_ID, fullUrl, rtype, id) && ok;
            } else {
              ok = false;
            }
          }
        } else {
          ok = false;
          rule(errors, "2023-11-13", IssueType.INVALID, entry.line(), entry.col(), stack.addToLiteralPath(ENTRY, PATH_ARG), false, I18nConstants.BUNDLE_ENTRY_URL_ABSOLUTE, fullUrl);
        }
      }
      if (url != null) {
        if (!(!url.equals(fullUrl) || (url.matches(urlRegex) && url.endsWith("/" + id))) && !isV3orV2Url(url))
          ok = rule(errors, NO_RULE_DATE, IssueType.INVALID, entry.line(), entry.col(), stack.addToLiteralPath(ENTRY, PATH_ARG), false, I18nConstants.BUNDLE_BUNDLE_ENTRY_MISMATCHIDURL, url, fullUrl, id) && ok;
        ok = rule(errors, NO_RULE_DATE, IssueType.INVALID, entry.line(), entry.col(), stack.addToLiteralPath(ENTRY, PATH_ARG), !url.equals(fullUrl) || serverBase == null || (url.equals(Utilities.pathURL(serverBase, entry.getNamedChild(RESOURCE, false).fhirType(), id))), I18nConstants.BUNDLE_BUNDLE_ENTRY_CANONICAL, url, fullUrl) && ok;
      }

      if (!VersionUtilities.isR2Ver(context.getVersion())) {
        ok = rule(errors, NO_RULE_DATE, IssueType.INVALID, entry.line(), entry.col(), estack.getLiteralPath(), fullUrlOptional || fullUrl != null, I18nConstants.BUNDLE_BUNDLE_ENTRY_FULLURL_REQUIRED) && ok;
      }
      // check bundle profile requests
      if (rtype != null) {
        int rcount = counter.containsKey(rtype) ? counter.get(rtype)+1 : 0;
        counter.put(rtype, rcount);
        Element res = entry.getNamedChild(RESOURCE, false);
        NodeStack rstack = estack.push(res, -1, null, null);
        for (BundleValidationRule bvr : validator().getBundleValidationRules()) {
          if (meetsRule(bvr, rtype, rcount, count)) {
            StructureDefinition defn = context.fetchResource(StructureDefinition.class, bvr.getProfile());
            if (defn == null) {
              throw new Error(context.formatMessage(I18nConstants.BUNDLE_RULE_PROFILE_UNKNOWN, bvr.getRule(), bvr.getProfile()));
            } else {
              if (validator().isCrumbTrails()) {
                res.addMessage(signpost(errors, NO_RULE_DATE, IssueType.INFORMATIONAL, res.line(), res.col(), stack.getLiteralPath(), I18nConstants.VALIDATION_VAL_PROFILE_SIGNPOST_BUNDLE_PARAM, defn.getUrl()));
              }
              stack.resetIds();
              ok = validator().startInner(hostContext, errors, res, res, defn, rstack, false, pct, mode, false) && ok;
            }
          }
        }
        // also, while we're here, check the specials, since this doesn't happen anywhere else 
        ((InstanceValidator) parent).checkSpecials(hostContext, errors, res, rstack, true, pct, mode, true, ok);
      }
      
      // todo: check specials
      count++;
    }
    return ok;
  }


  private boolean validateLink(List<ValidationMessage> errors, Element bundle, List<Element> links, Element link, NodeStack stack, String type, List<Element> entries) {
    switch (type) {
    case "document": return validateDocumentLink(errors, bundle, links, link, stack, entries);
    case "message": return validateMessageLink(errors, bundle, links, link, stack, entries);
    case "history":
    case "searchset": return validateSearchLink(errors, bundle, links, link, stack);
    case "collection": return validateCollectionLink(errors, bundle, links, link, stack);
    case "subscription-notification": return validateSubscriptionLink(errors, bundle, links, link, stack);
    case "transaction":
    case "transaction-response":
    case "batch":
    case "batch-response":
      return validateTransactionOrBatchLink(errors, bundle, links, link, stack);
    default:
      return true; // unknown document type, deal with that elsewhere
    }
//    rule(errors, "2022-12-09", IssueType.INVALID, l.line(), l.col(), stack.getLiteralPath(), false, I18nConstants.BUNDLE_LINK_UNKNOWN, );    
  }

  private boolean validateDocumentLink(List<ValidationMessage> errors, Element bundle, List<Element> links, Element link, NodeStack stack, List<Element> entries) {
    boolean ok = true;
    Element relE = link.getNamedChild("relation", false);
    if (relE != null) {
      NodeStack relStack = stack.push(relE, -1, null, null); 
      String rel = relE.getValue();
      ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), !Utilities.existsInList(rel, "first", "previous", "next", "last"), I18nConstants.BUNDLE_LINK_SEARCH_PROHIBITED, rel);
      if ("self".equals(rel)) {
        ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), relationshipUnique(rel, link, links), I18nConstants.BUNDLE_LINK_SEARCH_NO_DUPLICATES, rel) && ok;
      }
      if ("stylesheet".equals(rel)) {
        Element urlE = link.getNamedChild("url", false);
        if (urlE != null) {
          NodeStack urlStack = stack.push(urlE, -1, null, null); 
          String url = urlE.getValue();
          if (url != null) {
            if (Utilities.isAbsoluteUrl(url)) {
              // todo: do we need to consider rel = base?
              if (url.equals("https://hl7.org/fhir/fhir.css")) {
                // well, this is ok!
              } else {
                warning(errors, "2022-12-09", IssueType.BUSINESSRULE, urlE.line(), urlE.col(), urlStack.getLiteralPath(), false, I18nConstants.BUNDLE_LINK_STYELSHEET_EXTERNAL);
                if (url.startsWith("http://")) {
                  warning(errors, "2022-12-09", IssueType.BUSINESSRULE, urlE.line(), urlE.col(), urlStack.getLiteralPath(), false, I18nConstants.BUNDLE_LINK_STYELSHEET_INSECURE);
                } 
                if (!Utilities.isAbsoluteUrlLinkable(url)) {
                  warning(errors, "2022-12-09", IssueType.BUSINESSRULE, urlE.line(), urlE.col(), urlStack.getLiteralPath(), false, I18nConstants.BUNDLE_LINK_STYELSHEET_LINKABLE);
                }
              }
            } else {
              // has to resolve in the bundle
              boolean found = false;
              for (Element e : entries) {
                Element res = e.getNamedChild(RESOURCE, false);
                if (res != null && (""+res.fhirType()+"/"+res.getIdBase()).equals(url)) {
                  found = true;
                  break;
                }                
              }
              ok = rule(errors, "2022-12-09", IssueType.NOTFOUND, urlE.line(), urlE.col(), urlStack.getLiteralPath(), found, I18nConstants.BUNDLE_LINK_STYELSHEET_NOT_FOUND) && ok;              
            }
          }
        }
      }
    }
    return ok;
  }

  private boolean validateMessageLink(List<ValidationMessage> errors, Element bundle, List<Element> links, Element link, NodeStack stack, List<Element> entries) {
    boolean ok = true;
    Element relE = link.getNamedChild("relation", false);
    if (relE != null) {
      NodeStack relStack = stack.push(relE, -1, null, null); 
      String rel = relE.getValue();
      ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), !Utilities.existsInList(rel, "first", "previous", "next", "last"), I18nConstants.BUNDLE_LINK_SEARCH_PROHIBITED, rel);
      if ("self".equals(rel)) {
        ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), relationshipUnique(rel, link, links), I18nConstants.BUNDLE_LINK_SEARCH_NO_DUPLICATES, rel) && ok;
      }
    }
    return ok;
  }

  private boolean validateSearchLink(List<ValidationMessage> errors, Element bundle, List<Element> links, Element link,  NodeStack stack) {
    String rel = StringUtils.defaultString(link.getNamedChildValue("relation", false));
    if (Utilities.existsInList(rel, "first", "previous", "next", "last", "self")) {
      return rule(errors, "2022-12-09", IssueType.INVALID, link.line(), link.col(), stack.getLiteralPath(), relationshipUnique(rel, link, links), I18nConstants.BUNDLE_LINK_SEARCH_NO_DUPLICATES, rel);
    } else {
      return true;
    }
  }

  private boolean relationshipUnique(String rel, Element link, List<Element> links) {
    for (Element l : links) {
      if (l != link && rel.equals(l.getNamedChildValue("relation", false))) {
        return false;
      }
      if (l == link) {
        // we only want to complain once, so we only look above this one
        return true; 
      }
    }
    return true;
  }

  private boolean validateCollectionLink(List<ValidationMessage> errors, Element bundle, List<Element> links, Element link, NodeStack stack) {
    boolean ok = true;  
    Element relE = link.getNamedChild("relation", false);
    if (relE != null) {
      NodeStack relStack = stack.push(relE, -1, null, null); 
      String rel = relE.getValue();
      ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), !Utilities.existsInList(rel, "first", "previous", "next", "last"), I18nConstants.BUNDLE_LINK_SEARCH_PROHIBITED, rel);
      if ("self".equals(rel)) {
        ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), relationshipUnique(rel, link, links), I18nConstants.BUNDLE_LINK_SEARCH_NO_DUPLICATES, rel) && ok;
      }
    }
    return ok;
  }

  private boolean validateSubscriptionLink(List<ValidationMessage> errors, Element bundle, List<Element> links, Element link, NodeStack stack) {
    boolean ok = true;  
    Element relE = link.getNamedChild("relation", false);
    if (relE != null) {
      NodeStack relStack = stack.push(relE, -1, null, null); 
      String rel = relE.getValue();
      ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), !Utilities.existsInList(rel, "first", "previous", "next", "last"), I18nConstants.BUNDLE_LINK_SEARCH_PROHIBITED, rel);
      if ("self".equals(rel)) {
        ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), relationshipUnique(rel, link, links), I18nConstants.BUNDLE_LINK_SEARCH_NO_DUPLICATES, rel) && ok;
      }
    }
    return ok;
  }

  private boolean validateTransactionOrBatchLink(List<ValidationMessage> errors, Element bundle, List<Element> links, Element link, NodeStack stack) {
    boolean ok = true;  
    Element relE = link.getNamedChild("relation", false);
    if (relE != null) {
      NodeStack relStack = stack.push(relE, -1, null, null); 
      String rel = relE.getValue();
      ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), !Utilities.existsInList(rel, "first", "previous", "next", "last"), I18nConstants.BUNDLE_LINK_SEARCH_PROHIBITED, rel);
      if ("self".equals(rel)) {
        ok = rule(errors, "2022-12-09", IssueType.INVALID, relE.line(), relE.col(), relStack.getLiteralPath(), relationshipUnique(rel, link, links), I18nConstants.BUNDLE_LINK_SEARCH_NO_DUPLICATES, rel) && ok;
      }
    }
    return ok;
  }

  private boolean checkSearchSet(List<ValidationMessage> errors, Element bundle, List<Element> entries, NodeStack stack) {
    boolean ok = true;
    
    // warning: should have self link
    List<Element> links = new ArrayList<Element>();
    bundle.getNamedChildren(LINK, links);
    Element selfLink = getSelfLink(links);
    List<String> types = new ArrayList<>();
    if (selfLink == null) {
      warning(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), stack.getLiteralPath(), false, I18nConstants.BUNDLE_SEARCH_NOSELF);
    } else {
      readSearchResourceTypes(selfLink.getNamedChildValue("url", false), types);
      if (types.size() == 0) {
        hint(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), stack.getLiteralPath(), false, I18nConstants.BUNDLE_SEARCH_SELF_NOT_UNDERSTOOD);
      }
    }

    Boolean searchMode = readHasSearchMode(entries);
    if (searchMode != null && searchMode == false) { // if no resources have search mode
      boolean typeProblem = false;
      String rtype = null;
      int count = 0;
      for (Element entry : entries) {
        NodeStack estack = stack.push(entry, count, null, null);
        count++;
        Element res = entry.getNamedChild(RESOURCE, false);
        if (rule(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), estack.getLiteralPath(), res != null, I18nConstants.BUNDLE_SEARCH_ENTRY_NO_RESOURCE)) {
          NodeStack rstack = estack.push(res, -1, null, null);
          String rt = res.fhirType();
          Boolean bok = checkSearchType(types, rt);
          if (bok == null) {
            typeProblem = true;
            hint(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), rstack.getLiteralPath(), selfLink == null, I18nConstants.BUNDLE_SEARCH_ENTRY_TYPE_NOT_SURE);                       
            String id = res.getNamedChildValue("id", false);
            warning(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), rstack.getLiteralPath(), id != null || "OperationOutcome".equals(rt), I18nConstants.BUNDLE_SEARCH_ENTRY_NO_RESOURCE_ID);
          } else if (bok) {
            if (!"OperationOutcome".equals(rt)) {
              String id = res.getNamedChildValue("id", false);
              warning(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), rstack.getLiteralPath(), id != null, I18nConstants.BUNDLE_SEARCH_ENTRY_NO_RESOURCE_ID);
              if (rtype != null && !rt.equals(rtype)) {
                typeProblem = true;
              } else if (rtype == null) {
                rtype = rt;
              }
            }
          } else {
            typeProblem = true;
            warning(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), estack.getLiteralPath(), false, I18nConstants.BUNDLE_SEARCH_ENTRY_WRONG_RESOURCE_TYPE_NO_MODE, rt, types);            
          }
        } else {
          ok = false;
        }
      }
      if (typeProblem) {
        warning(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), stack.getLiteralPath(), !typeProblem, I18nConstants.BUNDLE_SEARCH_NO_MODE);
      } else {
        hint(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), stack.getLiteralPath(), !typeProblem, I18nConstants.BUNDLE_SEARCH_NO_MODE);        
      }
    } else {
      int count = 0;
      for (Element entry : entries) {
        NodeStack estack = stack.push(entry, count, null, null);
        count++;
        Element res = entry.getNamedChild(RESOURCE, false);
        String sm = null;
        Element s = entry.getNamedChild("search", false);
        if (s != null) {
          sm = s.getNamedChildValue("mode", false);
        }
        warning(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), estack.getLiteralPath(), sm != null, I18nConstants.BUNDLE_SEARCH_NO_MODE);
        if (rule(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), estack.getLiteralPath(), res != null, I18nConstants.BUNDLE_SEARCH_ENTRY_NO_RESOURCE)) {
          NodeStack rstack = estack.push(res, -1, null, null);
          String rt = res.fhirType();
          String id = res.getNamedChildValue("id", false);
          if (sm != null) {
            if ("match".equals(sm)) {
              ok = rule(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), rstack.getLiteralPath(), id != null, I18nConstants.BUNDLE_SEARCH_ENTRY_NO_RESOURCE_ID) && ok;
              ok = rule(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), rstack.getLiteralPath(), types.size() == 0 || checkSearchType(types, rt), I18nConstants.BUNDLE_SEARCH_ENTRY_WRONG_RESOURCE_TYPE_MODE, rt, types) && ok;
            } else if ("include".equals(sm)) {
              ok = rule(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), rstack.getLiteralPath(), id != null, I18nConstants.BUNDLE_SEARCH_ENTRY_NO_RESOURCE_ID) && ok;
            } else { // outcome
              ok = rule(errors, NO_RULE_DATE, IssueType.INVALID, bundle.line(), bundle.col(), rstack.getLiteralPath(), "OperationOutcome".equals(rt), I18nConstants.BUNDLE_SEARCH_ENTRY_WRONG_RESOURCE_TYPE_OUTCOME, rt) && ok;
            }
          }
        } else {
          ok = false;
        }
      }
    }      
    return ok;
  }

  private Boolean checkSearchType(List<String> types, String rt) {
    if (types.size() == 0) {
      return null;
    } else {      
      return Utilities.existsInList(rt, types);
    }
  }

  private Boolean readHasSearchMode(List<Element> entries) {
    boolean all = true;
    boolean any = false;
    for (Element entry : entries) {
      String sm = null;
      Element s = entry.getNamedChild("search", false);
      if (s != null) {
        sm = s.getNamedChildValue("mode", false);
      }
      if (sm != null) {
        any = true;
      } else {
        all = false;
      }
    }
    if (all) {
      return true;
    } else if (any) {
      return null;      
    } else {
      return false;
    }
  }

  private void readSearchResourceTypes(String ref, List<String> types) {
    if (ref == null) {
      return;
    }
    String[] head = null;
    String[] tail = null;
    if (ref.contains("?")) {
      head = ref.substring(0, ref.indexOf("?")).split("\\/");
      tail = ref.substring(ref.indexOf("?")+1).split("\\&");
    } else {
      head = ref.split("\\/");
    }
    if (head == null || head.length == 0) {
      return;
    } else if (context.getResourceNames().contains(head[head.length-1])) {
      types.add(head[head.length-1]);
    } else if (tail != null) {
      for (String s : tail) {
        if (s.startsWith("_type=")) {
          for (String t : s.substring(6).split("\\,")) {
            types.add(t);
          }
        }
      }      
    }
  }

  private Element getSelfLink(List<Element> links) {
    for (Element link : links) {
      if ("self".equals(link.getNamedChildValue("relation", false))) {
        return link;
      }
    }
    return null;
  }

  private boolean validateDocument(List<ValidationMessage> errors, Element bundle, List<Element> entries, Element composition, NodeStack stack, String fullUrl, String id) {
    boolean ok = true;
    // first entry must be a composition
    if (rule(errors, NO_RULE_DATE, IssueType.INVALID, composition.line(), composition.col(), stack.getLiteralPath(), composition.getType().equals("Composition"), I18nConstants.BUNDLE_BUNDLE_ENTRY_DOCUMENT)) {

      // the composition subject etc references must resolve in the bundle
      ok = validateDocumentReference(errors, bundle, entries, composition, stack, fullUrl, id, false, "subject", "Composition") && ok;
      ok = validateDocumentReference(errors, bundle, entries, composition, stack, fullUrl, id, true, "author", "Composition") && ok;
      ok = validateDocumentReference(errors, bundle, entries, composition, stack, fullUrl, id, false, "encounter", "Composition") && ok;
      ok = validateDocumentReference(errors, bundle, entries, composition, stack, fullUrl, id, false, "custodian", "Composition") && ok;
      ok = validateDocumentSubReference(errors, bundle, entries, composition, stack, fullUrl, id, "Composition", "attester", false, "party") && ok;
      ok = validateDocumentSubReference(errors, bundle, entries, composition, stack, fullUrl, id, "Composition", "event", true, "detail") && ok;

      ok = validateSections(errors, bundle, entries, composition, stack, fullUrl, id) && ok;
    } else {
      ok = false;
    }
    return ok;
  }

  private boolean validateSections(List<ValidationMessage> errors, Element bundle, List<Element> entries, Element focus, NodeStack stack, String fullUrl, String id) {
    boolean ok = true;
    List<Element> sections = new ArrayList<Element>();
    focus.getNamedChildren("section", sections);
    int i = 1;
    for (Element section : sections) {
      NodeStack localStack = stack.push(section, i, null, null);

      // technically R4+, but there won't be matches from before that
      ok = validateDocumentReference(errors, bundle, entries, section, stack, fullUrl, id, true, "author", "Section") && ok;
      ok = validateDocumentReference(errors, bundle, entries, section, stack, fullUrl, id, false, "focus", "Section") && ok;

      List<Element> sectionEntries = new ArrayList<Element>();
      section.getNamedChildren(ENTRY, sectionEntries);
      int j = 1;
      for (Element sectionEntry : sectionEntries) {
        NodeStack localStack2 = localStack.push(sectionEntry, j, null, null);
        ok = validateBundleReference(errors, bundle, entries, sectionEntry, "Section Entry", localStack2, fullUrl, "Composition", id) && ok;
        j++;
      }
      ok = validateSections(errors, bundle, entries, section, localStack, fullUrl, id) && ok;
      i++;
    }
    return ok;
  }


  public boolean validateDocumentSubReference(List<ValidationMessage> errors, Element bundle, List<Element> entries, Element composition, NodeStack stack, String fullUrl, String id, String title, String parent, boolean repeats, String propName) {
    boolean ok = true;
    List<Element> list = new ArrayList<>();
    composition.getNamedChildren(parent, list);
    int i = 1;
    for (Element elem : list) {
      ok = validateDocumentReference(errors, bundle, entries, elem, stack.push(elem, i, null, null), fullUrl, id, repeats, propName, title + "." + parent) && ok;
      i++;
    }
    return ok;
  }

  public boolean validateDocumentReference(List<ValidationMessage> errors, Element bundle, List<Element> entries, Element composition, NodeStack stack, String fullUrl, String id, boolean repeats, String propName, String title) {
    boolean ok = true;

    List<Element> list = new ArrayList<>();
    composition.getNamedChildren(propName, list);
    if (repeats) {
      int i = 1;
      for (Element elem : list) {
        ok = validateBundleReference(errors, bundle, entries, elem, title + "." + propName, stack.push(elem, i, null, null), fullUrl, "Composition", id) && ok;
        i++;
      }
    } else if (list.size() > 0) {
      Element elem = list.get(0);
      ok = validateBundleReference(errors, bundle, entries, elem, title + "." + propName, stack.push(elem, -1, null, null), fullUrl, "Composition", id) && ok;
    }
    return ok;
  }

  private boolean validateMessage(List<ValidationMessage> errors, List<Element> entries, Element messageHeader, NodeStack stack, String fullUrl, String id) {
    boolean ok = true;
    // first entry must be a messageheader
    if (rule(errors, NO_RULE_DATE, IssueType.INVALID, messageHeader.line(), messageHeader.col(), stack.getLiteralPath(), messageHeader.getType().equals("MessageHeader"), I18nConstants.VALIDATION_BUNDLE_MESSAGE)) {
      List<Element> elements = messageHeader.getChildren("focus");
      for (Element elem : elements)
        ok = validateBundleReference(errors, messageHeader, entries, elem, "MessageHeader Data", stack.push(elem, -1, null, null), fullUrl, "MessageHeader", id) && ok;
    }
    return ok;
  }

  private boolean validateBundleReference(List<ValidationMessage> errors, Element bundle, List<Element> entries, Element ref, String name, NodeStack stack, String fullUrl, String type, String id) {
    String reference = null;
    try {
      reference = ref.getNamedChildValue("reference", false);
    } catch (Error e) {
    }

    if (ref != null && !Utilities.noString(reference) && !reference.startsWith("#")) {
      Element target = resolveInBundle(bundle, entries, reference, fullUrl, type, id, stack, errors, name, ref, false, false);
      if (target == null) {
        return false;
      }
    }
    return true;
  }


  /**
   * As per outline for <a href=http://hl7.org/fhir/stu3/documents.html#content>Document Content</a>:
   * <li>"The document date (mandatory). This is found in Bundle.meta.lastUpdated and identifies when the document bundle
   * was assembled from the underlying resources"</li>
   * <p></p>
   * This check was not being done for release versions < r4.
   * <p></p>
   * Related JIRA ticket is <a href=https://jira.hl7.org/browse/FHIR-26544>FHIR-26544</a>
   *
   * @param bundle {@link org.hl7.fhir.r5.elementmodel}
   * @param errors {@link List<ValidationMessage>}
   * @param stack {@link NodeStack}
   */
  private boolean handleSpecialCaseForLastUpdated(Element bundle, List<ValidationMessage> errors, NodeStack stack) {
    boolean ok = bundle.hasChild(META, false)
      && bundle.getNamedChild(META, false).hasChild(LAST_UPDATED, false)
      && bundle.getNamedChild(META, false).getNamedChild(LAST_UPDATED, false).hasValue();
    ruleHtml(errors, NO_RULE_DATE, IssueType.REQUIRED, stack.getLiteralPath(), ok, I18nConstants.DOCUMENT_DATE_REQUIRED, I18nConstants.DOCUMENT_DATE_REQUIRED_HTML);
    return ok;
  }

  private boolean checkAllInterlinked(List<ValidationMessage> errors, List<Element> entries, NodeStack stack, Element bundle, boolean isMessage) {
    boolean ok = true;
    List<EntrySummary> entryList = new ArrayList<>();
    int i = 0;
    for (Element entry : entries) {
      Element r = entry.getNamedChild(RESOURCE, false);
      if (r != null) {
        EntrySummary e = new EntrySummary(i, entry, r);
        entryList.add(e);
      }
      i++;
    }
    
    for (EntrySummary e : entryList) {
      List<StringWithSource> references = findReferences(e.getEntry());
      for (StringWithSource ref : references) {
        Element tgt = resolveInBundle(bundle, entries, ref.getReference(), e.getEntry().getChildValue(FULL_URL), e.getResource().fhirType(), e.getResource().getIdBase(), stack, errors, ref.getSource().getPath(), ref.getSource(), ref.isWarning() || true, ref.isNlLink());
        if (tgt != null) { 
          EntrySummary t = entryForTarget(entryList, tgt); 
          if (t != null ) { 
            if (t != e) { 
              e.getTargets().add(t); 
            } else { 
            } 
          } 
        } 
      }
    }

    Set<EntrySummary> visited = new HashSet<>();
    if (entryList.size() > 0) {
      visitLinked(visited, entryList.get(0));
    }
    visitBundleLinks(visited, entryList, bundle);
    boolean foundRevLinks;
    do {
      foundRevLinks = false;
      for (EntrySummary e : entryList) {
        if (!visited.contains(e)) {
//          System.out.println("Not visited "+e.getIndex()+" - check for reverse links");             
          boolean add = false;
          for (EntrySummary t : e.getTargets()) {
            if (visited.contains(t)) {
              add = true;
            }
          }
          if (add) {
            if (isMessage) {
              hint(errors, NO_RULE_DATE, IssueType.INFORMATIONAL, e.getEntry().line(), e.getEntry().col(), 
                  stack.addToLiteralPath(ENTRY + '[' + (i + 1) + ']'), isExpectedToBeReverse(e.getResource().fhirType()), 
                  I18nConstants.BUNDLE_BUNDLE_ENTRY_REVERSE_MSG, (e.getEntry().getChildValue(FULL_URL) != null ? "'" + e.getEntry().getChildValue(FULL_URL) + "'" : ""));              
            } else {
            // this was illegal up to R4B, but changed to be legal in R5
            if (VersionUtilities.isR5VerOrLater(context.getVersion())) {
              hint(errors, NO_RULE_DATE, IssueType.INFORMATIONAL, e.getEntry().line(), e.getEntry().col(), 
                  stack.addToLiteralPath(ENTRY + '[' + (i + 1) + ']'), isExpectedToBeReverse(e.getResource().fhirType()), 
                  I18nConstants.BUNDLE_BUNDLE_ENTRY_REVERSE_R5, (e.getEntry().getChildValue(FULL_URL) != null ? "'" + e.getEntry().getChildValue(FULL_URL) + "'" : ""));              
            } else {
              warning(errors, NO_RULE_DATE, IssueType.INVALID, e.getEntry().line(), e.getEntry().col(), 
                stack.addToLiteralPath(ENTRY + '[' + (i + 1) + ']'), isExpectedToBeReverse(e.getResource().fhirType()), 
                I18nConstants.BUNDLE_BUNDLE_ENTRY_REVERSE_R4, (e.getEntry().getChildValue(FULL_URL) != null ? "'" + e.getEntry().getChildValue(FULL_URL) + "'" : ""));
            }
            }
            foundRevLinks = true;
            visitLinked(visited, e);
          }
        }
      }
    } while (foundRevLinks);

    i = 0;
    for (EntrySummary e : entryList) {
      Element entry = e.getEntry();
      if (isMessage) {
        warning(errors, NO_RULE_DATE, IssueType.INFORMATIONAL, entry.line(), entry.col(), stack.addToLiteralPath(ENTRY + '[' + (i + 1) + ']'), visited.contains(e), I18nConstants.BUNDLE_BUNDLE_ENTRY_ORPHAN_MESSAGE, (entry.getChildValue(FULL_URL) != null ? "'" + entry.getChildValue(FULL_URL) + "'" : ""));
      } else {
        ok = rule(errors, NO_RULE_DATE, IssueType.INFORMATIONAL, entry.line(), entry.col(), stack.addToLiteralPath(ENTRY + '[' + (i + 1) + ']'), visited.contains(e), I18nConstants.BUNDLE_BUNDLE_ENTRY_ORPHAN_DOCUMENT, (entry.getChildValue(FULL_URL) != null ? "'" + entry.getChildValue(FULL_URL) + "'" : "")) && ok;
      }
      i++;
    }
    return ok;
  }



  private void visitBundleLinks(Set<EntrySummary> visited, List<EntrySummary> entryList, Element bundle) {
    List<Element> links = bundle.getChildrenByName("link");
    for (Element link : links) {
      String rel = link.getNamedChildValue("relation", false);
      String url = link.getNamedChildValue("url", false);
      if (rel != null && url != null) {
        if (Utilities.existsInList(rel, "stylesheet")) {
          for (EntrySummary e : entryList) {
            if (e.getResource() != null) {
              if (url.equals(e.getResource().fhirType()+"/"+e.getResource().getIdBase())) {
                visited.add(e);
                break;
              }
            }
          }
        }
      }
    }    
  }

  private boolean isExpectedToBeReverse(String fhirType) {
    return Utilities.existsInList(fhirType, "Provenance");
  }

  private String getCanonicalURLForEntry(Element entry) {
    Element e = entry.getNamedChild(RESOURCE, false);
    if (e == null)
      return null;
    return e.getNamedChildValue("url", false);
  }

  private String getIdForEntry(Element entry) {
    Element e = entry.getNamedChild(RESOURCE, false);
    if (e == null)
      return null;
    return e.getNamedChildValue(ID, false);
  }

  private String getTypeForEntry(Element entry) {
    Element e = entry.getNamedChild(RESOURCE, false);
    if (e == null)
      return null;
    return e.fhirType();
  }

  /**
   * Check each resource entry to ensure that the entry's fullURL includes the resource's id
   * value. Adds an ERROR ValidationMessge to errors List for a given entry if it references
   * a resource and fullURL does not include the resource's id.
   *
   * @param errors  List of ValidationMessage objects that new errors will be added to.
   * @param entries List of entry Element objects to be checked.
   * @param stack   Current NodeStack used to create path names in error detail messages.
   */
  private void validateResourceIds(List<ValidationMessage> errors, List<Element> entries, NodeStack stack) {
    // TODO: Need to handle _version
    int i = 1;
    for (Element entry : entries) {
      String fullUrl = entry.getNamedChildValue(FULL_URL, false);
      Element resource = entry.getNamedChild(RESOURCE, false);
      String id = resource != null ? resource.getNamedChildValue(ID, false) : null;
      if (id != null && fullUrl != null) {
        String urlId = null;
        if (fullUrl.startsWith("https://") || fullUrl.startsWith("http://")) {
          urlId = fullUrl.substring(fullUrl.lastIndexOf('/') + 1);
        } else if (fullUrl.startsWith("urn:uuid") || fullUrl.startsWith("urn:oid")) {
          urlId = fullUrl.substring(fullUrl.lastIndexOf(':') + 1);
        }
        rule(errors, NO_RULE_DATE, IssueType.INVALID, entry.line(), entry.col(), stack.addToLiteralPath("entry[" + i + "]"), urlId.equals(id), I18nConstants.BUNDLE_BUNDLE_ENTRY_IDURLMISMATCH, id, fullUrl);
      }
      i++;
    }
  }

  private EntrySummary entryForTarget(List<EntrySummary> entryList, Element tgt) {
    for (EntrySummary e : entryList) {
      if (e.getEntry() == tgt) {
        return e;
      }
    }
    return null;
  }

  private void visitLinked(Set<EntrySummary> visited, EntrySummary t) {
    if (!visited.contains(t)) {
      visited.add(t);
      for (EntrySummary e : t.getTargets()) {
        visitLinked(visited, e);
      }
    }
  }

  // not used?
//  private boolean followResourceLinks(Element entry, Map<String, Element> visitedResources, Map<Element, Element> candidateEntries, List<Element> candidateResources, List<ValidationMessage> errors, NodeStack stack) {
//    return followResourceLinks(entry, visitedResources, candidateEntries, candidateResources, errors, stack, 0);
//  }
//
//  private boolean followResourceLinks(Element entry, Map<String, Element> visitedResources, Map<Element, Element> candidateEntries, List<Element> candidateResources, List<ValidationMessage> errors, NodeStack stack, int depth) {
//    boolean ok = true;
//    Element resource = entry.getNamedChild(RESOURCE, false);
//    if (visitedResources.containsValue(resource))
//      return ok;
//
//    visitedResources.put(entry.getNamedChildValue(FULL_URL), resource);
//
//    String type = null;
//    Set<String> references = findReferences(resource);
//    for (String reference : references) {
//      // We don't want errors when just retrieving the element as they will be caught (with better path info) in subsequent processing
//      BooleanHolder bh = new BooleanHolder();
//      IndexedElement r = getFromBundle(stack.getElement(), reference, entry.getChildValue(FULL_URL), new ArrayList<ValidationMessage>(), stack.addToLiteralPath("entry[" + candidateResources.indexOf(resource) + "]"), type, "transaction".equals(stack.getElement().getChildValue(TYPE)), bh);
//      ok = ok && bh.ok();
//      if (r != null && !visitedResources.containsValue(r.getMatch())) {
//        followResourceLinks(candidateEntries.get(r.getMatch()), visitedResources, candidateEntries, candidateResources, errors, stack, depth + 1);
//      }
//    }
//    return ok;
//  }


  private List<StringWithSource> findReferences(Element start) {
    List<StringWithSource> references = new ArrayList<StringWithSource>();
    findReferences(start, references);
    return references;
  }

  private void findReferences(Element start, List<StringWithSource> references) {
    for (Element child : start.getChildren()) {
      if (child.getType().equals("Reference")) {
        String ref = child.getChildValue("reference");
        if (ref != null && !ref.startsWith("#") && !hasReference(ref, references))
          references.add(new StringWithSource(ref, child, false, false));
      }
      if (Utilities.existsInList(child.getType(), "url", "uri"/*, "canonical"*/) &&
          !Utilities.existsInList(child.getName(), "system") &&
          !Utilities.existsInList(child.getProperty().getDefinition().getPath(), "Bundle.entry.fullUrl", "Coding.system",  "Identifier.system", "Meta.profile", "Extension.url", "Quantity.system",
              "MessageHeader.source.endpoint", "MessageHeader.destination.endpoint", "Endpoint.address")) {
        String ref = child.primitiveValue();
        if (ref != null && !ref.startsWith("#") && !hasReference(ref, references))
          references.add(new StringWithSource(ref, child, true, isNLLink(start)));
      }
      // don't walk into a sub-bundle 
      if (!"Bundle".equals(child.fhirType())) {
        findReferences(child, references);
      }
    }
  }


  private boolean isNLLink(Element parent) {
    return parent != null && "extension".equals(parent.getName()) && "http://hl7.org/fhir/StructureDefinition/narrativeLink".equals(parent.getNamedChildValue("url", false));
  }

  private boolean hasReference(String ref, List<StringWithSource> references) {
    for (StringWithSource t : references) {
      if (ref.equals(t.getReference())) {
        return true;
      }
    }
    return false;
  }

  // hack for pre-UTG v2/v3
  private boolean isV3orV2Url(String url) {
    return url.startsWith("http://hl7.org/fhir/v3/") || url.startsWith("http://hl7.org/fhir/v2/");
  }


  public boolean meetsRule(BundleValidationRule bvr, String rtype, int rcount, int count) {
    if (bvr.getRule() == null) {
      throw new Error(context.formatMessage(I18nConstants.BUNDLE_RULE_NONE));
    }
    String rule =  bvr.getRule();
    String t = rule.contains(":") ? rule.substring(0, rule.indexOf(":")) : Utilities.isInteger(rule) ? null : rule; 
    String index = rule.contains(":") ? rule.substring(rule.indexOf(":")+1) : Utilities.isInteger(rule) ? rule : null;
    if (Utilities.noString(t) && Utilities.noString(index)) {
      throw new Error(context.formatMessage(I18nConstants.BUNDLE_RULE_NONE));
    }
    if (!Utilities.noString(t)) {
      if (!context.getResourceNames().contains(t)) {
        throw new Error(context.formatMessage(I18nConstants.BUNDLE_RULE_UNKNOWN, t));
      }
    }
    if (!Utilities.noString(index)) {
      if (!Utilities.isInteger(index)) {
        throw new Error(context.formatMessage(I18nConstants.BUNDLE_RULE_INVALID_INDEX, index));
      }
    }
    if (t == null) {
      return Integer.toString(count).equals(index);
    } else if (index == null) {
      return t.equals(rtype);
    } else {
      return t.equals(rtype) && Integer.toString(rcount).equals(index);
    }
  }

}