export class OperationResult {

  operationOutcome: fhir.r4.OperationOutcome;
  issues: Issue[] = [];

  constructor(operationOutcome: fhir.r4.OperationOutcome) {
    this.operationOutcome = operationOutcome;
    this.issues = this.operationOutcome.issue?.map(ooIssue => new Issue(ooIssue));
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

  constructor(ooIssue: fhir.r4.OperationOutcomeIssue) {
    this.severity = ooIssue.severity as IssueSeverity;
    this.code = ooIssue.code;
    if (ooIssue.expression && ooIssue.expression.length) {
      this.expression = ooIssue.expression[0];
    } else if (ooIssue.location && ooIssue.location.length) {
      this.expression = ooIssue.location[0];
    }
    this.line = Issue.getLineNo(ooIssue);
    this.col = Issue.getColNo(ooIssue);

    const sliceCutIndex = ooIssue.diagnostics?.indexOf('Slice info: 1.)');
    if (sliceCutIndex >= 0) {
      this.text = ooIssue.diagnostics.substring(0, sliceCutIndex).trimEnd();
      this.sliceInfo = ooIssue.diagnostics.substring(sliceCutIndex + 15).trimStart().split(/\d+[.][)]/);
    } else {
      this.text = ooIssue.diagnostics;
    }
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
}

export const enum IssueSeverity {
  Fatal = 'fatal',
  Error = 'error',
  Warning = 'warning',
  Information = 'information'
}
