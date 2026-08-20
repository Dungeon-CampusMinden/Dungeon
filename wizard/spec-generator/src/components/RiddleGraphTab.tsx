import type { AnyGraphNode, DeerProject, Riddle } from "@/data/DeerSchema";
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
  type IsValidConnection,
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
import { GRAPH_EDGE_TYPES, type DeletableGraphEdge } from "./graph/DeletableEdge";
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
  const riddleLabels = React.useMemo(
    () => new Map(riddles.map((riddle, index) => [riddle.id, riddleGraphLabel(riddle, index)])),
    [riddles],
  );
  const nodeLabels = React.useMemo(
    () => new Map(graph.nodes.map((node) => [node.id, graphNodeLabel(node, riddleLabels)])),
    [graph, riddleLabels],
  );
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

  const flowNodes = React.useMemo<GraphFlowNode[]>(
    () => graph.nodes.map((node) => toFlowNode(node, positions, riddles, riddleLabels, setEditingRiddleId)),
    [graph, positions, riddles, riddleLabels],
  );
  React.useEffect(() => setNodes(flowNodes), [flowNodes, setNodes]);

  const flowEdges = React.useMemo<DeletableGraphEdge[]>(
    () => graph.edges.map((edge) => ({
      id: `${edge.from}->${edge.to}`,
      source: edge.from,
      target: edge.to,
      type: "deletable",
      data: {
        sourceLabel: nodeLabels.get(edge.from) ?? "Nicht mehr vorhandener Schritt",
        targetLabel: nodeLabels.get(edge.to) ?? "Nicht mehr vorhandener Schritt",
      },
      markerEnd: { type: MarkerType.ArrowClosed },
    })),
    [graph, nodeLabels],
  );

  const connectNodes = React.useCallback((source: string, target: string) => {
    updateDraft((current) => {
      const nodes = current.project.riddleGraph.nodes;
      const edges = current.project.riddleGraph.edges;
      const sourceNode = nodes.find((node) => node.id === source);
      const targetNode = nodes.find((node) => node.id === target);
      if (!sourceNode || !targetNode || sourceNode.kind === "end" || targetNode.kind === "start") {
        toast.error("Diese Schritte können nicht in dieser Richtung verbunden werden.");
        return false;
      }
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

  const onConnect = React.useCallback<OnConnect>((connection: Connection) => {
    const { source, target } = connection;
    if (source && target) connectNodes(source, target);
  }, [connectNodes]);

  const isValidConnection = React.useCallback<IsValidConnection>((connection) => {
    const { source, target } = connection;
    if (!source || !target || source === target) return false;
    const sourceNode = graph.nodes.find((node) => node.id === source);
    const targetNode = graph.nodes.find((node) => node.id === target);
    if (!sourceNode || !targetNode || sourceNode.kind === "end" || targetNode.kind === "start") {
      return false;
    }
    return !hasEdge(graph.edges, source, target) && !wouldCreateCycle(graph.edges, source, target);
  }, [graph]);

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

  const updateRiddle = (updated: DeerProject) => {
    updateDraft((current) => {
      const edited = updated.riddles.find((riddle) => riddle.id === editingRiddleId);
      if (!edited) return false;
      const index = current.project.riddles.findIndex((riddle) => riddle.id === edited.id);
      if (index === -1) return false;
      current.project.riddles[index] = structuredClone(edited);
      current.project.surfaces = structuredClone(updated.surfaces);
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
        Ziehe vom blauen Verbindungspunkt eines Schritts zum nächsten. Der folgende Schritt wird nach
        seinem Vorgänger verfügbar. Hat er mehrere Vorgänger, müssen alle davon gelöst sein. Verbindungen
        entfernst du über das X in ihrer Mitte oder mit der ENTF-Taste.
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
          isValidConnection={isValidConnection}
          onEdgesDelete={onEdgesDelete}
          connectionMode={ConnectionMode.Strict}
          connectionRadius={28}
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
          onChange={updateRiddle}
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
  riddleLabels: ReadonlyMap<string, string>,
  onEditRiddle: (riddleId: string) => void,
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
        data: {},
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
          label: riddleLabels.get(node.riddleId) ?? "Nicht mehr vorhandenes Rätsel",
          onEdit: () => onEditRiddle(node.riddleId),
        },
      };
  }
}

function riddleGraphLabel(riddle: Riddle, index: number): string {
  return `Rätsel ${index + 1}: ${riddle.title.trim() || "Unbenannt"}`;
}

function graphNodeLabel(node: AnyGraphNode, riddleLabels: ReadonlyMap<string, string>): string {
  if (node.kind === "start") return "Start";
  if (node.kind === "end") return "Ende";
  return riddleLabels.get(node.riddleId) ?? "Nicht mehr vorhandenes Rätsel";
}
