import type { AnyGraphNode, DeerSchema, Riddle, RiddleGraph, Surface } from "@/data/DeerSchema";
import { Util } from "@/data/Util";
import { useLocalStorage } from "@uidotdev/usehooks";
import {
  Background,
  ConnectionMode,
  Controls,
  MarkerType,
  MiniMap,
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
import { GRAPH_EDGE_TYPES } from "./graph/DeletableEdge";
import { GRAPH_NODE_TYPES, type GraphFlowNode } from "./graph/GraphNodes";
import {
  computeAutoLayout,
  GRAPH_LAYOUT_STORAGE_KEY,
  hasEdge,
  withMissingPositions,
  wouldCreateCycle,
  type GraphLayout,
} from "./graph/graphLayout";

export function RiddleGraphTab(props: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  return (
    <ReactFlowProvider>
      <RiddleGraphEditor {...props} />
    </ReactFlowProvider>
  );
}

/**
 * `useLocalStorage` re-parses its JSON on every render, so both the schema and the layout arrive
 * with a fresh object identity each time. Everything React Flow receives is therefore derived from
 * serialized keys, otherwise the flow store would be updated in an endless loop.
 */
function useStableValue<T>(value: T): T {
  const key = JSON.stringify(value);
  return React.useMemo(() => JSON.parse(key) as T, [key]);
}

function RiddleGraphEditor({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  const { resolvedTheme } = useTheme();
  const [layout, setLayout] = useLocalStorage<GraphLayout>(GRAPH_LAYOUT_STORAGE_KEY, {});
  const [editingRiddleId, setEditingRiddleId] = React.useState<string | null>(null);
  const [nodes, setNodes, onNodesChange] = useNodesState<GraphFlowNode>([]);

  // Event handlers always write to the schema of the latest render, not to a captured copy.
  const schemaRef = React.useRef(deerSchema);
  const updateRef = React.useRef(updateDeerSchema);
  React.useEffect(() => {
    schemaRef.current = deerSchema;
    updateRef.current = updateDeerSchema;
  });

  const graph = useStableValue(deerSchema.riddleGraph);
  const riddles = useStableValue(deerSchema.riddles);
  const surfaces = useStableValue(deerSchema.surfaces);
  const storedLayout = useStableValue(layout);

  useGraphRiddleSync(graph, riddles, schemaRef, updateRef);

  const positions = React.useMemo(() => withMissingPositions(graph, storedLayout), [graph, storedLayout]);

  React.useEffect(() => {
    if (positions !== storedLayout) setLayout(positions);
  }, [positions, storedLayout, setLayout]);

  const onEndSurfaceChange = React.useCallback((nodeId: string, surfaceId: string) => {
    const schema = schemaRef.current;
    const node = schema.riddleGraph.nodes.find((candidate) => candidate.id === nodeId && candidate.kind === "end");
    if (!node || node.kind !== "end") return;

    node.surfaceId = surfaceId;
    updateRef.current(schema);
  }, []);

  const flowNodes = React.useMemo<GraphFlowNode[]>(
    () =>
      graph.nodes.map((node) =>
        toFlowNode(node, positions, riddles, surfaces, setEditingRiddleId, onEndSurfaceChange),
      ),
    [graph, positions, riddles, surfaces, onEndSurfaceChange],
  );

  React.useEffect(() => setNodes(flowNodes), [flowNodes, setNodes]);

  const flowEdges = React.useMemo<Edge[]>(
    () =>
      graph.edges.map((edge) => ({
        id: `${edge.from}->${edge.to}`,
        source: edge.from,
        target: edge.to,
        type: "deletable",
        markerEnd: { type: MarkerType.ArrowClosed },
      })),
    [graph],
  );

  const isValidConnection = React.useCallback<IsValidConnection>(
    (connection) => {
      const { source, target } = connection;
      if (!source || !target || source === target) return false;
      if (hasEdge(graph.edges, source, target)) return false;
      return !wouldCreateCycle(graph.edges, source, target);
    },
    [graph],
  );

  const onConnect = React.useCallback<OnConnect>((connection: Connection) => {
    const { source, target } = connection;
    if (!source || !target) return;

    const schema = schemaRef.current;
    const edges = schema.riddleGraph.edges;
    if (source === target) {
      toast.error("Ein Schritt kann nicht mit sich selbst verbunden werden.");
      return;
    }
    if (hasEdge(edges, source, target)) {
      toast.error("Diese Verbindung gibt es bereits.");
      return;
    }
    if (wouldCreateCycle(edges, source, target)) {
      toast.error("Diese Verbindung würde einen Kreis erzeugen und ist deshalb nicht erlaubt.");
      return;
    }

    schema.riddleGraph.edges = [...edges, { from: source, to: target }];
    updateRef.current(schema);
  }, []);

  const onEdgesDelete = React.useCallback((deleted: Edge[]) => {
    const deletedIds = new Set(deleted.map((edge) => edge.id));
    const schema = schemaRef.current;
    schema.riddleGraph.edges = schema.riddleGraph.edges.filter(
      (edge) => !deletedIds.has(`${edge.from}->${edge.to}`),
    );
    updateRef.current(schema);
  }, []);

  const onNodeDragStop = React.useCallback<OnNodeDrag<GraphFlowNode>>(
    (_event, _node, draggedNodes) => {
      const moved = Object.fromEntries(draggedNodes.map((node) => [node.id, node.position]));
      setLayout({ ...positions, ...moved });
    },
    [positions, setLayout],
  );

  const editingIndex = deerSchema.riddles.findIndex((riddle) => riddle.id === editingRiddleId);
  const editingRiddle = editingIndex >= 0 ? deerSchema.riddles[editingIndex] : null;

  const saveRiddle = (updated: Riddle) => {
    deerSchema.riddles[editingIndex] = updated;
    updateDeerSchema(deerSchema);
  };

  const deleteRiddle = () => {
    deerSchema.riddles.splice(editingIndex, 1);
    updateDeerSchema(deerSchema);
  };

  return (
    <div className="flex flex-col gap-0">
      <h1>Spielablauf</h1>
      <p className="text-sm text-muted-foreground">
        Hier legst du fest, in welcher Reihenfolge die Rätsel gelöst werden. Ziehe eine Verbindung von einem
        Punkt am unteren Rand eines Rätsels zum oberen Rand eines darauf folgenden Rätsels. Eine Verbindung
        entfernst du über das X in ihrer Mitte oder mit der ENTF-Taste.
      </p>
      <Button
        variant="outline"
        className="my-2 max-w-52"
        onClick={() => setLayout(computeAutoLayout(schemaRef.current.riddleGraph))}
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
          isValidConnection={isValidConnection}
          connectionMode={ConnectionMode.Strict}
          colorMode={resolvedTheme === "light" ? "light" : "dark"}
          nodesConnectable
          nodesDraggable
          elementsSelectable
          fitView
        >
          <Background />
          <Controls showInteractive={false} />
          {/* <MiniMap pannable zoomable /> */}
        </ReactFlow>
      </div>

      {editingRiddle && (
        <RiddleEditDialog
          key={editingRiddle.id}
          riddle={editingRiddle}
          deerSchema={deerSchema}
          open={editingRiddleId !== null}
          setOpen={(open) => {
            if (!open) setEditingRiddleId(null);
          }}
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

/** Every riddle gets exactly one node, so the graph never drifts away from the riddle list. */
function useGraphRiddleSync(
  graph: RiddleGraph,
  riddles: Riddle[],
  schemaRef: React.RefObject<DeerSchema>,
  updateRef: React.RefObject<(updatedSchema: DeerSchema) => void>,
) {
  React.useEffect(() => {
    // Reconcile against the latest mutable schema so repeated mount effects remain idempotent.
    const schema = schemaRef.current;
    const currentNodes = schema.riddleGraph.nodes;
    const riddleIds = new Set(riddles.map((riddle) => riddle.id));
    const canonicalNodeIds = new Map<string, string>();
    const staleNodeIds = new Set<string>();
    const duplicateNodeIds = new Map<string, string>();
    const retainedNodes = currentNodes.filter((node) => {
      if (node.kind !== "riddle") return true;
      if (!riddleIds.has(node.riddleId)) {
        staleNodeIds.add(node.id);
        return false;
      }
      const canonicalNodeId = canonicalNodeIds.get(node.riddleId);
      if (canonicalNodeId !== undefined) {
        duplicateNodeIds.set(node.id, canonicalNodeId);
        return false;
      }
      canonicalNodeIds.set(node.riddleId, node.id);
      return true;
    });
    const missingRiddles = riddles.filter((riddle) => !canonicalNodeIds.has(riddle.id));
    const hasStartNode = currentNodes.some((node) => node.kind === "start");
    if (
      staleNodeIds.size === 0 &&
      duplicateNodeIds.size === 0 &&
      missingRiddles.length === 0 &&
      hasStartNode
    ) {
      return;
    }

    const newNodes: AnyGraphNode[] = missingRiddles.map((riddle) => ({
      id: Util.generateUniqueId("n"),
      kind: "riddle",
      riddleId: riddle.id,
    }));
    if (!hasStartNode) {
      newNodes.unshift({ id: Util.generateUniqueId("n"), kind: "start" });
    }

    schema.riddleGraph.nodes = [
      ...retainedNodes,
      ...newNodes,
    ];
    const edgeKeys = new Set<string>();
    schema.riddleGraph.edges = schema.riddleGraph.edges
      .filter((edge) => !staleNodeIds.has(edge.from) && !staleNodeIds.has(edge.to))
      .map((edge) => ({
        from: duplicateNodeIds.get(edge.from) ?? edge.from,
        to: duplicateNodeIds.get(edge.to) ?? edge.to,
      }))
      .filter((edge) => edge.from !== edge.to)
      .filter((edge) => {
        const key = `${edge.from}->${edge.to}`;
        if (edgeKeys.has(key)) return false;
        edgeKeys.add(key);
        return true;
      });
    updateRef.current(schema);
  }, [graph, riddles, schemaRef, updateRef]);
}
