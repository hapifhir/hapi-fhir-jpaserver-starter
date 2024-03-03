package some.custom.pkg1;

import ca.uhn.fhir.rest.annotation.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Code taken from hapi documentation on how to implement an operation which handles its own request/response
 * <a href="https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations_operations.html#manually-handing-requestresponse">...</a>
 */

@Component
public class CustomOperationBean {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(CustomOperationBean.class);

	@Operation(name = "$springBeanOperation", manualResponse = true, manualRequest = true)
	public void springBeanOperation(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
		throws IOException {
		String contentType = theServletRequest.getContentType();
		byte[] bytes = IOUtils.toByteArray(theServletRequest.getInputStream());

		ourLog.info("Received call with content type {} and {} bytes", contentType, bytes.length);

		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write("springBean");
		theServletResponse.getWriter().close();
	}
}
