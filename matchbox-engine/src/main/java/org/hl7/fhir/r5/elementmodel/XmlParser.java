package org.hl7.fhir.r5.elementmodel;

import java.io.ByteArrayInputStream;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4b.elementmodel.ParserBase.NamedElement;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element.SpecialElement;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Constants;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.ElementDefinition.PropertyRepresentation;
import org.hl7.fhir.r5.model.Enumeration;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.r5.utils.formats.XmlLocationAnnotator;
import org.hl7.fhir.r5.utils.formats.XmlLocationData;
import org.hl7.fhir.utilities.ElementDecoration;
import org.hl7.fhir.utilities.StringPair;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.i18n.I18nConstants;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.xhtml.CDANarrativeFormat;
import org.hl7.fhir.utilities.xhtml.XhtmlComposer;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.hl7.fhir.utilities.xml.IXMLWriter;
import org.hl7.fhir.utilities.xml.XMLUtil;
import org.hl7.fhir.utilities.xml.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class XmlParser extends ParserBase {
  private boolean allowXsiLocation;
  private String version;

  public XmlParser(IWorkerContext context) {
    super(context);
  }

  private String schemaPath;

  public String getSchemaPath() {
    return schemaPath;
  }
  public void setSchemaPath(String schemaPath) {
    this.schemaPath = schemaPath;
  }

  public boolean isAllowXsiLocation() {
    return allowXsiLocation;
  }

  public void setAllowXsiLocation(boolean allowXsiLocation) {
    this.allowXsiLocation = allowXsiLocation;
  }

  public List<ValidatedFragment> parse(InputStream inStream) throws FHIRFormatError, DefinitionException, FHIRException, IOException {
    
    byte[] content = TextFile.streamToBytes(inStream);
    ValidatedFragment focusFragment = new ValidatedFragment(ValidatedFragment.FOCUS_NAME, "xml", content, false);
    
    ByteArrayInputStream stream = new ByteArrayInputStream(content);
    Document doc = null;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // xxe protection
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);

			factory.setNamespaceAware(true);
			if (policy == ValidationPolicy.EVERYTHING) {
				// The SAX interface appears to not work when reporting the correct version/encoding.
				// if we can, we'll inspect the header/encoding ourselves
				if (stream.markSupported()) {
					stream.mark(1024);
          version = checkHeader(focusFragment.getErrors(), stream);
					stream.reset();
				}
				// use a slower parser that keeps location data

				// MATCHBOX PATCH: xxe protection: https://github.com/ahdis/matchbox/security/code-scanning/45
				TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
				transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

				Transformer nullTransformer = transformerFactory.newTransformer();
				DocumentBuilder docBuilder = factory.newDocumentBuilder();
				doc = docBuilder.newDocument();
				DOMResult domResult = new DOMResult(doc);
				SAXParserFactory spf = SAXParserFactory.newInstance();
				spf.setNamespaceAware(true);
				spf.setValidating(false);
				// xxe protection
				spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
				spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				SAXParser saxParser = spf.newSAXParser();
				XMLReader xmlReader = saxParser.getXMLReader();
				// xxe protection
				xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
				xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        XmlLocationAnnotator locationAnnotator = new XmlLocationAnnotator(xmlReader, doc);
        InputSource inputSource = new InputSource(stream);
        SAXSource saxSource = new SAXSource(locationAnnotator, inputSource);
        nullTransformer.transform(saxSource, domResult);
      } else {
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new NullErrorHandler());
        doc = builder.parse(stream);
      }
    } catch (Exception e) {
      if (e.getMessage().contains("lineNumber:") && e.getMessage().contains("columnNumber:")) {
        int line = Utilities.parseInt(extractVal(e.getMessage(), "lineNumber"), 0); 
        int col = Utilities.parseInt(extractVal(e.getMessage(), "columnNumber"), 0); 
        logError(focusFragment.getErrors(), ValidationMessage.NO_RULE_DATE, line, col, "(xml)", IssueType.INVALID, e.getMessage().substring(e.getMessage().lastIndexOf(";")+1).trim(), IssueSeverity.FATAL);
      } else {
        logError(focusFragment.getErrors(), ValidationMessage.NO_RULE_DATE, 0, 0, "(xml)", IssueType.INVALID, e.getMessage(), IssueSeverity.FATAL);
      }
      doc = null;
    }
    if (doc != null) {
      focusFragment.setElement(parse(focusFragment.getErrors(), doc));
    }
    List<ValidatedFragment> res = new ArrayList<>();
    res.add(focusFragment);
    return res;
  }


  private String extractVal(String src, String name) {
    src = src.substring(src.indexOf(name)+name.length()+1);
    src = src.substring(0, src.indexOf(";")).trim();
    return src;
  }
  private void checkForProcessingInstruction(List<ValidationMessage> errors, Document document) throws FHIRFormatError {
    if (policy == ValidationPolicy.EVERYTHING && FormatUtilities.FHIR_NS.equals(document.getDocumentElement().getNamespaceURI())) {
      Node node = document.getFirstChild();
      while (node != null) {
        if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
          logError(errors, ValidationMessage.NO_RULE_DATE, line(document, false), col(document, false), "(document)", IssueType.INVALID, context.formatMessage(
              I18nConstants.NO_PROCESSING_INSTRUCTIONS_ALLOWED_IN_RESOURCES), IssueSeverity.ERROR);
        node = node.getNextSibling();
      }
    }
  }


  private int line(Node node, boolean end) {
    XmlLocationData loc = node == null ? null : (XmlLocationData) node.getUserData(XmlLocationData.LOCATION_DATA_KEY);
    return loc == null ? 0 : end ? loc.getEndLine() : loc.getStartLine();
  }

  private int col(Node node, boolean end) {
    XmlLocationData loc = node == null ? null : (XmlLocationData) node.getUserData(XmlLocationData.LOCATION_DATA_KEY);
    return loc == null ? 0 : end ? loc.getEndColumn() : loc.getStartColumn();
  }

  public Element parse(List<ValidationMessage> errors, Document doc) throws FHIRFormatError, DefinitionException, FHIRException, IOException {
    checkForProcessingInstruction(errors, doc);
    org.w3c.dom.Element element = doc.getDocumentElement();
    return parse(errors, element);
  }

  public Element parse(List<ValidationMessage> errors, org.w3c.dom.Element element) throws FHIRFormatError, DefinitionException, FHIRException, IOException {
    String ns = element.getNamespaceURI();
    String name = element.getLocalName();
    String path = "/"+pathPrefix(ns)+name;

    StructureDefinition sd = getDefinition(errors, line(element, false), col(element, false), (ns == null ? "noNamespace" : ns), name);
    if (sd == null)
      return null;

    Element result = new Element(element.getLocalName(), new Property(context, sd.getSnapshot().getElement().get(0), sd, getProfileUtilities(), getContextUtilities())).setFormat(FhirFormat.XML);
    result.setPath(element.getLocalName());
    checkElement(errors, element, result, path, result.getProperty(), false);
    result.markLocation(line(element, false), col(element, false));
    result.setType(element.getLocalName());
    parseChildren(errors, path, element, result);
    result.numberChildren();
    return result;
  }

  private String pathPrefix(String ns) {
    if (Utilities.noString(ns))
      return "";
    if (ns.equals(FormatUtilities.FHIR_NS))
      return "f:";
    if (ns.equals(FormatUtilities.XHTML_NS))
      return "h:";
    if (ns.equals("urn:hl7-org:v3"))
      return "v3:";
    if (ns.equals("urn:hl7-org:sdtc")) 
      return "sdtc:";
    if (ns.equals("urn:ihe:pharm"))
      return "pharm:";
    if (ns.equals("urn:oid:1.3.6.1.4.1.19376.1.3.2")) {
          // MATCHBOX
          return "lab:";
        }
        return "?:";
      }

  private boolean empty(org.w3c.dom.Element element) {
    for (int i = 0; i < element.getAttributes().getLength(); i++) {
      String n = element.getAttributes().item(i).getNodeName();
      if (!n.equals("xmlns") && !n.startsWith("xmlns:"))
        return false;
    }
    if (!Utilities.noString(element.getTextContent().trim()))
      return false;

    Node n = element.getFirstChild();
    while (n != null) {
      if (n.getNodeType() == Node.ELEMENT_NODE)
        return false;
      n = n.getNextSibling();
    }
    return true;
  }

  private void checkElement(List<ValidationMessage> errors, org.w3c.dom.Element element, Element e, String path, Property prop, boolean xsiTypeChecked) throws FHIRFormatError {
    if (policy == ValidationPolicy.EVERYTHING) {
      if (empty(element) && FormatUtilities.FHIR_NS.equals(element.getNamespaceURI())) // this rule only applies to FHIR Content
        logError(errors, ValidationMessage.NO_RULE_DATE, line(element, false), col(element, false), path, IssueType.INVALID, context.formatMessage(I18nConstants.ELEMENT_MUST_HAVE_SOME_CONTENT), IssueSeverity.ERROR);
      String ns = prop.getXmlNamespace();
      String elementNs = element.getNamespaceURI();
      if (elementNs == null) {
        elementNs = "noNamespace";
      }
      if (!elementNs.equals(ns)) {
        logError(errors, ValidationMessage.NO_RULE_DATE, line(element, false), col(element, false), path, IssueType.INVALID, context.formatMessage(I18nConstants.WRONG_NAMESPACE__EXPECTED_, ns), IssueSeverity.ERROR);
      }
      if (!xsiTypeChecked) {
        String xsiType = element.getAttributeNS(FormatUtilities.NS_XSI, "type");
        if (!Utilities.noString(xsiType)) {
          String actualType = prop.getXmlTypeName();
          if (xsiType.equals(actualType)) {
            logError(errors, "2023-10-12", line(element, false), col(element, false), path, IssueType.INVALID, context.formatMessage(I18nConstants.XSI_TYPE_UNNECESSARY), IssueSeverity.INFORMATION);            
          } else {
            StructureDefinition sd = findLegalConstraint(xsiType, actualType);
            if (sd != null) {
              e.setType(sd.getType());
              e.setExplicitType(xsiType);
            } else {
              logError(errors, "2023-10-12", line(element, false), col(element, false), path, IssueType.INVALID, context.formatMessage(I18nConstants.XSI_TYPE_WRONG, xsiType, actualType), IssueSeverity.ERROR);           
            }  
          }
        }
      }
    }
  }

  private StructureDefinition findLegalConstraint(String xsiType, String actualType) {
    StructureDefinition sdA = context.fetchTypeDefinition(actualType);
    StructureDefinition sd = context.fetchTypeDefinition(xsiType);
    while (sd != null) {
      if (sd == sdA) {
        return sd;
      }
      sd = context.fetchResource(StructureDefinition.class, sd.getBaseDefinition());
    }
    return null;
  }
  
  public Element parse(List<ValidationMessage> errors, org.w3c.dom.Element base, String type) throws Exception {
    StructureDefinition sd = getDefinition(errors, 0, 0, FormatUtilities.FHIR_NS, type);
    Element result = new Element(base.getLocalName(), new Property(context, sd.getSnapshot().getElement().get(0), sd, getProfileUtilities(), getContextUtilities())).setFormat(FhirFormat.XML).setNativeObject(base);
    result.setPath(base.getLocalName());
    String path = "/"+pathPrefix(base.getNamespaceURI())+base.getLocalName();
    checkElement(errors, base, result, path, result.getProperty(), false);
    result.setType(base.getLocalName());
    parseChildren(errors, path, base, result);
    result.numberChildren();
    return result;
  }

  private void parseChildren(List<ValidationMessage> errors, String path, org.w3c.dom.Element node, Element element) throws FHIRFormatError, FHIRException, IOException, DefinitionException {
    // this parsing routine retains the original order in a the XML file, to support validation
    reapComments(node, element);
    List<Property> properties = element.getProperty().getChildProperties(element.getName(), XMLUtil.getXsiType(node));
    Property cgProp = getChoiceGroupProp(properties);
    Property mtProp = cgProp == null ? null : getTextProp(cgProp.getChildProperties(null, null));

    String text = mtProp == null ? XMLUtil.getDirectText(node).trim() : null;
    int line = line(node, false);
    int col = col(node, false);
    if (!Utilities.noString(text)) {
      Property property = getTextProp(properties);
      if (property != null) {
        if ("ED.data[x]".equals(property.getDefinition().getId()) || (property.getDefinition()!=null && property.getDefinition().getBase()!=null && "ED.data[x]".equals(property.getDefinition().getBase().getPath()))) {
          if ("B64".equals(node.getAttribute("representation"))) {
            Element n = new Element("dataBase64Binary", property, "base64Binary", text).markLocation(line, col).setFormat(FhirFormat.XML);
            n.setPath(element.getPath()+"."+property.getName());
            element.getChildren().add(n);
          } else {
            Element n = new Element("dataString", property, "string", text).markLocation(line, col).setFormat(FhirFormat.XML);
            n.setPath(element.getPath()+"."+property.getName());
            element.getChildren().add(n);
          }
        } else {
          Element n = new Element(property.getName(), property, property.getType(), text).markLocation(line, col).setFormat(FhirFormat.XML);
          n.setPath(element.getPath()+"."+property.getName());
          element.getChildren().add(n);
        }
      } else {
        Node n = node.getFirstChild();
        while (n != null) {
          if (n.getNodeType() == Node.TEXT_NODE && !Utilities.noString(n.getTextContent().trim())) {
            Node nt = n; // try to find the nearest element for a line/col location
            boolean end = false;
            while (nt.getPreviousSibling() != null && nt.getNodeType() != Node.ELEMENT_NODE) {
              nt = nt.getPreviousSibling();
              end = true;
            }
            while (nt.getNextSibling() != null && nt.getNodeType() != Node.ELEMENT_NODE) {
              nt = nt.getNextSibling();
              end = false;
            }
            line = line(nt, end);
            col = col(nt, end);
            logError(errors, ValidationMessage.NO_RULE_DATE, line, col, path, IssueType.STRUCTURE, context.formatMessage(I18nConstants.TEXT_SHOULD_NOT_BE_PRESENT, Utilities.makeSingleLine(n.getTextContent().trim())), IssueSeverity.ERROR);
          }
          n = n.getNextSibling();
        }
      }    		
    }

    for (int i = 0; i < node.getAttributes().getLength(); i++) {
      Node attr = node.getAttributes().item(i);
      String value = attr.getNodeValue();
      if (!validAttrValue(value)) {
        logError(errors, ValidationMessage.NO_RULE_DATE, line, col, path, IssueType.STRUCTURE, context.formatMessage(I18nConstants.XML_ATTR_VALUE_INVALID, attr.getNodeName()), IssueSeverity.ERROR);
      }
      if (!(attr.getNodeName().equals("xmlns") || attr.getNodeName().startsWith("xmlns:"))) {
        Property property = getAttrProp(properties, attr.getLocalName(), attr.getNamespaceURI());
        if (property != null) {
          String av = attr.getNodeValue();

					// matchbox-engine: if we are parsing from CDA we need to collapse non string types https://www.w3.org/TR/xmlschema-2/#rf-whiteSpace
					// If the attribute type is not CDATA, then the XML processor must further process the normalized attribute value by discarding any leading and trailing space (#x20) characters
					if ("urn:hl7-org:v3".equals(node.getNamespaceURI()) || "urn:hl7-org:sdtc".equals(node.getNamespaceURI()) || "urn:ihe:pharm".equals(node.getNamespaceURI()) || "urn:oid:1.3.6.1.4.1.19376.1.3.2".equals(node.getNamespaceURI())) {
						av = av.trim();
					}

          if (ToolingExtensions.hasExtension(property.getDefinition(), ToolingExtensions.EXT_DATE_FORMAT))
            av = convertForDateFormatFromExternal(ToolingExtensions.readStringExtension(property.getDefinition(), ToolingExtensions.EXT_DATE_FORMAT), av);          
          if (property.getName().equals("value") && element.isPrimitive())
            element.setValue(av);
          else {
            String[] vl = {av};
            if (property.isList() && av.contains(" ")) {
              vl = av.split(" ");
            }
            for (String v : vl) {
              Element n = new Element(property.getName(), property, property.getType(), v).markLocation(line, col).setFormat(FhirFormat.XML);
              n.setPath(element.getPath()+"."+property.getName());
              element.getChildren().add(n);
            }
          }
        } else {
          boolean ok = false;
          if (FormatUtilities.FHIR_NS.equals(node.getNamespaceURI())) {
            if (attr.getLocalName().equals("schemaLocation") && FormatUtilities.NS_XSI.equals(attr.getNamespaceURI())) {
              ok = ok || allowXsiLocation; 
            }
          } else
            ok = ok || (attr.getLocalName().equals("schemaLocation")); // xsi:schemalocation allowed for non FHIR content
          ok = ok || (hasTypeAttr(element) && attr.getLocalName().equals("type") && FormatUtilities.NS_XSI.equals(attr.getNamespaceURI())); // xsi:type allowed if element says so
          if (!ok) { 
            logError(errors, ValidationMessage.NO_RULE_DATE, line(node, false), col(node, false), path, IssueType.STRUCTURE, context.formatMessage(I18nConstants.UNDEFINED_ATTRIBUTE__ON__FOR_TYPE__PROPERTIES__, attr.getNodeName(), node.getNodeName(), element.fhirType(), properties), IssueSeverity.ERROR);
          }
        }
      }
    }

    String lastName = null;
    int repeatCount = 0;
    Node child = node.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        Property property = getElementProp(properties, child.getLocalName(), child.getNamespaceURI());
        
        if (property != null) {
          if (property.getName().equals(lastName)) {
            repeatCount++;
          } else {
            lastName = property.getName();
            repeatCount = 0;
          }
          if (!property.isChoice() && "xhtml".equals(property.getType())) {
            XhtmlNode xhtml;
            if (property.getDefinition().hasRepresentation(PropertyRepresentation.CDATEXT))
              xhtml = new CDANarrativeFormat().convert((org.w3c.dom.Element) child);
            else {
              XhtmlParser xp = new XhtmlParser();
              xhtml = xp.parseHtmlNode((org.w3c.dom.Element) child);
              if (policy == ValidationPolicy.EVERYTHING) {
                for (StringPair s : xp.getValidationIssues()) {
                  logError(errors, "2022-11-17", line(child, false), col(child, false), path, IssueType.INVALID, context.formatMessage(s.getName(), s.getValue()), IssueSeverity.ERROR);                
                }
              }
            }
            Element n = new Element(property.getName(), property, "xhtml", new XhtmlComposer(XhtmlComposer.XML, false).compose(xhtml)).setXhtml(xhtml).markLocation(line(child, false), col(child, false)).setFormat(FhirFormat.XML).setNativeObject(child);
            n.setPath(element.getPath()+"."+property.getName());
            element.getChildren().add(n);
          } else {
            String npath = path+"/"+pathPrefix(child.getNamespaceURI())+child.getLocalName();
            String name = child.getLocalName();
            if (!property.isChoice() && !name.equals(property.getName())) {
              name = property.getName();
            }
            Element n = new Element(name, property).markLocation(line(child, false), col(child, false)).setFormat(FhirFormat.XML).setNativeObject(child);
            if (property.isList()) {
              n.setPath(element.getPath()+"."+property.getName()+"["+repeatCount+"]");    				  
            } else {
              n.setPath(element.getPath()+"."+property.getName());
            }
            boolean xsiTypeChecked = false;
            boolean ok = true;
            if (property.isChoice()) {
              if (property.getDefinition().hasRepresentation(PropertyRepresentation.TYPEATTR)) {
                String xsiType = ((org.w3c.dom.Element) child).getAttributeNS(FormatUtilities.NS_XSI, "type");
                if (Utilities.noString(xsiType)) {
                  if (ToolingExtensions.hasExtension(property.getDefinition(), "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype")) {
                    xsiType = ToolingExtensions.readStringExtension(property.getDefinition(), "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype");
                    n.setType(xsiType);
                  } else {
                    logError(errors, ValidationMessage.NO_RULE_DATE, line(child, false), col(child, false), path, IssueType.STRUCTURE, context.formatMessage(I18nConstants.NO_TYPE_FOUND_ON_, child.getLocalName()), IssueSeverity.ERROR);
                    ok = false;
                  }
                } else {
                  if (xsiType.contains(":"))
                    xsiType = xsiType.substring(xsiType.indexOf(":")+1);
                  n.setType(xsiType);
                  n.setExplicitType(xsiType);
                }
                xsiTypeChecked = true;
              } else
                n.setType(n.getType());
            }
            checkElement(errors, (org.w3c.dom.Element) child, n, npath, n.getProperty(), xsiTypeChecked);
            element.getChildren().add(n);
            if (ok) {
              if (property.isResource())
                parseResource(errors, npath, (org.w3c.dom.Element) child, n, property);
              else
                parseChildren(errors, npath, (org.w3c.dom.Element) child, n);
            }
          }
        } else {
          if (cgProp != null) {
            property = getElementProp(cgProp.getChildProperties(null, null), child.getLocalName(), child.getNamespaceURI());
            if (property != null) {
              if (cgProp.getName().equals(lastName)) {
                repeatCount++;
              } else {
                lastName = cgProp.getName();
                repeatCount = 0;
              }
              
              String npath = path+"/"+pathPrefix(cgProp.getXmlNamespace())+cgProp.getName();
              String name = cgProp.getName();
              Element cgn = new Element(cgProp.getName(), cgProp).setFormat(FhirFormat.XML);
              cgn.setPath(element.getPath()+"."+cgProp.getName()+"["+repeatCount+"]"); 
              element.getChildren().add(cgn);
              
              npath = npath+"/"+pathPrefix(child.getNamespaceURI())+child.getLocalName();
              name = child.getLocalName();
              Element n = new Element(name, property).markLocation(line(child, false), col(child, false)).setFormat(FhirFormat.XML).setNativeObject(child);
              cgn.getChildren().add(n);
              n.setPath(element.getPath()+"."+property.getName());
              checkElement(errors, (org.w3c.dom.Element) child, n, npath, n.getProperty(), false);
              parseChildren(errors, npath, (org.w3c.dom.Element) child, n);
            }
          }
          if (property == null) {
            logError(errors, ValidationMessage.NO_RULE_DATE, line(child, false), col(child, false), path, IssueType.STRUCTURE, context.formatMessage(I18nConstants.UNDEFINED_ELEMENT_, child.getLocalName(), path), IssueSeverity.ERROR);
          }
        }
      } else if (child.getNodeType() == Node.TEXT_NODE && !Utilities.noString(child.getTextContent().trim()) && mtProp != null) {
        if (cgProp.getName().equals(lastName)) {
          repeatCount++;
        } else {
          lastName = cgProp.getName();
          repeatCount = 0;
        }
        
        String npath = path+"/"+pathPrefix(cgProp.getXmlNamespace())+cgProp.getName();
        String name = cgProp.getName();
        Element cgn = new Element(cgProp.getName(), cgProp).setFormat(FhirFormat.XML);
        cgn.setPath(element.getPath()+"."+cgProp.getName()+"["+repeatCount+"]"); 
        element.getChildren().add(cgn);
        
        npath = npath+"/text()";
        name = mtProp.getName();
        Element n = new Element(name, mtProp, mtProp.getType(), child.getTextContent().trim()).markLocation(line(child, false), col(child, false)).setFormat(FhirFormat.XML).setNativeObject(child);
        cgn.getChildren().add(n);
        n.setPath(element.getPath()+"."+mtProp.getName());

        
      } else if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
        logError(errors, ValidationMessage.NO_RULE_DATE, line(child, false), col(child, false), path, IssueType.STRUCTURE, context.formatMessage(I18nConstants.CDATA_IS_NOT_ALLOWED), IssueSeverity.ERROR);
      } else if (!Utilities.existsInList(child.getNodeType(), 3, 8)) {
        logError(errors, ValidationMessage.NO_RULE_DATE, line(child, false), col(child, false), path, IssueType.STRUCTURE, context.formatMessage(I18nConstants.NODE_TYPE__IS_NOT_ALLOWED, Integer.toString(child.getNodeType())), IssueSeverity.ERROR);
      }
      child = child.getNextSibling();
    }
  }

  private Property getChoiceGroupProp(List<Property> properties) {
    for (Property p : properties) {
      if (p.getDefinition().hasExtension(ToolingExtensions.EXT_ID_CHOICE_GROUP)) {
        return p;
      }
    }
    return null;
  }
  
  private boolean validAttrValue(String value) {
    if (version == null) {
      return true;
    }
    if (version.equals("1.0")) {
      boolean ok = true;
      for (char ch : value.toCharArray()) {
        if (ch <= 0x1F && !Utilities.existsInList(ch, '\r', '\n', '\t')) {
          ok = false;
        }
      }
      return ok;
    } else
      return true;
  }


  private Property getElementProp(List<Property> properties, String nodeName, String namespace) {
    List<Property> propsSortedByLongestFirst = new ArrayList<Property>(properties);
    // sort properties according to their name longest first, so .requestOrganizationReference comes first before .request[x]
    // and therefore the longer property names get evaluated first
    Collections.sort(propsSortedByLongestFirst, new Comparator<Property>() {
      @Override
      public int compare(Property o1, Property o2) {
        return o2.getName().length() - o1.getName().length();
      }
    });
    // first scan, by namespace
    for (Property p : propsSortedByLongestFirst) {
      if (!p.getDefinition().hasRepresentation(PropertyRepresentation.XMLATTR) && !p.getDefinition().hasRepresentation(PropertyRepresentation.XMLTEXT)) {
        if (p.getXmlName().equals(nodeName) && p.getXmlNamespace().equals(namespace)) 
          return p;
      }
    }
    for (Property p : propsSortedByLongestFirst) {
      if (!p.getDefinition().hasRepresentation(PropertyRepresentation.XMLATTR) && !p.getDefinition().hasRepresentation(PropertyRepresentation.XMLTEXT)) {
        if (p.getXmlName().equals(nodeName)) 
          return p;
        if (p.getName().endsWith("[x]") && nodeName.length() > p.getName().length()-3 && p.getName().substring(0, p.getName().length()-3).equals(nodeName.substring(0, p.getName().length()-3))) 
          return p;
      }
    }
    

    return null;
  }

  private Property getAttrProp(List<Property> properties, String nodeName, String namespace) {
    for (Property p : properties) {
      if (p.getXmlName().equals(nodeName) && p.getDefinition().hasRepresentation(PropertyRepresentation.XMLATTR) && p.getXmlNamespace().equals(namespace)) {
        return p;
      }
    }
    if (namespace == null) {
      for (Property p : properties) {
        if (p.getXmlName().equals(nodeName) && p.getDefinition().hasRepresentation(PropertyRepresentation.XMLATTR)) {
          return p;
        }    
      }
    }
    return null;
  }

  private Property getTextProp(List<Property> properties) {
    for (Property p : properties)
      if (p.getDefinition().hasRepresentation(PropertyRepresentation.XMLTEXT)) 
        return p;
    return null;
  }

  private String convertForDateFormatFromExternal(String fmt, String av) throws FHIRException {
// MATCHBOX PATCH: support for the long date format
    if ("v3".equals(fmt) || "YYYYMMDDHHMMSS.UUUU[+|-ZZzz]".equals(fmt)) {
      try {
        DateTimeType d = DateTimeType.parseV3(av);
        return d.asStringValue();
      } catch (Exception e) {
        return av; // not at all clear what to do in this case.
      }
    }
    throw new FHIRException(context.formatMessage(I18nConstants.UNKNOWN_DATA_FORMAT_, fmt));
  }

  private String convertForDateFormatToExternal(String fmt, String av) throws FHIRException {
// MATCHBOX PATCH: support for the long date format
    if ("v3".equals(fmt) || "YYYYMMDDHHMMSS.UUUU[+|-ZZzz]".equals(fmt)) {
      DateTimeType d = new DateTimeType(av);
      return d.getAsV3();
    } else
      throw new FHIRException(context.formatMessage(I18nConstants.UNKNOWN_DATE_FORMAT_, fmt));
  }

  private void parseResource(List<ValidationMessage> errors, String string, org.w3c.dom.Element container, Element parent, Property elementProperty) throws FHIRFormatError, DefinitionException, FHIRException, IOException {
    org.w3c.dom.Element res = XMLUtil.getFirstChild(container);
    String name = res.getLocalName();
    StructureDefinition sd = context.fetchResource(StructureDefinition.class, ProfileUtilities.sdNs(name, null));
    if (sd == null)
      throw new FHIRFormatError(context.formatMessage(I18nConstants.CONTAINED_RESOURCE_DOES_NOT_APPEAR_TO_BE_A_FHIR_RESOURCE_UNKNOWN_NAME_, res.getLocalName()));
    parent.updateProperty(new Property(context, sd.getSnapshot().getElement().get(0), sd, getProfileUtilities(), getContextUtilities()), SpecialElement.fromProperty(parent.getProperty()), elementProperty);
    parent.setType(name);
    parseChildren(errors, res.getLocalName(), res, parent);
  }

  private void reapComments(org.w3c.dom.Element element, Element context) {
    Node node = element.getPreviousSibling();
    while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
      if (node.getNodeType() == Node.COMMENT_NODE)
        context.getComments().add(0, node.getTextContent());
      node = node.getPreviousSibling();
    }
    node = element.getLastChild();
    while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
      node = node.getPreviousSibling();
    }
    while (node != null) {
      if (node.getNodeType() == Node.COMMENT_NODE)
        context.getComments().add(node.getTextContent());
      node = node.getNextSibling();
    }
  }

  private boolean isAttr(Property property) {
    for (Enumeration<PropertyRepresentation> r : property.getDefinition().getRepresentation()) {
      if (r.getValue() == PropertyRepresentation.XMLATTR) {
        return true;
      }
    }
    return false;
  }

  private boolean isCdaText(Property property) {
    for (Enumeration<PropertyRepresentation> r : property.getDefinition().getRepresentation()) {
      if (r.getValue() == PropertyRepresentation.CDATEXT) {
        return true;
      }
    }
    return false;
  }

  private boolean isTypeAttr(Property property) {
    for (Enumeration<PropertyRepresentation> r : property.getDefinition().getRepresentation()) {
      if (r.getValue() == PropertyRepresentation.TYPEATTR) {
        return true;
      }
    }
    return false;
  }

  private boolean isText(Property property) {
    for (Enumeration<PropertyRepresentation> r : property.getDefinition().getRepresentation()) {
      if (r.getValue() == PropertyRepresentation.XMLTEXT) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void compose(Element e, OutputStream stream, OutputStyle style, String base) throws IOException, FHIRException {
    XMLWriter xml = new XMLWriter(stream, "UTF-8");
    xml.setSortAttributes(false);
    xml.setPretty(style == OutputStyle.PRETTY);
    xml.start();
    if (e.getPath() == null) {
      e.populatePaths(null);
    }
    String ns = e.getProperty().getXmlNamespace();
    if (ns!=null && !"noNamespace".equals(ns)) {
      xml.setDefaultNamespace(ns);
    }
    if (hasTypeAttr(e))
      xml.namespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
    addNamespaces(xml, e);
    composeElement(xml, e, e.getType(), true);
    xml.end();
  }

  private void addNamespaces(IXMLWriter xml, Element e) throws IOException {
    String ns = e.getProperty().getXmlNamespace();
    if (ns!=null && xml.getDefaultNamespace()!=null && !xml.getDefaultNamespace().equals(ns)){
      if (!xml.namespaceDefined(ns)) {
        String prefix = pathPrefix(ns);
        if (prefix.endsWith(":")) {
          prefix = prefix.substring(0, prefix.length()-1);
        }
        if ("?".equals(prefix)) {
          xml.namespace(ns);
        } else {
          xml.namespace(ns, prefix);
        }
      }
    }
    for (Element c : e.getChildren()) {
      addNamespaces(xml, c);
    }
  }

  private boolean hasTypeAttr(Element e) {
    if (isTypeAttr(e.getProperty()))
      return true;
    for (Element c : e.getChildren()) {
      if (hasTypeAttr(c))
        return true;
    }
    // xsi_type is always allowed on CDA elements. right now, I'm not sure where to indicate this in the model, 
    // so it's just hardcoded here 
    if (e.getType() != null && e.getType().startsWith(Constants.NS_CDA_ROOT)) {
      return true;
    }
    return false;
  }

  private void setXsiTypeIfIsTypeAttr(IXMLWriter xml, Element element) throws IOException, FHIRException {
    if (isTypeAttr(element.getProperty()) && !Utilities.noString(element.getType())) {
      String type = element.getType();
      if (Utilities.isAbsoluteUrl(type)) {
        type = type.substring(type.lastIndexOf("/")+1);
      }
      xml.attribute("xsi:type",type);    
    }
  }

  public void compose(Element e, IXMLWriter xml) throws Exception {
    if (e.getPath() == null) {
      e.populatePaths(null);
    }
    xml.start();
    xml.setDefaultNamespace(e.getProperty().getXmlNamespace());
    if (schemaPath != null) {
      xml.setSchemaLocation(FormatUtilities.FHIR_NS, Utilities.pathURL(schemaPath, e.fhirType()+".xsd"));
    }
    composeElement(xml, e, e.getType(), true);
    xml.end();
  }

  private void composeElement(IXMLWriter xml, Element element, String elementName, boolean root) throws IOException, FHIRException {
    if (showDecorations) {
      @SuppressWarnings("unchecked")
      List<ElementDecoration> decorations = (List<ElementDecoration>) element.getUserData("fhir.decorations");
      if (decorations != null)
        for (ElementDecoration d : decorations)
          xml.decorate(d);
    }
    for (String s : element.getComments()) {
      xml.comment(s, true);
    }
    if (isText(element.getProperty())) {
      if (linkResolver != null)
        xml.link(linkResolver.resolveProperty(element.getProperty()));
      xml.enter(element.getProperty().getXmlNamespace(),elementName);
      if (linkResolver != null && element.getProperty().isReference()) {
        String ref = linkResolver.resolveReference(getReferenceForElement(element));
        if (ref != null) {
          xml.externalLink(ref);
        }
      }
      xml.text(element.getValue());
      xml.exit(element.getProperty().getXmlNamespace(),elementName);   
    } else if (!element.hasChildren() && !element.hasValue()) {
      if (element.getExplicitType() != null)
        xml.attribute("xsi:type", element.getExplicitType());
      xml.element(elementName);
    } else if (element.isPrimitive() || (element.hasType() && isPrimitive(element.getType()))) {
      if (element.getType().equals("xhtml")) {
        String rawXhtml = element.getValue();
        if (isCdaText(element.getProperty())) {
          new CDANarrativeFormat().convert(xml, new XhtmlParser().parseFragment(rawXhtml));
        } else {
          xml.escapedText(rawXhtml);
          xml.anchor("end-xhtml");
        }
      } else if (isText(element.getProperty())) {
        if (linkResolver != null)
          xml.link(linkResolver.resolveProperty(element.getProperty()));
        xml.text(element.getValue());
      } else {
        setXsiTypeIfIsTypeAttr(xml, element);
        if (element.hasValue()) {
          if (linkResolver != null)
            xml.link(linkResolver.resolveType(element.getType()));
          xml.attribute("value", element.getValue());
        }
        if (linkResolver != null)
          xml.link(linkResolver.resolveProperty(element.getProperty()));
        if (element.hasChildren()) {
          xml.enter(element.getProperty().getXmlNamespace(), elementName);
          if (linkResolver != null && element.getProperty().isReference()) {
            String ref = linkResolver.resolveReference(getReferenceForElement(element));
            if (ref != null) {
              xml.externalLink(ref);
            }
          }
          for (Element child : element.getChildren()) 
            composeElement(xml, child, child.getName(), false);
          xml.exit(element.getProperty().getXmlNamespace(),elementName);
        } else
          xml.element(elementName);
      }
    } else {
      setXsiTypeIfIsTypeAttr(xml, element);
      Set<String> handled = new HashSet<>();
      for (Element child : element.getChildren()) {
        if (!handled.contains(child.getName()) && isAttr(child.getProperty()) && wantCompose(element.getPath(), child)) {
          handled.add(child.getName());
          String av = child.getValue();
          if (child.getProperty().isList()) {
            for (Element c2 : element.getChildren()) {
              if (c2 != child && c2.getName().equals(child.getName())) {
                av = av + " "+c2.getValue();
              }
            }            
          }
          if (linkResolver != null)
            xml.link(linkResolver.resolveType(child.getType()));
          if (ToolingExtensions.hasExtension(child.getProperty().getDefinition(), ToolingExtensions.EXT_DATE_FORMAT))
            av = convertForDateFormatToExternal(ToolingExtensions.readStringExtension(child.getProperty().getDefinition(), ToolingExtensions.EXT_DATE_FORMAT), av);
          // MATCHBOX PATCH: adjusting it for pharm
					xml.attribute(child.getProperty().getXmlName(), av);
        }
      }
      if (linkResolver != null)
        xml.link(linkResolver.resolveProperty(element.getProperty()));
      if (!xml.namespaceDefined(element.getProperty().getXmlNamespace())) {
        String abbrev = makeNamespaceAbbrev(element.getProperty(), xml);
        xml.namespace(element.getProperty().getXmlNamespace(), abbrev);
      }
      xml.enter(element.getProperty().getXmlNamespace(), elementName);

      if (!root && element.getSpecial() != null) {
        if (linkResolver != null)
          xml.link(linkResolver.resolveProperty(element.getProperty()));
        xml.enter(element.getProperty().getXmlNamespace(),element.getType());
      }
      if (linkResolver != null && element.getProperty().isReference()) {
        String ref = linkResolver.resolveReference(getReferenceForElement(element));
        if (ref != null) {
          xml.externalLink(ref);
        }
      }
      for (Element child : element.getChildren()) {
        if (wantCompose(element.getPath(), child)) {
          if (isText(child.getProperty())) {
            if (linkResolver != null)
              xml.link(linkResolver.resolveProperty(element.getProperty()));
            xml.text(child.getValue());
          } else if (!isAttr(child.getProperty()))
            composeElement(xml, child, child.getName(), false);
        }
      }
      if (!root && element.getSpecial() != null)
        xml.exit(element.getProperty().getXmlNamespace(),element.getType());
      xml.exit(element.getProperty().getXmlNamespace(),elementName);
    }
  }

  private String makeNamespaceAbbrev(Property property, IXMLWriter xml) {
    // it's a cosmetic thing, but we're going to try to come up with a nice namespace

    ElementDefinition ed = property.getDefinition();
    String ns = property.getXmlNamespace();
    String n = property.getXmlName();
    
    String diff = property.getName().toLowerCase().replace(n.toLowerCase(), "");
    if (!Utilities.noString(diff) && diff.length() <= 5 && Utilities.isToken(diff) && !xml.abbreviationDefined(diff)) {
      return diff;
    }
    
    int i = ns.length()-1;
    while (i > 0) {
      if (Character.isAlphabetic(ns.charAt(i)) || Character.isDigit(ns.charAt(i))) {
        i--;
      } else {
        break;
      }
    }
    String tail = ns.substring(i+1);
    if (!Utilities.noString(tail) && tail.length() <= 5 && Utilities.isToken(tail) && !xml.abbreviationDefined(tail)) {
      return tail;
    }
    
    i = 0;
    while (xml.abbreviationDefined("ns"+i)) {
      i++;
    }
    return "ns"+i;
  }
  private String checkHeader(List<ValidationMessage> errors, InputStream stream) throws IOException {
    try {
      // the stream will either start with the UTF-8 BOF or with <xml
      int i0 = stream.read();
      int i1 = stream.read();
      int i2 = stream.read();

      StringBuilder b = new StringBuilder();
      if (i0 == 0xEF && i1 == 0xBB && i2 == 0xBF) {
        // ok, it's UTF-8
      } else if (i0 == 0x3C && i1 == 0x3F && i2 == 0x78) { // <xm
        b.append((char) i0);
        b.append((char) i1);
        b.append((char) i2);
      } else if (i0 == 60) { // just plain old XML with no header
        return "1.0";        
      } else {
        throw new Exception(context.formatMessage(I18nConstants.XML_ENCODING_INVALID));
      }
      int i = stream.read();
      do {
        b.append((char) i);
        i = stream.read();
      } while (i != 0x3E);
      String header = b.toString();
      String e = null;
      i = header.indexOf("encoding=\"");
      if (i > -1) {
        e = header.substring(i+10, i+15);
      } else {
        i = header.indexOf("encoding='");
        if (i > -1) {
          e = header.substring(i+10, i+15);
        } 
      }
      if (e != null && !"UTF-8".equalsIgnoreCase(e)) {
        logError(errors, ValidationMessage.NO_RULE_DATE, 0, 0, "XML", IssueType.INVALID, context.formatMessage(I18nConstants.XML_ENCODING_INVALID), IssueSeverity.ERROR);
      }

      i = header.indexOf("version=\"");
      if (i > -1) {
        return header.substring(i+9, i+12);
      } else {
        i = header.indexOf("version='");
        if (i > -1) {
          return header.substring(i+9, i+12);          
        } 
      }
      return "?xml-p1?";
    } catch (Exception e) {
      // suppress this error 
      logError(errors, ValidationMessage.NO_RULE_DATE, 0, 0, "XML", IssueType.INVALID, e.getMessage(), IssueSeverity.ERROR);
    }
    return "?xml-p2?";
  }

  class NullErrorHandler implements ErrorHandler {
    @Override
    public void fatalError(SAXParseException e) {
        // do nothing
    }

    @Override
    public void error(SAXParseException e) {
        // do nothing
    }
    
    @Override
    public void warning(SAXParseException e) {
        // do nothing
    }
}
}