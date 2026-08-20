import React from "react";
import { toast, Toaster } from "sonner";
import { ArrowLeftIcon, FolderOpenIcon, PlusIcon, Trash2Icon } from "lucide-react";
import "./App.css";
import { ThemeProvider } from "./components/ThemeProvider";
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
import { BrowserDraftStorage, type DraftSummary } from "./data/DraftStorage";
import { BrowserAssetStorage } from "./data/AssetStorage";
import { useWizardStorage, WizardStorageProvider, type WizardStoragePort } from "./data/WizardStorage";
import { BrowserWizardHost, detectNativeHost, NativeWizardHost } from "./data/NativeWizardHost";
import { cloneDraft, createWizardDraft, type UpdateDraft, type WizardDraft } from "./data/WizardDraft";
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
import type { TabId } from "./data/Tabs";
import type { WizardWork } from "./data/WizardWork";

type SaveState = "unsaved" | "saving" | "saved" | "error";

async function createStorage(): Promise<WizardStoragePort> {
  const assets = new BrowserAssetStorage();
  return {
    drafts: new BrowserDraftStorage(),
    assets,
    host: await detectNativeHost() ? new NativeWizardHost() : new BrowserWizardHost(),
  };
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
  const [drafts, setDrafts] = React.useState<DraftSummary[] | null>(null);
  const [draft, setDraft] = React.useState<WizardDraft | null>(null);
  const [error, setError] = React.useState<string | null>(null);
  const [busy, setBusy] = React.useState(false);
  const [deleteDraft, setDeleteDraft] = React.useState<DraftSummary | null>(null);
  const [deleteError, setDeleteError] = React.useState<string | null>(null);
  const [deleting, setDeleting] = React.useState(false);

  const refresh = React.useCallback(async () => {
    try { setDrafts(await storage.drafts.list()); setError(null); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "Die Entwürfe konnten nicht geladen werden."); }
  }, [storage]);
  React.useEffect(() => { void refresh(); }, [refresh]);

  const openDraft = async (draftId: string) => {
    setBusy(true);
    try {
      const loaded = await storage.drafts.load(draftId);
      if (!loaded) throw new Error("Der Entwurf ist nicht mehr vorhanden.");
      setDraft(loaded); setError(null);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Der Entwurf konnte nicht geöffnet werden."); }
    finally { setBusy(false); }
  };
  const createDraft = async () => {
    setBusy(true);
    try {
      const saved = await storage.drafts.save(createWizardDraft());
      setDraft(saved); setError(null);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Der Entwurf konnte nicht angelegt werden."); }
    finally { setBusy(false); }
  };
  const deleteSelectedDraft = async () => {
    const selected = deleteDraft;
    if (selected === null || deleting) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await storage.drafts.delete(selected.draftId);
      setDrafts((current) => current?.filter((summary) => summary.draftId !== selected.draftId) ?? current);
      setDeleteDraft(null);
      await refresh();
    } catch (cause) {
      try {
        const currentDrafts = await storage.drafts.list();
        setDrafts(currentDrafts);
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
      setDeleting(false);
    }
  };

  if (draft) {
    return <DraftEditor key={draft.draftId} initialDraft={draft} onClose={async () => { setDraft(null); await refresh(); }} />;
  }
  return (
    <div className="mx-auto flex min-h-screen max-w-4xl flex-col gap-6 p-6 md:p-10">
      <div><p className="mb-1 text-sm text-muted-foreground">Dungeon Wizard</p><h1 className="mb-2 text-3xl font-semibold">Welches Spiel möchtest du bearbeiten?</h1><p className="text-muted-foreground">Öffne einen vorhandenen Entwurf oder beginne ein neues Spiel.</p></div>
      {error && <Alert variant="destructive"><AlertTitle>Entwürfe nicht verfügbar</AlertTitle><AlertDescription>{error}</AlertDescription></Alert>}
      <Button className="w-fit" onClick={() => void createDraft()} disabled={busy || deleting}><PlusIcon />Neues Spiel</Button>
      <div className="grid gap-3">
        {drafts === null && <p className="text-muted-foreground">Entwürfe werden geladen…</p>}
        {drafts?.length === 0 && <div className="panel text-muted-foreground">Noch keine Entwürfe vorhanden.</div>}
        {drafts?.map((summary) => (
          <div key={summary.draftId} className="panel flex items-stretch gap-2 bg-background p-0">
            <Button
              type="button"
              variant="ghost"
              disabled={busy || deleting}
              onClick={() => void openDraft(summary.draftId)}
              className="h-auto min-w-0 flex-1 justify-between rounded-[var(--radius-sm)] px-4 py-4 text-left whitespace-normal"
            >
              <span className="min-w-0"><span className="block font-medium">{summary.title.trim() || "Unbenanntes Spiel"}</span><span className="block text-sm font-normal text-muted-foreground">{summary.savedAt ? `Zuletzt gespeichert: ${new Date(summary.savedAt).toLocaleString("de-DE")}` : "Noch nicht gespeichert"}</span></span>
              <FolderOpenIcon className="size-5 shrink-0" />
            </Button>
            <div className="flex items-center border-l border-[var(--border-color)] px-2">
              <Button
                type="button"
                variant="destructive"
                size="icon"
                disabled={busy || deleting}
                aria-label={`Entwurf ${summary.title.trim() || "Unbenanntes Spiel"} endgültig löschen`}
                onClick={() => { setDeleteDraft(summary); setDeleteError(null); }}
              >
                <Trash2Icon />
              </Button>
            </div>
          </div>
        ))}
      </div>
      {!storage.host.native && <Alert><AlertTitle>Separater Entwicklungs- und UI-Testmodus</AlertTitle><AlertDescription>Diese Entwürfe bleiben ausschließlich in diesem Browser. Sie können nicht in die lokale Wizard-Anwendung übertragen, vollständig geprüft oder als Spiel verpackt werden.</AlertDescription></Alert>}
      <Dialog
        open={deleteDraft !== null}
        onOpenChange={(open) => { if (!open && !deleting) { setDeleteDraft(null); setDeleteError(null); } }}
      >
        <DialogContent showCloseButton={false}>
          <DialogHeader>
            <DialogTitle>Entwurf endgültig löschen?</DialogTitle>
            <DialogDescription>
              Der Entwurf „{deleteDraft?.title.trim() || "Unbenanntes Spiel"}“ und alle im Wizard gespeicherten Uploads werden dauerhaft gelöscht. Bereits erstellte Spieldateien im gewählten Projektordner bleiben erhalten. Diese Aktion kann nicht rückgängig gemacht werden.
            </DialogDescription>
          </DialogHeader>
          {deleteError && <Alert variant="destructive"><AlertTitle>Löschen nicht möglich</AlertTitle><AlertDescription>{deleteError}</AlertDescription></Alert>}
          <DialogFooter>
            <Button type="button" variant="outline" disabled={deleting} onClick={() => { setDeleteDraft(null); setDeleteError(null); }}>Abbrechen</Button>
            <Button type="button" variant="destructive" disabled={deleting} onClick={() => void deleteSelectedDraft()}>
              {deleting ? "Wird gelöscht…" : "Endgültig löschen"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
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
  const transitionWizardWork = React.useCallback((from: Exclude<WizardWork, null>, to: Exclude<WizardWork, null>) => {
    if (wizardWorkRef.current !== from) return false;
    wizardWorkRef.current = to;
    setWizardWork(to);
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
  const { issueReport, assetStorageStatus } = useErrorCheck(draft.draftId, project, draft.uploads);
  const updateProject = (updatedProject: DeerProject) => updateDraft((current) => { current.project = structuredClone(updatedProject); });
  const setTab = (activeTab: TabId) => {
    if (wizardWorkRef.current !== null) return;
    updateDraft((current) => { current.ui.activeTab = activeTab; current.ui.touchedTabs = withTouchedTab(current.ui.touchedTabs, activeTab); });
  };
  const hasTouchedAllTabs = Object.values(touchedTabs).every((touched) => touched);
  const saveText = { unsaved: "Änderungen noch nicht gespeichert", saving: "Wird gespeichert…", saved: "Gespeichert", error: "Speichern fehlgeschlagen – Änderungen bleiben geöffnet" }[saveState];

  return (
    <UploadReferencesProvider draftId={draft.draftId} value={draft.uploads}>
      <div className="min-h-screen max-w-7xl mx-auto bg-background p-4 lg:border-x border-[var(--border-color)]">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <Button variant="ghost" disabled={wizardWork !== null} onClick={() => {
            if (wizardWorkRef.current !== null) return;
            void flush().then(onClose).catch((cause) => toast.error("Bitte speichere den Entwurf, bevor du zurückgehst.", { description: cause instanceof Error ? cause.message : undefined }));
          }}><ArrowLeftIcon />Meine Spiele</Button>
          <div className={`text-sm ${saveState === "error" ? "text-destructive" : "text-muted-foreground"}`}>{saveText}</div>
        </div>
        <h1 className="mb-4 text-center text-3xl font-bold">{project.metadata.title.trim() || "Neues Spiel"}</h1>
        <div className="grid grid-cols-1 lg:grid-cols-[320px_1fr] gap-4 max-w-full">
          <div className="lg:sticky lg:top-0 flex flex-col gap-4"><SidebarNavigation issueReport={issueReport} touchedTabs={touchedTabs} tab={tab} setTab={setTab} disabled={wizardWork !== null} /><ErrorDetector issueReport={issueReport} assetStorageStatus={assetStorageStatus} touchedAll={hasTouchedAllTabs} className="lg:block hidden" /></div>
          <div className="row-span-2 panel">
            {tab === "metadata" && <MetadataTab deerSchema={project} updateDeerSchema={updateProject} />}
            {tab === "scenario" && <ScenarioTab deerSchema={project} updateDeerSchema={updateProject} />}
            {tab === "session" && <SessionTab deerSchema={project} updateDeerSchema={updateProject} />}
            {tab === "assets" && <AssetsTab draft={draft} updateDraft={updateDraft} beginWork={beginWizardWork} finishWork={finishWizardWork} />}
            {tab === "riddles" && <RiddlesTab draft={draft} updateDraft={updateDraft} />}
            {tab === "riddle_graph" && <RiddleGraphTab draft={draft} updateDraft={updateDraft} />}
            {tab === "game_end" && <GameEndTab deerSchema={project} updateDeerSchema={updateProject} />}
            {tab === "review" && <ReviewTab draft={draft} updateDraft={updateDraft} flush={flush} work={wizardWork} beginWork={beginWizardWork} transitionWork={transitionWizardWork} finishWork={finishWizardWork} />}
            <InPageNavigation tab={tab} setTab={setTab} disabled={wizardWork !== null} />
          </div>
          <ErrorDetector issueReport={issueReport} assetStorageStatus={assetStorageStatus} touchedAll={hasTouchedAllTabs} className="lg:hidden" />
        </div>
      </div>
    </UploadReferencesProvider>
  );
}

function Layout() {
  return <ThemeProvider attribute="class" defaultTheme="dark" disableTransitionOnChange><main className="typeset typeset-docs"><App /></main><Toaster position="bottom-right" richColors /></ThemeProvider>;
}

export default Layout;
