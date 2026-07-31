import type { AnyRiddle, CollectionRiddle, DeerSchema, InputRiddle } from "@/data/DeerSchema";
import { SurfaceSelector } from "../SurfacesTab";
import { Field, FieldDescription, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { COLLECTION_SOURCE_KINDS } from "./riddleTypes";
import { SimpleSelect } from "./SimpleSelect";

const YES_NO = [
  { value: "true", label: "Ja" },
  { value: "false", label: "Nein" },
] as const;

/** Renders the type specific editor for the parameters of a riddle. */
export function RiddleParametersEditor({
  riddle,
  setRiddle,
  deerSchema,
}: {
  riddle: AnyRiddle;
  setRiddle: (updated: AnyRiddle) => void;
  deerSchema: DeerSchema;
}) {
  if (riddle.type === "collection") {
    return (
      <CollectionParametersEditor
        riddle={riddle}
        setRiddle={setRiddle as (updated: CollectionRiddle) => void}
        deerSchema={deerSchema}
      />
    );
  }
  return (
    <InputParametersEditor
      riddle={riddle}
      setRiddle={setRiddle as (updated: InputRiddle) => void}
      deerSchema={deerSchema}
    />
  );
}

function CollectionParametersEditor({
  riddle,
  setRiddle,
  deerSchema,
}: {
  riddle: CollectionRiddle;
  setRiddle: (updated: CollectionRiddle) => void;
  deerSchema: DeerSchema;
}) {
  const parameters = riddle.parameters;

  const updateParameters = (updated: Partial<CollectionRiddle["parameters"]>) => {
    setRiddle({ ...riddle, parameters: { ...parameters, ...updated } });
  };

  const toggleResourceId = (resourceId: string) => {
    const selected = parameters.resourceIds.includes(resourceId);
    updateParameters({
      resourceIds: selected
        ? parameters.resourceIds.filter((id) => id !== resourceId)
        : [...parameters.resourceIds, resourceId],
    });
  };

  return (
    <div className="flex flex-col gap-4">
      <Field>
        <FieldLabel>Ort</FieldLabel>
        <SurfaceSelector
          items={deerSchema.surfaces}
          value={parameters.surfaceId}
          onChange={(newValue) => updateParameters({ surfaceId: newValue })}
        />
      </Field>
      <Field>
        <FieldLabel>Fundort</FieldLabel>
        <SimpleSelect
          options={COLLECTION_SOURCE_KINDS}
          value={parameters.sourceKind}
          onChange={(newValue) =>
            updateParameters({ sourceKind: newValue as CollectionRiddle["parameters"]["sourceKind"] })
          }
        />
      </Field>
      <Field>
        <FieldLabel>Belohnungen</FieldLabel>
        <FieldDescription>Welches Material erhalten die Spieler beim Lösen des Rätsels?</FieldDescription>
        <div className="flex flex-col gap-1">
          {riddle.resources.length === 0 && (
            <span className="text-sm text-muted-foreground">Lege zuerst ein Material an.</span>
          )}
          {riddle.resources.map((resource) => (
            <label key={resource.id} className="flex cursor-pointer items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={parameters.resourceIds.includes(resource.id)}
                onChange={() => toggleResourceId(resource.id)}
              />
              {resource.title}
            </label>
          ))}
        </div>
      </Field>
    </div>
  );
}

function InputParametersEditor({
  riddle,
  setRiddle,
  deerSchema,
}: {
  riddle: InputRiddle;
  setRiddle: (updated: InputRiddle) => void;
  deerSchema: DeerSchema;
}) {
  const parameters = riddle.parameters;

  const updateParameters = (updated: Partial<InputRiddle["parameters"]>) => {
    setRiddle({ ...riddle, parameters: { ...parameters, ...updated } });
  };

  return (
    <div className="flex flex-col gap-4">
      <Field>
        <FieldLabel>Gerät</FieldLabel>
        <SurfaceSelector
          items={deerSchema.surfaces}
          value={parameters.surfaceId}
          onChange={(newValue) => updateParameters({ surfaceId: newValue })}
        />
      </Field>
      <Field>
        <FieldLabel>Lösung</FieldLabel>
        <FieldDescription>Eine Zahl mit bis zu 8 Ziffern.</FieldDescription>
        <Input
          value={parameters.answer}
          inputMode="numeric"
          onChange={(e) => updateParameters({ answer: e.target.value.replace(/[^0-9]/g, "").slice(0, 8) })}
        />
      </Field>
      <Field>
        <FieldLabel>Stellenanzahl anzeigen</FieldLabel>
        <SimpleSelect
          options={YES_NO}
          value={parameters.showDigitCount ? "true" : "false"}
          onChange={(newValue) => updateParameters({ showDigitCount: newValue === "true" })}
        />
      </Field>
    </div>
  );
}
