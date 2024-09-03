package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.TUSFileTypes;
import ca.uhn.fhir.jpa.starter.TusServerProperties;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
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
import javax.sound.sampled.*;
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
	private static final long FIXED_DELAY = 15 * 60000;

	private static final long INITIAl_DELAY = 5 * 60000;
	@Autowired
	AppProperties appProperties;
	@Autowired
	TusServerProperties tusServerProperties;
	@Autowired
	private TusFileUploadService tusFileUploadService;
	public void getBytesAndSaveImage(TusFileUploadService tusFileUploadService, String uploadUrl, String fileType) throws TusException, IOException {
		String basePath;

		if (fileType.equals(TUSFileTypes.IMAGE.name())) {
			basePath = appProperties.getImage_path();
		} else {
			return;
		}

		List<String> subDirectories = getSubDirectories(basePath + File.separator + "uploads");
		if (!subDirectories.isEmpty()) {
			for (String subDirectory : subDirectories) {
				transferImagesToFinalStorage(uploadUrl, basePath);
			}
		}
	}

	public void saveBytesToFile(TusFileUploadService tusFileUploadService, String uploadUrl, String fileType) throws TusException, IOException, UnsupportedAudioFileException {
		transferFileToFinalStorage(uploadUrl, fileType);
	}

	@Scheduled(initialDelay = INITIAl_DELAY, fixedDelay = FIXED_DELAY)
	private void transferImageToFinalStorageScheduler() throws TusException, IOException, UnsupportedAudioFileException {
		List<String> subDirectories = getSubDirectories(appProperties.getImage_path() + File.separator + "uploads");
		if(!subDirectories.isEmpty()){
			for (String subDirectory: subDirectories){
				String uploadUrl = tusServerProperties.getContextPath() + "/" + subDirectory;

				//check if the file is locked
				if (isFileLocked(uploadUrl)){
					logger.warn("File is locked, skipping transfer for:" + uploadUrl);
					return;
				}

				String fileName = new String(Base64.decodeBase64(tusFileUploadService.getUploadInfo(uploadUrl).getEncodedMetadata().split(" ")[1]), Charsets.UTF_8);
				if (fileName.contains(".jpeg") || fileName.contains(".jpg"))
					transferImagesToFinalStorage(uploadUrl, appProperties.getImage_path());
				else if (fileName.contains(".lo"))
					transferFileToFinalStorage(uploadUrl, TUSFileTypes.LOFILE.name());
				else if (fileName.contains(".wav"))
					transferFileToFinalStorage(uploadUrl, TUSFileTypes.AUDIO.name());
				else
					logger.warn("Wrong File format for the file:" + fileName);
			}
		}
	}

	private void transferFileToFinalStorage(String uploadUrl, String fileType) throws TusException, IOException, UnsupportedAudioFileException {
		InputStream inputStream = tusFileUploadService.getUploadedBytes(uploadUrl);
		String fileName = new String(Base64.decodeBase64(tusFileUploadService.getUploadInfo(uploadUrl).getEncodedMetadata().split(" ")[1]), Charsets.UTF_8);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, bytesRead);
		}
		inputStream.close();
		byteArrayOutputStream.close();
		byte[] completeByteArray = byteArrayOutputStream.toByteArray();
		String folderPath = "";
		if (fileType.equals(TUSFileTypes.AUDIO.name()))
			folderPath = appProperties.getAudio_recordings_path();
		else if (fileType.equals(TUSFileTypes.LOFILE.name()))
			folderPath = appProperties.getHyper_spectral_files_path();
		boolean isFileSaved = saveByteArrayDataToFile(tusFileUploadService, completeByteArray, folderPath, fileName, uploadUrl);
		if (isFileSaved){
			tusFileUploadService.deleteUpload(uploadUrl);
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

	private void transferImagesToFinalStorage(String uploadUrl, String outputPath) throws TusException, IOException{
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
			boolean isImageSaved = saveImageToFile(tusFileUploadService, image, outputPath, fileName, uploadUrl);
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

	private static boolean saveImageToFile(TusFileUploadService tusFileUploadService, BufferedImage image, String folderPath, String fileName, String uploadUrl) throws IOException, TusException {
		Path outputPath = Paths.get(folderPath, fileName);
		File outputFile = outputPath.toFile();

		// Create parent directories if they don't exist
		Files.createDirectories(outputPath.getParent());
		if(outputFile.exists()){
			tusFileUploadService.deleteUpload(uploadUrl);
			logger.warn("File already exists with the name" + fileName);
		}else{
			if (image != null)
				return ImageIO.write(image, "jpeg", outputFile);
		}
		return false;
	}

	private static boolean saveByteArrayDataToFile(TusFileUploadService tusFileUploadService, byte[] byteArrayData, String folderPath, String fileName, String uploadUrl) throws IOException, TusException, UnsupportedAudioFileException {
		Path outputPath = Paths.get(folderPath, fileName);
		File outputFile = outputPath.toFile();
		Files.createDirectories(outputPath.getParent());
		if (outputFile.exists()){
			tusFileUploadService.deleteUpload(uploadUrl);
			logger.warn("File already exists with the name" + fileName);
		} else{
			try(FileOutputStream fos = new FileOutputStream(outputFile)){
				fos.write(byteArrayData);
			}
			return true;
		}
		return false;
	}
}
