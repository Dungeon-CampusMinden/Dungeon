import type { Issue, TabIssues } from "@/data/ErrorChecker";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import { CircleAlertIcon, CircleXIcon, InfoIcon } from "lucide-react";

export function fieldIssues(tabIssues: TabIssues | undefined, field: string): Issue[] {
  return tabIssues?.[field] ?? [];
}

export function prefixedFieldIssues(tabIssues: TabIssues | undefined, prefix: string): Issue[] {
  return Object.entries(tabIssues ?? {})
    .filter(([field]) => field === prefix || field.startsWith(`${prefix}:`))
    .flatMap(([, issues]) => issues);
}

export function hasFieldErrors(tabIssues: TabIssues | undefined, field: string): boolean {
  return fieldIssues(tabIssues, field).some((issue) => issue.severity === "error");
}

export function ValidationFeedback({ issues, className }: { issues: Issue[]; className?: string }) {
  if (issues.length === 0) return null;
  return (
    <div className={`flex flex-col gap-2 ${className ?? ""}`}>
      {issues.map((issue, index) => (
        <Alert
          key={`${issue.severity}-${issue.description}-${index}`}
          variant={issue.severity === "error" ? "destructive" : "default"}
          className={issue.severity === "warning" ? "border-amber-500/30 bg-amber-500/10" : undefined}
        >
          {issue.severity === "error" ? <CircleXIcon /> : issue.severity === "warning" ? <CircleAlertIcon /> : <InfoIcon />}
          <AlertTitle>{issue.description}</AlertTitle>
          {issue.details && <AlertDescription>{issue.details}</AlertDescription>}
        </Alert>
      ))}
    </div>
  );
}
