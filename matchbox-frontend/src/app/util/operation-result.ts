export class OperationResult {

  operationOutcome: fhir.r4.OperationOutcome;
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
      undefined,
      undefined,
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
  line?: number;
  col?: number;
  sliceInfo: string[] = [];
  markdown: boolean;

  constructor(severity: IssueSeverity, code: string, text: string, expression?: string, line?: number, col?: number, sliceInfo?: string[], markdown?: boolean) {
    this.severity = severity;
    this.code = code;
    this.text = text;
    this.expression = expression;
    this.line = line;
    this.col = col;
    this.sliceInfo = sliceInfo ?? [];
    this.markdown = markdown;
  }

  static fromOoIssue(ooIssue: fhir.r4.OperationOutcomeIssue): Issue {
    let expression: string;
    if (ooIssue.expression && ooIssue.expression.length) {
      expression = ooIssue.expression[0];
    } else if (ooIssue.location && ooIssue.location.length) {
      expression = ooIssue.location[0];
    }

    const sliceCutIndex = ooIssue.diagnostics?.indexOf('Slice info: 1.)');
    let text: string;
    let sliceInfo: string[] = null;
    if (sliceCutIndex >= 0) {
      text = ooIssue.diagnostics.substring(0, sliceCutIndex).trimEnd();
      sliceInfo = ooIssue.diagnostics.substring(sliceCutIndex + 15).trimStart().split(/\d+[.][)]/);
    } else {
      text = ooIssue.diagnostics;
    }
    let markdown = Issue.getExtensionStringValue(ooIssue, "http://hl7.org/fhir/StructureDefinition/rendering-style") == "markdown";
    return new Issue(
      ooIssue.severity as IssueSeverity,
      ooIssue.code,
      text,
      expression,
      Issue.getLineNo(ooIssue),
      Issue.getColNo(ooIssue),
      sliceInfo,
      markdown
    );
  }

  static getLineNo(issue: fhir.r4.OperationOutcomeIssue): number | undefined {
    const line = Issue.getExtensionIntValue(
      issue,
      'http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line'
    );
    return (line && line > 0) ? line : undefined;
  }

  static getColNo(issue: fhir.r4.OperationOutcomeIssue): number | undefined {
    const col = Issue.getExtensionIntValue(issue, 'http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-col');
    return (col && col > 0) ? col : undefined;
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
