import React from "react";
import { CircleAlertIcon } from "lucide-react";
import { Tooltip, TooltipContent, TooltipTrigger } from "../ui/tooltip";

export function AssetTile({
  children,
  label,
  selected = false,
  warning,
  onClick,
}: {
  children: React.ReactNode;
  label: string;
  selected?: boolean;
  warning?: string;
  onClick: () => void;
}) {
  return (
    <div className="relative aspect-square min-w-0">
      <button
        type="button"
        aria-pressed={selected || undefined}
        title={label}
        onClick={onClick}
        className={`flex size-full min-w-0 flex-col items-center justify-center gap-2 rounded-lg border p-2 text-xs transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${
          selected
            ? "border-primary bg-primary/10 text-primary ring-2 ring-primary/30"
            : "border-border bg-background hover:border-primary/50 hover:bg-accent"
        }`}
      >
        {children}
        <span className="w-full truncate text-center">{label}</span>
      </button>
      {warning && (
        <Tooltip>
          <TooltipTrigger
            render={
              <span
                role="img"
                aria-label={warning}
                tabIndex={0}
                className="absolute right-2 top-2 inline-flex size-6 items-center justify-center text-status-warning outline-none focus-visible:ring-2 focus-visible:ring-status-warning"
              />
            }
          >
            <CircleAlertIcon className="size-4" />
          </TooltipTrigger>
          <TooltipContent>{warning}</TooltipContent>
        </Tooltip>
      )}
    </div>
  );
}
