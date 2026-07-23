import type { DeerSchema } from "@/data/DeerSchema";
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
  FieldLegend,
  FieldSeparator,
  FieldSet,
} from "./ui/field";
import { Input } from "./ui/input";
import { Textarea } from "./ui/textarea";

export function MetadataTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
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
                updateDeerSchema(deerSchema);
              }}
            />
          </Field>
          <Field>
            <FieldLabel>Beschreibung</FieldLabel>
            <Textarea
              value={deerSchema.metadata.description}
              onChange={(e) => {
                deerSchema.metadata.description = e.target.value;
                updateDeerSchema(deerSchema);
              }}
            />
          </Field>
          <Field>
            <FieldLabel>Autor</FieldLabel>
            <Input
              value={deerSchema.metadata.author}
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
          </Field>
          <Field>
            <FieldLabel>Debrief Prompts</FieldLabel>
          </Field>
        </FieldGroup>
      </FieldSet>
    </div>
  );
}
