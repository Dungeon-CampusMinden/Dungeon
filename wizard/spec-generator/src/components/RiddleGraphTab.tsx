import type { AnyGraphNode, Riddle, Surface } from "@/data/DeerSchema";
import type { GraphLayout, UpdateDraft, WizardDraft } from "@/data/WizardDraft";
import { removeRiddle } from "@/data/RiddleGraphActions";
import {
  Background,
  ConnectionMode,
  Controls,
  MarkerType,
  ReactFlow,
  ReactFlowProvider,
  useNodesState,
  type Connection,
  type Edge,
  type OnConnect,
  type OnNodeDrag,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { LayoutGridIcon } from "lucide-react";
import { useTheme } from "next-themes";
import React from "react";
import { toast } from "sonner";
import { RiddleEditDialog } from "./riddles/RiddleEditDialog";
import { Button } from "./ui/button";
import { GRAPH_EDGE_TYPES } from "./graph/DeletableEdge";
import { GRAPH_NODE_TYPES, type GraphFlowNode } from "./graph/GraphNodes";
import {
  computeAutoLayout,
  hasEdge,
  withMissingPositions,
  wouldCreateCycle,
} from "./graph/graphLayout";

export function RiddleGraphTab(props: {
  draft: WizardDraft;
  updateDraft: UpdateDraft;
}) {
  return (
    <ReactFlowProvider>
      <RiddleGraphEditor {...props} />
    </ReactFlowProvider>
  );
}

function useStableValue<T>(value: T): T {
  const key = JSON.stringify(value);
  return React.useMemo(() => JSON.parse(key) as T, [key]);
}

function RiddleGraphEditor({
  draft,
  updateDraft,
}: {
  draft: WizardDraft;
  updateDraft: UpdateDraft;
}) {
  const { resolvedTheme } = useTheme();
  const [editingRiddleId, setEditingRiddleId] = React.useState<string | null>(null);
  const [nodes, setNodes, onNodesChange] = useNodesState<GraphFlowNode>([]);

  const project = draft.project;
  const graph = useStableValue(project.riddleGraph);
  const riddles = useStableValue(project.riddles);
  const surfaces = useStableValue(project.surfaces);
  const storedLayout = useStableValue(draft.graphLayout);
  const positions = React.useMemo(() => withMissingPositions(graph, storedLayout), [graph, storedLayout]);

  const persistLayout = React.useCallback((graphLayout: GraphLayout) => {
    updateDraft((current) => {
      current.graphLayout = structuredClone(graphLayout);
    });
  }, [updateDraft]);

  React.useEffect(() => {
    if (positions !== storedLayout) persistLayout(positions);
  }, [positions, storedLayout, persistLayout]);

  const onEndSurfaceChange = React.useCallback((nodeId: string, surfaceId: string) => {
    updateDraft((current) => {
      const node = current.project.riddleGraph.nodes.find(
        (candidate) => candidate.id === nodeId && candidate.kind === "end",
      );
      if (!node || node.kind !== "end") return false;
      node.surfaceId = surfaceId;
    });
  }, [updateDraft]);

  const flowNodes = React.useMemo<GraphFlowNode[]>(
    () => graph.nodes.map((node) =>
      toFlowNode(node, positions, riddles, surfaces, setEditingRiddleId, onEndSurfaceChange),
    ),
    [graph, positions, riddles, surfaces, onEndSurfaceChange],
  );
  React.useEffect(() => setNodes(flowNodes), [flowNodes, setNodes]);

  const flowEdges = React.useMemo<Edge[]>(
    () => graph.edges.map((edge) => ({
      id: `${edge.from}->${edge.to}`,
      source: edge.from,
      target: edge.to,
      type: "deletable",
      markerEnd: { type: MarkerType.ArrowClosed },
    })),
    [graph],
  );

  const onConnect = React.useCallback<OnConnect>((connection: Connection) => {
    const { source, target } = connection;
    if (!source || !target) return;
    updateDraft((current) => {
      const edges = current.project.riddleGraph.edges;
      if (source === target) {
        toast.error("Ein Schritt kann nicht mit sich selbst verbunden werden.");
        return false;
      }
      if (hasEdge(edges, source, target)) {
        toast.error("Diese Verbindung gibt es bereits.");
        return false;
      }
      if (wouldCreateCycle(edges, source, target)) {
        toast.error("Diese Verbindung würde einen Kreis erzeugen und ist deshalb nicht erlaubt.");
        return false;
      }
      current.project.riddleGraph.edges.push({ from: source, to: target });
    });
  }, [updateDraft]);

  const onEdgesDelete = React.useCallback((deleted: Edge[]) => {
    const deletedIds = new Set(deleted.map((edge) => edge.id));
    updateDraft((current) => {
      current.project.riddleGraph.edges = current.project.riddleGraph.edges.filter(
        (edge) => !deletedIds.has(`${edge.from}->${edge.to}`),
      );
    });
  }, [updateDraft]);

  const onNodeDragStop = React.useCallback<OnNodeDrag<GraphFlowNode>>(
    (_event, _node, draggedNodes) => {
      const moved = Object.fromEntries(draggedNodes.map((node) => [node.id, node.position]));
      updateDraft((current) => {
        Object.assign(current.graphLayout, moved);
      });
    },
    [updateDraft],
  );

  const editingIndex = project.riddles.findIndex((riddle) => riddle.id === editingRiddleId);
  const editingRiddle = editingIndex >= 0 ? project.riddles[editingIndex] : null;

  const saveRiddle = (updated: Riddle) => {
    updateDraft((current) => {
      const index = current.project.riddles.findIndex((riddle) => riddle.id === updated.id);
      if (index === -1) return false;
      current.project.riddles[index] = structuredClone(updated);
    });
  };

  const deleteRiddle = () => {
    if (!editingRiddle) return;
    const riddleId = editingRiddle.id;
    updateDraft((current) => {
      const removedNodeIds = removeRiddle(current.project, riddleId);
      for (const nodeId of removedNodeIds) delete current.graphLayout[nodeId];
    });
    setEditingRiddleId(null);
  };

  return (
    <div className="flex flex-col gap-0">
      <h1>Spielablauf</h1>
      <p className="text-sm text-muted-foreground">
        Eine Verbindung bedeutet: Der folgende Schritt wird nach seinem Vorgänger verfügbar. Hat ein
        Schritt mehrere Vorgänger, müssen alle davon gelöst sein. Kreise und doppelte Verbindungen sind
        nicht erlaubt. Verbindungen entfernst du über das X in ihrer Mitte oder mit der ENTF-Taste.
      </p>
      <Button
        variant="outline"
        className="my-2 max-w-52"
        onClick={() => updateDraft((current) => {
          current.graphLayout = computeAutoLayout(current.project.riddleGraph);
        })}
      >
        <LayoutGridIcon />
        Automatisch anordnen
      </Button>

      <div className="h-[65vh] w-full overflow-hidden rounded-lg border border-border">
        <ReactFlow<GraphFlowNode>
          nodes={nodes}
          edges={flowEdges}
          nodeTypes={GRAPH_NODE_TYPES}
          edgeTypes={GRAPH_EDGE_TYPES}
          onNodesChange={onNodesChange}
          onNodeDragStop={onNodeDragStop}
          onConnect={onConnect}
          onEdgesDelete={onEdgesDelete}
          connectionMode={ConnectionMode.Strict}
          colorMode={resolvedTheme === "light" ? "light" : "dark"}
          nodesConnectable
          nodesDraggable
          elementsSelectable
          fitView
        >
          <Background />
          <Controls showInteractive={false} />
        </ReactFlow>
      </div>

      {editingRiddle && (
        <RiddleEditDialog
          key={editingRiddle.id}
          riddle={editingRiddle}
          deerSchema={project}
          open={editingRiddleId !== null}
          setOpen={(open) => { if (!open) setEditingRiddleId(null); }}
          onSave={saveRiddle}
          onDelete={deleteRiddle}
        />
      )}
    </div>
  );
}

function toFlowNode(
  node: AnyGraphNode,
  positions: GraphLayout,
  riddles: Riddle[],
  surfaces: Surface[],
  onEditRiddle: (riddleId: string) => void,
  onEndSurfaceChange: (nodeId: string, surfaceId: string) => void,
): GraphFlowNode {
  const position = positions[node.id] ?? { x: 0, y: 0 };
  switch (node.kind) {
    case "start":
      return { id: node.id, type: "start", position, data: {}, deletable: false };
    case "end":
      return {
        id: node.id,
        type: "end",
        position,
        deletable: false,
        data: {
          surfaceId: node.surfaceId,
          surfaces,
          onSurfaceChange: (surfaceId) => onEndSurfaceChange(node.id, surfaceId),
        },
      };
    case "riddle":
      return {
        id: node.id,
        type: "riddle",
        position,
        deletable: false,
        data: {
          riddleId: node.riddleId,
          riddle: riddles.find((riddle) => riddle.id === node.riddleId),
          onEdit: () => onEditRiddle(node.riddleId),
        },
      };
  }
}
