package org.hl7.fhir.r5.elementmodel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element.SpecialElement;
import org.hl7.fhir.r5.fhirpath.ExpressionNode;
import org.hl7.fhir.r5.fhirpath.FHIRLexer;
import org.hl7.fhir.r5.fhirpath.FHIRPathEngine;
import org.hl7.fhir.r5.fhirpath.FHIRLexer.FHIRLexerException;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.ConceptMap.ConceptMapGroupUnmappedMode;
import org.hl7.fhir.r5.model.Enumerations.ConceptMapRelationship;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.StructureMap.StructureMapGroupTypeMode;
import org.hl7.fhir.r5.model.StructureMap.StructureMapTransform;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.SourceLocation;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.validation.ValidationMessage.Source;

public class FmlParser extends ParserBase {

  private FHIRPathEngine fpe;

  public FmlParser(IWorkerContext context) {
    super(context);
    fpe = new FHIRPathEngine(context);
  }

  @Override
  public List<ValidatedFragment> parse(InputStream inStream) throws IOException, FHIRFormatError, DefinitionException, FHIRException {
    byte[] content = TextFile.streamToBytes(inStream);
    ByteArrayInputStream stream = new ByteArrayInputStream(content);
    String text = TextFile.streamToString(stream);
    List<ValidatedFragment> result = new ArrayList<>();
    ValidatedFragment focusFragment = new ValidatedFragment(ValidatedFragment.FOCUS_NAME, "fml", content, false);
    focusFragment.setElement(parse(focusFragment.getErrors(), text));
    result.add(focusFragment);
    return result;
  }

  @Override
  public void compose(Element e, OutputStream destination, OutputStyle style, String base)
      throws FHIRException, IOException {
    throw new Error("Not done yet");
  }

  public Element parse(List<ValidationMessage> errors, String text) throws FHIRException {
    FHIRLexer lexer = new FHIRLexer(text, "source", true, true);
    if (lexer.done())
      throw lexer.error("Map Input cannot be empty");
    Element result = Manager.build(context, context.fetchTypeDefinition("StructureMap"));
    try {
      if (lexer.hasToken("map")) {
        lexer.token("map");
        result.makeElement("url").markLocation(lexer.getCurrentLocation()).setValue(lexer.readConstant("url"));
        lexer.token("=");
        result.makeElement("name").markLocation(lexer.getCurrentLocation()).setValue(lexer.readConstant("name"));
        if (lexer.hasComments()) {
          result.makeElement("description").markLocation(lexer.getCurrentLocation()).setValue(lexer.getAllComments());
        }
      }
      while (lexer.hasToken("///")) {
        lexer.next();
        String fid = lexer.takeDottedToken();
        Element e = result.makeElement(fid).markLocation(lexer.getCurrentLocation());
        lexer.token("=");
        // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777        
        String multiline = lexer.getCurrent();
        if (fid.equals("description")) {
          String descr = lexer.readConstant("description");
          if ("\"\"".equals(multiline)) {
            descr = lexer.readConstant("description multiline");
            if (descr.startsWith("\r")) {
              descr = descr.substring(1);
            }
            if (descr.startsWith("\n")) {
                descr = descr.substring(1);
            }
            if (descr.endsWith("\n")) {
                descr = descr.substring(0, descr.length()-1);
            }
            if (descr.endsWith("\r")) {
                descr = descr.substring(0, descr.length()-1);
            }
            lexer.skipToken("\"\"");
          } 
          e.setValue(descr);
        } else {
            e.setValue(lexer.readConstant("meta value"));
        }
      }
      lexer.setMetadataFormat(false);
      if (!result.hasChild("status")) {
        result.makeElement("status").setValue("draft");
      }
      if (!result.hasChild("id") && result.hasChild("name")) {
        String id = Utilities.makeId(result.getChildValue("name"));
        if (!Utilities.noString(id)) {
          result.makeElement("id").setValue(id);
        }
      }
      if (!result.hasChild("description") && result.hasChild("title")) {
        result.makeElement("description").setValue(Utilities.makeId(result.getChildValue("title")));
      }
      
      while (lexer.hasToken("conceptmap"))
        parseConceptMap(result, lexer);

      while (lexer.hasToken("uses"))
        parseUses(result, lexer);
      while (lexer.hasToken("imports"))
        parseImports(result, lexer);

      while (lexer.hasToken("conceptmap"))
        parseConceptMap(result, lexer);

      while (!lexer.done()) {
        parseGroup(result, lexer);
      }
    } catch (FHIRLexerException e) {
      if (policy == ValidationPolicy.NONE) {
        throw e;
      } else {
        logError(errors, "2023-02-24", e.getLocation().getLine(), e.getLocation().getColumn(), "??", IssueType.INVALID, e.getMessage(), IssueSeverity.FATAL);
      }
    } catch (Exception e) {
      if (policy == ValidationPolicy.NONE) {
        throw e;
      } else {
        logError(errors, "2023-02-24", -1, -1, "?", IssueType.INVALID, e.getMessage(), IssueSeverity.FATAL);
      }
    }
    result.setIgnorePropertyOrder(true);
    return result;
  }

  private void parseConceptMap(Element structureMap, FHIRLexer lexer) throws FHIRLexerException {
    lexer.token("conceptmap");
    Element map = structureMap.makeElement("contained");
    StructureDefinition sd = context.fetchTypeDefinition("ConceptMap");
    map.updateProperty(new Property(context, sd.getSnapshot().getElement().get(0), sd, getProfileUtilities(), getContextUtilities()), SpecialElement.fromProperty(map.getElementProperty() != null ? map.getElementProperty() : map.getProperty()), map.getProperty());
    map.setType("ConceptMap");
    Element eid = map.makeElement("id").markLocation(lexer.getCurrentLocation());
    String id = lexer.readConstant("map id");
    if (id.startsWith("#"))
      throw lexer.error("Concept Map identifier must not start with #");
    eid.setValue(id);
    map.makeElement("status").setValue(structureMap.getChildValue("status"));
    lexer.token("{");
    //    lexer.token("source");
    //    map.setSource(new UriType(lexer.readConstant("source")));
    //    lexer.token("target");
    //    map.setSource(new UriType(lexer.readConstant("target")));
    Map<String, String> prefixes = new HashMap<String, String>();
    while (lexer.hasToken("prefix")) {
      lexer.token("prefix");
      String n = lexer.take();
      lexer.token("=");
      String v = lexer.readConstant("prefix url");
      prefixes.put(n, v);
    }
    while (lexer.hasToken("unmapped")) {
      lexer.token("unmapped");
      lexer.token("for");
      String n = readPrefix(prefixes, lexer);
      Element g = getGroupE(map, n, null);
      lexer.token("=");
      SourceLocation loc = lexer.getCurrentLocation();
      String v = lexer.take();
      if (v.equals("provided")) {
        g.makeElement("unmapped").makeElement("mode").markLocation(loc).setValue(ConceptMapGroupUnmappedMode.USESOURCECODE.toCode());
      } else
        throw lexer.error("Only unmapped mode PROVIDED is supported at this time");
    }
    while (!lexer.hasToken("}")) {
      String comments = lexer.hasComments() ? lexer.getAllComments() : null;
      String srcs = readPrefix(prefixes, lexer);
      lexer.token(":");
      SourceLocation scloc = lexer.getCurrentLocation();
      String sc = lexer.getCurrent().startsWith("\"") ? lexer.readConstant("code") : lexer.take();
      SourceLocation relLoc = lexer.getCurrentLocation();
      ConceptMapRelationship rel = readRelationship(lexer);
      String tgts = readPrefix(prefixes, lexer);
      Element g = getGroupE(map, srcs, tgts);
      Element e = g.addElement("element");
      if (comments != null) {
        for (String s : comments.split("\\r\\n")) {
          e.getComments().add(s);
        }
      }
      e.makeElement("code").markLocation(scloc).setValue(sc.startsWith("\"") ? lexer.processConstant(sc) : sc);
      Element tgt = e.addElement("target");
      tgt.makeElement("relationship").markLocation(relLoc).setValue(rel.toCode());
      lexer.token(":");
      tgt.makeElement("code").markLocation(lexer.getCurrentLocation()).setValue(lexer.getCurrent().startsWith("\"") ? lexer.readConstant("code") : lexer.take());
      if (lexer.hasComments()) {
        tgt.makeElement("comment").markLocation(lexer.getCommentLocation()).setValue(lexer.getFirstComment());
      }
    }
    lexer.token("}");
  }
  
  private Element getGroupE(Element map, String srcs, String tgts) {
    for (Element grp : map.getChildrenByName("group")) {
      if (grp.getChildValue("source").equals(srcs)) {
        Element tgt = grp.getNamedChild("target");
        if (tgt == null || tgts == null || tgts.equals(tgt.getValue())) {
          if (tgt == null && tgts != null)
            grp.makeElement("target").setValue(tgts);
          return grp;
        }
      }
    }
    Element grp = map.addElement("group");
    grp.makeElement("source").setValue(srcs);
    grp.makeElement("target").setValue(tgts);
    return grp;
  }

  private String readPrefix(Map<String, String> prefixes, FHIRLexer lexer) throws FHIRLexerException {
    String prefix = lexer.take();
    if (!prefixes.containsKey(prefix))
      throw lexer.error("Unknown prefix '" + prefix + "'");
    return prefixes.get(prefix);
  }


  private ConceptMapRelationship readRelationship(FHIRLexer lexer) throws FHIRLexerException {
    String token = lexer.take();
    if (token.equals("-"))
      return ConceptMapRelationship.RELATEDTO;
    if (token.equals("=")) // temporary
      return ConceptMapRelationship.RELATEDTO;
    if (token.equals("=="))
      return ConceptMapRelationship.EQUIVALENT;
    if (token.equals("!="))
      return ConceptMapRelationship.NOTRELATEDTO;
    if (token.equals("<="))
      return ConceptMapRelationship.SOURCEISNARROWERTHANTARGET;
    if (token.equals(">="))
      return ConceptMapRelationship.SOURCEISBROADERTHANTARGET;
    throw lexer.error("Unknown relationship token '" + token + "'");
  }

  private void parseUses(Element result, FHIRLexer lexer) throws FHIRException {
    lexer.token("uses");
    Element st = result.addElement("structure");
    st.makeElement("url").markLocation(lexer.getCurrentLocation()).setValue(lexer.readConstant("url"));
    if (lexer.hasToken("alias")) {
      lexer.token("alias");
      st.makeElement("alias").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    }
    lexer.token("as");
    st.makeElement("mode").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    lexer.skipToken(";");
    if (lexer.hasComments()) {
      st.makeElement("documentation").markLocation(lexer.getCommentLocation()).setValue(lexer.getFirstComment());
    }
  }
  

  private void parseImports(Element result, FHIRLexer lexer) throws FHIRException {
    lexer.token("imports");
    result.addElement("import").markLocation(lexer.getCurrentLocation()).setValue(lexer.readConstant("url"));
    lexer.skipToken(";");
  }

  private void parseGroup(Element result, FHIRLexer lexer) throws FHIRException {
    SourceLocation commLoc = lexer.getCommentLocation();
    String comment = lexer.getAllComments();
    lexer.token("group");
    Element group = result.addElement("group").markLocation(lexer.getCurrentLocation());
    if (!Utilities.noString(comment)) {
      group.makeElement("documentation").markLocation(commLoc).setValue(comment);
    }
    boolean newFmt = false;
    if (lexer.hasToken("for")) {
      lexer.token("for");
      SourceLocation loc = lexer.getCurrentLocation();
      if ("type".equals(lexer.getCurrent())) {
        lexer.token("type");
        lexer.token("+");
        lexer.token("types");
        group.makeElement("typeMode").markLocation(loc).setValue(StructureMapGroupTypeMode.TYPEANDTYPES.toCode());
      } else {
        lexer.token("types");
        group.makeElement("typeMode").markLocation(loc).setValue(StructureMapGroupTypeMode.TYPES.toCode());
      }
    }
    group.makeElement("name").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    if (lexer.hasToken("(")) {
      newFmt = true;
      lexer.take();
      while (!lexer.hasToken(")")) {
        parseInput(group, lexer, true);
        if (lexer.hasToken(","))
          lexer.token(",");
      }
      lexer.take();
    }
    if (lexer.hasToken("extends")) {
      lexer.next();
      group.makeElement("extends").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    }
    if (newFmt) {
      if (lexer.hasToken("<")) {
        lexer.token("<");
        lexer.token("<");
        if (lexer.hasToken("types")) {
          group.makeElement("typeMode").markLocation(lexer.getCurrentLocation()).setValue(StructureMapGroupTypeMode.TYPES.toCode());
          lexer.token("types");
        } else {
          group.makeElement("typeMode").markLocation(lexer.getCurrentLocation()).setValue(StructureMapGroupTypeMode.TYPEANDTYPES.toCode());
          lexer.token("type");
          lexer.token("+");
        }
        lexer.token(">");
        lexer.token(">");
      }
      lexer.token("{");
    }
    if (newFmt) {
      while (!lexer.hasToken("}")) {
        if (lexer.done())
          throw lexer.error("premature termination expecting 'endgroup'");
        parseRule(result, group, lexer, true);
      }
    } else {
      while (lexer.hasToken("input"))
        parseInput(group, lexer, false);
      while (!lexer.hasToken("endgroup")) {
        if (lexer.done())
          throw lexer.error("premature termination expecting 'endgroup'");
        parseRule(result, group, lexer, false);
      }
    }
    lexer.next();
    if (newFmt && lexer.hasToken(";"))
      lexer.next();
  }
  

  private void parseRule(Element map, Element context, FHIRLexer lexer, boolean newFmt) throws FHIRException {
    Element rule = context.addElement("rule").markLocation(lexer.getCurrentLocation());
    if (!newFmt) {
      rule.makeElement("name").markLocation(lexer.getCurrentLocation()).setValue(lexer.takeDottedToken());
      lexer.token(":");
      lexer.token("for");
    } else {
      if (lexer.hasComments()) {
        rule.makeElement("documentation").markLocation(lexer.getCommentLocation()).setValue(lexer.getFirstComment());
      }
    }

    boolean done = false;
    while (!done) {
      parseSource(rule, lexer);
      done = !lexer.hasToken(",");
      if (!done)
        lexer.next();
    }
    if ((newFmt && lexer.hasToken("->")) || (!newFmt && lexer.hasToken("make"))) {
      lexer.token(newFmt ? "->" : "make");
      done = false;
      while (!done) {
        parseTarget(rule, lexer);
        done = !lexer.hasToken(",");
        if (!done)
          lexer.next();
      }
    }
    if (lexer.hasToken("then")) {
      lexer.token("then");
      if (lexer.hasToken("{")) {
        lexer.token("{");
        while (!lexer.hasToken("}")) {
          if (lexer.done())
            throw lexer.error("premature termination expecting '}' in nested group");
          parseRule(map, rule, lexer, newFmt);
        }
        lexer.token("}");
      } else {
        done = false;
        while (!done) {
          parseRuleReference(rule, lexer);
          done = !lexer.hasToken(",");
          if (!done)
            lexer.next();
        }
      }
    }
    if (!rule.hasChild("documentation") && lexer.hasComments()) {
      rule.makeElement("documentation").markLocation(lexer.getCommentLocation()).setValue(lexer.getFirstComment());
    }

    if (isSimpleSyntax(rule)) {
      rule.forceElement("source").makeElement("variable").setValue(StructureMapUtilities.AUTO_VAR_NAME);
      rule.forceElement("target").makeElement("variable").setValue(StructureMapUtilities.AUTO_VAR_NAME);
      rule.forceElement("target").makeElement("transform").setValue(StructureMapTransform.CREATE.toCode());
      Element dep = rule.forceElement("dependent").markLocation(rule);
      dep.makeElement("name").markLocation(rule).setValue(StructureMapUtilities.DEF_GROUP_NAME);
      dep.addElement("parameter").markLocation(dep).makeElement("valueId").markLocation(dep).setValue(StructureMapUtilities.AUTO_VAR_NAME);
      dep.addElement("parameter").markLocation(dep).makeElement("valueId").markLocation(dep).setValue(StructureMapUtilities.AUTO_VAR_NAME);
      // no dependencies - imply what is to be done based on types
    }
    if (newFmt) {
      if (lexer.isConstant()) {
        if (lexer.isStringConstant()) {
          rule.makeElement("name").markLocation(lexer.getCurrentLocation()).setValue(fixName(lexer.readConstant("ruleName")));
        } else {
          rule.makeElement("name").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
        }
      } else {
        if (rule.getChildrenByName("source").size() != 1 || !rule.getChildrenByName("source").get(0).hasChild("element"))
          throw lexer.error("Complex rules must have an explicit name");
        if (rule.getChildrenByName("source").get(0).hasChild("type"))
          rule.makeElement("name").setValue(rule.getChildrenByName("source").get(0).getNamedChildValue("element") + Utilities.capitalize(rule.getChildrenByName("source").get(0).getNamedChildValue("type")));
        else
          rule.makeElement("name").setValue(rule.getChildrenByName("source").get(0).getNamedChildValue("element"));
      }
      lexer.token(";");
    }
  }

  private String fixName(String c) {
    return c.replace("-", "");
  }

  private void parseRuleReference(Element rule, FHIRLexer lexer) throws FHIRLexerException {
    Element ref = rule.addElement("dependent").markLocation(lexer.getCurrentLocation());
    ref.makeElement("name").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    lexer.token("(");
    boolean done = false;
    while (!done) {
      parseParameter(ref, lexer, false);
      done = !lexer.hasToken(",");
      if (!done)
        lexer.next();
    }
    lexer.token(")");
  }
  
  // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
  private String removeQuotedOrBacktick(String token) {
    if (token.startsWith("`") && token.endsWith("`")) {
       return token.substring(1,token.length()-1);
    }
    if (token.startsWith("\"") && token.endsWith("\"")) {
        return token.substring(1,token.length()-1);
     }
    return token;
  }

  private void parseSource(Element rule, FHIRLexer lexer) throws FHIRException {
    Element source = rule.addElement("source").markLocation(lexer.getCurrentLocation());
    source.makeElement("context").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    if (source.getChildValue("context").equals("search") && lexer.hasToken("(")) {
      source.makeElement("context").markLocation(lexer.getCurrentLocation()).setValue("@search");
      lexer.take();
      SourceLocation loc = lexer.getCurrentLocation();
      ExpressionNode node = fpe.parse(lexer);
      source.setUserData(StructureMapUtilities.MAP_SEARCH_EXPRESSION, node);
      source.makeElement("element").markLocation(loc).setValue(node.toString());
      lexer.token(")");
    } else if (lexer.hasToken(".")) {
      lexer.token(".");
      // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
      source.makeElement("element").markLocation(lexer.getCurrentLocation()).setValue(removeQuotedOrBacktick(lexer.take()));
    }
    if (lexer.hasToken(":")) {
      // type and cardinality
      lexer.token(":");
      source.makeElement("type").markLocation(lexer.getCurrentLocation()).setValue(lexer.takeDottedToken());
    }
    if (Utilities.isInteger(lexer.getCurrent())) {
      source.makeElement("min").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
      lexer.token("..");
      source.makeElement("max").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    }
    if (lexer.hasToken("default")) {
      lexer.token("default");
     source.makeElement("defaultValue").markLocation(lexer.getCurrentLocation()).setValue(lexer.readConstant("default value"));
    }
    if (Utilities.existsInList(lexer.getCurrent(), "first", "last", "not_first", "not_last", "only_one")) {
      source.makeElement("listMode").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    }

    if (lexer.hasToken("as")) {
      lexer.take();
      source.makeElement("variable").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    }
    if (lexer.hasToken("where")) {
      lexer.take();
      SourceLocation loc = lexer.getCurrentLocation();
      ExpressionNode node = fpe.parse(lexer);
      source.setUserData(StructureMapUtilities.MAP_WHERE_EXPRESSION, node);
      source.makeElement("condition").markLocation(loc).setValue(node.toString());
    }
    if (lexer.hasToken("check")) {
      lexer.take();
      SourceLocation loc = lexer.getCurrentLocation();
      ExpressionNode node = fpe.parse(lexer);
      source.setUserData(StructureMapUtilities.MAP_WHERE_CHECK, node);
      source.makeElement("check").markLocation(loc).setValue(node.toString());
    }
    if (lexer.hasToken("log")) {
      lexer.take();
      SourceLocation loc = lexer.getCurrentLocation();
      ExpressionNode node = fpe.parse(lexer);
      // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777 
      source.setUserData(StructureMapUtilities.MAP_WHERE_LOG, node);
      source.makeElement("logMessage").markLocation(loc).setValue(node.toString());
    }
  }
  
  private void parseTarget(Element rule, FHIRLexer lexer) throws FHIRException {
    Element target = rule.addElement("target").markLocation(lexer.getCurrentLocation());
    SourceLocation loc = lexer.getCurrentLocation();
    String start = lexer.take();
    if (lexer.hasToken(".")) {
      target.makeElement("context").markLocation(loc).setValue(start);
      start = null;
      lexer.token(".");
      // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
      target.makeElement("element").markLocation(lexer.getCurrentLocation()).setValue(removeQuotedOrBacktick(lexer.take()));
    }
    String name;
    boolean isConstant = false;
    if (lexer.hasToken("=")) {
      if (start != null) {
        target.makeElement("context").markLocation(loc).setValue(start);
      }
      lexer.token("=");
      isConstant = lexer.isConstant();
      loc = lexer.getCurrentLocation();
      name = lexer.take();
    } else {
      loc = lexer.getCurrentLocation();
      name = start;
    }

    if ("(".equals(name)) {
      // inline fluentpath expression
      target.makeElement("transform").markLocation(lexer.getCurrentLocation()).setValue(StructureMapTransform.EVALUATE.toCode());
      loc = lexer.getCurrentLocation();
      ExpressionNode node = fpe.parse(lexer);
      target.setUserData(StructureMapUtilities.MAP_EXPRESSION, node);
      target.addElement("parameter").markLocation(loc).makeElement("valueString").setValue(node.toString());
      lexer.token(")");
    } else if (lexer.hasToken("(")) {
      target.makeElement("transform").markLocation(loc).setValue(name);
      lexer.token("(");
      if (target.getChildValue("transform").equals(StructureMapTransform.EVALUATE.toCode())) {
        parseParameter(target, lexer, true);
        lexer.token(",");
        loc = lexer.getCurrentLocation();
        ExpressionNode node = fpe.parse(lexer);
        target.setUserData(StructureMapUtilities.MAP_EXPRESSION, node);
        target.addElement("parameter").markLocation(loc).makeElement("valueString").setValue(node.toString());
      } else {
        while (!lexer.hasToken(")")) {
          parseParameter(target, lexer, true);
          if (!lexer.hasToken(")"))
            lexer.token(",");
        }
      }
      lexer.token(")");
    } else if (name != null) {
      target.makeElement("transform").markLocation(loc).setValue(StructureMapTransform.COPY.toCode());
      if (!isConstant) {
        loc = lexer.getCurrentLocation();
        String id = name;
        while (lexer.hasToken(".")) {
          id = id + lexer.take() + lexer.take();
        }
        target.addElement("parameter").markLocation(loc).makeElement("valueId").setValue(id);
      } else {
        target.addElement("parameter").markLocation(lexer.getCurrentLocation()).makeElement("valueString").setValue(readConstant(name, lexer));
      }
    }
    if (lexer.hasToken("as")) {
      lexer.take();
      target.makeElement("variable").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    }
    while (Utilities.existsInList(lexer.getCurrent(), "first", "last", "share", "collate")) {
      if (lexer.getCurrent().equals("share")) {
        target.makeElement("listMode").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
        target.makeElement("listRuleId").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
      } else {
        target.makeElement("listMode").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
      }
    }
  }

  private void parseParameter(Element ref, FHIRLexer lexer, boolean isTarget) throws FHIRLexerException, FHIRFormatError {
    boolean r5 = VersionUtilities.isR5Plus(context.getVersion());
    String name = r5 || isTarget ? "parameter" : "variable";
    if (ref.hasChildren(name) && !ref.getChildByName(name).isList()) {
      throw lexer.error("variable on target is not a list, so can't add an element");
    } else if (!lexer.isConstant()) {
      ref.addElement(name).markLocation(lexer.getCurrentLocation()).makeElement(r5 ? "valueId" : "value").setValue(lexer.take());
    } else if (lexer.isStringConstant())
      ref.addElement(name).markLocation(lexer.getCurrentLocation()).makeElement(r5 ? "valueString" : "value").setValue(lexer.readConstant("??"));
    else {
      ref.addElement(name).markLocation(lexer.getCurrentLocation()).makeElement(r5 ? "valueString" : "value").setValue(readConstant(lexer.take(), lexer));
    }
  }
 
  private void parseInput(Element group, FHIRLexer lexer, boolean newFmt) throws FHIRException {
    Element input = group.addElement("input").markLocation(lexer.getCurrentLocation());
    if (newFmt) {
      input.makeElement("mode").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    } else
      lexer.token("input");
    input.makeElement("name").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    if (lexer.hasToken(":")) {
      lexer.token(":");
      input.makeElement("type").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
    }
    if (!newFmt) {
      lexer.token("as");
      input.makeElement("mode").markLocation(lexer.getCurrentLocation()).setValue(lexer.take());
      if (lexer.hasComments()) {
        input.makeElement("documentation").markLocation(lexer.getCommentLocation()).setValue(lexer.getFirstComment());
      }
      lexer.skipToken(";");
    }
  }
  
  private boolean isSimpleSyntax(Element rule) {
    return
      (rule.getChildren("source").size() == 1 && rule.getChildren("source").get(0).hasChild("context") && rule.getChildren("source").get(0).hasChild("element") && !rule.getChildren("source").get(0).hasChild("variable")) &&
        (rule.getChildren("target").size() == 1 && rule.getChildren("target").get(0).hasChild("context") && rule.getChildren("target").get(0).hasChild("element") && !rule.getChildren("target").get(0).hasChild("variable") && 
           !rule.getChildren("target").get(0).hasChild("parameter")) &&
        (rule.getChildren("dependent").size() == 0 && rule.getChildren("rule").size() == 0);
  }

  private String readConstant(String s, FHIRLexer lexer) throws FHIRLexerException {
    if (Utilities.isInteger(s))
      return s;
    else if (Utilities.isDecimal(s, false))
      return s;
    else if (Utilities.existsInList(s, "true", "false"))
      return s;
    else
      return lexer.processConstant(s);
  }

  
}
