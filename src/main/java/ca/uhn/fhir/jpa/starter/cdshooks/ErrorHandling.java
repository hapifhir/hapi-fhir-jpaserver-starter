package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import jakarta.servlet.http.HttpServletResponse;

public class ErrorHandling {

	private ErrorHandling() {}

	public static void handleError(
			HttpServletResponse response, String message, Exception e, AppProperties myAppProperties)
			throws IOException {
		setAccessControlHeaders(response, myAppProperties);
		response.setStatus(500); // This will be overwritten with the correct status code downstream if needed.
		response.getWriter().println(message);
		printMessageAndCause(e, response);
		if (e instanceof BaseServerResponseException) {
			handleServerResponseException((BaseServerResponseException) e, response);
		} else if (e.getCause() instanceof BaseServerResponseException) {
			handleServerResponseException((BaseServerResponseException) e.getCause(), response);
		}
		printStackTrack(e, response);
	}

	private static void handleServerResponseException(BaseServerResponseException e, HttpServletResponse response)
			throws IOException {
		switch (e.getStatusCode()) {
			case 401:
			case 403:
				response.getWriter().println("Precondition Failed. Remote FHIR server returned: " + e.getStatusCode());
				response.getWriter()
						.println(
								"Ensure that the fhirAuthorization token is set or that the remote server allows unauthenticated access.");
				response.setStatus(412);
				break;
			case 404:
				response.getWriter().println("Precondition Failed. Remote FHIR server returned: " + e.getStatusCode());
				response.getWriter().println("Ensure the resource exists on the remote server.");
				response.setStatus(412);
				break;
			default:
				response.getWriter().println("Unhandled Error in Remote FHIR server: " + e.getStatusCode());
		}
	}

	private static void printMessageAndCause(Exception e, HttpServletResponse response) throws IOException {
		if (e.getMessage() != null) {
			response.getWriter().println(e.getMessage());
		}

		if (e.getCause() != null && e.getCause().getMessage() != null) {
			response.getWriter().println(e.getCause().getMessage());
		}
	}

	private static void printStackTrack(Exception e, HttpServletResponse response) throws IOException {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		response.getWriter().println(exceptionAsString);
	}

	public static void setAccessControlHeaders(HttpServletResponse resp, AppProperties myAppProperties) {
		if (myAppProperties.getCors() != null) {
			if (myAppProperties.getCors().getAllow_Credentials()) {
				resp.setHeader(
						"Access-Control-Allow-Origin",
						myAppProperties.getCors().getAllowed_origin().stream()
								.findFirst()
								.get());
				resp.setHeader(
						"Access-Control-Allow-Methods",
						String.join(", ", Arrays.asList("GET", "HEAD", "POST", "OPTIONS")));
				resp.setHeader(
						"Access-Control-Allow-Headers",
						String.join(
								", ",
								Arrays.asList(
										"x-fhir-starter",
										"Origin",
										"Accept",
										"X-Requested-With",
										"Content-Type",
										"Authorization",
										"Cache-Control")));
				resp.setHeader(
						"Access-Control-Expose-Headers",
						String.join(", ", Arrays.asList("Location", "Content-Location")));
				resp.setHeader("Access-Control-Max-Age", "86400");
			}
		}
	}

	public static class CdsHooksError extends RuntimeException {
		public CdsHooksError(String message) {
			super(message);
		}
	}
}
