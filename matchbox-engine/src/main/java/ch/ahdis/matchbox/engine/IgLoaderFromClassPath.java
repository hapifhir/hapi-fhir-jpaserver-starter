package ch.ahdis.matchbox.engine;

/*
 * #%L
 * Matchbox Engine
 * %%
 * Copyright (C) 2022 ahdis
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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.validation.IgLoader;

/**
 * Loads packages from the classpath
 * @author oliveregger
 *
 */
public class IgLoaderFromClassPath extends IgLoader {

	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IgLoaderFromClassPath.class);

	public IgLoaderFromClassPath(FilesystemPackageCacheManager packageCacheManager, SimpleWorkerContext context,
			String theVersion, boolean debug) {
		super(packageCacheManager, context, theVersion, debug);
	}

	@Override
	public void loadIg(List<ImplementationGuide> igs, Map<String, byte[]> binaries, String src, boolean recursive)
			throws IOException, FHIRException {

		if (binaries == null) {
			// prevent NP exception ValidatorUtils.grabNatives
			binaries = new HashMap<String, byte[]>();
		}
		super.loadIg(igs, binaries, src, recursive);
	}

	/**
	 * we want to load directly from the classpath the packages and not from the
	 * internet package cache manager
	 */
	@Override
	public Map<String, byte[]> loadIgSource(String src, boolean recursive, boolean explore)
			throws FHIRException, IOException {
		// we assume that /nameofpackage.tgz, not within in multiple directories
		if (src.startsWith("/") && src.endsWith(".tgz") && src.lastIndexOf("/") == 0) {
			InputStream stream = getClass().getResourceAsStream(src);
			if (stream == null) {
				throw new FHIRException("Unable to find/resolve/read from classpath" + src);
			}
			try {
				if (src.endsWith(".tgz")) {
					return loadPackage(NpmPackage.fromPackage(stream), false);
				}
			} finally {
				stream.close();
			}
			throw new FHIRException("Unable to find/resolve/read from classpath" + src);
		}
		return super.loadIgSource(src, recursive, explore);
	}

	/**
	 * we overwrite this method to not provoke depend packages to be loaded,
	 * otherwise we get cda-core-2.0.tgz .. load IG from hl7.terminology.r4#5.0.0
	 */
	public Map<String, byte[]> loadPackage(NpmPackage pi, boolean loadInContext) throws FHIRException, IOException {
		Map<String, byte[]> res = new HashMap<String, byte[]>();
//   for (String s : pi.dependencies()) {
//     if (s.endsWith(".x") && s.length() > 2) {
//       String packageMajorMinor = s.substring(0, s.length() - 2);
//       boolean found = false;
//       for (int i = 0; i < getContext().getLoadedPackages().size() && !found; ++i) {
//         String loadedPackage = getContext().getLoadedPackages().get(i);
//         if (loadedPackage.startsWith(packageMajorMinor)) {
//           found = true;
//         }
//       }
//       if (found)
//         continue;
//     }
//     if (!getContext().getLoadedPackages().contains(s)) {
//       if (!VersionUtilities.isCorePackage(s)) {
//         System.out.println("+  .. load IG from " + s);
//         res.putAll(fetchByPackage(s, loadInContext));
//       }
//     }
//   }
//
    getContext().getLoadedPackages().add(pi.name() + "#" + pi.version());
		log.info("Loading package " + pi.name() + "#" + pi.version() + "[FHIR] version=" + pi.fhirVersion());
		int count = 0;
		for (String s : pi.listResources("CodeSystem", "ConceptMap", "ImplementationGuide", "CapabilityStatement",
				"NamingSystem","Questionnaire", "Conformance", "StructureMap", "ValueSet", "StructureDefinition")) {
			++count;
			res.put(s, TextFile.streamToBytes(pi.load("package", s)));
		}
		String ini = "[FHIR]\r\nversion=" + pi.fhirVersion() + "\r\n";
		res.put("version.info", ini.getBytes());
		log.info("Finished loading " + count + " conformance resources for package " + pi.name() + "#" + pi.version());
		return res;
	}

}
