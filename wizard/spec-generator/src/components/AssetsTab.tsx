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
  getBundledAssetSource,
  getMediaTypeForPath,
  validateCustomAssetFile,
  type AssetSelection,
} from "./assets/assetPaths";
import type { WizardWork } from "@/data/WizardWork";
import type { TabIssues } from "@/data/ErrorChecker";
import { fieldIssues, ValidationFeedback } from "./ValidationFeedback";

export function AssetsTab({ draft, updateDraft, flush, work, beginWork, finishWork, issues }: {
  draft: WizardDraft;
  updateDraft: UpdateDraft;
  flush: () => Promise<WizardDraft>;
  work: WizardWork;
  beginWork: (work: Extract<WizardWork, "uploading">) => boolean;
  finishWork: (work: Extract<WizardWork, "uploading">) => void;
  issues: TabIssues;
}) {
  const storage = useWizardStorage();
  const project = draft.project;
  const assetList = project.assets;
  const [addOpen, setAddOpen] = React.useState(false);
  const [storageRevision, setStorageRevision] = React.useState(0);
  const previews = useAssetPreviews(assetList, storageRevision);
  const editingDisabled = work !== null;

  const rejectBlockedUpload = () => {
    toast.error("Dateien können gerade nicht geändert werden.", {
      description: "Warte, bis die laufende Prüfung, Spielerstellung oder Dateiübertragung abgeschlossen ist.",
    });
    return new Error("Eine andere Wizard-Arbeit läuft bereits.");
  };

  const applySelection = async (
    selection: AssetSelection,
  ): Promise<{
    path: string;
    mediaType: Asset["mediaType"];
    source: Asset["source"];
    upload?: UploadReference;
  }> => {
    if (selection.kind === "custom") {
      const mediaType = validateCustomAssetFile(selection.file);
      await flush();
      const storageKey = await storage.assets.putAssetFile(draft.draftId, selection.file);
      return {
        path: createCustomAssetPath(selection.file.name, storageKey),
        mediaType,
        source: { license: "" },
        upload: { storageKey, originalName: selection.file.name },
      };
    }
    return {
      path: selection.path,
      mediaType: getMediaTypeForPath(selection.path),
      source: getBundledAssetSource(selection.path) ?? { license: "" },
    };
  };

  const handleAddAsset = async (selection: AssetSelection) => {
    if (!beginWork("uploading")) throw rejectBlockedUpload();
    const id = Util.generateUniqueId("a");
    try {
      const { path, mediaType, source, upload } = await applySelection(selection);
      updateDraft((current) => {
        current.project.assets.push({ id, path, mediaType, source });
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
    if (!beginWork("uploading")) throw rejectBlockedUpload();
    const assetId = asset.id;
    try {
      const { path, mediaType, source, upload } = await applySelection(selection);
      updateDraft((current) => {
        const currentAsset = current.project.assets.find((entry) => entry.id === assetId);
        if (!currentAsset) throw new Error("Die Datei wurde zwischenzeitlich gelöscht.");
        currentAsset.path = path;
        currentAsset.mediaType = mediaType;
        currentAsset.source = source;
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
      delete current.uploads[asset.id];
    });
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <h1 className="wizard-page-title">Bilder und Dateien</h1>
        <p className="text-sm text-muted-foreground">
          Eigene oder mitgelieferte Dateien für den Raum. Erlaubte Formate: {ALLOWED_EXTENSIONS.join(", ")}.
        </p>
      </div>
      {work === "validating" && (
        <p className="mt-2 text-sm text-muted-foreground">
          Nach Abschluss der Prüfung kannst du die Dateien wieder bearbeiten.
        </p>
      )}
      {work === "packaging" && (
        <p className="mt-2 text-sm text-muted-foreground">
          Nach Abschluss der Spielerstellung kannst du die Dateien wieder bearbeiten.
        </p>
      )}
      <Button disabled={editingDisabled} onClick={() => setAddOpen(true)} className="max-w-40">
        <PlusIcon />
        Hinzufügen
      </Button>
      <AssetCreateDialog
        open={addOpen}
        setOpen={setAddOpen}
        onSelect={handleAddAsset}
        title="Datei hinzufügen"
      />
      <ValidationFeedback issues={fieldIssues(issues, "assets")} />
      {assetList.length === 0 ? (
        <p className="py-8 text-center text-muted-foreground">Noch keine Dateien hinzugefügt.</p>
      ) : (
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-5">
          {assetList.map((asset) => (
            <AssetCard
              key={asset.id}
              asset={asset}
              preview={previews[asset.id]}
              disabled={editingDisabled}
              onUpdate={handleUpdateAsset}
              onReplaceContent={handleReplaceContent}
              onDelete={handleDeleteAsset}
              issues={fieldIssues(issues, `asset:${asset.id}`)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
