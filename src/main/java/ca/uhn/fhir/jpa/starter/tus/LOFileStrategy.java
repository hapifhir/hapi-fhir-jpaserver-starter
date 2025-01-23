package ca.uhn.fhir.jpa.starter.tus;

import ca.uhn.fhir.jpa.starter.AppProperties;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Service
public class LOFileStrategy implements FileStrategy {
	@Autowired
	AppProperties appProperties;
	@Autowired
	private TusFileUploadService tusFileUploadService;

	private static final Logger logger = LoggerFactory.getLogger(LOFileStrategy.class);

	@Override
	public void transferToFinalStorage(String uploadUrl) throws TusException, IOException {
		InputStream inputStream = tusFileUploadService.getUploadedBytes(uploadUrl);
		String encodedMetadata = tusFileUploadService.getUploadInfo(uploadUrl).getEncodedMetadata();
		Map<String, String> dataList = extractKeyValuesFromMetaData(encodedMetadata.replace(" ", ""));

		// Decode metadata to get file details
		String fileName = new String(Base64.decodeBase64(dataList.get("filename")), Charsets.UTF_8);
		String loCamLength = new String(Base64.decodeBase64(dataList.get("lo_cam_length")), Charsets.UTF_8);
		String loCamName = new String(Base64.decodeBase64(dataList.get("lo_cam_name")), Charsets.UTF_8);

		// Create folder path and output file path
		String folderName = fileName.substring(0, fileName.lastIndexOf('.'));
		Path directoryPath = Paths.get(appProperties.getHyper_spectral_files_path(), folderName);
		Path outputPath = directoryPath.resolve(fileName);

		// Ensure the directory exists
		Files.createDirectories(directoryPath);

		// Stream data directly to the file
		try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
			byte[] buffer = new byte[8192]; // Use a larger buffer for efficiency
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		} finally {
			inputStream.close(); // Ensure the input stream is closed
		}

		// Create configurations file and clean up upload
		createConfigurationsFile(directoryPath, loCamName, loCamLength);
		tusFileUploadService.deleteUpload(uploadUrl);
	}


	private Map<String, String> extractKeyValuesFromMetaData(String encodedMetaDataInput){
		String[] keys = {"filename", "lo_cam_length", "lo_cam_name", "isCalibFile"};

		Map<String, String> keyValueMap = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			int startIndex = encodedMetaDataInput.indexOf(keys[i]) + keys[i].length();
			int endIndex = (i + 1 < keys.length) ? encodedMetaDataInput.indexOf(keys[i + 1]) : encodedMetaDataInput.length();
			keyValueMap.put(keys[i], encodedMetaDataInput.substring(startIndex, endIndex));
		}
		return keyValueMap;
	}

	private boolean saveByteArrayDataToFile(byte[] byteArrayData, Path directoryPath, String fileName, String uploadUrl) throws IOException, TusException {
		Files.createDirectories(directoryPath);
		Path filePath = directoryPath.resolve(fileName);
		File outputFile = filePath.toFile();

		if (!outputFile.exists()) {
			try (FileOutputStream fos = new FileOutputStream(outputFile)) {
				fos.write(byteArrayData);
			}
			logger.info("File saved at: " + filePath.toString());
		} else {
			tusFileUploadService.deleteUpload(uploadUrl);
			logger.warn("File already exists with the name: " + fileName);
		}
		return true;
	}

	private void createConfigurationsFile(Path directoryPath, String loCamName, String loCamLength) throws IOException {
		Path configFilePath = directoryPath.resolve("configurations.txt");
		String latestFieldCalibFile = getLatestCalibrationFile();
		String configContent = "CAMERA NAME: " + loCamName + "\n";
		configContent += "CAMERA LENGTH: " + loCamLength + "\n";
		if (latestFieldCalibFile != null)
			configContent += "FIELD CALIBRATION FILE: " + latestFieldCalibFile;
		Files.write(configFilePath, configContent.getBytes());
		logger.info("Configurations file created at: " + configFilePath.toString());
	}

	private String getLatestCalibrationFile(){
		File directory = new File(appProperties.getCalib_path());
		if (!directory.exists() || !directory.isDirectory()){
			return null;
		}
		File[] files = directory.listFiles();
		if (files == null || files.length == 0){
			return null;
		}
		File latestFile = Arrays.stream(files)
			.filter(File::isFile)
			.max(Comparator.comparingLong(File::lastModified))
			.orElse(null);

		if (latestFile != null)
			return latestFile.getName();
		else
			return null;
	}
}
