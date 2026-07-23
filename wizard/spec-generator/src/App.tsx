import { Toaster } from "sonner";
import "./App.css";

function App() {
  return (
    <div className={`grid grid-cols-[1fr] lg:grid-cols-[300px_1fr] gap-4 p-4 w-[1200px] max-w-full mx-auto`}>
      <div className="panel">
        <h2>Sidebar</h2>
        <p className="">
          The sidebar will hold the outline/overview and links to each step for quick navigation.
        </p>
      </div>
      <div className="row-span-2 panel">
        <h1>Wizard Spec Generator</h1>
        <p className="">There will be the main content here</p>
        <ul className="">
          <li>Each form shows its respective fields</li>
          <li>Continue/Back buttons</li>
        </ul>
      </div>
      <div className="panel rounded-sm">
        <h2>Error Detector</h2>
        <p className="">
          This will show any errors in the form and provide links to jump to the specific step that needs to
          be fixed.
        </p>
      </div>
    </div>
  );
}

function Layout() {
  return (
    <>
      <main className="typeset typeset-docs">
        <App />
      </main>
      <Toaster position="bottom-right" richColors />
    </>
  );
}

export default Layout;
