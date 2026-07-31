import type { AnyRiddle, DeerSchema } from "@/data/DeerSchema";
import React from "react";
import { PlusIcon } from "lucide-react";
import { RiddleCard } from "./riddles/RiddleCard";
import { RiddleEditDialog } from "./riddles/RiddleEditDialog";
import { createRiddle } from "./riddles/riddleTypes";
import { Button } from "./ui/button";

export function RiddlesTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  const riddles = deerSchema.riddles;
  const [editingId, setEditingId] = React.useState<string | null>(null);

  const editingIndex = riddles.findIndex((riddle) => riddle.id === editingId);
  const editingRiddle = editingIndex >= 0 ? riddles[editingIndex] : null;

  const addRiddle = () => {
    const newRiddle = createRiddle("collection");
    riddles.push(newRiddle);
    updateDeerSchema(deerSchema);
    setEditingId(newRiddle.id);
  };

  const saveRiddle = (index: number, updated: AnyRiddle) => {
    riddles[index] = updated;
    updateDeerSchema(deerSchema);
  };

  const deleteRiddle = (index: number) => {
    riddles.splice(index, 1);
    updateDeerSchema(deerSchema);
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
          onSave={(updated) => saveRiddle(editingIndex, updated)}
          onDelete={() => deleteRiddle(editingIndex)}
        />
      )}
    </div>
  );
}
