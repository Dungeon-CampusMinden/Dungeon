import type { DeerProject, Riddle } from "./DeerSchema";
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
