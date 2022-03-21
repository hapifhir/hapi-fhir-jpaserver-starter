package org.hl7.fhir.convertors.conv40_50.resources40_50;

import org.hl7.fhir.convertors.context.ConversionContext40_50;
import org.hl7.fhir.convertors.conv40_50.datatypes40_50.general40_50.CodeableConcept40_50;
import org.hl7.fhir.convertors.conv40_50.datatypes40_50.general40_50.Identifier40_50;
import org.hl7.fhir.convertors.conv40_50.datatypes40_50.metadata40_50.ContactDetail40_50;
import org.hl7.fhir.convertors.conv40_50.datatypes40_50.metadata40_50.UsageContext40_50;
import org.hl7.fhir.convertors.conv40_50.datatypes40_50.primitive40_50.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.StructureMap.StructureMapGroupTypeMode;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetParameterComponent;

import java.util.stream.Collectors;

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
// Generated on Sun, Feb 24, 2019 11:37+1100 for FHIR v4.0.0
public class StructureMap40_50 {

  public static org.hl7.fhir.r5.model.StructureMap convertStructureMap(org.hl7.fhir.r4.model.StructureMap src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r5.model.StructureMap tgt = new org.hl7.fhir.r5.model.StructureMap();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyDomainResource(src, tgt);
    if (src.hasUrl())
      tgt.setUrlElement(Uri40_50.convertUri(src.getUrlElement()));
    for (org.hl7.fhir.r4.model.Identifier t : src.getIdentifier())
      tgt.addIdentifier(Identifier40_50.convertIdentifier(t));
    if (src.hasVersion())
      tgt.setVersionElement(String40_50.convertString(src.getVersionElement()));
    if (src.hasName())
      tgt.setNameElement(String40_50.convertString(src.getNameElement()));
    if (src.hasTitle())
      tgt.setTitleElement(String40_50.convertString(src.getTitleElement()));
    if (src.hasStatus())
      tgt.setStatusElement(Enumerations40_50.convertPublicationStatus(src.getStatusElement()));
    if (src.hasExperimental())
      tgt.setExperimentalElement(Boolean40_50.convertBoolean(src.getExperimentalElement()));
    if (src.hasDate())
      tgt.setDateElement(DateTime40_50.convertDateTime(src.getDateElement()));
    if (src.hasPublisher())
      tgt.setPublisherElement(String40_50.convertString(src.getPublisherElement()));
    for (org.hl7.fhir.r4.model.ContactDetail t : src.getContact())
      tgt.addContact(ContactDetail40_50.convertContactDetail(t));
    if (src.hasDescription())
      tgt.setDescriptionElement(MarkDown40_50.convertMarkdown(src.getDescriptionElement()));
    for (org.hl7.fhir.r4.model.UsageContext t : src.getUseContext())
      tgt.addUseContext(UsageContext40_50.convertUsageContext(t));
    for (org.hl7.fhir.r4.model.CodeableConcept t : src.getJurisdiction())
      tgt.addJurisdiction(CodeableConcept40_50.convertCodeableConcept(t));
    if (src.hasPurpose())
      tgt.setPurposeElement(MarkDown40_50.convertMarkdown(src.getPurposeElement()));
    if (src.hasCopyright())
      tgt.setCopyrightElement(MarkDown40_50.convertMarkdown(src.getCopyrightElement()));
    for (org.hl7.fhir.r4.model.StructureMap.StructureMapStructureComponent t : src.getStructure())
      tgt.addStructure(convertStructureMapStructureComponent(t));
    for (org.hl7.fhir.r4.model.CanonicalType t : src.getImport())
      tgt.getImport().add(Canonical40_50.convertCanonical(t));
    for (org.hl7.fhir.r4.model.StructureMap.StructureMapGroupComponent t : src.getGroup())
      tgt.addGroup(convertStructureMapGroupComponent(t));
    return tgt;
  }

  public static org.hl7.fhir.r4.model.StructureMap convertStructureMap(org.hl7.fhir.r5.model.StructureMap src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r4.model.StructureMap tgt = new org.hl7.fhir.r4.model.StructureMap();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyDomainResource(src, tgt);
    if (src.hasUrl())
      tgt.setUrlElement(Uri40_50.convertUri(src.getUrlElement()));
    for (org.hl7.fhir.r5.model.Identifier t : src.getIdentifier())
      tgt.addIdentifier(Identifier40_50.convertIdentifier(t));
    if (src.hasVersion())
      tgt.setVersionElement(String40_50.convertString(src.getVersionElement()));
    if (src.hasName())
      tgt.setNameElement(String40_50.convertString(src.getNameElement()));
    if (src.hasTitle())
      tgt.setTitleElement(String40_50.convertString(src.getTitleElement()));
    if (src.hasStatus())
      tgt.setStatusElement(Enumerations40_50.convertPublicationStatus(src.getStatusElement()));
    if (src.hasExperimental())
      tgt.setExperimentalElement(Boolean40_50.convertBoolean(src.getExperimentalElement()));
    if (src.hasDate())
      tgt.setDateElement(DateTime40_50.convertDateTime(src.getDateElement()));
    if (src.hasPublisher())
      tgt.setPublisherElement(String40_50.convertString(src.getPublisherElement()));
    for (org.hl7.fhir.r5.model.ContactDetail t : src.getContact())
      tgt.addContact(ContactDetail40_50.convertContactDetail(t));
    if (src.hasDescription())
      tgt.setDescriptionElement(MarkDown40_50.convertMarkdown(src.getDescriptionElement()));
    for (org.hl7.fhir.r5.model.UsageContext t : src.getUseContext())
      tgt.addUseContext(UsageContext40_50.convertUsageContext(t));
    for (org.hl7.fhir.r5.model.CodeableConcept t : src.getJurisdiction())
      tgt.addJurisdiction(CodeableConcept40_50.convertCodeableConcept(t));
    if (src.hasPurpose())
      tgt.setPurposeElement(MarkDown40_50.convertMarkdown(src.getPurposeElement()));
    if (src.hasCopyright())
      tgt.setCopyrightElement(MarkDown40_50.convertMarkdown(src.getCopyrightElement()));
    for (org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent t : src.getStructure())
      tgt.addStructure(convertStructureMapStructureComponent(t));
    for (org.hl7.fhir.r5.model.CanonicalType t : src.getImport())
      tgt.getImport().add(Canonical40_50.convertCanonical(t));
    for (org.hl7.fhir.r5.model.StructureMap.StructureMapGroupComponent t : src.getGroup())
      tgt.addGroup(convertStructureMapGroupComponent(t));
    return tgt;
  }

  public static org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent convertStructureMapStructureComponent(org.hl7.fhir.r4.model.StructureMap.StructureMapStructureComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent tgt = new org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasUrl())
      tgt.setUrlElement(Canonical40_50.convertCanonical(src.getUrlElement()));
    if (src.hasMode())
      tgt.setModeElement(convertStructureMapModelMode(src.getModeElement()));
    if (src.hasAlias())
      tgt.setAliasElement(String40_50.convertString(src.getAliasElement()));
    if (src.hasDocumentation())
      tgt.setDocumentationElement(String40_50.convertString(src.getDocumentationElement()));
    return tgt;
  }

  public static org.hl7.fhir.r4.model.StructureMap.StructureMapStructureComponent convertStructureMapStructureComponent(org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r4.model.StructureMap.StructureMapStructureComponent tgt = new org.hl7.fhir.r4.model.StructureMap.StructureMapStructureComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasUrl())
      tgt.setUrlElement(Canonical40_50.convertCanonical(src.getUrlElement()));
    if (src.hasMode())
      tgt.setModeElement(convertStructureMapModelMode(src.getModeElement()));
    if (src.hasAlias())
      tgt.setAliasElement(String40_50.convertString(src.getAliasElement()));
    if (src.hasDocumentation())
      tgt.setDocumentationElement(String40_50.convertString(src.getDocumentationElement()));
    return tgt;
  }

  static public org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode> convertStructureMapModelMode(org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapModelMode> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode> tgt = new org.hl7.fhir.r5.model.Enumeration<>(new org.hl7.fhir.r5.model.StructureMap.StructureMapModelModeEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case SOURCE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode.SOURCE);
        break;
      case QUERIED:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode.QUERIED);
        break;
      case TARGET:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode.TARGET);
        break;
      case PRODUCED:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode.PRODUCED);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode.NULL);
        break;
    }
    return tgt;
  }

  static public org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapModelMode> convertStructureMapModelMode(org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapModelMode> tgt = new org.hl7.fhir.r4.model.Enumeration<>(new org.hl7.fhir.r4.model.StructureMap.StructureMapModelModeEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case SOURCE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapModelMode.SOURCE);
        break;
      case QUERIED:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapModelMode.QUERIED);
        break;
      case TARGET:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapModelMode.TARGET);
        break;
      case PRODUCED:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapModelMode.PRODUCED);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapModelMode.NULL);
        break;
    }
    return tgt;
  }

  public static org.hl7.fhir.r5.model.StructureMap.StructureMapGroupComponent convertStructureMapGroupComponent(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r5.model.StructureMap.StructureMapGroupComponent tgt = new org.hl7.fhir.r5.model.StructureMap.StructureMapGroupComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasName())
      tgt.setNameElement(Id40_50.convertId(src.getNameElement()));
    if (src.hasExtends())
      tgt.setExtendsElement(Id40_50.convertId(src.getExtendsElement()));
    if (src.hasTypeMode())
      tgt.setTypeModeElement(convertStructureMapGroupTypeMode(src.getTypeModeElement()));
    if (src.hasDocumentation())
      tgt.setDocumentationElement(String40_50.convertString(src.getDocumentationElement()));
    for (org.hl7.fhir.r4.model.StructureMap.StructureMapGroupInputComponent t : src.getInput())
      tgt.addInput(convertStructureMapGroupInputComponent(t));
    for (org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleComponent t : src.getRule())
      tgt.addRule(convertStructureMapGroupRuleComponent(t));
    return tgt;
  }

  public static org.hl7.fhir.r4.model.StructureMap.StructureMapGroupComponent convertStructureMapGroupComponent(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r4.model.StructureMap.StructureMapGroupComponent tgt = new org.hl7.fhir.r4.model.StructureMap.StructureMapGroupComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasName())
      tgt.setNameElement(Id40_50.convertId(src.getNameElement()));
    if (src.hasExtends())
      tgt.setExtendsElement(Id40_50.convertId(src.getExtendsElement()));
    if (src.hasTypeMode()) {
      tgt.setTypeModeElement(convertStructureMapGroupTypeMode(src.getTypeModeElement()));
    } else {
      tgt.setTypeMode(StructureMapGroupTypeMode.NONE);
    }
    if (src.hasDocumentation())
      tgt.setDocumentationElement(String40_50.convertString(src.getDocumentationElement()));
    for (org.hl7.fhir.r5.model.StructureMap.StructureMapGroupInputComponent t : src.getInput())
      tgt.addInput(convertStructureMapGroupInputComponent(t));
    for (org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleComponent t : src.getRule())
      tgt.addRule(convertStructureMapGroupRuleComponent(t));
    return tgt;
  }

  static public org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapGroupTypeMode> convertStructureMapGroupTypeMode(org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapGroupTypeMode> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapGroupTypeMode> tgt = new org.hl7.fhir.r5.model.Enumeration<>(new org.hl7.fhir.r5.model.StructureMap.StructureMapGroupTypeModeEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case NONE:
        return null;
      case TYPES:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupTypeMode.TYPES);
        break;
      case TYPEANDTYPES:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupTypeMode.TYPEANDTYPES);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupTypeMode.NULL);
        break;
    }
    return tgt;
  }

  static public org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapGroupTypeMode> convertStructureMapGroupTypeMode(org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapGroupTypeMode> src) throws FHIRException {
    org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapGroupTypeMode> tgt = new org.hl7.fhir.r4.model.Enumeration<>(new org.hl7.fhir.r4.model.StructureMap.StructureMapGroupTypeModeEnumFactory());
    if (src == null || src.isEmpty()) {
      tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupTypeMode.NONE);
      return tgt;
    }
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case TYPES:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupTypeMode.TYPES);
        break;
      case TYPEANDTYPES:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupTypeMode.TYPEANDTYPES);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupTypeMode.NULL);
        break;
    }
    return tgt;
  }

  public static org.hl7.fhir.r5.model.StructureMap.StructureMapGroupInputComponent convertStructureMapGroupInputComponent(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupInputComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r5.model.StructureMap.StructureMapGroupInputComponent tgt = new org.hl7.fhir.r5.model.StructureMap.StructureMapGroupInputComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasName())
      tgt.setNameElement(Id40_50.convertId(src.getNameElement()));
    if (src.hasType())
      tgt.setTypeElement(String40_50.convertString(src.getTypeElement()));
    if (src.hasMode())
      tgt.setModeElement(convertStructureMapInputMode(src.getModeElement()));
    if (src.hasDocumentation())
      tgt.setDocumentationElement(String40_50.convertString(src.getDocumentationElement()));
    return tgt;
  }

  public static org.hl7.fhir.r4.model.StructureMap.StructureMapGroupInputComponent convertStructureMapGroupInputComponent(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupInputComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r4.model.StructureMap.StructureMapGroupInputComponent tgt = new org.hl7.fhir.r4.model.StructureMap.StructureMapGroupInputComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasName())
      tgt.setNameElement(Id40_50.convertId(src.getNameElement()));
    if (src.hasType())
      tgt.setTypeElement(String40_50.convertString(src.getTypeElement()));
    if (src.hasMode())
      tgt.setModeElement(convertStructureMapInputMode(src.getModeElement()));
    if (src.hasDocumentation())
      tgt.setDocumentationElement(String40_50.convertString(src.getDocumentationElement()));
    return tgt;
  }

  static public org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapInputMode> convertStructureMapInputMode(org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapInputMode> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapInputMode> tgt = new org.hl7.fhir.r5.model.Enumeration<>(new org.hl7.fhir.r5.model.StructureMap.StructureMapInputModeEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case SOURCE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapInputMode.SOURCE);
        break;
      case TARGET:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapInputMode.TARGET);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapInputMode.NULL);
        break;
    }
    return tgt;
  }

  static public org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapInputMode> convertStructureMapInputMode(org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapInputMode> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapInputMode> tgt = new org.hl7.fhir.r4.model.Enumeration<>(new org.hl7.fhir.r4.model.StructureMap.StructureMapInputModeEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case SOURCE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapInputMode.SOURCE);
        break;
      case TARGET:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapInputMode.TARGET);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapInputMode.NULL);
        break;
    }
    return tgt;
  }

  public static org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleComponent convertStructureMapGroupRuleComponent(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleComponent tgt = new org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasName())
      tgt.setNameElement(Id40_50.convertId(src.getNameElement()));
    for (org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleSourceComponent t : src.getSource())
      tgt.addSource(convertStructureMapGroupRuleSourceComponent(t));
    for (org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetComponent t : src.getTarget())
      tgt.addTarget(convertStructureMapGroupRuleTargetComponent(t));
    for (org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleComponent t : src.getRule())
      tgt.addRule(convertStructureMapGroupRuleComponent(t));
    for (org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleDependentComponent t : src.getDependent())
      tgt.addDependent(convertStructureMapGroupRuleDependentComponent(t));
    if (src.hasDocumentation())
      tgt.setDocumentationElement(String40_50.convertString(src.getDocumentationElement()));
    return tgt;
  }

  public static org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleComponent convertStructureMapGroupRuleComponent(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleComponent tgt = new org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasName())
      tgt.setNameElement(Id40_50.convertId(src.getNameElement()));
    for (org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleSourceComponent t : src.getSource())
      tgt.addSource(convertStructureMapGroupRuleSourceComponent(t));
    for (org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetComponent t : src.getTarget())
      tgt.addTarget(convertStructureMapGroupRuleTargetComponent(t));
    for (org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleComponent t : src.getRule())
      tgt.addRule(convertStructureMapGroupRuleComponent(t));
    for (org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleDependentComponent t : src.getDependent())
      tgt.addDependent(convertStructureMapGroupRuleDependentComponent(t));
    if (src.hasDocumentation())
      tgt.setDocumentationElement(String40_50.convertString(src.getDocumentationElement()));
    return tgt;
  }

  public static org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleSourceComponent convertStructureMapGroupRuleSourceComponent(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleSourceComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleSourceComponent tgt = new org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleSourceComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasContext())
      tgt.setContextElement(Id40_50.convertId(src.getContextElement()));
    if (src.hasMin())
      tgt.setMinElement(Integer40_50.convertInteger(src.getMinElement()));
    if (src.hasMax())
      tgt.setMaxElement(String40_50.convertString(src.getMaxElement()));
    if (src.hasType())
      tgt.setTypeElement(String40_50.convertString(src.getTypeElement()));
    if (src.hasDefaultValue())
      tgt.setDefaultValueElement((StringType) ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().convertType(src.getDefaultValue()));
    if (src.hasElement())
      tgt.setElementElement(String40_50.convertString(src.getElementElement()));
    if (src.hasListMode())
      tgt.setListModeElement(convertStructureMapSourceListMode(src.getListModeElement()));
    if (src.hasVariable())
      tgt.setVariableElement(Id40_50.convertId(src.getVariableElement()));
    if (src.hasCondition())
      tgt.setConditionElement(String40_50.convertString(src.getConditionElement()));
    if (src.hasCheck())
      tgt.setCheckElement(String40_50.convertString(src.getCheckElement()));
    if (src.hasLogMessage())
      tgt.setLogMessageElement(String40_50.convertString(src.getLogMessageElement()));
    return tgt;
  }

  public static org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleSourceComponent convertStructureMapGroupRuleSourceComponent(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleSourceComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleSourceComponent tgt = new org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleSourceComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasContext())
      tgt.setContextElement(Id40_50.convertId(src.getContextElement()));
    if (src.hasMin())
      tgt.setMinElement(Integer40_50.convertInteger(src.getMinElement()));
    if (src.hasMax())
      tgt.setMaxElement(String40_50.convertString(src.getMaxElement()));
    if (src.hasType())
      tgt.setTypeElement(String40_50.convertString(src.getTypeElement()));
    if (src.hasDefaultValue())
      tgt.setDefaultValue(ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().convertType(src.getDefaultValueElement()));
    if (src.hasElement())
      tgt.setElementElement(String40_50.convertString(src.getElementElement()));
    if (src.hasListMode())
      tgt.setListModeElement(convertStructureMapSourceListMode(src.getListModeElement()));
    if (src.hasVariable())
      tgt.setVariableElement(Id40_50.convertId(src.getVariableElement()));
    if (src.hasCondition())
      tgt.setConditionElement(String40_50.convertString(src.getConditionElement()));
    if (src.hasCheck())
      tgt.setCheckElement(String40_50.convertString(src.getCheckElement()));
    if (src.hasLogMessage())
      tgt.setLogMessageElement(String40_50.convertString(src.getLogMessageElement()));
    return tgt;
  }

  static public org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListMode> convertStructureMapSourceListMode(org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListMode> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListMode> tgt = new org.hl7.fhir.r5.model.Enumeration<>(new org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListModeEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case FIRST:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListMode.FIRST);
        break;
      case NOTFIRST:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListMode.NOTFIRST);
        break;
      case LAST:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListMode.LAST);
        break;
      case NOTLAST:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListMode.NOTLAST);
        break;
      case ONLYONE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListMode.ONLYONE);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListMode.NULL);
        break;
    }
    return tgt;
  }

  static public org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListMode> convertStructureMapSourceListMode(org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapSourceListMode> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListMode> tgt = new org.hl7.fhir.r4.model.Enumeration<>(new org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListModeEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case FIRST:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListMode.FIRST);
        break;
      case NOTFIRST:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListMode.NOTFIRST);
        break;
      case LAST:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListMode.LAST);
        break;
      case NOTLAST:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListMode.NOTLAST);
        break;
      case ONLYONE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListMode.ONLYONE);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapSourceListMode.NULL);
        break;
    }
    return tgt;
  }

  public static org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetComponent convertStructureMapGroupRuleTargetComponent(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetComponent tgt = new org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasContext())
      tgt.setContextElement(Id40_50.convertId(src.getContextElement()));
    if (src.hasContextType() && src.getContextType() != org.hl7.fhir.r4.model.StructureMap.StructureMapContextType.VARIABLE)
      throw new Error("This conversion is not supported. Consult code maintainers"); // this should never happens - no one knows what the intent was here.
    if (src.hasElement())
      tgt.setElementElement(String40_50.convertString(src.getElementElement()));
    if (src.hasVariable())
      tgt.setVariableElement(Id40_50.convertId(src.getVariableElement()));
    tgt.setListMode(src.getListMode().stream()
      .map(StructureMap40_50::convertStructureMapTargetListMode)
      .collect(Collectors.toList()));
    if (src.hasListRuleId())
      tgt.setListRuleIdElement(Id40_50.convertId(src.getListRuleIdElement()));
    if (src.hasTransform())
      tgt.setTransformElement(convertStructureMapTransform(src.getTransformElement()));
    for (org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetParameterComponent t : src.getParameter())
      tgt.addParameter(convertStructureMapGroupRuleTargetParameterComponent(t));
    return tgt;
  }

  public static org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetComponent convertStructureMapGroupRuleTargetComponent(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetComponent tgt = new org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasContext())
      tgt.setContextElement(Id40_50.convertId(src.getContextElement()));
    tgt.setContextType(org.hl7.fhir.r4.model.StructureMap.StructureMapContextType.VARIABLE);
    if (src.hasElement())
      tgt.setElementElement(String40_50.convertString(src.getElementElement()));
    if (src.hasVariable())
      tgt.setVariableElement(Id40_50.convertId(src.getVariableElement()));
    tgt.setListMode(src.getListMode().stream()
      .map(StructureMap40_50::convertStructureMapTargetListMode)
      .collect(Collectors.toList()));
    if (src.hasListRuleId())
      tgt.setListRuleIdElement(Id40_50.convertId(src.getListRuleIdElement()));
    if (src.hasTransform())
      tgt.setTransformElement(convertStructureMapTransform(src.getTransformElement()));
    for (org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetParameterComponent t : src.getParameter())
      tgt.addParameter(convertStructureMapGroupRuleTargetParameterComponent(t));
    return tgt;
  }

  static public org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapTargetListMode> convertStructureMapTargetListMode(org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapTargetListMode> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapTargetListMode> tgt = new org.hl7.fhir.r5.model.Enumeration<>(new org.hl7.fhir.r5.model.StructureMap.StructureMapTargetListModeEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case FIRST:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTargetListMode.FIRST);
        break;
      case SHARE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTargetListMode.SHARE);
        break;
      case LAST:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTargetListMode.LAST);
        break;
      case COLLATE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTargetListMode.COLLATE);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTargetListMode.NULL);
        break;
    }
    return tgt;
  }

  static public org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapTargetListMode> convertStructureMapTargetListMode(org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapTargetListMode> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapTargetListMode> tgt = new org.hl7.fhir.r4.model.Enumeration<>(new org.hl7.fhir.r4.model.StructureMap.StructureMapTargetListModeEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case FIRST:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTargetListMode.FIRST);
        break;
      case SHARE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTargetListMode.SHARE);
        break;
      case LAST:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTargetListMode.LAST);
        break;
      case COLLATE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTargetListMode.COLLATE);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTargetListMode.NULL);
        break;
    }
    return tgt;
  }

  static public org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapTransform> convertStructureMapTransform(org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapTransform> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapTransform> tgt = new org.hl7.fhir.r5.model.Enumeration<>(new org.hl7.fhir.r5.model.StructureMap.StructureMapTransformEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case CREATE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.CREATE);
        break;
      case COPY:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.COPY);
        break;
      case TRUNCATE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.TRUNCATE);
        break;
      case ESCAPE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.ESCAPE);
        break;
      case CAST:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.CAST);
        break;
      case APPEND:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.APPEND);
        break;
      case TRANSLATE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.TRANSLATE);
        break;
      case REFERENCE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.REFERENCE);
        break;
      case DATEOP:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.DATEOP);
        break;
      case UUID:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.UUID);
        break;
      case POINTER:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.POINTER);
        break;
      case EVALUATE:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.EVALUATE);
        break;
      case CC:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.CC);
        break;
      case C:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.C);
        break;
      case QTY:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.QTY);
        break;
      case ID:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.ID);
        break;
      case CP:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.CP);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r5.model.StructureMap.StructureMapTransform.NULL);
        break;
    }
    return tgt;
  }

  static public org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapTransform> convertStructureMapTransform(org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.StructureMap.StructureMapTransform> src) throws FHIRException {
    if (src == null || src.isEmpty())
      return null;
    org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.StructureMap.StructureMapTransform> tgt = new org.hl7.fhir.r4.model.Enumeration<>(new org.hl7.fhir.r4.model.StructureMap.StructureMapTransformEnumFactory());
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    switch (src.getValue()) {
      case CREATE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.CREATE);
        break;
      case COPY:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.COPY);
        break;
      case TRUNCATE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.TRUNCATE);
        break;
      case ESCAPE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.ESCAPE);
        break;
      case CAST:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.CAST);
        break;
      case APPEND:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.APPEND);
        break;
      case TRANSLATE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.TRANSLATE);
        break;
      case REFERENCE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.REFERENCE);
        break;
      case DATEOP:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.DATEOP);
        break;
      case UUID:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.UUID);
        break;
      case POINTER:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.POINTER);
        break;
      case EVALUATE:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.EVALUATE);
        break;
      case CC:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.CC);
        break;
      case C:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.C);
        break;
      case QTY:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.QTY);
        break;
      case ID:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.ID);
        break;
      case CP:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.CP);
        break;
      default:
        tgt.setValue(org.hl7.fhir.r4.model.StructureMap.StructureMapTransform.NULL);
        break;
    }
    return tgt;
  }

  public static org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetParameterComponent convertStructureMapGroupRuleTargetParameterComponent(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetParameterComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetParameterComponent tgt = new org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetParameterComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasValue())
      tgt.setValue(ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().convertType(src.getValue()));
    return tgt;
  }

  public static org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetParameterComponent convertStructureMapGroupRuleTargetParameterComponent(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleTargetParameterComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetParameterComponent tgt = new org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetParameterComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasValue())
      tgt.setValue(ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().convertType(src.getValue()));
    return tgt;
  }

  public static org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleDependentComponent convertStructureMapGroupRuleDependentComponent(org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleDependentComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleDependentComponent tgt = new org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleDependentComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasName())
      tgt.setNameElement(Id40_50.convertId(src.getNameElement()));
    for (org.hl7.fhir.r4.model.StringType t : src.getVariable()) tgt.addParameter().setValue(String40_50.convertString(t));
    return tgt;
  }

  public static org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleDependentComponent convertStructureMapGroupRuleDependentComponent(org.hl7.fhir.r5.model.StructureMap.StructureMapGroupRuleDependentComponent src) throws FHIRException {
    if (src == null)
      return null;
    org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleDependentComponent tgt = new org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleDependentComponent();
    ConversionContext40_50.INSTANCE.getVersionConvertor_40_50().copyElement(src, tgt);
    if (src.hasName())
      tgt.setNameElement(Id40_50.convertId(src.getNameElement()));
    for (StructureMapGroupRuleTargetParameterComponent t : src.getParameter()) tgt.getVariable().add(new org.hl7.fhir.r4.model.StringType(t.getValue().primitiveValue()));
    return tgt;
  }
}