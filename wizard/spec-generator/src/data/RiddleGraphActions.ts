import type {
  AnyRiddleInput,
  DeerProject,
  InformationSource,
  Riddle,
} from "./DeerSchema";
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
      ...riddle.inputs
        .filter((input) => input.type === "numeric")
        .map((input) => input.surfaceId),
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

export function addRiddleInput(project: DeerProject, riddle: Riddle): void {
  const surfaceId = Util.generateUniqueId("s");
  project.surfaces.push({ id: surfaceId, kind: "keypad", title: "Zahlengerät" });
  riddle.inputs.push({
    id: Util.generateUniqueId("input"),
    type: "numeric",
    surfaceId,
    answer: "",
    showDigitCount: true,
  });
}

export function removeRiddleInput(project: DeerProject, riddle: Riddle, inputId: string): void {
  const input = riddle.inputs.find((candidate) => candidate.id === inputId);
  if (!input) return;
  if (input.type === "numeric") {
    project.surfaces = project.surfaces.filter((surface) => surface.id !== input.surfaceId);
  }
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
  const input = riddle.inputs[index];
  if (input.type === "numeric") {
    project.surfaces = project.surfaces.filter((surface) => surface.id !== input.surfaceId);
    riddle.inputs[index] = {
      id: input.id,
      type: "collection",
      informationSourceId: riddle.informationSources[0]?.id ?? "",
    };
    return;
  }
  const surfaceId = Util.generateUniqueId("s");
  project.surfaces.push({ id: surfaceId, kind: "keypad", title: "Zahlengerät" });
  riddle.inputs[index] = {
    id: input.id,
    type: "numeric",
    surfaceId,
    answer: "",
    showDigitCount: true,
  };
}
