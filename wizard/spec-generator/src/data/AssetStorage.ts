const DB_NAME = "spec-generator";
const DB_VERSION = 1;
const STORE_NAME = "assets";

export interface StoredAssetFile {
  id: string;
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
  putAssetFile(file: File): Promise<string>;
  getAssetFile(id: string): Promise<StoredAssetFile | null>;
  listAssetIds(): Promise<string[]>;
}

export class BrowserAssetStorage implements AssetStoragePort {
  /** Stores binary content by content hash and returns that private storage key. */
  async putAssetFile(file: File): Promise<string> {
    const contents = await file.arrayBuffer();
    const digest = await crypto.subtle.digest("SHA-256", contents);
    const hash = Array.from(new Uint8Array(digest), (byte) => byte.toString(16).padStart(2, "0")).join("");
    const id = hash.slice(0, 12);
    const blob = new Blob([contents], { type: file.type });
    const entry: StoredAssetFile = { id, name: file.name, mediaType: file.type, blob };
    await runTransaction("readwrite", (store) => store.put(entry));
    return id;
  }

  /** Returns the stored file for an asset id, or null if it is not available. */
  async getAssetFile(id: string): Promise<StoredAssetFile | null> {
    const entry = await runTransaction<StoredAssetFile | undefined>("readonly", (store) => store.get(id));
    return entry ?? null;
  }

  /** Returns the ids of all assets whose content is currently stored in the IndexedDB. */
  async listAssetIds(): Promise<string[]> {
    const keys = await runTransaction<IDBValidKey[]>("readonly", (store) => store.getAllKeys());
    return keys.map((key) => String(key));
  }
}
