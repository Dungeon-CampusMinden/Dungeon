import { Toaster } from "sonner";
import "./App.css";
import { ThemeProvider } from "./components/ThemeProvider";
import schema from "./data/deer.example.json";
import type { DeerSchema } from "./data/DeerSchema";
import { Button } from "./components/ui/button";
import { useLocalStorage } from "@uidotdev/usehooks";
import { ErrorDetector } from "./components/ErrorDetector";
import { SidebarNavigation } from "./components/SidebarNavigation";
import { MetadataTab } from "./components/MetadataTab";
import { ScenarioTab } from "./components/ScenarioTab";
import { SessionTab } from "./components/SessionTab";
import { SurfacesTab } from "./components/SurfacesTab";
import { CustomIcon } from "./components/CustomIcon";
import { AssetsTab } from "./components/AssetsTab";
import { RiddlesTab } from "./components/RiddlesTab";
import { useErrorCheck } from "./hooks/useErrorCheck";
import {
  createUntouchedTabs,
  TOUCHED_TABS_STORAGE_KEY,
  withTouchedTab,
  type TouchedTabs,
} from "./data/TabTouchState";
import React from "react";

function App() {
  const [deerSchema, setDeerSchema] = useLocalStorage<DeerSchema>("schema", schema as DeerSchema);
  const [tab, setTab] = useLocalStorage<string>("tab", "metadata");
  const [touchedTabs, setTouchedTabs] = useLocalStorage<TouchedTabs>(
    TOUCHED_TABS_STORAGE_KEY,
    createUntouchedTabs(),
  );

  const issueReport = useErrorCheck(deerSchema);

  // Opening a tab counts as touching it, so its status is shown from then on.
  React.useEffect(() => {
    setTouchedTabs((current) => withTouchedTab(current, tab));
  }, [tab, setTouchedTabs]);

  const updateDeerSchema = (updatedSchema: DeerSchema) => {
    setDeerSchema(JSON.parse(JSON.stringify(updatedSchema)));
  };

  const testAction = () => {
    deerSchema.metadata.title = "Updated Title";
    updateDeerSchema(deerSchema);
  };

  return (
    <div className="h-screen overflow-scroll max-w-7xl mx-auto bg-background p-4 lg:border-x border-[var(--border-color)]">
      <h1 className="text-3xl font-bold mb-4 text-center">Dungeon Spec Generator</h1>
      <div className={`grid grid-cols-1 lg:grid-cols-[320px_1fr] gap-4 max-w-full`}>
        <div className="lg:sticky lg:top-0 flex flex-col gap-4">
          <SidebarNavigation issueReport={issueReport} touchedTabs={touchedTabs} tab={tab} setTab={setTab} />
          <ErrorDetector
            deerSchema={deerSchema}
            updateDeerSchema={updateDeerSchema}
            className="lg:block hidden"
          />
        </div>
        <div className="row-span-2 panel">
          {tab === "metadata" && <MetadataTab deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} />}
          {tab === "scenario" && <ScenarioTab deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} />}
          {tab === "session" && <SessionTab deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} />}
          {tab === "surfaces" && <SurfacesTab deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} />}
          {tab === "assets" && <AssetsTab deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} />}
          {tab === "riddles" && <RiddlesTab deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} />}
          {tab === "review" && (
            <>
              <CustomIcon src="/bundled-assets/items/puzzle-piece.png" alt="Puzzle Piece" />
              <Button onClick={testAction}>Test</Button>
              <code className="block mb-4 p-2 bg-slate-100 rounded-sm text-sm">
                <pre>{JSON.stringify(deerSchema, null, 2)}</pre>
              </code>
            </>
          )}
        </div>
        <ErrorDetector deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} className="lg:hidden" />
      </div>
    </div>
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
