import type { DeerProject } from "@/data/DeerSchema";
import { Field, FieldGroup, FieldLabel, FieldSet } from "./ui/field";
import { Input } from "./ui/input";
import { StringListEditor } from "./StringListEditor";

export function GameEndTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerProject;
  updateDeerSchema: (updatedSchema: DeerProject) => void;
}) {
  const endNode = deerSchema.riddleGraph.nodes.find((node) => node.kind === "end");
  const exit = endNode?.kind === "end"
    ? deerSchema.surfaces.find((surface) => surface.id === endNode.surfaceId)
    : undefined;
  const failureTextRequired = deerSchema.session.time.limitMode === "hard";
  const failureText = deerSchema.scenario.failureText?.length
    ? deerSchema.scenario.failureText
    : failureTextRequired ? [""] : [];

  return (
    <div className="flex flex-col gap-0">
      <h1>Spiel-Ende</h1>
      <FieldSet>
        <FieldGroup>
          <Field>
            <FieldLabel>Name des Ausgangs</FieldLabel>
            <Input
              aria-label="Name des Ausgangs"
              value={exit?.title ?? ""}
              onChange={(event) => {
                if (!exit) return;
                exit.title = event.target.value;
                updateDeerSchema(deerSchema);
              }}
            />
          </Field>
          <Field>
            <FieldLabel>Fragen für die Nachbesprechung</FieldLabel>
            <StringListEditor
              itemNoun="Nachbesprechungsfrage"
              value={deerSchema.learningDesign.debriefPrompts}
              onChange={(newValue) => {
                deerSchema.learningDesign.debriefPrompts = newValue;
                updateDeerSchema(deerSchema);
              }}
            />
          </Field>
          <Field>
            <FieldLabel>Texte bei erfolgreichem Abschluss</FieldLabel>
            <StringListEditor
              itemNoun="Erfolgstext"
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
              itemNoun="Misserfolgstext"
              value={failureText}
              onChange={(newValue) => {
                if (newValue.length === 0) {
                  delete deerSchema.scenario.failureText;
                } else {
                  deerSchema.scenario.failureText = newValue;
                }
                updateDeerSchema(deerSchema);
              }}
              useTextarea
              preventEmpty={failureTextRequired}
            />
          </Field>
        </FieldGroup>
      </FieldSet>
    </div>
  );
}
