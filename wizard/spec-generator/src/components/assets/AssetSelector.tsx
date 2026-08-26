import type { Asset } from "@/data/DeerSchema";
import React from "react";
import { AssetCard } from "./AssetCard";
import { useAssetPreviews } from "./useAssetPreviews";
import { getAssetDisplayName } from "./assetPaths";
import { useUploadReferences } from "./UploadReferencesContext";
import { Button } from "../ui/button";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "../ui/dialog";
import { ImageIcon } from "lucide-react";

/**
 * Reusable picker that lets the user choose one asset out of the assets of the deer schema.
 * The value is the id of the selected asset, an empty string means "nothing selected".
 */
export function AssetSelector({
  items,
  value,
  onChange,
  allowEmpty = true,
  className = "",
  accessibleLabel = "Datei auswählen",
}: {
  items: Asset[];
  value: string;
  onChange: (newValue: string) => void;
  /** When true, the dialog offers an entry to clear the selection. */
  allowEmpty?: boolean;
  className?: string;
  accessibleLabel?: string;
}) {
  const uploads = useUploadReferences();
  const previews = useAssetPreviews(items);
  const [dialogOpen, setDialogOpen] = React.useState(false);

  const selectedAsset = items.find((asset) => asset.id === value);

  return (
    <div className={`flex items-center gap-2 ${className}`}>
      <button
        type="button"
        aria-label={accessibleLabel}
        className="w-32 cursor-pointer rounded-lg p-0 text-left"
        onClick={() => setDialogOpen(true)}
      >
        {selectedAsset ? (
          <AssetCard asset={selectedAsset} preview={previews[selectedAsset.id]} editable={false} />
        ) : (
          <EmptyAssetCard />
        )}
      </button>
      <Button aria-label={accessibleLabel} variant="outline" onClick={() => setDialogOpen(true)}>
        <ImageIcon />
        Datei wählen
      </Button>

      <AssetSelectDialog
        items={items}
        value={value}
        onChange={onChange}
        allowEmpty={allowEmpty}
        open={dialogOpen}
        setOpen={setDialogOpen}
        uploads={uploads}
      />
    </div>
  );
}

/** Dialog listing all assets of the deer schema. Clicking an asset confirms the choice. */
function AssetSelectDialog({
  items,
  value,
  onChange,
  allowEmpty,
  open,
  setOpen,
  uploads,
}: {
  items: Asset[];
  value: string;
  onChange: (newValue: string) => void;
  allowEmpty: boolean;
  open: boolean;
  setOpen: (open: boolean) => void;
  uploads: ReturnType<typeof useUploadReferences>;
}) {
  const previews = useAssetPreviews(items);
  const [hoveredId, setHoveredId] = React.useState<string | null>(null);

  const itemsSorted = [...items].sort((a, b) =>
    getAssetDisplayName(a, uploads[a.id]).localeCompare(getAssetDisplayName(b, uploads[b.id])),
  );

  const select = (newValue: string) => {
    onChange(newValue);
    setOpen(false);
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent className="max-h-[85vh] grid-rows-[auto_minmax(0,1fr)] sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>Datei auswählen</DialogTitle>
          <DialogDescription>Wähle eine der Dateien deines Abenteuers aus.</DialogDescription>
        </DialogHeader>

        <div className="min-h-0 overflow-y-auto">
          {itemsSorted.length === 0 && !allowEmpty ? (
            <p className="py-16 text-center text-muted-foreground">Noch keine Dateien hinzugefügt.</p>
          ) : (
            <div className="grid grid-cols-3 gap-4 sm:grid-cols-5 p-1">
              {allowEmpty && (
                <button
                  type="button"
                  aria-pressed={value === ""}
                  className="cursor-pointer rounded-lg p-0 text-left"
                  onMouseEnter={() => setHoveredId("")}
                  onMouseLeave={() => setHoveredId((current) => (current === "" ? null : current))}
                  onFocus={() => setHoveredId("")}
                  onBlur={() => setHoveredId((current) => (current === "" ? null : current))}
                  onClick={() => select("")}
                >
                  <EmptyAssetCard highlighted={value === "" || hoveredId === ""} />
                </button>
              )}
              {itemsSorted.map((asset) => {
                const selected = asset.id === value;
                return (
                  <button
                    key={asset.id}
                    type="button"
                    aria-pressed={selected}
                    className="cursor-pointer rounded-lg p-0 text-left"
                    onMouseEnter={() => setHoveredId(asset.id)}
                    onMouseLeave={() => setHoveredId((current) => (current === asset.id ? null : current))}
                    onFocus={() => setHoveredId(asset.id)}
                    onBlur={() => setHoveredId((current) => (current === asset.id ? null : current))}
                    onClick={() => select(asset.id)}
                  >
                    <AssetCard
                      asset={asset}
                      preview={previews[asset.id]}
                      editable={false}
                      highlighted={selected || hoveredId === asset.id}
                    />
                  </button>
                );
              })}
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}

/** Placeholder card shown when no asset is selected. */
function EmptyAssetCard({ highlighted = false }: { highlighted?: boolean }) {
  return (
    <div className="flex min-w-0 flex-col gap-1">
      <div
        className={`flex aspect-square items-center justify-center rounded-lg border border-dashed p-2 transition-colors ${
          highlighted ? "border-primary bg-primary/5 ring-2 ring-primary" : "border-border bg-muted/30"
        }`}
      >
        <ImageIcon className="size-8 text-muted-foreground" />
      </div>
      <span className="truncate text-sm text-muted-foreground">Keine Datei</span>
    </div>
  );
}
