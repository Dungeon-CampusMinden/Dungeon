import type { DeerProject, Riddle } from "@/data/DeerSchema";
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
import type { TabIssues } from "@/data/ErrorChecker";
import { fieldIssues, ValidationFeedback } from "../ValidationFeedback";
import { ResponsiveChoice } from "../ui/responsive-choice";

export function RiddleEditDialog({
  riddle,
  deerSchema,
  open,
  setOpen,
  onChange,
  onDelete,
  tabIssues,
}: {
  riddle: Riddle;
  deerSchema: DeerProject;
  open: boolean;
  setOpen: (open: boolean) => void;
  onChange: (updated: DeerProject) => void;
  onDelete: () => void;
  tabIssues: TabIssues;
}) {
  const draft = deerSchema.riddles.find((candidate) => candidate.id === riddle.id) ?? riddle;
  const riddleIssues = fieldIssues(tabIssues, `riddle:${riddle.id}`);

  const updateRiddle = (updated: Riddle) => {
    const next = structuredClone(deerSchema);
    const index = next.riddles.findIndex((candidate) => candidate.id === updated.id);
    if (index !== -1) next.riddles[index] = updated;
    onChange(next);
  };

  const toggleObjective = (objectiveId: string) => {
    const selected = draft.learningObjectiveIds.includes(objectiveId);
    updateRiddle({
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
      : selectedObjectives.map((objective) => objective.description.trim() || "Unbenanntes Lernziel").join(", ");

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent className="sm:max-w-2xl lg:max-w-4xl">
        <DialogHeader>
          <DialogTitle>Rätsel „{draft.title.trim() || "Unbenanntes Rätsel"}“ bearbeiten</DialogTitle>
        </DialogHeader>

        <div className="flex max-h-[65vh] flex-col gap-4 overflow-y-auto pr-1">
          <ValidationFeedback issues={riddleIssues} />
          <div className="flex flex-col gap-4">
            <Field>
              <FieldLabel>Titel</FieldLabel>
              <Input aria-label="Titel des Rätsels" value={draft.title} onChange={(e) => updateRiddle({ ...draft, title: e.target.value })} />
            </Field>

            <Field>
              <FieldLabel>Schwierigkeit</FieldLabel>
              <ResponsiveChoice
                accessibleLabel="Schwierigkeit des Rätsels"
                options={RIDDLE_DIFFICULTIES.map((item) => ({ value: item.value, label: item.label }))}
                value={draft.difficulty}
                onChange={(newValue) => updateRiddle({ ...draft, difficulty: newValue as Riddle["difficulty"] })}
              />
            </Field>

            <Field>
              <FieldLabel>Geschätzte Dauer: {draft.estimatedMinutes} Minuten</FieldLabel>
              <Slider
                aria-label="Geschätzte Dauer des Rätsels in Minuten"
                value={draft.estimatedMinutes}
                onValueChange={(value) => updateRiddle({ ...draft, estimatedMinutes: value as number })}
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
                      <Button aria-label="Lernziele des Rätsels auswählen" variant="outline" className="w-full justify-between font-normal">
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
                        <span className="whitespace-normal">
                          {objective.description.trim() || "Unbenanntes Lernziel"}
                        </span>
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
                project={deerSchema}
                riddle={draft}
                onChange={onChange}
              />
            </Field>

            <Field>
              <FieldLabel>Eingaben</FieldLabel>
              <RiddleInputsEditor project={deerSchema} riddle={draft} onChange={onChange} issues={tabIssues} />
            </Field>

            <Field>
              <FieldLabel>Hilfe</FieldLabel>
              <HintListEditor
                hints={draft.hints}
                onChange={(updated) => updateRiddle({ ...draft, hints: updated })}
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
          <DialogClose render={<Button />}>Fertig</DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
