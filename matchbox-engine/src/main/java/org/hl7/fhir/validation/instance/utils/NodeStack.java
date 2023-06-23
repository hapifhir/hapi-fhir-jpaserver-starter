package org.hl7.fhir.validation.instance.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.utilities.Utilities;

public class NodeStack {

	protected IWorkerContext context;
	private ElementDefinition definition;
	private Element element;
	private ElementDefinition extension;
	private String literalPath; // xpath format
	private List<String> logicalPaths; // dotted format, various entry points
	private NodeStack parent;
	private ElementDefinition type;
	private String workingLang;
	private Map<String, Element> ids;
	private boolean resetPoint = false;
	private boolean contained = false;

	public NodeStack(IWorkerContext context) {
		this.context = context;
	}

	public NodeStack(IWorkerContext context, String initialPath, Element element, String validationLanguage) {
		this.context = context;
		ids = new HashMap<>();
		this.element = element;
		literalPath = (initialPath == null ? "" : initialPath+".") + element.getPath();
		workingLang = validationLanguage;
		if (!element.getName().equals(element.fhirType())) {
			logicalPaths = new ArrayList<>();
			logicalPaths.add(element.fhirType());
		}
	}

	public NodeStack(IWorkerContext context, Element element, String refPath, String validationLanguage) {
		this.context = context;
		ids = new HashMap<>();
		this.element = element;
		literalPath = refPath + "->" + element.getName();
		workingLang = validationLanguage;
	}

	public String addToLiteralPath(String... path) {
		StringBuilder b = new StringBuilder();
		b.append(getLiteralPath());
		for (String p : path) {
			if (p.startsWith(":")) {
				b.append("[");
				b.append(p.substring(1));
				b.append("]");
			} else {
				b.append(".");
				b.append(p);
			}
		}
		return b.toString();
	}

	private ElementDefinition getDefinition() {
		return definition;
	}

	public Element getElement() {
		return element;
	}

	public String getLiteralPath() {
		return literalPath == null ? "" : literalPath;
	}

	public List<String> getLogicalPaths() {
		return logicalPaths == null ? new ArrayList<String>() : logicalPaths;
	}

	private ElementDefinition getType() {
		return type;
	}

	public NodeStack pushTarget(Element element, int count, ElementDefinition definition, ElementDefinition type) {
		return pushInternal(element, count, definition, type, "->");
	}

	public NodeStack push(Element element, int count, ElementDefinition definition, ElementDefinition type) {
		return pushInternal(element, count, definition, type, ".");
	}

	private NodeStack pushInternal(Element element, int count, ElementDefinition definition, ElementDefinition type, String sep) {
		NodeStack res = new NodeStack(context);
		res.ids = ids;
		res.parent = this;
		res.workingLang = this.workingLang;
		res.element = element;
		res.definition = definition;
		res.contained = contained;
		res.literalPath = getLiteralPath() + sep + element.getName();
		if (count > -1)
			res.literalPath = res.literalPath + "[" + Integer.toString(count) + "]";
		else if (element.getSpecial() == null && element.getProperty().isList())
			res.literalPath = res.literalPath + "[0]";
		else if (element.getProperty().isChoice()) {
			String n = res.literalPath.substring(res.literalPath.lastIndexOf(".") + 1);
			String en = element.getProperty().getName();
			if (en.endsWith("[x]")) {
				en = en.substring(0, en.length() - 3);
				String t = n.substring(en.length());
				if (isPrimitiveType(Utilities.uncapitalize(t)))
					t = Utilities.uncapitalize(t);
				res.literalPath = res.literalPath.substring(0, res.literalPath.lastIndexOf(".")) + "." + en + ".ofType(" + t + ")";
			} else {
				res.literalPath = res.literalPath.substring(0, res.literalPath.lastIndexOf(".")) + "." + en;;
			}
		}
		res.logicalPaths = new ArrayList<String>();
		if (type != null) {
			// type will be null if we on a stitching point of a contained resource, or if....
			res.type = type;
			String tn = res.type.getPath();
			String t = tail(definition.getPath());
			if ("Resource".equals(tn)) {
				tn = element.fhirType();
			}
			for (String lp : getLogicalPaths()) {
				res.logicalPaths.add(lp + "." + t);
				if (t.endsWith("[x]")) {
					res.logicalPaths.add(lp + "." + t.substring(0, t.length() - 3) + ".ofType("+type.getPath()+")");
					res.logicalPaths.add(lp + "." + t.substring(0, t.length() - 3) + type.getPath());
				}
			}
			res.logicalPaths.add(tn);
		} else if (definition != null) {
			for (String lp : getLogicalPaths()) {
				res.logicalPaths.add(lp + "." + element.getName());
			}
			res.logicalPaths.add(definition.typeSummary());
		} else
			res.logicalPaths.addAll(getLogicalPaths());
		return res;
	}

	private void setType(ElementDefinition type) {
		this.type = type;
	}

	public NodeStack resetIds() {
		ids = new HashMap<>();
		resetPoint  = true;
		return this;
	}
	public Map<String, Element> getIds() {
		return ids;
	}
	private String tail(String path) {
		return path.substring(path.lastIndexOf(".") + 1);
	}

	public boolean isPrimitiveType(String code) {
		StructureDefinition sd = context.fetchTypeDefinition(code);
		return sd != null && sd.getKind() == StructureDefinitionKind.PRIMITIVETYPE;
	}

	public String getWorkingLang() {
		return workingLang;
	}

	public void setWorkingLang(String workingLang) {
		this.workingLang = workingLang;
	}

	public NodeStack getParent() {
		return parent;
	}

	public void pathComment(String comment) {
		literalPath = literalPath + "/*"+comment+"*/";

	}

	public boolean isResetPoint() {
		return resetPoint;
	}

	@Override
	public String toString() {
		return literalPath;
	}

	public int depth() {
		if (parent == null) {
			return 0;
		} else {
			return parent.depth()+1;
		}
	}

	public boolean isContained() {
		return contained;
	}

	public void setContained(boolean contained) {
		this.contained = contained;
	}


}
