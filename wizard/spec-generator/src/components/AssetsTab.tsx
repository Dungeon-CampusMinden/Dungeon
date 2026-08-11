import type { Asset, DeerSchema } from "@/data/DeerSchema";
import React from "react";
import { Button } from "./ui/button";
import { PlusIcon } from "lucide-react";
import { Util } from "@/data/Util";
import { AssetStorage } from "@/data/AssetStorage";
import { AssetCard } from "./assets/AssetCard";
import { AssetCreateDialog } from "./assets/AssetCreateDialog";
import { useAssetPreviews } from "./assets/useAssetPreviews";
import {
  ALLOWED_EXTENSIONS,
  CUSTOM_PATH_PREFIX,
  getMediaTypeForPath,
  isBundledAssetPath,
  type AssetSelection,
} from "./assets/assetPaths";

export function AssetsTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  const assetList = deerSchema.assets;
  const [addOpen, setAddOpen] = React.useState(false);
  const [storageRevision, setStorageRevision] = React.useState(0);
  const previews = useAssetPreviews(assetList, storageRevision);

  /** Writes the selected content to the given asset id and returns the resulting path/mediaType. */
  const applySelection = async (id: string, selection: AssetSelection) => {
    if (selection.kind === "custom") {
      await AssetStorage.putAssetFile(id, selection.file);
      const extensionIndex = selection.file.name.lastIndexOf(".");
      const fileNameWithId =
        extensionIndex > 0
          ? `${selection.file.name.slice(0, extensionIndex)}-${id}${selection.file.name.slice(extensionIndex)}`
          : `${selection.file.name}-${id}`;
      return {
        path: `${CUSTOM_PATH_PREFIX}/${fileNameWithId}`,
        mediaType: getMediaTypeForPath(selection.file.name),
      };
    }

    // Bundled assets have no IndexedDB entry, so a previously stored file is removed.
    await AssetStorage.deleteAssetFile(id);
    return {
      path: selection.path,
      mediaType: getMediaTypeForPath(selection.path),
      sourceType: "bundled_asset" as const,
    };
  };

  const handleAddAsset = async (selection: AssetSelection) => {
    const id = Util.generateUniqueId("a");
    const { path, mediaType } = await applySelection(id, selection);

    const newAsset: Asset = {
      id,
      path,
      mediaType,
      source: {
        license: "",
        attribution: "",
      },
    };
    assetList.push(newAsset);
    updateDeerSchema(deerSchema);
    setStorageRevision((revision) => revision + 1);
  };

  const handleReplaceContent = async (asset: Asset, selection: AssetSelection) => {
    // The id stays the same so that all references to this asset keep working.
    const { path, mediaType } = await applySelection(asset.id, selection);
    asset.path = path;
    asset.mediaType = mediaType;
    updateDeerSchema(deerSchema);
    setStorageRevision((revision) => revision + 1);
  };

  const handleUpdateAsset = (updatedAsset: Asset) => {
    const index = assetList.findIndex((asset) => asset.id === updatedAsset.id);
    if (index === -1) return;
    assetList[index] = updatedAsset;
    updateDeerSchema(deerSchema);
  };

  const handleDeleteAsset = async (asset: Asset) => {
    const index = assetList.findIndex((entry) => entry.id === asset.id);
    if (index === -1) return;
    assetList.splice(index, 1);
    updateDeerSchema(deerSchema);
    if (!isBundledAssetPath(asset.path)) await AssetStorage.deleteAssetFile(asset.id);
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
        onSelect={(selection) => void handleAddAsset(selection)}
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
