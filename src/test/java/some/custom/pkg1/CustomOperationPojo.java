package some.custom.pkg1;

import ca.uhn.fhir.rest.annotation.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CustomOperationPojo {

	private final Logger LOGGER = LoggerFactory.getLogger(CustomOperationPojo.class);

	@Operation(name = "$pojoOperation", manualResponse = true, manualRequest = true)
	public void $pojoOperation(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
		throws IOException {
		String contentType = theServletRequest.getContentType();
		byte[] bytes = IOUtils.toByteArray(theServletRequest.getInputStream());

		LOGGER.info("Received call with content type {} and {} bytes", contentType, bytes.length);

		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write("pojo");
		theServletResponse.getWriter().close();
	}
}
