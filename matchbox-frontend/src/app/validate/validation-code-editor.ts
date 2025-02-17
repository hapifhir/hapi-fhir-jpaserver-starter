import {Ace} from "ace-builds";
import {Issue, IssueSeverity} from "../util/operation-result";
import {ValidationEntry} from "./validation-entry";
import {CodeEditorContent} from "./validate.component";

/**
 * A wrapper for the code editor used in validation, for convenience purpose.
 */
export class ValidationCodeEditor {

  constructor(private readonly editor: Ace.Editor,
              private readonly indentSpace: number) {
    this.editor.setReadOnly(true);
    this.editor.setTheme('ace/theme/textmate');
    this.editor.commands.removeCommand('find');
    this.editor.setOptions({
      tabSize: this.indentSpace,
      wrap: true,
      useWorker: false,
      useSvgGutterIcons: false,
    });
  }

  /**
   * Clears all annotations from the editor.
   */
  clearAnnotations(): void {
    this.editor.session.clearAnnotations();
  }

  /**
   * Sets annotations from a list of FHIR Issues.
   * @param issues FHIR Issues from an OperationOutcome.
   */
  setAnnotations(issues: Issue[]): void {
    const annotations = issues
      .filter((issue) => issue.line)
      .map((issue) => {
        let type;
        switch (issue.severity) {
          case IssueSeverity.Fatal:
          case IssueSeverity.Error:
            type = 'error';
            break;
          case IssueSeverity.Warning:
            type = 'warning';
            break;
          case IssueSeverity.Information:
            type = 'info';
            break;
        }
        return {
          row: issue.line - 1,
          column: issue.col,
          text: issue.text,
          type,
        };
      });
    this.editor.session.setAnnotations(annotations);
  }

  /**
   * Update the list of issues in the code editor from the current validation entry report.
   */
  updateEditorIssues(selectedEntry: ValidationEntry | null): void {
    // Remove old markers
    this.clearAnnotations();

    if (!selectedEntry?.result) {
      // If the report is not available yet, do nothing
      return;
    }
    // Add new markers
    this.setAnnotations(selectedEntry.result.issues);
  }

  /**
   * Scrolls the code editor to the location of an issue.
   * @param issue the FHIR Issue from an OperationOutcome.
   */
  scrollToIssueLocation(issue: Issue): void {
    this.editor.gotoLine(issue.line, issue.col, true);
    this.editor.scrollToLine(issue.line, false, true, () => {});
  }

  clearContent(): void {
    this.editor.setValue('', -1);
  }

  updateCodeEditorContent(selectedEntry: ValidationEntry | null,
                          editorContent: CodeEditorContent): void {
    if (!selectedEntry) {
      this.clearContent();
      this.clearAnnotations();
      return;
    }

    if (editorContent == CodeEditorContent.RESOURCE_CONTENT) {
      this.editor.setValue(selectedEntry.resource, -1);
      if (selectedEntry.mimetype === 'application/fhir+json') {
        this.editor.getSession().setMode('ace/mode/json');
      } else if (selectedEntry.mimetype === 'application/fhir+xml') {
        this.editor.getSession().setMode('ace/mode/xml');
      }
      this.updateEditorIssues(selectedEntry);
    } else if (editorContent == CodeEditorContent.MATCHSPARK_RESULT){
      this.editor.setValue(selectedEntry.aiRecommendation, -1);
      this.editor.getSession().setMode('ace/mode/txt');
    } else {
      if (selectedEntry.result !== undefined && 'operationOutcome' in selectedEntry.result) {
        this.editor.setValue(JSON.stringify(selectedEntry.result.operationOutcome, null, this.indentSpace), -1);
        this.editor.getSession().setMode('ace/mode/json');
      } else {
        this.clearContent();
      }
      this.clearAnnotations();
    }
  }
}
