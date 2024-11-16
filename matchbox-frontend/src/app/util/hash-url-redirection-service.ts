import { Injectable } from '@angular/core';

/**
 * A service that redirects the old URLs based on hash (HashLocationStrategy) to the new structure
 * (PathLocationStrategy).
 */
@Injectable({
  providedIn: 'root'
})
export class HashUrlRedirectionService {
  redirectHashUrl(): void {
    const url = window.location.href;
    window.location.replace(url.replace('#/', ''));
  }

  isHashUrl(): boolean {
    return window.location.href.indexOf('#/') > -1;
  }
}
