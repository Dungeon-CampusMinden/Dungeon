# Room Package Format

## Current Decisions

- The editable authoring source is `deer.json`.
- Generated room packages are shared as `deer.zip`.
- `deer.zip` contains exactly one playable escape-room level.
- The first Wizard UI prototype exposes only `deer.json` export. `deer.zip`
  generation is the next integration step.
- The first version supports only LibGDX-friendly custom media.
- The first version uses one standard theme.
- The wizard does not generate evaluation artifacts, telemetry profiles,
  debriefing guides, or pre/post tests in v0.
- Generated runtime files are derived from `deer.json` and can be overwritten by
  the generator.
- Custom assets are content assets, not theme replacements.
- The educator workflow should later become one-button simple: fill the wizard,
  press generate, receive a shareable `deer.zip`.
- The current web wizard target is smaller: create, validate, and export
  `deer.json`.
- The Java generator is responsible for producing the runtime package later.
- A preview is useful, but optional for the first draft. Regeneration with a new
  seed is required.

## Package Shape

The canonical representation during authoring and preview is an unpacked folder.
The archive form, `deer.zip`, is only for sharing, import, export, and
submission.

## Authoring And Generation Workflow

The current UI-first workflow is:

```text
Web wizard
  -> writes/updates deer.json
  -> validates game-breaking constraints
  -> exports deer.json
```

The later integrated workflow is:

```text
Web wizard
  -> writes/updates deer.json
  -> calls Java generator
  -> generator validates the authoring model
  -> generator creates/updates runtime files
  -> generator exports deer.zip
```

The educator should not eventually have to manually export a JSON file and
import it into a separate tool. For the first UI prototype, however, explicit
`deer.json` export is the intended deliverable.

Regeneration must be supported by changing the generation seed and running the
generator again. With the same `deer.json` and the same seed, the output should
be deterministic. With a different seed, the room may receive a different
layout while preserving the same riddles, assets, graph dependencies, and
solvability rules.

Preview is a separate feature decision. The first version may omit a live
playable preview if the Java/web boundary is too expensive, but the package
format should not prevent one later.

```text
room-package/
  deer.json
  room.json
  README.md
  levels/
    main_1.level
  riddles/
    graph.json
  assets/
    custom/
  validation/
    validation-report.md
```

## File Roles

### `deer.json`

The only editable source file. The wizard reads and writes this file.

It contains the authoring model:

- room metadata,
- selected predefined theme,
- target audience,
- player count and time limit,
- story beats,
- riddle graph,
- riddle parameters,
- custom content asset references,
- generation constraints.

The current detailed draft is stored in
[`deer-json-spec.md`](deer-json-spec.md). The first machine-readable schema is
stored in [`deer.schema.json`](deer.schema.json). A small valid discussion
example is stored in [`examples/deer.example.json`](examples/deer.example.json).

### `room.json`

Generated runtime manifest.

It contains:

- package format version,
- room id,
- start level,
- selected theme id,
- required systems,
- player setup,
- asset mappings,
- riddle graph entrypoint,
- validation artifact entrypoints.

### `levels/main_1.level`

Generated Dungeon level file. For v0, one package contains one playable level.
The naming follows the existing Dungeon level variant convention.

### `riddles/graph.json`

Generated runtime riddle graph. This should remain readable even if the first
runtime implementation later compiles it into Petri-net structures.

### `assets/custom/`

Custom educator uploads. These are limited to LibGDX-friendly content media in
v0, such as images and audio files supported by the runtime.

Simple text content should be entered directly in the wizard and stored in
`deer.json`, not uploaded as arbitrary documents.

Not supported in v0:

- custom tilesets,
- custom player sprites,
- custom enemy sprites,
- custom UI skins,
- shader/theme replacements,
- arbitrary documents that LibGDX cannot load directly.

If an educator needs document-like content, the first version should prefer text
entered into the wizard or image assets generated/exported outside the wizard.

### `validation/`

Generated validation support. This is not learning evaluation. It only records
whether the package is structurally and technically usable.

`validation-report.md` records schema, graph, asset, runtime, and generator
checks.

The client should prevent invalid states before the educator can export
`deer.json`. Generator validation is still required later as a safety net for
application bugs, edited JSON files, and future import workflows.

The export button should be disabled until the client preflight is valid.
Blocking errors should be shown at the step or graph element that caused them.
Warnings may remain visible at export time, but must not be confused with
blocking errors.

## Evaluation, Debriefing, Telemetry, Pre/Post Tests

The wizard should not generate evaluation artifacts, debriefing guides,
telemetry profiles, or pre/post tests in v0.

This can be revisited later, but it is intentionally outside the first final
version. Plausible later options are:

- educator-authored questions inside the wizard,
- predefined learning environments with predefined question banks,
- external question sets imported as content.
