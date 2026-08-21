import type { DeerProject, TimeLimitMode } from "@/data/DeerSchema";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
  FieldSeparator,
  FieldSet,
} from "./ui/field";
import { Input } from "./ui/input";
import { Slider } from "./ui/slider";
import { ResponsiveChoice } from "./ui/responsive-choice";
import type { TabIssues } from "@/data/ErrorChecker";
import { fieldIssues, hasFieldErrors, ValidationFeedback } from "./ValidationFeedback";

const LIMIT_MODES: { value: TimeLimitMode; label: string; description: string }[] = [
  { value: "hard", label: "Hart", description: "Das Abenteuer endet, wenn die Zeit abgelaufen ist." },
  { value: "soft", label: "Weich", description: "Die Zeit dient nur als Orientierung." },
];

export function SessionTab({
  deerSchema,
  updateDeerSchema,
  issues,
}: {
  deerSchema: DeerProject;
  updateDeerSchema: (updatedSchema: DeerProject) => void;
  issues: TabIssues;
}) {
  const emptyTargetAudience = hasFieldErrors(issues, "targetAudience");
  const emptyPriorKnowledge = hasFieldErrors(issues, "priorKnowledge");

  const minPlayer = deerSchema.session.playerCount.min;
  const maxPlayer = deerSchema.session.playerCount.max;
  const timeLimit = deerSchema.session.time.limitMinutes;
  const selectedLimitMode = LIMIT_MODES.find((mode) => mode.value === deerSchema.session.time.limitMode);

  return (
    <div className="flex flex-col gap-0">
      <h1>Spielablauf</h1>
      <FieldSet>
        <FieldGroup>
          <Field>
            <FieldLabel>Zielgruppe</FieldLabel>
            <Input
              aria-label="Zielgruppe"
              value={deerSchema.session.targetAudience}
              onChange={(e) => {
                deerSchema.session.targetAudience = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyTargetAudience}
            />
            {emptyTargetAudience && <FieldError>Die Zielgruppe darf nicht leer sein.</FieldError>}
          </Field>
          <Field>
            <FieldLabel>Vorkenntnisse</FieldLabel>
            <Input
              aria-label="Vorkenntnisse"
              value={deerSchema.session.priorKnowledge}
              onChange={(e) => {
                deerSchema.session.priorKnowledge = e.target.value;
                updateDeerSchema(deerSchema);
              }}
              aria-invalid={emptyPriorKnowledge}
            />
            {emptyPriorKnowledge && <FieldError>Die Vorkenntnisse dürfen nicht leer sein.</FieldError>}
          </Field>
        </FieldGroup>
        <FieldSeparator />
        <FieldGroup>
          <Field>
            <FieldLabel>Spieleranzahl</FieldLabel>
            <div className="grid grid-cols-[150px_1fr] items-center gap-2 text-sm text-muted-foreground">
              <div className="flex flex-col gap-0 items-end">
                <span>Mindestens: {minPlayer} Spieler</span>
                <span>Maximal: {maxPlayer} Spieler</span>
              </div>
              <Slider
                thumbLabels={["Minimale Spieleranzahl", "Maximale Spieleranzahl"]}
                value={[minPlayer, maxPlayer]}
                onValueChange={(value) => {
                  const [min, max] = value as [number, number];
                  deerSchema.session.playerCount.min = min;
                  deerSchema.session.playerCount.max = max;
                  updateDeerSchema(deerSchema);
                }}
                min={1}
                max={4}
                step={1}
              />
            </div>
            <ValidationFeedback issues={fieldIssues(issues, "playerCount")} />
          </Field>
          <Field>
            <FieldLabel>Zeitlimit</FieldLabel>
            <div className="grid grid-cols-[150px_1fr] items-center gap-2 text-sm text-muted-foreground">
              <span className="text-end">{timeLimit} Minuten</span>
              <Slider
                thumbLabels={["Zeitlimit in Minuten"]}
                value={timeLimit}
                onValueChange={(value) => {
                  deerSchema.session.time.limitMinutes = value as number;
                  updateDeerSchema(deerSchema);
                }}
                min={5}
                max={240}
                step={1}
              />
            </div>
            <ValidationFeedback issues={fieldIssues(issues, "time")} />
          </Field>
          <Field>
            <FieldLabel>Umgang mit dem Zeitlimit</FieldLabel>
            <ResponsiveChoice
              accessibleLabel="Umgang mit dem Zeitlimit"
              options={LIMIT_MODES}
              value={deerSchema.session.time.limitMode}
              onChange={(newValue) => {
                deerSchema.session.time.limitMode = newValue;
                updateDeerSchema(deerSchema);
              }}
            />
            <FieldDescription>{selectedLimitMode?.description}</FieldDescription>
          </Field>
        </FieldGroup>
      </FieldSet>
    </div>
  );
}
