import type { AnyResource, Asset } from "@/data/DeerSchema";
import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from "../ui/carousel";
import { AssetPreviewContent } from "../assets/AssetPreviewContent";
import { useAssetPreviews } from "../assets/useAssetPreviews";

/** Horizontally scrollable preview of all resources of a riddle. */
export function ResourceCarousel({ resources, assets }: { resources: AnyResource[]; assets: Asset[] }) {
  const usedAssets = assets.filter((asset) =>
    resources.some((resource) => resource.kind === "asset" && resource.assetId === asset.id),
  );
  const previews = useAssetPreviews(usedAssets);

  if (resources.length === 0) {
    return <span className="text-sm text-muted-foreground">Kein Material hinterlegt.</span>;
  }

  return (
    <Carousel opts={{ loop: false, align: "start" }} className="w-full">
      <CarouselContent className="-ml-2">
        {resources.map((resource) => (
          <CarouselItem key={resource.id} className="basis-1/2 pl-2 lg:basis-1/2">
            <ResourceCard resource={resource} assets={assets} previews={previews} />
          </CarouselItem>
        ))}
      </CarouselContent>
      <CarouselPrevious className="left-0 size-6 bg-background/80" />
      <CarouselNext className="right-0 size-6 bg-background/80" />
    </Carousel>
  );
}

function ResourceCard({
  resource,
  assets,
  previews,
}: {
  resource: AnyResource;
  assets: Asset[];
  previews: ReturnType<typeof useAssetPreviews>;
}) {
  const asset = resource.kind === "asset" ? assets.find((item) => item.id === resource.assetId) : undefined;

  return (
    <div className="flex h-full min-w-0 flex-col gap-2 rounded-lg border border-[var(--border-color)] p-2">
      <span className="truncate text-sm font-medium" title={resource.title}>
        {resource.title}
      </span>
      <div className="flex aspect-square items-center justify-center overflow-hidden rounded-md bg-muted/30 p-2">
        {resource.kind === "inline_text" ? (
          <span className="line-clamp-4 text-xs text-muted-foreground" title={resource.text}>
            {resource.text || "Kein Text hinterlegt."}
          </span>
        ) : asset ? (
          <AssetPreviewContent asset={asset} preview={previews[asset.id]} />
        ) : (
          <span className="text-center text-xs text-destructive">Keine Datei ausgewählt</span>
        )}
      </div>
    </div>
  );
}
