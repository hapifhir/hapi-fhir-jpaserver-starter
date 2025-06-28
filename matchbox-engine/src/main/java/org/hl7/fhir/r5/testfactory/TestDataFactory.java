package org.hl7.fhir.r5.testfactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.fhirpath.ExpressionNode.CollectionStatus;
import org.hl7.fhir.r5.fhirpath.FHIRPathEngine;
import org.hl7.fhir.r5.fhirpath.FHIRPathEngine.IEvaluationContext.FunctionDefinition;
import org.hl7.fhir.r5.fhirpath.FHIRPathUtilityClasses.FunctionDetails;
import org.hl7.fhir.r5.fhirpath.TypeDetails;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.liquid.BaseTableWrapper;
import org.hl7.fhir.r5.liquid.LiquidEngine;
import org.hl7.fhir.r5.liquid.LiquidEngine.LiquidDocument;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.testfactory.dataprovider.TableDataProvider;
import org.hl7.fhir.r5.testfactory.dataprovider.ValueSetDataProvider;
import org.hl7.fhir.utilities.CommaSeparatedStringBuilder;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.utilities.FileUtilities;
import org.hl7.fhir.utilities.MarkedToMoveToAdjunctPackage;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.filesystem.ManagedFileAccess;
import org.hl7.fhir.utilities.http.HTTPResult;
import org.hl7.fhir.utilities.http.ManagedWebAccess;
import org.hl7.fhir.utilities.json.JsonException;
import org.hl7.fhir.utilities.json.model.JsonObject;
import org.hl7.fhir.utilities.json.parser.JsonParser;

@MarkedToMoveToAdjunctPackage
@Slf4j
public class TestDataFactory {

  public static class DataTable extends Base {
    List<String> columns = new ArrayList<String>();
    List<List<String>> rows = new ArrayList<List<String>>();
    
    @Override
    public String fhirType() {
      return "DataTable";
    }
    @Override
    public String getIdBase() {
      return null;
    }
    @Override
    public void setIdBase(String value) {
      throw new Error("Readonly");
    }
    @Override
    public Base copy() {
      return this;
    }
    
    
    public List<String> getColumns() {
      return columns;
    }
    public List<List<String>> getRows() {
      return rows;
    }
    @Override
    public FhirPublication getFHIRPublicationVersion() {
      return FhirPublication.R5;
    }

    public Base[] getProperty(int hash, String name, boolean checkValid) throws FHIRException {
      if (rows != null && "rows".equals(name)) {
        Base[] l = new Base[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
          l[i] = BaseTableWrapper.forRow(columns, rows.get(i));
        }
        return l;      
      }
      return super.getProperty(hash, name, checkValid);
    }
    
    public String cell(int row, String col) {
      if (row >= 0 && row < rows.size()) {
        List<String> r = rows.get(row);
        int c = -1;
        if (Utilities.isInteger(col)) {
          c = Utilities.parseInt(col, -1);
        } else {
          c = columns.indexOf(col);
        }
        if (c > -1 && c  < r.size()) {
          return r.get(c);
        }
      }
      return null;
    }
    public String lookup(String lcol, String val, String rcol) {
      for (int i = 0; i < rows.size(); i++) {
        if (val.equals(cell(i, lcol))) {
          return cell(i, rcol);
        }
      }
      return null;
    }
  }
    
  public static class CellLookupFunction extends FunctionDefinition {

    @Override
    public String name() {
      return "cell";
    }

    @Override
    public FunctionDetails details() {
      return new FunctionDetails("Lookup a data element", 2, 2);
    }

    @Override
    public TypeDetails check(FHIRPathEngine engine, Object appContext, TypeDetails focus, List<TypeDetails> parameters) {
      return new TypeDetails(CollectionStatus.SINGLETON, "string");
    }

    @Override
    public List<Base> execute(FHIRPathEngine engine, Object appContext, List<Base> focus, List<List<Base>> parameters) {
      int row = Utilities.parseInt(parameters.get(0).get(0).primitiveValue(), 0);
      String col = parameters.get(1).get(0).primitiveValue();
      DataTable dt = (DataTable) focus.get(0);
      
      List<Base> res = new ArrayList<Base>();
      String s = dt.cell(row, col);
      if (!Utilities.noString(s)) {
        res.add(new StringType(s));
      }
      return res;   
    }
  }

  public static class TableLookupFunction extends FunctionDefinition {

    @Override
    public String name() {
      return "lookup";
    }

    @Override
    public FunctionDetails details() {
      // matchbox patch for https://github.com/hapifhir/org.hl7.fhir.core/issues/1942
      return new FunctionDetails("Lookup a value in a table", 3, 4);
    }

    @Override
    public TypeDetails check(FHIRPathEngine engine, Object appContext, TypeDetails focus, List<TypeDetails> parameters) {
      return new TypeDetails(CollectionStatus.SINGLETON, "string");
    }

    @Override
    public List<Base> execute(FHIRPathEngine engine, Object appContext, List<Base> focus, List<List<Base>> parameters) {

      List<Base> res = new ArrayList<Base>();
      if (focus.get(0) instanceof BaseTableWrapper && parameters.size() == 4 && parameters.get(0).size() == 1 && parameters.get(1).size() == 1 && parameters.get(2).size() == 1 && parameters.get(3).size() == 1) {
        BaseTableWrapper dt = (BaseTableWrapper) focus.get(0);
        String table = parameters.get(0).get(0).primitiveValue(); 
        String lcol = parameters.get(1).get(0).primitiveValue();
        String val = parameters.get(2).get(0).primitiveValue();
        String rcol = parameters.get(3).get(0).primitiveValue();
        if (table != null && lcol != null && val != null && rcol != null) {
          DataTable tbl = dt.getTables().get(table);
          if (tbl != null) {
            String s = tbl.lookup(lcol, val, rcol);
            if (!Utilities.noString(s)) {
              res.add(new StringType(s));
            }
          }
        }
      }
      // matchbox patch for https://github.com/hapifhir/org.hl7.fhir.core/issues/1942
      if (focus.get(0)!=null && focus.get(0) instanceof DataTable && parameters.size() == 3 && parameters.get(0).size() == 1 && parameters.get(1).size() == 1 && parameters.get(2).size() == 1) {
        String lcol = parameters.get(0).get(0).primitiveValue();
        String val = parameters.get(1).get(0).primitiveValue();
        String rcol = parameters.get(2).get(0).primitiveValue();
        DataTable tbl = (DataTable) focus.get(0);
        if (lcol != null && val != null && rcol != null) {
          if (tbl != null) {
            String s = tbl.lookup(lcol, val, rcol);
            if (!Utilities.noString(s)) {
              res.add(new StringType(s));
            }
          }
        }
      }
      return res;
    }
    
  }
  
  private String rootFolder;
  private LiquidEngine liquid;
  private PrintStream testLog;
  private IWorkerContext context;
  private String canonical;
  private FhirFormat format;
  private File localData;
  private FHIRPathEngine fpe;
  private JsonObject details;
  private String name;
  private boolean testing;
  private Map<String, String> profileMap;
  private Locale locale;
  
  public TestDataFactory(IWorkerContext context, JsonObject details, LiquidEngine liquid, FHIRPathEngine fpe, String canonical, String rootFolder, String logFolder, Map<String, String> profileMap, Locale locale) throws IOException {
    super();
    this.context = context;
    this.rootFolder = rootFolder;
    this.canonical = canonical;
    this.details = details;
    this.liquid = liquid;
    this.fpe = fpe;
    this.profileMap = profileMap;
    this.locale = locale;

    this.name = details.asString("name");
    if (Utilities.noString(name)) {
      throw new FHIRException("Factory has no name");
    }
    testLog = new PrintStream(new FileOutputStream(Utilities.path(logFolder, name+".log")));
    format = "json".equals(details.asString("format")) ? FhirFormat.JSON : FhirFormat.XML;
  }
  
  public String getName() {
    return name;
  }
  
  public void execute() throws FHIRException, IOException {
    String mode = details.asString( "mode");
    if ("liquid".equals(mode)) {
      executeLiquid();
    } else if ("profile".equals(mode)) {
      executeProfile();
    } else {
      error("Factory "+getName()+" mode '"+mode+"' unknown");
    }
    log("finished successfully");
    testLog.close();
  }
  

  private void logDataScheme(DataTable tbl, Map<String, DataTable> tables) throws IOException {
    log("data: "+CommaSeparatedStringBuilder.join(",", tbl.getColumns()));
    for (String tn : Utilities.sorted(tables.keySet())) {
      log("tn: "+CommaSeparatedStringBuilder.join(",", tables.get(tn).getColumns()));
    }
  }
  private void logDataScheme(TableDataProvider tbl, Map<String, DataTable> tables) throws IOException {
    log("data: "+CommaSeparatedStringBuilder.join(",", tbl.columns()));
    for (String tn : Utilities.sorted(tables.keySet())) {
      log("tn: "+CommaSeparatedStringBuilder.join(",", tables.get(tn).getColumns()));
    }
  }

  
  private void executeProfile() throws IOException {
    try {
      checkDownloadBaseData();
      
      TableDataProvider tbl = loadTable(Utilities.path(rootFolder, details.asString( "data")));
      Map<String, DataTable> tables = new HashMap<>();
      if (details.has("tables")) {
        JsonObject tablesJ = details.getJsonObject("tables");
        for (String n : tablesJ.getNames()) {
          tables.put(n, loadData(Utilities.path(rootFolder, tablesJ.asString(n))));
        } 
      }
      logDataScheme(tbl, tables);
      ProfileBasedFactory factory = new ProfileBasedFactory(fpe, localData.getAbsolutePath(), tbl, tables, details.forceArray("mappings"));
      factory.setLog(testLog);
      factory.setTesting(testing);
      factory.setMarkProfile(details.asBoolean("mark-profile"));
      String purl = details.asString( "profile");
      StructureDefinition profile = context.fetchResource(StructureDefinition.class, purl);
      if (profile == null) {
        error("Unable to find profile "+purl);
      } else if (!profile.hasSnapshot()) {
        error("Profile "+purl+" doesn't have a snapshot");
      }
      
      if ("true".equals(details.asString("bundle"))) {
        byte[] data = runBundle(profile, factory, tbl);
        String fn = Utilities.path(rootFolder, details.asString( "filename"));
        FileUtilities.bytesToFile(data, fn);
        profileMap.put(FileUtilities.changeFileExt(fn, ""), profile.getVersionedUrl());
      } else {
        while (tbl.nextRow()) {
          if (rowPasses(factory)) {
            byte[] data = factory.generateFormat(profile, format);
            String fn = Utilities.path(rootFolder, getFileName(details.asString( "filename"), tbl.columns(), tbl.cells()));
            FileUtilities.createDirectory(FileUtilities.getDirectoryForFile(fn));
            FileUtilities.bytesToFile(data, fn);
            profileMap.put(FileUtilities.changeFileExt(fn, ""), profile.getVersionedUrl());
          }
        }
      }
    } catch (Exception e) {
      log.error("Error running test factory '"+getName()+"': "+e.getMessage());
      log("Error running test case '"+getName()+"': "+e.getMessage());
      e.printStackTrace(testLog);
      throw new FHIRException(e);
    }
  }

  private void checkDownloadBaseData() throws IOException {
    localData = ManagedFileAccess.file(Utilities.path("[tmp]", "fhir-test-data.db"));  
    File localInfo = ManagedFileAccess.file(Utilities.path("[tmp]", "fhir-test-data.json"));  
    try {
      JsonObject local = localInfo.exists() ? JsonParser.parseObject(localInfo) : null; 
      JsonObject json = JsonParser.parseObjectFromUrl("http://fhir.org/downloads/test-data-versions.json");
      JsonObject current = json.forceArray("versions").get(0).asJsonObject();
      if (current == null) {
        throw new FHIRException("No current information about FHIR downloads");
      }
      String date = current.asString("date");
      if (date == null) {
        throw new FHIRException("No date on current information about FHIR downloads");
      }
      String filename = current.asString("filename");
      if (filename == null) {
        throw new FHIRException("No filename on current information about FHIR downloads");
      }
      if (local == null || !date.equals(local.asString("date"))) {
        HTTPResult data = ManagedWebAccess.get(Utilities.strings("general"), "http://fhir.org/downloads/"+filename);
        FileUtilities.bytesToFile(data.getContent(), localData);
        local = new JsonObject();
        local.set("date", date);
        JsonParser.compose(current, localInfo, true);
      }
    } catch (Exception e) {
      if (!localData.exists()) {
        log("Unable to download copy of FHIR testing data: "+ e.getMessage());
        throw new FHIRException("Unable to download copy of FHIR testing data", e);
      }
    }
  }

  private byte[] runBundle(StructureDefinition profile, ProfileBasedFactory factory, TableDataProvider tbl) throws IOException, FHIRException, SQLException {
    Element bundle = Manager.parse(context, bundleShell(), FhirFormat.JSON).get(0).getElement();
    bundle.makeElement("id").setValue(UUID.randomUUID().toString().toLowerCase());
    
    while (tbl.nextRow()) {
      if (rowPasses(factory)) {
        Element resource = factory.generate(profile);
        Element be = bundle.makeElement("entry");
        be.makeElement("fullUrl").setValue(Utilities.pathURL(canonical, "test", resource.fhirType(), resource.getIdBase()));
        be.makeElement("resource").getChildren().addAll(resource.getChildren());
      }
    }
    log("Saving Bundle");
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    Manager.compose(context, bundle, bs, format, OutputStyle.PRETTY, null);
    return bs.toByteArray();
  }

  private boolean rowPasses(ProfileBasedFactory factory) throws IOException {
    if (details.has("filter")) {
      List<String> ls = new ArrayList<String>();
      String res = factory.evaluateExpression(ls, details.get("filter"), "filter");
      for (String l : ls) {
        log(l);
      }
      return  Utilities.existsInList(res, "1", "true");
    } else {
      return true;
    }
  }

  private TableDataProvider loadTable(String path) throws IOException, InvalidFormatException {
    log("Load Data From "+path);
    return loadTableProvider(path, locale);
  }

  private void error(String msg) throws IOException {
    log(msg);
    testLog.close();
    throw new FHIRException(msg);
  }

  private void log(String msg) throws IOException {
    testLog.append(msg+"\r\n");
  }

  public void executeLiquid() throws IOException {
    try {
      LiquidDocument template = liquid.parse(FileUtilities.fileToString(Utilities.path(rootFolder, details.asString( "liquid"))), "liquid");
      log("liquid compiled");
      DataTable dt = loadData(Utilities.path(rootFolder, details.asString( "data")));
      Map<String, DataTable> tables = new HashMap<>();
      liquid.getVars().clear();
      if (details.has("tables")) {
        JsonObject tablesJ = details.getJsonObject("tables");
        for (String n : tablesJ.getNames()) {
          DataTable v = loadData(Utilities.path(rootFolder, tablesJ.asString(n)));
          liquid.getVars().put(n, v);
          tables.put(n, v);
        } 
      }

      logDataScheme(dt, tables);
      
      logStrings("columns", dt.columns);
      if ("true".equals(details.asString( "bundle"))) {
        byte[] data = runBundle(template, dt);
        FileUtilities.bytesToFile(data, Utilities.path(rootFolder, details.asString( "filename")));
      } else {
        for (List<String> row : dt.rows) { 
          byte[] data = runInstance(template, dt.columns, row);
          FileUtilities.bytesToFile(data, Utilities.path(rootFolder, getFileName(details.asString( "filename"), dt.columns, row)));
        }
      }
    } catch (Exception e) {
      log.error("Error running test factory '"+getName()+"': "+e.getMessage());
      log("Error running test case '"+getName()+"': "+e.getMessage());
      e.printStackTrace(testLog);
      throw new FHIRException(e);
    }
  }

  private void logStrings(String name, List<String> columns) throws IOException {
    log(name+": "+CommaSeparatedStringBuilder.join(", ", columns));    
  }

  private String getFileName(String name, List<String> columns, List<String> values) {
    for (int i = 0; i < columns.size(); i++) {
      name = name.replace("$"+columns.get(i)+"$", values.get(i));
    }
    return name;
  }

  private byte[] runInstance(LiquidDocument template, List<String> columns, List<String> row) throws JsonException, IOException {
    logStrings("row", row);
    BaseTableWrapper base = BaseTableWrapper.forRow(columns, row);
    String cnt = liquid.evaluate(template, base, this).trim();
    if (format == FhirFormat.JSON) {
      JsonObject j = JsonParser.parseObject(cnt, true);
      return JsonParser.composeBytes(j, true);
    } else {
      return FileUtilities.stringToBytes(cnt);
    }
  }

  private byte[] runBundle(LiquidDocument template, DataTable dt) throws JsonException, IOException {
    Element bundle = Manager.parse(context, bundleShell(), FhirFormat.JSON).get(0).getElement();
    bundle.makeElement("id").setValue(UUID.randomUUID().toString().toLowerCase());
    
    for (List<String> row : dt.rows) { 
      byte[] data = runInstance(template, dt.columns, row);
      Element resource = Manager.parse(context, new ByteArrayInputStream(data), format).get(0).getElement();
      Element be = bundle.makeElement("entry");
      be.makeElement("fullUrl").setValue(Utilities.pathURL(canonical, "test", resource.fhirType(), resource.getIdBase()));
      be.makeElement("resource").getChildren().addAll(resource.getChildren());
    }
    log("Saving Bundle");
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    Manager.compose(context, bundle, bs, format, OutputStyle.PRETTY, null);
    return bs.toByteArray();
  }

  private InputStream bundleShell() throws IOException {
    String bundle = "{\"resourceType\" : \"Bundle\", \"type\" : \"collection\"}";
    return new ByteArrayInputStream(FileUtilities.stringToBytes(bundle));
  }

  private DataTable loadData(String path) throws FHIRException, IOException, InvalidFormatException {
    log("Load Data From "+path);
    TableDataProvider tbl = loadTableProvider(path, locale);

    DataTable dt = new DataTable();
    for (String n : tbl.columns()) {
      dt.columns.add(n);
    }
    int t = dt.columns.size();
    while (tbl.nextRow()) {
      List<String> values = new ArrayList<String>();
      for (String b : tbl.cells()) {
        values.add(b);
      }
      while (values.size() < t) {
        values.add("");
      }
      while (values.size() > t) {
        values.remove(values.size()-1);
      }
      dt.rows.add(values);
    }
    return dt;
  }

  public TableDataProvider loadTableProvider(String path, Locale locale) {
    TableDataProvider tbl;
    if (Utilities.isAbsoluteUrl(path)) {
      ValueSet vs = context.findTxResource(ValueSet.class, path);
      if (vs == null) {
        throw new FHIRException("ValueSet "+path+" not found");
      } else {
        org.hl7.fhir.r5.terminologies.expansion.ValueSetExpansionOutcome exp = context.expandVS(vs, true, false);
        if (exp.isOk()) {
          tbl = new ValueSetDataProvider(exp.getValueset().getExpansion());
        } else {
          throw new FHIRException("ValueSet "+path+" coult not be expanded: "+exp.getError());
        }
      }
    } else {
      tbl = TableDataProvider.forFile(path, locale);
    }
    return tbl;
  }

  public String statedLog() {
    return name+".log";
  }

  public boolean isTesting() {
    return testing;
  }

  public void setTesting(boolean testing) {
    this.testing = testing;
  }
  
  
}
