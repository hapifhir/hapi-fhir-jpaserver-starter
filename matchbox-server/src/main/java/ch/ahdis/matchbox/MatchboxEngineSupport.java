package ch.ahdis.matchbox;

import java.io.IOException;
import java.net.URISyntaxException;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.validation.IgLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionDao;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionEntity;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionResourceEntity;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.MatchboxEngine.MatchboxEngineBuilder;

public class MatchboxEngineSupport {
	
	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchboxEngineSupport.class);

	private static MatchboxEngine engine = null;
	
	private boolean initialized = false;

  @Autowired
  private DaoRegistry myDaoRegistry;
  
	
	@Autowired
	private INpmPackageVersionResourceDao myPackageVersionResourceDao;
	
	@Autowired
	private PlatformTransactionManager myTxManager;
	
	@Autowired
	private IHapiPackageCacheManager myPackageCacheManager;

	@Autowired
  private INpmPackageVersionDao myNpmPackageVersionDao;
	
	@Autowired(required = false)
  private IBinaryStorageSvc myBinaryStorageSvc;

//  public ImplementationGuide getImplementationGuide(String canonical) {
//    SearchParameterMap params = new SearchParameterMap();
//    params.setLoadSynchronousUpTo(1);
//    params.add(ImplementationGuide.SP_RESOURCE, new ReferenceParam(canonical));
//    IBundleProvider search = myDaoRegistry.getResourceDao("ImplementationGuide").search(params);
//    Integer size = search.size();
//    if (size!=null && size.intValue()==1) {
//      return (ImplementationGuide) search.getResources(0, 1).get(0);
//    }
//    return null;
//  }

	// FIXME this assumes that the cannoncial and ig version are the same
	public NpmPackageVersionResourceEntity loadPackageAssetByUrl(String theCanonicalUrl) {
		NpmPackageVersionResourceEntity resourceEntity  = new TransactionTemplate(myTxManager).execute(tx -> {
			String canonicalUrl = theCanonicalUrl;
			int versionSeparator = canonicalUrl.lastIndexOf('|');
			Slice<NpmPackageVersionResourceEntity> slice;
			if (versionSeparator != -1) {
				String canonicalVersion = canonicalUrl.substring(versionSeparator + 1);
				canonicalUrl = canonicalUrl.substring(0, versionSeparator);
				slice = myPackageVersionResourceDao.findByCanonicalUrlByCanonicalVersion(PageRequest.of(0, 2), canonicalUrl, canonicalVersion);
			} else {
				slice = myPackageVersionResourceDao.findByCanonicalUrl(PageRequest.of(0, 2), canonicalUrl);
			}
			if (slice.isEmpty()) {
				return null;
			} 
			if (slice.getContent().size()>1) {
				log.error("multiple entries with same canoncial (version) for "+theCanonicalUrl);
			}
			return slice.getContent().get(0);
		});
		return resourceEntity;
	}
		
			
	/**
	 * returns a matchbox-engine for the specified canoncial
	 * @param canonical
	 * @return
	 * @throws IOException 
	 */
	public synchronized MatchboxEngine getMatchboxEngine(String canonical, boolean reload) {
		if (reload) {
			engine = null;
			this.setInitialized(false);
		}
		if (engine == null) {
				try {
					engine = new MatchboxEngineBuilder().getEngine();
				} catch (FHIRException e) {
					log.error("error generating machbox engine", e);
				} catch (IOException e) {
					log.error("error generating machbox engine", e);
				} catch (URISyntaxException e) {
					log.error("error generating machbox engine", e);
				}
				
					IgLoader igLoader = null;
					try {
						igLoader = new IgLoaderFromJpaPackageCache(engine.getPcm(), engine.getContext(), engine.getVersion(),
								engine.isDebug(), myPackageCacheManager, myNpmPackageVersionDao, myDaoRegistry, myBinaryStorageSvc, myTxManager);
						
					} catch (IOException e) {
						log.error("error generating matchbox engine", e);
					}
					engine.setIgLoader(igLoader);
					// FIXME we should do this configurable
					String tx = "https://tx.fhir.org/r4";
					try {
						engine.getContext().setNoTerminologyServer(false);
						engine.getContext().setCanRunWithoutTerminology(false);
						String txLog = engine.setTerminologyServer(tx, null, FhirPublication.R4);
						log.info(txLog);
						engine.loadPackage("hl7.terminology",  "5.0.0");
						engine.loadPackage("hl7.fhir.r4.core",  "4.0.1");
						this.initialized = true;
					} catch (FHIRException | URISyntaxException | IOException e) {
						log.error("error connecting to terminology server "+tx);
						return null;
					}
		}
		
		if (!this.isInitialized()) {
			log.error("ValidationEngine is not yet initialized");
		}

		// Current we just have one matchbox engine, we would like to create one per package and validation parameters
		// This will allow to have multiple valdiation engines defined per package and parameters
		// e.g. we can use validation with one version of a package and another version
		
		// FIXME: get the package from the canonical
		// FIXME: extend to additional validation parameters
		// FIXME: create a MatchboxEngine with the combination of ht package and validation parameters which can be used in subsequent requests
		
		if (canonical == null || "default".equals(canonical)) {
			return engine;
		}
		
		if (engine.getStructureDefinition(canonical)!=null) {
			// Core StructureDefininitions etc.
			return engine;
		}
		
		NpmPackageVersionResourceEntity  npm = loadPackageAssetByUrl(canonical);
		if (npm!=null) {
			NpmPackageVersionEntity npmPackageVersion = npm.getPackageVersion();
			String packageId = npmPackageVersion.getPackageId();
			String packageVersionId = npmPackageVersion.getVersionId();
			for(org.hl7.fhir.r5.model.ImplementationGuide igEngine: engine.getIgs()) {
				// we check that the package is load
				if (packageId.equals(igEngine.getPackageId()) && packageVersionId.equals(igEngine.getVersion())) {
					return engine;
				}
			}
			// loaddd missing package
			try {
				
				log.info("loading package into engine "+packageId+"#"+packageVersionId);
				engine.loadPackage(packageId,  packageVersionId);
				log.info("done loading package into engine "+packageId+"#"+packageVersionId);
			} catch (FHIRException e) {
				log.error("error loading pacakge "+packageId+"#"+packageVersionId, e);
				return null;
			} catch (IOException e) {
				log.error("error loading pacakge "+packageId+"#"+packageVersionId, e);
				return null;
			}
		}
		return engine;
	}


	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	

}
