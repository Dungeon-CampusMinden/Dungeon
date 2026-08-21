const EDITOR_SESSION_LOCK = "dungeon-wizard-editor-session";

export interface EditorSessionLock {
  release(): void;
}

/**
 * Acquires the single editor-session lock for this browser profile.
 * Resolves to null when another tab currently holds the lock.
 * Browsers without Web Locks receive an unrestricted no-op lock.
 */
export async function acquireEditorSessionLock(): Promise<EditorSessionLock | null> {
  if (!navigator.locks) return { release: () => {} };
  let release: (() => void) | null = null;
  const acquired = await new Promise<boolean>((resolve) => {
    void navigator.locks.request(EDITOR_SESSION_LOCK, { ifAvailable: true }, (lock) => {
      if (!lock) {
        resolve(false);
        return;
      }
      resolve(true);
      return new Promise<void>((keepLocked) => {
        release = keepLocked;
      });
    });
  });
  if (!acquired) return null;
  return {
    release: () => {
      release?.();
    },
  };
}
