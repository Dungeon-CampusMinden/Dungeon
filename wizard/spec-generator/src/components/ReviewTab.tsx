import React from "react";
import { CheckCircle2Icon, DownloadIcon, LoaderCircleIcon } from "lucide-react";
import type { ProjectValidationReport } from "@/data/ProjectValidationReport";
import { prepareProductionRequest } from "@/data/prepareProductionRequest";
import { useWizardStorage } from "@/data/WizardStorage";
import type { WizardDraft } from "@/data/WizardDraft";
import type { WizardWork } from "@/data/WizardWork";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import { Button } from "./ui/button";

type ReviewWork = Exclude<WizardWork, "uploading">;

export function ReviewTab({
  flush,
  currentDraftSnapshot,
  work,
  localReady,
  productionReport,
  beginWork,
  finishWork,
  acceptReport,
  acceptTechnicalError,
  acceptDownload,
  clearTechnicalError,
  downloadReady,
}: {
  flush: () => Promise<WizardDraft>;
  currentDraftSnapshot: () => WizardDraft;
  work: WizardWork;
  localReady: boolean;
  productionReport: ProjectValidationReport | null;
  beginWork: (work: Exclude<ReviewWork, null>) => boolean;
  finishWork: (work: Exclude<ReviewWork, null>) => void;
  acceptReport: (
    report: ProjectValidationReport,
    snapshot: WizardDraft,
    action: Exclude<ReviewWork, null>,
  ) => void;
  acceptTechnicalError: (action: Exclude<ReviewWork, null>, snapshot: WizardDraft) => void;
  acceptDownload: (jar: Blob, snapshot: WizardDraft) => void;
  clearTechnicalError: () => void;
  downloadReady: boolean;
}) {
  const storage = useWizardStorage();
  const headingRef = React.useRef<HTMLHeadingElement>(null);

  React.useEffect(() => { headingRef.current?.focus(); }, []);

  const run = React.useCallback(async (
    kind: Exclude<ReviewWork, null>,
    action: (attempt: { snapshot: WizardDraft }) => Promise<void>,
  ) => {
    if (!beginWork(kind)) return;
    clearTechnicalError();
    const attempt = { snapshot: currentDraftSnapshot() };
    try {
      await action(attempt);
    } catch {
      acceptTechnicalError(kind, attempt.snapshot);
    } finally {
      finishWork(kind);
    }
  }, [acceptTechnicalError, beginWork, clearTechnicalError, currentDraftSnapshot, finishWork]);

  const packageGame = () => run("packaging", async (attempt) => {
    const snapshot = await flush();
    attempt.snapshot = snapshot;
    const prepared = await prepareProductionRequest(storage, snapshot);
    const packaged = await storage.host.package(prepared.request);
    if (packaged.kind === "invalid") {
      acceptReport(packaged.report, prepared.snapshot, "packaging");
      return;
    }
    acceptDownload(packaged.jar, prepared.snapshot);
  });

  const packageReady = storage.host.native
    && localReady
    && productionReport?.valid === true
    && work === null;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 ref={headingRef} tabIndex={-1} className="mb-1">Spiel erstellen</h1>
        <p className="text-muted-foreground">
          Dein Spiel wird jetzt geprüft. Danach kannst du die fertige Spieldatei herunterladen.
        </p>
      </div>

      {!storage.host.native && (
        <Alert>
          <AlertTitle>Entwicklungsmodus</AlertTitle>
          <AlertDescription>
            Das Spiel kann nur in der lokal gestarteten Wizard-Anwendung erstellt werden.
          </AlertDescription>
        </Alert>
      )}

      {work && (
        <p role="status" className="flex items-center gap-2 text-sm text-muted-foreground">
          <LoaderCircleIcon className="animate-spin" />
          {work === "uploading"
            ? "Datei wird gespeichert…"
            : work === "validating"
              ? "Spiel wird geprüft…"
              : "Spieldatei wird erstellt…"}
        </p>
      )}

      <Button
        size="lg"
        className="h-auto min-h-14 w-full gap-2 whitespace-normal py-4 text-center text-base font-semibold leading-snug"
        onClick={() => void packageGame()}
        disabled={!packageReady}
      >
        <DownloadIcon className="size-5 shrink-0" />
        Spiel erstellen und herunterladen
      </Button>

      {downloadReady && (
        <Alert className="border-emerald-500/40 bg-emerald-500/10 text-status-success">
          <CheckCircle2Icon className="size-4 shrink-0 text-status-success" />
          <AlertTitle className="text-sm font-medium text-foreground">Das Spiel ist bereit</AlertTitle>
          <AlertDescription className="text-xs text-muted-foreground">
            Die Spieldatei wurde heruntergeladen und kann an alle Teilnehmenden verteilt werden.
          </AlertDescription>
        </Alert>
      )}
    </div>
  );
}
