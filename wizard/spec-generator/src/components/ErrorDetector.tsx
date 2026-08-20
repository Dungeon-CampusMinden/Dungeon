import { IssueList } from "./IssueList";
import { ErrorChecker, type IssueReport } from "@/data/ErrorChecker";
import type { AssetStorageCheckStatus } from "@/hooks/useErrorCheck";

export function ErrorDetector({
  issueReport,
  assetStorageStatus,
  touchedAll,
  productionReady,
  technicalError,
  className,
}: {
  issueReport: IssueReport;
  assetStorageStatus: AssetStorageCheckStatus;
  touchedAll: boolean;
  productionReady: boolean;
  technicalError: "validating" | "packaging" | null;
  className?: string;
}) {
  const issues = ErrorChecker.getSortedIssues(issueReport);

  return (
    <div className={`panel ${className ?? ""}`}>
      <div className="flex items-center justify-between mb-2">
        <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground px-1 mb-0">
          Fehlerübersicht
        </h2>
        {touchedAll && issues.length > 0 && (
          <span className="inline-flex items-center rounded-full bg-destructive/15 px-2 py-0.5 text-[11px] font-medium text-destructive">
            {issues.length} {issues.length === 1 ? "Problem" : "Probleme"}
          </span>
        )}
      </div>
      {!touchedAll && (
        <p className="text-xs text-muted-foreground leading-relaxed px-1">
          Sobald alle Bereiche bearbeitet wurden, wird hier eine Übersicht aller offenen Punkte angezeigt.
        </p>
      )}
      {assetStorageStatus === "checking" && (
        <p className="text-xs text-muted-foreground px-1">Die lokalen Dateien werden noch geprüft…</p>
      )}
      {assetStorageStatus === "error" && (
        <p className="text-xs text-destructive px-1">
          Die lokalen Dateien konnten technisch nicht vollständig geprüft werden. Stelle den lokalen
          Speicher wieder bereit und lade den Entwurf erneut.
        </p>
      )}
      {technicalError && (
        <p role="alert" className="text-xs text-destructive px-1">
          {technicalError === "validating"
            ? "Das Spiel konnte nicht geprüft werden. Deine Eingaben bleiben gespeichert. Verlasse diese Seite und öffne sie erneut."
            : "Die Spieldatei konnte nicht erstellt oder heruntergeladen werden. Deine Eingaben bleiben gespeichert. Versuche es erneut."}
        </p>
      )}
      {touchedAll && issues.length > 0 && <IssueList issues={issues} className="mt-2" />}
      {touchedAll && assetStorageStatus === "ready" && productionReady
        && technicalError === null && issues.length === 0 && (
        <IssueList issues={[]} className="mt-2" />
      )}
    </div>
  );
}
