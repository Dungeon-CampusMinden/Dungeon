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

function App() {
  const [deerSchema, setDeerSchema] = useLocalStorage<DeerSchema>("schema", schema as DeerSchema);
  const [tab, setTab] = useLocalStorage<string>("tab", "metadata");

  const updateDeerSchema = (updatedSchema: DeerSchema) => {
    setDeerSchema(JSON.parse(JSON.stringify(updatedSchema)));
  };

  const testAction = () => {
    deerSchema.metadata.title = "Updated Title";
    updateDeerSchema(deerSchema);
  };

  return (
    <div className={`grid grid-cols-1 lg:grid-cols-[320px_1fr] gap-4 p-4 w-[1200px] max-w-full mx-auto`}>
      <div className="lg:sticky lg:top-4 flex flex-col gap-4">
        <SidebarNavigation
          deerSchema={deerSchema}
          updateDeerSchema={updateDeerSchema}
          tab={tab}
          setTab={setTab}
        />
        <ErrorDetector
          deerSchema={deerSchema}
          updateDeerSchema={updateDeerSchema}
          className="lg:block hidden"
        />
      </div>
      <div className="row-span-2 panel">
        {tab === "metadata" && <MetadataTab deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} />}
        {tab === "review" && (
          <>
            <h1>Wizard Spec Generator</h1>
            <Button onClick={testAction}>Test</Button>
            <code className="block mb-4 p-2 bg-slate-100 rounded-sm text-sm">
              <pre>{JSON.stringify(deerSchema, null, 2)}</pre>
            </code>
          </>
        )}
      </div>
      <ErrorDetector deerSchema={deerSchema} updateDeerSchema={updateDeerSchema} className="lg:hidden" />
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
