package ca.uhn.fhir.jpa.starter.controller;


import java.io.IOException;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.UnsupportedAudioFileException;

import ca.uhn.fhir.jpa.starter.service.TusService;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/api/upload")
//access Cross
@CrossOrigin(origins = {"http://localhost:3000/", "http://testhost.dashboard:3000/", "https://oclink.io/", "https://opencampaignlink.org/"}, maxAge = 3600, allowCredentials = "true")
public class TusController {
	@Autowired
	private TusFileUploadService tusFileUploadService;

	@Autowired
	TusService tusService;

	@RequestMapping(value = {"", "/**"}, method = {RequestMethod.POST, RequestMethod.PATCH, RequestMethod.HEAD,
		RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.GET})
	public void processUpload(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) throws IOException {
		tusFileUploadService.process(servletRequest, servletResponse);
		//access response header Location,Upload-Offset,Upload-length
		servletResponse.addHeader("Access-Control-Expose-Headers","Location,Upload-Offset,Upload-Length");
	}
	
	// Need this API for app backward compatibility
	@RequestMapping(method = RequestMethod.POST, value = "/transferImage")
	public ResponseEntity<String> getBytesAndSaveImages(@RequestParam("uploadUrl") String uploadUrl) {
		try {
			tusService.getBytesAndSaveImage(tusFileUploadService, uploadUrl);
			return ResponseEntity.ok("Images uploaded and saved successfully.");
		} catch (TusException | IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the images.");
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/transferFile")
	public ResponseEntity<String> getBytesAndSaveFile(@RequestParam("uploadUrl") String uploadUrl, @RequestParam("fileType") String fileType) {
		if (Objects.equals(fileType, "IMAGE")){
			try {
				tusService.getBytesAndSaveImage(tusFileUploadService, uploadUrl);
				return ResponseEntity.ok("Images uploaded and saved successfully.");
			} catch (TusException | IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the images.");
			}
		} else if (Objects.equals(fileType, "AUDIO")){
			try{
				tusService.getAudioFileAndSave(tusFileUploadService, uploadUrl);
				return ResponseEntity.ok("Audio File uploaded and saved successfully.");
			} catch (TusException | IOException | UnsupportedAudioFileException e){
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the audio files.");
			}
		} else{
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unsupported file type.");
		}
	}
}
