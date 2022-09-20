package org.hl7.fhir.r5.elementmodel;

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



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r5.conformance.ProfileUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element.SpecialElement;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.formats.JsonCreator;
import org.hl7.fhir.r5.formats.JsonCreatorCanonical;
import org.hl7.fhir.r5.formats.JsonCreatorGson;
import org.hl7.fhir.r5.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.utils.FHIRPathEngine;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.i18n.I18nConstants;
import org.hl7.fhir.utilities.json.JsonTrackingParser;
import org.hl7.fhir.utilities.json.JsonTrackingParser.LocationData;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonParser extends ParserBase {

  private JsonCreator json;
  private Map<JsonElement, LocationData> map;
  private boolean allowComments;

  private ProfileUtilities profileUtilities;

  public JsonParser(IWorkerContext context, ProfileUtilities utilities) {
    super(context);

    this.profileUtilities = utilities;
  }

  public JsonParser(IWorkerContext context) {
    super(context);

    this.profileUtilities = new ProfileUtilities(this.context, null, null, new FHIRPathEngine(context));
  }

  // MATCHBOX Fixed for https://github.com/ahdis/matchbox/issues/31
  @Override
  protected StructureDefinition getDefinition(int line, int col, String name) throws FHIRFormatError {
    // orginal
//    if (name == null) {
//      logError(line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_CANNOT_BE_PARSED_AS_A_FHIR_OBJECT_NO_NAME), IssueSeverity.FATAL);
//      return null;
//    }
//    // first pass: only look at base definitions
//    for (StructureDefinition sd : context.getStructures()) {
//      if (sd.getUrl().equals("http://hl7.org/fhir/StructureDefinition/"+name)) {
//        context.generateSnapshot(sd); 
//        return sd;
//      }
//    }
//    for (StructureDefinition sd : context.getStructures()) {
//      if (name.equals(sd.getType()) && sd.getDerivation() == TypeDerivationRule.SPECIALIZATION) {
//        context.generateSnapshot(sd); 
//        return sd;
//      }
//    }
//    logError(line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_DOES_NOT_APPEAR_TO_BE_A_FHIR_RESOURCE_UNKNOWN_NAME_, name), IssueSeverity.FATAL);
//    return null;
    if (name == null) {
      logError(line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_CANNOT_BE_PARSED_AS_A_FHIR_OBJECT_NO_NAME), IssueSeverity.FATAL);
      return null;
    }
    StructureDefinition sd = context.fetchTypeDefinition(name);
    if (sd!=null) {
      return sd;
    }
    logError(line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_DOES_NOT_APPEAR_TO_BE_A_FHIR_RESOURCE_UNKNOWN_NAMESPACENAME_, "", name), IssueSeverity.FATAL);
    return null;

  }


  public Element parse(String source, String type) throws Exception {
    JsonObject obj = (JsonObject) new com.google.gson.JsonParser().parse(source);
    String path = "/"+type;
    StructureDefinition sd = getDefinition(-1, -1, type);
    if (sd == null)
      return null;

    Element result = new Element(type, new Property(context, sd.getSnapshot().getElement().get(0), sd, this.profileUtilities));
    result.setPath(type);
    checkObject(obj, path);
    result.setType(type);
    parseChildren(path, obj, result, true);
    result.numberChildren();
    return result;
  }


  @Override
  public List<NamedElement> parse(InputStream stream) throws IOException, FHIRException {
    // if we're parsing at this point, then we're going to use the custom parser
    List<NamedElement> res = new ArrayList<>();
    map = new IdentityHashMap<JsonElement, LocationData>();
    String source = TextFile.streamToString(stream);
    if (policy == ValidationPolicy.EVERYTHING) {
      JsonObject obj = null;
      try {
        obj = JsonTrackingParser.parse(source, map, false, allowComments);
      } catch (Exception e) {
        logError(-1, -1,context.formatMessage(I18nConstants.DOCUMENT), IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_JSON_, e.getMessage()), IssueSeverity.FATAL);
        return null;
      }
      assert (map.containsKey(obj));
      Element e = parse(obj);
      if (e != null) {
        res.add(new NamedElement(null, e));
      }
    } else {
      JsonObject obj = JsonTrackingParser.parse(source, null); // (JsonObject) new com.google.gson.JsonParser().parse(source);
      //			assert (map.containsKey(obj));
      Element e = parse(obj);
      if (e != null) {
        res.add(new NamedElement(null, e));
      }
    }
    return res;
  }

  public Element parse(JsonObject object, Map<JsonElement, LocationData> map) throws FHIRException {
    this.map = map;
    return parse(object);
  }

  public Element parse(JsonObject object) throws FHIRException {
    JsonElement rt = object.get("resourceType");
    if (rt == null) {
      logError(line(object), col(object), "$", IssueType.INVALID, context.formatMessage(I18nConstants.UNABLE_TO_FIND_RESOURCETYPE_PROPERTY), IssueSeverity.FATAL);
      return null;
    } else {
      String name = rt.getAsString();
      String path = name;

      StructureDefinition sd = getDefinition(line(object), col(object), name);
      if (sd == null)
        return null;

      Element result = new Element(name, new Property(context, sd.getSnapshot().getElement().get(0), sd, this.profileUtilities));
      checkObject(object, path);
      result.markLocation(line(object), col(object));
      result.setType(name);
      result.setPath(result.fhirType());
      parseChildren(path, object, result, true);
      result.numberChildren();
      return result;
    }
  }

  private void checkObject(JsonObject object, String path) throws FHIRFormatError {
    if (policy == ValidationPolicy.EVERYTHING) {
      boolean found = false;
      for (Entry<String, JsonElement> e : object.entrySet()) {
        //    		if (!e.getKey().equals("fhir_comments")) {
        found = true;
        break;
        //    		}
      }
      if (!found)
        logError(line(object), col(object), path, IssueType.INVALID, context.formatMessage(I18nConstants.OBJECT_MUST_HAVE_SOME_CONTENT), IssueSeverity.ERROR);
    }
  }

  private void parseChildren(String path, JsonObject object, Element element, boolean hasResourceType) throws FHIRException {
    reapComments(object, element);
    List<Property> properties = element.getProperty().getChildProperties(element.getName(), null);
    Set<String> processed = new HashSet<String>();
    if (hasResourceType)
      processed.add("resourceType");

    // note that we do not trouble ourselves to maintain the wire format order here - we don't even know what it was anyway
    // first pass: process the properties
    for (Property property : properties) {
      parseChildItem(path, object, element, processed, property);
    }

    // second pass: check for things not processed
    if (policy != ValidationPolicy.NONE) {
      for (Entry<String, JsonElement> e : object.entrySet()) {
        if (!processed.contains(e.getKey())) {
          logError(line(e.getValue()), col(e.getValue()), path, IssueType.STRUCTURE, context.formatMessage(I18nConstants.UNRECOGNISED_PROPERTY_, e.getKey()), IssueSeverity.ERROR);
        }
      }
    }
  }

  public void parseChildItem(String path, JsonObject object, Element context, Set<String> processed, Property property) {
    if (property.isChoice() || property.getDefinition().getPath().endsWith("data[x]")) {
      for (TypeRefComponent type : property.getDefinition().getType()) {
        String eName = property.getName().substring(0, property.getName().length()-3) + Utilities.capitalize(type.getWorkingCode());
        if (!isPrimitive(type.getWorkingCode()) && object.has(eName)) {
          parseChildComplex(path, object, context, processed, property, eName);
          break;
        } else if (isPrimitive(type.getWorkingCode()) && (object.has(eName) || object.has("_"+eName))) {
          parseChildPrimitive(object, context, processed, property, path, eName);
          break;
        }
      }
    } else if (property.isPrimitive(property.getType(null))) {
      parseChildPrimitive(object, context, processed, property, path, property.getName());
    } else if (object.has(property.getName())) {
      parseChildComplex(path, object, context, processed, property, property.getName());
    }
  }

  private void parseChildComplex(String path, JsonObject object, Element element, Set<String> processed, Property property, String name) throws FHIRException {
    processed.add(name);
    String npath = path+"."+property.getName();
    String fpath = element.getPath()+"."+property.getName();
    JsonElement e = object.get(name);
    if (property.isList() && (e instanceof JsonArray)) {
      JsonArray arr = (JsonArray) e;
      if (arr.size() == 0) {
        logError(line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ARRAY_CANNOT_BE_EMPTY), IssueSeverity.ERROR);			  
      }
      int c = 0;
      for (JsonElement am : arr) {
        parseChildComplexInstance(npath+"["+c+"]", fpath+"["+c+"]", object, element, property, name, am);
        c++;
      }
    } else {
      if (property.isList()) {
        logError(line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE_AN_ARRAY_NOT_, describeType(e), name, path), IssueSeverity.ERROR);
      }
      parseChildComplexInstance(npath, fpath, object, element, property, name, e);
    }
  }

  private String describeType(JsonElement e) {
    if (e.isJsonArray())
      return "an Array";
    if (e.isJsonObject())
      return "an Object";
    if (e.isJsonPrimitive())
      return "a primitive property";
    if (e.isJsonNull())
      return "a Null";
    return null;
  }

  private void parseChildComplexInstance(String npath, String fpath, JsonObject object, Element element, Property property, String name, JsonElement e) throws FHIRException {
    if (e instanceof JsonObject) {
      JsonObject child = (JsonObject) e;
      Element n = new Element(name, property).markLocation(line(child), col(child));
      n.setPath(fpath);
      checkObject(child, npath);
      element.getChildren().add(n);
      if (property.isResource())
        parseResource(npath, child, n, property);
      else
        parseChildren(npath, child, n, false);
    } else
      logError(line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE__NOT_, (property.isList() ? "an Array" : "an Object"), describe(e), name, npath), IssueSeverity.ERROR);
  }

  private String describe(JsonElement e) {
    if (e instanceof JsonArray) {
      return "an array";
    }
    if (e instanceof JsonObject) {
      return "an object";
    }
    if (e instanceof JsonNull) {
      return "null";
    }
    return "a primitive property";
  }

  private void parseChildPrimitive(JsonObject object, Element element, Set<String> processed, Property property, String path, String name) throws FHIRException {
    String npath = path+"."+property.getName();
    String fpath = element.getPath()+"."+property.getName();
    processed.add(name);
    processed.add("_"+name);
    JsonElement main = object.has(name) ? object.get(name) : null;
    JsonElement fork = object.has("_"+name) ? object.get("_"+name) : null;
    if (main != null || fork != null) {
      if (property.isList()) {
        boolean ok = true;
        if (!(main == null || main instanceof JsonArray)) {
          logError(line(main), col(main), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE_AN_ARRAY_NOT_, describe(main), name, path), IssueSeverity.ERROR);
          ok = false;
        }
        if (!(fork == null || fork instanceof JsonArray)) {
          logError(line(fork), col(fork), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_BASE_PROPERTY_MUST_BE_AN_ARRAY_NOT_, describe(main), name, path), IssueSeverity.ERROR);
          ok = false;
        }
        if (ok) {
          JsonArray arr1 = (JsonArray) main;
          JsonArray arr2 = (JsonArray) fork;
          for (int i = 0; i < Math.max(arrC(arr1), arrC(arr2)); i++) {
            JsonElement m = arrI(arr1, i);
            JsonElement f = arrI(arr2, i);
            parseChildPrimitiveInstance(element, property, name, npath, fpath, m, f);
          }
        }
      } else {
        parseChildPrimitiveInstance(element, property, name, npath, fpath, main, fork);
      }
    }
  }

  private JsonElement arrI(JsonArray arr, int i) {
    return arr == null || i >= arr.size() || arr.get(i) instanceof JsonNull ? null : arr.get(i);
  }

  private int arrC(JsonArray arr) {
    return arr == null ? 0 : arr.size();
  }

  private void parseChildPrimitiveInstance(Element element, Property property, String name, String npath, String fpath, JsonElement main, JsonElement fork) throws FHIRException {
    if (main != null && !(main instanceof JsonPrimitive))
      logError(line(main), col(main), npath, IssueType.INVALID, context.formatMessage(
          I18nConstants.THIS_PROPERTY_MUST_BE_AN_SIMPLE_VALUE_NOT_, describe(main), name, npath), IssueSeverity.ERROR);
    else if (fork != null && !(fork instanceof JsonObject))
      logError(line(fork), col(fork), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE_AN_OBJECT_NOT_, describe(fork), name, npath), IssueSeverity.ERROR);
    else {
      Element n = new Element(name, property).markLocation(line(main != null ? main : fork), col(main != null ? main : fork));
      n.setPath(fpath);
      element.getChildren().add(n);
      if (main != null) {
        JsonPrimitive p = (JsonPrimitive) main;
        if (p.isNumber() && p.getAsNumber() instanceof JsonTrackingParser.PresentedBigDecimal) {
          String rawValue = ((JsonTrackingParser.PresentedBigDecimal) p.getAsNumber()).getPresentation();
          n.setValue(rawValue);
        } else {
          n.setValue(p.getAsString());
        }
        if (!n.getProperty().isChoice() && n.getType().equals("xhtml")) {
          try {
            n.setXhtml(new XhtmlParser().setValidatorMode(policy == ValidationPolicy.EVERYTHING).parse(n.getValue(), null).getDocumentElement());
          } catch (Exception e) {
            logError(line(main), col(main), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_XHTML_, e.getMessage()), IssueSeverity.ERROR);
          }
        }
        if (policy == ValidationPolicy.EVERYTHING) {
          // now we cross-check the primitive format against the stated type
          if (Utilities.existsInList(n.getType(), "boolean")) {
            if (!p.isBoolean())
              logError(line(main), col(main), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_JSON_THE_PRIMITIVE_VALUE_MUST_BE_A_BOOLEAN), IssueSeverity.ERROR);
          } else if (Utilities.existsInList(n.getType(), "integer", "unsignedInt", "positiveInt", "decimal")) {
            if (!p.isNumber())
              logError(line(main), col(main), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_JSON_THE_PRIMITIVE_VALUE_MUST_BE_A_NUMBER), IssueSeverity.ERROR);
          } else if (!p.isString())
            logError(line(main), col(main), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_JSON_THE_PRIMITIVE_VALUE_MUST_BE_A_STRING), IssueSeverity.ERROR);
        }
      }
      if (fork != null) {
        JsonObject child = (JsonObject) fork;
        checkObject(child, npath);
        parseChildren(npath, child, n, false);
      }
    }
  }


  private void parseResource(String npath, JsonObject res, Element parent, Property elementProperty) throws FHIRException {
    JsonElement rt = res.get("resourceType");
    if (rt == null) {
      logError(line(res), col(res), npath, IssueType.INVALID, context.formatMessage(I18nConstants.UNABLE_TO_FIND_RESOURCETYPE_PROPERTY), IssueSeverity.FATAL);
    } else {
      String name = rt.getAsString();
      StructureDefinition sd = context.fetchResource(StructureDefinition.class, ProfileUtilities.sdNs(name, context.getOverrideVersionNs()));
      if (sd == null) {
        logError(line(res), col(res), npath, IssueType.INVALID, context.formatMessage(I18nConstants.CONTAINED_RESOURCE_DOES_NOT_APPEAR_TO_BE_A_FHIR_RESOURCE_UNKNOWN_NAME_, name), IssueSeverity.FATAL);			    
      } else {
        parent.updateProperty(new Property(context, sd.getSnapshot().getElement().get(0), sd, this.profileUtilities), SpecialElement.fromProperty(parent.getProperty()), elementProperty);
        parent.setType(name);
        parseChildren(npath, res, parent, true);
      }
    }
  }

  private void reapComments(JsonObject object, Element context) {
    if (object.has("fhir_comments")) {
      JsonArray arr = object.getAsJsonArray("fhir_comments");
      for (JsonElement e : arr) {
        context.getComments().add(e.getAsString());
      }
    }
  }

  private int line(JsonElement e) {
    if (map == null|| !map.containsKey(e))
      return -1;
    else
      return map.get(e).getLine();
  }

  private int col(JsonElement e) {
    if (map == null|| !map.containsKey(e))
      return -1;
    else
      return map.get(e).getCol();
  }


  protected void prop(String name, String value, String link) throws IOException {
    json.link(link);
    if (name != null)
      json.name(name);
    json.value(value);
  }

  protected void open(String name, String link) throws IOException {
    json.link(link);
    if (name != null)
      json.name(name);
    json.beginObject();
  }

  protected void close() throws IOException {
    json.endObject();
  }

  protected void openArray(String name, String link) throws IOException {
    json.link(link);
    if (name != null)
      json.name(name);
    json.beginArray();
  }

  protected void closeArray() throws IOException {
    json.endArray();
  }


  @Override
  public void compose(Element e, OutputStream stream, OutputStyle style, String identity) throws FHIRException, IOException {
    OutputStreamWriter osw = new OutputStreamWriter(stream, "UTF-8");
    if (style == OutputStyle.CANONICAL)
      json = new JsonCreatorCanonical(osw);
    else
      json = new JsonCreatorGson(osw);
    json.setIndent(style == OutputStyle.PRETTY ? "  " : "");
    json.beginObject();
    prop("resourceType", e.getType(), null);
    Set<String> done = new HashSet<String>();
    for (Element child : e.getChildren()) {
      compose(e.getName(), e, done, child);
    }
    json.endObject();
    json.finish();
    osw.flush();
  }

  public void compose(Element e, JsonCreator json) throws Exception {
    this.json = json;
    json.beginObject();

    prop("resourceType", e.getType(), linkResolver == null ? null : linkResolver.resolveProperty(e.getProperty()));
    Set<String> done = new HashSet<String>();
    for (Element child : e.getChildren()) {
      compose(e.getName(), e, done, child);
    }
    json.endObject();
    json.finish();
  }

  private void compose(String path, Element e, Set<String> done, Element child) throws IOException {
    if (wantCompose(path, child)) {
      boolean isList = child.hasElementProperty() ? child.getElementProperty().isList() : child.getProperty().isList();
      if (!isList) {// for specials, ignore the cardinality of the stated type
        compose(path, child);
      } else if (!done.contains(child.getName())) {
        done.add(child.getName());
        List<Element> list = e.getChildrenByName(child.getName());
        composeList(path, list);
      }
    }
  }


  private void composeList(String path, List<Element> list) throws IOException {
    // there will be at least one element
    String name = list.get(0).getName();
    boolean complex = true;
    if (list.get(0).isPrimitive()) {
      boolean prim = false;
      complex = false;
      for (Element item : list) {
        if (item.hasValue())
          prim = true;
        if (item.hasChildren())
          complex = true;
      }
      if (prim) {
        openArray(name, linkResolver == null ? null : linkResolver.resolveProperty(list.get(0).getProperty()));
        for (Element item : list) {
          if (item.hasValue())
            primitiveValue(null, item);
          else
            json.nullValue();
        }
        closeArray();
      }
      name = "_"+name;
    }
    if (complex) {
      openArray(name, linkResolver == null ? null : linkResolver.resolveProperty(list.get(0).getProperty()));
      for (Element item : list) {
        if (item.hasChildren()) {
          open(null,null);
          if (item.getProperty().isResource()) {
            prop("resourceType", item.getType(), linkResolver == null ? null : linkResolver.resolveType(item.getType()));
          }
          Set<String> done = new HashSet<String>();
          for (Element child : item.getChildren()) {
            compose(path+"."+name+"[]", item, done, child);
          }
          close();
        } else
          json.nullValue();
      }
      closeArray();
    }
  }

  private void primitiveValue(String name, Element item) throws IOException {
    if (name != null) {
      if (linkResolver != null)
        json.link(linkResolver.resolveProperty(item.getProperty()));
      json.name(name);
    }
    String type = item.getType();
    if (Utilities.existsInList(type, "boolean"))
      json.value(item.getValue().trim().equals("true") ? new Boolean(true) : new Boolean(false));
    else if (Utilities.existsInList(type, "integer", "unsignedInt", "positiveInt"))
      json.value(new Integer(item.getValue()));
    else if (Utilities.existsInList(type, "decimal"))
      try {
        json.value(new BigDecimal(item.getValue()));
      } catch (Exception e) {
        throw new NumberFormatException(context.formatMessage(I18nConstants.ERROR_WRITING_NUMBER__TO_JSON, item.getValue()));
      }
    else
      json.value(item.getValue());
  }

  private void compose(String path, Element element) throws IOException {
    String name = element.getName();
    if (element.isPrimitive() || isPrimitive(element.getType())) {
      if (element.hasValue())
        primitiveValue(name, element);
      name = "_"+name;
      if (element.getType().equals("xhtml"))
        json.anchor("end-xhtml");
    }
    if (element.hasChildren()) {
      open(name, linkResolver == null ? null : linkResolver.resolveProperty(element.getProperty()));
      if (element.getProperty().isResource()) {
        prop("resourceType", element.getType(), linkResolver == null ? null : linkResolver.resolveType(element.getType()));
      }
      Set<String> done = new HashSet<String>();
      for (Element child : element.getChildren()) {
        compose(path+"."+element.getName(), element, done, child);
      }
      close();
    }
  }

  public boolean isAllowComments() {
    return allowComments;
  }

  public JsonParser setAllowComments(boolean allowComments) {
    this.allowComments = allowComments;
    return this;
  }


}