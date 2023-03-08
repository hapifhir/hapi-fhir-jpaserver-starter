package ch.ahdis.matchbox;

import java.io.ByteArrayInputStream;
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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBinary;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.validation.IgLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.binary.svc.NullBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionDao;
import ca.uhn.fhir.jpa.model.dao.JpaPid;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionEntity;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionResourceEntity;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ca.uhn.fhir.jpa.packages.JpaPackageCache;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager.PackageContents;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.util.BinaryUtil;

/**
 * Loads packages from the classpath
 * 
 * @author oliveregger
 *
 */
public class IgLoaderFromJpaPackageCache extends IgLoader {

	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IgLoaderFromJpaPackageCache.class);

	private IHapiPackageCacheManager myPackageCacheManager;
	private INpmPackageVersionDao myNpmPackageVersionDao;
	private DaoRegistry myDaoRegistry;
	private IBinaryStorageSvc myBinaryStorageSvc;
	private PlatformTransactionManager myTxManager;

	private final Map<FhirVersionEnum, FhirContext> myVersionToContext = Collections.synchronizedMap(new HashMap<>());

	@Autowired
	private FhirContext myCtx;

	public IgLoaderFromJpaPackageCache(FilesystemPackageCacheManager packageCacheManager, SimpleWorkerContext context,
			String theVersion, boolean debug, IHapiPackageCacheManager myPackageCacheManager,
			INpmPackageVersionDao myNpmPackageVersionDao, DaoRegistry myDaoRegistry, IBinaryStorageSvc myBinaryStorageSvc,
			PlatformTransactionManager myTxManager) {
		super(packageCacheManager, context, theVersion, debug);
		this.myPackageCacheManager = myPackageCacheManager;
		this.myNpmPackageVersionDao = myNpmPackageVersionDao;
		this.myDaoRegistry = myDaoRegistry;
		this.myBinaryStorageSvc = myBinaryStorageSvc;
		this.myTxManager = myTxManager;
	}

	/**
	 * Helper method which will attempt to use the IBinaryStorageSvc to resolve the
	 * binary blob if available. If the bean is unavailable, fallback to assuming we
	 * are using an embedded base64 in the data element.
	 * 
	 * @param theBinary the Binary who's `data` blob you want to retrieve
	 * @return a byte array containing the blob.
	 *
	 * @throws IOException
	 */
	private byte[] fetchBlobFromBinary(IBaseBinary theBinary) throws IOException {
		if (myBinaryStorageSvc != null && !(myBinaryStorageSvc instanceof NullBinaryStorageSvcImpl)) {
			return myBinaryStorageSvc.fetchDataBlobFromBinary(theBinary);
		} else {
			byte[] value = BinaryUtil.getOrCreateData(myCtx, theBinary).getValue();
			if (value == null) {
				throw new InternalErrorException(
						Msg.code(1296) + "Failed to fetch blob from Binary/" + theBinary.getIdElement());
			}
			return value;
		}
	}

	@SuppressWarnings("unchecked")
	private IFhirResourceDao<IBaseBinary> getBinaryDao() {
		return myDaoRegistry.getResourceDao("Binary");
	}

	@Nonnull
	public FhirContext getFhirContext(FhirVersionEnum theFhirVersion) {
		return myVersionToContext.computeIfAbsent(theFhirVersion, v -> new FhirContext(v));
	}

	private org.hl7.fhir.r5.model.Resource loadPackageEntity(NpmPackageVersionResourceEntity contents) {
		try {
			JpaPid binaryPid = JpaPid.fromId(contents.getResourceBinary().getId());
			IBaseBinary binary = getBinaryDao().readByPid(binaryPid);
			byte[] resourceContentsBytes = fetchBlobFromBinary(binary);
			String resourceContents = new String(resourceContentsBytes, StandardCharsets.UTF_8);
			switch (contents.getFhirVersion()) {
			case DSTU3:
				return VersionConvertorFactory_30_50
						.convertResource(new org.hl7.fhir.dstu3.formats.JsonParser().parse(resourceContents));
			case R4:
				return VersionConvertorFactory_40_50
						.convertResource(new org.hl7.fhir.r4.formats.JsonParser().parse(resourceContents));
			case R4B:
				return VersionConvertorFactory_43_50
						.convertResource(new org.hl7.fhir.r4b.formats.JsonParser().parse(resourceContents));
			case R5:
				return new org.hl7.fhir.r5.formats.JsonParser().parse(resourceContents);
			default:
				log.error("FHIR version not support for loading form matchbox case ");
				throw new RuntimeException(Msg.code(1305) + "Failed to load package resource " + contents);
			}
		} catch (Exception e) {
			throw new RuntimeException(Msg.code(1305) + "Failed to load package resource " + contents, e);
		}
	}

	@Override
	public void loadIg(List<ImplementationGuide> igs, Map<String, byte[]> binaries, String src, boolean recursive)
			throws IOException, FHIRException {
		if (src.startsWith("hl7.terminology.r4") || src.startsWith("hl7.terminology.r5")) {
			log.info("Package ignored (use hl7.terminology#5.0.0) " + src);
			return;
		}
		if (src.equals("hl7.fhir.cda#dev")) {
			String replace = "hl7.fhir.cda#2.1.0-cibuild";
			log.info("Replacing hl7.fhir.cda#dev with " + replace);
			loadIg(igs, binaries, replace, recursive);
			return;
		}
		if (src.equals("ch.fhir.ig.ch-epr-term#current")) {
			String replace  = "ch.fhir.ig.ch-epr-term#2.0.x";
			log.info("ch.fhir.ig.ch-epr-term#current with " + replace);
			loadIg(igs, binaries, replace, recursive);
			return;
		}
		if (getContext().getLoadedPackages().contains(src)) {
			log.info("Package already in context " + src);
			return;
		}
		new TransactionTemplate(myTxManager).execute(tx -> {
			String version = null;
			String id = src;
			if (src.contains("#")) {
				version = src.substring(src.indexOf("#") + 1);
				id = src.substring(0, src.indexOf("#"));
			}
			NpmPackage npm = ((JpaPackageCache) myPackageCacheManager).loadPackageFromCacheOnly(id, version);
			if (npm == null) {
				log.error("package not found: " + id +" "+version ); 
				return null;
			}
			for (String dependency : npm.dependencies()) {
				log.info("Loading depending package " + dependency + " for "+src);
				try {
					loadIg(igs, binaries, dependency, recursive);
				} catch (FHIRException | IOException e) {
					throw new RuntimeException(Msg.code(1305) + "Failed to load dependency " + dependency);
				}
				log.info("Finished loading depending package " + dependency + " for "+ src);
			}
			// use above version because of potential .x version we resolve in the cache
			version = npm.version();
			Optional<NpmPackageVersionEntity> npmPackage = myNpmPackageVersionDao.findByPackageIdAndVersion(id, version);
			if (npmPackage.isPresent()) {
				int count = 0;
				log.info("Loading package " + src);

				// this way we have 0.5 seconds per 100 resources (eg hl7.fhir.r4.core has 15 seconds for 3128 resources)
				NpmPackage pi = this.loadPackage(npmPackage.get());
				getContext().getLoadedPackages().add(pi.name() + "#" + pi.version());
				try {
					for (String s : pi.listResources("CodeSystem", "ConceptMap", "ImplementationGuide", "CapabilityStatement",
							"NamingSystem", "Questionnaire", "Conformance", "StructureMap", "ValueSet", "StructureDefinition")) {
						++count;
						Resource r = null;
						try {
							r = loadResourceByVersion(npm.fhirVersion(), TextFile.streamToBytes(pi.load("package", s)), s);
							this.getContext().cacheResource(r);
						} catch (FHIRException e) {
							log.error(s, e);
						} catch (IOException e) {
							log.error(s, e);
						}
					}
				} catch (IOException e) {
					log.error("error reading package", e);
					return null;
				}

				log.info("Finished loading " + count + " conformance resources for package " + pi.name() + "#" + pi.version());

				// with hsql or psql this slow around 7 seconds per 100 resources (oe dev)
				// machine)
				// lets load the package directly
//				List<NpmPackageVersionResourceEntity> resources = npmPackage.get().getResources();
//				for (NpmPackageVersionResourceEntity resource: resources) {
//					++count;
//					if (count % 100 == 0) {
//						log.info(" ... loading "+count);
//					}
//					this.getContext().cacheResource(loadPackageEntity(resource));
//				}
//s				this.getContext().getLoadedPackages().add(id + "#" + version);


			} else {
				throw new RuntimeException(Msg.code(1305) + "Failed to load package resource " + src);
			}
			return null;
		});
	}

	private NpmPackage loadPackage(NpmPackageVersionEntity thePackageVersion) {
		PackageContents content = loadPackageContents(thePackageVersion);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
		try {
			return NpmPackage.fromPackage(inputStream);
		} catch (IOException e) {
			throw new InternalErrorException(Msg.code(1294) + e);
		}
	}

	private IHapiPackageCacheManager.PackageContents loadPackageContents(NpmPackageVersionEntity thePackageVersion) {
		IFhirResourceDao<? extends IBaseBinary> binaryDao = getBinaryDao();
		JpaPid binaryPid = JpaPid.fromId(thePackageVersion.getPackageBinary().getId());
		IBaseBinary binary = binaryDao.readByPid(binaryPid);
		try {
			byte[] content = fetchBlobFromBinary(binary);
			PackageContents retVal = new PackageContents().setBytes(content).setPackageId(thePackageVersion.getPackageId())
					.setVersion(thePackageVersion.getVersionId()).setLastModified(thePackageVersion.getUpdatedTime());
			return retVal;
		} catch (IOException e) {
			throw new InternalErrorException(Msg.code(1295) + "Failed to load package. There was a problem reading binaries",
					e);
		}
	}

	/**
	 * we want to load directly from the jpa package manager internet package cache
	 * manager
	 */
	@Override
	public Map<String, byte[]> loadIgSource(String src, boolean recursive, boolean explore)
			throws FHIRException, IOException {
		throw new RuntimeException(Msg.code(1305) + "Failed to load package, should not be here (loadIgSource) " + src);
	}

	/**
	 * we overwrite this method to not provoke depend packages to be loaded,
	 * otherwise we get cda-core-2.0.tgz .. load IG from hl7.terminology.r4#5.0.0
	 */
	public Map<String, byte[]> loadPackage(NpmPackage pi, boolean loadInContext) throws FHIRException, IOException {
		throw new RuntimeException(Msg.code(1305) + "Failed to load package, should not be her (loadpackage) " + pi);
	}

}
