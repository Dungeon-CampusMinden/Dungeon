import React from "react";
import { toast } from "sonner";
import {
  ArrowLeftIcon,
  CheckIcon,
  CircleAlertIcon,
  CircleCheckIcon,
  ClockIcon,
  FolderOpenIcon,
  InfoIcon,
  LoaderCircleIcon,
  PlusIcon,
  Trash2Icon,
  XCircleIcon,
} from "lucide-react";
import "./App.css";
import { ThemeProvider } from "./components/ThemeProvider";
import { ThemeToggle } from "./components/ThemeToggle";
import { Toaster } from "./components/ui/sonner";
import type { DeerProject } from "./data/DeerSchema";
import { ErrorDetector } from "./components/ErrorDetector";
import { SidebarNavigation } from "./components/SidebarNavigation";
import { MetadataTab } from "./components/MetadataTab";
import { ScenarioTab } from "./components/ScenarioTab";
import { SessionTab } from "./components/SessionTab";
import { AssetsTab } from "./components/AssetsTab";
import { RiddlesTab } from "./components/RiddlesTab";
import { RiddleGraphTab } from "./components/RiddleGraphTab";
import { GameEndTab } from "./components/GameEndTab";
import { ReviewTab } from "./components/ReviewTab";
import { InPageNavigation } from "./components/InPageNavigation";
import { useErrorCheck } from "./hooks/useErrorCheck";
import { withTouchedTab } from "./data/TabTouchState";
import { ErrorChecker, type IssueReport } from "./data/ErrorChecker";
import {
  productionIssueReport,
  type ProjectValidationReport,
} from "./data/ProjectValidationReport";
import { prepareProductionRequest } from "./data/prepareProductionRequest";
import { BrowserDraftStorage, type DraftSummary } from "./data/DraftStorage";
import { BrowserAssetStorage } from "./data/AssetStorage";
import {
  acquireEditorSessionLock,
  type EditorSessionLock,
} from "./data/EditorSessionLock";
import { useWizardStorage, WizardStorageProvider, type WizardStoragePort } from "./data/WizardStorage";
import { BrowserWizardHost, detectNativeHost, NativeWizardHost } from "./data/NativeWizardHost";
import {
  cloneDraft,
  createSeed,
  createWizardDraft,
  type UpdateDraft,
  type WizardDraft,
} from "./data/WizardDraft";
import { Alert, AlertDescription, AlertTitle } from "./components/ui/alert";
import { Button } from "./components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "./components/ui/dialog";
import { UploadReferencesProvider } from "./components/assets/UploadReferencesContext";
import { getAssetDisplayName, isBundledAssetPath } from "./components/assets/assetPaths";
import { VALIDATED_TAB_IDS, type TabId } from "./data/Tabs";
import type { WizardWork } from "./data/WizardWork";

type SaveState = "unsaved" | "saving" | "saved" | "error";

interface ProductionValidationState {
  report: ProjectValidationReport;
  snapshot: WizardDraft;
}

interface DraftListEntry extends DraftSummary {
  problemCount: number | null;
}

const productionContentKey = (draft: WizardDraft) => JSON.stringify([
  draft.seed,
  draft.project,
  draft.uploads,
]);

function wizardRoomFileName(title: string) {
  const safeTitle = title
    .replace(/\s+/g, "")
    .replace(/[<>:"/\\|?*]/g, "-")
    .replace(/\.+$/g, "");
  return `${safeTitle || "Spiel"}-WizardRoom.jar`;
}

function downloadWizardRoom(jar: Blob, title: string) {
  const url = URL.createObjectURL(jar);
  const link = document.createElement("a");
  link.href = url;
  link.download = wizardRoomFileName(title);
  document.body.append(link);
  link.click();
  link.remove();
  window.setTimeout(() => URL.revokeObjectURL(url), 1000);
}

function mergeIssueReports(local: IssueReport, production: IssueReport | null): IssueReport {
  if (production === null) return local;
  const tabIds = new Set([...Object.keys(local), ...Object.keys(production)]);
  return Object.fromEntries([...tabIds].map((tabId) => [
    tabId,
    { ...local[tabId], ...production[tabId] },
  ]));
}

const createAllTouchedTabs = (): WizardDraft["ui"]["touchedTabs"] =>
  Object.fromEntries(VALIDATED_TAB_IDS.map((tabId) => [tabId, true])) as WizardDraft["ui"]["touchedTabs"];

async function createStorage(): Promise<WizardStoragePort> {
  requestDurableStorage();
  const assets = new BrowserAssetStorage();
  return {
    drafts: new BrowserDraftStorage(),
    assets,
    host: await detectNativeHost() ? new NativeWizardHost() : new BrowserWizardHost(),
  };
}

/** Best-effort request so the browser avoids evicting IndexedDB drafts under storage pressure. */
function requestDurableStorage(): void {
  try {
    void navigator.storage?.persist?.().catch(() => {});
  } catch {
    // Browsers without the Storage Manager keep plain best-effort storage.
  }
}

/** Uses the local editor rules and stored uploads without invoking Java. */
async function createDraftListEntry(
  storage: WizardStoragePort,
  summary: DraftSummary,
): Promise<DraftListEntry> {
  try {
    const draft = await storage.drafts.load(summary.draftId);
    if (draft === null) return { ...summary, problemCount: null };

    const customAssets = draft.project.assets.filter((asset) => !isBundledAssetPath(asset.path));
    const storedUploadKeys = customAssets.length === 0
      ? new Set<string>()
      : new Set(await storage.assets.listAssetIds(summary.draftId));
    const storedAssetIds = new Set(
      customAssets
        .filter((asset) => {
          const storageKey = draft.uploads[asset.id]?.storageKey;
          return storageKey !== undefined && storedUploadKeys.has(storageKey);
        })
        .map((asset) => asset.id),
    );
    const assetDisplayNames = new Map(draft.project.assets.map((asset) => [
      asset.id,
      getAssetDisplayName(asset, draft.uploads[asset.id]),
    ]));
    const report = new ErrorChecker({ storedAssetIds, assetDisplayNames }).check(draft.project);
    const problemCount = ErrorChecker.getSortedIssues(report)
      .filter((issue) => issue.severity === "error").length;
    return { ...summary, problemCount };
  } catch {
    return { ...summary, problemCount: null };
  }
}

function App() {
  const [storage, setStorage] = React.useState<WizardStoragePort | null>(null);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    let cancelled = false;
    createStorage().then((next) => { if (!cancelled) setStorage(next); }).catch((cause) => {
      if (!cancelled) setError(cause instanceof Error ? cause.message : "Die Anwendung konnte nicht gestartet werden.");
    });
    return () => { cancelled = true; };
  }, []);

  if (error) return <FatalError message={error} />;
  if (!storage) return <p className="p-8 text-center text-muted-foreground">Wizard wird gestartet…</p>;
  return <WizardStorageProvider value={storage}><WizardWorkspace /></WizardStorageProvider>;
}

function FatalError({ message }: { message: string }) {
  return (
    <div className="mx-auto max-w-3xl p-8">
      <Alert variant="destructive"><AlertTitle>Wizard kann nicht gestartet werden</AlertTitle><AlertDescription>{message}</AlertDescription></Alert>
    </div>
  );
}

function WizardWorkspace() {
  const storage = useWizardStorage();
  const [drafts, setDrafts] = React.useState<DraftListEntry[] | null>(null);
  const [draft, setDraft] = React.useState<WizardDraft | null>(null);
  const [error, setError] = React.useState<string | null>(null);
  const [busy, setBusy] = React.useState(false);
  const [deleteDraft, setDeleteDraft] = React.useState<DraftSummary | null>(null);
  const [deleteError, setDeleteError] = React.useState<string | null>(null);
  const [deleting, setDeleting] = React.useState(false);
  const sessionLockRef = React.useRef<EditorSessionLock | null>(null);
  const [sessionBlocked, setSessionBlocked] = React.useState<"held" | "unsupported" | null>(null);

  const refresh = React.useCallback(async () => {
    try {
      const summaries = await storage.drafts.list();
      setDrafts(await Promise.all(summaries.map((summary) => createDraftListEntry(storage, summary))));
      setError(null);
    }
    catch (cause) { setError(cause instanceof Error ? cause.message : "Die Entwürfe konnten nicht geladen werden."); }
  }, [storage]);
  React.useEffect(() => { void refresh(); }, [refresh]);

  const acquireEditorSession = React.useCallback(async (): Promise<boolean> => {
    if (sessionLockRef.current) return true;
    const result = await acquireEditorSessionLock();
    if (result.status !== "acquired") {
      setSessionBlocked(result.status);
      return false;
    }
    sessionLockRef.current = result.lock;
    setSessionBlocked(null);
    return true;
  }, []);
  const releaseEditorSession = React.useCallback(() => {
    sessionLockRef.current?.release();
    sessionLockRef.current = null;
  }, []);

  const openDraft = async (draftId: string) => {
    if (!(await acquireEditorSession())) return;
    setBusy(true);
    try {
      const loaded = await storage.drafts.loadForEditing(draftId);
      if (!loaded) throw new Error("Der Entwurf ist nicht mehr vorhanden.");
      setDraft(loaded); setError(null);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Der Entwurf konnte nicht geöffnet werden.");
      releaseEditorSession();
    }
    finally { setBusy(false); }
  };
  const createDraft = async () => {
    if (!(await acquireEditorSession())) return;
    setBusy(true);
    try {
      const saved = await storage.drafts.save(createWizardDraft());
      setDraft(saved); setError(null);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Der Entwurf konnte nicht angelegt werden.");
      releaseEditorSession();
    }
    finally { setBusy(false); }
  };
  const deleteSelectedDraft = async () => {
    const selected = deleteDraft;
    if (selected === null || deleting) return;
    setDeleting(true);
    setDeleteError(null);
    let deleteLock: EditorSessionLock | null = null;
    try {
      const result = await acquireEditorSessionLock();
      if (result.status !== "acquired") {
        setDeleteError(result.status === "unsupported"
          ? "Dieser Browser kann das Spiel nicht sicher löschen. Öffne den Wizard in einem aktuellen unterstützten Browser."
          : "Dieses Spiel wird gerade in einem anderen Tab bearbeitet. Beende dort die Bearbeitung, bevor du es löschst.");
        return;
      }
      deleteLock = result.lock;
      setSessionBlocked(null);
      await storage.drafts.delete(selected.draftId);
      setDrafts((current) => current?.filter((summary) => summary.draftId !== selected.draftId) ?? current);
      setDeleteDraft(null);
      await refresh();
    } catch (cause) {
      try {
        const currentDrafts = await storage.drafts.list();
        setDrafts(await Promise.all(
          currentDrafts.map((summary) => createDraftListEntry(storage, summary)),
        ));
        const current = currentDrafts.find((draftSummary) => draftSummary.draftId === selected.draftId);
        if (current === undefined) {
          setDeleteDraft(null);
          setDeleteError(null);
          setError(null);
          return;
        }
        setDeleteDraft(current);
        setError(null);
      } catch {
        // Keep the original card and dialog if the uncertain state cannot be refreshed.
      }
      setDeleteError(cause instanceof Error
        ? cause.message
        : "Der Entwurf konnte nicht gelöscht werden. Er bleibt gespeichert. Versuche es erneut.");
    } finally {
      deleteLock?.release();
      setDeleting(false);
    }
  };

  if (draft) {
    return <DraftEditor key={draft.draftId} initialDraft={draft} onClose={async () => { releaseEditorSession(); setDraft(null); await refresh(); }} />;
  }
  return (
    <div className="wizard-launcher min-h-screen">
      <section className="wizard-launcher-hero" aria-labelledby="wizard-launcher-title">
        <div className="wizard-launcher-image" aria-hidden="true" />
        <div className="wizard-launcher-hero-shade" aria-hidden="true" />
        <div className="wizard-launcher-theme">
          <ThemeToggle className="wizard-launcher-theme-button" />
        </div>
        <div className="wizard-launcher-hero-inner">
          <div className="wizard-launcher-copy">
            <p className="wizard-launcher-brand">
              <span className="wizard-launcher-brand-mark" aria-hidden="true" />
              Dungeon Wizard
            </p>
            <h1 id="wizard-launcher-title" className="wizard-launcher-title">
              Welches Spiel möchtest du bearbeiten?
            </h1>
            <p className="wizard-launcher-intro">
              Öffne einen vorhandenen Entwurf oder beginne ein neues Spiel.
            </p>
            <Button
              size="lg"
              className="wizard-launcher-action gap-2 font-semibold"
              onClick={() => void createDraft()}
              disabled={busy || deleting}
            >
              {busy ? <LoaderCircleIcon className="size-4 animate-spin" /> : <PlusIcon className="size-4" />}
              {busy ? "Spiel wird angelegt…" : "Neues Spiel"}
            </Button>
          </div>
        </div>
      </section>

      <div className="wizard-library mx-auto flex max-w-4xl flex-col gap-6 px-4 py-10 sm:px-6 md:py-14">
        <div className="wizard-library-header flex items-end justify-between gap-4 border-b pb-4">
          <div>
            <p className="wizard-library-kicker mb-1 text-xs font-semibold uppercase tracking-[0.16em]">Weitermachen</p>
            <h2 className="wizard-library-title text-2xl font-semibold tracking-tight">Meine Spiele</h2>
          </div>
          {drafts && drafts.length > 0 && (
            <span className="wizard-library-count text-xs">
              {drafts.length} {drafts.length === 1 ? "Spiel" : "Spiele"}
            </span>
          )}
        </div>

        {error && (
          <Alert variant="destructive">
            <AlertTitle>Entwürfe nicht verfügbar</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {sessionBlocked === "held" && (
          <Alert variant="destructive">
            <AlertTitle>Wizard ist bereits in einem anderen Tab geöffnet</AlertTitle>
            <AlertDescription>
              Schließe den anderen Tab oder beende dort die Bearbeitung, bevor du hier ein Spiel öffnest oder anlegst.
            </AlertDescription>
          </Alert>
        )}

        {sessionBlocked === "unsupported" && (
          <Alert variant="destructive">
            <AlertTitle>Dieser Browser wird nicht unterstützt</AlertTitle>
            <AlertDescription>
              Öffne den Wizard in einem aktuellen unterstützten Browser, damit deine Spiele vor gleichzeitigen Änderungen geschützt sind.
            </AlertDescription>
          </Alert>
        )}

        <div className="flex flex-col gap-3">
          {drafts === null && (
            <div className="flex items-center justify-center py-12 text-sm text-muted-foreground">
              <LoaderCircleIcon className="size-5 animate-spin mr-2" />
              Entwürfe werden geladen…
            </div>
          )}
          {drafts?.length === 0 && (
            <div className="wizard-empty-library flex flex-col items-center justify-center text-center">
              <div className="wizard-empty-library-icon mb-4 flex size-12 items-center justify-center rounded-xl">
                <FolderOpenIcon className="size-6" />
              </div>
              <h3 className="wizard-empty-library-title text-base font-semibold">Noch keine Spiele angelegt</h3>
              <p className="wizard-empty-library-desc mt-1 max-w-sm text-sm">
                Mit „Neues Spiel“ beginnst du deinen ersten Entwurf.
              </p>
            </div>
          )}
          {drafts?.map((summary) => (
            <div
              key={summary.draftId}
              className="wizard-draft-row group relative flex items-stretch focus-within:ring-2 focus-within:ring-ring"
            >
              <button
                type="button"
                disabled={busy || deleting}
                onClick={() => void openDraft(summary.draftId)}
                className="flex min-w-0 flex-1 items-center justify-between gap-4 p-4 text-left outline-none cursor-pointer sm:px-5"
              >
                <span className="min-w-0">
                  <span className="wizard-draft-title block truncate text-base">
                    {summary.title.trim() || "Unbenanntes Spiel"}
                  </span>
                  <span className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs">
                    <span className="wizard-draft-meta inline-flex items-center gap-1.5">
                      <ClockIcon className="wizard-draft-meta-icon size-3.5 shrink-0" />
                      {summary.savedAt
                        ? `Zuletzt gespeichert: ${new Date(summary.savedAt).toLocaleString("de-DE")}`
                        : "Noch nicht gespeichert"}
                    </span>
                    <DraftReadinessIndicator problemCount={summary.problemCount} />
                  </span>
                </span>
                <span className="wizard-draft-open-action hidden shrink-0 items-center gap-2 text-sm font-medium sm:flex">
                  Öffnen
                  <FolderOpenIcon className="size-4" />
                </span>
              </button>
              <div className="wizard-draft-divider flex items-center border-l px-2 sm:px-3">
                <Button
                  type="button"
                  variant="ghost"
                  size="icon-sm"
                  className="text-muted-foreground hover:bg-destructive/15 hover:text-destructive focus-visible:border-destructive/40 focus-visible:ring-destructive/20"
                  disabled={busy || deleting}
                  aria-label={`Entwurf ${summary.title.trim() || "Unbenanntes Spiel"} endgültig löschen`}
                  onClick={() => { setDeleteDraft(summary); setDeleteError(null); }}
                >
                  <Trash2Icon className="size-4" />
                </Button>
              </div>
            </div>
          ))}
        </div>

        {!storage.host.native && (
          <Alert className="wizard-dev-banner mt-4">
            <InfoIcon className="size-4 text-muted-foreground" />
            <AlertTitle className="text-sm font-medium">Separater Entwicklungs- und UI-Testmodus</AlertTitle>
            <AlertDescription className="text-xs text-muted-foreground">
              Diese Entwürfe bleiben ausschließlich in diesem Browser. Sie können nicht in die lokale Wizard-Anwendung übertragen, vollständig geprüft oder als Spiel verpackt werden.
            </AlertDescription>
          </Alert>
        )}

        <Dialog
          open={deleteDraft !== null}
          onOpenChange={(open) => { if (!open && !deleting) { setDeleteDraft(null); setDeleteError(null); } }}
        >
          <DialogContent showCloseButton={false}>
            <DialogHeader>
              <DialogTitle>Entwurf endgültig löschen?</DialogTitle>
              <DialogDescription>
                Der Entwurf „{deleteDraft?.title.trim() || "Unbenanntes Spiel"}“ und alle im Wizard gespeicherten Uploads werden dauerhaft gelöscht. Bereits heruntergeladene Spieldateien bleiben erhalten. Diese Aktion kann nicht rückgängig gemacht werden.
              </DialogDescription>
            </DialogHeader>
            {deleteError && (
              <Alert variant="destructive">
                <AlertTitle>Löschen nicht möglich</AlertTitle>
                <AlertDescription>{deleteError}</AlertDescription>
              </Alert>
            )}
            <DialogFooter>
              <Button type="button" variant="outline" disabled={deleting} onClick={() => { setDeleteDraft(null); setDeleteError(null); }}>Abbrechen</Button>
              <Button type="button" variant="destructive" disabled={deleting} onClick={() => void deleteSelectedDraft()}>
                {deleting ? "Wird gelöscht…" : "Endgültig löschen"}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </div>
  );
}

function DraftReadinessIndicator({ problemCount }: { problemCount: number | null }) {
  if (problemCount === null) {
    return (
      <span className="wizard-draft-badge wizard-draft-badge-unknown">
        <InfoIcon className="size-3.5 shrink-0" />
        Prüfung nicht möglich
      </span>
    );
  }
  if (problemCount === 0) {
    return (
      <span className="wizard-draft-badge wizard-draft-badge-ready">
        <CircleCheckIcon className="size-3.5 shrink-0" />
        Lokal vollständig
      </span>
    );
  }
  return (
    <span className="wizard-draft-badge wizard-draft-badge-issues">
      <CircleAlertIcon className="size-3.5 shrink-0" />
      {problemCount} {problemCount === 1 ? "Problem" : "Probleme"}
    </span>
  );
}

function SaveStatusIndicator({ state }: { state: SaveState }) {
  switch (state) {
    case "saving":
      return (
        <span className="inline-flex items-center gap-1.5 rounded-full bg-muted/80 px-2.5 py-1 text-xs font-medium leading-none text-muted-foreground">
          <LoaderCircleIcon className="size-3.5 animate-spin" />
          <span className="hidden sm:inline">Wird gespeichert…</span>
        </span>
      );
    case "saved":
      return (
        <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-500/10 px-2.5 py-1 text-xs font-medium leading-none text-status-success">
          <CheckIcon className="size-3.5" />
          <span>Gespeichert</span>
        </span>
      );
    case "unsaved":
      return (
        <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-500/10 px-2.5 py-1 text-xs font-medium leading-none text-status-warning">
          <span className="size-1.5 rounded-full bg-status-warning animate-pulse" />
          <span className="hidden sm:inline">Ungespeichert</span>
        </span>
      );
    case "error":
      return (
        <span className="inline-flex items-center gap-1.5 rounded-full bg-destructive/15 px-2.5 py-1 text-xs font-medium leading-none text-destructive">
          <XCircleIcon className="size-3.5" />
          <span>Speichern fehlgeschlagen</span>
        </span>
      );
  }
}

function DraftEditor({ initialDraft, onClose }: { initialDraft: WizardDraft; onClose: () => Promise<void> }) {
  const storage = useWizardStorage();
  const [draft, setDraft] = React.useState(initialDraft);
  const [saveState, setSaveState] = React.useState<SaveState>("saved");
  const [wizardWork, setWizardWork] = React.useState<WizardWork>(null);
  const wizardWorkRef = React.useRef<WizardWork>(null);
  const latestDraftRef = React.useRef(initialDraft);
  const revisionRef = React.useRef(0);
  const savedRevisionRef = React.useRef(0);
  const timerRef = React.useRef<number | null>(null);
  const savePromiseRef = React.useRef<Promise<void> | null>(null);
  const productionValidationTimerRef = React.useRef<number | null>(null);
  const attemptedProductionContentRef = React.useRef<string | null>(null);
  const productionContentRef = React.useRef({
    key: productionContentKey(initialDraft),
    changedAt: performance.now(),
  });
  const localReadyRef = React.useRef(false);
  const [productionValidation, setProductionValidation] =
    React.useState<ProductionValidationState | null>(null);
  const [productionTechnicalError, setProductionTechnicalError] = React.useState<{
    snapshot: WizardDraft;
    action: "validating" | "packaging";
  } | null>(null);
  const [downloadedContentKey, setDownloadedContentKey] = React.useState<string | null>(null);

  const drainSaves = React.useCallback((): Promise<void> => {
    if (timerRef.current !== null) { window.clearTimeout(timerRef.current); timerRef.current = null; }
    if (savePromiseRef.current) return savePromiseRef.current;
    const run = (async () => {
      while (savedRevisionRef.current < revisionRef.current) {
        const savingRevision = revisionRef.current;
        const snapshot = cloneDraft(latestDraftRef.current);
        setSaveState("saving");
        try {
          const saved = await storage.drafts.save(snapshot);
          savedRevisionRef.current = savingRevision;
          const current = revisionRef.current === savingRevision
            ? cloneDraft(saved)
            : cloneDraft(latestDraftRef.current);
          current.savedAt = saved.savedAt;
          latestDraftRef.current = current;
          setDraft(current);
          setSaveState(revisionRef.current === savingRevision ? "saved" : "unsaved");
        } catch (cause) {
          setSaveState("error");
          throw cause;
        }
      }
    })().finally(() => { savePromiseRef.current = null; });
    savePromiseRef.current = run;
    return run;
  }, [storage]);

  const scheduleSave = React.useCallback(() => {
    if (timerRef.current !== null) window.clearTimeout(timerRef.current);
    timerRef.current = window.setTimeout(() => {
      timerRef.current = null;
      void drainSaves().catch((cause) => toast.error("Der Entwurf konnte nicht gespeichert werden.", { description: cause instanceof Error ? cause.message : undefined }));
    }, 500);
  }, [drainSaves]);

  React.useEffect(() => () => { if (timerRef.current !== null) window.clearTimeout(timerRef.current); }, []);
  React.useEffect(() => {
    const warnBeforeUnload = (event: BeforeUnloadEvent) => {
      if (wizardWorkRef.current === null
        && savedRevisionRef.current >= revisionRef.current
        && savePromiseRef.current === null) return;
      event.preventDefault();
    };
    window.addEventListener("beforeunload", warnBeforeUnload);
    return () => window.removeEventListener("beforeunload", warnBeforeUnload);
  }, []);

  const beginWizardWork = React.useCallback((work: Exclude<WizardWork, null>) => {
    if (wizardWorkRef.current !== null) return false;
    wizardWorkRef.current = work;
    setWizardWork(work);
    return true;
  }, []);
  const finishWizardWork = React.useCallback((work: Exclude<WizardWork, null>) => {
    if (wizardWorkRef.current !== work) return;
    wizardWorkRef.current = null;
    setWizardWork(null);
  }, []);

  const updateDraft = React.useCallback<UpdateDraft>((transform) => {
    const snapshot = cloneDraft(latestDraftRef.current);
    if (transform(snapshot) === false) return;
    const nextProductionContentKey = productionContentKey(snapshot);
    if (nextProductionContentKey !== productionContentRef.current.key) {
      productionContentRef.current = {
        key: nextProductionContentKey,
        changedAt: performance.now(),
      };
    }
    latestDraftRef.current = snapshot;
    revisionRef.current += 1;
    setDraft(snapshot);
    setSaveState("unsaved");
    scheduleSave();
  }, [scheduleSave]);

  const flush = React.useCallback(async (): Promise<WizardDraft> => {
    while (savedRevisionRef.current < revisionRef.current || savePromiseRef.current) await drainSaves();
    return cloneDraft(latestDraftRef.current);
  }, [drainSaves]);

  React.useEffect(() => {
    const touchedTabs = withTouchedTab(draft.ui.touchedTabs, draft.ui.activeTab);
    if (touchedTabs !== draft.ui.touchedTabs) {
      updateDraft((current) => {
        const latestTouchedTabs = withTouchedTab(current.ui.touchedTabs, current.ui.activeTab);
        if (latestTouchedTabs === current.ui.touchedTabs) return false;
        current.ui.touchedTabs = latestTouchedTabs;
      });
    }
  }, [draft.ui.activeTab, draft.ui.touchedTabs, updateDraft]);

  const project = draft.project;
  const tab = draft.ui.activeTab;
  const touchedTabs = draft.ui.touchedTabs;
  const contentKey = productionContentKey(draft);
  const { issueReport: localIssueReport, assetStorageStatus } = useErrorCheck(
    draft.draftId,
    project,
    draft.uploads,
  );
  const currentProduction = productionValidation
    && productionContentKey(productionValidation.snapshot) === contentKey
    ? productionValidation
    : null;
  const currentTechnicalError = productionTechnicalError
    && productionContentKey(productionTechnicalError.snapshot) === contentKey
    ? productionTechnicalError.action
    : null;
  const issueReport = React.useMemo(
    () => mergeIssueReports(
      localIssueReport,
      currentProduction
        ? productionIssueReport(currentProduction.report, currentProduction.snapshot)
        : null,
    ),
    [currentProduction, localIssueReport],
  );
  const localErrorCount = ErrorChecker.getSortedIssues(localIssueReport)
    .filter((issue) => issue.severity === "error").length;
  const localReady = assetStorageStatus === "ready" && localErrorCount === 0;
  React.useLayoutEffect(() => {
    localReadyRef.current = localReady;
  }, [localReady]);
  const updateProject = (updatedProject: DeerProject) => updateDraft((current) => { current.project = structuredClone(updatedProject); });
  const navigateToTab = (activeTab: TabId) => {
    if (wizardWorkRef.current === "uploading") return;
    updateDraft((current) => {
      current.ui.activeTab = activeTab;
      current.ui.touchedTabs = activeTab === "review"
        ? createAllTouchedTabs()
        : withTouchedTab(current.ui.touchedTabs, activeTab);
    });
  };
  const setTab = (activeTab: TabId) => navigateToTab(activeTab);
  const selectIssueTab = (activeTab: TabId) => navigateToTab(activeTab);
  const hasTouchedAllTabs = Object.values(touchedTabs).every((touched) => touched);
  React.useEffect(() => {
    if (tab !== "review" || hasTouchedAllTabs) return;
    updateDraft((current) => {
      if (Object.values(current.ui.touchedTabs).every((touched) => touched)) return false;
      current.ui.touchedTabs = createAllTouchedTabs();
    });
  }, [hasTouchedAllTabs, tab, updateDraft]);
  const currentDraftSnapshot = React.useCallback(
    () => cloneDraft(latestDraftRef.current),
    [],
  );
  const acceptProductionReport = React.useCallback((
    report: ProjectValidationReport,
    snapshot: WizardDraft,
    action: "validating" | "packaging",
  ) => {
    const snapshotContentKey = productionContentKey(snapshot);
    if (snapshotContentKey !== productionContentKey(latestDraftRef.current)) {
      if (action === "validating" && attemptedProductionContentRef.current === snapshotContentKey) {
        attemptedProductionContentRef.current = null;
      }
      return;
    }
    const technicalIssue = report.issues.find((issue) =>
      issue.messageKey === "validation.internal_error"
      || issue.messageKey === "validation.derivation.failed"
      || issue.messageKey === "validation.input.changed_during_read",
    );
    if (technicalIssue) {
      if (action === "validating") setProductionValidation(null);
      setProductionTechnicalError({ snapshot, action });
      return;
    }
    setProductionTechnicalError(null);
    setProductionValidation({ report, snapshot });
  }, []);
  const acceptProductionTechnicalError = React.useCallback((
    action: "validating" | "packaging",
    snapshot: WizardDraft,
  ) => {
    const snapshotContentKey = productionContentKey(snapshot);
    if (snapshotContentKey !== productionContentKey(latestDraftRef.current)) {
      if (action === "validating" && attemptedProductionContentRef.current === snapshotContentKey) {
        attemptedProductionContentRef.current = null;
      }
      return;
    }
    if (action === "validating") setProductionValidation(null);
    setProductionTechnicalError({ snapshot, action });
  }, []);
  const acceptProductionDownload = React.useCallback((jar: Blob, snapshot: WizardDraft) => {
    if (productionContentKey(snapshot) !== productionContentKey(latestDraftRef.current)) return;
    downloadWizardRoom(jar, snapshot.project.metadata.title);
    setDownloadedContentKey(productionContentKey(snapshot));
    setProductionTechnicalError(null);
  }, []);
  const clearProductionTechnicalError = React.useCallback(
    () => setProductionTechnicalError(null),
    [],
  );

  React.useEffect(() => {
    if (productionValidationTimerRef.current !== null) {
      window.clearTimeout(productionValidationTimerRef.current);
      productionValidationTimerRef.current = null;
    }
    if (!storage.host.native || !localReady || wizardWork !== null
      || currentProduction !== null
      || attemptedProductionContentRef.current === contentKey) return;
    if (draft.seed === undefined) {
      updateDraft((current) => {
        if (current.seed !== undefined) return false;
        current.seed = createSeed();
      });
      return;
    }

    const capturedContent = productionContentRef.current;
    const remainingDebounce = Math.max(
      0,
      Math.ceil(2000 - (performance.now() - capturedContent.changedAt)),
    );
    productionValidationTimerRef.current = window.setTimeout(() => {
      productionValidationTimerRef.current = null;
      const latestContent = productionContentRef.current;
      if (latestContent.key !== capturedContent.key
        || productionContentKey(latestDraftRef.current) !== capturedContent.key
        || performance.now() - latestContent.changedAt < 2000
        || !localReadyRef.current
        || wizardWorkRef.current !== null
        || attemptedProductionContentRef.current === capturedContent.key
        || !beginWizardWork("validating")) return;
      attemptedProductionContentRef.current = capturedContent.key;
      setProductionTechnicalError(null);
      const attempt = { snapshot: cloneDraft(latestDraftRef.current) };
      void (async () => {
        try {
          const snapshot = await flush();
          if (productionContentKey(snapshot) !== capturedContent.key
            || productionContentRef.current.key !== capturedContent.key
            || productionContentKey(latestDraftRef.current) !== capturedContent.key) {
            if (attemptedProductionContentRef.current === capturedContent.key) {
              attemptedProductionContentRef.current = null;
            }
            return;
          }
          attempt.snapshot = snapshot;
          const prepared = await prepareProductionRequest(storage, snapshot);
          const report = await storage.host.validate(prepared.request);
          acceptProductionReport(report, prepared.snapshot, "validating");
        } catch {
          acceptProductionTechnicalError("validating", attempt.snapshot);
        } finally {
          finishWizardWork("validating");
        }
      })();
    }, remainingDebounce);

    return () => {
      if (productionValidationTimerRef.current !== null) {
        window.clearTimeout(productionValidationTimerRef.current);
        productionValidationTimerRef.current = null;
      }
    };
  }, [
    acceptProductionReport,
    acceptProductionTechnicalError,
    beginWizardWork,
    contentKey,
    currentProduction,
    draft.seed,
    finishWizardWork,
    flush,
    localReady,
    storage,
    updateDraft,
    wizardWork,
  ]);

  return (
    <UploadReferencesProvider draftId={draft.draftId} value={draft.uploads}>
      <div className="min-h-screen bg-background text-foreground">
        <header className="sticky top-0 z-30 border-b border-border bg-background/95 backdrop-blur-sm">
          <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3 sm:px-6">
            <Button
              variant="ghost"
              size="sm"
              aria-label="Meine Spiele"
              title="Meine Spiele"
              disabled={wizardWork !== null}
              onClick={() => {
                if (wizardWorkRef.current !== null) return;
                void flush().then(onClose).catch((cause) => toast.error("Bitte speichere den Entwurf, bevor du zurückgehst.", { description: cause instanceof Error ? cause.message : undefined }));
              }}
              className="gap-1.5 text-muted-foreground hover:text-foreground"
            >
              <ArrowLeftIcon className="size-4" />
              <span className="hidden sm:inline">Meine Spiele</span>
            </Button>

            <h1 className="min-w-0 flex-1 truncate text-center text-base font-semibold leading-tight text-foreground sm:max-w-md sm:text-lg">
              {project.metadata.title.trim() || "Neues Spiel"}
            </h1>

            <div className="flex shrink-0 items-center gap-2">
              <SaveStatusIndicator state={saveState} />
              <ThemeToggle />
            </div>
          </div>
        </header>

        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6">
          <div className="grid grid-cols-1 lg:grid-cols-[300px_1fr] gap-6 items-start">
            <div className="lg:sticky lg:top-18 flex flex-col gap-4">
              <SidebarNavigation
                issueReport={issueReport}
                touchedTabs={touchedTabs}
                tab={tab}
                setTab={setTab}
                disabled={wizardWork === "uploading"}
              />
              <ErrorDetector
                issueReport={issueReport}
                assetStorageStatus={assetStorageStatus}
                touchedAll={hasTouchedAllTabs}
                touchedTabs={touchedTabs}
                currentTab={tab}
                productionReady={currentProduction?.report.valid === true}
                technicalError={currentTechnicalError}
                onIssueSelect={selectIssueTab}
                issueNavigationDisabled={wizardWork === "uploading"}
                className="hidden lg:block"
              />
            </div>

            <div className="panel min-w-0">
              {tab === "metadata" && <MetadataTab deerSchema={project} updateDeerSchema={updateProject} issues={localIssueReport.metadata} />}
              {tab === "scenario" && <ScenarioTab deerSchema={project} updateDeerSchema={updateProject} issues={localIssueReport.scenario} />}
              {tab === "session" && <SessionTab deerSchema={project} updateDeerSchema={updateProject} issues={localIssueReport.session} />}
              {tab === "assets" && (
                <AssetsTab
                  draft={draft}
                  updateDraft={updateDraft}
                  flush={flush}
                  work={wizardWork}
                  beginWork={beginWizardWork}
                  finishWork={finishWizardWork}
                  issues={localIssueReport.assets}
                />
              )}
              {tab === "riddles" && <RiddlesTab draft={draft} updateDraft={updateDraft} issues={localIssueReport.riddles} />}
              {tab === "riddle_graph" && (
                <RiddleGraphTab
                  draft={draft}
                  updateDraft={updateDraft}
                  issues={localIssueReport.riddle_graph}
                  riddleIssues={localIssueReport.riddles}
                />
              )}
              {tab === "game_end" && <GameEndTab deerSchema={project} updateDeerSchema={updateProject} issues={localIssueReport.game_end} />}
              {tab === "review" && (
                <ReviewTab
                  flush={flush}
                  currentDraftSnapshot={currentDraftSnapshot}
                  work={wizardWork}
                  localReady={localReady}
                  productionReport={currentProduction?.report ?? null}
                  beginWork={beginWizardWork}
                  finishWork={finishWizardWork}
                  acceptReport={acceptProductionReport}
                  acceptTechnicalError={acceptProductionTechnicalError}
                  acceptDownload={acceptProductionDownload}
                  clearTechnicalError={clearProductionTechnicalError}
                  downloadReady={downloadedContentKey === contentKey}
                />
              )}
              <InPageNavigation tab={tab} setTab={setTab} disabled={wizardWork === "uploading"} />
            </div>

            <ErrorDetector
              issueReport={issueReport}
              assetStorageStatus={assetStorageStatus}
              touchedAll={hasTouchedAllTabs}
              touchedTabs={touchedTabs}
              currentTab={tab}
              productionReady={currentProduction?.report.valid === true}
              technicalError={currentTechnicalError}
              onIssueSelect={selectIssueTab}
              issueNavigationDisabled={wizardWork === "uploading"}
              className="lg:hidden"
            />
          </div>
        </div>
      </div>
    </UploadReferencesProvider>
  );
}

function Layout() {
  return (
    <ThemeProvider
      attribute="class"
      defaultTheme="system"
      enableSystem
      storageKey="dungeon-wizard-theme"
      disableTransitionOnChange
    >
      <main className="min-h-screen bg-background [font-family:var(--font-geist)]">
        <App />
      </main>
      <Toaster position="bottom-right" richColors />
    </ThemeProvider>
  );
}

export default Layout;
