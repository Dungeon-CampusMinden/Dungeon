import type { AnyRiddleInput, ChoiceInput, CollectionInput, DeerProject, NumericInput, Riddle } from "@/data/DeerSchema";
import { CheckIcon, PlusIcon, TrashIcon } from "lucide-react";
import {
  addRiddleInput,
  convertRiddleInput,
  removeRiddleInput,
} from "@/data/RiddleGraphActions";
import { Button } from "../ui/button";
import { Field, FieldDescription, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { Textarea } from "../ui/textarea";
import { Util } from "@/data/Util";
import { INPUT_TYPES } from "./riddleTypes";
import { SimpleSelect } from "./SimpleSelect";
import type { Issue, TabIssues } from "@/data/ErrorChecker";
import { fieldIssues, ValidationFeedback } from "../ValidationFeedback";
import { ResponsiveChoice } from "../ui/responsive-choice";

const YES_NO = [
  { value: "true", label: "Ja" },
  { value: "false", label: "Nein" },
] as const;

export function RiddleInputsEditor({
  riddle,
  project,
  onChange,
  issues,
}: {
  project: DeerProject;
  riddle: Riddle;
  onChange: (updated: DeerProject) => void;
  issues?: TabIssues;
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
          className="flex flex-col gap-3 rounded-md border border-border p-3"
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
          ) : input.type === "numeric" ? (
            <NumericInputFields
              input={input}
              inputIndex={index}
              issues={fieldIssues(issues, `riddle:${riddle.id}:input:${input.id}:answer`)}
              onChange={(updated) => updateInput(index, updated)}
            />
          ) : (
            <ChoiceInputFields
              input={input}
              inputIndex={index}
              questionIssues={fieldIssues(issues, `riddle:${riddle.id}:input:${input.id}:question`)}
              optionIssues={fieldIssues(issues, `riddle:${riddle.id}:input:${input.id}:options`)}
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
  onChange,
  issues,
}: {
  input: NumericInput;
  inputIndex: number;
  onChange: (updated: NumericInput) => void;
  issues: Issue[];
}) {
  return (
    <>
      <Field>
        <FieldLabel>Lösung</FieldLabel>
        <FieldDescription>Eine Zahl mit bis zu 8 Ziffern.</FieldDescription>
        <Input
          aria-label={`Lösung für Eingabe ${inputIndex + 1}`}
          aria-invalid={issues.some((issue) => issue.severity === "error")}
          value={input.answer}
          inputMode="numeric"
          onChange={(e) => onChange({ ...input, answer: e.target.value.replace(/[^0-9]/g, "").slice(0, 8) })}
        />
        <ValidationFeedback issues={issues} />
      </Field>
      <Field>
        <FieldLabel>Stellenanzahl anzeigen</FieldLabel>
        <ResponsiveChoice
          accessibleLabel={`Stellenanzahl für Eingabe ${inputIndex + 1} anzeigen`}
          options={YES_NO}
          value={input.showDigitCount ? "true" : "false"}
          onChange={(newValue) => onChange({ ...input, showDigitCount: newValue === "true" })}
        />
      </Field>
    </>
  );
}

const MAX_CHOICE_OPTIONS = 6;
const MIN_CHOICE_OPTIONS = 2;

function ChoiceInputFields({
  input,
  inputIndex,
  onChange,
  questionIssues,
  optionIssues,
}: {
  input: ChoiceInput;
  inputIndex: number;
  onChange: (updated: ChoiceInput) => void;
  questionIssues: Issue[];
  optionIssues: Issue[];
}) {
  const toggleCorrect = (optionId: string) => {
    const isCorrect = input.correctOptionIds.includes(optionId);
    const correctOptionIds = input.multipleSelection
      ? isCorrect
        ? input.correctOptionIds.filter((id) => id !== optionId)
        : [...input.correctOptionIds, optionId]
      : [optionId];
    onChange({ ...input, correctOptionIds });
  };

  return (
    <>
      <Field>
        <FieldLabel>Frage</FieldLabel>
        <Textarea
          aria-label={`Frage für Eingabe ${inputIndex + 1}`}
          aria-invalid={questionIssues.some((issue) => issue.severity === "error")}
          value={input.question}
          onChange={(e) => onChange({ ...input, question: e.target.value })}
        />
        <ValidationFeedback issues={questionIssues} />
      </Field>
      <Field>
        <FieldLabel>Mehrere richtige Antworten</FieldLabel>
        <FieldDescription>Bei „Ja“ müssen die Spieler alle richtigen Antworten auswählen.</FieldDescription>
        <ResponsiveChoice
          accessibleLabel={`Mehrere richtige Antworten für Eingabe ${inputIndex + 1}`}
          options={YES_NO}
          value={input.multipleSelection ? "true" : "false"}
          onChange={(newValue) => onChange({
            ...input,
            multipleSelection: newValue === "true",
            correctOptionIds: newValue === "true" ? input.correctOptionIds : input.correctOptionIds.slice(0, 1),
          })}
        />
      </Field>
      <Field>
        <FieldLabel>Antworten</FieldLabel>
        <FieldDescription>Markiere {input.multipleSelection ? "alle richtigen Antworten" : "die richtige Antwort"} mit dem Haken.</FieldDescription>
        <div className="flex flex-col gap-2">
          {input.options.map((option, optionIndex) => {
            const isCorrect = input.correctOptionIds.includes(option.id);
            return (
              <div key={option.id} className="grid grid-cols-[auto_1fr_auto] items-center gap-2">
                <Button
                  aria-label={`Antwort ${optionIndex + 1} als richtig markieren`}
                  aria-pressed={isCorrect}
                  variant={isCorrect ? "default" : "outline"}
                  size="icon"
                  onClick={() => toggleCorrect(option.id)}
                >
                  <CheckIcon />
                </Button>
                <Input
                  aria-label={`Antwort ${optionIndex + 1} für Eingabe ${inputIndex + 1}`}
                  value={option.text}
                  onChange={(e) => onChange({
                    ...input,
                    options: input.options.map((entry) =>
                      entry.id === option.id ? { ...entry, text: e.target.value } : entry),
                  })}
                />
                <Button
                  aria-label={`Antwort ${optionIndex + 1} löschen`}
                  variant="ghost"
                  size="icon"
                  disabled={input.options.length <= MIN_CHOICE_OPTIONS}
                  onClick={() => onChange({
                    ...input,
                    options: input.options.filter((entry) => entry.id !== option.id),
                    correctOptionIds: input.correctOptionIds.filter((id) => id !== option.id),
                  })}
                >
                  <TrashIcon />
                </Button>
              </div>
            );
          })}
        </div>
        <Button
          variant="outline"
          className="lg:max-w-40"
          disabled={input.options.length >= MAX_CHOICE_OPTIONS}
          onClick={() => onChange({
            ...input,
            options: [...input.options, { id: Util.generateUniqueId("opt"), text: "" }],
          })}
        >
          <PlusIcon />
          Antwort
        </Button>
        <ValidationFeedback issues={optionIssues} />
      </Field>
    </>
  );
}
