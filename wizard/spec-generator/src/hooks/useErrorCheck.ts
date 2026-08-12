import React from "react";
import type { DeerSchema } from "@/data/DeerSchema";
import { AssetStorage } from "@/data/AssetStorage";
import { ErrorChecker, type IssueReport } from "@/data/ErrorChecker";
import { isBundledAssetPath } from "@/components/assets/assetPaths";

/**
 * Validates the given schema and keeps the result up to date. The ids of the uploaded assets are
 * read from the IndexedDB, because whether an asset file exists cannot be derived from the schema.
 */
export function useErrorCheck(deerSchema: DeerSchema): IssueReport {
  const [storedAssetIds, setStoredAssetIds] = React.useState<Set<string>>(new Set());

  // Reload whenever the set of uploaded assets changes.
  const customAssetKey = deerSchema.assets
    .filter((asset) => !isBundledAssetPath(asset.path))
    .map((asset) => `${asset.id}:${asset.path}`)
    .join("|");

  React.useEffect(() => {
    let cancelled = false;
    AssetStorage.listAssetIds()
      .then((ids) => {
        if (!cancelled) setStoredAssetIds(new Set(ids));
      })
      .catch(() => {
        if (!cancelled) setStoredAssetIds(new Set());
      });
    return () => {
      cancelled = true;
    };
  }, [customAssetKey]);

  return React.useMemo(
    () => new ErrorChecker({ storedAssetIds }).check(deerSchema),
    [deerSchema, storedAssetIds],
  );
}
