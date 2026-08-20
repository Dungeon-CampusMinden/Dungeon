import {
  ChevronRightIcon,
  CircleAlertIcon,
  CircleCheckIcon,
  CircleXIcon,
  InfoIcon,
} from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import type { IssueSeverity, LocatedIssue } from "@/data/ErrorChecker";
import { TABS } from "@/data/Tabs";

const SEVERITY_STYLES: Record<IssueSeverity, string> = {
  error: "border-destructive/30 bg-destructive/10 text-destructive",
  warning: "border-amber-500/30 bg-amber-500/10 text-amber-400",
  info: "border-blue-500/30 bg-blue-500/10 text-blue-400",
};

function SeverityIcon({ severity }: { severity: IssueSeverity }) {
  switch (severity) {
    case "error":
      return <CircleXIcon className="size-4 shrink-0 text-destructive" />;
    case "warning":
      return <CircleAlertIcon className="size-4 shrink-0 text-amber-400" />;
    default:
      return <InfoIcon className="size-4 shrink-0 text-blue-400" />;
  }
}

function teacherDescription(description: string): string {
  if (description.includes("internen Dateinamen")) {
    return "Zwei eigene Dateien stehen miteinander in Konflikt.";
  }
  return description
    .replace("Das Abenteuer hat keine Id.", "Das Abenteuer ist unvollständig.")
    .replace("Lernziele haben doppelte Ids.", "Ein Lernziel ist mehrfach angelegt.")
    .replace("Es gibt Rätsel mit doppelten Ids.", "Ein Rätsel ist mehrfach angelegt.")
    .replace("Informationsquellen mit doppelten Ids", "mehrfach angelegte Informationsquellen");
}

function teacherDetails(details: string | undefined): string | undefined {
  if (!details) return undefined;
  if (/\b(ids?|json|java|deer|hash|enum)\b/i.test(details)
    || /\([^)]*(world|container|keypad|door)[^)]*\)/i.test(details)
    || /(?:^|\s)\/(?:[^\s/]+\/)+/.test(details)) return undefined;
  return details;
}

/** Vertical list of all validation notices, most severe first. */
export function IssueList({
  issues,
  emptyMessage = "Alles in Ordnung.",
  onIssueSelect,
  navigationDisabled = false,
  className,
}: {
  issues: LocatedIssue[];
  emptyMessage?: string;
  onIssueSelect?: (tabId: string) => void;
  navigationDisabled?: boolean;
  className?: string;
}) {
  if (issues.length === 0) {
    return (
      <Alert className={`border-emerald-500/30 bg-emerald-500/10 text-emerald-400 ${className ?? ""}`}>
        <CircleCheckIcon className="size-4 shrink-0 text-emerald-400" />
        <AlertTitle className="text-sm font-medium text-foreground">{emptyMessage}</AlertTitle>
      </Alert>
    );
  }

  return (
    <div className={`flex flex-col gap-2 ${className ?? ""}`}>
      {issues.map(({ issue, tabId }, index) => {
        const targetLabel = TABS.find((tab) => tab.value === tabId)?.label ?? "Spiel erstellen";
        const description = teacherDescription(issue.description);
        return (
          <button
            key={`${tabId}-${issue.severity}-${index}-${issue.description}`}
            type="button"
            disabled={navigationDisabled || onIssueSelect === undefined}
            onClick={() => onIssueSelect?.(tabId)}
            aria-label={`${description} Bereich ${targetLabel} öffnen`}
            className="group/issue relative w-full rounded-lg text-left outline-none transition-transform enabled:cursor-pointer enabled:hover:-translate-y-px focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-default"
          >
            <Alert
              role="presentation"
              className={`py-2 pl-3 pr-8 text-sm transition-colors group-enabled/issue:group-hover/issue:border-current/50 ${SEVERITY_STYLES[issue.severity]}`}
            >
              <SeverityIcon severity={issue.severity} />
              <AlertTitle className="text-sm font-medium text-foreground text-wrap leading-snug">
                {description}
              </AlertTitle>
              {teacherDetails(issue.details) && (
                <AlertDescription className="text-xs text-muted-foreground mt-0.5">
                  {teacherDetails(issue.details)}
                </AlertDescription>
              )}
            </Alert>
            <ChevronRightIcon
              aria-hidden="true"
              className="pointer-events-none absolute right-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground transition-transform group-enabled/issue:group-hover/issue:translate-x-0.5"
            />
          </button>
        );
      })}
    </div>
  );
}
