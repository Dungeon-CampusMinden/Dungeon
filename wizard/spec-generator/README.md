# Wizard spec generator

The Spec Generator is the browser UI for authoring a private Wizard draft and
turning it into a DEER project through the local Java host.

## Install and run

- Run `npm install`.
- Run `npm run dev` for the UI-only Vite development server.
- Open the URL printed by Vite, usually `http://localhost:5173/`.

For the complete host flow, run `wizard/start_wizard_dev.cmd` on Windows or
`./wizard/start_wizard_dev.sh` on Linux from the repository root instead. Both
launchers build and start the Java host and its UI and require Java 25 and
Node.js 22.

Drafts and uploaded bytes are stored together in IndexedDB. Existing
LocalStorage, IndexedDB, or AppData drafts are not migrated. The Wizard allows
only one editing tab at a time. Opening, creating, and deleting games therefore
fail closed when another tab holds the editor lock or the browser does not
support Web Locks. Use a current supported browser.

Production uses the Java host at `127.0.0.1:27777` for validation and temporary
`WizardRoom.jar` packaging. A successful package response is downloaded by the
browser; no authoring project directory is written. The host does not store
drafts or uploads. When a draft is locally ready, the native host validates it
in the background two seconds after the last content change. The current JAR
requires Java 25.
