import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
import * as ace from 'ace-builds';

@Component({
  selector: 'app-operation-outcome',
  templateUrl: './operation-outcome.component.html',
  styleUrls: ['./operation-outcome.component.scss'],
})
export class OperationOutcomeComponent implements AfterViewInit, OnInit {
  @Input() mode: string;
  @Input() title: string;
  outcome: fhir.r4.OperationOutcome;

  @ViewChild('editor') private editor: ElementRef<HTMLElement>;

  content: string;

  jsonLines: string[];
  padNo: number;

  aceEditor: ace.Ace.Editor;

  constructor() {
    this.mode = 'ace/mode/json';
  }

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    if (this.editor && this.editor.nativeElement) {
      ace.config.set('fontSize', '11px');
      ace.config.set(
        'basePath',
        'https://unpkg.com/ace-builds@1.4.12/src-noconflict'
      );
      this.aceEditor = ace.edit(this.editor.nativeElement);
      this.aceEditor.setOption('useWorker', false);
      this.aceEditor.setTheme('ace/theme/chrome');
      if (this.mode?.indexOf('xml') >= 0) {
        this.aceEditor.session.setMode('ace/mode/xml');
      } else {
        this.aceEditor.session.setMode('ace/mode/json');
      }
      this.aceEditor.session.setValue(this.content);
      this.aceEditor.setReadOnly(true);
      this.aceEditor.on('change', () => {
        console.log(this.aceEditor.getValue());
      });
      this.updateAnnotations();
    }
  }

  updateAnnotations() {
    const annotations: ace.Ace.Annotation[] = [];
    if (this.outcome && this.aceEditor) {
      const prevMarkers = this.aceEditor.session.getMarkers();
      if (prevMarkers) {
        const prevMarkersArr = Object.keys(prevMarkers);
        for (let item of prevMarkersArr) {
          this.aceEditor.session.removeMarker(prevMarkers[item].id);
        }
      }
      this.outcome.issue?.forEach((issue) =>
        this.aceEditor
          .getSession()
          .addMarker(
            new ace.Range(
              this.getLineNo(issue) - 1,
              0,
              this.getLineNo(issue) - 1,
              1
            ),
            this.getLineAceClass(issue),
            'fullLine',
            true
          )
      );
      this.outcome.issue?.forEach((issue) =>
        annotations.push({
          row: this.getLineNo(issue) - 1,
          column: 0,
          text: issue.diagnostics, // Or the Json reply from the parser
          type: this.getErrorType(issue), // also "warning" and "information"
        })
      );
      this.aceEditor.session.setAnnotations(annotations);
    }
  }

  getErrorType(issue: fhir.r4.OperationOutcomeIssue): string {
    switch (issue.severity) {
      case 'fatal':
      case 'error':
        return 'error';
      case 'warning':
      case 'information':
        return 'warning';
      //      case 'information': does not show
      //        return 'information'
    }
    return '';
  }

  @Input() set json(value: string) {
    this.content = value;
    this.ngAfterViewInit();
  }

  @Input() set operationOutcome(value: fhir.r4.OperationOutcome) {
    this.outcome = value;
    this.outcome.issue?.sort(
      (issue1, issue2) => this.getLineNo(issue1) - this.getLineNo(issue2)
    );
    this.ngAfterViewInit();
  }

  getJson(): String {
    return this.content;
  }

  getLineAceClass(issue: fhir.r4.OperationOutcomeIssue): string {
    return 'ace-highlight-' + issue?.severity;
  }

  getLineNo(issue: fhir.r4.OperationOutcomeIssue): number {
    if (issue.extension?.length > 0) {
      return issue.extension[0].valueInteger;
    }
    return 0;
  }

  hasSliceInfo(issue: fhir.r4.OperationOutcomeIssue): boolean {
    return issue?.diagnostics?.indexOf('Slice info:') >= 0;
  }

  getSliceInfo(issue: fhir.r4.OperationOutcomeIssue): string[] {
    let slice = issue?.diagnostics
      .substring(issue?.diagnostics?.indexOf('Slice info: 1.)') + 15)
      .trimLeft();
    let regexp = new RegExp('[\\d]+[\\.][\\)]');
    return slice.split(regexp);
  }

  getLineFromExtension(issue: fhir.r4.OperationOutcomeIssue): string {
    if (issue.extension?.length > 0) {
      return 'L' + issue.extension[0].valueInteger;
    }
    return '';
  }

  getLocation(issue: fhir.r4.OperationOutcomeIssue): string {
    if (issue.location?.length > 0) {
      return issue.location[0];
    }
    return '';
  }

  scroll(line: number) {
    line -= 1;
    if (line < 0) {
      line = 0;
    }
    this.aceEditor.scrollToLine(line, false, true, null);
  }
}
