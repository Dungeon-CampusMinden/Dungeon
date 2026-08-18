import React from "react";
import {
  draftRevision,
  type DraftRevision,
  type DraftTransform,
  type UpdateDraft,
  type WizardDraft,
} from "@/data/WizardDraft";
import type { DeerProject } from "@/data/DeerSchema";
import { createDeerCandidate } from "@/data/createDeerCandidate";
import { ErrorChecker } from "@/data/ErrorChecker";
import { localizeProductionIssues, type ProjectValidationReport } from "@/data/ProjectValidationReport";
import { useWizardStorage } from "@/data/WizardStorage";
import { useErrorCheck } from "@/hooks/useErrorCheck";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import { Button } from "./ui/button";
import { IssueList } from "./IssueList";
import { CheckCircle2Icon, FolderIcon, LoaderCircleIcon, PlayIcon, RotateCwIcon } from "lucide-react";
import type { WizardWork } from "@/data/WizardWork";
import type {
  FinalizationIdentity,
  FinalizationStatus,
  PackageResult,
} from "@/data/NativeWizardHost";
import { DraftReloadRequiredError } from "@/data/DraftStorage";

async function candidateHash(candidate: DeerProject): Promise<string> {
  const bytes = new TextEncoder().encode(JSON.stringify(candidate));
  const digest = await crypto.subtle.digest("SHA-256", bytes);
  return Array.from(new Uint8Array(digest), (byte) => byte.toString(16).padStart(2, "0")).join("");
}

function uploadBindings(draft: WizardDraft, candidate: DeerProject): Record<string, string> {
  const usedAssetIds = new Set(candidate.assets.map((asset) => asset.id));
  return Object.fromEntries(Object.entries(draft.uploads)
    .filter(([assetId]) => usedAssetIds.has(assetId))
    .map(([assetId, upload]) => [assetId, upload.storageKey]));
}

type ReviewWork = Exclude<WizardWork, "uploading">;

function finalizationIdentity(draft: WizardDraft): FinalizationIdentity | null {
  const value = draft.finalization;
  if (!value?.deerSha256) return null;
  return {
    seed: value.seed,
    finalizedAt: value.finalizedAt,
    projectDirectory: value.projectDirectory,
    deerSha256: value.deerSha256,
  };
}

function sameIdentity(left: FinalizationIdentity, right: FinalizationIdentity): boolean {
  return left.seed === right.seed
    && left.finalizedAt === right.finalizedAt
    && left.projectDirectory === right.projectDirectory
    && left.deerSha256 === right.deerSha256;
}

function statusIdentity(status: FinalizationStatus | null): FinalizationIdentity | null {
  if (status?.seed === null || status?.seed === undefined
    || status.finalizedAt === null || status.projectDirectory === null
    || status.deerSha256 === null) return null;
  return {
    seed: status.seed,
    finalizedAt: status.finalizedAt,
    projectDirectory: status.projectDirectory,
    deerSha256: status.deerSha256,
  };
}

function readyStatus(result: PackageResult): FinalizationStatus {
  return { ...result, status: "ready" };
}

function incrementedRevision(revision: DraftRevision): DraftRevision {
  if (revision === Number.MAX_SAFE_INTEGER) {
    throw new Error("Der Entwurf kann nicht weiter verarbeitet werden.");
  }
  return draftRevision(revision + 1);
}

export function ReviewTab({
  draft,
  updateDraft,
  flush,
  adoptHostMutation,
  onReloadRequired,
  work,
  beginWork,
  transitionWork,
  finishWork,
}: {
  draft: WizardDraft;
  updateDraft: UpdateDraft;
  flush: () => Promise<WizardDraft>;
  adoptHostMutation: (
    expectedRevision: DraftRevision,
    nextRevision: DraftRevision,
    transform: DraftTransform,
  ) => WizardDraft;
  onReloadRequired: (cause: DraftReloadRequiredError) => void;
  work: WizardWork;
  beginWork: (work: Exclude<ReviewWork, null>) => boolean;
  transitionWork: (
    from: Exclude<ReviewWork, null>,
    to: Exclude<ReviewWork, null>,
  ) => boolean;
  finishWork: (work: Exclude<ReviewWork, null>) => void;
}) {
  const storage = useWizardStorage();
  const candidate = React.useMemo(() => createDeerCandidate(draft), [draft]);
  const { issueReport, assetStorageStatus } = useErrorCheck(draft.draftId, candidate, draft.uploads);
  const localIssues = ErrorChecker.getSortedIssues(issueReport);
  const localErrorCount = localIssues.filter((issue) => issue.severity === "error").length;
  const [projectDirectory, setProjectDirectory] = React.useState(
    draft.projectDirectory ?? draft.finalization?.projectDirectory ?? "",
  );
  const [report, setReport] = React.useState<{
    value: ProjectValidationReport;
    snapshot: WizardDraft;
  } | null>(null);
  const [technicalError, setTechnicalError] = React.useState<string | null>(null);
  const [statusError, setStatusError] = React.useState<string | null>(null);
  const [hostStatus, setHostStatus] = React.useState<FinalizationStatus | null>(null);
  const [currentHash, setCurrentHash] = React.useState<string | null>(null);

  React.useEffect(() => {
    let cancelled = false;
    void candidateHash(candidate).then((hash) => { if (!cancelled) setCurrentHash(hash); });
    return () => { cancelled = true; };
  }, [candidate]);

  React.useEffect(() => {
    if (!storage.host.native || work !== null) return;
    let cancelled = false;
    setStatusError(null);
    void storage.host.finalizationStatus(draft.draftId).then((status) => {
      if (!cancelled) setHostStatus(status);
    }).catch((cause) => {
      if (!cancelled) {
        setHostStatus(null);
        if (cause instanceof DraftReloadRequiredError) onReloadRequired(cause);
        else setStatusError(cause instanceof Error ? cause.message : "Der Status der Spieldatei konnte nicht geprüft werden.");
      }
    });
    return () => { cancelled = true; };
  }, [draft.draftId, draft.revision, onReloadRequired, storage, work]);

  const productionIssues = React.useMemo(
    () => report ? localizeProductionIssues(report.value, report.snapshot) : [],
    [report],
  );
  const storedIdentity = finalizationIdentity(draft);
  const confirmedIdentity = statusIdentity(hostStatus);
  const hostIdentityMatches = Boolean(
    storedIdentity
    && confirmedIdentity
    && hostStatus?.revision === draft.revision
    && sameIdentity(storedIdentity, confirmedIdentity),
  );
  const finalizedCurrent = Boolean(
    draft.finalization
    && draft.finalization.candidateHash !== undefined
    && currentHash === draft.finalization.candidateHash
    && projectDirectory === draft.finalization.projectDirectory,
  );
  const packageMissing = hostStatus?.status === "finalized" && hostIdentityMatches;
  const ready = hostStatus?.status === "ready" && hostIdentityMatches;

  const run = async (kind: Exclude<ReviewWork, null>, action: () => Promise<void>) => {
    if (!beginWork(kind)) return;
    setTechnicalError(null);
    try { await action(); }
    catch (cause) {
      if (cause instanceof DraftReloadRequiredError) onReloadRequired(cause);
      else setTechnicalError(cause instanceof Error ? cause.message : "Der Vorgang ist technisch fehlgeschlagen.");
    }
    finally { finishWork(kind); }
  };

  const chooseDirectory = () => run("choosing", async () => {
    const selected = await storage.host.chooseProjectDirectory();
    if (selected !== null) {
      setProjectDirectory(selected);
      updateDraft((current) => { current.projectDirectory = selected; });
      await flush();
    }
  });

  const validate = () => run("validating", async () => {
    setReport(null);
    const snapshot = await flush();
    const snapshotCandidate = createDeerCandidate(snapshot);
    const hash = await candidateHash(snapshotCandidate);
    const next = await storage.host.validate(
      snapshot.draftId,
      snapshot.revision,
      hash,
      snapshotCandidate,
      uploadBindings(snapshot, snapshotCandidate),
    );
    if (next.revision !== snapshot.revision || next.candidateHash !== hash) {
      throw new Error("Das Prüfergebnis gehört nicht mehr zum gespeicherten Entwurf.");
    }
    setReport({ value: next.report, snapshot });
  });

  const packageFinalizedProject = async (
    finalization: FinalizationIdentity,
    operationAlreadyStarted = false,
  ) => {
    if (!operationAlreadyStarted && !beginWork("packaging")) return;
    if (!operationAlreadyStarted) setTechnicalError(null);
    try {
      const snapshot = await flush();
      const currentIdentity = finalizationIdentity(snapshot);
      if (!currentIdentity || !sameIdentity(currentIdentity, finalization)) {
        throw new Error("Die gespeicherte Spielversion hat sich geändert. Erstelle das Spiel erneut.");
      }
      const expectedRevision = incrementedRevision(snapshot.revision);
      const packaged = await storage.host.package(snapshot.draftId, {
        revision: snapshot.revision,
        ...finalization,
      });
      if (packaged.revision !== expectedRevision
        || !sameIdentity(packaged, finalization)
        || !sameIdentity(currentIdentity, finalization)) {
        throw new Error(
          "Die Spieldatei wurde erstellt, gehört aber nicht mehr zum aktuell geöffneten Projektstand.",
        );
      }
      adoptHostMutation(snapshot.revision, packaged.revision, (current) => {
        const adoptedIdentity = finalizationIdentity(current);
        if (!adoptedIdentity || !sameIdentity(adoptedIdentity, finalization)
          || !current.finalization) return false;
        current.finalization = {
          ...current.finalization,
          jarPath: packaged.jarPath,
          jarSha256: packaged.jarSha256,
        };
      });
      // jarPath is host-owned. The response is displayed only after the complete identity matches.
      setHostStatus(readyStatus(packaged));
    } catch (cause) {
      if (cause instanceof DraftReloadRequiredError) onReloadRequired(cause);
      else setTechnicalError(cause instanceof Error ? cause.message : "Die Spieldatei konnte nicht erstellt werden.");
    } finally { finishWork("packaging"); }
  };

  const finalize = () => run("finalizing", async () => {
    setReport(null);
    if (!projectDirectory) throw new Error("Wähle zuerst einen Zielordner aus.");
    let snapshot = await flush();
    if (snapshot.projectDirectory !== projectDirectory) {
      updateDraft((current) => { current.projectDirectory = projectDirectory; });
      snapshot = await flush();
    }
    const snapshotCandidate = createDeerCandidate(snapshot);
    const hash = await candidateHash(snapshotCandidate);
    const targetDirectory = snapshot.projectDirectory ?? projectDirectory;
    const successfulRevision = incrementedRevision(snapshot.revision);
    const result = await storage.host.finalize(
      snapshot.draftId,
      snapshot.revision,
      hash,
      snapshotCandidate,
      uploadBindings(snapshot, snapshotCandidate),
      targetDirectory,
    );
    if (result.candidateHash !== hash
      || (result.projectDirectory !== null && result.projectDirectory !== targetDirectory)
      || (result.report.valid
        ? result.revision !== successfulRevision
        : result.revision !== snapshot.revision)) {
      throw new Error("Das Ergebnis gehört nicht mehr zum gespeicherten Entwurf.");
    }
    setReport({ value: result.report, snapshot });
    if (!result.report.valid) return;
    if (result.seed === null || result.finalizedAt === null || result.projectDirectory === null
      || result.deerSha256 === null) {
      throw new Error("Das Spiel wurde nicht vollständig bestätigt.");
    }
    adoptHostMutation(snapshot.revision, result.revision, (current) => {
      current.finalization = {
        seed: result.seed as number,
        finalizedAt: result.finalizedAt as string,
        projectDirectory: result.projectDirectory as string,
        deerSha256: result.deerSha256 as string,
      };
      current.projectDirectory = result.projectDirectory as string;
    });
    updateDraft((current) => {
      if (!current.finalization
        || current.finalization.seed !== result.seed
        || current.finalization.finalizedAt !== result.finalizedAt
        || current.finalization.projectDirectory !== result.projectDirectory
        || current.finalization.deerSha256 !== result.deerSha256) return false;
      current.finalization.candidateHash = hash;
    });
    await flush();
    const finalization: FinalizationIdentity = {
      seed: result.seed,
      projectDirectory: result.projectDirectory,
      finalizedAt: result.finalizedAt,
      deerSha256: result.deerSha256,
    };
    if (!transitionWork("finalizing", "packaging")) {
      throw new Error("Die Erstellung konnte nicht sicher fortgesetzt werden.");
    }
    await packageFinalizedProject(finalization, true);
  });

  const nativeOperationBlocked = assetStorageStatus !== "ready" || work !== null;
  const finalizeBlocked = localErrorCount > 0 || nativeOperationBlocked;

  return (
    <div className="flex flex-col gap-4" aria-busy={work !== null}>
      <div><h1 className="mb-1">Spiel erstellen</h1><p className="text-muted-foreground">Prüfe deinen Entwurf und erstelle anschließend die Spieldatei für alle Mitspielenden.</p></div>

      {!storage.host.native && (
        <Alert><AlertTitle>Separater Entwicklungs- und UI-Testmodus</AlertTitle><AlertDescription>Dieser Entwurf bleibt ausschließlich in diesem Browser. Er kann nicht in die lokale Wizard-Anwendung übertragen, vollständig geprüft oder als Spiel verpackt werden.</AlertDescription></Alert>
      )}

      <section className="panel flex flex-col gap-3">
        <div><h2 className="mb-1">1. Zielordner</h2><p className="text-sm text-muted-foreground">Hier werden das fertige Projekt und die Spieldatei abgelegt.</p></div>
        <div className="flex flex-wrap items-center gap-3">
          <Button variant="outline" onClick={() => void chooseDirectory()} disabled={!storage.host.native || work !== null}><FolderIcon />Ordner wählen</Button>
          <span className="min-w-0 text-sm text-muted-foreground">{projectDirectory || "Noch kein Ordner gewählt"}</span>
        </div>
      </section>

      <section className="panel flex flex-col gap-3">
        <div><h2 className="mb-1">2. Prüfen und erstellen</h2><p className="text-sm text-muted-foreground">Die schnelle Vorprüfung läuft während der Bearbeitung. Die vollständige Prüfung verwendet dieselben Regeln wie das fertige Spiel.</p></div>
        {localErrorCount > 0 && <Alert variant="destructive"><AlertTitle>Der Entwurf ist noch nicht vollständig</AlertTitle><AlertDescription>{localErrorCount} {localErrorCount === 1 ? "Fehler muss" : "Fehler müssen"} vor dem Erstellen behoben werden.</AlertDescription></Alert>}
        {assetStorageStatus === "checking" && <p className="text-sm text-muted-foreground">Eigene Dateien werden geprüft…</p>}
        {assetStorageStatus === "error" && <Alert variant="destructive"><AlertTitle>Eigene Dateien konnten nicht geprüft werden</AlertTitle><AlertDescription>Deine Eingaben bleiben erhalten. Prüfe, ob der lokale Speicher erreichbar ist, und versuche es erneut.</AlertDescription></Alert>}
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" onClick={() => void validate()} disabled={!storage.host.native || nativeOperationBlocked}><CheckCircle2Icon />Entwurf vollständig prüfen</Button>
          <Button onClick={() => void finalize()} disabled={!storage.host.native || !projectDirectory || finalizeBlocked}><PlayIcon />Spiel erstellen</Button>
        </div>
      </section>

      {work && <p className="flex items-center gap-2 text-sm text-muted-foreground"><LoaderCircleIcon className="animate-spin" />{work === "uploading" ? "Datei wird sicher gespeichert…" : work === "choosing" ? "Ordnerauswahl wird geöffnet…" : work === "validating" ? "Entwurf wird vollständig geprüft…" : work === "finalizing" ? "Projekt wird sicher gespeichert…" : "Spieldatei wird erstellt…"}</p>}

      {technicalError && <Alert variant="destructive"><AlertTitle>Vorgang nicht abgeschlossen</AlertTitle><AlertDescription>{technicalError}</AlertDescription></Alert>}

      {statusError && <Alert variant="destructive"><AlertTitle>Spielstatus nicht bestätigt</AlertTitle><AlertDescription>{statusError}</AlertDescription></Alert>}

      {storage.host.native && draft.finalization && hostStatus === null && !statusError && (
        <p className="text-sm text-muted-foreground">Der gespeicherte Spielstand wird geprüft…</p>
      )}

      {storage.host.native && hostStatus?.status === "invalid" && (
        <Alert><AlertTitle>Gespeicherte Ausgabe nicht mehr bestätigt</AlertTitle><AlertDescription>Das Projekt oder die Spieldatei hat sich außerhalb des Wizards verändert. Deine Eingaben bleiben erhalten. Erstelle das Spiel erneut.</AlertDescription></Alert>
      )}

      {storage.host.native && draft.finalization && hostStatus !== null
        && hostStatus.status !== "invalid" && !hostIdentityMatches && (
        <Alert><AlertTitle>Gespeicherte Spielversion nicht bestätigt</AlertTitle><AlertDescription>Der lokale Host bestätigt diese frühere Spielversion nicht mehr. Deine Eingaben bleiben erhalten. Erstelle das Spiel erneut.</AlertDescription></Alert>
      )}

      {packageMissing && storedIdentity && (
        <Alert><AlertTitle>{finalizedCurrent ? "Das Projekt ist sicher gespeichert" : "Eine frühere Projektversion ist sicher gespeichert"}</AlertTitle><AlertDescription>{finalizedCurrent ? "Die Spieldatei fehlt noch. Du kannst nur diesen letzten Schritt erneut ausführen, ohne das Projekt noch einmal zu speichern." : "Für die zuletzt sicher gespeicherte Version fehlt noch die Spieldatei. Du kannst sie separat erstellen; neuere Änderungen sind darin nicht enthalten."}</AlertDescription><Button className="mt-3" variant="outline" disabled={work !== null} onClick={() => void packageFinalizedProject(storedIdentity)}><RotateCwIcon />Spieldatei erstellen</Button></Alert>
      )}

      {ready && finalizedCurrent && (
        <Alert className="border-green-500/40 text-green-500"><CheckCircle2Icon /><AlertTitle>Das Spiel ist bereit</AlertTitle><AlertDescription>Verteile die erzeugte WizardRoom.jar an alle Teilnehmenden. Zum Starten wird derzeit Java 25 benötigt. Eine .exe folgt später. Letzter Erfolg: {new Date(hostStatus!.finalizedAt!).toLocaleString("de-DE")}.</AlertDescription></Alert>
      )}
      {ready && !finalizedCurrent && (
        <Alert><AlertTitle>Spieldatei der letzten Projektversion vorhanden</AlertTitle><AlertDescription>Die Spieldatei gehört zur zuletzt sicher gespeicherten Projektversion. Erstelle das Spiel erneut, damit deine aktuellen Änderungen enthalten sind.</AlertDescription></Alert>
      )}
      {report && (
        <section className="flex flex-col gap-2"><h2 className="mb-0">Ergebnis der vollständigen Prüfung</h2><IssueList issues={productionIssues} emptyMessage="Die vollständige Prüfung hat keine Probleme gefunden." /></section>
      )}
      <section className="flex flex-col gap-2"><h2 className="mb-0">Schnelle Vorprüfung</h2><IssueList issues={localIssues} emptyMessage="Lokal wurden keine Probleme gefunden." /></section>
    </div>
  );
}
