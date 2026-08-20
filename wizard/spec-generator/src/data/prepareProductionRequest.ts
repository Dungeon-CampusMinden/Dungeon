import { isCustomAssetPath } from "@/components/assets/assetPaths";
import { createDeerCandidate } from "./createDeerCandidate";
import type { ProductionRequest } from "./NativeWizardHost";
import type { WizardDraft } from "./WizardDraft";
import type { WizardStoragePort } from "./WizardStorage";

function bytesBase64(bytes: Uint8Array): string {
  let binary = "";
  const chunkSize = 0x8000;
  for (let offset = 0; offset < bytes.length; offset += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(offset, offset + chunkSize));
  }
  return btoa(binary);
}

/** Builds one closed production input from the saved draft and its stored upload bytes. */
export async function prepareProductionRequest(
  storage: WizardStoragePort,
  snapshot: WizardDraft,
): Promise<{ request: ProductionRequest; snapshot: WizardDraft }> {
  const project = createDeerCandidate(snapshot);
  const customAssets: ProductionRequest["customAssets"] = [];
  const includedCustomPaths = new Set<string>();
  for (const asset of project.assets) {
    if (!isCustomAssetPath(asset.path) || includedCustomPaths.has(asset.path)) continue;
    includedCustomPaths.add(asset.path);
    const upload = snapshot.uploads[asset.id];
    if (!upload) throw new Error(`Die eigene Datei "${asset.path}" fehlt im Entwurf.`);
    const stored = await storage.assets.getAssetFile(snapshot.draftId, upload.storageKey);
    if (!stored) throw new Error(`Die eigene Datei "${upload.originalName}" ist nicht mehr gespeichert.`);
    customAssets.push({
      path: asset.path,
      bytesBase64: bytesBase64(new Uint8Array(await stored.blob.arrayBuffer())),
    });
  }
  return { request: { project, customAssets }, snapshot };
}
