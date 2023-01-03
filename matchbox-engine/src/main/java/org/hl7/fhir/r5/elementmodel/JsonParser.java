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
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r5.conformance.ProfileUtilities;
import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element.SpecialElement;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.formats.JsonCreator;
import org.hl7.fhir.r5.formats.JsonCreatorCanonical;
import org.hl7.fhir.r5.formats.JsonCreatorDirect;
import org.hl7.fhir.r5.formats.JsonCreatorGson;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.utils.FHIRPathEngine;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.CommaSeparatedStringBuilder;
import org.hl7.fhir.utilities.StringPair;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.i18n.I18nConstants;
import org.hl7.fhir.utilities.json.model.JsonArray;
import org.hl7.fhir.utilities.json.model.JsonComment;
import org.hl7.fhir.utilities.json.model.JsonElement;
import org.hl7.fhir.utilities.json.model.JsonNull;
import org.hl7.fhir.utilities.json.model.JsonObject;
import org.hl7.fhir.utilities.json.model.JsonPrimitive;
import org.hl7.fhir.utilities.json.model.JsonProperty;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;


public class JsonParser extends ParserBase {

  private JsonCreator json;
  private boolean allowComments;

  private ProfileUtilities profileUtilities;
  private Element baseElement;

  public JsonParser(IWorkerContext context, ProfileUtilities utilities) {
    super(context);

    this.profileUtilities = utilities;
  }

  public JsonParser(IWorkerContext context) {
    super(context);

    this.profileUtilities = new ProfileUtilities(this.context, null, null, new FHIRPathEngine(context));
  }

  // MATCHBOX Fixed for https://github.com/ahdis/matchbox/issues/31
//  @Override
//  protected StructureDefinition getDefinition(int line, int col, String name) throws FHIRFormatError {
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
//    if (name == null) {
//      logError(line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_CANNOT_BE_PARSED_AS_A_FHIR_OBJECT_NO_NAME), IssueSeverity.FATAL);
//      return null;
//    }
//    StructureDefinition sd = context.fetchTypeDefinition(name);
//    if (sd!=null) {
//      return sd;
//    }
//    logError(line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_DOES_NOT_APPEAR_TO_BE_A_FHIR_RESOURCE_UNKNOWN_NAMESPACENAME_, "", name), IssueSeverity.FATAL);
//    return null;

//  }


  public Element parse(String source, String type) throws Exception {
    JsonObject obj = org.hl7.fhir.utilities.json.parser.JsonParser.parseObject(source, true, true); 
    String path = "/"+type;
    StructureDefinition sd = getDefinition(-1, -1, type);
    if (sd == null)
      return null;

    Element result = new Element(type, new Property(context, sd.getSnapshot().getElement().get(0), sd, this.profileUtilities));
    result.setPath(type);
    checkObject(obj, result, path);
    result.setType(type);
    parseChildren(path, obj, result, true);
    result.numberChildren();
    return result;
  }


  @Override
  public List<NamedElement> parse(InputStream stream) throws IOException, FHIRException {
    // if we're parsing at this point, then we're going to use the custom parser
    List<NamedElement> res = new ArrayList<>();
    String source = TextFile.streamToString(stream);
    JsonObject obj = null;
    if (policy == ValidationPolicy.EVERYTHING) {
      try {
        obj = org.hl7.fhir.utilities.json.parser.JsonParser.parseObject(source, true, true); 
      } catch (Exception e) {
        logError(ValidationMessage.NO_RULE_DATE, -1, -1,context.formatMessage(I18nConstants.DOCUMENT), IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_JSON_, e.getMessage()), IssueSeverity.FATAL);
        return null;
      }
    } else {
      obj = org.hl7.fhir.utilities.json.parser.JsonParser.parseObject(source, true, true); 
    }
    Element e = parse(obj);
    if (e != null) {
      res.add(new NamedElement(null, e));
    }
    return res;
  }

  public Element parse(JsonObject object) throws FHIRException {
    StructureDefinition sd = getLogical();
    String name;
    String path;      
    if (sd == null) {
      JsonElement rt = object.get("resourceType");
      if (rt == null) {
        logError(ValidationMessage.NO_RULE_DATE, line(object), col(object), "$", IssueType.INVALID, context.formatMessage(I18nConstants.UNABLE_TO_FIND_RESOURCETYPE_PROPERTY), IssueSeverity.FATAL);
        return null;
      } else if (!rt.isJsonString()) {
        logError("2022-11-26", line(object), col(object), "$", IssueType.INVALID, context.formatMessage(I18nConstants.RESOURCETYPE_PROPERTY_WRONG_TYPE, rt.type().toName()), IssueSeverity.FATAL);
        return null;
      } else {
        name = rt.asString();

        sd = getDefinition(line(object), col(object), name);
        if (sd == null) {
         return null;
        }
      }
      path = name;
    } else {
      name = sd.getType();
      path = sd.getTypeTail();
    }
    baseElement = new Element(name, new Property(context, sd.getSnapshot().getElement().get(0), sd, this.profileUtilities));
    checkObject(object, baseElement, path);
    baseElement.markLocation(line(object), col(object));
    baseElement.setType(name);
    baseElement.setPath(baseElement.fhirTypeRoot());
    parseChildren(path, object, baseElement, true);
    baseElement.numberChildren();
    return baseElement;
  }

  private void checkObject(JsonObject object, Element b, String path) {
    checkComments(object, b, path);
    if (policy == ValidationPolicy.EVERYTHING) {
      if (object.getProperties().size() == 0) {
        logError(ValidationMessage.NO_RULE_DATE, line(object), col(object), path, IssueType.INVALID, context.formatMessage(I18nConstants.OBJECT_MUST_HAVE_SOME_CONTENT), IssueSeverity.ERROR);
      }
    }    
  }

  private void checkComments(JsonElement element, Element b, String path) throws FHIRFormatError {
    if (element != null && element.hasComments()) {
      if (allowComments) {
        for (JsonComment c : element.getComments()) {
          b.getComments().add(c.getContent());
        }
      } else {
        for (JsonComment c : element.getComments()) {
          logError("2022-11-26", c.getStart().getLine(), c.getStart().getCol(), path, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_COMMENTS_NOT_ALLOWED), IssueSeverity.ERROR);
        }        
      }
    }
  }

  private void parseChildren(String path, JsonObject object, Element element, boolean hasResourceType) throws FHIRException {
    reapComments(object, element);
    List<Property> properties = element.getProperty().getChildProperties(element.getName(), null);
    Set<String> processed = new HashSet<String>();
    if (hasResourceType) {
      processed.add("resourceType");
    }
    Map<String, JsonProperty> recognisedChildren = new HashMap<>();
    Set<String> unique = new HashSet<>();
    for (JsonProperty p : object.getProperties()) {
      if (p.isUnquotedName()) {
        logError("2022-11-26", line(p.getValue()), col(p.getValue()), path, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_PROPERTY_NO_QUOTES, p.getName()), IssueSeverity.ERROR);
      }
      if (p.isNoComma()) {
        logError("2022-11-26", line(p.getValue()), col(p.getValue()), path, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_COMMA_MISSING), IssueSeverity.ERROR);        
      }
      if (unique.contains(p.getName())) {
        logError("2022-11-26", line(p.getValue()), col(p.getValue()), path, IssueType.INVALID, context.formatMessage(I18nConstants.DUPLICATE_JSON_PROPERTY, p.getName()), IssueSeverity.ERROR);
      } else {
        unique.add(p.getName());
        recognisedChildren.put(p.getName(), p);        
      }
    }

    // note that we do not trouble ourselves to maintain the wire format order here - we don't even know what it was anyway
    // first pass: process the properties
    for (Property property : properties) {
      parseChildItem(path, recognisedChildren, element, processed, property);
    }

    // second pass: check for things not processed
    if (policy != ValidationPolicy.NONE) {
      for (Entry<String, JsonProperty> e : recognisedChildren.entrySet()) {
        if (!processed.contains(e.getKey())) {
          StructureDefinition sd = element.getProperty().isLogical() ? new ContextUtilities(context).fetchByJsonName(e.getKey()) : null;
          if (sd != null) {
            Property property = new Property(context, sd.getSnapshot().getElementFirstRep(), sd, element.getProperty().getUtils());
            parseChildItem(path, recognisedChildren, element, null, property);
          } else {
            logError(ValidationMessage.NO_RULE_DATE, line(e.getValue().getValue()), col(e.getValue().getValue()), path, IssueType.STRUCTURE, context.formatMessage(I18nConstants.UNRECOGNISED_PROPERTY_, e.getKey()), IssueSeverity.ERROR);
          }
        }
      }
    }
    if (object.isExtraComma()) {
      logError("2022-11-26", object.getEnd().getLine(), object.getEnd().getCol(), path, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_COMMA_EXTRA, "Object"), IssueSeverity.ERROR);
    }

  }

  public void parseChildItem(String path, Map<String, JsonProperty> children, Element context, Set<String> processed, Property property) {
    if (property.isChoice() || property.getDefinition().getPath().endsWith("data[x]")) {
      if (property.isJsonPrimitiveChoice()) {
        if (children.containsKey(property.getJsonName())) {
          JsonElement je = children.get(property.getJsonName()).getValue();
          if (processed != null) processed.add(property.getJsonName());
          String type = getTypeFromJsonType(je);
          if (type == null) {
            logError(ValidationMessage.NO_RULE_DATE, line(je), col(je), path, IssueType.STRUCTURE, this.context.formatMessage(I18nConstants.UNRECOGNISED_PROPERTY_TYPE, describeType(je), property.getName(), property.typeSummary()), IssueSeverity.ERROR);
          } else if (property.hasType(type)) {
            Property np = new Property(property.getContext(), property.getDefinition(), property.getStructure(), property.getUtils(), type);
            parseChildPrimitive(children, context, processed, np, path, property.getName(), false);
          } else {
            logError(ValidationMessage.NO_RULE_DATE, line(je), col(je), path, IssueType.STRUCTURE, this.context.formatMessage(I18nConstants.UNRECOGNISED_PROPERTY_TYPE_WRONG, describeType(je), property.getName(), type, property.typeSummary()), IssueSeverity.ERROR);
          }
        }
      } else {
        for (TypeRefComponent type : property.getDefinition().getType()) {
          String eName = property.getJsonName().substring(0, property.getName().length()-3) + Utilities.capitalize(type.getWorkingCode());
          if (!isPrimitive(type.getWorkingCode()) && children.containsKey(eName)) {
            parseChildComplex(path, children, context, processed, property, eName, false);
            break;
          } else if (isPrimitive(type.getWorkingCode()) && (children.containsKey(eName) || children.containsKey("_"+eName))) {
            parseChildPrimitive(children, context, processed, property, path, eName, false);
            break;
          }
        }
      }
    } else if (property.isPrimitive(property.getType(null))) {
      parseChildPrimitive(children, context, processed, property, path, property.getJsonName(), property.hasJsonName());
    } else if (children.containsKey(property.getJsonName())) {
      parseChildComplex(path, children, context, processed, property, property.getJsonName(), property.hasJsonName());
    }
  }

  private String getTypeFromJsonType(JsonElement je) {
    if (je.isJsonPrimitive()) {
      JsonPrimitive p = je.asJsonPrimitive();
      if (p.isJsonString()) {
        return "string";
      } else if (p.isJsonBoolean()) {
        return "boolean";
      } else {
        String s = p.asString();
        if (Utilities.isInteger(s)) {
          return "integer";
        } else {
          return "decimal";
        }
      }
    } else {
      return null;
    }
  }

  private void parseChildComplex(String path, Map<String, JsonProperty> children, Element element, Set<String> processed, Property property, String name, boolean isJsonName) throws FHIRException {
    if (processed != null) {
      processed.add(name);
    }
    String npath = path+"."+property.getName();
    String fpath = element.getPath()+"."+property.getName();
    JsonProperty p = children.get(name);
    JsonElement e = p == null ? null : p.getValue();
    if (property.isList() && !property.isJsonKeyArray() && (e instanceof JsonArray)) {
      JsonArray arr = (JsonArray) e;
      if (arr.isExtraComma()) {
        logError("2022-11-26", arr.getEnd().getLine(), arr.getEnd().getCol(), npath, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_COMMA_EXTRA, "Array"), IssueSeverity.ERROR);
      }
      if (arr.size() == 0) {
        if (property.canBeEmpty()) {
          // nothing
        } else {
          logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ARRAY_CANNOT_BE_EMPTY), IssueSeverity.ERROR);
        }
      }
      int c = 0;
      for (JsonElement am : arr) {
        parseChildComplexInstance(npath+"["+c+"]", fpath+"["+c+"]", element, property, name, am, c == 0 ? arr : null, path);
        c++;
      }
    } else if (property.isJsonKeyArray()) {
      String code = property.getJsonKeyProperty();
      List<Property> properties = property.getChildProperties(element.getName(), null);
      if (properties.size() != 2) {
        logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.OBJECT_CANNOT_BE_KEYED_ARRAY_CHILD_COUNT, propNames(properties)), IssueSeverity.ERROR);               
      } else {
        Property propK = properties.get(0);
        Property propV = properties.get(1);
        if (!propK.getName().equals(code)) {
          logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.OBJECT_CANNOT_BE_KEYED_ARRAY_PROP_NAME, propNames(properties)), IssueSeverity.ERROR);                       
        } else if (!propK.isPrimitive())  {
          logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.OBJECT_CANNOT_BE_KEYED_ARRAY_PROP_TYPE, propNames(properties), propK.typeSummary()), IssueSeverity.ERROR);                       
        } else if (propV.isList())  {
          logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.OBJECT_CANNOT_BE_KEYED_ARRAY_NO_LIST, propV.getName()), IssueSeverity.ERROR);                       
        } else if (propV.isChoice() && propV.getName().endsWith("[x]"))  {
          logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.OBJECT_CANNOT_BE_KEYED_ARRAY_NO_CHOICE, propV.getName()), IssueSeverity.ERROR);                       
        } else if (!(e instanceof JsonObject)) {
          logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE_AN_OBJECT_NOT_, describe(e)), IssueSeverity.ERROR);                       
        } else {
          JsonObject o = (JsonObject) e;
          if (o.isExtraComma()) {
            logError("2022-11-26", o.getEnd().getLine(), o.getEnd().getCol(), npath, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_COMMA_EXTRA, "Object"), IssueSeverity.ERROR);
          }

          int i = 0;
          Set<String> names = new HashSet<>();
          for (JsonProperty pv : o.getProperties()) {
            if (names.contains(pv.getName())) {
              logError("2022-11-26", line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.DUPLICATE_JSON_PROPERTY_KEY, pv.getName()), IssueSeverity.ERROR);                                     
            } else {
              names.add(pv.getName());
            }
            // create an array entry
            String npathArr = path+"."+property.getName()+"["+i+"]";
            String fpathArr = element.getPath()+"."+property.getName()+"["+i+"]";
            
            Element n = new Element(name, property).markLocation(line(pv.getValue()), col(pv.getValue()));
            n.setPath(fpath);
            element.getChildren().add(n);
            // handle the key
            String fpathKey = fpathArr+"."+propK.getName();
            Element nKey = new Element(code, propK).markLocation(line(pv.getValue()), col(pv.getValue()));
            checkComments(pv.getValue(), n, fpathArr);
            nKey.setPath(fpathKey);
            n.getChildren().add(nKey);
            nKey.setValue(pv.getName());
            

            boolean ok = true;
            Property pvl = propV;
            if (propV.isJsonPrimitiveChoice()) {
              ok = false;
              String type = getTypeFromJsonType(pv.getValue());
              if (type == null) {
                logError(ValidationMessage.NO_RULE_DATE, line(pv.getValue()), col(pv.getValue()), path, IssueType.STRUCTURE, this.context.formatMessage(I18nConstants.UNRECOGNISED_PROPERTY_TYPE, describeType(pv.getValue()), propV.getName(), propV.typeSummary()), IssueSeverity.ERROR);
              } else if (propV.hasType(type)) {
                pvl = new Property(propV.getContext(), propV.getDefinition(), propV.getStructure(), propV.getUtils(), type);
                ok = true;
              } else {
                logError(ValidationMessage.NO_RULE_DATE, line(pv.getValue()), col(pv.getValue()), path, IssueType.STRUCTURE, this.context.formatMessage(I18nConstants.UNRECOGNISED_PROPERTY_TYPE_WRONG, describeType(pv.getValue()), propV.getName(), type, propV.typeSummary()), IssueSeverity.ERROR);
              }
            }
            if (ok) {
              // handle the value
              String npathV = npathArr+"."+pvl.getName();
              String fpathV = fpathArr+"."+pvl.getName();
              if (propV.isPrimitive(pvl.getType(null))) {
                parseChildPrimitiveInstance(n, pvl, pvl.getName(), false, npathV, fpathV, pv.getValue(), null);
              } else if (pv.getValue() instanceof JsonObject || pv.getValue() instanceof JsonNull) {
                parseChildComplexInstance(npathV, fpathV, n, pvl, pvl.getName(), pv.getValue(), null, null);
              } else {
                logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE_AN_OBJECT_NOT_, describe(pv.getValue())), IssueSeverity.ERROR);                       
              }
            }
            i++;
          }
        }
      }
    } else {
      if (property.isList()) {
        logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE_AN_ARRAY_NOT_, describe(e), name, path), IssueSeverity.ERROR);
      }
      parseChildComplexInstance(npath, fpath, element, property, name, e, null, null);
    }
  }

  private Object propNames(List<Property> properties) {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    for (Property p: properties) {
      b.append(p.getName());
    }
    return b.toString();
  }

  private void parseChildComplexInstance(String npath, String fpath, Element element, Property property, String name, JsonElement e, JsonElement commentContext, String commentPath) throws FHIRException {
    if (property.hasTypeSpecifier()) {
      FHIRPathEngine fpe = new FHIRPathEngine(context);
      String type = null;
      String cond = null;
      for (StringPair sp : property.getTypeSpecifiers()) {
        if (fpe.evaluateToBoolean(null, baseElement, baseElement, element, fpe.parse(sp.getName()))) {
          type = sp.getValue();
          cond = sp.getName();
          break;
        }
      }
      if (type != null) {
        StructureDefinition sd = context.fetchResource(StructureDefinition.class, type);
        if (sd == null) {
          logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.TYPE_SPECIFIER_ILLEGAL_TYPE, type, cond), IssueSeverity.ERROR);
        } else {
          if (sd.getAbstract()) {
            logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.TYPE_SPECIFIER_ABSTRACT_TYPE, type, cond), IssueSeverity.ERROR);
          }
          property = property.cloneToType(sd);
        }
      } else {
        StructureDefinition sd = context.fetchTypeDefinition(property.getType());
        if (sd == null) {
          logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.TYPE_SPECIFIER_NM_ILLEGAL_TYPE, property.getType()), IssueSeverity.ERROR);
        } else if (sd.getAbstract()) {
          logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.TYPE_SPECIFIER_NM_ABSTRACT_TYPE, property.getType()), IssueSeverity.ERROR);
        }        
      }
    }
    if (e instanceof JsonObject) {
      JsonObject child = (JsonObject) e;
      Element n = new Element(name, property).markLocation(line(child), col(child));
      n.setPath(fpath);
      checkComments(commentContext, n, commentPath);        
      checkObject(child, n, npath);
      element.getChildren().add(n);
      if (property.isResource()) {
        parseResource(npath, child, n, property);
      } else {
        parseChildren(npath, child, n, false);
      }
    } else if (property.isNullable() && e instanceof JsonNull) {
      // we create an element marked as a null element so we know something was present
      JsonNull child = (JsonNull) e;
      Element n = new Element(name, property).markLocation(line(child), col(child));
      checkComments(commentContext, n, commentPath);        
      checkComments(child, n, fpath);
      n.setPath(fpath);
      element.getChildren().add(n);
      n.setNull(true);
      // nothing to do, it's ok, but we treat it like it doesn't exist
    } else {
      logError(ValidationMessage.NO_RULE_DATE, line(e), col(e), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE__NOT_, (property.isList() ? "an Array" : "an Object"), describe(e), name, npath), IssueSeverity.ERROR);
    }
  }

  private String describe(JsonElement e) {
    if (e instanceof JsonArray) {
      return "an Array";
    }
    if (e instanceof JsonObject) {
      return "an Object";
    }
    if (e instanceof JsonNull) {
      return "a Null";
    }
    if (e instanceof JsonPrimitive) {
      return "a Primitive property";
    }
    return null;
  }

  private String describeType(JsonElement e) {
    return e.type().toName();
  }

  private void parseChildPrimitive(Map<String, JsonProperty> children, Element element, Set<String> processed, Property property, String path, String name, boolean isJsonName) throws FHIRException {
    String npath = path+"."+property.getName();
    String fpath = element.getPath()+"."+property.getName();
    processed.add(name);
    processed.add("_"+name);
    JsonProperty main = children.containsKey(name) ? children.get(name) : null;
    JsonProperty fork = children.containsKey("_"+name) ? children.get("_"+name) : null;
    if (main != null && main.getValue().isJsonString() && main.isUnquotedValue()) {
      logError("2022-11-26", line(main.getValue()), col(main.getValue()), path, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_PROPERTY_VALUE_NO_QUOTES, main.getName(), main.getValue().asString()), IssueSeverity.ERROR);
    }
    if (main != null || fork != null) {
      if (property.isList()) {
        boolean ok = true;
        if (!(main == null || main.getValue() instanceof JsonArray)) {
          logError(ValidationMessage.NO_RULE_DATE, line(main.getValue()), col(main.getValue()), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE_AN_ARRAY_NOT_, describe(main.getValue()), name, path), IssueSeverity.ERROR);
          ok = false;
        }
        if (!(fork == null || fork.getValue() instanceof JsonArray)) {
          logError(ValidationMessage.NO_RULE_DATE, line(fork.getValue()), col(fork.getValue()), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_BASE_PROPERTY_MUST_BE_AN_ARRAY_NOT_, describe(main.getValue()), name, path), IssueSeverity.ERROR);
          ok = false;
        }
        if (ok) {
          JsonArray arr1 = (JsonArray) (main == null ? null : main.getValue());
          JsonArray arr2 = (JsonArray) (fork == null ? null : fork.getValue());
          if (arr1 != null && arr1.isExtraComma()) {
            logError("2022-11-26", arr1.getEnd().getLine(), arr1.getEnd().getCol(), npath, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_COMMA_EXTRA, "Array"), IssueSeverity.ERROR);
          }
          if (arr2 != null && arr2.isExtraComma()) {
            logError("2022-11-26", arr2.getEnd().getLine(), arr2.getEnd().getCol(), npath, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_COMMA_EXTRA, "Array"), IssueSeverity.ERROR);
          }

          for (int i = 0; i < Math.max(arrC(arr1), arrC(arr2)); i++) {
            JsonElement m = arrI(arr1, i);
            JsonElement f = arrI(arr2, i);
            if (m != null && m.isJsonString() && arr1.isUnquoted(i)) {
              logError("2022-11-26", line(m), col(m), path+"."+name+"["+i+"]", IssueType.INVALID, context.formatMessage(I18nConstants.JSON_PROPERTY_VALUE_NO_QUOTES, "item", m.asString()), IssueSeverity.ERROR);
            }
            parseChildPrimitiveInstance(element, property, name, isJsonName, npath, fpath, m, f);
          }
        }
      } else {
        parseChildPrimitiveInstance(element, property, name, isJsonName, npath, fpath, main == null ? null : main.getValue(), fork == null ? null : fork.getValue());
      }
    }
  }

  private JsonElement arrI(JsonArray arr, int i) {
    return arr == null || i >= arr.size() || arr.get(i) instanceof JsonNull ? null : arr.get(i);
  }

  private int arrC(JsonArray arr) {
    return arr == null ? 0 : arr.size();
  }

  private void parseChildPrimitiveInstance(Element element, Property property, String name, boolean isJsonName, String npath, String fpath, JsonElement main, JsonElement fork) throws FHIRException {
    if (main != null && !(main.isJsonBoolean() || main.isJsonNumber() || main.isJsonString())) {
      logError(ValidationMessage.NO_RULE_DATE, line(main), col(main), npath, IssueType.INVALID, context.formatMessage(
          I18nConstants.THIS_PROPERTY_MUST_BE_AN_SIMPLE_VALUE_NOT_, describe(main), name, npath), IssueSeverity.ERROR);
    } else if (fork != null && !(fork instanceof JsonObject)) {
      logError(ValidationMessage.NO_RULE_DATE, line(fork), col(fork), npath, IssueType.INVALID, context.formatMessage(I18nConstants.THIS_PROPERTY_MUST_BE_AN_OBJECT_NOT_, describe(fork), name, npath), IssueSeverity.ERROR);
    } else {
      Element n = new Element(isJsonName ? property.getName() : name, property).markLocation(line(main != null ? main : fork), col(main != null ? main : fork));
      if (main != null) {
        checkComments(main, n, npath);
      }
      if (fork != null) {
        checkComments(fork, n, npath);
      }
      n.setPath(fpath);
      element.getChildren().add(n);
      if (main != null) {
        JsonPrimitive p = (JsonPrimitive) main;
        n.setValue(property.hasImpliedPrefix() ? property.getImpliedPrefix()+p.asString() : p.asString());
        if (!n.getProperty().isChoice() && n.getType().equals("xhtml")) {
          try {
            XhtmlParser xhtml = new XhtmlParser();
            n.setXhtml(xhtml.setXmlMode(true).parse(n.getValue(), null).getDocumentElement());
            if (policy == ValidationPolicy.EVERYTHING) {
              for (StringPair s : xhtml.getValidationIssues()) {
                logError("2022-11-17", line(main), col(main), npath, IssueType.INVALID, context.formatMessage(s.getName(), s.getValue()), IssueSeverity.ERROR);                
              }
            }
          } catch (Exception e) {
            logError(ValidationMessage.NO_RULE_DATE, line(main), col(main), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_XHTML_, e.getMessage()), IssueSeverity.ERROR);
          }
        }
        if (policy == ValidationPolicy.EVERYTHING) {
          // now we cross-check the primitive format against the stated type
          if (Utilities.existsInList(n.getType(), "boolean")) {
            if (!p.isJsonBoolean()) {
              logError(ValidationMessage.NO_RULE_DATE, line(main), col(main), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_JSON_THE_PRIMITIVE_VALUE_MUST_BE_A_BOOLEAN), IssueSeverity.ERROR);
            }
          } else if (Utilities.existsInList(n.getType(), "integer", "unsignedInt", "positiveInt", "decimal")) {
            if (!p.isJsonNumber())
              logError(ValidationMessage.NO_RULE_DATE, line(main), col(main), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_JSON_THE_PRIMITIVE_VALUE_MUST_BE_A_NUMBER), IssueSeverity.ERROR);
          } else if (!p.isJsonString()) {
            logError(ValidationMessage.NO_RULE_DATE, line(main), col(main), npath, IssueType.INVALID, context.formatMessage(I18nConstants.ERROR_PARSING_JSON_THE_PRIMITIVE_VALUE_MUST_BE_A_STRING), IssueSeverity.ERROR);
          }
        }
      }
      if (fork != null) {
        JsonObject child = (JsonObject) fork;
        checkObject(child, n, npath);
        parseChildren(npath, child, n, false);
      }
    }
  }


  private void parseResource(String npath, JsonObject res, Element parent, Property elementProperty) throws FHIRException {
    JsonElement rt = res.get("resourceType");
    if (rt == null) {
      logError(ValidationMessage.NO_RULE_DATE, line(res), col(res), npath, IssueType.INVALID, context.formatMessage(I18nConstants.UNABLE_TO_FIND_RESOURCETYPE_PROPERTY), IssueSeverity.FATAL);
    } else if (!rt.isJsonString()) {
      logError("2022-11-26", line(res), col(res), npath, IssueType.INVALID, context.formatMessage(I18nConstants.RESOURCETYPE_PROPERTY_WRONG_TYPE, rt.type().toName()), IssueSeverity.FATAL);
    } else {
      String name = rt.asString();
      StructureDefinition sd = context.fetchResource(StructureDefinition.class, ProfileUtilities.sdNs(name, null));
      if (sd == null) {
        logError(ValidationMessage.NO_RULE_DATE, line(res), col(res), npath, IssueType.INVALID, context.formatMessage(I18nConstants.CONTAINED_RESOURCE_DOES_NOT_APPEAR_TO_BE_A_FHIR_RESOURCE_UNKNOWN_NAME_, name), IssueSeverity.FATAL);			    
      } else {
        parent.updateProperty(new Property(context, sd.getSnapshot().getElement().get(0), sd, this.profileUtilities), SpecialElement.fromProperty(parent.getProperty()), elementProperty);
        parent.setType(name);
        parseChildren(npath, res, parent, true);
      }
    }
    if (res.isExtraComma()) {
      logError("2022-11-26", res.getEnd().getLine(), res.getEnd().getCol(), npath, IssueType.INVALID, context.formatMessage(I18nConstants.JSON_COMMA_EXTRA, "Object"), IssueSeverity.ERROR);
    }

  }

  private void reapComments(JsonObject object, Element context) {
    // todo
  }

  private int line(JsonElement e) {
    return e.getStart().getLine();
  }

  private int col(JsonElement e) {
    return e.getEnd().getCol();
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
    if (e.getPath() == null) {
      e.populatePaths(null);
    }

    OutputStreamWriter osw = new OutputStreamWriter(stream, "UTF-8");
    if (style == OutputStyle.CANONICAL) {
      json = new JsonCreatorCanonical(osw);
    } else if (style == OutputStyle.PRETTY) {
      json = new JsonCreatorDirect(osw, true, allowComments);
    } else {
      json = new JsonCreatorDirect(osw, false, allowComments);
    }
    checkComposeComments(e);
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

  private void checkComposeComments(Element e) {
    for (String s : e.getComments()) {
      json.comment(s);
    }
  }

  public void compose(Element e, JsonCreator json) throws Exception {
    if (e.getPath() == null) {
      e.populatePaths(null);
    }
    
    this.json = json;
    checkComposeComments(e);
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
    checkComposeComments(child);
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