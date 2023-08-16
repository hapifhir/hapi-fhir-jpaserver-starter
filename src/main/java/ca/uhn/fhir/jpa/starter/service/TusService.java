package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.TusServerProperties;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Import(AppProperties.class)
@Service
public class TusService {
	private static final Logger logger = LoggerFactory.getLogger(TusService.class);
	private static final long FIXED_DELAY = 50 * 60000;
	@Autowired
	AppProperties appProperties;
	@Autowired
	TusServerProperties tusServerProperties;
	@Autowired
	private TusFileUploadService tusFileUploadService;
	public void getBytesAndSaveImage(TusFileUploadService tusFileUploadService, String uploadUrl) throws TusException, IOException {
		List<String> subDirectories = getSubDirectories(appProperties.getImage_path() + File.separator + "uploads");
		if (!subDirectories.isEmpty()) {
			for (String subDirectory : subDirectories) {
				transferImagesToFinalStorage(uploadUrl);
			}
		}
	}

	@Scheduled(fixedDelay = FIXED_DELAY)
	private void transferImageToFinalStorageScheduler() throws TusException, IOException{
		List<String> subDirectories = getSubDirectories(appProperties.getImage_path() + File.separator + "uploads");
		if(!subDirectories.isEmpty()){
			for (String subDirectory: subDirectories){
				String uploadUrl = tusServerProperties.getContextPath() + "/" + subDirectory;
				transferImagesToFinalStorage(uploadUrl);
			}
		}
	}

	private void transferImagesToFinalStorage(String uploadUrl) throws TusException, IOException{
		try{
			InputStream inputStream = tusFileUploadService.getUploadedBytes(uploadUrl);
			String fileName = new String(Base64.decodeBase64(tusFileUploadService.getUploadInfo(uploadUrl).getEncodedMetadata().split(" ")[1]), Charsets.UTF_8);

			// Use ByteArrayOutputStream to collect all the bytes from the InputStream.
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			// Define a buffer to read data in chunks.
			byte[] buffer = new byte[1024]; // You can adjust the buffer size as per your requirement.

			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				// Write the bytesRead number of bytes to the ByteArrayOutputStream.
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			}

			// Close the InputStream and ByteArrayOutputStream when done reading.
			inputStream.close();
			byteArrayOutputStream.close();

			// Get the complete byte array from the ByteArrayOutputStream.
			byte[] completeByteArray = byteArrayOutputStream.toByteArray();
			BufferedImage image = byteArrayToBufferedImage(completeByteArray);
			boolean isImageSaved = saveImageToFile(image, appProperties.getImage_path(), fileName);
			if (isImageSaved)
				tusFileUploadService.deleteUpload(uploadUrl);
		} catch (FileNotFoundException e){
			logger.warn(ExceptionUtils.getStackTrace(e));
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

	private static BufferedImage byteArrayToBufferedImage(byte[] byteArrayData) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayData);
		return ImageIO.read(inputStream);
	}

	private static boolean saveImageToFile(BufferedImage image, String folderPath, String fileName) throws IOException {
		Path outputPath = Paths.get(folderPath, fileName);
		File outputFile = outputPath.toFile();

		// Create parent directories if they don't exist
		Files.createDirectories(outputPath.getParent());
		if(outputFile.exists()){
			logger.warn("File already exists with the name" + fileName);
		}else{
			if (image != null)
				return ImageIO.write(image, "jpeg", outputFile);
		}
		return false;
	}

}
