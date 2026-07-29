import fs from "fs/promises";
import path from "path";

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
      license: string | null;
    };

export function assetManifest(assetDir: string, outputDir: string) {
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
          license: licensePath ? toAssetPath(assetDir, licensePath) : null,
        });
        assetCount += 1;
      }

      return entries;
    }

    const manifest = await walk(assetDir);

    await fs.mkdir(outputDir, { recursive: true });

    await fs.writeFile(path.join(outputDir, "assets-manifest.json"), JSON.stringify(manifest, null, 2));

    console.log(`Generated asset manifest (${assetCount} assets)`);
  }

  return {
    name: "assets-manifest",

    async buildStart() {
      await generate();
    },

    async configureServer() {
      await generate();
    },
  };
}
