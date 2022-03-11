package org.mitre.healthmanager.sphr

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.jpa.starter.Application
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import org.apache.commons.io.IOUtils
import org.awaitility.Awaitility
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.r4.model.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.io.DefaultResourceLoader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [Application::class],
    properties = [
        "spring.batch.job.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:dbr4",
        "spring.datasource.username=sa",
        "spring.datasource.password=null",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=ca.uhn.fhir.jpa.model.dialect.HapiFhirH2Dialect",
        "hapi.fhir.enable_repository_validating_interceptor=true",
        "hapi.fhir.fhir_version=r4",
    ]
)
class ProcessMessageTests {

    private val ourLog = LoggerFactory.getLogger(ProcessMessageTests::class.java)
    private val ourCtx: FhirContext = FhirContext.forR4()
    init {
        ourCtx.restfulClientFactory.serverValidationMode = ServerValidationModeEnum.NEVER
        ourCtx.restfulClientFactory.socketTimeout = 1200 * 1000
    }

    @LocalServerPort
    private var port = 0

    @Test
    @Order(0)
    fun testSuccessfulBundleStorage() {
        val methodName = "testSuccessfulBundleStorage"
        ourLog.info("Entering $methodName()...")
        val testClient : IGenericClient = ourCtx.newRestfulGenericClient("http://localhost:$port/fhir/")

        // Submit the bundle
        val messageBundle: Bundle = ourCtx.newJsonParser().parseResource<Bundle>(
            Bundle::class.java, stringFromResource("healthmanager/processmessage/BundleMessage_valid.json")
        )
        val response : Bundle = testClient
            .operation()
            .processMessage()
            .setMessageBundle<Bundle>(messageBundle)
            .synchronous(Bundle::class.java)
            .execute()

        Assertions.assertEquals(1, response.entry.size)
        when (val firstResource = response.entry[0].resource) {
            is MessageHeader -> {
                Assertions.assertEquals(firstResource.response.code, MessageHeader.ResponseType.OK)
            }
            else -> {
                Assertions.fail("response doesn't have a message header")
            }
        }

        Thread.sleep(1000) // give indexing a second to occur

        // find the patient id
        val patientResultsBundle : Bundle = testClient
            .search<IBaseBundle>()
            .forResource(Patient::class.java)
            //.where(Patient.IDENTIFIER.exactly().systemAndIdentifier("urn:mitre:healthmanager:account:username", "aKutch271"))
            .returnBundle(Bundle::class.java)
            .execute()

        /* Indexing seems to be taking too long, so don't search on patient id
        // give indexing a few more seconds
        if (patientResultsBundle.entry.size == 0) {
            Awaitility.await().atMost(1, TimeUnit.MINUTES).until {
                Thread.sleep(5000) // execute below function every 1 second
                ourLog.info("waiting for indexing")
                patientResultsBundle = testClient
                    .search<IBaseBundle>()
                    .forResource(Patient::class.java)
                    .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("urn:mitre:healthmanager:account:username", "aKutch271"))
                    .returnBundle(Bundle::class.java)
                    .execute()
                patientResultsBundle?.entry?.size!! > 0
            }
        }
         */

        Assertions.assertEquals(1, patientResultsBundle.entry.size)
        val patientId = when (val firstResource = patientResultsBundle.entry[0].resource) {
            is Patient -> {
                val username = firstResource.identifier.filter { it -> it.system == "urn:mitre:healthmanager:account:username" }
                Assertions.assertEquals(1, username.size)
                Assertions.assertEquals("aKutch271", username[0].value)
                firstResource.idElement.idPart
            }
            else -> {
                Assertions.fail("response didn't return a patient")
            }
        }

        // check other resources
        val patientEverythingResult : Parameters = testClient
            .operation()
            .onInstance(IdType("Patient", patientId))
            .named("\$everything")
            .withNoParameters(Parameters::class.java)
            .useHttpGet()
            .execute()
        Assertions.assertEquals(1, patientEverythingResult.parameter.size)
        when (val everythingBundle = patientEverythingResult.parameter[0].resource) {
            is Bundle -> {
                // 3 entries stored from the bundle
                Assertions.assertEquals(3, everythingBundle.entry.size)
            }
            else -> {
                Assertions.fail("\$everything didn't return a bundle")
            }
        }
    }
}

fun stringFromResource(theLocation : String) : String {
    val inputStream : InputStream = if (theLocation.startsWith(File.separator)) {
        FileInputStream(theLocation)
    }
    else {
        val resourceLoader = DefaultResourceLoader()
        val resource = resourceLoader.getResource(theLocation)
        resource.inputStream
    }

    return IOUtils.toString(inputStream, com.google.common.base.Charsets.UTF_8)
}