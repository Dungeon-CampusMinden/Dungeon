import type { DeerSchema } from "@/data/DeerSchema";
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
import { ObjectListStringEditor, StringListEditor } from "./StringListEditor";

export function MetadataTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  const emptyTitle = deerSchema.metadata.title === "";
  const emptyDescription = deerSchema.metadata.description === "";
  const emptyAuthor = deerSchema.metadata.author === "";

  return (
    <div className="flex flex-col gap-0">
      <h1>Eckdaten & Lernziele</h1>
      <FieldSet>
        <FieldLegend>Eckdaten</FieldLegend>
        <FieldGroup>
          <Field>
            <FieldLabel>Titel</FieldLabel>
            <Input
              value={deerSchema.metadata.title}
              onChange={(e) => {
                deerSchema.metadata.title = e.target.value;
                deerSchema.metadata.id = Util.generateUniqueId();
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyTitle}
            />
            {emptyTitle && <FieldError>Der Titel darf nicht leer sein.</FieldError>}
          </Field>
          <Field>
            <FieldLabel>Beschreibung</FieldLabel>
            <Textarea
              value={deerSchema.metadata.description}
              onChange={(e) => {
                deerSchema.metadata.description = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyDescription}
            />
            {emptyDescription && <FieldError>Die Beschreibung darf nicht leer sein.</FieldError>}
          </Field>
          <Field>
            <FieldLabel>Autor</FieldLabel>
            <Input
              value={deerSchema.metadata.author}
              onChange={(e) => {
                deerSchema.metadata.author = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyAuthor}
            />
            {emptyAuthor && <FieldError>Der Autor darf nicht leer sein.</FieldError>}
          </Field>
        </FieldGroup>
        <FieldSeparator />
        <FieldGroup>
          <Field>
            <FieldLabel>Lernziele</FieldLabel>
            <ObjectListStringEditor
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
        </FieldGroup>
      </FieldSet>
    </div>
  );
}
