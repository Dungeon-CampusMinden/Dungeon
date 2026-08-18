import type { DeerSchema } from "./DeerSchema";
import { Util } from "./Util";

export const DEER_SCHEMA_STORAGE_KEY = "deerSchema:0.4";

/** Creates the private starting state for a new DEER 0.4 adventure. */
export function createDeerSchema(): DeerSchema {
  const worldSurfaceId = Util.generateUniqueId("s");
  const doorSurfaceId = Util.generateUniqueId("s");

  return {
    formatVersion: "0.4",
    seed: Util.generateSafeInteger(),
    metadata: {
      id: Util.generateUniqueId("adventure"),
      title: "",
      locale: "de-DE",
    },
    learningDesign: {
      objectives: [],
      debriefPrompts: [],
    },
    session: {
      targetAudience: "",
      priorKnowledge: "",
      playerCount: { min: 1, max: 1 },
      time: { limitMinutes: 60, limitMode: "soft" },
    },
    scenario: {
      themeId: "default",
      mission: "",
      introText: [],
      successText: [],
    },
    surfaces: [
      { id: worldSurfaceId, kind: "world", title: "Raum" },
      { id: doorSurfaceId, kind: "door", title: "Ausgang" },
    ],
    riddleGraph: {
      nodes: [
        { id: Util.generateUniqueId("n"), kind: "start" },
        { id: Util.generateUniqueId("n"), kind: "end", surfaceId: doorSurfaceId },
      ],
      edges: [],
    },
    riddles: [],
    assets: [],
  };
}
