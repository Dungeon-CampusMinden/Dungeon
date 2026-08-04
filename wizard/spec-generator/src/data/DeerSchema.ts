export interface DeerSchema {
  formatVersion: string;
  metadata: Metadata;
  learningDesign: LearningDesign;
  session: Session;
  scenario: Scenario;
  surfaces: Surface[];
  riddleGraph: RiddleGraph;
  riddles: Riddle[];
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
export type TimeLimitMode = "hard" | "soft";
export interface Time {
  limitMinutes: number;
  limitMode: TimeLimitMode;
}
export interface Session {
  targetAudience: string;
  priorKnowledge: string;
  playerCount: PlayerCount;
  time: Time;
}

export interface Scenario {
  themeId: string;
  mission: string;
  introText: string[];
  successText: string[];
  failureText: string[];
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
  from: string;
  to: string;
}

export interface RiddleGraph {
  nodes: AnyGraphNode[];
  edges: GraphEdge[];
}
//#endregion

//#region Riddles
export interface Resource {
  id: string;
  kind: "inline_text" | "asset";
  title: string;
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

export type HintSeverity = "orientation" | "approach" | "solution";

export interface RiddleHint {
  id: string;
  title: string;
  text: string;
  severity: HintSeverity;
}

export interface InformationSource {
  id: string;
  surfaceId: string;
  resources: AnyResource[];
}

export interface RiddleInput {
  id: string;
  type: "collection" | "numeric";
}
export interface CollectionInput extends RiddleInput {
  type: "collection";
  informationSourceId: string;
}
export interface NumericInput extends RiddleInput {
  type: "numeric";
  surfaceId: string;
  answer: string;
  showDigitCount: boolean;
}
export type AnyRiddleInput = CollectionInput | NumericInput;

export type RiddleDifficulty = "easy" | "medium" | "hard";

export interface Riddle {
  id: string;
  title: string;
  difficulty: RiddleDifficulty;
  learningObjectiveIds: string[];
  estimatedMinutes: number;
  informationSources: InformationSource[];
  inputs: AnyRiddleInput[];
  hints: RiddleHint[];
}
//#endregion

//#region Asset
export interface AssetSource {
  license: string;
  attribution?: string;
}

export type AssetMediaType = "image/png" | "image/jpeg";

export interface Asset {
  id: string;
  path: string;
  mediaType: AssetMediaType;
  source: AssetSource;
}
//#endregion
