import { VALIDATED_TAB_IDS } from "./ErrorChecker";

/** Maps a tab id to whether the user has already worked on that tab. */
export type TouchedTabs = Record<string, boolean>;

/** Local storage key under which the touch state is persisted, separately from the schema. */
export const TOUCHED_TABS_STORAGE_KEY = "touchedTabs";

const buildTouchedTabs = (touched: boolean): TouchedTabs =>
  Object.fromEntries(VALIDATED_TAB_IDS.map((tabId) => [tabId, touched]));

/**
 * Touch state for a freshly created adventure: nothing has been edited yet, so the outline stays
 * neutral instead of immediately flagging every field of the example config.
 */
export const createUntouchedTabs = (): TouchedTabs => buildTouchedTabs(false);

/**
 * Touch state for an imported adventure: the whole config was authored before, so every tab is
 * treated as touched and its status is shown right away.
 */
export const createImportedTouchedTabs = (): TouchedTabs => buildTouchedTabs(true);

/** Returns a new touch state with the given tab marked as touched. */
export const withTouchedTab = (touchedTabs: TouchedTabs, tabId: string): TouchedTabs =>
  touchedTabs[tabId] ? touchedTabs : { ...touchedTabs, [tabId]: true };

export const isTabTouched = (touchedTabs: TouchedTabs, tabId: string) => touchedTabs[tabId] === true;
