import type { DeerProject } from "@/data/DeerSchema";
import { Field, FieldGroup, FieldLabel, FieldSeparator, FieldSet } from "./ui/field";
import { Input } from "./ui/input";
import { StringListEditor } from "./StringListEditor";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import type { TabIssues } from "@/data/ErrorChecker";
import { fieldIssues, hasFieldErrors, ValidationFeedback } from "./ValidationFeedback";

const THEMES = [{ value: "default", label: "Dungeon" }];

export function ScenarioTab({
  deerSchema,
  updateDeerSchema,
  issues,
}: {
  deerSchema: DeerProject;
  updateDeerSchema: (updatedSchema: DeerProject) => void;
  issues: TabIssues;
}) {
  const emptyMission = hasFieldErrors(issues, "mission");

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
              <SelectTrigger aria-label="Thema der Geschichte" aria-invalid={hasFieldErrors(issues, "themeId")} className="w-[180px]">
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
            <ValidationFeedback issues={fieldIssues(issues, "themeId")} />
          </Field>
          <Field>
            <FieldLabel>Mission</FieldLabel>
            <Input
              aria-label="Mission der Geschichte"
              value={deerSchema.scenario.mission}
              onChange={(e) => {
                deerSchema.scenario.mission = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyMission}
            />
            <ValidationFeedback issues={fieldIssues(issues, "mission")} />
          </Field>
        </FieldGroup>
        <FieldSeparator />
        <FieldGroup>
          <Field>
            <FieldLabel>Intro Texte</FieldLabel>
            <StringListEditor
              itemNoun="Introtext"
              value={deerSchema.scenario.introText}
              onChange={(newValue) => {
                deerSchema.scenario.introText = newValue;
                updateDeerSchema(deerSchema);
              }}
              useTextarea
              preventEmpty
              issues={fieldIssues(issues, "introText")}
            />
          </Field>
        </FieldGroup>
      </FieldSet>
    </div>
  );
}
