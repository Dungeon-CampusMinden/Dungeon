import type { DeerProject } from "@/data/DeerSchema";
import type { UpdateDraft, WizardDraft } from "@/data/WizardDraft";
import { addRiddle as addRiddleToProject, removeRiddle } from "@/data/RiddleGraphActions";
import React from "react";
import { PlusIcon } from "lucide-react";
import { RiddleCard } from "./riddles/RiddleCard";
import { RiddleEditDialog } from "./riddles/RiddleEditDialog";
import { createRiddle } from "./riddles/riddleTypes";
import { Button } from "./ui/button";
import type { TabIssues } from "@/data/ErrorChecker";
import { fieldIssues, prefixedFieldIssues, ValidationFeedback } from "./ValidationFeedback";

export function RiddlesTab({
  draft,
  updateDraft,
  issues,
}: {
  draft: WizardDraft;
  updateDraft: UpdateDraft;
  issues: TabIssues;
}) {
  const deerSchema = draft.project;
  const riddles = deerSchema.riddles;
  const [editingId, setEditingId] = React.useState<string | null>(null);

  const editingIndex = riddles.findIndex((riddle) => riddle.id === editingId);
  const editingRiddle = editingIndex >= 0 ? riddles[editingIndex] : null;

  const addRiddle = () => {
    const newRiddle = createRiddle();
    updateDraft((current) => addRiddleToProject(current.project, newRiddle));
    setEditingId(newRiddle.id);
  };

  const updateRiddle = (updated: DeerProject) => {
    updateDraft((current) => {
      const edited = updated.riddles.find((riddle) => riddle.id === editingId);
      if (!edited) return false;
      const index = current.project.riddles.findIndex((riddle) => riddle.id === edited.id);
      if (index === -1) return false;
      current.project.riddles[index] = structuredClone(edited);
      current.project.surfaces = structuredClone(updated.surfaces);
    });
  };

  const deleteRiddle = (riddleId: string) => {
    updateDraft((current) => {
      const removedNodeIds = removeRiddle(current.project, riddleId);
      for (const nodeId of removedNodeIds) delete current.graphLayout[nodeId];
    });
  };

  return (
    <div className="flex flex-col gap-0">
      <h1>Rätsel</h1>
      <p className="text-sm text-muted-foreground">
        Hier kannst du die Rätsel definieren, die die Spieler in deinem Abenteuer lösen müssen.
      </p>
      <Button onClick={addRiddle} className="my-2 max-w-40">
        <PlusIcon />
        Hinzufügen
      </Button>
      <ValidationFeedback issues={fieldIssues(issues, "riddles")} className="mb-3" />

      {riddles.length === 0 && (
        <span className="text-sm text-muted-foreground">Es sind noch keine Rätsel vorhanden.</span>
      )}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        {riddles.map((riddle) => (
          <RiddleCard
            key={riddle.id}
            riddle={riddle}
            deerSchema={deerSchema}
            onEdit={() => setEditingId(riddle.id)}
            issues={prefixedFieldIssues(issues, `riddle:${riddle.id}`)}
          />
        ))}
      </div>

      {editingRiddle && (
        <RiddleEditDialog
          key={editingRiddle.id}
          riddle={editingRiddle}
          deerSchema={deerSchema}
          open={editingId !== null}
          setOpen={(open) => {
            if (!open) setEditingId(null);
          }}
          onChange={updateRiddle}
          onDelete={() => deleteRiddle(editingRiddle.id)}
          tabIssues={issues}
        />
      )}
    </div>
  );
}
