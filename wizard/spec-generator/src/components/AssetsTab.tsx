import type { Asset, AssetMediaType, DeerSchema } from "@/data/DeerSchema";
import { Input } from "./ui/input";
import React from "react";
import assets from "../../public/assets-manifest.json";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "./ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { Button } from "./ui/button";
import {
  AlertTriangle,
  ArrowLeft,
  File,
  FileAudio,
  FileText,
  FileType,
  Folder,
  PackageIcon,
  PencilIcon,
  PlusIcon,
  Trash2Icon,
  UploadIcon,
} from "lucide-react";
import { Field, FieldLabel } from "./ui/field";
import { Separator } from "./ui/separator";
import { Util } from "@/data/Util";
import { AssetStorage } from "@/data/AssetStorage";

const ALLOWED_EXTENSIONS = ["png", "txt", "wav", "ttf"];

const MEDIA_TYPE_BY_EXTENSION: Record<string, AssetMediaType> = {
  png: "image/png",
  jpg: "image/jpeg",
  jpeg: "image/jpeg",
  txt: "text/plain",
  wav: "audio/wav",
  ttf: "font/ttf",
};

/** Prefix of asset paths whose content lives in the IndexedDB. */
const CUSTOM_PATH_PREFIX = "/assets/custom";
/** Prefix of asset paths whose content is served by the webserver under /bundled-assets. */
const BUNDLED_PATH_PREFIX = "/assets/bundled";

type AssetEntry =
  | { path: string; type: "directory"; entries: AssetEntry[] }
  | { path: string; type: "file"; license: string | null };

/** Result of the asset selector dialog: either an uploaded file or a bundled asset path. */
export type AssetSelection = { kind: "custom"; file: File } | { kind: "bundled"; path: string };

const normalizeAssetPath = (assetPath: string) => `/${assetPath.replace(/^\/+/, "")}`;

const getParentPath = (assetPath: string) => {
  const segments = normalizeAssetPath(assetPath).split("/").filter(Boolean);
  return segments.length > 1 ? `/${segments.slice(0, -1).join("/")}` : "/";
};

const getAssetName = (assetPath: string) => assetPath.split("/").filter(Boolean).at(-1) ?? "/";

const getFileExtension = (filePath: string) => filePath.split(".").pop()?.toLowerCase() ?? "";

const getMediaTypeForPath = (filePath: string): AssetMediaType =>
  MEDIA_TYPE_BY_EXTENSION[getFileExtension(filePath)] ?? "text/plain";

const isBundledAssetPath = (assetPath: string) => assetPath.startsWith(`${BUNDLED_PATH_PREFIX}/`);

/** Converts a manifest path (e.g. /character/knight.png) into an asset path. */
const toBundledAssetPath = (manifestPath: string) =>
  `${BUNDLED_PATH_PREFIX}${normalizeAssetPath(manifestPath)}`;

/** Converts a bundled asset path back into the manifest path it originated from. */
const toManifestPath = (assetPath: string) => assetPath.slice(BUNDLED_PATH_PREFIX.length) || "/";

/** URL under which the webserver serves the content of a bundled asset. */
const getBundledAssetUrl = (assetPath: string) => `/bundled-assets${toManifestPath(assetPath)}`;

/** Preview state of an asset, resolved either from the IndexedDB or from the bundled assets. */
type AssetPreview = { previewUrl: string | null; missing: boolean };

export function AssetsTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  const assetList = deerSchema.assets;
  const [addOpen, setAddOpen] = React.useState(false);
  const [previews, setPreviews] = React.useState<Record<string, AssetPreview>>({});
  // Bumped whenever a file was written to the IndexedDB, to force a reload of the previews.
  const [storageRevision, setStorageRevision] = React.useState(0);

  const previewKey = assetList.map((asset) => `${asset.id}:${asset.path}`).join("|");

  React.useEffect(() => {
    let cancelled = false;
    const createdUrls: string[] = [];

    (async () => {
      const nextPreviews: Record<string, AssetPreview> = {};
      for (const asset of assetList) {
        // Bundled assets are served by the webserver and never stored in the IndexedDB.
        if (isBundledAssetPath(asset.path)) {
          const isImage = asset.mediaType.startsWith("image/");
          nextPreviews[asset.id] = {
            previewUrl: isImage ? getBundledAssetUrl(asset.path) : null,
            missing: false,
          };
          continue;
        }

        const storedFile = await AssetStorage.getAssetFile(asset.id);
        if (!storedFile) {
          nextPreviews[asset.id] = { previewUrl: null, missing: true };
          continue;
        }
        const objectUrl = storedFile.blob.type.startsWith("image/")
          ? URL.createObjectURL(storedFile.blob)
          : null;
        if (objectUrl) createdUrls.push(objectUrl);
        nextPreviews[asset.id] = { previewUrl: objectUrl, missing: false };
      }

      if (cancelled) {
        createdUrls.forEach((url) => URL.revokeObjectURL(url));
        return;
      }
      setPreviews(nextPreviews);
    })();

    return () => {
      cancelled = true;
      createdUrls.forEach((url) => URL.revokeObjectURL(url));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [previewKey, storageRevision]);

  /** Writes the selected content to the given asset id and returns the resulting path/mediaType. */
  const applySelection = async (id: string, selection: AssetSelection) => {
    if (selection.kind === "custom") {
      await AssetStorage.putAssetFile(id, selection.file);
      return {
        path: `${CUSTOM_PATH_PREFIX}/${id}-${selection.file.name}`,
        mediaType: getMediaTypeForPath(selection.file.name),
        sourceType: "educator_upload" as const,
      };
    }

    // Bundled assets have no IndexedDB entry, so a previously stored file is removed.
    await AssetStorage.deleteAssetFile(id);
    return {
      path: selection.path,
      mediaType: getMediaTypeForPath(selection.path),
      sourceType: "bundled_asset" as const,
    };
  };

  const handleAddAsset = async (selection: AssetSelection) => {
    const id = Util.generateUniqueId("asset");
    const { path, mediaType, sourceType } = await applySelection(id, selection);

    const newAsset: Asset = {
      id,
      path,
      mediaType,
      purpose: "decorative",
      source: {
        type: sourceType,
        license: "",
        attribution: "",
      },
      accessibility: {
        decorative: true,
      },
    };
    assetList.push(newAsset);
    updateDeerSchema(deerSchema);
    setStorageRevision((revision) => revision + 1);
  };

  const handleReplaceContent = async (asset: Asset, selection: AssetSelection) => {
    // The id stays the same so that all references to this asset keep working.
    const { path, mediaType, sourceType } = await applySelection(asset.id, selection);
    asset.path = path;
    asset.mediaType = mediaType;
    asset.source.type = sourceType;
    updateDeerSchema(deerSchema);
    setStorageRevision((revision) => revision + 1);
  };

  const handleUpdateAsset = (updatedAsset: Asset) => {
    const index = assetList.findIndex((asset) => asset.id === updatedAsset.id);
    if (index === -1) return;
    assetList[index] = updatedAsset;
    updateDeerSchema(deerSchema);
  };

  const handleDeleteAsset = async (asset: Asset) => {
    const index = assetList.findIndex((entry) => entry.id === asset.id);
    if (index === -1) return;
    assetList.splice(index, 1);
    updateDeerSchema(deerSchema);
    if (!isBundledAssetPath(asset.path)) await AssetStorage.deleteAssetFile(asset.id);
  };

  return (
    <div className="flex flex-col gap-0">
      <h1>Bilder und Dateien</h1>
      <p className="text-sm text-muted-foreground">
        Eigene oder mitgelieferte Dateien für den Raum. Erlaubte Formate: {ALLOWED_EXTENSIONS.join(", ")}.
      </p>

      <Button onClick={() => setAddOpen(true)} className="my-2 max-w-40">
        <PlusIcon />
        Hinzufügen
      </Button>

      <AssetSelectorDialog
        open={addOpen}
        setOpen={setAddOpen}
        onSelect={(selection) => void handleAddAsset(selection)}
        title="Datei hinzufügen"
      />

      {assetList.length === 0 ? (
        <p className="py-8 text-center text-muted-foreground">Noch keine Dateien hinzugefügt.</p>
      ) : (
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-5">
          {assetList.map((asset) => (
            <AssetCard
              key={asset.id}
              asset={asset}
              preview={previews[asset.id]}
              onUpdate={handleUpdateAsset}
              onReplaceContent={handleReplaceContent}
              onDelete={handleDeleteAsset}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function AssetCard({
  asset,
  preview,
  onUpdate,
  onReplaceContent,
  onDelete,
}: {
  asset: Asset;
  preview: AssetPreview | undefined;
  onUpdate: (updatedAsset: Asset) => void;
  onReplaceContent: (asset: Asset, selection: AssetSelection) => Promise<void>;
  onDelete: (asset: Asset) => void;
}) {
  const [editOpen, setEditOpen] = React.useState(false);
  const missing = preview?.missing ?? false;
  const fileName = getAssetName(asset.path);

  return (
    <div className="flex min-w-0 flex-col gap-1">
      <div
        title={missing ? "Datei fehlt" : fileName}
        className={`flex aspect-square items-center justify-center rounded-lg border p-2 ${
          missing ? "border-destructive bg-destructive/5" : "border-border bg-muted/30"
        }`}
      >
        <AssetPreviewContent asset={asset} preview={preview} />
      </div>
      <div className="flex min-w-0 items-center gap-1">
        <span className="min-w-0 flex-1 truncate text-sm" title={fileName}>
          {fileName}
        </span>
        <DropdownMenu>
          <DropdownMenuTrigger render={<Button variant="outline" size="icon-sm" />}>
            <PencilIcon />
            <span className="sr-only">Datei bearbeiten</span>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => setEditOpen(true)}>
              <PencilIcon />
              Bearbeiten
            </DropdownMenuItem>
            <DropdownMenuItem variant="destructive" onClick={() => onDelete(asset)}>
              <Trash2Icon />
              Löschen
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <AssetEditDialog
        asset={asset}
        missing={missing}
        open={editOpen}
        setOpen={setEditOpen}
        onUpdate={onUpdate}
        onReplaceContent={onReplaceContent}
      />
    </div>
  );
}

function AssetPreviewContent({ asset, preview }: { asset: Asset; preview: AssetPreview | undefined }) {
  if (preview === undefined) {
    return <span className="text-xs text-muted-foreground">Lädt…</span>;
  }

  if (preview.missing) {
    return (
      <div className="flex flex-col items-center gap-1 text-destructive">
        <AlertTriangle className="size-8" />
        <span className="text-center text-xs">Datei fehlt</span>
      </div>
    );
  }

  if (preview.previewUrl) {
    return (
      <img
        src={preview.previewUrl}
        alt=""
        className="max-h-full max-w-full object-contain [image-rendering:pixelated]"
      />
    );
  }

  return <FileTypeIcon filePath={asset.path} showExtension />;
}

function FileTypeIcon({ filePath, showExtension = false }: { filePath: string; showExtension?: boolean }) {
  const extension = getFileExtension(filePath);
  const Icon =
    extension === "txt" ? FileText : extension === "wav" ? FileAudio : extension === "ttf" ? FileType : File;

  return (
    <div className="flex flex-col items-center gap-1 text-muted-foreground">
      <Icon className="size-10" />
      {showExtension && <span className="text-xs uppercase">{extension}</span>}
    </div>
  );
}

function AssetEditDialog({
  asset,
  missing,
  open,
  setOpen,
  onUpdate,
  onReplaceContent,
}: {
  asset: Asset;
  missing: boolean;
  open: boolean;
  setOpen: (open: boolean) => void;
  onUpdate: (updatedAsset: Asset) => void;
  onReplaceContent: (asset: Asset, selection: AssetSelection) => Promise<void>;
}) {
  const [selectorOpen, setSelectorOpen] = React.useState(false);
  const [license, setLicense] = React.useState(asset.source.license);
  const [attribution, setAttribution] = React.useState(asset.source.attribution ?? "");

  const handleOpenChange = (nextOpen: boolean) => {
    if (nextOpen) {
      setLicense(asset.source.license);
      setAttribution(asset.source.attribution ?? "");
    }
    setOpen(nextOpen);
  };

  const handleSave = () => {
    onUpdate({
      ...asset,
      source: { ...asset.source, license, attribution },
    });
    setOpen(false);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Datei bearbeiten</DialogTitle>
          <DialogDescription>Lizenzangaben anpassen oder die Datei ersetzen.</DialogDescription>
        </DialogHeader>

        <div className="flex flex-col gap-4">
          <Field>
            <FieldLabel>Lizenz</FieldLabel>
            <Input value={license} onChange={(e) => setLicense(e.target.value)} />
          </Field>
          <Field>
            <FieldLabel>Urheber</FieldLabel>
            <Input value={attribution} onChange={(e) => setAttribution(e.target.value)} />
          </Field>

          <Separator />

          <Field>
            <FieldLabel>Dateipfad</FieldLabel>
            <div className="grid grid-cols-[1fr_auto] items-center gap-2">
              <Input value={asset.path} readOnly aria-invalid={missing} />
              <Button variant="outline" onClick={() => setSelectorOpen(true)}>
                <UploadIcon />
                Ersetzen
              </Button>
            </div>
            {missing && (
              <span className="text-sm text-destructive">
                Die hinterlegte Datei ist nicht verfügbar. Wähle eine neue Datei aus, um sie zu ersetzen.
              </span>
            )}
          </Field>

          <AssetSelectorDialog
            open={selectorOpen}
            setOpen={setSelectorOpen}
            currentPath={asset.path}
            onSelect={(selection) => void onReplaceContent(asset, selection)}
            title="Datei ersetzen"
          />
        </div>

        <DialogFooter>
          <DialogClose render={<Button variant="outline" />}>Abbrechen</DialogClose>
          <Button onClick={handleSave}>Speichern</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/**
 * Dialog to pick the content of an asset: either a file uploaded by the user (custom asset)
 * or a file from the bundled assets shipped with the application.
 */
export function AssetSelectorDialog({
  open,
  setOpen,
  onSelect,
  currentPath = null,
  extensions = ALLOWED_EXTENSIONS,
  title = "Datei auswählen",
}: {
  open: boolean;
  setOpen: (open: boolean) => void;
  onSelect: (selection: AssetSelection) => void;
  currentPath?: string | null;
  extensions?: string[] | null;
  title?: string;
}) {
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const [mode, setMode] = React.useState<"custom" | "bundled">("custom");
  const [uploadedFile, setUploadedFile] = React.useState<File | null>(null);
  const [selectedFolder, setSelectedFolder] = React.useState("/");
  const [selectedManifestPath, setSelectedManifestPath] = React.useState<string | null>(null);

  // "assets" is a json file with the structure of the assets folder, generated by the assetManifest plugin
  const assetsManifest = assets as AssetEntry[];

  const findFolderInManifest = (
    manifest: AssetEntry[] | AssetEntry,
    folderPath: string,
  ): AssetEntry | null => {
    if (Array.isArray(manifest)) {
      for (const entry of manifest) {
        const found = findFolderInManifest(entry, folderPath);
        if (found) return found;
      }
      return null;
    }

    if (normalizeAssetPath(manifest.path) === folderPath) return manifest;
    if (manifest.type !== "directory") return null;
    for (const entry of manifest.entries) {
      if (entry.type === "directory") {
        const found = findFolderInManifest(entry, folderPath);
        if (found) return found;
      }
    }
    return null;
  };

  const getEntriesInFolder = (folderPath: string) => {
    if (folderPath === "/") return assetsManifest;
    const folder = findFolderInManifest(assetsManifest, folderPath);
    if (!folder || folder.type !== "directory") return [];
    return folder.entries.filter((entry) => {
      if (entry.type === "directory") return true;
      const ext = getFileExtension(entry.path);
      return extensions ? extensions.includes(ext) : true;
    });
  };

  const entriesInSelectedFolder = getEntriesInFolder(selectedFolder);
  const showBackOneLevel = selectedFolder !== "/";

  const handleOpenChange = (nextOpen: boolean) => {
    if (nextOpen) {
      const isBundled = currentPath !== null && isBundledAssetPath(currentPath);
      const manifestPath = isBundled ? toManifestPath(currentPath) : null;
      setMode(isBundled ? "bundled" : "custom");
      setUploadedFile(null);
      setSelectedManifestPath(manifestPath);
      setSelectedFolder(manifestPath ? getParentPath(manifestPath) : "/");
    }
    setOpen(nextOpen);
  };

  const canConfirm = mode === "custom" ? uploadedFile !== null : selectedManifestPath !== null;

  const handleConfirm = () => {
    if (mode === "custom") {
      if (!uploadedFile) return;
      onSelect({ kind: "custom", file: uploadedFile });
    } else {
      if (!selectedManifestPath) return;
      onSelect({ kind: "bundled", path: toBundledAssetPath(selectedManifestPath) });
    }
    setOpen(false);
  };

  const allowedExtensions = extensions ?? ALLOWED_EXTENSIONS;
  const acceptAttribute = allowedExtensions.map((extension) => `.${extension}`).join(",");

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="max-h-[85vh] grid-rows-[auto_minmax(0,1fr)_auto_auto] sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>
            Lade eine eigene Datei hoch oder wähle eine mitgelieferte Datei aus.
          </DialogDescription>
        </DialogHeader>

        <Tabs
          value={mode}
          onValueChange={(value) => setMode(value as "custom" | "bundled")}
          className="min-h-0"
        >
          <TabsList>
            <TabsTrigger value="custom">
              <UploadIcon />
              Eigene Datei
            </TabsTrigger>
            <TabsTrigger value="bundled">
              <PackageIcon />
              Dungeon Assets
            </TabsTrigger>
          </TabsList>

          <TabsContent value="custom" className="min-h-0">
            <div className="flex h-full max-h-[55vh] min-h-64 flex-col items-center justify-center gap-3 overflow-hidden rounded-lg border border-[var(--border-color)] bg-muted/30 p-3">
              {uploadedFile ? (
                <UploadedFilePreview file={uploadedFile} />
              ) : (
                <p className="text-center text-muted-foreground">
                  Noch keine Datei ausgewählt. Erlaubte Formate: {allowedExtensions.join(", ")}.
                </p>
              )}
              <Button variant="outline" className="shrink-0" onClick={() => fileInputRef.current?.click()}>
                <UploadIcon />
                Datei auswählen
              </Button>
              <input
                ref={fileInputRef}
                type="file"
                className="hidden"
                accept={acceptAttribute}
                onChange={(e) => {
                  const file = e.target.files?.[0] ?? null;
                  e.target.value = "";
                  if (file) setUploadedFile(file);
                }}
              />
            </div>
          </TabsContent>

          <TabsContent value="bundled" className="min-h-0">
            <div className="h-full max-h-[55vh] min-h-64 overflow-y-auto rounded-lg border border-[var(--border-color)] bg-muted/30 p-3">
              <div className="grid grid-cols-3 gap-3 sm:grid-cols-5">
                {showBackOneLevel && (
                  <AssetTile label="Zurück" onClick={() => setSelectedFolder(getParentPath(selectedFolder))}>
                    <ArrowLeft className="size-8" />
                  </AssetTile>
                )}
                {entriesInSelectedFolder.map((entry) => {
                  const entryPath = normalizeAssetPath(entry.path);
                  return (
                    <AssetTile
                      key={entryPath}
                      label={getAssetName(entryPath)}
                      selected={entry.type === "file" && selectedManifestPath === entryPath}
                      onClick={() =>
                        entry.type === "directory"
                          ? setSelectedFolder(entryPath)
                          : setSelectedManifestPath(entryPath)
                      }
                    >
                      {entry.type === "directory" ? (
                        <Folder className="size-14 fill-current text-primary/70" size="lg" />
                      ) : getFileExtension(entryPath) === "png" ? (
                        <img
                          src={`/bundled-assets${entryPath}`}
                          alt=""
                          className="size-12 object-contain [image-rendering:pixelated]"
                        />
                      ) : (
                        <File className="size-9 text-muted-foreground" />
                      )}
                    </AssetTile>
                  );
                })}
              </div>
              {entriesInSelectedFolder.length === 0 && !showBackOneLevel && (
                <p className="py-16 text-center text-muted-foreground">
                  Keine Einträge in diesem Ordner gefunden.
                </p>
              )}
            </div>
          </TabsContent>
        </Tabs>

        <Field>
          <FieldLabel>Ausgewählte Datei</FieldLabel>
          <Input
            value={
              mode === "custom"
                ? (uploadedFile?.name ?? "")
                : selectedManifestPath
                  ? toBundledAssetPath(selectedManifestPath)
                  : ""
            }
            readOnly
          />
        </Field>

        <DialogFooter>
          <DialogClose render={<Button variant="outline" />}>Abbrechen</DialogClose>
          <Button onClick={handleConfirm} disabled={!canConfirm}>
            Auswählen
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function UploadedFilePreview({ file }: { file: File }) {
  const [objectUrl, setObjectUrl] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (!file.type.startsWith("image/")) {
      setObjectUrl(null);
      return;
    }
    const url = URL.createObjectURL(file);
    setObjectUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  return (
    <div className="flex min-h-0 w-full flex-1 flex-col items-center justify-center gap-2">
      {objectUrl ? (
        <img
          src={objectUrl}
          alt=""
          className="min-h-0 w-auto max-w-full flex-1 object-contain [image-rendering:pixelated]"
        />
      ) : (
        <FileTypeIcon filePath={file.name} />
      )}
      <span className="max-w-full shrink-0 truncate text-sm">{file.name}</span>
    </div>
  );
}

function AssetTile({
  children,
  label,
  selected = false,
  onClick,
}: {
  children: React.ReactNode;
  label: string;
  selected?: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      aria-pressed={selected || undefined}
      title={label}
      onClick={onClick}
      className={`flex aspect-square min-w-0 flex-col items-center justify-center gap-2 rounded-lg border p-2 text-xs transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${
        selected
          ? "border-primary bg-primary/10 text-primary ring-2 ring-primary/30"
          : "border-border bg-background hover:border-primary/50 hover:bg-accent"
      }`}
    >
      {children}
      <span className="w-full truncate text-center">{label}</span>
    </button>
  );
}
