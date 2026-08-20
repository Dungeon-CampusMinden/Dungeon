import type { Asset } from "@/data/DeerSchema";
import React from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "../ui/dropdown-menu";
import { Button } from "../ui/button";
import { PencilIcon, Trash2Icon } from "lucide-react";
import { AssetPreviewContent } from "./AssetPreviewContent";
import { AssetEditDialog } from "./AssetEditDialog";
import { getAssetDisplayName, type AssetPreview, type AssetSelection } from "./assetPaths";
import { useUploadReferences } from "./UploadReferencesContext";

export function AssetCard({
  asset,
  preview,
  editable = true,
  disabled = false,
  highlighted = false,
  onUpdate,
  onReplaceContent,
  onDelete,
}: {
  asset: Asset;
  preview: AssetPreview | undefined;
  /** When false the edit menu is hidden and the card is a pure preview. */
  editable?: boolean;
  /** Disables the edit trigger while another exclusive Wizard operation is running. */
  disabled?: boolean;
  /** Highlights the card, e.g. while it is hovered or selected from the outside. */
  highlighted?: boolean;
  onUpdate?: (updatedAsset: Asset) => void;
  onReplaceContent?: (asset: Asset, selection: AssetSelection) => Promise<void>;
  onDelete?: (asset: Asset) => void;
}) {
  const uploads = useUploadReferences();
  const [editOpen, setEditOpen] = React.useState(false);
  const missing = preview?.missing ?? false;
  const technicalError = preview?.technicalError ?? false;
  const fileName = getAssetDisplayName(asset, uploads[asset.id]);

  const stateClasses = technicalError
    ? "border-yellow-500/60 bg-yellow-500/5"
    : missing
    ? "border-destructive bg-destructive/5"
    : highlighted
      ? "border-primary bg-primary/5 ring-2 ring-primary"
      : "border-border bg-muted/30";

  return (
    <div className="flex min-w-0 flex-col gap-1">
      <div
        title={technicalError ? "Lokaler Speicher nicht verfügbar" : missing ? "Datei fehlt" : fileName}
        className={`flex aspect-square items-center justify-center rounded-lg border p-2 transition-colors ${stateClasses}`}
      >
        <AssetPreviewContent asset={asset} preview={preview} />
      </div>
      <div className="flex min-w-0 items-center gap-1">
        <span className="min-w-0 flex-1 truncate text-sm" title={fileName}>
          {fileName}
        </span>
        {editable && (
          <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="outline" size="icon-sm" disabled={disabled} />}>
              <PencilIcon />
              <span className="sr-only">Datei bearbeiten</span>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem disabled={disabled} onClick={() => setEditOpen(true)}>
                <PencilIcon />
                Bearbeiten
              </DropdownMenuItem>
              <DropdownMenuItem disabled={disabled} variant="destructive" onClick={() => onDelete?.(asset)}>
                <Trash2Icon />
                Löschen
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )}
      </div>

      {editable && (
        <AssetEditDialog
          asset={asset}
          displayName={fileName}
          missing={missing}
          disabled={disabled}
          open={editOpen}
          setOpen={setEditOpen}
          onUpdate={(updatedAsset) => onUpdate?.(updatedAsset)}
          onReplaceContent={async (updatedAsset, selection) => {
            await onReplaceContent?.(updatedAsset, selection);
          }}
        />
      )}
    </div>
  );
}
