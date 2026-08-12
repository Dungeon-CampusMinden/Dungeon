import { File, FileAudio, FileText, FileType } from "lucide-react";
import { getFileExtension } from "./assetPaths";

export function FileTypeIcon({
  filePath,
  showExtension = false,
}: {
  filePath: string;
  showExtension?: boolean;
}) {
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
