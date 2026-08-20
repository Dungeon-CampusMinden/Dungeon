import React from "react";
import { createDeerCandidate } from "@/data/createDeerCandidate";
import { ErrorChecker } from "@/data/ErrorChecker";
import { localizeProductionIssues, type ProjectValidationReport } from "@/data/ProjectValidationReport";
import { useWizardStorage } from "@/data/WizardStorage";
import { createSeed, type UpdateDraft, type WizardDraft } from "@/data/WizardDraft";
import { useErrorCheck } from "@/hooks/useErrorCheck";
import { isCustomAssetPath } from "./assets/assetPaths";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import { Button } from "./ui/button";
import { IssueList } from "./IssueList";
import { CheckCircle2Icon, FolderIcon, LoaderCircleIcon, PlayIcon } from "lucide-react";
import type { WizardWork } from "@/data/WizardWork";
import type { ProductionRequest } from "@/data/NativeWizardHost";

type ReviewWork = Exclude<WizardWork, "uploading">;

function bytesBase64(bytes: Uint8Array): string {
  let binary = "";
  const chunkSize = 0x8000;
  for (let offset = 0; offset < bytes.length; offset += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(offset, offset + chunkSize));
  }
  return btoa(binary);
}

export function ReviewTab({
  draft,
  updateDraft,
  flush,
  work,
  beginWork,
  transitionWork,
  finishWork,
}: {
  draft: WizardDraft;
  updateDraft: UpdateDraft;
  flush: () => Promise<WizardDraft>;
  work: WizardWork;
  beginWork: (work: Exclude<ReviewWork, null>) => boolean;
  transitionWork: (
    from: Exclude<ReviewWork, null>,
    to: Exclude<ReviewWork, null>,
  ) => boolean;
  finishWork: (work: Exclude<ReviewWork, null>) => void;
}) {
  const storage = useWizardStorage();
  const { issueReport, assetStorageStatus } = useErrorCheck(
    draft.draftId,
    draft.project,
    draft.uploads,
  );
  const localIssues = ErrorChecker.getSortedIssues(issueReport);
  const localErrorCount = localIssues.filter((issue) => issue.severity === "error").length;
  const [projectDirectory, setProjectDirectory] = React.useState(draft.projectDirectory ?? "");
  const [report, setReport] = React.useState<{
    value: ProjectValidationReport;
    snapshot: WizardDraft;
  } | null>(null);
  const [technicalError, setTechnicalError] = React.useState<string | null>(null);
  const [readyJarPath, setReadyJarPath] = React.useState<string | null>(null);

  React.useEffect(() => { setReadyJarPath(null); }, [draft.project, draft.uploads, projectDirectory]);

  const productionIssues = React.useMemo(
    () => report ? localizeProductionIssues(report.value, report.snapshot) : [],
    [report],
  );

  const run = async (kind: Exclude<ReviewWork, null>, action: () => Promise<void>) => {
    if (!beginWork(kind)) return;
    setTechnicalError(null);
    try { await action(); }
    catch (cause) {
      setTechnicalError(cause instanceof Error ? cause.message : "Der Vorgang ist technisch fehlgeschlagen.");
    } finally { finishWork(kind); }
  };

  const prepareRequest = async (): Promise<{
    request: ProductionRequest;
    snapshot: WizardDraft;
  }> => {
    let snapshot = await flush();
    if (snapshot.seed === undefined) {
      updateDraft((current) => {
        if (current.seed === undefined) current.seed = createSeed();
      });
      snapshot = await flush();
    }

    const project = createDeerCandidate(snapshot);
    const customAssets: ProductionRequest["customAssets"] = [];
    const includedCustomPaths = new Set<string>();
    for (const asset of project.assets) {
      if (!isCustomAssetPath(asset.path)) continue;
      if (includedCustomPaths.has(asset.path)) continue;
      includedCustomPaths.add(asset.path);
      const upload = snapshot.uploads[asset.id];
      if (!upload) throw new Error(`Die eigene Datei "${asset.path}" fehlt im Entwurf.`);
      const stored = await storage.assets.getAssetFile(snapshot.draftId, upload.storageKey);
      if (!stored) throw new Error(`Die eigene Datei "${upload.originalName}" ist nicht mehr gespeichert.`);
      customAssets.push({
        path: asset.path,
        bytesBase64: bytesBase64(new Uint8Array(await stored.blob.arrayBuffer())),
      });
    }
    return { request: { project, customAssets }, snapshot };
  };

  const chooseDirectory = () => run("choosing", async () => {
    const selected = await storage.host.chooseProjectDirectory();
    if (selected === null) return;
    setProjectDirectory(selected);
    updateDraft((current) => { current.projectDirectory = selected; });
    await flush();
  });

  const validate = () => run("validating", async () => {
    setReport(null);
    const { request, snapshot } = await prepareRequest();
    const result = await storage.host.validate(request);
    setReport({ value: result, snapshot });
  });

  const finalize = () => run("finalizing", async () => {
    setReport(null);
    setReadyJarPath(null);
    if (!projectDirectory) throw new Error("Wähle zuerst einen Zielordner aus.");
    if (draft.projectDirectory !== projectDirectory) {
      updateDraft((current) => { current.projectDirectory = projectDirectory; });
    }
    const { request, snapshot } = await prepareRequest();
    const finalized = await storage.host.finalize({ ...request, projectDirectory });
    setReport({ value: finalized, snapshot });
    if (!finalized.valid) return;
    if (!transitionWork("finalizing", "packaging")) {
      throw new Error("Die Erstellung konnte nicht fortgesetzt werden.");
    }
    try {
      const packaged = await storage.host.package(projectDirectory, request.project.metadata.id);
      setReport({ value: packaged.report, snapshot });
      if (!packaged.report.valid || packaged.jarPath === null) return;
      setReadyJarPath(packaged.jarPath);
    } finally { finishWork("packaging"); }
  });

  const nativeOperationBlocked = assetStorageStatus !== "ready" || work !== null;
  const finalizeBlocked = localErrorCount > 0 || nativeOperationBlocked;

  return (
    <div className="flex flex-col gap-4" aria-busy={work !== null}>
      <div><h1 className="mb-1">Spiel erstellen</h1><p className="text-muted-foreground">Prüfe deinen Entwurf und erstelle anschließend die Spieldatei für alle Mitspielenden.</p></div>

      {!storage.host.native && (
        <Alert><AlertTitle>Entwicklungsmodus</AlertTitle><AlertDescription>Vollständiges Prüfen und Erstellen ist nur in der lokalen Wizard-Anwendung verfügbar.</AlertDescription></Alert>
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
        {assetStorageStatus === "error" && <Alert variant="destructive"><AlertTitle>Eigene Dateien konnten nicht geprüft werden</AlertTitle><AlertDescription>Deine Eingaben bleiben erhalten. Versuche es erneut.</AlertDescription></Alert>}
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" onClick={() => void validate()} disabled={!storage.host.native || nativeOperationBlocked}><CheckCircle2Icon />Entwurf vollständig prüfen</Button>
          <Button onClick={() => void finalize()} disabled={!storage.host.native || !projectDirectory || finalizeBlocked}><PlayIcon />Spiel erstellen</Button>
        </div>
      </section>

      {work && <p className="flex items-center gap-2 text-sm text-muted-foreground"><LoaderCircleIcon className="animate-spin" />{work === "uploading" ? "Datei wird gespeichert…" : work === "choosing" ? "Ordnerauswahl wird geöffnet…" : work === "validating" ? "Entwurf wird vollständig geprüft…" : work === "finalizing" ? "Projekt wird gespeichert…" : "Spieldatei wird erstellt…"}</p>}

      {technicalError && <Alert variant="destructive"><AlertTitle>Vorgang nicht abgeschlossen</AlertTitle><AlertDescription>{technicalError} Versuche den Vorgang erneut.</AlertDescription></Alert>}

      {readyJarPath && (
        <Alert className="border-green-500/40 text-green-500"><CheckCircle2Icon /><AlertTitle>Das Spiel ist bereit</AlertTitle><AlertDescription>Verteile die erzeugte WizardRoom.jar an alle Teilnehmenden. Speicherort: {readyJarPath}</AlertDescription></Alert>
      )}
      {report && (
        <section className="flex flex-col gap-2"><h2 className="mb-0">Ergebnis der vollständigen Prüfung</h2><IssueList issues={productionIssues} emptyMessage="Die vollständige Prüfung hat keine Probleme gefunden." /></section>
      )}
      <section className="flex flex-col gap-2"><h2 className="mb-0">Schnelle Vorprüfung</h2><IssueList issues={localIssues} emptyMessage="Lokal wurden keine Probleme gefunden." /></section>
    </div>
  );
}
