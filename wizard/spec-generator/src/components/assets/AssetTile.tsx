import React from "react";

export function AssetTile({
  children,
  label,
  selected = false,
  onClick,
}: {
  children: React.ReactNode;
  label: string;
  selected?: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      aria-pressed={selected || undefined}
      title={label}
      onClick={onClick}
      className={`flex aspect-square min-w-0 flex-col items-center justify-center gap-2 rounded-lg border p-2 text-xs transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${
        selected
          ? "border-primary bg-primary/10 text-primary ring-2 ring-primary/30"
          : "border-border bg-background hover:border-primary/50 hover:bg-accent"
      }`}
    >
      {children}
      <span className="w-full truncate text-center">{label}</span>
    </button>
  );
}
