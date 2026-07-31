import type { AnyRiddle, DeerSchema, RiddleHint } from "@/data/DeerSchema";
import { ClockIcon, InfoIcon, PencilIcon } from "lucide-react";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { Card, CardContent } from "../ui/card";
import { Separator } from "../ui/separator";
import { Tooltip, TooltipContent, TooltipTrigger } from "../ui/tooltip";
import { RiddleParametersView } from "./RiddleParametersView";
import { getRiddleDifficulty, getRiddleType, RiddleTypeIcon } from "./riddleTypes";

export function RiddleCard({
  riddle,
  deerSchema,
  onEdit,
}: {
  riddle: AnyRiddle;
  deerSchema: DeerSchema;
  onEdit: () => void;
}) {
  const riddleType = getRiddleType(riddle.type);
  const difficulty = getRiddleDifficulty(riddle.difficulty);

  return (
    <Card size="sm" className="h-full">
      <CardContent className="flex flex-col gap-3">
        <div className="relative flex items-center justify-center">
          <h3 className="m-0 text-center text-base font-medium">{riddle.title}</h3>
          <Button
            variant="ghost"
            size="icon"
            className="absolute right-0"
            aria-label="Rätsel bearbeiten"
            onClick={onEdit}
          >
            <PencilIcon />
          </Button>
        </div>

        <div className="flex items-center justify-center gap-2 text-muted-foreground">
          <RiddleTypeIcon type={riddle.type} size={22} />
          <span className="text-sm">{riddleType?.label ?? riddle.type}</span>
        </div>

        <div className="flex flex-wrap items-center justify-center gap-2">
          <Badge className={difficulty?.className}>{difficulty?.label ?? riddle.difficulty}</Badge>
          <Badge variant="outline">
            <ClockIcon />
            {riddle.estimatedMinutes} Min.
          </Badge>
        </div>

        <Separator />

        <RiddleSection title="Material">{/* TODO: Karussell mit Resource-Karten */}</RiddleSection>

        <Separator />

        <RiddleSection title="Hilfe">
          <HintList hints={riddle.hints} />
        </RiddleSection>

        <Separator />

        <RiddleSection title="Einstellungen">
          <RiddleParametersView riddle={riddle} deerSchema={deerSchema} />
        </RiddleSection>
      </CardContent>
    </Card>
  );
}

function RiddleSection({ title, children }: { title: string; children?: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs font-medium tracking-wide text-muted-foreground uppercase">{title}</span>
      {children}
    </div>
  );
}

function HintList({ hints }: { hints: RiddleHint[] }) {
  if (hints.length === 0) {
    return <span className="text-sm text-muted-foreground">Keine Hilfe hinterlegt.</span>;
  }
  return (
    <ul className="m-0 flex list-none flex-col gap-0 p-0">
      {[...hints]
        .sort((a, b) => a.severity - b.severity)
        .map((hint) => (
          <li key={hint.id} className="flex items-center gap-2 text-sm">
            <Badge variant="outline" title="Stufe">
              {hint.severity}
            </Badge>
            <span className="truncate">{hint.title}</span>
            <Tooltip>
              <TooltipTrigger
                delay={100}
                render={<button className="text-muted-foreground" aria-label={`Hilfetext: ${hint.title}`} />}
              >
                <InfoIcon size={16} />
              </TooltipTrigger>
              <TooltipContent>{hint.text || "Kein Text hinterlegt."}</TooltipContent>
            </Tooltip>
          </li>
        ))}
    </ul>
  );
}
