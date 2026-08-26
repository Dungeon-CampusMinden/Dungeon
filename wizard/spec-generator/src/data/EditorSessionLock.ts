const EDITOR_SESSION_LOCK = "dungeon-wizard-editor-session";

export interface EditorSessionLock {
  release(): void;
}

export type EditorSessionLockResult =
  | { status: "acquired"; lock: EditorSessionLock }
  | { status: "held" | "unsupported" };

/**
 * Acquires the single editor-session lock for this browser profile.
 * Reports whether another tab holds the lock or the browser lacks Web Locks.
 */
export async function acquireEditorSessionLock(): Promise<EditorSessionLockResult> {
  if (!navigator.locks) return { status: "unsupported" };
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
  if (!acquired) return { status: "held" };
  return {
    status: "acquired",
    lock: {
      release: () => {
        release?.();
      },
    },
  };
}
