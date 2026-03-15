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



import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities.SourcedChildDefinitions;
import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.extensions.ExtensionDefinitions;
import org.hl7.fhir.r5.extensions.ExtensionUtilities;
import org.hl7.fhir.r5.fhirpath.TypeDetails;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.model.Constants;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.ElementDefinition.PropertyRepresentation;
import org.hl7.fhir.r5.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;

import org.hl7.fhir.r5.utils.TypesUtilities;
import org.hl7.fhir.utilities.CommaSeparatedStringBuilder;
import org.hl7.fhir.utilities.MarkedToMoveToAdjunctPackage;
import org.hl7.fhir.utilities.StringPair;
import org.hl7.fhir.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MarkedToMoveToAdjunctPackage
@Slf4j
public class Property {

  private IWorkerContext context;
	private ElementDefinition definition;
	private StructureDefinition structure;
  private ProfileUtilities profileUtilities;
  private ContextUtilities utils;
  private TypeRefComponent type;

  public Property(IWorkerContext context, ElementDefinition definition, StructureDefinition structure, ProfileUtilities profileUtilities, ContextUtilities utils) {
		this.context = context;
		this.definition = definition;
		this.structure = structure;
		this.utils = utils;
    this.profileUtilities = profileUtilities;
	}


  public Property(IWorkerContext context, ElementDefinition definition, StructureDefinition structure, ProfileUtilities profileUtilities, ContextUtilities utils, String type) {
    this.context = context;
    this.definition = definition;
    this.structure = structure;
    this.profileUtilities = profileUtilities;
    this.utils = utils;
    for (TypeRefComponent tr : definition.getType()) {
      if (tr.getWorkingCode().equals(type)) {
        this.type = tr;
      }
    }
  }
  
	public Property(IWorkerContext context, ElementDefinition definition, StructureDefinition structure) {
    this(context, definition, structure, new ProfileUtilities(context, null, null), new ContextUtilities(context));
	}

	public String getName() {
		return definition.getPath().substring(definition.getPath().lastIndexOf(".")+1);
	}

  public String getJsonName() {
    if (definition.hasExtension(ExtensionDefinitions.EXT_JSON_NAME, ExtensionDefinitions.EXT_JSON_NAME_DEPRECATED)) {
      return ExtensionUtilities.readStringExtension(definition, ExtensionDefinitions.EXT_JSON_NAME, ExtensionDefinitions.EXT_JSON_NAME_DEPRECATED);
    } else {
      return getName();
    }
  }

  public String getXmlName() {
    if (definition.hasExtension(ExtensionDefinitions.EXT_XML_NAME)) {
      return ExtensionUtilities.readStringExtension(definition, ExtensionDefinitions.EXT_XML_NAME);
    } else if (definition.hasExtension(ExtensionDefinitions.EXT_XML_NAME_DEPRECATED)) {
      return ExtensionUtilities.readStringExtension(definition, ExtensionDefinitions.EXT_XML_NAME_DEPRECATED);
    } else {
      return getName();
    }
  }

  public String getXmlNamespace() {
    if (ExtensionUtilities.hasAnyOfExtensions(definition, ExtensionDefinitions.EXT_XML_NAMESPACE, ExtensionDefinitions.EXT_XML_NAMESPACE_DEPRECATED)) {
      return ExtensionUtilities.readStringExtension(definition, ExtensionDefinitions.EXT_XML_NAMESPACE, ExtensionDefinitions.EXT_XML_NAMESPACE_DEPRECATED);
    } else if (ExtensionUtilities.hasAnyOfExtensions(structure, ExtensionDefinitions.EXT_XML_NAMESPACE, ExtensionDefinitions.EXT_XML_NAMESPACE_DEPRECATED)) {
      return ExtensionUtilities.readStringExtension(structure, ExtensionDefinitions.EXT_XML_NAMESPACE, ExtensionDefinitions.EXT_XML_NAMESPACE_DEPRECATED);
    } else {
      return FormatUtilities.FHIR_NS;
    }
  }
	
	public ElementDefinition getDefinition() {
		return definition;
	}

	public String getType() {
	  if (type != null) {
	    return type.getWorkingCode();
	  } else  if (definition.getType().size() == 0)
			return null;
		else if (definition.getType().size() > 1) {
			String tn = definition.getType().get(0).getWorkingCode();
			for (int i = 1; i < definition.getType().size(); i++) {
				if (!tn.equals(definition.getType().get(i).getWorkingCode()))
					return null; // though really, we shouldn't get here - type != null when definition.getType.size() > 1, or it should be
			}
			return tn;
		} else
			return definition.getType().get(0).getWorkingCode();
	}

	public String getType(String elementName) {
	  if (type != null) {
      return type.getWorkingCode();
    } 
	  if (!definition.getPath().contains("."))
      return definition.getPath();
    ElementDefinition ed = definition;
    if (definition.hasContentReference()) {
      String url = null;
      String path = definition.getContentReference();
      if (!path.startsWith("#")) {
        if (path.contains("#")) {
          url = path.substring(0, path.indexOf("#"));
          path = path.substring(path.indexOf("#")+1);
        } else {
          throw new Error("Illegal content reference '"+path+"'");
        }
      } else {
        path = path.substring(1);
      }
      StructureDefinition sd = (url == null || url.equals(structure.getUrl())) ? structure : profileUtilities.findProfile(url, structure);
      if (sd == null) {
        throw new Error("Unknown Type in content reference '"+path+"'");        
      }
      boolean found = false;
      for (ElementDefinition d : sd.getSnapshot().getElement()) {
        if (d.hasId() && d.getId().equals(path)) {
          found = true;
          ed = d;
        }
      }
      if (!found)
        throw new Error("Unable to resolve "+definition.getContentReference()+" at "+definition.getPath()+" on "+sd.getUrl());
    }
    if (ed.getType().size() == 0)
			return null;
    else if (ed.getType().size() > 1) {
      String t = ed.getType().get(0).getCode();
			boolean all = true;
      for (TypeRefComponent tr : ed.getType()) {
				if (!t.equals(tr.getCode()))
					all = false;
			}
			if (all)
				return t;
      String tail = ed.getPath().substring(ed.getPath().lastIndexOf(".")+1);
      if (tail.endsWith("[x]") && elementName != null && elementName.startsWith(tail.substring(0, tail.length()-3))) {
				String name = elementName.substring(tail.length()-3);
        return isPrimitive(lowFirst(name)) ? lowFirst(name) : name;        
			} else {
	      if (ExtensionUtilities.hasExtension(ed, "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype"))
	        return ExtensionUtilities.readStringExtension(ed, "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype");
        throw new Error("logic error, gettype when types > 1, name mismatch for "+elementName+" on at "+ed.getPath());
			}
    } else if (ed.getType().get(0).getCode() == null) {
      if (Utilities.existsInList(ed.getId(), "Element.id", "Extension.url"))
        return "string";
      else
        return structure.getId();
		} else
      return ed.getType().get(0).getWorkingCode();
	}

  public boolean typeIsConsistent(String typeName) {
    for (TypeRefComponent tr : definition.getType()) {
      if (typeName.equals(tr.getWorkingCode()) || typeSpecializes(tr.getWorkingCode(), typeName)) {
        return true;
      }
    }
    return false;
  }

  
  private boolean typeSpecializes(String workingCode, String typeName) {
    if ("string".equals(typeName)) {
      return Utilities.existsInList(workingCode, "uri", "oid", "canonical", "url", "uuid", "id", "markdown");
    }
    if ("integer".equals(typeName)) {
      return Utilities.existsInList(workingCode, "positiveInt", "unsignedInt");
    }
    return false;
  }


  public boolean hasType(String typeName) {
    if (type != null) {
      return false; // ?
    } else if (definition.getType().size() == 0) {
      return false;
    } else if (isJsonPrimitiveChoice()) { 
      for (TypeRefComponent tr : definition.getType()) {
        if (typeName.equals(tr.getWorkingCode())) {
          return true;
        }
      }
      return false;
    } else if (definition.getType().size() > 1) {
      String t = definition.getType().get(0).getCode();
      boolean all = true;
      for (TypeRefComponent tr : definition.getType()) {
        if (!t.equals(tr.getCode()))
          all = false;
      }
      if (all)
        return true;
      String tail = definition.getPath().substring(definition.getPath().lastIndexOf(".")+1);
      if (tail.endsWith("[x]") && typeName.startsWith(tail.substring(0, tail.length()-3))) {
//        String name = elementName.substring(tail.length()-3);
        return true;        
      } else
        return false;
    } else
      return true;
  }

	public StructureDefinition getStructure() {
		return structure;
	}

	/**
	 * Is the given name a primitive
	 * 
	 * @param E.g. "Observation.status"
	 */
	public boolean isPrimitiveName(String name) {
	  String code = getType(name);
      return isPrimitive(code);
	}

	/**
	 * Is the given type a primitive
	 * 
	 * @param E.g. "integer"
	 */
	public boolean isPrimitive(String code) {
	  return context.isPrimitiveType(code);
	}

	public boolean isPrimitive() {
	  return isPrimitive(getType());
	}
	private String lowFirst(String t) {
		return t.substring(0, 1).toLowerCase()+t.substring(1);
	}

	public boolean isResource() {
	  if (type != null) {
	    String tc = type.getCode();
      return (("Resource".equals(tc) || "DomainResource".equals(tc)) || utils.isResource(tc));
	  } else if (definition.getType().size() > 0) {
      String tc = definition.getType().get(0).getCode();
      return definition.getType().size() == 1 && (("Resource".equals(tc) || "DomainResource".equals(tc)) ||  utils.isResource(tc));
    }
	  else {
	    return !definition.getPath().contains(".") && (structure.getKind() == StructureDefinitionKind.RESOURCE);
	  }
	}

  public boolean isList() {
    return !"1".equals(definition.getBase().hasMax() ? definition.getBase().getMax() : definition.getMax());
  }

  /**
   * This handles a very special case: An extension used with json extensions in CDS hooks, 
   * where the extension definition, not the base, decides whether it's an array or not 
   * @return
   */
  public boolean isJsonList() {
    if (definition.hasExtension(ExtensionDefinitions.EXT_JSON_NAME, ExtensionDefinitions.EXT_JSON_NAME_DEPRECATED)) {
      return !"1".equals(definition.getMax());
    } else {
      return !"1".equals(definition.getBase().hasMax() ? definition.getBase().getMax() : definition.getMax());
    }
  }

  public boolean isBaseList() {
    return !"1".equals(definition.getBase().getMax());
  }

  public String getScopedPropertyName() {
    return definition.getBase().getPath();
  }

  private boolean isElementWithOnlyExtension(final ElementDefinition ed, final List<ElementDefinition> children) {
    boolean result = false;
    if (!ed.getType().isEmpty()) {
      result = true;
      for (final ElementDefinition ele : children) {
        if (!ele.getPath().contains("extension")) {
          result = false;
          break;
        }
      }
    }
    return result;
  }
  
	public boolean IsLogicalAndHasPrimitiveValue(String name) {
//		if (canBePrimitive!= null)
//			return canBePrimitive;
		
  	if (structure.getKind() != StructureDefinitionKind.LOGICAL)
  		return false;
  	if (!hasType(name))
  		return false;
  	StructureDefinition sd = context.fetchResource(StructureDefinition.class, structure.getUrl().substring(0, structure.getUrl().lastIndexOf("/")+1)+getType(name));
  	if (sd == null)
  	  sd = context.fetchResource(StructureDefinition.class, ProfileUtilities.sdNs(getType(name), null));
    if (sd != null && sd.getKind() == StructureDefinitionKind.PRIMITIVETYPE)
      return true;
  	if (sd == null || sd.getKind() != StructureDefinitionKind.LOGICAL)
  		return false;
  	for (ElementDefinition ed : sd.getSnapshot().getElement()) {
  		if (ed.getPath().equals(sd.getId()+".value") && ed.getType().size() == 1 && isPrimitive(ed.getType().get(0).getCode())) {
  			return true;
  		}
  	}
  	return false;
	}

  public boolean isChoice() {
    if (type != null) {
      return true;
    }
    if (definition.getType().size() <= 1)
      return false;
    String tn = definition.getType().get(0).getCode();
    for (int i = 1; i < definition.getType().size(); i++) 
      if (!definition.getType().get(i).getCode().equals(tn))
        return true;
    return false;
  }


  public List<Property> getChildProperties(String elementName, String statedType) throws FHIRException {
    String cacheKey = structure.getVUrl()+"#"+definition.getPath()+":"+elementName+"/"+statedType;
    List<Property> cached = profileUtilities.getCachedPropertyList().get(cacheKey);
    if (cached != null) {
      return cached;
    }
    ElementDefinition ed = definition;
    StructureDefinition sd = structure;
    boolean isCDA = isCDAElement(structure);
    SourcedChildDefinitions children = profileUtilities.getChildMap(sd, ed, false);
    String url = null;
    if (children.getList().isEmpty() || isElementWithOnlyExtension(ed, children.getList())) {
      // ok, find the right definitions
      String t = null;
      if (ed.getType().size() == 1 && (statedType == null || !isCDA))
        t = ed.getType().get(0).getWorkingCode();
      else if (ed.getType().size() == 0)
        throw new Error("types == 0, and no children found on "+getDefinition().getPath());
      else {
        t = ed.getType().get(0).getWorkingCode();
        boolean all = true;
        for (TypeRefComponent tr : ed.getType()) {
          if (!tr.getWorkingCode().equals(t)) {
            all = false;
            break;
          }
        }
        if (!all || (isCDA && statedType != null)) {
          // ok, it's polymorphic
          if (ed.hasRepresentation(PropertyRepresentation.TYPEATTR) || isCDA) {
            t = statedType;
            if (t == null && ExtensionUtilities.hasExtension(ed, "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype"))
              t = ExtensionUtilities.readStringExtension(ed, "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype");
            boolean ok = false;
            for (TypeRefComponent tr : ed.getType()) { 
              if (tr.getWorkingCode().equals(t)) 
                ok = true;
              if (Utilities.isAbsoluteUrl(tr.getWorkingCode())) {
                StructureDefinition sdt = context.fetchResource(StructureDefinition.class, tr.getWorkingCode());
                if (sdt != null && sdt.getTypeTail().equals(t)) {
                  url = tr.getWorkingCode();
                  ok = true;
                }
                if (!ok) {
                  sdt = findAncestor(t, sdt);
                  if (sdt != null) {
                    url = sdt.getUrl();
                    ok = true;
                  }
                }
              }
              if (ok) {
                break;
              }
            }
            if (!ok) {
              log.error("Type '"+t+"' (from '"+statedType+"') is not an acceptable type for '"+elementName+"' of type '"+ed.typeSummary()+"' on property "+definition.getPath());
              return new ArrayList<Property>();
            }
          } else {
            t = elementName.substring(tail(ed.getPath()).length() - 3);
            if (isPrimitive(lowFirst(t)))
              t = lowFirst(t);
          }
        }
      }
      if (!"xhtml".equals(t)) {
        for (TypeRefComponent aType: ed.getType()) {
          if (aType.getWorkingCode().equals(t)) {
            if (aType.hasProfile()) {
              assert aType.getProfile().size() == 1; 
              url = aType.getProfile().get(0).getValue();
            } else {
              url = ProfileUtilities.sdNs(t, null);
            }
            break;
          }
        }
        if (url==null) {
          throw new FHIRException("Unable to find type " + t + " for element " + elementName + " with path " + ed.getPath());
        }
        sd = context.fetchResource(StructureDefinition.class, url);        
        if (sd == null)
          throw new DefinitionException("Unable to find definition '"+url+"' for type '"+t+"' for name '"+elementName+"' on property "+definition.getPath());
        children = profileUtilities.getChildMap(sd, sd.getSnapshot().getElement().get(0), false);
      }
    }
    List<Property> properties = new ArrayList<Property>();
    for (ElementDefinition child : children.getList()) {
      properties.add(new Property(context, child, sd, this.profileUtilities, this.utils));
    }
    profileUtilities.getCachedPropertyList().put(cacheKey, properties);
    return properties;
  }

  private StructureDefinition findAncestor(String type, StructureDefinition sdt) {
    if (sdt != null) {
      StructureDefinition sd = context.fetchTypeDefinition(type);
      StructureDefinition t = sd;
      while (t != null) {
        if (t == sdt) {
          return sd; 
        }
        t = context.fetchResource(StructureDefinition.class, t.getBaseDefinition());
      }
    }
    return null;
  }


  private boolean isCDAElement(StructureDefinition sd) {
    return sd.hasUrl() && sd.getUrl().startsWith(Constants.NS_CDA_ROOT);
  }


  protected List<Property> getChildProperties(TypeDetails type) throws DefinitionException {
    ElementDefinition ed = definition;
    StructureDefinition sd = structure;
    SourcedChildDefinitions children = profileUtilities.getChildMap(sd, ed, false);
    if (children.getList().isEmpty()) {
      // ok, find the right definitions
      String t = null;
      if (ed.getType().size() == 1)
        t = ed.getType().get(0).getCode();
      else if (ed.getType().size() == 0)
        throw new Error("types == 0, and no children found");
      else {
        t = ed.getType().get(0).getCode();
        boolean all = true;
        for (TypeRefComponent tr : ed.getType()) {
          if (!tr.getCode().equals(t)) {
            all = false;
            break;
          }
        }
        if (!all) {
          // ok, it's polymorphic
          t = type.getType();
        }
      }
      if (!"xhtml".equals(t)) {
        sd = context.fetchResource(StructureDefinition.class, t);
        if (sd == null)
          throw new DefinitionException("Unable to find class '"+t+"' for name '"+ed.getPath()+"' on property "+definition.getPath());
        children = profileUtilities.getChildMap(sd, sd.getSnapshot().getElement().get(0), false);
      }
    }
    List<Property> properties = new ArrayList<Property>();
    for (ElementDefinition child : children.getList()) {
      properties.add(new Property(context, child, sd, this.profileUtilities, this.utils));
    }
    return properties;
  }

  private String tail(String path) {
    return path.contains(".") ? path.substring(path.lastIndexOf(".")+1) : path;
  }

  public Property getChild(String elementName, String childName) throws FHIRException {
    List<Property> children = getChildProperties(elementName, null);
    for (Property p : children) {
      if (p.getName().equals(childName)) {
        return p;
      }
    }
    return null;
  }

  public Property getChild(String name, TypeDetails type) throws DefinitionException {
    List<Property> children = getChildProperties(type);
    for (Property p : children) {
      if (p.getName().equals(name) || p.getName().equals(name+"[x]")) {
        return p;
      }
    }
    return null;
  }

  // matchbox-patch: handle choice types (name[x]) when looking up child by name
  public Property getChild(String name) throws FHIRException {
    List<Property> children = getChildProperties(name, null);
    for (Property p : children) {
      if (p.getName().equals(name) || p.getName().equals(name+"[x]")) {
        return p;
      }
    }
    return null;
  }

  public Property getChildSimpleName(String elementName, String name) throws FHIRException {
    List<Property> children = getChildProperties(elementName, null);
    for (Property p : children) {
      if (p.getName().equals(name) || p.getName().equals(name+"[x]")) {
        return p;
      }
    }
    return null;
  }

  public IWorkerContext getContext() {
    return context;
  }

  @Override
  public String toString() {
    return definition.getPath();
  }


  public boolean isJsonKeyArray() {
    return definition.hasExtension(ExtensionDefinitions.EXT_JSON_PROP_KEY);
  }


  public String getJsonKeyProperty() {
    return ExtensionUtilities.readStringExtension(definition, ExtensionDefinitions.EXT_JSON_PROP_KEY);
  }


  public boolean hasTypeSpecifier() {
    return definition.hasExtension(ExtensionDefinitions.EXT_TYPE_SPEC);
  }


  public List<StringPair> getTypeSpecifiers() {
    List<StringPair> res = new ArrayList<>();
    for (Extension e : definition.getExtensionsByUrl(ExtensionDefinitions.EXT_TYPE_SPEC)) {
      res.add(new StringPair(ExtensionUtilities.readStringExtension(e,  "condition"), ExtensionUtilities.readStringExtension(e,  "type")));
    }
    return res;
  }


  public Property cloneToType(StructureDefinition sd) {
    Property res = new Property(context, definition.copy(), sd);
    res.definition.getType().clear();
    res.definition.getType().add(new TypeRefComponent(sd.getUrl()));
    return res;
  }


  public boolean hasImpliedPrefix() {
    return definition.hasExtension(ExtensionDefinitions.EXT_IMPLIED_PREFIX);
  }


  public String getImpliedPrefix() {
    return ExtensionUtilities.readStringExtension(definition, ExtensionDefinitions.EXT_IMPLIED_PREFIX);
  }


  public boolean isNullable() {    
    return ExtensionUtilities.readBoolExtension(definition, ExtensionDefinitions.EXT_JSON_NULLABLE);
  }


  public String summary() {
    return structure.getUrl()+"#"+definition.getId();
  }


  public boolean canBeEmpty() {
    if (definition.hasExtension(ExtensionDefinitions.EXT_JSON_EMPTY)) {
      return !"absent".equals(ExtensionUtilities.readStringExtension(definition, ExtensionDefinitions.EXT_JSON_EMPTY));
    } else {
      return false;
    }
  }


  public boolean isLogical() {
    return structure.getKind() == StructureDefinitionKind.LOGICAL;
  }


  public ProfileUtilities getUtils() {
    return profileUtilities;
  }
  public ContextUtilities getContextUtils() {
    return utils;
  }

  public boolean isJsonPrimitiveChoice() {
    return ExtensionUtilities.readBoolExtension(definition, ExtensionDefinitions.EXT_JSON_PRIMITIVE_CHOICE);
  }

  public Object typeSummary() {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder(" | ");
    for (TypeRefComponent t : definition.getType()) {
      b.append(t.getCode());
    }
    return b.toString();
  }


  public boolean hasJsonName() {
    return definition.hasExtension(ExtensionDefinitions.EXT_JSON_NAME, ExtensionDefinitions.EXT_JSON_NAME_DEPRECATED);
  }


  public boolean isTranslatable() {
    boolean ok = ExtensionUtilities.readBoolExtension(definition, ExtensionDefinitions.EXT_TRANSLATABLE);
    if (!ok && !definition.getPath().endsWith(".id") && !definition.getPath().endsWith(".linkId") && !Utilities.existsInList(definition.getBase().getPath(), "Resource.id", "Reference.reference", "Coding.version", "Identifier.value", "SampledData.offsets", "SampledData.data", "ContactPoint.value")) {
      String t = getType();
      ok = Utilities.existsInList(t, "string", "markdown");
    }
    if (Utilities.existsInList(pathForElement(getStructure().getType(), getDefinition().getBase().getPath()), "CanonicalResource.version")) {
      return false;
    }
    return ok;
  }  


  private String pathForElement(String type, String path) {
    // special case support for metadata elements prior to R5:
    if (utils.getCanonicalResourceNames().contains(type)) {
      String fp = path.replace(type+".", "CanonicalResource.");
      if (Utilities.existsInList(fp,
         "CanonicalResource.url", "CanonicalResource.identifier", "CanonicalResource.version", "CanonicalResource.name", 
         "CanonicalResource.title", "CanonicalResource.status", "CanonicalResource.experimental", "CanonicalResource.date",
         "CanonicalResource.publisher", "CanonicalResource.contact", "CanonicalResource.description", "CanonicalResource.useContext", 
         "CanonicalResource.jurisdiction"))  {
        return fp;
      }
    }
    return path; 
  }
  
  public String getXmlTypeName() {
    TypeRefComponent tr = type;
    if (tr == null) {
      tr = definition.getTypeFirstRep();
    }
    StructureDefinition sd = context.fetchTypeDefinition(tr.getWorkingCode());
    return sd.getSnapshot().getElementFirstRep().getPath();
  }


  public boolean isReference() {
    if (type != null) {
      return isRef(type);
    }
    for (TypeRefComponent tr : definition.getType()) {
      boolean ref = isRef(tr);
      if (ref) {
        return true;
      }
    }
    return false;
  }


  private boolean isRef(TypeRefComponent tr) {
    return Utilities.existsInList(tr.getWorkingCode(), "Reference", "url", "uri", "canonical");
  }


  public boolean canBeType(String type) {
    for (TypeRefComponent tr : getDefinition().getType()) {
      if (type.equals(tr.getWorkingCode())) {
        return true;
      }
    }
    return false;
  }

  public String getExtensionStyle() {
    ElementDefinition ed = getDefinition();
    if (ed.hasExtension(ExtensionDefinitions.EXT_EXTENSION_STYLE_NEW, ExtensionDefinitions.EXT_EXTENSION_STYLE_DEPRECATED)) {
      return ed.getExtensionString(ExtensionDefinitions.EXT_EXTENSION_STYLE_NEW, ExtensionDefinitions.EXT_EXTENSION_STYLE_DEPRECATED);
    }
    if (ed.getType().size() == 1) {
      StructureDefinition sd = context.fetchTypeDefinition(ed.getTypeFirstRep().getWorkingCode());
      if (sd != null && sd.hasExtension(ExtensionDefinitions.EXT_EXTENSION_STYLE_NEW, ExtensionDefinitions.EXT_EXTENSION_STYLE_DEPRECATED)) {
        return sd.getExtensionString(ExtensionDefinitions.EXT_EXTENSION_STYLE_NEW, ExtensionDefinitions.EXT_EXTENSION_STYLE_DEPRECATED);
      }
    }
    return null;
  }
}