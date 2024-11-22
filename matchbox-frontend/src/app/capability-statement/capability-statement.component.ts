import { AfterViewInit, Component } from '@angular/core';
import { FhirConfigService } from '../fhirConfig.service';
import FhirClient from 'fhir-kit-client';
import ace, { Ace } from 'ace-builds';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/theme-textmate';
import {OperationResult} from "../util/operation-result";

const INDENT_SPACES = 4;

@Component({
    selector: 'app-capability-statement',
    templateUrl: './capability-statement.component.html',
    styleUrls: ['./capability-statement.component.scss'],
    standalone: false
})
export class CapabilityStatementComponent implements AfterViewInit {
  capabilityStatement: string | null = null;
  operationResult: OperationResult | null = null;
  client: FhirClient;
  editor: Ace.Editor;
  loading = true;

  constructor(private data: FhirConfigService) {
    this.client = data.getFhirClient();
  }

  ngAfterViewInit() {
    this.client
      .capabilityStatement()
      .then((data: fhir.r4.CapabilityStatement) => {
        this.loading = false;
        this.operationResult = null;
        this.editor = ace.edit('code');
        this.editor.setReadOnly(true);
        this.editor.setValue(JSON.stringify(data, null, INDENT_SPACES), -1);
        this.editor.getSession().setMode('ace/mode/json');
        this.editor.setTheme('ace/theme/textmate');
        this.editor.commands.removeCommand('find');
        this.editor.setOptions({
          maxLines: 10000,
          tabSize: INDENT_SPACES,
          wrap: true,
          useWorker: false,
        });
        this.editor.resize(true);
      })
      .catch((error) => {
        console.error(error);
        this.loading = false;
        this.capabilityStatement = null;
        if (this.editor) {
          this.editor.destroy();
          this.editor.container.remove();
        }
        this.editor = null;
        if (error.response?.data) {
          this.operationResult = OperationResult.fromOperationOutcome(error.response.data);
        } else {
          this.operationResult = OperationResult.fromMatchboxError(error.message);
        }
      });
  }
}
