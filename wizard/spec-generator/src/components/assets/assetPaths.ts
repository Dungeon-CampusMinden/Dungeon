import type { AssetMediaType } from "@/data/DeerSchema";

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

/** Prefix of asset paths whose content lives in the IndexedDB. */
export const CUSTOM_PATH_PREFIX = "/assets/custom";
/** Prefix of asset paths whose content is served by the webserver under /bundled-assets. */
export const BUNDLED_PATH_PREFIX = "/assets/bundled";

export type AssetEntry =
  | { path: string; type: "directory"; entries: AssetEntry[] }
  | { path: string; type: "file"; license: string | null };

/** Result of the asset selector dialog: either an uploaded file or a bundled asset path. */
export type AssetSelection = { kind: "custom"; file: File } | { kind: "bundled"; path: string };

/** Preview state of an asset, resolved either from the IndexedDB or from the bundled assets. */
export type AssetPreview = { previewUrl: string | null; missing: boolean };

export const normalizeAssetPath = (assetPath: string) => `/${assetPath.replace(/^\/+/, "")}`;

export const getParentPath = (assetPath: string) => {
  const segments = normalizeAssetPath(assetPath).split("/").filter(Boolean);
  return segments.length > 1 ? `/${segments.slice(0, -1).join("/")}` : "/";
};

export const getAssetName = (assetPath: string) => assetPath.split("/").filter(Boolean).at(-1) ?? "/";

export const getFileExtension = (filePath: string) => filePath.split(".").pop()?.toLowerCase() ?? "";

export const getMediaTypeForPath = (filePath: string): AssetMediaType =>
  MEDIA_TYPE_BY_EXTENSION[getFileExtension(filePath)] ?? "text/plain";

export const isBundledAssetPath = (assetPath: string) => assetPath.startsWith(`${BUNDLED_PATH_PREFIX}/`);

/** Converts a manifest path (e.g. /character/knight.png) into an asset path. */
export const toBundledAssetPath = (manifestPath: string) =>
  `${BUNDLED_PATH_PREFIX}${normalizeAssetPath(manifestPath)}`;

/** Converts a bundled asset path back into the manifest path it originated from. */
export const toManifestPath = (assetPath: string) => assetPath.slice(BUNDLED_PATH_PREFIX.length) || "/";

/** URL under which the webserver serves the content of a bundled asset. */
export const getBundledAssetUrl = (assetPath: string) => `/bundled-assets${toManifestPath(assetPath)}`;
