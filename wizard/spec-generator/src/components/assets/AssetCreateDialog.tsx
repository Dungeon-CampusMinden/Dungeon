import React from "react";
import assets from "virtual:dungeon-assets-manifest";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../ui/tabs";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Field, FieldLabel } from "../ui/field";
import { ArrowLeft, File, Folder, PackageIcon, UploadIcon } from "lucide-react";
import { AssetTile } from "./AssetTile";
import {
  ALLOWED_EXTENSIONS,
  getAssetName,
  getFileExtension,
  getParentPath,
  isBundledAssetPath,
  normalizeAssetPath,
  toBundledAssetPath,
  toManifestPath,
  type AssetEntry,
  type AssetSelection,
} from "./assetPaths";
import { FileTypeIcon } from "./FileTypeIcon";
import { USE_NN_BELOW } from "./assetPaths";

/**
 * Dialog to pick the content of an asset: either a file uploaded by the user (custom asset)
 * or a file from the bundled assets shipped with the application.
 */
export function AssetCreateDialog({
  open,
  setOpen,
  onSelect,
  currentPath = null,
  extensions = ALLOWED_EXTENSIONS,
  title = "Datei auswählen",
}: {
  open: boolean;
  setOpen: (open: boolean) => void;
  onSelect: (selection: AssetSelection) => Promise<void> | void;
  currentPath?: string | null;
  extensions?: string[] | null;
  title?: string;
}) {
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const [mode, setMode] = React.useState<"custom" | "bundled">("custom");
  const [uploadedFile, setUploadedFile] = React.useState<File | null>(null);
  const [selectedFolder, setSelectedFolder] = React.useState("/");
  const [selectedManifestPath, setSelectedManifestPath] = React.useState<string | null>(null);
  const [pending, setPending] = React.useState(false);

  React.useEffect(() => {
    if (open) return;
    setMode("custom");
    setUploadedFile(null);
    setSelectedManifestPath(null);
    setSelectedFolder("/");
  }, [open]);

  // The assetManifest plugin generates this folder structure from the bundled assets.
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
    if (pending) return;
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

  const handleConfirm = async () => {
    let selection: AssetSelection;
    if (mode === "custom") {
      if (!uploadedFile) return;
      selection = { kind: "custom", file: uploadedFile };
    } else {
      if (!selectedManifestPath) return;
      selection = { kind: "bundled", path: toBundledAssetPath(selectedManifestPath) };
    }
    setPending(true);
    try {
      await onSelect(selection);
      setOpen(false);
    } catch {
      // The caller reports the technical error. Keep the selection open for a retry.
    } finally {
      setPending(false);
    }
  };

  const allowedExtensions = extensions ?? ALLOWED_EXTENSIONS;
  const acceptAttribute = allowedExtensions.map((extension) => `.${extension}`).join(",");

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="max-h-[85vh] grid-rows-[auto_minmax(0,1fr)_auto_auto] sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>
            Lade eine eigene Datei hoch oder wähle eine Datei aus dem Dungeon aus.
          </DialogDescription>
        </DialogHeader>

        <Tabs
          value={mode}
          onValueChange={(value) => setMode(value as "custom" | "bundled")}
          className={`min-h-0 ${pending ? "pointer-events-none opacity-70" : ""}`}
        >
          <TabsList>
            <TabsTrigger value="custom">
              <UploadIcon />
              Eigene Datei
            </TabsTrigger>
            <TabsTrigger value="bundled">
              <PackageIcon />
              Spielbibliothek
            </TabsTrigger>
          </TabsList>

          <TabsContent value="custom" className="min-h-0">
            <div className="flex h-full max-h-[55vh] min-h-64 flex-col items-center justify-center gap-3 overflow-hidden rounded-lg border border-border bg-muted/30 p-3">
              {uploadedFile ? (
                <UploadedFilePreview file={uploadedFile} />
              ) : (
                <p className="text-center text-muted-foreground">
                  Noch keine Datei ausgewählt. Erlaubte Formate: {allowedExtensions.join(", ")}.
                </p>
              )}
              <Button
                variant="outline"
                className="shrink-0"
                disabled={pending}
                onClick={() => fileInputRef.current?.click()}
              >
                <UploadIcon />
                Datei auswählen
              </Button>
              <input
                ref={fileInputRef}
                type="file"
                className="hidden"
                accept={acceptAttribute}
                disabled={pending}
                onChange={(e) => {
                  const file = e.target.files?.[0] ?? null;
                  e.target.value = "";
                  if (file) setUploadedFile(file);
                }}
              />
            </div>
          </TabsContent>

          <TabsContent value="bundled" className="min-h-0">
            <div className="h-full max-h-[55vh] min-h-64 overflow-y-auto rounded-lg border border-border bg-muted/30 p-3">
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
                      warning={
                        entry.type === "file" && entry.source === null ? "Lizenz fehlt" : undefined
                      }
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
            aria-label="Ausgewählte Datei"
            value={
              mode === "custom"
                ? (uploadedFile?.name ?? "")
                : selectedManifestPath
                  ? getAssetName(selectedManifestPath)
                  : ""
            }
            readOnly
          />
        </Field>

        <DialogFooter>
          <DialogClose render={<Button variant="outline" disabled={pending} />}>Abbrechen</DialogClose>
          <Button onClick={() => void handleConfirm()} disabled={!canConfirm || pending}>
            {pending ? "Wird gespeichert…" : "Auswählen"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function UploadedFilePreview({ file }: { file: File }) {
  const [objectUrl, setObjectUrl] = React.useState<string | null>(null);
  const [pixelated, setPixelated] = React.useState(false);

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
          onLoad={(e) => {
            const img = e.currentTarget;
            setPixelated(img.naturalWidth < USE_NN_BELOW || img.naturalHeight < USE_NN_BELOW);
          }}
          className={`min-h-0 w-auto max-w-full flex-1 object-contain ${
            pixelated ? "[image-rendering:pixelated]" : ""
          }`}
        />
      ) : (
        <FileTypeIcon filePath={file.name} />
      )}
      <span className="max-w-full shrink-0 truncate text-sm">{file.name}</span>
    </div>
  );
}
