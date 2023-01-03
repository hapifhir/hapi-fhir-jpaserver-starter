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
import java.util.List;

import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.i18n.I18nConstants;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.validation.ValidationMessage.Source;

public abstract class ParserBase {

  public enum IdRenderingPolicy {
    All, None, RootOnly, NotRoot;

    boolean forRoot() {
      return this == All || this == RootOnly;
    }

    boolean forInner() {
      return this == All || this == NotRoot;
    }
  }

  public class NamedElement {
    private String name;
    private Element element;
    public NamedElement(String name, Element element) {
      super();
      this.name = name;
      this.element = element;
    }
    public String getName() {
      return name;
    }
    public Element getElement() {
      return element;
    }
    
  }

  public interface ILinkResolver {
    String resolveType(String type);
    String resolveProperty(Property property);
    String resolvePage(String string);
  }
  
  public enum ValidationPolicy { NONE, QUICK, EVERYTHING }

  public boolean isPrimitive(String code) {
    StructureDefinition sd = context.fetchTypeDefinition(code);
    if (sd != null) {
      return sd.getKind() == StructureDefinitionKind.PRIMITIVETYPE;
    }

    return Utilities.existsInList(code, "boolean", "integer", "integer64", "string", "decimal", "uri", "base64Binary", "instant", "date", "dateTime", "time", "code", "oid", "id", "markdown", "unsignedInt", "positiveInt", "uuid", "xhtml", "url", "canonical");
    
	}

	protected IWorkerContext context;
	protected ValidationPolicy policy;
  protected List<ValidationMessage> errors;
  protected ILinkResolver linkResolver;
  protected boolean showDecorations;
  protected IdRenderingPolicy idPolicy = IdRenderingPolicy.All;
  protected StructureDefinition logical;
  
	public ParserBase(IWorkerContext context) {
		super();
		this.context = context;
		policy = ValidationPolicy.NONE;
	}

	public void setupValidation(ValidationPolicy policy, List<ValidationMessage> errors) {
	  this.policy = policy;
	  this.errors = errors;
	}

  public abstract List<NamedElement> parse(InputStream stream) throws IOException, FHIRFormatError, DefinitionException, FHIRException;
  
  public Element parseSingle(InputStream stream) throws IOException, FHIRFormatError, DefinitionException, FHIRException {
    List<NamedElement> res = parse(stream);
    if (res.size() == 0) {
      throw new FHIRException("Parsing FHIR content returned no elements in a context where one element is required");
    }
    if (res.size() != 1) {
      throw new FHIRException("Parsing FHIR content returned multiple elements in a context where only one element is allowed");
    }
    return res.get(0).getElement();
  }

	public abstract void compose(Element e, OutputStream destination, OutputStyle style, String base)  throws FHIRException, IOException;

	//FIXME: i18n should be done here
	public void logError(String ruleDate, int line, int col, String path, IssueType type, String message, IssueSeverity level) throws FHIRFormatError {
	  if (errors != null) {
	    if (policy == ValidationPolicy.EVERYTHING) {
	      ValidationMessage msg = new ValidationMessage(Source.InstanceValidator, type, line, col, path, message, level);
	      msg.setRuleDate(ruleDate);
	      errors.add(msg);
	    } else if (level == IssueSeverity.FATAL || (level == IssueSeverity.ERROR && policy == ValidationPolicy.QUICK))
	      throw new FHIRFormatError(message+String.format(" at line %d col %d", line, col));
	  }
	}
	
	
	protected StructureDefinition getDefinition(int line, int col, String ns, String name) throws FHIRFormatError {
    if (ns == null) {
      logError(ValidationMessage.NO_RULE_DATE, line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS__CANNOT_BE_PARSED_AS_A_FHIR_OBJECT_NO_NAMESPACE, name), IssueSeverity.FATAL);
      return null;
    }
    if (name == null) {
      logError(ValidationMessage.NO_RULE_DATE, line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_CANNOT_BE_PARSED_AS_A_FHIR_OBJECT_NO_NAME), IssueSeverity.FATAL);
      return null;
  	}
	  for (StructureDefinition sd : new ContextUtilities(context).allStructures()) {
	    if (sd.getDerivation() == TypeDerivationRule.SPECIALIZATION && !sd.getUrl().startsWith("http://hl7.org/fhir/StructureDefinition/de-")) {
	      if(name.equals(sd.getType()) && (ns == null || ns.equals(FormatUtilities.FHIR_NS)) && !ToolingExtensions.hasExtension(sd, "http://hl7.org/fhir/StructureDefinition/elementdefinition-namespace"))
	        return sd;
	      String sns = ToolingExtensions.readStringExtension(sd, "http://hl7.org/fhir/StructureDefinition/elementdefinition-namespace");
	      if ((name.equals(sd.getType()) || name.equals(sd.getName())) && ns != null && ns.equals(sns))
	        return sd;
	    }
	  }
	  logError(ValidationMessage.NO_RULE_DATE, line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_DOES_NOT_APPEAR_TO_BE_A_FHIR_RESOURCE_UNKNOWN_NAMESPACENAME_, ns, name), IssueSeverity.FATAL);
	  return null;
  }

  protected StructureDefinition getDefinition(int line, int col, String name) throws FHIRFormatError {
    if (name == null) {
      logError(ValidationMessage.NO_RULE_DATE, line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_CANNOT_BE_PARSED_AS_A_FHIR_OBJECT_NO_NAME), IssueSeverity.FATAL);
      return null;
  	}
    // first pass: only look at base definitions
	  for (StructureDefinition sd : context.fetchResourcesByType(StructureDefinition.class)) {
	    if (sd.getUrl().equals("http://hl7.org/fhir/StructureDefinition/"+name)) {
	      new ContextUtilities(context).generateSnapshot(sd); 
	      return sd;
	    }
	  }
    for (StructureDefinition sd : context.fetchResourcesByType(StructureDefinition.class)) {
      if (name.equals(sd.getType()) && sd.getDerivation() == TypeDerivationRule.SPECIALIZATION) {
        new ContextUtilities(context).generateSnapshot(sd); 
        return sd;
      }
    }
	  logError(ValidationMessage.NO_RULE_DATE, line, col, name, IssueType.STRUCTURE, context.formatMessage(I18nConstants.THIS_DOES_NOT_APPEAR_TO_BE_A_FHIR_RESOURCE_UNKNOWN_NAME_, name), IssueSeverity.FATAL);
	  return null;
  }

  public ILinkResolver getLinkResolver() {
    return linkResolver;
  }

  public ParserBase setLinkResolver(ILinkResolver linkResolver) {
    this.linkResolver = linkResolver;
    return this;
  }

  public boolean isShowDecorations() {
    return showDecorations;
  }

  public void setShowDecorations(boolean showDecorations) {
    this.showDecorations = showDecorations;
  }

  public String getImpliedProfile() {
    return null;
  }


  public IdRenderingPolicy getIdPolicy() {
    return idPolicy;
  }

  public void setIdPolicy(IdRenderingPolicy idPolicy) {
    this.idPolicy = idPolicy;
  }

  protected boolean wantCompose(String path, Element e) {
    if (!"id".equals(e.getName())) {
      return true;
    }
    if (path!=null && path.contains(".")) {
      return idPolicy.forInner();
    } else {
      return idPolicy.forRoot();
    }
  }

  public boolean hasLogical() {
    return logical != null;
  }

  public StructureDefinition getLogical() {
    return logical;
  }

  public void setLogical(StructureDefinition logical) {
    this.logical = logical;
  }

}