import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";
import {Issue, OperationResult} from "../util/operation-result";

const SEVERITY_ORDER = ['fatal', 'error', 'warning', 'information'];

@Component({
  selector: 'app-operation-result',
  templateUrl: './operation-result.component.html',
  styleUrls: ['./operation-result.component.scss'],
})
export class OperationResultComponent implements OnInit {
  @Output() select: EventEmitter<Issue> = new EventEmitter();
  result: OperationResult;
  reactsToClick: boolean = false;

  @Input() set operationResult(value: OperationResult) {
    this.result = value;
    if (this.result && this.result.issues.length) {
      this.result.issues.sort(OperationResultComponent.sortIssues);
    }
  }

  constructor(private sanitized: DomSanitizer) {
  }

  ngOnInit(): void {
    this.reactsToClick = this.select.observed;
  }

  static sortIssues(issue1: Issue, issue2: Issue): number {
    // Sort by severity, then line number
    const severityDiff = SEVERITY_ORDER.indexOf(issue1.severity) - SEVERITY_ORDER.indexOf(issue2.severity);
    if (severityDiff !== 0) {
      return severityDiff;
    }
    return (issue1.line ?? 0) - (issue2.line ?? 0);
  }

  getTemplateHeaderLine(issue: Issue): SafeHtml {
    let line = '';
    if (issue.code) {
      line += `<span class="code"> [${issue.code}]</span>`;
    }
    line += ': ';
    const items = [];
    if (issue.line) {
      items.push(`line ${issue.line}`);
    }
    if (issue.col) {
      items.push(`column ${issue.col}`);
    }
    if (issue.expression) {
      items.push(`in <code>${issue.expression}</code>`);
    }
    if (items.length) {
      line += items.join(', ') + ":";
    }
    return this.sanitized.bypassSecurityTrustHtml(line);
  }
}

