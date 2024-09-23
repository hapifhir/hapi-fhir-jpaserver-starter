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
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AudioFileStrategy implements FileStrategy {
	@Autowired
	AppProperties appProperties;
	@Autowired
	private TusFileUploadService tusFileUploadService;
	private static final Logger logger = LoggerFactory.getLogger(AudioFileStrategy.class);

	@Override
	public void transferToFinalStorage(String uploadUrl) throws TusException, IOException, UnsupportedAudioFileException {
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
		boolean isFileSaved = saveByteArrayDataToFile(completeByteArray, appProperties.getAudio_recordings_path(), fileName, uploadUrl);
		if (isFileSaved) {
			tusFileUploadService.deleteUpload(uploadUrl);
		}
	}

	private boolean saveByteArrayDataToFile(byte[] byteArrayData, String folderPath, String fileName, String uploadUrl) throws IOException, TusException {
		Path outputPath = Paths.get(folderPath, fileName);
		File outputFile = outputPath.toFile();
		Files.createDirectories(outputPath.getParent());
		if (!outputFile.exists()) {
			try (FileOutputStream fos = new FileOutputStream(outputFile)) {
				fos.write(byteArrayData);
			}
			return true;
		} else {
			tusFileUploadService.deleteUpload(uploadUrl);
			logger.warn("File already exists with the name {}", fileName);
		}
		return false;
	}
}

