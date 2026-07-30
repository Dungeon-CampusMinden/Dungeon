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
  /** Stores (or overwrites) the binary content of an asset under the given asset id. */
  static async putAssetFile(id: string, file: File): Promise<void> {
    const blob = new Blob([await file.arrayBuffer()], { type: file.type });
    const entry: StoredAssetFile = { id, name: file.name, mediaType: file.type, blob };
    await runTransaction("readwrite", (store) => store.put(entry));
  }

  /** Returns the stored file for an asset id, or null if it is not available. */
  static async getAssetFile(id: string): Promise<StoredAssetFile | null> {
    const entry = await runTransaction<StoredAssetFile | undefined>("readonly", (store) => store.get(id));
    return entry ?? null;
  }

  static async deleteAssetFile(id: string): Promise<void> {
    await runTransaction("readwrite", (store) => store.delete(id));
  }
}
