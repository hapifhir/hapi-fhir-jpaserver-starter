package ca.uhn.fhir.jpa.starter.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.joda.time.Instant;
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
		InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		BufferedReader bufferedReader = new BufferedReader(reader);

		StringBuilder requestBody = new StringBuilder();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			requestBody.append(line);
		}

		Bundle fhirBundle = (Bundle) FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
				.parseResource(requestBody.toString());

		String modifiedBody = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
				.encodeResourceToString(fhirBundle);

		if (requestBody.toString().contains("Patient/null") || requestBody.toString().contains("Encounter/null")) {
			uploadToS3Async(fhirBundle, username);
			for (Bundle.BundleEntryComponent entry : fhirBundle.getEntry()) {
				if (entry.getResource().getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.Observation)) {
					Observation observation = (Observation) entry.getResource();
					String date = observation.getEffectiveDateTimeType().asStringValue();
					String oneMinuteIncrement = DateUtilityHelper.addOneMinute(date);
					String practitionerRole = observation.getPerformer().get(0).getReference().toString();
					IGenericClient fhirClient = fhirClientAuthenticatorService.getFhirClient();
					Bundle result = (Bundle) fhirClient.search().byUrl(FhirClientAuthenticatorService.serverBase + "/Encounter?date=ge" + date
							+"&date=le"+oneMinuteIncrement+ "&participant=" + practitionerRole).execute();
					org.hl7.fhir.r4.model.Encounter closestEncounter = null;
					long secondsDifference = 999999999;
					for(Bundle.BundleEntryComponent innerEntry: result.getEntry()) {
						org.hl7.fhir.r4.model.Encounter enc = (org.hl7.fhir.r4.model.Encounter) innerEntry.getResource();
						String dateEncounter = enc.getPeriod().getStartElement().getValueAsString();
						long diff = DateUtilityHelper.differenceInSeconds(date,dateEncounter);
						if(diff<secondsDifference) {
							closestEncounter = enc;
							secondsDifference = diff;
						}
					}
					modifiedBody = modifiedBody.replace("Patient/null", closestEncounter.getSubject().getReference());
					modifiedBody = modifiedBody.replace("Encounter/null", "Encounter/"+closestEncounter.getIdElement().getIdPart());
					break;
				}
			}
		}

		ModifiedBodyRequestWrapper modifiedRequest = new ModifiedBodyRequestWrapper(request, modifiedBody);
		return modifiedRequest;
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
