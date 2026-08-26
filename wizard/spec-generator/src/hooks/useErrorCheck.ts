import React from "react";
import type { DeerProject } from "@/data/DeerSchema";
import type { WizardDraft } from "@/data/WizardDraft";
import { useWizardStorage } from "@/data/WizardStorage";
import { ErrorChecker, type IssueReport } from "@/data/ErrorChecker";
import { getAssetDisplayName, isBundledAssetPath } from "@/components/assets/assetPaths";

export type AssetStorageCheckStatus = "checking" | "ready" | "error";

export interface ErrorCheckResult {
  issueReport: IssueReport;
  assetStorageStatus: AssetStorageCheckStatus;
}

interface AssetStorageCheckState {
  checkedKey: string | null;
  status: AssetStorageCheckStatus;
  storedAssetIds: Set<string> | null;
}

const NO_STORED_ASSETS = new Set<string>();

/** Validates project data and reports whether custom-asset storage could be checked completely. */
export function useErrorCheck(
  draftId: string,
  deerSchema: DeerProject,
  uploads: WizardDraft["uploads"],
): ErrorCheckResult {
  const storage = useWizardStorage();
  const customAssets = deerSchema.assets.filter((asset) => !isBundledAssetPath(asset.path));
  const customAssetKey = customAssets
    .map((asset) => `${asset.id}:${asset.path}:${uploads[asset.id]?.storageKey ?? ""}`)
    .join("|");
  const [storageCheck, setStorageCheck] = React.useState<AssetStorageCheckState>({
    checkedKey: null,
    status: "checking",
    storedAssetIds: null,
  });

  React.useEffect(() => {
    if (customAssets.length === 0) {
      setStorageCheck({ checkedKey: null, status: "checking", storedAssetIds: null });
      return;
    }

    let cancelled = false;
    setStorageCheck({ checkedKey: customAssetKey, status: "checking", storedAssetIds: null });
    storage.assets.listAssetIds(draftId)
      .then((ids) => {
        if (cancelled) return;
        const storageKeys = new Set(ids);
        const availableAssetIds = customAssets
          .filter((asset) => {
            const storageKey = uploads[asset.id]?.storageKey;
            return storageKey !== undefined && storageKeys.has(storageKey);
          })
          .map((asset) => asset.id);
        setStorageCheck({
          checkedKey: customAssetKey,
          status: "ready",
          storedAssetIds: new Set(availableAssetIds),
        });
      })
      .catch(() => {
        if (!cancelled) {
          setStorageCheck({ checkedKey: customAssetKey, status: "error", storedAssetIds: null });
        }
      });
    return () => {
      cancelled = true;
    };
  }, [customAssetKey, draftId, storage]);

  const hasCustomAssets = customAssets.length > 0;
  const matchesCurrentProject = storageCheck.checkedKey === customAssetKey;
  const assetStorageStatus: AssetStorageCheckStatus = !hasCustomAssets
    ? "ready"
    : matchesCurrentProject
      ? storageCheck.status
      : "checking";
  const storedAssetIds = !hasCustomAssets
    ? NO_STORED_ASSETS
    : matchesCurrentProject && storageCheck.status === "ready"
      ? storageCheck.storedAssetIds
      : null;

  const assetDisplayNames = React.useMemo(
    () => new Map(deerSchema.assets.map((asset) => [
      asset.id,
      getAssetDisplayName(asset, uploads[asset.id]),
    ])),
    [deerSchema.assets, uploads],
  );

  const issueReport = React.useMemo(
    () => new ErrorChecker({ storedAssetIds, assetDisplayNames }).check(deerSchema),
    [assetDisplayNames, deerSchema, storedAssetIds],
  );
  return { issueReport, assetStorageStatus };
}
