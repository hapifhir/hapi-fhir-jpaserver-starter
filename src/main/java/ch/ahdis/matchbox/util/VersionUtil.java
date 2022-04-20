package ch.ahdis.matchbox.util;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.left;

/*
  Copyright (c) 2011+, HL7, Inc.
  All rights reserved.
  
  Redistribution and use in source and binary forms, with or without modification, 
  are permitted provided that the following conditions are met:
    
   * Redistributions of source code must retain the above copyright notice, this 
     list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright notice, 
     this list of conditions and the following disclaimer in the documentation 
     and/or other materials provided with the distribution.
   * Neither the name of HL7 nor the names of its contributors may be used to 
     endorse or promote products derived from this software without specific 
     prior written permission.
  
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.
  
 */


import java.io.InputStream;
import java.time.Duration;
import java.util.Date;
import java.util.Properties;

import org.hl7.fhir.r5.model.InstantType;
import org.hl7.fhir.utilities.Utilities;

/**
 * Used internally by matchbox to log the version of the HAPI FHIR framework
 * once, when the framework is first loaded by the classloader.
 */
public class VersionUtil {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ch.ahdis.matchbox.util.VersionUtil.class);
  private static String ourVersion;
  private static String ourBuildNumber;
  private static String ourBuildTime;

  static {
    initialize();
  }

  public static String getBuildNumber() {
    return ourBuildNumber;
  }

  public static String getBuildTime() {
    return ourBuildTime;
  }

  public static String getVersion() {
    return ourVersion;
  }
  
  public static String getPoweredBy() {
    return "powered by matchbox "+ ch.ahdis.matchbox.util.VersionUtil.getVersion()+", hapi-fhir "+ ca.uhn.fhir.util.VersionUtil.getVersion()+" and org.hl7.fhir.core "+ org.hl7.fhir.validation.cli.utils.VersionUtil.getVersion();
  }
  

  private static void initialize() {
    try (InputStream is = ca.uhn.fhir.util.VersionUtil.class.getResourceAsStream("/matchbox-build.properties")) {

      Properties p = new Properties();
      if (is != null) {
        p.load(is);
      }

      ourVersion = p.getProperty("matchbox.version");
      ourVersion = defaultIfBlank(ourVersion, "(unknown)");

      ourBuildNumber = p.getProperty("matchbox.buildnumber");
      ourBuildTime = p.getProperty("matchbox.timestamp");

    } catch (Exception e) {
      ourLog.warn("Unable to determine version information", e);
    }
  }

  public static String getVersionString() {
    return "Version " + getVersion() + " (Git# " + left(getBuildNumber(), 12)+"). Built " + getBuildTime();
  }

}