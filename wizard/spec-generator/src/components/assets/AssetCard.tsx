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
import { getAssetName, type AssetPreview, type AssetSelection } from "./assetPaths";

export function AssetCard({
  asset,
  preview,
  onUpdate,
  onReplaceContent,
  onDelete,
}: {
  asset: Asset;
  preview: AssetPreview | undefined;
  onUpdate: (updatedAsset: Asset) => void;
  onReplaceContent: (asset: Asset, selection: AssetSelection) => Promise<void>;
  onDelete: (asset: Asset) => void;
}) {
  const [editOpen, setEditOpen] = React.useState(false);
  const missing = preview?.missing ?? false;
  const fileName = getAssetName(asset.path);

  return (
    <div className="flex min-w-0 flex-col gap-1">
      <div
        title={missing ? "Datei fehlt" : fileName}
        className={`flex aspect-square items-center justify-center rounded-lg border p-2 ${
          missing ? "border-destructive bg-destructive/5" : "border-border bg-muted/30"
        }`}
      >
        <AssetPreviewContent asset={asset} preview={preview} />
      </div>
      <div className="flex min-w-0 items-center gap-1">
        <span className="min-w-0 flex-1 truncate text-sm" title={fileName}>
          {fileName}
        </span>
        <DropdownMenu>
          <DropdownMenuTrigger render={<Button variant="outline" size="icon-sm" />}>
            <PencilIcon />
            <span className="sr-only">Datei bearbeiten</span>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => setEditOpen(true)}>
              <PencilIcon />
              Bearbeiten
            </DropdownMenuItem>
            <DropdownMenuItem variant="destructive" onClick={() => onDelete(asset)}>
              <Trash2Icon />
              Löschen
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <AssetEditDialog
        asset={asset}
        missing={missing}
        open={editOpen}
        setOpen={setEditOpen}
        onUpdate={onUpdate}
        onReplaceContent={onReplaceContent}
      />
    </div>
  );
}
