import type { DeerProject, InformationSource, Riddle, RiddleHint } from "@/data/DeerSchema";
import { ClockIcon, InfoIcon, PencilIcon } from "lucide-react";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { Card, CardContent } from "../ui/card";
import { Separator } from "../ui/separator";
import { Tooltip, TooltipContent, TooltipTrigger } from "../ui/tooltip";
import { ResourceCarousel } from "./ResourceCarousel";
import { RiddleInputsView } from "./RiddleInputsView";
import { getHintSeverity, getHintSeverityOrder, getRiddleDifficulty } from "./riddleTypes";
import type { Issue } from "@/data/ErrorChecker";
import { ValidationFeedback } from "../ValidationFeedback";

export function RiddleCard({
  riddle,
  deerSchema,
  onEdit,
  issues = [],
}: {
  riddle: Riddle;
  deerSchema: DeerProject;
  onEdit: () => void;
  issues?: Issue[];
}) {
  const difficulty = getRiddleDifficulty(riddle.difficulty);
  const assignedSourceIds = new Set(
    riddle.inputs.filter((input) => input.type === "collection").map((input) => input.informationSourceId),
  );
  const unassignedSources = riddle.informationSources.filter((source) => !assignedSourceIds.has(source.id));

  return (
    <Card size="sm" className="h-full">
      <CardContent className="flex flex-col gap-3">
        <div className="relative flex items-center justify-center">
          <h3 className="m-0 text-center text-base font-medium">{riddle.title}</h3>
          <Button
            variant="ghost"
            size="icon"
            className="absolute right-0 top-1/2 -translate-y-1/2"
            aria-label="Rätsel bearbeiten"
            onClick={onEdit}
          >
            <PencilIcon />
          </Button>
        </div>

        <div className="flex flex-wrap items-center justify-center gap-2">
          <Badge className={difficulty?.className}>{difficulty?.label ?? riddle.difficulty}</Badge>
          <Badge variant="outline">
            <ClockIcon />
            {riddle.estimatedMinutes} Min.
          </Badge>
        </div>

        <Separator />

        <ValidationFeedback issues={issues} />

        <RiddleSection title="Eingaben">
          <RiddleInputsView riddle={riddle} deerSchema={deerSchema} />
        </RiddleSection>

        {unassignedSources.length > 0 && (
          <>
            <Separator />

            <RiddleSection title="Weiteres Material">
              <UnassignedSourceList sources={unassignedSources} deerSchema={deerSchema} />
            </RiddleSection>
          </>
        )}

        <Separator />

        <RiddleSection title="Hilfe">
          <HintList hints={riddle.hints} />
        </RiddleSection>
      </CardContent>
    </Card>
  );
}

function UnassignedSourceList({
  sources,
  deerSchema,
}: {
  sources: InformationSource[];
  deerSchema: DeerProject;
}) {
  return (
    <div className="flex flex-col gap-2">
      {sources.map((source) => (
        <div key={source.id} className="flex flex-col gap-1">
          <ResourceCarousel resources={source.resources} assets={deerSchema.assets} />
        </div>
      ))}
    </div>
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
        .sort((a, b) => getHintSeverityOrder(a.severity) - getHintSeverityOrder(b.severity))
        .map((hint) => (
          <li key={hint.id} className="flex items-center gap-2 text-sm">
            <Badge variant="outline" title="Stufe">
              {getHintSeverity(hint.severity)?.label ?? hint.severity}
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
