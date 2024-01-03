export interface ITarEntry {
  name: string; // "package/package.json",
  mode: string; // "0100644 ",
  uid: number; // 0,
  gid: number; // 0,
  size: number; // 647,
  mtime: number; // 1641566058,
  checksum: number; // 13500,
  type: string; // "0",
  linkname: string; // "",
  ustarFormat: string; // "ustar",
  version: string; // "00",
  uname: string; // "",
  gname: string; // "",
  devmajor: number; //0,
  devminor: number; //0,
  namePrefix: string; // "",
  buffer: ArrayBuffer; // {}
  getBlobUrl: () => string; // ???
  readAsJSON: () => any;
  readAsString: () => string;
}
