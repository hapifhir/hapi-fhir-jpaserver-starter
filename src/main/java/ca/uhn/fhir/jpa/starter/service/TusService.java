package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.TusServerProperties;
import ca.uhn.fhir.jpa.starter.tus.FileStrategyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class TusService {
	private static final Logger logger = LoggerFactory.getLogger(TusService.class);
	private static final long FIXED_DELAY = 3000;
	private static final long INITIAl_DELAY = 3000;

	@Autowired
	private AppProperties appProperties;
	@Autowired
	private FileStrategyContext fileStrategyContext; // Inject FileStrategyContext
	@Autowired
	private TusServerProperties tusServerProperties;

	public void transferToFinalStorage(String uploadUrl, String fileType) throws Exception {
		if (fileStrategyContext.isFileLocked(uploadUrl)) {
			logger.warn("File is locked, skipping transfer for: " + uploadUrl);
			return;
		}

		// Set strategy based on file type
		fileStrategyContext.setFileStrategy(fileType);
		// Execute the transfer via the selected strategy
		fileStrategyContext.executeStrategy(uploadUrl);
	}

	@Scheduled(initialDelay = INITIAl_DELAY, fixedDelay = FIXED_DELAY)
	private void transferScheduledFiles() throws Exception {
		List<String> subDirectories = getSubDirectories(appProperties.getImage_path() + File.separator + "uploads");
		for (String subDirectory : subDirectories) {
			String uploadUrl = tusServerProperties.getContextPath() + "/" + subDirectory;
			String fileType = fileStrategyContext.determineFileType(uploadUrl);
			transferToFinalStorage(uploadUrl, fileType);
		}
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
}

