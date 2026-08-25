import { IssueList } from "./IssueList";
import { ErrorChecker, type IssueReport } from "@/data/ErrorChecker";
import type { AssetStorageCheckStatus } from "@/hooks/useErrorCheck";
import { isTabId, type TabId } from "@/data/Tabs";
import type { TouchedTabs } from "@/data/TabTouchState";

export function ErrorDetector({
  issueReport,
  assetStorageStatus,
  touchedAll,
  touchedTabs,
  currentTab,
  productionReady,
  technicalError,
  onIssueSelect,
  issueNavigationDisabled = false,
  className,
}: {
  issueReport: IssueReport;
  assetStorageStatus: AssetStorageCheckStatus;
  touchedAll: boolean;
  touchedTabs: TouchedTabs;
  currentTab: TabId;
  productionReady: boolean;
  technicalError: "validating" | "packaging" | null;
  onIssueSelect: (tabId: TabId) => void;
  issueNavigationDisabled?: boolean;
  className?: string;
}) {
  const issues = ErrorChecker.getSortedLocatedIssues(issueReport).filter(
    ({ tabId }) => touchedAll || tabId === currentTab
      || (isTabId(tabId) && tabId !== "review" && touchedTabs[tabId]),
  );

  return (
    <div className={`panel ${className ?? ""}`}>
      <div className="mb-2 flex items-center justify-between gap-3">
        <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground px-1 mb-0">
          Fehlerübersicht
        </h2>
        {issues.length > 0 && (
          <span className="inline-flex items-center rounded-full bg-destructive/15 px-2 py-0.5 text-[11px] font-medium leading-none text-destructive">
            {issues.length} {issues.length === 1 ? "Problem" : "Probleme"}
          </span>
        )}
      </div>
      {!touchedAll && issues.length === 0 && (
        <p className="text-xs text-muted-foreground leading-relaxed px-1">
          In diesem und bereits bearbeiteten Bereichen gibt es noch keine offenen Punkte.
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
      {issues.length > 0 && (
        <IssueList
          issues={issues}
          onIssueSelect={(tabId) => onIssueSelect(isTabId(tabId) ? tabId : "review")}
          navigationDisabled={issueNavigationDisabled}
          className="mt-2"
        />
      )}
      {touchedAll && assetStorageStatus === "ready" && productionReady
        && technicalError === null && issues.length === 0 && (
        <IssueList issues={[]} className="mt-2" />
      )}
    </div>
  );
}
