export interface DeerSchema {
  formatVersion: string;
  metadata: Metadata;
  learningDesign: LearningDesign;
  session: Session;
  scenario: Scenario;
  surfaces: Surface[];
  riddleGraph: RiddleGraph;
  riddles: AnyRiddle[];
  assets: Asset[];
}

export interface Metadata {
  id: string;
  title: string;
  locale: string;
  description: string;
  author: string;
}

export interface Objective {
  id: string;
  description: string;
}
export interface LearningDesign {
  objectives: Objective[];
  debriefPrompts: string[];
}

export interface PlayerCount {
  min: number;
  max: number;
}
export interface Time {
  limitMinutes: number;
  limitMode: "hard";
}
export interface Session {
  targetAudience: string;
  priorKnowledge: string;
  playerCount: PlayerCount;
  time: Time;
}

export interface Scenario {
  themeId: string;
  playerRole: string;
  premise: string;
  mission: string;
  introTexts: string[];
  successTexts: string[];
  failureTexts: string[];
}

export interface Surface {
  id: string;
  kind: "world" | "container" | "keypad" | "door";
  title: string;
}

//#region Riddle Graph
export interface GraphNode {
  id: string;
  kind: "start" | "end" | "riddle";
}
export interface StartNode extends GraphNode {
  kind: "start";
}
export interface EndNode extends GraphNode {
  kind: "end";
  surfaceId: string;
}
export interface RiddleNode extends GraphNode {
  kind: "riddle";
  riddleId: string;
}
export type AnyGraphNode = StartNode | EndNode | RiddleNode;

export interface GraphEdge {
  id: string;
  from: string;
  to: string;
}

export interface RiddleGraph {
  startNodeId: string;
  endNodeId: string;
  nodes: AnyGraphNode[];
  edges: GraphEdge[];
}
//#endregion

//#region Riddles
export interface Resource {
  id: string;
  kind: "inline_text" | "asset";
  title: string;
  availability: "visible_in_level" | "inside_container";
  purpose: "clue" | "context" | "instruction" | "decoy";
}
export interface ResourceText extends Resource {
  kind: "inline_text";
  text: string;
}
export interface ResourceAsset extends Resource {
  kind: "asset";
  assetId: string;
}
export type AnyResource = ResourceText | ResourceAsset;

export interface RiddleHint {
  id: string;
  title: string;
  text: string;
  severity: number;
}

export interface CollectionRiddleParameters {
  surfaceId: string;
  sourceKind: "container" | "world_object";
  rewardMode: "find_resource";
  resourceIds: string[];
}
export interface InputRiddleParameters {
  surfaceId: string;
  inputMode: "numeric";
  answer: string;
  showDigitCount: boolean;
}

export type RiddleDifficulty = "easy" | "medium" | "hard";

export interface Riddle {
  id: string;
  type: "collection" | "input";
  title: string;
  difficulty: RiddleDifficulty;
  learningObjectiveIds: string[];
  playerFacingTask: string;
  estimatedMinutes: number;
  resources: AnyResource[];
  hints: RiddleHint[];
}
export interface CollectionRiddle extends Riddle {
  type: "collection";
  parameters: CollectionRiddleParameters;
}
export interface InputRiddle extends Riddle {
  type: "input";
  parameters: InputRiddleParameters;
}
export type AnyRiddle = CollectionRiddle | InputRiddle;
//#endregion

//#region Asset
export interface AssetSource {
  license: string;
  attribution?: string;
}

export type AssetMediaType = "image/png" | "image/jpeg" | "text/plain" | "audio/wav" | "font/ttf";

export interface Asset {
  id: string;
  path: string;
  mediaType: AssetMediaType;
  source: AssetSource;
}
//#endregion
