import { VALIDATED_TAB_IDS, type TabId, type ValidatedTabId } from "./Tabs";

/** Maps a tab id to whether the user has already worked on that tab. */
export type TouchedTabs = Record<ValidatedTabId, boolean>;

const buildTouchedTabs = (touched: boolean): TouchedTabs =>
  Object.fromEntries(VALIDATED_TAB_IDS.map((tabId) => [tabId, touched])) as TouchedTabs;

/**
 * Touch state for a freshly created adventure: nothing has been edited yet, so the outline stays
 * neutral instead of immediately flagging every field of the example config.
 */
export const createUntouchedTabs = (): TouchedTabs => buildTouchedTabs(false);

/** Returns a new touch state with the given tab marked as touched. */
export const withTouchedTab = (touchedTabs: TouchedTabs, tabId: TabId): TouchedTabs => {
  if (tabId === "review" || touchedTabs[tabId]) return touchedTabs;
  return { ...touchedTabs, [tabId]: true };
};

export const isTabTouched = (touchedTabs: TouchedTabs, tabId: TabId) =>
  tabId !== "review" && touchedTabs[tabId];
