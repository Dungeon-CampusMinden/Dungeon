import type { AssetMediaType } from "@/data/DeerSchema";
import assetsManifest from "@/data/assets-manifest.json";

export const ALLOWED_EXTENSIONS = ["png", "txt", "wav", "ttf"];
export const USE_NN_BELOW = 128;

const MEDIA_TYPE_BY_EXTENSION: Record<string, AssetMediaType> = {
  png: "image/png",
  jpg: "image/jpeg",
  jpeg: "image/jpeg",
  txt: "text/plain",
  wav: "audio/wav",
  ttf: "font/ttf",
};

/**
 * Prefix of asset paths whose content lives in the IndexedDB. Every asset path that does not
 * start with this prefix refers to a bundled asset served by the webserver.
 */
export const CUSTOM_PATH_PREFIX = "assets/custom";

export type AssetEntry =
  | { path: string; type: "directory"; entries: AssetEntry[] }
  | { path: string; type: "file"; license: string | null };

/** Result of the asset selector dialog: either an uploaded file or a bundled asset path. */
export type AssetSelection = { kind: "custom"; file: File } | { kind: "bundled"; path: string };

/** Preview state of an asset, resolved either from the IndexedDB or from the bundled assets. */
export type AssetPreview = { previewUrl: string | null; missing: boolean };

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

export const getMediaTypeForPath = (filePath: string): AssetMediaType =>
  MEDIA_TYPE_BY_EXTENSION[getFileExtension(filePath)] ?? "text/plain";

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

const collectFilePaths = (entries: AssetEntry[], target: Set<string>) => {
  for (const entry of entries) {
    if (entry.type === "directory") collectFilePaths(entry.entries, target);
    else target.add(toBundledAssetPath(entry.path));
  }
};

/** All asset paths that are shipped with the application, as they appear in the deer schema. */
export const getBundledAssetPaths = (): Set<string> => {
  if (!bundledAssetPathCache) {
    bundledAssetPathCache = new Set<string>();
    collectFilePaths(assetsManifest as AssetEntry[], bundledAssetPathCache);
  }
  return bundledAssetPathCache;
};
