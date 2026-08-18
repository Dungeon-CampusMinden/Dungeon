import React from "react";
import { toast, Toaster } from "sonner";
import { ArrowLeftIcon, FolderOpenIcon, PlusIcon } from "lucide-react";
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
import {
  BrowserDraftStorage,
  DraftReloadRequiredError,
  type DraftSummary,
} from "./data/DraftStorage";
import { BrowserAssetStorage } from "./data/AssetStorage";
import { useWizardStorage, WizardStorageProvider, type WizardStoragePort } from "./data/WizardStorage";
import { BrowserWizardHost, detectNativeHost, NativeAssetStorage, NativeDraftStorage, NativeWizardHost } from "./data/NativeWizardHost";
import { cloneDraft, createWizardDraft, type DraftRevision, type DraftTransform, type UpdateDraft, type WizardDraft } from "./data/WizardDraft";
import { Alert, AlertDescription, AlertTitle } from "./components/ui/alert";
import { Button } from "./components/ui/button";
import { UploadReferencesProvider } from "./components/assets/UploadReferencesContext";
import type { TabId } from "./data/Tabs";
import type { WizardWork } from "./data/WizardWork";

type SaveState = "unsaved" | "saving" | "saved" | "error" | "conflict";

async function createStorage(): Promise<WizardStoragePort> {
  if (await detectNativeHost()) {
    return { drafts: new NativeDraftStorage(), assets: new NativeAssetStorage(), host: new NativeWizardHost() };
  }
  return { drafts: new BrowserDraftStorage(), assets: new BrowserAssetStorage(), host: new BrowserWizardHost() };
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

  if (draft) {
    return <DraftEditor key={draft.draftId} initialDraft={draft} onClose={async () => { setDraft(null); await refresh(); }} />;
  }
  return (
    <div className="mx-auto flex min-h-screen max-w-4xl flex-col gap-6 p-6 md:p-10">
      <div><p className="mb-1 text-sm text-muted-foreground">Dungeon Wizard</p><h1 className="mb-2 text-3xl font-semibold">Welches Spiel möchtest du bearbeiten?</h1><p className="text-muted-foreground">Öffne einen vorhandenen Entwurf oder beginne ein neues Spiel.</p></div>
      {error && <Alert variant="destructive"><AlertTitle>Entwürfe nicht verfügbar</AlertTitle><AlertDescription>{error}</AlertDescription></Alert>}
      <Button className="w-fit" onClick={() => void createDraft()} disabled={busy}><PlusIcon />Neues Spiel</Button>
      <div className="grid gap-3">
        {drafts === null && <p className="text-muted-foreground">Entwürfe werden geladen…</p>}
        {drafts?.length === 0 && <div className="panel text-muted-foreground">Noch keine Entwürfe vorhanden.</div>}
        {drafts?.map((summary) => (
          <button key={summary.draftId} type="button" disabled={busy} onClick={() => void openDraft(summary.draftId)} className="panel flex items-center justify-between gap-4 bg-background text-left transition-colors hover:bg-muted disabled:opacity-50">
            <span><span className="block font-medium">{summary.title.trim() || "Unbenanntes Spiel"}</span><span className="text-sm text-muted-foreground">{summary.savedAt ? `Zuletzt gespeichert: ${new Date(summary.savedAt).toLocaleString("de-DE")}` : "Noch nicht gespeichert"}</span></span>
            <FolderOpenIcon className="size-5 shrink-0" />
          </button>
        ))}
      </div>
      {!storage.host.native && <Alert><AlertTitle>Separater Entwicklungs- und UI-Testmodus</AlertTitle><AlertDescription>Diese Entwürfe bleiben ausschließlich in diesem Browser. Sie können nicht in die lokale Wizard-Anwendung übertragen, vollständig geprüft oder als Spiel verpackt werden.</AlertDescription></Alert>}
    </div>
  );
}

function DraftEditor({ initialDraft, onClose }: { initialDraft: WizardDraft; onClose: () => Promise<void> }) {
  const storage = useWizardStorage();
  const [draft, setDraft] = React.useState(initialDraft);
  const [saveState, setSaveState] = React.useState<SaveState>("saved");
  const [reloadRequired, setReloadRequired] = React.useState<DraftReloadRequiredError | null>(null);
  const [reloadError, setReloadError] = React.useState<string | null>(null);
  const [editorSession, setEditorSession] = React.useState(0);
  const [wizardWork, setWizardWork] = React.useState<WizardWork>(null);
  const wizardWorkRef = React.useRef<WizardWork>(null);
  const latestDraftRef = React.useRef(initialDraft);
  const revisionRef = React.useRef(0);
  const savedRevisionRef = React.useRef(0);
  const timerRef = React.useRef<number | null>(null);
  const savePromiseRef = React.useRef<Promise<void> | null>(null);
  const reloadRequiredRef = React.useRef<DraftReloadRequiredError | null>(null);

  const enterReloadRequired = React.useCallback((cause: DraftReloadRequiredError) => {
    if (timerRef.current !== null) {
      window.clearTimeout(timerRef.current);
      timerRef.current = null;
    }
    reloadRequiredRef.current = cause;
    setReloadRequired(cause);
    setReloadError(null);
    setSaveState("conflict");
  }, []);

  const drainSaves = React.useCallback((): Promise<void> => {
    if (timerRef.current !== null) { window.clearTimeout(timerRef.current); timerRef.current = null; }
    if (reloadRequiredRef.current) return Promise.reject(reloadRequiredRef.current);
    if (savePromiseRef.current) return savePromiseRef.current;
    const run = (async () => {
      while (savedRevisionRef.current < revisionRef.current && !reloadRequiredRef.current) {
        const savingRevision = revisionRef.current;
        const snapshot = cloneDraft(latestDraftRef.current);
        setSaveState("saving");
        try {
          const saved = await storage.drafts.save(snapshot);
          savedRevisionRef.current = savingRevision;
          const current = revisionRef.current === savingRevision
            ? cloneDraft(saved)
            : cloneDraft(latestDraftRef.current);
          // The host revision acknowledges the sent snapshot even if a newer local edit exists.
          // Only host metadata is merged in that case; newer authoring content stays untouched.
          current.revision = saved.revision;
          current.savedAt = saved.savedAt;
          current.saveStatus = revisionRef.current === savingRevision ? "saved" : "unsaved";
          latestDraftRef.current = current;
          setDraft(current);
          setSaveState(revisionRef.current === savingRevision ? "saved" : "unsaved");
        } catch (cause) {
          if (cause instanceof DraftReloadRequiredError) {
            enterReloadRequired(cause);
          } else {
            setSaveState("error");
          }
          throw cause;
        }
      }
    })().finally(() => { savePromiseRef.current = null; });
    savePromiseRef.current = run;
    return run;
  }, [enterReloadRequired, storage]);

  const scheduleSave = React.useCallback(() => {
    if (reloadRequiredRef.current) return;
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
        && reloadRequiredRef.current === null
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

  const updateDraft = React.useCallback<UpdateDraft>((transform: DraftTransform) => {
    const snapshot = cloneDraft(latestDraftRef.current);
    if (transform(snapshot) === false) return;
    snapshot.saveStatus = "unsaved";
    latestDraftRef.current = snapshot;
    revisionRef.current += 1;
    setDraft(snapshot);
    setSaveState(reloadRequiredRef.current ? "conflict" : "unsaved");
    if (!reloadRequiredRef.current) scheduleSave();
  }, [scheduleSave]);

  const adoptHostMutation = React.useCallback((
    expectedRevision: DraftRevision,
    nextRevision: DraftRevision,
    transform: DraftTransform,
  ): WizardDraft => {
    if (savePromiseRef.current !== null || savedRevisionRef.current < revisionRef.current) {
      throw new Error("Neuere Änderungen müssen zuerst gespeichert werden.");
    }
    const snapshot = cloneDraft(latestDraftRef.current);
    if (snapshot.revision !== expectedRevision
      || expectedRevision === Number.MAX_SAFE_INTEGER
      || nextRevision !== expectedRevision + 1) {
      throw new Error("Die Host-Antwort gehört nicht mehr zum gespeicherten Entwurf.");
    }
    if (transform(snapshot) === false) {
      throw new Error("Die Host-Antwort konnte nicht übernommen werden.");
    }
    snapshot.revision = nextRevision;
    snapshot.saveStatus = "saved";
    latestDraftRef.current = snapshot;
    setDraft(snapshot);
    setSaveState("saved");
    return cloneDraft(snapshot);
  }, []);

  const flush = React.useCallback(async (): Promise<WizardDraft> => {
    if (reloadRequiredRef.current) throw reloadRequiredRef.current;
    while (savedRevisionRef.current < revisionRef.current || savePromiseRef.current) await drainSaves();
    return cloneDraft(latestDraftRef.current);
  }, [drainSaves]);

  const loadSavedDraft = React.useCallback(async () => {
    if (!window.confirm(
      "Gespeicherten Stand laden? Nur deine aktuell geöffneten, nicht gespeicherten oder widersprüchlichen Änderungen werden verworfen.",
    )) return;
    try {
      await savePromiseRef.current?.catch(() => undefined);
      const loaded = await storage.drafts.load(latestDraftRef.current.draftId);
      if (!loaded) throw new Error("Der gespeicherte Entwurf ist nicht mehr vorhanden.");
      if (timerRef.current !== null) {
        window.clearTimeout(timerRef.current);
        timerRef.current = null;
      }
      savePromiseRef.current = null;
      revisionRef.current = 0;
      savedRevisionRef.current = 0;
      reloadRequiredRef.current = null;
      wizardWorkRef.current = null;
      const snapshot = cloneDraft(loaded);
      latestDraftRef.current = snapshot;
      setDraft(snapshot);
      setSaveState("saved");
      setReloadRequired(null);
      setReloadError(null);
      setWizardWork(null);
      setEditorSession((current) => current + 1);
    } catch (cause) {
      setReloadError(cause instanceof Error ? cause.message : "Der gespeicherte Stand konnte nicht geladen werden.");
    }
  }, [storage]);

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
  const saveText = { unsaved: "Änderungen noch nicht gespeichert", saving: "Wird gespeichert…", saved: "Gespeichert", error: "Speichern fehlgeschlagen – Änderungen bleiben geöffnet", conflict: "Neuladen erforderlich – Änderungen bleiben geöffnet" }[saveState];

  return (
    <UploadReferencesProvider draftId={draft.draftId} value={draft.uploads}>
      <div className="min-h-screen max-w-7xl mx-auto bg-background p-4 lg:border-x border-[var(--border-color)]">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <Button variant="ghost" disabled={wizardWork !== null} onClick={() => {
            if (wizardWorkRef.current !== null) return;
            if (reloadRequiredRef.current) {
              toast.error("Lade zuerst den gespeicherten Stand über „Gespeicherten Stand laden“.");
              return;
            }
            void flush().then(onClose).catch((cause) => toast.error("Bitte speichere den Entwurf, bevor du zurückgehst.", { description: cause instanceof Error ? cause.message : undefined }));
          }}><ArrowLeftIcon />Meine Spiele</Button>
          <div className={`text-sm ${saveState === "error" || saveState === "conflict" ? "text-destructive" : "text-muted-foreground"}`}>{saveText}</div>
        </div>
        {reloadRequired && (
          <Alert variant="destructive" className="mb-4">
            <AlertTitle>{reloadRequired.title}</AlertTitle>
            <AlertDescription>
              <p>{reloadRequired.message} Er wird nicht weiter automatisch gespeichert. Um weiterzuarbeiten, musst du den aktuell gespeicherten Stand laden.</p>
              <p className="mt-2">Dabei werden ausschließlich deine aktuell geöffneten, nicht gespeicherten oder widersprüchlichen Änderungen verworfen. Der gespeicherte Stand bleibt unverändert.</p>
              {reloadError && <p className="mt-2">Laden fehlgeschlagen: {reloadError}</p>}
              <Button className="mt-3" variant="destructive" onClick={() => void loadSavedDraft()}>
                Gespeicherten Stand laden
              </Button>
            </AlertDescription>
          </Alert>
        )}
        <h1 className="mb-4 text-center text-3xl font-bold">{project.metadata.title.trim() || "Neues Spiel"}</h1>
        <div className="grid grid-cols-1 lg:grid-cols-[320px_1fr] gap-4 max-w-full">
          <div className="lg:sticky lg:top-0 flex flex-col gap-4"><SidebarNavigation issueReport={issueReport} touchedTabs={touchedTabs} tab={tab} setTab={setTab} disabled={wizardWork !== null} /><ErrorDetector issueReport={issueReport} assetStorageStatus={assetStorageStatus} touchedAll={hasTouchedAllTabs} className="lg:block hidden" /></div>
          <div key={editorSession} className="row-span-2 panel">
            {tab === "metadata" && <MetadataTab deerSchema={project} updateDeerSchema={updateProject} />}
            {tab === "scenario" && <ScenarioTab deerSchema={project} updateDeerSchema={updateProject} />}
            {tab === "session" && <SessionTab deerSchema={project} updateDeerSchema={updateProject} />}
            {tab === "assets" && <AssetsTab draft={draft} updateDraft={updateDraft} beginWork={beginWizardWork} finishWork={finishWizardWork} />}
            {tab === "riddles" && <RiddlesTab draft={draft} updateDraft={updateDraft} />}
            {tab === "riddle_graph" && <RiddleGraphTab draft={draft} updateDraft={updateDraft} />}
            {tab === "game_end" && <GameEndTab deerSchema={project} updateDeerSchema={updateProject} />}
            {tab === "review" && <ReviewTab draft={draft} updateDraft={updateDraft} flush={flush} adoptHostMutation={adoptHostMutation} onReloadRequired={enterReloadRequired} work={wizardWork} beginWork={beginWizardWork} transitionWork={transitionWizardWork} finishWork={finishWizardWork} />}
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
