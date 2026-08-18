export const TABS = [
  { value: "metadata", label: "Eckdaten & Lernziel" },
  { value: "scenario", label: "Geschichte" },
  { value: "session", label: "Spieleinstellungen" },
  { value: "assets", label: "Eigene Bilder & Dateien" },
  { value: "riddles", label: "Rätsel" },
  { value: "riddle_graph", label: "Spielablauf" },
  { value: "game_end", label: "Spiel-Ende" },
  { value: "review", label: "Entwurf prüfen" },
] as const;

export type TabId = (typeof TABS)[number]["value"];

export const VALIDATED_TAB_IDS = [
  "metadata",
  "scenario",
  "session",
  "assets",
  "riddles",
  "riddle_graph",
  "game_end",
] as const satisfies readonly TabId[];

export type ValidatedTabId = (typeof VALIDATED_TAB_IDS)[number];

export const isTabId = (value: unknown): value is TabId =>
  typeof value === "string" && TABS.some((tab) => tab.value === value);
