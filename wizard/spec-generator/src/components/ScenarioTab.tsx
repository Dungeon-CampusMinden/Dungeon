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
  const emptyPlayerRole = deerSchema.scenario.playerRole === "";
  const emptyPremise = deerSchema.scenario.premise === "";
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
            <FieldLabel>Spielerrolle</FieldLabel>
            <Input
              value={deerSchema.scenario.playerRole}
              onChange={(e) => {
                deerSchema.scenario.playerRole = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyPlayerRole}
            />
            {emptyPlayerRole && <FieldError>Die Spielerrolle darf nicht leer sein.</FieldError>}
          </Field>
          <Field>
            <FieldLabel>Premisse</FieldLabel>
            <Input
              value={deerSchema.scenario.premise}
              onChange={(e) => {
                deerSchema.scenario.premise = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyPremise}
            />
            {emptyPremise && <FieldError>Die Premisse darf nicht leer sein.</FieldError>}
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
              value={deerSchema.scenario.introTexts}
              onChange={(newValue) => {
                deerSchema.scenario.introTexts = newValue;
                updateDeerSchema(deerSchema);
              }}
              useTextarea
              preventEmpty
            />
          </Field>
          <Field>
            <FieldLabel>Texte bei erfolgreichem Abschluss</FieldLabel>
            <StringListEditor
              value={deerSchema.scenario.successTexts}
              onChange={(newValue) => {
                deerSchema.scenario.successTexts = newValue;
                updateDeerSchema(deerSchema);
              }}
              useTextarea
              preventEmpty
            />
          </Field>
          <Field>
            <FieldLabel>Texte bei Misserfolg</FieldLabel>
            <StringListEditor
              value={deerSchema.scenario.failureTexts}
              onChange={(newValue) => {
                deerSchema.scenario.failureTexts = newValue;
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
