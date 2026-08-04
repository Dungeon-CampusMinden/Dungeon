import type { AnyRiddleInput, CollectionInput, DeerSchema, NumericInput, Riddle } from "@/data/DeerSchema";
import { PlusIcon, TrashIcon } from "lucide-react";
import { SurfaceSelector } from "../SurfacesTab";
import { Button } from "../ui/button";
import { Field, FieldDescription, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { convertRiddleInput, createRiddleInput, INPUT_TYPES } from "./riddleTypes";
import { SimpleSelect } from "./SimpleSelect";

const YES_NO = [
  { value: "true", label: "Ja" },
  { value: "false", label: "Nein" },
] as const;

export function RiddleInputsEditor({
  riddle,
  setRiddle,
  deerSchema,
}: {
  riddle: Riddle;
  setRiddle: (updated: Riddle) => void;
  deerSchema: DeerSchema;
}) {
  const inputs = riddle.inputs;

  const updateInput = (index: number, updated: AnyRiddleInput) => {
    const next = [...inputs];
    next[index] = updated;
    setRiddle({ ...riddle, inputs: next });
  };

  const addInput = () => {
    setRiddle({ ...riddle, inputs: [...inputs, createRiddleInput("numeric")] });
  };

  const removeInput = (index: number) => {
    const next = [...inputs];
    next.splice(index, 1);
    setRiddle({ ...riddle, inputs: next });
  };

  return (
    <div className="flex flex-col gap-3">
      <Button onClick={addInput} className="lg:max-w-40">
        <PlusIcon />
        Eingabe
      </Button>
      {inputs.length === 0 && (
        <span className="text-sm text-muted-foreground">Noch keine Eingabe hinterlegt.</span>
      )}
      {inputs.map((input, index) => (
        <div
          key={input.id}
          className="flex flex-col gap-3 rounded-md border border-[var(--border-color)] p-3"
        >
          <div className="grid grid-cols-[1fr_auto] items-end gap-2">
            <Field>
              <FieldLabel>Art der Eingabe</FieldLabel>
              <SimpleSelect
                options={INPUT_TYPES.map((type) => ({ value: type.value, label: type.label }))}
                value={input.type}
                onChange={(newValue) =>
                  updateInput(index, convertRiddleInput(input, newValue as AnyRiddleInput["type"]))
                }
              />
            </Field>
            <Button variant="destructive" size="icon" onClick={() => removeInput(index)}>
              <TrashIcon />
            </Button>
          </div>
          {input.type === "collection" ? (
            <CollectionInputFields
              input={input}
              riddle={riddle}
              onChange={(updated) => updateInput(index, updated)}
            />
          ) : (
            <NumericInputFields
              input={input}
              deerSchema={deerSchema}
              onChange={(updated) => updateInput(index, updated)}
            />
          )}
        </div>
      ))}
    </div>
  );
}

function CollectionInputFields({
  input,
  riddle,
  onChange,
}: {
  input: CollectionInput;
  riddle: Riddle;
  onChange: (updated: CollectionInput) => void;
}) {
  const options = riddle.informationSources.map((source, index) => ({
    value: source.id,
    label: source.resources[0]?.title || `Informationsquelle ${index + 1}`,
  }));

  return (
    <Field>
      <FieldLabel>Informationsquelle</FieldLabel>
      <FieldDescription>Welche Materialien müssen die Spieler finden?</FieldDescription>
      {options.length === 0 ? (
        <span className="text-sm text-muted-foreground">Lege zuerst eine Informationsquelle an.</span>
      ) : (
        <SimpleSelect
          options={options}
          value={input.informationSourceId}
          onChange={(newValue) => onChange({ ...input, informationSourceId: newValue })}
        />
      )}
    </Field>
  );
}

function NumericInputFields({
  input,
  deerSchema,
  onChange,
}: {
  input: NumericInput;
  deerSchema: DeerSchema;
  onChange: (updated: NumericInput) => void;
}) {
  return (
    <>
      <Field>
        <FieldLabel>Gerät</FieldLabel>
        <SurfaceSelector
          items={deerSchema.surfaces}
          value={input.surfaceId}
          onChange={(newValue) => onChange({ ...input, surfaceId: newValue })}
        />
      </Field>
      <Field>
        <FieldLabel>Lösung</FieldLabel>
        <FieldDescription>Eine Zahl mit bis zu 8 Ziffern.</FieldDescription>
        <Input
          value={input.answer}
          inputMode="numeric"
          onChange={(e) => onChange({ ...input, answer: e.target.value.replace(/[^0-9]/g, "").slice(0, 8) })}
        />
      </Field>
      <Field>
        <FieldLabel>Stellenanzahl anzeigen</FieldLabel>
        <SimpleSelect
          options={YES_NO}
          value={input.showDigitCount ? "true" : "false"}
          onChange={(newValue) => onChange({ ...input, showDigitCount: newValue === "true" })}
        />
      </Field>
    </>
  );
}
