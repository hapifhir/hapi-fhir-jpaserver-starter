package ch.ahdis.matchbox.mappinglanguage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.loaders.BaseLoaderR5;
import org.hl7.fhir.convertors.loaders.R4ToR5Loader;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.utilities.npm.ToolsVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.cache.IResourceChangeEvent;
import ca.uhn.fhir.jpa.cache.IResourceChangeListener;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.UriParam;

/**
 * IWorkerContext implementation 
 * based on SimpleWorkerContext
 * that converts R4 Resources from the database to R5 on the fly 
 * @author Alexander Kreutz
 *
 */
public class ConvertingWorkerContext extends SimpleWorkerContext implements IResourceChangeListener {

	
	 @Autowired
	 @Qualifier("myJpaValidationSupport")
	 protected IValidationSupport myValidationSupport;
	 
	 @Autowired
	 private DaoRegistry myDaoRegistry;
	 
	 private static final Logger ourLog = LoggerFactory.getLogger(ConvertingWorkerContext.class);
	
	public ConvertingWorkerContext(HapiWorkerContext parent) throws FileNotFoundException, IOException, FHIRException {
		super();
	    setCanRunWithoutTerminology(true);	
	    FilesystemPackageCacheManager pcm = new FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION);
	    NpmPackage npm = pcm.loadPackage("hl7.fhir.r4.core#4.0.1", null);
	    if (npm != null) {
	      version = npm.fhirVersion();
	      loadFromPackage(npm, new R4ToR5Loader(new String[]{"CapabilityStatement", "StructureDefinition", "ValueSet", "CodeSystem", "SearchParameter", "OperationDefinition", "Questionnaire", "ConceptMap", "StructureMap", "NamingSystem"}, new BaseLoaderR5.NullLoaderKnowledgeProvider()));//, ValidatorUtils.loaderForVersion(version));
	    }
	    
	    /*
		for (var str : parent.getStructures()) {
			org.hl7.fhir.r5.model.StructureDefinition r5Structure = (org.hl7.fhir.r5.model.StructureDefinition) VersionConvertor_40_50.convertResource(str);
		    generateSnapshot(r5Structure, true);
		    cacheResource(r5Structure);
		}*/
		
	}
	
	public ConvertingWorkerContext(ConvertingWorkerContext parent) throws FileNotFoundException, IOException, FHIRException {
		super(parent);	
		this.myValidationSupport = parent.myValidationSupport;
		this.myDaoRegistry = parent.myDaoRegistry;
	}
	
	@Override
	public <T extends Resource> T fetchResource(Class<T> class_, String uri) {
		
		T result =super.fetchResource(class_, uri);
		if (result == null) {
			load(getClassForR4(class_.getSimpleName()), uri);
			result = super.fetchResource(class_, uri);
		}
		return result;
	}
	
    public <T extends org.hl7.fhir.r4.model.Resource> void load(Class<T> cl, String uri) {
		
		Resource res = VersionConvertorFactory_40_50.convertResource((org.hl7.fhir.r4.model.Resource) myValidationSupport.fetchResource(cl, uri));
		if (res != null) {
		  dropResource(res.getClass().getSimpleName(), res.getId());		
		  ourLog.info("cache: "+res);
		  cacheResource(res);
		}
	}
    
    @Override
	public StructureMap getTransform(String code) {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		StructureMap result = super.getTransform(code);
		if (result == null) {
			loadMap(code);
			result = super.getTransform(code);
		}
		return result;
	
	}
	
	public void loadMap(String code) {
		SearchParameterMap params = new SearchParameterMap();
		params.setLoadSynchronousUpTo(1);
		params.add(StructureMap.SP_URL, new UriParam(code));
		IBundleProvider search = myDaoRegistry.getResourceDao("StructureMap").search(params);
		if (search.size()>0) {
		  List<IBaseResource> results = search.getResources(0, 1);		
		  dropResource("StructureMap", code);
		  cacheResource(VersionConvertorFactory_40_50.convertResource((org.hl7.fhir.r4.model.StructureMap) results.get(0)));
		}
	}
	
	public void load(String resourceType, IIdType id) {
		IBaseResource res = myDaoRegistry.getResourceDao(resourceType).read(id);
		if (res != null) {
			ourLog.info("cache: "+res);
			Resource convRes = VersionConvertorFactory_40_50.convertResource((org.hl7.fhir.r4.model.Resource) res);
			dropResource(resourceType, convRes.getId());
			cacheResource(convRes);
		}
	}
    
	@Override
	public void handleChange(IResourceChangeEvent theResourceChangeEvent) {
		for (IIdType type : theResourceChangeEvent.getDeletedResourceIds()) {
			dropResource(type.getResourceType(), type.getIdPart());
		}
		for (IIdType type : theResourceChangeEvent.getCreatedResourceIds()) {
			load(type.getResourceType(), type);
		}
		for (IIdType type : theResourceChangeEvent.getUpdatedResourceIds()) {
			load(type.getResourceType(), type);
		}
		
	}

	@Override
	public void handleInit(Collection<IIdType> theResourceIds) {
		ourLog.info("initializing WorkerContext cache");
		for (IIdType type : theResourceIds) {			
			load(type.getResourceType(), type);
		}		
	}
	
	public <T extends org.hl7.fhir.r4.model.Resource> Class<T> getClassForR4(String resourceType) {
		return myDaoRegistry.getResourceDao(resourceType).getResourceType();
	}
	
	@Override
	public String getVersion() {			
		return "5.0";
	}

	
	/*
	@Override
	protected void copy(SimpleWorkerContext other) {
		System.out.println("copy");
		super.copy(other);
	}

	@Override
	public List<String> getLoadedPackages() {
		System.out.println("getLoadedPackages");
		return super.getLoadedPackages();
	}

	@Override
	public String connectToTSServer(TerminologyClient client, String log) {
		System.out.println("connectToTSServer");
		return super.connectToTSServer(client, log);
	}

	@Override
	public void loadFromFile(InputStream stream, String name, IContextResourceLoader loader) throws IOException, FHIRException {
		System.out.println("loadFromFile");
		super.loadFromFile(stream, name, loader);
	}

	@Override
	public void loadFromFile(InputStream stream, String name, IContextResourceLoader loader, ILoadFilter filter) throws IOException, FHIRException {
		System.out.println("loadFromFile");
		super.loadFromFile(stream, name, loader, filter);
	}

	@Override
	public int loadFromPackage(NpmPackage pi, IContextResourceLoader loader) throws FileNotFoundException, IOException, FHIRException {
		System.out.println("loadFromPackage");
		return super.loadFromPackage(pi, loader);
	}

	@Override
	public int loadFromPackage(NpmPackage pi, IContextResourceLoader loader, String[] types) throws FileNotFoundException, IOException, FHIRException {
		System.out.println("loadFromPackage");
		return super.loadFromPackage(pi, loader, types);
	}

	@Override
	public int loadFromPackageAndDependencies(NpmPackage pi, IContextResourceLoader loader, BasePackageCacheManager pcm) throws FileNotFoundException, IOException, FHIRException {
		System.out.println("loadFromPackageAndDependencies");
		return super.loadFromPackageAndDependencies(pi, loader, pcm);
	}

	@Override
	public int loadFromPackageAndDependenciesInt(NpmPackage pi, IContextResourceLoader loader, BasePackageCacheManager pcm, String path) throws FileNotFoundException, IOException, FHIRException {
		System.out.println("loadFromPackageAndDependenciesInt");
		return super.loadFromPackageAndDependenciesInt(pi, loader, pcm, path);
	}

	@Override
	public int loadFromPackageInt(NpmPackage pi, IContextResourceLoader loader, String... types) throws FileNotFoundException, IOException, FHIRException {
		System.out.println("loadFromPackageInt");
		return super.loadFromPackageInt(pi, loader, types);
	}

	@Override
	public void loadFromFile(String file, IContextResourceLoader loader) throws IOException, FHIRException {
		System.out.println("loadFromFile");
		super.loadFromFile(file, loader);
	}

	@Override
	public IParser getParser(ParserType type) {
		System.out.println("getParser");
		return super.getParser(type);
	}

	@Override
	public IParser getParser(String type) {
		System.out.println("getParser");
		return super.getParser(type);
	}

	@Override
	public IParser newJsonParser() {
		System.out.println("newJsonParser");
		return super.newJsonParser();
	}

	@Override
	public IParser newXmlParser() {
		System.out.println("newXmlParser");
		return super.newXmlParser();
	}

	@Override
	public IResourceValidator newValidator() throws FHIRException {
		System.out.println("newValidator");
		return super.newValidator();
	}

	@Override
	public List<String> getResourceNames() {
		System.out.println("getResourceNames");
		return super.getResourceNames();
	}

	@Override
	public List<String> getTypeNames() {
		System.out.println("getTypeNames");
		return super.getTypeNames();
	}

	@Override
	public String getAbbreviation(String name) {
		System.out.println("getAbbreviation");
		return super.getAbbreviation(name);
	}

	@Override
	public boolean isDatatype(String typeSimple) {
		System.out.println("isDatatype");
		return super.isDatatype(typeSimple);
	}

	@Override
	public boolean isResource(String t) {
		System.out.println("isResource");
		return super.isResource(t);
	}

	@Override
	public boolean hasLinkFor(String typeSimple) {
		System.out.println("hasLinkFor");
		return super.hasLinkFor(typeSimple);
	}

	@Override
	public String getLinkFor(String corePath, String typeSimple) {
		System.out.println("getLinkFor");
		return super.getLinkFor(corePath, typeSimple);
	}

	@Override
	public BindingResolution resolveBinding(StructureDefinition profile, ElementDefinitionBindingComponent binding, String path) {
		System.out.println("resolveBinding");
		return super.resolveBinding(profile, binding, path);
	}

	@Override
	public BindingResolution resolveBinding(StructureDefinition profile, String url, String path) {
		System.out.println("resolveBinding");
		return super.resolveBinding(profile, url, path);
	}

	@Override
	public String getLinkForProfile(StructureDefinition profile, String url) {
		System.out.println("getLinkForProfile");
		return super.getLinkForProfile(profile, url);
	}

	@Override
	public Questionnaire getQuestionnaire() {
		System.out.println("getQuestionnaire");
		return super.getQuestionnaire();
	}

	@Override
	public void setQuestionnaire(Questionnaire questionnaire) {
		System.out.println("setQuestionnaire");
		super.setQuestionnaire(questionnaire);
	}

	@Override
	public List<StructureDefinition> allStructures() {
		System.out.println("allStructures");
		return super.allStructures();
	}

	@Override
	public void loadBinariesFromFolder(String folder) throws FileNotFoundException, Exception {
		System.out.println("loadBinariesFromFolder");
		super.loadBinariesFromFolder(folder);
	}

	@Override
	public void loadBinariesFromFolder(NpmPackage pi) throws FileNotFoundException, Exception {
		System.out.println("loadBinariesFromFolder");
		super.loadBinariesFromFolder(pi);
	}

	@Override
	public void loadFromFolder(String folder) throws FileNotFoundException, Exception {
		System.out.println("loadFromFolder");
		super.loadFromFolder(folder);
	}

	@Override
	public boolean prependLinks() {
		System.out.println("prependLinks");
		return super.prependLinks();
	}

	@Override
	public boolean hasCache() {
		System.out.println("hasCache");
		return super.hasCache();
	}

	

	@Override
	public List<StructureMap> findTransformsforSource(String url) {
		System.out.println("findTransformsforSource");
		return super.findTransformsforSource(url);
	}

	@Override
	public IValidatorFactory getValidatorFactory() {
		System.out.println("getValidatorFactory");
		return super.getValidatorFactory();
	}

	@Override
	public void setValidatorFactory(IValidatorFactory validatorFactory) {
		System.out.println("setValidatorFactory");
		super.setValidatorFactory(validatorFactory);
	}

	

	@Override
	public StructureDefinition fetchRawProfile(String uri) {
		System.out.println("fetchRawProfile");
		return super.fetchRawProfile(uri);
	}

	@Override
	public void generateSnapshot(StructureDefinition p) throws DefinitionException, FHIRException {
		System.out.println("generateSnapshot");
		super.generateSnapshot(p);
	}

	@Override
	public void generateSnapshot(StructureDefinition p, boolean logical) throws DefinitionException, FHIRException {
		System.out.println("generateSnapshot");
		super.generateSnapshot(p, logical);
	}

	@Override
	public boolean isIgnoreProfileErrors() {
		System.out.println("isIgnoreProfileErrors");
		return super.isIgnoreProfileErrors();
	}

	@Override
	public void setIgnoreProfileErrors(boolean ignoreProfileErrors) {
		System.out.println("setIgnoreProfileErrors");
		super.setIgnoreProfileErrors(ignoreProfileErrors);
	}

	@Override
	public String listMapUrls() {
		System.out.println("listMapUrls");
		return super.listMapUrls();
	}

	@Override
	public boolean isProgress() {
		System.out.println("isProgress");
		return super.isProgress();
	}

	@Override
	public void setProgress(boolean progress) {
		System.out.println("setProgress");
		super.setProgress(progress);
	}

	@Override
	public boolean hasPackage(String id, String ver) {
		System.out.println("hasPackage");
		return super.hasPackage(id, ver);
	}

	@Override
	public boolean hasPackage(String idAndver) {
		System.out.println("hasPackage");
		return super.hasPackage(idAndver);
	}

	@Override
	public void setClock(TimeTracker tt) {
	    System.out.println("setClock");
		super.setClock(tt);
	}

	@Override
	public boolean isCanNoTS() {
		System.out.println("isCanNoTS");
		return super.isCanNoTS();
	}

	@Override
	public void setCanNoTS(boolean canNoTS) {
		System.out.println("setCanNoTS");
		super.setCanNoTS(canNoTS);
	}

	@Override
	public XVerExtensionManager getXVer() {
		System.out.println("getXVer");
		return super.getXVer();
	}

	@Override
	protected void copy(BaseWorkerContext other) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.copy(other);
	}

	@Override
	public void cachePackage(PackageVersion packageDetails, List<PackageVersion> dependencies) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.cachePackage(packageDetails, dependencies);
	}

	@Override
	public void cacheResource(Resource r) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.cacheResource(r);
	}

	@Override
	public void registerResourceFromPackage(CanonicalResourceProxy r, PackageVersion packageInfo) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.registerResourceFromPackage(r, packageInfo);
	}

	@Override
	public void cacheResourceFromPackage(Resource r, PackageVersion packageInfo) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.cacheResourceFromPackage(r, packageInfo);
	}

	@Override
	public void fixOldSD(StructureDefinition sd) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.fixOldSD(sd);
	}

	@Override
	protected <T extends CanonicalResource> void seeMetadataResource(T r, Map<String, T> map, List<T> list, boolean addId) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.seeMetadataResource(r, map, list, addId);
	}

	@Override
	public CodeSystem fetchCodeSystem(String system) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.fetchCodeSystem(system);
	}

	@Override
	public boolean supportsSystem(String system) throws TerminologyServiceException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.supportsSystem(system);
	}

	@Override
	protected void tlog(String msg) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.tlog(msg);
	}

	@Override
	public int getExpandCodesLimit() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getExpandCodesLimit();
	}

	@Override
	public void setExpandCodesLimit(int expandCodesLimit) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setExpandCodesLimit(expandCodesLimit);
	}

	@Override
	public ValueSetExpansionOutcome expandVS(ElementDefinitionBindingComponent binding, boolean cacheOk, boolean heirarchical) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.expandVS(binding, cacheOk, heirarchical);
	}

	@Override
	public ValueSetExpansionOutcome expandVS(ConceptSetComponent inc, boolean hierarchical) throws TerminologyServiceException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.expandVS(inc, hierarchical);
	}

	@Override
	public ValueSetExpansionOutcome expandVS(ValueSet vs, boolean cacheOk, boolean heirarchical) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.expandVS(vs, cacheOk, heirarchical);
	}

	@Override
	public ValueSetExpansionOutcome expandVS(ValueSet vs, boolean cacheOk, boolean heirarchical, Parameters p) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.expandVS(vs, cacheOk, heirarchical, p);
	}

	@Override
	public ValidationResult validateCode(ValidationOptions options, String system, String code, String display) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.validateCode(options, system, code, display);
	}

	@Override
	public ValidationResult validateCode(ValidationOptions options, String system, String code, String display, ValueSet vs) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.validateCode(options, system, code, display, vs);
	}

	@Override
	public ValidationResult validateCode(ValidationOptions options, String code, ValueSet vs) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.validateCode(options, code, vs);
	}

	@Override
	public void validateCodeBatch(ValidationOptions options, List<? extends CodingValidationRequest> codes, ValueSet vs) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.validateCodeBatch(options, codes, vs);
	}

	@Override
	public ValidationResult validateCode(ValidationOptions options, Coding code, ValueSet vs) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.validateCode(options, code, vs);
	}

	@Override
	public ValidationResult validateCode(ValidationOptions options, CodeableConcept code, ValueSet vs) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.validateCode(options, code, vs);
	}

	@Override
	public ValidationResult processValidationResult(Parameters pOut) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.processValidationResult(pOut);
	}

	@Override
	public void initTS(String cachePath) throws Exception {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.initTS(cachePath);
	}

	@Override
	public void clearTSCache(String url) throws Exception {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.clearTSCache(url);
	}

	@Override
	public List<ConceptMap> findMapsForSource(String url) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.findMapsForSource(url);
	}

	@Override
	public boolean isCanRunWithoutTerminology() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.isCanRunWithoutTerminology();
	}

	@Override
	public void setCanRunWithoutTerminology(boolean canRunWithoutTerminology) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setCanRunWithoutTerminology(canRunWithoutTerminology);
	}

	@Override
	public void setLogger(ILoggingService logger) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setLogger(logger);
	}

	@Override
	public Parameters getExpansionParameters() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getExpansionParameters();
	}

	@Override
	public void setExpansionProfile(Parameters expParameters) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setExpansionProfile(expParameters);
	}

	@Override
	public boolean isNoTerminologyServer() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.isNoTerminologyServer();
	}

	@Override
	public void setNoTerminologyServer(boolean noTerminologyServer) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setNoTerminologyServer(noTerminologyServer);
	}

	@Override
	public String getName() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getName();
	}

	@Override
	public void setName(String name) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setName(name);
	}

	@Override
	public Set<String> getResourceNamesAsSet() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getResourceNamesAsSet();
	}

	@Override
	public boolean isAllowLoadingDuplicates() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.isAllowLoadingDuplicates();
	}

	@Override
	public void setAllowLoadingDuplicates(boolean allowLoadingDuplicates) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setAllowLoadingDuplicates(allowLoadingDuplicates);
	}

	@Override
	public <T extends Resource> T fetchResourceWithException(Class<T> class_, String uri) throws FHIRException {		
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+":A "+uri);
		T result = super.fetchResourceWithException(class_, uri);
		
		return result;
	}

	@Override
	public <T extends Resource> T fetchResourceWithException(String cls, String uri) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+":B "+uri);
		return super.fetchResourceWithException(cls, uri);
	}
	
	
	@Override
	public <T extends Resource> T fetchResourceWithException(Class<T> class_, String uri, CanonicalResource source) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+":C "+uri);
		T result = super.fetchResourceWithException(class_, uri, source);
		return result;	
	}
	
	

	@Override
	public <T extends Resource> T fetchResourceWithException(String cls, String uri, CanonicalResource source) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+":D "+uri);
		return super.fetchResourceWithException(cls, uri, source);
	}

	@Override
	public Resource fetchResourceById(String type, String uri) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.fetchResourceById(type, uri);
	}

	@Override
	public <T extends Resource> T fetchResource(Class<T> class_, String uri, CanonicalResource source) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.fetchResource(class_, uri, source);
	}

	
	@Override
	public TranslationServices translator() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.translator();
	}

	@Override
	public void setTranslator(TranslationServices translator) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setTranslator(translator);
	}

	@Override
	public void reportStatus(JsonObject json) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.reportStatus(json);
	}

	@Override
	public void dropResource(Resource r) throws FHIRException {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.dropResource(r);
	}

	@Override
	public void dropResource(String fhirType, String id) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" id="+id);
		super.dropResource(fhirType, id);
	}

	@Override
	public List<CanonicalResource> allConformanceResources() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.allConformanceResources();
	}

	@Override
	public String listSupportedSystems() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.listSupportedSystems();
	}

	@Override
	public int totalCount() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.totalCount();
	}

	@Override
	public List<ConceptMap> listMaps() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.listMaps();
	}

	@Override
	public List<StructureMap> listTransforms() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.listTransforms();
	}

	

	@Override
	public List<StructureDefinition> listStructures() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.listStructures();
	}

	@Override
	public StructureDefinition getStructure(String code) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getStructure(code);
	}

	@Override
	public String oid2Uri(String oid) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.oid2Uri(oid);
	}

	@Override
	public void cacheVS(JsonObject json, Map<String, ValidationResult> t) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.cacheVS(json, t);
	}

	@Override
	public SearchParameter getSearchParameter(String code) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getSearchParameter(code);
	}

	@Override
	public String getOverrideVersionNs() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getOverrideVersionNs();
	}

	@Override
	public void setOverrideVersionNs(String value) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setOverrideVersionNs(value);
	}

	@Override
	public ILoggingService getLogger() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getLogger();
	}

	@Override
	public StructureDefinition fetchTypeDefinition(String typeName) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.fetchTypeDefinition(typeName);
	}

	@Override
	public boolean isTlogging() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.isTlogging();
	}

	@Override
	public void setTlogging(boolean tlogging) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setTlogging(tlogging);
	}

	@Override
	public UcumService getUcumService() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getUcumService();
	}

	@Override
	public void setUcumService(UcumService ucumService) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setUcumService(ucumService);
	}

	@Override
	public List<StructureDefinition> getStructures() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getStructures();
	}

	@Override
	public String getLinkForUrl(String corePath, String url) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getLinkForUrl(corePath, url);
	}

	@Override
	public List<ImplementationGuide> allImplementationGuides() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.allImplementationGuides();
	}

	@Override
	public Map<String, byte[]> getBinaries() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getBinaries();
	}

	@Override
	public void finishLoading() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.finishLoading();
	}

	@Override
	protected String tail(String url) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.tail(url);
	}

	@Override
	public int getClientRetryCount() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getClientRetryCount();
	}

	@Override
	public IWorkerContext setClientRetryCount(int value) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.setClientRetryCount(value);
	}

	@Override
	public String getTxCache() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getTxCache();
	}

	@Override
	public TerminologyClient getTxClient() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getTxClient();
	}

	@Override
	public String getCacheId() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getCacheId();
	}

	@Override
	public void setCacheId(String cacheId) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setCacheId(cacheId);
	}

	@Override
	public TerminologyCapabilities getTxCaps() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getTxCaps();
	}

	@Override
	public void setTxCaps(TerminologyCapabilities txCaps) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setTxCaps(txCaps);
	}

	@Override
	public TimeTracker clock() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.clock();
	}

	@Override
	public int countAllCaches() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.countAllCaches();
	}

	@Override
	public Set<String> getCodeSystemsUsed() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getCodeSystemsUsed();
	}

	@Override
	public String getSpecUrl() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getSpecUrl();
	}

	@Override
	public ICanonicalResourceLocator getLocator() {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return super.getLocator();
	}

	@Override
	public void setLocator(ICanonicalResourceLocator locator) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		super.setLocator(locator);
	}

*/
	
}
