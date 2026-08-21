import type { Asset, AssetMediaType } from "@/data/DeerSchema";
import type { UploadReference } from "@/data/WizardDraft";
import assetsManifest from "virtual:dungeon-assets-manifest";

export const ALLOWED_EXTENSIONS = ["png", "jpg", "jpeg"];
export const USE_NN_BELOW = 128;

const MEDIA_TYPE_BY_EXTENSION: Record<string, AssetMediaType> = {
  png: "image/png",
  jpg: "image/jpeg",
  jpeg: "image/jpeg",
};

/**
 * Prefix of asset paths whose content lives in the IndexedDB. Every asset path that does not
 * start with this prefix refers to a bundled asset served by the webserver.
 */
export const CUSTOM_PATH_PREFIX = "assets/custom";

export type AssetEntry =
  | { path: string; type: "directory"; entries: AssetEntry[] }
  | { path: string; type: "file"; source: Asset["source"] | null };

/** Result of the asset selector dialog: either an uploaded file or a bundled asset path. */
export type AssetSelection = { kind: "custom"; file: File } | { kind: "bundled"; path: string };

/** Preview state of an asset, resolved either from the IndexedDB or from the bundled assets. */
export type AssetPreview = {
  previewUrl: string | null;
  missing: boolean;
  technicalError: boolean;
};

/** Asset paths of the schema have no leading slash, manifest paths always have one. */
export const stripLeadingSlash = (assetPath: string) => assetPath.replace(/^\/+/, "");

/** Normalizes a manifest path (the paths inside the bundled assets manifest). */
export const normalizeAssetPath = (assetPath: string) => `/${stripLeadingSlash(assetPath)}`;

export const getParentPath = (assetPath: string) => {
  const segments = normalizeAssetPath(assetPath).split("/").filter(Boolean);
  return segments.length > 1 ? `/${segments.slice(0, -1).join("/")}` : "/";
};

export const getAssetName = (assetPath: string) => assetPath.split("/").filter(Boolean).at(-1) ?? "/";

export const getFileExtension = (filePath: string) => filePath.split(".").pop()?.toLowerCase() ?? "";

export const getMediaTypeForPath = (filePath: string): AssetMediaType => {
  const mediaType = MEDIA_TYPE_BY_EXTENSION[getFileExtension(filePath)];
  if (!mediaType) throw new Error("Es werden nur PNG- und JPEG-Dateien unterstützt.");
  return mediaType;
};

export function validateCustomAssetFile(file: File): AssetMediaType {
  if (file.size === 0) throw new Error("Die ausgewählte Datei ist leer.");
  if (file.size > 16 * 1024 * 1024) throw new Error("Die Datei darf höchstens 16 MiB groß sein.");
  const extension = getFileExtension(file.name);
  const mediaType = MEDIA_TYPE_BY_EXTENSION[extension];
  if (!mediaType) {
    throw new Error("Wähle eine Datei mit der Endung .png, .jpg oder .jpeg aus.");
  }
  if (file.type !== mediaType) {
    throw new Error(
      `Dateiendung und Dateityp passen nicht zusammen. Erwartet wird ${mediaType}.`,
    );
  }
  return mediaType;
}

const WINDOWS_RESERVED_STEM = /^(con|prn|aux|nul|com[1-9]|lpt[1-9])$/i;
const MAX_CUSTOM_STEM_LENGTH = 48;

export function createCustomAssetPath(originalName: string, storageKey: string): string {
  if (!/^[0-9a-f]{64}$/.test(storageKey)) {
    throw new Error("Der Dateiinhalt konnte nicht sicher adressiert werden.");
  }
  const extension = getFileExtension(originalName);
  if (!MEDIA_TYPE_BY_EXTENSION[extension]) {
    throw new Error("Wähle eine Datei mit der Endung .png, .jpg oder .jpeg aus.");
  }
  const extensionStart = originalName.lastIndexOf(".");
  let stem = originalName
    .slice(0, extensionStart)
    .normalize("NFKC")
    .replace(/[^A-Za-z0-9_-]+/g, "-")
    .replace(/-+/g, "-")
    .replace(/^[-_.]+|[-_.]+$/g, "")
    .slice(0, MAX_CUSTOM_STEM_LENGTH)
    .replace(/[-_.]+$/g, "");
  if (!stem) stem = "datei";
  if (WINDOWS_RESERVED_STEM.test(stem)) stem = `datei-${stem}`;
  return `${CUSTOM_PATH_PREFIX}/${stem}-${storageKey.slice(0, 12)}.${extension}`;
}

export function getAssetDisplayName(asset: Asset, upload?: UploadReference): string {
  if (upload?.originalName) return upload.originalName;
  return isCustomAssetPath(asset.path) ? "Eigene Datei" : getAssetName(asset.path);
}

export const isCustomAssetPath = (assetPath: string) =>
  stripLeadingSlash(assetPath).startsWith(`${CUSTOM_PATH_PREFIX}/`);

/** Everything that is not a custom asset is a bundled asset shipped with the application. */
export const isBundledAssetPath = (assetPath: string) => !isCustomAssetPath(assetPath);

/** Converts a manifest path (e.g. /character/knight.png) into an asset path. */
export const toBundledAssetPath = (manifestPath: string) => stripLeadingSlash(manifestPath);

/** Converts a bundled asset path back into the manifest path it originated from. */
export const toManifestPath = (assetPath: string) => normalizeAssetPath(assetPath);

/** URL under which the webserver serves the content of a bundled asset. */
export const getBundledAssetUrl = (assetPath: string) => `/bundled-assets/${stripLeadingSlash(assetPath)}`;

let bundledAssetPathCache: Set<string> | null = null;
let bundledAssetSourceCache: Map<string, Asset["source"]> | null = null;

const collectBundledAssets = (
  entries: AssetEntry[],
  paths: Set<string>,
  sources: Map<string, Asset["source"]>,
) => {
  for (const entry of entries) {
    if (entry.type === "directory") {
      collectBundledAssets(entry.entries, paths, sources);
      continue;
    }

    const assetPath = toBundledAssetPath(entry.path);
    paths.add(assetPath);
    if (entry.source) sources.set(assetPath, entry.source);
  }
};

const ensureBundledAssetCaches = () => {
  if (bundledAssetPathCache && bundledAssetSourceCache) return;

  bundledAssetPathCache = new Set<string>();
  bundledAssetSourceCache = new Map<string, Asset["source"]>();
  collectBundledAssets(
    assetsManifest as AssetEntry[],
    bundledAssetPathCache,
    bundledAssetSourceCache,
  );
};

/** All asset paths that are shipped with the application, as they appear in the deer schema. */
export const getBundledAssetPaths = (): Set<string> => {
  ensureBundledAssetCaches();
  return bundledAssetPathCache!;
};

/** License metadata shipped with a bundled asset, if its license file declares it. */
export const getBundledAssetSource = (assetPath: string): Asset["source"] | null => {
  ensureBundledAssetCaches();
  const source = bundledAssetSourceCache!.get(stripLeadingSlash(assetPath));
  return source ? { ...source } : null;
};
