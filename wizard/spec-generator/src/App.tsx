import React from "react";
import { toast, Toaster } from "sonner";
import "./App.css";
import { ThemeProvider } from "./components/ThemeProvider";
import type { DeerProject } from "./data/DeerSchema";
import { ErrorDetector } from "./components/ErrorDetector";
import { SidebarNavigation } from "./components/SidebarNavigation";
import { MetadataTab } from "./components/MetadataTab";
import { ScenarioTab } from "./components/ScenarioTab";
import { SessionTab } from "./components/SessionTab";
import { SurfacesTab } from "./components/SurfacesTab";
import { AssetsTab } from "./components/AssetsTab";
import { RiddlesTab } from "./components/RiddlesTab";
import { RiddleGraphTab } from "./components/RiddleGraphTab";
import { GameEndTab } from "./components/GameEndTab";
import { ReviewTab } from "./components/ReviewTab";
import { InPageNavigation } from "./components/InPageNavigation";
import { useErrorCheck } from "./hooks/useErrorCheck";
import { withTouchedTab } from "./data/TabTouchState";
import { BrowserDraftStorage } from "./data/DraftStorage";
import { BrowserAssetStorage } from "./data/AssetStorage";
import { useWizardStorage, WizardStorageProvider, type WizardStoragePort } from "./data/WizardStorage";
import {
  cloneDraft,
  createWizardDraft,
  type DraftTransform,
  type UpdateDraft,
  type WizardDraft,
} from "./data/WizardDraft";
import { Alert, AlertDescription, AlertTitle } from "./components/ui/alert";
import { UploadReferencesProvider } from "./components/assets/UploadReferencesContext";
import type { TabId } from "./data/Tabs";

const browserStorage: WizardStoragePort = {
  drafts: new BrowserDraftStorage(),
  assets: new BrowserAssetStorage(),
};

function loadInitialDraft(): { draft: WizardDraft | null; error: string | null } {
  try {
    const first = browserStorage.drafts.list()[0];
    if (first) return { draft: browserStorage.drafts.load(first.draftId), error: null };
    return { draft: browserStorage.drafts.save(createWizardDraft()), error: null };
  } catch (error) {
    return {
      draft: null,
      error: error instanceof Error ? error.message : "Der Entwurf konnte nicht geladen werden.",
    };
  }
}

function App() {
  const [initial] = React.useState(loadInitialDraft);
  if (!initial.draft) {
    return (
      <div className="mx-auto max-w-3xl p-8">
        <Alert variant="destructive">
          <AlertTitle>Entwurf kann nicht geöffnet werden</AlertTitle>
          <AlertDescription>{initial.error}</AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <WizardStorageProvider value={browserStorage}>
      <DraftEditor initialDraft={initial.draft} />
    </WizardStorageProvider>
  );
}

function DraftEditor({ initialDraft }: { initialDraft: WizardDraft }) {
  const storage = useWizardStorage();
  const [draft, setDraft] = React.useState(initialDraft);
  const latestDraftRef = React.useRef(initialDraft);
  const updateDraft = React.useCallback<UpdateDraft>((transform: DraftTransform) => {
    const snapshot = cloneDraft(latestDraftRef.current);
    if (transform(snapshot) === false) return;
    snapshot.saveStatus = "unsaved";
    latestDraftRef.current = snapshot;
    try {
      const saved = storage.drafts.save(snapshot);
      latestDraftRef.current = saved;
      setDraft(saved);
    } catch (error) {
      latestDraftRef.current = snapshot;
      setDraft(snapshot);
      toast.error("Der Entwurf konnte nicht gespeichert werden.", {
        description: error instanceof Error ? error.message : undefined,
      });
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
  }, [draft, updateDraft]);

  const project = draft.project;
  const tab = draft.ui.activeTab;
  const touchedTabs = draft.ui.touchedTabs;
  const { issueReport, assetStorageStatus } = useErrorCheck(project, draft.uploads);

  const updateProject = (updatedProject: DeerProject) => {
    updateDraft((current) => {
      current.project = structuredClone(updatedProject);
    });
  };
  const setTab = (activeTab: TabId) => {
    updateDraft((current) => {
      current.ui.activeTab = activeTab;
      current.ui.touchedTabs = withTouchedTab(current.ui.touchedTabs, activeTab);
    });
  };

  const hasTouchedAllTabs = Object.values(touchedTabs).every((touched) => touched);

  return (
    <UploadReferencesProvider value={draft.uploads}>
    <div className="h-screen overflow-scroll max-w-7xl mx-auto bg-background p-4 lg:border-x border-[var(--border-color)]">
      <h1 className="mb-4 text-center text-3xl font-bold">Dungeon Spec Generator</h1>
      <div className="grid grid-cols-1 lg:grid-cols-[320px_1fr] gap-4 max-w-full">
        <div className="lg:sticky lg:top-0 flex flex-col gap-4">
          <SidebarNavigation issueReport={issueReport} touchedTabs={touchedTabs} tab={tab} setTab={setTab} />
          <ErrorDetector
            issueReport={issueReport}
            assetStorageStatus={assetStorageStatus}
            touchedAll={hasTouchedAllTabs}
            className="lg:block hidden"
          />
        </div>
        <div className="row-span-2 panel">
          {tab === "metadata" && <MetadataTab deerSchema={project} updateDeerSchema={updateProject} />}
          {tab === "scenario" && <ScenarioTab deerSchema={project} updateDeerSchema={updateProject} />}
          {tab === "session" && <SessionTab deerSchema={project} updateDeerSchema={updateProject} />}
          {tab === "surfaces" && <SurfacesTab deerSchema={project} updateDeerSchema={updateProject} />}
          {tab === "assets" && (
            <AssetsTab draft={draft} updateDraft={updateDraft} />
          )}
          {tab === "riddles" && <RiddlesTab draft={draft} updateDraft={updateDraft} />}
          {tab === "riddle_graph" && (
            <RiddleGraphTab draft={draft} updateDraft={updateDraft} />
          )}
          {tab === "game_end" && <GameEndTab deerSchema={project} updateDeerSchema={updateProject} />}
          {tab === "review" && <ReviewTab draft={draft} />}
          <InPageNavigation tab={tab} setTab={setTab} />
        </div>
        <ErrorDetector
          issueReport={issueReport}
          assetStorageStatus={assetStorageStatus}
          touchedAll={hasTouchedAllTabs}
          className="lg:hidden"
        />
      </div>
    </div>
    </UploadReferencesProvider>
  );
}

function Layout() {
  return (
    <ThemeProvider attribute="class" defaultTheme="dark" disableTransitionOnChange>
      <main className="typeset typeset-docs">
        <App />
      </main>
      <Toaster position="bottom-right" richColors />
    </ThemeProvider>
  );
}

export default Layout;
