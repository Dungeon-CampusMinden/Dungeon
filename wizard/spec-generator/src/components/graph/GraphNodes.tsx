import type { Riddle } from "@/data/DeerSchema";
import { Handle, Position, type Node, type NodeProps } from "@xyflow/react";
import {
  CircleQuestionMarkIcon,
  ClockIcon,
  FlagIcon,
  PencilIcon,
  PlayIcon,
} from "lucide-react";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { getRiddleDifficulty } from "../riddles/riddleTypes";

export type StartNodeData = { label?: string };
export type EndNodeData = Record<string, never>;
export type RiddleNodeData = {
  riddleId: string;
  riddle: Riddle | undefined;
  label: string;
  onEdit: () => void;
};

export type GraphFlowNode =
  | Node<StartNodeData, "start">
  | Node<EndNodeData, "end">
  | Node<RiddleNodeData, "riddle">;

const NODE_CLASS =
  "w-56 rounded-lg border border-border bg-card px-3 py-2 text-card-foreground shadow-sm transition-shadow";
const HANDLE_CLASS = "wizard-graph-handle";

export function StartFlowNode({ selected }: NodeProps<Node<StartNodeData, "start">>) {
  return (
    <div className={`${NODE_CLASS} ${selected ? "ring-2 ring-ring" : ""}`}>
      <div className="flex items-center gap-2">
        <PlayIcon size={16} className="text-emerald-400 shrink-0" />
        <span className="text-sm font-medium">Start</span>
      </div>
      <p className="m-0 text-xs text-muted-foreground">Hier beginnt das Abenteuer.</p>
      <Handle type="source" position={Position.Bottom} className={HANDLE_CLASS} />
    </div>
  );
}

export function EndFlowNode({ selected }: NodeProps<Node<EndNodeData, "end">>) {
  return (
    <div className={`${NODE_CLASS} ${selected ? "ring-2 ring-ring" : ""}`}>
      <div className="flex items-center gap-2">
        <FlagIcon size={16} className="text-blue-400 shrink-0" />
        <span className="text-sm font-medium">Ende</span>
      </div>
      <p className="m-0 text-xs text-muted-foreground">Hier endet das Abenteuer.</p>
      <Handle type="target" position={Position.Top} className={HANDLE_CLASS} />
    </div>
  );
}

export function RiddleFlowNode({ data, selected }: NodeProps<Node<RiddleNodeData, "riddle">>) {
  const riddle = data.riddle;
  const difficulty = riddle ? getRiddleDifficulty(riddle.difficulty) : undefined;

  return (
    <div className={`${NODE_CLASS} ${selected ? "ring-2 ring-ring" : ""}`}>
      <div className="flex items-start gap-1">
        <div className="flex min-w-0 flex-1 items-start gap-2">
          <CircleQuestionMarkIcon size={16} className="mt-0.5 shrink-0 text-muted-foreground" />
          <span className="text-sm font-medium break-words">{data.label}</span>
        </div>
        {riddle && (
          <Button
            variant="ghost"
            size="icon"
            className="nodrag size-6 shrink-0"
            aria-label={`${data.label} bearbeiten`}
            onClick={data.onEdit}
          >
            <PencilIcon />
          </Button>
        )}
      </div>

      {riddle && (
        <div className="mt-2 flex flex-wrap items-center gap-1">
          <Badge className={`${difficulty?.className} text-[10px]`}>
            {difficulty?.label ?? riddle.difficulty}
          </Badge>
          <Badge variant="outline" className="text-[10px]">
            <ClockIcon />
            {riddle.estimatedMinutes} Min.
          </Badge>
        </div>
      )}

      <Handle type="target" position={Position.Top} className={HANDLE_CLASS} />
      <Handle type="source" position={Position.Bottom} className={HANDLE_CLASS} />
    </div>
  );
}

export const GRAPH_NODE_TYPES = {
  start: StartFlowNode,
  end: EndFlowNode,
  riddle: RiddleFlowNode,
};
