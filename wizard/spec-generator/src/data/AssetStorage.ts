import { browserDraftExists, withBrowserDraftStoreLock } from "./DraftStorage";

const DB_NAME = "spec-generator";
const DB_VERSION = 2;
const STORE_NAME = "draft-assets-v2";

export interface StoredAssetFile {
  id: string;
  draftId: string;
  storageKey: string;
  name: string;
  mediaType: string;
  blob: Blob;
}

function openDatabase(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: "id" });
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
    request.onblocked = () => reject(new Error("Die lokale Dateidatenbank ist blockiert."));
  });
}

async function runTransaction<T>(
  mode: IDBTransactionMode,
  action: (store: IDBObjectStore) => IDBRequest<T>,
): Promise<T> {
  const db = await openDatabase();
  try {
    return await new Promise<T>((resolve, reject) => {
      const transaction = db.transaction(STORE_NAME, mode);
      let result: T;
      transaction.oncomplete = () => resolve(result);
      transaction.onerror = () => reject(transaction.error ?? new Error("Dateizugriff fehlgeschlagen."));
      transaction.onabort = () => reject(transaction.error ?? new Error("Dateizugriff abgebrochen."));
      try {
        const request = action(transaction.objectStore(STORE_NAME));
        request.onsuccess = () => {
          result = request.result;
        };
      } catch (error) {
        transaction.abort();
        reject(error);
      }
    });
  } finally {
    db.close();
  }
}

export interface AssetStoragePort {
  putAssetFile(draftId: string, file: File): Promise<string>;
  getAssetFile(draftId: string, storageKey: string): Promise<StoredAssetFile | null>;
  listAssetIds(draftId: string): Promise<string[]>;
}

/** Removes every private browser upload belonging to one draft in one transaction. */
export async function deleteDraftAssetFiles(draftId: string): Promise<void> {
  const db = await openDatabase();
  try {
    await new Promise<void>((resolve, reject) => {
      const transaction = db.transaction(STORE_NAME, "readwrite");
      const request = transaction.objectStore(STORE_NAME).openCursor();
      transaction.oncomplete = () => resolve();
      transaction.onerror = () => reject(transaction.error ?? new Error("Dateien konnten nicht gelöscht werden."));
      transaction.onabort = () => reject(transaction.error ?? new Error("Dateilöschung wurde abgebrochen."));
      request.onsuccess = () => {
        const cursor = request.result;
        if (cursor === null) return;
        const entry = cursor.value as StoredAssetFile;
        const ownedKey = typeof cursor.primaryKey === "string"
          && cursor.primaryKey.startsWith(`${draftId}:`);
        if (entry.draftId === draftId || ownedKey) cursor.delete();
        cursor.continue();
      };
      request.onerror = () => transaction.abort();
    });
  } finally {
    db.close();
  }
}

export class BrowserAssetStorage implements AssetStoragePort {
  /** Stores binary content by content hash and returns that private storage key. */
  async putAssetFile(draftId: string, file: File): Promise<string> {
    const contents = await file.arrayBuffer();
    const digest = await crypto.subtle.digest("SHA-256", contents);
    const hash = Array.from(new Uint8Array(digest), (byte) => byte.toString(16).padStart(2, "0")).join("");
    const id = `${draftId}:${hash}`;
    const blob = new Blob([contents], { type: file.type });
    const entry: StoredAssetFile = { id, draftId, storageKey: hash, name: file.name, mediaType: file.type, blob };
    try {
      return await withBrowserDraftStoreLock(async () => {
        if (!browserDraftExists(draftId)) {
          throw new Error("Der Entwurf ist nicht mehr vorhanden.");
        }
        await runTransaction("readwrite", (store) => store.put(entry));
        return hash;
      });
    } catch (cause) {
      if (cause instanceof Error && cause.message === "Der Entwurf ist nicht mehr vorhanden.") {
        throw cause;
      }
      throw new Error("Die Datei konnte nicht sicher im Entwurf gespeichert werden.");
    }
  }

  deleteDraftFiles(draftId: string): Promise<void> {
    return deleteDraftAssetFiles(draftId);
  }

  /** Returns the stored file for an asset id, or null if it is not available. */
  async getAssetFile(draftId: string, storageKey: string): Promise<StoredAssetFile | null> {
    const entry = await runTransaction<StoredAssetFile | undefined>("readonly", (store) =>
      store.get(`${draftId}:${storageKey}`));
    return entry ?? null;
  }

  /** Returns the ids of all assets whose content is currently stored in the IndexedDB. */
  async listAssetIds(draftId: string): Promise<string[]> {
    const entries = await runTransaction<StoredAssetFile[]>("readonly", (store) => store.getAll());
    return entries.filter((entry) => entry.draftId === draftId).map((entry) => entry.storageKey);
  }
}
