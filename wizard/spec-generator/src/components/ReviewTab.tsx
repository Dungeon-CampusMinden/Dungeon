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
import { CheckCircle2Icon, DownloadIcon, LoaderCircleIcon } from "lucide-react";
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

function downloadJar(jar: Blob) {
  const url = URL.createObjectURL(jar);
  const link = document.createElement("a");
  link.href = url;
  link.download = "WizardRoom.jar";
  document.body.append(link);
  link.click();
  link.remove();
  window.setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export function ReviewTab({
  draft,
  updateDraft,
  flush,
  work,
  beginWork,
  finishWork,
}: {
  draft: WizardDraft;
  updateDraft: UpdateDraft;
  flush: () => Promise<WizardDraft>;
  work: WizardWork;
  beginWork: (work: Exclude<ReviewWork, null>) => boolean;
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
  const [report, setReport] = React.useState<{
    value: ProjectValidationReport;
    snapshot: WizardDraft;
  } | null>(null);
  const [technicalError, setTechnicalError] = React.useState<string | null>(null);
  const [downloaded, setDownloaded] = React.useState(false);

  React.useEffect(() => { setDownloaded(false); }, [draft.project, draft.uploads]);

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

  const validate = () => run("validating", async () => {
    setReport(null);
    const { request, snapshot } = await prepareRequest();
    const result = await storage.host.validate(request);
    setReport({ value: result, snapshot });
  });

  const packageGame = () => run("packaging", async () => {
    setReport(null);
    setDownloaded(false);
    const { request, snapshot } = await prepareRequest();
    const packaged = await storage.host.package(request);
    if (packaged.kind === "invalid") {
      setReport({ value: packaged.report, snapshot });
      return;
    }
    downloadJar(packaged.jar);
    setDownloaded(true);
  });

  const nativeOperationBlocked = assetStorageStatus !== "ready" || work !== null;
  const packageBlocked = localErrorCount > 0 || nativeOperationBlocked;

  return (
    <div className="flex flex-col gap-4" aria-busy={work !== null}>
      <div><h1 className="mb-1">Spiel erstellen</h1><p className="text-muted-foreground">Prüfe deinen Entwurf und erstelle anschließend die Spieldatei für alle Mitspielenden.</p></div>

      {!storage.host.native && (
        <Alert><AlertTitle>Entwicklungsmodus</AlertTitle><AlertDescription>Vollständiges Prüfen und Erstellen ist nur in der lokalen Wizard-Anwendung verfügbar.</AlertDescription></Alert>
      )}

      <section className="flex flex-col gap-3 rounded-lg border border-border bg-muted/20 p-4">
        <div><h2 className="mb-1 text-base font-semibold text-foreground">Prüfen und herunterladen</h2><p className="text-sm text-muted-foreground">Die schnelle Vorprüfung läuft während der Bearbeitung. Beim Erstellen wird das Spiel vollständig geprüft und anschließend als WizardRoom.jar heruntergeladen.</p></div>
        {localErrorCount > 0 && <Alert variant="destructive"><AlertTitle>Der Entwurf ist noch nicht vollständig</AlertTitle><AlertDescription>{localErrorCount} {localErrorCount === 1 ? "Fehler muss" : "Fehler müssen"} vor dem Erstellen behoben werden.</AlertDescription></Alert>}
        {assetStorageStatus === "checking" && <p className="text-sm text-muted-foreground">Eigene Dateien werden geprüft…</p>}
        {assetStorageStatus === "error" && <Alert variant="destructive"><AlertTitle>Eigene Dateien konnten nicht geprüft werden</AlertTitle><AlertDescription>Deine Eingaben bleiben erhalten. Versuche es erneut.</AlertDescription></Alert>}
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" onClick={() => void validate()} disabled={!storage.host.native || nativeOperationBlocked}><CheckCircle2Icon />Entwurf vollständig prüfen</Button>
          <Button onClick={() => void packageGame()} disabled={!storage.host.native || packageBlocked}><DownloadIcon />Spiel erstellen und herunterladen</Button>
        </div>
      </section>

      {work && <p className="flex items-center gap-2 text-sm text-muted-foreground"><LoaderCircleIcon className="animate-spin" />{work === "uploading" ? "Datei wird gespeichert…" : work === "validating" ? "Entwurf wird vollständig geprüft…" : "Spieldatei wird erstellt…"}</p>}

      {technicalError && <Alert variant="destructive"><AlertTitle>Vorgang nicht abgeschlossen</AlertTitle><AlertDescription>{technicalError} Versuche den Vorgang erneut.</AlertDescription></Alert>}

      {downloaded && (
        <Alert className="border-emerald-500/40 bg-emerald-500/10 text-emerald-400"><CheckCircle2Icon className="size-4 shrink-0 text-emerald-400" /><AlertTitle className="text-sm font-medium text-foreground">Das Spiel ist bereit</AlertTitle><AlertDescription className="text-xs text-muted-foreground">WizardRoom.jar wurde heruntergeladen und kann an alle Teilnehmenden verteilt werden.</AlertDescription></Alert>
      )}
      {report && (
        <section className="flex flex-col gap-2"><h2 className="mb-0 text-sm font-semibold uppercase tracking-wider text-muted-foreground">Ergebnis der vollständigen Prüfung</h2><IssueList issues={productionIssues} emptyMessage="Die vollständige Prüfung hat keine Probleme gefunden." /></section>
      )}
      <section className="flex flex-col gap-2"><h2 className="mb-0 text-sm font-semibold uppercase tracking-wider text-muted-foreground">Schnelle Vorprüfung</h2><IssueList issues={localIssues} emptyMessage="Lokal wurden keine Probleme gefunden." /></section>
    </div>
  );
}
