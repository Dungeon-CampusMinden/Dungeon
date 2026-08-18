import type { DeerProject } from "@/data/DeerSchema";
import {
  Field,
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

export function MetadataTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerProject;
  updateDeerSchema: (updatedSchema: DeerProject) => void;
}) {
  const emptyTitle = deerSchema.metadata.title === "";

  return (
    <div className="flex flex-col gap-0">
      <h1>Eckdaten & Lernziele</h1>
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
            />
          </Field>
        </FieldGroup>
      </FieldSet>
    </div>
  );
}
