export interface DeerSchema {
  formatVersion: "0.5";
  seed: number;
  metadata: Metadata;
  learningDesign: LearningDesign;
  session: Session;
  scenario: Scenario;
  surfaces: Surface[];
  riddleGraph: RiddleGraph;
  riddles: Riddle[];
  assets: Asset[];
}

/** The DEER-shaped project data kept in a private draft before a seed exists. */
export type DeerProject = Omit<DeerSchema, "seed">;

export interface Metadata {
  id: string;
  title: string;
  locale: string;
  description?: string;
  author?: string;
  operatorEmail?: string;
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
  failureText?: string[];
}

export interface Surface {
  id: string;
  kind: "world" | "container" | "keypad" | "terminal" | "assignment" | "door";
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
  type: "collection" | "numeric" | "choice" | "assignment";
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
export interface ChoiceOption {
  id: string;
  text: string;
}
export interface ChoiceInput extends RiddleInput {
  type: "choice";
  surfaceId: string;
  question: string;
  options: ChoiceOption[];
  correctOptionIds: string[];
  multipleSelection: boolean;
}
export interface AssignmentContainer {
  id: string;
  name: string;
}
export interface AssignmentItem {
  id: string;
  name: string;
  /** Bundled sprite path from {@link ASSIGNMENT_SPRITES}. */
  sprite: string;
  /** Target container; absent when the item belongs into none. */
  containerId?: string;
}
export interface AssignmentInput extends RiddleInput {
  type: "assignment";
  surfaceId: string;
  containers: AssignmentContainer[];
  items: AssignmentItem[];
}
export type AnyRiddleInput = CollectionInput | NumericInput | ChoiceInput | AssignmentInput;

/** Inputs answered at their own placed device surface. */
export type SurfaceRiddleInput = NumericInput | ChoiceInput | AssignmentInput;

/** Item sprites teachers can pick for assignment items; mirrors the DEER schema enum. */
export const ASSIGNMENT_SPRITES = [
  { path: "items/book/letter.png", label: "Brief" },
  { path: "items/book/magic_scroll.png", label: "Schriftrolle" },
  { path: "items/rpg/item_book_blue.png", label: "Buch" },
  { path: "items/potion/health_potion.png", label: "Trank" },
  { path: "items/rpg/food_bread.png", label: "Brot" },
  { path: "items/rpg/food_apple.png", label: "Apfel" },
  { path: "items/key/gold_key.png", label: "Schlüssel" },
  { path: "items/resource/gold.png", label: "Gold" },
  { path: "items/resource/emerald.png", label: "Edelstein" },
  { path: "items/resource/wood.png", label: "Holz" },
  { path: "items/resource/flower_red.png", label: "Blume" },
  { path: "items/resource/stone.png", label: "Stein" },
] as const;

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
