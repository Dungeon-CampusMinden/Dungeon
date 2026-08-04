import type { DeerSchema } from "@/data/DeerSchema";
import { Field, FieldError, FieldGroup, FieldLabel, FieldSeparator, FieldSet } from "./ui/field";
import { Input } from "./ui/input";
import { StringListEditor } from "./StringListEditor";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "./ui/select";

const THEMES = [{ value: "default", label: "Dungeon" }];

export function ScenarioTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  const emptyMission = deerSchema.scenario.mission === "";

  const selectedTheme = THEMES.find((theme) => theme.value === deerSchema.scenario.themeId);

  return (
    <div className="flex flex-col gap-0">
      <h1>Geschichte</h1>
      <FieldSet>
        <FieldGroup>
          <Field>
            <FieldLabel>Thema</FieldLabel>
            <Select
              items={THEMES}
              value={deerSchema.scenario.themeId}
              onValueChange={(newValue) => {
                deerSchema.scenario.themeId = newValue ?? "";
                updateDeerSchema(deerSchema);
              }}
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="Wähle ein Thema" />
              </SelectTrigger>
              <SelectContent alignItemWithTrigger={false}>
                <SelectGroup>
                  {THEMES.map((item) => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
            {deerSchema.scenario.themeId === "" && (
              <FieldError>Es muss ein Thema ausgewählt werden.</FieldError>
            )}
          </Field>
          <Field>
            <FieldLabel>Mission</FieldLabel>
            <Input
              value={deerSchema.scenario.mission}
              onChange={(e) => {
                deerSchema.scenario.mission = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyMission}
            />
            {emptyMission && <FieldError>Die Mission darf nicht leer sein.</FieldError>}
          </Field>
        </FieldGroup>
        <FieldSeparator />
        <FieldGroup>
          <Field>
            <FieldLabel>Intro Texte</FieldLabel>
            <StringListEditor
              value={deerSchema.scenario.introText}
              onChange={(newValue) => {
                deerSchema.scenario.introText = newValue;
                updateDeerSchema(deerSchema);
              }}
              useTextarea
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
