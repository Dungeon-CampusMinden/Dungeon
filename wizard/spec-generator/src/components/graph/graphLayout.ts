import type { AnyGraphNode, GraphEdge, RiddleGraph } from "@/data/DeerSchema";

export interface NodePosition {
  x: number;
  y: number;
}

/** Node positions are purely visual, so they live in local storage instead of the exported schema. */
export type GraphLayout = Record<string, NodePosition>;

export const GRAPH_LAYOUT_STORAGE_KEY = "riddleGraphLayout";

const COLUMN_WIDTH = 280;
const ROW_HEIGHT = 160;

const buildOutgoing = (edges: GraphEdge[]) => {
  const outgoing = new Map<string, string[]>();
  for (const edge of edges) {
    outgoing.set(edge.from, [...(outgoing.get(edge.from) ?? []), edge.to]);
  }
  return outgoing;
};

/** Returns true if `to` can already reach `from`, meaning a new edge from → to would close a cycle. */
export function wouldCreateCycle(edges: GraphEdge[], from: string, to: string): boolean {
  if (from === to) return true;

  const outgoing = buildOutgoing(edges);
  const visited = new Set<string>();
  const queue = [to];
  while (queue.length > 0) {
    const current = queue.shift() as string;
    if (current === from) return true;
    if (visited.has(current)) continue;
    visited.add(current);
    queue.push(...(outgoing.get(current) ?? []));
  }
  return false;
}

export function hasEdge(edges: GraphEdge[], from: string, to: string): boolean {
  return edges.some((edge) => edge.from === from && edge.to === to);
}

const KIND_ORDER: Record<AnyGraphNode["kind"], number> = { start: 0, riddle: 1, end: 2 };

const sortNodesForLayout = (nodes: AnyGraphNode[]) =>
  [...nodes].sort((a, b) => KIND_ORDER[a.kind] - KIND_ORDER[b.kind]);

/**
 * Assigns each node the row of its longest path from a node without predecessors, so edges
 * generally point from top to bottom. Nodes of the same row are arranged horizontally.
 */
export function computeAutoLayout(graph: RiddleGraph): GraphLayout {
  const outgoing = buildOutgoing(graph.edges);
  const incomingCount = new Map<string, number>();
  for (const node of graph.nodes) incomingCount.set(node.id, 0);
  for (const edge of graph.edges) {
    if (!incomingCount.has(edge.from) || !incomingCount.has(edge.to)) continue;
    incomingCount.set(edge.to, (incomingCount.get(edge.to) ?? 0) + 1);
  }

  const row = new Map<string, number>();
  for (const node of graph.nodes) row.set(node.id, 0);

  // Kahn's algorithm; nodes left over by cycles keep their default column.
  const queue = graph.nodes.filter((node) => incomingCount.get(node.id) === 0).map((node) => node.id);
  while (queue.length > 0) {
    const current = queue.shift() as string;
    for (const next of outgoing.get(current) ?? []) {
      if (!row.has(next)) continue;
      row.set(next, Math.max(row.get(next) ?? 0, (row.get(current) ?? 0) + 1));
      const remaining = (incomingCount.get(next) ?? 0) - 1;
      incomingCount.set(next, remaining);
      if (remaining === 0) queue.push(next);
    }
  }

  // The end node is always drawn last so the flow reads towards the exit.
  const endNodeIds = graph.nodes.filter((node) => node.kind === "end").map((node) => node.id);
  if (endNodeIds.length > 0 && row.size > 0) {
    const maxRow = Math.max(...row.values());
    for (const id of endNodeIds) row.set(id, maxRow);
  }

  const columnsPerRow = new Map<number, number>();
  const layout: GraphLayout = {};
  for (const node of sortNodesForLayout(graph.nodes)) {
    const nodeRow = row.get(node.id) ?? 0;
    const column = columnsPerRow.get(nodeRow) ?? 0;
    columnsPerRow.set(nodeRow, column + 1);
    layout[node.id] = { x: column * COLUMN_WIDTH, y: nodeRow * ROW_HEIGHT };
  }
  return layout;
}

/** Fills in positions for nodes the user has never dragged, keeping stored positions untouched. */
export function withMissingPositions(graph: RiddleGraph, layout: GraphLayout): GraphLayout {
  const missing = graph.nodes.filter((node) => layout[node.id] === undefined);
  if (missing.length === 0) return layout;

  const autoLayout = computeAutoLayout(graph);
  const merged = { ...layout };
  for (const node of missing) {
    merged[node.id] = autoLayout[node.id] ?? { x: 0, y: 0 };
  }
  return merged;
}
