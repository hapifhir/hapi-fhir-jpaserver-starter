package ca.uhn.fhir.jpa.starter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(AppProperties.class)
@Configuration
public class DashboardEnvironmentConfig {

	@Autowired
	AppProperties appProperties;

	@Bean
	public Map<String, Map<ConfigDefinitionTypes, String>> getEnvToFilePathMapping(){
		Map<String, Map<ConfigDefinitionTypes, String>> directoryToFilesMap = new HashMap<>();
		  File[] directories = new File(appProperties.getEnvs()).listFiles(File::isDirectory);
		  if (directories != null) {
			  for (File directory : directories) {
				  Map<ConfigDefinitionTypes, String> fileNameToPathMap = new HashMap<>();
				  File[] files = new File(directory.getAbsolutePath()).listFiles(File::isFile);
				  if (files != null) {
					  for (File file : files) {
						  try {
							  ConfigDefinitionTypes defintionType = ConfigDefinitionTypes.valueOf(FilenameUtils.removeExtension(file.getName()));
							  fileNameToPathMap.put(defintionType, file.getAbsolutePath());  
						  }catch(IllegalArgumentException exception) {
							  exception.printStackTrace();
						  }
						  
					  }
				  }
				  directoryToFilesMap.put(directory.getName(), fileNameToPathMap);
			  }
		  }
		  return directoryToFilesMap;
		}
	}
