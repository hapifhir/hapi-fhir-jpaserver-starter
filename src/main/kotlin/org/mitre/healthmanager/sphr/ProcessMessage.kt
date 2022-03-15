/*
Copyright 2022 The MITRE Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.mitre.healthmanager.sphr

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.jpa.api.dao.DaoRegistry
import ca.uhn.fhir.jpa.dao.r4.FhirSystemDaoR4
import ca.uhn.fhir.jpa.starter.AppProperties
import ca.uhn.fhir.rest.api.server.RequestDetails
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.r4.model.*
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


@Autowired
var appProperties: AppProperties? = null

@Autowired
var myDaoRegistry: DaoRegistry? = null



open class ProcessMessage : FhirSystemDaoR4() {

    override fun processMessage(theRequestDetails: RequestDetails, theMessage: IBaseBundle?): IBaseBundle {

        val fhirContext : FhirContext= myDaoRegistry?.systemDao?.context
            ?: throw InternalErrorException("no fhircontext")

        // A new bundle instance will be created from the contents of theMessage
        val messageBundle = fhirContext.newRestfulGenericClient(theRequestDetails.fhirServerBase)
            .create()
            .resource(theMessage)
            .prettyPrint()
            .encodedJson()
            .withAdditionalHeader("Referer", theRequestDetails.fhirServerBase)
            .execute()

        // Validation and initial processing
        // 1. must be a bundle with type 'message'
        // 2. must have at least two entries (header plus content)
        // 3. first entry must be a MessageHeader entry
        val theHeader = getMessageHeader(theMessage)
        // 4. header must specify the pdr event
        // 5. username extension must be present
        val username = getUsernameFromHeader(theHeader)

        // Check for the existence of an account with the username specified in the MessageHeader
        // 1. If one does exist, note the resource id, which will be used later
        // 2. If one does not exist, find the unique Patient instance in the message bundle (error if none or multiple)
        // and use it to create a new patient instance (make sure the username is present in the identifier list with system
        // "urn:mitre:healthmanager:account:username"). Record the resource id for use later
        // check if username exists already. If not, create skeleton record
        val patientSearchClient: IGenericClient = fhirContext.newRestfulGenericClient(theRequestDetails.fhirServerBase)
        val patientResultsBundle = patientSearchClient
            .search<IBaseBundle>()
            .forResource(Patient::class.java)
            .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("urn:mitre:healthmanager:account:username", username))
            .returnBundle(Bundle::class.java)
            .execute()
        val patientInternalId = when (patientResultsBundle.entry.size) {
            0 -> {
                val patientSkeleton = Patient()
                patientSkeleton.addIdentifier()
                    .setSystem("urn:mitre:healthmanager:account:username")
                    .setValue(username)
                val createResults = fhirContext.newRestfulGenericClient(theRequestDetails.fhirServerBase)
                    .create()
                    .resource(patientSkeleton)
                    .prettyPrint()
                    .encodedJson()
                    .execute()
                createResults.resource.idElement.idPart
            }
            1 -> {
                patientResultsBundle.entry[0].resource.idElement.idPart
            }
            else -> {
                throw InternalErrorException("multiple patient instances with username '$username'")
            }
        }

        // Store the MessageHeader (theHeader) as its own instance with the following changes
        // The focus list of the message header will be updated to contain only references to
        // - The bundle instance containing the message contents (id via results.id.idPart)
        // - The patient instance representing the account (id via patientInternalId)
        theHeader.focus.add(0, Reference("Bundle/" + messageBundle.id.idPart.toString()))
        theHeader.focus.add(1, Reference("Patient/" + patientInternalId.toString()))
        val createMessageHeaderResults = fhirContext.newRestfulGenericClient(theRequestDetails.fhirServerBase)
            .create()
            .resource(theHeader)
            .prettyPrint()
            .encodedJson()
            .execute()



        // store individual entries
        // take the theMessage (which is passed in) and make the following changes/checks:
        // - type is transaction
        // - remove MessageHeader entry
        // - make sure request details for Patient is put with the ID from step 2 (patientInternalId)
        // - make sure request details for all other types is post with whatever that type is
        // - TODO: check that there is a patient entry (which is either an already existing patient or a new patient the ID needs to be updated by using the messageHeader focus list from above)
        if (theMessage is Bundle) {

            if (theHeader.source.endpoint == "urn:apple:health-kit") {
                // specific temporary logic to handle apple health kit issues, including
                // 1. no patient entry, which is needed to make the references work
                // 2. links to encounter records, but encounters aren't present
                fixAppleHealthKitBundle(theMessage, patientInternalId)
            }

            theMessage.type = Bundle.BundleType.TRANSACTION
            var indexToRemove: Int? = null
            for ((i, e) in theMessage.entry.withIndex()) {

                /// make sure a fullUrl is present
                if ((e.fullUrl == null) || (e.fullUrl == "")) {
                    val entryId = e.resource.idElement.idPart
                    e.fullUrl = when {
                        entryId == null -> {
                            ""
                        }
                        isGUID(entryId) -> {
                            "urn:uuid:$entryId"
                        }
                        entryId != "" -> {
                            "${e.resource.resourceType}/$entryId"
                        }
                        else -> {
                            ""
                        }
                    }
                }

                when (e.resource.resourceType) {
                    ResourceType.MessageHeader -> {
                        indexToRemove = i
                    }
                    ResourceType.Patient -> {
                        // update this patient record, linkages will be updated by bundle processing
                        e.request.method = Bundle.HTTPVerb.PUT
                        e.request.url = "Patient/" + patientInternalId
                    }
                    else -> {
                        // create
                        e.request.method = Bundle.HTTPVerb.POST
                        e.request.url = e.resource.resourceType.toString()
                    }
                }
            }
            // remove the MessageHeader entry
            if (indexToRemove is Int) {
                theMessage.entry.removeAt(indexToRemove)
            }
        }
        else {
            throw InternalErrorException("bundle not provided to \$process-message")
        }
        fhirContext.newRestfulGenericClient(theRequestDetails.fhirServerBase)
            .transaction()
            .withBundle(theMessage)
            .execute()

        // NOTE: this line is the reason the provider doesn't do this itself
        // -- it doesn't know its own address (HapiProperties is JPA server only)
        val serverAddress: String = appProperties?.server_address ?: theRequestDetails.fhirServerBase
        val response = Bundle()
        response.type = Bundle.BundleType.MESSAGE
        val newHeader = MessageHeader()
        newHeader.addDestination().endpoint = theHeader.destinationFirstRep.endpoint
        newHeader.source = MessageHeader.MessageSourceComponent()
            .setEndpoint("$serverAddress\$process-message")
        newHeader.response = MessageHeader.MessageHeaderResponseComponent()
            .setCode(MessageHeader.ResponseType.OK)
        response.addEntry().resource = newHeader

        return response
    }



}

fun getMessageHeader(theMessage : IBaseBundle?) : MessageHeader {
    if (theMessage is Bundle) {

        if (theMessage.type != Bundle.BundleType.MESSAGE) {
            throw UnprocessableEntityException("\$process-message bundle must have type 'message'")
        }

        if (theMessage.entry.size > 1) {
            val firstEntry = theMessage.entry[0].resource
            if (firstEntry is MessageHeader) {
               return firstEntry
            }
            else {
                throw UnprocessableEntityException("First entry of the message Bundle must be a MessageHeader instance")
            }
        }
        else {
            throw UnprocessableEntityException("message Bundle must have at least 2 entries: a MessageHeader and another resource instance")
        }
    }
    else {
        throw UnprocessableEntityException("\$proccess-message must receive a bundle")
    }
}

/// Returns the username associated with this messageheader
fun getUsernameFromHeader (header : MessageHeader) : String {

    /// check header event
    when (val headerEvent = header.event) {
        is UriType -> {
            if (headerEvent.valueAsString != "urn:mitre:healthmanager:pdr") {
                throw UnprocessableEntityException("only pdr event supported")
            }
            else {
                println("just checking")
            }
        }
        else -> {
            throw UnprocessableEntityException("only pdr event supported")
        }
    }

     // get username from extension
    if (header.hasExtension("https://github.com/Open-Health-Manager/patient-data-receipt-ig/StructureDefinition/AccountExtension")) {
        val usernameExtension = header.getExtensionByUrl("https://github.com/Open-Health-Manager/patient-data-receipt-ig/StructureDefinition/AccountExtension")
        when (val usernameExtValue = usernameExtension.value) {
            is StringType -> {
                return usernameExtValue.value
            }
            else -> {
                throw UnprocessableEntityException("invalid username extension in pdr message header")
            }
        }
    }
    else {
        throw UnprocessableEntityException("no username found in pdr message header")
    }

}

fun isGUID(theId : String?) : Boolean {
    return try {
        UUID.fromString(theId)
        true
    } catch (exception: IllegalArgumentException) {
        false
    }
}

fun fixAppleHealthKitBundle(theMessage : Bundle, internalPatientId : String) {
    var messagePatientId : String? = null

    theMessage.entry.forEach { entry ->
        when (val resource = entry.resource) {
            is Observation -> {

                // replace patient reference with internal reference
                resource.subject.reference = "Patient/$internalPatientId"
                /*
                val patientReference = resource.subject.reference

                val referencedPatientId = patientReference.substringAfter("/")
                if (messagePatientId == null) {
                    messagePatientId = referencedPatientId
                }
                else if (messagePatientId != referencedPatientId) {
                    throw UnprocessableEntityException("Health kit: multiple referenced patients provided, only one allowed")
                }

                 */
                // remove encounter link
                resource.encounter = null

            }
            is Procedure -> {
                // replace patient reference with internal reference
                resource.subject.reference = "Patient/$internalPatientId"
                // remove encounter link
                resource.encounter = null
            }
            is Condition -> {
                // replace patient reference with internal reference
                // NOTE: in DSTU-2 it is patient instead of subject, so probably can't get conditions currently
                resource.subject.reference = "Patient/$internalPatientId"
                resource.asserter = null
            }
            is AllergyIntolerance -> {
                // replace patient reference with internal reference
                resource.patient.reference = "Patient/$internalPatientId"
            }
            else -> {
                // do nothing
            }
        }



    }
}