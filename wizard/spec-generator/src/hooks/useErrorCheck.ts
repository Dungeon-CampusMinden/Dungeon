import React from "react";

import { getAssetDisplayName, isBundledAssetPath } from "@/components/assets/assetPaths";
import type { DeerProject } from "@/data/DeerSchema";
import { ErrorChecker, type IssueReport } from "@/data/ErrorChecker";
import type { WizardDraft } from "@/data/WizardDraft";
import { useWizardStorage } from "@/data/WizardStorage";

export type AssetStorageCheckStatus = "checking" | "ready" | "error";

export interface ErrorCheckResult {
  issueReport: IssueReport;
  assetStorageStatus: AssetStorageCheckStatus;
}

interface AssetStorageCheckState {
  checkedKey: string | null;
  status: AssetStorageCheckStatus;
  storageKeys: Set<string> | null;
}

const NO_STORED_ASSETS = new Set<string>();

/** Validates project data and reports whether custom-asset storage could be checked completely. */
export function useErrorCheck(
  draftId: string,
  deerSchema: DeerProject,
  uploads: WizardDraft["uploads"],
): ErrorCheckResult {
  const storage = useWizardStorage();
  const customAssets = React.useMemo(
    () => deerSchema.assets.filter((asset) => !isBundledAssetPath(asset.path)),
    [deerSchema.assets],
  );
  const hasCustomAssets = customAssets.length > 0;
  const customAssetKey = customAssets
    .map((asset) => `${asset.id}:${asset.path}:${uploads[asset.id]?.storageKey ?? ""}`)
    .join("|");
  const [storageCheck, setStorageCheck] = React.useState<AssetStorageCheckState>({
    checkedKey: null,
    status: "checking",
    storageKeys: null,
  });

  React.useEffect(() => {
    if (!hasCustomAssets) return;

    let cancelled = false;
    storage.assets
      .listAssetIds(draftId)
      .then((ids) => {
        if (cancelled) return;
        setStorageCheck({ checkedKey: customAssetKey, status: "ready", storageKeys: new Set(ids) });
      })
      .catch(() => {
        if (!cancelled) {
          setStorageCheck({ checkedKey: customAssetKey, status: "error", storageKeys: null });
        }
      });
    return () => {
      cancelled = true;
    };
  }, [customAssetKey, draftId, hasCustomAssets, storage]);

  const matchesCurrentProject = storageCheck.checkedKey === customAssetKey;
  const assetStorageStatus: AssetStorageCheckStatus = !hasCustomAssets
    ? "ready"
    : matchesCurrentProject
      ? storageCheck.status
      : "checking";
  const checkedStorageKeys = matchesCurrentProject ? storageCheck.storageKeys : null;
  const storedAssetIds = React.useMemo(() => {
    if (!hasCustomAssets) return NO_STORED_ASSETS;
    if (!checkedStorageKeys) return null;
    return new Set(
      customAssets
        .filter((asset) => {
          const storageKey = uploads[asset.id]?.storageKey;
          return storageKey !== undefined && checkedStorageKeys.has(storageKey);
        })
        .map((asset) => asset.id),
    );
  }, [checkedStorageKeys, customAssets, hasCustomAssets, uploads]);

  const assetDisplayNames = React.useMemo(
    () =>
      new Map(
        deerSchema.assets.map((asset) => [asset.id, getAssetDisplayName(asset, uploads[asset.id])]),
      ),
    [deerSchema.assets, uploads],
  );

  const issueReport = React.useMemo(
    () => new ErrorChecker({ storedAssetIds, assetDisplayNames }).check(deerSchema),
    [assetDisplayNames, deerSchema, storedAssetIds],
  );
  return { issueReport, assetStorageStatus };
}
