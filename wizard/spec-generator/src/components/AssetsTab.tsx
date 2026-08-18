import type { Asset } from "@/data/DeerSchema";
import React from "react";
import { Button } from "./ui/button";
import { PlusIcon } from "lucide-react";
import { toast } from "sonner";
import { Util } from "@/data/Util";
import { useWizardStorage } from "@/data/WizardStorage";
import type { UpdateDraft, UploadReference, WizardDraft } from "@/data/WizardDraft";
import { AssetCard } from "./assets/AssetCard";
import { AssetCreateDialog } from "./assets/AssetCreateDialog";
import { useAssetPreviews } from "./assets/useAssetPreviews";
import {
  ALLOWED_EXTENSIONS,
  createCustomAssetPath,
  getMediaTypeForPath,
  validateCustomAssetFile,
  type AssetSelection,
} from "./assets/assetPaths";
import type { WizardWork } from "@/data/WizardWork";

export function AssetsTab({ draft, updateDraft, beginWork, finishWork }: {
  draft: WizardDraft;
  updateDraft: UpdateDraft;
  beginWork: (work: Extract<WizardWork, "uploading">) => boolean;
  finishWork: (work: Extract<WizardWork, "uploading">) => void;
}) {
  const storage = useWizardStorage();
  const project = draft.project;
  const assetList = project.assets;
  const [addOpen, setAddOpen] = React.useState(false);
  const [storageRevision, setStorageRevision] = React.useState(0);
  const previews = useAssetPreviews(assetList, storageRevision);

  const applySelection = async (
    selection: AssetSelection,
  ): Promise<{ path: string; mediaType: Asset["mediaType"]; upload?: UploadReference }> => {
    if (selection.kind === "custom") {
      const mediaType = validateCustomAssetFile(selection.file);
      const storageKey = await storage.assets.putAssetFile(draft.draftId, selection.file);
      return {
        path: createCustomAssetPath(selection.file.name, storageKey),
        mediaType,
        upload: { storageKey, originalName: selection.file.name },
      };
    }
    return { path: selection.path, mediaType: getMediaTypeForPath(selection.path) };
  };

  const handleAddAsset = async (selection: AssetSelection) => {
    if (!beginWork("uploading")) throw new Error("Ein anderer Speichervorgang läuft bereits.");
    const id = Util.generateUniqueId("a");
    try {
      const { path, mediaType, upload } = await applySelection(selection);
      updateDraft((current) => {
        current.project.assets.push({ id, path, mediaType, source: { license: "" } });
        if (upload) current.uploads[id] = upload;
      });
      setStorageRevision((revision) => revision + 1);
    } catch (error) {
      toast.error("Die Datei konnte nicht gespeichert werden.", {
        description: error instanceof Error ? error.message : undefined,
      });
      throw error;
    } finally {
      finishWork("uploading");
    }
  };

  const handleReplaceContent = async (asset: Asset, selection: AssetSelection) => {
    if (!beginWork("uploading")) throw new Error("Ein anderer Speichervorgang läuft bereits.");
    const assetId = asset.id;
    try {
      const { path, mediaType, upload } = await applySelection(selection);
      updateDraft((current) => {
        const currentAsset = current.project.assets.find((entry) => entry.id === assetId);
        if (!currentAsset) throw new Error("Die Datei wurde zwischenzeitlich gelöscht.");
        currentAsset.path = path;
        currentAsset.mediaType = mediaType;
        if (upload) current.uploads[assetId] = upload;
        else delete current.uploads[assetId];
      });
      setStorageRevision((revision) => revision + 1);
    } catch (error) {
      toast.error("Der neue Dateiinhalt konnte nicht gespeichert werden.", {
        description: error instanceof Error ? error.message : undefined,
      });
      throw error;
    } finally {
      finishWork("uploading");
    }
  };

  const handleUpdateAsset = (updatedAsset: Asset) => {
    updateDraft((current) => {
      const index = current.project.assets.findIndex((asset) => asset.id === updatedAsset.id);
      if (index === -1) return false;
      current.project.assets[index] = structuredClone(updatedAsset);
    });
  };

  const handleDeleteAsset = (asset: Asset) => {
    updateDraft((current) => {
      const index = current.project.assets.findIndex((entry) => entry.id === asset.id);
      if (index === -1) return false;
      current.project.assets.splice(index, 1);
      // Blob content may be shared by another draft. A later GC can remove unreferenced data.
      delete current.uploads[asset.id];
    });
  };

  return (
    <div className="flex flex-col gap-0">
      <h1>Bilder und Dateien</h1>
      <p className="text-sm text-muted-foreground">
        Eigene oder mitgelieferte Dateien für den Raum. Erlaubte Formate: {ALLOWED_EXTENSIONS.join(", ")}.
      </p>
      <Button onClick={() => setAddOpen(true)} className="my-2 max-w-40">
        <PlusIcon />
        Hinzufügen
      </Button>
      <AssetCreateDialog
        open={addOpen}
        setOpen={setAddOpen}
        onSelect={handleAddAsset}
        title="Datei hinzufügen"
      />
      {assetList.length === 0 ? (
        <p className="py-8 text-center text-muted-foreground">Noch keine Dateien hinzugefügt.</p>
      ) : (
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-5">
          {assetList.map((asset) => (
            <AssetCard
              key={asset.id}
              asset={asset}
              preview={previews[asset.id]}
              onUpdate={handleUpdateAsset}
              onReplaceContent={handleReplaceContent}
              onDelete={handleDeleteAsset}
            />
          ))}
        </div>
      )}
    </div>
  );
}
