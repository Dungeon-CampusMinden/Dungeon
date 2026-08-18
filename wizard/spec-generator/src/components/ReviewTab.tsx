import React from "react";
import type { WizardDraft } from "@/data/WizardDraft";
import { createDeerCandidate } from "@/data/createDeerCandidate";
import { ErrorChecker } from "@/data/ErrorChecker";
import { useErrorCheck } from "@/hooks/useErrorCheck";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import { IssueList } from "./IssueList";

const TEMPORARY_REVIEW_SEED = 0;

export function ReviewTab({ draft }: { draft: WizardDraft }) {
  // The temporary seed exists only in this in-memory projection. It is never written to the draft.
  const candidate = React.useMemo(
    () => createDeerCandidate(draft, TEMPORARY_REVIEW_SEED),
    [draft],
  );
  const { issueReport, assetStorageStatus } = useErrorCheck(candidate, draft.uploads);
  const issues = ErrorChecker.getSortedIssues(issueReport);
  const errorCount = issues.filter((issue) => issue.severity === "error").length;
  const warningCount = issues.filter((issue) => issue.severity === "warning").length;

  return (
    <div className="flex flex-col gap-3">
      <h1>Entwurf prüfen</h1>

      <Alert className="mt-0">
        <AlertTitle>Lokale Vorprüfung</AlertTitle>
        <AlertDescription>
          Diese Ansicht prüft deinen Entwurf auf direkt erkennbare Probleme. Behebe die angezeigten
          Fehler, bevor du später das spielbare Projekt erstellst.
        </AlertDescription>
      </Alert>

      {assetStorageStatus === "checking" && (
        <Alert className="mt-0">
          <AlertTitle>Lokale Dateien werden geprüft</AlertTitle>
          <AlertDescription>
            Die Vorprüfung ist noch nicht vollständig. Bitte warte einen Moment.
          </AlertDescription>
        </Alert>
      )}
      {assetStorageStatus === "error" && (
        <Alert variant="destructive" className="mt-0">
          <AlertTitle>Lokale Dateien konnten nicht geprüft werden</AlertTitle>
          <AlertDescription>
            Die Vorprüfung ist nicht vollständig. Versuche es erneut, indem du diese Ansicht noch
            einmal öffnest. Falls das Problem bleibt, starte die Anwendung neu.
          </AlertDescription>
        </Alert>
      )}

      {errorCount > 0 && (
        <Alert variant="destructive" className="mt-0">
          <AlertTitle>Der Entwurf ist noch nicht vollständig.</AlertTitle>
          <AlertDescription>
            {errorCount} {errorCount === 1 ? "Fehler muss" : "Fehler müssen"} behoben werden,
            bevor das spielbare Projekt erstellt werden kann.
          </AlertDescription>
        </Alert>
      )}
      {warningCount > 0 && (
        <Alert className="mt-0 border-yellow-500/40 text-yellow-500">
          <AlertTitle>
            {warningCount} {warningCount === 1 ? "Warnung" : "Warnungen"}
          </AlertTitle>
          <AlertDescription>
            Warnungen verhindern das spätere Erstellen des spielbaren Projekts nicht.
          </AlertDescription>
        </Alert>
      )}
      {assetStorageStatus === "ready" && issues.length === 0 && (
        <IssueList className="mt-0" issues={[]} emptyMessage="Lokal wurden keine Probleme gefunden." />
      )}
      {issues.length > 0 && <IssueList className="mt-0" issues={issues} />}
    </div>
  );
}
