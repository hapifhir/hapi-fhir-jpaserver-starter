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

import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r5.conformance.ElementRedirection;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities.AllowUnknownProfile;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.ObjectConverter;
import org.hl7.fhir.r5.elementmodel.Property;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.DataType;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.ElementDefinition.DiscriminatorType;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionBaseComponent;
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
import org.hl7.fhir.r5.model.ExpressionNode;
import org.hl7.fhir.r5.model.ExpressionNode.Kind;
import org.hl7.fhir.r5.model.ExpressionNode.Operation;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureDefinition.ExtensionContextType;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionContextComponent;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionDifferentialComponent;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionMappingComponent;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionSnapshotComponent;
import org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r5.terminologies.ValueSetExpander.ValueSetExpansionOutcome;
import org.hl7.fhir.r5.utils.FHIRPathEngine;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.r5.utils.TranslatingUtilities;
import org.hl7.fhir.r5.utils.XVerExtensionManager;
import org.hl7.fhir.r5.utils.XVerExtensionManager.XVerExtensionStatus;
import org.hl7.fhir.r5.utils.formats.CSVWriter;
import org.hl7.fhir.utilities.CommaSeparatedStringBuilder;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
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
public class ProfileUtilities extends TranslatingUtilities {

	public enum MappingMergeModeOption {
		DUPLICATE, // if there's more than one mapping for the same URI, just keep them all
		IGNORE, // if there's more than one, keep the first
		OVERWRITE, // if there's opre than, keep the last
		APPEND, // if there's more than one, append them with ';'
	}

	public enum AllowUnknownProfile {
		NONE, // exception if there's any unknown profiles (the default)
		NON_EXTNEIONS, // don't raise an exception except on Extension (because more is going on there
		ALL_TYPES // allow any unknow profile
	}

	private static final List<String> INHERITED_ED_URLS = Arrays.asList(
		"http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-binding-style",
		"http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-extension-style");

	public IWorkerContext getContext() {
		return this.context;
	}

	public static class SourcedChildDefinitions {
		private StructureDefinition source;
		private List<ElementDefinition> list;
		public SourcedChildDefinitions(StructureDefinition source, List<ElementDefinition> list) {
			super();
			this.source = source;
			this.list = list;
		}
		public StructureDefinition getSource() {
			return source;
		}
		public List<ElementDefinition> getList() {
			return list;
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

	public static final String UD_BASE_MODEL = "base.model";
	public static final String UD_BASE_PATH = "base.path";
	public static final String UD_DERIVATION_EQUALS = "derivation.equals";
	public static final String UD_DERIVATION_POINTER = "derived.pointer";
	public static final String UD_IS_DERIVED = "derived.fact";
	public static final String UD_GENERATED_IN_SNAPSHOT = "profileutilities.snapshot.processed";
	private static final boolean COPY_BINDING_EXTENSIONS = false;
	private static final boolean DONT_DO_THIS = false;

	private boolean debug;
	// note that ProfileUtilities are used re-entrantly internally, so nothing with process state can be here
	private final IWorkerContext context;
	private FHIRPathEngine fpe;
	private List<ValidationMessage> messages;
	private List<String> snapshotStack = new ArrayList<String>();
	private ProfileKnowledgeProvider pkp;
	//  private boolean igmode;
	private boolean exception;
	private ValidationOptions terminologyServiceOptions = new ValidationOptions();
	private boolean newSlicingProcessing;
	private String defWebRoot;
	private boolean autoFixSliceNames;
	private XVerExtensionManager xver;
	private boolean wantFixDifferentialFirstElementType;
	private Set<String> masterSourceFileNames;
	private Map<ElementDefinition, SourcedChildDefinitions> childMapCache = new HashMap<>();
	private AllowUnknownProfile allowUnknownProfile = AllowUnknownProfile.ALL_TYPES;
	private MappingMergeModeOption mappingMergeMode = MappingMergeModeOption.APPEND;

	public ProfileUtilities(IWorkerContext context, List<ValidationMessage> messages, ProfileKnowledgeProvider pkp, FHIRPathEngine fpe) {
		super();
		this.context = context;
		this.messages = messages;
		this.pkp = pkp;

		this.fpe = fpe;
		if (context != null && this.fpe == null) {
			this.fpe = new FHIRPathEngine(context, this);
		}
	}

	public ProfileUtilities(IWorkerContext context, List<ValidationMessage> messages, ProfileKnowledgeProvider pkp) {
		super();
		this.context = context;
		this.messages = messages;
		this.pkp = pkp;
		if (context != null) {
			this.fpe = new FHIRPathEngine(context, this);
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

	public SourcedChildDefinitions getChildMap(StructureDefinition profile, ElementDefinition element) throws DefinitionException {
		if (childMapCache.containsKey(element)) {
			return childMapCache.get(element);
		}
		StructureDefinition src = profile;
		if (element.getContentReference() != null) {
			List<ElementDefinition> list = null;
			String id = null;
			if (element.getContentReference().startsWith("#")) {
				// internal reference
				id = element.getContentReference().substring(1);
				list = profile.getSnapshot().getElement();
			} else if (element.getContentReference().contains("#")) {
				// external reference
				String ref = element.getContentReference();
				StructureDefinition sd = context.fetchResource(StructureDefinition.class, ref.substring(0, ref.indexOf("#")), profile);
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
					return getChildMap(profile, e);
			}
			throw new DefinitionException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_NAME_REFERENCE__AT_PATH_, element.getContentReference(), element.getPath()));

		} else {
			List<ElementDefinition> res = new ArrayList<ElementDefinition>();
			List<ElementDefinition> elements = profile.getSnapshot().getElement();
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
			SourcedChildDefinitions result  = new SourcedChildDefinitions(src, res);
			childMapCache.put(element, result);
			return result;
		}
	}


	public List<ElementDefinition> getSliceList(StructureDefinition profile, ElementDefinition element) throws DefinitionException {
		if (!element.hasSlicing())
			throw new Error(context.formatMessage(I18nConstants.GETSLICELIST_SHOULD_ONLY_BE_CALLED_WHEN_THE_ELEMENT_HAS_SLICING));

		List<ElementDefinition> res = new ArrayList<ElementDefinition>();
		List<ElementDefinition> elements = profile.getSnapshot().getElement();
		String path = element.getPath();
		for (int index = elements.indexOf(element) + 1; index < elements.size(); index++) {
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
			if (e.getId() == null)
				throw new Error(context.formatMessage(I18nConstants.ELEMENT_ID__NULL__ON_, e.toString(), profile.getUrl()));

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
						StructureDefinition sd = context.fetchResource(StructureDefinition.class, url, profile);
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

	private void updateMaps(StructureDefinition base, StructureDefinition derived) throws DefinitionException {
		if (base == null)
			throw new DefinitionException(context.formatMessage(I18nConstants.NO_BASE_PROFILE_PROVIDED));
		if (derived == null)
			throw new DefinitionException(context.formatMessage(I18nConstants.NO_DERIVED_STRUCTURE_PROVIDED));

		for (StructureDefinitionMappingComponent baseMap : base.getMapping()) {
			boolean found = false;
			for (StructureDefinitionMappingComponent derivedMap : derived.getMapping()) {
				if (derivedMap.getUri() != null && derivedMap.getUri().equals(baseMap.getUri())) {
					found = true;
					break;
				}
			}
			if (!found) {
				derived.getMapping().add(baseMap);
			}
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

		fixTypeOfResourceId(base);

		if (snapshotStack.contains(derived.getUrl())) {
			throw new DefinitionException(context.formatMessage(I18nConstants.CIRCULAR_SNAPSHOT_REFERENCES_DETECTED_CANNOT_GENERATE_SNAPSHOT_STACK__, snapshotStack.toString()));
		}
		derived.setUserData("profileutils.snapshot.generating", true);
		snapshotStack.add(derived.getUrl());
		try {

			if (!Utilities.noString(webUrl) && !webUrl.endsWith("/"))
				webUrl = webUrl + '/';

			if (defWebRoot == null)
				defWebRoot = webUrl;
			derived.setSnapshot(new StructureDefinitionSnapshotComponent());

			try {
				checkDifferential(derived.getDifferential().getElement(), derived.getTypeName(), derived.getUrl());
				checkDifferentialBaseType(derived);

				copyInheritedExtensions(base, derived);
				// so we have two lists - the base list, and the differential list
				// the differential list is only allowed to include things that are in the base list, but
				// is allowed to include them multiple times - thereby slicing them

				// our approach is to walk through the base list, and see whether the differential
				// says anything about them.
				// we need a diff cursor because we can only look ahead, in the bound scoped by longer paths


				for (ElementDefinition e : derived.getDifferential().getElement())
					e.clearUserData(UD_GENERATED_IN_SNAPSHOT);

				// we actually delegate the work to a subroutine so we can re-enter it with a different cursors
				StructureDefinitionDifferentialComponent diff = cloneDiff(derived.getDifferential()); // we make a copy here because we're sometimes going to hack the differential while processing it. Have to migrate user data back afterwards

				StructureDefinitionSnapshotComponent baseSnapshot  = base.getSnapshot();
				if (derived.getDerivation() == TypeDerivationRule.SPECIALIZATION) {
					String derivedType = derived.getTypeName();

					baseSnapshot = cloneSnapshot(baseSnapshot, base.getTypeName(), derivedType);
				}
				//      if (derived.getId().equals("2.16.840.1.113883.10.20.22.2.1.1")) {
				//        debug = true;
				//      }

				ProfilePathProcessor.processPaths(this, base, derived, url, webUrl, diff, baseSnapshot);

				checkGroupConstraints(derived);
				if (derived.getDerivation() == TypeDerivationRule.SPECIALIZATION) {
					for (ElementDefinition e : diff.getElement()) {
						if (!e.hasUserData(UD_GENERATED_IN_SNAPSHOT) && e.getPath().contains(".")) {
							ElementDefinition outcome = updateURLs(url, webUrl, e.copy());
							e.setUserData(UD_GENERATED_IN_SNAPSHOT, outcome);
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
				}

				if (derived.getKind() != StructureDefinitionKind.LOGICAL && !derived.getSnapshot().getElementFirstRep().getType().isEmpty())
					throw new Error(context.formatMessage(I18nConstants.TYPE_ON_FIRST_SNAPSHOT_ELEMENT_FOR__IN__FROM_, derived.getSnapshot().getElementFirstRep().getPath(), derived.getUrl(), base.getUrl()));
				updateMaps(base, derived);

				setIds(derived, false);
				if (debug) {
					System.out.println("Differential: ");
					for (ElementDefinition ed : derived.getDifferential().getElement())
						System.out.println("  "+ed.getId()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed));
					System.out.println("Snapshot: ");
					for (ElementDefinition ed : derived.getSnapshot().getElement())
						System.out.println("  "+ed.getId()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed));
					System.out.println("diff: ");
					for (ElementDefinition ed : diff.getElement())
						System.out.println("  "+ed.getId()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed)+" [gen = "+(ed.hasUserData(UD_GENERATED_IN_SNAPSHOT) ? ed.getUserData(UD_GENERATED_IN_SNAPSHOT) : "--")+"]");
				}
				CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
				//Check that all differential elements have a corresponding snapshot element
				int ce = 0;
				for (ElementDefinition e : diff.getElement()) {
					if (!e.hasUserData("diff-source"))
						throw new Error(context.formatMessage(I18nConstants.UNXPECTED_INTERNAL_CONDITION__NO_SOURCE_ON_DIFF_ELEMENT));
					else {
						if (e.hasUserData(UD_DERIVATION_EQUALS))
							((Base) e.getUserData("diff-source")).setUserData(UD_DERIVATION_EQUALS, e.getUserData(UD_DERIVATION_EQUALS));
						if (e.hasUserData(UD_DERIVATION_POINTER))
							((Base) e.getUserData("diff-source")).setUserData(UD_DERIVATION_POINTER, e.getUserData(UD_DERIVATION_POINTER));
					}
					if (!e.hasUserData(UD_GENERATED_IN_SNAPSHOT)) {
						b.append(e.hasId() ? "id: "+e.getId() : "path: "+e.getPath());
						ce++;
						if (e.hasId()) {
							String msg = "No match found in the generated snapshot: check that the path and definitions are legal in the differential (including order)";
							messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, url+"#"+e.getId(), msg, ValidationMessage.IssueSeverity.ERROR));
						}
					}
				}
				if (!Utilities.noString(b.toString())) {
					String msg = "The profile "+derived.getUrl()+" has "+ce+" "+Utilities.pluralize("element", ce)+" in the differential ("+b.toString()+") that don't have a matching element in the snapshot: check that the path and definitions are legal in the differential (including order)";
					if (debug) {
						System.out.println("Error in snapshot generation: "+msg);
						if (!debug) {
							System.out.println("Differential: ");
							for (ElementDefinition ed : derived.getDifferential().getElement())
								System.out.println("  "+ed.getId()+" = "+ed.getPath()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed));
							System.out.println("Snapshot: ");
							for (ElementDefinition ed : derived.getSnapshot().getElement())
								System.out.println("  "+ed.getId()+" = "+ed.getPath()+" : "+typeSummaryWithProfile(ed)+"["+ed.getMin()+".."+ed.getMax()+"]"+sliceSummary(ed)+"  "+constraintSummary(ed));
						}
					}
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
				// last, check for wrong profiles or target profiles
				for (ElementDefinition ed : derived.getSnapshot().getElement()) {
					for (TypeRefComponent t : ed.getType()) {
						for (UriType u : t.getProfile()) {
							StructureDefinition sd = context.fetchResource(StructureDefinition.class, u.getValue(), derived);
							if (sd == null) {
								if (xver != null && xver.matchingUrl(u.getValue()) && xver.status(u.getValue()) == XVerExtensionStatus.Valid) {
									sd = xver.makeDefinition(u.getValue());
								}
							}
							if (sd == null) {
								if (messages != null) {
									messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, url+"#"+ed.getId(), "The type of profile "+u.getValue()+" cannot be checked as the profile is not known", IssueSeverity.WARNING));
								}
							} else {
								String wt = t.getWorkingCode();
								if (ed.getPath().equals("Bundle.entry.response.outcome")) {
									wt = "OperationOutcome";
								}
								String tt = sd.getType();
								boolean elementProfile = u.hasExtension(ToolingExtensions.EXT_PROFILE_ELEMENT);
								if (elementProfile) {
									ElementDefinition edt = sd.getSnapshot().getElementById(u.getExtensionString(ToolingExtensions.EXT_PROFILE_ELEMENT));
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
				}
			} catch (Exception e) {
				// if we had an exception generating the snapshot, make sure we don't leave any half generated snapshot behind
				derived.setSnapshot(null);
				derived.clearUserData("profileutils.snapshot.generating");
				throw e;
			}
		} finally {
			derived.clearUserData("profileutils.snapshot.generating");
			snapshotStack.remove(derived.getUrl());
		}
	}

	private void handleError(String url, String msg) {
		if (exception)
			throw new DefinitionException(msg);
		else
			messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.VALUE, url, msg, ValidationMessage.IssueSeverity.ERROR));
	}




	private void copyInheritedExtensions(StructureDefinition base, StructureDefinition derived) {
		for (Extension ext : base.getExtension()) {
			if (Utilities.existsInList(ext.getUrl(), INHERITED_ED_URLS) && !derived.hasExtension(ext.getUrl())) {
				derived.getExtension().add(ext.copy());
			}
		}

	}

	private void addInheritedElementsForSpecialization(StructureDefinitionSnapshotComponent snapshot, ElementDefinition focus, String type, String path, String url, String weburl) {
		StructureDefinition sd = context.fetchTypeDefinition(type);
		if (sd != null) {
			// don't do this. should already be in snapshot ... addInheritedElementsForSpecialization(snapshot, focus, sd.getBaseDefinition(), path, url, weburl);
			for (ElementDefinition ed : sd.getSnapshot().getElement()) {
				if (ed.getPath().contains(".")) {
					ElementDefinition outcome = updateURLs(url, weburl, ed.copy());
					outcome.setPath(outcome.getPath().replace(sd.getTypeName(), path));
					snapshot.getElement().add(outcome);
				} else {
					focus.getConstraint().addAll(ed.getConstraint());
					for (Extension ext : ed.getExtension()) {
						if (Utilities.existsInList(ext.getUrl(), INHERITED_ED_URLS) && !focus.hasExtension(ext.getUrl())) {
							focus.getExtension().add(ext.copy());
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
					tr.removeExtension(ToolingExtensions.EXT_FHIR_TYPE);
					ToolingExtensions.addUrlExtension(tr, ToolingExtensions.EXT_FHIR_TYPE, "id");
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
			if (wantFixDifferentialFirstElementType && typeMatchesAncestor(derived.getDifferential().getElementFirstRep().getType(), derived.getBaseDefinition(), derived)) {
				derived.getDifferential().getElementFirstRep().getType().clear();
			} else if (derived.getKind() != StructureDefinitionKind.LOGICAL) {
				throw new Error(context.formatMessage(I18nConstants.TYPE_ON_FIRST_DIFFERENTIAL_ELEMENT));
			}
		}
	}

	private boolean typeMatchesAncestor(List<TypeRefComponent> type, String baseDefinition, Resource src) {
		StructureDefinition sd = context.fetchResource(StructureDefinition.class, baseDefinition, src);
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
//      System.out.println(children);
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
		for (ElementDefinition ed : elements) {
			if (!ed.hasPath()) {
				throw new FHIRException(context.formatMessage(I18nConstants.NO_PATH_ON_ELEMENT_IN_DIFFERENTIAL_IN_, url));
			}
			String p = ed.getPath();
			if (p == null) {
				throw new FHIRException(context.formatMessage(I18nConstants.NO_PATH_VALUE_ON_ELEMENT_IN_DIFFERENTIAL_IN_, url));
			}
			if (!((first && type.equals(p)) || p.startsWith(type+"."))) {
				throw new FHIRException(context.formatMessage(I18nConstants.ILLEGAL_PATH__IN_DIFFERENTIAL_IN__MUST_START_WITH_, p, url, type, (first ? " (or be '"+type+"')" : "")));
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
			sd = context.fetchResource(StructureDefinition.class, sd.getBaseDefinition(), sd);
		}
		return false;
	}


	private StructureDefinitionDifferentialComponent cloneDiff(StructureDefinitionDifferentialComponent source) {
		StructureDefinitionDifferentialComponent diff = new StructureDefinitionDifferentialComponent();
		for (ElementDefinition sed : source.getElement()) {
			ElementDefinition ted = sed.copy();
			diff.getElement().add(ted);
			ted.setUserData("diff-source", sed);
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
													  List<ElementDefinition> diffMatches, ElementDefinition outcome, String webUrl, Resource srcSD) {
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
		while (sd != null) {
			for (TypeRefComponent tr : types) {
				if (sd.getUrl().startsWith("http://hl7.org/fhir/StructureDefinition") && sd.getType().equals(tr.getCode())) {
					return true;
				}
				if (inner == null && sd.getUrl().equals(tr.getCode())) {
					return true;
				}
				if (inner != null) {
					ElementDefinition ed = null;
					for (ElementDefinition t : sd.getSnapshot().getElement()) {
						if (inner.equals(t.getId())) {
							ed = t;
						}
					}
					if (ed != null) {
						return isMatchingType(ed.getType(), types);
					}
				}
			}
			sd = context.fetchResource(StructureDefinition.class, sd.getBaseDefinition(), sd);
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
				System.out.println("Type error: use of a simple type \""+t.getCode()+"\" wrongly constraining "+base.getPath());
				return true;
			}
		}
		return false;
	}

	protected boolean isGenerating(StructureDefinition sd) {
		return sd.hasUserData("profileutils.snapshot.generating");
	}


	protected void checkNotGenerating(StructureDefinition sd, String role) {
		if (sd.hasUserData("profileutils.snapshot.generating")) {
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

	protected void removeStatusExtensions(ElementDefinition outcome) {
		outcome.removeExtension(ToolingExtensions.EXT_FMM_LEVEL);
		outcome.removeExtension(ToolingExtensions.EXT_FMM_SUPPORT);
		outcome.removeExtension(ToolingExtensions.EXT_FMM_DERIVED);
		outcome.removeExtension(ToolingExtensions.EXT_STANDARDS_STATUS);
		outcome.removeExtension(ToolingExtensions.EXT_NORMATIVE_VERSION);
		outcome.removeExtension(ToolingExtensions.EXT_WORKGROUP);
		outcome.removeExtension(ToolingExtensions.EXT_FMM_SUPPORT);
		outcome.removeExtension(ToolingExtensions.EXT_FMM_DERIVED);
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
			inv.setUserData(UD_IS_DERIVED, true);
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
		derived.setUserData(UD_BASE_MODEL, baseProfileUrl);
		derived.setUserData(UD_BASE_PATH, base.getPath());
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

	protected StructureDefinition getProfileForDataType(TypeRefComponent type, String webUrl, Resource src)  {
		StructureDefinition sd = null;
		if (type.hasProfile()) {
			sd = context.fetchResource(StructureDefinition.class, type.getProfile().get(0).getValue(), src);
			if (sd == null) {
				if (xver != null && xver.matchingUrl(type.getProfile().get(0).getValue()) && xver.status(type.getProfile().get(0).getValue()) == XVerExtensionStatus.Valid) {
					sd = xver.makeDefinition(type.getProfile().get(0).getValue());
					generateSnapshot(context.fetchTypeDefinition("Extension"), sd, sd.getUrl(), webUrl, sd.getName());
				}
			}
			if (sd == null) {
				if (debug) {
					System.out.println("Failed to find referenced profile: " + type.getProfile());
				}
			}

		}
		if (sd == null)
			sd = context.fetchTypeDefinition(type.getWorkingCode());
		if (sd == null)
			System.out.println("XX: failed to find profle for type: " + type.getWorkingCode()); // debug GJM
		return sd;
	}

	protected StructureDefinition getProfileForDataType(String type)  {
		StructureDefinition sd = context.fetchTypeDefinition(type);
		if (sd == null)
			System.out.println("XX: failed to find profle for type: " + type); // debug GJM
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
	public ElementDefinition updateURLs(String url, String webUrl, ElementDefinition element) {
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
					element.setDefinition(processRelativeUrls(element.getDefinition(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, null, false));
				}
				if (element.hasComment()) {
					element.setComment(processRelativeUrls(element.getComment(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, null, false));
				}
				if (element.hasRequirements()) {
					element.setRequirements(processRelativeUrls(element.getRequirements(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, null, false));
				}
				if (element.hasMeaningWhenMissing()) {
					element.setMeaningWhenMissing(processRelativeUrls(element.getMeaningWhenMissing(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, null, false));
				}
				if (element.hasBinding() && element.getBinding().hasDescription()) {
					element.getBinding().setDescription(processRelativeUrls(element.getBinding().getDescription(), webUrl, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, null, false));
				}
			}
		}
		return element;
	}

	public static String processRelativeUrls(String markdown, String webUrl, String basePath, List<String> resourceNames, Set<String> baseFilenames, Set<String> localFilenames, boolean processRelatives) {
		if (markdown == null) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		int i = 0;
		while (i < markdown.length()) {
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
						if (isLikelySourceURLReference(url, resourceNames, baseFilenames, localFilenames)) {
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
//                System.out.println("Making "+url+" relative to '"+webUrl+"'");
								b.append(webUrl);
							} else {
//                System.out.println("Not making "+url+" relative to '"+webUrl+"'");
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
			i++;
		}
		return b.toString();
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


	private static boolean isLikelySourceURLReference(String url, List<String> resourceNames, Set<String> baseFilenames, Set<String> localFilenames) {
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
		if (diff.size() != base.size())
			return false;
		for (int i = 0; i < diff.size(); i++)
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
//        System.out.println("mismatch in paths: "+statedPath +" vs " +path);
//      }
			if (ok) {
        /*
         * Commenting this out because it raises warnings when profiling inherited elements.  For example,
         * Error: unknown element 'Bundle.meta.profile' (or it is out of order) in profile ... (looking for 'Bundle.entry')
         * Not sure we have enough information here to do the check properly.  Might be better done when we're sorting the profile?

        if (i != start && result.isEmpty() && !path.startsWith(context.getElement().get(start).getPath()))
          messages.add(new ValidationMessage(Source.ProfileValidator, IssueType.VALUE, "StructureDefinition.differential.element["+Integer.toString(start)+"]", "Error: unknown element '"+context.getElement().get(start).getPath()+"' (or it is out of order) in profile '"+url+"' (looking for '"+path+"')", IssueSeverity.WARNING));

         */
				result.add(context.getElement().get(i));
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

	protected void updateFromDefinition(ElementDefinition dest, ElementDefinition source, String pn, boolean trimDifferential, String purl, StructureDefinition srcSD, StructureDefinition derivedSrc) throws DefinitionException, FHIRException {
		source.setUserData(UD_GENERATED_IN_SNAPSHOT, dest);
		// we start with a clone of the base profile ('dest') and we copy from the profile ('source')
		// over the top for anything the source has
		ElementDefinition base = dest;
		ElementDefinition derived = source;
		derived.setUserData(UD_DERIVATION_POINTER, base);
		boolean isExtension = checkExtensionDoco(base);


		for (Extension ext : source.getExtension()) {
			if (Utilities.existsInList(ext.getUrl(), INHERITED_ED_URLS) && !dest.hasExtension(ext.getUrl())) {
				dest.getExtension().add(ext.copy());
			}
		}
		// Before applying changes, apply them to what's in the profile
		StructureDefinition profile = null;
		if (base.hasSliceName())
			profile = base.getType().size() == 1 && base.getTypeFirstRep().hasProfile() ? context.fetchResource(StructureDefinition.class, base.getTypeFirstRep().getProfile().get(0).getValue(), srcSD) : null;
		if (profile==null)
			profile = source.getType().size() == 1 && source.getTypeFirstRep().hasProfile() ? context.fetchResource(StructureDefinition.class, source.getTypeFirstRep().getProfile().get(0).getValue(), derivedSrc) : null;
		if (profile != null) {
			ElementDefinition e = profile.getSnapshot().getElement().get(0);
			String webroot = profile.getUserString("webroot");

			if (e.hasDefinition()) {
				base.setDefinition(processRelativeUrls(e.getDefinition(), webroot, context.getSpecUrl(), context.getResourceNames(), masterSourceFileNames, null, true));
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
		} else if (source.getType().size() == 1 && source.getTypeFirstRep().hasProfile() && !source.getTypeFirstRep().getProfile().get(0).hasExtension(ToolingExtensions.EXT_PROFILE_ELEMENT)) {
			// todo: should we change down the profile_element if there's one?
			String type = source.getTypeFirstRep().getWorkingCode();
			if ("Extension".equals(type)) {
				System.out.println("Can't find Extension definition for "+source.getTypeFirstRep().getProfile().get(0).asStringValue()+" but trying to go on");
				if (allowUnknownProfile != AllowUnknownProfile.ALL_TYPES) {
					throw new DefinitionException("Unable to find Extension definition for "+source.getTypeFirstRep().getProfile().get(0).asStringValue());
				}
			} else {
				System.out.println("Can't find "+type+" profile "+source.getTypeFirstRep().getProfile().get(0).asStringValue()+" but trying to go on");
				if (allowUnknownProfile == AllowUnknownProfile.NONE) {
					throw new DefinitionException("Unable to find "+type+" profile "+source.getTypeFirstRep().getProfile().get(0).asStringValue());
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
					derived.getShortElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasDefinitionElement()) {
				if (derived.getDefinition().startsWith("..."))
					base.setDefinition(Utilities.appendDerivedTextToBase(base.getDefinition(), derived.getDefinition()));
				else if (!Base.compareDeep(derived.getDefinitionElement(), base.getDefinitionElement(), false))
					base.setDefinitionElement(derived.getDefinitionElement().copy());
				else if (trimDifferential)
					derived.setDefinitionElement(null);
				else if (derived.hasDefinitionElement())
					derived.getDefinitionElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasCommentElement()) {
				if (derived.getComment().startsWith("..."))
					base.setComment(Utilities.appendDerivedTextToBase(base.getComment(), derived.getComment()));
				else if (derived.hasCommentElement()!= base.hasCommentElement() || !Base.compareDeep(derived.getCommentElement(), base.getCommentElement(), false))
					base.setCommentElement(derived.getCommentElement().copy());
				else if (trimDifferential)
					base.setCommentElement(derived.getCommentElement().copy());
				else if (derived.hasCommentElement())
					derived.getCommentElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasLabelElement()) {
				if (derived.getLabel().startsWith("..."))
					base.setLabel(Utilities.appendDerivedTextToBase(base.getLabel(), derived.getLabel()));
				else if (!base.hasLabelElement() || !Base.compareDeep(derived.getLabelElement(), base.getLabelElement(), false))
					base.setLabelElement(derived.getLabelElement().copy());
				else if (trimDifferential)
					base.setLabelElement(derived.getLabelElement().copy());
				else if (derived.hasLabelElement())
					derived.getLabelElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasRequirementsElement()) {
				if (derived.getRequirements().startsWith("..."))
					base.setRequirements(Utilities.appendDerivedTextToBase(base.getRequirements(), derived.getRequirements()));
				else if (!base.hasRequirementsElement() || !Base.compareDeep(derived.getRequirementsElement(), base.getRequirementsElement(), false))
					base.setRequirementsElement(derived.getRequirementsElement().copy());
				else if (trimDifferential)
					base.setRequirementsElement(derived.getRequirementsElement().copy());
				else if (derived.hasRequirementsElement())
					derived.getRequirementsElement().setUserData(UD_DERIVATION_EQUALS, true);
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
						t.setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasMinElement()) {
				if (!Base.compareDeep(derived.getMinElement(), base.getMinElement(), false)) {
					if (derived.getMin() < base.getMin() && !derived.hasSliceName()) // in a slice, minimum cardinality rules do not apply
						messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+source.getPath(), "Element "+base.getPath()+": derived min ("+Integer.toString(derived.getMin())+") cannot be less than base min ("+Integer.toString(base.getMin())+")", ValidationMessage.IssueSeverity.ERROR));
					base.setMinElement(derived.getMinElement().copy());
				} else if (trimDifferential)
					derived.setMinElement(null);
				else
					derived.getMinElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasMaxElement()) {
				if (!Base.compareDeep(derived.getMaxElement(), base.getMaxElement(), false)) {
					if (isLargerMax(derived.getMax(), base.getMax()))
						messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+source.getPath(), "Element "+base.getPath()+": derived max ("+derived.getMax()+") cannot be greater than base max ("+base.getMax()+")", ValidationMessage.IssueSeverity.ERROR));
					base.setMaxElement(derived.getMaxElement().copy());
				} else if (trimDifferential)
					derived.setMaxElement(null);
				else
					derived.getMaxElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasFixed()) {
				if (!Base.compareDeep(derived.getFixed(), base.getFixed(), true)) {
					base.setFixed(derived.getFixed().copy());
				} else if (trimDifferential)
					derived.setFixed(null);
				else
					derived.getFixed().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasPattern()) {
				if (!Base.compareDeep(derived.getPattern(), base.getPattern(), false)) {
					base.setPattern(derived.getPattern().copy());
				} else
				if (trimDifferential)
					derived.setPattern(null);
				else
					derived.getPattern().setUserData(UD_DERIVATION_EQUALS, true);
			}

			for (ElementDefinitionExampleComponent ex : derived.getExample()) {
				boolean found = false;
				for (ElementDefinitionExampleComponent exS : base.getExample())
					if (Base.compareDeep(ex, exS, false))
						found = true;
				if (!found)
					base.addExample(ex.copy());
				else if (trimDifferential)
					derived.getExample().remove(ex);
				else
					ex.setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasMaxLengthElement()) {
				if (!Base.compareDeep(derived.getMaxLengthElement(), base.getMaxLengthElement(), false))
					base.setMaxLengthElement(derived.getMaxLengthElement().copy());
				else if (trimDifferential)
					derived.setMaxLengthElement(null);
				else
					derived.getMaxLengthElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasMaxValue()) {
				if (!Base.compareDeep(derived.getMaxValue(), base.getMaxValue(), false))
					base.setMaxValue(derived.getMaxValue().copy());
				else if (trimDifferential)
					derived.setMaxValue(null);
				else
					derived.getMaxValue().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasMinValue()) {
				if (!Base.compareDeep(derived.getMinValue(), base.getMinValue(), false))
					base.setMinValue(derived.getMinValue().copy());
				else if (trimDifferential)
					derived.setMinValue(null);
				else
					derived.getMinValue().setUserData(UD_DERIVATION_EQUALS, true);
			}

			// todo: what to do about conditions?
			// condition : id 0..*

			if (derived.hasMustSupportElement()) {
				if (!(base.hasMustSupportElement() && Base.compareDeep(derived.getMustSupportElement(), base.getMustSupportElement(), false))) {
					if (base.hasMustSupport() && base.getMustSupport() && !derived.getMustSupport()) {
						messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Illegal constraint [must-support = false] when [must-support = true] in the base profile", ValidationMessage.IssueSeverity.ERROR));
					}
					base.setMustSupportElement(derived.getMustSupportElement().copy());
				} else if (trimDifferential)
					derived.setMustSupportElement(null);
				else
					derived.getMustSupportElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasMustHaveValueElement()) {
				if (!(base.hasMustHaveValueElement() && Base.compareDeep(derived.getMustHaveValueElement(), base.getMustHaveValueElement(), false))) {
					if (base.hasMustHaveValue() && base.getMustHaveValue() && !derived.getMustHaveValue()) {
						messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Illegal constraint [must-have-value = false] when [must-have-value = true] in the base profile", ValidationMessage.IssueSeverity.ERROR));
					}
					base.setMustHaveValueElement(derived.getMustHaveValueElement().copy());
				} else if (trimDifferential)
					derived.setMustHaveValueElement(null);
				else
					derived.getMustHaveValueElement().setUserData(UD_DERIVATION_EQUALS, true);
			}


			// profiles cannot change : isModifier, defaultValue, meaningWhenMissing
			// but extensions can change isModifier
			if (isExtension) {
				if (derived.hasIsModifierElement() && !(base.hasIsModifierElement() && Base.compareDeep(derived.getIsModifierElement(), base.getIsModifierElement(), false)))
					base.setIsModifierElement(derived.getIsModifierElement().copy());
				else if (trimDifferential)
					derived.setIsModifierElement(null);
				else if (derived.hasIsModifierElement())
					derived.getIsModifierElement().setUserData(UD_DERIVATION_EQUALS, true);
				if (derived.hasIsModifierReasonElement() && !(base.hasIsModifierReasonElement() && Base.compareDeep(derived.getIsModifierReasonElement(), base.getIsModifierReasonElement(), false)))
					base.setIsModifierReasonElement(derived.getIsModifierReasonElement().copy());
				else if (trimDifferential)
					derived.setIsModifierReasonElement(null);
				else if (derived.hasIsModifierReasonElement())
					derived.getIsModifierReasonElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasBinding()) {
				if (!base.hasBinding() || !Base.compareDeep(derived.getBinding(), base.getBinding(), false)) {
					if (base.hasBinding() && base.getBinding().getStrength() == BindingStrength.REQUIRED && derived.getBinding().getStrength() != BindingStrength.REQUIRED)
						messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "illegal attempt to change the binding on "+derived.getPath()+" from "+base.getBinding().getStrength().toCode()+" to "+derived.getBinding().getStrength().toCode(), ValidationMessage.IssueSeverity.ERROR));
//            throw new DefinitionException("StructureDefinition "+pn+" at "+derived.getPath()+": illegal attempt to change a binding from "+base.getBinding().getStrength().toCode()+" to "+derived.getBinding().getStrength().toCode());
					else if (base.hasBinding() && derived.hasBinding() && base.getBinding().getStrength() == BindingStrength.REQUIRED && base.getBinding().hasValueSet() && derived.getBinding().hasValueSet()) {
						ValueSet baseVs = context.fetchResource(ValueSet.class, base.getBinding().getValueSet(), srcSD);
						ValueSet contextVs = context.fetchResource(ValueSet.class, derived.getBinding().getValueSet(), derivedSrc);
						if (baseVs == null) {
							messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+base.getPath(), "Binding "+base.getBinding().getValueSet()+" could not be located", ValidationMessage.IssueSeverity.WARNING));
						} else if (contextVs == null) {
							messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Binding "+derived.getBinding().getValueSet()+" could not be located", ValidationMessage.IssueSeverity.WARNING));
						} else {
							ValueSetExpansionOutcome expBase = context.expandVS(baseVs, true, false);
							ValueSetExpansionOutcome expDerived = context.expandVS(contextVs, true, false);
							if (expBase.getValueset() == null)
								messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+base.getPath(), "Binding "+base.getBinding().getValueSet()+" could not be expanded", ValidationMessage.IssueSeverity.WARNING));
							else if (expDerived.getValueset() == null)
								messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Binding "+derived.getBinding().getValueSet()+" could not be expanded", ValidationMessage.IssueSeverity.WARNING));
							// MATCHBOX Backported from 7166d55
							else if (ToolingExtensions.hasExtension(expBase.getValueset().getExpansion(), ToolingExtensions.EXT_EXP_TOOCOSTLY)) {
								if (ToolingExtensions.hasExtension(expDerived.getValueset().getExpansion(), ToolingExtensions.EXT_EXP_TOOCOSTLY) || expDerived.getValueset().getExpansion().getContains().size() > 100) {
									messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Unable to check if "+derived.getBinding().getValueSet()+" is a proper subset of " +base.getBinding().getValueSet()+" - base value set is too large to check", ValidationMessage.IssueSeverity.WARNING));
								} else {
									boolean ok = true;
									for (ValueSetExpansionContainsComponent cc : expDerived.getValueset().getExpansion().getContains()) {
										IWorkerContext.ValidationResult vr = context.validateCode(null, cc.getSystem(), cc.getVersion(), cc.getCode(), null, baseVs);
										if (!vr.isOk()) {
											ok = false;
											break;
										}
									}
									if (!ok) {
										messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Binding "+derived.getBinding().getValueSet()+" is not a subset of binding "+base.getBinding().getValueSet(), ValidationMessage.IssueSeverity.ERROR));
									}
								}
							} else if (!isSubset(expBase.getValueset(), expDerived.getValueset()))
								messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, pn+"."+derived.getPath(), "Binding "+derived.getBinding().getValueSet()+" is not a subset of binding "+base.getBinding().getValueSet(), ValidationMessage.IssueSeverity.ERROR));
						}
					}
					ElementDefinitionBindingComponent d = derived.getBinding();
					ElementDefinitionBindingComponent nb = base.getBinding().copy();
					if (!COPY_BINDING_EXTENSIONS) {
						nb.getExtension().clear();
					}
					nb.setDescription(null);
					nb.getExtension().addAll(d.getExtension());
					if (d.hasStrength()) {
						nb.setStrength(d.getStrength());
					}
					if (d.hasDescription()) {
						nb.setDescription(d.getDescription());
					}
					if (d.hasValueSet()) {
						nb.setValueSet(d.getValueSet());
					}
					base.setBinding(nb);
				} else if (trimDifferential)
					derived.setBinding(null);
				else
					derived.getBinding().setUserData(UD_DERIVATION_EQUALS, true);
			} // else if (base.hasBinding() && doesn't have bindable type )
			//  base

			if (derived.hasIsSummaryElement()) {
				if (!Base.compareDeep(derived.getIsSummaryElement(), base.getIsSummaryElement(), false)) {
					if (base.hasIsSummary() && !context.getVersion().equals("1.4.0")) // work around a known issue with some 1.4.0 cosntraints
						throw new Error(context.formatMessage(I18nConstants.ERROR_IN_PROFILE__AT__BASE_ISSUMMARY___DERIVED_ISSUMMARY__, purl, derived.getPath(), base.getIsSummaryElement().asStringValue(), derived.getIsSummaryElement().asStringValue()));
					base.setIsSummaryElement(derived.getIsSummaryElement().copy());
				} else if (trimDifferential)
					derived.setIsSummaryElement(null);
				else
					derived.getIsSummaryElement().setUserData(UD_DERIVATION_EQUALS, true);
			}

			if (derived.hasType()) {
				if (!Base.compareDeep(derived.getType(), base.getType(), false)) {
					if (base.hasType()) {
						for (TypeRefComponent ts : derived.getType()) {
							checkTypeDerivation(purl, derivedSrc, base, derived, ts);
						}
					}
					base.getType().clear();
					for (TypeRefComponent t : derived.getType()) {
						TypeRefComponent tt = t.copy();
//            tt.setUserData(DERIVATION_EQUALS, true);
						base.getType().add(tt);
					}
				}
				else if (trimDifferential)
					derived.getType().clear();
				else
					for (TypeRefComponent t : derived.getType())
						t.setUserData(UD_DERIVATION_EQUALS, true);
			}

			List<ElementDefinitionMappingComponent> list = new ArrayList<>();
			list.addAll(base.getMapping());
			base.getMapping().clear();
			addMappings(base.getMapping(), list);
			if (derived.hasMapping()) {
				addMappings(base.getMapping(), derived.getMapping());
			}
			for (ElementDefinitionMappingComponent m : base.getMapping()) {
				if (m.hasMap()) {
					m.setMap(m.getMap().trim());
				}
			}

			// todo: constraints are cumulative. there is no replacing
			for (ElementDefinitionConstraintComponent s : base.getConstraint()) {
				s.setUserData(UD_IS_DERIVED, true);
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

			// finally, we copy any extensions from source to dest
			for (Extension ex : derived.getExtension()) {
				StructureDefinition sd  = context.fetchResource(StructureDefinition.class, ex.getUrl(), derivedSrc);
				if (sd == null || sd.getSnapshot() == null || sd.getSnapshot().getElementFirstRep().getMax().equals("1")) {
					ToolingExtensions.removeExtension(dest, ex.getUrl());
				}
				dest.addExtension(ex.copy());
			}
		}
		if (dest.hasFixed()) {
			checkTypeOk(dest, dest.getFixed().fhirType(), srcSD, "fixed");
		}
		if (dest.hasPattern()) {
			checkTypeOk(dest, dest.getPattern().fhirType(), srcSD, "pattern");
		}
		//updateURLs(url, webUrl, dest);
	}

	private void addMappings(List<ElementDefinitionMappingComponent> destination, List<ElementDefinitionMappingComponent> source) {
		for (ElementDefinitionMappingComponent s : source) {
			boolean found = false;
			for (ElementDefinitionMappingComponent d : destination) {
				if (compareMaps(s, d)) {
					found = true;
					d.setUserData(UD_DERIVATION_EQUALS, true);
					break;
				}
			}
			if (!found) {
				destination.add(s);
			}
		}
	}

	private boolean compareMaps(ElementDefinitionMappingComponent s, ElementDefinitionMappingComponent d) {
		if (d.getIdentity().equals(s.getIdentity()) && d.getMap().equals(s.getMap())) {
			return true;
		}
		if (VersionUtilities.isR5Plus(context.getVersion())) {
			if (d.getIdentity().equals(s.getIdentity())) {
				switch (mappingMergeMode) {
					case APPEND:
						if (!Utilities.splitStrings(d.getMap(), "\\,").contains(s.getMap())) {
							d.setMap(d.getMap()+","+s.getMap());
						}
						return true;
					case DUPLICATE:
						return false;
					case IGNORE:
						d.setMap(s.getMap());
						return true;
					case OVERWRITE:
						return true;
					default:
						return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void checkTypeDerivation(String purl, StructureDefinition srcSD, ElementDefinition base, ElementDefinition derived, TypeRefComponent ts) {
		boolean ok = false;
		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
		String t = ts.getWorkingCode();
		for (TypeRefComponent td : base.getType()) {;
			String tt = td.getWorkingCode();
			b.append(tt);
			if (td.hasCode() && (tt.equals(t))) {
				ok = true;
			}
			if (!ok) {
				StructureDefinition sdt = context.fetchTypeDefinition(tt);
				if (sdt != null && (sdt.getAbstract() || sdt.getKind() == StructureDefinitionKind.LOGICAL)) {
					StructureDefinition sdb = context.fetchTypeDefinition(t);
					while (sdb != null && !ok) {
						ok = sdb.getType().equals(sdt.getType());
						sdb = context.fetchResource(StructureDefinition.class, sdb.getBaseDefinition(), sdb);
					}
				}
			}
			// work around for old badly generated SDs
			if (DONT_DO_THIS && Utilities.existsInList(tt, "Extension", "uri", "string", "Element")) {
				ok = true;
			}
			if (DONT_DO_THIS && Utilities.existsInList(tt, "Resource","DomainResource") && pkp.isResource(t)) {
				ok = true;
			}
			if (ok && ts.hasTargetProfile()) {
				// check that any derived target has a reference chain back to one of the base target profiles
				for (UriType u : ts.getTargetProfile()) {
					String url = u.getValue();
					boolean tgtOk = !td.hasTargetProfile() || td.hasTargetProfile(url);
					while (url != null && !tgtOk) {
						StructureDefinition sd = context.fetchResource(StructureDefinition.class, url);
						if (sd == null) {
							if (messages != null) {
								messages.add(new ValidationMessage(Source.InstanceValidator, IssueType.BUSINESSRULE, purl+"#"+derived.getPath(), "Cannot check whether the target profile "+url+" is valid constraint on the base because it is not known", IssueSeverity.WARNING));
							}
							url = null;
							tgtOk = true; // suppress error message
						} else {
							url = sd.getBaseDefinition();
							tgtOk = td.hasTargetProfile(url);
						}
					}
					if (!tgtOk) {
						if (messages == null) {
							throw new FHIRException(context.formatMessage(I18nConstants.ERROR_AT__THE_TARGET_PROFILE__IS_NOT__VALID_CONSTRAINT_ON_THE_BASE_, purl, derived.getPath(), url, td.getTargetProfile()));
						} else {
							messages.add(new ValidationMessage(Source.InstanceValidator, IssueType.BUSINESSRULE, derived.getPath(), "The target profile "+u.getValue()+" is not a valid constraint on the base ("+td.getTargetProfile()+") at "+derived.getPath(), IssueSeverity.ERROR));
						}
					}
				}
			}
		}
		if (!ok) {
			throw new DefinitionException(context.formatMessage(I18nConstants.STRUCTUREDEFINITION__AT__ILLEGAL_CONSTRAINED_TYPE__FROM__IN_, purl, derived.getPath(), t, b.toString(), srcSD.getUrl()));
		}
	}


	private void checkTypeOk(ElementDefinition dest, String ft, StructureDefinition sd, String fieldName) {
		boolean ok = false;
		Set<String> types = new HashSet<>();
		if (dest.getPath().contains(".")) {
			for (TypeRefComponent t : dest.getType()) {
				if (t.hasCode()) {
					types.add(t.getWorkingCode());
				}
				ok = ft.equals(t.getWorkingCode());
			}
		} else {
			types.add(sd.getType());
			ok = ft.equals(sd.getType());

		}
		if (!ok) {
			messages.add(new ValidationMessage(Source.InstanceValidator, IssueType.CONFLICT, dest.getId(), "The "+fieldName+" value has type '"+ft+"' which is not valid (valid "+Utilities.pluralize("type", dest.getType().size())+": "+types.toString()+")", IssueSeverity.ERROR));
		}
	}

	private boolean hasBindableType(ElementDefinition ed) {
		for (TypeRefComponent tr : ed.getType()) {
			if (Utilities.existsInList(tr.getWorkingCode(), "Coding", "CodeableConcept", "Quantity", "uri", "string", "code", "CodeableReference")) {
				return true;
			}
			StructureDefinition sd = context.fetchTypeDefinition(tr.getCode());
			if (sd != null && sd.hasExtension(ToolingExtensions.EXT_BINDING_STYLE)) {
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


	private boolean isSubset(ValueSet expBase, ValueSet expDerived) {
		return codesInExpansion(expDerived.getExpansion().getContains(), expBase.getExpansion());
	}


	private boolean codesInExpansion(List<ValueSetExpansionContainsComponent> contains, ValueSetExpansionComponent expansion) {
		for (ValueSetExpansionContainsComponent cc : contains) {
			if (!inExpansion(cc, expansion.getContains())) {
				return false;
			}
			if (!codesInExpansion(cc.getContains(), expansion)) {
				return false;
			}
		}
		return true;
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



	protected ElementDefinitionResolution getElementById(StructureDefinition source, List<ElementDefinition> elements, String contentReference) {
		if (!contentReference.startsWith("#") && contentReference.contains("#")) {
			String url = contentReference.substring(0, contentReference.indexOf("#"));
			contentReference = contentReference.substring(contentReference.indexOf("#"));
			if (!url.equals(source.getUrl())){
				source = context.fetchResource(StructureDefinition.class, url, source);
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


	public StructureDefinition getProfile(StructureDefinition source, String url) {
		StructureDefinition profile = null;
		String code = null;
		if (url.startsWith("#")) {
			profile = source;
			code = url.substring(1);
		} else if (context != null) {
			String[] parts = url.split("\\#");
			profile = context.fetchResource(StructureDefinition.class, parts[0], source);
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
		private List<ElementDefinition> snapshot;
		private int prefixLength;
		private String base;
		private String name;
		private String baseName;
		private Set<String> errors = new HashSet<String>();

		public ElementDefinitionComparer(boolean inExtension, List<ElementDefinition> snapshot, String base, int prefixLength, String name, String baseName) {
			this.inExtension = inExtension;
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
			if (o1.getBaseIndex() == 0)
				o1.setBaseIndex(find(o1.getSelf().getPath(), true));
			if (o2.getBaseIndex() == 0)
				o2.setBaseIndex(find(o2.getSelf().getPath(), true));
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
					errors.add("Differential contains path "+path+" which is not found in the in base "+baseName);
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
		if(!hasSlicing) {
			// if Differential does not have slicing then safe to pre-sort the list
			// so elements and subcomponents are together
			Collections.sort(diffList, new ElementNameCompare());
		}

		processElementsIntoTree(edh, i, diff.getDifferential().getElement());

		// now, we sort the siblings throughout the tree
		ElementDefinitionComparer cmp = new ElementDefinitionComparer(true, base.getSnapshot().getElement(), "", 0, name, base.getType());
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
			errors.add("The diff list size changed when sorting - was "+diffList.size()+" is now "+newDiff.size());
		} else {
			for (int i = 0; i < Integer.min(diffList.size(), newDiff.size()); i++) {
				ElementDefinition e = diffList.get(i);
				ElementDefinition n = newDiff.get(i);
				if (!n.getPath().equals(e.getPath())) {
					errors.add("The element "+e.getPath()+" is out of order (and maybe others after it)");
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
		cmp.checkForErrors(errors);

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
				StructureDefinition profile = context.fetchResource(StructureDefinition.class, child.getSelf().getType().get(0).getProfile().get(0).getValue());
				while (profile != null && profile.getDerivation() == TypeDerivationRule.CONSTRAINT) {
					profile = context.fetchResource(StructureDefinition.class, profile.getBaseDefinition());
				}
				if (profile==null) {
					ccmp = null; // this might happen before everything is loaded. And we don't so much care about sot order in this case
				} else {
					ccmp = new ElementDefinitionComparer(true, profile.getSnapshot().getElement(), profile.getType(), child.getSelf().getPath().length(), cmp.name, profile.present());
				}
			} else {
				ccmp = new ElementDefinitionComparer(true, cmp.snapshot, cmp.base, cmp.prefixLength, cmp.name, cmp.name);
			}
		} else if (ed.getType().get(0).getWorkingCode().equals("Extension") && child.getSelf().getType().size() == 1 && child.getSelf().getType().get(0).hasProfile()) {
			StructureDefinition profile = context.fetchResource(StructureDefinition.class, child.getSelf().getType().get(0).getProfile().get(0).getValue());
			if (profile==null)
				ccmp = null; // this might happen before everything is loaded. And we don't so much care about sot order in this case
			else
				ccmp = new ElementDefinitionComparer(true, profile.getSnapshot().getElement(), resolveType(ed.getType().get(0).getWorkingCode()), child.getSelf().getPath().length(), cmp.name, profile.present());
		} else if (ed.getType().size() == 1 && !ed.getType().get(0).getWorkingCode().equals("*")) {
			StructureDefinition profile = context.fetchResource(StructureDefinition.class, sdNs(ed.getType().get(0).getWorkingCode()));
			if (profile==null)
				throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_PROFILE__IN_ELEMENT_, sdNs(ed.getType().get(0).getWorkingCode()), ed.getPath()));
			ccmp = new ElementDefinitionComparer(false, profile.getSnapshot().getElement(), resolveType(ed.getType().get(0).getWorkingCode()), child.getSelf().getPath().length(), cmp.name, profile.present());
		} else if (child.getSelf().getType().size() == 1) {
			StructureDefinition profile = context.fetchResource(StructureDefinition.class, sdNs(child.getSelf().getType().get(0).getWorkingCode()));
			if (profile==null)
				throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_PROFILE__IN_ELEMENT_, sdNs(ed.getType().get(0).getWorkingCode()), ed.getPath()));
			ccmp = new ElementDefinitionComparer(false, profile.getSnapshot().getElement(), child.getSelf().getType().get(0).getWorkingCode(), child.getSelf().getPath().length(), cmp.name, profile.present());
		} else if (ed.getPath().endsWith("[x]") && !child.getSelf().getPath().endsWith("[x]")) {
			String edLastNode = ed.getPath().replaceAll("(.*\\.)*(.*)", "$2");
			String childLastNode = child.getSelf().getPath().replaceAll("(.*\\.)*(.*)", "$2");
			String p = childLastNode.substring(edLastNode.length()-3);
			if (isPrimitive(Utilities.uncapitalize(p)))
				p = Utilities.uncapitalize(p);
			StructureDefinition sd = context.fetchResource(StructureDefinition.class, sdNs(p));
			if (sd == null)
				throw new Error(context.formatMessage(I18nConstants.UNABLE_TO_FIND_PROFILE__AT_, p, ed.getId()));
			ccmp = new ElementDefinitionComparer(false, sd.getSnapshot().getElement(), p, child.getSelf().getPath().length(), cmp.name, sd.present());
		} else if (child.getSelf().hasType() && child.getSelf().getType().get(0).getWorkingCode().equals("Reference")) {
			for (TypeRefComponent t: child.getSelf().getType()) {
				if (!t.getWorkingCode().equals("Reference")) {
					throw new Error(context.formatMessage(I18nConstants.CANT_HAVE_CHILDREN_ON_AN_ELEMENT_WITH_A_POLYMORPHIC_TYPE__YOU_MUST_SLICE_AND_CONSTRAIN_THE_TYPES_FIRST_SORTELEMENTS_, ed.getPath(), typeCode(ed.getType())));
				}
			}
			StructureDefinition profile = context.fetchResource(StructureDefinition.class, sdNs(ed.getType().get(0).getWorkingCode()));
			ccmp = new ElementDefinitionComparer(false, profile.getSnapshot().getElement(), ed.getType().get(0).getWorkingCode(), child.getSelf().getPath().length(), cmp.name, profile.present());
		} else if (!child.getSelf().hasType() && ed.getType().get(0).getWorkingCode().equals("Reference")) {
			for (TypeRefComponent t: ed.getType()) {
				if (!t.getWorkingCode().equals("Reference")) {
					throw new Error(context.formatMessage(I18nConstants.NOT_HANDLED_YET_SORTELEMENTS_, ed.getPath(), typeCode(ed.getType())));
				}
			}
			StructureDefinition profile = context.fetchResource(StructureDefinition.class, sdNs(ed.getType().get(0).getWorkingCode()));
			ccmp = new ElementDefinitionComparer(false, profile.getSnapshot().getElement(), ed.getType().get(0).getWorkingCode(), child.getSelf().getPath().length(), cmp.name, profile.present());
		} else {
			// this is allowed if we only profile the extensions
			StructureDefinition profile = context.fetchResource(StructureDefinition.class, sdNs("Element"));
			if (profile==null)
				throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_PROFILE__IN_ELEMENT_, sdNs(ed.getType().get(0).getWorkingCode()), ed.getPath()));
			ccmp = new ElementDefinitionComparer(false, profile.getSnapshot().getElement(), "Element", child.getSelf().getPath().length(), cmp.name, profile.present());
//      throw new Error("Not handled yet (sortElements: "+ed.getPath()+":"+typeCode(ed.getType())+")");
		}
		return ccmp;
	}

	private String resolveType(String code) {
		if (Utilities.isAbsoluteUrl(code)) {
			StructureDefinition sd = context.fetchResource(StructureDefinition.class, code);
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

		StructureDefinition base = context.fetchResource(StructureDefinition.class, structure.getBaseDefinition(), structure);

		if (base != null) {
			SchematronWriter sch = new SchematronWriter(dest, SchematronType.PROFILE, base.getName());

			ElementDefinition ed = structure.getSnapshot().getElement().get(0);
			generateForChildren(sch, "f:"+ed.getPath(), ed, structure, base);
			sch.dump();
		}
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
				if (exception || messages == null) {
					throw new DefinitionException(context.formatMessage(I18nConstants.SAME_ID_ON_MULTIPLE_ELEMENTS__IN_, bs, idList.get(bs), ed.getPath(), name));
				} else
					messages.add(new ValidationMessage(Source.ProfileValidator, ValidationMessage.IssueType.BUSINESSRULE, name+"."+bs, "Duplicate Element id "+bs, ValidationMessage.IssueSeverity.ERROR));
			}
			idList.put(bs, ed.getPath());
			if (ed.hasContentReference() && ed.getContentReference().startsWith("#")) {
				String s = ed.getContentReference();
				String typeURL = getUrlForSource(type, srcSD);
				if (replacedIds.containsKey(s.substring(1))) {
					ed.setContentReference(typeURL+"#"+replacedIds.get(s.substring(1)));
				} else {
					ed.setContentReference(typeURL+s);
				}
			}
		}
		// second path - fix up any broken path based id references

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
				String ndx = ToolingExtensions.readStringExtension(ex, "index");
				DataType value = ToolingExtensions.getExtension(ex, "exValue").getValue();
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
		SourcedChildDefinitions children = getChildMap(profile, ed);
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
			SourcedChildDefinitions children = getChildMap(profile, ed);
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
				String ndx = ToolingExtensions.readStringExtension(ex, "index");
				Extension exv = ToolingExtensions.getExtension(ex, "exValue");
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
			StructureDefinition base = context.fetchResource(StructureDefinition.class, sd.getBaseDefinition(), sd);
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
			if (ed.hasUserData("slice-name")) {
				ed.setSliceName(ed.getUserString("slice-name"));
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
			StructureDefinition template = context.fetchResource(StructureDefinition.class, "http://fhir-registry.smarthealthit.org/StructureDefinition/capabilities");
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
		return exception;
	}


	public void setThrowException(boolean exception) {
		this.exception = exception;
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

	public static boolean isModifierExtension(StructureDefinition sd) {
		ElementDefinition defn = sd.getSnapshot().getElementByPath("Extension");
		return defn.getIsModifier();
	}


}
