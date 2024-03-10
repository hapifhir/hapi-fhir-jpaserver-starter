package ch.ahdis.matchbox.mappinglanguage;

/*
 * #%L
 * Matchbox Engine
 * %%
 * Copyright (C) 2022 ahdis
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.utilities.NamedItemList;

/**
 * sort function for elementmodel.Element that can deal with multiple instances
 * of the same StructureMap
 * 
 * @author alexander kreutz
 *
 */
public class ElementModelSorter {

	public static void sort(Element el) {
		if (el.hasChildren()) {

			for (Element child : el.getChildren()) {
				sort(child);
			}
			NamedItemList<Element> ch = el.getChildren();
			ch.sort(new ElementSortComparator(el, el.getProperty()));
		}
	}
}

class ElementSortComparator implements Comparator<Element> {
	private List<String> children;

	public ElementSortComparator(Element e, org.hl7.fhir.r5.elementmodel.Property property) {
		String tn = e.getType();
//    StructureDefinition sd = property.getContext().fetchResource(StructureDefinition.class, ProfileUtilities.sdNs(tn, property.getContext().getOverrideVersionNs()));
		StructureDefinition sd = property.getContext().fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/"+tn);
		children = new ArrayList<String>();
		if (sd != null && !sd.getAbstract())
			for (ElementDefinition def : sd.getSnapshot().getElement()) {
				children.add(def.getPath());
			}
		else
			for (ElementDefinition def : property.getStructure().getSnapshot().getElement()) {
				children.add(def.getPath());
			}
	}

	@Override
	public int compare(Element e0, Element e1) {
		int i0 = find(e0);
		int i1 = find(e1);
		return Integer.compare(i0, i1);
	}

	private int find(Element e0) {
		int i = e0.getElementProperty() != null ? children.indexOf(e0.getElementProperty().getDefinition().getPath())
				: children.indexOf(e0.getProperty().getDefinition().getPath());
		return i;
	}

}
