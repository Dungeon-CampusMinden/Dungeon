import type { AnyRiddleInput, CollectionInput, DeerProject, NumericInput, Riddle } from "@/data/DeerSchema";
import { PlusIcon, TrashIcon } from "lucide-react";
import {
  addRiddleInput,
  convertRiddleInput,
  removeRiddleInput,
} from "@/data/RiddleGraphActions";
import { Button } from "../ui/button";
import { Field, FieldDescription, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { INPUT_TYPES } from "./riddleTypes";
import { SimpleSelect } from "./SimpleSelect";

const YES_NO = [
  { value: "true", label: "Ja" },
  { value: "false", label: "Nein" },
] as const;

export function RiddleInputsEditor({
  riddle,
  project,
  onChange,
}: {
  project: DeerProject;
  riddle: Riddle;
  onChange: (updated: DeerProject) => void;
}) {
  const inputs = riddle.inputs;

  const updateProject = (transform: (next: DeerProject, nextRiddle: Riddle) => void) => {
    const next = structuredClone(project);
    const nextRiddle = next.riddles.find((candidate) => candidate.id === riddle.id);
    if (!nextRiddle) return;
    transform(next, nextRiddle);
    onChange(next);
  };

  const updateInput = (index: number, updated: AnyRiddleInput) => {
    updateProject((_next, nextRiddle) => {
      nextRiddle.inputs[index] = updated;
    });
  };

  const addInput = () => {
    updateProject((next, nextRiddle) => addRiddleInput(next, nextRiddle));
  };

  const removeInput = (index: number) => {
    const inputId = inputs[index]?.id;
    if (inputId) updateProject((next, nextRiddle) => removeRiddleInput(next, nextRiddle, inputId));
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
                accessibleLabel={`Art der Eingabe ${index + 1}`}
                options={INPUT_TYPES.map((type) => ({ value: type.value, label: type.label }))}
                value={input.type}
                onChange={(newValue) =>
                  updateProject((next, nextRiddle) =>
                    convertRiddleInput(next, nextRiddle, input.id, newValue as AnyRiddleInput["type"]),
                  )
                }
              />
            </Field>
            <Button aria-label={`Eingabe ${index + 1} löschen`} variant="destructive" size="icon" onClick={() => removeInput(index)}>
              <TrashIcon />
            </Button>
          </div>
          {input.type === "collection" ? (
            <CollectionInputFields
              input={input}
              inputIndex={index}
              riddle={riddle}
              onChange={(updated) => updateInput(index, updated)}
            />
          ) : (
            <NumericInputFields
              input={input}
              inputIndex={index}
              project={project}
              onChange={(updated) => updateInput(index, updated)}
              onSurfaceTitleChange={(title) => updateProject((next) => {
                const surface = next.surfaces.find((candidate) => candidate.id === input.surfaceId);
                if (surface) surface.title = title;
              })}
            />
          )}
        </div>
      ))}
    </div>
  );
}

function CollectionInputFields({
  input,
  inputIndex,
  riddle,
  onChange,
}: {
  input: CollectionInput;
  inputIndex: number;
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
          accessibleLabel={`Informationsquelle für Eingabe ${inputIndex + 1}`}
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
  inputIndex,
  project,
  onChange,
  onSurfaceTitleChange,
}: {
  input: NumericInput;
  inputIndex: number;
  project: DeerProject;
  onChange: (updated: NumericInput) => void;
  onSurfaceTitleChange: (title: string) => void;
}) {
  return (
    <>
      <Field>
        <FieldLabel>Gerät</FieldLabel>
        <Input
          aria-label={`Gerät für Eingabe ${inputIndex + 1}`}
          value={project.surfaces.find((surface) => surface.id === input.surfaceId)?.title ?? ""}
          onChange={(event) => onSurfaceTitleChange(event.target.value)}
        />
      </Field>
      <Field>
        <FieldLabel>Lösung</FieldLabel>
        <FieldDescription>Eine Zahl mit bis zu 8 Ziffern.</FieldDescription>
        <Input
          aria-label={`Lösung für Eingabe ${inputIndex + 1}`}
          value={input.answer}
          inputMode="numeric"
          onChange={(e) => onChange({ ...input, answer: e.target.value.replace(/[^0-9]/g, "").slice(0, 8) })}
        />
      </Field>
      <Field>
        <FieldLabel>Stellenanzahl anzeigen</FieldLabel>
        <SimpleSelect
          accessibleLabel={`Stellenanzahl für Eingabe ${inputIndex + 1} anzeigen`}
          options={YES_NO}
          value={input.showDigitCount ? "true" : "false"}
          onChange={(newValue) => onChange({ ...input, showDigitCount: newValue === "true" })}
        />
      </Field>
    </>
  );
}
