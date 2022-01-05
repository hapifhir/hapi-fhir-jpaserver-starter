/**
 * Copyright (c) 2022- by RALY GmbH. All rights reserved.
 */
package ch.ahdis.matchbox.mappinglanguage;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.conformance.ProfileUtilities.ProfileKnowledgeProvider;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.r5.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.r5.model.ConceptMap.TargetElementComponent;
import org.hl7.fhir.r5.model.Enumerations.ConceptMapRelationship;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.utils.structuremap.ITransformerServices;
import org.hl7.fhir.r5.utils.structuremap.SourceElementComponentWrapper;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.r5.utils.structuremap.TransformContext;

/**
 * Class to overwrite tranlation method to fix certain problems with CDA2FHIR mapping
 */
public class MatchboxStructureMapUtilities extends StructureMapUtilities {

	public MatchboxStructureMapUtilities(IWorkerContext worker) {
		super(worker);

	}

	public MatchboxStructureMapUtilities(IWorkerContext worker, ITransformerServices services,
			ProfileKnowledgeProvider pkp) {
		super(worker, services, pkp);

	}

	public MatchboxStructureMapUtilities(IWorkerContext worker, ITransformerServices services) {
		super(worker, services);

	}

	@Override
	public Base translate(TransformContext context, StructureMap map, Base source, String conceptMapUrl,
			String fieldToReturn) throws FHIRException {
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
		} else if (source.fhirType().endsWith("CE")) {// else if ("CE".equals(source.fhirType())) {
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
			String uri = getWorker().oid2Uri(src.getCode());
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
					StructureMap mapU = getWorker().fetchResource(StructureMap.class, p[0]);
					for (Resource r : mapU.getContained()) {
						if (r instanceof ConceptMap && r.getId().equals(p[1])) {
							cmap = (ConceptMap) r;
							su = conceptMapUrl;
						}
					}
				}
				if (cmap == null)
					cmap = getWorker().fetchResource(ConceptMap.class, conceptMapUrl);
			}
			Base outcome = null;
			boolean done = false;
			String message = null;
			if (cmap == null) {
				if (getServices() == null)
					message = "No map found for " + conceptMapUrl;
				else {
					outcome = getServices().translate(context.getAppInfo(), src, conceptMapUrl);
					done = true;
				}
			} else {
				List<SourceElementComponentWrapper> list = new ArrayList<SourceElementComponentWrapper>();
				for (ConceptMapGroupComponent g : cmap.getGroup()) {
					for (SourceElementComponent e : g.getElement()) {
						if (!src.hasSystem() && src.getCode().equals(e.getCode()))
							list.add(new SourceElementComponentWrapper(g, e));
						else if (src.hasSystem() && src.getSystem().equals(g.getSource())
								&& src.getCode().equals(e.getCode()))
							list.add(new SourceElementComponentWrapper(g, e));
					}
				}
				if (list.size() == 0)
					done = true;
				else if (list.get(0).getComp().getTarget().size() == 0)
					message = "Concept map " + su + " found no translation for " + src.getCode();
				else {
					for (TargetElementComponent tgt : list.get(0).getComp().getTarget()) {
						if (outcome == null && list.get(0).getComp().getTarget().size() > 1) {
							outcome = new CodeableConcept();
						}
						if (tgt.getRelationship() == null || EnumSet
								.of(ConceptMapRelationship.RELATEDTO, ConceptMapRelationship.EQUIVALENT,
										ConceptMapRelationship.SOURCEISNARROWERTHANTARGET)
								.contains(tgt.getRelationship())) {
							if (done && "code".equals(fieldToReturn)) {
								message = "Concept map " + su + " found multiple matches for " + src.getCode();
								done = false;
							} else {
								done = true;
								if (outcome instanceof CodeableConcept) {
									((CodeableConcept) outcome).addCoding(new Coding().setCode(tgt.getCode())
											.setSystem(list.get(0).getGroup().getTarget())
											.setDisplay(tgt.getDisplay()));
								} else {
									outcome = new Coding().setCode(tgt.getCode())
											.setSystem(list.get(0).getGroup().getTarget()).setDisplay(tgt.getDisplay());
								}
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
			if ("code".equals(fieldToReturn) && (outcome instanceof Coding))
				return new CodeType(((Coding) outcome).getCode());
			else if ("code".equals(fieldToReturn) && (outcome instanceof CodeableConcept)
					&& ((CodeableConcept) outcome).getCoding().size() > 0)
				return new CodeType(((CodeableConcept) outcome).getCodingFirstRep().getCode());
			else
				return outcome;
		}

	}

}
