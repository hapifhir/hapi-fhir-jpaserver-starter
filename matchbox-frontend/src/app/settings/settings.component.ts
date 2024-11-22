import { Component } from '@angular/core';
import packageJson from '../../../package.json';

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
    standalone: false
})
export class SettingsComponent {
  public version: string = packageJson.version;
}
