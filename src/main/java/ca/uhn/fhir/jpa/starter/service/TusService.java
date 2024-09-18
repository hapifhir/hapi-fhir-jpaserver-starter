package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.TUSFileTypes;
import ca.uhn.fhir.jpa.starter.TusServerProperties;
import ca.uhn.fhir.jpa.starter.tus.*;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TusService {
	private static final Logger logger = LoggerFactory.getLogger(TusService.class);
	private static final long FIXED_DELAY = 3000;
	private static final long INITIAl_DELAY = 3000;
	@Autowired
	AppProperties appProperties;
	@Autowired
	TusServerProperties tusServerProperties;
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

	public void transferToFinalStorage(String uploadUrl, String fileType) throws TusException, IOException, UnsupportedAudioFileException {
		FileStrategy strategy = getFileStrategy(fileType);
		if (strategy != null) {
			if (isFileLocked(uploadUrl)){
				logger.warn("File is locked, skipping transfer for:" + uploadUrl);
				return;
			}
			strategy.transferToFinalStorage(uploadUrl);
		} else {
			logger.warn("No strategy found for file type: " + fileType);
		}
	}

	private boolean isFileLocked(String uploadUrl) throws IOException, TusException {
		UploadInfo uploadInfo = tusFileUploadService.getUploadInfo(uploadUrl);

		if (uploadInfo == null){
			logger.warn("No upload Information found for: " + uploadUrl);
			return true;
		}

		long uploadedBytes = uploadInfo.getOffset();
		long totalBytes = uploadInfo.getLength();

		//If the file is not fully uploaded, it's considered locked
		return uploadedBytes < totalBytes;
	}

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

	@Scheduled(initialDelay = INITIAl_DELAY, fixedDelay = FIXED_DELAY)
	private void transferScheduledFiles() throws TusException, IOException, UnsupportedAudioFileException {
		List<String> subDirectories = getSubDirectories(appProperties.getImage_path() + File.separator + "uploads");
		for (String subDirectory : subDirectories) {
			String uploadUrl = tusServerProperties.getContextPath() + "/" + subDirectory;
			String fileType = determineFileType(uploadUrl);
			transferToFinalStorage(uploadUrl, fileType);
		}
	}

	private String determineFileType(String uploadUrl) throws TusException, IOException {
		Map<String,String> dataList = extractKeyValuesFromMetaData(tusFileUploadService.getUploadInfo(uploadUrl).getEncodedMetadata().replace(" ",""));
		String fileName = new String(Base64.decodeBase64(dataList.get("filename")), Charsets.UTF_8);
		String isCalibFile = new String(Base64.decodeBase64(dataList.get("isCalibFile")), Charsets.UTF_8);
		if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
			return TUSFileTypes.IMAGE.name();
		} else if (fileName.endsWith(".wav")) {
			return TUSFileTypes.AUDIO.name();
		} else if (fileName.contains(".lo") && !isCalibFile.equals("True")) {
			return TUSFileTypes.LOFILE.name();
		}
		return "UNKNOWN";
	}

	public static List<String> getSubDirectories(String directoryPath) {
		List<String> subDirectories = new ArrayList<>();
		File rootDirectory = new File(directoryPath);
		if (rootDirectory.exists() && rootDirectory.isDirectory()) {
			File[] files = rootDirectory.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						subDirectories.add(file.getName());
					}
				}
			}
		}
		return subDirectories;
	}

	private Map<String, String> extractKeyValuesFromMetaData(String encodedMetaDataInput){
		String[] keys = {"filename", "isCalibFile"};

		Map<String, String> keyValueMap = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			int startIndex = encodedMetaDataInput.indexOf(keys[i]) + keys[i].length();
			int endIndex = (i + 1 < keys.length) ? encodedMetaDataInput.indexOf(keys[i + 1]) : encodedMetaDataInput.length();
			keyValueMap.put(keys[i], encodedMetaDataInput.substring(startIndex, endIndex));
		}
		return keyValueMap;
	}
}

