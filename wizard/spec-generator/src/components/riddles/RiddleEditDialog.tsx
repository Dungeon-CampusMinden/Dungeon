import type { AnyRiddle, DeerSchema } from "@/data/DeerSchema";
import React from "react";
import { ChevronDownIcon, TrashIcon } from "lucide-react";
import { Button } from "../ui/button";
import { Dialog, DialogClose, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "../ui/dialog";
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "../ui/dropdown-menu";
import { Field, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { Separator } from "../ui/separator";
import { Slider } from "../ui/slider";
import { Textarea } from "../ui/textarea";
import { RiddleParametersEditor } from "./RiddleParametersEditor";
import { HintListEditor, ResourceListEditor } from "./RiddleSubEditors";
import { convertRiddleType, RIDDLE_DIFFICULTIES, RIDDLE_TYPES } from "./riddleTypes";
import { SimpleSelect } from "./SimpleSelect";

export function RiddleEditDialog({
  riddle,
  deerSchema,
  open,
  setOpen,
  onSave,
  onDelete,
}: {
  riddle: AnyRiddle;
  deerSchema: DeerSchema;
  open: boolean;
  setOpen: (open: boolean) => void;
  onSave: (updated: AnyRiddle) => void;
  onDelete: () => void;
}) {
  const [draft, setDraft] = React.useState<AnyRiddle>(riddle);

  const handleOpenChange = (nextOpen: boolean) => {
    if (nextOpen) {
      setDraft(JSON.parse(JSON.stringify(riddle)) as AnyRiddle);
    }
    setOpen(nextOpen);
  };

  const handleSave = () => {
    onSave(draft);
    setOpen(false);
  };

  const toggleObjective = (objectiveId: string) => {
    const selected = draft.learningObjectiveIds.includes(objectiveId);
    setDraft({
      ...draft,
      learningObjectiveIds: selected
        ? draft.learningObjectiveIds.filter((id) => id !== objectiveId)
        : [...draft.learningObjectiveIds, objectiveId],
    });
  };

  const objectives = deerSchema.learningDesign.objectives;
  const selectedObjectives = objectives.filter((objective) =>
    draft.learningObjectiveIds.includes(objective.id),
  );
  const selectedObjectivesLabel =
    selectedObjectives.length === 0
      ? "Keine Lernziele ausgewählt"
      : selectedObjectives.map((objective) => objective.description || objective.id).join(", ");

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>Rätsel '{draft.title}' bearbeiten</DialogTitle>
        </DialogHeader>

        <div className="flex max-h-[65vh] flex-col gap-4 overflow-y-auto pr-1">
          <Field>
            <FieldLabel>Titel</FieldLabel>
            <Input value={draft.title} onChange={(e) => setDraft({ ...draft, title: e.target.value })} />
          </Field>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field>
              <FieldLabel>Art des Rätsels</FieldLabel>
              <SimpleSelect
                options={RIDDLE_TYPES.map((type) => ({ value: type.value, label: type.label }))}
                value={draft.type}
                onChange={(newValue) => setDraft(convertRiddleType(draft, newValue as AnyRiddle["type"]))}
              />
            </Field>
            <Field>
              <FieldLabel>Schwierigkeit</FieldLabel>
              <SimpleSelect
                options={RIDDLE_DIFFICULTIES.map((item) => ({ value: item.value, label: item.label }))}
                value={draft.difficulty}
                onChange={(newValue) =>
                  setDraft({ ...draft, difficulty: newValue as AnyRiddle["difficulty"] })
                }
              />
            </Field>
          </div>

          <Field>
            <FieldLabel>Geschätzte Dauer: {draft.estimatedMinutes} Minuten</FieldLabel>
            <Slider
              value={draft.estimatedMinutes}
              onValueChange={(value) => setDraft({ ...draft, estimatedMinutes: value as number })}
              min={1}
              max={60}
              step={1}
            />
          </Field>

          <Field>
            <FieldLabel>Aufgabe für die Spieler</FieldLabel>
            <Textarea
              value={draft.playerFacingTask}
              onChange={(e) => setDraft({ ...draft, playerFacingTask: e.target.value })}
            />
          </Field>

          <Field>
            <FieldLabel>
              Lernziele <span className="text-xs text-muted-foreground">(Mehrfachauswahl)</span>
            </FieldLabel>
            {objectives.length === 0 ? (
              <span className="text-sm text-muted-foreground">Es sind noch keine Lernziele definiert.</span>
            ) : (
              <DropdownMenu>
                <DropdownMenuTrigger
                  render={
                    <Button variant="outline" className="w-full justify-between font-normal">
                      <span className="truncate">{selectedObjectivesLabel}</span>
                      <ChevronDownIcon />
                    </Button>
                  }
                />
                <DropdownMenuContent align="start" className="max-w-[min(32rem,var(--available-width))]">
                  {objectives.map((objective) => (
                    <DropdownMenuCheckboxItem
                      key={objective.id}
                      checked={draft.learningObjectiveIds.includes(objective.id)}
                      onCheckedChange={() => toggleObjective(objective.id)}
                      closeOnClick={false}
                    >
                      <span className="whitespace-normal">{objective.description || objective.id}</span>
                    </DropdownMenuCheckboxItem>
                  ))}
                </DropdownMenuContent>
              </DropdownMenu>
            )}
          </Field>

          <Separator />

          {draft.type === "collection" && (
            <>
              <Field>
                <FieldLabel>Material</FieldLabel>
                <ResourceListEditor
                  resources={draft.resources}
                  assets={deerSchema.assets}
                  onChange={(updated) => setDraft({ ...draft, resources: updated })}
                />
              </Field>
              <Separator />
            </>
          )}

          <Field>
            <FieldLabel>Hilfe</FieldLabel>
            <HintListEditor
              hints={draft.hints}
              onChange={(updated) => setDraft({ ...draft, hints: updated })}
            />
          </Field>

          <Separator />

          <Field>
            <FieldLabel>Einstellungen</FieldLabel>
            <RiddleParametersEditor riddle={draft} setRiddle={setDraft} deerSchema={deerSchema} />
          </Field>
        </div>

        <DialogFooter className="sm:justify-between">
          <Button
            variant="destructive"
            onClick={() => {
              onDelete();
              setOpen(false);
            }}
          >
            <TrashIcon />
            Löschen
          </Button>
          <div className="flex gap-2">
            <DialogClose render={<Button variant="outline" />}>Abbrechen</DialogClose>
            <Button onClick={handleSave}>Speichern</Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
