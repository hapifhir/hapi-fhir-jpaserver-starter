package ch.ahdis.matchbox.mappinglanguage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hl7.fhir.r5.conformance.ProfileUtilities;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.StructureDefinition;

/**
 * sort function for elementmodel.Element that can deal with multiple instances of the
 * same StructureMap 
 * @author alexander kreutz
 *
 */
public class ElementModelSorter {

	public static void sort(Element el) {
		if (el.hasChildren()) {

			for (Element child : el.getChildren()) {
				sort(child);
			}
			List<Element> ch = el.getChildren();
			Collections.sort(ch, new ElementSortComparator(el, el.getProperty()));
		}
	}
}

class ElementSortComparator implements Comparator<Element> {
	private List<String> children;

	public ElementSortComparator(Element e, org.hl7.fhir.r5.elementmodel.Property property) {
		String tn = e.getType();
		StructureDefinition sd = property.getContext().fetchResource(StructureDefinition.class, ProfileUtilities.sdNs(tn, property.getContext().getOverrideVersionNs()));
		children = new ArrayList<String>();
		if (sd != null && !sd.getAbstract())
			for (ElementDefinition def : sd.getSnapshot().getElement()) {
				children.add(def.getName());
			}
		else
			for (ElementDefinition def : property.getStructure().getSnapshot().getElement()) {
				children.add(def.getName());
			}
	}

	@Override
	public int compare(Element e0, Element e1) {
		int i0 = find(e0);
		int i1 = find(e1);
		return Integer.compare(i0, i1);
	}

	private int find(Element e0) {
		int i = e0.getElementProperty() != null ? children.indexOf(e0.getElementProperty().getDefinition().getName()) : children.indexOf(e0.getProperty().getDefinition().getName());
		return i;
	}

}
