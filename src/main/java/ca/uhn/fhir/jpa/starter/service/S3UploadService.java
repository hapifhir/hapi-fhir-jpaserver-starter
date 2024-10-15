package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.AppProperties;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Import(AppProperties.class)
@Service
public class S3UploadService {
	private static final Logger logger = LoggerFactory.getLogger(S3UploadService.class);

	@Autowired
	AppProperties appProperties;

	@Scheduled(fixedDelay = 30000)
	public void uploadDirectory() throws IOException {
		File hyperSpectralDirectory = new File(appProperties.getHyper_spectral_files_path());
		File[] directoriesToUpload = hyperSpectralDirectory.listFiles(File::isDirectory);

		if (directoriesToUpload != null && directoriesToUpload.length != 0) {
			for (File directory : directoriesToUpload) {
				if (!directory.isDirectory()) {
					logger.error("Provided path is not a directory: " + directory.getAbsolutePath());
					continue;
				}

				String baseS3Key = directory.getName();
				List<File> filesInDirectory = new ArrayList<>(); // Store files to upload
				boolean uploadSuccessful = true; // Track if all files are uploaded

				try (Stream<Path> filePathStream = Files.walk(directory.toPath())) {
					filePathStream
						.filter(Files::isRegularFile) // Filter to include only files
						.forEach(filePath -> filesInDirectory.add(filePath.toFile())); // Collect files
				} catch (IOException e) {
					logger.error("Error walking directory: " + directory.getAbsolutePath(), e);
					uploadSuccessful = false;
				}

				// Now upload each file
				for (File file : filesInDirectory) {
					String keyName = baseS3Key + "/" + directory.toPath().relativize(file.toPath()).toString().replace("\\", "/");

					if (!uploadFile(appProperties.getAws_access_key(), appProperties.getAws_secret_key(), appProperties.getHyperSpectral_bucket_name(), keyName, file)) {
						uploadSuccessful = false; // Mark as failed if any upload fails
						logger.error("Failed to upload file: " + file.getName());
					}
				}

				// If all files were uploaded successfully, delete the directory
				if (uploadSuccessful) {
					try {
						deleteDirectory(directory);
						logger.info("Successfully deleted directory after uploading: " + directory.getName());
					} catch (IOException e) {
						logger.error("Error deleting directory: " + directory.getName(), e);
					}
				}
			}
		} else {
			logger.info("No pending loraw files to upload to S3");
		}
	}

	private boolean uploadFile(String accessKey, String secretKey, String bucketName, String keyName, File file) {
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

		AmazonS3 s3Client = AmazonS3Client.builder()
			.withRegion(Regions.EU_NORTH_1)
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
			.build();

		try {
			if (!s3Client.doesObjectExist(bucketName, keyName))
			{
				s3Client.putObject(bucketName, keyName, file);
				logger.info("Successfully Uploaded File to S3: " + file.getName());
			} else {
				logger.info("File already exists in the S3: " + file.getName());
			}
			return true;
		} catch (Exception e) {
			logger.error("Error uploading file: " + file.getName(), e);
			return false;
		}
	}

	private void deleteDirectory(File directory) throws IOException {
		Files.walk(directory.toPath())
			.map(Path::toFile)
			.sorted(Comparator.reverseOrder())
			.forEach(File::delete);
	}
}
