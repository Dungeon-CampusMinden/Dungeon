import { CircleAlertIcon, CircleCheckIcon, CircleXIcon, InfoIcon } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import type { Issue, IssueSeverity } from "@/data/ErrorChecker";

const SEVERITY_STYLES: Record<IssueSeverity, string> = {
  error: "border-red-500/40 text-red-500",
  warning: "border-yellow-500/40 text-yellow-500",
  info: "border-blue-500/40 text-blue-500",
};

function SeverityIcon({ severity }: { severity: IssueSeverity }) {
  switch (severity) {
    case "error":
      return <CircleXIcon />;
    case "warning":
      return <CircleAlertIcon />;
    default:
      return <InfoIcon />;
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
      <Alert className={`border-green-500/40 text-green-500 ${className ?? ""}`}>
        <CircleCheckIcon />
        <AlertTitle>{emptyMessage}</AlertTitle>
      </Alert>
    );
  }

  return (
    <div className={`flex flex-col gap-2 ${className ?? ""}`}>
      {issues.map((issue, index) => (
        <Alert
          key={`${issue.severity}-${index}-${issue.description}`}
          className={SEVERITY_STYLES[issue.severity]}
        >
          <SeverityIcon severity={issue.severity} />
          <AlertTitle className="text-foreground text-wrap">{teacherDescription(issue.description)}</AlertTitle>
          {teacherDetails(issue.details) && <AlertDescription>{teacherDetails(issue.details)}</AlertDescription>}
        </Alert>
      ))}
    </div>
  );
}
