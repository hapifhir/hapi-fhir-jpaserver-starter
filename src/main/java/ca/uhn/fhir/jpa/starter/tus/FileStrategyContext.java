package ca.uhn.fhir.jpa.starter.tus;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.jpa.starter.TUSFileTypes;
import ca.uhn.fhir.jpa.starter.TusServerProperties;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class FileStrategyContext {
	private static final Logger logger = LoggerFactory.getLogger(FileStrategyContext.class);

	@Autowired
	private TusFileUploadService tusFileUploadService;
	@Autowired
	private ImageFileStrategy imageFileStrategy;
	@Autowired
	private AudioFileStrategy audioFileStrategy;
	@Autowired
	private LOFileStrategy loFileStrategy;
	@Autowired
	private CalibFileStrategy calibFileStrategy;
	@Autowired
	private TusServerProperties tusServerProperties;

	private FileStrategy strategy;

	// Method to set the appropriate strategy
	public void setFileStrategy(String fileType) {
		this.strategy = getFileStrategy(fileType);
	}

	// Execute the strategy
	public void executeStrategy(String uploadUrl) throws Exception {
		if (strategy != null) {
			strategy.transferToFinalStorage(uploadUrl);
		} else {
			throw new IllegalStateException("File strategy is not set");
		}
	}

	// Retrieve the file strategy based on file type
	private FileStrategy getFileStrategy(String fileType) {
		switch (fileType) {
			case "IMAGE":
				return imageFileStrategy;
			case "AUDIO":
				return audioFileStrategy;
			case "LOFILE":
				return loFileStrategy;
			case "CALIB":
				return calibFileStrategy;
			default:
				return null;
		}
	}

	// Check if a file is locked (i.e., not fully uploaded)
	public boolean isFileLocked(String uploadUrl) throws IOException, TusException {
		UploadInfo uploadInfo = tusFileUploadService.getUploadInfo(uploadUrl);

		if (uploadInfo == null) {
			logger.warn("No upload Information found for: " + uploadUrl);
			return true;
		}

		long uploadedBytes = uploadInfo.getOffset();
		long totalBytes = uploadInfo.getLength();

		// If the file is not fully uploaded, it's considered locked
		return uploadedBytes < totalBytes;
	}

	// Determine file type based on metadata
	public String determineFileType(String uploadUrl) throws TusException, IOException {
		Map<String, String> dataList = extractKeyValuesFromMetaData(tusFileUploadService.getUploadInfo(uploadUrl).getEncodedMetadata().replace(" ", ""));
		String fileName = new String(Base64.decodeBase64(dataList.get("filename")), Charsets.UTF_8);
		String isCalibFile = "False";
		if(dataList.containsKey("isCalibFile")) {
			isCalibFile = new String(Base64.decodeBase64(dataList.get("isCalibFile")), Charsets.UTF_8);
		}

		if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
			return TUSFileTypes.IMAGE.name();
		} else if (fileName.endsWith(".wav")) {
			return TUSFileTypes.AUDIO.name();
		} else if (fileName.contains(".lo") && !isCalibFile.equals("True")) {
			return TUSFileTypes.LOFILE.name();
		}
		return "UNKNOWN";
	}

	// Extract key values from metadata
	private Map<String, String> extractKeyValuesFromMetaData(String encodedMetaDataInput) {
		String[] keys = {"filename", "isCalibFile"};
		Map<String, String> keyValueMap = new HashMap<>();

		for (String key : keys) {
			int startIndex = encodedMetaDataInput.indexOf(key);
			if (startIndex != -1) {
				startIndex += key.length();
				int endIndex = encodedMetaDataInput.indexOf(",", startIndex); // Use a delimiter if present, else go to the end
				if (endIndex == -1) {
					endIndex = encodedMetaDataInput.length();
				}
				keyValueMap.put(key, encodedMetaDataInput.substring(startIndex, endIndex));
			}
		}

		return keyValueMap;
	}
}