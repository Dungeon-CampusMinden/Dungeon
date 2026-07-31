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
import { AssetSelectorDialog } from "./AssetSelectorDialog";
import type { AssetSelection } from "./assetPaths";

export function AssetEditDialog({
  asset,
  missing,
  open,
  setOpen,
  onUpdate,
  onReplaceContent,
}: {
  asset: Asset;
  missing: boolean;
  open: boolean;
  setOpen: (open: boolean) => void;
  onUpdate: (updatedAsset: Asset) => void;
  onReplaceContent: (asset: Asset, selection: AssetSelection) => Promise<void>;
}) {
  const [selectorOpen, setSelectorOpen] = React.useState(false);
  const [license, setLicense] = React.useState(asset.source.license);
  const [attribution, setAttribution] = React.useState(asset.source.attribution ?? "");

  const handleOpenChange = (nextOpen: boolean) => {
    if (nextOpen) {
      setLicense(asset.source.license);
      setAttribution(asset.source.attribution ?? "");
    }
    setOpen(nextOpen);
  };

  const handleSave = () => {
    onUpdate({
      ...asset,
      source: { ...asset.source, license, attribution },
    });
    setOpen(false);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Datei bearbeiten</DialogTitle>
          <DialogDescription>Lizenzangaben anpassen oder die Datei ersetzen.</DialogDescription>
        </DialogHeader>

        <div className="flex flex-col gap-4">
          <Field>
            <FieldLabel>Lizenz</FieldLabel>
            <Input value={license} onChange={(e) => setLicense(e.target.value)} />
          </Field>
          <Field>
            <FieldLabel>Urheber</FieldLabel>
            <Input value={attribution} onChange={(e) => setAttribution(e.target.value)} />
          </Field>

          <Separator />

          <Field>
            <FieldLabel>Dateipfad</FieldLabel>
            <div className="grid grid-cols-[1fr_auto] items-center gap-2">
              <Input value={asset.path} readOnly aria-invalid={missing} />
              <Button variant="outline" onClick={() => setSelectorOpen(true)}>
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

          <AssetSelectorDialog
            open={selectorOpen}
            setOpen={setSelectorOpen}
            currentPath={asset.path}
            onSelect={(selection) => void onReplaceContent(asset, selection)}
            title="Datei ersetzen"
          />
        </div>

        <DialogFooter>
          <DialogClose render={<Button variant="outline" />}>Abbrechen</DialogClose>
          <Button onClick={handleSave}>Speichern</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
