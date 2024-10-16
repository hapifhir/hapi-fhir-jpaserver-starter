package org.hl7.fhir.r5.utils.structuremap;

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


// remember group resolution
// trace - account for which wasn't transformed in the source

import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.conformance.profile.ProfileKnowledgeProvider;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities;
import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.FmlParser;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Property;
import org.hl7.fhir.r5.elementmodel.ParserBase.ValidationPolicy;
import org.hl7.fhir.r5.fhirpath.ExpressionNode;
import org.hl7.fhir.r5.fhirpath.FHIRPathEngine;
import org.hl7.fhir.r5.fhirpath.TypeDetails;
import org.hl7.fhir.r5.fhirpath.ExpressionNode.CollectionStatus;
import org.hl7.fhir.r5.fhirpath.TypeDetails.ProfiledType;
import org.hl7.fhir.r5.formats.IParser;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.r5.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.r5.model.ConceptMap.TargetElementComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionMappingComponent;
import org.hl7.fhir.r5.model.Enumeration;
import org.hl7.fhir.r5.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r5.model.Enumerations.ConceptMapRelationship;
import org.hl7.fhir.r5.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionMappingComponent;
import org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule;
import org.hl7.fhir.r5.model.StructureMap.*;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r5.renderers.TerminologyRenderer;
import org.hl7.fhir.r5.terminologies.expansion.ValueSetExpansionOutcome;
import org.hl7.fhir.r5.terminologies.utilities.ValidationResult;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.CommaSeparatedStringBuilder;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationOptions;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import net.sourceforge.plantuml.utils.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Services in this class:
 * <p>
 * string render(map) - take a structure and convert it to text
 * map parse(text) - take a text representation and parse it
 * getTargetType(map) - return the definition for the type to create to hand in
 * transform(appInfo, source, map, target) - transform from source to target following the map
 * analyse(appInfo, map) - generate profiles and other analysis artifacts for the targets of the transform
 * map generateMapFromMappings(StructureDefinition) - build a mapping from a structure definition with loigcal mappings
 *
 * @author Grahame Grieve
 */
public class StructureMapUtilities {

  public static final String MAP_WHERE_CHECK = "map.where.check";
  public static final String MAP_WHERE_LOG = "map.where.log";
  public static final String MAP_WHERE_EXPRESSION = "map.where.expression";
  public static final String MAP_SEARCH_EXPRESSION = "map.search.expression";
  public static final String MAP_EXPRESSION = "map.transform.expression";
  private static final boolean MULTIPLE_TARGETS_ONELINE = true;
  public static final String AUTO_VAR_NAME = "vvv";
  public static final String DEF_GROUP_NAME = "DefaultMappingGroupAnonymousAlias";
  
  private final IWorkerContext worker;
  private final FHIRPathEngine fpe;
  private ITransformerServices services;
  private ProfileKnowledgeProvider pkp;
  private final Map<String, Integer> ids = new HashMap<String, Integer>();
  private ValidationOptions terminologyServiceOptions = new ValidationOptions(FhirPublication.R5);
  private final ProfileUtilities profileUtilities;
  private boolean exceptionsForChecks = true;
  private boolean debug;

  public StructureMapUtilities(IWorkerContext worker, ITransformerServices services, ProfileKnowledgeProvider pkp) {
    super();
    this.worker = worker;
    this.services = services;
    this.pkp = pkp;
    fpe = new FHIRPathEngine(worker);
    fpe.setHostServices(new FHIRPathHostServices(this));
    profileUtilities = new ProfileUtilities(worker, null, null);
  }

  public StructureMapUtilities(IWorkerContext worker, ITransformerServices services) {
    super();
    this.worker = worker;
    this.services = services;
    fpe = new FHIRPathEngine(worker);
    fpe.setHostServices(new FHIRPathHostServices(this));
    profileUtilities = new ProfileUtilities(worker, null, null);
  }

  public StructureMapUtilities(IWorkerContext worker) {
    super();
    this.worker = worker;
    fpe = new FHIRPathEngine(worker);
    fpe.setHostServices(new FHIRPathHostServices(this));
    profileUtilities = new ProfileUtilities(worker, null, null);

  }

  public static String render(StructureMap map) {
    StringBuilder b = new StringBuilder();
    b.append("/// url = \""+map.getUrl()+"\"\r\n");
    b.append("/// name = \""+map.getName()+"\"\r\n");
    b.append("/// title = \""+map.getTitle()+"\"\r\n");
    b.append("/// status = \""+map.getStatus().toCode()+"\"\r\n");
    b.append("\r\n");
    if (map.getDescription() != null) {
      b.append("/// description = \"\"\"\r\n");
      renderMultilineDoco(b, map.getDescription(), 0, false);
      b.append("\"\"\"\r\n");
    }
    renderConceptMaps(b, map);
    renderUses(b, map);
    renderImports(b, map);
    for (StructureMapGroupComponent g : map.getGroup())
      renderGroup(b, g);
    return b.toString();
  }

  private static void renderConceptMaps(StringBuilder b, StructureMap map) {
    for (Resource r : map.getContained()) {
      if (r instanceof ConceptMap) {
        produceConceptMap(b, (ConceptMap) r);
      }
    }
  }

  private static void produceConceptMap(StringBuilder b, ConceptMap cm) {
    b.append("conceptmap \"");
    b.append(cm.getId());
    b.append("\" {\r\n");
    Map<String, String> prefixesSrc = new HashMap<String, String>();
    Map<String, String> prefixesTgt = new HashMap<String, String>();
    char prefix = 's';
    for (ConceptMapGroupComponent cg : cm.getGroup()) {
      if (!prefixesSrc.containsKey(cg.getSource())) {
        prefixesSrc.put(cg.getSource(), String.valueOf(prefix));
        b.append("  prefix ");
        b.append(prefix);
        b.append(" = \"");
        b.append(cg.getSource());
        b.append("\"\r\n");
        prefix++;
      }
      if (!prefixesTgt.containsKey(cg.getTarget())) {
        prefixesTgt.put(cg.getTarget(), String.valueOf(prefix));
        b.append("  prefix ");
        b.append(prefix);
        b.append(" = \"");
        b.append(cg.getTarget());
        b.append("\"\r\n");
        prefix++;
      }
    }
    b.append("\r\n");
    for (ConceptMapGroupComponent cg : cm.getGroup()) {
      if (cg.hasUnmapped()) {
        b.append("  unmapped for ");
        b.append(prefixesSrc.get(cg.getSource()));
        b.append(" = ");
        b.append(cg.getUnmapped().getMode().toCode());
        b.append("\r\n");
      }
    }

    for (ConceptMapGroupComponent cg : cm.getGroup()) {
      for (SourceElementComponent ce : cg.getElement()) {
        b.append("  ");
        b.append(prefixesSrc.get(cg.getSource()));
        b.append(":");
        if (Utilities.isToken(ce.getCode())) {
          b.append(ce.getCode());
        } else {
          b.append("\"");
          b.append(ce.getCode());
          b.append("\"");
        }
        b.append(" ");
        b.append(getChar(ce.getTargetFirstRep().getRelationship()));
        b.append(" ");
        b.append(prefixesTgt.get(cg.getTarget()));
        b.append(":");
        if (Utilities.isToken(ce.getTargetFirstRep().getCode())) {
          b.append(ce.getTargetFirstRep().getCode());
        } else {
          b.append("\"");
          b.append(ce.getTargetFirstRep().getCode());
          b.append("\"");
        }
        b.append("\r\n");
      }
    }
    b.append("}\r\n\r\n");
  }

  private static Object getChar(ConceptMapRelationship relationship) {
    switch (relationship) {
      case RELATEDTO:
        return "-";
      case EQUIVALENT:
        return "==";
      case NOTRELATEDTO:
        return "!=";
      case SOURCEISNARROWERTHANTARGET:
        return "<=";
      case SOURCEISBROADERTHANTARGET:
        return ">=";
      default:
        return "??";
    }
  }

  private static void renderUses(StringBuilder b, StructureMap map) {
    for (StructureMapStructureComponent s : map.getStructure()) {
      b.append("uses \"");
      b.append(s.getUrl());
      b.append("\" ");
      if (s.hasAlias()) {
        b.append("alias ");
        b.append(s.getAlias());
        b.append(" ");
      }
      b.append("as ");
      b.append(s.getMode().toCode());
      renderDoco(b, s.getDocumentation());
      b.append("\r\n");
    }
    if (map.hasStructure())
      b.append("\r\n");
  }

  private static void renderImports(StringBuilder b, StructureMap map) {
    for (UriType s : map.getImport()) {
      b.append("imports \"");
      b.append(s.getValue());
      b.append("\"\r\n");
    }
    if (map.hasImport())
      b.append("\r\n");
  }

  public static String groupToString(StructureMapGroupComponent g) {
    StringBuilder b = new StringBuilder();
    renderGroup(b, g);
    return b.toString();
  }

  private static void renderGroup(StringBuilder b, StructureMapGroupComponent g) {
    if (g.hasDocumentation()) {
      renderMultilineDoco(b, g.getDocumentation(), 0, true);
    }
    b.append("group ");
    b.append(g.getName());
    b.append("(");
    boolean first = true;
    for (StructureMapGroupInputComponent gi : g.getInput()) {
      if (first)
        first = false;
      else
        b.append(", ");
      b.append(gi.getMode().toCode());
      b.append(" ");
      b.append(gi.getName());
      if (gi.hasType()) {
        b.append(" : ");
        b.append(gi.getType());
      }
    }
    b.append(")");
    if (g.hasExtends()) {
      b.append(" extends ");
      b.append(g.getExtends());
    }

    if (g.hasTypeMode()) {
      switch (g.getTypeMode()) {
        case TYPES:
          b.append(" <<types>>");
          break;
        case TYPEANDTYPES:
          b.append(" <<type+>>");
          break;
        default: // NONE, NULL
      }
    }
    b.append(" {\r\n");
    for (StructureMapGroupRuleComponent r : g.getRule()) {
      renderRule(b, r, 2);
    }
    b.append("}\r\n\r\n");
  }

  public static String ruleToString(StructureMapGroupRuleComponent r) {
    StringBuilder b = new StringBuilder();
    renderRule(b, r, 0);
    return b.toString();
  }

  private static void renderRule(StringBuilder b, StructureMapGroupRuleComponent r, int indent) {
    // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
    if (r.hasDocumentation()) {
      renderMultilineDoco(b, r.getDocumentation(), indent, true);
    }
    for (int i = 0; i < indent; i++)
      b.append(' ');
    boolean canBeAbbreviated = checkisSimple(r);
    {
      boolean first = true;
      for (StructureMapGroupRuleSourceComponent rs : r.getSource()) {
        if (first)
          first = false;
        else
          b.append(", ");
        renderSource(b, rs, canBeAbbreviated);
      }
    }
    if (r.getTarget().size() > 1) {
      b.append(" -> ");
      boolean first = true;
      for (StructureMapGroupRuleTargetComponent rt : r.getTarget()) {
        if (first)
          first = false;
        else
          b.append(", ");
        if (MULTIPLE_TARGETS_ONELINE)
          b.append(' ');
        else {
          b.append("\r\n");
          for (int i = 0; i < indent + 4; i++)
            b.append(' ');
        }
        renderTarget(b, rt, false);
      }
    } else if (r.hasTarget()) {
      b.append(" -> ");
      renderTarget(b, r.getTarget().get(0), canBeAbbreviated);
    }
    if (r.hasRule()) {
      b.append(" then {\r\n");
      for (StructureMapGroupRuleComponent ir : r.getRule()) {
        renderRule(b, ir, indent + 2);
      }
      for (int i = 0; i < indent; i++)
        b.append(' ');
      b.append("}");
    } else if (!canBeAbbreviated) {
      if (r.hasDependent()) {
        b.append(" then ");
        boolean first = true;
        for (StructureMapGroupRuleDependentComponent rd : r.getDependent()) {
          if (first)
            first = false;
          else
            b.append(", ");
          b.append(rd.getName());
          b.append("(");
          boolean ifirst = true;
          for (StructureMapGroupRuleTargetParameterComponent rdp : rd.getParameter()) {
            if (ifirst)
              ifirst = false;
            else
              b.append(", ");
            renderTransformParam(b, rdp);
          }
          b.append(")");
        }
      }
    }
    if (r.hasName()) {
      String n = ntail(r.getName());
      if (!n.startsWith("\""))
        n = "\"" + n + "\"";
      if (!matchesName(n, r.getSource())) {
        b.append(" ");
        b.append(n);
      }
    }
    b.append(";");
    b.append("\r\n");
  }

  private static boolean matchesName(String n, List<StructureMapGroupRuleSourceComponent> source) {
    if (source.size() != 1)
      return false;
    if (!source.get(0).hasElement())
      return false;
    String s = source.get(0).getElement();
    if (n.equals(s) || n.equals("\"" + s + "\""))
      return true;
    if (source.get(0).hasType()) {
      s = source.get(0).getElement() + "-" + source.get(0).getType();
      return n.equals(s) || n.equals("\"" + s + "\"");
    }
    return false;
  }

  private static String ntail(String name) {
    if (name == null)
      return null;
    if (name.startsWith("\"")) {
      name = name.substring(1);
      name = name.substring(0, name.length() - 1);
    }
    return "\"" + (name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : name) + "\"";
  }

  private static boolean checkisSimple(StructureMapGroupRuleComponent r) {
    return
      (r.getSource().size() == 1 && r.getSourceFirstRep().hasElement() && r.getSourceFirstRep().hasVariable()) &&
        (r.getTarget().size() == 1 && r.getTargetFirstRep().hasVariable() && (r.getTargetFirstRep().getTransform() == null || r.getTargetFirstRep().getTransform() == StructureMapTransform.CREATE) && r.getTargetFirstRep().getParameter().size() == 0) &&
        (r.getDependent().size() == 0 || (r.getDependent().size() == 1 && StructureMapUtilities.DEF_GROUP_NAME.equals(r.getDependentFirstRep().getName()))) && (r.getRule().size() == 0);
  }

  public static String sourceToString(StructureMapGroupRuleSourceComponent r) {
    StringBuilder b = new StringBuilder();
    renderSource(b, r, false);
    return b.toString();
  }

  private static void renderSource(StringBuilder b, StructureMapGroupRuleSourceComponent rs, boolean abbreviate) {
    b.append(rs.getContext());
    if (rs.getContext().equals("@search")) {
      b.append('(');
      b.append(rs.getElement());
      b.append(')');
    } else if (rs.hasElement()) {
      b.append('.');
      b.append(rs.getElement());
    }
    if (rs.hasType()) {
      b.append(" : ");
      b.append(rs.getType());
      if (rs.hasMin()) {
        b.append(" ");
        b.append(rs.getMin());
        b.append("..");
        b.append(rs.getMax());
      }
    }

    if (rs.hasListMode()) {
      b.append(" ");
      b.append(rs.getListMode().toCode());
    }
    if (rs.hasDefaultValue()) {
      b.append(" default ");
      b.append("\"" + Utilities.escapeJson(rs.getDefaultValue()) + "\"");
    }
    if (!abbreviate && rs.hasVariable()) {
      b.append(" as ");
      b.append(rs.getVariable());
    }
    if (rs.hasCondition()) {
      b.append(" where ");
      b.append(rs.getCondition());
    }
    if (rs.hasCheck()) {
      b.append(" check ");
      b.append(rs.getCheck());
    }
    if (rs.hasLogMessage()) {
      b.append(" log ");
      b.append(rs.getLogMessage());
    }
  }

  public static String targetToString(StructureMapGroupRuleTargetComponent rt) {
    StringBuilder b = new StringBuilder();
    renderTarget(b, rt, false);
    return b.toString();
  }

  private static void renderTarget(StringBuilder b, StructureMapGroupRuleTargetComponent rt, boolean abbreviate) {
    if (rt.hasContext()) {
      b.append(rt.getContext());
      if (rt.hasElement()) {
        b.append('.');
        b.append(rt.getElement());
      }
    }
    if (!abbreviate && rt.hasTransform()) {
      if (rt.hasContext())
        b.append(" = ");
      if (rt.getTransform() == StructureMapTransform.COPY && rt.getParameter().size() == 1) {
        renderTransformParam(b, rt.getParameter().get(0));
      } else if (rt.getTransform() == StructureMapTransform.EVALUATE && rt.getParameter().size() == 1) {
        b.append("(");
        b.append(((StringType) rt.getParameter().get(0).getValue()).asStringValue());
        b.append(")");
      } else if (rt.getTransform() == StructureMapTransform.EVALUATE && rt.getParameter().size() == 2) {
        b.append(rt.getTransform().toCode());
        b.append("(");
        b.append(((IdType) rt.getParameter().get(0).getValue()).asStringValue());
        b.append(", ");
        b.append(((StringType) rt.getParameter().get(1).getValue()).asStringValue());
        b.append(")");
      } else {
        b.append(rt.getTransform().toCode());
        b.append("(");
        boolean first = true;
        for (StructureMapGroupRuleTargetParameterComponent rtp : rt.getParameter()) {
          if (first)
            first = false;
          else
            b.append(", ");
          renderTransformParam(b, rtp);
        }
        b.append(")");
      }
    }
    if (!abbreviate && rt.hasVariable()) {
      b.append(" as ");
      b.append(rt.getVariable());
    }
    for (Enumeration<StructureMapTargetListMode> lm : rt.getListMode()) {
      b.append(" ");
      b.append(lm.getValue().toCode());
      if (lm.getValue() == StructureMapTargetListMode.SHARE) {
        b.append(" ");
        b.append(rt.getListRuleId());
      }
    }
  }

  public static String paramToString(StructureMapGroupRuleTargetParameterComponent rtp) {
    StringBuilder b = new StringBuilder();
    renderTransformParam(b, rtp);
    return b.toString();
  }

  private static void renderTransformParam(StringBuilder b, StructureMapGroupRuleTargetParameterComponent rtp) {
    try {
      if (rtp.hasValueBooleanType())
        b.append(rtp.getValueBooleanType().asStringValue());
      else if (rtp.hasValueDecimalType())
        b.append(rtp.getValueDecimalType().asStringValue());
      else if (rtp.hasValueIdType())
        b.append(rtp.getValueIdType().asStringValue());
      else if (rtp.hasValueIntegerType())
        b.append(rtp.getValueIntegerType().asStringValue());
      else
        b.append("'" + Utilities.escapeJava(rtp.getValueStringType().asStringValue()) + "'");
    } catch (FHIRException e) {
      e.printStackTrace();
      b.append("error!");
    }
  }

  private static void renderDoco(StringBuilder b, String doco) {
      // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
    renderDoco(b, doco, true);
  }

  private static void renderDoco(StringBuilder b, String doco, boolean addComment) {
    if (Utilities.noString(doco))
      return;
    if (b != null && b.length() > 1 && b.charAt(b.length() - 1) != '\n' && b.charAt(b.length() - 1) != ' ') {
      b.append(" ");
    }
    // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
    if (addComment) {
      b.append("// ");
    }
    b.append(doco.replace("\r\n", " ").replace("\r", " ").replace("\n", " "));
  }

  private static void renderMultilineDoco(StringBuilder b, String doco, int indent, boolean addComment) {
    if (Utilities.noString(doco))
      return;
    String[] lines = doco.split("\\r?\\n");
    for (String line : lines) {
      for (int i = 0; i < indent; i++)
        b.append(' ');
      renderDoco(b, line, addComment);
      b.append("\r\n");
    }
  }
  
  public ITransformerServices getServices() {
    return services;
  }

  public IWorkerContext getWorker() {
    return worker;
  }

  // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
  public StructureMap parse(String text, String srcName) throws FHIRException {
    IWorkerContext context = this.getWorker();
    if (!(context.getVersion().equals("5.0.0"))) {
      log("FHIR version needs to be 5.0.0");
      return null;
    }
    FmlParser fp = new FmlParser(context);
    fp.setupValidation(ValidationPolicy.EVERYTHING);     
    List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
    Element res = fp.parse(errors, Utilities.stripBOM(text));
    if (res == null) {
      Log.error(errors.toString());
      throw new FHIRException("Unable to parse Map Source for "+srcName + " Details "+errors.toString());
    }
    ByteArrayOutputStream boas = new ByteArrayOutputStream();
    try {
     new org.hl7.fhir.r5.elementmodel.JsonParser(this.getWorker()).compose(res, boas,
        IParser.OutputStyle.PRETTY,
        null);
        return (StructureMap) new org.hl7.fhir.r5.formats.JsonParser().parse( new ByteArrayInputStream(boas.toByteArray()));				
      } catch (IOException e) {
      throw new FHIRException(e.getMessage(), e);
    }    
  }
  // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777

  public StructureDefinition getTargetType(StructureMap map) throws FHIRException {
    boolean found = false;
    StructureDefinition res = null;
    for (StructureMapStructureComponent uses : map.getStructure()) {
      if (uses.getMode() == StructureMapModelMode.TARGET) {
        if (found)
          throw new FHIRException("Multiple targets found in map " + map.getUrl());
        found = true;
        res = worker.fetchResource(StructureDefinition.class, uses.getUrl());
        if (res == null)
          throw new FHIRException("Unable to find " + uses.getUrl() + " referenced from map " + map.getUrl());
      }
    }
    if (res == null)
      throw new FHIRException("No targets found in map " + map.getUrl());
    return res;
  }

  private void log(String cnt) {
    if (debug) {
      if (getServices() != null)
        getServices().log(cnt);
      else
        System.out.println(cnt);
    }
  }

  /**
   * Given an item, return all the children that conform to the pattern described in name
   * <p>
   * Possible patterns:
   * - a simple name (which may be the base of a name with [] e.g. value[x])
   * - a name with a type replacement e.g. valueCodeableConcept
   * - * which means all children
   * - ** which means all descendents
   *
   * @param item
   * @param name
   * @param result
   * @throws FHIRException
   */
  protected void getChildrenByName(Base item, String name, List<Base> result) throws FHIRException {
    for (Base v : item.listChildrenByName(name, true))
      if (v != null)
        result.add(v);
  }

  public void transform(Object appInfo, Base source, StructureMap map, Base target) throws FHIRException {
    TransformContext context = new TransformContext(appInfo);
    log("Start Transform " + map.getUrl());
    StructureMapGroupComponent g = map.getGroup().get(0);

    Variables vars = new Variables();
    vars.add(VariableMode.INPUT, getInputName(g, StructureMapInputMode.SOURCE, "source"), source);
    if (target != null)
      vars.add(VariableMode.OUTPUT, getInputName(g, StructureMapInputMode.TARGET, "target"), target);
    else if (getInputName(g, StructureMapInputMode.TARGET, null) != null) {
      String type = getInputType(g, StructureMapInputMode.TARGET);
      throw new FHIRException("not handled yet: creating a type of " + type);
    }

    executeGroup("", context, map, vars, g, true);
    if (target instanceof Element)
      ((Element) target).sort();
  }

  private String getInputType(StructureMapGroupComponent g, StructureMapInputMode mode) {
    String type = null;
    for (StructureMapGroupInputComponent inp : g.getInput()) {
      if (inp.getMode() == mode)
        if (type != null)
          throw new DefinitionException("This engine does not support multiple source inputs");
        else
          type = inp.getType();
    }
    return type;
  }

  private String getInputName(StructureMapGroupComponent g, StructureMapInputMode mode, String def) throws DefinitionException {
    String name = null;
    for (StructureMapGroupInputComponent inp : g.getInput()) {
      if (inp.getMode() == mode)
        if (name != null)
          throw new DefinitionException("This engine does not support multiple source inputs");
        else
          name = inp.getName();
    }
    return name == null ? def : name;
  }

  private void executeGroup(String indent, TransformContext context, StructureMap map, Variables vars, StructureMapGroupComponent group, boolean atRoot) throws FHIRException {
    log(indent + "Group : " + group.getName() + "; vars = " + vars.summary());
    // todo: check inputs
    if (group.hasExtends()) {
      ResolvedGroup rg = resolveGroupReference(map, group, group.getExtends());
      executeGroup(indent + " ", context, rg.getTargetMap(), vars, rg.getTargetGroup(), false);
    }

    for (StructureMapGroupRuleComponent r : group.getRule()) {
      executeRule(indent + "  ", context, map, vars, group, r, atRoot);
    }
  }

  private void executeRule(String indent, TransformContext context, StructureMap map, Variables vars, StructureMapGroupComponent group, StructureMapGroupRuleComponent rule, boolean atRoot) throws FHIRException {
    log(indent + "rule : " + rule.getName() + "; vars = " + vars.summary());
    Variables srcVars = vars.copy();
    if (rule.getSource().size() != 1)
      throw new FHIRException("Rule \"" + rule.getName() + "\": not handled yet");
    List<Variables> source = processSource(rule.getName(), context, srcVars, rule.getSource().get(0), map.getUrl(), indent);
    if (source != null) {
      for (Variables v : source) {
        for (StructureMapGroupRuleTargetComponent t : rule.getTarget()) {
          processTarget(map.getName()+"|"+group.getName()+"|"+rule.getName(), context, v, map, group, t, rule.getSource().size() == 1 ? rule.getSourceFirstRep().getVariable() : null, atRoot, vars);
        }
        if (rule.hasRule()) {
          for (StructureMapGroupRuleComponent childrule : rule.getRule()) {
            executeRule(indent + "  ", context, map, v, group, childrule, false);
          }
          // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
        } else if (rule.hasDependent() && !checkisSimple(rule)) {
          for (StructureMapGroupRuleDependentComponent dependent : rule.getDependent()) {
            executeDependency(indent + "  ", context, map, v, group, dependent);
          }
          // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
        } else if (checkisSimple(rule)) {
          // simple inferred, map by type
          if (debug) {
            log(v.summary());
          }
          Base src = v.get(VariableMode.INPUT, rule.getSourceFirstRep().getVariable());
          Base tgt = v.get(VariableMode.OUTPUT, rule.getTargetFirstRep().getVariable());
          String srcType = src.fhirType();
          String tgtType = tgt.fhirType();
          ResolvedGroup defGroup = resolveGroupByTypes(map, rule.getName(), group, srcType, tgtType);
          Variables vdef = new Variables();
          vdef.add(VariableMode.INPUT, defGroup.getTargetGroup().getInput().get(0).getName(), src);
          vdef.add(VariableMode.OUTPUT, defGroup.getTargetGroup().getInput().get(1).getName(), tgt);
          executeGroup(indent + "  ", context, defGroup.getTargetMap(), vdef, defGroup.getTargetGroup(), false);
        }
      }
    }
  }

  private void executeDependency(String indent, TransformContext context, StructureMap map, Variables vin, StructureMapGroupComponent group, StructureMapGroupRuleDependentComponent dependent) throws FHIRException {
    ResolvedGroup rg = resolveGroupReference(map, group, dependent.getName());

    if (rg.getTargetGroup().getInput().size() != dependent.getParameter().size()) {
      throw new FHIRException("Rule '" + dependent.getName() + "' has " + rg.getTargetGroup().getInput().size() + " but the invocation has " + dependent.getParameter().size() + " variables");
    }
    Variables v = new Variables();
    for (int i = 0; i < rg.getTargetGroup().getInput().size(); i++) {
      StructureMapGroupInputComponent input = rg.getTargetGroup().getInput().get(i);
      StructureMapGroupRuleTargetParameterComponent rdp = dependent.getParameter().get(i);
      String var = rdp.getValue().primitiveValue();
      VariableMode mode = input.getMode() == StructureMapInputMode.SOURCE ? VariableMode.INPUT : VariableMode.OUTPUT;
      Base vv = vin.get(mode, var);
      if (vv == null && mode == VariableMode.INPUT) //* once source, always source. but target can be treated as source at user convenient
        vv = vin.get(VariableMode.OUTPUT, var);
      if (vv == null)
        throw new FHIRException("Rule '" + dependent.getName() + "' " + mode.toString() + " variable '" + input.getName() + "' named as '" + var + "' has no value (vars = " + vin.summary() + ")");
      v.add(mode, input.getName(), vv);
    }
    executeGroup(indent + "  ", context, rg.getTargetMap(), v, rg.getTargetGroup(), false);
  }

  private String determineTypeFromSourceType(StructureMap map, StructureMapGroupComponent source, Base base, String[] types) throws FHIRException {
    String type = base.fhirType();
    String kn = "type^" + type;
    if (source.hasUserData(kn))
      return source.getUserString(kn);

    ResolvedGroup res = new ResolvedGroup(null, null);
    for (StructureMapGroupComponent grp : map.getGroup()) {
      if (matchesByType(map, grp, type)) {
        if (res.getTargetMap() == null) {
          res.setTargetMap(map);
          res.setTargetGroup(grp);
        } else
          throw new FHIRException("Multiple possible matches looking for default rule for '" + type + "'");
      }
    }
    if (res.getTargetMap() != null) {
      String result = getActualType(res.getTargetMap(), res.getTargetGroup().getInput().get(1).getType());
      source.setUserData(kn, result);
      return result;
    }

    for (UriType imp : map.getImport()) {
      List<StructureMap> impMapList = findMatchingMaps(imp.getValue());
      if (impMapList.size() == 0)
        throw new FHIRException("Unable to find map(s) for " + imp.getValue());
      for (StructureMap impMap : impMapList) {
        if (!impMap.getUrl().equals(map.getUrl())) {
          for (StructureMapGroupComponent grp : impMap.getGroup()) {
            if (matchesByType(impMap, grp, type)) {
              if (res.getTargetMap() == null) {
                res.setTargetMap(impMap);
                res.setTargetGroup(grp);
              } else
                throw new FHIRException("Multiple possible matches for default rule for '" + type + "' in " + res.getTargetMap().getUrl() + " (" + res.getTargetGroup().getName() + ") and " + impMap.getUrl() + " (" + grp.getName() + ")");
            }
          }
        }
      }
    }
    if (res.getTargetGroup() == null)
      throw new FHIRException("No matches found for default rule for '" + type + "' from " + map.getUrl());
    String result = getActualType(res.getTargetMap(), res.getTargetGroup().getInput().get(1).getType()); // should be .getType, but R2...
    source.setUserData(kn, result);
    return result;
  }

  private List<StructureMap> findMatchingMaps(String value) {
    List<StructureMap> res = new ArrayList<StructureMap>();
    if (value.contains("*")) {
      for (StructureMap sm : worker.fetchResourcesByType(StructureMap.class)) {
        if (urlMatches(value, sm.getUrl())) {
          res.add(sm);
        }
      }
    } else {
      StructureMap sm = worker.fetchResource(StructureMap.class, value);
      if (sm != null)
        res.add(sm);
    }
    Set<String> check = new HashSet<String>();
    for (StructureMap sm : res) {
      if (check.contains(sm.getUrl()))
        throw new FHIRException("duplicate");
      else
        check.add(sm.getUrl());
    }
    return res;
  }

  private boolean urlMatches(String mask, String url) {
    return url.length() > mask.length() && url.startsWith(mask.substring(0, mask.indexOf("*"))) && url.endsWith(mask.substring(mask.indexOf("*") + 1));
  }

  private ResolvedGroup resolveGroupByTypes(StructureMap map, String ruleid, StructureMapGroupComponent source, String srcType, String tgtType) throws FHIRException {
    String kn = "types^" + srcType + ":" + tgtType;
    if (source.hasUserData(kn))
      return (ResolvedGroup) source.getUserData(kn);

    ResolvedGroup res = new ResolvedGroup(null, null);
    for (StructureMapGroupComponent grp : map.getGroup()) {
      if (matchesByType(map, grp, srcType, tgtType)) {
        if (res.getTargetMap() == null) {
          res.setTargetMap(map);
          res.setTargetGroup(grp);
        } else
          throw new FHIRException("Multiple possible matches looking for rule for '" + srcType + "/" + tgtType + "', from rule '" + ruleid + "'");
      }
    }
    if (res.getTargetMap() != null) {
      source.setUserData(kn, res);
      return res;
    }

    for (UriType imp : map.getImport()) {
      List<StructureMap> impMapList = findMatchingMaps(imp.getValue());
      if (impMapList.size() == 0)
        throw new FHIRException("Unable to find map(s) for " + imp.getValue());
      for (StructureMap impMap : impMapList) {
        if (!impMap.getUrl().equals(map.getUrl())) {
          for (StructureMapGroupComponent grp : impMap.getGroup()) {
            if (matchesByType(impMap, grp, srcType, tgtType)) {
              if (res.getTargetMap() == null) {
                res.setTargetMap(impMap);
                res.setTargetGroup(grp);
              } else
                throw new FHIRException("Multiple possible matches for rule for '" + srcType + "/" + tgtType + "' in " + res.getTargetMap().getUrl() + " and " + impMap.getUrl() + ", from rule '" + ruleid + "'");
            }
          }
        }
      }
    }
    if (res.getTargetGroup() == null)
      throw new FHIRException("No matches found for rule for '" + srcType + " to " + tgtType + "' from " + map.getUrl() + ", from rule '" + ruleid + "'");
    source.setUserData(kn, res);
    return res;
  }


  private boolean matchesByType(StructureMap map, StructureMapGroupComponent grp, String type) throws FHIRException {
    if (grp.getTypeMode() != StructureMapGroupTypeMode.TYPEANDTYPES)
      return false;
    if (grp.getInput().size() != 2 || grp.getInput().get(0).getMode() != StructureMapInputMode.SOURCE || grp.getInput().get(1).getMode() != StructureMapInputMode.TARGET)
      return false;
    return matchesType(map, type, grp.getInput().get(0).getType());
  }

  private boolean matchesByType(StructureMap map, StructureMapGroupComponent grp, String srcType, String tgtType) throws FHIRException {
    if (!grp.hasTypeMode())
      return false;
    if (grp.getInput().size() != 2 || grp.getInput().get(0).getMode() != StructureMapInputMode.SOURCE || grp.getInput().get(1).getMode() != StructureMapInputMode.TARGET)
      return false;
    if (!grp.getInput().get(0).hasType() || !grp.getInput().get(1).hasType())
      return false;
    return matchesType(map, srcType, grp.getInput().get(0).getType()) && matchesType(map, tgtType, grp.getInput().get(1).getType());
  }

  private boolean matchesType(StructureMap map, String actualType, String statedType) throws FHIRException {
    // check the aliases
    for (StructureMapStructureComponent imp : map.getStructure()) {
      if (imp.hasAlias() && statedType.equals(imp.getAlias())) {
        StructureDefinition sd = worker.fetchResource(StructureDefinition.class, imp.getUrl());
        if (sd != null)
          statedType = sd.getType();
        break;
      }
    }

    // patch https://github.com/ahdis/matchbox/issues/67 actualType is also not prefixed with the canonical URL (see TestObservationCondition)
    for (StructureMapStructureComponent imp : map.getStructure()) {
      if (imp.hasAlias() && actualType.equals(imp.getAlias())) {
        StructureDefinition sd = worker.fetchResource(StructureDefinition.class, imp.getUrl());
        if (sd != null)
          actualType = sd.getType();
        break;
      }
    }

    if (Utilities.isAbsoluteUrl(actualType)) {
      StructureDefinition sd = worker.fetchResource(StructureDefinition.class, actualType);
      if (sd != null)
        actualType = sd.getType();
    }
    if (Utilities.isAbsoluteUrl(statedType)) {
      StructureDefinition sd = worker.fetchResource(StructureDefinition.class, statedType);
      if (sd != null)
        statedType = sd.getType();
    }
    return actualType.equals(statedType);
  }

  private String getActualType(StructureMap map, String statedType) throws FHIRException {
    // check the aliases
    for (StructureMapStructureComponent imp : map.getStructure()) {
      if (imp.hasAlias() && statedType.equals(imp.getAlias())) {
        StructureDefinition sd = worker.fetchResource(StructureDefinition.class, imp.getUrl());
        if (sd == null)
          throw new FHIRException("Unable to resolve structure " + imp.getUrl());
        return sd.getId(); // should be sd.getType(), but R2...
      }
    }
    return statedType;
  }


  private ResolvedGroup resolveGroupReference(StructureMap map, StructureMapGroupComponent source, String name) throws FHIRException {
    String kn = "ref^" + name;
    if (source.hasUserData(kn))
      return (ResolvedGroup) source.getUserData(kn);

    ResolvedGroup res = new ResolvedGroup(null, null);
    for (StructureMapGroupComponent grp : map.getGroup()) {
      if (grp.getName().equals(name)) {
        if (res.getTargetMap() == null) {
          res.setTargetMap(map);
          res.setTargetGroup(grp);
        } else
          throw new FHIRException("Multiple possible matches for rule '" + name + "'");
      }
    }
    if (res.getTargetMap() != null) {
      source.setUserData(kn, res);
      return res;
    }

    for (UriType imp : map.getImport()) {
      List<StructureMap> impMapList = findMatchingMaps(imp.getValue());
      if (impMapList.size() == 0)
        throw new FHIRException("Unable to find map(s) for " + imp.getValue());
      for (StructureMap impMap : impMapList) {
        if (!impMap.getUrl().equals(map.getUrl())) {
          for (StructureMapGroupComponent grp : impMap.getGroup()) {
            if (grp.getName().equals(name)) {
              if (res.getTargetMap() == null) {
                res.setTargetMap(impMap);
                res.setTargetGroup(grp);
              } else
                throw new FHIRException("Multiple possible matches for rule group '" + name + "' in " +
                  res.getTargetMap().getUrl() + "#" + res.getTargetGroup().getName() + " and " +
                  impMap.getUrl() + "#" + grp.getName());
            }
          }
        }
      }
    }
    if (res.getTargetGroup() == null)
      throw new FHIRException("No matches found for rule '" + name + "'. Reference found in " + map.getUrl());
    source.setUserData(kn, res);
    return res;
  }

  private List<Variables> processSource(String ruleId, TransformContext context, Variables vars, StructureMapGroupRuleSourceComponent src, String pathForErrors, String indent) throws FHIRException {
    List<Base> items;
    if (src.getContext().equals("@search")) {
      ExpressionNode expr = (ExpressionNode) src.getUserData(MAP_SEARCH_EXPRESSION);
      if (expr == null) {
        expr = fpe.parse(src.getElement());
        src.setUserData(MAP_SEARCH_EXPRESSION, expr);
      }
      String search = fpe.evaluateToString(vars, null, null, new StringType(), expr); // string is a holder of nothing to ensure that variables are processed correctly 
      items = services.performSearch(context.getAppInfo(), search);
    } else {
      items = new ArrayList<Base>();
      Base b = vars.get(VariableMode.INPUT, src.getContext());
      if (b == null)
        throw new FHIRException("Unknown input variable " + src.getContext() + " in " + pathForErrors + " rule " + ruleId + " (vars = " + vars.summary() + ")");

      if (!src.hasElement())
        items.add(b);
      else {
        getChildrenByName(b, src.getElement(), items);
        if (items.size() == 0 && src.hasDefaultValue())
          items.add(src.getDefaultValueElement());
      }
    }

    if (src.hasType()) {
      List<Base> remove = new ArrayList<Base>();
      for (Base item : items) {
        if (item != null && !isType(item, src.getType())) {
          remove.add(item);
        }
      }
      items.removeAll(remove);
    }
    
    if (src.hasCondition()) {
      ExpressionNode expr = (ExpressionNode) src.getUserData(MAP_WHERE_EXPRESSION);
      if (expr == null) {
        expr = fpe.parse(src.getCondition());
        src.setUserData(MAP_WHERE_EXPRESSION, expr);
      }
      List<Base> remove = new ArrayList<Base>();
      for (Base item : items) {
        Variables varsForSource = vars.copy();
        if (src.hasVariable()) {
            varsForSource.add(VariableMode.INPUT, src.getVariable(), item);
        }
        if (!fpe.evaluateToBoolean(varsForSource, null, null, item, expr)) {
            // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
          log(indent + "  condition [" + src.getCondition() + "] for " + item.toString() + (src.hasVariable() ? " with variable "+ src.getVariable(): "" ) + " : false");
          remove.add(item);
        } else
            // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
          log(indent + "  condition [" + src.getCondition() + "] for " + item.toString() + (src.hasVariable() ? " with variable "+ src.getVariable(): "" ) + " : true");
      }
      items.removeAll(remove);
    }

    if (src.hasCheck()) {
      ExpressionNode expr = (ExpressionNode) src.getUserData(MAP_WHERE_CHECK);
      if (expr == null) {
        expr = fpe.parse(src.getCheck());
        src.setUserData(MAP_WHERE_CHECK, expr);
      }
      for (Base item : items) {
        Variables varsForSource = vars.copy();
        if (src.hasVariable()) {
            varsForSource.add(VariableMode.INPUT, src.getVariable(), item);
        }
        if (!fpe.evaluateToBoolean(varsForSource, null, null, item, expr))
          throw new FHIRException("Rule \"" + ruleId + "\": Check condition failed");
      }
    }

    if (src.hasLogMessage()) {
      ExpressionNode expr = (ExpressionNode) src.getUserData(MAP_WHERE_LOG);
      if (expr == null) {
        expr = fpe.parse(src.getLogMessage());
        src.setUserData(MAP_WHERE_LOG, expr);
      }
      CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
      for (Base item : items) {
        Variables varsForSource = vars.copy();
        if (src.hasVariable()) {
            varsForSource.add(VariableMode.INPUT, src.getVariable(), item);
        }
        b.appendIfNotNull(fpe.evaluateToString(varsForSource, null, null, item, expr));
      }
      if (b.length() > 0)
        services.log(b.toString());
    }
    

    if (src.hasListMode() && !items.isEmpty()) {
      switch (src.getListMode()) {
        case FIRST:
          Base bt = items.get(0);
          items.clear();
          items.add(bt);
          break;
        case NOTFIRST:
          if (items.size() > 0)
            items.remove(0);
          break;
        case LAST:
          bt = items.get(items.size() - 1);
          items.clear();
          items.add(bt);
          break;
        case NOTLAST:
          if (items.size() > 0)
            items.remove(items.size() - 1);
          break;
        case ONLYONE:
          if (items.size() > 1)
            throw new FHIRException("Rule \"" + ruleId + "\": Check condition failed: the collection has more than one item");
          break;
        case NULL:
      }
    }
    List<Variables> result = new ArrayList<Variables>();
    for (Base r : items) {
      Variables v = vars.copy();
      if (src.hasVariable())
        v.add(VariableMode.INPUT, src.getVariable(), r);
      result.add(v);
    }
    return result;
  }


  private boolean isType(Base item, String type) {
    return type.equals(item.fhirType());
  }

  private void processTarget(String rulePath, TransformContext context, Variables vars, StructureMap map, StructureMapGroupComponent group, StructureMapGroupRuleTargetComponent tgt, String srcVar, boolean atRoot, Variables sharedVars) throws FHIRException {
    Base dest = null;
    if (tgt.hasContext()) {
      dest = vars.get(VariableMode.OUTPUT, tgt.getContext());
      if (dest == null) {
        throw new FHIRException("Rul \"" + rulePath + "\": target context not known: " + tgt.getContext());
      }
    }
    Base v = null;
    if (tgt.hasTransform()) {
      v = runTransform(rulePath, context, map, group, tgt, vars, dest, tgt.getElement(), srcVar, atRoot);
      if (v != null && dest != null) {
        try {
          v = dest.setProperty(tgt.getElement().hashCode(), tgt.getElement(), v); // reset v because some implementations may have to rewrite v when setting the value
        } catch (Exception e) {
          throw new FHIRException("Error setting "+tgt.getElement()+" on "+dest.fhirType()+" for rule "+rulePath+" to value "+v.toString()+": "+e.getMessage(), e);
        }
      }
    } else if (dest != null) {
      if (tgt.hasListMode(StructureMapTargetListMode.SHARE)) {
        v = sharedVars.get(VariableMode.SHARED, tgt.getListRuleId());
        if (v == null) {
          v = dest.makeProperty(tgt.getElement().hashCode(), tgt.getElement());
          sharedVars.add(VariableMode.SHARED, tgt.getListRuleId(), v);
        }
      } else if (tgt.hasElement()) {
        v = dest.makeProperty(tgt.getElement().hashCode(), tgt.getElement());
      } else {
        v = dest;
      }
    }
    if (tgt.hasVariable() && v != null)
      vars.add(VariableMode.OUTPUT, tgt.getVariable(), v);
  }
  
  private Base runTransform(String rulePath, TransformContext context, StructureMap map, StructureMapGroupComponent group, StructureMapGroupRuleTargetComponent tgt, Variables vars, Base dest, String element, String srcVar, boolean root) throws FHIRException {
    try {
      switch (tgt.getTransform()) {
        case CREATE:
          String tn;
          if (tgt.getParameter().isEmpty()) {
            // we have to work out the type. First, we see if there is a single type for the target. If there is, we use that
            String[] types = dest.getTypesForProperty(element.hashCode(), element);
            if (types.length == 1 && !"*".equals(types[0]) && !types[0].equals("Resource"))
              tn = types[0];
            else if (srcVar != null) {
              tn = determineTypeFromSourceType(map, group, vars.get(VariableMode.INPUT, srcVar), types);
            } else
              throw new FHIRException("Cannot determine type implicitly because there is no single input variable");
          } else {
            tn = getParamStringNoNull(vars, tgt.getParameter().get(0), tgt.toString());
            // ok, now we resolve the type name against the import statements
            for (StructureMapStructureComponent uses : map.getStructure()) {
              if (uses.getMode() == StructureMapModelMode.TARGET && uses.hasAlias() && tn.equals(uses.getAlias())) {
                tn = uses.getUrl();
                break;
              }
            }
          }
          // matchbox patch https://github.com/ahdis/matchbox/issues/264
          Base res = services != null ? services.createType(context.getAppInfo(), tn, profileUtilities) : typeFactory(tn);
          if (res.isResource() && !res.fhirType().equals("Parameters")) {
//	        res.setIdBase(tgt.getParameter().size() > 1 ? getParamString(vars, tgt.getParameter().get(0)) : UUID.randomUUID().toString().toLowerCase());
            if (services != null)
              res = services.createResource(context.getAppInfo(), res, root);
          }
          if (tgt.hasUserData("profile"))
            res.setUserData("profile", tgt.getUserData("profile"));
          return res;
        case COPY:
          return getParam(vars, tgt.getParameter().get(0));
        case EVALUATE:
          ExpressionNode expr = (ExpressionNode) tgt.getUserData(MAP_EXPRESSION);
          if (expr == null) {
            expr = fpe.parse(getParamStringNoNull(vars, tgt.getParameter().get(tgt.getParameter().size() - 1), tgt.toString()));
            tgt.setUserData(MAP_EXPRESSION, expr);
          }
          List<Base> v = fpe.evaluate(vars, null, null, tgt.getParameter().size() == 2 ? getParam(vars, tgt.getParameter().get(0)) : new BooleanType(false), expr);
          if (v.size() == 0)
            return null;
          else if (v.size() != 1)
            throw new FHIRException("Rule \"" + rulePath+ "\": Evaluation of " + expr.toString() + " returned " + v.size() + " objects");
          else
            return v.get(0);

        case TRUNCATE:
          String src = getParamString(vars, tgt.getParameter().get(0));
          String len = getParamStringNoNull(vars, tgt.getParameter().get(1), tgt.toString());
          if (Utilities.isInteger(len)) {
            int l = Integer.parseInt(len);
            if (src.length() > l)
              src = src.substring(0, l);
          }
          return new StringType(src);
        case ESCAPE:
          throw new FHIRException("Rule \"" + rulePath + "\": Transform " + tgt.getTransform().toCode() + " not supported yet");
        case CAST:
          src = getParamString(vars, tgt.getParameter().get(0));
          if (tgt.getParameter().size() == 1)
            throw new FHIRException("Implicit type parameters on cast not yet supported");
          String t = getParamString(vars, tgt.getParameter().get(1));
          switch(t) {
            case "boolean":
              return new BooleanType(src);
            case "integer":
              return new IntegerType(src);
            case  "integer64":
              return new Integer64Type(src);
            case  "string":
              return new StringType(src);
            case  "decimal":
              return new DecimalType(src);
            case  "uri":
              return new UriType(src);
            case  "base64Binary":
              return new Base64BinaryType(src);
            case  "instant":
              return new InstantType(src);
            case  "date":
              return new DateType(src);
            case  "dateTime":
              return new DateTimeType(src);
            case  "time":
              return new TimeType(src);
            case  "code":
              return new CodeType(src);
            case  "oid":
              return new OidType(src);
            case  "id":
              return new IdType(src);
            case  "markdown":
              return new MarkdownType(src);
            case  "unsignedInt":
              return new UnsignedIntType(src);
            case  "positiveInt":
              return new PositiveIntType(src);
            case  "uuid":
              return new UuidType(src);
            case  "url":
              return new UrlType(src);
            case  "canonical":
              return new CanonicalType(src);
          }
          throw new FHIRException("cast to " + t + " not yet supported");
        case APPEND:
          StringBuilder sb = new StringBuilder(getParamString(vars, tgt.getParameter().get(0)));
          for (int i = 1; i < tgt.getParameter().size(); i++)
            sb.append(getParamString(vars, tgt.getParameter().get(i)));
          return new StringType(sb.toString());
        case TRANSLATE:
          return translate(context, map, vars, tgt.getParameter());
        case REFERENCE:
          Base b = getParam(vars, tgt.getParameter().get(0));
          if (b == null)
            throw new FHIRException("Rule \"" + rulePath + "\": Unable to find parameter " + ((IdType) tgt.getParameter().get(0).getValue()).asStringValue());
          if (!b.isResource())
            throw new FHIRException("Rule \"" + rulePath + "\": Transform engine cannot point at an element of type " + b.fhirType());
          else {
            String id = b.getIdBase();
            if (id == null) {
              id = UUID.randomUUID().toString().toLowerCase();
              b.setIdBase(id);
            }
            return new StringType(b.fhirType() + "/" + id);
          }
        case DATEOP:
          throw new FHIRException("Rule \"" + rulePath + "\": Transform " + tgt.getTransform().toCode() + " not supported yet");
        case UUID:
          return new IdType(UUID.randomUUID().toString());
        case POINTER:
          b = getParam(vars, tgt.getParameter().get(0));
          if (b instanceof Resource)
            return new UriType("urn:uuid:" + ((Resource) b).getId());
          else
            throw new FHIRException("Rule \"" + rulePath + "\": Transform engine cannot point at an element of type " + b.fhirType());
        case CC:
          CodeableConcept cc = new CodeableConcept();
          cc.addCoding(buildCoding(getParamStringNoNull(vars, tgt.getParameter().get(0), tgt.toString()), getParamStringNoNull(vars, tgt.getParameter().get(1), tgt.toString())));
          return cc;
        case C:
          Coding c = buildCoding(getParamStringNoNull(vars, tgt.getParameter().get(0), tgt.toString()), getParamStringNoNull(vars, tgt.getParameter().get(1), tgt.toString()));
          return c;
        default:
          throw new FHIRException("Rule \"" + rulePath + "\": Transform Unknown: " + tgt.getTransform().toCode());
      }
    } catch (Exception e) {
      throw new FHIRException("Exception executing transform " + tgt.toString() + " on Rule \"" + rulePath + "\": " + e.getMessage(), e);
    }
  }

  private Base typeFactory(String tn) {
    if (Utilities.isAbsoluteUrl(tn) && !tn.startsWith("http://hl7.org/fhir/StructureDefinition")) {
      StructureDefinition sd = worker.fetchTypeDefinition(tn);
      if (sd == null) {
        if (Utilities.existsInList(tn, "http://hl7.org/fhirpath/System.String")) {
          sd = worker.fetchTypeDefinition("string"); 
        }
      }
      if (sd == null) {
        throw new FHIRException("Unable to create type "+tn);
      } else {
        // matchbox pr https://github.com/hapifhir/org.hl7.fhir.core/issues/1777
        return Manager.build(worker, sd, profileUtilities);
      }
    } else {
      return ResourceFactory.createResourceOrType(tn);
    }
  }


  private Coding buildCoding(String uri, String code) throws FHIRException {
    // if we can get this as a valueSet, we will
    String system = null;
    String display = null;
    String version = null;
    ValueSet vs = Utilities.noString(uri) ? null : worker.fetchResourceWithException(ValueSet.class, uri);
    if (vs != null) {
      ValueSetExpansionOutcome vse = worker.expandVS(vs, true, false);
      if (vse.getError() != null)
        throw new FHIRException(vse.getError());
      CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
      for (ValueSetExpansionContainsComponent t : vse.getValueset().getExpansion().getContains()) {
        if (t.hasCode())
          b.append(t.getCode());
        if (code.equals(t.getCode()) && t.hasSystem()) {
          system = t.getSystem();
          version = t.getVersion();
          display = t.getDisplay();
          break;
        }
        if (code.equalsIgnoreCase(t.getDisplay()) && t.hasSystem()) {
          system = t.getSystem();
          version = t.getVersion();
          display = t.getDisplay();
          break;
        }
      }
      if (system == null)
        throw new FHIRException("The code '" + code + "' is not in the value set '" + uri + "' (valid codes: " + b.toString() + "; also checked displays)");
    } else {
      system = uri;
    }
    ValidationResult vr = worker.validateCode(terminologyServiceOptions.withVersionFlexible(true), system, version, code, null);
    if (vr != null && vr.getDisplay() != null)
      display = vr.getDisplay();
    return new Coding().setSystem(system).setCode(code).setDisplay(display);
  }


  private String getParamStringNoNull(Variables vars, StructureMapGroupRuleTargetParameterComponent parameter, String message) throws FHIRException {
    Base b = getParam(vars, parameter);
    if (b == null)
      throw new FHIRException("Unable to find a value for " + parameter.toString() + ". Context: " + message);
    if (!b.hasPrimitiveValue())
      throw new FHIRException("Found a value for " + parameter.toString() + ", but it has a type of " + b.fhirType() + " and cannot be treated as a string. Context: " + message);
    return b.primitiveValue();
  }

  private String getParamString(Variables vars, StructureMapGroupRuleTargetParameterComponent parameter) throws DefinitionException {
    Base b = getParam(vars, parameter);
    if (b == null || !b.hasPrimitiveValue())
      return null;
    return b.primitiveValue();
  }


  private Base getParam(Variables vars, StructureMapGroupRuleTargetParameterComponent parameter) throws DefinitionException {
    DataType p = parameter.getValue();
    if (!(p instanceof IdType))
      return p;
    else {
      String n = ((IdType) p).asStringValue();
      Base b = vars.get(VariableMode.INPUT, n);
      if (b == null)
        b = vars.get(VariableMode.OUTPUT, n);
      if (b == null)
        throw new DefinitionException("Variable " + n + " not found (" + vars.summary() + ")");
      return b;
    }
  }


  private Base translate(TransformContext context, StructureMap map, Variables vars, List<StructureMapGroupRuleTargetParameterComponent> parameter) throws FHIRException {
    Base src = getParam(vars, parameter.get(0));
    String id = getParamString(vars, parameter.get(1));
    String fld = parameter.size() > 2 ? getParamString(vars, parameter.get(2)) : null;
    return translate(context, map, src, id, fld);
  }

  public Base translate(TransformContext context, StructureMap map, Base source, String conceptMapUrl, String fieldToReturn) throws FHIRException {
    Coding src = new Coding();
    if (source.isPrimitive()) {
      src.setCode(source.primitiveValue());
    } else if ("Coding".equals(source.fhirType())) {
      Base[] b = source.getProperty("system".hashCode(), "system", true);
      if (b.length == 1)
        src.setSystem(b[0].primitiveValue());
      b = source.getProperty("code".hashCode(), "code", true);
      if (b.length == 1)
        src.setCode(b[0].primitiveValue());
    } else if ("CE".equals(source.fhirType())) {
      Base[] b = source.getProperty("codeSystem".hashCode(), "codeSystem", true);
      if (b.length == 1)
        src.setSystem(b[0].primitiveValue());
      b = source.getProperty("code".hashCode(), "code", true);
      if (b.length == 1)
        src.setCode(b[0].primitiveValue());
    } else
      throw new FHIRException("Unable to translate source " + source.fhirType());

    String su = conceptMapUrl;
    if (conceptMapUrl.equals("http://hl7.org/fhir/ConceptMap/special-oid2uri")) {
      String uri = new ContextUtilities(worker).oid2Uri(src.getCode());
      if (uri == null)
        uri = "urn:oid:" + src.getCode();
      if ("uri".equals(fieldToReturn))
        return new UriType(uri);
      else
        throw new FHIRException("Error in return code");
    } else {
      ConceptMap cmap = null;
      if (conceptMapUrl.startsWith("#")) {
        for (Resource r : map.getContained()) {
          if (r instanceof ConceptMap && r.getId().equals(conceptMapUrl.substring(1))) {
            cmap = (ConceptMap) r;
            su = map.getUrl() + "#" + conceptMapUrl;
          }
        }
        if (cmap == null)
          throw new FHIRException("Unable to translate - cannot find map " + conceptMapUrl);
      } else {
        if (conceptMapUrl.contains("#")) {
          String[] p = conceptMapUrl.split("\\#");
          StructureMap mapU = worker.fetchResource(StructureMap.class, p[0]);
          for (Resource r : mapU.getContained()) {
            if (r instanceof ConceptMap && r.getId().equals(p[1])) {
              cmap = (ConceptMap) r;
              su = conceptMapUrl;
            }
          }
        }
        if (cmap == null)
          cmap = worker.fetchResource(ConceptMap.class, conceptMapUrl);
      }
      Coding outcome = null;
      boolean done = false;
      String message = null;
      if (cmap == null) {
        if (services == null)
          message = "No map found for " + conceptMapUrl;
        else {
          outcome = services.translate(context.getAppInfo(), src, conceptMapUrl);
          done = true;
        }
      } else {
        List<SourceElementComponentWrapper> list = new ArrayList<SourceElementComponentWrapper>();
        for (ConceptMapGroupComponent g : cmap.getGroup()) {
          for (SourceElementComponent e : g.getElement()) {
            if (!src.hasSystem() && src.getCode().equals(e.getCode()))
              list.add(new SourceElementComponentWrapper(g, e));
            else if (src.hasSystem() && src.getSystem().equals(g.getSource()) && src.getCode().equals(e.getCode()))
              list.add(new SourceElementComponentWrapper(g, e));
          }
        }
        if (list.size() == 0)
          done = true;
        else if (list.get(0).getComp().getTarget().size() == 0)
          message = "Concept map " + su + " found no translation for " + src.getCode();
        else {
          for (TargetElementComponent tgt : list.get(0).getComp().getTarget()) {
            if (tgt.getRelationship() == null || EnumSet.of(ConceptMapRelationship.RELATEDTO, ConceptMapRelationship.EQUIVALENT, ConceptMapRelationship.SOURCEISNARROWERTHANTARGET).contains(tgt.getRelationship())) {
              if (done) {
                message = "Concept map " + su + " found multiple matches for " + src.getCode();
                done = false;
              } else {
                done = true;
                outcome = new Coding().setCode(tgt.getCode()).setSystem(list.get(0).getGroup().getTarget());
              }
            }
          }
          if (!done)
            message = "Concept map " + su + " found no usable translation for " + src.getCode();
        }
      }
      if (!done)
        throw new FHIRException(message);
      if (outcome == null)
        return null;
      if ("code".equals(fieldToReturn))
        return new CodeType(outcome.getCode());
      else
        return outcome;
    }
  }


  /**
   * Given a structure map, return a set of analyses on it.
   * <p>
   * Returned:
   * - a list or profiles for what it will create. First profile is the target
   * - a table with a summary (in xhtml) for easy human undertanding of the mapping
   *
   * @param appInfo
   * @param map
   * @return
   * @throws Exception
   */
  public StructureMapAnalysis analyse(Object appInfo, StructureMap map) throws FHIRException {
    ids.clear();
    StructureMapAnalysis result = new StructureMapAnalysis();
    TransformContext context = new TransformContext(appInfo);
    VariablesForProfiling vars = new VariablesForProfiling(this, false, false);
    if (map.hasGroup()) {
      StructureMapGroupComponent start = map.getGroup().get(0);
      for (StructureMapGroupInputComponent t : start.getInput()) {
        PropertyWithType ti = resolveType(map, t.getType(), t.getMode());
        if (t.getMode() == StructureMapInputMode.SOURCE)
          vars.add(VariableMode.INPUT, t.getName(), ti);
        else
          vars.add(VariableMode.OUTPUT, t.getName(), createProfile(map, result.profiles, ti, start.getName(), start));
      }
      result.summary = new XhtmlNode(NodeType.Element, "table").setAttribute("class", "grid");
      XhtmlNode tr = result.summary.addTag("tr");
      tr.addTag("td").addTag("b").addText("Source");
      tr.addTag("td").addTag("b").addText("Target");

      log("Start Profiling Transform " + map.getUrl());
      analyseGroup("", context, map, vars, start, result);
    }
    ProfileUtilities pu = new ProfileUtilities(worker, null, pkp);
    for (StructureDefinition sd : result.getProfiles())
      pu.cleanUpDifferential(sd);
    return result;
  }


  private void analyseGroup(String indent, TransformContext context, StructureMap map, VariablesForProfiling vars, StructureMapGroupComponent group, StructureMapAnalysis result) throws FHIRException {
    log(indent + "Analyse Group : " + group.getName());
    // todo: extends
    // todo: check inputs
    XhtmlNode tr = result.summary.addTag("tr").setAttribute("class", "diff-title");
    XhtmlNode xs = tr.addTag("td");
    XhtmlNode xt = tr.addTag("td");
    for (StructureMapGroupInputComponent inp : group.getInput()) {
      if (inp.getMode() == StructureMapInputMode.SOURCE)
        noteInput(vars, inp, VariableMode.INPUT, xs);
      if (inp.getMode() == StructureMapInputMode.TARGET)
        noteInput(vars, inp, VariableMode.OUTPUT, xt);
    }
    for (StructureMapGroupRuleComponent r : group.getRule()) {
      analyseRule(indent + "  ", context, map, vars, group, r, result);
    }
  }


  private void noteInput(VariablesForProfiling vars, StructureMapGroupInputComponent inp, VariableMode mode, XhtmlNode xs) {
    VariableForProfiling v = vars.get(mode, inp.getName());
    if (v != null)
      xs.addText("Input: " + v.getProperty().getPath());
  }

  private void analyseRule(String indent, TransformContext context, StructureMap map, VariablesForProfiling vars, StructureMapGroupComponent group, StructureMapGroupRuleComponent rule, StructureMapAnalysis result) throws FHIRException {
    log(indent + "Analyse rule : " + rule.getName());
    XhtmlNode tr = result.summary.addTag("tr");
    XhtmlNode xs = tr.addTag("td");
    XhtmlNode xt = tr.addTag("td");

    VariablesForProfiling srcVars = vars.copy();
    if (rule.getSource().size() != 1)
      throw new FHIRException("Rule \"" + rule.getName() + "\": not handled yet");
    VariablesForProfiling source = analyseSource(rule.getName(), context, srcVars, rule.getSourceFirstRep(), xs);

    TargetWriter tw = new TargetWriter();
    for (StructureMapGroupRuleTargetComponent t : rule.getTarget()) {
      analyseTarget(rule.getName(), context, source, map, t, rule.getSourceFirstRep().getVariable(), tw, result.profiles, rule.getName());
    }
    tw.commit(xt);

    for (StructureMapGroupRuleComponent childrule : rule.getRule()) {
      analyseRule(indent + "  ", context, map, source, group, childrule, result);
    }
//    for (StructureMapGroupRuleDependentComponent dependent : rule.getDependent()) {
//      executeDependency(indent+"  ", context, map, v, group, dependent); // do we need group here?
//    }
  }

  private VariablesForProfiling analyseSource(String ruleId, TransformContext context, VariablesForProfiling vars, StructureMapGroupRuleSourceComponent src, XhtmlNode td) throws FHIRException {
    VariableForProfiling var = vars.get(VariableMode.INPUT, src.getContext());
    if (var == null)
      throw new FHIRException("Rule \"" + ruleId + "\": Unknown input variable " + src.getContext());
    PropertyWithType prop = var.getProperty();

    boolean optional = false;
    boolean repeating = false;

    if (src.hasCondition()) {
      optional = true;
    }

    if (src.hasElement()) {
      Property element = prop.getBaseProperty().getChild(prop.getTypes().getType(), src.getElement());
      if (element == null)
        throw new FHIRException("Rule \"" + ruleId + "\": Unknown element name " + src.getElement());
      if (element.getDefinition().getMin() == 0)
        optional = true;
      if (element.getDefinition().getMax().equals("*"))
        repeating = true;
      VariablesForProfiling result = vars.copy(optional, repeating);
      TypeDetails type = new TypeDetails(CollectionStatus.SINGLETON);
      for (TypeRefComponent tr : element.getDefinition().getType()) {
        if (!tr.hasCode())
          throw new FHIRException("Rule \"" + ruleId + "\": Element has no type");
        ProfiledType pt = new ProfiledType(tr.getWorkingCode());
        if (tr.hasProfile())
          pt.addProfiles(tr.getProfile());
        if (element.getDefinition().hasBinding())
          pt.addBinding(element.getDefinition().getBinding());
        type.addType(pt);
      }
      td.addText(prop.getPath() + "." + src.getElement());
      if (src.hasVariable())
        result.add(VariableMode.INPUT, src.getVariable(), new PropertyWithType(prop.getPath() + "." + src.getElement(), element, null, type));
      return result;
    } else {
      td.addText(prop.getPath()); // ditto!
      return vars.copy(optional, repeating);
    }
  }


  private void analyseTarget(String ruleId, TransformContext context, VariablesForProfiling vars, StructureMap map, StructureMapGroupRuleTargetComponent tgt, String tv, TargetWriter tw, List<StructureDefinition> profiles, String sliceName) throws FHIRException {
    VariableForProfiling var = null;
    if (tgt.hasContext()) {
      var = vars.get(VariableMode.OUTPUT, tgt.getContext());
      if (var == null)
        throw new FHIRException("Rule \"" + ruleId + "\": target context not known: " + tgt.getContext());
      if (!tgt.hasElement())
        throw new FHIRException("Rule \"" + ruleId + "\": Not supported yet");
    }


    TypeDetails type = null;
    if (tgt.hasTransform()) {
      type = analyseTransform(context, map, tgt, var, vars);
    } else {
      Property vp = var.getProperty().getBaseProperty().getChild(tgt.getElement(), tgt.getElement());
      if (vp == null)
        throw new FHIRException("Unknown Property " + tgt.getElement() + " on " + var.getProperty().getPath());

      type = new TypeDetails(CollectionStatus.SINGLETON, vp.getType(tgt.getElement()));
    }

    if (tgt.getTransform() == StructureMapTransform.CREATE) {
      String s = getParamString(vars, tgt.getParameter().get(0));
      if (worker.getResourceNames().contains(s))
        tw.newResource(tgt.getVariable(), s);
    } else {
      boolean mapsSrc = false;
      for (StructureMapGroupRuleTargetParameterComponent p : tgt.getParameter()) {
        DataType pr = p.getValue();
        if (pr instanceof IdType && ((IdType) pr).asStringValue().equals(tv))
          mapsSrc = true;
      }
      if (mapsSrc) {
        if (var == null)
          throw new FHIRException("Rule \"" + ruleId + "\": Attempt to assign with no context");
        tw.valueAssignment(tgt.getContext(), var.getProperty().getPath() + "." + tgt.getElement() + getTransformSuffix(tgt.getTransform()));
      } else if (tgt.hasContext()) {
        if (isSignificantElement(var.getProperty(), tgt.getElement())) {
          String td = describeTransform(tgt);
          if (td != null)
            tw.keyAssignment(tgt.getContext(), var.getProperty().getPath() + "." + tgt.getElement() + " = " + td);
        }
      }
    }
    DataType fixed = generateFixedValue(tgt);

    PropertyWithType prop = updateProfile(var, tgt.getElement(), type, map, profiles, sliceName, fixed, tgt);
    if (tgt.hasVariable())
      if (tgt.hasElement())
        vars.add(VariableMode.OUTPUT, tgt.getVariable(), prop);
      else
        vars.add(VariableMode.OUTPUT, tgt.getVariable(), prop);
  }

  private DataType generateFixedValue(StructureMapGroupRuleTargetComponent tgt) {
    if (!allParametersFixed(tgt))
      return null;
    if (!tgt.hasTransform())
      return null;
    switch (tgt.getTransform()) {
      case COPY:
        return tgt.getParameter().get(0).getValue();
      case TRUNCATE:
        return null;
      //case ESCAPE:
      //case CAST:
      //case APPEND:
      case TRANSLATE:
        return null;
      //case DATEOP,
      //case UUID,
      //case POINTER,
      //case EVALUATE,
      case CC:
        CodeableConcept cc = new CodeableConcept();
        cc.addCoding(buildCoding(tgt.getParameter().get(0).getValue(), tgt.getParameter().get(1).getValue()));
        return cc;
      case C:
        return buildCoding(tgt.getParameter().get(0).getValue(), tgt.getParameter().get(1).getValue());
      case QTY:
        return null;
      //case ID,
      //case CP,
      default:
        return null;
    }
  }

  @SuppressWarnings("rawtypes")
  private Coding buildCoding(DataType value1, DataType value2) {
    return new Coding().setSystem(((PrimitiveType) value1).asStringValue()).setCode(((PrimitiveType) value2).asStringValue());
  }

  private boolean allParametersFixed(StructureMapGroupRuleTargetComponent tgt) {
    for (StructureMapGroupRuleTargetParameterComponent p : tgt.getParameter()) {
      DataType pr = p.getValue();
      if (pr instanceof IdType)
        return false;
    }
    return true;
  }

  private String describeTransform(StructureMapGroupRuleTargetComponent tgt) throws FHIRException {
    switch (tgt.getTransform()) {
      case COPY:
        return null;
      case TRUNCATE:
        return null;
      //case ESCAPE:
      //case CAST:
      //case APPEND:
      case TRANSLATE:
        return null;
      //case DATEOP,
      //case UUID,
      //case POINTER,
      //case EVALUATE,
      case CC:
        return describeTransformCCorC(tgt);
      case C:
        return describeTransformCCorC(tgt);
      case QTY:
        return null;
      //case ID,
      //case CP,
      default:
        return null;
    }
  }

  @SuppressWarnings("rawtypes")
  private String describeTransformCCorC(StructureMapGroupRuleTargetComponent tgt) throws FHIRException {
    if (tgt.getParameter().size() < 2)
      return null;
    DataType p1 = tgt.getParameter().get(0).getValue();
    DataType p2 = tgt.getParameter().get(1).getValue();
    if (p1 instanceof IdType || p2 instanceof IdType)
      return null;
    if (!(p1 instanceof PrimitiveType) || !(p2 instanceof PrimitiveType))
      return null;
    String uri = ((PrimitiveType) p1).asStringValue();
    String code = ((PrimitiveType) p2).asStringValue();
    if (Utilities.noString(uri))
      throw new FHIRException("Describe Transform, but the uri is blank");
    if (Utilities.noString(code))
      throw new FHIRException("Describe Transform, but the code is blank");
    Coding c = buildCoding(uri, code);
    return c.getSystem() + "#" + c.getCode() + (c.hasDisplay() ? "(" + c.getDisplay() + ")" : "");
  }


  private boolean isSignificantElement(PropertyWithType property, String element) {
    if ("Observation".equals(property.getPath()))
      return "code".equals(element);
    else if ("Bundle".equals(property.getPath()))
      return "type".equals(element);
    else
      return false;
  }

  private String getTransformSuffix(StructureMapTransform transform) {
    switch (transform) {
      case COPY:
        return "";
      case TRUNCATE:
        return " (truncated)";
      //case ESCAPE:
      //case CAST:
      //case APPEND:
      case TRANSLATE:
        return " (translated)";
      //case DATEOP,
      //case UUID,
      //case POINTER,
      //case EVALUATE,
      case CC:
        return " (--> CodeableConcept)";
      case C:
        return " (--> Coding)";
      case QTY:
        return " (--> Quantity)";
      //case ID,
      //case CP,
      default:
        return " {??)";
    }
  }

  private PropertyWithType updateProfile(VariableForProfiling var, String element, TypeDetails type, StructureMap map, List<StructureDefinition> profiles, String sliceName, DataType fixed, StructureMapGroupRuleTargetComponent tgt) throws FHIRException {
    if (var == null) {
      assert (Utilities.noString(element));
      // 1. start the new structure definition
      StructureDefinition sdn = worker.fetchResource(StructureDefinition.class, type.getType());
      if (sdn == null)
        throw new FHIRException("Unable to find definition for " + type.getType());
      ElementDefinition edn = sdn.getSnapshot().getElementFirstRep();
      PropertyWithType pn = createProfile(map, profiles, new PropertyWithType(sdn.getId(), new Property(worker, edn, sdn), null, type), sliceName, tgt);
      return pn;
    } else {
      assert (!Utilities.noString(element));
      Property pvb = var.getProperty().getBaseProperty();
      Property pvd = var.getProperty().getProfileProperty();
      Property pc = pvb.getChild(element, var.getProperty().getTypes());
      if (pc == null)
        throw new DefinitionException("Unable to find a definition for " + pvb.getDefinition().getPath() + "." + element);

      // the profile structure definition (derived)
      StructureDefinition sd = var.getProperty().getProfileProperty().getStructure();
      ElementDefinition ednew = sd.getDifferential().addElement();
      ednew.setPath(var.getProperty().getProfileProperty().getDefinition().getPath() + "." + pc.getName());
      ednew.setUserData("slice-name", sliceName);
      ednew.setFixed(fixed);
      for (ProfiledType pt : type.getProfiledTypes()) {
        if (pt.hasBindings())
          ednew.setBinding(pt.getBindings().get(0));
        if (pt.getUri().startsWith("http://hl7.org/fhir/StructureDefinition/")) {
          String t = pt.getUri().substring(40);
          t = checkType(t, pc, pt.getProfiles());
          if (t != null) {
            if (pt.hasProfiles()) {
              for (String p : pt.getProfiles())
                if (t.equals("Reference"))
                  ednew.getType(t).addTargetProfile(p);
                else
                  ednew.getType(t).addProfile(p);
            } else
              ednew.getType(t);
          }
        }
      }

      return new PropertyWithType(var.getProperty().getPath() + "." + element, pc, new Property(worker, ednew, sd), type);
    }
  }


  private String checkType(String t, Property pvb, List<String> profiles) throws FHIRException {
    if (pvb.getDefinition().getType().size() == 1 && isCompatibleType(t, pvb.getDefinition().getType().get(0).getWorkingCode()) && profilesMatch(profiles, pvb.getDefinition().getType().get(0).getProfile()))
      return null;
    for (TypeRefComponent tr : pvb.getDefinition().getType()) {
      if (isCompatibleType(t, tr.getWorkingCode()))
        return tr.getWorkingCode(); // note what is returned - the base type, not the inferred mapping type
    }
    throw new FHIRException("The type " + t + " is not compatible with the allowed types for " + pvb.getDefinition().getPath());
  }

  private boolean profilesMatch(List<String> profiles, List<CanonicalType> profile) {
    return profiles == null || profiles.size() == 0 || profile.size() == 0 || (profiles.size() == 1 && profiles.get(0).equals(profile.get(0).getValue()));
  }

  private boolean isCompatibleType(String t, String code) {
    if (t.equals(code))
      return true;
    if (t.equals("string")) {
      StructureDefinition sd = worker.fetchTypeDefinition(code);
      return sd != null && sd.getBaseDefinition().equals("http://hl7.org/fhir/StructureDefinition/string");
    }
    return false;
  }

  private TypeDetails analyseTransform(TransformContext context, StructureMap map, StructureMapGroupRuleTargetComponent tgt, VariableForProfiling var, VariablesForProfiling vars) throws FHIRException {
    switch (tgt.getTransform()) {
      case CREATE:
        String p = getParamString(vars, tgt.getParameter().get(0));
        return new TypeDetails(CollectionStatus.SINGLETON, p);
      case COPY:
        return getParam(vars, tgt.getParameter().get(0));
      case EVALUATE:
        ExpressionNode expr = (ExpressionNode) tgt.getUserData(MAP_EXPRESSION);
        if (expr == null) {
          expr = fpe.parse(getParamString(vars, tgt.getParameter().get(tgt.getParameter().size() - 1)));
        }
        return fpe.check(vars, null, expr);
      case TRANSLATE:
        return new TypeDetails(CollectionStatus.SINGLETON, "CodeableConcept");
      case CC:
        ProfiledType res = new ProfiledType("CodeableConcept");
        if (tgt.getParameter().size() >= 2 && isParamId(vars, tgt.getParameter().get(1))) {
          TypeDetails td = vars.get(null, getParamId(vars, tgt.getParameter().get(1))).getProperty().getTypes();
          if (td != null && td.hasBinding())
            // todo: do we need to check that there's no implicit translation her? I don't think we do...
            res.addBinding(td.getBinding());
        }
        return new TypeDetails(CollectionStatus.SINGLETON, res);
      case C:
        return new TypeDetails(CollectionStatus.SINGLETON, "Coding");
      case QTY:
        return new TypeDetails(CollectionStatus.SINGLETON, "Quantity");
      case REFERENCE:
        VariableForProfiling vrs = vars.get(VariableMode.OUTPUT, getParamId(vars, tgt.getParameterFirstRep()));
        if (vrs == null)
          throw new FHIRException("Unable to resolve variable \"" + getParamId(vars, tgt.getParameterFirstRep()) + "\"");
        String profile = vrs.getProperty().getProfileProperty().getStructure().getUrl();
        TypeDetails td = new TypeDetails(CollectionStatus.SINGLETON);
        td.addType("Reference", profile);
        return td;
      case UUID:
        return new TypeDetails(CollectionStatus.SINGLETON, "id");
      default:
        throw new FHIRException("Transform Unknown or not handled yet: " + tgt.getTransform().toCode());
    }
  }

  private String getParamString(VariablesForProfiling vars, StructureMapGroupRuleTargetParameterComponent parameter) {
    DataType p = parameter.getValue();
    if (p == null || p instanceof IdType)
      return null;
    if (!p.hasPrimitiveValue())
      return null;
    return p.primitiveValue();
  }

  private String getParamId(VariablesForProfiling vars, StructureMapGroupRuleTargetParameterComponent parameter) {
    DataType p = parameter.getValue();
    if (p == null || !(p instanceof IdType))
      return null;
    return p.primitiveValue();
  }

  private boolean isParamId(VariablesForProfiling vars, StructureMapGroupRuleTargetParameterComponent parameter) {
    DataType p = parameter.getValue();
    if (p == null || !(p instanceof IdType))
      return false;
    return vars.get(null, p.primitiveValue()) != null;
  }

  private TypeDetails getParam(VariablesForProfiling vars, StructureMapGroupRuleTargetParameterComponent parameter) throws DefinitionException {
    DataType p = parameter.getValue();
    if (!(p instanceof IdType))
      return new TypeDetails(CollectionStatus.SINGLETON, ProfileUtilities.sdNs(p.fhirType(), null));
    else {
      String n = ((IdType) p).asStringValue();
      VariableForProfiling b = vars.get(VariableMode.INPUT, n);
      if (b == null)
        b = vars.get(VariableMode.OUTPUT, n);
      if (b == null)
        throw new DefinitionException("Variable " + n + " not found (" + vars.summary() + ")");
      return b.getProperty().getTypes();
    }
  }

  private PropertyWithType createProfile(StructureMap map, List<StructureDefinition> profiles, PropertyWithType prop, String sliceName, Base ctxt) throws FHIRException {
    if (prop.getBaseProperty().getDefinition().getPath().contains("."))
      throw new DefinitionException("Unable to process entry point");

    String type = prop.getBaseProperty().getDefinition().getPath();
    String suffix = "";
    if (ids.containsKey(type)) {
      int id = ids.get(type);
      id++;
      ids.put(type, id);
      suffix = "-" + id;
    } else
      ids.put(type, 0);

    StructureDefinition profile = new StructureDefinition();
    profiles.add(profile);
    profile.setDerivation(TypeDerivationRule.CONSTRAINT);
    profile.setType(type);
    profile.setBaseDefinition(prop.getBaseProperty().getStructure().getUrl());
    profile.setName("Profile for " + profile.getType() + " for " + sliceName);
    profile.setUrl(map.getUrl().replace("StructureMap", "StructureDefinition") + "-" + profile.getType() + suffix);
    ctxt.setUserData("profile", profile.getUrl()); // then we can easily assign this profile url for validation later when we actually transform
    profile.setId(map.getId() + "-" + profile.getType() + suffix);
    profile.setStatus(map.getStatus());
    profile.setExperimental(map.getExperimental());
    profile.setDescription("Generated automatically from the mapping by the Java Reference Implementation");
    for (ContactDetail c : map.getContact()) {
      ContactDetail p = profile.addContact();
      p.setName(c.getName());
      for (ContactPoint cc : c.getTelecom())
        p.addTelecom(cc);
    }
    profile.setDate(map.getDate());
    profile.setCopyright(map.getCopyright());
    profile.setFhirVersion(FHIRVersion.fromCode(Constants.VERSION));
    profile.setKind(prop.getBaseProperty().getStructure().getKind());
    profile.setAbstract(false);
    ElementDefinition ed = profile.getDifferential().addElement();
    ed.setPath(profile.getType());
    prop.setProfileProperty(new Property(worker, ed, profile));
    return prop;
  }

  private PropertyWithType resolveType(StructureMap map, String type, StructureMapInputMode mode) throws FHIRException {
    for (StructureMapStructureComponent imp : map.getStructure()) {
      if ((imp.getMode() == StructureMapModelMode.SOURCE && mode == StructureMapInputMode.SOURCE) ||
        (imp.getMode() == StructureMapModelMode.TARGET && mode == StructureMapInputMode.TARGET)) {
        StructureDefinition sd = worker.fetchResource(StructureDefinition.class, imp.getUrl());
        if (sd == null)
          throw new FHIRException("Import " + imp.getUrl() + " cannot be resolved");
        if (sd.getId().equals(type)) {
          return new PropertyWithType(sd.getType(), new Property(worker, sd.getSnapshot().getElement().get(0), sd), null, new TypeDetails(CollectionStatus.SINGLETON, sd.getUrl()));
        }
      }
    }
    throw new FHIRException("Unable to find structure definition for " + type + " in imports");
  }


  public StructureMap generateMapFromMappings(StructureDefinition sd) throws IOException, FHIRException {
    String id = getLogicalMappingId(sd);
    if (id == null)
      return null;
    String prefix = ToolingExtensions.readStringExtension(sd, ToolingExtensions.EXT_MAPPING_PREFIX);
    String suffix = ToolingExtensions.readStringExtension(sd, ToolingExtensions.EXT_MAPPING_SUFFIX);
    if (prefix == null || suffix == null)
      return null;
    // we build this by text. Any element that has a mapping, we put it's mappings inside it....
    StringBuilder b = new StringBuilder();
    b.append(prefix);

    ElementDefinition root = sd.getSnapshot().getElementFirstRep();
    String m = getMapping(root, id);
    if (m != null)
      b.append(m + "\r\n");
    addChildMappings(b, id, "", sd, root, false);
    b.append("\r\n");
    b.append(suffix);
    b.append("\r\n");
    StructureMap map = parse(b.toString(), sd.getUrl());
    map.setId(tail(map.getUrl()));
    if (!map.hasStatus()) {
      map.setStatus(PublicationStatus.DRAFT);
    }
    if (!map.hasDescription() && map.hasTitle()) {
      map.setDescription(map.getTitle());
    }
    map.getText().setStatus(NarrativeStatus.GENERATED);
    map.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
    map.getText().getDiv().addTag("pre").addText(render(map));
    return map;
  }


  private String tail(String url) {
    return url.substring(url.lastIndexOf("/") + 1);
  }


  private void addChildMappings(StringBuilder b, String id, String indent, StructureDefinition sd, ElementDefinition ed, boolean inner) throws DefinitionException {
    boolean first = true;
    List<ElementDefinition> children = profileUtilities.getChildMap(sd, ed).getList();
    for (ElementDefinition child : children) {
      if (first && inner) {
        b.append(" then {\r\n");
        first = false;
      }
      String map = getMapping(child, id);
      if (map != null) {
        b.append(indent + "  " + child.getPath() + ": " + map);
        addChildMappings(b, id, indent + "  ", sd, child, true);
        b.append("\r\n");
      }
    }
    if (!first && inner)
      b.append(indent + "}");

  }


  private String getMapping(ElementDefinition ed, String id) {
    for (ElementDefinitionMappingComponent map : ed.getMapping())
      if (id.equals(map.getIdentity()))
        return map.getMap();
    return null;
  }


  private String getLogicalMappingId(StructureDefinition sd) {
    String id = null;
    for (StructureDefinitionMappingComponent map : sd.getMapping()) {
      if ("http://hl7.org/fhir/logical".equals(map.getUri()))
        return map.getIdentity();
    }
    return null;
  }

  public ValidationOptions getTerminologyServiceOptions() {
    return terminologyServiceOptions;
  }

  public void setTerminologyServiceOptions(ValidationOptions terminologyServiceOptions) {
    this.terminologyServiceOptions = terminologyServiceOptions;
  }

  public boolean isExceptionsForChecks() {
    return exceptionsForChecks;
  }

  public void setExceptionsForChecks(boolean exceptionsForChecks) {
    this.exceptionsForChecks = exceptionsForChecks;
  }

  public List<StructureMap> getMapsForUrl(List<StructureMap> maps, String url, StructureMapInputMode mode) {
    List<StructureMap> res = new ArrayList<>();
    for (StructureMap map : maps) {
      if (mapIsForUrl(map, url, mode)) {
        res.add(map);
      }
    }
    return res;
  }

  private boolean mapIsForUrl(StructureMap map, String url, StructureMapInputMode mode) {
    for (StructureMapGroupComponent grp : map.getGroup()) {
      if (grp.getTypeMode() != StructureMapGroupTypeMode.NULL) {
        for (StructureMapGroupInputComponent p : grp.getInput()) {
          if (mode == null || mode == p.getMode()) { 
            String t = resolveInputType(p, map);
            if (url.equals(t)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public List<StructureMap> getMapsForUrlPrefix(List<StructureMap> maps, String url, StructureMapInputMode mode) {
    List<StructureMap> res = new ArrayList<>();
    for (StructureMap map : maps) {
      if (mapIsForUrlPrefix(map, url, mode)) {
        res.add(map);
      }
    }
    return res;
  }

  private boolean mapIsForUrlPrefix(StructureMap map, String url, StructureMapInputMode mode) {
    for (StructureMapGroupComponent grp : map.getGroup()) {
      if (grp.getTypeMode() != StructureMapGroupTypeMode.NULL) {
        for (StructureMapGroupInputComponent p : grp.getInput()) {
          if (mode == null || mode == p.getMode()) { 
            String t = resolveInputType(p, map);
            if (t != null && t.startsWith(url)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private String resolveInputType(StructureMapGroupInputComponent p, StructureMap map) {
    for (StructureMapStructureComponent struc : map.getStructure()) {
      if (struc.hasAlias() && struc.getAlias().equals(p.getType())) {
        return struc.getUrl();
      }
    }
    return null;
  }

  public ResolvedGroup getGroupForUrl(StructureMap map, String url, StructureMapInputMode mode) {
    for (StructureMapGroupComponent grp : map.getGroup()) {
      if (grp.getTypeMode() != StructureMapGroupTypeMode.NULL) {
        for (StructureMapGroupInputComponent p : grp.getInput()) {
          if (mode == null || mode == p.getMode()) { 
            String t = resolveInputType(p, map);
            if (url.equals(t)) {
              return new ResolvedGroup(map, grp);
            }
          }
        }
      }
    }
    return null;
 }

  public String getInputType(ResolvedGroup grp, StructureMapInputMode mode) {
    if (grp.getTargetGroup().getInput().size() != 2 || grp.getTargetGroup().getInput().get(0).getMode() == grp.getTargetGroup().getInput().get(1).getMode()) {
      return null;      
    } else if (grp.getTargetGroup().getInput().get(0).getMode() == mode) {
      return resolveInputType(grp.getTargetGroup().getInput().get(0), grp.getTargetMap());
    } else {
      return resolveInputType(grp.getTargetGroup().getInput().get(1), grp.getTargetMap());
    }
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

}