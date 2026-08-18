import type { DeerProject, Riddle } from "@/data/DeerSchema";
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
import { RiddleInputsEditor } from "./RiddleInputsEditor";
import { HintListEditor, InformationSourceListEditor } from "./RiddleSubEditors";
import { RIDDLE_DIFFICULTIES } from "./riddleTypes";
import { SimpleSelect } from "./SimpleSelect";

export function RiddleEditDialog({
  riddle,
  deerSchema,
  open,
  setOpen,
  onSave,
  onDelete,
}: {
  riddle: Riddle;
  deerSchema: DeerProject;
  open: boolean;
  setOpen: (open: boolean) => void;
  onSave: (updated: Riddle) => void;
  onDelete: () => void;
}) {
  const [draft, setDraft] = React.useState<Riddle>(riddle);

  const handleOpenChange = (nextOpen: boolean) => {
    if (nextOpen) {
      setDraft(JSON.parse(JSON.stringify(riddle)) as Riddle);
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
      <DialogContent className="sm:max-w-2xl lg:max-w-4xl">
        <DialogHeader>
          <DialogTitle>Rätsel '{draft.title}' bearbeiten</DialogTitle>
        </DialogHeader>

        <div className="flex max-h-[65vh] flex-col gap-4 overflow-y-auto pr-1">
          <div className="flex flex-col gap-4">
            <Field>
              <FieldLabel>Titel</FieldLabel>
              <Input value={draft.title} onChange={(e) => setDraft({ ...draft, title: e.target.value })} />
            </Field>

            <Field>
              <FieldLabel>Schwierigkeit</FieldLabel>
              <SimpleSelect
                options={RIDDLE_DIFFICULTIES.map((item) => ({ value: item.value, label: item.label }))}
                value={draft.difficulty}
                onChange={(newValue) => setDraft({ ...draft, difficulty: newValue as Riddle["difficulty"] })}
              />
            </Field>

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
          </div>

          <div className="flex flex-col gap-4">
            <Separator />

            <Field>
              <FieldLabel>Informationsquellen</FieldLabel>
              <InformationSourceListEditor
                informationSources={draft.informationSources}
                deerSchema={deerSchema}
                onChange={(updated) => setDraft({ ...draft, informationSources: updated })}
              />
            </Field>

            <Field>
              <FieldLabel>Eingaben</FieldLabel>
              <RiddleInputsEditor riddle={draft} setRiddle={setDraft} deerSchema={deerSchema} />
            </Field>

            <Field>
              <FieldLabel>Hilfe</FieldLabel>
              <HintListEditor
                hints={draft.hints}
                onChange={(updated) => setDraft({ ...draft, hints: updated })}
              />
            </Field>
          </div>
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
