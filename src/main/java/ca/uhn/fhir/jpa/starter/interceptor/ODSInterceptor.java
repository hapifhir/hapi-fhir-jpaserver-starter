package ca.uhn.fhir.jpa.starter.interceptor;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
import org.hl7.fhir.dstu3.utils.formats.Turtle.StringType;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;

@Interceptor
public class ODSInterceptor extends InterceptorAdapter {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ODSInterceptor.class);

    private static final Pattern operationOutcomeInvalidParameterPattern = Pattern.compile("(?:Unknown search parameter )\"(.*)\"(?: for resource type)");

    private static final Pattern lastUpdatedRequestParameterPattern = Pattern.compile("gt\\d{4}-\\d{2}-\\d{2}");

    private static final Pattern integerPattern = Pattern.compile("^\\d+(,\\d+)*$");

    @Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
    public boolean outgoingResponse(RequestDetails theRequestDetails, ResponseDetails theResponseDetails, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
        logger.debug("In custom ODSInterceptor");
        logger.debug("the response is a " + theResponseDetails.getResponseResource().fhirType());

        if (theRequestDetails.getHeaders("X-Fhir-Modified") != null && theRequestDetails.getHeaders("X-Fhir-Modified").size()>0) {
            logger.debug("The Header 'X-Fhir-Modified' is supplied, so modifying the response specific to ODS");
            if (theResponseDetails.getResponseResource().fhirType().equals("Organization")) {

                logger.debug("Organization so modifying the resource directly" );
                theResponseDetails.setResponseResource(modifyOrganization((org.hl7.fhir.dstu3.model.Organization)theResponseDetails.getResponseResource()));
    
            } else if (theResponseDetails.getResponseResource().fhirType().equals("Bundle")) {
                
                logger.debug("Bundle so iterating the entries and modifying each Organization" );

                Bundle bundle = (org.hl7.fhir.dstu3.model.Bundle)theResponseDetails.getResponseResource();

                // Get the"total", then the "self" and "next" links
                int total = bundle.getTotal();

                String selfLink = "";
                String nextLink = "";
                if (bundle.hasLink()) {
                    for(var link : bundle.getLink()) {
                        if (link.getRelation().equals("self")) {
                            selfLink = link.getUrl();
                            logger.debug("Existing selfLink is " + selfLink);
                            continue;
                        }
                        if (link.getRelation().equals("next")) {
                            nextLink = link.getUrl();
                            logger.debug("Existing nextLink is " + nextLink);
                            continue;
                        }
                    }
                }

                // Next Link will look something like this https://hapi-server-stu3.ods-stu3.poc.dc4h.link/STU3?_getpages=8dce47ef-b99b-44b6-96a4-c5d74eb0c3b8&_getpagesoffset=20&_count=20&_pretty=true&_bundletype=searchset
                // If on the last page, the self link will contain _getpages
                // Split the URL into parameters
                Map<String, String> parameters = null;
                URL parsedUrl = null;
                try {

                    if (nextLink!=null && !nextLink.equals("") && nextLink.contains("_getpages")) {
                        parsedUrl = new URL(nextLink);
                    } else if (selfLink!=null && !selfLink.equals("") && selfLink.contains("_getpages")) {
                        parsedUrl = new URL(selfLink);
                    } 
                    if (parsedUrl!=null) {
                        parameters = splitQuery(parsedUrl);
                    }
                    
                } catch (UnsupportedEncodingException e) {
                    logger.error("UnsupportedEncodingException trying to parse the `next` link from the Bundle: " + nextLink , e);
                } catch (MalformedURLException e) {
                    logger.error("MalformedURLException trying to parse the `next` link from the Bundle: " + nextLink , e);
                }

                if (parameters!=null) {

                    String resultsKey = parameters.get("_getpages");
                    logger.debug("`resultsKey` is " + resultsKey);
                    String countString = parameters.get("_count");
                    logger.debug("`_count` is '" + countString + "`");
                    if (!countString.equals("")) {

                        int count = Integer.parseInt(countString);
                        //int getpagesoffset = Integer.getInteger(parameters.get("_getpagesoffset"));
                        String pretty = parameters.get("_pretty");
                        String bundletype = parameters.get("_bundletype");
    
                        int firstPagesOffset = 0;
    
                        // Work out the "first" page
                        String firstLink = String.format("%s://%s%s?_getpages=%s&_getpagesoffset=%s&_count=%s&_pretty=%s&_bundletype=%s", parsedUrl.getProtocol(), parsedUrl.getAuthority(), parsedUrl.getPath(), resultsKey, firstPagesOffset, count, pretty, bundletype);
                        logger.debug("firstLink  " + firstLink);
                        int lastPageOffset = (((int)Math.floor(total / count)) * count);
    
                        String lastLink = String.format("%s://%s%s?_getpages=%s&_getpagesoffset=%s&_count=%s&_pretty=%s&_bundletype=%s", parsedUrl.getProtocol(), parsedUrl.getAuthority(), parsedUrl.getPath(), resultsKey, lastPageOffset, count, pretty, bundletype);
                        logger.debug("lastLink  " + lastLink);
    
                        List<BundleLinkComponent> newLinkList = new ArrayList<BundleLinkComponent>();
                        newLinkList.add(0, new BundleLinkComponent(new org.hl7.fhir.dstu3.model.StringType("self"), new org.hl7.fhir.dstu3.model.UriType(selfLink)));
                        newLinkList.add(1, new BundleLinkComponent(new org.hl7.fhir.dstu3.model.StringType("first"), new org.hl7.fhir.dstu3.model.UriType(firstLink)));
                        newLinkList.add(2, new BundleLinkComponent(new org.hl7.fhir.dstu3.model.StringType("last"), new org.hl7.fhir.dstu3.model.UriType(lastLink)));
                        newLinkList.add(3, new BundleLinkComponent(new org.hl7.fhir.dstu3.model.StringType("next"), new org.hl7.fhir.dstu3.model.UriType(nextLink)));
    
                        bundle.setLink(newLinkList);
                    } else {
                        logger.debug("`count` was empty");
                    }
                    

                } else {
                    logger.debug("No parameters were able to be pased from  " + nextLink);
                }
                
                var newEntryList = new ArrayList<org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent>();
                if (bundle.hasEntry()) {
                    for (var entry : bundle.getEntry()) {
                        if (entry.getResource().fhirType().equals("Organization")) {
                            var newEntry = entry;
                            // Clear out the "search" field, we dont need this
                            newEntry.setSearch(null);
                            newEntry.setResource(modifyOrganization((Organization) entry.getResource()));
                            newEntryList.add(newEntry);
                        }
                    }
                    // Replace the list of entries with the new list
                    bundle.setEntry(newEntryList);
                }
    
            } 
        } 

        return true;
    } 

    @Hook(Pointcut.SERVER_OUTGOING_FAILURE_OPERATIONOUTCOME)
    public void outgoingOperationOutcome(
        RequestDetails theRequestDetails, 
        ca.uhn.fhir.rest.server.servlet.ServletRequestDetails theServletRequestDetails, 
        org.hl7.fhir.instance.model.api.IBaseOperationOutcome operationOutcome) {

            logger.debug("In custom ODSInterceptor for SERVER_OUTGOING_FAILURE_OPERATIONOUTCOME");
            if (theRequestDetails.getHeaders("X-Fhir-Modified") != null && theRequestDetails.getHeaders("X-Fhir-Modified").size()>0) {
                logger.debug("The Header 'X-Fhir-Modified' is supplied, so modifying the OperationOutcome specific to ODS");

                operationOutcome = modifyOperationOutcome((org.hl7.fhir.dstu3.model.OperationOutcome)operationOutcome);

            }
    }

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLER_SELECTED)
	public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws InvalidRequestException {
        logger.debug("incomingRequestPostProcessed - validating parameters" );

        if (theRequestDetails.getHeaders("X-Fhir-Modified") != null && theRequestDetails.getHeaders("X-Fhir-Modified").size()>0) {

            // If _lastUpdated is supplied (as ods-lastUpdated), validate it
            var lastUpdated = getRequestParameter(theRequestDetails, "ods-lastUpdated");
            if (lastUpdated!=null) {
                logger.debug("Validating ods-lastUpdated parameter" );
                Matcher m = lastUpdatedRequestParameterPattern.matcher(lastUpdated);
                
                
                if (!m.find()) {
                    if (!lastUpdated.contains("gt")) {
                        throw new InvalidRequestException("Last Updated Parameter Validation Error", buildOperationOutcome(IssueType.INVALID, "INVALID_VALUE", "An input field has an invalid value for its type", "Supplied date prefix is invalid, must be gt"));
                    } else {
                        throw new InvalidRequestException("Last Updated Parameter Validation Error", buildOperationOutcome(IssueType.INVALID, "INVALID_VALUE", "An input field has an invalid value for its type", "Supplied date is invalid"));
                    }
                }
            }

            // If identifier is specified, make sure the system is valid
            String identifier = getRequestParameter(theRequestDetails, "identifier");
            logger.debug("identifier supplied: " + identifier );
            if (identifier!=null && identifier.contains("|") ) {
                String[] parts = identifier.split(Pattern.quote("|"));
                //logger.debug("identifier system: " + parts[0] );
                //logger.debug("identifier value: " + parts[1] );
                String identifierSystem = parts[0];
                //String identifierValue = parts[1];
                if (identifierSystem!=null && !identifierSystem.equals("https://fhir.nhs.uk/Id/ods-organization-code")) {
                    logger.debug("Error thrown as identifier system is: `" + identifierSystem + "`" );
                    throw new InvalidRequestException("Identifier System Parameter Validation Error", buildOperationOutcome(IssueType.CODEINVALID, "INVALID_IDENTIFIER_SYSTEM", "Invalid identifier system", "Invalid ods-org-role parameter. Should be https://fhir.nhs.uk/Id/ods-organization-code."));
                }
            }

            // ods-org-role needs to be a number
            String odsorgrole = getRequestParameter(theRequestDetails, "ods-org-role");
            String orgroleSystem = "";
            String orgroleIdentifier = "";
            if (odsorgrole!=null) {
                if (odsorgrole.contains("|") ) {
                    String[] parts = odsorgrole.split(Pattern.quote("|"));
                    orgroleSystem = parts[0];
                    orgroleIdentifier = parts[1];
                    if (orgroleSystem!=null && !orgroleSystem.equals("https://directory.spineservices.nhs.uk/STU3/CodeSystem/ODSAPI-OrganizationRole-1")) {
                        logger.debug("Error thrown as ods-org-role system is: `" + orgroleSystem + "`" );
                        throw new InvalidRequestException("Identifier System Parameter Validation Error", buildOperationOutcome(IssueType.CODEINVALID, "INVALID_CODE_SYSTEM", "Invalid code system", "Invalid ods-org-role parameter. Should be https://directory.spineservices.nhs.uk/STU3/CodeSystem/ODSAPI-OrganizationRole-1."));
                    }
                } else {
                    orgroleIdentifier = odsorgrole;
                }
                logger.debug("Validating regex ods-org-role parameter value " + orgroleIdentifier );
                Matcher m = integerPattern.matcher(orgroleIdentifier);
                if (!m.find()) {
                    throw new InvalidRequestException("Last Updated Parameter Validation Error", buildOperationOutcome(IssueType.CODEINVALID, "INVALID_CODE_VALUE", "Invalid code value", "Invalid FHIR ods-org-role parameter"));
                }
            }
        }
        
        return true;
        
	}

    private String getRequestParameter(RequestDetails theRequestDetails, String parameterName) {
        if (theRequestDetails!=null && theRequestDetails.getParameters()!=null && theRequestDetails.getParameters().containsKey(parameterName)) {
            return theRequestDetails.getParameters().get(parameterName)[0];
        } else {
            return null;
        }
    }



    private OperationOutcome buildOperationOutcome(IssueType issueType, String codingCode, String codingDisplay, String diagnostics) {
        String issueSystem = "https://fhir.nhs.uk/STU3/CodeSystem/Spine-ErrorOrWarningCode-1";


        org.hl7.fhir.dstu3.model.OperationOutcome operationOutcome = new org.hl7.fhir.dstu3.model.OperationOutcome();
        
        Meta meta = new Meta();
        meta.addProfile("https://fhir.nhs.uk/STU3/StructureDefinition/Spine-OperationOutcome-1");
        operationOutcome.setMeta(meta);

        var newListtList = new ArrayList<org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent>();

        org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent issue = new org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent();

        issue.setSeverity(IssueSeverity.ERROR);
        issue.setCode(issueType);
        issue.setDetails(buildIssueDetails(issueSystem, codingCode, codingDisplay));
        issue.setDiagnostics(diagnostics);
        
        newListtList.add(issue);
        

        // Replace issueList
        operationOutcome.setIssue(newListtList);
        return (operationOutcome);
    }

    private OperationOutcome modifyOperationOutcome(org.hl7.fhir.dstu3.model.OperationOutcome operationOutcome) {

        String issueSystem = "https://fhir.nhs.uk/STU3/CodeSystem/Spine-ErrorOrWarningCode-1";

        operationOutcome.setId(UUID.randomUUID().toString());

        var newListtList = new ArrayList<org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent>();
        for(var issue : operationOutcome.getIssue()) {
            if (issue.getDiagnostics()!=null && issue.getDiagnostics().contains("HAPI-2001")) {
                logger.debug("Issue is " + issue.getDiagnostics() + " so translating" );
                issue.setCode(IssueType.NOTFOUND);
                issue.setDetails(buildIssueDetails(issueSystem, "NO_RECORD_FOUND", "No record found"));
                issue.setDiagnostics("No record found for supplied ODS code");
            } else if (issue.getDiagnostics()!=null && issue.getDiagnostics().contains("HAPI-0524")) {
                logger.debug("Issue is " + issue.getDiagnostics() + " so translating" );
                issue.setCode(IssueType.INVALID);
                issue.setDetails(buildIssueDetails(issueSystem, "INVALID_PARAMETER", "Invalid parameter"));
                issue.setDiagnostics("Unknown argument found - " + findParameter(issue.getDiagnostics()));
            }
            newListtList.add(issue);
        }

        Meta meta = new Meta();
        meta.addProfile("https://fhir.nhs.uk/STU3/StructureDefinition/Spine-OperationOutcome-1");
        operationOutcome.setMeta(meta);

        // Replace issueList
        operationOutcome.setIssue(newListtList);
        return (operationOutcome);
    }


    public Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    private String findParameter(String text) {
        logger.debug("trying to find parameter using regex from string " + text);
        Matcher m = operationOutcomeInvalidParameterPattern.matcher(text);
        if (m.find()){
            return m.group(1);
        }else{
            return null;//or empty string, or maybe throw exception
        }
    }

    private org.hl7.fhir.dstu3.model.CodeableConcept buildIssueDetails(String system, String code, String display) {
        org.hl7.fhir.dstu3.model.Coding issueCoding = new Coding(system, code, display);
        org.hl7.fhir.dstu3.model.CodeableConcept issueDetail = new CodeableConcept(issueCoding);
        return issueDetail;
    }

    private org.hl7.fhir.dstu3.model.Organization modifyOrganization(org.hl7.fhir.dstu3.model.Organization organization) {

        // Fix lastUpdated - move from extension to meta.lastUpdated
        // Check for the extension containing last Updated, it looks like this
        //{
        //    "url": "http://ods-dataLastUpdated",
        //    "valueDateTime": "2020-04-05T00:00:00+00:00"
        //}

        String lastUpdatedExtensionUrl = "http://ods-dataLastUpdated";

        var extensions = organization.getExtensionsByUrl(lastUpdatedExtensionUrl);
        if (extensions.size()>0) {
            DateTimeType lastUpdatedDate = (DateTimeType)extensions.get(0).getValue();
            organization.getMeta().setLastUpdated(java.util.Date.from((lastUpdatedDate.getValue().toInstant().truncatedTo( ChronoUnit.SECONDS ))));
        }

        
        // Build a new list of extensions, minux the ods-dataLastUpdated one
        List<org.hl7.fhir.dstu3.model.Extension> newExtensionList = new ArrayList<org.hl7.fhir.dstu3.model.Extension>();
        for(var extension : organization.getExtension() ) {
            if (!extension.getUrl().equals(lastUpdatedExtensionUrl)) {
                newExtensionList.add(extension);
            }
        }

        // Replace extension list with the new one
        organization.setExtension(newExtensionList);


        // Now remove the meta.extension
        if (organization.getMeta().getExtension() != null && organization.getMeta().getExtension().size()>0) {
            organization.getMeta().setExtension(null);
        }

        Meta newMeta = new Meta();
        newMeta.setProfile(organization.getMeta().getProfile());
        newMeta.setLastUpdated(organization.getMeta().getLastUpdated());
        organization.setMeta(newMeta);

        organization.getMeta().setVersionIdElement(null);

        return organization;

    }



    /*
    public class ODSOperationOutcome extends OperationOutcome {

        public ODSOperationOutcome(OperationOutcome outcome) {
            this.setMeta(outcome.getMeta());
            this.setId(outcome.getId());
            this.setIssue(outcome.getIssue());
            this.setText(null);
        }


        public Narrative getText() { 
            return null;
          }
    }*/

}



