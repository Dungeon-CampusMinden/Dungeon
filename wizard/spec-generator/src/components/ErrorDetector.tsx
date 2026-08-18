import { IssueList } from "./IssueList";
import { ErrorChecker, type IssueReport } from "@/data/ErrorChecker";
import type { AssetStorageCheckStatus } from "@/hooks/useErrorCheck";

export function ErrorDetector({
  issueReport,
  assetStorageStatus,
  touchedAll,
  className,
}: {
  issueReport: IssueReport;
  assetStorageStatus: AssetStorageCheckStatus;
  touchedAll: boolean;
  className?: string;
}) {
  const issues = ErrorChecker.getSortedIssues(issueReport);

  return (
    <div className={`panel rounded-sm ${className ?? ""}`}>
      <h2>Fehlerübersicht</h2>
      {!touchedAll && (
        <p className="text-muted-foreground">
          Sobald alle Felder ausgefüllt sind, wird hier eine Übersicht aller gefundenen Fehler angezeigt.
        </p>
      )}
      {assetStorageStatus === "checking" && (
        <p className="text-muted-foreground">Die lokalen Dateien werden noch geprüft.</p>
      )}
      {assetStorageStatus === "error" && (
        <p className="text-destructive">
          Die lokalen Dateien konnten technisch nicht vollständig geprüft werden. Stelle den lokalen
          Speicher wieder bereit und lade den Entwurf erneut.
        </p>
      )}
      {touchedAll && issues.length > 0 && <IssueList issues={issues} className="mt-2" />}
      {touchedAll && assetStorageStatus === "ready" && issues.length === 0 && (
        <IssueList issues={[]} className="mt-2" />
      )}
    </div>
  );
}
