import type { Asset } from "@/data/DeerSchema";
import React from "react";
import { useWizardStorage } from "@/data/WizardStorage";
import { useUploadReferences } from "./UploadReferencesContext";
import { getBundledAssetUrl, isBundledAssetPath, type AssetPreview } from "./assetPaths";

/**
 * Resolves the preview of every given asset, either from the bundled assets served by the
 * bundled assets or the injected asset storage. Bump `storageRevision` after a write.
 */
export function useAssetPreviews(
  assets: Asset[],
  storageRevision = 0,
) {
  const uploads = useUploadReferences();
  const storage = useWizardStorage();
  const [previews, setPreviews] = React.useState<Record<string, AssetPreview>>({});

  const previewKey = assets.map((asset) => `${asset.id}:${asset.path}`).join("|");

  React.useEffect(() => {
    let cancelled = false;
    const createdUrls: string[] = [];

    (async () => {
      const nextPreviews: Record<string, AssetPreview> = {};
      for (const asset of assets) {
        // Bundled assets are served directly and do not use the draft's asset storage.
        if (isBundledAssetPath(asset.path)) {
          const isImage = asset.mediaType.startsWith("image/");
          nextPreviews[asset.id] = {
            previewUrl: isImage ? getBundledAssetUrl(asset.path) : null,
            missing: false,
            technicalError: false,
          };
          continue;
        }

        const storageKey = uploads[asset.id]?.storageKey;
        let storedFile;
        try {
          storedFile = storageKey ? await storage.assets.getAssetFile(storageKey) : null;
        } catch {
          nextPreviews[asset.id] = {
            previewUrl: null,
            missing: false,
            technicalError: true,
          };
          continue;
        }
        if (!storedFile) {
          nextPreviews[asset.id] = { previewUrl: null, missing: true, technicalError: false };
          continue;
        }
        const objectUrl = storedFile.blob.type.startsWith("image/")
          ? URL.createObjectURL(storedFile.blob)
          : null;
        if (objectUrl) createdUrls.push(objectUrl);
        nextPreviews[asset.id] = { previewUrl: objectUrl, missing: false, technicalError: false };
      }

      if (cancelled) {
        createdUrls.forEach((url) => URL.revokeObjectURL(url));
        return;
      }
      setPreviews(nextPreviews);
    })();

    return () => {
      cancelled = true;
      createdUrls.forEach((url) => URL.revokeObjectURL(url));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [previewKey, uploads, storage, storageRevision]);

  return previews;
}
