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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageFileStrategy implements FileStrategy {
	@Autowired
	AppProperties appProperties;
	@Autowired
	private TusFileUploadService tusFileUploadService;
	private static final Logger logger = LoggerFactory.getLogger(ImageFileStrategy.class);

	@Override
	public void transferToFinalStorage(String uploadUrl) throws TusException, IOException {
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
		BufferedImage image = byteArrayToBufferedImage(completeByteArray);
		boolean isImageSaved = saveImageToFile(image, appProperties.getImage_path(), fileName, uploadUrl);
		if (isImageSaved) {
			tusFileUploadService.deleteUpload(uploadUrl);
		}
	}

	private static BufferedImage byteArrayToBufferedImage(byte[] byteArrayData) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayData);
		return ImageIO.read(inputStream);
	}

	private boolean saveImageToFile(BufferedImage image, String folderPath, String fileName, String uploadUrl) throws IOException, TusException {
		Path outputPath = Paths.get(folderPath, fileName);
		File outputFile = outputPath.toFile();
		Files.createDirectories(outputPath.getParent());
		if (!outputFile.exists()) {
			return ImageIO.write(image, "jpeg", outputFile);
		} else {
			tusFileUploadService.deleteUpload(uploadUrl);
			logger.warn("File already exists with the name {}", fileName);
		}
		return false;
	}
}

