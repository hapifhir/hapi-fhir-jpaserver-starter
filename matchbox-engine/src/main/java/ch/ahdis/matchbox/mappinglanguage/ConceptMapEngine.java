package ch.ahdis.matchbox.mappinglanguage;

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



import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.r5.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.r5.model.ConceptMap.TargetElementComponent;
import org.hl7.fhir.r5.model.Enumerations.ConceptMapRelationship;
import org.hl7.fhir.utilities.CanonicalPair;

public class ConceptMapEngine {

  private IWorkerContext context;

  public ConceptMapEngine(IWorkerContext context) {
    this.context = context;
  }

  public Coding translate(Coding source, String url) throws FHIRException {
    ConceptMap cm = context.fetchResource(ConceptMap.class, url);
    if (cm == null)
      throw new FHIRException("Unable to find ConceptMap '"+url+"'");
    if (source.hasSystem()) 
      return translateBySystem(cm, source.getSystem(), source.getCode());
    else
      return translateByJustCode(cm, source.getCode());
  }

  private Coding translateByJustCode(ConceptMap cm, String code) throws FHIRException {
    SourceElementComponent ct = null;
    ConceptMapGroupComponent cg = null;
    for (ConceptMapGroupComponent g : cm.getGroup()) {
      for (SourceElementComponent e : g.getElement()) {
        if (code.equals(e.getCode())) {
          if (e != null)
            throw new FHIRException("Unable to process translate "+code+" because multiple candidate matches were found in concept map "+cm.getUrl());
          ct = e;
          cg = g;
        }
      }
    }
    if (ct == null)
      return null;
    TargetElementComponent tt = null;
    for (TargetElementComponent t : ct.getTarget()) {
      if (!t.hasDependsOn() && !t.hasProduct() && isOkRelationship(t.getRelationship())) {
        if (tt != null)
          throw new FHIRException("Unable to process translate "+code+" because multiple targets were found in concept map "+cm.getUrl());
        tt = t;       
      }
    }
    if (tt == null)
      return null;
    CanonicalPair cp = new CanonicalPair(cg.getTarget());
    return new Coding().setSystem(cp.getUrl()).setVersion(cp.getVersion()).setCode(tt.getCode()).setDisplay(tt.getDisplay());      
  }

  private boolean isOkRelationship(ConceptMapRelationship relationship) {
    return relationship != null && relationship != ConceptMapRelationship.NOTRELATEDTO;
  }

  private Coding translateBySystem(ConceptMap cm, String system, String code) {
    throw new Error("Not done yet");
  }

}