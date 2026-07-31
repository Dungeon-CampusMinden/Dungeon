import type { Asset } from "@/data/DeerSchema";
import React from "react";
import { AlertTriangle } from "lucide-react";
import { FileTypeIcon } from "./FileTypeIcon";
import { USE_NN_BELOW, type AssetPreview } from "./assetPaths";

export function AssetPreviewContent({ asset, preview }: { asset: Asset; preview: AssetPreview | undefined }) {
  const [pixelated, setPixelated] = React.useState(false);

  React.useEffect(() => {
    setPixelated(false);
  }, [preview?.previewUrl]);

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
        onLoad={(e) => {
          const img = e.currentTarget;
          setPixelated(img.naturalWidth < USE_NN_BELOW || img.naturalHeight < USE_NN_BELOW);
        }}
        className={`mt-0 max-h-full max-w-full object-contain ${
          pixelated ? "[image-rendering:pixelated]" : ""
        }`}
      />
    );
  }

  return <FileTypeIcon filePath={asset.path} showExtension />;
}
