import type {
  AnyRiddleInput,
  DeerProject,
  InformationSource,
  Riddle,
  Surface,
  SurfaceRiddleInput,
} from "./DeerSchema";
import { ASSIGNMENT_SPRITES } from "./DeerSchema";
import { Util } from "./Util";

export function addRiddle(project: DeerProject, riddle: Riddle): void {
  project.riddles.push(riddle);
  project.riddleGraph.nodes.push({
    id: Util.generateUniqueId("n"),
    kind: "riddle",
    riddleId: riddle.id,
  });
}

export function removeRiddle(project: DeerProject, riddleId: string): string[] {
  const riddle = project.riddles.find((candidate) => candidate.id === riddleId);
  if (riddle) {
    const ownedSurfaceIds = new Set([
      ...riddle.informationSources.map((source) => source.surfaceId),
      ...riddle.inputs.flatMap((input) => (input.type === "collection" ? [] : [input.surfaceId])),
    ]);
    project.surfaces = project.surfaces.filter((surface) => !ownedSurfaceIds.has(surface.id));
  }
  project.riddles = project.riddles.filter((riddle) => riddle.id !== riddleId);
  const removedNodeIds = project.riddleGraph.nodes
    .filter((node) => node.kind === "riddle" && node.riddleId === riddleId)
    .map((node) => node.id);
  const removed = new Set(removedNodeIds);
  project.riddleGraph.nodes = project.riddleGraph.nodes.filter((node) => !removed.has(node.id));
  project.riddleGraph.edges = project.riddleGraph.edges.filter(
    (edge) => !removed.has(edge.from) && !removed.has(edge.to),
  );
  return removedNodeIds;
}

export function addInformationSource(project: DeerProject, riddle: Riddle): void {
  const surfaceId = Util.generateUniqueId("s");
  const source: InformationSource = {
    id: Util.generateUniqueId("source"),
    surfaceId,
    resources: [],
  };
  project.surfaces.push({ id: surfaceId, kind: "container", title: "Versteck" });
  riddle.informationSources.push(source);
}

export function removeInformationSource(project: DeerProject, riddle: Riddle, sourceId: string): void {
  const source = riddle.informationSources.find((candidate) => candidate.id === sourceId);
  if (!source) return;
  project.surfaces = project.surfaces.filter((surface) => surface.id !== source.surfaceId);
  riddle.informationSources = riddle.informationSources.filter((candidate) => candidate.id !== sourceId);
  for (const input of riddle.inputs) {
    if (input.type === "collection" && input.informationSourceId === sourceId) {
      input.informationSourceId = "";
    }
  }
}

const DEVICE_SURFACES = {
  numeric: { kind: "keypad", title: "Zahlengerät" },
  choice: { kind: "terminal", title: "Fragestation" },
  assignment: { kind: "assignment", title: "Zuordnung" },
} as const satisfies Record<SurfaceRiddleInput["type"], Pick<Surface, "kind" | "title">>;

/** Creates an input of the given type and the device surface it needs. */
function createRiddleInput(
  project: DeerProject,
  riddle: Riddle,
  id: string,
  type: AnyRiddleInput["type"],
): AnyRiddleInput {
  if (type === "collection") {
    return { id, type, informationSourceId: riddle.informationSources[0]?.id ?? "" };
  }
  const surfaceId = Util.generateUniqueId("s");
  project.surfaces.push({ id: surfaceId, ...DEVICE_SURFACES[type] });
  if (type === "numeric") {
    return { id, type, surfaceId, answer: "", showDigitCount: true };
  }
  if (type === "assignment") {
    const containerId = Util.generateUniqueId("box");
    return {
      id,
      type,
      surfaceId,
      containers: [{ id: containerId, name: "" }],
      items: [
        { id: Util.generateUniqueId("item"), name: "", sprite: ASSIGNMENT_SPRITES[0].path, containerId },
        { id: Util.generateUniqueId("item"), name: "", sprite: ASSIGNMENT_SPRITES[1].path },
      ],
    };
  }
  return {
    id,
    type,
    surfaceId,
    question: "",
    options: [
      { id: Util.generateUniqueId("opt"), text: "" },
      { id: Util.generateUniqueId("opt"), text: "" },
    ],
    correctOptionIds: [],
    multipleSelection: false,
  };
}

function removeInputSurface(project: DeerProject, input: AnyRiddleInput): void {
  if (input.type === "collection") return;
  project.surfaces = project.surfaces.filter((surface) => surface.id !== input.surfaceId);
}

export function addRiddleInput(project: DeerProject, riddle: Riddle): void {
  riddle.inputs.push(createRiddleInput(project, riddle, Util.generateUniqueId("input"), "numeric"));
}

export function removeRiddleInput(project: DeerProject, riddle: Riddle, inputId: string): void {
  const input = riddle.inputs.find((candidate) => candidate.id === inputId);
  if (!input) return;
  removeInputSurface(project, input);
  riddle.inputs = riddle.inputs.filter((candidate) => candidate.id !== inputId);
}

export function convertRiddleInput(
  project: DeerProject,
  riddle: Riddle,
  inputId: string,
  type: AnyRiddleInput["type"],
): void {
  const index = riddle.inputs.findIndex((candidate) => candidate.id === inputId);
  if (index === -1 || riddle.inputs[index].type === type) return;
  removeInputSurface(project, riddle.inputs[index]);
  riddle.inputs[index] = createRiddleInput(project, riddle, inputId, type);
}
