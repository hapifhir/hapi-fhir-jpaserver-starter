package ca.uhn.fhir.jpa.starter.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.iprd.fhir.utils.DateUtilityHelper;
import com.iprd.fhir.utils.ModifiedBodyRequestWrapper;
import com.iprd.fhir.utils.Utils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.impl.GenericClient;

@Import(AppProperties.class)
@Service
public class FixNullReferenceInBundle {

	@Autowired
	AppProperties appProperties;

	@Autowired
	FhirClientAuthenticatorService fhirClientAuthenticatorService;
	private static final Logger logger = LoggerFactory.getLogger(FixNullReferenceInBundle.class);

	public void uploadToS3Async(Bundle bundle, String userName) {
		new Thread(new Runnable() {
			public void run() {
				appProperties.equals("ff");
				uploadFileToS3(appProperties.getAws_access_key(), appProperties.getAws_secret_key(),
						"impact-health-logs", "logs", userName + Instant.now().toString(),
						FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().encodeResourceToString(bundle));
				//
			}
		}).start();
	}

	public HttpServletRequest fixNullReference(HttpServletRequest request, String username) throws IOException {
		InputStream inputStream = request.getInputStream();
		boolean specialTreatment = false;
		try {
			String requestBody ="";
			if(null!=request.getHeader("Content-Encoding")) {
				if(request.getHeader("Content-Encoding").contains("gzip")) {
					// Create a GZIPInputStream to decompress the data
					GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);

					// Create a buffer to read the decompressed data
					byte[] buffer = new byte[1024];
					ByteArrayOutputStream decompressedData = new ByteArrayOutputStream();

					int bytesRead;
					while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
						decompressedData.write(buffer, 0, bytesRead);
					}

					// Close the input streams
					gzipInputStream.close();
					inputStream.close();

					// Now, you can process the decompressed data as needed
					requestBody = new String(decompressedData.toByteArray(), "UTF-8");
		
				}else {
					// Now, you can process the decompressed data as needed
					requestBody = Utils.getBody(request);
				}
			}
			else {
				// Now, you can process the decompressed data as needed
				requestBody = Utils.getBody(request);
			}
						

			logger.warn("Request body " + requestBody.toString());
			Bundle fhirBundle = (Bundle) FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
					.parseResource(requestBody.toString());

			String modifiedBody = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
					.encodeResourceToString(fhirBundle);

			if (requestBody.toString().contains("\"reference\":\"Patient/null\"")
					|| requestBody.toString().contains("\"reference\":\"Patient/\"")
					|| requestBody.toString().contains("\"reference\":\"Encounter/null\"")) {
				uploadToS3Async(fhirBundle, username);
				String date = "";
				String oneMinuteIncrement = "";
				String practitionerRole = "";

				for (Bundle.BundleEntryComponent entry : fhirBundle.getEntry()) {
					//patient 4390fdf6-ea03-4e5b-9c79-a27af6fe587a
					//encounter ebdac583-55d7-4643-8a19-0ab8e699d6b8
					
					//patient 5286c163-6e69-40e2-8299-3ebe9b39e023
					//encounter b1731736-9f4a-4782-82a2-61c18c288d0d

					//patient 5206a32e-f6da-4cdb-96aa-0399fc056776
					//encounter 9602fe19-79e4-4e4b-bddb-e266fafe6d1f
					if (entry.getResource().getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.Encounter)) {
						org.hl7.fhir.r4.model.Encounter encounter = (org.hl7.fhir.r4.model.Encounter) entry.getResource();
						if (encounter.getSubject().getReference().equals("Patient/null")) {
							String encounterId = encounter.getIdElement().getIdPart();
							switch (encounterId) {
								case "ebdac583-55d7-4643-8a19-0ab8e699d6b8":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "4390fdf6-ea03-4e5b-9c79-a27af6fe587a" + "\"");
									specialTreatment = true;
									break;
								case "b1731736-9f4a-4782-82a2-61c18c288d0d":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "5286c163-6e69-40e2-8299-3ebe9b39e023" + "\"");
									specialTreatment = true;
									break;
								case "9602fe19-79e4-4e4b-bddb-e266fafe6d1f":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "5206a32e-f6da-4cdb-96aa-0399fc056776" + "\"");
									specialTreatment = true;
									break;
								case "6af137f5-6ac1-4508-aa5f-15062496a051":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "172e709c-9409-4f9f-9919-f54976a419f0" + "\"");
									specialTreatment = true;
									break;
								case "37ecb6b9-ced5-4ffc-82fb-3c82d60d7f6b":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "513bff7e-ee77-42af-8f0d-9a0f4adcae0d" + "\"");
									specialTreatment = true;
									break;
								case "36f62697-18f6-499b-a110-f0fdade40434":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "ab03f715-bfd1-4555-b58d-f8e093ad014f" + "\"");
									specialTreatment = true;
									break;
								case "3ac2935f-3323-4269-b846-7293dbc02789":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "fcefe9c9-3219-4ac9-8efe-e0de739bcbf6" + "\"");
									specialTreatment = true;
									break;
								case "1130a294-eb90-4ee1-8070-6cfc916f4605":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "aeb8fa2d-58f4-4fff-ae7a-5516c4c9a7dd" + "\"");
									specialTreatment = true;
									break;
								case "a2dcb7ea-e719-4687-b157-9bf8184985a3":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "ccb95362-c677-44d5-8bd9-9cc6dc3d64fc" + "\"");
									specialTreatment = true;
									break;
								case "c4331ae3-12e1-4199-99c5-bcddf82800df":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "71029cab-0a17-4ced-bfd7-948d62238ae5" + "\"");
									specialTreatment = true;
									break;
								case "a92f71ba-e8c4-46c0-9e75-4aa7c78907f3":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "00b563a5-be9a-49e3-a9d0-aab9dca20e43" + "\"");
									specialTreatment = true;
									break;
								case "038a1586-fe61-4760-ada0-f86a49f8ba50":
									modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
										+ "15817257-3c21-4fdb-8e5e-e21b81c31b14" + "\"");
									specialTreatment = true;
									break;
								default:
									break;
							}
						}
					}
					else if (entry.getResource().getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.Observation)) {
						Observation observation = (Observation) entry.getResource();
						if (observation.getSubject().getReference().equals("Patient/")
								|| observation.getEncounter().getReference().equals("Encounter/null")) {
							date = observation.getEffectiveDateTimeType().asStringValue();
							oneMinuteIncrement = DateUtilityHelper.addOneMinute(date);
							practitionerRole = observation.getPerformer().get(0).getReference().toString();
							break;
						}
					}
					else if (entry.getResource().getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.QuestionnaireResponse)) {
						QuestionnaireResponse questionnaireResponse = (QuestionnaireResponse) entry.getResource();
						if (questionnaireResponse.getSubject().getReference().equals("Patient/")
								|| questionnaireResponse.getEncounter().getReference().equals("Encounter/null")) {
							date = questionnaireResponse.getAuthoredElement().asStringValue();
							oneMinuteIncrement = DateUtilityHelper.addOneMinute(date);
							practitionerRole = questionnaireResponse.getAuthor().getReference().toString();
							break;
						}
						if(questionnaireResponse.getSubject().getReference().equals("Patient/null") && questionnaireResponse.getIdElement().getIdPart().equals("0e569153-c3bd-4738-82a9-05ea092f29d3")) {
							modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
								+ "aeb8fa2d-58f4-4fff-ae7a-5516c4c9a7dd" + "\"");
							specialTreatment = true;
							break;
						}
					}
					else if (entry.getResource().getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.Procedure)) {
						Procedure procedure = (Procedure) entry.getResource();
						if (procedure.getSubject().getReference().equals("Patient/")
								|| procedure.getEncounter().getReference().equals("Encounter/null")) {
							date = procedure.getPerformedDateTimeType().asStringValue();
							oneMinuteIncrement = DateUtilityHelper.addOneMinute(date);
							practitionerRole = procedure.getRecorder().getReference().toString();
							break;
						}
					}
				}

				if(!specialTreatment) {
					IGenericClient fhirClient = fhirClientAuthenticatorService.getFhirClient();
					Bundle result = (Bundle) fhirClient.search()
							.byUrl(FhirClientAuthenticatorService.serverBase + "/Encounter?date=ge" + date + "&date=le"
									+ oneMinuteIncrement + "&participant=" + practitionerRole)
							.execute();
					org.hl7.fhir.r4.model.Encounter closestEncounter = null;
					long secondsDifference = 999999999;
					for (Bundle.BundleEntryComponent innerEntry : result.getEntry()) {
						org.hl7.fhir.r4.model.Encounter enc = (org.hl7.fhir.r4.model.Encounter) innerEntry.getResource();
						String dateEncounter = enc.getPeriod().getStartElement().getValueAsString();
						long diff = DateUtilityHelper.differenceInSeconds(date, dateEncounter);
						if (diff < secondsDifference) {
							closestEncounter = enc;
							secondsDifference = diff;
						}
					}

					// If the closest encounter is not found in the db, i am looking in the same bundle with practitionerRole

					if(closestEncounter == null) {
						String finalPractitionerRole = practitionerRole;
						List<org.hl7.fhir.r4.model.Encounter> encounterList = fhirBundle.getEntry().stream()
							.map(Bundle.BundleEntryComponent::getResource)
							.filter(resource -> resource instanceof org.hl7.fhir.r4.model.Encounter)
							.map(resource -> (org.hl7.fhir.r4.model.Encounter) resource)
							.filter(encounter -> encounter.hasParticipant() && encounter.getParticipant().get(0).hasIndividual() && encounter.getParticipant().get(0).getIndividual().getReference().equals(finalPractitionerRole))
							.collect(Collectors.toList());

						if(encounterList.size() == 1) {
							closestEncounter = encounterList.get(0);
						}
					}

					if(closestEncounter != null) {
						modifiedBody = modifiedBody.replace("\"reference\":\"Patient/\"", "\"reference\":\"Patient/"
							+ closestEncounter.getSubject().getReference().replace("Patient/", "") + "\"");
						modifiedBody = modifiedBody.replace("\"reference\":\"Patient/null\"", "\"reference\":\"Patient/"
							+ closestEncounter.getSubject().getReference().replace("Patient/", "") + "\"");
						modifiedBody = modifiedBody.replace("\"reference\":\"Encounter/null\"",
							"\"reference\":\"Encounter/" + closestEncounter.getIdElement().getIdPart() + "\"");
					}
					logger.warn("Modified Request body " +modifiedBody);
				}
			}

			Map<String, String> modifiedHeaders = new HashMap<>();
			modifiedHeaders.put("Content-Encoding", request.getHeader("Content-Encoding").replace("gzip", ""));

			ModifiedBodyRequestWrapper modifiedRequest = new ModifiedBodyRequestWrapper(request, modifiedBody,
					modifiedHeaders);
			return modifiedRequest;
		} catch (EOFException e) {
			return request;
		}catch(java.util.zip.ZipException e) {
			return request;
		}catch(Exception e) {
			return request;
		}
	}

	public static void uploadFileToS3(String accessKey, String secretKey, String bucketName, String folderName,
			String fileName, String content) {

		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

		// Create an S3 client
		AmazonS3 s3Client = AmazonS3Client.builder().withRegion(Regions.US_EAST_1) // Replace with your desired region
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

		try {
			// Format the current date as a folder name
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String currentDateFolder = dateFormat.format(new Date());

			// Check if the folder exists, and if not, create it
			if (!s3Client.doesObjectExist(bucketName, folderName + "/" + currentDateFolder + "/")) {
				s3Client.putObject(bucketName, folderName + "/" + currentDateFolder + "/", "");
			}

			// Upload the string content as a file within the folder
			s3Client.putObject(bucketName, folderName + "/" + currentDateFolder + "/" + fileName,
					new ByteArrayInputStream(content.getBytes()), new ObjectMetadata());

			System.out.println("File uploaded successfully to S3.");

		} catch (AmazonServiceException e) {
			e.printStackTrace();
		} catch (SdkClientException e) {
			e.printStackTrace();
		}
	}
}
