# Wizard spec generator

The Spec Generator is the browser UI for authoring a private Wizard draft and
turning it into a DEER project through the local Java host.

## Install and run

- Run `npm install`.
- Run `npm run dev` for the UI-only Vite development server.
- Open the URL printed by Vite, usually `http://localhost:5173/`.

For the complete host flow, run `wizard/start_wizard_dev.cmd` from the
repository root instead. It starts the Java host and its UI.

Draft v1 and uploaded bytes are stored together in a new IndexedDB. Existing
LocalStorage, IndexedDB, or AppData drafts are not migrated. V0 does not
support multiple tabs editing the Wizard at the same time.

Production uses the Java host at `127.0.0.1:27777` for validation and temporary
`WizardRoom.jar` packaging. A successful package response is downloaded by the
browser; no authoring project directory is written. The host does not store
drafts or uploads. The current JAR requires Java 25; a runtime-bundled
`jpackage` distribution remains a later milestone.
