import type { Asset } from "@/data/DeerSchema";
import React from "react";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../ui/dialog";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Field, FieldLabel } from "../ui/field";
import { Separator } from "../ui/separator";
import { UploadIcon } from "lucide-react";
import { AssetCreateDialog } from "./AssetCreateDialog";
import { getBundledAssetSource, isBundledAssetPath, type AssetSelection } from "./assetPaths";

export function AssetEditDialog({
  asset,
  displayName,
  missing,
  disabled,
  open,
  setOpen,
  onUpdate,
  onReplaceContent,
}: {
  asset: Asset;
  displayName: string;
  missing: boolean;
  disabled: boolean;
  open: boolean;
  setOpen: (open: boolean) => void;
  onUpdate: (updatedAsset: Asset) => void;
  onReplaceContent: (asset: Asset, selection: AssetSelection) => Promise<void>;
}) {
  const [selectorOpen, setSelectorOpen] = React.useState(false);
  const bundledAsset = isBundledAssetPath(asset.path);
  const bundledSourceLocked = bundledAsset && getBundledAssetSource(asset.path) !== null;
  const sourceFieldsDisabled = disabled || bundledSourceLocked;
  const description = bundledSourceLocked
    ? "Lizenzangaben aus der Spielbibliothek können nicht geändert werden. Du kannst die Datei ersetzen."
    : bundledAsset
      ? "Für diese Datei fehlt eine Lizenzangabe. Ergänze sie oder ersetze die Datei."
      : "Lizenzangaben anpassen oder die Datei ersetzen.";
  const updateSource = (license: string, attribution: string) => {
    onUpdate({
      ...asset,
      source: {
        ...asset.source,
        license,
        attribution: attribution.trim() === "" ? undefined : attribution,
      },
    });
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Datei bearbeiten</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>

        <div className="flex flex-col gap-4">
          <Field>
            <FieldLabel>Lizenz</FieldLabel>
            <Input
              aria-label="Lizenz der Datei"
              disabled={sourceFieldsDisabled}
              value={asset.source.license}
              onChange={(e) => updateSource(e.target.value, asset.source.attribution ?? "")}
            />
          </Field>
          <Field>
            <FieldLabel>Urheber</FieldLabel>
            <Input
              aria-label="Urheber der Datei"
              disabled={sourceFieldsDisabled}
              value={asset.source.attribution ?? ""}
              onChange={(e) => updateSource(asset.source.license, e.target.value)}
            />
          </Field>

          <Separator />

          <Field>
            <FieldLabel>Dateiname</FieldLabel>
            <div className="grid grid-cols-[1fr_auto] items-center gap-2">
              <Input aria-label="Dateiname" value={displayName} readOnly aria-invalid={missing} />
              <Button variant="outline" disabled={disabled} onClick={() => setSelectorOpen(true)}>
                <UploadIcon />
                Ersetzen
              </Button>
            </div>
            {missing && (
              <span className="text-sm text-destructive">
                Die hinterlegte Datei ist nicht verfügbar. Wähle eine neue Datei aus, um sie zu ersetzen.
              </span>
            )}
          </Field>

          <AssetCreateDialog
            open={selectorOpen}
            setOpen={setSelectorOpen}
            currentPath={asset.path}
            onSelect={(selection) => onReplaceContent(asset, selection)}
            title="Datei ersetzen"
          />
        </div>

        <DialogFooter>
          <DialogClose render={<Button />}>Fertig</DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
