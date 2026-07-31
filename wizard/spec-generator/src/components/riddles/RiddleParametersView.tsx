import type { AnyRiddle, CollectionRiddle, DeerSchema, InputRiddle } from "@/data/DeerSchema";
import { Util } from "@/data/Util";
import { SurfaceIcon } from "../SurfacesTab";
import { COLLECTION_SOURCE_KINDS } from "./riddleTypes";

/** Renders the type specific parameters of a riddle in a read-only fashion. */
export function RiddleParametersView({ riddle, deerSchema }: { riddle: AnyRiddle; deerSchema: DeerSchema }) {
  if (riddle.type === "collection") {
    return <CollectionParametersView riddle={riddle} deerSchema={deerSchema} />;
  }
  return <InputParametersView riddle={riddle} deerSchema={deerSchema} />;
}

function ParameterRow({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="grid grid-cols-[140px_1fr] items-center gap-2 text-sm">
      <span className="text-muted-foreground">{label}</span>
      <div className="flex items-center gap-2">{children}</div>
    </div>
  );
}

function SurfaceValue({ deerSchema, surfaceId }: { deerSchema: DeerSchema; surfaceId: string }) {
  const surface = Util.getSurface(deerSchema, surfaceId);
  if (!surface) {
    return <span className="text-destructive">Kein Ort ausgewählt</span>;
  }
  return (
    <>
      <SurfaceIcon kind={surface.kind} size={18} />
      <span>{surface.title}</span>
    </>
  );
}

function CollectionParametersView({
  riddle,
  deerSchema,
}: {
  riddle: CollectionRiddle;
  deerSchema: DeerSchema;
}) {
  const sourceKind = COLLECTION_SOURCE_KINDS.find((item) => item.value === riddle.parameters.sourceKind);
  return (
    <div className="flex flex-col gap-1">
      <ParameterRow label="Ort">
        <SurfaceValue deerSchema={deerSchema} surfaceId={riddle.parameters.surfaceId} />
      </ParameterRow>
      <ParameterRow label="Fundort">
        <span>{sourceKind?.label ?? riddle.parameters.sourceKind}</span>
      </ParameterRow>
      <ParameterRow label="Belohnung">
        <span>{riddle.parameters.resourceIds.length} Material</span>
      </ParameterRow>
    </div>
  );
}

function InputParametersView({ riddle, deerSchema }: { riddle: InputRiddle; deerSchema: DeerSchema }) {
  return (
    <div className="flex flex-col gap-1">
      <ParameterRow label="Gerät">
        <SurfaceValue deerSchema={deerSchema} surfaceId={riddle.parameters.surfaceId} />
      </ParameterRow>
      <ParameterRow label="Lösung">
        <span className="font-mono">{riddle.parameters.answer || "—"}</span>
      </ParameterRow>
      <ParameterRow label="Stellenanzahl">
        <span>{riddle.parameters.showDigitCount ? "Wird angezeigt" : "Wird nicht angezeigt"}</span>
      </ParameterRow>
    </div>
  );
}
