import {
  BaseEdge,
  EdgeLabelRenderer,
  getBezierPath,
  useReactFlow,
  type Edge,
  type EdgeProps,
} from "@xyflow/react";
import { XIcon } from "lucide-react";

interface DeletableEdgeData extends Record<string, unknown> {
  sourceLabel: string;
  targetLabel: string;
}

export type DeletableGraphEdge = Edge<DeletableEdgeData, "deletable"> & {
  data: DeletableEdgeData;
};

/** Edge with a delete button, so connections can be removed without knowing the Del shortcut. */
export function DeletableEdge({
  id,
  data,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  markerEnd,
  style,
}: EdgeProps<DeletableGraphEdge>) {
  const { deleteElements } = useReactFlow();
  const [edgePath, labelX, labelY] = getBezierPath({
    sourceX,
    sourceY,
    targetX,
    targetY,
    sourcePosition,
    targetPosition,
  });

  return (
    <>
      <BaseEdge id={id} path={edgePath} markerEnd={markerEnd} style={style} />
      <EdgeLabelRenderer>
        <button
          type="button"
          className="nodrag nopan pointer-events-auto absolute flex size-5 items-center justify-center rounded-full border border-border bg-card text-muted-foreground opacity-70 transition-opacity hover:bg-destructive/10 hover:text-destructive hover:opacity-100"
          style={{ transform: `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)` }}
          aria-label={`Verbindung von ${data.sourceLabel} nach ${data.targetLabel} entfernen`}
          onClick={() => deleteElements({ edges: [{ id }] })}
        >
          <XIcon size={12} />
        </button>
      </EdgeLabelRenderer>
    </>
  );
}

export const GRAPH_EDGE_TYPES = { deletable: DeletableEdge };
