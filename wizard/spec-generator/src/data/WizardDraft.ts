import type { DeerProject } from "./DeerSchema";
import { createDeerProject } from "./createDeerProject";
import { createUntouchedTabs, type TouchedTabs } from "./TabTouchState";
import type { TabId } from "./Tabs";
import { Util } from "./Util";

export const WIZARD_DRAFT_VERSION = "1" as const;

export interface UploadReference {
  storageKey: string;
  originalName: string;
}

export interface NodePosition { x: number; y: number; }
export type GraphLayout = Record<string, NodePosition>;

export interface WizardDraft {
  draftVersion: typeof WIZARD_DRAFT_VERSION;
  draftId: string;
  seed?: number;
  project: DeerProject;
  graphLayout: GraphLayout;
  ui: { activeTab: TabId; touchedTabs: TouchedTabs };
  uploads: Record<string, UploadReference>;
  savedAt?: string;
}

export type DraftTransform = (draft: WizardDraft) => boolean | void;
export type UpdateDraft = (transform: DraftTransform) => void;

export function createWizardDraft(): WizardDraft {
  return {
    draftVersion: WIZARD_DRAFT_VERSION,
    draftId: Util.generateUniqueId("draft"),
    project: createDeerProject(),
    graphLayout: {},
    ui: { activeTab: "metadata", touchedTabs: createUntouchedTabs() },
    uploads: {},
  };
}

export function cloneDraft(draft: WizardDraft): WizardDraft {
  return structuredClone(draft);
}

/** Returns one uniformly distributed integer from the 53-bit safe integer range. */
export function createSeed(): number {
  const words = crypto.getRandomValues(new Uint32Array(2));
  return (words[0] & 0x1fffff) * 0x100000000 + words[1];
}
