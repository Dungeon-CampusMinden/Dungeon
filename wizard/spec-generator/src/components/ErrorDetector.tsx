import type { DeerSchema } from "@/data/DeerSchema";
import { IssueList } from "./IssueList";
import { ErrorChecker, type IssueReport } from "@/data/ErrorChecker";

export function ErrorDetector({
  deerSchema,
  updateDeerSchema,
  issueReport,
  touchedAll,
  className,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
  issueReport: IssueReport;
  touchedAll: boolean;
  className?: string;
}) {
  return (
    <div className={`panel rounded-sm ${className ?? ""}`}>
      <h2>Fehlerübersicht</h2>
      {!touchedAll && (
        <p className="text-muted-foreground">
          Sobal alle Felder ausgefüllt sind, wird hier eine Übersicht aller gefundenen Fehler angezeigt.
        </p>
      )}
      {touchedAll && <IssueList issues={ErrorChecker.getSortedIssues(issueReport)} className="mt-2" />}
    </div>
  );
}
