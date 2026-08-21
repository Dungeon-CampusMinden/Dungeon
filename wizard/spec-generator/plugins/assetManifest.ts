import fs from "fs/promises";
import path from "path";

const VIRTUAL_MODULE_ID = "virtual:dungeon-assets-manifest";
const RESOLVED_VIRTUAL_MODULE_ID = `\0${VIRTUAL_MODULE_ID}`;

const toAssetPath = (assetDir: string, entryPath: string) =>
  `/${path.relative(assetDir, entryPath).replaceAll("\\", "/")}`;

type ManifestEntry =
  | {
      path: string;
      type: "directory";
      entries: ManifestEntry[];
    }
  | {
      path: string;
      type: "file";
      source: {
        license: string;
        attribution?: string;
      } | null;
    };

async function readAssetSource(licensePath: string | undefined) {
  if (!licensePath) return null;

  const metadata = new Map<string, string>();
  const licenseContent = await fs.readFile(licensePath, "utf8");

  for (const line of licenseContent.split(/\r?\n/)) {
    const match = /^\s*-\s*([^:]+):\s*(.*?)\s*$/.exec(line);
    if (match?.[1] && match[2]) metadata.set(match[1].trim().toLowerCase(), match[2]);
  }

  const license = metadata.get("license") ?? metadata.get("lizenz");
  if (!license) return null;

  const attribution =
    metadata.get("author") ?? metadata.get("autor") ?? metadata.get("urheber");

  return {
    license,
    ...(attribution ? { attribution } : {}),
  };
}

export function assetManifest(assetDir: string) {
  const excludedDirectories = new Set([
    "dungeon",
    "language_default",
    "levels",
    "logo",
    "shaders",
    "skin",
    "spritesheets",
    "animation",
  ]);
  const includedExtensions = new Set([".png", ".wav", ".ttf"]);

  async function generate() {
    let assetCount = 0;

    async function walk(dir: string): Promise<ManifestEntry[]> {
      const directoryEntries = await fs.readdir(dir, { withFileTypes: true });
      const licenses = directoryEntries
        .filter((entry) => entry.isFile() && entry.name.endsWith(".license.md"))
        .map((entry) => path.join(dir, entry.name));
      const entries: ManifestEntry[] = [];

      for (const entry of directoryEntries.sort((left, right) => left.name.localeCompare(right.name))) {
        const abs = path.join(dir, entry.name);

        if (entry.isDirectory()) {
          if (excludedDirectories.has(entry.name)) {
            continue;
          }

          const childEntries = await walk(abs);

          if (childEntries.length > 0) {
            entries.push({
              path: toAssetPath(assetDir, abs),
              type: "directory",
              entries: childEntries,
            });
          }

          continue;
        }

        if (entry.name.endsWith(".license.md")) {
          continue;
        }

        if (includedExtensions.has(path.extname(entry.name).toLowerCase()) === false) {
          continue;
        }

        const assetName = path.basename(entry.name, path.extname(entry.name));
        const exactLicensePath = path.join(dir, `${assetName}.license.md`);
        const licensePath =
          licenses.find((candidate) => candidate === exactLicensePath) ??
          licenses
            .filter((candidate) => {
              const licenseName = path.basename(candidate, ".license.md");
              return licenseName.length > 0 && assetName.includes(licenseName);
            })
            .sort((left, right) => {
              const lengthDifference =
                path.basename(right, ".license.md").length - path.basename(left, ".license.md").length;

              return lengthDifference || left.localeCompare(right);
            })[0];

        entries.push({
          path: toAssetPath(assetDir, abs),
          type: "file",
          source: await readAssetSource(licensePath),
        });
        assetCount += 1;
      }

      return entries;
    }

    const manifest = await walk(assetDir);
    console.log(`Generated asset manifest (${assetCount} assets)`);
    return manifest;
  }

  let manifest: ManifestEntry[] | null = null;

  return {
    name: "assets-manifest",

    async buildStart() {
      manifest = await generate();
    },

    resolveId(id: string) {
      if (id === VIRTUAL_MODULE_ID) return RESOLVED_VIRTUAL_MODULE_ID;
    },

    async load(id: string) {
      if (id !== RESOLVED_VIRTUAL_MODULE_ID) return;
      manifest ??= await generate();
      return `export default ${JSON.stringify(manifest)};`;
    },
  };
}
