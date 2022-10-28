package ca.uhn.fhir.jpa.starter.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import android.util.Base64;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Media;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.AppProperties;

@Import(AppProperties.class)
@Interceptor
public class ServerInterceptor {
		
	String imagePath;
	
	public ServerInterceptor(String path) {
		imagePath = path;
	}
	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
	public void insert(IBaseResource theResource) throws IOException {
		if(theResource.fhirType().equals("Media")) {
			if(((Media) theResource).getContent().hasData()) {
				byte[] bitmapdata = ((Media) theResource).getContent().getDataElement().getValue();
				String mediaId = ((Media) theResource).getIdElement().getIdPart();
				byte[] base64 = Base64.decode(bitmapdata, Base64.DEFAULT);
				File image = new File(imagePath+"//"+mediaId+".jpeg");
				FileUtils.writeByteArrayToFile(image, base64);
				String imagePath = image.getAbsolutePath();
				long imageSize = Files.size(Paths.get(imagePath));
				long byteSize = base64.length;
				if(imageSize == byteSize) {
					((Media) theResource).getContent().setDataElement(null);
		 			((Media) theResource).getContent().setUrl(imagePath);
				}
				else {
					System.out.println("Image Not Proper");
				}	
			}
		}
	}
	
	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
	   public void update(IBaseResource theOldResource, IBaseResource theResource) throws IOException {
		if(theResource.fhirType().equals("Media")) {
			if(((Media) theResource).getContent().hasData()) {
				byte[] bitmapdata = ((Media) theResource).getContent().getDataElement().getValue();
				String mediaId = ((Media) theResource).getIdElement().getIdPart();
				byte[] base64 = Base64.decode(bitmapdata, Base64.DEFAULT);
				File image = new File(imagePath+"//"+mediaId+".jpeg");
				FileUtils.writeByteArrayToFile(image, base64);
				String imagePath = image.getAbsolutePath();
				long imageSize = Files.size(Paths.get(imagePath));
				long byteSize = base64.length;
				if(imageSize == byteSize) {
					((Media) theResource).getContent().setDataElement(null);
		 			((Media) theResource).getContent().setUrl(imagePath);
				}
				else {
					System.out.println("Image Not Proper");
				}	
			}
		}
	}
}
