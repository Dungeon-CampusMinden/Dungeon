import React from "react";
import { FileTypeIcon } from "./FileTypeIcon";
import { USE_NN_BELOW } from "./assetPaths";

export function UploadedFilePreview({ file }: { file: File }) {
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
