import type { DeerProject } from "@/data/DeerSchema";
import { Field, FieldGroup, FieldLabel, FieldSet } from "./ui/field";
import { Input } from "./ui/input";
import { StringListEditor } from "./StringListEditor";
import type { TabIssues } from "@/data/ErrorChecker";
import { fieldIssues, ValidationFeedback } from "./ValidationFeedback";

export function GameEndTab({
  deerSchema,
  updateDeerSchema,
  issues,
}: {
  deerSchema: DeerProject;
  updateDeerSchema: (updatedSchema: DeerProject) => void;
  issues: TabIssues;
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
            <ValidationFeedback issues={fieldIssues(issues, "exit")} />
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
              issues={fieldIssues(issues, "debriefPrompts")}
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
              issues={fieldIssues(issues, "successText")}
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
              issues={fieldIssues(issues, "failureText")}
            />
          </Field>
        </FieldGroup>
      </FieldSet>
    </div>
  );
}
