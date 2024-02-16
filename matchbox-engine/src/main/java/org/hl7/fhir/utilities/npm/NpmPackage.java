//
// Source code recreated from a .class file by Quiltflower
//

package org.hl7.fhir.utilities.npm;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.ByteProvider;
import org.hl7.fhir.utilities.CommaSeparatedStringBuilder;
import org.hl7.fhir.utilities.SimpleHTTPClient;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.SimpleHTTPClient.HTTPResult;
import org.hl7.fhir.utilities.json.JsonException;
import org.hl7.fhir.utilities.json.model.JsonArray;
import org.hl7.fhir.utilities.json.model.JsonElement;
import org.hl7.fhir.utilities.json.model.JsonObject;
import org.hl7.fhir.utilities.json.model.JsonProperty;
import org.hl7.fhir.utilities.json.parser.JsonParser;
import org.hl7.fhir.utilities.npm.PackageGenerator.PackageType;

public class NpmPackage {
	private String path;
	private JsonObject npm;
	private Map<String, NpmPackage.NpmPackageFolder> folders = new HashMap();
	private boolean changedByLoader;
	private Map<String, Object> userData;
	private boolean minimalMemory;
	private int size;
	private boolean warned = false;
	private static final int BUFFER_SIZE = 1024;

	public static boolean isValidName(String pid) {
		return pid.matches("^[a-z][a-zA-Z0-9]*(\\.[a-z][a-zA-Z0-9\\-]*)+$");
	}

	public static boolean isValidVersion(String ver) {
		return ver.matches("^[0-9]+\\.[0-9]+\\.[0-9]+$");
	}

	private NpmPackage() {
	}

	public static NpmPackage fromFolder(String path) throws IOException {
		NpmPackage res = new NpmPackage();
		res.loadFiles(path, new File(path));
		res.checkIndexed(path);
		return res;
	}

	public static NpmPackage fromFolderMinimal(String path) throws IOException {
		NpmPackage res = new NpmPackage();
		res.minimalMemory = true;
		res.loadFiles(path, new File(path));
		res.checkIndexed(path);
		return res;
	}

	public static NpmPackage empty(PackageGenerator thePackageGenerator) {
		NpmPackage retVal = new NpmPackage();
		retVal.npm = thePackageGenerator.getRootJsonObject();
		return retVal;
	}

	public static NpmPackage empty() {
		return new NpmPackage();
	}

	public Map<String, Object> getUserData() {
		if (this.userData == null) {
			this.userData = new HashMap();
		}

		return this.userData;
	}

	public void loadFiles(String path, File source, String... exemptions) throws FileNotFoundException, IOException {
		this.npm = JsonParser.parseObject(TextFile.fileToString(Utilities.path(new String[]{path, "package", "package.json"})));
		this.path = path;
		File dir = new File(path);

		for(File f : dir.listFiles()) {
			if (!isInternalExemptFile(f) && !Utilities.existsInList(f.getName(), exemptions)) {
				if (f.isDirectory()) {
					String d = f.getName();
					if (!d.equals("package")) {
						d = Utilities.path(new String[]{"package", d});
					}

					File ij = new File(Utilities.path(new String[]{f.getAbsolutePath(), ".index.json"}));
					NpmPackage.NpmPackageFolder folder = new NpmPackage.NpmPackageFolder(d);
					folder.folder = f;
					this.folders.put(d, folder);
					if ((ij.exists() || !this.minimalMemory) && !this.minimalMemory) {
						try {
							if (!ij.exists() || !folder.readIndex(JsonParser.parseObject(ij), folder.getTypes())) {
								this.indexFolder(folder.getFolderName(), folder);
							}
						} catch (Exception var13) {
							throw new IOException("Error parsing " + ij.getAbsolutePath() + ": " + var13.getMessage(), var13);
						}
					}

					this.loadSubFolders(dir.getAbsolutePath(), f);
				} else {
					NpmPackage.NpmPackageFolder folder = new NpmPackage.NpmPackageFolder(Utilities.path(new String[]{"package", "$root"}));
					folder.folder = dir;
					this.folders.put(Utilities.path(new String[]{"package", "$root"}), folder);
				}
			}
		}
	}

	public static boolean isInternalExemptFile(File f) {
		return Utilities.existsInList(f.getName(), new String[]{".git", ".svn", ".DS_Store"})
			|| Utilities.existsInList(f.getName(), new String[]{"package-list.json"})
			|| Utilities.endsWithInList(f.getName(), new String[]{".tgz"});
	}

	private void loadSubFolders(String rootPath, File dir) throws IOException {
		for(File f : dir.listFiles()) {
			if (f.isDirectory()) {
				String d = f.getAbsolutePath().substring(rootPath.length() + 1);
				if (!d.startsWith("package")) {
					d = Utilities.path(new String[]{"package", d});
				}

				NpmPackage.NpmPackageFolder folder = new NpmPackage.NpmPackageFolder(d);
				folder.folder = f;
				this.folders.put(d, folder);
				File ij = new File(Utilities.path(new String[]{f.getAbsolutePath(), ".index.json"}));
				if (ij.exists() || !this.minimalMemory) {
					try {
						if (!ij.exists() || !folder.readIndex(JsonParser.parseObject(ij), folder.getTypes())) {
							this.indexFolder(folder.getFolderName(), folder);
						}
					} catch (Exception var11) {
						throw new IOException("Error parsing " + ij.getAbsolutePath() + ": " + var11.getMessage(), var11);
					}
				}

				this.loadSubFolders(rootPath, f);
			}
		}
	}

	public static NpmPackage fromFolder(String folder, PackageType defType, String... exemptions) throws IOException {
		NpmPackage res = new NpmPackage();
		res.loadFiles(folder, new File(folder), exemptions);
		if (!res.folders.containsKey("package")) {
			res.folders.put("package", res.new NpmPackageFolder("package"));
		}

		if (!((NpmPackage.NpmPackageFolder)res.folders.get("package")).hasFile("package.json") && defType != null) {
			TextFile.stringToFile(
				"{ \"type\" : \"" + defType.getCode() + "\"}",
				Utilities.path(new String[]{((NpmPackage.NpmPackageFolder)res.folders.get("package")).folder.getAbsolutePath(), "package.json"})
			);
		}

		res.npm = JsonParser.parseObject(new String(((NpmPackage.NpmPackageFolder)res.folders.get("package")).fetchFile("package.json")));
		return res;
	}

	@Nonnull
	public static NpmPackage fromPackage(InputStream tgz) throws IOException {
		return fromPackage(tgz, null, false);
	}

	public static NpmPackage fromPackage(InputStream tgz, String desc) throws IOException {
		return fromPackage(tgz, desc, false);
	}

	public static NpmPackage fromPackage(InputStream tgz, String desc, boolean progress) throws IOException {
		NpmPackage res = new NpmPackage();
		res.readStream(tgz, desc, progress);
		return res;
	}

	public static NpmPackage extractFromTgz(InputStream tgz, String desc, String tempDir, boolean minimal) throws IOException {
		Utilities.createDirectory(tempDir);
		int size = 0;

		GzipCompressorInputStream gzipIn;
		try {
			gzipIn = new GzipCompressorInputStream(tgz);
		} catch (Exception var20) {
			throw new IOException("Error reading " + (desc == null ? "package" : desc) + ": " + var20.getMessage(), var20);
		}

		TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);

		TarArchiveEntry entry;
		try {
			while((entry = (TarArchiveEntry)tarIn.getNextEntry()) != null) {
				String n = entry.getName();
				if (n.contains("/..") || n.contains("../")) {
					throw new RuntimeException("Entry with an illegal name: " + n);
				}

				if (!entry.isDirectory()) {
					byte[] data = new byte[1024];
					String filename = Utilities.path(new String[]{tempDir, n});
					String folder = Utilities.getDirectoryForFile(filename);
					Utilities.createDirectory(folder);
					FileOutputStream fos = new FileOutputStream(filename);
					BufferedOutputStream dst = new BufferedOutputStream(fos, 1024);

					int count;
					try {
						while((count = tarIn.read(data, 0, 1024)) != -1) {
							dst.write(data, 0, count);
							size += count;
						}
					} catch (Throwable var21) {
						try {
							dst.close();
						} catch (Throwable var18) {
							var21.addSuppressed(var18);
						}

						throw var21;
					}

					dst.close();
					fos.close();
				} else if (!Utilities.noString(n)) {
					String dir = n.substring(0, n.length() - 1);
					Utilities.createDirectory(Utilities.path(new String[]{tempDir, dir}));
				}
			}
		} catch (Throwable var22) {
			try {
				tarIn.close();
			} catch (Throwable var17) {
				var22.addSuppressed(var17);
			}

			throw var22;
		}

		tarIn.close();

		try {
			NpmPackage npm = fromFolderMinimal(tempDir);
			npm.setSize(size);
			if (!minimal) {
				npm.checkIndexed(desc);
			}

			return npm;
		} catch (Exception var19) {
			throw new IOException("Error parsing " + (desc == null ? "" : desc + "#") + "package/package.json: " + var19.getMessage(), var19);
		}
	}

	public void readStream(InputStream tgz, String desc, boolean progress) throws IOException {
		GzipCompressorInputStream gzipIn;
		try {
			gzipIn = new GzipCompressorInputStream(tgz);
		} catch (Exception var19) {
			throw new IOException("Error reading " + (desc == null ? "package" : desc) + ": " + var19.getMessage(), var19);
		}

		TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);

		try {
			int i = 0;
			int c = 12;

			TarArchiveEntry entry;
			while((entry = (TarArchiveEntry)tarIn.getNextEntry()) != null) {
				++i;
				String n = entry.getName();
				if (n.contains("..")) {
					throw new RuntimeException("Entry with an illegal name: " + n);
				}

				if (entry.isDirectory()) {
					String dir = n.substring(0, n.length() - 1);
					if (dir.startsWith("package/")) {
						dir = dir.substring(8);
					}

					this.folders.put(dir, new NpmPackage.NpmPackageFolder(dir));
				} else {
					byte[] data = new byte[1024];
					ByteArrayOutputStream fos = new ByteArrayOutputStream();
					BufferedOutputStream dest = new BufferedOutputStream(fos, 1024);

					int count;
					try {
						while((count = tarIn.read(data, 0, 1024)) != -1) {
							dest.write(data, 0, count);
						}
					} catch (Throwable var20) {
						try {
							dest.close();
						} catch (Throwable var17) {
							var20.addSuppressed(var17);
						}

						throw var20;
					}

					dest.close();
					fos.close();
					this.loadFile(n, fos.toByteArray());
				}

				if (progress && i % 50 == 0) {
					++c;
					System.out.print(".");
					if (c == 120) {
						System.out.println("");
						System.out.print("  ");
						c = 2;
					}
				}
			}
		} catch (Throwable var21) {
			try {
				tarIn.close();
			} catch (Throwable var16) {
				var21.addSuppressed(var16);
			}

			throw var21;
		}

		tarIn.close();

		try {
			this.npm = JsonParser.parseObject(((NpmPackage.NpmPackageFolder)this.folders.get("package")).fetchFile("package.json"));
		} catch (Exception var18) {
			throw new IOException("Error parsing " + (desc == null ? "" : desc + "#") + "package/package.json: " + var18.getMessage(), var18);
		}

		this.checkIndexed(desc);
	}

	public void loadFile(String n, byte[] data) throws IOException {
		String dir = n.contains("/") ? n.substring(0, n.lastIndexOf("/")) : "$root";
		if (dir.startsWith("package/")) {
			dir = dir.substring(8);
		}

		n = n.substring(n.lastIndexOf("/") + 1);
		NpmPackage.NpmPackageFolder index = (NpmPackage.NpmPackageFolder)this.folders.get(dir);
		if (index == null) {
			index = new NpmPackage.NpmPackageFolder(dir);
			this.folders.put(dir, index);
		}

		index.content.put(n, data);
	}

	private void checkIndexed(String desc) throws IOException {
		for(NpmPackage.NpmPackageFolder folder : this.folders.values()) {
			JsonObject index = folder.index();
			if (index == null || index.forceArray("files").size() == 0) {
				this.indexFolder(desc, folder);
			}
		}
	}

	public void indexFolder(String desc, NpmPackage.NpmPackageFolder folder) throws FileNotFoundException, IOException {
		List<String> remove = new ArrayList();
		NpmPackageIndexBuilder indexer = new NpmPackageIndexBuilder();
		indexer.start();

		for(String n : folder.listFiles()) {
			if (!indexer.seeFile(n, folder.fetchFile(n))) {
				remove.add(n);
			}
		}

		for(String n : remove) {
			folder.removeFile(n);
		}

		String json = indexer.build();

		try {
			if (!this.minimalMemory) {
				folder.readIndex(JsonParser.parseObject(json), folder.getTypes());
			}

			if (folder.folder != null) {
				TextFile.stringToFile(json, Utilities.path(new String[]{folder.folder.getAbsolutePath(), ".index.json"}));
			}
		} catch (Exception var7) {
			TextFile.stringToFile(json, Utilities.path(new String[]{"[tmp]", ".index.json"}));
			throw new IOException("Error parsing " + (desc == null ? "" : desc + "#") + "package/" + folder.folderName + "/.index.json: " + var7.getMessage(), var7);
		}
	}

	public static NpmPackage fromZip(InputStream stream, boolean dropRootFolder, String desc) throws IOException {
		NpmPackage res = new NpmPackage();

		ZipInputStream zip;
		ZipEntry ze;
		for(zip = new ZipInputStream(stream); (ze = zip.getNextEntry()) != null; zip.closeEntry()) {
			byte[] buffer = new byte[2048];
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(bytes, buffer.length);

			int size;
			while((size = zip.read(buffer, 0, buffer.length)) != -1) {
				bos.write(buffer, 0, size);
			}

			bos.flush();
			bos.close();
			if (bytes.size() > 0) {
				if (dropRootFolder) {
					res.loadFile(ze.getName().substring(ze.getName().indexOf("/") + 1), bytes.toByteArray());
				} else {
					res.loadFile(ze.getName(), bytes.toByteArray());
				}
			}
		}

		zip.close();

		try {
			res.npm = JsonParser.parseObject(((NpmPackage.NpmPackageFolder)res.folders.get("package")).fetchFile("package.json"));
		} catch (Exception var10) {
			throw new IOException("Error parsing " + (desc == null ? "" : desc + "#") + "package/package.json: " + var10.getMessage(), var10);
		}

		res.checkIndexed(desc);
		return res;
	}

	public List<String> list(String folder) throws IOException {
		List<String> res = new ArrayList();
		if (this.folders.containsKey(folder)) {
			res.addAll(((NpmPackage.NpmPackageFolder)this.folders.get(folder)).listFiles());
		} else if (this.folders.containsKey(Utilities.path(new String[]{"package", folder}))) {
			res.addAll(((NpmPackage.NpmPackageFolder)this.folders.get(Utilities.path(new String[]{"package", folder}))).listFiles());
		}

		return res;
	}

	public List<String> listResources(String... types) throws IOException {
		return this.listResources(Utilities.strings(types));
	}

	public List<String> listResources(List<String> types) throws IOException {
		List<String> res = new ArrayList();
		NpmPackage.NpmPackageFolder folder = (NpmPackage.NpmPackageFolder)this.folders.get("package");
		if (types.size() == 0) {
			for(String s : folder.types.keySet()) {
				if (folder.types.containsKey(s)) {
					res.addAll((Collection)folder.types.get(s));
				}
			}
		} else {
			for(String s : types) {
				if (folder.types.containsKey(s)) {
					res.addAll((Collection)folder.types.get(s));
				}
			}
		}

		Collections.sort(res);
		return res;
	}

	public List<NpmPackage.PackageResourceInformation> listIndexedResources(String... types) throws IOException {
		return this.listIndexedResources(Utilities.strings(types));
	}

	public List<NpmPackage.PackageResourceInformation> listIndexedResources(List<String> types) throws IOException {
		List<NpmPackage.PackageResourceInformation> res = new ArrayList();

		for(NpmPackage.NpmPackageFolder folder : this.folders.values()) {
			JsonObject index = folder.index();
			if (index != null) {
				for(JsonObject fi : index.getJsonObjects("files")) {
					if (Utilities.existsInList(fi.asString("resourceType"), types) || types.isEmpty()) {
						res.add(new NpmPackage.PackageResourceInformation(folder.folder == null ? "@" + folder.getFolderName() : folder.folder.getAbsolutePath(), fi));
					}
				}
			}
		}

		return res;
	}

	public InputStream loadResource(String file) throws IOException {
		NpmPackage.NpmPackageFolder folder = (NpmPackage.NpmPackageFolder)this.folders.get("package");
		return new ByteArrayInputStream(folder.fetchFile(file));
	}

	public InputStream loadByCanonical(String canonical) throws IOException {
		return this.loadByCanonicalVersion("package", canonical, null);
	}

	public InputStream loadByCanonical(String folder, String canonical) throws IOException {
		return this.loadByCanonicalVersion(folder, canonical, null);
	}

	public InputStream loadByCanonicalVersion(String canonical, String version) throws IOException {
		return this.loadByCanonicalVersion("package", canonical, version);
	}

	public InputStream loadByCanonicalVersion(String folder, String canonical, String version) throws IOException {
		NpmPackage.NpmPackageFolder f = (NpmPackage.NpmPackageFolder)this.folders.get(folder);
		List<JsonObject> matches = new ArrayList();

		for(JsonObject file : f.index().getJsonObjects("files")) {
			if (canonical.equals(file.asString("url"))) {
				if (version != null && version.equals(file.asString("version"))) {
					return this.load("package", file.asString("filename"));
				}

				if (version == null) {
					matches.add(file);
				}
			}

			if (matches.size() > 0) {
				if (matches.size() == 1) {
					return this.load("package", ((JsonObject)matches.get(0)).asString("filename"));
				}

				Collections.sort(matches, new NpmPackage.IndexVersionSorter());
				return this.load("package", ((JsonObject)matches.get(matches.size() - 1)).asString("filename"));
			}
		}

		return null;
	}

	public InputStream load(String file) throws IOException {
		return this.load("package", file);
	}

	public InputStream load(String folder, String file) throws IOException {
		NpmPackage.NpmPackageFolder f = (NpmPackage.NpmPackageFolder)this.folders.get(folder);
		if (f == null) {
			f = (NpmPackage.NpmPackageFolder)this.folders.get(Utilities.path(new String[]{"package", folder}));
		}

		if (f != null && f.hasFile(file)) {
			return new ByteArrayInputStream(f.fetchFile(file));
		} else {
			throw new IOException("Unable to find the file " + folder + "/" + file + " in the package " + this.name());
		}
	}

	public ByteProvider getProvider(String folder, String file) throws IOException {
		NpmPackage.NpmPackageFolder f = (NpmPackage.NpmPackageFolder)this.folders.get(folder);
		if (f == null) {
			f = (NpmPackage.NpmPackageFolder)this.folders.get(Utilities.path(new String[]{"package", folder}));
		}

		if (f != null && f.hasFile(file)) {
			return f.getProvider(file);
		} else {
			throw new IOException("Unable to find the file " + folder + "/" + file + " in the package " + this.name());
		}
	}

	public boolean hasFile(String folder, String file) throws IOException {
		NpmPackage.NpmPackageFolder f = (NpmPackage.NpmPackageFolder)this.folders.get(folder);
		if (f == null) {
			f = (NpmPackage.NpmPackageFolder)this.folders.get(Utilities.path(new String[]{"package", folder}));
		}

		return f != null && f.hasFile(file);
	}

	public JsonObject getNpm() {
		return this.npm;
	}

	public void setNpm(JsonObject npm) {
		this.npm = npm;
	}

	public String name() {
		return this.npm.asString("name");
	}

	public String id() {
		return this.npm.asString("name");
	}

	public String date() {
		return this.npm.asString("date");
	}

	public String canonical() {
		return this.npm.asString("canonical");
	}

	public String version() {
		return this.npm.asString("version");
	}

	public String fhirVersion() {
		if ("hl7.fhir.core".equals(this.npm.asString("name"))) {
			return this.npm.asString("version");
		} else if (!this.npm.asString("name").startsWith("hl7.fhir.r2.")
			&& !this.npm.asString("name").startsWith("hl7.fhir.r2b.")
			&& !this.npm.asString("name").startsWith("hl7.fhir.r3.")
			&& !this.npm.asString("name").startsWith("hl7.fhir.r4.")
			&& !this.npm.asString("name").startsWith("hl7.fhir.r4b.")
			&& !this.npm.asString("name").startsWith("hl7.fhir.r5.")) {
			JsonObject dep = null;
			if (this.npm.hasObject("dependencies")) {
				dep = this.npm.getJsonObject("dependencies");
				if (dep != null) {
					for(JsonProperty e : dep.getProperties()) {
						if (Utilities.existsInList(e.getName(), new String[]{"hl7.fhir.r2.core", "hl7.fhir.r2b.core", "hl7.fhir.r3.core", "hl7.fhir.r4.core"})) {
							return e.getValue().asString();
						}

						if (Utilities.existsInList(e.getName(), new String[]{"hl7.fhir.core"})) {
							return e.getValue().asString();
						}
					}
				}
			}

			if (this.npm.hasArray("fhirVersions")) {
				JsonArray e = this.npm.getJsonArray("fhirVersions");
				if (e.size() > 0) {
					return ((JsonElement)e.getItems().get(0)).asString();
				}
			}

			if (dep != null) {
				if (dep.has("simplifier.core.r4")) {
					return "4.0";
				}

				if (dep.has("simplifier.core.r3")) {
					return "3.0";
				}

				if (dep.has("simplifier.core.r2")) {
					return "2.0";
				}
			}

			throw new FHIRException("no core dependency or FHIR Version found in the Package definition");
		} else {
			return this.npm.asString("version");
		}
	}

	public String summary() {
		return this.path != null ? this.path : "memory";
	}

	public boolean isType(PackageType template) {
		return template.getCode().equals(this.type()) || template.getOldCode().equals(this.type());
	}

	public String type() {
		return this.npm.asString("type");
	}

	public String description() {
		return this.npm.asString("description");
	}

	public String getPath() {
		return this.path;
	}

	public List<String> dependencies() {
		List<String> res = new ArrayList();
		if (this.npm.has("dependencies")) {
			for(JsonProperty e : this.npm.getJsonObject("dependencies").getProperties()) {
				res.add(e.getName() + "#" + e.getValue().asString());
			}
		}

		return res;
	}

	public String homepage() {
		return this.npm.asString("homepage");
	}

	public String url() {
		return this.npm.asString("url");
	}

	public String title() {
		return this.npm.asString("title");
	}

	public String toolsVersion() {
		return this.npm.asString("tools-version");
	}

	public String license() {
		return this.npm.asString("license");
	}

	public String getWebLocation() {
		return this.npm.hasPrimitive("url") ? PackageHacker.fixPackageUrl(this.npm.asString("url")) : this.npm.asString("canonical");
	}

	public InputStream loadResource(String type, String id) throws IOException {
		NpmPackage.NpmPackageFolder f = (NpmPackage.NpmPackageFolder)this.folders.get("package");
		JsonArray files = f.index().getJsonArray("files");

		for(JsonElement e : files.getItems()) {
			JsonObject i = (JsonObject)e;
			if (type.equals(i.asString("resourceType")) && id.equals(i.asString("id"))) {
				return this.load("package", i.asString("filename"));
			}
		}

		return null;
	}

	public InputStream loadExampleResource(String type, String id) throws IOException {
		NpmPackage.NpmPackageFolder f = (NpmPackage.NpmPackageFolder)this.folders.get("example");
		if (f == null) {
			f = (NpmPackage.NpmPackageFolder)this.folders.get("package/example");
		}

		if (f != null) {
			JsonArray files = f.index().getJsonArray("files");

			for(JsonElement e : files.getItems()) {
				JsonObject i = (JsonObject)e;
				if (type.equals(i.asString("resourceType")) && id.equals(i.asString("id"))) {
					return this.load("example", i.asString("filename"));
				}
			}
		}

		return null;
	}

	public Map<String, NpmPackage.NpmPackageFolder> getFolders() {
		return this.folders;
	}

	public void save(File directory) throws IOException {
		assert !this.minimalMemory;

		File dir = new File(Utilities.path(new String[]{directory.getAbsolutePath(), this.name()}));
		if (!dir.exists()) {
			Utilities.createDirectory(dir.getAbsolutePath());
		} else {
			Utilities.clearDirectory(dir.getAbsolutePath(), new String[0]);
		}

		for(NpmPackage.NpmPackageFolder folder : this.folders.values()) {
			String n = folder.folderName;
			File pd = new File(Utilities.path(new String[]{dir.getAbsolutePath(), n}));
			if (!pd.exists()) {
				Utilities.createDirectory(pd.getAbsolutePath());
			}

			NpmPackageIndexBuilder indexer = new NpmPackageIndexBuilder();
			indexer.start();

			for(String s : folder.content.keySet()) {
				byte[] b = (byte[])folder.content.get(s);
				indexer.seeFile(s, b);
				if (!s.equals(".index.json") && !s.equals("package.json")) {
					TextFile.bytesToFile(b, Utilities.path(new String[]{dir.getAbsolutePath(), n, s}));
				}
			}

			byte[] cnt = indexer.build().getBytes(StandardCharsets.UTF_8);
			TextFile.bytesToFile(cnt, Utilities.path(new String[]{dir.getAbsolutePath(), n, ".index.json"}));
		}

		byte[] cnt = TextFile.stringToBytes(JsonParser.compose(this.npm, true), false);
		TextFile.bytesToFile(cnt, Utilities.path(new String[]{dir.getAbsolutePath(), "package", "package.json"}));
	}

	public void save(OutputStream stream) throws IOException {
		assert !this.minimalMemory;

		ByteArrayOutputStream OutputStream = new ByteArrayOutputStream();
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(OutputStream);
		GzipParameters gp = new GzipParameters();
		gp.setCompressionLevel(9);
		GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(stream, gp);
		TarArchiveOutputStream tar = new TarArchiveOutputStream(gzipOutputStream);

		for(NpmPackage.NpmPackageFolder folder : this.folders.values()) {
			String n = folder.folderName;
			if (!"package".equals(n) && !n.startsWith("package/") && !n.startsWith("package\\")) {
				n = "package/" + n;
			}

			NpmPackageIndexBuilder indexer = new NpmPackageIndexBuilder();
			indexer.start();

			for(String s : folder.content.keySet()) {
				byte[] b = (byte[])folder.content.get(s);
				String name = n + "/" + s;
				if (b == null) {
					System.out.println(name + " is null");
				} else {
					indexer.seeFile(s, b);
					if (!s.equals(".index.json") && !s.equals("package.json")) {
						TarArchiveEntry entry = new TarArchiveEntry(name);
						entry.setSize((long)b.length);
						tar.putArchiveEntry(entry);
						tar.write(b);
						tar.closeArchiveEntry();
					}
				}
			}

			byte[] cnt = indexer.build().getBytes(StandardCharsets.UTF_8);
			TarArchiveEntry entry = new TarArchiveEntry(n + "/.index.json");
			entry.setSize((long)cnt.length);
			tar.putArchiveEntry(entry);
			tar.write(cnt);
			tar.closeArchiveEntry();
		}

		byte[] cnt = TextFile.stringToBytes(JsonParser.compose(this.npm, true), false);
		TarArchiveEntry entry = new TarArchiveEntry("package/package.json");
		entry.setSize((long)cnt.length);
		tar.putArchiveEntry(entry);
		tar.write(cnt);
		tar.closeArchiveEntry();
		tar.finish();
		tar.close();
		gzipOutputStream.close();
		bufferedOutputStream.close();
		OutputStream.close();
		byte[] b = OutputStream.toByteArray();
		stream.write(b);
	}

	public Map<String, List<String>> getTypes() {
		return ((NpmPackage.NpmPackageFolder)this.folders.get("package")).types;
	}

	public String fhirVersionList() {
		if (!this.npm.has("fhirVersions")) {
			return "";
		} else {
			CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
			if (this.npm.hasArray("fhirVersions")) {
				for(String n : this.npm.getJsonArray("fhirVersions").asStrings()) {
					b.append(n);
				}
			}

			if (this.npm.hasPrimitive("fhirVersions")) {
				b.append(this.npm.asString("fhirVersions"));
			}

			return b.toString();
		}
	}

	public String dependencySummary() {
		if (!this.npm.has("dependencies")) {
			return "";
		} else {
			CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();

			for(JsonProperty e : this.npm.getJsonObject("dependencies").getProperties()) {
				b.append(e.getName() + "#" + e.getValue().asString());
			}

			return b.toString();
		}
	}

	public void unPack(String dir) throws IOException {
		this.unPack(dir, false);
	}

	public void unPackWithAppend(String dir) throws IOException {
		this.unPack(dir, true);
	}

	public void unPack(String dir, boolean withAppend) throws IOException {
		assert !this.minimalMemory;

		for(NpmPackage.NpmPackageFolder folder : this.folders.values()) {
			String dn = folder.getFolderName();
			if (!dn.equals("package") && (dn.startsWith("package/") || dn.startsWith("package\\"))) {
				dn = dn.substring(8);
			}

			if (dn.equals("$root")) {
				dn = dir;
			} else {
				dn = Utilities.path(new String[]{dir, dn});
			}

			Utilities.createDirectory(dn);

			for(String s : folder.listFiles()) {
				String fn = Utilities.path(new String[]{dn, s});
				File f = new File(fn);
				if (withAppend && f.getName().startsWith("_append.")) {
					String appendFn = Utilities.path(new String[]{dn, s.substring(8)});
					if (new File(appendFn).exists()) {
						TextFile.appendBytesToFile(folder.fetchFile(s), appendFn);
					} else {
						TextFile.bytesToFile(folder.fetchFile(s), appendFn);
					}
				} else {
					TextFile.bytesToFile(folder.fetchFile(s), fn);
				}
			}
		}
	}

	public void debugDump(String purpose) {
	}

	private List<String> sorted(Set<String> keys) {
		List<String> res = new ArrayList();
		res.addAll(keys);
		Collections.sort(res);
		return res;
	}

	public void clearFolder(String folderName) {
		NpmPackage.NpmPackageFolder folder = (NpmPackage.NpmPackageFolder)this.folders.get(folderName);
		folder.content.clear();
		folder.types.clear();
	}

	public void deleteFolder(String folderName) {
		this.folders.remove(folderName);
	}

	public void addFile(String folderName, String name, byte[] cnt, String type) {
		assert !this.minimalMemory;

		if (!this.folders.containsKey(folderName)) {
			this.folders.put(folderName, new NpmPackage.NpmPackageFolder(folderName));
		}

		NpmPackage.NpmPackageFolder folder = (NpmPackage.NpmPackageFolder)this.folders.get(folderName);
		folder.content.put(name, cnt);
		if (!folder.types.containsKey(type)) {
			folder.types.put(type, new ArrayList());
		}

		((List)folder.types.get(type)).add(name);
		if ("package".equals(folderName) && "package.json".equals(name)) {
			try {
				this.npm = JsonParser.parseObject(cnt);
			} catch (IOException var7) {
			}
		}
	}

	public void loadAllFiles() throws IOException {
		for(String folder : this.folders.keySet()) {
			NpmPackage.NpmPackageFolder pf = (NpmPackage.NpmPackageFolder)this.folders.get(folder);
			String p = folder.contains("$") ? this.path : Utilities.path(new String[]{this.path, folder});
			File file = new File(p);
			if (file.exists()) {
				for(File f : file.listFiles()) {
					if (!f.isDirectory() && !isInternalExemptFile(f)) {
						pf.getContent().put(f.getName(), TextFile.fileToBytes(f));
					}
				}
			}
		}
	}

	public void loadAllFiles(NpmPackage.ITransformingLoader loader) throws IOException {
		for(String folder : this.folders.keySet()) {
			NpmPackage.NpmPackageFolder pf = (NpmPackage.NpmPackageFolder)this.folders.get(folder);
			String p = folder.contains("$") ? this.path : Utilities.path(new String[]{this.path, folder});

			for(File f : new File(p).listFiles()) {
				if (!f.isDirectory() && !isInternalExemptFile(f)) {
					pf.getContent().put(f.getName(), loader.load(f));
				}
			}
		}
	}

	public boolean isChangedByLoader() {
		return this.changedByLoader;
	}

	public boolean isCore() {
		return Utilities.existsInList(this.npm.asString("type"), new String[]{"fhir.core", "Core"});
	}

	public boolean isCoreExamples() {
		return this.name().startsWith("hl7.fhir.r") && this.name().endsWith(".examples");
	}

	public boolean isTx() {
		return this.npm.asString("name").startsWith("hl7.terminology");
	}

	public boolean hasCanonical(String url) throws IOException {
		if (url == null) {
			return false;
		} else {
			String u = url.contains("|") ? url.substring(0, url.indexOf("|")) : url;
			String v = url.contains("|") ? url.substring(url.indexOf("|") + 1) : null;
			NpmPackage.NpmPackageFolder folder = (NpmPackage.NpmPackageFolder)this.folders.get("package");
			if (folder != null) {
				for(JsonObject o : folder.index().getJsonObjects("files")) {
					if (u.equals(o.asString("url")) && (v == null || v.equals(o.asString("version")))) {
						return true;
					}
				}
			}

			return false;
		}
	}

	public boolean canLazyLoad() throws IOException {
		for(NpmPackage.NpmPackageFolder folder : this.folders.values()) {
			if (folder.folder == null) {
				return false;
			}
		}

		if (Utilities.existsInList(
			this.name(),
			new String[]{
				"fhir.test.data.r2", "fhir.test.data.r3", "fhir.test.data.r4", "fhir.tx.support.r2", "fhir.tx.support.r3", "fhir.tx.support.r4", "us.nlm.vsac"
			}
		)) {
			return true;
		} else if (this.npm.asBoolean("lazy-load")) {
			return true;
		} else {
			return this.hasFile("other", "spec.internals") || ((NpmPackage.NpmPackageFolder)this.folders.get("package")).cachedIndex != null;
		}
	}

	public boolean isNotForPublication() {
		return this.npm.asBoolean("notForPublication");
	}

	public InputStream load(NpmPackage.PackageResourceInformation p) throws FileNotFoundException {
		if (p.filename.startsWith("@")) {
			// MATCHBOX: fix for windows
			String[] pl = p.filename.replace("\\", "/").substring(1).split("\\/");
			return new ByteArrayInputStream((byte[])((NpmPackage.NpmPackageFolder)this.folders.get(pl[0])).content.get(pl[1]));
		} else {
			return new FileInputStream(p.filename);
		}
	}

	public Date dateAsDate() {
		try {
			String d = this.date();
			if (d == null) {
				String var2 = this.name();
				switch(var2) {
					case "hl7.fhir.r2.core":
						d = "20151024000000";
						break;
					case "hl7.fhir.r2b.core":
						d = "20160330000000";
						break;
					case "hl7.fhir.r3.core":
						d = "20191024000000";
						break;
					case "hl7.fhir.r4.core":
						d = "20191030000000";
						break;
					case "hl7.fhir.r4b.core":
						d = "202112200000000";
						break;
					case "hl7.fhir.r5.core":
						d = "20211219000000";
						break;
					default:
						return new Date();
				}
			}

			return new SimpleDateFormat("yyyyMMddHHmmss").parse(d);
		} catch (ParseException var4) {
			return new Date();
		}
	}

	public static NpmPackage fromUrl(String source) throws IOException {
		SimpleHTTPClient fetcher = new SimpleHTTPClient();
		HTTPResult res = fetcher.get(source + "?nocache=" + System.currentTimeMillis());
		res.checkThrowException();
		return fromPackage(new ByteArrayInputStream(res.getContent()));
	}

	public String toString() {
		return "NpmPackage " + this.name() + "#" + this.version() + " [path=" + this.path + "]";
	}

	public String getFilePath(String d) throws IOException {
		return Utilities.path(new String[]{this.path, "package", d});
	}

	public boolean isMinimalMemory() {
		return this.minimalMemory;
	}

	public int getSize() {
		return this.size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isWarned() {
		return this.warned;
	}

	public void setWarned(boolean warned) {
		this.warned = warned;
	}

	public interface ITransformingLoader {
		byte[] load(File var1);
	}

	public class IndexVersionSorter implements Comparator<JsonObject> {
		public IndexVersionSorter() {
		}

		public int compare(JsonObject o0, JsonObject o1) {
			String v0 = o0.asString("version");
			String v1 = o1.asString("version");
			return v0.compareTo(v1);
		}
	}

	public class NpmPackageFolder {
		private String folderName;
		private Map<String, List<String>> types;
		private Map<String, byte[]> content;
		private JsonObject cachedIndex;
		private File folder;

		public NpmPackageFolder(String folderName) {
			this.folderName = folderName;
			if (!NpmPackage.this.minimalMemory) {
				this.types = new HashMap();
				this.content = new HashMap();
			}
		}

		private String fn(String name) throws IOException {
			return Utilities.path(new String[]{this.folder.getAbsolutePath(), name});
		}

		public Map<String, List<String>> getTypes() throws JsonException, IOException {
			if (NpmPackage.this.minimalMemory) {
				Map<String, List<String>> typeMap = new HashMap();
				this.readIndex(JsonParser.parseObjectFromFile(this.fn(".index.json")), typeMap);
				return typeMap;
			} else {
				return this.types;
			}
		}

		public String getFolderName() {
			return this.folderName;
		}

		public boolean readIndex(JsonObject index, Map<String, List<String>> typeMap) {
			if (index.has("index-version") && index.asInteger("index-version") == NpmPackageIndexBuilder.CURRENT_INDEX_VERSION) {
				if (!NpmPackage.this.minimalMemory) {
					this.cachedIndex = index;
				}

				for(JsonObject file : index.getJsonObjects("files")) {
					String type = file.asString("resourceType");
					String name = file.asString("filename");
					if (!typeMap.containsKey(type)) {
						typeMap.put(type, new ArrayList());
					}

					((List)typeMap.get(type)).add(name);
				}

				return true;
			} else {
				return false;
			}
		}

		public List<String> listFiles() {
			List<String> res = new ArrayList();
			if (this.folder != null) {
				for(File f : this.folder.listFiles()) {
					if (!f.isDirectory() && !Utilities.existsInList(f.getName(), new String[]{"package.json", ".index.json"})) {
						res.add(f.getName());
					}
				}
			} else {
				for(String s : this.content.keySet()) {
					if (!Utilities.existsInList(s, new String[]{"package.json", ".index.json"})) {
						res.add(s);
					}
				}
			}

			Collections.sort(res);
			return res;
		}

		public Map<String, byte[]> getContent() {
			assert !NpmPackage.this.minimalMemory;

			return this.content;
		}

		public byte[] fetchFile(String file) throws FileNotFoundException, IOException {
			if (this.folder != null) {
				File f = new File(Utilities.path(new String[]{this.folder.getAbsolutePath(), file}));
				return f.exists() ? TextFile.fileToBytes(f) : null;
			} else {
				return (byte[])this.content.get(file);
			}
		}

		public ByteProvider getProvider(String file) throws FileNotFoundException, IOException {
			if (this.folder != null) {
				File f = new File(Utilities.path(new String[]{this.folder.getAbsolutePath(), file}));
				return f.exists() ? ByteProvider.forFile(f) : null;
			} else {
				return ByteProvider.forBytes((byte[])this.content.get(file));
			}
		}

		public boolean hasFile(String file) throws IOException {
			return this.folder != null ? new File(Utilities.path(new String[]{this.folder.getAbsolutePath(), file})).exists() : this.content.containsKey(file);
		}

		public String dump() {
			return this.folderName
				+ " ("
				+ (this.folder == null ? "null" : this.folder.toString())
				+ ")"
				+ (NpmPackage.this.minimalMemory ? "" : " | " + Boolean.toString(this.cachedIndex != null) + " | " + this.content.size() + " | " + this.types.size());
		}

		public void removeFile(String n) throws IOException {
			if (this.folder != null) {
				new File(Utilities.path(new String[]{this.folder.getAbsolutePath(), n})).delete();
			} else {
				this.content.remove(n);
			}

			NpmPackage.this.changedByLoader = true;
		}

		public JsonObject index() throws IOException {
			if (this.cachedIndex != null) {
				return this.cachedIndex;
			} else if (this.folder == null) {
				return null;
			} else {
				File ij = new File(this.fn(".index.json"));
				return ij.exists() ? JsonParser.parseObject(ij) : null;
			}
		}
	}

	public class PackageResourceInformation {
		private String id;
		private String resourceType;
		private String url;
		private String version;
		private String filename;
		private String supplements;
		private String stype;

		public PackageResourceInformation(String root, JsonObject fi) throws IOException {
			this.id = fi.asString("id");
			this.resourceType = fi.asString("resourceType");
			this.url = fi.asString("url");
			this.version = fi.asString("version");
			this.filename = Utilities.path(new String[]{root, fi.asString("filename")});
			this.supplements = fi.asString("supplements");
			this.stype = fi.asString("type");
		}

		public String getId() {
			return this.id;
		}

		public String getResourceType() {
			return this.resourceType;
		}

		public String getStatedType() {
			return this.stype;
		}

		public String getUrl() {
			return this.url;
		}

		public String getVersion() {
			return this.version;
		}

		public String getFilename() {
			return this.filename;
		}

		public String getSupplements() {
			return this.supplements;
		}

		public boolean hasId() {
			return !Utilities.noString(this.id);
		}
	}

	public class PackageResourceInformationSorter implements Comparator<NpmPackage.PackageResourceInformation> {
		public PackageResourceInformationSorter() {
		}

		public int compare(NpmPackage.PackageResourceInformation o1, NpmPackage.PackageResourceInformation o2) {
			return o1.filename.compareTo(o2.filename);
		}
	}
}
