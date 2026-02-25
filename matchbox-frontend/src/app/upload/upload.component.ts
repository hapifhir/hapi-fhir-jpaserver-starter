import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { UploadedFile } from './uploaded-file';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class UploadComponent {
  @Output() addFiles = new EventEmitter<UploadedFile>();

  dragCounter = 0;

  public selectedFile: UploadedFile | null = null;

  constructor(private readonly changeDetectorRef: ChangeDetectorRef) {}

  public clear() {
    this.selectedFile = null;
    this.changeDetectorRef.markForCheck();
  }

  onDrop(ev) {
    // Prevent default behavior (Prevent file from being opened)
    ev.preventDefault();
    this.dragCounter = 0;
    const files = ev.target.files || ev.dataTransfer.items;
    if (files) {
      for (const element of files) {
        // If dropped items aren't files, reject them
        if (element.kind === undefined || element.kind === 'file') {
          const file = element.getAsFile ? element.getAsFile() : element;
          this.setSelectedFile({ name: file.name, contentType: '', blob: file });
        }
        if (element.kind === 'string' || element.kind === 'text/uri-list') {
          this.fetchData(ev.dataTransfer.getData('URL'));
        }
      }
    }
  }

  async fetchData(url: string) {
    const res = await fetch(url, { cache: 'no-store' });
    const contentType = res.headers.get('Content-Type');
    const blob = (await res.blob()) as File;
    this.setSelectedFile({ name: url, contentType, blob });
  }

  setSelectedFile(file: UploadedFile) {
    this.addFiles.emit(file);
    this.selectedFile = file;
    this.changeDetectorRef.markForCheck();
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
