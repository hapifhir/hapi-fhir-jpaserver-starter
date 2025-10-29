# Narrative Block - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Overview**](overview.md)
* **Narrative Block**

## Narrative Block

The Section.text field is used to store narrative to be rendered, as described above in [CDA Conformance](overview.md#cda-conformance), and is therefore referred to as the CDA Narrative Block.

[The CDA Narrative Block schema can be found here.](NarrativeBlock.xsd)

The content model of the CDA Narrative Block schema is specially hand crafted to meet the requirements outlined above (see [Human Readability and Rendering CDA Documents](overview.md#human-readability-and-rendering-cda-documents)). The schema is registered as a MIME type (text/x-hl7-text+xml), which is the fixed media type for Section.text. Components of the schema are described in the sections that follow.

### <content>

The CDA `<content>` element is used to wrap a string of text so that it can be explicitly referenced, or so that it can suggest rendering characteristics. The `<content>` element can nest recursively, which enables wrapping a string of plain text down to as small a chunk as desired.

The `<content>` element contains an optional identifier, that can serve as the target of a reference. All values of attributes of type XML ID must be unique within the document (per the [W3C XML specification](http://www.w3.org/TR/2004/REC-xml-20040204/#id)). The originalText component of a RIM attribute present in any CDA entry can make explicit reference to the identifier, thereby indicating the original text associated with the attribute in the CDA entry.

Example 5

```
<section>
   <code code="10153-2" 
    codeSystem="2.16.840.1.113883.6.1" 
    codeSystemName="LOINC"/>
   <title>Past Medical History</title>
   <text>
    There is a history of <content ID="a1">Asthma</content>
   </text>
   <entry>
      <observation classCode="OBS" moodCode="EVN">
         <code code="195967001" 
          codeSystem="2.16.840.1.113883.6.96" 
          codeSystemName="SNOMED CT" 
          displayName="Asthma">
            <originalText>
               <reference value="#a1"/>
            </originalText>
         </code>
         <statusCode code="completed"/>
      </observation>
   </entry>
</section>

```

There is no requirement that CDA entries must reference into the CDA Narrative Block. The referencing mechanism can be used where it is important to represent the original text component of a coded CDA entry.

The `<content>` element contains an optional "revised" attribute that can be valued with "insert" or "delete", which can be used to indicate narrative changes from the last version of a CDA document. The attribute is limited to a single generation, in that it only reflects the changes from the preceding version of a document. If applied, it needs to be used in conjunction with standard CDA revision tracking. Changes to a CDA document that has been released for patient care still require a formal versioning and revision, and the revised document can optionally carry the "revised" attribute to show the delta in the narrative. Receivers are required to interpret the "revised" attribute when rendering by visually distinguishing or suppressing deleted narrative.

### <linkHtml>

The CDA `<linkHtml>` is a generic referencing mechanism, similar, but not identical, to the HTML anchor tag. It can be used to reference identifiers that are either internal or external to the document.

Multimedia that is integral to a document, and part of the attestable content of the document requires the use of the [ObservationMedia](StructureDefinition-ObservationMedia.md) CDA entry, which is referenced by the `<renderMultiMedia>` element (see [<renderMultiMedia>](#rendermultimedia)). Multimedia that is simply referenced by the document and not an integral part of the document can use `<linkHtml>`.

The source of a link uses the linkHtml.href attribute. The target of an internal reference is an identifier of type XML ID, which can exist on other elements in the same or a different narrative block, or XML ID attributes that have been added to the [<section>](StructureDefinition-Section.md), [<observationMedia>](StructureDefinition-ObservationMedia.md), or [<renderMultiMedia>](#rendermultimedia) elements of the CDA Schema. The linkHtml.name attribute is deprecated, because attributes of type XML ID provide an alternative and more consistent target for referencing. Following the conventions of HTML, an internal link is prefaced with the pound sign, as shown in the following example.

Example 6

```
<section ID="SECT001">
   <code code="10164-2" codeSystem="2.16.840.1.113883.6.1" 
    codeSystemName="LOINC"/>
   <title>History of Present Illness</title>
   <text>Mr. Smith is a 57 year old male presenting with 
    chest pain. He sustained a myocardial infarction 3 years 
    ago, ...
   </text>
</section>

 ...

<section ID="SECT003">
   <code code="10153-2" codeSystem="2.16.840.1.113883.6.1" 
    codeSystemName="LOINC"/>
   <title>Past Medical History</title>
   <text>History of coronary artery disease, as noted
    <linkHtml href="#SECT001">above</linkHtml>.</text>
</section>

```

CDA links do not convey shareable meaning. Shareable semantics are only achieved by the inclusion of CDA entries and their associated formalized relationships. There is no requirement that a receiver render an internal or external link, or the target of an external link.

### <sub> and <sup>

The CDA `<sub>` and `<sup>` elements are used to indicate subscripts and superscripts, respectively.

Receivers are required to interpret these elements when rendering by visually distinguishing subscripted and superscripted characters.

### <br>

The CDA `<br/>` element is used to indicate a hard line break. It differs from the CDA `<paragraph>` element in that the 
 element has no content. Receivers are required to interpret this element when rendering so as to represent a line break.

### <footnote> and <footnoteRef>

The CDA `<footnote>` element is used to indicate a footnote. The element contains the footnote, inline with the flow of text to which it is applied.

The `<footnoteRef>` element can reference an existing footnote in the same or different CDA Narrative Block of the same document. It can be used when the same footnote is being used multiple times. The value of the footnoteRef.IDREF must be an footnote.ID value in the same document.

Receivers are required to interpret these elements when rendering by visually distinguishing footnoted text. The exact rendition is at the discretion of the recipient, and might include a mark at the location of the footnote with a hyperlink to the footnoted text, a simple demarcation (such as "This is the text [this is the footnote] that is being footnoted"), etc.

### <renderMultiMedia>

The CDA `<renderMultiMedia>` element references external multimedia that is integral to a document, and part of the attestable content of the document, and serves to show where the referenced multimedia is to be rendered.

The `<renderMultiMedia>` element has an optional `<caption>`, and contains a required referencedObject attribute (of type XML IDREFS), the values of which must equal the XML ID value(s) of [ObservationMedia](StructureDefinition-ObservationMedia.md) or [RegionOfInterest](StructureDefinition-RegionOfInterest.md) CDA entries within the same document.

Example 7

```
<section>
   <code code="8709-8" codeSystem="2.16.840.1.113883.6.1" 
    codeSystemName="LOINC"/>
   <title>Skin exam</title>
   <text>Erythematous rash, palmar surface, left index 
    finger.<renderMultiMedia referencedObject="MM1"/>
   </text>
   <entry>
      <observationMedia classCode="OBS" moodCode="EVN" ID="MM1">
         <id root="2.16.840.1.113883.19.2.1"/>
         <value xsi:type="ED" mediaType="image/jpeg">
            <reference value="left\-hand\-image.jpeg"/>
         </value>
      </observationMedia>
   </entry>
</section>

```

Multimedia that is simply referenced by the document and not an integral part of the document must use `<linkHtml>`.

The expected behavior is that the referenced multimedia should be rendered or referenced at the point of reference. Where a caption is present, it must also be rendered. `<renderMultiMedia>` can either reference a single [ObservationMedia](StructureDefinition-ObservationMedia.md), or one or more [RegionOfInterest](StructureDefinition-RegionOfInterest.md). If `<renderMultiMedia>` references a single ObservationMedia, that ObservationMedia should be rendered or referenced at the point of reference. If `<renderMultiMedia>` references one or more RegionOfInterest, all RegionOfInterests should be rendered or referenced at the point of reference, atop the multimedia they are regions of. If `<renderMultiMedia>` references more than one RegionOfInterest, each RegionOfInterest must be a region on the same multimedia.

### <paragraph>

A CDA `<paragraph>` is similar to the HTML paragraph, which allows blocks of narrative to be broken up into logically consistent structures. A CDA `<paragraph>` element contains an optional caption, which if present must come first before any other character data.

### <list>

A CDA `<list>` is similar to the HTML list. A CDA `<list>` has an optional caption, and contains one or more `<item>` elements. A CDA `<item>` element contains an optional caption, which if present must come first before any other character data. The required listType attribute specifies whether the `<list>` is ordered or unordered (with unordered being the default). Unordered lists are typically rendered with bullets, whereas ordered lists are typically rendered with numbers, although this is not a requirement.

### <table>

The CDA `<table>` is similar to the HTML table. The table markup is for presentation purposes only and, unlike a database table, does not possess meaningful field names.

CDA modifies the strict XHTML table model by removing formatting tags and by setting the content model of cells to be similar to the contents of other elements in the CDA Narrative Block.

The table.border, table.cellspacing, and table.cellpadding attributes are deprecated, because the styleCode attribute (see [styleCode attribute](#stylecode-attribute) provides a more consistent way for senders to suggest rendering characteristics.

### <caption>

The CDA `<caption>` is a label for a paragraph, list, list item, table, or table cell. It can also be used within the `<renderMultiMedia>` element to indicate a label for referenced ObservationMedia and RegionOfInterest entries. A `<caption>` contains plain text and may contain links and footnotes.

### styleCode attribute

The styleCode attribute is used within the CDA Narrative Block to give the instance author the ability to suggest rendering characteristics of the nested character data. Receivers are not required to render documents using the style hints provided and can present stylized text in accordance with their local style conventions.

The value set is drawn from the HL7 styleType vocabulary domain, and has a CWE coding strength.

Table 89: Value set for styleCode (CWE)

* Code: Font style (Defines font rendering characteristics.)
* Code: Bold
  * Definition: Render with a bold font.
* Code: Underline
  * Definition: Render with an underlines font.
* Code: Italics
  * Definition: Render italicized.
* Code: Emphasis
  * Definition: Render with some type of emphasis.
* Code: Table rule style (Defines table cell rendering characteristics.)
* Code: Lrule
  * Definition: Render cell with left-sided rule.
* Code: Rrule
  * Definition: Render cell with right-sided rule.
* Code: Toprule
  * Definition: Render cell with rule on top.
* Code: Botrule
  * Definition: Render cell with rule on bottom.
* Code: Ordered list style (Defines rendering characteristics for ordered lists.)
* Code: Arabic
  * Definition: List is ordered using Arabic numerals: 1, 2, 3.
* Code: LittleRoman
  * Definition: List is ordered using little Roman numerals: i, ii, iii.
* Code: BigRoman
  * Definition: List is ordered using big Roman numerals: I, II, III.
* Code: LittleAlpha
  * Definition: List is ordered using little alpha characters: a, b, c.
* Code: BigAlpha
  * Definition: List is ordered using big alpha characters: A, B, C.
* Code: Unordered list style (Defines rendering characteristics for unordered lists.)
* Code: Disc
  * Definition: List bullets are simple solid discs.
* Code: Circle
  * Definition: List bullets are hollow discs.
* Code: Square
  * Definition: List bullets are solid squares.

Local extensions to the styleType vocabulary domain must follow the following convention: `\[x\]\[A-Za-z\]\[A-Za-z0-9\]\*` (first character is "x", second character is an upper or lower case A-Z, remaining characters are any combination of upper and lower case letters or numbers).

The styleCode attribute can contain multiple values, separated by white space. Where an element containing a styleCode attribute is nested within another element containing a styleCode attribute, the style effects are additive, as in the following example:

Example 8

```
<section>
   <text><content styleCode="Bold">This is rendered bold, 
    <content styleCode="Italics">this is rendered bold and 
    italicized,</content> this is rendered bold. </content>
    <content styleCode="Bold Italics">This is also rendered 
    bold and italicized.</content>
   </text>
</section>

```

### Referencing in and out of the narrative block

To summarize the mechanisms for referencing in and out of the CDA Narrative Block:

* CDA entries can point in to the `<content>` element of the CDA Narrative Block (see [<content>](#content)).
* The `<linkHtml>` element of the CDA Narrative Block can reference targets that are either internal or external to the document (see [<linkHtml>](#linkhtml)).
* The `<footnoteRef>` element of the CDA Narrative Block can reference a `<footnote>` element in the same or different CDA Narrative Block of the same document (see [<footnote> and <footnoteRef>](#footnote-and-footnoteref)).
* The `<renderMultiMedia>` element of the CDA Narrative Block can point out to CDA ObservationMedia and RegionOfInterest entries of the same document (see [<renderMultiMedia>](#rendermultimedia)).

