import { WIZARD_DRAFT_VERSION, type WizardDraft } from "./WizardDraft";
import { isTabId, VALIDATED_TAB_IDS, type ValidatedTabId } from "./Tabs";

export const DB_NAME = "dungeon-wizard-v1";
export const DB_VERSION = 1;
export const DRAFT_STORE = "drafts";
export const UPLOAD_STORE = "uploads";
export const UPLOAD_BY_DRAFT = "byDraft";

export function openWizardDatabase(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = () => {
      const db = request.result;
      db.createObjectStore(DRAFT_STORE, { keyPath: "draftId" });
      const uploads = db.createObjectStore(UPLOAD_STORE, { keyPath: ["draftId", "storageKey"] });
      uploads.createIndex(UPLOAD_BY_DRAFT, "draftId");
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
    request.onblocked = () => reject(new Error("Der lokale Entwurfsspeicher ist blockiert."));
  });
}

export function transactionDone(transaction: IDBTransaction): Promise<void> {
  return new Promise((resolve, reject) => {
    transaction.oncomplete = () => resolve();
    transaction.onerror = () => reject(transaction.error ?? new Error("Der lokale Speicher ist fehlgeschlagen."));
    transaction.onabort = () => reject(transaction.error ?? new Error("Der lokale Speicher wurde abgebrochen."));
  });
}

export function requestResult<T>(request: IDBRequest<T>): Promise<T> {
  return new Promise((resolve, reject) => {
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

export interface DraftSummary { draftId: string; title: string; savedAt?: string; }
export interface DraftStoragePort {
  list(): Promise<DraftSummary[]>;
  load(draftId: string): Promise<WizardDraft | null>;
  loadForEditing(draftId: string): Promise<WizardDraft | null>;
  save(draft: WizardDraft): Promise<WizardDraft>;
  delete(draftId: string): Promise<void>;
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);
const isString = (value: unknown): value is string => typeof value === "string";
const isNumber = (value: unknown): value is number => typeof value === "number" && Number.isFinite(value);
const isOptionalString = (value: unknown) => value === undefined || isString(value);
const isStringArray = (value: unknown): value is string[] => Array.isArray(value) && value.every(isString);

function isResource(value: unknown): boolean {
  if (!isRecord(value) || !isString(value.id) || !isString(value.title)) return false;
  if (value.kind === "inline_text") return isString(value.text);
  return value.kind === "asset" && isString(value.assetId);
}

function isInformationSource(value: unknown): boolean {
  return isRecord(value) && isString(value.id) && isString(value.surfaceId)
    && Array.isArray(value.resources) && value.resources.every(isResource);
}

function isRiddleInput(value: unknown): boolean {
  if (!isRecord(value) || !isString(value.id)) return false;
  if (value.type === "collection") return isString(value.informationSourceId);
  return value.type === "numeric" && isString(value.surfaceId) && isString(value.answer)
    && typeof value.showDigitCount === "boolean";
}

function isHint(value: unknown): boolean {
  return isRecord(value) && isString(value.id) && isString(value.title) && isString(value.text)
    && ["orientation", "approach", "solution"].includes(String(value.severity));
}

function isDeerProject(value: unknown): boolean {
  if (!isRecord(value) || value.formatVersion !== "0.5") return false;
  const metadata = value.metadata;
  const learning = value.learningDesign;
  const session = value.session;
  const scenario = value.scenario;
  const graph = value.riddleGraph;
  return isRecord(metadata) && isString(metadata.id) && isString(metadata.title) && isString(metadata.locale)
    && isOptionalString(metadata.description) && isOptionalString(metadata.author)
    && isOptionalString(metadata.operatorEmail)
    && isRecord(learning) && Array.isArray(learning.objectives)
    && learning.objectives.every((item) => isRecord(item) && isString(item.id) && isString(item.description))
    && isStringArray(learning.debriefPrompts)
    && isRecord(session) && isString(session.targetAudience) && isString(session.priorKnowledge)
    && isRecord(session.playerCount) && isNumber(session.playerCount.min) && isNumber(session.playerCount.max)
    && isRecord(session.time) && isNumber(session.time.limitMinutes)
    && (session.time.limitMode === "hard" || session.time.limitMode === "soft")
    && isRecord(scenario) && isString(scenario.themeId) && isString(scenario.mission)
    && isStringArray(scenario.introText) && isStringArray(scenario.successText)
    && (scenario.failureText === undefined || isStringArray(scenario.failureText))
    && Array.isArray(value.surfaces) && value.surfaces.every((item) =>
      isRecord(item) && isString(item.id) && isString(item.title)
      && ["world", "container", "keypad", "door"].includes(String(item.kind)))
    && isRecord(graph) && Array.isArray(graph.nodes) && graph.nodes.every((item) =>
      isRecord(item) && isString(item.id)
      && (item.kind === "start" || (item.kind === "end" && isString(item.surfaceId))
        || (item.kind === "riddle" && isString(item.riddleId))))
    && Array.isArray(graph.edges) && graph.edges.every((item) =>
      isRecord(item) && isString(item.from) && isString(item.to))
    && Array.isArray(value.riddles) && value.riddles.every((riddle) =>
      isRecord(riddle) && isString(riddle.id) && isString(riddle.title)
      && ["easy", "medium", "hard"].includes(String(riddle.difficulty))
      && isStringArray(riddle.learningObjectiveIds) && isNumber(riddle.estimatedMinutes)
      && Array.isArray(riddle.informationSources)
      && riddle.informationSources.every(isInformationSource)
      && Array.isArray(riddle.inputs) && riddle.inputs.every(isRiddleInput)
      && Array.isArray(riddle.hints) && riddle.hints.every(isHint))
    && Array.isArray(value.assets) && value.assets.every((asset) =>
      isRecord(asset) && isString(asset.id) && isString(asset.path)
      && (asset.mediaType === "image/png" || asset.mediaType === "image/jpeg")
      && isRecord(asset.source) && isString(asset.source.license) && isOptionalString(asset.source.attribution));
}

export function assertWizardDraft(value: unknown): asserts value is WizardDraft {
  if (!isRecord(value) || value.draftVersion !== WIZARD_DRAFT_VERSION
    || !isString(value.draftId) || value.draftId.length === 0
    || (value.seed !== undefined && (!Number.isSafeInteger(value.seed) || (value.seed as number) < 0))
    || !isDeerProject(value.project)
    || !isRecord(value.graphLayout) || !Object.values(value.graphLayout).every((position) =>
      isRecord(position) && isNumber(position.x) && isNumber(position.y))
    || !isRecord(value.ui) || !isTabId(value.ui.activeTab) || !isRecord(value.ui.touchedTabs)
    || Object.keys(value.ui.touchedTabs).length !== VALIDATED_TAB_IDS.length
    || !Object.keys(value.ui.touchedTabs).every((key) => VALIDATED_TAB_IDS.includes(key as ValidatedTabId))
    || !Object.values(value.ui.touchedTabs).every((touched) => typeof touched === "boolean")
    || !isRecord(value.uploads) || !Object.values(value.uploads).every((upload) =>
      isRecord(upload) && isString(upload.storageKey) && /^[0-9a-f]{64}$/.test(upload.storageKey)
      && isString(upload.originalName))
    || (value.savedAt !== undefined && !isString(value.savedAt))) {
    throw new Error("Der gespeicherte Entwurf ist unvollständig oder beschädigt.");
  }
}

function readPrivateDraft(value: unknown): { draft: WizardDraft; migrated: boolean } {
  let candidate = value;
  let migrated = false;
  if (isRecord(value) && value.draftVersion === WIZARD_DRAFT_VERSION
    && isRecord(value.project) && value.project.formatVersion === "0.4") {
    candidate = structuredClone(value);
    (candidate as Record<string, unknown> & { project: Record<string, unknown> })
      .project.formatVersion = "0.5";
    migrated = true;
  }
  assertWizardDraft(candidate);
  return { draft: candidate, migrated };
}

function pruneUnreferencedUploads(transaction: IDBTransaction, draft: WizardDraft): void {
  const retainedStorageKeys = new Set(
    Object.values(draft.uploads).map((upload) => upload.storageKey),
  );
  const uploadStore = transaction.objectStore(UPLOAD_STORE);
  const request = uploadStore.index(UPLOAD_BY_DRAFT)
    .openKeyCursor(IDBKeyRange.only(draft.draftId));
  request.onsuccess = () => {
    const cursor = request.result;
    if (cursor === null) return;
    const primaryKey = cursor.primaryKey;
    const storageKey = Array.isArray(primaryKey) ? primaryKey[1] : undefined;
    if (typeof storageKey !== "string") {
      transaction.abort();
      return;
    }
    if (!retainedStorageKeys.has(storageKey)) uploadStore.delete(primaryKey);
    cursor.continue();
  };
}

export class BrowserDraftStorage implements DraftStoragePort {
  async list(): Promise<DraftSummary[]> {
    const db = await openWizardDatabase();
    try {
      const transaction = db.transaction(DRAFT_STORE, "readonly");
      const drafts = await requestResult(transaction.objectStore(DRAFT_STORE).getAll()) as unknown[];
      await transactionDone(transaction);
      return drafts.map((value) => {
        const { draft } = readPrivateDraft(value);
        return { draftId: draft.draftId, title: draft.project.metadata.title,
          ...(draft.savedAt ? { savedAt: draft.savedAt } : {}) };
      }).sort((a, b) => (b.savedAt ?? "").localeCompare(a.savedAt ?? ""));
    } finally { db.close(); }
  }

  async load(draftId: string): Promise<WizardDraft | null> {
    const db = await openWizardDatabase();
    try {
      const transaction = db.transaction(DRAFT_STORE, "readonly");
      const value = await requestResult(transaction.objectStore(DRAFT_STORE).get(draftId)) as unknown;
      await transactionDone(transaction);
      if (value === undefined) return null;
      return structuredClone(readPrivateDraft(value).draft);
    } finally { db.close(); }
  }

  async loadForEditing(draftId: string): Promise<WizardDraft | null> {
    const db = await openWizardDatabase();
    try {
      const transaction = db.transaction([DRAFT_STORE, UPLOAD_STORE], "readwrite");
      const completion = transactionDone(transaction);
      try {
        const value = await requestResult(
          transaction.objectStore(DRAFT_STORE).get(draftId),
        ) as unknown;
        if (value === undefined) {
          await completion;
          return null;
        }
        const { draft, migrated } = readPrivateDraft(value);
        if (migrated) transaction.objectStore(DRAFT_STORE).put(draft);
        pruneUnreferencedUploads(transaction, draft);
        await completion;
        return structuredClone(draft);
      } catch (cause) {
        try { transaction.abort(); } catch {
          // The failing request may already have aborted or completed the transaction.
        }
        await completion.catch(() => {});
        throw cause;
      }
    } finally { db.close(); }
  }

  async save(draft: WizardDraft): Promise<WizardDraft> {
    assertWizardDraft(draft);
    const stored = { ...structuredClone(draft), savedAt: new Date().toISOString() };
    const db = await openWizardDatabase();
    try {
      const transaction = db.transaction([DRAFT_STORE, UPLOAD_STORE], "readwrite");
      transaction.objectStore(DRAFT_STORE).put(stored);
      pruneUnreferencedUploads(transaction, stored);
      await transactionDone(transaction);
      return stored;
    } finally { db.close(); }
  }

  async delete(draftId: string): Promise<void> {
    const db = await openWizardDatabase();
    try {
      const transaction = db.transaction([DRAFT_STORE, UPLOAD_STORE], "readwrite");
      transaction.objectStore(DRAFT_STORE).delete(draftId);
      const request = transaction.objectStore(UPLOAD_STORE).index(UPLOAD_BY_DRAFT)
        .openKeyCursor(IDBKeyRange.only(draftId));
      request.onsuccess = () => {
        const cursor = request.result;
        if (cursor === null) return;
        transaction.objectStore(UPLOAD_STORE).delete(cursor.primaryKey);
        cursor.continue();
      };
      await transactionDone(transaction);
    } finally { db.close(); }
  }
}
