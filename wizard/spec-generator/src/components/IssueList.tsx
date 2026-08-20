import { CircleAlertIcon, CircleCheckIcon, CircleXIcon, InfoIcon } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import type { Issue, IssueSeverity } from "@/data/ErrorChecker";

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
  className,
}: {
  issues: Issue[];
  emptyMessage?: string;
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
      {issues.map((issue, index) => (
        <Alert
          key={`${issue.severity}-${index}-${issue.description}`}
          className={`py-2 px-3 text-sm ${SEVERITY_STYLES[issue.severity]}`}
        >
          <SeverityIcon severity={issue.severity} />
          <AlertTitle className="text-sm font-medium text-foreground text-wrap leading-snug">
            {teacherDescription(issue.description)}
          </AlertTitle>
          {teacherDetails(issue.details) && (
            <AlertDescription className="text-xs text-muted-foreground mt-0.5">
              {teacherDetails(issue.details)}
            </AlertDescription>
          )}
        </Alert>
      ))}
    </div>
  );
}
