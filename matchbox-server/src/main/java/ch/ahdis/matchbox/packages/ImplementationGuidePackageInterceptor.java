package ch.ahdis.matchbox.packages;

/*
 * #%L
 * Matchbox Server
 * %%
 * Copyright (C) 2018 - 2019 ahdis
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager.PackageContents;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.util.FhirTerser;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.IOException;
import java.util.Optional;

/**
 * ImplementationGuidePackageInterceptor returns the NPM Package stored on the server
 * <p>
 * GET {{host}}/ImplementationGuide/id HTTP/1.1 Accept: application/gzip
 */
public class ImplementationGuidePackageInterceptor extends InterceptorAdapter {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
		.getLogger(ImplementationGuidePackageInterceptor.class);

	private final IHapiPackageCacheManager myPackageCacheManager;

	private final FhirContext myFhirContext;

	public ImplementationGuidePackageInterceptor(final IHapiPackageCacheManager myPackageCacheManager,
																final FhirContext myFhirContext) {
		super();
		this.myPackageCacheManager = myPackageCacheManager;
		this.myFhirContext = myFhirContext;
	}


	@Override
	public boolean outgoingResponse(RequestDetails theRequestDetails, ResponseDetails theResponseDetails,
											  HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
		throws AuthenticationException {
		final IBaseResource responseResource = theResponseDetails.getResponseResource();
		if (responseResource == null) {
			return super.outgoingResponse(theRequestDetails, theResponseDetails, theServletRequest, theServletResponse);
		}
		final String accept = theServletRequest.getHeader(Constants.HEADER_ACCEPT);
		if ("application/gzip".equals(accept) && responseResource.fhirType().equals("ImplementationGuide")
			&& "GET".equals(theServletRequest.getMethod())) {

			final FhirTerser terser = this.myFhirContext.newTerser();
			final Optional<String> packageId = terser.getSinglePrimitiveValue(responseResource, "packageId");
			final Optional<String> version = terser.getSinglePrimitiveValue(responseResource, "version");
			if (packageId.isPresent() && version.isPresent()) {
				try {
					final PackageContents npm = this.myPackageCacheManager.loadPackageContents(packageId.get(),
																														version.get());
					theServletResponse.setContentType("application/gzip");
					theServletResponse.setBufferSize(npm.getBytes().length);
					final ServletOutputStream output = theServletResponse.getOutputStream();
					output.write(npm.getBytes());
					output.close();
					return false;
				} catch (FHIRException | IOException e) {
					log.error("exception retrieving npm package", e);
					return super.outgoingResponse(theRequestDetails,
															theResponseDetails,
															theServletRequest,
															theServletResponse);
				}
			}
		}
		return super.outgoingResponse(theRequestDetails, theResponseDetails, theServletRequest, theServletResponse);
	}

}
