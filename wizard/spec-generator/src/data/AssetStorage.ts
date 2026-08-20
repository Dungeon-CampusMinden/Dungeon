import {
  openWizardDatabase,
  requestResult,
  transactionDone,
  UPLOAD_BY_DRAFT,
  UPLOAD_STORE,
} from "./DraftStorage";

const MAX_UPLOAD_BYTES = 16 * 1024 * 1024;

export interface StoredAssetFile {
  draftId: string;
  storageKey: string;
  name: string;
  mediaType: string;
  blob: Blob;
}

export interface AssetStoragePort {
  putAssetFile(draftId: string, file: File): Promise<string>;
  getAssetFile(draftId: string, storageKey: string): Promise<StoredAssetFile | null>;
  listAssetIds(draftId: string): Promise<string[]>;
}

export class BrowserAssetStorage implements AssetStoragePort {
  async putAssetFile(draftId: string, file: File): Promise<string> {
    if (file.type !== "image/png" && file.type !== "image/jpeg") {
      throw new Error("Es werden nur PNG- und JPEG-Dateien unterstützt.");
    }
    if (file.size === 0) throw new Error("Die ausgewählte Datei ist leer.");
    if (file.size > MAX_UPLOAD_BYTES) throw new Error("Die Datei darf höchstens 16 MiB groß sein.");

    const contents = await file.arrayBuffer();
    const digest = await crypto.subtle.digest("SHA-256", contents);
    const storageKey = Array.from(
      new Uint8Array(digest),
      (byte) => byte.toString(16).padStart(2, "0"),
    ).join("");
    const entry: StoredAssetFile = {
      draftId,
      storageKey,
      name: file.name,
      mediaType: file.type,
      blob: new Blob([contents], { type: file.type }),
    };
    const db = await openWizardDatabase();
    try {
      const transaction = db.transaction(UPLOAD_STORE, "readwrite");
      transaction.objectStore(UPLOAD_STORE).put(entry);
      await transactionDone(transaction);
      return storageKey;
    } finally { db.close(); }
  }

  async getAssetFile(draftId: string, storageKey: string): Promise<StoredAssetFile | null> {
    const db = await openWizardDatabase();
    try {
      const transaction = db.transaction(UPLOAD_STORE, "readonly");
      const entry = await requestResult(
        transaction.objectStore(UPLOAD_STORE).get([draftId, storageKey]),
      ) as StoredAssetFile | undefined;
      await transactionDone(transaction);
      return entry ?? null;
    } finally { db.close(); }
  }

  async listAssetIds(draftId: string): Promise<string[]> {
    const db = await openWizardDatabase();
    try {
      const transaction = db.transaction(UPLOAD_STORE, "readonly");
      const entries = await requestResult(
        transaction.objectStore(UPLOAD_STORE).index(UPLOAD_BY_DRAFT).getAll(draftId),
      ) as StoredAssetFile[];
      await transactionDone(transaction);
      return entries.map((entry) => entry.storageKey);
    } finally { db.close(); }
  }
}
