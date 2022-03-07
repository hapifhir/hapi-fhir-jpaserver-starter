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
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao
import ca.uhn.fhir.jpa.dao.r4.FhirSystemDaoR4
import ca.uhn.fhir.jpa.starter.AppProperties
import ca.uhn.fhir.rest.api.server.RequestDetails
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException
import ca.uhn.fhir.util.BundleUtil
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.UriType
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.r4.model.Patient
import org.springframework.beans.factory.annotation.Autowired


@Autowired
var appProperties: AppProperties? = null

@Autowired
var myDaoRegistry: DaoRegistry? = null



open class ProcessMessage : FhirSystemDaoR4() {

    override fun processMessage(theRequestDetails: RequestDetails, theMessage: IBaseBundle?): IBaseBundle {

        val fhirContext : FhirContext= myDaoRegistry?.systemDao?.context
            ?: throw InternalErrorException("no fhircontext")

        // Validation and initial processing
        // 1. must be a bundle with type 'message'
        // 2. must have at least two entries (header plus content)
        // 3. first entry must be a MessageHeader entry
        val theHeader = getMessageHeader(theMessage)
        // 4. header must specify the pdr event
        // 5. username extension must be present
        val username = getUsernameFromHeader(theHeader)

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

        // store the bundle as a bundle
        val results = fhirContext.newRestfulGenericClient(theRequestDetails.fhirServerBase)
            .create()
            .resource(theMessage)
            .prettyPrint()
            .encodedJson()
            .withAdditionalHeader("Referer", theRequestDetails.fhirServerBase)
            .execute()

        // store individual entries
        // TODO

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