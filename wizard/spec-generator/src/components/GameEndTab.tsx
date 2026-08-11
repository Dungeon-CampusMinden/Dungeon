import type { DeerSchema } from "@/data/DeerSchema";
import { Field, FieldGroup, FieldLabel, FieldSet } from "./ui/field";
import { StringListEditor } from "./StringListEditor";

export function GameEndTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  return (
    <div className="flex flex-col gap-0">
      <h1>Spiel-Ende</h1>
      <FieldSet>
        <FieldGroup>
          <Field>
            <FieldLabel>Debrief Prompts</FieldLabel>
            <StringListEditor
              value={deerSchema.learningDesign.debriefPrompts}
              onChange={(newValue) => {
                deerSchema.learningDesign.debriefPrompts = newValue;
                updateDeerSchema(deerSchema);
              }}
              preventEmpty
            />
          </Field>
          <Field>
            <FieldLabel>Texte bei erfolgreichem Abschluss</FieldLabel>
            <StringListEditor
              value={deerSchema.scenario.successText}
              onChange={(newValue) => {
                deerSchema.scenario.successText = newValue;
                updateDeerSchema(deerSchema);
              }}
              useTextarea
              preventEmpty
            />
          </Field>
          <Field>
            <FieldLabel>Texte bei Misserfolg</FieldLabel>
            <StringListEditor
              value={deerSchema.scenario.failureText}
              onChange={(newValue) => {
                deerSchema.scenario.failureText = newValue;
                updateDeerSchema(deerSchema);
              }}
              useTextarea
              preventEmpty
            />
          </Field>
        </FieldGroup>
      </FieldSet>
    </div>
  );
}
