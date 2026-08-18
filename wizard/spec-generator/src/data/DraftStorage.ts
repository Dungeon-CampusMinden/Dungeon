import { WIZARD_DRAFT_VERSION, type WizardDraft } from "./WizardDraft";
import { isTabId, VALIDATED_TAB_IDS, type ValidatedTabId } from "./Tabs";

const STORE_VERSION = "1" as const;
const DRAFT_STORE_KEY = "wizardDraftStore";

interface DraftStoreEnvelope {
  storeVersion: typeof STORE_VERSION;
  draftOrder: string[];
  drafts: Record<string, WizardDraft>;
}

export interface DraftSummary {
  draftId: string;
  title: string;
  savedAt?: string;
}

export interface DraftStoragePort {
  list(): DraftSummary[];
  load(draftId: string): WizardDraft | null;
  save(draft: WizardDraft): WizardDraft;
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
    isRecord(upload) && isString(upload.storageKey) && /^[0-9a-f]{12}$/.test(upload.storageKey)
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
    && isString(value.finalizedAt);
}

function assertWizardDraft(value: unknown): asserts value is WizardDraft {
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
    !isString(value.draftId) || value.draftId.length === 0 || !isDeerProject(value.project)
    || !isGraphLayout(value.graphLayout) || !isUploads(value.uploads) || !isUiState(value.ui)
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
      throw new Error(`Der gespeicherte Entwurf ${draftId} hat eine widersprüchliche ID.`);
    }
  }

  return value as DraftStoreEnvelope;
}

export class BrowserDraftStorage implements DraftStoragePort {
  list(): DraftSummary[] {
    const store = readStore();
    return store.draftOrder.map((draftId) => {
      const draft = store.drafts[draftId];
      if (!draft) throw new Error(`Der Entwurf ${draftId} konnte nicht geladen werden.`);
      assertWizardDraft(draft);
      return { draftId, title: draft.project.metadata.title, savedAt: draft.savedAt };
    });
  }

  load(draftId: string): WizardDraft | null {
    const draft = readStore().drafts[draftId];
    if (!draft) return null;
    assertWizardDraft(draft);
    return structuredClone(draft);
  }

  save(draft: WizardDraft): WizardDraft {
    if (draft.draftVersion !== WIZARD_DRAFT_VERSION) {
      throw new Error(`Entwurfsversion ${draft.draftVersion} wird nicht unterstützt.`);
    }
    const stored: WizardDraft = {
      ...structuredClone(draft),
      savedAt: new Date().toISOString(),
      saveStatus: "saved",
    };
    const previous = readStore();
    const next: DraftStoreEnvelope = {
      storeVersion: STORE_VERSION,
      draftOrder: previous.draftOrder.includes(stored.draftId)
        ? previous.draftOrder
        : [...previous.draftOrder, stored.draftId],
      drafts: { ...previous.drafts, [stored.draftId]: stored },
    };

    // One write commits index and draft together. If it throws, the previous envelope is untouched.
    localStorage.setItem(DRAFT_STORE_KEY, JSON.stringify(next));
    return stored;
  }
}
