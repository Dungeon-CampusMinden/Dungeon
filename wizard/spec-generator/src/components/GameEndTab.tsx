import type { DeerProject } from "@/data/DeerSchema";
import type { TabIssues } from "@/data/ErrorChecker";

import { StringListEditor } from "./StringListEditor";
import { Field, FieldGroup, FieldLabel, FieldSet } from "./ui/field";
import { Input } from "./ui/input";
import { fieldIssues, ValidationFeedback } from "./ValidationFeedback";

/** Returns the surface that the end node of the riddle graph uses as exit. */
function findExit(project: DeerProject) {
  const endNode = project.riddleGraph.nodes.find((node) => node.kind === "end");
  return endNode?.kind === "end"
    ? project.surfaces.find((surface) => surface.id === endNode.surfaceId)
    : undefined;
}

export function GameEndTab({
  deerSchema,
  updateDeerSchema,
  issues,
}: {
  deerSchema: DeerProject;
  updateDeerSchema: (transform: (project: DeerProject) => void) => void;
  issues: TabIssues;
}) {
  const exit = findExit(deerSchema);
  const failureTextRequired = deerSchema.session.time.limitMode === "hard";
  const failureText = deerSchema.scenario.failureText?.length
    ? deerSchema.scenario.failureText
    : failureTextRequired
      ? [""]
      : [];

  return (
    <div className="flex flex-col gap-5">
      <h1 className="wizard-page-title">Spiel-Ende</h1>
      <FieldSet>
        <FieldGroup>
          <Field>
            <FieldLabel>Name des Ausgangs</FieldLabel>
            <Input
              aria-label="Name des Ausgangs"
              value={exit?.title ?? ""}
              onChange={(event) => {
                if (!exit) return;
                updateDeerSchema((project) => {
                  const target = findExit(project);
                  if (target) target.title = event.target.value;
                });
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
                updateDeerSchema((project) => {
                  project.learningDesign.debriefPrompts = newValue;
                });
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
                updateDeerSchema((project) => {
                  project.scenario.successText = newValue;
                });
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
                updateDeerSchema((project) => {
                  if (newValue.length === 0) {
                    delete project.scenario.failureText;
                  } else {
                    project.scenario.failureText = newValue;
                  }
                });
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
