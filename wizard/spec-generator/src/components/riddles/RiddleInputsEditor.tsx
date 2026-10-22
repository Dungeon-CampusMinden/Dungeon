import { CheckIcon, PlusIcon, TrashIcon } from "lucide-react";

import type {
  AnyRiddleInput,
  AssignmentInput,
  ChoiceInput,
  CollectionInput,
  DeerProject,
  NumericInput,
  Riddle,
} from "@/data/DeerSchema";
import { ASSIGNMENT_SPRITES } from "@/data/DeerSchema";
import type { Issue, TabIssues } from "@/data/ErrorChecker";
import { addRiddleInput, convertRiddleInput, removeRiddleInput } from "@/data/RiddleGraphActions";
import { Util } from "@/data/Util";

import { getBundledAssetUrl } from "../assets/assetPaths";
import { Button } from "../ui/button";
import { Field, FieldDescription, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { ResponsiveChoice } from "../ui/responsive-choice";
import { Textarea } from "../ui/textarea";
import { fieldIssues, ValidationFeedback } from "../ValidationFeedback";
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
        <div key={input.id} className="flex flex-col gap-3 rounded-md border border-border p-3">
          <div className="grid grid-cols-[1fr_auto] items-end gap-2">
            <Field>
              <FieldLabel>Art der Eingabe</FieldLabel>
              <SimpleSelect
                accessibleLabel={`Art der Eingabe ${index + 1}`}
                options={INPUT_TYPES.map((type) => ({ value: type.value, label: type.label }))}
                value={input.type}
                onChange={(newValue) =>
                  updateProject((next, nextRiddle) =>
                    convertRiddleInput(
                      next,
                      nextRiddle,
                      input.id,
                      newValue as AnyRiddleInput["type"],
                    ),
                  )
                }
              />
            </Field>
            <Button
              aria-label={`Eingabe ${index + 1} löschen`}
              variant="destructive"
              size="icon"
              onClick={() => removeInput(index)}
            >
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
          ) : input.type === "assignment" ? (
            <AssignmentInputFields
              input={input}
              inputIndex={index}
              containerIssues={fieldIssues(
                issues,
                `riddle:${riddle.id}:input:${input.id}:containers`,
              )}
              itemIssues={fieldIssues(issues, `riddle:${riddle.id}:input:${input.id}:items`)}
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
        <span className="text-sm text-muted-foreground">
          Lege zuerst eine Informationsquelle an.
        </span>
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
          onChange={(e) =>
            onChange({ ...input, answer: e.target.value.replace(/[^0-9]/g, "").slice(0, 8) })
          }
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
  // Keeps the marked answers in the order the teacher sees them.
  const inOptionOrder = (ids: string[]) =>
    input.options.map((option) => option.id).filter((id) => ids.includes(id));
  const toggleCorrect = (optionId: string) => {
    const isCorrect = input.correctOptionIds.includes(optionId);
    const correctOptionIds = input.multipleSelection
      ? isCorrect
        ? input.correctOptionIds.filter((id) => id !== optionId)
        : inOptionOrder([...input.correctOptionIds, optionId])
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
        <FieldDescription>
          Bei „Ja“ müssen die Spieler alle richtigen Antworten auswählen.
        </FieldDescription>
        <ResponsiveChoice
          accessibleLabel={`Mehrere richtige Antworten für Eingabe ${inputIndex + 1}`}
          options={YES_NO}
          value={input.multipleSelection ? "true" : "false"}
          onChange={(newValue) =>
            onChange({
              ...input,
              multipleSelection: newValue === "true",
              correctOptionIds:
                newValue === "true"
                  ? input.correctOptionIds
                  : inOptionOrder(input.correctOptionIds).slice(0, 1),
            })
          }
        />
      </Field>
      <Field>
        <FieldLabel>Antworten</FieldLabel>
        <FieldDescription>
          Markiere {input.multipleSelection ? "alle richtigen Antworten" : "die richtige Antwort"}{" "}
          mit dem Haken.
        </FieldDescription>
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
                  onChange={(e) =>
                    onChange({
                      ...input,
                      options: input.options.map((entry) =>
                        entry.id === option.id ? { ...entry, text: e.target.value } : entry,
                      ),
                    })
                  }
                />
                <Button
                  aria-label={`Antwort ${optionIndex + 1} löschen`}
                  variant="ghost"
                  size="icon"
                  disabled={input.options.length <= MIN_CHOICE_OPTIONS}
                  onClick={() =>
                    onChange({
                      ...input,
                      options: input.options.filter((entry) => entry.id !== option.id),
                      correctOptionIds: input.correctOptionIds.filter((id) => id !== option.id),
                    })
                  }
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
          onClick={() =>
            onChange({
              ...input,
              options: [...input.options, { id: Util.generateUniqueId("opt"), text: "" }],
            })
          }
        >
          <PlusIcon />
          Antwort
        </Button>
        <ValidationFeedback issues={optionIssues} />
      </Field>
    </>
  );
}

const MIN_CONTAINERS = 1;
const MAX_CONTAINERS = 4;
const MIN_ITEMS = 2;
const MAX_ITEMS = 8;
const NO_CONTAINER = "none";

function AssignmentInputFields({
  input,
  inputIndex,
  onChange,
  containerIssues,
  itemIssues,
}: {
  input: AssignmentInput;
  inputIndex: number;
  onChange: (updated: AssignmentInput) => void;
  containerIssues: Issue[];
  itemIssues: Issue[];
}) {
  const targetOptions = [
    ...input.containers.map((container, index) => ({
      value: container.id,
      label: container.name || `Kiste ${index + 1}`,
    })),
    { value: NO_CONTAINER, label: "Keine Kiste" },
  ];
  const spriteOptions = ASSIGNMENT_SPRITES.map((sprite) => ({
    value: sprite.path,
    label: sprite.label,
  }));

  return (
    <>
      <Field>
        <FieldLabel>Kisten</FieldLabel>
        <FieldDescription>In diese Kisten sortieren die Spieler die Gegenstände.</FieldDescription>
        <div className="flex flex-col gap-2">
          {input.containers.map((container, containerIndex) => (
            <div key={container.id} className="grid grid-cols-[1fr_auto] items-center gap-2">
              <Input
                aria-label={`Name von Kiste ${containerIndex + 1} für Eingabe ${inputIndex + 1}`}
                value={container.name}
                onChange={(e) =>
                  onChange({
                    ...input,
                    containers: input.containers.map((entry) =>
                      entry.id === container.id ? { ...entry, name: e.target.value } : entry,
                    ),
                  })
                }
              />
              <Button
                aria-label={`Kiste ${containerIndex + 1} löschen`}
                variant="ghost"
                size="icon"
                disabled={input.containers.length <= MIN_CONTAINERS}
                onClick={() =>
                  onChange({
                    ...input,
                    containers: input.containers.filter((entry) => entry.id !== container.id),
                    items: input.items.map((item) =>
                      item.containerId === container.id
                        ? { ...item, containerId: undefined }
                        : item,
                    ),
                  })
                }
              >
                <TrashIcon />
              </Button>
            </div>
          ))}
        </div>
        <Button
          variant="outline"
          className="lg:max-w-40"
          disabled={input.containers.length >= MAX_CONTAINERS}
          onClick={() =>
            onChange({
              ...input,
              containers: [...input.containers, { id: Util.generateUniqueId("box"), name: "" }],
            })
          }
        >
          <PlusIcon />
          Kiste
        </Button>
        <ValidationFeedback issues={containerIssues} />
      </Field>
      <Field>
        <FieldLabel>Gegenstände</FieldLabel>
        <FieldDescription>
          Wähle für jeden Gegenstand ein Bild und die Kiste, in die er gehört.
        </FieldDescription>
        <div className="flex flex-col gap-2">
          {input.items.map((item, itemIndex) => (
            <div key={item.id} className="flex flex-wrap items-center gap-2">
              <img
                src={getBundledAssetUrl(item.sprite)}
                alt=""
                className="size-8 object-contain [image-rendering:pixelated]"
              />
              <SimpleSelect
                accessibleLabel={`Bild von Gegenstand ${itemIndex + 1} für Eingabe ${inputIndex + 1}`}
                className="w-36"
                options={spriteOptions}
                value={item.sprite}
                onChange={(newValue) =>
                  onChange({
                    ...input,
                    items: input.items.map((entry) =>
                      entry.id === item.id ? { ...entry, sprite: newValue } : entry,
                    ),
                  })
                }
              />
              <Input
                aria-label={`Name von Gegenstand ${itemIndex + 1} für Eingabe ${inputIndex + 1}`}
                className="min-w-40 flex-1"
                value={item.name}
                onChange={(e) =>
                  onChange({
                    ...input,
                    items: input.items.map((entry) =>
                      entry.id === item.id ? { ...entry, name: e.target.value } : entry,
                    ),
                  })
                }
              />
              <SimpleSelect
                accessibleLabel={`Kiste von Gegenstand ${itemIndex + 1} für Eingabe ${inputIndex + 1}`}
                className="w-40"
                options={targetOptions}
                value={item.containerId ?? NO_CONTAINER}
                onChange={(newValue) =>
                  onChange({
                    ...input,
                    items: input.items.map((entry) =>
                      entry.id === item.id
                        ? {
                            ...entry,
                            containerId: newValue === NO_CONTAINER ? undefined : newValue,
                          }
                        : entry,
                    ),
                  })
                }
              />
              <Button
                aria-label={`Gegenstand ${itemIndex + 1} löschen`}
                variant="ghost"
                size="icon"
                disabled={input.items.length <= MIN_ITEMS}
                onClick={() =>
                  onChange({ ...input, items: input.items.filter((entry) => entry.id !== item.id) })
                }
              >
                <TrashIcon />
              </Button>
            </div>
          ))}
        </div>
        <Button
          variant="outline"
          className="lg:max-w-40"
          disabled={input.items.length >= MAX_ITEMS}
          onClick={() =>
            onChange({
              ...input,
              items: [
                ...input.items,
                { id: Util.generateUniqueId("item"), name: "", sprite: ASSIGNMENT_SPRITES[0].path },
              ],
            })
          }
        >
          <PlusIcon />
          Gegenstand
        </Button>
        <ValidationFeedback issues={itemIssues} />
      </Field>
    </>
  );
}
