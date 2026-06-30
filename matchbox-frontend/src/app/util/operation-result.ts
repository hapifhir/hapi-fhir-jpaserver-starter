export class OperationResult {

  operationOutcome: fhir.r4.OperationOutcome | undefined;
  issues: Issue[];

  constructor(issues?: Issue[], operationOutcome?: fhir.r4.OperationOutcome) {
    this.operationOutcome = operationOutcome;
    this.issues = issues ?? [];
  }

  static fromOperationOutcome(operationOutcome: fhir.r4.OperationOutcome): OperationResult {
    const issues = operationOutcome.issue?.map(ooIssue => Issue.fromOoIssue(ooIssue));
    return new OperationResult(issues, operationOutcome);
  }

  static fromMatchboxError(error: string): OperationResult {
    const result = new OperationResult();
    result.issues.push(new Issue(
      IssueSeverity.Fatal,
      'matchbox',
      error,
      0,
      0,
      undefined,
      undefined
    ));
    return result;
  }
}

export class Issue {
  severity: IssueSeverity;
  code: string;
  text: string;
  expression?: string;
  line: number;
  col: number;
  sliceInfo: string[] = [];
  markdown: boolean;
  details: string;

  constructor(
    severity: IssueSeverity,
    code: string,
    text: string,
    line: number,
    col: number,
    expression?: string,
    sliceInfo?: string[],
    markdown?: boolean,
    details?: string
  ) {
    this.severity = severity;
    this.code = code;
    this.text = text;
    this.expression = expression;
    this.line = line;
    this.col = col;
    this.sliceInfo = sliceInfo ?? [];
    this.markdown = markdown ?? false;
    this.details = details ?? "";
    if (markdown && this.text.includes('```markdown')) {
      this.text = this.text.substring(11, this.text.length - 3);
    }
  }

  static fromOoIssue(ooIssue: fhir.r4.OperationOutcomeIssue): Issue {
    let expression: string;
    if (ooIssue.expression && ooIssue.expression.length) {
      expression = ooIssue.expression[0];
    } else if (ooIssue.location && ooIssue.location.length) {
      expression = ooIssue.location[0];
    } else {
      expression = '';
    }

    const sliceCutIndex = ooIssue.diagnostics?.indexOf('Slice info: 1.)') ?? -1;
    let text: string;
    let sliceInfo: string[] | undefined = undefined;
    if (sliceCutIndex >= 0) {
      text = ooIssue.diagnostics!!.substring(0, sliceCutIndex).trimEnd();
      sliceInfo = ooIssue.diagnostics!!
        .substring(sliceCutIndex + 15)
        .trimStart()
        .split(/\d+[.][)]/);
    } else {
      text = ooIssue.diagnostics ?? "";
    }
    let markdown =
      Issue.getExtensionStringValue(ooIssue, 'http://hl7.org/fhir/StructureDefinition/rendering-style') == 'markdown';
    let details = ooIssue.details ? ooIssue.details.text : undefined;
    return new Issue(
      ooIssue.severity as IssueSeverity,
      ooIssue.code,
      text,
      Issue.getLineNo(ooIssue),
      Issue.getColNo(ooIssue),
      expression,
      sliceInfo,
      markdown,
      details
    );
  }

  static getLineNo(issue: fhir.r4.OperationOutcomeIssue): number {
    const line = Issue.getExtensionIntValue(
      issue,
      'http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line'
    );
    return line && line > 0 ? line : 0;
  }

  static getColNo(issue: fhir.r4.OperationOutcomeIssue): number {
    const col = Issue.getExtensionIntValue(issue, 'http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-col');
    return col && col > 0 ? col : 0;
  }

  static getExtensionIntValue(issue: fhir.r4.OperationOutcomeIssue, url: string): number | undefined {
    if (!issue.extension) {
      return undefined;
    }
    for (const ext of issue.extension) {
      if (ext.url === url) {
        return ext.valueInteger;
      }
    }
    return undefined;
  }

  static getExtensionStringValue(issue: fhir.r4.OperationOutcomeIssue, url: string): string | undefined {
    if (!issue.extension) {
      return undefined;
    }
    for (const ext of issue.extension) {
      if (ext.url === url) {
        return ext.valueString;
      }
    }
    return undefined;
  }
}

export const enum IssueSeverity {
  Fatal = 'fatal',
  Error = 'error',
  Warning = 'warning',
  Information = 'information'
}
