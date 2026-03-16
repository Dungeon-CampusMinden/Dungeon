# Blockly Dungeon Codex

This document is the internal technical "codex" for the Blockly part of the Dungeon project. It
is aimed at developers who are familiar with Java, TypeScript, Gradle and Node, but are new to
this codebase.

It focuses on:

- what the Blockly tooling is and how it fits into the overall Dungeon ecosystem,
- how the `blockly` module is segmented into subprojects,
- how levels/tasks are represented end‑to‑end,
- how the web frontend talks to the Java backend,
- how to build, run and extend the system,
- where to find more detailed documentation.

For a general, non‑technical overview of the Dungeon project and its educational context, see the
repository‑root `README.md`.

For user/instructor‑focused Blockly docs (German), see `blockly/doc/readme.md`.

---

## 1. Big Picture: What Blockly Dungeon Is

### 1.1 Purpose

Blockly Dungeon is a **visual programming interface** on top of the Java‑based Dungeon engine.
Instead of writing Java, learners assemble **Blockly blocks** in a browser to control a hero in
a 2D dungeon. The goal is to teach basic programming concepts (sequences, loops, conditions,
variables, arrays, functions) via game‑based learning.

It is primarily used in the PRODUS workshops as "Workshop 1: Blockly‑Dungeon" (see root
`README.md`).

### 1.2 Relation to the Dungeon Engine

The underlying game engine and DSL live in the `dungeon/` module. The same engine is also used
by other experiences (`advancedDungeon/`, `escapeRoom/`, `theLastHourEscapeRoom/`, etc.).

The `blockly/` module provides:

- a **Java application** (`client.Client`) that boots the engine in a special Blockly mode,
  registers a curated set of levels, and exposes an HTTP API for the web UI,
- a **web frontend** (`blockly/frontend/`) built with TypeScript + Vite + Google Blockly,
- a **compiler/tooling subproject** (`blockly/dgir-compiler/`) used by the Java side for
  parsing/compiling user code,
- a **VS Code extension** (`blockly/vs-code-extension/`) for Editor‑based workflows,
- **documentation** and **assets** that are specific to the Blockly experience.

Conceptually:

- The learner works in the browser.
- The browser generates Java‑like code or a command stream from the block structure.
- The Java backend receives this code via HTTP, validates/interprets it, and drives the hero
  inside the Dungeon engine.


---

## 2. Module and Subproject Overview

At the top level, this repository contains several game experiences and tooling modules. For
this codex, the focus is on `blockly/`:

- `blockly/`
  - `build.gradle` – Gradle `application` project for the Java backend
  - `src/main/java/` – Java application and integration code (entry point `client.Client`)
  - `src/main/antlr/blockly.g4` – ANTLR grammar used by the compiler/tooling
  - `assets/` – assets that the Java application exposes (e.g. levels, items, monsters,
    objects, popups, tiles). These are added as `main` resources in the Gradle build.
  - `dgir-compiler/` – Gradle subproject providing the compiler (see its own `README.md`)
  - `frontend/` – TypeScript + Vite web UI for Blockly Dungeon
  - `vs-code-extension/` – VS Code extension for Dungeon/Blockly related workflows
  - `doc/` – German documentation for installation, usage and extension of Blockly Dungeon
  - `tooling/` – helper scripts for installing/updating and killing dev servers

External linkage:

- `dungeon/` – main game engine and DSL, used as a dependency by `blockly` (see
  `blockly/build.gradle`).
- Root `start_blockly.sh` – convenience script to start backend and frontend together.

### 2.1 Java Backend (`blockly/` Gradle project)

`blockly/build.gradle` defines a Gradle `application` with main class `client.Client`:

- depends on `:dungeon` and `:blockly:dgir-compiler`,
- bundles a **fat JAR** with all runtime dependencies,
- exposes a `runJar` task which passes a `web` system property to the main class,
- registers `assets/` as resources for the main source set.
- exposes `run` and `runJar` tasks; both can optionally start the Vite frontend and open it in your browser (defaults to `http://localhost:5173/`).

#### Running the backend (with optional frontend)

- Backend only (previous default):
  - `./gradlew :blockly:run`
- Backend + frontend auto-start + browser open:
  - `./gradlew :blockly:run -PstartFrontend=true`
  - accepts `-Pfrontend=true` as equivalent flags.
- Jar run with the same behavior:
  - `./gradlew :blockly:runJar -PstartFrontend=true`
- Passing CLI args to `client.Client` when using Gradle (alternative to project properties):
  - `./gradlew :blockly:run --args="web=true"`
  - `./gradlew :blockly:runJar --args="web=true"`
  - combine with frontend flag if desired: `./gradlew :blockly:run -PstartFrontend=true --args="web=true"`
- What the flags do:
  - `startFrontend`/`frontend`/`web` project property: triggers `npm run dev` in `blockly/frontend`, waits briefly, then attempts to open the frontend URL.
- Prereqs: ensure Node/npm deps are installed once in `blockly/frontend` (`npm install`).

Relevant excerpt:

- Main class: `client.Client`.
- Jar manifest: `Main-Class: client.Client`.
- Resource root: `sourceSets.main.resources.srcDirs = ['assets/']`.

### 2.2 Frontend (`blockly/frontend/`)

A separate Node project with Vite:

- `blockly/frontend/package.json`
  - scripts:
    - `npm run dev` – start Vite dev server (default `http://localhost:5173`)
    - `npm run build` – `tsc && vite build`
    - `npm run preview` – preview the production build
    - `npm run lint` / `lint:fix`
  - dependencies:
    - `blockly` – Google Blockly core library
  - devDependencies: TypeScript, Vite, ESLint, etc.

Important source files:

- `src/index.ts` – main entry point; bootstraps Blockly workspace and UI.
- `src/config.ts` – frontend configuration (API URL, limits, visibility flags).
- `src/blocks/dungeon.ts` – custom block definitions for Dungeon.
- `src/generators/java.ts` – Java code generator for the custom blocks.
- `src/toolbox.ts` – Blockly toolbox (categories & blocks) used in the UI.
- `src/utils/level.ts` – level list management, selector UI, level progress.
- `src/utils/workspace.ts` – workspace helpers: start/reset buttons, REST calls,
  limiting logic, and level completion checks.
- `src/serialization.ts` – workspace saving/loading based on level.

Details on this architecture and how to extend it are in `blockly/doc/extend.md`.

### 2.3 Compiler (`blockly/dgir-compiler/`)

The `dgir-compiler` is a separate Gradle subproject whose JAR is pulled into the main
`blockly` JAR (see dependency and `jar` task in `blockly/build.gradle`). It contains the
compiler which can compile into the DGIR which is executed by the blockly backend.

See `blockly/dgir-compiler/README.md` for the details of its purpose and usage.

### 2.4 VS Code Extension (`blockly/vs-code-extension/`)

This subproject adds editor integration:

- `vs-code-extension/src/extension.ts` – main extension entry point.
- `vs-code-extension/src/handlers/*.ts` – handlers for sending Blockly files, providing
  language features, etc.
- `vs-code-extension/package.json` – extension metadata, activation events and commands.

See `blockly/vs-code-extension/README.md` for installation and usage instructions.

### 2.5 Documentation (`blockly/doc/`)

Key entry point:

- `blockly/doc/readme.md` – overview of all Blockly‑specific docs (in German).

Important documents include:

- `introduction.md` – conceptual intro and motivation.
- `installation.md` – installation workflow.
- `usage.md` – how to use the web UI and blocks.
- `extend.md` – architecture and how to add new blocks.
- `blocks.md`, `examples.md`, `resources.md`, `troubleshooting.md` – reference and
  troubleshooting.
- `visualization_concept.md` – HUD and visualization ideas.

This codex links to these docs rather than duplicating them.


---

## 3. Java Backend: Entry Point and Runtime Model

### 3.1 `client.Client` Overview

The main entry point for the Blockly‑aware dungeon is `blockly/src/main/java/client/Client.java`.
It is intended to be run via the Gradle task `runBlockly` (see top‑level Gradle config).

High‑level responsibilities:

- parse CLI arguments (notably `web=true`),
- configure the `Game` (frame rate, window, keyboard configs, etc.),
- register all Blockly levels with the `DungeonLoader`,
- create the hero entity and core systems,
- start the HTTP server used by the Blockly frontend,
- manage level load hooks and restarts.

Key fields and flags:

- `public static boolean runInWeb = false;`
  - set to `true` when started with `web=true` (this is what `start_blockly.sh` does for the
    web version).
- `public static final String WIZARD_NAME = "Algorim";` – name of the Blockly hero.
- `public static final Vector2 MOVEMENT_FORCE = Vector2.of(7.5, 7.5);` – base movement.

### 3.2 Level Registration

`onSetup()` is registered via `Game.userOnSetup` and wires levels into the engine using the
`DungeonLoader`:

- chapter 1: levels `level001`–`level012`
- chapter 2: levels `level013`–`level017`
- chapter 3: levels `level018`–`level022`

Level registration follows this pattern:

- `DungeonLoader.addLevel(Tuple.of("level001", Level001.class));`

Each `LevelXXX` class (in `level/produs` packages) defines the actual level layout, goals,
triggers and NPCs, using the same level DSL and engine concepts as the main `dungeon` module.

The first load chooses the initial level by index: `DungeonLoader.loadLevel(0);`.

### 3.3 Game Configuration

`configGame()` performs core engine configuration:

- `Game.loadConfig(new SimpleIPath("dungeon_config.json"), ...)` – loads keyboard and general
  engine configuration from the repo‑root `dungeon_config.json`.
- `Game.frameRate(30);` – 30 FPS.
- `Game.disableAudio(true);` – audio disabled by default for the Blockly setup.
- `Game.resizeable(true);`
- `Game.windowTitle("Blockly Dungeon");`.

### 3.4 Systems and Execution

`createSystems()` registers core systems with `Game.add(...)`, including:

- dungeon core systems: `CollisionSystem`, `AISystem`, `HealthSystem`, `ProjectileSystem`,
  `SpikeSystem`, `IdleSoundSystem`, `PathSystem`, `LevelTickSystem`, `LeverSystem`,
  `BlockSystem`, `FallingSystem`, `PitSystem`, `FogSystem`, `PressurePlateSystem`, etc.
- `TintTilesSystem` – visual effect.
- `BlocklyCommandExecuteSystem` – interprets commands sent from Blockly and applies them to the
  hero/world.
- conditional debug systems: `Debugger`, `DebugDrawSystem`, `LevelEditorSystem` when
  `DEBUG_MODE` is `true`.

`onLevelLoad()` is registered via `Game.userOnLevelLoad` to:

- stop currently running Blockly code (`BlocklyCodeRunner.instance().stopCode()`),
- fully stop and clear the `BlocklyCommandExecuteSystem` to avoid leftover commands,
- reset hero velocity and ammunition for the new level.

### 3.5 HTTP Server for Frontend Communication

The backend exposes an HTTP API (and properly tracks dialog state) via:

- `Server.instance().start()` from `server.Server`, returning an embedded `HttpServer`.
- `DialogTracker.instance()` for dialog/interaction tracking.

Endpoints are called by the frontend through the `api` utilities (see below). They cover:

- listing available levels,
- selecting a level,
- submitting generated code,
- checking code execution status,
- resetting & clearing the current run.

(Exact route names are defined in the Java server code; see `blockly/doc/extend.md` and the
Java `server` package for details.)

### 3.6 Hero Lifecycle and Restart

`Client.createHero()`:

- removes any existing entities with `PlayerComponent`,
- creates a new hero entity via `HeroTankControlledFactory.blocklyHero(...)`,
- attaches an `AmmunitionComponent`,
- adds the hero to the game.

`Client.restart()`:

- can be requested from non‑main threads; in that case it schedules itself for the next game
  tick and waits via `Server.waitDelta()`,
- stops Blockly code and clears commands,
- removes all entities and reloads the current level,
- restarts `PositionSystem`, clears dialogs.

This restart logic is used to robustly reset the game state when the user restarts their
program from the frontend.


---

## 4. Frontend Architecture

The frontend lives in `blockly/frontend/` and is described in more detail in
`blockly/doc/introduction.md` and `blockly/doc/extend.md`. Below is an overview focused on
how it integrates with the backend and levels.

### 4.1 Entry Point: `src/index.ts`

`index.ts` sets up the Blockly workspace and the UI:

- imports and registers **custom blocks** from `blocks/dungeon.ts`,
- imports the **Java generator** from `generators/java.ts`,
- loads existing workspace state from local storage (`serialization.ts`),
- initializes the **toolbox** from `toolbox.ts`,
- applies frontend configuration from `config.ts`,
- sets the Blockly locale to German (`blockly/msg/de`).

Core steps:

1. Get references to DOM elements (Blockly div, generated code div).
2. Inject Blockly workspace:
   - `toolbox: toolbox`,
   - zoom config, trashcan, media directory.
3. Call `setupButtons(workspace)` (see below).
4. Register change listeners:
   - `Blockly.Events.disableOrphans` to disable unconnected blocks,
   - workspace listeners that save the current state on meaningful changes,
   - workspace listener that regenerates code and updates the code view.
5. Initialize the **level selector** via `setupLevelSelector()` and listen to
   `"levelChanged"` events.
6. On `levelChanged`:
   - save workspace for the old level,
   - adjust toolbox (block/category availability) based on level restrictions,
   - clear workspace, load new level state, ensure a start block exists,
   - refresh toolbox.
7. Optionally hide generated code or server response sections, based on `config` flags.
8. Finally, call `updateLevelList()` to fetch level names from the backend and select the
   initial level, load its workspace and center the view.

### 4.2 Configuration: `src/config.ts`

`config.ts` defines the frontend configuration interface and default values:

- `API_URL` – base URL for backend API (defaults to `http://localhost:8080/`).
- `CHARACTER_MAX_MOVEMENT`, `VARIABLE_MAX_VALUE`, `REPEAT_MAX_VALUE`, `ARRAY_MAX_VALUE` –
  numeric limits that restrict how large user inputs may be.
- `HIDE_GENERATED_CODE` – whether to hide the generated Java code view.
- `HIDE_RESPONSE_INFO` – whether to hide the backend response display.
- `LIMITS` – optional per‑block limits (regex‑like keys; values are max allowed counts).

These values influence both the UI constraints (e.g. max numbers in blocks) and the runtime
limits applied by the utility logic.

### 4.3 Level Handling: `src/utils/level.ts`

This module connects frontend level selection with the backend:

- keeps a `levelNames: string[]` list and `currentLevelIndex`,
- reads and writes `levelProgress` in `localStorage` to control which levels are unlocked,
- provides `isLevelAvailable(levelName)` and `setCurrentLevel(newLevelName, force)`.

`updateLevelList()`:

- repeatedly calls `call_levels_route()` (see API section) until it gets a non‑empty list of
  levels or a timeout is reached,
- fills `levelNames`, updates the level `<select>` element and arrow buttons,
- selects the first level and dispatches a `"levelChanged"` event.

`setupLevelSelector()`:

- dynamically adds a level selector bar (`<select>` with prev/next buttons) to the `<header>`,
- wires up change/click handlers that:
  - call `setCurrentLevel(...)`, which in turn
  - calls the backend `call_level_route()` API to activate that level and receive any
    **blocked blocks/categories** for the level.

`completeLevel()` is called from the workspace logic when the backend reports that the current
level has changed, and it advances `levelProgress` and selects the next level accordingly.

### 4.4 Workspace & Execution: `src/utils/workspace.ts`

This module contains most of the glue logic between the Blockly workspace and the backend
execution API.

Key responsibilities:

- Detect and track the **start block**:
  - `getStartBlock(workspace)` scans for the single `"start"` block that anchors the program.
- Toolbox manipulation:
  - `getAllBlocksFromToolboxDefinition`, `getAllCategoriesFromToolboxDefinition`,
    `blockElementsFromToolbox`, `resetBlocksAndCategories` – used to disable blocks or
    categories depending on level‑specific restrictions.
- Error display:
  - `clearAllWarnings(workspace)` and `displayErrorOnBlock(block, error)` for visual feedback.

Most importantly, it wires the **Start** and **Reset** buttons:

- `setupButtons(workspace)`:
  - obtains `startBtn`, `resetBtn`, and `delay` input from the DOM,
  - calls `setupStartButton` and `setupResetButton`.

`setupStartButton`:

1. On click:
   - disables the Start button,
   - programmatically clicks Reset to ensure a clean state, then waits briefly (workaround
     for race conditions),
   - parses the delay input (defaults to 1s, errors if not a number).
2. Determines the current start block:
   - `currentBlock = getStartBlock(workspace);`
3. Generates Java code:
   - `const code = javaGenerator.workspaceToCode(workspace);`
4. Sends the code to the backend:
   - `call_code_route(code)` – POSTs code to the backend; if it fails, clears the backend
     (`call_clear_route`) and re‑enables the Start button.
5. Polls the backend for execution status:
   - uses `call_code_status_route()` in a loop until status is `"completed"` or `"error"`.
6. After execution ends, it waits a bit and calls `call_level_route()` without arguments to get
   the currently active level from the backend; if that differs from `getCurrentLevel()`, it
   calls `completeLevel()`.
7. Finally, clears highlight and re‑enables the Start button.

`setupResetButton` (truncated above, see source):

- sends a reset request to the backend via `call_reset_route()`,
- typically also clears backend state via `call_clear_route()`,
- resets highlights and warnings in the workspace.

This workflow ties the frontend buttons directly to the backend execution lifecycle.

### 4.5 Blocks and Generators

Blocks are defined in `src/blocks/dungeon.ts` using Blockly’s JSON definition format. Important
points:

- Some **predefined blocks** are explicitly removed:
  - `logic_boolean`, `controls_if`, `controls_ifelse`.
  - This enforces a simplified, curated block set.
- A custom **start block** (`type: "start"`) represents the program entry point.
- Block categories include:
  - movement (`move`, `rotate` with a `Direction` input),
  - variables (`set_number`, `set_number_expression`, `get_variable`, `var_number`),
  - arithmetic expressions (`expression`),
  - directions (`direction_up`, `direction_down`, `direction_left`, `direction_right`,
    `direction_here`),
  - arrays (`var_array`, and others further down the file),
  - control structures (loops and conditionals),
  - skills/actions (attack, interact, spells, etc.),
  - function definition and invocation blocks.

Many blocks rely on `config` for limits (e.g. `max: config.VARIABLE_MAX_VALUE`).

The code generation side (in `src/generators/java.ts` and `src/generators/java/...`) implements
one function per block type, mapping a block instance to Java‑like code. Examples are given in
`blockly/doc/extend.md`; typical patterns:

- retrieving numeric fields via `block.getFieldValue("amount")`,
- concatenating repeated actions (e.g. movement loops),
- emitting calls like `oben();`, `links();`, `nutze();` etc.

On the backend, this generated code is ultimately run by `BlocklyCodeRunner`, which:

- builds a `UserScript.java` with an `execute()` method containing the user’s code,
- compiles and runs it within a controlled environment,
- whitelists only allowed methods in `BlocklyCommands` (for security and pacing via `sleep`).

For the full details of this pipeline, see `blockly/doc/extend.md`.


---

## 5. End‑to‑End Level and Task Flow

This section describes how a level/task flows through the system, from assets and Java classes
through to the web UI and completion detection.

### 5.1 Terminology

In the Blockly Dungeon context:

- **Level**: a single playable dungeon map with a clear goal (e.g. reach exit, collect item,
  defeat enemy). Levels are identified by IDs like `level001`, `level002`, ...
- **Chapter**: a group of levels with a common theme/difficulty (e.g. chapter 1 is
  `level001`–`level012`).
- **Task/Quest**: the in‑level goal and constraints the learner must satisfy. Tasks are mostly
  expressed inside the level’s logic (Java) and assets (NPC dialogs, triggers) rather than in a
  standalone task file.

### 5.2 Java Level Representation

On the backend, levels are represented as Java classes under `level.produs.*` (see imports in
`client.Client`). Each level class:

- defines the map layout (tiles, objects, monsters),
- wires interactions, dialogues and triggers,
- sets the win condition (e.g. reaching an exit, toggling switches, etc.),
- may configure which blocks or categories are allowed via the backend API.

These classes are registered using `DungeonLoader.addLevel(Tuple.of("levelXXX", LevelXXX.class))`.
When the game runs, `DungeonLoader.loadLevel(0)` loads the first level; later level changes are
triggered by calls from the frontend and by the internal completion logic.

### 5.3 Assets

Level‑specific and generic assets used by the Blockly setup are stored in `blockly/assets/`.
Typical directories include:

- `items/` – consumables and objects.
- `monsters/` – enemy sprites and metadata (see `blockly/assets/monsters/README.md`).
- `objects/` – static/dynamic level objects.
- `level001/`, `levels/` – tile maps, metadata or additional per‑level assets.
- `popups/` – tutorial or hint images/dialog assets.
- `stone/`, `media/` – other graphics and media.

These are available on the classpath because `assets/` is configured as a resource directory in
`blockly/build.gradle`. The Java level classes load them via the engine’s asset utilities.

### 5.4 Frontend Level Representation

On the frontend, levels are known by their string IDs (e.g. `"level001"`). The mapping between
ID and human‑readable name/description is generally done on the backend or via predefined
conventions; the UI currently displays the raw IDs in the selector.

Important pieces:

- `updateLevelList()` obtains the list of level IDs from the backend via `call_levels_route()`.
- `setCurrentLevel(name)` informs the backend of the selected level via `call_level_route(name)`
  and dispatches a `"levelChanged"` event with backend‑supplied restrictions.
- The workspace is stored per level, typically using keys like `"BlocklyWorkspace-<level>"` or
  a similar scheme in `serialization.ts` (localStorage‑based).
- `levelProgress` in `localStorage` gates which levels are selectable, effectively encoding the
  learner’s progression.

### 5.5 Block/Category Restrictions per Level

The backend can respond to `call_level_route(levelName)` with a structure that contains a
`block_blocks` array. The frontend interprets this as a list of blocks/categories to disable
for the current level.

In `setCurrentLevel`:

- the response is read as `blockBlocks = response.block_blocks ?? [];`,
- the resulting `blockedElements` are included in the `LevelChangedEvent` fired to listeners.

In `index.ts`, the level change listener calls:

- `blockElementsFromToolbox(toolbox, blockedElements, "Not available in Level");`

`blockElementsFromToolbox`:

- computes a regex from the blocked element names,
- hides or marks categories as disabled if their `name` matches,
- marks blocks as disabled if their `type` matches.

This mechanism allows each level to restrict the available constructs and gradually introduce
more complex blocks.

### 5.6 Completion Detection and Level Advancement

After a program run finishes (see `setupStartButton` above), the frontend:

1. waits a short period for the backend to finalize level logic,
2. calls `call_level_route()` **without** parameters to ask the backend which level is now
   active,
3. compares the backend’s level name with `getCurrentLevel()`.

If the backend reports a different level, `completeLevel()` is called. This:

- increments `levelProgress` (if needed),
- selects the next level from `levelNames` and triggers a level change event.

Thus, the **backend is the source of truth** for whether the task has been successfully
completed and which level to load next.


---

## 6. API and Communication (Frontend ↔ Backend)

### 6.1 API URL

The frontend talks to the backend using HTTP requests against `config.API_URL`, which defaults
to:

- `http://localhost:8080/`

When running the Java backend and Vite dev server locally on the same machine, this is the
expected configuration.

### 6.2 Main Routes

The exact implementation of `call_*_route` functions lives in the API utilities under
`blockly/frontend/src/api/` (see `blockly/doc/extend.md`). At a high level, there are routes
for:

- `call_levels_route()` – GET a list of available level IDs.
- `call_level_route(levelName?)` –
  - with a parameter: set the active level and receive configured restrictions
    (`block_blocks`) and metadata,
  - without a parameter: query the currently active level (used for completion checks).
- `call_code_route(code)` – POST the generated code to be executed.
- `call_code_status_route()` – GET the current execution status (`running`, `completed`,
  `error`).
- `call_reset_route()` – reset the current run and hero state.
- `call_clear_route()` – clear any leftover code or commands on the backend.

The Java side (`server.Server` and related handlers) maps these HTTP requests to operations in
`BlocklyCodeRunner`, `DungeonLoader` and the systems registered in `Client`.

---

## 7. Build and Run Workflows

This section collects the typical ways to build and run the Blockly Dungeon system.

### 7.1 Prerequisites

From the root `README.md` and the build files:

- **Java**: JDK 21 (LTS) installed and on `PATH`.
- **Gradle**: use the provided wrapper (`./gradlew`) rather than a system Gradle.
- **Node.js + npm**: any reasonably recent LTS version should work; see
  `blockly/frontend/package.json` for TypeScript/Vite versions.
- **Browser**: a modern browser with JavaScript ES modules support.

For more detailed installation steps (German), see:

- `blockly/doc/installation.md`.
- `blockly/tooling/install_doc.md`.

### 7.2 Fast Path: `start_blockly.sh`

At the repository root, `start_blockly.sh` orchestrates starting the backend and frontend.

The script roughly does the following:

1. Ask: `Web Version (yes/no)?`.
2. If **yes**:
   - runs `./gradlew runBlockly -Pweb=true &` to start the Java backend in web mode,
   - `cd blockly/frontend` and run `npm run dev &` to start the Vite dev server,
   - waits 5 seconds and then opens `http://localhost:5173/` in the default browser,
   - waits until you press `CTRL+C`, then stops both processes.
3. If **no**:
   - runs `./gradlew runBlockly` (without web flag); this mode is used for the Java‑only
     version (e.g. with the VS Code extension controlling the hero).

The script also sets a trap to kill both background jobs on exit.

This is the recommended way for workshop setups and quick local testing of the web UI.

### 7.3 Running Backend Only (Gradle)

From the repository root:

- Build everything:

  ```bash
  ./gradlew build
  ```

- Run the Blockly backend in **web mode** (expected with the frontend):

  ```bash
  ./gradlew runBlockly -Pweb=true
  ```

  This starts the dungeon engine and HTTP server on port 8080 (see server config).

- Run the Blockly backend in **non‑web mode**:

  ```bash
  ./gradlew runBlockly
  ```

  This is typically used with the VS Code extension or Java‑only workflows.

You can also run the `blockly` fat JAR produced by the `jar` task directly (see
`blockly/build.gradle`), passing `web=true` as a CLI argument.

### 7.4 Running Frontend Only (Node/Vite)

From `blockly/frontend/`:

- Install dependencies:

  ```bash
  npm install
  ```

- Start the Vite dev server:

  ```bash
  npm run dev
  ```

  By default this runs on `http://localhost:5173`. Ensure that the Java backend is already
  running and reachable via `config.API_URL`.

- Build production assets:

  ```bash
  npm run build
  ```

- Preview the production build:

  ```bash
  npm run preview
  ```

### 7.5 dgir-Compiler and Other Gradle Tasks

The `dgir-compiler` project is built automatically when building the `blockly` module, but can
also be built explicitly:

```bash
./gradlew :blockly:dgir-compiler:build
```

The `blockly` project uses ANTLR to generate parser code from `blockly.g4`; this is wired by
`generateGrammarSource` in `blockly/build.gradle`.

Tests in the `blockly` module (and subprojects) use JUnit via the shared support dependencies
from the root `dependencies.gradle`.

---

## 8. Where to Find More Documentation

For deeper dives or user/instructor‑oriented documentation, use these entry points:

- `blockly/doc/readme.md` – overview of all Blockly docs.
- `blockly/doc/introduction.md` – conceptual intro and context.
- `blockly/doc/installation.md` – installation and environment setup.
- `blockly/doc/usage.md` – explains the web UI, buttons and advanced usage.
- `blockly/doc/blocks.md` – block reference.
- `blockly/doc/examples.md` – usage examples and patterns.
- `blockly/doc/extend.md` – architecture + how to add blocks and server functionality.
- `blockly/doc/troubleshooting.md` – common issues and solutions.
- `blockly/doc/resources.md` – further reading and external resources.

Cross‑project docs:

- Root `README.md` – high‑level project overview and funding context.
- `dungeon/doc/` – engine and DSL documentation (e.g. animation, DSL usage).
- `doc/` – additional global documentation and publication material.


---

## 9. Onboarding for New Contributors

This section is a suggested path for developers who want to extend or debug the Blockly
experience.

### 9.1 Mental Model Checklist

Before making changes, you should be comfortable with:

- The **Dungeon engine** (entities, systems, levels) – skim `dungeon/doc` and the root
  `README.md`.
- The **frontend architecture** – skim `blockly/doc/introduction.md` and `blockly/doc/extend.md`.
- The **end‑to‑end flow**: Blockly blocks → generated Java code → `BlocklyCodeRunner` → hero
  in the dungeon.

### 9.2 First‑Time Setup

1. Install JDK 21 and Node.js.
2. Clone this repository.
3. From the repo root, run:

   ```bash
   ./gradlew build
   ```

4. From `blockly/frontend/`, run:

   ```bash
   npm install
   ```

5. Start the full web experience via:

   ```bash
   ./start_blockly.sh
   ```

6. Choose `yes` for the web version. After a few seconds, your browser should open
   `http://localhost:5173/`. Select a level and run some simple programs.

If anything fails, consult `blockly/doc/installation.md` and `blockly/doc/troubleshooting.md`.

### 9.3 Good First Changes

Some low‑risk ways to get started:

- **Tweak an existing block**
  - Files: `blockly/frontend/src/blocks/dungeon.ts`, `blockly/frontend/src/generators/java.ts`
    and `blockly/frontend/src/generators/java/*`.
  - For example, adjust the tooltip, label, or constraints of a movement or variable block.

- **Add a new block**
  - Follow `blockly/doc/extend.md`:
    1. add block to `toolbox.ts`,
    2. define it in `blocks/dungeon.ts`,
    3. implement its generator function in `generators/java/...`,
    4. implement the underlying command in the Java backend (`BlocklyCommands`, etc.).

- **Adjust toolbox per level**
  - Files: `blockly/frontend/src/utils/level.ts`, backend level configuration.
  - Extend the backend’s `block_blocks` data so certain blocks/categories are unavailable in
    early levels.

- **Add or tweak a simple level**
  - Files: `level/produs/LevelXXX.java` (where `XXX` is a new number), registration in
    `client.Client`, assets in `blockly/assets/levels/` and related directories.

- **Documentation improvements**
  - Files: `blockly/doc/*.md`, `blockly/src/README.md` (this codex), top‑level docs.

### 9.4 Conventions and Tooling

- Java:
  - Uses typical Dungeon coding style (see existing code in `dungeon/` and `blockly/`).
  - Tests rely on JUnit; run with `./gradlew test`.

- TypeScript:
  - ESLint is configured in `blockly/frontend/eslint.config.js` and
    `blockly/vs-code-extension/eslint.config.mjs`.
  - Use `npm run lint` (and `lint:fix`) in the respective projects.

- General contribution process:
  - See `CONTRIBUTING.md` at the repo root for guidelines on issues, branches and pull
    requests.

### 9.5 Common Pitfalls

- **Port conflicts**: Vite defaults to port 5173, backend to 8080. If anything else is using
  those ports, adjust configuration or stop the conflicting services.
- **API URL mismatch**: `config.API_URL` must match where the backend is actually running.
- **Version mismatch**: old Java or Node versions may cause Gradle or npm errors; align with
  versions implied by `build.gradle` and `package.json`.
- **Frontend cannot reach backend**: check network errors in the browser dev tools, ensure the
  backend is running, and CORS configuration allows the frontend origin.

For more, see `blockly/doc/troubleshooting.md`.


---

## 10. Reference: Important Files and Entry Points

**Backend (Java)**

- `blockly/build.gradle` – Gradle project configuration for the Blockly backend.
- `blockly/src/main/java/client/Client.java` – main entry point; configures game, levels,
  systems and HTTP server.
- `blockly/src/main/antlr/blockly.g4` – ANTLR grammar used by the compiler.
- `dungeon_config.json` – engine configuration loaded by `Client`.

**Frontend (TypeScript/Vite)**

- `blockly/frontend/package.json` – scripts and dependencies.
- `blockly/frontend/vite.config.ts` – Vite build and dev configuration.
- `blockly/frontend/tsconfig.json` – TypeScript compiler configuration.
- `blockly/frontend/src/index.ts` – frontend bootstrap, workspace and UI wiring.
- `blockly/frontend/src/config.ts` – API URL and limits.
- `blockly/frontend/src/blocks/dungeon.ts` – block definitions.
- `blockly/frontend/src/generators/java.ts` and `src/generators/java/*` – code generators.
- `blockly/frontend/src/toolbox.ts` – toolbox categories and block catalog.
- `blockly/frontend/src/utils/level.ts` – level list, selector and progress.
- `blockly/frontend/src/utils/workspace.ts` – Start/Reset buttons, execution control,
  toolbox restriction logic.
- `blockly/frontend/src/serialization.ts` – workspace persistence.

**Other**

- `blockly/dgir-compiler/` – compiler subproject (see its `README.md`).
- `blockly/vs-code-extension/` – VS Code extension (see its `README.md`).
- `blockly/doc/` – user and extension docs.
- `start_blockly.sh` – helper script to start backend and frontend together.

