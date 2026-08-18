import { Toaster } from "sonner";
import "./App.css";
import { ThemeProvider } from "./components/ThemeProvider";
import type { DeerSchema } from "./data/DeerSchema";
import { createDeerSchema, DEER_SCHEMA_STORAGE_KEY } from "./data/createDeerSchema";
import { useLocalStorage } from "@uidotdev/usehooks";
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
import {
  createUntouchedTabs,
  TOUCHED_TABS_STORAGE_KEY,
  withTouchedTab,
  type TouchedTabs,
} from "./data/TabTouchState";
import React from "react";
import { ButtonGroup } from "./components/ui/button-group";
import { Button } from "./components/ui/button";

const initialDeerSchema = createDeerSchema();
const initialTouchedTabs = createUntouchedTabs();

function App() {
  const [deerSchema, setDeerSchema] = useLocalStorage<DeerSchema>(
    DEER_SCHEMA_STORAGE_KEY,
    initialDeerSchema,
  );
  const [tab, setTab] = useLocalStorage<string>("tab", "metadata");
  const [touchedTabs, setTouchedTabs] = useLocalStorage<TouchedTabs>(
    TOUCHED_TABS_STORAGE_KEY,
    initialTouchedTabs,
  );

  const issueReport = useErrorCheck(deerSchema);

  // Opening a tab counts as touching it, so its status is shown from then on.
  React.useEffect(() => {
    const updatedTouchedTabs = withTouchedTab(touchedTabs, tab);
    if (updatedTouchedTabs !== touchedTabs) setTouchedTabs(updatedTouchedTabs);
  }, [tab, touchedTabs, setTouchedTabs]);

  const updateDeerSchema = (updatedSchema: DeerSchema) => {
    const storedSchema = JSON.parse(JSON.stringify(updatedSchema)) as DeerSchema;
    if (storedSchema.metadata.description?.trim() === "") delete storedSchema.metadata.description;
    if (storedSchema.metadata.author?.trim() === "") delete storedSchema.metadata.author;
    if (storedSchema.scenario.failureText?.length === 0) delete storedSchema.scenario.failureText;
    for (const asset of storedSchema.assets) {
      if (asset.source.attribution?.trim() === "") delete asset.source.attribution;
    }
    setDeerSchema(storedSchema);
  };

  const importSchema = () => {
    const input = document.createElement("input");
    input.type = "file";
    input.accept = ".json";
    input.onchange = (event) => {
      const file = (event.target as HTMLInputElement).files?.[0];
      if (!file) return;
      importSchemaFromFile(file, updateDeerSchema);
    };
    input.click();
  };

  const hasTouchedAllTabs = Object.values(touchedTabs).every((touched) => touched);

  return (
    <div className="h-screen overflow-scroll max-w-7xl mx-auto bg-background p-4 lg:border-x border-[var(--border-color)]">
      <div className="grid grid-cols-[1fr_auto] mb-4">
        <h1 className="text-3xl font-bold text-center">Dungeon Spec Generator</h1>
        <ButtonGroup className="mt-0">
          <Button variant="outline" onClick={importSchema}>
            Import
          </Button>
          <Button variant="outline" onClick={() => exportSchema(deerSchema)}>
            Export
          </Button>
        </ButtonGroup>
      </div>
      <div className={`grid grid-cols-1 lg:grid-cols-[320px_1fr] gap-4 max-w-full`}>
        <div className="lg:sticky lg:top-0 flex flex-col gap-4">
          <SidebarNavigation issueReport={issueReport} touchedTabs={touchedTabs} tab={tab} setTab={setTab} />
          <ErrorDetector
            deerSchema={deerSchema}
            updateDeerSchema={updateDeerSchema}
            issueReport={issueReport}
            touchedAll={hasTouchedAllTabs}
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
          {tab === "riddle_graph" && (
            <RiddleGraphTab deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} />
          )}
          {tab === "game_end" && <GameEndTab deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} />}
          {tab === "review" && (
            <ReviewTab
              deerSchema={deerSchema}
              updateDeerSchema={updateDeerSchema}
              issueReport={issueReport}
            />
          )}
          <InPageNavigation tab={tab} setTab={setTab} />
        </div>
        <ErrorDetector
          deerSchema={deerSchema}
          updateDeerSchema={updateDeerSchema}
          issueReport={issueReport}
          touchedAll={hasTouchedAllTabs}
          className="lg:hidden"
        />
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

function exportSchema(schema: DeerSchema) {
  const dataStr = JSON.stringify(schema, null, 2);
  const blob = new Blob([dataStr], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "deer-schema.json";
  link.click();
  URL.revokeObjectURL(url);
}

function importSchemaFromFile(file: File, setDeerSchema: (schema: DeerSchema) => void) {
  const reader = new FileReader();
  reader.onload = (e) => {
    try {
      const importedSchema = JSON.parse(e.target?.result as string);
      setDeerSchema(importedSchema);
    } catch (error) {
      console.error("Fehler beim Importieren des Schemas:", error);
    }
  };
  reader.readAsText(file);
}
