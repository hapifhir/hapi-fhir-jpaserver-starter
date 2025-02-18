package org.hl7.fhir.utilities.npm;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.FileUtilities;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.filesystem.ManagedFileAccess;
import org.hl7.fhir.utilities.json.model.JsonArray;
import org.hl7.fhir.utilities.json.model.JsonObject;
import org.hl7.fhir.utilities.json.parser.JsonParser;

/**
 * This class builds the .index.json for a package 
 * 
 * it also builds a .index.db since that may provide faster access
 * 
 * @author grahame
 *
 */
public class NpmPackageIndexBuilder {
  
  public static final Integer CURRENT_INDEX_VERSION = 2;
  private JsonObject index;
  private JsonArray files;
  private Connection conn;
  private PreparedStatement psql;
  private String dbFilename;
  
  public void start(String filename) {
    index = new JsonObject();
    index.add("index-version", CURRENT_INDEX_VERSION);
    files = new JsonArray();
    index.add("files", files);

    dbFilename = filename;
    if (filename != null) {
      try {
        ManagedFileAccess.file(filename).delete();
        conn = DriverManager.getConnection("jdbc:sqlite:"+filename); 
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE ResourceList (\r\n"+
            "FileName       nvarchar NOT NULL,\r\n"+
            "ResourceType   nvarchar NOT NULL,\r\n"+
            "Id             nvarchar NULL,\r\n"+
            "Url            nvarchar NULL,\r\n"+
            "Version        nvarchar NULL,\r\n"+
            "Kind           nvarchar NULL,\r\n"+
            "Type           nvarchar NULL,\r\n"+
            "Supplements    nvarchar NULL,\r\n"+
            "Content        nvarchar NULL,\r\n"+
            "ValueSet       nvarchar NULL,\r\n"+
            "Derivation     nvarchar NULL,\r\n"+
            "PRIMARY KEY (FileName))\r\n");

        psql = conn.prepareStatement("Insert into ResourceList (FileName, ResourceType, Id, Url, Version, Kind, Type, Supplements, Content, ValueSet) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      } catch (Exception e) {
        if (conn != null) { 
          try {
            conn.close();
          } catch (SQLException e1) {
          }
        }
        conn = null;
      }
    }
  }

  public boolean seeFile(String name, byte[] content) {
    if (name.endsWith(".json")) {
      /*
       * MATCHBOX PATCH https://github.com/ahdis/matchbox/issues/342
       * We are only interested in some String fields on the first level of the JSON file.
       * We can then use a streaming parser to get the values of these fields, instead of parsing the whole file, and 
       * allocating memory for everything in it.
       * This relies on the fact that the key 'resourceType' will happen before all other keys (which should be the 
       * case in all FHIR resources). Otherwise, the parser should go over the file content twice: one to detect that
       * key, and, if the key is found, a second time to get the values of the other keys.
       */
      try (final var parser = new JsonFactory().createParser(content)) {
        JsonObject fi = null;
        
        while (parser.nextToken() != JsonToken.END_OBJECT) {
          String fieldName = parser.currentName();
          if ("resourceType".equals(fieldName)) {
            parser.nextToken();
            fi = new JsonObject();
            files.add(fi);
            fi.add("filename", name);
            fi.add(fieldName, parser.getText());
          }

          if (fi != null && ("id".equals(fieldName) || "url".equals(fieldName) || "version".equals(fieldName)
            || "kind".equals(fieldName) || "type".equals(fieldName) || "supplements".equals(fieldName)
            || "content".equals(fieldName) || "valueSet".equals(fieldName) || "derivation".equals(fieldName))) {
            parser.nextToken();
            fi.add(fieldName, parser.getText());
          }
        }
          
        if (fi != null && psql != null) {
          psql.setString(1, name); // FileName); 
          psql.setString(2, fi.asString("resourceType")); // ResourceType"); 
          psql.setString(3, fi.asString("id")); // Id"); 
          psql.setString(4, fi.asString("url")); // Url"); 
          psql.setString(5, fi.asString("version")); // Version"); 
          psql.setString(6, fi.asString("kind")); // Kind");
          psql.setString(7, fi.asString("type")); // Type"); 
          psql.setString(8, fi.asString("supplements")); // Supplements"); 
          psql.setString(9, fi.asString("content")); // Content");
          psql.setString(10, fi.asString("valueSet")); // ValueSet");
          psql.setString(10, fi.asString("derivation")); // ValueSet");
          psql.execute();
        }
      } catch (Exception e) {
//        System.out.println("Error parsing "+name+": "+e.getMessage());
        if (name.contains("openapi")) {
          return false;
        }
      }
    }
    return true;
  }

  public String build() {
    try {
      if (conn != null) {
        conn.close();
      }
    } catch (Exception e) {
      // nothing
    }
    String res = JsonParser.compose(index, true);
    index = null;
    files = null;
    return res;
  }
  
//  private Map<String, List<String>> types = new HashMap<>();
//  private Map<String, String> canonicalMap = new HashMap<>();


  public void executeWithStatus(String folder) throws IOException {
    System.out.print("Index Package "+folder+" ... ");
    execute(folder);
    System.out.println("done");
  }
  
  public void execute(String folder) throws IOException {
    if (existsFolder(folder, "package")) {
      folder = Utilities.path(folder, "package"); 
    }
    if (!existsFile(folder, "package.json")) {
      throw new FHIRException("Not a proper package? (can't find package.json)");
    }
    start(Utilities.path(folder, ".index.db"));
    File dir = ManagedFileAccess.file(folder);
    for (File f : dir.listFiles()) {
      seeFile(f.getName(), FileUtilities.fileToBytes(f));
    }
    FileUtilities.stringToFile(build(), Utilities.path(folder, ".index.json"));
  }

  private boolean existsFolder(String... args) throws IOException {
    File f = ManagedFileAccess.file(Utilities.path(args));
    return f.exists() && f.isDirectory();
  }

  private boolean existsFile(String... args) throws IOException {
    File f = ManagedFileAccess.file(Utilities.path(args));
    return f.exists() && !f.isDirectory();
  }

  public static void main(String[] args) throws IOException {
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r4.core");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r4.examples");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r4.expansions");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r4.elements");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r3.core");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r3.examples");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r3.expansions");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r3.elements");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r2b.core");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r2b.examples");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r2b.expansions");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r2.core");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r2.examples");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r2.expansions");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.r2.elements");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\fhir.test.data\\fhir.test.data.r2");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\fhir.test.data\\fhir.test.data.r3");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\fhir.test.data\\fhir.test.data.r4");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\fhir.tx.support\\fhir.tx.support.r2");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\fhir.tx.support\\fhir.tx.support.r3");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\fhir.tx.support\\fhir.tx.support.r4");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.core#1.0.2");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.core#1.4.0");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.core#3.0.2");
    new NpmPackageIndexBuilder().executeWithStatus("C:\\work\\org.hl7.fhir\\packages\\hl7.fhir.rX\\hl7.fhir.core#4.0.1");
  }

  public String getDbFilename() {
    return dbFilename;
  }


}