import JSZip from "jszip";
import type { DeerSchema } from "./DeerSchema";
import { AssetStorage } from "./AssetStorage";
import { getAssetName, isCustomAssetPath, stripLeadingSlash } from "@/components/assets/assetPaths";

export async function createSchemaZip(deerSchema: DeerSchema): Promise<Blob> {
  assertSupportedDeerDocument(deerSchema);

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

/** Checks only the minimum browser-to-backend handoff contract. */
export function assertSupportedDeerDocument(value: unknown): asserts value is DeerSchema {
  if (typeof value !== "object" || value === null || Array.isArray(value)) {
    throw new Error("Die Datei enthält kein gültiges Abenteuer.");
  }

  const document = value as Record<string, unknown>;
  if (document.formatVersion !== "0.4") {
    throw new Error("Die Datei wurde mit einer nicht unterstützten Version erstellt.");
  }
  if (!Number.isSafeInteger(document.seed) || (document.seed as number) < 0) {
    throw new Error("Die Datei enthält einen ungültigen Zufallswert.");
  }
}

export function downloadBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}
