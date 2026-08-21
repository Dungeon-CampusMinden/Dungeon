import type { AnyRiddleInput, DeerProject, InformationSource, Riddle } from "@/data/DeerSchema";
import { ResourceCarousel } from "./ResourceCarousel";
import { getInputType, InputTypeIcon } from "./riddleTypes";

export function RiddleInputsView({ riddle, deerSchema }: { riddle: Riddle; deerSchema: DeerProject }) {
  if (riddle.inputs.length === 0) {
    return <span className="text-sm text-muted-foreground">Keine Eingabe hinterlegt.</span>;
  }
  return (
    <div className="flex flex-col gap-2">
      {riddle.inputs.map((input) => (
        <InputView key={input.id} input={input} riddle={riddle} deerSchema={deerSchema} />
      ))}
    </div>
  );
}

function ParameterRow({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="grid grid-cols-[140px_1fr] items-center gap-2 text-sm">
      <span className="text-muted-foreground">{label}</span>
      <div className="flex items-center gap-2">{children}</div>
    </div>
  );
}

function InputView({
  input,
  riddle,
  deerSchema,
}: {
  input: AnyRiddleInput;
  riddle: Riddle;
  deerSchema: DeerProject;
}) {
  const inputType = getInputType(input.type);

  return (
    <div className="flex flex-col gap-1 border border-border rounded-md p-2">
      <div className="flex items-center gap-2 text-sm">
        <InputTypeIcon type={input.type} size={18} />
        <span>{inputType?.label ?? input.type}</span>
      </div>
      {input.type === "collection" ? (
        <CollectionInputView
          informationSource={riddle.informationSources.find(
            (source) => source.id === input.informationSourceId,
          )}
          deerSchema={deerSchema}
        />
      ) : (
        <>
          <ParameterRow label="Lösung">
            <span className="font-mono">{input.answer || "—"}</span>
          </ParameterRow>
          <ParameterRow label="Stellenanzahl">
            <span>{input.showDigitCount ? "Wird angezeigt" : "Wird nicht angezeigt"}</span>
          </ParameterRow>
        </>
      )}
    </div>
  );
}

function CollectionInputView({
  informationSource,
  deerSchema,
}: {
  informationSource: InformationSource | undefined;
  deerSchema: DeerProject;
}) {
  if (!informationSource) {
    return (
      <ParameterRow label="Informationsquelle">
        <span className="text-destructive">Keine Informationsquelle ausgewählt</span>
      </ParameterRow>
    );
  }
  return (
    <>
      <div className="mt-1 flex flex-col gap-1">
        <span className="text-sm text-muted-foreground">Material</span>
        <ResourceCarousel resources={informationSource.resources} assets={deerSchema.assets} />
      </div>
    </>
  );
}
