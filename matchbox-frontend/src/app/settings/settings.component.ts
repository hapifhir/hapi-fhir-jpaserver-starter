import { Component, OnInit } from '@angular/core';
import packageJson from '../../../package.json';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements OnInit {
  public version: string = packageJson.version;

  ngOnInit(): void {
    // Nothing to do here
  }
}
