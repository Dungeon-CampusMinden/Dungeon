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
          <AlertTitle className="text-foreground text-wrap">{issue.description}</AlertTitle>
          {issue.details && <AlertDescription>{issue.details}</AlertDescription>}
        </Alert>
      ))}
    </div>
  );
}
