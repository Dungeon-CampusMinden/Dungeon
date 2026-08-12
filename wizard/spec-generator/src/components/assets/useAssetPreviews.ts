import type { Asset } from "@/data/DeerSchema";
import React from "react";
import { AssetStorage } from "@/data/AssetStorage";
import { getBundledAssetUrl, isBundledAssetPath, type AssetPreview } from "./assetPaths";

/**
 * Resolves the preview of every given asset, either from the bundled assets served by the
 * webserver or from the IndexedDB. Bump `storageRevision` to force a reload after a write.
 */
export function useAssetPreviews(assets: Asset[], storageRevision = 0) {
  const [previews, setPreviews] = React.useState<Record<string, AssetPreview>>({});

  const previewKey = assets.map((asset) => `${asset.id}:${asset.path}`).join("|");

  React.useEffect(() => {
    let cancelled = false;
    const createdUrls: string[] = [];

    (async () => {
      const nextPreviews: Record<string, AssetPreview> = {};
      for (const asset of assets) {
        // Bundled assets are served by the webserver and never stored in the IndexedDB.
        if (isBundledAssetPath(asset.path)) {
          const isImage = asset.mediaType.startsWith("image/");
          nextPreviews[asset.id] = {
            previewUrl: isImage ? getBundledAssetUrl(asset.path) : null,
            missing: false,
          };
          continue;
        }

        const storedFile = await AssetStorage.getAssetFile(asset.id);
        if (!storedFile) {
          nextPreviews[asset.id] = { previewUrl: null, missing: true };
          continue;
        }
        const objectUrl = storedFile.blob.type.startsWith("image/")
          ? URL.createObjectURL(storedFile.blob)
          : null;
        if (objectUrl) createdUrls.push(objectUrl);
        nextPreviews[asset.id] = { previewUrl: objectUrl, missing: false };
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
  }, [previewKey, storageRevision]);

  return previews;
}
