import type { DeerSchema } from "@/data/DeerSchema";
import { Field, FieldError, FieldGroup, FieldLabel, FieldSeparator, FieldSet } from "./ui/field";
import { Input } from "./ui/input";
import { StringListEditor } from "./StringListEditor";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Slider } from "./ui/slider";

const THEMES = [{ value: "default", label: "Dungeon" }];

export function SessionTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  const emptyTargetAudience = deerSchema.session.targetAudience === "";
  const emptyPriorKnowledge = deerSchema.session.priorKnowledge === "";

  const minPlayer = deerSchema.session.playerCount.min;
  const maxPlayer = deerSchema.session.playerCount.max;
  const timeLimit = deerSchema.session.time.limitMinutes;

  return (
    <div className="flex flex-col gap-0">
      <h1>Spielablauf</h1>
      <FieldSet>
        <FieldGroup>
          <Field>
            <FieldLabel>Zielgruppe</FieldLabel>
            <Input
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
                value={[minPlayer, maxPlayer]}
                onValueChange={(value) => {
                  const [min, max] = value as [number, number];
                  deerSchema.session.playerCount.min = min;
                  deerSchema.session.playerCount.max = max;
                  updateDeerSchema(deerSchema);
                }}
                min={1}
                max={10}
                step={1}
              />
            </div>
          </Field>
          <Field>
            <FieldLabel>Zeitlimit</FieldLabel>
            <div className="grid grid-cols-[150px_1fr] items-center gap-2 text-sm text-muted-foreground">
              <span className="text-end">{timeLimit} Minuten</span>
              <Slider
                value={timeLimit}
                onValueChange={(value) => {
                  deerSchema.session.time.limitMinutes = value as number;
                  updateDeerSchema(deerSchema);
                }}
                min={5}
                max={180}
                step={1}
              />
            </div>
          </Field>
        </FieldGroup>
      </FieldSet>
    </div>
  );
}
