package ca.uhn.fhir.jpa.starter.javascript;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Custom R4 system-level operation that runs a server-side JavaScript file (via the embedded
 * GraalJS engine) to transform FHIR resources.
 *
 * <p>Callers do <b>not</b> supply the JavaScript itself — they reference one of the scripts the
 * server administrator has placed in the configured scripts directory
 * ({@code hapi.fhir.javascript_execution_scripts_dir}). This keeps the set of executable code
 * fixed and vetted, instead of accepting arbitrary code over the wire.
 *
 * <p>Invoke it as {@code POST [base]/$execute-javascript} with a {@code Parameters} body carrying a
 * {@code script} (the file name, with or without the {@code .js} suffix), any number of inline
 * {@code resource} parameters, and any number of {@code reference} parameters — literal references
 * the server reads before the script runs, e.g.
 *
 * <pre>{@code
 * {
 *   "resourceType": "Parameters",
 *   "parameter": [
 *     { "name": "script", "valueString": "merge-patients" },
 *     { "name": "resource", "resource": { "resourceType": "Patient", "id": "1" } },
 *     { "name": "reference", "valueReference": { "reference": "Patient/2" } }
 *   ]
 * }
 * }</pre>
 *
 * <p>Inside the script:
 * <ul>
 *   <li>{@code input} is a JavaScript array of the input resources (parsed JSON objects): the
 *       inline {@code resource} parameters first, followed by the resources resolved from the
 *       {@code reference} parameters, in declared order — possibly empty if none were sent.</li>
 *   <li>The value the script evaluates to is collected as output. It may be a single resource
 *       object or an array of resource objects. Each element is parsed back into a FHIR resource
 *       and returned under a {@code return} parameter of the response {@code Parameters}.</li>
 * </ul>
 *
 * <p><b>Security:</b> this provider is disabled unless
 * {@code hapi.fhir.javascript_execution_enabled=true}. The {@code script} parameter is only ever
 * resolved to a file inside the configured scripts directory — the name is validated to a bare
 * file name and the resolved path is verified to stay within that directory, so callers cannot
 * traverse the filesystem or execute code that the administrator has not installed. As
 * defense-in-depth, each script runs in a fresh GraalJS {@link Context} created with no host access
 * — Java class lookup, filesystem, network, and thread creation are all denied by default — so
 * scripts are limited to pure JavaScript and cannot reach the host JVM, filesystem or network. Each
 * invocation is also bounded by an execution timeout
 * ({@code hapi.fhir.javascript_execution_timeout_seconds}, default 30s); a script that overruns has
 * its context canceled and the call fails.
 */
@Conditional({OnR4Condition.class})
@ConditionalOnProperty(name = "hapi.fhir.javascript_execution_enabled", havingValue = "true")
@Service
public class JavaScriptExecutionR4OperationProvider {

	private static final Logger ourLog = LoggerFactory.getLogger(JavaScriptExecutionR4OperationProvider.class);

	/** A script name is a bare file name (optionally ending in {@code .js}) — never a path. */
	private static final Pattern SCRIPT_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]+(\\.js)?");

	private final FhirContext myFhirContext;
	private final DaoRegistry myDaoRegistry;
	private final ObjectMapper myObjectMapper = new ObjectMapper();
	private final Path myScriptsDir;
	private final long myTimeoutSeconds;

	public JavaScriptExecutionR4OperationProvider(
			FhirContext theFhirContext,
			DaoRegistry theDaoRegistry,
			@Value("${hapi.fhir.javascript_execution_scripts_dir:}") String theScriptsDir,
			@Value("${hapi.fhir.javascript_execution_timeout_seconds:30}") long theTimeoutSeconds) {
		this.myFhirContext = theFhirContext;
		this.myDaoRegistry = theDaoRegistry;
		this.myScriptsDir = (theScriptsDir == null || theScriptsDir.isBlank())
				? null
				: Paths.get(theScriptsDir).toAbsolutePath().normalize();
		this.myTimeoutSeconds = theTimeoutSeconds;
	}

	@Operation(name = "$execute-javascript", idempotent = true)
	public Parameters executeJavascript(
			@OperationParam(name = "script", min = 1, max = 1) StringType theScriptName,
			@OperationParam(name = "resource", min = 0, max = OperationParam.MAX_UNLIMITED)
					List<IBaseResource> theInputResources,
			@OperationParam(name = "reference", min = 0, max = OperationParam.MAX_UNLIMITED)
					List<Reference> theReferences,
			RequestDetails theRequestDetails) {

		if (theScriptName == null || theScriptName.isEmpty()) {
			throw new InvalidRequestException("A non-empty 'script' parameter (the script name) is required.");
		}

		String script = loadScript(theScriptName.getValue());

		// 'input' = inline 'resource' parameters first, then the resources resolved from the literal
		// 'reference' parameters (read from the server before the script runs), in declared order.
		List<IBaseResource> inputs = new ArrayList<>();
		if (theInputResources != null) {
			inputs.addAll(theInputResources);
		}
		if (theReferences != null) {
			for (Reference reference : theReferences) {
				inputs.add(resolveReference(reference, theRequestDetails));
			}
		}

		String resultJson = runScript(script, serializeInput(inputs));

		Parameters response = new Parameters();
		for (Resource resource : parseOutputResources(resultJson)) {
			response.addParameter().setName("return").setResource(resource);
		}
		return response;
	}

	/** Reads the resource named by a literal reference (e.g. {@code Patient/123}) from the server. */
	private IBaseResource resolveReference(Reference theReference, RequestDetails theRequestDetails) {
		String reference = theReference == null ? null : theReference.getReference();
		if (reference == null || reference.isBlank()) {
			throw new InvalidRequestException(
					"Each 'reference' parameter must contain a literal reference such as 'Patient/123'.");
		}

		IdType id = new IdType(reference);
		if (!id.hasResourceType() || !id.hasIdPart()) {
			throw new InvalidRequestException("Not a literal reference (expected '<Type>/<id>'): " + reference);
		}
		if (!myDaoRegistry.isResourceTypeSupported(id.getResourceType())) {
			throw new InvalidRequestException("Unsupported resource type in reference: " + reference);
		}

		IFhirResourceDao<?> dao = myDaoRegistry.getResourceDao(id.getResourceType());
		return dao.read(id, theRequestDetails);
	}

	/**
	 * Resolves a caller-supplied script name to a file inside the configured scripts directory,
	 * rejecting anything that is not a plain name within that directory.
	 */
	private String loadScript(String theScriptName) {
		if (myScriptsDir == null) {
			throw new InvalidRequestException(
					"Server-side script execution is not configured (hapi.fhir.javascript_execution_scripts_dir).");
		}
		if (!SCRIPT_NAME_PATTERN.matcher(theScriptName).matches()) {
			throw new InvalidRequestException("Invalid script name: " + theScriptName);
		}

		String fileName = theScriptName.endsWith(".js") ? theScriptName : theScriptName + ".js";
		Path scriptPath = myScriptsDir.resolve(fileName).normalize();
		// Defense-in-depth: even after name validation, confirm we never escaped the base directory.
		if (!scriptPath.startsWith(myScriptsDir)) {
			throw new InvalidRequestException("Invalid script name: " + theScriptName);
		}
		if (!Files.isRegularFile(scriptPath)) {
			throw new InvalidRequestException("Unknown script: " + theScriptName);
		}

		try {
			return Files.readString(scriptPath, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new InternalErrorException("Failed to read script '" + theScriptName + "': " + e.getMessage(), e);
		}
	}

	/** Serializes the input resources into a single JavaScript-parseable JSON array string. */
	private String serializeInput(List<IBaseResource> theInputResources) {
		IParser parser = myFhirContext.newJsonParser();
		StringBuilder sb = new StringBuilder("[");
		if (theInputResources != null) {
			for (int i = 0; i < theInputResources.size(); i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(parser.encodeResourceToString(theInputResources.get(i)));
			}
		}
		return sb.append(']').toString();
	}

	/**
	 * Runs the script on a dedicated daemon thread and enforces {@link #myTimeoutSeconds}. If the
	 * script overruns, the GraalJS context is cancelled from this thread — even a tight CPU-bound
	 * loop is unwound cleanly, with no need to forcibly stop the thread. Each context is fresh and
	 * isolated and the sandbox denies all host access, so a cancelled script holds no shared state.
	 */
	private String runScript(String theScript, String theInputJson) {
		AtomicReference<String> resultRef = new AtomicReference<>();
		AtomicReference<Exception> errorRef = new AtomicReference<>();

		// No host access: scripts cannot reach Java classes, the filesystem, the network or threads.
		Context context = Context.newBuilder("js")
				.option("engine.WarnInterpreterOnly", "false")
				.build();

		Thread worker = new Thread(
				() -> {
					try {
						resultRef.set(evaluate(context, theScript, theInputJson));
					} catch (Exception e) {
						errorRef.set(e);
					}
				},
				"js-exec-" + Thread.currentThread().getId());
		worker.setDaemon(true);
		worker.start();

		try {
			worker.join(TimeUnit.SECONDS.toMillis(myTimeoutSeconds));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			context.close(true);
			throw new InternalErrorException("Interrupted while waiting for script to complete.", e);
		}

		if (worker.isAlive()) {
			// Cancel the in-flight evaluation; this unblocks the worker by throwing inside the script.
			context.close(true);
			throw new InvalidRequestException("JavaScript execution timed out after " + myTimeoutSeconds + " seconds.");
		}

		context.close();

		Exception error = errorRef.get();
		if (error != null) {
			// A PolyglotException covers syntax/runtime JS errors as well as the sandbox blocking host
			// access (e.g. an undefined 'Java'). Either way it is caller error -> 400, not 500.
			ourLog.debug("JavaScript execution failed", error);
			throw new InvalidRequestException("JavaScript execution failed: " + error.getMessage(), error);
		}
		return resultRef.get();
	}

	/** Evaluates the script in the given fresh, sandboxed GraalJS context and returns the JSON result. */
	private String evaluate(Context theContext, String theScript, String theInputJson) {
		org.graalvm.polyglot.Value bindings = theContext.getBindings("js");
		bindings.putMember("__inputJson", theInputJson);

		theContext.eval("js", "var input = JSON.parse(__inputJson);");
		org.graalvm.polyglot.Value result = theContext.eval("js", theScript);
		bindings.putMember("__result", result);
		return theContext.eval("js", "JSON.stringify(__result === undefined ? null : __result)").asString();
	}

	/** Parses the script's JSON result (single object or array) back into FHIR resources. */
	private List<Resource> parseOutputResources(String theResultJson) {
		List<Resource> resources = new ArrayList<>();
		if (theResultJson == null) {
			return resources;
		}

		JsonNode root;
		try {
			root = myObjectMapper.readTree(theResultJson);
		} catch (Exception e) {
			throw new InvalidRequestException("Script result was not valid JSON: " + e.getMessage(), e);
		}

		if (root == null || root.isNull()) {
			return resources;
		}

		IParser parser = myFhirContext.newJsonParser();
		if (root.isArray()) {
			for (JsonNode node : root) {
				resources.add(parseResourceNode(parser, node));
			}
		} else {
			resources.add(parseResourceNode(parser, root));
		}
		return resources;
	}

	private Resource parseResourceNode(IParser theParser, JsonNode theNode) {
		if (!theNode.isObject() || !theNode.hasNonNull("resourceType")) {
			throw new InvalidRequestException(
					"Script must return FHIR resource object(s) with a 'resourceType'; got: " + theNode);
		}
		try {
			return (Resource) theParser.parseResource(theNode.toString());
		} catch (Exception e) {
			throw new InvalidRequestException("Could not parse script result as a FHIR resource: " + e.getMessage(), e);
		}
	}
}
