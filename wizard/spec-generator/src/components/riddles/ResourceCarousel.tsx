import type { AnyResource, Asset } from "@/data/DeerSchema";
import React from "react";
import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from "../ui/carousel";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../ui/dialog";
import { AssetPreviewContent } from "../assets/AssetPreviewContent";
import { useAssetPreviews } from "../assets/useAssetPreviews";

/** Horizontally scrollable preview of all resources of a riddle. */
export function ResourceCarousel({ resources, assets }: { resources: AnyResource[]; assets: Asset[] }) {
  const usedAssets = assets.filter((asset) =>
    resources.some((resource) => resource.kind === "asset" && resource.assetId === asset.id),
  );
  const previews = useAssetPreviews(usedAssets);
  const [detailResource, setDetailResource] = React.useState<AnyResource | null>(null);

  if (resources.length === 0) {
    return <span className="text-sm text-muted-foreground">Kein Material hinterlegt.</span>;
  }

  return (
    <>
      <Carousel opts={{ loop: false, align: "start" }} className="w-full">
        <CarouselContent className="-ml-2">
          {resources.map((resource) => (
            <CarouselItem key={resource.id} className="basis-1/3 pl-2">
              <ResourceCard
                resource={resource}
                assets={assets}
                previews={previews}
                onClick={() => setDetailResource(resource)}
              />
            </CarouselItem>
          ))}
        </CarouselContent>
        <CarouselPrevious className="left-0 size-6 bg-background/80" />
        <CarouselNext className="right-0 size-6 bg-background/80" />
      </Carousel>

      <ResourceDetailDialog
        resource={detailResource}
        assets={assets}
        previews={previews}
        onClose={() => setDetailResource(null)}
      />
    </>
  );
}

function ResourceCard({
  resource,
  assets,
  previews,
  onClick,
}: {
  resource: AnyResource;
  assets: Asset[];
  previews: ReturnType<typeof useAssetPreviews>;
  onClick: () => void;
}) {
  const asset = resource.kind === "asset" ? assets.find((item) => item.id === resource.assetId) : undefined;

  return (
    <button
      type="button"
      onClick={onClick}
      aria-label={`Material '${resource.title}' anzeigen`}
      className="flex h-full w-full min-w-0 cursor-pointer flex-col gap-1 rounded-lg border border-[var(--border-color)] bg-transparent p-1.5 text-left transition-colors hover:bg-muted/40"
    >
      <span className="w-full truncate text-xs font-medium" title={resource.title}>
        {resource.title}
      </span>
      <div className="flex aspect-square w-full items-center justify-center overflow-hidden rounded-md bg-muted/30 p-1.5">
        {resource.kind === "inline_text" ? (
          <span className="line-clamp-4 text-[0.65rem] text-muted-foreground">
            {resource.text || "Kein Text hinterlegt."}
          </span>
        ) : asset ? (
          <AssetPreviewContent asset={asset} preview={previews[asset.id]} />
        ) : (
          <span className="text-center text-[0.65rem] text-destructive">Keine Datei</span>
        )}
      </div>
    </button>
  );
}

function ResourceDetailDialog({
  resource,
  assets,
  previews,
  onClose,
}: {
  resource: AnyResource | null;
  assets: Asset[];
  previews: ReturnType<typeof useAssetPreviews>;
  onClose: () => void;
}) {
  const asset = resource?.kind === "asset" ? assets.find((item) => item.id === resource.assetId) : undefined;

  return (
    <Dialog open={resource !== null} onOpenChange={(nextOpen) => !nextOpen && onClose()}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{resource?.title ?? "Material"}</DialogTitle>
        </DialogHeader>
        <div className="flex max-h-[70vh] items-center justify-center overflow-auto rounded-md bg-muted/30 p-4">
          {resource === null ? null : resource.kind === "inline_text" ? (
            <span className="w-full text-sm whitespace-pre-wrap">
              {resource.text || "Kein Text hinterlegt."}
            </span>
          ) : asset ? (
            <AssetPreviewContent asset={asset} preview={previews[asset.id]} />
          ) : (
            <span className="text-sm text-destructive">Keine Datei ausgewählt</span>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
