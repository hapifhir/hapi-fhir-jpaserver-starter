import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

export interface IDroppedBlob {
  blob: File;
  name: string;
  contentType: string;
}

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class UploadComponent {
  @Output() addFiles = new EventEmitter<IDroppedBlob>();

  dragCounter = 0;

  @Output() selectedFile: IDroppedBlob | null = null;

  @Input() clear() {
    this.selectedFile = null;
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

  setSelectedFile(file: IDroppedBlob) {
    this.addFiles.emit(file);
    this.selectedFile = file;
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
