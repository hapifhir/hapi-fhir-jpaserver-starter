package org.hl7.fhir.r5.terminologies.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.formats.JsonParser;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r5.model.ValueSet.ConceptSetFilterComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r5.terminologies.expansion.ValueSetExpansionOutcome;
import org.hl7.fhir.r5.utils.UserDataNames;
import org.hl7.fhir.utilities.*;
import org.hl7.fhir.utilities.filesystem.ManagedFileAccess;
import org.hl7.fhir.utilities.json.model.JsonNull;
import org.hl7.fhir.utilities.json.model.JsonProperty;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationOptions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@MarkedToMoveToAdjunctPackage
@Slf4j
public class TerminologyCache {


	public static class SourcedCodeSystem {
		private String server;
		private CodeSystem cs;

		public SourcedCodeSystem(String server, CodeSystem cs) {
			super();
			this.server = server;
			this.cs = cs;
		}
		public String getServer() {
			return server;
		}
		public CodeSystem getCs() {
			return cs;
		}
	}


	public static class SourcedCodeSystemEntry {
		private String server;
		private String filename;

		public SourcedCodeSystemEntry(String server, String filename) {
			super();
			this.server = server;
			this.filename = filename;
		}
		public String getServer() {
			return server;
		}
		public String getFilename() {
			return filename;
		}
	}


	public static class SourcedValueSet {
		private String server;
		private ValueSet vs;

		public SourcedValueSet(String server, ValueSet vs) {
			super();
			this.server = server;
			this.vs = vs;
		}
		public String getServer() {
			return server;
		}
		public ValueSet getVs() {
			return vs;
		}
	}

	public static class SourcedValueSetEntry {
		private String server;
		private String filename;

		public SourcedValueSetEntry(String server, String filename) {
			super();
			this.server = server;
			this.filename = filename;
		}
		public String getServer() {
			return server;
		}
		public String getFilename() {
			return filename;
		}
	}

	public static final boolean TRANSIENT = false;
	public static final boolean PERMANENT = true;
	private static final String NAME_FOR_NO_SYSTEM = "all-systems";
	private static final String ENTRY_MARKER = "-------------------------------------------------------------------------------------";
	private static final String BREAK = "####";
	private static final String CACHE_FILE_EXTENSION = ".cache";
	private static final String CAPABILITY_STATEMENT_TITLE = ".capabilityStatement";
	private static final String TERMINOLOGY_CAPABILITIES_TITLE = ".terminologyCapabilities";
	private static final String FIXED_CACHE_VERSION = "4"; // last change: change the way tx.fhir.org handles expansions


	private SystemNameKeyGenerator systemNameKeyGenerator = new SystemNameKeyGenerator();

	public class CacheToken {
		@Getter
		private String name;
		private String key;
		@Getter
		private String request;
		@Accessors(fluent = true)
		@Getter
		private boolean hasVersion;

		public void setName(String n) {
			String systemName = getSystemNameKeyGenerator().getNameForSystem(n);
			if (name == null)
				name = systemName;
			else if (!systemName.equals(name))
				name = NAME_FOR_NO_SYSTEM;
		}
	}

	public static class SubsumesResult {

		private Boolean result;

		protected SubsumesResult(Boolean result) {
			super();
			this.result = result;
		}

		public Boolean getResult() {
			return result;
		}

	}

	protected SystemNameKeyGenerator getSystemNameKeyGenerator() {
		return systemNameKeyGenerator;
	}
	public class SystemNameKeyGenerator {
		public static final String SNOMED_SCT_CODESYSTEM_URL = "http://snomed.info/sct";
		public static final String RXNORM_CODESYSTEM_URL = "http://www.nlm.nih.gov/research/umls/rxnorm";
		public static final String LOINC_CODESYSTEM_URL = "http://loinc.org";
		public static final String UCUM_CODESYSTEM_URL = "http://unitsofmeasure.org";

		public static final String HL7_TERMINOLOGY_CODESYSTEM_BASE_URL = "http://terminology.hl7.org/CodeSystem/";
		public static final String HL7_SID_CODESYSTEM_BASE_URL = "http://hl7.org/fhir/sid/";
		public static final String HL7_FHIR_CODESYSTEM_BASE_URL = "http://hl7.org/fhir/";

		public static final String ISO_CODESYSTEM_URN = "urn:iso:std:iso:";
		public static final String LANG_CODESYSTEM_URN = "urn:ietf:bcp:47";
		public static final String MIMETYPES_CODESYSTEM_URN = "urn:ietf:bcp:13";

		public static final String _11073_CODESYSTEM_URN = "urn:iso:std:iso:11073:10101";
		public static final String DICOM_CODESYSTEM_URL = "http://dicom.nema.org/resources/ontology/DCM";

		public String getNameForSystem(String system) {
			final int lastPipe = system.lastIndexOf('|');
			final String systemBaseName = lastPipe == -1 ? system : system.substring(0,lastPipe);
			String systemVersion = lastPipe == -1 ? null : system.substring(lastPipe + 1);

			if (systemVersion != null) {
				if (systemVersion.startsWith("http://snomed.info/sct/")) {
					systemVersion = systemVersion.substring(23);
				}
				systemVersion = systemVersion.replace(":", "").replace("/", "").replace("\\", "").replace("?", "").replace("$", "").replace("*", "").replace("#", "").replace("%", "");
			}
			if (systemBaseName.equals(SNOMED_SCT_CODESYSTEM_URL))
				return getVersionedSystem("snomed", systemVersion);
			if (systemBaseName.equals(RXNORM_CODESYSTEM_URL))
				return getVersionedSystem("rxnorm", systemVersion);
			if (systemBaseName.equals(LOINC_CODESYSTEM_URL))
				return getVersionedSystem("loinc", systemVersion);
			if (systemBaseName.equals(UCUM_CODESYSTEM_URL))
				return getVersionedSystem("ucum", systemVersion);
			if (systemBaseName.startsWith(HL7_SID_CODESYSTEM_BASE_URL))
				return getVersionedSystem(normalizeBaseURL(HL7_SID_CODESYSTEM_BASE_URL, systemBaseName), systemVersion);
			if (systemBaseName.equals(_11073_CODESYSTEM_URN))
				return getVersionedSystem("11073", systemVersion);
			if (systemBaseName.startsWith(ISO_CODESYSTEM_URN))
				return getVersionedSystem("iso"+systemBaseName.substring(ISO_CODESYSTEM_URN.length()).replace(":", ""), systemVersion);
			if (systemBaseName.startsWith(HL7_TERMINOLOGY_CODESYSTEM_BASE_URL))
				return getVersionedSystem(normalizeBaseURL(HL7_TERMINOLOGY_CODESYSTEM_BASE_URL, systemBaseName), systemVersion);
			if (systemBaseName.startsWith(HL7_FHIR_CODESYSTEM_BASE_URL))
				return getVersionedSystem(normalizeBaseURL(HL7_FHIR_CODESYSTEM_BASE_URL, systemBaseName), systemVersion);
			if (systemBaseName.equals(LANG_CODESYSTEM_URN))
				return getVersionedSystem("lang", systemVersion);
			if (systemBaseName.equals(MIMETYPES_CODESYSTEM_URN))
				return getVersionedSystem("mimetypes", systemVersion);
			if (systemBaseName.equals(DICOM_CODESYSTEM_URL))
				return getVersionedSystem("dicom", systemVersion);
			return getVersionedSystem(systemBaseName.replace("/", "_").replace(":", "_").replace("?", "X").replace("#", "X"), systemVersion);
		}

		public String normalizeBaseURL(String baseUrl, String fullUrl) {
			return fullUrl.substring(baseUrl.length()).replace("/", "");
		}

		public String getVersionedSystem(String baseSystem, String version) {
			if (version != null) {
				return baseSystem + "_" + version;
			}
			return baseSystem;
		}
	}


	private class CacheEntry {
		private String request;
		private boolean persistent;
		private ValidationResult v;
		private ValueSetExpansionOutcome e;
		private SubsumesResult s;
	}

	private class NamedCache {
		private String name;
		private List<CacheEntry> list = new ArrayList<CacheEntry>(); // persistent entries
		private Map<String, CacheEntry> map = new HashMap<String, CacheEntry>();
	}


	private Object lock;
	private String folder;
	@Getter private int requestCount;
	@Getter private int hitCount;
	@Getter private int networkCount;

	private final static long CAPABILITY_CACHE_EXPIRATION_HOURS = 24;
	private final static long CAPABILITY_CACHE_EXPIRATION_MILLISECONDS = CAPABILITY_CACHE_EXPIRATION_HOURS * 60 * 60 * 1000;
	private final long capabilityCacheExpirationMilliseconds;
	private final TerminologyCapabilitiesCache<CapabilityStatement> capabilityStatementCache;
	private final TerminologyCapabilitiesCache<TerminologyCapabilities> terminologyCapabilitiesCache;
	private Map<String, NamedCache> caches = new HashMap<String, NamedCache>();
	private Map<String, SourcedValueSetEntry> vsCache = new HashMap<>();
	private Map<String, SourcedCodeSystemEntry> csCache = new HashMap<>();
	private Map<String, String> serverMap = new HashMap<>();

	@Getter @Setter private static boolean noCaching;
	@Getter @Setter private static boolean cacheErrors;

	protected TerminologyCache(Object lock, String folder, Long capabilityCacheExpirationMilliseconds) throws FileNotFoundException, IOException, FHIRException {
		super();
		log.info("QLDBG <init> folder = {}", folder);
		this.lock = lock;
		this.capabilityCacheExpirationMilliseconds = capabilityCacheExpirationMilliseconds;
		capabilityStatementCache = new CommonsTerminologyCapabilitiesCache<>(capabilityCacheExpirationMilliseconds, TimeUnit.MILLISECONDS);
		terminologyCapabilitiesCache = new CommonsTerminologyCapabilitiesCache<>(capabilityCacheExpirationMilliseconds, TimeUnit.MILLISECONDS);
		if (folder == null) {
			folder = Utilities.path("[tmp]", "default-tx-cache");
		} else if ("n/a".equals(folder)) {
			// this is a weird way to do things but it maintains the legacy interface
			folder = null;
		}
		this.folder = folder;
		requestCount = 0;
		hitCount = 0;
		networkCount = 0;

		if (folder != null) {
			File f = ManagedFileAccess.file(folder);
			if (!f.exists()) {
				FileUtilities.createDirectory(folder);
			}
			if (!f.exists()) {
				throw new IOException("Unable to create terminology cache at "+folder);
			}
			checkVersion();
			load();
		}
	}

	// use lock from the context
	public TerminologyCache(Object lock, String folder) throws IOException, FHIRException {
		this(lock, folder, CAPABILITY_CACHE_EXPIRATION_MILLISECONDS);
	}

	private void checkVersion() throws IOException {
		File verFile = ManagedFileAccess.file(Utilities.path(folder, "version.ctl"));
		if (verFile.exists()) {
			String ver = FileUtilities.fileToString(verFile);
			if (!ver.equals(FIXED_CACHE_VERSION)) {
				log.info("Terminology Cache Version has changed from 1 to "+FIXED_CACHE_VERSION+", so clearing txCache");
				clear();
			}
			FileUtilities.stringToFile(FIXED_CACHE_VERSION, verFile);
		} else {
			FileUtilities.stringToFile(FIXED_CACHE_VERSION, verFile);
		}
	}

	public String getServerId(String address) throws IOException  {
		if (serverMap.containsKey(address)) {
			return serverMap.get(address);
		}
		String id = address.replace("http://", "").replace("https://", "").replace("/", ".");
		int i = 1;
		while (serverMap.containsValue(id)) {
			i++;
			id =  address.replace("https:", "").replace("https:", "").replace("/", ".")+i;
		}
		serverMap.put(address, id);
		if (folder != null) {
			IniFile ini = new IniFile(Utilities.path(folder, "servers.ini"));
			ini.setStringProperty("servers", id, address, null);
			ini.save();
		}
		return id;
	}

	public void unload() {
		// not useable after this is called
		caches.clear();
		vsCache.clear();
		csCache.clear();
	}

	public void clear() throws IOException {
		if (folder != null) {
			FileUtilities.clearDirectory(folder);
		}
		caches.clear();
		vsCache.clear();
		csCache.clear();
	}

	public boolean hasCapabilityStatement(String address) {
		log.info("QLDBG hasCapabilityStatement "+address+" : "+capabilityStatementCache.containsKey(address));
		return capabilityStatementCache.containsKey(address);
	}

	public CapabilityStatement getCapabilityStatement(String address) {
		return capabilityStatementCache.get(address);
	}

	public void cacheCapabilityStatement(String address, CapabilityStatement capabilityStatement) throws IOException {
		log.info("QLDBG cacheCapabilityStatement "+address);
		if (noCaching) {
			return;
		}
		this.capabilityStatementCache.put(address, capabilityStatement);
		save(capabilityStatement, CAPABILITY_STATEMENT_TITLE+"."+getServerId(address));
	}


	public boolean hasTerminologyCapabilities(String address) {
		log.info("QLDBG hasTerminologyCapabilities "+address+" : "+terminologyCapabilitiesCache.containsKey(address));
		return terminologyCapabilitiesCache.containsKey(address);
	}

	public TerminologyCapabilities getTerminologyCapabilities(String address) {
		return terminologyCapabilitiesCache.get(address);
	}

	public void cacheTerminologyCapabilities(String address, TerminologyCapabilities terminologyCapabilities) throws IOException {
		log.info("QLDBG cacheTerminologyCapabilities "+address);
		if (noCaching) {
			return;
		}
		this.terminologyCapabilitiesCache.put(address, terminologyCapabilities);
		save(terminologyCapabilities, TERMINOLOGY_CAPABILITIES_TITLE+"."+getServerId(address));
	}


	public CacheToken generateValidationToken(ValidationOptions options, Coding code, ValueSet vs, Parameters expParameters) {
		log.info("QLDBG generateValidationToken code {} in vs {}", code.getCode(), vs == null ? "null" : vs.getUrl());
		try {
			CacheToken ct = new CacheToken();
			if (code.hasSystem()) {
				ct.setName(code.getSystem());
				ct.hasVersion = code.hasVersion();
			}
			else
				ct.name = NAME_FOR_NO_SYSTEM;
			nameCacheToken(vs, ct);
			JsonParser json = new JsonParser();
			json.setOutputStyle(OutputStyle.PRETTY);
			// PATCH MATCHBOX: need to copy expParameters to avoid multithreading issues
			String expJS = expParameters == null ? "" : json.composeString(expParameters.copy());
			// END PATCH MATCHBOX

			if (vs != null && vs.hasUrl() && vs.hasVersion()) {
				ct.request = "{\"code\" : "+json.composeString(code, "codeableConcept")+", \"url\": \""+Utilities.escapeJson(vs.getUrl())
					+"\", \"version\": \""+Utilities.escapeJson(vs.getVersion())+"\""+(options == null ? "" : ", "+options.toJson())+", \"profile\": "+expJS+"}\r\n";
			} else if (options.getVsAsUrl()) {
				ct.request = "{\"code\" : "+json.composeString(code, "code")+", \"valueSet\" :"+extracted(json, vs)+(options == null ? "" : ", "+options.toJson())+", \"profile\": "+expJS+"}";
			} else {
				ValueSet vsc = getVSEssense(vs);
				ct.request = "{\"code\" : "+json.composeString(code, "code")+", \"valueSet\" :"+(vsc == null ? "null" : extracted(json, vsc))+(options == null ? "" : ", "+options.toJson())+", \"profile\": "+expJS+"}";
			}
			ct.key = String.valueOf(hashJson(ct.request));
			return ct;
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public CacheToken generateValidationToken(ValidationOptions options, Coding code, String vsUrl, Parameters expParameters) {
		log.info("QLDBG generateValidationToken code {} in vs {}", code.getCode(), vsUrl == null ? "null" : vsUrl);
		try {
			CacheToken ct = new CacheToken();
			if (code.hasSystem()) {
				ct.setName(code.getSystem());
				ct.hasVersion = code.hasVersion();
			} else {
				ct.name = NAME_FOR_NO_SYSTEM;
			}
			ct.setName(vsUrl);
			JsonParser json = new JsonParser();
			json.setOutputStyle(OutputStyle.PRETTY);
			String expJS = json.composeString(expParameters);

			ct.request = "{\"code\" : "+json.composeString(code, "code")+", \"valueSet\" :"+(vsUrl == null ? "null" : vsUrl)+(options == null ? "" : ", "+options.toJson())+", \"profile\": "+expJS+"}";
			ct.key = String.valueOf(hashJson(ct.request));
			return ct;
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public String extracted(JsonParser json, ValueSet vsc) throws IOException {
		String s = null;
		if (vsc.getExpansion().getContains().size() > 1000 || vsc.getCompose().getIncludeFirstRep().getConcept().size() > 1000) {
			s =  vsc.getUrl();
		} else {
			s = json.composeString(vsc);
		}
		return s;
	}

	public CacheToken generateValidationToken(ValidationOptions options, CodeableConcept code, ValueSet vs, Parameters expParameters) {
		log.info("QLDBG generateValidationToken code {} in vs {}", code.hasText() ? code.getText() : "(no text)", vs == null ? "null" : vs.getUrl());
		try {
			CacheToken ct = new CacheToken();
			for (Coding c : code.getCoding()) {
				if (c.hasSystem()) {
					ct.setName(c.getSystem());
					ct.hasVersion = c.hasVersion();
				}
			}
			nameCacheToken(vs, ct);
			JsonParser json = new JsonParser();
			json.setOutputStyle(OutputStyle.PRETTY);
			String expJS = json.composeString(expParameters);
			if (vs != null && vs.hasUrl() && vs.hasVersion()) {
				ct.request = "{\"code\" : "+json.composeString(code, "codeableConcept")+", \"url\": \""+Utilities.escapeJson(vs.getUrl())+
					"\", \"version\": \""+Utilities.escapeJson(vs.getVersion())+"\""+(options == null ? "" : ", "+options.toJson())+", \"profile\": "+expJS+"}\r\n";
			} else if (vs == null) {
				ct.request = "{\"code\" : "+json.composeString(code, "codeableConcept")+(options == null ? "" : ", "+options.toJson())+", \"profile\": "+expJS+"}";
			} else {
				ValueSet vsc = getVSEssense(vs);
				ct.request = "{\"code\" : "+json.composeString(code, "codeableConcept")+", \"valueSet\" :"+extracted(json, vsc)+(options == null ? "" : ", "+options.toJson())+", \"profile\": "+expJS+"}";
			}
			ct.key = String.valueOf(hashJson(ct.request));
			return ct;
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public ValueSet getVSEssense(ValueSet vs) {
		if (vs == null)
			return null;
		ValueSet vsc = new ValueSet();
		vsc.setCompose(vs.getCompose());
		if (vs.hasExpansion()) {
			vsc.getExpansion().getParameter().addAll(vs.getExpansion().getParameter());
			vsc.getExpansion().getContains().addAll(vs.getExpansion().getContains());
		}
		return vsc;
	}

	public CacheToken generateExpandToken(ValueSet vs, boolean hierarchical) {
		log.info("QLDBG generateExpandToken vs {}", vs == null ? "null" : vs.getUrl());
		CacheToken ct = new CacheToken();
		nameCacheToken(vs, ct);
		if (vs.hasUrl() && vs.hasVersion()) {
			ct.request = "{\"hierarchical\" : "+(hierarchical ? "true" : "false")+", \"url\": \""+Utilities.escapeJson(vs.getUrl())+"\", \"version\": \""+Utilities.escapeJson(vs.getVersion())+"\"}\r\n";
		} else {
			ValueSet vsc = getVSEssense(vs);
			JsonParser json = new JsonParser();
			json.setOutputStyle(OutputStyle.PRETTY);
			try {
				ct.request = "{\"hierarchical\" : "+(hierarchical ? "true" : "false")+", \"valueSet\" :"+extracted(json, vsc)+"}\r\n";
			} catch (IOException e) {
				throw new Error(e);
			}
		}
		ct.key = String.valueOf(hashJson(ct.request));
		return ct;
	}

	public CacheToken generateExpandToken(String url, boolean hierarchical) {
		log.info("QLDBG generateExpandToken url {}", url == null ? "null" : url);
		CacheToken ct = new CacheToken();
		ct.request = "{\"hierarchical\" : "+(hierarchical ? "true" : "false")+", \"url\": \""+Utilities.escapeJson(url)+"\"}\r\n";
		ct.key = String.valueOf(hashJson(ct.request));
		return ct;
	}

	public void nameCacheToken(ValueSet vs, CacheToken ct) {
		log.info("QLDBG nameCacheToken vs {}", vs == null ? "null" : vs.getUrl());
		if (vs != null) {
			for (ConceptSetComponent inc : vs.getCompose().getInclude()) {
				if (inc.hasSystem()) {
					ct.setName(inc.getSystem());
					ct.hasVersion = inc.hasVersion();
				}
			}
			for (ConceptSetComponent inc : vs.getCompose().getExclude()) {
				if (inc.hasSystem()) {
					ct.setName(inc.getSystem());
					ct.hasVersion = inc.hasVersion();
				}
			}
			for (ValueSetExpansionContainsComponent inc : vs.getExpansion().getContains()) {
				if (inc.hasSystem()) {
					ct.setName(inc.getSystem());
					ct.hasVersion = inc.hasVersion();
				}
			}
		}
	}

	private String normalizeSystemPath(String path) {
		return path.replace("/", "").replace('|','X');
	}



	public NamedCache getNamedCache(CacheToken cacheToken) {

		final String cacheName = cacheToken.name == null ? "null" : cacheToken.name;

		NamedCache nc = caches.get(cacheName);

		if (nc == null) {
			nc = new NamedCache();
			nc.name = cacheName;
			caches.put(nc.name, nc);
		}
		return nc;
	}

	public ValueSetExpansionOutcome getExpansion(CacheToken cacheToken) {
		synchronized (lock) {
			NamedCache nc = getNamedCache(cacheToken);
			CacheEntry e = nc.map.get(cacheToken.key);
			if (e == null)
				return null;
			else
				return e.e;
		}
	}

	public void cacheExpansion(CacheToken cacheToken, ValueSetExpansionOutcome res, boolean persistent) {
		synchronized (lock) {
			NamedCache nc = getNamedCache(cacheToken);
			CacheEntry e = new CacheEntry();
			e.request = cacheToken.request;
			e.persistent = persistent;
			e.e = res;
			store(cacheToken, persistent, nc, e);
		}
	}

	public void store(CacheToken cacheToken, boolean persistent, NamedCache nc, CacheEntry e) {
		log.info("QLDBG store NamedCache.name={}, CacheToken.name={}, CacheToken.key={}", nc.name, cacheToken.name, cacheToken.key);
		if (noCaching) {
			return;
		}

		if ( !cacheErrors &&
			( e.v!= null
				&& e.v.getErrorClass() == TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED
				&& !cacheToken.hasVersion)) {
			return;
		}

		boolean n = nc.map.containsKey(cacheToken.key);
		nc.map.put(cacheToken.key, e);
		if (persistent) {
			if (n) {
				for (int i = nc.list.size()- 1; i>= 0; i--) {
					if (nc.list.get(i).request.equals(e.request)) {
						nc.list.remove(i);
					}
				}
			}
			nc.list.add(e);
			save(nc);
		}
	}

	public ValidationResult getValidation(CacheToken cacheToken) {
		log.info("QLDBG getValidation CacheToken.name={}, CacheToken.key={}", cacheToken.name, cacheToken.key);
		if (cacheToken.key == null) {
			return null;
		}
		synchronized (lock) {
			requestCount++;
			NamedCache nc = getNamedCache(cacheToken);
			CacheEntry e = nc.map.get(cacheToken.key);
			if (e == null) {
				networkCount++;
				return null;
			} else {
				hitCount++;
				return new ValidationResult(e.v);
			}
		}
	}

	public void cacheValidation(CacheToken cacheToken, ValidationResult res, boolean persistent) {
		log.info("QLDBG cacheValidation CacheToken.name={}, CacheToken.key={}", cacheToken.name, cacheToken.key);
		if (cacheToken.key != null) {
			synchronized (lock) {
				NamedCache nc = getNamedCache(cacheToken);
				CacheEntry e = new CacheEntry();
				e.request = cacheToken.request;
				e.persistent = persistent;
				e.v = new ValidationResult(res);
				store(cacheToken, persistent, nc, e);
			}
		}
	}


	// persistence

	public void save() {

	}

	private <K extends Resource> void save(K resource, String title) {
		if (folder == null)
			return;

		try {
			OutputStreamWriter sw = new OutputStreamWriter(ManagedFileAccess.outStream(Utilities.path(folder, title + CACHE_FILE_EXTENSION)), "UTF-8");

			JsonParser json = new JsonParser();
			json.setOutputStyle(OutputStyle.PRETTY);

			sw.write(json.composeString(resource).trim());
			sw.close();
		} catch (Exception e) {
			log.error("error saving capability statement "+e.getMessage(), e);
		}
	}

	private void save(NamedCache nc) {
		log.info("QLDBG cacheValidation NamedCache.name={}", nc.name);
		if (folder == null)
			return;

		try {
			OutputStreamWriter sw = new OutputStreamWriter(ManagedFileAccess.outStream(Utilities.path(folder, nc.name+CACHE_FILE_EXTENSION)), "UTF-8");
			sw.write(ENTRY_MARKER+"\r\n");
			JsonParser json = new JsonParser();
			json.setOutputStyle(OutputStyle.PRETTY);
			for (CacheEntry ce : nc.list) {
				sw.write(ce.request.trim());
				sw.write(BREAK+"\r\n");
				if (ce.e != null) {
					sw.write("e: {\r\n");
					if (ce.e.isFromServer())
						sw.write("  \"from-server\" : true,\r\n");
					if (ce.e.getValueset() != null) {
						if (ce.e.getValueset().hasUserData(UserDataNames.VS_EXPANSION_SOURCE)) {
							sw.write("  \"source\" : "+Utilities.escapeJson(ce.e.getValueset().getUserString(UserDataNames.VS_EXPANSION_SOURCE)).trim()+",\r\n");
						}
						sw.write("  \"valueSet\" : "+json.composeString(ce.e.getValueset()).trim()+",\r\n");
					}
					sw.write("  \"error\" : \""+Utilities.escapeJson(ce.e.getError()).trim()+"\"\r\n}\r\n");
				} else if (ce.s != null) {
					sw.write("s: {\r\n");
					sw.write("  \"result\" : "+ce.s.result+"\r\n}\r\n");
				} else {
					sw.write("v: {\r\n");
					boolean first = true;
					if (ce.v.getDisplay() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"display\" : \""+Utilities.escapeJson(ce.v.getDisplay()).trim()+"\"");
					}
					if (ce.v.getCode() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"code\" : \""+Utilities.escapeJson(ce.v.getCode()).trim()+"\"");
					}
					if (ce.v.getSystem() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"system\" : \""+Utilities.escapeJson(ce.v.getSystem()).trim()+"\"");
					}
					if (ce.v.getVersion() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"version\" : \""+Utilities.escapeJson(ce.v.getVersion()).trim()+"\"");
					}
					if (ce.v.getSeverity() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"severity\" : "+"\""+ce.v.getSeverity().toCode().trim()+"\""+"");
					}
					if (ce.v.getMessage() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"error\" : \""+Utilities.escapeJson(ce.v.getMessage()).trim()+"\"");
					}
					if (ce.v.getErrorClass() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"class\" : \""+Utilities.escapeJson(ce.v.getErrorClass().toString())+"\"");
					}
					if (ce.v.getDefinition() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"definition\" : \""+Utilities.escapeJson(ce.v.getDefinition()).trim()+"\"");
					}
					if (ce.v.getStatus() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"status\" : \""+Utilities.escapeJson(ce.v.getStatus()).trim()+"\"");
					}
					if (ce.v.getServer() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"server\" : \""+Utilities.escapeJson(ce.v.getServer()).trim()+"\"");
					}
					if (ce.v.isInactive()) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"inactive\" : true");
					}
					if (ce.v.getUnknownSystems() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"unknown-systems\" : \""+Utilities.escapeJson(CommaSeparatedStringBuilder.join(",", ce.v.getUnknownSystems())).trim()+"\"");
					}
					if (ce.v.getParameters() != null) {
						if (first) first = false; else sw.write(",\r\n");
						sw.write("  \"parameters\" : "+json.composeString(ce.v.getParameters()).trim()+"\r\n");
					}
					if (ce.v.getIssues() != null) {
						if (first) first = false; else sw.write(",\r\n");
						OperationOutcome oo = new OperationOutcome();
						oo.setIssue(ce.v.getIssues());
						sw.write("  \"issues\" : "+json.composeString(oo).trim()+"\r\n");
					}
					sw.write("\r\n}\r\n");
				}
				sw.write(ENTRY_MARKER+"\r\n");
			}
			sw.close();
		} catch (Exception e) {
			log.error("error saving "+nc.name+": "+e.getMessage(), e);
		}
	}

	private boolean isCapabilityCache(String fn) {
		log.info("QLDBG isCapabilityCache {}", fn);
		if (fn == null) {
			return false;
		}
		return fn.startsWith(CAPABILITY_STATEMENT_TITLE) || fn.startsWith(TERMINOLOGY_CAPABILITIES_TITLE);
	}

	private void loadCapabilityCache(String fn) throws IOException {
		log.info("QLDBG loadCapabilityCache {}", fn);
		if (TerminologyCapabilitiesCache.cacheFileHasExpired(Utilities.path(folder, fn), capabilityCacheExpirationMilliseconds)) {
			return;
		}
		try {
			String src = FileUtilities.fileToString(Utilities.path(folder, fn));
			String serverId = Utilities.getFileNameForName(fn).replace(CACHE_FILE_EXTENSION, "");
			serverId = serverId.substring(serverId.indexOf(".")+1);
			serverId = serverId.substring(serverId.indexOf(".")+1);
			String address = getServerForId(serverId);
			if (address != null) {
				JsonObject o = (JsonObject) new com.google.gson.JsonParser().parse(src);
				Resource resource = new JsonParser().parse(o);

				if (fn.startsWith(CAPABILITY_STATEMENT_TITLE)) {
					this.capabilityStatementCache.put(address, (CapabilityStatement) resource);
				} else if (fn.startsWith(TERMINOLOGY_CAPABILITIES_TITLE)) {
					this.terminologyCapabilitiesCache.put(address, (TerminologyCapabilities) resource);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FHIRException("Error loading " + fn + ": " + e.getMessage(), e);
		}
	}

	private String getServerForId(String serverId) {
		for (String n : serverMap.keySet()) {
			if (serverMap.get(n).equals(serverId)) {
				return n;
			}
		}
		return null;
	}

	private CacheEntry getCacheEntry(String request, String resultString) throws IOException {
		CacheEntry ce = new CacheEntry();
		ce.persistent = true;
		ce.request = request;
		char e = resultString.charAt(0);
		resultString = resultString.substring(3);
		JsonObject o = (JsonObject) new com.google.gson.JsonParser().parse(resultString);
		String error = loadJS(o.get("error"));
		if (e == 'e') {
			if (o.has("valueSet")) {
				ce.e = new ValueSetExpansionOutcome((ValueSet) new JsonParser().parse(o.getAsJsonObject("valueSet")), error, TerminologyServiceErrorClass.UNKNOWN, o.has("from-server"));
				if (o.has("source")) {
					ce.e.getValueset().setUserData(UserDataNames.VS_EXPANSION_SOURCE, o.get("source").getAsString());
				}
			} else {
				ce.e = new ValueSetExpansionOutcome(error, TerminologyServiceErrorClass.UNKNOWN, o.has("from-server"));
			}
		} else if (e == 's') {
			ce.s = new SubsumesResult(o.get("result").getAsBoolean());
		} else {
			String t = loadJS(o.get("severity"));
			IssueSeverity severity = t == null ? null :  IssueSeverity.fromCode(t);
			String display = loadJS(o.get("display"));
			String code = loadJS(o.get("code"));
			String system = loadJS(o.get("system"));
			String version = loadJS(o.get("version"));
			String definition = loadJS(o.get("definition"));
			String server = loadJS(o.get("server"));
			String status = loadJS(o.get("status"));
			boolean inactive = "true".equals(loadJS(o.get("inactive")));
			String unknownSystems = loadJS(o.get("unknown-systems"));
			OperationOutcome oo = o.has("issues") ? (OperationOutcome) new JsonParser().parse(o.getAsJsonObject("issues")) : null;
			Parameters p = o.has("parameters") ? (Parameters) new JsonParser().parse(o.getAsJsonObject("parameters")) : null;
			t = loadJS(o.get("class"));
			TerminologyServiceErrorClass errorClass = t == null ? null : TerminologyServiceErrorClass.valueOf(t) ;
			ce.v = new ValidationResult(severity, error, system, version, new ConceptDefinitionComponent().setDisplay(display).setDefinition(definition).setCode(code), display, null).setErrorClass(errorClass);
			ce.v.setUnknownSystems(CommaSeparatedStringBuilder.toSet(unknownSystems));
			ce.v.setServer(server);
			ce.v.setStatus(inactive, status);
			if (oo != null) {
				ce.v.setIssues(oo.getIssue());
			}
			if (p != null) {
				ce.v.setParameters(p);
			}
		}
		return ce;
	}

	private void loadNamedCache(String fn) throws IOException {
		log.info("QLDBG loadNamedCache {}", fn);
		int c = 0;
		try {
			String src = FileUtilities.fileToString(Utilities.path(folder, fn));
			String title = fn.substring(0, fn.lastIndexOf("."));

			NamedCache nc = new NamedCache();
			nc.name = title;

			if (src.startsWith("?"))
				src = src.substring(1);
			int i = src.indexOf(ENTRY_MARKER);
			while (i > -1) {
				c++;
				String s = src.substring(0, i);
				src = src.substring(i + ENTRY_MARKER.length() + 1);
				i = src.indexOf(ENTRY_MARKER);
				if (!Utilities.noString(s)) {
					int j = s.indexOf(BREAK);
					String request = s.substring(0, j);
					String p = s.substring(j + BREAK.length() + 1).trim();

					CacheEntry cacheEntry = getCacheEntry(request, p);

					nc.map.put(String.valueOf(hashJson(cacheEntry.request)), cacheEntry);
					nc.list.add(cacheEntry);
				}
				caches.put(nc.name, nc);
			}
		} catch (Exception e) {
			log.error("Error loading "+fn+": "+e.getMessage()+" entry "+c+" - ignoring it", e);
		}
	}

	private void load() throws FHIRException, IOException {
		IniFile ini = new IniFile(Utilities.path(folder, "servers.ini"));
		if (ini.hasSection("servers")) {
			for (String n : ini.getPropertyNames("servers")) {
				serverMap.put(ini.getStringProperty("servers", n), n);
			}
		}

		for (String fn : ManagedFileAccess.file(folder).list()) {
			if (fn.endsWith(CACHE_FILE_EXTENSION) && !fn.equals("validation" + CACHE_FILE_EXTENSION)) {
				try {
					if (isCapabilityCache(fn)) {
						loadCapabilityCache(fn);
					} else {
						loadNamedCache(fn);
					}
				} catch (FHIRException e) {
					throw e;
				}
			}
		}
		try {
			File f = ManagedFileAccess.file(Utilities.path(folder, "vs-externals.json"));
			if (f.exists()) {
				org.hl7.fhir.utilities.json.model.JsonObject json = org.hl7.fhir.utilities.json.parser.JsonParser.parseObject(f);
				for (JsonProperty p : json.getProperties()) {
					if (p.getValue().isJsonNull()) {
						vsCache.put(p.getName(), null);
					} else {
						org.hl7.fhir.utilities.json.model.JsonObject j = p.getValue().asJsonObject();
						vsCache.put(p.getName(), new SourcedValueSetEntry(j.asString("server"), j.asString("filename")));
					}
				}
			}
		} catch (Exception e) {
			log.error("Error loading vs external cache: "+e.getMessage(), e);
		}
		try {
			File f = ManagedFileAccess.file(Utilities.path(folder, "cs-externals.json"));
			if (f.exists()) {
				org.hl7.fhir.utilities.json.model.JsonObject json = org.hl7.fhir.utilities.json.parser.JsonParser.parseObject(f);
				for (JsonProperty p : json.getProperties()) {
					if (p.getValue().isJsonNull()) {
						csCache.put(p.getName(), null);
					} else {
						org.hl7.fhir.utilities.json.model.JsonObject j = p.getValue().asJsonObject();
						csCache.put(p.getName(), new SourcedCodeSystemEntry(j.asString("server"), j.asString("filename")));
					}
				}
			}
		} catch (Exception e) {
			log.error("Error loading vs external cache: "+e.getMessage(), e);
		}
	}

	private String loadJS(JsonElement e) {
		if (e == null)
			return null;
		if (!(e instanceof JsonPrimitive))
			return null;
		String s = e.getAsString();
		if ("".equals(s))
			return null;
		return s;
	}

	public String hashJson(String s) {
		return String.valueOf(s
								  .trim()
								  .replaceAll("\\r\\n?", "\n")
								  .hashCode());
	}

	// management

	public String summary(ValueSet vs) {
		if (vs == null)
			return "null";

		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
		for (ConceptSetComponent cc : vs.getCompose().getInclude())
			b.append("Include "+getIncSummary(cc));
		for (ConceptSetComponent cc : vs.getCompose().getExclude())
			b.append("Exclude "+getIncSummary(cc));
		return b.toString();
	}

	private String getIncSummary(ConceptSetComponent cc) {
		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
		for (UriType vs : cc.getValueSet())
			b.append(vs.asStringValue());
		String vsd = b.length() > 0 ? " where the codes are in the value sets ("+b.toString()+")" : "";
		String system = cc.getSystem();
		if (cc.hasConcept())
			return Integer.toString(cc.getConcept().size())+" codes from "+system+vsd;
		if (cc.hasFilter()) {
			String s = "";
			for (ConceptSetFilterComponent f : cc.getFilter()) {
				if (!Utilities.noString(s))
					s = s + " & ";
				s = s + f.getProperty()+" "+(f.hasOp() ? f.getOp().toCode() : "?")+" "+f.getValue();
			}
			return "from "+system+" where "+s+vsd;
		}
		return "All codes from "+system+vsd;
	}

	public String summary(Coding code) {
		return code.getSystem()+"#"+code.getCode()+(code.hasDisplay() ? ": \""+code.getDisplay()+"\"" : "");
	}

	public String summary(CodeableConcept code) {
		StringBuilder b = new StringBuilder();
		b.append("{");
		boolean first = true;
		for (Coding c : code.getCoding()) {
			if (first) first = false; else b.append(",");
			b.append(summary(c));
		}
		b.append("}: \"");
		b.append(code.getText());
		b.append("\"");
		return b.toString();
	}

	public void removeCS(String url) {
		synchronized (lock) {
			String name = getSystemNameKeyGenerator().getNameForSystem(url);
			if (caches.containsKey(name)) {
				caches.remove(name);
			}
		}
	}

	public String getFolder() {
		return folder;
	}

	public Map<String, String> servers() {
		Map<String, String> servers = new HashMap<>();
//    servers.put("http://local.fhir.org/r2", "tx.fhir.org");
//    servers.put("http://local.fhir.org/r3", "tx.fhir.org");
//    servers.put("http://local.fhir.org/r4", "tx.fhir.org");
//    servers.put("http://local.fhir.org/r5", "tx.fhir.org");
//
//    servers.put("http://tx-dev.fhir.org/r2", "tx.fhir.org");
//    servers.put("http://tx-dev.fhir.org/r3", "tx.fhir.org");
//    servers.put("http://tx-dev.fhir.org/r4", "tx.fhir.org");
//    servers.put("http://tx-dev.fhir.org/r5", "tx.fhir.org");

		servers.put("http://tx.fhir.org/r2", "tx.fhir.org");
		servers.put("http://tx.fhir.org/r3", "tx.fhir.org");
		servers.put("http://tx.fhir.org/r4", "tx.fhir.org");
		servers.put("http://tx.fhir.org/r5", "tx.fhir.org");

		return servers;
	}

	public boolean hasValueSet(String canonical) {
		return vsCache.containsKey(canonical);
	}

	public boolean hasCodeSystem(String canonical) {
		return csCache.containsKey(canonical);
	}

	public SourcedValueSet getValueSet(String canonical) {
		log.info("QLDBG getValueSet {}", canonical);
		SourcedValueSetEntry sp = vsCache.get(canonical);
		if (sp == null || folder == null) {
			return null;
		} else {
			try {
				return new SourcedValueSet(sp.getServer(), sp.getFilename() == null ? null : (ValueSet) new JsonParser().parse(ManagedFileAccess.inStream(Utilities.path(folder, sp.getFilename()))));
			} catch (Exception e) {
				return null;
			}
		}
	}

	public SourcedCodeSystem getCodeSystem(String canonical) {
		log.info("QLDBG getCodeSystem {}", canonical);
		SourcedCodeSystemEntry sp = csCache.get(canonical);
		if (sp == null || folder == null) {
			return null;
		} else {
			try {
				return new SourcedCodeSystem(sp.getServer(), sp.getFilename() == null ? null : (CodeSystem) new JsonParser().parse(ManagedFileAccess.inStream(Utilities.path(folder, sp.getFilename()))));
			} catch (Exception e) {
				return null;
			}
		}
	}

	public void cacheValueSet(String canonical, SourcedValueSet svs) {
		log.info("QLDBG cacheValueSet {}", canonical);
		if (canonical == null) {
			return;
		}
		try {
			if (svs == null) {
				vsCache.put(canonical, null);
			} else {
				String uuid = UUIDUtilities.makeUuidLC();
				String fn = "vs-"+uuid+".json";
				if (folder != null) {
					new JsonParser().compose(ManagedFileAccess.outStream(Utilities.path(folder, fn)), svs.getVs());
				}
				vsCache.put(canonical, new SourcedValueSetEntry(svs.getServer(), fn));
			}
			org.hl7.fhir.utilities.json.model.JsonObject j = new org.hl7.fhir.utilities.json.model.JsonObject();
			for (String k : vsCache.keySet()) {
				SourcedValueSetEntry sve = vsCache.get(k);
				if (sve == null) {
					j.add(k, new JsonNull());
				} else {
					org.hl7.fhir.utilities.json.model.JsonObject e = new org.hl7.fhir.utilities.json.model.JsonObject();
					e.set("server", sve.getServer());
					if (sve.getFilename() != null) {
						e.set("filename", sve.getFilename());
					}
					j.add(k, e);
				}
			}
			if (folder != null) {
				org.hl7.fhir.utilities.json.parser.JsonParser.compose(j, ManagedFileAccess.file(Utilities.path(folder, "vs-externals.json")), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cacheCodeSystem(String canonical, SourcedCodeSystem scs) {
		log.info("QLDBG cacheCodeSystem {}", canonical);
		if (canonical == null) {
			return;
		}
		try {
			if (scs == null) {
				csCache.put(canonical, null);
			} else {
				String uuid = UUIDUtilities.makeUuidLC();
				String fn = "cs-"+uuid+".json";
				if (folder != null) {
					new JsonParser().compose(ManagedFileAccess.outStream(Utilities.path(folder, fn)), scs.getCs());
				}
				csCache.put(canonical, new SourcedCodeSystemEntry(scs.getServer(), fn));
			}
			org.hl7.fhir.utilities.json.model.JsonObject j = new org.hl7.fhir.utilities.json.model.JsonObject();
			for (String k : csCache.keySet()) {
				SourcedCodeSystemEntry sve = csCache.get(k);
				if (sve == null) {
					j.add(k, new JsonNull());
				} else {
					org.hl7.fhir.utilities.json.model.JsonObject e = new org.hl7.fhir.utilities.json.model.JsonObject();
					e.set("server", sve.getServer());
					if (sve.getFilename() != null) {
						e.set("filename", sve.getFilename());
					}
					j.add(k, e);
				}
			}
			if (folder != null) {
				org.hl7.fhir.utilities.json.parser.JsonParser.compose(j, ManagedFileAccess.file(Utilities.path(folder, "cs-externals.json")), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CacheToken generateSubsumesToken(ValidationOptions options, Coding parent, Coding child, Parameters expParameters) {
		try {
			CacheToken ct = new CacheToken();
			if (parent.hasSystem()) {
				ct.setName(parent.getSystem());
			}
			if (child.hasSystem()) {
				ct.setName(child.getSystem());
			}
			ct.hasVersion = parent.hasVersion() || child.hasVersion();
			JsonParser json = new JsonParser();
			json.setOutputStyle(OutputStyle.PRETTY);
			String expJS = json.composeString(expParameters);
			ct.request = "{\"op\": \"subsumes\", \"parent\" : "+json.composeString(parent, "code")+", \"child\" :"+json.composeString(child, "code")+(options == null ? "" : ", "+options.toJson())+", \"profile\": "+expJS+"}";
			ct.key = String.valueOf(hashJson(ct.request));
			return ct;
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public Boolean getSubsumes(CacheToken cacheToken) {
		if (cacheToken.key == null) {
			return null;
		}
		synchronized (lock) {
			requestCount++;
			NamedCache nc = getNamedCache(cacheToken);
			CacheEntry e = nc.map.get(cacheToken.key);
			if (e == null) {
				networkCount++;
				return null;
			} else {
				hitCount++;
				return e.s.result;
			}
		}

	}

	public void cacheSubsumes(CacheToken cacheToken, Boolean b, boolean persistent) {
		if (cacheToken.key != null) {
			synchronized (lock) {
				NamedCache nc = getNamedCache(cacheToken);
				CacheEntry e = new CacheEntry();
				e.request = cacheToken.request;
				e.persistent = persistent;
				e.s = new SubsumesResult(b);
				store(cacheToken, persistent, nc, e);
			}
		}
	}


	public String getReport() {
		int c = 0;
		for (NamedCache nc : caches.values()) {
			c += nc.list.size();
		}
		return "txCache report: "+
			c+" entries in "+caches.size()+" buckets + "+vsCache.size()+" VS, "+csCache.size()+" CS & "+serverMap.size()+" SM. Hitcount = "+hitCount+"/"+requestCount+", "+networkCount;
	}
}