import JSZip from "jszip";
import type { DeerSchema } from "./DeerSchema";
import { AssetStorage } from "./AssetStorage";
import { getAssetName, isCustomAssetPath, stripLeadingSlash } from "@/components/assets/assetPaths";

export async function createSchemaZip(deerSchema: DeerSchema): Promise<Blob> {
  const zip = new JSZip();
  zip.file("deer.json", JSON.stringify(deerSchema, null, 2));
  zip.folder("assets/custom");

  const missing: string[] = [];
  for (const asset of deerSchema.assets) {
    if (!isCustomAssetPath(asset.path)) continue;
    const stored = await AssetStorage.getAssetFile(asset.id);
    if (!stored) {
      missing.push(getAssetName(asset.path) || asset.id);
      continue;
    }
    zip.file(stripLeadingSlash(asset.path), stored.blob);
  }

  if (missing.length > 0) {
    throw new Error(`Der Inhalt folgender Dateien fehlt: ${missing.join(", ")}`);
  }

  return zip.generateAsync({ type: "blob" });
}

export function downloadBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}
