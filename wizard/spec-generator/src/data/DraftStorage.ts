import {
  draftRevision,
  type DraftRevision,
  WIZARD_DRAFT_VERSION,
  type WizardDraft,
} from "./WizardDraft";
import { isTabId, VALIDATED_TAB_IDS, type ValidatedTabId } from "./Tabs";

const STORE_VERSION = "2" as const;
const DRAFT_STORE_KEY = "wizardDraftStore-v3";
const DRAFT_SAVE_LOCK = "dungeon-wizard-draft-store-v3-save";

export function browserDraftExists(draftId: string): boolean {
  return readStore().drafts[draftId] !== undefined;
}

export function withBrowserDraftStoreLock<T>(action: () => Promise<T>): Promise<T> {
  if (!("locks" in navigator) || navigator.locks === undefined) {
    return Promise.reject(new Error("Die benötigte Browsersperre ist nicht verfügbar."));
  }
  return navigator.locks.request(DRAFT_SAVE_LOCK, { mode: "exclusive" }, action);
}

interface DraftStoreEnvelope {
  storeVersion: typeof STORE_VERSION;
  draftOrder: string[];
  drafts: Record<string, WizardDraft>;
}

export interface DraftSummary {
  draftId: string;
  title: string;
  revision: DraftRevision;
  savedAt?: string;
}

export interface DraftStoragePort {
  list(): Promise<DraftSummary[]>;
  load(draftId: string): Promise<WizardDraft | null>;
  save(draft: WizardDraft): Promise<WizardDraft>;
  delete(draftId: string, revision: DraftRevision): Promise<void>;
}

export type DraftReloadReason = "revision-conflict" | "finalization-recovered";

export class DraftReloadRequiredError extends Error {
  readonly reason: DraftReloadReason;
  readonly title: string;

  constructor(reason: DraftReloadReason) {
    const recovered = reason === "finalization-recovered";
    super(recovered
      ? "Der lokale Wizard hat einen unterbrochenen Speichervorgang sicher wiederhergestellt. Dein geöffneter Stand kann deshalb veraltet sein und bleibt hier erhalten."
      : "Dieser Entwurf wurde inzwischen an anderer Stelle geändert. Dein vollständig geöffneter Stand bleibt hier erhalten.");
    this.name = "DraftReloadRequiredError";
    this.reason = reason;
    this.title = recovered
      ? "Ein Speichervorgang wurde wiederhergestellt"
      : "Der gespeicherte Entwurf wurde an anderer Stelle geändert";
  }
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);
const isString = (value: unknown): value is string => typeof value === "string";
const isNumber = (value: unknown): value is number =>
  typeof value === "number" && Number.isFinite(value);
const isOptionalString = (value: unknown) => value === undefined || isString(value);
const isStringArray = (value: unknown): value is string[] =>
  Array.isArray(value) && value.every(isString);

function isMetadata(value: unknown): boolean {
  return isRecord(value) && isString(value.id) && isString(value.title) && isString(value.locale)
    && isOptionalString(value.description) && isOptionalString(value.author);
}

function isLearningDesign(value: unknown): boolean {
  return isRecord(value)
    && Array.isArray(value.objectives)
    && value.objectives.every((objective) =>
      isRecord(objective) && isString(objective.id) && isString(objective.description))
    && isStringArray(value.debriefPrompts);
}

function isSession(value: unknown): boolean {
  if (!isRecord(value) || !isRecord(value.playerCount) || !isRecord(value.time)) return false;
  return isString(value.targetAudience)
    && isString(value.priorKnowledge)
    && isNumber(value.playerCount.min)
    && isNumber(value.playerCount.max)
    && isNumber(value.time.limitMinutes)
    && (value.time.limitMode === "hard" || value.time.limitMode === "soft");
}

function isScenario(value: unknown): boolean {
  return isRecord(value) && isString(value.themeId) && isString(value.mission)
    && isStringArray(value.introText) && isStringArray(value.successText)
    && (value.failureText === undefined || isStringArray(value.failureText));
}

function isSurface(value: unknown): boolean {
  return isRecord(value) && isString(value.id) && isString(value.title)
    && ["world", "container", "keypad", "door"].includes(String(value.kind));
}

function isGraphNode(value: unknown): boolean {
  if (!isRecord(value) || !isString(value.id)) return false;
  if (value.kind === "start") return true;
  if (value.kind === "end") return isString(value.surfaceId);
  return value.kind === "riddle" && isString(value.riddleId);
}

function isRiddleGraph(value: unknown): boolean {
  return isRecord(value)
    && Array.isArray(value.nodes) && value.nodes.every(isGraphNode)
    && Array.isArray(value.edges) && value.edges.every((edge) =>
      isRecord(edge) && isString(edge.from) && isString(edge.to));
}

function isResource(value: unknown): boolean {
  if (!isRecord(value) || !isString(value.id) || !isString(value.title)) return false;
  if (value.kind === "inline_text") return isString(value.text);
  return value.kind === "asset" && isString(value.assetId);
}

function isRiddleInput(value: unknown): boolean {
  if (!isRecord(value) || !isString(value.id)) return false;
  if (value.type === "collection") return isString(value.informationSourceId);
  return value.type === "numeric" && isString(value.surfaceId) && isString(value.answer)
    && typeof value.showDigitCount === "boolean";
}

function isInformationSource(value: unknown): boolean {
  return isRecord(value) && isString(value.id) && isString(value.surfaceId)
    && Array.isArray(value.resources) && value.resources.every(isResource);
}

function isHint(value: unknown): boolean {
  return isRecord(value) && isString(value.id) && isString(value.title) && isString(value.text)
    && ["orientation", "approach", "solution"].includes(String(value.severity));
}

function isRiddle(value: unknown): boolean {
  return isRecord(value) && isString(value.id) && isString(value.title)
    && ["easy", "medium", "hard"].includes(String(value.difficulty))
    && isStringArray(value.learningObjectiveIds) && isNumber(value.estimatedMinutes)
    && Array.isArray(value.informationSources)
    && value.informationSources.every(isInformationSource)
    && Array.isArray(value.inputs) && value.inputs.every(isRiddleInput)
    && Array.isArray(value.hints) && value.hints.every(isHint);
}

function isAsset(value: unknown): boolean {
  if (!isRecord(value) || !isRecord(value.source)) return false;
  return isString(value.id) && isString(value.path)
    && (value.mediaType === "image/png" || value.mediaType === "image/jpeg")
    && isString(value.source.license) && isOptionalString(value.source.attribution);
}

function isDeerProject(value: unknown): boolean {
  return isRecord(value) && value.formatVersion === "0.4"
    && isMetadata(value.metadata) && isLearningDesign(value.learningDesign)
    && isSession(value.session) && isScenario(value.scenario)
    && Array.isArray(value.surfaces) && value.surfaces.every(isSurface)
    && isRiddleGraph(value.riddleGraph)
    && Array.isArray(value.riddles) && value.riddles.every(isRiddle)
    && Array.isArray(value.assets) && value.assets.every(isAsset);
}

function isGraphLayout(value: unknown): boolean {
  return isRecord(value) && Object.values(value).every((position) =>
    isRecord(position) && isNumber(position.x) && isNumber(position.y));
}

function isUploads(value: unknown): boolean {
  return isRecord(value) && Object.values(value).every((upload) =>
    isRecord(upload) && isString(upload.storageKey) && /^[0-9a-f]{64}$/.test(upload.storageKey)
    && isString(upload.originalName));
}

function isUiState(value: unknown): boolean {
  if (!isRecord(value) || !isTabId(value.activeTab) || !isRecord(value.touchedTabs)) return false;
  const touchedKeys = Object.keys(value.touchedTabs);
  return touchedKeys.length === VALIDATED_TAB_IDS.length
    && touchedKeys.every((key) => VALIDATED_TAB_IDS.includes(key as ValidatedTabId))
    && Object.values(value.touchedTabs).every((touched) => typeof touched === "boolean");
}

function isFinalization(value: unknown): boolean {
  return isRecord(value) && Number.isSafeInteger(value.seed) && (value.seed as number) >= 0
    && isString(value.projectDirectory)
    && isString(value.finalizedAt)
    && (value.candidateHash === undefined
      || (isString(value.candidateHash) && /^[0-9a-f]{64}$/.test(value.candidateHash)))
    && isString(value.deerSha256) && /^[0-9a-f]{64}$/.test(value.deerSha256)
    && (value.finalizedProjectSha256 === undefined
      || (isString(value.finalizedProjectSha256)
        && /^[0-9a-f]{64}$/.test(value.finalizedProjectSha256)))
    && isOptionalString(value.jarPath)
    && (value.jarSha256 === undefined
      || (isString(value.jarSha256) && /^[0-9a-f]{64}$/.test(value.jarSha256)))
    && ((value.jarPath === undefined) === (value.jarSha256 === undefined));
}

export function assertWizardDraft(value: unknown): asserts value is WizardDraft {
  if (!isRecord(value)) throw new Error("Der gespeicherte Entwurf ist beschädigt.");
  if (value.draftVersion !== WIZARD_DRAFT_VERSION) {
    throw new Error(
      `Entwurfsversion ${String(value.draftVersion ?? "unbekannt")} wird nicht unterstützt.`,
    );
  }
  if (isRecord(value.project) && value.project.formatVersion !== "0.4") {
    throw new Error(
      `Projektversion ${String(value.project.formatVersion ?? "unbekannt")} wird nicht unterstützt.`,
    );
  }
  if (
    !isString(value.draftId) || value.draftId.length === 0
    || !Number.isSafeInteger(value.revision) || (value.revision as number) < 0
    || !isDeerProject(value.project)
    || !isGraphLayout(value.graphLayout) || !isUploads(value.uploads) || !isUiState(value.ui)
    || (value.projectDirectory !== undefined
      && (!isString(value.projectDirectory) || value.projectDirectory.length === 0))
    || (value.savedAt !== undefined && !isString(value.savedAt))
    || (value.saveStatus !== "unsaved" && value.saveStatus !== "saved")
    || (value.finalization !== undefined && !isFinalization(value.finalization))
  ) {
    throw new Error("Der gespeicherte Entwurf ist unvollständig oder beschädigt.");
  }
}

function readStore(): DraftStoreEnvelope {
  const raw = localStorage.getItem(DRAFT_STORE_KEY);
  if (raw === null) {
    return { storeVersion: STORE_VERSION, draftOrder: [], drafts: {} };
  }

  let value: unknown;
  try {
    value = JSON.parse(raw);
  } catch {
    throw new Error("Der lokale Entwurfsspeicher ist beschädigt.");
  }
  if (typeof value !== "object" || value === null || Array.isArray(value)) {
    throw new Error("Der lokale Entwurfsspeicher ist beschädigt.");
  }
  const store = value as Record<string, unknown>;
  if (store.storeVersion !== STORE_VERSION) {
    throw new Error(
      `Speicherversion ${String(store.storeVersion ?? "unbekannt")} wird nicht unterstützt.`,
    );
  }
  if (
    !Array.isArray(store.draftOrder) ||
    !store.draftOrder.every((entry) => typeof entry === "string") ||
    typeof store.drafts !== "object" ||
    store.drafts === null ||
    Array.isArray(store.drafts)
  ) {
    throw new Error("Der lokale Entwurfsspeicher ist unvollständig.");
  }

  const draftOrder = store.draftOrder as string[];
  const draftIds = Object.keys(store.drafts as Record<string, unknown>);
  if (
    new Set(draftOrder).size !== draftOrder.length ||
    draftIds.length !== draftOrder.length ||
    draftIds.some((draftId) => !draftOrder.includes(draftId))
  ) {
    throw new Error("Der lokale Entwurfsspeicher enthält verwaiste Einträge.");
  }
  const drafts = store.drafts as Record<string, unknown>;
  for (const draftId of draftOrder) {
    const draft = drafts[draftId];
    assertWizardDraft(draft);
    if (draft.draftId !== draftId) {
      throw new Error("Ein gespeicherter Entwurf ist widersprüchlich.");
    }
  }

  return value as DraftStoreEnvelope;
}

export class BrowserDraftStorage implements DraftStoragePort {
  private readonly deleteAssetFiles: (draftId: string) => Promise<void>;

  constructor(deleteAssetFiles: (draftId: string) => Promise<void>) {
    this.deleteAssetFiles = deleteAssetFiles;
  }

  async list(): Promise<DraftSummary[]> {
    const store = readStore();
    return store.draftOrder.map((draftId) => {
      const draft = store.drafts[draftId];
      if (!draft) throw new Error("Ein gespeicherter Entwurf konnte nicht geladen werden.");
      assertWizardDraft(draft);
      return {
        draftId,
        title: draft.project.metadata.title,
        revision: draft.revision,
        savedAt: draft.savedAt,
      };
    });
  }

  async load(draftId: string): Promise<WizardDraft | null> {
    const draft = readStore().drafts[draftId];
    if (!draft) return null;
    assertWizardDraft(draft);
    return structuredClone(draft);
  }

  async save(draft: WizardDraft): Promise<WizardDraft> {
    if (draft.draftVersion !== WIZARD_DRAFT_VERSION) {
      throw new Error(`Entwurfsversion ${draft.draftVersion} wird nicht unterstützt.`);
    }
    assertWizardDraft(draft);
    if (!("locks" in navigator) || navigator.locks === undefined) {
      throw new Error(
        "Dein Browser kann Entwürfe nicht sicher speichern, weil die benötigte Sperrfunktion fehlt. Verwende die lokale Wizard-Anwendung oder einen aktuellen Browser.",
      );
    }
    return withBrowserDraftStoreLock(async () => {
      const previous = readStore();
      const current = previous.drafts[draft.draftId];
      if (current === undefined) {
        if (draft.revision !== 0) {
          throw new DraftReloadRequiredError("revision-conflict");
        }
      } else if (current.revision !== draft.revision) {
        throw new DraftReloadRequiredError("revision-conflict");
      }
      if (draft.revision === Number.MAX_SAFE_INTEGER) {
        throw new Error("Der Entwurf kann nicht weiter gespeichert werden.");
      }
      const stored: WizardDraft = {
        ...structuredClone(draft),
        revision: draftRevision(draft.revision + 1),
        savedAt: new Date().toISOString(),
        saveStatus: "saved",
      };
      const next: DraftStoreEnvelope = {
        storeVersion: STORE_VERSION,
        draftOrder: previous.draftOrder.includes(stored.draftId)
          ? previous.draftOrder
          : [...previous.draftOrder, stored.draftId],
        drafts: { ...previous.drafts, [stored.draftId]: stored },
      };

      // The exclusive cross-tab lock makes compare-and-write one CAS operation.
      localStorage.setItem(DRAFT_STORE_KEY, JSON.stringify(next));
      return stored;
    });
  }

  async delete(draftId: string, revision: DraftRevision): Promise<void> {
    if (!("locks" in navigator) || navigator.locks === undefined) {
      throw new Error(
        "Dein Browser kann Entwürfe nicht sicher löschen, weil die benötigte Sperrfunktion fehlt. Verwende die lokale Wizard-Anwendung oder einen aktuellen Browser.",
      );
    }
    try {
      await withBrowserDraftStoreLock(async () => {
        const previous = readStore();
        const current = previous.drafts[draftId];
        if (current === undefined) {
          await this.deleteAssetFiles(draftId);
          return;
        }
        if (current.revision !== revision) {
          throw new DraftReloadRequiredError("revision-conflict");
        }

        const { [draftId]: removed, ...remainingDrafts } = previous.drafts;
        if (removed === undefined) return;
        const next: DraftStoreEnvelope = {
          storeVersion: STORE_VERSION,
          draftOrder: previous.draftOrder.filter((id) => id !== draftId),
          drafts: remainingDrafts,
        };
        localStorage.setItem(DRAFT_STORE_KEY, JSON.stringify(next));
        try {
          await this.deleteAssetFiles(draftId);
        } catch (cause) {
          // The upload transaction is atomic. Restore the still-complete visible draft on failure.
          localStorage.setItem(DRAFT_STORE_KEY, JSON.stringify(previous));
          throw cause;
        }
      });
    } catch (cause) {
      if (cause instanceof DraftReloadRequiredError) throw cause;
      throw new Error(
        "Der Entwurf konnte nicht vollständig gelöscht werden. Er bleibt gespeichert. Versuche es erneut.",
      );
    }
  }
}
