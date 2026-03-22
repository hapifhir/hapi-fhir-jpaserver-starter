package org.hl7.fhir.r5.conformance.profile;

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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r5.conformance.ElementRedirection;
import org.hl7.fhir.r5.conformance.profile.MappingAssistant.MappingMergeModeOption;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.ObjectConverter;
import org.hl7.fhir.r5.elementmodel.Property;
import org.hl7.fhir.r5.extensions.ExtensionDefinitions;
import org.hl7.fhir.r5.extensions.ExtensionUtilities;
import org.hl7.fhir.r5.fhirpath.ExpressionNode;
import org.hl7.fhir.r5.fhirpath.ExpressionNode.Kind;
import org.hl7.fhir.r5.fhirpath.ExpressionNode.Operation;
import org.hl7.fhir.r5.fhirpath.FHIRPathEngine;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.ElementDefinition.DiscriminatorType;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionBaseComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionBindingAdditionalComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionBindingComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionConstraintComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionExampleComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionMappingComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionSlicingComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionSlicingDiscriminatorComponent;
import org.hl7.fhir.r5.model.ElementDefinition.SlicingRules;
import org.hl7.fhir.r5.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r5.model.Enumerations.BindingStrength;
import org.hl7.fhir.r5.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.StructureDefinition.ExtensionContextType;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionContextComponent;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionDifferentialComponent;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionSnapshotComponent;
import org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r5.terminologies.expansion.ValueSetExpansionOutcome;
import org.hl7.fhir.r5.terminologies.utilities.ValidationResult;

import org.hl7.fhir.r5.utils.UserDataNames;
import org.hl7.fhir.r5.utils.xver.XVerExtensionManager;
import org.hl7.fhir.r5.utils.xver.XVerExtensionManager.XVerExtensionStatus;
import org.hl7.fhir.r5.utils.formats.CSVWriter;
import org.hl7.fhir.r5.utils.xver.XVerExtensionManagerFactory;
import org.hl7.fhir.utilities.*;
import org.hl7.fhir.utilities.i18n.I18nConstants;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.validation.ValidationMessage.Source;
import org.hl7.fhir.utilities.validation.ValidationOptions;
import org.hl7.fhir.utilities.xhtml.HierarchicalTableGenerator.Row;
import org.hl7.fhir.utilities.xml.SchematronWriter;
import org.hl7.fhir.utilities.xml.SchematronWriter.Rule;
import org.hl7.fhir.utilities.xml.SchematronWriter.SchematronType;
import org.hl7.fhir.utilities.xml.SchematronWriter.Section;


/** 
 * This class provides a set of utility operations for working with Profiles.
 * Key functionality:
 *  * getChildMap --?
 *  * getChildList
 *  * generateSnapshot: Given a base (snapshot) profile structure, and a differential profile, generate a new snapshot profile
 *  * closeDifferential: fill out a differential by excluding anything not mentioned
 *  * generateExtensionsTable: generate the HTML for a hierarchical table presentation of the extensions
 *  * generateTable: generate  the HTML for a hierarchical table presentation of a structure
 *  * generateSpanningTable: generate the HTML for a table presentation of a network of structures, starting at a nominated point
 *  * summarize: describe the contents of a profile
 *  
 * note to maintainers: Do not make modifications to the snapshot generation without first changing the snapshot generation test cases to demonstrate the grounds for your change
 *  
 * @author Grahame
 *
 */
@MarkedToMoveToAdjunctPackage
@Slf4j
public class ProfileUtilities {

  private static boolean suppressIgnorableExceptions;

  
  public class ElementDefinitionCounter {
    int countMin = 0;
    int countMax = 0;
    int index = 0;
    ElementDefinition focus;
    Set<String> names = new HashSet<>();

    public ElementDefinitionCounter(ElementDefinition ed, int i) {
      focus = ed;
      index = i;
    }

    public int checkMin() {
      if (countMin > focus.getMin()) {
        return countMin;
      } else {     
        return -1;
      }
    }

    public int checkMax() {
      if (countMax > max(focus.getMax())) {
        return countMax;
      } else {     
        return -1;
      }
    }

    private int max(String max) {
      if ("*".equals(max)) {
        return Integer.MAX_VALUE;
      } else {
        return Integer.parseInt(max);
      }
    }

    public boolean count(ElementDefinition ed, String name) {
      countMin = countMin + ed.getMin();
      if (countMax < Integer.MAX_VALUE) {
        int m = max(ed.getMax());
        if (m == Integer.MAX_VALUE) {
          countMax = m;
        } else {
          countMax = countMax + m;
        }
      }
      boolean ok = !names.contains(name);
      names.add(name);
      return ok;
    }

    public ElementDefinition getFocus() {
      return focus;
    }

    public boolean checkMinMax() {
      return countMin <= countMax;
    }

    public int getIndex() {
      return index;
    }
    
  }

  public enum AllowUnknownProfile {
    NONE, // exception if there's any unknown profiles (the default)
    NON_EXTNEIONS, // don't raise an exception except on Extension (because more is going on there
    ALL_TYPES // allow any unknow profile
  }

  /**
   * These extensions are stripped in inherited profiles (and may be replaced by 
   */
  
  public static final List<String> NON_INHERITED_ED_URLS = Arrays.asList(
      "http://hl7.org/fhir/tools/StructureDefinition/binding-definition",
      "http://hl7.org/fhir/tools/StructureDefinition/no-binding",
      "http://hl7.org/fhir/StructureDefinition/elementdefinition-isCommonBinding",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-standards-status",  
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-category",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-fmm",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-implements",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-explicit-type-name",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-security-category",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-wg",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-normative-version",
      "http://hl7.org/fhir/tools/StructureDefinition/obligation-profile",
      "http://hl7.org/fhir/StructureDefinition/obligation-profile",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-standards-status-reason",
      ExtensionDefinitions.EXT_SUMMARY/*,
      ExtensionDefinitions.EXT_OBLIGATION_CORE,
      ExtensionDefinitions.EXT_OBLIGATION_TOOLS*/);

  public static final List<String> DEFAULT_INHERITED_ED_URLS = Arrays.asList(
      "http://hl7.org/fhir/StructureDefinition/questionnaire-optionRestriction",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-referenceProfile",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-referenceResource",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-unitOption",

      "http://hl7.org/fhir/StructureDefinition/mimeType");

  /**
   * These extensions are ignored when found in differentials
   */  
  public static final List<String> NON_OVERRIDING_ED_URLS = Arrays.asList(
      "http://hl7.org/fhir/StructureDefinition/elementdefinition-translatable",
      ExtensionDefinitions.EXT_JSON_NAME, ExtensionDefinitions.EXT_JSON_NAME_DEPRECATED,
      "http://hl7.org/fhir/tools/StructureDefinition/implied-string-prefix",
      "http://hl7.org/fhir/tools/StructureDefinition/json-empty-behavior",
      "http://hl7.org/fhir/tools/StructureDefinition/json-nullable",
      "http://hl7.org/fhir/tools/StructureDefinition/json-primitive-choice",
      "http://hl7.org/fhir/tools/StructureDefinition/json-property-key",
      "http://hl7.org/fhir/tools/StructureDefinition/type-specifier",
      "http://hl7.org/fhir/tools/StructureDefinition/xml-choice-group",
      ExtensionDefinitions.EXT_XML_NAMESPACE, ExtensionDefinitions.EXT_XML_NAMESPACE_DEPRECATED,
      ExtensionDefinitions.EXT_XML_NAME, ExtensionDefinitions.EXT_XML_NAME_DEPRECATED,
      "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype"
      );

  /**
   * When these extensions are found, they override whatever is set on the ancestor element 
   */  
  public static final List<String> OVERRIDING_ED_URLS = Arrays.asList(
      "http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-date-format",
      ExtensionDefinitions.EXT_DATE_RULES,
      "http://hl7.org/fhir/StructureDefinition/designNote",
      "http://hl7.org/fhir/StructureDefinition/elementdefinition-allowedUnits",
      "http://hl7.org/fhir/StructureDefinition/elementdefinition-question",
      "http://hl7.org/fhir/StructureDefinition/entryFormat",
      "http://hl7.org/fhir/StructureDefinition/maxDecimalPlaces",
      "http://hl7.org/fhir/StructureDefinition/maxSize",
      "http://hl7.org/fhir/StructureDefinition/minLength",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-choiceOrientation",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-displayCategory",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-hidden",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-signatureRequired",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-sliderStepValue",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-supportLink",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-unitValueSet",
      "http://hl7.org/fhir/StructureDefinition/questionnaire-usageMode",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-display-hint",
      "http://hl7.org/fhir/StructureDefinition/structuredefinition-explicit-type-name"
      );
  
  public IWorkerContext getContext() {
    return this.context;
  }

  public static class SourcedChildDefinitions {
    private StructureDefinition source;
    private List<ElementDefinition> list;
    private String path;
    public SourcedChildDefinitions(StructureDefinition source, List<ElementDefinition> list) {
      super();
      this.source = source;
      this.list = list;
    }
    public SourcedChildDefinitions(StructureDefinition source, List<ElementDefinition> list, String path) {
      super();
      this.source = source;
      this.list = list;
      this.path = path;
    }
    public StructureDefinition getSource() {
      return source;
    }
    public List<ElementDefinition> getList() {
      return list;
    }
    public String getPath() {
      return path;
    }
    
  }

  public class ElementDefinitionResolution {

    private StructureDefinition source;
    private ElementDefinition element;

    public ElementDefinitionResolution(StructureDefinition source, ElementDefinition element) {
      this.source = source;
      this.element = element;
    }

    public StructureDefinition getSource() {
      return source;
    }

    public ElementDefinition getElement() {
      return element;
    }

  }

  public static class ElementChoiceGroup {
    private Row row;
    private String name;
    private boolean mandatory;
    private List<String> elements = new ArrayList<>();
    
    public ElementChoiceGroup(String name, boolean mandatory) {
      super();
      this.name = name;
      this.mandatory = mandatory;
    }
    public Row getRow() {
      return row;
    }
    public List<String> getElements() {
      return elements;
    }
    public void setRow(Row row) {
      this.row = row;      
    }
    public String getName() {
      return name;
    }
    public boolean isMandatory() {
      return mandatory;
    }
    public void setMandatory(boolean mandatory) {
      this.mandatory = mandatory;
    }
    
  }
  
  private static final int MAX_RECURSION_LIMIT = 10;
  
  public static class ExtensionContext {

    private ElementDefinition element;
    private StructureDefinition defn;

    public ExtensionContext(StructureDefinition ext, ElementDefinition ed) {
      this.defn = ext;
      this.element = ed;
    }

    public ElementDefinition getElement() {
      return element;
    }

    public StructureDefinition getDefn() {
      return defn;
    }

    public String getUrl() {
      if (element == defn.getSnapshot().getElement().get(0))
        return defn.getUrl();
      else
        return element.getSliceName();
    }

    public ElementDefinition getExtensionValueDefinition() {
      int i = defn.getSnapshot().getElement().indexOf(element)+1;
      while (i < defn.getSnapshot().getElement().size()) {
        ElementDefinition ed = defn.getSnapshot().getElement().get(i);
        if (ed.getPath().equals(element.getPath()))
          return null;
        if (ed.getPath().startsWith(element.getPath()+".value") && !ed.hasSlicing())
          return ed;
        i++;
      }
      return null;
    }
  }
  
  private static final boolean COPY_BINDING_EXTENSIONS = false;
  private static final boolean DONT_DO_THIS = false;
  
  private boolean debug;
  // note that ProfileUtilities are used re-entrantly internally, so nothing with process state can be here
  private final IWorkerContext context;
  private FHIRPathEngine fpe;
  private List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
  private List<String> snapshotStack = new ArrayList<String>();
  private ProfileKnowledgeProvider pkp;
//  private boolean igmode;
  private ValidationOptions terminologyServiceOptions = new ValidationOptions(FhirPublication.R5);
  private boolean newSlicingProcessing;
  private String defWebRoot;
  private boolean autoFixSliceNames;
  private XVerExtensionManager xver;
  private boolean wantFixDifferentialFirstElementType;
  private Set<String> masterSourceFileNames;
  private Set<String> localFileNames;
  private Map<String, SourcedChildDefinitions> childMapCache = new HashMap<>();
  private AllowUnknownProfile allowUnknownProfile = AllowUnknownProfile.ALL_TYPES;
  private MappingMergeModeOption mappingMergeMode = MappingMergeModeOption.APPEND;
  private boolean forPublication;
  private List<StructureDefinition> obligationProfiles = new ArrayList<>();
  private boolean wantThrowExceptions;
  private List<String> suppressedMappings= new ArrayList<>();
  @Getter @Setter private Parameters parameters;
  
  public ProfileUtilities(IWorkerContext context, List<ValidationMessage> messages, ProfileKnowledgeProvider pkp, FHIRPathEngine fpe) {
    super();
    this.context = context;
    if (messages != null) {
      this.messages = messages;
    } else {
      wantThrowExceptions = true;
    }
    this.pkp = pkp;

    this.fpe = fpe;
    if (context != null && this.fpe == null) {
      this.fpe = new FHIRPathEngine(context, this);
    }
    if (context != null) {
      parameters = context.getExpansionParameters();
    }
  }

  public ProfileUtilities(IWorkerContext context, List<ValidationMessage> messages, ProfileKnowledgeProvider pkp) {
    super();
    this.context = context;
    if (messages != null) {
      this.messages = messages;
    } else {
      wantThrowExceptions = true;
    }
    this.pkp = pkp;
    if (context != null) {
      this.fpe = new FHIRPathEngine(context, this);
    }

    if (context != null) {
      parameters = context.getExpansionParameters();
    }
  }
  
  public boolean isWantFixDifferentialFirstElementType() {
    return wantFixDifferentialFirstElementType;
  }

  public void setWantFixDifferentialFirstElementType(boolean wantFixDifferentialFirstElementType) {
    this.wantFixDifferentialFirstElementType = wantFixDifferentialFirstElementType;
  }

  public boolean isAutoFixSliceNames() {
    return autoFixSliceNames;
  }

  public ProfileUtilities setAutoFixSliceNames(boolean autoFixSliceNames) {
    this.autoFixSliceNames = autoFixSliceNames;
    return this;
  }

  public SourcedChildDefinitions getChildMap(StructureDefinition profile, ElementDefinition element, boolean chaseTypes) throws DefinitionException {
    return getChildMap(profile, element, chaseTypes, null);
  }
  public SourcedChildDefinitions getChildMap(StructureDefinition profile, ElementDefinition element, boolean chaseTypes, String type) throws DefinitionException {
    String cacheKey = "cm."+profile.getVersionedUrl()+"#"+(element.hasId() ? element.getId() : element.getPath())+"."+chaseTypes;
    if (childMapCache.containsKey(cacheKey)) {
      return childMapCache.get(cacheKey);
    }
    StructureDefinition src = profile;
    List<ElementDefinition> res = new ArrayList<ElementDefinition>();
    List<ElementDefinition> elements = profile.getSnapshot().getElement();
    int iOffs = elements.indexOf(element) + 1;
    boolean walksIntoElement = elements.size() > iOffs && elements.get(iOffs).getPath().startsWith(element.getPath());
    if (element.getContentReference() != null && !walksIntoElement) {
      List<ElementDefinition> list = null;
      String id = null;
      if (element.getContentReference().startsWith("#")) {
        // internal reference
        id = element.getContentReference().substring(1);
        list = profile.getSnapshot().getElement();
      } else if (element.getContentReference().contains("#")) {
        // external reference
        String ref = element.getContentReference();
        StructureDefinition sd = findProfile(element.getContentReferenceElement(), profile);
        if (sd == null) {
          throw new DefinitionException("unable to process contentReference '"+element.getContentReference()+"' on element '"+element.getId()+"'");
        }
        src = sd;
        list = sd.getSnapshot().getElement();
        id = ref.substring(ref.indexOf("#")+1);        
      } else {
        throw new DefinitionException("unable to process contentReference '"+element.getContentReference()+"' on element '"+element.getId()+"'");
      }
        
      for (ElementDefinition e : list) {
        if (id.equals(e.getId()))
          return getChildMap(src, e, true);
      }
      throw new DefinitionException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_NAME_REFERENCE__AT_PATH_, element.getContentReference(), element.getPath()));

    } else {
      String path = element.getPath();
      for (int index = elements.indexOf(element) + 1; index < elements.size(); index++) {
        ElementDefinition e = elements.get(index);
        if (e.getPath().startsWith(path + ".")) {
          // We only want direct children, not all descendants
          if (!e.getPath().substring(path.length()+1).contains("."))
            res.add(e);
        } else
          break;
      }
      if (res.isEmpty() && chaseTypes) {
        // we've got no in-line children. Some consumers of this routine will figure this out for themselves but most just want to walk into 
        // the type children.
        src = null;
        if (type != null) {
          src = context.fetchTypeDefinition(type);
        } else if (element.getType().isEmpty()) {
          throw new DefinitionException("No defined children and no type information on element '"+element.getId()+"'");
        } else if (element.getType().size() > 1) {
          // this is a problem. There's two separate but related issues
          // the first is what's going on here - the profile has walked into an element without fixing the type
          //   this might be ok - maybe it's just going to constrain extensions for all types, though this is generally a bad idea
          //   but if that's all it's doing, we'll just pretend we have an element. Only, it's not really an element so that might
          //   blow up on us later in mystifying ways. We'll have to wear it though, because there's profiles out there that do this
          // the second problem is whether this should be some common descendent of Element - I'm not clear about that
          //   left as a problem for the future.
          //
          // this is what the code was prior to 2025-08-27:
          //   throw new DefinitionException("No defined children and multiple possible types '"+element.typeSummary()+"' on element '"+element.getId()+"'");
          src = context.fetchTypeDefinition("Element");
        } else if (element.getType().get(0).getProfile().size() > 1) {
          throw new DefinitionException("No defined children and multiple possible type profiles '"+element.typeSummary()+"' on element '"+element.getId()+"'");
        } else if (element.getType().get(0).hasProfile()) {
          src = findProfile(element.getType().get(0).getProfile().get(0), profile);
          if (src == null) {
            throw new DefinitionException("No defined children and unknown type profile '"+element.typeSummary()+"' on element '"+element.getId()+"'");
          }
        } else {
          src = context.fetchTypeDefinition(element.getType().get(0).getWorkingCode());
          if (src == null) {
            throw new DefinitionException("No defined children and unknown type '"+element.typeSummary()+"' on element '"+element.getId()+"'");
          }
        }
        SourcedChildDefinitions scd  = getChildMap(src, src.getSnapshot().getElementFirstRep(), false);
        res = scd.list;
      }
      SourcedChildDefinitions result  = new SourcedChildDefinitions(src, res);
      childMapCache.put(cacheKey, result);
      return result;
    }
  }


  public List<ElementDefinition> getSliceList(StructureDefinition profile, ElementDefinition element) throws DefinitionException {
    if (!element.hasSlicing())
      throw new Error(context.formatMessage(I18nConstants.GETSLICELIST_SHOULD_ONLY_BE_CALLED_WHEN_THE_ELEMENT_HAS_SLICING));

    List<ElementDefinition> res = new ArrayList<ElementDefinition>();
    List<ElementDefinition> elements = profile.getSnapshot().getElement();
    String path = element.getPath();
    int start = findElementIndex(elements, element);
    for (int index = start + 1; index < elements.size(); index++) {
      ElementDefinition e = elements.get(index);
      if (e.getPath().startsWith(path + ".") || e.getPath().equals(path)) {
        // We want elements with the same path (until we hit an element that doesn't start with the same path)
        if (e.getPath().equals(element.getPath()))
          res.add(e);
      } else
        break;
    }
    return res;
  }


  private int findElementIndex(List<ElementDefinition> elements, ElementDefinition element) {
    int res = elements.indexOf(element);
    if (res == -1) {
      for (int i = 0; i < elements.size(); i++) {
        Element t  = elements.get(i);
        if (t.getId().equals(element.getId())) {
          res = i;
        }
      }
    }
    return res;
  }

  /**
   * Given a Structure, navigate to the element given by the path and return the direct children of that element
   *
   * @param profile The structure to navigate into
   * @param path The path of the element within the structure to get the children for
   * @return A List containing the element children (all of them are Elements)
   */
  public List<ElementDefinition> getChildList(StructureDefinition profile, String path, String id) {
    return getChildList(profile, path, id, false);
  }
  
  public List<ElementDefinition> getChildList(StructureDefinition profile, String path, String id, boolean diff) {
    return getChildList(profile, path, id, diff, false);
  }
  
  public List<ElementDefinition> getChildList(StructureDefinition profile, String path, String id, boolean diff, boolean refs) {
    List<ElementDefinition> res = new ArrayList<ElementDefinition>();

    boolean capturing = id==null;
    if (id==null && !path.contains("."))
      capturing = true;
  
    List<ElementDefinition> list = diff ? profile.getDifferential().getElement() : profile.getSnapshot().getElement();
    for (ElementDefinition e : list) {
      if (e == null)
        throw new Error(context.formatMessage(I18nConstants.ELEMENT__NULL_, profile.getUrl()));
//      if (e.getId() == null) // this is sort of true, but in some corner cases it's not, and in those cases, we don't care
//        throw new Error(context.formatMessage(I18nConstants.ELEMENT_ID__NULL__ON_, e.toString(), profile.getUrl()));
      
      if (!capturing && id!=null && e.getId().equals(id)) {
        capturing = true;
      }
      
      // If our element is a slice, stop capturing children as soon as we see the next slice
      if (capturing && e.hasId() && id!= null && !e.getId().equals(id) && e.getPath().equals(path))
        break;
      
      if (capturing) {
        String p = e.getPath();
  
        if (refs && !Utilities.noString(e.getContentReference()) && path.startsWith(p)) {
          if (path.length() > p.length()) {
            return getChildList(profile, e.getContentReference()+"."+path.substring(p.length()+1), null, diff);
          } else if (e.getContentReference().startsWith("#")) {
            return getChildList(profile, e.getContentReference().substring(1), null, diff);            
          } else if (e.getContentReference().contains("#")) {
            String url = e.getContentReference().substring(0, e.getContentReference().indexOf("#"));
            StructureDefinition sd = findProfile(e.getContentReferenceElement(), profile);
            if (sd == null) {
              throw new DefinitionException("Unable to find Structure "+url);
            }
            return getChildList(sd, e.getContentReference().substring(e.getContentReference().indexOf("#")+1), null, diff);            
          } else {
            return getChildList(profile, e.getContentReference(), null, diff);
          }
          
        } else if (p.startsWith(path+".") && !p.equals(path)) {
          String tail = p.substring(path.length()+1);
          if (!tail.contains(".")) {
            res.add(e);
          }
        }
      }
    }

    return res;
  }

  public List<ElementDefinition> getChildList(StructureDefinition structure, ElementDefinition element, boolean diff, boolean refs) {
    return getChildList(structure, element.getPath(), element.getId(), diff, refs);
  }

  public List<ElementDefinition> getChildList(StructureDefinition structure, ElementDefinition element, boolean diff) {
    return getChildList(structure, element.getPath(), element.getId(), diff);
  }

  public List<ElementDefinition> getChildList(StructureDefinition structure, ElementDefinition element) {
    if (element.hasContentReference()) {
      ElementDefinition target = element;
      for (ElementDefinition t : structure.getSnapshot().getElement()) {
        if (t.getId().equals(element.getContentReference().substring(1))) {
          target = t;
        }
      }      
      return getChildList(structure, target.getPath(), target.getId(), false);
    } else {
      return getChildList(structure, element.getPath(), element.getId(), false);
    }
	}

  /**
   * Given a base (snapshot) profile structure, and a differential profile, generate a new snapshot profile
   *
   * @param base - the base structure on which the differential will be applied
   * @param derived - the differential to apply to the base
   * @param url - where the base has relative urls for profile references, these need to be converted to absolutes by prepending this URL (e.g. the canonical URL)
   * @param webUrl - where the base has relative urls in markdown, these need to be converted to absolutes by prepending this URL (this is not the same as the canonical URL)
   * @return
   * @throws FHIRException 
   * @throws DefinitionException 
   * @throws Exception
   */
  public void generateSnapshot(StructureDefinition base, StructureDefinition derived, String url, String webUrl, String profileName) throws DefinitionException, FHIRException {
    if (base == null) {
      throw new DefinitionException(context.formatMessage(I18nConstants.NO_BASE_PROFILE_PROVIDED));
    }
    if (derived == null) {
      throw new DefinitionException(context.formatMessage(I18nConstants.NO_DERIVED_STRUCTURE_PROVIDED));
    }
    checkNotGenerating(base, "Base for generating a snapshot for the profile "+derived.getUrl());
    checkNotGenerating(derived, "Focus for generating a snapshot");

    if (!base.hasType()) {
      throw new DefinitionException(context.formatMessage(I18nConstants.BASE_PROFILE__HAS_NO_TYPE, base.getUrl()));
    }
    if (!derived.hasType()) {
      throw new DefinitionException(context.formatMessage(I18nConstants.DERIVED_PROFILE__HAS_NO_TYPE, derived.getUrl()));
    }
    if (!derived.hasDerivation()) {
      throw new DefinitionException(context.formatMessage(I18nConstants.DERIVED_PROFILE__HAS_NO_DERIVATION_VALUE_AND_SO_CANT_BE_PROCESSED, derived.getUrl()));
    }
    if (!base.getType().equals(derived.getType()) && derived.getDerivation() == TypeDerivationRule.CONSTRAINT) {
      throw new DefinitionException(context.formatMessage(I18nConstants.BASE__DERIVED_PROFILES_HAVE_DIFFERENT_TYPES____VS___, base.getUrl(), base.getType(), derived.getUrl(), derived.getType()));
    }
    if (!base.hasSnapshot()) {
      StructureDefinition sdb = findProfile(base.getBaseDefinitionElement(), base);
      if (sdb == null)
        throw new DefinitionException(context.formatMessage(I18nConstants.UNABLE_TO_FIND_BASE__FOR_, base.getBaseDefinition(), base.getUrl()));
      checkNotGenerating(sdb, "an extension base");
      generateSnapshot(sdb, base, base.getUrl(), (sdb.hasWebPath()) ? Utilities.extractBaseUrl(sdb.getWebPath()) : webUrl, base.getName());
    }
    fixTypeOfResourceId(base);
    if (base.hasExtension(ExtensionDefinitions.EXT_TYPE_PARAMETER)) {
      checkTypeParameters(base, derived);
    }
    
    if (snapshotStack.contains(derived.getUrl())) {
      throw new DefinitionException(context.formatMessage(I18nConstants.CIRCULAR_SNAPSHOT_REFERENCES_DETECTED_CANNOT_GENERATE_SNAPSHOT_STACK__, snapshotStack.toString()));
    }
    derived.setGeneratingSnapshot(true);
    snapshotStack.add(derived.getUrl());
    boolean oldCopyUserData = Base.isCopyUserData();
    Base.setCopyUserData(true);
    try {

      if (!Utilities.noString(webUrl) && !webUrl.endsWith("/"))
        webUrl = webUrl + '/';

      if (defWebRoot == null)
        defWebRoot = webUrl;
      derived.setSnapshot(new StructureDefinitionSnapshotComponent());

      try {
        checkDifferential(derived.getDifferential().getElement(), derived.getTypeName(), derived.getUrl());
        checkDifferentialBaseType(derived);

          log.debug("Differential: ");
          int debugPadding = 0;
          for (ElementDefinition ed : derived.getDifferential().getElement()) {
            log.debug(" "+Utilities.padLeft(Integer.toString(debugPadding), ' ', 3)+" "+ed.getId()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed));
            debugPadding++;
          }
          log.debug("Snapshot: ");
          debugPadding = 0;
          for (ElementDefinition ed : base.getSnapshot().getElement()) {
            log.debug(" "+Utilities.padLeft(Integer.toString(debugPadding), ' ', 3)+" "+ed.getId()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed));
            debugPadding++;
          }


        copyInheritedExtensions(base, derived, webUrl);

        findInheritedObligationProfiles(derived);
        // so we have two lists - the base list, and the differential list
        // the differential list is only allowed to include things that are in the base list, but
        // is allowed to include them multiple times - thereby slicing them

        // our approach is to walk through the base list, and see whether the differential
        // says anything about them.
        // we need a diff cursor because we can only look ahead, in the bound scoped by longer paths


        for (ElementDefinition e : derived.getDifferential().getElement()) 
          e.clearUserData(UserDataNames.SNAPSHOT_GENERATED_IN_SNAPSHOT);

        // we actually delegate the work to a subroutine so we can re-enter it with a different cursors
        StructureDefinitionDifferentialComponent diff = cloneDiff(derived.getDifferential()); // we make a copy here because we're sometimes going to hack the differential while processing it. Have to migrate user data back afterwards
        new SnapshotGenerationPreProcessor(this).process(diff, derived);
        
        StructureDefinitionSnapshotComponent baseSnapshot  = base.getSnapshot();
        if (derived.getDerivation() == TypeDerivationRule.SPECIALIZATION) {
          String derivedType = derived.getTypeName();

          baseSnapshot = cloneSnapshot(baseSnapshot, base.getTypeName(), derivedType);
        }
        //      if (derived.getId().equals("2.16.840.1.113883.10.20.22.2.1.1")) {
        //        debug = true;
        //      }

        MappingAssistant mappingDetails = new MappingAssistant(mappingMergeMode, base, derived, context.getVersion(), suppressedMappings);
        
        ProfilePathProcessor.processPaths(this, base, derived, url, webUrl, diff, baseSnapshot, mappingDetails);

        checkGroupConstraints(derived);
        if (derived.getDerivation() == TypeDerivationRule.SPECIALIZATION) {
          int i = 0;
          for (ElementDefinition e : diff.getElement()) {
            if (!e.hasUserData(UserDataNames.SNAPSHOT_GENERATED_IN_SNAPSHOT) && e.getPath().contains(".")) {
              ElementDefinition existing = getElementInCurrentContext(e.getPath(), derived.getSnapshot().getElement());
              if (existing != null) {
                updateFromDefinition(existing, e, profileName, false, url, base, derived, "StructureDefinition.differential.element["+i+"]", mappingDetails, false);
              } else {
                ElementDefinition outcome = updateURLs(url, webUrl, e.copy(), true);
                e.setUserData(UserDataNames.SNAPSHOT_GENERATED_IN_SNAPSHOT, outcome);
                markExtensions(outcome, true, derived);
                derived.getSnapshot().addElement(outcome);
                if (walksInto(diff.getElement(), e)) {
                  if (e.getType().size() > 1) {
                    throw new DefinitionException("Unsupported scenario: specialization walks into multiple types at "+e.getId()); 
                  } else {
                    addInheritedElementsForSpecialization(derived.getSnapshot(), outcome, outcome.getTypeFirstRep().getWorkingCode(), outcome.getPath(), url, webUrl);
                  }
                }
              }
            }
            i++;
          }
        }

        for (int i = 0; i < derived.getSnapshot().getElement().size(); i++) {
          ElementDefinition ed = derived.getSnapshot().getElement().get(i);
          if (ed.getType().size() > 1) {
            List<TypeRefComponent> toRemove = new ArrayList<ElementDefinition.TypeRefComponent>();
            for (TypeRefComponent tr : ed.getType()) {
              ElementDefinition typeSlice = findTypeSlice(derived.getSnapshot().getElement(), i, ed.getPath(), tr.getWorkingCode());
              if (typeSlice != null && typeSlice.prohibited()) {
                toRemove.add(tr);
              }
            }
            ed.getType().removeAll(toRemove);
          }
        }
        if (derived.getKind() != StructureDefinitionKind.LOGICAL && !derived.getSnapshot().getElementFirstRep().getType().isEmpty())
          throw new Error(context.formatMessage(I18nConstants.TYPE_ON_FIRST_SNAPSHOT_ELEMENT_FOR__IN__FROM_, derived.getSnapshot().getElementFirstRep().getPath(), derived.getUrl(), base.getUrl()));
        mappingDetails.update();

        setIds(derived, false);

          log.debug("Differential: ");
          int debugPad = 0;
          for (ElementDefinition ed : derived.getDifferential().getElement()) {
            log.debug(" "+Utilities.padLeft(Integer.toString(debugPad), ' ', 3)+" "+ed.getId()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed));
            debugPad++;
          }
          log.debug("Diff (processed): ");
          debugPad = 0;
          for (ElementDefinition ed : diff.getElement()) {
            log.debug(" "+Utilities.padLeft(Integer.toString(debugPad), ' ', 3)+" "+ed.getId()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+
                " -> "+(destInfo(ed, derived.getSnapshot().getElement())));
            debugPad++;
          }
          log.debug("Snapshot: ");
          debugPad = 0;
          for (ElementDefinition ed : derived.getSnapshot().getElement()) {
            log.debug(" "+Utilities.padLeft(Integer.toString(debugPad), ' ', 3)+" "+ed.getId()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed));
            debugPad++;
          }

        CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
        //Check that all differential elements have a corresponding snapshot element
        int ce = 0;
        int i = 0;
        for (ElementDefinition e : diff.getElement()) {
          if (!e.hasUserData(UserDataNames.SNAPSHOT_diff_source)) {
            // was injected during preprocessing - this is ok
          } else {
            if (e.hasUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS))
              ((Base) e.getUserData(UserDataNames.SNAPSHOT_diff_source)).setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, e.getUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS));
            if (e.hasUserData(UserDataNames.SNAPSHOT_DERIVATION_POINTER))
              ((Base) e.getUserData(UserDataNames.SNAPSHOT_diff_source)).setUserData(UserDataNames.SNAPSHOT_DERIVATION_POINTER, e.getUserData(UserDataNames.SNAPSHOT_DERIVATION_POINTER));
          }
          if (!e.hasUserData(UserDataNames.SNAPSHOT_GENERATED_IN_SNAPSHOT)) {
            b.append(e.hasId() ? "id: "+e.getId() : "path: "+e.getPath());
            ce++;
            if (e.hasId()) {
              String msg = "No match found for "+e.getId()+" in the generated snapshot: check that the path and definitions are legal in the differential (including order)";
              addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, "StructureDefinition.differential.element["+i+"]", msg, ValidationMessage.IssueSeverity.ERROR));
            }
          } else {
            ElementDefinition sed = (ElementDefinition) e.getUserData(UserDataNames.SNAPSHOT_GENERATED_IN_SNAPSHOT);
            sed.setUserData(UserDataNames.SNAPSHOT_DERIVATION_DIFF, e); // note: this means diff/snapshot are cross-linked
          }
          i++;
        }
        if (!Utilities.noString(b.toString())) {
          String msg = "The profile "+derived.getUrl()+" has "+ce+" "+Utilities.pluralize("element", ce)+" in the differential ("+b.toString()+") that don't have a matching element in the snapshot: check that the path and definitions are legal in the differential (including order)";

            log.debug("Error in snapshot generation: "+msg);

          log.debug("Differential: ");
              for (ElementDefinition ed : derived.getDifferential().getElement())
                log.debug("  "+ed.getId()+" = "+ed.getPath()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed));
          log.debug("Snapshot: ");
              for (ElementDefinition ed : derived.getSnapshot().getElement())
                log.debug("  "+ed.getId()+" = "+ed.getPath()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed));


          handleError(url, msg);
        }
        // hack around a problem in R4 definitions (somewhere?)
        for (ElementDefinition ed : derived.getSnapshot().getElement()) {
          for (ElementDefinitionMappingComponent mm : ed.getMapping()) {
            if (mm.hasMap()) {
              mm.setMap(mm.getMap().trim());
            }
          }
          for (ElementDefinitionConstraintComponent s : ed.getConstraint()) {
            if (s.hasSource()) {
              String ref = s.getSource();
              if (!Utilities.isAbsoluteUrl(ref)) {
                if (ref.contains(".")) {
                  s.setSource("http://hl7.org/fhir/StructureDefinition/"+ref.substring(0, ref.indexOf("."))+"#"+ref);
                } else {
                  s.setSource("http://hl7.org/fhir/StructureDefinition/"+ref);
                }
              }  
            }
          }
        }
        if (derived.getDerivation() == TypeDerivationRule.SPECIALIZATION) {
          for (ElementDefinition ed : derived.getSnapshot().getElement()) {
            if (!ed.hasBase()) {
              ed.getBase().setPath(ed.getPath()).setMin(ed.getMin()).setMax(ed.getMax());
            }
          }
        }
        // check slicing is ok while we're at it. and while we're doing this. update the minimum count if we need to
        String tn = derived.getType();
        if (tn.contains("/")) {
          tn = tn.substring(tn.lastIndexOf("/")+1);
        }
        Map<String, ElementDefinitionCounter> slices = new HashMap<>();
        i = 0;
        for (ElementDefinition ed : derived.getSnapshot().getElement()) {
          if (ed.hasSlicing()) {
            slices.put(ed.getPath(), new ElementDefinitionCounter(ed, i));            
          } else {
            Set<String> toRemove = new HashSet<>();
            for (String s : slices.keySet()) {
              if (Utilities.charCount(s, '.') >= Utilities.charCount(ed.getPath(), '.') && !s.equals(ed.getPath())) {
                toRemove.add(s);
              }
            }
            for (String s : toRemove) {
              ElementDefinitionCounter slice = slices.get(s);
              int count = slice.checkMin();
              boolean repeats = !"1".equals(slice.getFocus().getBase().getMax()); // type slicing if repeats = 1
              if (count > -1 && repeats) {
                if (slice.getFocus().hasUserData(UserDataNames.SNAPSHOT_auto_added_slicing)) {
                  slice.getFocus().setMin(count);
                } else {
                  String msg = "The slice definition for "+slice.getFocus().getId()+" has a minimum of "+slice.getFocus().getMin()+" but the slices add up to a minimum of "+count; 
                  addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, 
                      "StructureDefinition.snapshot.element["+slice.getIndex()+"]", msg, forPublication ? ValidationMessage.IssueSeverity.ERROR : ValidationMessage.IssueSeverity.INFORMATION).setIgnorableError(true));
                }
              }
              count = slice.checkMax();
              if (count > -1 && repeats) {
                String msg = "The slice definition for "+slice.getFocus().getId()+" has a maximum of "+slice.getFocus().getMax()+" but the slices add up to a maximum of "+count+". Check that this is what is intended"; 
                addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, 
                    "StructureDefinition.snapshot.element["+slice.getIndex()+"]", msg, ValidationMessage.IssueSeverity.INFORMATION));                                            
              }
              if (!slice.checkMinMax()) {
                String msg = "The slice definition for "+slice.getFocus().getId()+" has a maximum of "+slice.getFocus().getMax()+" which is less than the minimum of "+slice.getFocus().getMin(); 
                addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, 
                    "StructureDefinition.snapshot.element["+slice.getIndex()+"]", msg, ValidationMessage.IssueSeverity.WARNING));                                                            
              }
              slices.remove(s);
            }            
          }
          if (ed.getPath().contains(".") && !ed.getPath().startsWith(tn+".")) {
            throw new Error("The element "+ed.getId()+" in the profile '"+derived.getVersionedUrl()+" doesn't have the right path (should start with "+tn+".");
          }
          if (ed.hasSliceName() && !slices.containsKey(ed.getPath())) {
            String msg = "The element "+ed.getId()+" launches straight into slicing without the slicing being set up properly first";
            addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, 
                "StructureDefinition.snapshot.element["+i+"]", msg, ValidationMessage.IssueSeverity.ERROR).setIgnorableError(true));            
          }
          if (ed.hasSliceName() && slices.containsKey(ed.getPath())) {
            if (!slices.get(ed.getPath()).count(ed, ed.getSliceName())) {
              String msg = "Duplicate slice name "+ed.getSliceName()+" on "+ed.getId()+" (["+i+"])";
              addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, 
                  "StructureDefinition.snapshot.element["+i+"]", msg, ValidationMessage.IssueSeverity.ERROR).setIgnorableError(true));            
            }
          }
          i++;
        }
        
        i = 0;
        // last, check for wrong profiles or target profiles, or unlabeled extensions
        for (ElementDefinition ed : derived.getSnapshot().getElement()) {
          for (TypeRefComponent t : ed.getType()) {
            for (UriType u : t.getProfile()) {
              StructureDefinition sd = findProfile(u, derived);
              if (sd == null) {
                if (makeXVer().matchingUrl(u.getValue()) && xver.status(u.getValue()) == XVerExtensionStatus.Valid) {
                  sd = xver.getDefinition(u.getValue());
                }
              }
              if (sd == null) {
                addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, 
                      "StructureDefinition.snapshot.element["+i+"]", "The type of profile "+u.getValue()+" cannot be checked as the profile is not known", IssueSeverity.WARNING));
              } else {
                String wt = t.getWorkingCode();
                if (ed.getPath().equals("Bundle.entry.response.outcome")) {
                  wt = "OperationOutcome";
                }
                String tt = sd.getType();
                boolean elementProfile = u.hasExtension(ExtensionDefinitions.EXT_PROFILE_ELEMENT);
                if (elementProfile) {
                  ElementDefinition edt = sd.getSnapshot().getElementById(u.getExtensionString(ExtensionDefinitions.EXT_PROFILE_ELEMENT));
                  if (edt == null) {
                    handleError(url, "The profile "+u.getValue()+" has type "+sd.getType()+" which is not consistent with the stated type "+wt);
                  } else {
                    tt = edt.typeSummary();
                  }
                }
                if (!tt.equals(wt)) {
                  boolean ok = !elementProfile && isCompatibleType(wt, sd);
                  if (!ok) {
                    handleError(url, "The profile "+u.getValue()+" has type "+sd.getType()+" which is not consistent with the stated type "+wt);
                  }
                }
              }
            }
          }
          i++;
        }
      } catch (Exception e) {
        log.error("Exception generating snapshot for "+derived.getVersionedUrl()+": " +e.getMessage());
        log.debug(e.getMessage(), e);
        // if we had an exception generating the snapshot, make sure we don't leave any half generated snapshot behind
        derived.setSnapshot(null);
        derived.setGeneratingSnapshot(false);
        throw e;
      }
    } finally {
      Base.setCopyUserData(oldCopyUserData);          
      derived.setGeneratingSnapshot(false);
      snapshotStack.remove(derived.getUrl());
    }
    if (base.getVersion() != null) {
      derived.getSnapshot().addExtension(ExtensionDefinitions.EXT_VERSION_BASE, new StringType(base.getVersion()));
    }
    derived.setGeneratedSnapshot(true);
    //derived.setUserData(UserDataNames.SNAPSHOT_GENERATED, true); // used by the publisher
    derived.setUserData(UserDataNames.SNAPSHOT_GENERATED_MESSAGES, messages); // used by the publisher
  }


  private String destInfo(ElementDefinition ed, List<ElementDefinition> snapshot) {
    ElementDefinition sed = (ElementDefinition) ed.getUserData(UserDataNames.SNAPSHOT_GENERATED_IN_SNAPSHOT);
    if (sed == null) {
      return "(null)";
    } else {
      int index = snapshot.indexOf(sed);
      return ""+index+" "+sed.getId();
    }
  }

  private ElementDefinition findTypeSlice(List<ElementDefinition> list, int i, String path, String typeCode) {
    for (int j = i+1; j < list.size(); j++) {
      ElementDefinition ed = list.get(j);
      if (pathMatches(path, ed) && typeMatches(ed, typeCode)) {
        return ed;
      }
    }
    return null;
  }

  private boolean pathMatches(String path, ElementDefinition ed) {
    String p = ed.getPath();
    if (path.equals(p)) {
      return true;
    }
    if (path.endsWith("[x]")) { // it should
      path = path.substring(0, path.length()-3);
      if (p.startsWith(path) && p.length() > path.length() && !p.substring(path.length()).contains(".")) {
        return true;
      }
    }
    return false;
  }

  private boolean typeMatches(ElementDefinition ed, String typeCode) {
    return ed.getType().size() == 1 && typeCode.equals(ed.getTypeFirstRep().getWorkingCode());
  }

  private void checkTypeParameters(StructureDefinition base, StructureDefinition derived) {
    String bt = ExtensionUtilities.readStringSubExtension(base, ExtensionDefinitions.EXT_TYPE_PARAMETER, "type");
    if (!derived.hasExtension(ExtensionDefinitions.EXT_TYPE_PARAMETER)) {
      throw new DefinitionException(context.formatMessage(I18nConstants.SD_TYPE_PARAMETER_MISSING, base.getVersionedUrl(), bt, derived.getVersionedUrl()));
    }
    String dt = ExtensionUtilities.readStringSubExtension(derived, ExtensionDefinitions.EXT_TYPE_PARAMETER, "type");
    StructureDefinition bsd = context.fetchTypeDefinition(bt);
    StructureDefinition dsd = context.fetchTypeDefinition(dt);
    if (bsd == null) {
      throw new DefinitionException(context.formatMessage(I18nConstants.SD_TYPE_PARAMETER_UNKNOWN, base.getVersionedUrl(), bt));
    }
    if (dsd == null) {
      throw new DefinitionException(context.formatMessage(I18nConstants.SD_TYPE_PARAMETER_UNKNOWN, derived.getVersionedUrl(), dt));
    }
    StructureDefinition t = dsd;
    while (t != bsd && t != null) {
      t = findProfile(t.getBaseDefinitionElement(), t);
    }
    if (t == null) {
      throw new DefinitionException(context.formatMessage(I18nConstants.SD_TYPE_PARAMETER_INVALID, base.getVersionedUrl(), bt, derived.getVersionedUrl(), dt));
    }
  }

  private XVerExtensionManager makeXVer() {
    if (xver == null) {
      xver = XVerExtensionManagerFactory.createExtensionManager(context);
    }
    return xver;
  }

  private ElementDefinition getElementInCurrentContext(String path, List<ElementDefinition> list) {
    for (int i = list.size() -1; i >= 0; i--) {
      ElementDefinition t = list.get(i);
      if (t.getPath().equals(path)) {
        return t;
      } else if (!path.startsWith(head(t.getPath()))) {
        return null;
      }
    }
    return null;
  }

  private String head(String path) {
    return path.contains(".") ? path.substring(0, path.lastIndexOf(".")+1) : path;
  }

  private void findInheritedObligationProfiles(StructureDefinition derived) {
    List<Extension> list = derived.getExtensionsByUrl(ExtensionDefinitions.EXT_OBLIGATION_INHERITS_NEW, ExtensionDefinitions.EXT_OBLIGATION_INHERITS_OLD);
    for (Extension ext : list) {
      StructureDefinition op = findProfile(ext.getValueCanonicalType(), derived);
      if (op != null && ExtensionUtilities.readBoolExtension(op, ExtensionDefinitions.EXT_OBLIGATION_PROFILE_FLAG_NEW, ExtensionDefinitions.EXT_OBLIGATION_PROFILE_FLAG_OLD)) {
        if (derived.getBaseDefinitionNoVersion().equals(op.getBaseDefinitionNoVersion())) {
          obligationProfiles.add(op);
        }
      }
    }
  }

  private void handleError(String url, String msg) {
    addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, url, msg, ValidationMessage.IssueSeverity.ERROR));
  }

  private void addMessage(ValidationMessage msg) {
    messages.add(msg);
    if (msg.getLevel() == IssueSeverity.ERROR && wantThrowExceptions) {
      throw new DefinitionException(msg.getMessage());   
    }
  }

  private void copyInheritedExtensions(StructureDefinition base, StructureDefinition derived, String webUrl) {
    for (Extension ext : base.getExtension()) {
      if (!Utilities.existsInList(ext.getUrl(), NON_INHERITED_ED_URLS)) {
        String action = getExtensionAction(ext.getUrl());
        if (!"ignore".equals(action)) {
          boolean exists = derived.hasExtension(ext.getUrl());
          if ("add".equals(action) || !exists) {
            Extension next = ext.copy();
            if (next.hasValueMarkdownType()) {
              MarkdownType md = next.getValueMarkdownType();
              md.setValue(processRelativeUrls(md.getValue(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, false));
            }
            derived.getExtension().add(next);            
          } else if ("overwrite".equals(action)) {
            Extension oext = derived.getExtensionByUrl(ext.getUrl());
            Extension next = ext.copy();
            if (next.hasValueMarkdownType()) {
              MarkdownType md = next.getValueMarkdownType();
              md.setValue(processRelativeUrls(md.getValue(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, false));
            }
            oext.setValue(next.getValue());  
          }
        }
      }
    }
  }

  private String getExtensionAction(String url) {
    StructureDefinition sd = context.fetchResourceRaw(StructureDefinition.class, url, IWorkerContext.VersionResolutionRules.defaultRule());
    if (sd != null && sd.hasExtension(ExtensionDefinitions.EXT_SNAPSHOT_BEHAVIOR)) {
      return ExtensionUtilities.readStringExtension(sd, ExtensionDefinitions.EXT_SNAPSHOT_BEHAVIOR);
    }
    return "defer";
  }

  private void addInheritedElementsForSpecialization(StructureDefinitionSnapshotComponent snapshot, ElementDefinition focus, String type, String path, String url, String weburl) {
     StructureDefinition sd = context.fetchTypeDefinition(type);
     if (sd != null) {
       // don't do this. should already be in snapshot ... addInheritedElementsForSpecialization(snapshot, focus, sd.getBaseDefinition(), path, url, weburl);
       for (ElementDefinition ed : sd.getSnapshot().getElement()) {
         if (ed.getPath().contains(".")) {
           ElementDefinition outcome = updateURLs(url, weburl, ed.copy(), true);
           outcome.setPath(outcome.getPath().replace(sd.getTypeName(), path));
           markExtensions(outcome, false, sd);
           snapshot.getElement().add(outcome);
         } else {
           focus.getConstraint().addAll(ed.getConstraint());
           for (Extension ext : ed.getExtension()) {
             if (!Utilities.existsInList(ext.getUrl(), NON_INHERITED_ED_URLS) && !focus.hasExtension(ext.getUrl())) {
               focus.getExtension().add(markExtensionSource(ext.copy(), false, sd));
             }
           }
         }
       }
     }
  }

  private boolean walksInto(List<ElementDefinition> list, ElementDefinition ed) {
    int i = list.indexOf(ed);
    return (i < list.size() - 1) && list.get(i + 1).getPath().startsWith(ed.getPath()+".");
  }

  private void fixTypeOfResourceId(StructureDefinition base) {
    if (base.getKind() == StructureDefinitionKind.RESOURCE && (base.getFhirVersion() == null || VersionUtilities.isR4Plus(base.getFhirVersion().toCode()))) {
      fixTypeOfResourceId(base.getSnapshot().getElement());
      fixTypeOfResourceId(base.getDifferential().getElement());      
    }
  }

  private void fixTypeOfResourceId(List<ElementDefinition> list) {
    for (ElementDefinition ed : list) {
      if (ed.hasBase() && ed.getBase().getPath().equals("Resource.id")) {
        for (TypeRefComponent tr : ed.getType()) {
          tr.setCode("http://hl7.org/fhirpath/System.String");
          tr.removeExtension(ExtensionDefinitions.EXT_FHIR_TYPE);
          ExtensionUtilities.addUrlExtension(tr, ExtensionDefinitions.EXT_FHIR_TYPE, "id");
        }
      }
    }    
  }

  /**
   * Check if derived has the correct base type
   *
   * Clear first element of differential under certain conditions.
   *
   * @param derived
   * @throws Error
   */
  private void checkDifferentialBaseType(StructureDefinition derived) throws Error {
    if (derived.hasDifferential() && !derived.getDifferential().getElementFirstRep().getPath().contains(".") && !derived.getDifferential().getElementFirstRep().getType().isEmpty()) {
      if (wantFixDifferentialFirstElementType && typeMatchesAncestor(derived.getDifferential().getElementFirstRep().getType(), derived.getBaseDefinitionElement(), derived)) {
        derived.getDifferential().getElementFirstRep().getType().clear();
      } else if (derived.getKind() != StructureDefinitionKind.LOGICAL) {
        throw new Error(context.formatMessage(I18nConstants.TYPE_ON_FIRST_DIFFERENTIAL_ELEMENT));
      }
    }
  }

  private boolean typeMatchesAncestor(List<TypeRefComponent> type, UriType baseDefinition, StructureDefinition src) {
    StructureDefinition sd = findProfile(baseDefinition, src);
    return sd != null && type.size() == 1 && sd.getType().equals(type.get(0).getCode()); 
  }


  private void checkGroupConstraints(StructureDefinition derived) {
    List<ElementDefinition> toRemove = new ArrayList<>();
//    List<ElementDefinition> processed = new ArrayList<>();
    for (ElementDefinition element : derived.getSnapshot().getElement()) {
      if (!toRemove.contains(element) && !element.hasSlicing() && !"0".equals(element.getMax())) {
        checkForChildrenInGroup(derived, toRemove, element);
      }
    }
    derived.getSnapshot().getElement().removeAll(toRemove);
  }

  private void checkForChildrenInGroup(StructureDefinition derived, List<ElementDefinition> toRemove, ElementDefinition element) throws Error {
    List<ElementDefinition> children = getChildren(derived, element);
    List<ElementChoiceGroup> groups = readChoices(element, children);
    for (ElementChoiceGroup group : groups) {
      String mandated = null;
      Set<String> names = new HashSet<>();
      for (ElementDefinition ed : children) {
        String name = tail(ed.getPath());
        if (names.contains(name)) {
          throw new Error("huh?");
        } else {
          names.add(name);
        }
        if (group.getElements().contains(name)) {
          if (ed.getMin() == 1) {
            if (mandated == null) {
              mandated = name;
            } else {
              throw new Error("Error: there are two mandatory elements in "+derived.getUrl()+" when there can only be one: "+mandated+" and "+name);
            }
          }
        }
      }
      if (mandated != null) {
        for (ElementDefinition ed : children) {
          String name = tail(ed.getPath());
          if (group.getElements().contains(name) && !mandated.equals(name)) {
            ed.setMax("0");
            addAllChildren(derived, ed, toRemove);
          }
        }
      }
    }
  }

  private List<ElementDefinition> getChildren(StructureDefinition derived, ElementDefinition element) {
    List<ElementDefinition> elements = derived.getSnapshot().getElement();
    int index = elements.indexOf(element) + 1;
    String path = element.getPath()+".";
    List<ElementDefinition> list = new ArrayList<>();
    while (index < elements.size()) {
      ElementDefinition e = elements.get(index);
      String p = e.getPath();
      if (p.startsWith(path) && !e.hasSliceName()) {
        if (!p.substring(path.length()).contains(".")) {
          list.add(e);
        }
        index++;
      } else  {
        break;
      }
    }
    return list;
  }

  private void addAllChildren(StructureDefinition derived, ElementDefinition element, List<ElementDefinition> toRemove) {
    List<ElementDefinition> children = getChildList(derived, element);
    for (ElementDefinition child : children) {
      toRemove.add(child);
      addAllChildren(derived, child, toRemove);
    }
  }

  /**
   * Check that a differential is valid.
   * @param elements
   * @param type
   * @param url
   */
  private void checkDifferential(List<ElementDefinition> elements, String type, String url) {
    boolean first = true;
    String t = urlTail(type);
    for (ElementDefinition ed : elements) {
      if (!ed.hasPath()) {
        throw new FHIRException(context.formatMessage(I18nConstants.NO_PATH_ON_ELEMENT_IN_DIFFERENTIAL_IN_, url));
      }
      String p = ed.getPath();
      if (p == null) {
        throw new FHIRException(context.formatMessage(I18nConstants.NO_PATH_VALUE_ON_ELEMENT_IN_DIFFERENTIAL_IN_, url));
      }
      if (!((first && t.equals(p)) || p.startsWith(t+"."))) {
        throw new FHIRException(context.formatMessage(I18nConstants.ILLEGAL_PATH__IN_DIFFERENTIAL_IN__MUST_START_WITH_, p, url, t, (first ? " (or be '"+t+"')" : "")));
      }
      if (p.contains(".")) {
        // Element names (the parts of a path delineated by the '.' character) SHALL NOT contain whitespace (i.e. Unicode characters marked as whitespace)
        // Element names SHALL NOT contain the characters ,:;'"/|?!@#$%^&*()[]{}
        // Element names SHOULD not contain non-ASCII characters
        // Element names SHALL NOT exceed 64 characters in length
        String[] pl = p.split("\\.");
        for (String pp : pl) {
          if (pp.length() < 1) {
            throw new FHIRException(context.formatMessage(I18nConstants.ILLEGAL_PATH__IN_DIFFERENTIAL_IN__NAME_PORTION_MISING_, p, url));
          }
          if (pp.length() > 64) {
            throw new FHIRException(context.formatMessage(I18nConstants.ILLEGAL_PATH__IN_DIFFERENTIAL_IN__NAME_PORTION_EXCEEDS_64_CHARS_IN_LENGTH, p, url));
          }
          for (char ch : pp.toCharArray()) {
            if (Utilities.isWhitespace(ch)) {
              throw new FHIRException(context.formatMessage(I18nConstants.ILLEGAL_PATH__IN_DIFFERENTIAL_IN__NO_UNICODE_WHITESPACE, p, url));
            }
            if (Utilities.existsInList(ch, ',', ':', ';', '\'', '"', '/', '|', '?', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '{', '}')) {
              throw new FHIRException(context.formatMessage(I18nConstants.ILLEGAL_PATH__IN_DIFFERENTIAL_IN__ILLEGAL_CHARACTER_, p, url, ch));
            }
            if (ch < ' ' || ch > 'z') {
              throw new FHIRException(context.formatMessage(I18nConstants.ILLEGAL_PATH__IN_DIFFERENTIAL_IN__ILLEGAL_CHARACTER_, p, url, ch));
            }
          }
          if (pp.contains("[") || pp.contains("]")) {
            if (!pp.endsWith("[x]") || (pp.substring(0, pp.length()-3).contains("[") || (pp.substring(0, pp.length()-3).contains("]")))) {
              throw new FHIRException(context.formatMessage(I18nConstants.ILLEGAL_PATH__IN_DIFFERENTIAL_IN__ILLEGAL_CHARACTERS_, p, url));
            }
          }
        }
      }
    }    
  }


  private boolean isCompatibleType(String base, StructureDefinition sdt) {
    StructureDefinition sdb = context.fetchTypeDefinition(base);
    if (sdb.getType().equals(sdt.getType())) {
      return true;
    }
    StructureDefinition sd = context.fetchTypeDefinition(sdt.getType());
    while (sd != null) {
      if (sd.getType().equals(sdb.getType())) {
        return true;
      }
      if (sd.getUrl().equals(sdb.getUrl())) {
        return true;
      }
      sd = findProfile(sd.getBaseDefinitionElement(), sd);
    }
    return false;
  }


  private StructureDefinitionDifferentialComponent cloneDiff(StructureDefinitionDifferentialComponent source) {
    StructureDefinitionDifferentialComponent diff = new StructureDefinitionDifferentialComponent();
    for (ElementDefinition sed : source.getElement()) {
      ElementDefinition ted = sed.copy();
      diff.getElement().add(ted);
      ted.setUserData(UserDataNames.SNAPSHOT_diff_source, sed);
    }
    return diff;
  }

  private StructureDefinitionSnapshotComponent cloneSnapshot(StructureDefinitionSnapshotComponent source, String baseType, String derivedType) {
  	StructureDefinitionSnapshotComponent diff = new StructureDefinitionSnapshotComponent();
    for (ElementDefinition sed : source.getElement()) {
      ElementDefinition ted = sed.copy();
      ted.setId(ted.getId().replaceFirst(baseType,derivedType));
      ted.setPath(ted.getPath().replaceFirst(baseType,derivedType));
      diff.getElement().add(ted);
    }
    return diff;
  }

  private String constraintSummary(ElementDefinition ed) {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    if (ed.hasPattern())
      b.append("pattern="+ed.getPattern().fhirType());
    if (ed.hasFixed())
      b.append("fixed="+ed.getFixed().fhirType());
    if (ed.hasConstraint())
      b.append("constraints="+ed.getConstraint().size());
    return b.toString();
  }


  private String sliceSummary(ElementDefinition ed) {
    if (!ed.hasSlicing() && !ed.hasSliceName())
      return "";
    if (ed.hasSliceName())
      return " (slicename = "+ed.getSliceName()+")";
    
    StringBuilder b = new StringBuilder();
    boolean first = true;
    for (ElementDefinitionSlicingDiscriminatorComponent d : ed.getSlicing().getDiscriminator()) {
      if (first) 
        first = false;
      else
        b.append("|");
      b.append(d.getPath());
    }
    return " (slicing by "+b.toString()+")";
  }


//  private String typeSummary(ElementDefinition ed) {
//    StringBuilder b = new StringBuilder();
//    boolean first = true;
//    for (TypeRefComponent tr : ed.getType()) {
//      if (first) 
//        first = false;
//      else
//        b.append("|");
//      b.append(tr.getWorkingCode());
//    }
//    return b.toString();
//  }

  private String typeSummaryWithProfile(ElementDefinition ed) {
    StringBuilder b = new StringBuilder();
    boolean first = true;
    for (TypeRefComponent tr : ed.getType()) {
      if (first) 
        first = false;
      else
        b.append("|");
      b.append(tr.getWorkingCode());
      if (tr.hasProfile()) {
        b.append("(");
        b.append(tr.getProfile());
        b.append(")");
        
      }
    }
    return b.toString();
  }


//  private boolean findMatchingElement(String id, List<ElementDefinition> list) {
//    for (ElementDefinition ed : list) {
//      if (ed.getId().equals(id))
//        return true;
//      if (id.endsWith("[x]")) {
//        if (ed.getId().startsWith(id.substring(0, id.length()-3)) && !ed.getId().substring(id.length()-3).contains("."))
//          return true;
//      }
//    }
//    return false;
//  }

  protected ElementDefinition getById(List<ElementDefinition> list, String baseId) {
    for (ElementDefinition t : list) {
      if (baseId.equals(t.getId())) {
        return t;
      }
    }
    return null;
  }

  protected void updateConstraintSources(ElementDefinition ed, String url) {
    for (ElementDefinitionConstraintComponent c : ed.getConstraint()) {
      if (!c.hasSource()) {
        c.setSource(url);
      }
    }
    
  }

  protected Set<String> getListOfTypes(ElementDefinition e) {
    Set<String> result = new HashSet<>();
    for (TypeRefComponent t : e.getType()) {
      result.add(t.getCode());
    }
    return result;
  }

  StructureDefinition getTypeForElement(StructureDefinitionDifferentialComponent differential, int diffCursor, String profileName,
      List<ElementDefinition> diffMatches, ElementDefinition outcome, String webUrl, StructureDefinition srcSD) {
    if (outcome.getType().size() == 0) {
      if (outcome.hasContentReference()) { 
        throw new Error(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_CONTENT_REFERENCE_IN_THIS_CONTEXT, outcome.getContentReference(), outcome.getId(), outcome.getPath()));
      } else {
        throw new DefinitionException(context.formatMessage(I18nConstants._HAS_NO_CHILDREN__AND_NO_TYPES_IN_PROFILE_, diffMatches.get(0).getPath(), differential.getElement().get(diffCursor).getPath(), profileName));
      }
    }
    if (outcome.getType().size() > 1) {
      for (TypeRefComponent t : outcome.getType()) {
        if (!t.getWorkingCode().equals("Reference"))
          throw new DefinitionException(context.formatMessage(I18nConstants._HAS_CHILDREN__AND_MULTIPLE_TYPES__IN_PROFILE_, diffMatches.get(0).getPath(), differential.getElement().get(diffCursor).getPath(), typeCode(outcome.getType()), profileName));
      }
    }
    StructureDefinition dt = getProfileForDataType(outcome.getType().get(0), webUrl, srcSD);
    if (dt == null)
      throw new DefinitionException(context.formatMessage(I18nConstants.UNKNOWN_TYPE__AT_, outcome.getType().get(0), diffMatches.get(0).getPath()));
    return dt;
  }

  protected String sliceNames(List<ElementDefinition> diffMatches) {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    for (ElementDefinition ed : diffMatches) {
      if (ed.hasSliceName()) {
        b.append(ed.getSliceName());
      }
    }
    return b.toString();
  }

  protected boolean isMatchingType(StructureDefinition sd, List<TypeRefComponent> types, String inner) {
    StructureDefinition tsd = sd;
    while (tsd != null) {
      for (TypeRefComponent tr : types) {
        if (tsd.getUrl().startsWith("http://hl7.org/fhir/StructureDefinition") && tsd.getType().equals(tr.getCode())) {
          return true;
        }
        if (inner == null && tsd.getUrl().equals(tr.getCode())) {
          return true;
        }
        if (inner != null) {
          ElementDefinition ed = null;
          for (ElementDefinition t : tsd.getSnapshot().getElement()) {
            if (inner.equals(t.getId())) {
              ed = t;
            }
          }
          if (ed != null) {
            return isMatchingType(ed.getType(), types);
          }
        }
      }
      tsd = findProfile(tsd.getBaseDefinitionElement(), tsd);
    }
    return false;
  }

  private boolean isMatchingType(List<TypeRefComponent> test, List<TypeRefComponent> desired) {
    for (TypeRefComponent t : test) {
      for (TypeRefComponent d : desired) {
        if (t.getCode().equals(d.getCode())) {
          return true;          
        }
      }
    }
    return false;
  }

  protected boolean isValidType(TypeRefComponent t, ElementDefinition base) {
    for (TypeRefComponent tr : base.getType()) {
      if (tr.getCode().equals(t.getCode())) {
        return true;
      }
      if (tr.getWorkingCode().equals(t.getCode())) {
        log.error("Type error: use of a simple type \""+t.getCode()+"\" wrongly constraining "+base.getPath());
        return true;
      }
    }
    return false;
  }

  protected void checkNotGenerating(StructureDefinition sd, String role) {
    if (sd.isGeneratingSnapshot()) {
      throw new FHIRException(context.formatMessage(I18nConstants.ATTEMPT_TO_USE_A_SNAPSHOT_ON_PROFILE__AS__BEFORE_IT_IS_GENERATED, sd.getUrl(), role));
    }
  }

  protected boolean isBaseResource(List<TypeRefComponent> types) {
    if (types.isEmpty())
      return false;
    for (TypeRefComponent type : types) {
      String t = type.getWorkingCode();
      if ("Resource".equals(t))
        return false;
    }
    return true;
    
  }

  String determineFixedType(List<ElementDefinition> diffMatches, String fixedType, int i) {
    if (diffMatches.get(i).getType().size() == 0 && diffMatches.get(i).hasSliceName()) {
      String n = tail(diffMatches.get(i).getPath()).replace("[x]", "");
      String t = diffMatches.get(i).getSliceName().substring(n.length());
      if (isDataType(t)) {
        fixedType = t;
      } else if (isPrimitive(Utilities.uncapitalize(t))) {
        fixedType = Utilities.uncapitalize(t);
      } else {
        throw new FHIRException(context.formatMessage(I18nConstants.UNEXPECTED_CONDITION_IN_DIFFERENTIAL_TYPESLICETYPELISTSIZE__10_AND_IMPLICIT_SLICE_NAME_DOES_NOT_CONTAIN_A_VALID_TYPE__AT_, t, diffMatches.get(i).getPath(), diffMatches.get(i).getSliceName()));
      }                
    } else if (diffMatches.get(i).getType().size() == 1) {
      fixedType = diffMatches.get(i).getType().get(0).getCode();
    } else {
      throw new FHIRException(context.formatMessage(I18nConstants.UNEXPECTED_CONDITION_IN_DIFFERENTIAL_TYPESLICETYPELISTSIZE__1_AT_, diffMatches.get(i).getPath(), diffMatches.get(i).getSliceName()));
    }
    return fixedType;
  }


  protected BaseTypeSlice chooseMatchingBaseSlice(List<BaseTypeSlice> baseSlices, String type) {
    for (BaseTypeSlice bs : baseSlices) {
      if (bs.getType().equals(type)) {
        return bs;
      }
    }
    return null;
  }


  protected List<BaseTypeSlice> findBaseSlices(StructureDefinitionSnapshotComponent list, int start) {
    List<BaseTypeSlice> res = new ArrayList<>();
    ElementDefinition base = list.getElement().get(start);
    int i = start + 1;
    while (i <  list.getElement().size() && list.getElement().get(i).getPath().startsWith(base.getPath()+".")) {
      i++;      
    };
    while (i <  list.getElement().size() && list.getElement().get(i).getPath().equals(base.getPath()) && list.getElement().get(i).hasSliceName()) {
      int s = i;
      i++;
      while (i <  list.getElement().size() && list.getElement().get(i).getPath().startsWith(base.getPath()+".")) {
        i++;      
      };
      res.add(new BaseTypeSlice(list.getElement().get(s), list.getElement().get(s).getTypeFirstRep().getCode(), s, i-1));
    }
    return res;
  }


  protected String getWebUrl(StructureDefinition dt, String webUrl) {
    if (dt.hasWebPath()) {
      // this is a hack, but it works for now, since we don't have deep folders
      String url = dt.getWebPath();
      int i = url.lastIndexOf("/");
      if (i < 1) {
        return defWebRoot;
      } else {
        return url.substring(0, i+1);
      }
    } else {  
      return webUrl;
    }
  }

  protected String descED(List<ElementDefinition> list, int index) {
    return index >=0 && index < list.size() ? list.get(index).present() : "X";
  }

  
 
  protected String rootName(String cpath) {
    String t = tail(cpath);
    return t.replace("[x]", "");
  }


  protected String determineTypeSlicePath(String path, String cpath) {
    String headP = path.substring(0, path.lastIndexOf("."));
//    String tailP = path.substring(path.lastIndexOf(".")+1);
    String tailC = cpath.substring(cpath.lastIndexOf(".")+1);
    return headP+"."+tailC;
  }


  protected boolean isImplicitSlicing(ElementDefinition ed, String path) {
    if (ed == null || ed.getPath() == null || path == null)
      return false;
    if (path.equals(ed.getPath()))
      return false;
    boolean ok = path.endsWith("[x]") && ed.getPath().startsWith(path.substring(0, path.length()-3));
    return ok;
  }


  protected boolean diffsConstrainTypes(List<ElementDefinition> diffMatches, String cPath, List<TypeSlice> typeList) {
//    if (diffMatches.size() < 2)
    //      return false;
    String p = diffMatches.get(0).getPath();
    if (!p.endsWith("[x]") && !cPath.endsWith("[x]"))
      return false;
    typeList.clear();
    String rn = tail(cPath);
    rn = rn.substring(0, rn.length()-3);
    for (int i = 0; i < diffMatches.size(); i++) {
      ElementDefinition ed = diffMatches.get(i);
      String n = tail(ed.getPath());
      if (!n.startsWith(rn))
        return false;
      String s = n.substring(rn.length());
      if (!s.contains(".")) {
        if (ed.hasSliceName() && ed.getType().size() == 1) {
          typeList.add(new TypeSlice(ed, ed.getTypeFirstRep().getWorkingCode()));
        } else if (ed.hasSliceName() && ed.getType().size() == 0) {
          if (isDataType(s)) {
            typeList.add(new TypeSlice(ed, s));
          } else if (isPrimitive(Utilities.uncapitalize(s))) {
            typeList.add(new TypeSlice(ed, Utilities.uncapitalize(s)));
          } else {
            String tn = ed.getSliceName().substring(n.length());
            if (isDataType(tn)) {
              typeList.add(new TypeSlice(ed, tn));
            } else if (isPrimitive(Utilities.uncapitalize(tn))) {
              typeList.add(new TypeSlice(ed, Utilities.uncapitalize(tn)));
            }
          }
        } else if (!ed.hasSliceName() && !s.equals("[x]")) {
          if (isDataType(s))
            typeList.add(new TypeSlice(ed, s));
          else if (isConstrainedDataType(s))
            typeList.add(new TypeSlice(ed, baseType(s)));
          else if (isPrimitive(Utilities.uncapitalize(s)))
            typeList.add(new TypeSlice(ed, Utilities.uncapitalize(s)));
        } else if (!ed.hasSliceName() && s.equals("[x]"))
            typeList.add(new TypeSlice(ed, null));
          }
        }
    return true;
  }


  protected List<ElementRedirection> redirectorStack(List<ElementRedirection> redirector, ElementDefinition outcome, String path) {
    List<ElementRedirection> result = new ArrayList<ElementRedirection>();
    result.addAll(redirector);
    result.add(new ElementRedirection(outcome, path));
    return result;
  }


  protected List<TypeRefComponent> getByTypeName(List<TypeRefComponent> type, String t) {
    List<TypeRefComponent> res = new ArrayList<TypeRefComponent>();
    for (TypeRefComponent tr : type) {
      if (t.equals(tr.getWorkingCode()))
          res.add(tr);
    }
    return res;
  }


  protected void replaceFromContentReference(ElementDefinition outcome, ElementDefinition tgt) {
    outcome.setContentReference(null);
    outcome.getType().clear(); // though it should be clear anyway
    outcome.getType().addAll(tgt.getType());    
  }


  protected boolean baseWalksInto(List<ElementDefinition> elements, int cursor) {
    if (cursor >= elements.size())
      return false;
    String path = elements.get(cursor).getPath();
    String prevPath = elements.get(cursor - 1).getPath();
    return path.startsWith(prevPath + ".");
  }


  protected  ElementDefinition fillOutFromBase(ElementDefinition profile, ElementDefinition usage) throws FHIRFormatError {
    ElementDefinition res = profile.copy();
    if (!res.hasSliceName())
      res.setSliceName(usage.getSliceName());
    if (!res.hasLabel())
      res.setLabel(usage.getLabel());
    for (Coding c : usage.getCode())
      if (!res.hasCode(c))
        res.addCode(c);
    
    if (!res.hasDefinition())
      res.setDefinition(usage.getDefinition());
    if (!res.hasShort() && usage.hasShort())
      res.setShort(usage.getShort());
    if (!res.hasComment() && usage.hasComment())
      res.setComment(usage.getComment());
    if (!res.hasRequirements() && usage.hasRequirements())
      res.setRequirements(usage.getRequirements());
    for (StringType c : usage.getAlias())
      if (!res.hasAlias(c.getValue()))
        res.addAlias(c.getValue());
    if (!res.hasMin() && usage.hasMin())
      res.setMin(usage.getMin());
    if (!res.hasMax() && usage.hasMax())
      res.setMax(usage.getMax());
     
    if (!res.hasFixed() && usage.hasFixed())
      res.setFixed(usage.getFixed());
    if (!res.hasPattern() && usage.hasPattern())
      res.setPattern(usage.getPattern());
    if (!res.hasExample() && usage.hasExample())
      res.setExample(usage.getExample());
    if (!res.hasMinValue() && usage.hasMinValue())
      res.setMinValue(usage.getMinValue());
    if (!res.hasMaxValue() && usage.hasMaxValue())
      res.setMaxValue(usage.getMaxValue());     
    if (!res.hasMaxLength() && usage.hasMaxLength())
      res.setMaxLength(usage.getMaxLength());
    if (!res.hasMustSupport() && usage.hasMustSupport())
      res.setMustSupport(usage.getMustSupport());
    if (!res.hasIsSummary() && usage.hasIsSummary())
      res.setIsSummary(usage.getIsSummary());
    if (!res.hasIsModifier() && usage.hasIsModifier())
      res.setIsModifier(usage.getIsModifier());
    if (!res.hasIsModifierReason() && usage.hasIsModifierReason())
      res.setIsModifierReason(usage.getIsModifierReason());
    if (!res.hasMustHaveValue() && usage.hasMustHaveValue())
      res.setMustHaveValue(usage.getMustHaveValue());
    if (!res.hasBinding() && usage.hasBinding())
      res.setBinding(usage.getBinding().copy());
    for (ElementDefinitionConstraintComponent c : usage.getConstraint())
      if (!res.hasConstraint(c.getKey()))
        res.addConstraint(c);
    for (Extension e : usage.getExtension()) {
      if (!res.hasExtension(e.getUrl()))
        res.addExtension(e.copy());
    }
    
    return res;
  }


  protected boolean checkExtensionDoco(ElementDefinition base) {
    // see task 3970. For an extension, there's no point copying across all the underlying definitional stuff
    boolean isExtension = (base.getPath().equals("Extension") || base.getPath().endsWith(".extension") || base.getPath().endsWith(".modifierExtension")) &&
          (!base.hasBase() || !"II.extension".equals(base.getBase().getPath()));
    if (isExtension) {
      base.setDefinition("An Extension");
      base.setShort("Extension");
      base.setCommentElement(null);
      base.setRequirementsElement(null);
      base.getAlias().clear();
      base.getMapping().clear();
    }
    return isExtension;
  }


  protected String pathTail(List<ElementDefinition> diffMatches, int i) {
    
    ElementDefinition d = diffMatches.get(i);
    String s = d.getPath().contains(".") ? d.getPath().substring(d.getPath().lastIndexOf(".")+1) : d.getPath();
    return "."+s + (d.hasType() && d.getType().get(0).hasProfile() ? "["+d.getType().get(0).getProfile()+"]" : "");
  }


  protected void markDerived(ElementDefinition outcome) {
    for (ElementDefinitionConstraintComponent inv : outcome.getConstraint())
      inv.setUserData(UserDataNames.SNAPSHOT_IS_DERIVED, true);
  }


  static String summarizeSlicing(ElementDefinitionSlicingComponent slice) {
    StringBuilder b = new StringBuilder();
    boolean first = true;
    for (ElementDefinitionSlicingDiscriminatorComponent d : slice.getDiscriminator()) {
      if (first)
        first = false;
      else
        b.append(", ");
      b.append(d.getType().toCode()+":"+d.getPath());
    }
    b.append(" (");
    if (slice.hasOrdered())
      b.append(slice.getOrdered() ? "ordered" : "unordered");
    b.append("/");
    if (slice.hasRules())
      b.append(slice.getRules().toCode());
    b.append(")");
    if (slice.hasDescription()) {
      b.append(" \"");
      b.append(slice.getDescription());
      b.append("\"");
    }
    return b.toString();
  }


  protected void updateFromBase(ElementDefinition derived, ElementDefinition base, String baseProfileUrl) {
    derived.setUserData(UserDataNames.SNAPSHOT_BASE_MODEL, baseProfileUrl);
    derived.setUserData(UserDataNames.SNAPSHOT_BASE_PATH, base.getPath());
    if (base.hasBase()) {
      if (!derived.hasBase())
        derived.setBase(new ElementDefinitionBaseComponent());
      derived.getBase().setPath(base.getBase().getPath());
      derived.getBase().setMin(base.getBase().getMin());
      derived.getBase().setMax(base.getBase().getMax());
    } else {
      if (!derived.hasBase())
        derived.setBase(new ElementDefinitionBaseComponent());
      derived.getBase().setPath(base.getPath());
      derived.getBase().setMin(base.getMin());
      derived.getBase().setMax(base.getMax());
    }
  }


  protected boolean pathStartsWith(String p1, String p2) {
    return p1.startsWith(p2) || (p2.endsWith("[x].") && p1.startsWith(p2.substring(0, p2.length()-4)));
  }

  private boolean pathMatches(String p1, String p2) {
    return p1.equals(p2) || (p2.endsWith("[x]") && p1.startsWith(p2.substring(0, p2.length()-3)) && !p1.substring(p2.length()-3).contains("."));
  }


  protected String fixedPathSource(String contextPath, String pathSimple, List<ElementRedirection> redirector) {
    if (contextPath == null)
      return pathSimple;
//    String ptail = pathSimple.substring(contextPath.length() + 1);
    if (redirector != null && redirector.size() > 0) {
      String ptail = null;
      if (contextPath.length() >= pathSimple.length()) {
        ptail = pathSimple.substring(pathSimple.indexOf(".")+1);
      } else {
        ptail = pathSimple.substring(contextPath.length()+1);
      }
      return redirector.get(redirector.size()-1).getPath()+"."+ptail;
//      return contextPath+"."+tail(redirector.getPath())+"."+ptail.substring(ptail.indexOf(".")+1);
    } else {
      String ptail = pathSimple.substring(pathSimple.indexOf(".")+1);
      return contextPath+"."+ptail;
    }
  }
  
  protected String fixedPathDest(String contextPath, String pathSimple, List<ElementRedirection> redirector, String redirectSource) {
    String s;
    if (contextPath == null)
      s = pathSimple;
    else {
      if (redirector != null && redirector.size() > 0) {
        String ptail = null;
        if (redirectSource.length() >= pathSimple.length()) {
          ptail = pathSimple.substring(pathSimple.indexOf(".")+1);
        } else {
          ptail = pathSimple.substring(redirectSource.length()+1);
        }
  //      ptail = ptail.substring(ptail.indexOf(".")+1);
        s = contextPath+"."+/*tail(redirector.getPath())+"."+*/ptail;
      } else {
        String ptail = pathSimple.substring(pathSimple.indexOf(".")+1);
        s = contextPath+"."+ptail;
      }
    }
    return s;
  }  

  protected StructureDefinition getProfileForDataType(TypeRefComponent type, String webUrl, StructureDefinition src)  {
    StructureDefinition sd = null;
    if (type.hasProfile()) {
      sd = findProfile(type.getProfile().get(0), src);
      if (sd == null) {
        if (makeXVer().matchingUrl(type.getProfile().get(0).getValue()) && xver.status(type.getProfile().get(0).getValue()) == XVerExtensionStatus.Valid) {
          sd = xver.getDefinition(type.getProfile().get(0).getValue());
          generateSnapshot(context.fetchTypeDefinition("Extension"), sd, sd.getUrl(), webUrl, sd.getName());
        }
      }
      if (sd == null) {
          log.debug("Failed to find referenced profile: " + type.getProfile());
      }
        
    }
    if (sd == null)
      sd = context.fetchTypeDefinition(type.getWorkingCode());
    if (sd == null)
      log.warn("XX: failed to find profle for type: " + type.getWorkingCode()); // debug GJM
    return sd;
  }

  protected StructureDefinition getProfileForDataType(String type)  {
    StructureDefinition sd = context.fetchTypeDefinition(type);
    if (sd == null)
      log.warn("XX: failed to find profle for type: " + type); // debug GJM
    return sd;
  }

  static String typeCode(List<TypeRefComponent> types) {
    StringBuilder b = new StringBuilder();
    boolean first = true;
    for (TypeRefComponent type : types) {
      if (first) first = false; else b.append(", ");
      b.append(type.getWorkingCode());
      if (type.hasTargetProfile())
        b.append("{"+type.getTargetProfile()+"}");
      else if (type.hasProfile())
        b.append("{"+type.getProfile()+"}");
    }
    return b.toString();
  }


  protected boolean isDataType(List<TypeRefComponent> types) {
    if (types.isEmpty())
      return false;
    for (TypeRefComponent type : types) {
      String t = type.getWorkingCode();
      if (!isDataType(t) && !isPrimitive(t))
        return false;
    }
    return true;
  }


  /**
   * Finds internal references in an Element's Binding and StructureDefinition references (in TypeRef) and bases them on the given url
   * @param url - the base url to use to turn internal references into absolute references
   * @param element - the Element to update
   * @return - the updated Element
   */
  public ElementDefinition updateURLs(String url, String webUrl, ElementDefinition element, boolean processRelatives) {
    if (element != null) {
      ElementDefinition defn = element;
      if (defn.hasBinding() && defn.getBinding().hasValueSet() && defn.getBinding().getValueSet().startsWith("#"))
        defn.getBinding().setValueSet(url+defn.getBinding().getValueSet());
      for (TypeRefComponent t : defn.getType()) {
        for (UriType u : t.getProfile()) {
          if (u.getValue().startsWith("#"))
            u.setValue(url+t.getProfile());
        }
        for (UriType u : t.getTargetProfile()) {
          if (u.getValue().startsWith("#"))
            u.setValue(url+t.getTargetProfile());
        }
      }
      if (webUrl != null) {
        // also, must touch up the markdown
        if (element.hasDefinition()) {
          element.setDefinition(processRelativeUrls(element.getDefinition(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, processRelatives));
        }
        if (element.hasComment()) {
          element.setComment(processRelativeUrls(element.getComment(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, processRelatives));
        }
        if (element.hasRequirements()) {
          element.setRequirements(processRelativeUrls(element.getRequirements(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, processRelatives));
        }
        if (element.hasMeaningWhenMissing()) {
          element.setMeaningWhenMissing(processRelativeUrls(element.getMeaningWhenMissing(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, processRelatives));
        }
        if (element.hasBinding() && element.getBinding().hasDescription()) {
          element.getBinding().setDescription(processRelativeUrls(element.getBinding().getDescription(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, processRelatives));
        }
        for (Extension ext : element.getExtension()) {
          if (ext.hasValueMarkdownType()) {
            MarkdownType md = ext.getValueMarkdownType();
            md.setValue(processRelativeUrls(md.getValue(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, processRelatives));
          }
        }
      }
    }
    return element;
  }

  
  public static String processRelativeUrls(String markdown, String webUrl, String basePath, List<String> resourceNames, Set<String> baseFilenames, Set<String> localFilenames, boolean processRelatives) {
    if (markdown == null) {
      return "";
    }
    Set<String> anchorRefs = new HashSet<>();
    markdown = markdown+" ";
    
    StringBuilder b = new StringBuilder();
    int i = 0;
    int left = -1;
    boolean processingLink = false;
    int linkLeft = -1;
    while (i < markdown.length()) {
      if (markdown.charAt(i) == '[') {
        if (left == -1) {
          left = i;
        } else {
          left = Integer.MAX_VALUE;
        }
      }
      if (markdown.charAt(i) == ']') {
        if (left != -1 && left != Integer.MAX_VALUE && markdown.length() > i && markdown.charAt(i+1) != '(') {
          String n = markdown.substring(left+1, i);
          if (anchorRefs.contains(n) && markdown.length() > i && markdown.charAt(i+1) == ':') {
            processingLink = true;            
          } else {
            anchorRefs.add(n);
          }
        }
        left = -1;
      }
      if (processingLink) {
        char ch = markdown.charAt(i);
        if (linkLeft == -1) {
          if (ch != ']' && ch != ':' && !Character.isWhitespace(ch)) {
            linkLeft = i;
          } else {
            b.append(ch);
          }
        } else {
          if (Character.isWhitespace(ch)) {
            // found the end of the processible link:
            String url = markdown.substring(linkLeft, i);
            if (isLikelySourceURLReference(url, resourceNames, baseFilenames, localFilenames, webUrl)) {
              b.append(basePath);
              if (!Utilities.noString(basePath) && !basePath.endsWith("/")) {
                b.append("/");
              }
            }
            b.append(url);
            b.append(ch);
            linkLeft = -1;
          }
        }
      } else {
        if (i < markdown.length()-3 && markdown.substring(i, i+2).equals("](")) {

          int j = i + 2;
          while (j < markdown.length() && markdown.charAt(j) != ')')
            j++;
          if (j < markdown.length()) {
            String url = markdown.substring(i+2, j);
            if (!Utilities.isAbsoluteUrl(url) && !url.startsWith("..")) {
              // 
              // In principle, relative URLs are supposed to be converted to absolute URLs in snapshots. 
              // that's what this code is doing. 
              // 
              // But that hasn't always happened and there's packages out there where the snapshots 
              // contain relative references that actually are references to the main specification 
              // 
              // This code is trying to guess which relative references are actually to the
              // base specification.
              // 
              if (isLikelySourceURLReference(url, resourceNames, baseFilenames, localFilenames, webUrl)) {
                b.append("](");
                b.append(basePath);
                if (!Utilities.noString(basePath) && !basePath.endsWith("/")) {
                  b.append("/");
                }
                i = i + 1;
              } else {
                b.append("](");
                // disabled 7-Dec 2021 GDG - we don't want to fool with relative URLs at all? 
                // re-enabled 11-Feb 2022 GDG - we do want to do this. At least, $assemble in davinci-dtr, where the markdown comes from the SDC IG, and an SDC local reference must be changed to point to SDC. in this case, it's called when generating snapshots
                // added processRelatives parameter to deal with this (well, to try)
                if (processRelatives && webUrl != null && !issLocalFileName(url, localFilenames)) {

                  b.append(webUrl);
                  if (!Utilities.noString(webUrl) && !webUrl.endsWith("/")) {
                    b.append("/");
                  }
                } else {
                  //DO NOTHING
                }
                i = i + 1;
              }
            } else
              b.append(markdown.charAt(i));
          } else 
            b.append(markdown.charAt(i));
        } else {
          b.append(markdown.charAt(i));
        }
      }
      i++;
    }
    String s = b.toString();
    return Utilities.rightTrim(s);
  }

  private static boolean issLocalFileName(String url, Set<String> localFilenames) {
    if (localFilenames != null) {
      for (String n : localFilenames) {
        if (url.startsWith(n.toLowerCase())) {
          return true;
        }
      }
    }
    return false; 
  }


  private static boolean isLikelySourceURLReference(String url, List<String> resourceNames, Set<String> baseFilenames, Set<String> localFilenames, String baseUrl) {
    if (url == null) {
      return false;
    }
    if (baseUrl != null && !baseUrl.startsWith("http://hl7.org/fhir/R")) {
      if (resourceNames != null) {
        for (String n : resourceNames) {
          if (n != null && url.startsWith(n.toLowerCase()+".html")) {
            return true;
          }
          if (n != null && url.startsWith(n.toLowerCase()+"-definitions.html")) {
            return true;
          }
        }
      }
      if (localFilenames != null) {
        for (String n : localFilenames) {
          if (n != null && url.startsWith(n.toLowerCase())) {
            return false;
          }
        }
      }
      if (baseFilenames != null) {
        for (String n : baseFilenames) {
          if (n != null && url.startsWith(n.toLowerCase())) {
            return true;
          }
        }
      }
    }
    return 
        url.startsWith("extensibility.html") || 
        url.startsWith("terminologies.html") || 
        url.startsWith("observation.html") || 
        url.startsWith("codesystem.html") || 
        url.startsWith("fhirpath.html") || 
        url.startsWith("datatypes.html") || 
        url.startsWith("operations.html") || 
        url.startsWith("resource.html") || 
        url.startsWith("elementdefinition.html") ||
        url.startsWith("element-definitions.html") ||
        url.startsWith("snomedct.html") ||
        url.startsWith("loinc.html") ||
        url.startsWith("http.html") ||
        url.startsWith("references") ||
        url.startsWith("license.html") ||
        url.startsWith("narrative.html") || 
        url.startsWith("search.html") ||
        url.startsWith("security.html") ||
        url.startsWith("versions.html") ||
        url.startsWith("patient-operation-match.html") ||
        (url.startsWith("extension-") && url.contains(".html")) || 
        url.startsWith("resource-definitions.html");
  }

  protected List<ElementDefinition> getSiblings(List<ElementDefinition> list, ElementDefinition current) {
    List<ElementDefinition> result = new ArrayList<ElementDefinition>();
    String path = current.getPath();
    int cursor = list.indexOf(current)+1;
    while (cursor < list.size() && list.get(cursor).getPath().length() >= path.length()) {
      if (pathMatches(list.get(cursor).getPath(), path))
        result.add(list.get(cursor));
      cursor++;
    }
    return result;
  }

  protected void updateFromSlicing(ElementDefinitionSlicingComponent dst, ElementDefinitionSlicingComponent src) {
    if (src.hasOrderedElement())
      dst.setOrderedElement(src.getOrderedElement().copy());
    if (src.hasDiscriminator()) {
      //    dst.getDiscriminator().addAll(src.getDiscriminator());  Can't use addAll because it uses object equality, not string equality
      for (ElementDefinitionSlicingDiscriminatorComponent s : src.getDiscriminator()) {
        boolean found = false;
        for (ElementDefinitionSlicingDiscriminatorComponent d : dst.getDiscriminator()) {
          if (matches(d, s)) {
            found = true;
            break;
          }
        }
        if (!found)
          dst.getDiscriminator().add(s);
      }
    }
    if (src.hasRulesElement())
      dst.setRulesElement(src.getRulesElement().copy());
  }

  protected boolean orderMatches(BooleanType diff, BooleanType base) {
    return (diff == null) || (base == null) || (diff.getValue() == base.getValue());
  }

  protected boolean discriminatorMatches(List<ElementDefinitionSlicingDiscriminatorComponent> diff, List<ElementDefinitionSlicingDiscriminatorComponent> base) {
    if (diff.isEmpty() || base.isEmpty())
    	return true;
    if (diff.size() < base.size())
    	return false;
    for (int i = 0; i < base.size(); i++)
    	if (!matches(diff.get(i), base.get(i)))
    		return false;
    return true;
  }

  private boolean matches(ElementDefinitionSlicingDiscriminatorComponent c1, ElementDefinitionSlicingDiscriminatorComponent c2) {
    return c1.getType().equals(c2.getType()) && c1.getPath().equals(c2.getPath());
  }


  protected boolean ruleMatches(SlicingRules diff, SlicingRules base) {
    return (diff == null) || (base == null) || (diff == base) || (base == SlicingRules.OPEN) ||
        ((diff == SlicingRules.OPENATEND && base == SlicingRules.CLOSED));
  }

  protected boolean isSlicedToOneOnly(ElementDefinition e) {
    return (e.hasSlicing() && e.hasMaxElement() && e.getMax().equals("1"));
  }

  protected boolean isTypeSlicing(ElementDefinition e) {
    return (e.hasSlicing() && e.getSlicing().getDiscriminator().size() == 1 && 
        e.getSlicing().getDiscriminatorFirstRep().getType() == DiscriminatorType.TYPE &&
        "$this".equals(e.getSlicing().getDiscriminatorFirstRep().getPath()));
  }

  
  protected ElementDefinitionSlicingComponent makeExtensionSlicing() {
  	ElementDefinitionSlicingComponent slice = new ElementDefinitionSlicingComponent();
    slice.addDiscriminator().setPath("url").setType(DiscriminatorType.VALUE);
    slice.setOrdered(false);
    slice.setRules(SlicingRules.OPEN);
    return slice;
  }

  protected boolean isExtension(ElementDefinition currentBase) {
    return currentBase.getPath().endsWith(".extension") || currentBase.getPath().endsWith(".modifierExtension");
  }

  boolean hasInnerDiffMatches(StructureDefinitionDifferentialComponent context, String path, int start, int end, List<ElementDefinition> base, boolean allowSlices) throws DefinitionException {
    end = Math.min(context.getElement().size(), end);
    start = Math.max(0,  start);
  
    for (int i = start; i <= end; i++) {
      ElementDefinition ed = context.getElement().get(i);
      String statedPath = ed.getPath();
      if (!allowSlices && statedPath.equals(path) && ed.hasSliceName()) {
        return false;
      } else if (statedPath.startsWith(path+".")) {
        return true;
      } else if (path.endsWith("[x]") && statedPath.startsWith(path.substring(0, path.length() -3))) {
        return true;
      } else if (i != start && !allowSlices && !statedPath.startsWith(path+".")) {
        return false;
      } else if (i != start && allowSlices && !statedPath.startsWith(path)) {
        return false;
      } else {
        // not sure why we get here, but returning false at this point makes a bunch of tests fail
      }
    }
    return false;
  }

  protected List<ElementDefinition> getDiffMatches(StructureDefinitionDifferentialComponent context, String path, int start, int end, String profileName) throws DefinitionException {
    List<ElementDefinition> result = new ArrayList<ElementDefinition>();
    String[] p = path.split("\\.");
    for (int i = start; i <= end; i++) {
      String statedPath = context.getElement().get(i).getPath();
      String[] sp = statedPath.split("\\.");
      boolean ok = sp.length == p.length;
      for (int j = 0; j < p.length; j++) {
        ok = ok && sp.length > j && (p[j].equals(sp[j]) || isSameBase(p[j], sp[j]));
      }
// don't need this debug check - everything is ok
//      if (ok != (statedPath.equals(path) || (path.endsWith("[x]") && statedPath.length() > path.length() - 2 &&
//            statedPath.substring(0, path.length()-3).equals(path.substring(0, path.length()-3)) &&
//            (statedPath.length() < path.length() || !statedPath.substring(path.length()).contains("."))))) {
//
//      }
      if (ok) {
        /*
         * Commenting this out because it raises warnings when profiling inherited elements.  For example,
         * Error: unknown element 'Bundle.meta.profile' (or it is out of order) in profile ... (looking for 'Bundle.entry')
         * Not sure we have enough information here to do the check properly.  Might be better done when we're sorting the profile?

        if (i != start && result.isEmpty() && !path.startsWith(context.getElement().get(start).getPath()))
          addMessage(new ValidationMessage(Source.ProfileValidator, IssueType.VALUE, "StructureDefinition.differential.element["+Integer.toString(start)+"]", "Error: unknown element '"+context.getElement().get(start).getPath()+"' (or it is out of order) in profile '"+url+"' (looking for '"+path+"')", IssueSeverity.WARNING));

         */
        result.add(context.getElement().get(i));
      }
    }
    if (debug) {
      Set<String> ids = new HashSet<>();
      for (ElementDefinition ed : result) {
        ids.add(ed.getIdOrPath());
      }
    }
    return result;
  }


  private boolean isSameBase(String p, String sp) {
    return (p.endsWith("[x]") && sp.startsWith(p.substring(0, p.length()-3))) || (sp.endsWith("[x]") && p.startsWith(sp.substring(0, sp.length()-3))) ;
  }

  protected int findEndOfElement(StructureDefinitionDifferentialComponent context, int cursor) {
	    int result = cursor;
	    if (cursor >= context.getElement().size())
	      return result;
	    String path = context.getElement().get(cursor).getPath()+".";
	    while (result < context.getElement().size()- 1 && context.getElement().get(result+1).getPath().startsWith(path))
	      result++;
	    return result;
	  }

  protected int findEndOfElement(StructureDefinitionSnapshotComponent context, int cursor) {
      int result = cursor;
      String path = context.getElement().get(cursor).getPath()+".";
      while (result < context.getElement().size()- 1 && context.getElement().get(result+1).getPath().startsWith(path))
        result++;
      return result;
  }
  
  protected int findEndOfElementNoSlices(StructureDefinitionSnapshotComponent context, int cursor) {
    int result = cursor;
    String path = context.getElement().get(cursor).getPath()+".";
    while (result < context.getElement().size()- 1 && context.getElement().get(result+1).getPath().startsWith(path) && !context.getElement().get(result+1).hasSliceName())
      result++;
    return result;
  }

  protected boolean unbounded(ElementDefinition definition) {
    StringType max = definition.getMaxElement();
    if (max == null)
      return false; // this is not valid
    if (max.getValue().equals("1"))
      return false;
    if (max.getValue().equals("0"))
      return false;
    return true;
  }


  public void updateFromObligationProfiles(ElementDefinition base) {
    List<ElementDefinition> obligationProfileElements = new ArrayList<>();
    for (StructureDefinition sd : obligationProfiles) {
      ElementDefinition ed = sd.getSnapshot().getElementById(base.getId());
      if (ed != null) {
        obligationProfileElements.add(ed);
      }
    }
    for (ElementDefinition ed : obligationProfileElements) {
      for (Extension ext : ed.getExtension()) {
        if (Utilities.existsInList(ext.getUrl(), ExtensionDefinitions.EXT_OBLIGATION_CORE, ExtensionDefinitions.EXT_OBLIGATION_TOOLS)) {
          base.getExtension().add(ext.copy());
        }      
      }
    }
    boolean hasMustSupport = false;
    for (ElementDefinition ed : obligationProfileElements) {
      hasMustSupport = hasMustSupport || ed.hasMustSupportElement();
    }
    if (hasMustSupport) {
      for (ElementDefinition ed : obligationProfileElements) {
        mergeExtensions(base.getMustSupportElement(), ed.getMustSupportElement());
        if (ed.getMustSupport()) {
          base.setMustSupport(true);
        }
      }
    }
    boolean hasBinding = false;
    for (ElementDefinition ed : obligationProfileElements) {
      hasBinding = hasBinding || ed.hasBinding();
    }
    if (hasBinding) {
      ElementDefinitionBindingComponent binding = base.getBinding();
      for (ElementDefinition ed : obligationProfileElements) {
        for (Extension ext : ed.getBinding().getExtension()) {
          if (ExtensionDefinitions.EXT_BINDING_ADDITIONAL.equals(ext.getUrl())) {
            String p = ext.getExtensionString("purpose");
            if (!Utilities.existsInList(p, "maximum", "required", "extensible")) {
              if (!binding.hasExtension(ext)) {
                binding.getExtension().add(ext.copy());
              }
            }
          }
        }
        for (ElementDefinitionBindingAdditionalComponent ab : ed.getBinding().getAdditional()) {
          if (!Utilities.existsInList(ab.getPurpose().toCode(), "maximum", "required", "extensible")) {
            if (!binding.hasAdditional(ab)) {
              binding.getAdditional().add(ab.copy());
            }
          }
        }
      }
    }
  }

  
  protected void updateFromDefinition(ElementDefinition dest, ElementDefinition source, String pn, boolean trimDifferential, String purl, StructureDefinition srcSD, StructureDefinition derivedSrc, String path, MappingAssistant mappings, boolean fromSlicer) throws DefinitionException, FHIRException {
    source.setUserData(UserDataNames.SNAPSHOT_GENERATED_IN_SNAPSHOT, dest);
    // we start with a clone of the base profile ('dest') and we copy from the profile ('source')
    // over the top for anything the source has
    ElementDefinition base = dest;
    ElementDefinition derived = source;
    derived.setUserData(UserDataNames.SNAPSHOT_DERIVATION_POINTER, base);
    boolean isExtension = checkExtensionDoco(base);
    List<ElementDefinition> obligationProfileElements = new ArrayList<>();
    for (StructureDefinition sd : obligationProfiles) {
      ElementDefinition ed = sd.getSnapshot().getElementById(base.getId());
      if (ed != null) {
        obligationProfileElements.add(ed);
      }
    }

    // hack workaround for problem in R5 snapshots
    List<Extension> elist = dest.getExtensionsByUrl(ExtensionDefinitions.EXT_TRANSLATABLE);
    if (elist.size() == 2) {
      dest.getExtension().remove(elist.get(1));
    }
    updateExtensionsFromDefinition(dest, source, derivedSrc, srcSD);

    for (ElementDefinition ed : obligationProfileElements) {
      for (Extension ext : ed.getExtension()) {
        if (Utilities.existsInList(ext.getUrl(), ExtensionDefinitions.EXT_OBLIGATION_CORE, ExtensionDefinitions.EXT_OBLIGATION_TOOLS)) {
          dest.getExtension().add(new Extension(ExtensionDefinitions.EXT_OBLIGATION_CORE, ext.getValue().copy()));
        }      
      }
    }
    
    // Before applying changes, apply them to what's in the profile
    // but only if it's an extension or a resource

    StructureDefinition profile = null;
    boolean msg = true;
    if (base.hasSliceName()) {
      profile = base.getType().size() == 1 && base.getTypeFirstRep().hasProfile() ? findProfile(base.getTypeFirstRep().getProfile().get(0), srcSD) : null;
    }
    if (profile == null && source.getTypeFirstRep().hasProfile()) {
      String pu = source.getTypeFirstRep().getProfile().get(0).getValue();
      profile = findProfile(source.getTypeFirstRep().getProfile().get(0), derivedSrc);
      if (profile == null) {
        if (makeXVer().matchingUrl(pu)) {
          switch (xver.status(pu)) {
            case BadVersion:
              throw new FHIRException("Reference to invalid version in extension url " + pu);
            case Invalid:
              throw new FHIRException("Reference to invalid extension " + pu);
            case Unknown:
              throw new FHIRException("Reference to unknown extension " + pu);
            case Valid:
              profile = xver.getDefinition(pu);
              generateSnapshot(context.fetchTypeDefinition("Extension"), profile, profile.getUrl(), context.getSpecUrl(), profile.getName());
          }
        }
        
      }
      if (profile != null && !"Extension".equals(profile.getType()) && profile.getKind() != StructureDefinitionKind.RESOURCE && profile.getKind() != StructureDefinitionKind.LOGICAL) {
        // this is a problem - we're kind of hacking things here. The problem is that we sometimes want the details from the profile to override the 
        // inherited attributes, and sometimes not
        profile = null;
        msg = false;
      }
    }
    if (profile != null && (profile.getKind() == StructureDefinitionKind.RESOURCE || "Extension".equals(profile.getType()))) {
      if (profile.getSnapshot().getElement().isEmpty()) {
        throw new DefinitionException(context.formatMessage(I18nConstants.SNAPSHOT_IS_EMPTY, profile.getVersionedUrl()));
      }
      ElementDefinition e = profile.getSnapshot().getElement().get(0);
      String webroot = profile.getUserString(UserDataNames.render_webroot);

      if (e.hasDefinition()) {
        base.setDefinition(processRelativeUrls(e.getDefinition(), webroot, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, true));
      }
      if (e.getBinding().hasDescription()) {
        base.getBinding().setDescription(processRelativeUrls(e.getBinding().getDescription(), webroot, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, localFileNames, true));
      }
      base.setShort(e.getShort());
      if (e.hasCommentElement())
        base.setCommentElement(e.getCommentElement());
      if (e.hasRequirementsElement())
        base.setRequirementsElement(e.getRequirementsElement());
      base.getAlias().clear();
      base.getAlias().addAll(e.getAlias());
      base.getMapping().clear();
      base.getMapping().addAll(e.getMapping());
    } else if (source.getType().size() == 1 && source.getTypeFirstRep().hasProfile() && !source.getTypeFirstRep().getProfile().get(0).hasExtension(ExtensionDefinitions.EXT_PROFILE_ELEMENT)) {
      // todo: should we change down the profile_element if there's one?
      String type = source.getTypeFirstRep().getWorkingCode();
      if (msg) {
        if ("Extension".equals(type)) {
          log.warn("Can't find Extension definition for "+source.getTypeFirstRep().getProfile().get(0).asStringValue()+" but trying to go on");
          if (allowUnknownProfile != AllowUnknownProfile.ALL_TYPES) {
            throw new DefinitionException("Unable to find Extension definition for "+source.getTypeFirstRep().getProfile().get(0).asStringValue());          
          }
        } else {
          log.warn("Can't find "+type+" profile "+source.getTypeFirstRep().getProfile().get(0).asStringValue()+" but trying to go on");
          if (allowUnknownProfile == AllowUnknownProfile.NONE) {
            throw new DefinitionException("Unable to find "+type+" profile "+source.getTypeFirstRep().getProfile().get(0).asStringValue());          
          }
        }
      }
    }
    if (derived != null) {
      if (derived.hasSliceName()) {
        base.setSliceName(derived.getSliceName());
      }

      if (derived.hasShortElement()) {
        if (!Base.compareDeep(derived.getShortElement(), base.getShortElement(), false))
          base.setShortElement(derived.getShortElement().copy());
        else if (trimDifferential)
          derived.setShortElement(null);
        else if (derived.hasShortElement())
          derived.getShortElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      if (derived.hasDefinitionElement()) {
        if (!Base.compareDeep(derived.getDefinitionElement(), base.getDefinitionElement(), false)) {
          base.setDefinitionElement(mergeMarkdown(derived.getDefinitionElement(), base.getDefinitionElement()));
        } else if (trimDifferential)
          derived.setDefinitionElement(null);
        else if (derived.hasDefinitionElement())
          derived.getDefinitionElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      if (derived.hasCommentElement()) {
        if (!Base.compareDeep(derived.getCommentElement(), base.getCommentElement(), false))
          base.setCommentElement(mergeMarkdown(derived.getCommentElement(), base.getCommentElement()));
        else if (trimDifferential)
          base.setCommentElement(derived.getCommentElement().copy());
        else if (derived.hasCommentElement())
          derived.getCommentElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      if (derived.hasLabelElement()) {
       if (!base.hasLabelElement() || !Base.compareDeep(derived.getLabelElement(), base.getLabelElement(), false))
          base.setLabelElement(mergeStrings(derived.getLabelElement(), base.getLabelElement()));
        else if (trimDifferential)
          base.setLabelElement(derived.getLabelElement().copy());
        else if (derived.hasLabelElement())
          derived.getLabelElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      if (derived.hasRequirementsElement()) {
        if (!base.hasRequirementsElement() || !Base.compareDeep(derived.getRequirementsElement(), base.getRequirementsElement(), false))
          base.setRequirementsElement(mergeMarkdown(derived.getRequirementsElement(), base.getRequirementsElement()));
        else if (trimDifferential)
          base.setRequirementsElement(derived.getRequirementsElement().copy());
        else if (derived.hasRequirementsElement())
          derived.getRequirementsElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }
      // sdf-9
      if (derived.hasRequirements() && !base.getPath().contains("."))
        derived.setRequirements(null);
      if (base.hasRequirements() && !base.getPath().contains("."))
        base.setRequirements(null);

      if (derived.hasAlias()) {
        if (!Base.compareDeep(derived.getAlias(), base.getAlias(), false))
          for (StringType s : derived.getAlias()) {
            if (!base.hasAlias(s.getValue()))
              base.getAlias().add(s.copy());
          }
        else if (trimDifferential)
          derived.getAlias().clear();
        else
          for (StringType t : derived.getAlias())
            t.setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      if (derived.hasMinElement()) {
        if (!Base.compareDeep(derived.getMinElement(), base.getMinElement(), false)) {
          if (derived.getMin() < base.getMin() && !derived.hasSliceName()) // in a slice, minimum cardinality rules do not apply
            addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+source.getPath(), "Element "+base.getPath()+": derived min ("+Integer.toString(derived.getMin())+") cannot be less than the base min ("+Integer.toString(base.getMin())+") in "+srcSD.getVersionedUrl(), ValidationMessage.IssueSeverity.ERROR));
          base.setMinElement(derived.getMinElement().copy());
        } else if (trimDifferential)
          derived.setMinElement(null);
        else
          derived.getMinElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      if (derived.hasMaxElement()) {
        if (!Base.compareDeep(derived.getMaxElement(), base.getMaxElement(), false)) {
          if (isLargerMax(derived.getMax(), base.getMax()))
            addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+source.getPath(), "Element "+base.getPath()+": derived max ("+derived.getMax()+") cannot be greater than the base max ("+base.getMax()+")", ValidationMessage.IssueSeverity.ERROR));
          base.setMaxElement(derived.getMaxElement().copy());
        } else if (trimDifferential)
          derived.setMaxElement(null);
        else
          derived.getMaxElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      if (derived.hasFixed()) {
        if (!Base.compareDeep(derived.getFixed(), base.getFixed(), true)) {
          base.setFixed(derived.getFixed().copy());
        } else if (trimDifferential)
          derived.setFixed(null);
        else
          derived.getFixed().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      if (derived.hasPattern()) {
        if (!Base.compareDeep(derived.getPattern(), base.getPattern(), false)) {
          base.setPattern(derived.getPattern().copy());
        } else
          if (trimDifferential)
            derived.setPattern(null);
          else
            derived.getPattern().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      List<ElementDefinitionExampleComponent> toDelB = new ArrayList<>();
      List<ElementDefinitionExampleComponent> toDelD = new ArrayList<>();
      for (ElementDefinitionExampleComponent ex : derived.getExample()) {
        boolean delete = ex.hasExtension(ExtensionDefinitions.EXT_ED_SUPPRESS);
        if (delete && "$all".equals(ex.getLabel())) {
          toDelB.addAll(base.getExample());
        } else {
          boolean found = false;
          for (ElementDefinitionExampleComponent exS : base.getExample()) {
            if (Base.compareDeep(ex.getLabel(), exS.getLabel(), false) && Base.compareDeep(ex.getValue(), exS.getValue(), false)) {
              if (delete) {
                toDelB.add(exS);
              } else {
                found = true;
              }
            }
          }
          if (delete) {
            toDelD.add(ex);
          } else if (!found) {
            base.addExample(ex.copy());
          } else if (trimDifferential) {
            derived.getExample().remove(ex);
          } else {
            ex.setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
          }
        }
      }
      base.getExample().removeAll(toDelB);
      derived.getExample().removeAll(toDelD);

      if (derived.hasMaxLengthElement()) {
        if (!Base.compareDeep(derived.getMaxLengthElement(), base.getMaxLengthElement(), false))
          base.setMaxLengthElement(derived.getMaxLengthElement().copy());
        else if (trimDifferential)
          derived.setMaxLengthElement(null);
        else
          derived.getMaxLengthElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }
  
      if (derived.hasMaxValue()) {
        if (!Base.compareDeep(derived.getMaxValue(), base.getMaxValue(), false))
          base.setMaxValue(derived.getMaxValue().copy());
        else if (trimDifferential)
          derived.setMaxValue(null);
        else
          derived.getMaxValue().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }
  
      if (derived.hasMinValue()) {
        if (!Base.compareDeep(derived.getMinValue(), base.getMinValue(), false))
          base.setMinValue(derived.getMinValue().copy());
        else if (trimDifferential)
          derived.setMinValue(null);
        else
          derived.getMinValue().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      // todo: what to do about conditions?
      // condition : id 0..*

      boolean hasMustSupport = derived.hasMustSupportElement();
      for (ElementDefinition ed : obligationProfileElements) {
        hasMustSupport = hasMustSupport || ed.hasMustSupportElement();
      }
      if (hasMustSupport) {
        BooleanType mse = derived.getMustSupportElement().copy();
        for (ElementDefinition ed : obligationProfileElements) {
          mergeExtensions(mse, ed.getMustSupportElement());
          if (ed.getMustSupport()) {
            mse.setValue(true);
          }
        }
        if (!(base.hasMustSupportElement() && Base.compareDeep(base.getMustSupportElement(), mse, false))) {
          if (base.hasMustSupport() && base.getMustSupport() && !derived.getMustSupport() && !fromSlicer) {
            addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Illegal constraint [must-support = false] when [must-support = true] in the base profile", ValidationMessage.IssueSeverity.ERROR));
          }
          base.setMustSupportElement(mse);
        } else if (trimDifferential)
          derived.setMustSupportElement(null);
        else
          derived.getMustSupportElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }
      
      if (derived.hasMustHaveValueElement()) {
        if (!(base.hasMustHaveValueElement() && Base.compareDeep(derived.getMustHaveValueElement(), base.getMustHaveValueElement(), false))) {
          if (base.hasMustHaveValue() && base.getMustHaveValue() && !derived.getMustHaveValue() && !fromSlicer) {
            addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Illegal constraint [must-have-value = false] when [must-have-value = true] in the base profile", ValidationMessage.IssueSeverity.ERROR));
          }
          base.setMustHaveValueElement(derived.getMustHaveValueElement().copy());
        } else if (trimDifferential)
          derived.setMustHaveValueElement(null);
        else
          derived.getMustHaveValueElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }
      if (derived.hasValueAlternatives()) {
        if (!Base.compareDeep(derived.getValueAlternatives(), base.getValueAlternatives(), false))
          for (CanonicalType s : derived.getValueAlternatives()) {
            if (!base.hasValueAlternatives(s.getValue()))
              base.getValueAlternatives().add(s.copy());
          }
        else if (trimDifferential)
          derived.getValueAlternatives().clear();
        else
          for (CanonicalType t : derived.getValueAlternatives())
            t.setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      // profiles cannot change : isModifier, defaultValue, meaningWhenMissing
      // but extensions can change isModifier
      if (isExtension) {
        if (derived.hasIsModifierElement() && !(base.hasIsModifierElement() && Base.compareDeep(derived.getIsModifierElement(), base.getIsModifierElement(), false))) {
          base.setIsModifierElement(derived.getIsModifierElement().copy());
        } else if (trimDifferential) {
          derived.setIsModifierElement(null);
        } else if (derived.hasIsModifierElement()) {
          derived.getIsModifierElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
        }
        if (derived.hasIsModifierReasonElement() && !(base.hasIsModifierReasonElement() && Base.compareDeep(derived.getIsModifierReasonElement(), base.getIsModifierReasonElement(), false))) {
          base.setIsModifierReasonElement(derived.getIsModifierReasonElement().copy());
        } else if (trimDifferential) {
          derived.setIsModifierReasonElement(null);
        } else if (derived.hasIsModifierReasonElement()) {
          derived.getIsModifierReasonElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
        }
        if (base.getIsModifier() && !base.hasIsModifierReason()) {
          // we get here because modifier extensions don't get a modifier reason from the type
          base.setIsModifierReason("Modifier extensions are labelled as such because they modify the meaning or interpretation of the resource or element that contains them");
        }
      }

      boolean hasBinding = derived.hasBinding();
      for (ElementDefinition ed : obligationProfileElements) {
        hasBinding = hasBinding || ed.hasBinding();
      }
      if (hasBinding) {
        updateExtensionsFromDefinition(dest.getBinding(), source.getBinding(), derivedSrc, srcSD);
        ElementDefinitionBindingComponent binding = derived.getBinding();
        for (ElementDefinition ed : obligationProfileElements) {
          for (Extension ext : ed.getBinding().getExtension()) {
            if (ExtensionDefinitions.EXT_BINDING_ADDITIONAL.equals(ext.getUrl())) {
              String p = ext.getExtensionString("purpose");
              if (!Utilities.existsInList(p, "maximum", "required", "extensible")) {
                if (!binding.hasExtension(ext)) {
                  binding.getExtension().add(ext.copy());
                }
              }
            }
          }
          for (ElementDefinitionBindingAdditionalComponent ab : ed.getBinding().getAdditional()) {
            if (!Utilities.existsInList(ab.getPurpose().toCode(), "maximum", "required", "extensible")) {
              if (binding.hasAdditional(ab)) {
                binding.getAdditional().add(ab.copy());
              }
            }
          }
        }
        
        if (!base.hasBinding() || !Base.compareDeep(derived.getBinding(), base.getBinding(), false)) {
          if (base.hasBinding() && base.getBinding().getStrength() == BindingStrength.REQUIRED && derived.getBinding().getStrength() != BindingStrength.REQUIRED)
            addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "illegal attempt to change the binding on "+derived.getPath()+" from "+base.getBinding().getStrength().toCode()+" to "+derived.getBinding().getStrength().toCode(), ValidationMessage.IssueSeverity.ERROR));
//            throw new DefinitionException("StructureDefinition "+pn+" at "+derived.getPath()+": illegal attempt to change a binding from "+base.getBinding().getStrength().toCode()+" to "+derived.getBinding().getStrength().toCode());
          else if (base.hasBinding() && derived.hasBinding() && base.getBinding().getStrength() == BindingStrength.REQUIRED && base.getBinding().hasValueSet() && derived.getBinding().hasValueSet()) {
            ValueSet baseVs = context.findTxResource(ValueSet.class, base.getBinding().getValueSet(), getVersionResolutionRules(base.getBinding().getValueSetElement()), null, srcSD);
            ValueSet contextVs = context.findTxResource(ValueSet.class, derived.getBinding().getValueSet(), getVersionResolutionRules(derived.getBinding().getValueSetElement()), null, derivedSrc);
            if (baseVs == null) {
              addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+base.getPath(), "Binding "+base.getBinding().getValueSet()+" could not be located", ValidationMessage.IssueSeverity.WARNING));
            } else if (contextVs == null) {
              addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Binding "+derived.getBinding().getValueSet()+" could not be located", ValidationMessage.IssueSeverity.WARNING));
            } else {
              ValueSetExpansionOutcome expBase = context.expandVS(baseVs, true, false);
              ValueSetExpansionOutcome expDerived = context.expandVS(contextVs, true, false);
              if (expBase.getValueset() == null)
                addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+base.getPath(), "Binding "+base.getBinding().getValueSet()+" could not be expanded", ValidationMessage.IssueSeverity.WARNING));
              else if (expDerived.getValueset() == null)
                addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Binding "+derived.getBinding().getValueSet()+" could not be expanded", ValidationMessage.IssueSeverity.WARNING));
              else if (ExtensionUtilities.hasExtension(expBase.getValueset().getExpansion(), ExtensionDefinitions.EXT_EXP_TOOCOSTLY)) {
                if (ExtensionUtilities.hasExtension(expDerived.getValueset().getExpansion(), ExtensionDefinitions.EXT_EXP_TOOCOSTLY) || expDerived.getValueset().getExpansion().getContains().size() > 100) {
                  addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Unable to check if "+derived.getBinding().getValueSet()+" is a proper subset of " +base.getBinding().getValueSet()+" - base value set is too large to check", ValidationMessage.IssueSeverity.WARNING));
                } else {
                  boolean ok = true;
                  for (ValueSetExpansionContainsComponent cc : expDerived.getValueset().getExpansion().getContains()) {
                    ValidationResult vr = context.validateCode(new ValidationOptions(), cc.getSystem(), cc.getVersion(), cc.getCode(), null, baseVs);
                    if (!vr.isOk()) {
                      ok = false;
                      break;                      
                    }
                  }
                  if (!ok) {
                    addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Binding "+derived.getBinding().getValueSet()+" is not a subset of binding "+base.getBinding().getValueSet(), ValidationMessage.IssueSeverity.ERROR));
                  }
                }
              } else if (expBase.getValueset().getExpansion().getContains().size() == 1000 || 
                  expDerived.getValueset().getExpansion().getContains().size() == 1000) {
                addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Unable to check if "+derived.getBinding().getValueSet()+" is a proper subset of " +base.getBinding().getValueSet()+" - value set is too large to check", ValidationMessage.IssueSeverity.WARNING));
              } else {
                 String msgs = checkSubset(expBase.getValueset(), expDerived.getValueset());
                 if (msgs != null) {
                  addMessage(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Binding "+derived.getBinding().getValueSet()+" is not a subset of binding "+base.getBinding().getValueSet()+" because "+msgs, ValidationMessage.IssueSeverity.ERROR));
                }
              }
            }
          }
          ElementDefinitionBindingComponent d = derived.getBinding();
          ElementDefinitionBindingComponent nb = base.getBinding().copy();
          if (!COPY_BINDING_EXTENSIONS) {
            nb.getExtension().clear();
          }
          nb.setDescription(null);
          for (Extension dex : d.getExtension()) {
            nb.getExtension().add(markExtensionSource(dex.copy(), false, srcSD));
          }
          if (d.hasStrength()) {
            nb.setStrength(d.getStrength());
          }
          if (d.hasDescription()) {
            nb.setDescription(d.getDescription());
          }
          if (d.hasValueSet()) {
            nb.setValueSetElement(d.getValueSetElement());
          }
          for (ElementDefinitionBindingAdditionalComponent ab : d.getAdditional()) {
            ElementDefinitionBindingAdditionalComponent eab = getMatchingAdditionalBinding(nb, ab);
            if (eab != null) {
              mergeAdditionalBinding(eab, ab);
            } else {
              nb.getAdditional().add(ab);
            }
          }
          base.setBinding(nb); 
        } else if (trimDifferential)
          derived.setBinding(null);
        else
          derived.getBinding().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      } else if (base.hasBinding()) {
         base.getBinding().getExtension().removeIf(ext -> Utilities.existsInList(ext.getUrl(), ProfileUtilities.NON_INHERITED_ED_URLS));
         for (Extension ex : base.getBinding().getExtension()) {
           markExtensionSource(ex, false, srcSD);
         }
      }

      if (derived.hasIsSummaryElement()) {
        if (!Base.compareDeep(derived.getIsSummaryElement(), base.getIsSummaryElement(), false)) {
          if (base.hasIsSummary() && !context.getVersion().equals("1.4.0")) // work around a known issue with some 1.4.0 cosntraints
            throw new Error(context.formatMessage(I18nConstants.ERROR_IN_PROFILE__AT__BASE_ISSUMMARY___DERIVED_ISSUMMARY__, purl, derived.getPath(), base.getIsSummaryElement().asStringValue(), derived.getIsSummaryElement().asStringValue()));
          base.setIsSummaryElement(derived.getIsSummaryElement().copy());
        } else if (trimDifferential)
          derived.setIsSummaryElement(null);
        else
          derived.getIsSummaryElement().setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
      }

      // this would make sense but blows up the process later, so we let it happen anyway, and sort out the business rule elsewhere
      //if (!derived.hasContentReference() && !base.hasContentReference()) {

      if (derived.hasType()) {
        if (!Base.compareDeep(derived.getType(), base.getType(), false)) {
          if (base.hasType()) {
            for (TypeRefComponent ts : derived.getType()) {
              checkTypeDerivation(purl, srcSD, base, derived, ts, path, derivedSrc.getDerivation() == TypeDerivationRule.SPECIALIZATION);
            }
          }
          base.getType().clear();
          for (TypeRefComponent t : derived.getType()) {
            TypeRefComponent tt = t.copy();
            //            tt.setUserData(DERIVATION_EQUALS, true);
            base.getType().add(tt);
            for (Extension ex : tt.getExtension()) {
              markExtensionSource(ex, false, srcSD);
            }
          }
        }
        else if (trimDifferential)
          derived.getType().clear();
        else {
          for (TypeRefComponent t : derived.getType()) {
            t.setUserData(UserDataNames.SNAPSHOT_DERIVATION_EQUALS, true);
            for (Extension ex : t.getExtension()) {
              markExtensionSource(ex, true, derivedSrc);
            }
          }
        }
      }
      
      mappings.merge(derived, base); // note reversal of names to be correct in .merge()

      // todo: constraints are cumulative. there is no replacing
      for (ElementDefinitionConstraintComponent s : base.getConstraint()) { 
        s.setUserData(UserDataNames.SNAPSHOT_IS_DERIVED, true);
        if (!s.hasSource()) {
          s.setSource(srcSD.getUrl());
        } 
      }
      if (derived.hasConstraint()) {
        for (ElementDefinitionConstraintComponent s : derived.getConstraint()) {
          if (!base.hasConstraint(s.getKey())) {
            ElementDefinitionConstraintComponent inv = s.copy();
            base.getConstraint().add(inv);
          }
      	}
      }
      for (IdType id : derived.getCondition()) {
        if (!base.hasCondition(id)) {
          base.getCondition().add(id);
        }
      }
      
      // now, check that we still have a bindable type; if not, delete the binding - see task 8477
      if (dest.hasBinding() && !hasBindableType(dest)) {
        dest.setBinding(null);
      }
        
//      // finally, we copy any extensions from source to dest
      //no, we already did.
//      for (Extension ex : derived.getExtension()) {
//        !
//        StructureDefinition sd  = findProfile(ex.getUrl(), derivedSrc);
//        if (sd == null || sd.getSnapshot() == null || sd.getSnapshot().getElementFirstRep().getMax().equals("1")) {
//          ToolingExtensions.removeExtension(dest, ex.getUrl());
//        }
//        dest.addExtension(ex.copy());
//      }
    }
    if (dest.hasFixed()) {
      checkTypeOk(dest, dest.getFixed().fhirType(), srcSD, "fixed");
    }
    if (dest.hasPattern()) {
      checkTypeOk(dest, dest.getPattern().fhirType(), srcSD, "pattern");
    }
    //updateURLs(url, webUrl, dest);
  }

  private IWorkerContext.VersionResolutionRules getVersionResolutionRules(Element element) {
    return ExtensionUtilities.getVersionResolutionRules(element);
  }

  private MarkdownType mergeMarkdown(MarkdownType dest, MarkdownType source) {
    MarkdownType mergedMarkdown = dest.copy();
    if (!mergedMarkdown.hasValue() && source.hasValue()) {
      mergedMarkdown.setValue(source.getValue());
    } else if (mergedMarkdown.hasValue() && source.hasValue() && mergedMarkdown.getValue().startsWith("...")) {
      mergedMarkdown.setValue(Utilities.appendDerivedTextToBase(source.getValue(), mergedMarkdown.getValue()));
    }
    for (Extension sourceExtension : source.getExtension()) {
      Extension matchingExtension = findMatchingExtension(mergedMarkdown, sourceExtension);
      if (matchingExtension == null) {
        mergedMarkdown.addExtension(sourceExtension.copy());
      } else {
        matchingExtension.setValue(sourceExtension.getValue());
      }
    }
    return mergedMarkdown;
  }

  private StringType mergeStrings(StringType dest, StringType source) {
    StringType res = dest.copy();
    if (!res.hasValue() && source.hasValue()) {
      res.setValue(source.getValue());
    } else if (res.hasValue() && source.hasValue() && res.getValue().startsWith("...")) {
      res.setValue(Utilities.appendDerivedTextToBase(res.getValue(), source.getValue()));
    }
    for (Extension sourceExtension : source.getExtension()) {
      Extension matchingExtension = findMatchingExtension(res, sourceExtension);
      if (matchingExtension == null) {
        res.addExtension(sourceExtension.copy());
      } else {
        matchingExtension.setValue(sourceExtension.getValue());
      }
    }
    return res;
  }

  private Extension findMatchingExtension(Element res, Extension extensionToMatch) {
    for (Extension elementExtension : res.getExtensionsByUrl(extensionToMatch.getUrl())) {
      if (ExtensionDefinitions.EXT_TRANSLATION.equals(elementExtension.getUrl())) {
        String slang = extensionToMatch.getExtensionString("lang");
        String dlang = elementExtension.getExtensionString("lang");
        if (Utilities.stringsEqual(slang, dlang)) {
          return elementExtension;
        }
      } else {
        return elementExtension;
      }

    }
    return null;
  }

  private static Extension markExtensionSource(Extension extension, boolean overrideSource, StructureDefinition srcSD) {
    if (overrideSource || !extension.hasUserData(UserDataNames.SNAPSHOT_EXTENSION_SOURCE)) {
      extension.setUserData(UserDataNames.SNAPSHOT_EXTENSION_SOURCE, srcSD);
    }
    if (Utilities.existsInList(extension.getUrl(), ExtensionDefinitions.EXT_OBLIGATION_CORE, ExtensionDefinitions.EXT_OBLIGATION_TOOLS)) {
      Extension sub = extension.getExtensionByUrl(ExtensionDefinitions.EXT_OBLIGATION_SOURCE, ExtensionDefinitions.EXT_OBLIGATION_SOURCE_SHORT);
      if (sub == null || overrideSource) {
        ExtensionUtilities.setCanonicalExtension(extension, ExtensionDefinitions.EXT_OBLIGATION_SOURCE, srcSD.getVersionedUrl());
      }
    }
    return extension;
  }

  private void updateExtensionsFromDefinition(Element dest, Element source, StructureDefinition destSD, StructureDefinition srcSD) {
    dest.getExtension().removeIf(ext -> Utilities.existsInList(ext.getUrl(), NON_INHERITED_ED_URLS) || (Utilities.existsInList(ext.getUrl(), DEFAULT_INHERITED_ED_URLS) && source.hasExtension(ext.getUrl())));

    for (Extension ext : source.getExtension()) {
      if (!dest.hasExtension(ext.getUrl())) {
        dest.getExtension().add(markExtensionSource(ext.copy(), false, srcSD));
      } else if (Utilities.existsInList(ext.getUrl(), NON_OVERRIDING_ED_URLS)) {
        // do nothing
        for (Extension ex2 : dest.getExtensionsByUrl(ext.getUrl())) {
          markExtensionSource(ex2, true, destSD);
        }
      } else if (Utilities.existsInList(ext.getUrl(), OVERRIDING_ED_URLS)) {
        dest.getExtensionByUrl(ext.getUrl()).setValue(ext.getValue());
        markExtensionSource(dest.getExtensionByUrl(ext.getUrl()), false, srcSD);
      } else {
        dest.getExtension().add(markExtensionSource(ext.copy(), false, srcSD));  
      }
    }
  }

  private void mergeAdditionalBinding(ElementDefinitionBindingAdditionalComponent dest, ElementDefinitionBindingAdditionalComponent source) {
    for (UsageContext t : source.getUsage()) {
      if (!hasUsage(dest, t)) {
        dest.addUsage(t);
      }
    }
    if (source.getAny()) {
      source.setAny(true);
    }
    if (source.hasShortDoco()) {
      dest.setShortDoco(source.getShortDoco());
    }
    if (source.hasDocumentation()) {
      dest.setDocumentation(source.getDocumentation());
    }
    
  }

  private boolean hasUsage(ElementDefinitionBindingAdditionalComponent dest, UsageContext tgt) {
    for (UsageContext t : dest.getUsage()) {
      if (t.getCode() != null && t.getCode().matches(tgt.getCode()) && t.getValue() != null && t.getValue().equals(tgt.getValue())) {
        return true;
      }
    }
    return false;
  }

  private ElementDefinitionBindingAdditionalComponent getMatchingAdditionalBinding(ElementDefinitionBindingComponent nb,ElementDefinitionBindingAdditionalComponent ab) {
    for (ElementDefinitionBindingAdditionalComponent t : nb.getAdditional()) {
      if (t.getValueSet() != null && t.getValueSet().equals(ab.getValueSet()) && t.getPurpose() == ab.getPurpose() && !ab.hasUsage()) {
        return t;
      }
    }
    return null;
  }

  private void mergeExtensions(Element tgt, Element src) {
     tgt.getExtension().addAll(src.getExtension());
  }

  private void checkTypeDerivation(String purl, StructureDefinition srcSD, ElementDefinition base, ElementDefinition derived, TypeRefComponent ts, String path, boolean specialising) {
    boolean ok = false;
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    String t = ts.getWorkingCode();
    String tDesc = ts.toString();
    for (TypeRefComponent td : base.getType()) {;
      boolean matchType = false;
      String tt = td.getWorkingCode();
      b.append(td.toString());
      if (td.hasCode() && (tt.equals(t))) {
        matchType = true;
      }
      if (!matchType) {
        StructureDefinition sdt = context.fetchTypeDefinition(tt);
        if (sdt != null && (sdt.getAbstract() || sdt.getKind() == StructureDefinitionKind.LOGICAL)) {
          StructureDefinition sdb = context.fetchTypeDefinition(t);
          while (sdb != null && !matchType) {
            matchType = sdb.getType().equals(sdt.getType());
            sdb = findProfile(sdb.getBaseDefinitionElement(), sdb);
          }
        }
      }
     // work around for old badly generated SDs
//      if (DONT_DO_THIS && Utilities.existsInList(tt, "Extension", "uri", "string", "Element")) {
//        matchType = true;
//      }
//      if (DONT_DO_THIS && Utilities.existsInList(tt, "Resource","DomainResource") && pkp.isResource(t)) {
//        matchType = true;
//      }
      if (matchType) {
        ts.copyNewExtensions(td, "http://hl7.org/fhir/StructureDefinition/elementdefinition-type-must-support");
        ts.copyExtensions(td, "http://hl7.org/fhir/StructureDefinition/elementdefinition-pattern", "http://hl7.org/fhir/StructureDefinition/obligation", "http://hl7.org/fhir/tools/StructureDefinition/obligation");
        if (ts.hasTargetProfile()) {
          // check that any derived target has a reference chain back to one of the base target profiles
          for (UriType u : ts.getTargetProfile()) {
            String url = u.getValue();
            boolean tgtOk = !td.hasTargetProfile() || sdConformsToTargets(path, derived.getPath(), url, ExtensionUtilities.getVersionResolutionRules(u), td);
            if (tgtOk) {
              ok = true;
            } else if (specialising) {
              ok = true;
            } else {
              addMessage(new ValidationMessage(Source.InstanceValidator, IssueType.BUSINESSRULE, derived.getPath(), context.formatMessage(I18nConstants.ERROR_AT__THE_TARGET_PROFILE__IS_NOT__VALID_CONSTRAINT_ON_THE_BASE_, purl, derived.getPath(), url, td.getTargetProfile()), IssueSeverity.ERROR));
            }
          }
        } else {
          ok = true;
        }
      }
    }
    if (!ok && !isSuppressIgnorableExceptions()) {
      throw new DefinitionException(context.formatMessage(I18nConstants.STRUCTUREDEFINITION__AT__ILLEGAL_CONSTRAINED_TYPE__FROM__IN_, purl, derived.getPath(), tDesc, b.toString(), srcSD.getUrl()));
    }
  }


  private boolean sdConformsToTargets(String path, String dPath, String url, IWorkerContext.VersionResolutionRules rules, TypeRefComponent td) {
    if (td.hasTargetProfile(url)) {
      return true;
    }
    if (url != null && url.contains("|") && td.hasTargetProfile(url.substring(0, url.indexOf("|")))) {
      return true;
    }
    StructureDefinition sd = context.fetchResourceRaw(StructureDefinition.class, url, rules);
    if (sd == null) {
      addMessage(new ValidationMessage(Source.InstanceValidator, IssueType.BUSINESSRULE, path, "Cannot check whether the target profile " + url + " on "+dPath+" is valid constraint on the base because it is not known", IssueSeverity.WARNING));
      return true;
    } else {
      if (sd.hasBaseDefinition() && sdConformsToTargets(path, dPath, sd.getBaseDefinition(), ExtensionUtilities.getVersionResolutionRules(sd.getBaseDefinitionElement()), td)) {
        return true;
      }
      for (Extension ext : sd.getExtensionsByUrl(ExtensionDefinitions.EXT_SD_IMPOSE_PROFILE)) {
        if (sdConformsToTargets(path, dPath, ext.getValueCanonicalType().asStringValue(), ExtensionUtilities.getVersionResolutionRules(ext.getValueCanonicalType()), td)) {
          return true;
        }
      }
    }
    return false;
  }

  private void checkTypeOk(ElementDefinition dest, String ft, StructureDefinition sd, String fieldName) {
    boolean ok = false;
    Set<String> types = new HashSet<>();
    if (dest.getPath().contains(".")) {
      for (TypeRefComponent t : dest.getType()) {
        if (t.hasCode()) {
          types.add(t.getWorkingCode());
        }
        ok = ok || ft.equals(t.getWorkingCode());
      }
    } else {
      types.add(sd.getType());
      ok = ok || ft.equals(sd.getType());

    }
    if (!ok) {
      addMessage(new ValidationMessage(Source.InstanceValidator, IssueType.CONFLICT, dest.getId(), "The "+fieldName+" value has type '"+ft+"' which is not valid (valid "+Utilities.pluralize("type", dest.getType().size())+": "+types.toString()+")", IssueSeverity.ERROR));
    }
  }

  private boolean hasBindableType(ElementDefinition ed) {
    for (TypeRefComponent tr : ed.getType()) {
      if (Utilities.existsInList(tr.getWorkingCode(), "Coding", "CodeableConcept", "Quantity", "uri", "string", "code", "CodeableReference")) {
        return true;
      }
      StructureDefinition sd = context.fetchTypeDefinition(tr.getCode());
      if (sd != null && sd.hasExtension(ExtensionDefinitions.EXT_BINDING_STYLE)) {
        return true;
      }
      if (sd != null && sd.hasExtension(ExtensionDefinitions.EXT_TYPE_CHARACTERISTICS) &&
          "can-bind".equals(ExtensionUtilities.readStringExtension(sd, ExtensionDefinitions.EXT_TYPE_CHARACTERISTICS))) {
        return true;
      }
    }
    return false;
  }


  private boolean isLargerMax(String derived, String base) {
    if ("*".equals(base)) {
      return false;
    }
    if ("*".equals(derived)) {
      return true;
    }
    return Integer.parseInt(derived) > Integer.parseInt(base);
  }


  private String checkSubset(ValueSet expBase, ValueSet expDerived) {
    Set<String> codes = new HashSet<>();
    checkCodesInExpansion(codes, expDerived.getExpansion().getContains(), expBase.getExpansion());
    if (codes.isEmpty()) {
      return null;
    } else {
      return "The codes '"+CommaSeparatedStringBuilder.join(",", codes)+"' are not in the base valueset";
    }
  }


  private void checkCodesInExpansion(Set<String> codes, List<ValueSetExpansionContainsComponent> contains, ValueSetExpansionComponent expansion) {
    for (ValueSetExpansionContainsComponent cc : contains) {
      if (!inExpansion(cc, expansion.getContains())) {
        codes.add(cc.getCode());
      }
      checkCodesInExpansion(codes, cc.getContains(), expansion);
    }
  }


  private boolean inExpansion(ValueSetExpansionContainsComponent cc, List<ValueSetExpansionContainsComponent> contains) {
    for (ValueSetExpansionContainsComponent cc1 : contains) {
      if (cc.getSystem().equals(cc1.getSystem()) && cc.getCode().equals(cc1.getCode())) {
        return true;
      }
      if (inExpansion(cc,  cc1.getContains())) {
        return true;
      }
    }
    return false;
  }
  
  public void closeDifferential(StructureDefinition base, StructureDefinition derived) throws FHIRException {
    for (ElementDefinition edb : base.getSnapshot().getElement()) {
      if (isImmediateChild(edb) && !edb.getPath().endsWith(".id")) {
        ElementDefinition edm = getMatchInDerived(edb, derived.getDifferential().getElement());
        if (edm == null) {
          ElementDefinition edd = derived.getDifferential().addElement();
          edd.setPath(edb.getPath());
          edd.setMax("0");
        } else if (edb.hasSlicing()) {
          closeChildren(base, edb, derived, edm);
        }
      }
    }
    sortDifferential(base, derived, derived.getName(), new ArrayList<String>(), false);
  }

  private void closeChildren(StructureDefinition base, ElementDefinition edb, StructureDefinition derived, ElementDefinition edm) {
//    String path = edb.getPath()+".";
    int baseStart = base.getSnapshot().getElement().indexOf(edb);
    int baseEnd = findEnd(base.getSnapshot().getElement(), edb, baseStart+1);
    int diffStart = derived.getDifferential().getElement().indexOf(edm);
    int diffEnd = findEnd(derived.getDifferential().getElement(), edm, diffStart+1);
    
    for (int cBase = baseStart; cBase < baseEnd; cBase++) {
      ElementDefinition edBase = base.getSnapshot().getElement().get(cBase);
      if (isImmediateChild(edBase, edb)) {
        ElementDefinition edMatch = getMatchInDerived(edBase, derived.getDifferential().getElement(), diffStart, diffEnd);
        if (edMatch == null) {
          ElementDefinition edd = derived.getDifferential().addElement();
          edd.setPath(edBase.getPath());
          edd.setMax("0");
        } else {
          closeChildren(base, edBase, derived, edMatch);
        }        
      }
    }
  }

  private int findEnd(List<ElementDefinition> list, ElementDefinition ed, int cursor) {
    String path = ed.getPath()+".";
    while (cursor < list.size() && list.get(cursor).getPath().startsWith(path)) {
      cursor++;
    }
    return cursor;
  }


  private ElementDefinition getMatchInDerived(ElementDefinition ed, List<ElementDefinition> list) {
    for (ElementDefinition t : list) {
      if (t.getPath().equals(ed.getPath())) {
        return t;
      }
    }
    return null;
  }

  private ElementDefinition getMatchInDerived(ElementDefinition ed, List<ElementDefinition> list, int start, int end) {
    for (int i = start; i < end; i++) {
      ElementDefinition t = list.get(i);
      if (t.getPath().equals(ed.getPath())) {
        return t;
      }
    }
    return null;
  }


  private boolean isImmediateChild(ElementDefinition ed) {
    String p = ed.getPath();
    if (!p.contains(".")) {
      return false;
    }
    p = p.substring(p.indexOf(".")+1);
    return !p.contains(".");
  }

  private boolean isImmediateChild(ElementDefinition candidate, ElementDefinition base) {
    String p = candidate.getPath();
    if (!p.contains("."))
      return false;
    if (!p.startsWith(base.getPath()+"."))
      return false;
    p = p.substring(base.getPath().length()+1);
    return !p.contains(".");
  }



  private ElementDefinition getUrlFor(StructureDefinition ed, ElementDefinition c) {
    int i = ed.getSnapshot().getElement().indexOf(c) + 1;
    while (i < ed.getSnapshot().getElement().size() && ed.getSnapshot().getElement().get(i).getPath().startsWith(c.getPath()+".")) {
      if (ed.getSnapshot().getElement().get(i).getPath().equals(c.getPath()+".url"))
        return ed.getSnapshot().getElement().get(i);
      i++;
    }
    return null;
  }



  protected ElementDefinitionResolution getElementById(StructureDefinition source, List<ElementDefinition> elements, UriType contentRefElement) {
    String contentReference = contentRefElement.getValue();
    if (!contentReference.startsWith("#") && contentReference.contains("#")) {
      String url = contentReference.substring(0, contentReference.indexOf("#"));
      contentReference = contentReference.substring(contentReference.indexOf("#"));
      if (!url.equals(source.getUrl())){
        source = findProfile(contentRefElement, source);
        if (source == null) {
          return null;
        }
        elements = source.getSnapshot().getElement();
      }      
    }
    for (ElementDefinition ed : elements)
      if (ed.hasId() && ("#"+ed.getId()).equals(contentReference))
        return new ElementDefinitionResolution(source, ed);
    return null;
  }


  public static String describeExtensionContext(StructureDefinition ext) {
    StringBuilder b = new StringBuilder();
    b.append("Use on ");
    for (int i = 0; i < ext.getContext().size(); i++) {
      StructureDefinitionContextComponent ec = ext.getContext().get(i);
      if (i > 0) 
        b.append(i < ext.getContext().size() - 1 ? ", " : " or ");
      b.append(ec.getType().getDisplay());
      b.append(" ");
      b.append(ec.getExpression());
    }
    if (ext.hasContextInvariant()) {
      b.append(", with <a href=\"structuredefinition-definitions.html#StructureDefinition.contextInvariant\">Context Invariant</a> = ");
      boolean first = true;
      for (StringType s : ext.getContextInvariant()) {
        if (first)
          first = false;
        else
          b.append(", ");
        b.append("<code>"+s.getValue()+"</code>");
      }
    }
    return b.toString(); 
  }



 
//  public XhtmlNode generateTable(String defFile, StructureDefinition profile, boolean diff, String imageFolder, boolean inlineGraphics, String profileBaseFileName, boolean snapshot, String corePath, String imagePath,
//                                 boolean logicalModel, boolean allInvariants, Set<String> outputTracker, boolean mustSupport, RenderingContext rc) throws IOException, FHIRException {
//    return generateTable(defFile, profile, diff, imageFolder, inlineGraphics, profileBaseFileName, snapshot, corePath, imagePath, logicalModel, allInvariants, outputTracker, mustSupport, rc, "");
//  }

 


 
  protected String tail(String path) {
    if (path == null) {
      return "";
    } else if (path.contains("."))
      return path.substring(path.lastIndexOf('.')+1);
    else
      return path;
  }

  private boolean isDataType(String value) {
    StructureDefinition sd = context.fetchTypeDefinition(value);
    if (sd == null) // might be running before all SDs are available
      return Utilities.existsInList(value, "Address", "Age", "Annotation", "Attachment", "CodeableConcept", "Coding", "ContactPoint", "Count", "Distance", "Duration", "HumanName", "Identifier", "Money", "Period", "Quantity", "Range", "Ratio", "Reference", "SampledData", "Signature", "Timing", 
            "ContactDetail", "Contributor", "DataRequirement", "Expression", "ParameterDefinition", "RelatedArtifact", "TriggerDefinition", "UsageContext");
    else 
      return sd.getKind() == StructureDefinitionKind.COMPLEXTYPE && sd.getDerivation() == TypeDerivationRule.SPECIALIZATION;
  }

  private boolean isConstrainedDataType(String value) {
    StructureDefinition sd = context.fetchTypeDefinition(value);
    if (sd == null) // might be running before all SDs are available
      return Utilities.existsInList(value, "SimpleQuantity", "MoneyQuantity");
    else 
      return sd.getKind() == StructureDefinitionKind.COMPLEXTYPE && sd.getDerivation() == TypeDerivationRule.CONSTRAINT;
  }

  private String baseType(String value) {
    StructureDefinition sd = context.fetchTypeDefinition(value);
    if (sd != null) // might be running before all SDs are available
      return sd.getTypeName();
    if (Utilities.existsInList(value, "SimpleQuantity", "MoneyQuantity"))
      return "Quantity";
    throw new Error(context.formatMessage(I18nConstants.INTERNAL_ERROR___TYPE_NOT_KNOWN_, value));
  }


  protected boolean isPrimitive(String value) {
    StructureDefinition sd = context.fetchTypeDefinition(value);
    if (sd == null) // might be running before all SDs are available
      return Utilities.existsInList(value, "base64Binary", "boolean", "canonical", "code", "date", "dateTime", "decimal", "id", "instant", "integer", "integer64", "markdown", "oid", "positiveInt", "string", "time", "unsignedInt", "uri", "url", "uuid");
    else 
      return sd.getKind() == StructureDefinitionKind.PRIMITIVETYPE;
  }

//  private static String listStructures(StructureDefinition p) {
//    StringBuilder b = new StringBuilder();
//    boolean first = true;
//    for (ProfileStructureComponent s : p.getStructure()) {
//      if (first)
//        first = false;
//      else
//        b.append(", ");
//      if (pkp != null && pkp.hasLinkFor(s.getType()))
//        b.append("<a href=\""+pkp.getLinkFor(s.getType())+"\">"+s.getType()+"</a>");
//      else
//        b.append(s.getType());
//    }
//    return b.toString();
//  }


  public StructureDefinition getProfile(StructureDefinition source, UriType ref) {
    if (ref == null) {
      return null;
    }
    String url = ref.primitiveValue();
  	StructureDefinition profile = null;
  	String code = null;
  	if (url.startsWith("#")) {
  		profile = source;
  		code = url.substring(1);
  	} else if (context != null) {
  		String[] parts = url.split("\\#");
  		profile = findProfile(ref, source);
      code = parts.length == 1 ? null : parts[1];
  	}  	  
  	if (profile == null)
  		return null;
  	if (code == null)
  		return profile;
  	for (Resource r : profile.getContained()) {
  		if (r instanceof StructureDefinition && r.getId().equals(code))
  			return (StructureDefinition) r;
  	}
  	return null;
  }



  private static class ElementDefinitionHolder {
    private String name;
    private ElementDefinition self;
    private int baseIndex = 0;
    private List<ElementDefinitionHolder> children;
    private boolean placeHolder = false;

    public ElementDefinitionHolder(ElementDefinition self, boolean isPlaceholder) {
      super();
      this.self = self;
      this.name = self.getPath();
      this.placeHolder = isPlaceholder;
      children = new ArrayList<ElementDefinitionHolder>();      
    }

    public ElementDefinitionHolder(ElementDefinition self) {
      this(self, false);
    }

    public ElementDefinition getSelf() {
      return self;
    }

    public List<ElementDefinitionHolder> getChildren() {
      return children;
    }

    public int getBaseIndex() {
      return baseIndex;
    }

    public void setBaseIndex(int baseIndex) {
      this.baseIndex = baseIndex;
    }

    public boolean isPlaceHolder() {
      return this.placeHolder;
    }

    @Override
    public String toString() {
      if (self.hasSliceName())
        return self.getPath()+"("+self.getSliceName()+")";
      else
        return self.getPath();
    }
  }

  private static class ElementDefinitionComparer implements Comparator<ElementDefinitionHolder> {

    private boolean inExtension;
    private StructureDefinition src;
    private List<ElementDefinition> snapshot;
    private int prefixLength;
    private String base;
    private String name;
    private String baseName;
    private Set<String> errors = new HashSet<String>();

    public ElementDefinitionComparer(boolean inExtension, StructureDefinition src, List<ElementDefinition> snapshot, String base, int prefixLength, String name, String baseName) {
      this.inExtension = inExtension;
      this.src = src;
      this.snapshot = snapshot;
      this.prefixLength = prefixLength;
      this.base = base;
      if (Utilities.isAbsoluteUrl(base)) {
        this.base = urlTail(base);
      }
      this.name = name;
      this.baseName = baseName;
    }

    @Override
    public int compare(ElementDefinitionHolder o1, ElementDefinitionHolder o2) {
      if (o1.getBaseIndex() == 0) {
        o1.setBaseIndex(find(o1.getSelf().getPath(), true));
      }
      if (o2.getBaseIndex() == 0) {
        o2.setBaseIndex(find(o2.getSelf().getPath(), true));
      }
      return o1.getBaseIndex() - o2.getBaseIndex();
    }

    private int find(String path, boolean mandatory) {
      String op = path;
      int lc = 0;
      String actual = base+path.substring(prefixLength);
      for (int i = 0; i < snapshot.size(); i++) {
        String p = snapshot.get(i).getPath();
        if (p.equals(actual)) {
          return i;
        }
        if (p.endsWith("[x]") && actual.startsWith(p.substring(0, p.length()-3)) && !(actual.endsWith("[x]")) && !actual.substring(p.length()-3).contains(".")) {
          return i;
        }
        if (actual.endsWith("[x]") && p.startsWith(actual.substring(0, actual.length()-3)) && !p.substring(actual.length()-3).contains(".")) {
          return i;
        }
        if (path.startsWith(p+".") && snapshot.get(i).hasContentReference()) {
          String ref = snapshot.get(i).getContentReference();
          if (ref.substring(1, 2).toUpperCase().equals(ref.substring(1,2))) {
            actual = base+(ref.substring(1)+"."+path.substring(p.length()+1)).substring(prefixLength);
            path = actual;
          } else if (ref.startsWith("http:")) {
            actual = base+(ref.substring(ref.indexOf("#")+1)+"."+path.substring(p.length()+1)).substring(prefixLength);
            path = actual;            
          } else {
            // Older versions of FHIR (e.g. 2016May) had reference of the style #parameter instead of #Parameters.parameter, so we have to handle that
            actual = base+(path.substring(0,  path.indexOf(".")+1) + ref.substring(1)+"."+path.substring(p.length()+1)).substring(prefixLength);
            path = actual;
          }
            
          i = 0;
          lc++;
          if (lc > MAX_RECURSION_LIMIT)
            throw new Error("Internal recursion detection: find() loop path recursion > "+MAX_RECURSION_LIMIT+" - check paths are valid (for path "+path+"/"+op+")");
        }
      }
      if (mandatory) {
        if (prefixLength == 0)
          errors.add("Differential contains path "+path+" which is not found in the base "+baseName);
        else
          errors.add("Differential contains path "+path+" which is actually "+actual+", which is not found in the in base "+ baseName);
      }
      return 0;
    }

    public void checkForErrors(List<String> errorList) {
      if (errors.size() > 0) {
//        CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
//        for (String s : errors)
//          b.append("StructureDefinition "+name+": "+s);
//        throw new DefinitionException(b.toString());
        for (String s : errors)
          if (s.startsWith("!"))
            errorList.add("!StructureDefinition "+name+": "+s.substring(1));
          else
            errorList.add("StructureDefinition "+name+": "+s);
      }
    }
  }


  public void sortDifferential(StructureDefinition base, StructureDefinition diff, String name, List<String> errors, boolean errorIfChanges) throws FHIRException  {
    int index = 0;
    for (ElementDefinition ed : diff.getDifferential().getElement()) {
      ed.setUserData(UserDataNames.SNAPSHOT_SORT_ed_index, Integer.toString(index));
      index++;
    }
    List<ElementDefinition> original = new ArrayList<>();
    original.addAll(diff.getDifferential().getElement());
    final List<ElementDefinition> diffList = diff.getDifferential().getElement();
    int lastCount = diffList.size();
    // first, we move the differential elements into a tree
    if (diffList.isEmpty())
      return;
    
    ElementDefinitionHolder edh = null;
    int i = 0;
    if (diffList.get(0).getPath().contains(".")) {
      String newPath = diffList.get(0).getPath().split("\\.")[0];
      ElementDefinition e = new ElementDefinition(newPath);
      edh = new ElementDefinitionHolder(e, true);
    } else {
      edh = new ElementDefinitionHolder(diffList.get(0));
      i = 1;
    }

    boolean hasSlicing = false;
    List<String> paths = new ArrayList<String>(); // in a differential, slicing may not be stated explicitly
    for(ElementDefinition elt : diffList) {
      if (elt.hasSlicing() || paths.contains(elt.getPath())) {
        hasSlicing = true;
        break;
      }
      paths.add(elt.getPath());
    }

    processElementsIntoTree(edh, i, diff.getDifferential().getElement());

    // now, we sort the siblings throughout the tree
    ElementDefinitionComparer cmp = new ElementDefinitionComparer(true, base, base.getSnapshot().getElement(), "", 0, name, base.getType());
    sortElements(edh, cmp, errors);

    // now, we serialise them back to a list
    List<ElementDefinition> newDiff = new ArrayList<>();
    writeElements(edh, newDiff);
    if (errorIfChanges) {
      compareDiffs(original, newDiff, errors);
    }
    diffList.clear();
    diffList.addAll(newDiff);
    
    if (lastCount != diffList.size())
      errors.add("Sort failed: counts differ; at least one of the paths in the differential is illegal");
  }

  private void compareDiffs(List<ElementDefinition> diffList, List<ElementDefinition> newDiff, List<String> errors) {
    if (diffList.size() != newDiff.size()) {
      errors.add("The diff list size changed when sorting - was "+diffList.size()+" is now "+newDiff.size()+
          " ["+CommaSeparatedStringBuilder.buildObjects(diffList)+"]/["+CommaSeparatedStringBuilder.buildObjects(newDiff)+"]");
    } else {
      for (int i = 0; i < Integer.min(diffList.size(), newDiff.size()); i++) {
        ElementDefinition e = diffList.get(i);
        ElementDefinition n = newDiff.get(i);
        if (!n.getPath().equals(e.getPath())) {
          errors.add("The element "+(e.hasId() ? e.getId() : e.getPath())+" @diff["+e.getUserString(UserDataNames.SNAPSHOT_SORT_ed_index)+"] is out of order (and maybe others after it)");
          return;
        }   
      }
    }
  }


  private int processElementsIntoTree(ElementDefinitionHolder edh, int i, List<ElementDefinition> list) {
    String path = edh.getSelf().getPath();
    final String prefix = path + ".";
    while (i < list.size() && list.get(i).getPath().startsWith(prefix)) {
      if (list.get(i).getPath().substring(prefix.length()+1).contains(".")) {
        String newPath = prefix + list.get(i).getPath().substring(prefix.length()).split("\\.")[0];
        ElementDefinition e = new ElementDefinition(newPath);
        ElementDefinitionHolder child = new ElementDefinitionHolder(e, true);
        edh.getChildren().add(child);
        i = processElementsIntoTree(child, i, list);
        
      } else {
        ElementDefinitionHolder child = new ElementDefinitionHolder(list.get(i));
        edh.getChildren().add(child);
        i = processElementsIntoTree(child, i+1, list);
      }
    }
    return i;
  }

  private void sortElements(ElementDefinitionHolder edh, ElementDefinitionComparer cmp, List<String> errors) throws FHIRException {
    if (edh.getChildren().size() == 1)
      // special case - sort needsto allocate base numbers, but there'll be no sort if there's only 1 child. So in that case, we just go ahead and allocated base number directly
      edh.getChildren().get(0).baseIndex = cmp.find(edh.getChildren().get(0).getSelf().getPath(), false);
    else
      Collections.sort(edh.getChildren(), cmp);
    if (debug) {
      cmp.checkForErrors(errors);
    }

    for (ElementDefinitionHolder child : edh.getChildren()) {
      if (child.getChildren().size() > 0) {
        ElementDefinitionComparer ccmp = getComparer(cmp, child);
        if (ccmp != null) {
          sortElements(child, ccmp, errors);
        }
      }
    }
  }

  public ElementDefinitionComparer getComparer(ElementDefinitionComparer cmp, ElementDefinitionHolder child) throws FHIRException, Error {
    // what we have to check for here is running off the base profile into a data type profile
    ElementDefinition ed = cmp.snapshot.get(child.getBaseIndex());
    ElementDefinitionComparer ccmp;
    if (ed.getType().isEmpty() || isAbstract(ed.getType().get(0).getWorkingCode()) || ed.getType().get(0).getWorkingCode().equals(ed.getPath())) {
      if (ed.hasType() && "Resource".equals(ed.getType().get(0).getWorkingCode()) && (child.getSelf().hasType() && child.getSelf().getType().get(0).hasProfile())) {
        if (child.getSelf().getType().get(0).getProfile().size() > 1) {
          throw new FHIRException(context.formatMessage(I18nConstants.UNHANDLED_SITUATION_RESOURCE_IS_PROFILED_TO_MORE_THAN_ONE_OPTION__CANNOT_SORT_PROFILE));
        }
        StructureDefinition profile = findProfile(child.getSelf().getType().get(0).getProfile().get(0), cmp.src);
        while (profile != null && profile.getDerivation() == TypeDerivationRule.CONSTRAINT) {
          profile = findProfile(profile.getBaseDefinitionElement(), profile);
        }
        if (profile==null) {
          ccmp = null; // this might happen before everything is loaded. And we don't so much care about sot order in this case
        } else {
          ccmp = new ElementDefinitionComparer(true, profile, profile.getSnapshot().getElement(), profile.getType(), child.getSelf().getPath().length(), cmp.name, profile.present());
        }
      } else {
        ccmp = new ElementDefinitionComparer(true, cmp.src, cmp.snapshot, cmp.base, cmp.prefixLength, cmp.name, cmp.name);
      }
    } else if (ed.getType().get(0).getWorkingCode().equals("Extension") && child.getSelf().getType().size() == 1 && child.getSelf().getType().get(0).hasProfile()) {
      StructureDefinition profile = findProfile(child.getSelf().getType().get(0).getProfile().get(0), cmp.src);
      if (profile==null)
        ccmp = null; // this might happen before everything is loaded. And we don't so much care about sot order in this case
      else
        ccmp = new ElementDefinitionComparer(true, profile, profile.getSnapshot().getElement(), resolveType(ed.getType().get(0).getWorkingCode(), cmp.src), child.getSelf().getPath().length(), cmp.name, profile.present());
    } else if (ed.getType().size() == 1 && !ed.getType().get(0).getWorkingCode().equals("*")) {
      StructureDefinition profile = findProfile(new UriType(sdNs(ed.getType().get(0).getWorkingCode())), cmp.src);
      if (profile==null)
        throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_PROFILE__IN_ELEMENT_, sdNs(ed.getType().get(0).getWorkingCode()), ed.getPath()));
      ccmp = new ElementDefinitionComparer(false, profile, profile.getSnapshot().getElement(), resolveType(ed.getType().get(0).getWorkingCode(), cmp.src), child.getSelf().getPath().length(), cmp.name, profile.present());
    } else if (child.getSelf().getType().size() == 1) {
      StructureDefinition profile = findProfile(new UriType(sdNs(child.getSelf().getType().get(0).getWorkingCode())), cmp.src);
      if (profile==null)
        throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_PROFILE__IN_ELEMENT_, sdNs(ed.getType().get(0).getWorkingCode()), ed.getPath()));
      ccmp = new ElementDefinitionComparer(false, profile, profile.getSnapshot().getElement(), child.getSelf().getType().get(0).getWorkingCode(), child.getSelf().getPath().length(), cmp.name, profile.present());
    } else if (ed.getPath().endsWith("[x]") && !child.getSelf().getPath().endsWith("[x]")) {
      String edLastNode = ed.getPath().replaceAll("(.*\\.)*(.*)", "$2");
      String childLastNode = child.getSelf().getPath().replaceAll("(.*\\.)*(.*)", "$2");
      String p = childLastNode.substring(edLastNode.length()-3);
      if (isPrimitive(Utilities.uncapitalize(p)))
        p = Utilities.uncapitalize(p);
      StructureDefinition sd = findProfile(new UriType(sdNs(p)), cmp.src);
      if (sd == null)
        throw new Error(context.formatMessage(I18nConstants.UNABLE_TO_FIND_PROFILE__AT_, p, ed.getId()));
      ccmp = new ElementDefinitionComparer(false, sd, sd.getSnapshot().getElement(), p, child.getSelf().getPath().length(), cmp.name, sd.present());
    } else if (child.getSelf().hasType() && child.getSelf().getType().get(0).getWorkingCode().equals("Reference")) {
      for (TypeRefComponent t: child.getSelf().getType()) {
        if (!t.getWorkingCode().equals("Reference")) {
          throw new Error(context.formatMessage(I18nConstants.CANT_HAVE_CHILDREN_ON_AN_ELEMENT_WITH_A_POLYMORPHIC_TYPE__YOU_MUST_SLICE_AND_CONSTRAIN_THE_TYPES_FIRST_SORTELEMENTS_, ed.getPath(), typeCode(ed.getType())));
        }
      }
      StructureDefinition profile = findProfile(new UriType(sdNs(ed.getType().get(0).getWorkingCode())), cmp.src);
      ccmp = new ElementDefinitionComparer(false, profile, profile.getSnapshot().getElement(), ed.getType().get(0).getWorkingCode(), child.getSelf().getPath().length(), cmp.name, profile.present());
    } else if (!child.getSelf().hasType() && ed.getType().get(0).getWorkingCode().equals("Reference")) {
      for (TypeRefComponent t: ed.getType()) {
        if (!t.getWorkingCode().equals("Reference")) {
          throw new Error(context.formatMessage(I18nConstants.NOT_HANDLED_YET_SORTELEMENTS_, ed.getPath(), typeCode(ed.getType())));
        }
      }
      StructureDefinition profile = findProfile(new UriType(sdNs(ed.getType().get(0).getWorkingCode())), cmp.src);
      ccmp = new ElementDefinitionComparer(false, profile, profile.getSnapshot().getElement(), ed.getType().get(0).getWorkingCode(), child.getSelf().getPath().length(), cmp.name, profile.present());
    } else {
      // this is allowed if we only profile the extensions
      StructureDefinition profile = findProfile(new UriType(sdNs("Element")), cmp.src);
      if (profile==null)
        throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_PROFILE__IN_ELEMENT_, sdNs(ed.getType().get(0).getWorkingCode()), ed.getPath()));
      ccmp = new ElementDefinitionComparer(false, profile, profile.getSnapshot().getElement(), "Element", child.getSelf().getPath().length(), cmp.name, profile.present());
//      throw new Error("Not handled yet (sortElements: "+ed.getPath()+":"+typeCode(ed.getType())+")");
    }
    return ccmp;
  }

  private String resolveType(String code, StructureDefinition src) {
    if (Utilities.isAbsoluteUrl(code)) {
      StructureDefinition sd = findProfile(new UriType(code), src);
      if (sd != null) {
        return sd.getType();
      }
    }
    return code;
  }

  private static String sdNs(String type) {
    return sdNs(type, null);
  }
  
  public static String sdNs(String type, String overrideVersionNs) {
    if (Utilities.isAbsoluteUrl(type))
      return type;
    else if (overrideVersionNs != null)
      return Utilities.pathURL(overrideVersionNs, type);
    else
      return "http://hl7.org/fhir/StructureDefinition/"+type;
  }


  private boolean isAbstract(String code) {
    return code.equals("Element") || code.equals("BackboneElement") || code.equals("Resource") || code.equals("DomainResource");
  }


  private void writeElements(ElementDefinitionHolder edh, List<ElementDefinition> list) {
    if (!edh.isPlaceHolder())
      list.add(edh.getSelf());
    for (ElementDefinitionHolder child : edh.getChildren()) {
      writeElements(child, list);
    }
  }

  /**
   * First compare element by path then by name if same
   */
  private static class ElementNameCompare implements Comparator<ElementDefinition> {

    @Override
    public int compare(ElementDefinition o1, ElementDefinition o2) {
      String path1 = normalizePath(o1);
      String path2 = normalizePath(o2);
      int cmp = path1.compareTo(path2);
      if (cmp == 0) {
        String name1 = o1.hasSliceName() ? o1.getSliceName() : "";
        String name2 = o2.hasSliceName() ? o2.getSliceName() : "";
        cmp = name1.compareTo(name2);
      }
      return cmp;
    }

    private static String normalizePath(ElementDefinition e) {
      if (!e.hasPath()) return "";
      String path = e.getPath();
      // if sorting element names make sure onset[x] appears before onsetAge, onsetDate, etc.
      // so strip off the [x] suffix when comparing the path names.
      if (path.endsWith("[x]")) {
        path = path.substring(0, path.length()-3);
      }
      return path;
    }

  }


  // generate schematrons for the rules in a structure definition
  public void generateSchematrons(OutputStream dest, StructureDefinition structure) throws IOException, DefinitionException {
    if (structure.getDerivation() != TypeDerivationRule.CONSTRAINT)
      throw new DefinitionException(context.formatMessage(I18nConstants.NOT_THE_RIGHT_KIND_OF_STRUCTURE_TO_GENERATE_SCHEMATRONS_FOR));
    if (!structure.hasSnapshot())
      throw new DefinitionException(context.formatMessage(I18nConstants.NEEDS_A_SNAPSHOT));

  	StructureDefinition base = findProfile(structure.getBaseDefinitionElement(), structure);

  	if (base != null) {
  	  SchematronWriter sch = new SchematronWriter(dest, SchematronType.PROFILE, base.getName());

  	  ElementDefinition ed = structure.getSnapshot().getElement().get(0);
  	  generateForChildren(sch, "f:"+ed.getPath(), ed, structure, base);
  	  sch.dump();
  	}
  }

  public StructureDefinition findProfileStr(String ref, Resource source) {
    return findProfile(new UriType(ref), source);
  }

  public StructureDefinition findProfile(UriType ref, Resource source) {
    String url = ref.getValue();
    if (url == null) {
      return null;
    }
    if (url.contains("#")) {
      url = url.substring(0, url.indexOf("#"));
    }
    String u = url;
    String v = null;
    if (url.contains("|")) {
      v = url.substring(u.indexOf("|")+1);
      u = u.substring(0, u.indexOf("|"));
    }
    if (parameters != null) {
      if (v == null) {
        for (Parameters.ParametersParameterComponent p : parameters.getParameter()) {
          if ("default-profile-version".equals(p.getName())) {
            String s = p.getValue().primitiveValue();
            if (s.startsWith(u + "|")) {
              v = s.substring(s.indexOf("|") + 1);
            }
          }
        }
      }
      for (Parameters.ParametersParameterComponent p : parameters.getParameter()) {
        if ("force-profile-version".equals(p.getName())) {
          String s = p.getValue().primitiveValue();
          if (s.startsWith(u + "|")) {
            v = s.substring(s.indexOf("|") + 1);
          }
        }
      }
      for (Parameters.ParametersParameterComponent p : parameters.getParameter()) {
        if ("check-profile-version".equals(p.getName())) {
          String s = p.getValue().primitiveValue();
          if (s.startsWith(u + "|")) {
            String vc = s.substring(s.indexOf("|") + 1);
            if (!vc.equals(v)) {
              throw new FHIRException("Profile resolves to " + v + " which does not match required profile version v" + vc);
            }
          }
        }
      }
    }
    // switch the extension pack in
    if (source != null && source.getSourcePackage() != null && source.getSourcePackage().isCore()) {
      source = null;
    }
    // matchbox patch #424 findProfile gets called by FHIRPathEngine
// FIXME #487  StructureDefinition sd = context.fetchResource(StructureDefinition.class, u, ExtensionUtilities.getVersionResolutionRules(ref), v, source);
    StructureDefinition sd = context.fetchResource(StructureDefinition.class, u, null, null, source); 
    if (sd == null) {
      if (makeXVer().matchingUrl(u) && xver.status(u) == XVerExtensionStatus.Valid) {
        sd = xver.getDefinition(u);
        if (sd!=null) {
          generateSnapshot(context.fetchTypeDefinition("Extension"), sd, sd.getUrl(), context.getSpecUrl(), sd.getName());
        }
      }
    }
    return sd;
  }

  // generate a CSV representation of the structure definition
  public void generateCsv(OutputStream dest, StructureDefinition structure, boolean asXml) throws IOException, DefinitionException, Exception {
    if (!structure.hasSnapshot())
      throw new DefinitionException(context.formatMessage(I18nConstants.NEEDS_A_SNAPSHOT));

    CSVWriter csv = new CSVWriter(dest, structure, asXml);

    for (ElementDefinition child : structure.getSnapshot().getElement()) {
      csv.processElement(null, child);
    }
    csv.dump();
  }
  
  // generate a CSV representation of the structure definition
  public void addToCSV(CSVWriter csv, StructureDefinition structure) throws IOException, DefinitionException, Exception {
    if (!structure.hasSnapshot())
      throw new DefinitionException(context.formatMessage(I18nConstants.NEEDS_A_SNAPSHOT));

    for (ElementDefinition child : structure.getSnapshot().getElement()) {
      csv.processElement(structure, child);
    }
  }
  
  
  private class Slicer extends ElementDefinitionSlicingComponent {
    String criteria = "";
    String name = "";   
    boolean check;
    public Slicer(boolean cantCheck) {
      super();
      this.check = cantCheck;
    }
  }
  
  private Slicer generateSlicer(ElementDefinition child, ElementDefinitionSlicingComponent slicing, StructureDefinition structure) {
    // given a child in a structure, it's sliced. figure out the slicing xpath
    if (child.getPath().endsWith(".extension")) {
      ElementDefinition ued = getUrlFor(structure, child);
      if ((ued == null || !ued.hasFixed()) && !(child.hasType() && (child.getType().get(0).hasProfile())))
        return new Slicer(false);
      else {
      Slicer s = new Slicer(true);
      String url = (ued == null || !ued.hasFixed()) ? child.getType().get(0).getProfile().get(0).getValue() : ((UriType) ued.getFixed()).asStringValue();
      s.name = " with URL = '"+url+"'";
      s.criteria = "[@url = '"+url+"']";
      return s;
      }
    } else
      return new Slicer(false);
  }

  private void generateForChildren(SchematronWriter sch, String xpath, ElementDefinition ed, StructureDefinition structure, StructureDefinition base) throws IOException {
    //    generateForChild(txt, structure, child);
    List<ElementDefinition> children = getChildList(structure, ed);
    String sliceName = null;
    ElementDefinitionSlicingComponent slicing = null;
    for (ElementDefinition child : children) {
      String name = tail(child.getPath());
      if (child.hasSlicing()) {
        sliceName = name;
        slicing = child.getSlicing();        
      } else if (!name.equals(sliceName))
        slicing = null;

      ElementDefinition based = getByPath(base, child.getPath());
      boolean doMin = (child.getMin() > 0) && (based == null || (child.getMin() != based.getMin()));
      boolean doMax = child.hasMax() && !child.getMax().equals("*") && (based == null || (!child.getMax().equals(based.getMax())));
      Slicer slicer = slicing == null ? new Slicer(true) : generateSlicer(child, slicing, structure);
      if (slicer.check) {
        if (doMin || doMax) {
          Section s = sch.section(xpath);
          Rule r = s.rule(xpath);
          if (doMin) 
            r.assrt("count(f:"+name+slicer.criteria+") >= "+Integer.toString(child.getMin()), name+slicer.name+": minimum cardinality of '"+name+"' is "+Integer.toString(child.getMin()));
          if (doMax) 
            r.assrt("count(f:"+name+slicer.criteria+") <= "+child.getMax(), name+slicer.name+": maximum cardinality of '"+name+"' is "+child.getMax());
        }
      }
    }
/// xpath has been removed
//    for (ElementDefinitionConstraintComponent inv : ed.getConstraint()) {
//      if (inv.hasXpath()) {
//        Section s = sch.section(ed.getPath());
//        Rule r = s.rule(xpath);
//        r.assrt(inv.getXpath(), (inv.hasId() ? inv.getId()+": " : "")+inv.getHuman()+(inv.hasUserData(IS_DERIVED) ? " (inherited)" : ""));
//      }
//    }
    if (!ed.hasContentReference()) {
      for (ElementDefinition child : children) {
        String name = tail(child.getPath());
        generateForChildren(sch, xpath+"/f:"+name, child, structure, base);
      }
    }
  }




  private ElementDefinition getByPath(StructureDefinition base, String path) {
		for (ElementDefinition ed : base.getSnapshot().getElement()) {
			if (ed.getPath().equals(path))
				return ed;
			if (ed.getPath().endsWith("[x]") && ed.getPath().length() <= path.length()-3 &&  ed.getPath().substring(0, ed.getPath().length()-3).equals(path.substring(0, ed.getPath().length()-3)))
				return ed;
    }
	  return null;
  }


  public void setIds(StructureDefinition sd, boolean checkFirst) throws DefinitionException  {
    if (!checkFirst || !sd.hasDifferential() || hasMissingIds(sd.getDifferential().getElement())) {
      if (!sd.hasDifferential())
        sd.setDifferential(new StructureDefinitionDifferentialComponent());
      generateIds(sd.getDifferential().getElement(), sd.getUrl(), sd.getType(), sd);
    }
    if (!checkFirst || !sd.hasSnapshot() || hasMissingIds(sd.getSnapshot().getElement())) {
      if (!sd.hasSnapshot())
        sd.setSnapshot(new StructureDefinitionSnapshotComponent());
      generateIds(sd.getSnapshot().getElement(), sd.getUrl(), sd.getType(), sd);
    }
  }


  private boolean hasMissingIds(List<ElementDefinition> list) {
    for (ElementDefinition ed : list) {
      if (!ed.hasId())
        return true;
    }    
    return false;
  }

  private class SliceList {

    private Map<String, String> slices = new HashMap<>();
    
    public void seeElement(ElementDefinition ed) {
      Iterator<Map.Entry<String,String>> iter = slices.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<String,String> entry = iter.next();
        if (entry.getKey().length() > ed.getPath().length() || entry.getKey().equals(ed.getPath()))
          iter.remove();
      }
      
      if (ed.hasSliceName()) 
        slices.put(ed.getPath(), ed.getSliceName());
    }

    public String[] analyse(List<String> paths) {
      String s = paths.get(0);
      String[] res = new String[paths.size()];
      res[0] = null;
      for (int i = 1; i < paths.size(); i++) {
        s = s + "."+paths.get(i);
        if (slices.containsKey(s)) 
          res[i] = slices.get(s);
        else
          res[i] = null;
      }
      return res;
    }

  }

  protected void generateIds(List<ElementDefinition> list, String name, String type, StructureDefinition srcSD) throws DefinitionException  {
    if (list.isEmpty())
      return;
    
    Map<String, String> idList = new HashMap<String, String>();
    Map<String, String> replacedIds = new HashMap<String, String>();
    
    SliceList sliceInfo = new SliceList();
    // first pass, update the element ids
    for (ElementDefinition ed : list) {
      generateIdForElement(list, name, type, srcSD, ed, sliceInfo, replacedIds, idList);
    }  
    // second path - fix up any broken path based id references
    
  }

  private void generateIdForElement(List<ElementDefinition> list, String name, String type, StructureDefinition srcSD, ElementDefinition ed, SliceList sliceInfo, Map<String, String> replacedIds, Map<String, String> idList) {
    List<String> paths = new ArrayList<String>();
    if (!ed.hasPath())
      throw new DefinitionException(context.formatMessage(I18nConstants.NO_PATH_ON_ELEMENT_DEFINITION__IN_, Integer.toString(list.indexOf(ed)), name));
    sliceInfo.seeElement(ed);
    String[] pl = ed.getPath().split("\\.");
    for (int i = paths.size(); i < pl.length; i++) // -1 because the last path is in focus
      paths.add(pl[i]);
    String slices[] = sliceInfo.analyse(paths);

    StringBuilder b = new StringBuilder();
    b.append(paths.get(0));
    for (int i = 1; i < paths.size(); i++) {
      b.append(".");
      String s = paths.get(i);
      String p = slices[i];
      b.append(fixChars(s));
      if (p != null) {
        b.append(":");
        b.append(p);
      }
    }
    String bs = b.toString();
    if (ed.hasId()) {
      replacedIds.put(ed.getId(), ed.getPath());
    }
    ed.setId(bs);
    if (idList.containsKey(bs)) {
      addMessage(new ValidationMessage(Source.ProfileValidator, IssueType.BUSINESSRULE, name +"."+bs, context.formatMessage(I18nConstants.SAME_ID_ON_MULTIPLE_ELEMENTS__IN_, bs, idList.get(bs), ed.getPath(), name), IssueSeverity.ERROR));
    }
    idList.put(bs, ed.getPath());
    if (ed.hasContentReference() && ed.getContentReference().startsWith("#")) {
      String s = ed.getContentReference();
      String typeURL = getUrlForSource(type, srcSD);
      ed.setContentReference(typeURL+s);
    }
  }


  private String getUrlForSource(String type, StructureDefinition srcSD) {
    if (srcSD.getKind() == StructureDefinitionKind.LOGICAL) {
      return srcSD.getUrl();
    } else {
      return "http://hl7.org/fhir/StructureDefinition/"+type;
    }
  }

  private Object fixChars(String s) {
    return s.replace("_", "-");
  }


//  private String describeExtension(ElementDefinition ed) {
//    if (!ed.hasType() || !ed.getTypeFirstRep().hasProfile())
//      return "";
//    return "$"+urlTail(ed.getTypeFirstRep().getProfile());
//  }
//

  private static String urlTail(String profile) {
    return profile.contains("/") ? profile.substring(profile.lastIndexOf("/")+1) : profile;
  }
//
//
//  private String checkName(String name) {
////    if (name.contains("."))
//////      throw new Exception("Illegal name "+name+": no '.'");
////    if (name.contains(" "))
////      throw new Exception("Illegal name "+name+": no spaces");
//    StringBuilder b = new StringBuilder();
//    for (char c : name.toCharArray()) {
//      if (!Utilities.existsInList(c, '.', ' ', ':', '"', '\'', '(', ')', '&', '[', ']'))
//        b.append(c);
//    }
//    return b.toString().toLowerCase();
//  }
//
//
//  private int charCount(String path, char t) {
//    int res = 0;
//    for (char ch : path.toCharArray()) {
//      if (ch == t)
//        res++;
//    }
//    return res;
//  }

//
//private void generateForChild(TextStreamWriter txt,
//    StructureDefinition structure, ElementDefinition child) {
//  // TODO Auto-generated method stub
//
//}

  private interface ExampleValueAccessor {
    DataType getExampleValue(ElementDefinition ed);
    String getId();
  }

  private class BaseExampleValueAccessor implements ExampleValueAccessor {
    @Override
    public DataType getExampleValue(ElementDefinition ed) {
      if (ed.hasFixed())
        return ed.getFixed();
      if (ed.hasExample())
        return ed.getExample().get(0).getValue();
      else
        return null;
    }

    @Override
    public String getId() {
      return "-genexample";
    }
  }
  
  private class ExtendedExampleValueAccessor implements ExampleValueAccessor {
    private String index;

    public ExtendedExampleValueAccessor(String index) {
      this.index = index;
    }
    @Override
    public DataType getExampleValue(ElementDefinition ed) {
      if (ed.hasFixed())
        return ed.getFixed();
      for (Extension ex : ed.getExtension()) {
       String ndx = ExtensionUtilities.readStringExtension(ex, "index");
       DataType value = ExtensionUtilities.getExtension(ex, "exValue").getValue();
       if (index.equals(ndx) && value != null)
         return value;
      }
      return null;
    }
    @Override
    public String getId() {
      return "-genexample-"+index;
    }
  }
  
  public List<org.hl7.fhir.r5.elementmodel.Element> generateExamples(StructureDefinition sd, boolean evenWhenNoExamples) throws FHIRException {
    List<org.hl7.fhir.r5.elementmodel.Element> examples = new ArrayList<org.hl7.fhir.r5.elementmodel.Element>();
    if (sd.hasSnapshot()) {
      if (evenWhenNoExamples || hasAnyExampleValues(sd)) 
        examples.add(generateExample(sd, new BaseExampleValueAccessor()));
      for (int i = 1; i <= 50; i++) {
        if (hasAnyExampleValues(sd, Integer.toString(i))) 
          examples.add(generateExample(sd, new ExtendedExampleValueAccessor(Integer.toString(i))));
      }
    }
    return examples;
  }

  private org.hl7.fhir.r5.elementmodel.Element generateExample(StructureDefinition profile, ExampleValueAccessor accessor) throws FHIRException {
    ElementDefinition ed = profile.getSnapshot().getElementFirstRep();
    org.hl7.fhir.r5.elementmodel.Element r = new org.hl7.fhir.r5.elementmodel.Element(ed.getPath(), new Property(context, ed, profile));
    SourcedChildDefinitions children = getChildMap(profile, ed, true);
    for (ElementDefinition child : children.getList()) {
      if (child.getPath().endsWith(".id")) {
        org.hl7.fhir.r5.elementmodel.Element id = new org.hl7.fhir.r5.elementmodel.Element("id", new Property(context, child, profile));
        id.setValue(profile.getId()+accessor.getId());
        r.getChildren().add(id);
      } else { 
        org.hl7.fhir.r5.elementmodel.Element e = createExampleElement(profile, child, accessor);
        if (e != null)
          r.getChildren().add(e);
      }
    }
    return r;
  }

  private org.hl7.fhir.r5.elementmodel.Element createExampleElement(StructureDefinition profile, ElementDefinition ed, ExampleValueAccessor accessor) throws FHIRException {
    DataType v = accessor.getExampleValue(ed);
    if (v != null) {
      return new ObjectConverter(context).convert(new Property(context, ed, profile), v);
    } else {
      org.hl7.fhir.r5.elementmodel.Element res = new org.hl7.fhir.r5.elementmodel.Element(tail(ed.getPath()), new Property(context, ed, profile));
      boolean hasValue = false;
      SourcedChildDefinitions children = getChildMap(profile, ed, true);
      for (ElementDefinition child : children.getList()) {
        if (!child.hasContentReference()) {
        org.hl7.fhir.r5.elementmodel.Element e = createExampleElement(profile, child, accessor);
        if (e != null) {
          hasValue = true;
          res.getChildren().add(e);
        }
      }
      }
      if (hasValue)
        return res;
      else
        return null;
    }
  }

  private boolean hasAnyExampleValues(StructureDefinition sd, String index) {
    for (ElementDefinition ed : sd.getSnapshot().getElement())
      for (Extension ex : ed.getExtension()) {
        String ndx = ExtensionUtilities.readStringExtension(ex, "index");
        Extension exv = ExtensionUtilities.getExtension(ex, "exValue");
        if (exv != null) {
          DataType value = exv.getValue();
        if (index.equals(ndx) && value != null)
          return true;
        }
       }
    return false;
  }


  private boolean hasAnyExampleValues(StructureDefinition sd) {
    for (ElementDefinition ed : sd.getSnapshot().getElement())
      if (ed.hasExample())
        return true;
    return false;
  }


  public void populateLogicalSnapshot(StructureDefinition sd) throws FHIRException {
    sd.getSnapshot().getElement().add(sd.getDifferential().getElementFirstRep().copy());
    
    if (sd.hasBaseDefinition()) {
    StructureDefinition base = findProfile(sd.getBaseDefinitionElement(), sd);
    if (base == null)
        throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_FIND_BASE_DEFINITION_FOR_LOGICAL_MODEL__FROM_, sd.getBaseDefinition(), sd.getUrl()));
    copyElements(sd, base.getSnapshot().getElement());
    }
    copyElements(sd, sd.getDifferential().getElement());
  }


  private void copyElements(StructureDefinition sd, List<ElementDefinition> list) {
    for (ElementDefinition ed : list) {
      if (ed.getPath().contains(".")) {
        ElementDefinition n = ed.copy();
        n.setPath(sd.getSnapshot().getElementFirstRep().getPath()+"."+ed.getPath().substring(ed.getPath().indexOf(".")+1));
        sd.getSnapshot().addElement(n);
      }
    }
  }

    
  public void cleanUpDifferential(StructureDefinition sd) {
    if (sd.getDifferential().getElement().size() > 1)
      cleanUpDifferential(sd, 1);
  }
  
  private void cleanUpDifferential(StructureDefinition sd, int start) {
    int level = Utilities.charCount(sd.getDifferential().getElement().get(start).getPath(), '.');
    int c = start;
    int len = sd.getDifferential().getElement().size();
    HashSet<String> paths = new HashSet<String>();
    while (c < len && Utilities.charCount(sd.getDifferential().getElement().get(c).getPath(), '.') == level) {
      ElementDefinition ed = sd.getDifferential().getElement().get(c);
      if (!paths.contains(ed.getPath())) {
        paths.add(ed.getPath());
        int ic = c+1; 
        while (ic < len && Utilities.charCount(sd.getDifferential().getElement().get(ic).getPath(), '.') > level) 
          ic++;
        ElementDefinition slicer = null;
        List<ElementDefinition> slices = new ArrayList<ElementDefinition>();
        slices.add(ed);
        while (ic < len && Utilities.charCount(sd.getDifferential().getElement().get(ic).getPath(), '.') == level) {
          ElementDefinition edi = sd.getDifferential().getElement().get(ic);
          if (ed.getPath().equals(edi.getPath())) {
            if (slicer == null) {
              slicer = new ElementDefinition();
              slicer.setPath(edi.getPath());
              slicer.getSlicing().setRules(SlicingRules.OPEN);
              sd.getDifferential().getElement().add(c, slicer);
              c++;
              ic++;
            }
            slices.add(edi);
          }
          ic++;
          while (ic < len && Utilities.charCount(sd.getDifferential().getElement().get(ic).getPath(), '.') > level) 
            ic++;
        }
        // now we're at the end, we're going to figure out the slicing discriminator
        if (slicer != null)
          determineSlicing(slicer, slices);
      }
      c++;
      if (c < len && Utilities.charCount(sd.getDifferential().getElement().get(c).getPath(), '.') > level) {
        cleanUpDifferential(sd, c);
        c++;
        while (c < len && Utilities.charCount(sd.getDifferential().getElement().get(c).getPath(), '.') > level) 
          c++;
      }
  }
  }


  private void determineSlicing(ElementDefinition slicer, List<ElementDefinition> slices) {
    // first, name them
    int i = 0;
    for (ElementDefinition ed : slices) {
      if (ed.hasUserData(UserDataNames.SNAPSHOT_slice_name)) {
        ed.setSliceName(ed.getUserString(UserDataNames.SNAPSHOT_slice_name));
      } else {
        i++;
        ed.setSliceName("slice-"+Integer.toString(i));
      }
    }
    // now, the hard bit, how are they differentiated? 
    // right now, we hard code this...
    if (slicer.getPath().endsWith(".extension") || slicer.getPath().endsWith(".modifierExtension"))
      slicer.getSlicing().addDiscriminator().setType(DiscriminatorType.VALUE).setPath("url");
    else if (slicer.getPath().equals("DiagnosticReport.result"))
      slicer.getSlicing().addDiscriminator().setType(DiscriminatorType.VALUE).setPath("reference.code");
    else if (slicer.getPath().equals("Observation.related"))
      slicer.getSlicing().addDiscriminator().setType(DiscriminatorType.VALUE).setPath("target.reference.code");
    else if (slicer.getPath().equals("Bundle.entry"))
      slicer.getSlicing().addDiscriminator().setType(DiscriminatorType.VALUE).setPath("resource.@profile");
    else  
      throw new Error("No slicing for "+slicer.getPath());
  }


  public static ElementDefinitionSlicingDiscriminatorComponent interpretR2Discriminator(String discriminator, boolean isExists) {
    if (discriminator.endsWith("@pattern"))
      return makeDiscriminator(DiscriminatorType.PATTERN, discriminator.length() == 8 ? "" : discriminator.substring(0,discriminator.length()-9)); 
    if (discriminator.endsWith("@profile"))
      return makeDiscriminator(DiscriminatorType.PROFILE, discriminator.length() == 8 ? "" : discriminator.substring(0,discriminator.length()-9)); 
    if (discriminator.endsWith("@type")) 
      return makeDiscriminator(DiscriminatorType.TYPE, discriminator.length() == 5 ? "" : discriminator.substring(0,discriminator.length()-6));
    if (discriminator.endsWith("@exists"))
      return makeDiscriminator(DiscriminatorType.EXISTS, discriminator.length() == 7 ? "" : discriminator.substring(0,discriminator.length()-8)); 
    if (isExists)
      return makeDiscriminator(DiscriminatorType.EXISTS, discriminator); 
    return new ElementDefinitionSlicingDiscriminatorComponent().setType(DiscriminatorType.VALUE).setPath(discriminator);
  }


  private static ElementDefinitionSlicingDiscriminatorComponent makeDiscriminator(DiscriminatorType dType, String str) {
    return new ElementDefinitionSlicingDiscriminatorComponent().setType(dType).setPath(Utilities.noString(str)? "$this" : str);
  }


  public static String buildR2Discriminator(ElementDefinitionSlicingDiscriminatorComponent t) throws FHIRException {
    switch (t.getType()) {
    case PROFILE: return t.getPath()+"/@profile";
    case PATTERN: return t.getPath()+"/@pattern";
    case TYPE: return t.getPath()+"/@type";
    case VALUE: return t.getPath();
    case EXISTS: return t.getPath(); // determination of value vs. exists is based on whether there's only 2 slices - one with minOccurs=1 and other with maxOccur=0
    default: throw new FHIRException("Unable to represent "+t.getType().toCode()+":"+t.getPath()+" in R2");    
    }
  }


  public static StructureDefinition makeExtensionForVersionedURL(IWorkerContext context, String url) {
    String epath = url.substring(54);
    if (!epath.contains("."))
      return null;
    String type = epath.substring(0, epath.indexOf("."));
    StructureDefinition sd = context.fetchTypeDefinition(type);
    if (sd == null)
      return null;
    ElementDefinition ed = null;
    for (ElementDefinition t : sd.getSnapshot().getElement()) {
      if (t.getPath().equals(epath)) {
        ed = t;
        break;
      }
    }
    if (ed == null)
      return null;
    if ("Element".equals(ed.typeSummary()) || "BackboneElement".equals(ed.typeSummary())) {
      return null;
    } else {
      StructureDefinition template = context.fetchResource(StructureDefinition.class, "http://fhir-registry.smarthealthit.org/StructureDefinition/capabilities", IWorkerContext.VersionResolutionRules.defaultRule());
      StructureDefinition ext = template.copy();
      ext.setUrl(url);
      ext.setId("extension-"+epath);
      ext.setName("Extension-"+epath);
      ext.setTitle("Extension for r4 "+epath);
      ext.setStatus(sd.getStatus());
      ext.setDate(sd.getDate());
      ext.getContact().clear();
      ext.getContact().addAll(sd.getContact());
      ext.setFhirVersion(sd.getFhirVersion());
      ext.setDescription(ed.getDefinition());
      ext.getContext().clear();
      ext.addContext().setType(ExtensionContextType.ELEMENT).setExpression(epath.substring(0, epath.lastIndexOf(".")));
      ext.getDifferential().getElement().clear();
      ext.getSnapshot().getElement().get(3).setFixed(new UriType(url));
      ext.getSnapshot().getElement().set(4, ed.copy());
      ext.getSnapshot().getElement().get(4).setPath("Extension.value"+Utilities.capitalize(ed.typeSummary()));
      return ext;      
    }

  }


  public boolean isThrowException() {
    return wantThrowExceptions;
  }


  public void setThrowException(boolean exception) {
    this.wantThrowExceptions = exception;
  }


  public ValidationOptions getTerminologyServiceOptions() {
    return terminologyServiceOptions;
  }


  public void setTerminologyServiceOptions(ValidationOptions terminologyServiceOptions) {
    this.terminologyServiceOptions = terminologyServiceOptions;
  }


  public boolean isNewSlicingProcessing() {
    return newSlicingProcessing;
  }


  public ProfileUtilities setNewSlicingProcessing(boolean newSlicingProcessing) {
    this.newSlicingProcessing = newSlicingProcessing;
    return this;
  }


  public boolean isDebug() {
    return debug;
  }


  public void setDebug(boolean debug) {
    this.debug = debug;
  }


  public String getDefWebRoot() {
    return defWebRoot;
  }


  public void setDefWebRoot(String defWebRoot) {
    this.defWebRoot = defWebRoot;
    if (!this.defWebRoot.endsWith("/"))
      this.defWebRoot = this.defWebRoot + '/';
  }


  public static StructureDefinition makeBaseDefinition(FHIRVersion fhirVersion) {
    return makeBaseDefinition(fhirVersion.toCode());
  }
  public static StructureDefinition makeBaseDefinition(String fhirVersion) {
    StructureDefinition base = new StructureDefinition();
    base.setId("Base");
    base.setUrl("http://hl7.org/fhir/StructureDefinition/Base");
    base.setVersion(fhirVersion);
    base.setName("Base"); 
    base.setStatus(PublicationStatus.ACTIVE);
    base.setDate(new Date());
    base.setFhirVersion(FHIRVersion.fromCode(fhirVersion));
    base.setKind(StructureDefinitionKind.COMPLEXTYPE); 
    base.setAbstract(true); 
    base.setType("Base");
    base.setWebPath("http://build.fhir.org/types.html#Base");
    ElementDefinition e = base.getSnapshot().getElementFirstRep();
    e.setId("Base");
    e.setPath("Base"); 
    e.setMin(0); 
    e.setMax("*"); 
    e.getBase().setPath("Base");
    e.getBase().setMin(0); 
    e.getBase().setMax("*"); 
    e.setIsModifier(false); 
    e = base.getDifferential().getElementFirstRep();
    e.setId("Base");
    e.setPath("Base"); 
    e.setMin(0); 
    e.setMax("*"); 
    return base;
  }

  public XVerExtensionManager getXver() {
    return xver;
  }

  public ProfileUtilities setXver(XVerExtensionManager xver) {
    this.xver = xver;
    return this;
  }


  private List<ElementChoiceGroup> readChoices(ElementDefinition ed, List<ElementDefinition> children) {
    List<ElementChoiceGroup> result = new ArrayList<>();
    for (ElementDefinitionConstraintComponent c : ed.getConstraint()) {
      ElementChoiceGroup grp = processConstraint(children, c);
      if (grp != null) {
        result.add(grp);
      }
    }
    return result;
  }

  public ElementChoiceGroup processConstraint(List<ElementDefinition> children, ElementDefinitionConstraintComponent c) {
    if (!c.hasExpression()) {
      return null;
    }
    ExpressionNode expr = null;
    try {
      expr = fpe.parse(c.getExpression());
    } catch (Exception e) {
      return null;
    }
    if (expr.getKind() != Kind.Group || expr.getOpNext() == null || !(expr.getOperation() == Operation.Equals || expr.getOperation() == Operation.LessOrEqual)) {
      return null;      
    }
    ExpressionNode n1 = expr.getGroup();
    ExpressionNode n2 = expr.getOpNext();
    if (n2.getKind() != Kind.Constant || n2.getInner() != null || n2.getOpNext() != null || !"1".equals(n2.getConstant().primitiveValue())) {
      return null;
    }
    ElementChoiceGroup grp = new ElementChoiceGroup(c.getKey(), expr.getOperation() == Operation.Equals);
    while (n1 != null) {
      if (n1.getKind() != Kind.Name || n1.getInner() != null) {
        return null;
      }
      grp.elements.add(n1.getName());
      if (n1.getOperation() == null || n1.getOperation() == Operation.Union) {
        n1 = n1.getOpNext();
      } else {
        return null;
      }
    }
    int total = 0;
    for (String n : grp.elements) {
      boolean found = false;
      for (ElementDefinition child : children) {
        String name = tail(child.getPath());
        if (n.equals(name)) {
          found = true;
          if (!"0".equals(child.getMax())) {
            total++;
          }
        }
      }
      if (!found) {
        return null;
      }
    }
    if (total <= 1) {
      return null;
    }
    return grp;
  }

  public Set<String> getMasterSourceFileNames() {
    return masterSourceFileNames;
  }

  public void setMasterSourceFileNames(Set<String> masterSourceFileNames) {
    this.masterSourceFileNames = masterSourceFileNames;
  }

  
  public Set<String> getLocalFileNames() {
    return localFileNames;
  }

  public void setLocalFileNames(Set<String> localFileNames) {
    this.localFileNames = localFileNames;
  }

  public ProfileKnowledgeProvider getPkp() {
    return pkp;
  }


  public static final String UD_ERROR_STATUS = "error-status";
  public static final int STATUS_OK = 0;
  public static final int STATUS_HINT = 1;
  public static final int STATUS_WARNING = 2;
  public static final int STATUS_ERROR = 3;
  public static final int STATUS_FATAL = 4;
  private static final String ROW_COLOR_ERROR = "#ffcccc";
  private static final String ROW_COLOR_FATAL = "#ff9999";
  private static final String ROW_COLOR_WARNING = "#ffebcc";
  private static final String ROW_COLOR_HINT = "#ebf5ff";
  private static final String ROW_COLOR_NOT_MUST_SUPPORT = "#d6eaf8";

  public String getRowColor(ElementDefinition element, boolean isConstraintMode) {
    switch (element.getUserInt(UD_ERROR_STATUS)) {
    case STATUS_HINT: return ROW_COLOR_HINT;
    case STATUS_WARNING: return ROW_COLOR_WARNING;
    case STATUS_ERROR: return ROW_COLOR_ERROR;
    case STATUS_FATAL: return ROW_COLOR_FATAL;
    }
    if (isConstraintMode && !element.getMustSupport() && !element.getIsModifier() && element.getPath().contains("."))
      return null; // ROW_COLOR_NOT_MUST_SUPPORT;
    else
      return null;
  }

  public static boolean isExtensionDefinition(StructureDefinition sd) {
    return sd.getDerivation() == TypeDerivationRule.CONSTRAINT && sd.getType().equals("Extension");
  }

  public AllowUnknownProfile getAllowUnknownProfile() {
    return allowUnknownProfile;
  }

  public void setAllowUnknownProfile(AllowUnknownProfile allowUnknownProfile) {
    this.allowUnknownProfile = allowUnknownProfile;
  }

  public static boolean isSimpleExtension(StructureDefinition sd) {
    if (!isExtensionDefinition(sd)) {
      return false;
    }
    ElementDefinition value = sd.getSnapshot().getElementByPath("Extension.value");
    return value != null && !value.isProhibited();
  }

  public static boolean isComplexExtension(StructureDefinition sd) {
    if (!isExtensionDefinition(sd)) {
      return false;
    }
    ElementDefinition value = sd.getSnapshot().getElementByPath("Extension.value");
    return value == null || value.isProhibited();
  }

  public static boolean isModifierExtension(StructureDefinition sd) {
    ElementDefinition defn = sd.getSnapshot().hasElement() ? sd.getSnapshot().getElementByPath("Extension") : sd.getDifferential().getElementByPath("Extension");
    return defn != null && defn.getIsModifier();
  }

  public boolean isForPublication() {
    return forPublication;
  }

  public void setForPublication(boolean forPublication) {
    this.forPublication = forPublication;
  }

  public List<ValidationMessage> getMessages() {
    return messages;
  }

  public static boolean isResourceBoundary(ElementDefinition ed) {
    return ed.getType().size() == 1 && "Resource".equals(ed.getTypeFirstRep().getCode());
  }

  public static boolean isSuppressIgnorableExceptions() {
    return suppressIgnorableExceptions;
  }

  public static void setSuppressIgnorableExceptions(boolean suppressIgnorableExceptions) {
    ProfileUtilities.suppressIgnorableExceptions = suppressIgnorableExceptions;
  }

  public void setMessages(List<ValidationMessage> messages) {
    if (messages != null) {
      this.messages = messages;
      wantThrowExceptions = false;
    }
  }

  private Map<String, List<Property>> propertyCache = new HashMap<>();
  
  public Map<String, List<Property>> getCachedPropertyList() {
    return propertyCache;
  }

  public void checkExtensions(ElementDefinition outcome) {
    outcome.getExtension().removeIf(ext -> Utilities.existsInList(ext.getUrl(), ProfileUtilities.NON_INHERITED_ED_URLS));
    if (outcome.hasBinding()) {
      outcome.getBinding().getExtension().removeIf(ext -> Utilities.existsInList(ext.getUrl(), ProfileUtilities.NON_INHERITED_ED_URLS));      
    }

  }
  
  public static void markExtensions(ElementDefinition ed, boolean overrideSource, StructureDefinition src) {
    for (Extension ex : ed.getExtension()) {
      markExtensionSource(ex, overrideSource, src);
    }
    for (Extension ex : ed.getBinding().getExtension()) {
      markExtensionSource(ex, overrideSource, src);
    }
    for (TypeRefComponent t : ed.getType()) {
      for (Extension ex : t.getExtension()) {
        markExtensionSource(ex, overrideSource, src);
      }
    }
  }

  public static boolean hasObligations(StructureDefinition sd) {
    if (sd.hasExtension(ExtensionDefinitions.EXT_OBLIGATION_CORE)) {
      return true;
    }
    for (ElementDefinition ed : sd.getSnapshot().getElement()) {
      if (ed.hasExtension(ExtensionDefinitions.EXT_OBLIGATION_CORE)) {
        return true;
      }
      for (TypeRefComponent tr : ed.getType()) {
        if (tr.hasExtension(ExtensionDefinitions.EXT_OBLIGATION_CORE)) {
          return true;
        }
      }
    }
    return false;
  }

  public List<String> getSuppressedMappings() {
    return suppressedMappings;
  }

  public void setSuppressedMappings(List<String> suppressedMappings) {
    this.suppressedMappings = suppressedMappings;
  }

  public FHIRPathEngine getFpe() {
    return fpe;
  }

}
