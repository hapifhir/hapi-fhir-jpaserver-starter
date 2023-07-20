package ca.uhn.fhir.jpa.starter.controller;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@CrossOrigin(origins = {"http://localhost:3000/", "http://testhost.dashboard:3000/", "https://oclink.io/", "https://opencampaignlink.org/"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/iprd")
public class MultiPartFileController {
	@Autowired
	AppProperties appProperties;

	@RequestMapping(method = RequestMethod.POST, value = "/transferImage")
	public String uploadFile(@RequestParam("file") MultipartFile[] files) {
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					try {
						String fileName = file.getOriginalFilename();
						File destinationFile = new File(appProperties.getImage_path() + File.separator + fileName);
						if(!destinationFile.exists()){
							file.transferTo(destinationFile.getAbsoluteFile());
							System.out.println("Transfer success for file: " + fileName);
						} else{
							System.out.println("File Already Exists: " + fileName);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return "image upload success";
		} else {
			// Handle empty file uploads (no files selected)
			return "image upload failed";
		}
	}
}
