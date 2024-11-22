import { Component } from '@angular/core';
import packageJson from '../../../package.json';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
    standalone: false
})
export class HomeComponent {
  public version: string = packageJson.version;
}
