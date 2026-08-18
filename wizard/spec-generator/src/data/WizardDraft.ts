import type { DeerProject } from "./DeerSchema";
import { createDeerProject } from "./createDeerProject";
import { createUntouchedTabs, type TouchedTabs } from "./TabTouchState";
import type { TabId } from "./Tabs";
import { Util } from "./Util";

export const WIZARD_DRAFT_VERSION = "1" as const;

declare const draftRevisionBrand: unique symbol;
export type DraftRevision = number & { readonly [draftRevisionBrand]: "DraftRevision" };

export function draftRevision(value: number): DraftRevision {
  if (!Number.isSafeInteger(value) || value < 0) {
    throw new Error("Die Entwurfsrevision ist ungültig.");
  }
  return value as DraftRevision;
}

export interface UploadReference {
  storageKey: string;
  originalName: string;
}

export interface NodePosition {
  x: number;
  y: number;
}

export type GraphLayout = Record<string, NodePosition>;

export interface DraftFinalization {
  seed: number;
  projectDirectory: string;
  finalizedAt: string;
  candidateHash?: string;
  deerSha256: string;
  /** Host-owned hash of the finalized DEER project and its assets. The UI only preserves it. */
  readonly finalizedProjectSha256?: string;
  readonly jarPath?: string;
  readonly jarSha256?: string;
}

export interface WizardDraft {
  draftVersion: typeof WIZARD_DRAFT_VERSION;
  draftId: string;
  revision: DraftRevision;
  project: DeerProject;
  graphLayout: GraphLayout;
  ui: {
    activeTab: TabId;
    touchedTabs: TouchedTabs;
  };
  uploads: Record<string, UploadReference>;
  projectDirectory?: string;
  savedAt?: string;
  saveStatus: "unsaved" | "saved";
  finalization?: DraftFinalization;
}

export type DraftTransform = (draft: WizardDraft) => boolean | void;
export type UpdateDraft = (transform: DraftTransform) => void;

export function createWizardDraft(): WizardDraft {
  return {
    draftVersion: WIZARD_DRAFT_VERSION,
    draftId: Util.generateUniqueId("draft"),
    revision: draftRevision(0),
    project: createDeerProject(),
    graphLayout: {},
    ui: {
      activeTab: "metadata",
      touchedTabs: createUntouchedTabs(),
    },
    uploads: {},
    saveStatus: "unsaved",
  };
}

export function cloneDraft(draft: WizardDraft): WizardDraft {
  return structuredClone(draft);
}
