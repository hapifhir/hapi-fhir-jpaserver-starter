package ca.uhn.fhir.jpa.starter.tus;

import ca.uhn.fhir.jpa.starter.AppProperties;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class CalibFileStrategy implements FileStrategy{

	@Autowired
	AppProperties appProperties;
	@Autowired
	private TusFileUploadService tusFileUploadService;

	private static final Logger logger = LoggerFactory.getLogger(CalibFileStrategy.class);

	@Override
	public void transferToFinalStorage(String uploadUrl) throws TusException, IOException {
		InputStream inputStream = tusFileUploadService.getUploadedBytes(uploadUrl);
		Map<String,String> dataList = extractKeyValuesFromMetaData(tusFileUploadService.getUploadInfo(uploadUrl).getEncodedMetadata().replace(" ",""));
		String fileName = new String(Base64.decodeBase64(dataList.get("filename")), Charsets.UTF_8);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, bytesRead);
		}
		inputStream.close();
		byteArrayOutputStream.close();
		byte[] completeByteArray = byteArrayOutputStream.toByteArray();
		boolean isFileSaved = saveByteArrayDataToFile(completeByteArray, appProperties.getCalib_path(), fileName, uploadUrl);
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
