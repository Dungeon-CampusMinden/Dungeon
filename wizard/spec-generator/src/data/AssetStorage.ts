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
      const request = action(transaction.objectStore(STORE_NAME));
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  } finally {
    db.close();
  }
}

export class AssetStorage {
  /** Stores (or overwrites) the binary content of an asset under its content hash id. */
  static async putAssetFile(file: File): Promise<string> {
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
  static async getAssetFile(id: string): Promise<StoredAssetFile | null> {
    const entry = await runTransaction<StoredAssetFile | undefined>("readonly", (store) => store.get(id));
    return entry ?? null;
  }

  static async deleteAssetFile(id: string): Promise<void> {
    await runTransaction("readwrite", (store) => store.delete(id));
  }

  /** Returns the ids of all assets whose content is currently stored in the IndexedDB. */
  static async listAssetIds(): Promise<string[]> {
    const keys = await runTransaction<IDBValidKey[]>("readonly", (store) => store.getAllKeys());
    return keys.map((key) => String(key));
  }
}
