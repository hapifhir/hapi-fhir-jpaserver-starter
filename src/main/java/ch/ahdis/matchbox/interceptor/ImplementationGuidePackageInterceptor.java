package ch.ahdis.matchbox.interceptor;

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

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager.PackageContents;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.util.FhirTerser;

/**
 * ImplementationGuidePackageInterceptor returns the NPM Package stored
 * on the srever
 * 
 * GET {{host}}/ImplementationGuide/id HTTP/1.1 Accept: application/gzip
 *
 */
public class ImplementationGuidePackageInterceptor extends InterceptorAdapter {

  public ImplementationGuidePackageInterceptor(IHapiPackageCacheManager myPackageCacheManager,
      FhirContext myFhirContext) {
    super();
    this.myPackageCacheManager = myPackageCacheManager;
    this.myFhirContext = myFhirContext;
  }

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(ImplementationGuidePackageInterceptor.class);

  private IHapiPackageCacheManager myPackageCacheManager;

  protected FhirContext myFhirContext;

  @Override
  public boolean outgoingResponse(RequestDetails theRequestDetails, ResponseDetails theResponseDetails,
      HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws AuthenticationException {
    IBaseResource responseResource = theResponseDetails.getResponseResource();
    if (responseResource == null) {
      return super.outgoingResponse(theRequestDetails, theResponseDetails, theServletRequest, theServletResponse);
    }
    String accept = defaultString(theServletRequest.getHeader(Constants.HEADER_ACCEPT));
    if ("application/gzip".equals(accept) && responseResource.fhirType().equals("ImplementationGuide")
        && "GET".equals(theServletRequest.getMethod())) {

      FhirTerser terser = myFhirContext.newTerser();
      Optional<String> packageId = terser.getSinglePrimitiveValue(responseResource, "packageId");
      Optional<String> version = terser.getSinglePrimitiveValue(responseResource, "version");
      if (packageId.isPresent() && version.isPresent()) {
        try {
          PackageContents npm = myPackageCacheManager.loadPackageContents(packageId.get(), version.get());
          theServletResponse.setContentType("application/gzip");
          theServletResponse.setBufferSize(npm.getBytes().length);
          ServletOutputStream output = theServletResponse.getOutputStream();
          output.write(npm.getBytes());
          output.close();
          return false;
        } catch (FHIRException | IOException e) {
          log.error("eception retrieving npm package", e);
          return super.outgoingResponse(theRequestDetails, theResponseDetails, theServletRequest, theServletResponse);
        }
      }
    }
    return super.outgoingResponse(theRequestDetails, theResponseDetails, theServletRequest, theServletResponse);
  }

}
