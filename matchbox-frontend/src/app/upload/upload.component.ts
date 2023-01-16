import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';

export interface IDroppedBlob {
  blob: Blob;
  name: string;
  contentType: string;
}

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UploadComponent {
  @Output() addFiles = new EventEmitter<IDroppedBlob>();

  dragCounter = 0;

  checkStatus(response): boolean {
    if (!response.ok) {
      throw new Error(`HTTP ${response.status} - ${response.statusText}`);
    }
    return response;
  }

  onDrop(ev) {
    // Prevent default behavior (Prevent file from being opened)
    ev.preventDefault();
    this.dragCounter = 0;
    const files = ev.target.files || ev.dataTransfer.items;
    if (files) {
      for (let i = 0; i < files.length; i++) {
        // If dropped items aren't files, reject them
        if (files[i].kind === undefined || files[i].kind === 'file') {
          const file = files[i].getAsFile ? files[i].getAsFile() : files[i];
          this.addFiles.emit({ name: file.name, contentType: '', blob: file });
        }
        if (files[i].kind === 'string' || files[i].kind === 'text/uri-list') {
          var url = ev.dataTransfer.getData('URL');
          this.fetchData(url);
        }
      }
    }
  }

  async fetchData(url: string) {
    const res = await fetch(url, { cache: 'no-store' });
    const contentType = res.headers.get('Content-Type');
    const blob = await res.blob();
    this.addFiles.emit({ name: url, contentType, blob });
  }

  onDragOver(ev: DragEvent) {
    ev.preventDefault();
  }

  onDragEnter() {
    this.dragCounter++;
  }

  onDragLeave() {
    this.dragCounter--;
  }
}
