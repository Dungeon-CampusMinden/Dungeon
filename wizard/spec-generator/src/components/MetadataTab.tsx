import type { DeerProject } from "@/data/DeerSchema";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
  FieldLegend,
  FieldSeparator,
  FieldSet,
} from "./ui/field";
import { Input } from "./ui/input";
import { Textarea } from "./ui/textarea";
import { Util } from "@/data/Util";
import { ObjectListStringEditor } from "./StringListEditor";
import type { TabIssues } from "@/data/ErrorChecker";
import { fieldIssues, hasFieldErrors, ValidationFeedback } from "./ValidationFeedback";

export function MetadataTab({
  deerSchema,
  updateDeerSchema,
  issues,
}: {
  deerSchema: DeerProject;
  updateDeerSchema: (updatedSchema: DeerProject) => void;
  issues: TabIssues;
}) {
  const emptyTitle = hasFieldErrors(issues, "title");
  const invalidOperatorEmail = hasFieldErrors(issues, "operatorEmail");

  return (
    <div className="flex flex-col gap-5">
      <h1 className="wizard-page-title">Eckdaten & Lernziele</h1>
      <FieldSet>
        <FieldLegend>Eckdaten</FieldLegend>
        <FieldGroup>
          <Field>
            <FieldLabel>Titel</FieldLabel>
            <Input
              aria-label="Titel des Spiels"
              value={deerSchema.metadata.title}
              onChange={(e) => {
                deerSchema.metadata.title = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyTitle}
            />
            {emptyTitle && <FieldError>Der Titel darf nicht leer sein.</FieldError>}
          </Field>
          <Field>
            <FieldLabel>Beschreibung</FieldLabel>
            <Textarea
              aria-label="Beschreibung des Spiels"
              value={deerSchema.metadata.description ?? ""}
              onChange={(e) => {
                deerSchema.metadata.description = e.target.value;
                updateDeerSchema(deerSchema);
              }}
            />
          </Field>
          <Field>
            <FieldLabel>Autor</FieldLabel>
            <Input
              aria-label="Autor des Spiels"
              value={deerSchema.metadata.author ?? ""}
              onChange={(e) => {
                deerSchema.metadata.author = e.target.value;
                updateDeerSchema(deerSchema);
              }}
            />
          </Field>
          <Field>
            <FieldLabel>E-Mail-Adresse für Tracking-Hinweise (optional)</FieldLabel>
            <Input
              type="email"
              aria-label="E-Mail-Adresse für Tracking-Hinweise"
              value={deerSchema.metadata.operatorEmail ?? ""}
              onChange={(e) => {
                deerSchema.metadata.operatorEmail = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={invalidOperatorEmail}
            />
            {invalidOperatorEmail && (
              <FieldError>Die E-Mail-Adresse für Tracking-Hinweise ist ungültig.</FieldError>
            )}
            <FieldDescription>
              Leer lassen, um tracking@example.com zu verwenden.
            </FieldDescription>
          </Field>
        </FieldGroup>
        <FieldSeparator />
        <FieldGroup>
          <Field>
            <FieldLabel>Lernziele</FieldLabel>
            <ObjectListStringEditor
              itemNoun="Lernziel"
              value={deerSchema.learningDesign.objectives}
              onChange={(newValue) => {
                deerSchema.learningDesign.objectives = newValue;
                updateDeerSchema(deerSchema);
              }}
              getItemText={(item) => item.description}
              setItemText={(item, text) => {
                item.description = text;
              }}
              produceItem={() => ({ id: Util.generateUniqueId(), description: "" })}
              useTextarea
              preventEmpty
              issues={fieldIssues(issues, "objectives")}
            />
          </Field>
        </FieldGroup>
      </FieldSet>
      <ValidationFeedback issues={[...fieldIssues(issues, "id"), ...fieldIssues(issues, "locale")]} className="mt-4" />
    </div>
  );
}
