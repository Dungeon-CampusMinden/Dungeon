import type { DeerProject } from "./DeerSchema";
import { Util } from "./Util";

/** Creates structurally editable project data for a new private draft. */
export function createDeerProject(): DeerProject {
  const worldSurfaceId = Util.generateUniqueId("s");
  const doorSurfaceId = Util.generateUniqueId("s");

  return {
    formatVersion: "0.4",
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
