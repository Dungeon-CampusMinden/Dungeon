import type { Asset, AssetMediaType, DeerProject, Riddle, RiddleGraph, Surface } from "./DeerSchema";
import { getBundledAssetPaths, isBundledAssetPath } from "@/components/assets/assetPaths";
import { VALIDATED_TAB_IDS, type ValidatedTabId } from "./Tabs";

export type IssueSeverity = "info" | "warning" | "error";

export interface Issue {
  description: string;
  details?: string;
  severity: IssueSeverity;
}

export type TabIssues = Record<string, Issue[]>;

export type IssueReport = Record<string, TabIssues>;

export interface ErrorCheckerContext {
  storedAssetIds: Set<string> | null;
  assetDisplayNames: ReadonlyMap<string, string>;
}

const THEME_IDS = ["default"];
const SURFACE_KINDS: Surface["kind"][] = ["world", "container", "keypad", "door"];
const RIDDLE_DIFFICULTIES = ["easy", "medium", "hard"];
const RESOURCE_KINDS = ["inline_text", "asset"];
const HINT_SEVERITIES = ["orientation", "approach", "solution"];
const INPUT_TYPES = ["collection", "numeric"];
const TIME_LIMIT_MODES = ["hard", "soft"];
const ASSET_MEDIA_TYPES: AssetMediaType[] = ["image/png", "image/jpeg"];

export const SEVERITY_ORDER: Record<IssueSeverity, number> = { info: 0, warning: 1, error: 2 };

const isBlank = (value: unknown) => typeof value !== "string" || value.trim() === "";

export class ErrorChecker {
  private readonly context: ErrorCheckerContext;
  private report: IssueReport = {};

  constructor(context: ErrorCheckerContext = {
    storedAssetIds: new Set(),
    assetDisplayNames: new Map(),
  }) {
    this.context = context;
  }

  check(deerSchema: DeerProject): IssueReport {
    this.report = Object.fromEntries(VALIDATED_TAB_IDS.map((tabId) => [tabId, {}])) as IssueReport;

    this.checkMetadata(deerSchema);
    this.checkScenario(deerSchema);
    this.checkSession(deerSchema);
    this.checkSurfaces(deerSchema);
    this.checkAssets(deerSchema);
    this.checkRiddles(deerSchema);
    this.checkRiddleGraph(deerSchema);
    this.checkSurfaceProfile(deerSchema);

    return this.report;
  }

  static getHighestSeverity(tabIssues: TabIssues | undefined): IssueSeverity | null {
    let highest: IssueSeverity | null = null;
    for (const issues of Object.values(tabIssues ?? {})) {
      for (const issue of issues) {
        if (highest === null || SEVERITY_ORDER[issue.severity] > SEVERITY_ORDER[highest]) {
          highest = issue.severity;
        }
      }
    }
    return highest;
  }

  static getIssues(tabIssues: TabIssues | undefined): Issue[] {
    return Object.values(tabIssues ?? {}).flat();
  }

  /** All issues of every tab, most severe first. */
  static getSortedIssues(report: IssueReport): Issue[] {
    return Object.values(report)
      .flatMap((tabIssues) => ErrorChecker.getIssues(tabIssues))
      .sort((a, b) => SEVERITY_ORDER[b.severity] - SEVERITY_ORDER[a.severity]);
  }

  //#region Helpers
  private add(tabId: ValidatedTabId, field: string, issue: Issue) {
    const tab = (this.report[tabId] ??= {});
    (tab[field] ??= []).push(issue);
  }

  private error(tabId: ValidatedTabId, field: string, description: string, details?: string) {
    this.add(tabId, field, { description, details, severity: "error" });
  }

  private warning(tabId: ValidatedTabId, field: string, description: string, details?: string) {
    this.add(tabId, field, { description, details, severity: "warning" });
  }

  private info(tabId: ValidatedTabId, field: string, description: string, details?: string) {
    this.add(tabId, field, { description, details, severity: "info" });
  }

  /** Reports an error when the given text is empty or only whitespace. */
  private requireText(
    tabId: ValidatedTabId,
    field: string,
    value: string,
    description: string,
    details?: string,
  ) {
    if (isBlank(value)) this.error(tabId, field, description, details);
  }

  /** Reports an error when the value is not one of the allowed select options. */
  private requireOption(
    tabId: ValidatedTabId,
    field: string,
    value: string,
    allowed: readonly string[],
    description: string,
  ) {
    if (!allowed.includes(value)) {
      this.error(tabId, field, description);
    }
  }

  /** Reports an error when a list is empty and for every entry that is blank. */
  private requireTexts(
    tabId: ValidatedTabId,
    field: string,
    values: string[],
    emptyListDescription: string,
    emptyEntryDescription: string,
  ) {
    if (!Array.isArray(values) || values.length === 0) {
      this.error(tabId, field, emptyListDescription);
      return;
    }
    const blankCount = values.filter(isBlank).length;
    if (blankCount > 0) {
      this.error(tabId, field, emptyEntryDescription, `${blankCount} leere(r) Eintrag/Einträge.`);
    }
  }

  private static findDuplicates(ids: string[]): string[] {
    const seen = new Set<string>();
    const duplicates = new Set<string>();
    for (const id of ids) {
      if (seen.has(id)) duplicates.add(id);
      seen.add(id);
    }
    return [...duplicates];
  }
  //#endregion

  //#region Metadata & learning design
  private checkMetadata(deerSchema: DeerProject) {
    const { metadata, learningDesign } = deerSchema;

    this.requireText("metadata", "id", metadata.id, "Das Abenteuer konnte nicht vollständig eingerichtet werden.");
    this.requireText("metadata", "title", metadata.title, "Der Titel darf nicht leer sein.");
    this.requireText("metadata", "locale", metadata.locale, "Es ist keine Sprache gesetzt.");
    if (learningDesign.objectives.length === 0) {
      this.error("metadata", "objectives", "Es muss mindestens ein Lernziel geben.");
    } else {
      const blankCount = learningDesign.objectives.filter((objective) =>
        isBlank(objective.description),
      ).length;
      if (blankCount > 0) {
        this.error(
          "metadata",
          "objectives",
          "Lernziele dürfen nicht leer sein.",
          `${blankCount} leere(s) Lernziel(e).`,
        );
      }
      const duplicates = ErrorChecker.findDuplicates(
        learningDesign.objectives.map((objective) => objective.id),
      );
      if (duplicates.length > 0) {
        this.error("metadata", "objectives", "Einige Lernziele sind doppelt angelegt.");
      }
    }

    const blankDebriefPrompts = learningDesign.debriefPrompts.filter(isBlank).length;
    if (blankDebriefPrompts > 0) {
      this.error(
        "game_end",
        "debriefPrompts",
        "Debrief-Fragen dürfen nicht leer sein.",
        `${blankDebriefPrompts} leere(r) Eintrag/Einträge.`,
      );
    }
    const duplicateDebriefPrompts = ErrorChecker.findDuplicates(learningDesign.debriefPrompts);
    if (duplicateDebriefPrompts.length > 0) {
      this.error(
        "game_end",
        "debriefPrompts",
        "Debrief-Fragen dürfen nicht doppelt vorkommen.",
        duplicateDebriefPrompts.join(", "),
      );
    }
  }
  //#endregion

  //#region Scenario
  private checkScenario(deerSchema: DeerProject) {
    const { scenario } = deerSchema;

    this.requireOption(
      "scenario",
      "themeId",
      scenario.themeId,
      THEME_IDS,
      "Es muss ein gültiges Thema ausgewählt werden.",
    );
    this.requireText("scenario", "mission", scenario.mission, "Die Mission darf nicht leer sein.");

    this.requireTexts(
      "scenario",
      "introText",
      scenario.introText,
      "Es muss mindestens einen Intro-Text geben.",
      "Intro-Texte dürfen nicht leer sein.",
    );
    this.requireTexts(
      "game_end",
      "successText",
      scenario.successText,
      "Es muss mindestens einen Text für den erfolgreichen Abschluss geben.",
      "Texte für den erfolgreichen Abschluss dürfen nicht leer sein.",
    );
    // A hard limit requires failure text; for a soft limit the whole field may be absent.
    if (deerSchema.session.time.limitMode === "hard" || scenario.failureText !== undefined) {
      this.requireTexts(
        "game_end",
        "failureText",
        scenario.failureText ?? [],
        "Es muss mindestens einen Text für den Misserfolg geben.",
        "Texte für den Misserfolg dürfen nicht leer sein.",
      );
    }
  }
  //#endregion

  //#region Session
  private checkSession(deerSchema: DeerProject) {
    const { session } = deerSchema;

    this.requireText(
      "session",
      "targetAudience",
      session.targetAudience,
      "Die Zielgruppe darf nicht leer sein.",
    );
    this.requireText(
      "session",
      "priorKnowledge",
      session.priorKnowledge,
      "Die Vorkenntnisse dürfen nicht leer sein.",
    );

    const { min, max } = session.playerCount;
    if (min < 1) {
      this.error(
        "session",
        "playerCount",
        "Es muss mindestens ein Spieler möglich sein.",
        `Minimum: ${min}.`,
      );
    }
    if (max < min) {
      this.error(
        "session",
        "playerCount",
        "Die maximale Spieleranzahl darf nicht kleiner als die minimale sein.",
        `Minimum: ${min}, Maximum: ${max}.`,
      );
    }

    if (session.time.limitMinutes <= 0) {
      this.error("session", "time", "Das Zeitlimit muss größer als 0 Minuten sein.");
    }
    this.requireOption(
      "session",
      "time",
      session.time.limitMode,
      TIME_LIMIT_MODES,
      "Ungültiger Zeitlimit-Modus.",
    );
  }
  //#endregion

  //#region Surfaces
  private checkSurfaces(deerSchema: DeerProject) {
    const { surfaces } = deerSchema;

    if (surfaces.length === 0) {
      this.error("game_end", "exit", "Das Abenteuer konnte nicht vollständig eingerichtet werden.");
      return;
    }

    const duplicates = ErrorChecker.findDuplicates(surfaces.map((surface) => surface.id));
    if (duplicates.length > 0) {
      this.error("riddles", "riddles", "Einige Fundorte oder Geräte sind nicht eindeutig zugeordnet.");
    }

    const worldCount = surfaces.filter((surface) => surface.kind === "world").length;
    if (worldCount !== 1) {
      this.error(
        "game_end",
        "exit",
        "Das Abenteuer konnte nicht vollständig eingerichtet werden.",
        `Gefunden: ${worldCount}.`,
      );
    }
    const doorCount = surfaces.filter((surface) => surface.kind === "door").length;
    if (doorCount !== 1) {
      this.error(
        "game_end",
        "exit",
        "Es muss genau einen Ausgang geben.",
        `Gefunden: ${doorCount}.`,
      );
    }

    for (const surface of surfaces) {
      const tabId: ValidatedTabId = surface.kind === "door" || surface.kind === "world"
        ? "game_end"
        : "riddles";
      const field = surface.kind === "door" ? "exit" : "riddles";
      const description = surface.kind === "door"
        ? "Der Name des Ausgangs darf nicht leer sein."
        : surface.kind === "container"
          ? "Der Fundort einer Informationsquelle darf nicht leer sein."
          : surface.kind === "keypad"
            ? "Der Name eines Zahlengeräts darf nicht leer sein."
            : "Das Abenteuer konnte nicht vollständig eingerichtet werden.";
      this.requireText(tabId, field, surface.title, description);
      this.requireOption(
        tabId,
        field,
        surface.kind,
        SURFACE_KINDS,
        "Ein Bestandteil des Abenteuers ist ungültig eingerichtet.",
      );
    }
  }
  //#endregion

  //#region Assets
  private checkAssets(deerSchema: DeerProject) {
    const { assets } = deerSchema;

    const duplicates = ErrorChecker.findDuplicates(assets.map((asset) => asset.id));
    if (duplicates.length > 0) {
      this.error("assets", "assets", "Einige Dateien sind doppelt angelegt.");
    }

    const customPaths = new Map<string, Asset>();
    for (const asset of assets) {
      this.checkAsset(asset);
      if (!isBlank(asset.path) && !isBundledAssetPath(asset.path)) {
        const normalizedPath = asset.path.normalize("NFKC").toLowerCase();
        const previous = customPaths.get(normalizedPath);
        if (previous) {
          this.error(
            "assets",
            `asset:${asset.id}`,
            `Die eigenen Dateien "${this.assetName(previous)}" und "${this.assetName(asset)}" verwenden denselben internen Dateinamen.`,
          );
        } else {
          customPaths.set(normalizedPath, asset);
        }
      }
    }
  }

  private assetName(asset: Asset): string {
    return this.context.assetDisplayNames.get(asset.id) ?? "Datei";
  }

  private checkAsset(asset: Asset) {
    const field = `asset:${asset.id}`;
    const name = this.assetName(asset);

    if (isBlank(asset.path)) {
      this.error("assets", field, `Die Datei "${name}" hat keinen gültigen Speicherort.`);
    } else if (isBundledAssetPath(asset.path)) {
      if (!getBundledAssetPaths().has(asset.path)) {
        this.error(
          "assets",
          field,
          `Die mitgelieferte Datei "${name}" existiert nicht.`,
          "Die Datei ist nicht mehr in der mitgelieferten Auswahl verfügbar.",
        );
      }
    } else if (this.context.storedAssetIds !== null && !this.context.storedAssetIds.has(asset.id)) {
      this.error(
        "assets",
        field,
        `Der Inhalt der Datei "${name}" wurde nicht gefunden.`,
        "Die hochgeladene Datei ist nicht mehr im lokalen Speicher vorhanden.",
      );
    }

    this.requireOption(
      "assets",
      field,
      asset.mediaType,
      ASSET_MEDIA_TYPES,
      `Die Datei "${name}" hat kein unterstütztes Format.`,
    );

    if (isBlank(asset.source?.license)) {
      this.error(
        "assets",
        field,
        `Für die Datei "${name}" ist keine Lizenz angegeben.`,
      );
    }
  }
  //#endregion

  //#region Surface profile
  private checkSurfaceProfile(deerSchema: DeerProject) {
    const surfacesById = new Map(deerSchema.surfaces.map((surface) => [surface.id, surface]));
    const containerUses = new Map<string, number>();
    const keypadUses = new Map<string, number>();
    const count = (uses: Map<string, number>, id: string) => uses.set(id, (uses.get(id) ?? 0) + 1);

    for (const riddle of deerSchema.riddles) {
      const field = `riddle:${riddle.id}`;
      const name = riddle.title.trim() || "Unbenanntes Rätsel";
      for (const source of riddle.informationSources) {
        count(containerUses, source.surfaceId);
        const surface = surfacesById.get(source.surfaceId);
        if (surface && surface.kind !== "container") {
          this.error(
            "riddles",
            field,
            `Der Fundort einer Informationsquelle von "${name}" ist ungültig eingerichtet.`,
          );
        }
      }
      for (const input of riddle.inputs) {
        if (input.type !== "numeric") continue;
        count(keypadUses, input.surfaceId);
        const surface = surfacesById.get(input.surfaceId);
        if (surface && surface.kind !== "keypad") {
          this.error(
            "riddles",
            field,
            `Das Zahlengerät von "${name}" ist ungültig eingerichtet.`,
          );
        }
      }
    }

    for (const node of deerSchema.riddleGraph.nodes) {
      if (node.kind !== "end") continue;
      const surface = surfacesById.get(node.surfaceId);
      if (surface && surface.kind !== "door") {
        this.error(
          "game_end",
          "exit",
          "Der Ausgang ist ungültig eingerichtet.",
        );
      }
    }

    for (const surface of deerSchema.surfaces) {
      const uses =
        surface.kind === "container"
          ? containerUses.get(surface.id)
          : surface.kind === "keypad"
            ? keypadUses.get(surface.id)
            : undefined;
      if ((surface.kind === "container" || surface.kind === "keypad") && uses !== 1) {
        this.error(
          "riddles",
          "riddles",
          surface.kind === "container"
            ? `Der Fundort "${surface.title.trim() || "Unbenannter Fundort"}" muss genau einer Informationsquelle gehören.`
            : `Das Gerät "${surface.title.trim() || "Unbenanntes Gerät"}" muss genau einer Zahleneingabe gehören.`,
          `Gefundene Verwendungen: ${uses ?? 0}.`,
        );
      }
    }
  }
  //#endregion

  //#region Riddles
  private checkRiddles(deerSchema: DeerProject) {
    const { riddles } = deerSchema;

    if (riddles.length === 0) {
      this.error("riddles", "riddles", "Es muss mindestens ein Rätsel geben.");
      return;
    }

    const duplicates = ErrorChecker.findDuplicates(riddles.map((riddle) => riddle.id));
    if (duplicates.length > 0) {
      this.error("riddles", "riddles", "Einige Rätsel sind doppelt angelegt.");
    }

    const surfaceIds = new Set(deerSchema.surfaces.map((surface) => surface.id));
    const assetIds = new Set(deerSchema.assets.map((asset) => asset.id));
    const objectiveIds = new Set(deerSchema.learningDesign.objectives.map((objective) => objective.id));

    for (const riddle of riddles) {
      this.checkRiddle(riddle, surfaceIds, assetIds, objectiveIds);
    }
  }

  private checkRiddle(
    riddle: Riddle,
    surfaceIds: Set<string>,
    assetIds: Set<string>,
    objectiveIds: Set<string>,
  ) {
    const field = `riddle:${riddle.id}`;
    const name = riddle.title.trim() || "Unbenanntes Rätsel";

    this.requireText("riddles", field, riddle.title, "Der Titel des Rätsels darf nicht leer sein.");
    this.requireOption(
      "riddles",
      field,
      riddle.difficulty,
      RIDDLE_DIFFICULTIES,
      `Das Rätsel "${name}" hat keinen gültigen Schwierigkeitsgrad.`,
    );

    if (riddle.estimatedMinutes <= 0) {
      this.error("riddles", field, `Die geschätzte Dauer von "${name}" muss größer als 0 Minuten sein.`);
    }

    const unknownObjectives = riddle.learningObjectiveIds.filter((id) => !objectiveIds.has(id));
    if (unknownObjectives.length > 0) {
      this.error(
        "riddles",
        field,
        `Das Rätsel "${name}" verweist auf unbekannte Lernziele.`,
      );
    }
    if (riddle.learningObjectiveIds.length === 0) {
      this.error("riddles", field, `Das Rätsel "${name}" ist keinem Lernziel zugeordnet.`);
    }

    if (riddle.hints.length === 0) {
      this.warning(
        "riddles",
        field,
        `Das Rätsel "${name}" hat keine Hinweise.`,
        "Hinweise helfen den Spielern, wenn sie nicht weiterkommen.",
      );
    }
    for (const hint of riddle.hints) {
      this.requireText("riddles", field, hint.title, `Ein Hinweis von "${name}" hat keinen Titel.`);
      this.requireText("riddles", field, hint.text, `Ein Hinweis von "${name}" hat keinen Text.`);
      this.requireOption(
        "riddles",
        field,
        hint.severity,
        HINT_SEVERITIES,
        `Ein Hinweis von "${name}" hat keine gültige Stufe.`,
      );
    }

    this.checkInformationSources(riddle, field, name, surfaceIds, assetIds);
    this.checkInputs(riddle, field, name, surfaceIds);
  }

  private checkInformationSources(
    riddle: Riddle,
    field: string,
    name: string,
    surfaceIds: Set<string>,
    assetIds: Set<string>,
  ) {
    const duplicates = ErrorChecker.findDuplicates(riddle.informationSources.map((source) => source.id));
    if (duplicates.length > 0) {
      this.error(
        "riddles",
        field,
        `Das Rätsel "${name}" hat doppelt angelegte Informationsquellen.`,
      );
    }

    for (const source of riddle.informationSources) {
      this.checkSurfaceReference("riddles", field, source.surfaceId, surfaceIds, name);

      if (source.resources.length === 0) {
        this.error(
          "riddles",
          field,
          `Eine Informationsquelle von "${name}" enthält kein Material.`,
          "Jede Informationsquelle braucht mindestens ein Material.",
        );
      }

      for (const resource of source.resources) {
        this.requireText("riddles", field, resource.title, `Ein Material von "${name}" hat keinen Titel.`);
        this.requireOption(
          "riddles",
          field,
          resource.kind,
          RESOURCE_KINDS,
          `Ein Material von "${name}" hat keine gültige Art.`,
        );

        if (resource.kind === "inline_text") {
          this.requireText(
            "riddles",
            field,
            resource.text,
            `Das Material "${resource.title || "Unbenanntes Material"}" hat keinen Text.`,
          );
        } else if (resource.kind === "asset") {
          if (isBlank(resource.assetId)) {
            this.error(
              "riddles",
              field,
              `Für das Material "${resource.title || "Unbenanntes Material"}" ist keine Datei ausgewählt.`,
            );
          } else if (!assetIds.has(resource.assetId)) {
            this.error(
              "riddles",
              field,
              `Die Datei für das Material "${resource.title || "Unbenanntes Material"}" ist nicht mehr vorhanden.`,
            );
          }
        }
      }
    }
  }

  private checkInputs(riddle: Riddle, field: string, name: string, surfaceIds: Set<string>) {
    if (riddle.inputs.length === 0) {
      this.error("riddles", field, `Das Rätsel "${name}" hat keine Eingabe.`);
      return;
    }

    const duplicates = ErrorChecker.findDuplicates(riddle.inputs.map((input) => input.id));
    if (duplicates.length > 0) {
      this.error(
        "riddles",
        field,
        `Das Rätsel "${name}" hat doppelt angelegte Eingaben.`,
      );
    }

    const sourceIds = new Set(riddle.informationSources.map((source) => source.id));

    for (const input of riddle.inputs) {
      this.requireOption(
        "riddles",
        field,
        input.type,
        INPUT_TYPES,
        `Eine Eingabe von "${name}" hat keine gültige Art.`,
      );

      if (input.type === "collection") {
        if (isBlank(input.informationSourceId)) {
          this.error(
            "riddles",
            field,
            `Für eine Eingabe von "${name}" ist keine Informationsquelle gewählt.`,
          );
        } else if (!sourceIds.has(input.informationSourceId)) {
          this.error(
            "riddles",
            field,
            `Die gewählte Informationsquelle für "${name}" ist nicht mehr vorhanden.`,
          );
        }
      } else if (input.type === "numeric") {
        this.checkSurfaceReference("riddles", field, input.surfaceId, surfaceIds, name);

        if (isBlank(input.answer)) {
          this.error("riddles", field, `Für das Rätsel "${name}" ist keine Lösung hinterlegt.`);
        } else if (!/^\d{1,8}$/.test(input.answer)) {
          this.error(
            "riddles",
            field,
            `Die Lösung von "${name}" muss aus 1 bis 8 Ziffern bestehen.`,
            `Aktuelle Lösung: "${input.answer}".`,
          );
        }
      }
    }
  }

  private checkSurfaceReference(
    tabId: ValidatedTabId,
    field: string,
    surfaceId: string,
    surfaceIds: Set<string>,
    riddleName: string,
  ) {
    if (isBlank(surfaceId)) {
      this.error(tabId, field, `Für das Rätsel "${riddleName}" fehlt ein Fundort oder Gerät.`);
    } else if (!surfaceIds.has(surfaceId)) {
      this.error(
        tabId,
        field,
        `Ein Fundort oder Gerät von "${riddleName}" ist nicht mehr vorhanden.`,
      );
    }
  }
  //#endregion

  //#region Riddle graph
  private checkRiddleGraph(deerSchema: DeerProject) {
    const graph = deerSchema.riddleGraph;
    const nodeIds = new Set(graph.nodes.map((node) => node.id));
    const riddleIds = new Set(deerSchema.riddles.map((riddle) => riddle.id));
    const surfaceIds = new Set(deerSchema.surfaces.map((surface) => surface.id));
    const riddleNames = new Map(deerSchema.riddles.map((riddle) => [
      riddle.id,
      riddle.title.trim() || "Unbenanntes Rätsel",
    ]));
    const nodeNames = new Map(graph.nodes.map((node) => [
      node.id,
      node.kind === "start"
        ? "Start"
        : node.kind === "end"
          ? "Ende"
          : (riddleNames.get(node.riddleId) ?? "Schritt"),
    ]));
    const displayNodes = (ids: string[]) =>
      ids.map((id) => nodeNames.get(id) ?? "Unbekannter Schritt").join(", ");
    const displayEdge = (from: string, to: string) =>
      `${nodeNames.get(from) ?? "Unbekannter Schritt"} → ${nodeNames.get(to) ?? "Unbekannter Schritt"}`;

    const duplicates = ErrorChecker.findDuplicates(graph.nodes.map((node) => node.id));
    if (duplicates.length > 0) {
      this.error(
        "riddle_graph",
        "nodes",
        "Es gibt mehrfach angelegte Schritte.",
        displayNodes(duplicates),
      );
    }

    const startNodes = graph.nodes.filter((node) => node.kind === "start");
    const endNodes = graph.nodes.filter((node) => node.kind === "end");
    if (startNodes.length !== 1) {
      this.error(
        "riddle_graph",
        "nodes",
        "Es muss genau einen Startpunkt im Spielablauf geben.",
        `Gefunden: ${startNodes.length}.`,
      );
    }
    if (endNodes.length !== 1) {
      this.error(
        "riddle_graph",
        "nodes",
        "Es muss genau einen Endpunkt im Spielablauf geben.",
        `Gefunden: ${endNodes.length}.`,
      );
    }

    for (const node of graph.nodes) {
      if (node.kind === "riddle" && !riddleIds.has(node.riddleId)) {
        this.error(
          "riddle_graph",
          "nodes",
          "Ein Schritt verweist auf ein unbekanntes Rätsel.",
        );
      }
      if (node.kind === "end" && !surfaceIds.has(node.surfaceId)) {
        this.error(
          "game_end",
          "exit",
          "Der Ausgang ist nicht mehr vorhanden.",
        );
      }
    }

    const usedRiddleIds = new Set(
      graph.nodes.filter((node) => node.kind === "riddle").map((node) => node.riddleId),
    );
    const duplicateRiddleNodes = ErrorChecker.findDuplicates(
      graph.nodes.filter((node) => node.kind === "riddle").map((node) => node.riddleId),
    );
    if (duplicateRiddleNodes.length > 0) {
      this.error(
        "riddle_graph",
        "nodes",
        "Jedes Rätsel darf genau einmal im Spielablauf vorkommen.",
        duplicateRiddleNodes.map((id) => riddleNames.get(id) ?? "Unbenanntes Rätsel").join(", "),
      );
    }
    const unusedRiddles = [...riddleIds].filter((id) => !usedRiddleIds.has(id));
    if (unusedRiddles.length > 0) {
      this.error(
        "riddle_graph",
        "nodes",
        "Es gibt Rätsel, die im Spielablauf nicht vorkommen.",
        unusedRiddles.map((id) => riddleNames.get(id) ?? "Unbenanntes Rätsel").join(", "),
      );
    }

    for (const edge of graph.edges) {
      if (!nodeIds.has(edge.from) || !nodeIds.has(edge.to)) {
        this.error(
          "riddle_graph",
          "edges",
          "Eine Verbindung verweist auf einen unbekannten Schritt.",
          displayEdge(edge.from, edge.to),
        );
      } else if (edge.from === edge.to) {
        this.error(
          "riddle_graph",
          "edges",
          "Eine Verbindung zeigt auf sich selbst.",
          nodeNames.get(edge.from) ?? "Schritt",
        );
      }
    }

    const duplicateEdges = ErrorChecker.findDuplicates(
      graph.edges.map((edge) => `${edge.from}->${edge.to}`),
    );
    if (duplicateEdges.length > 0) {
      this.error(
        "riddle_graph",
        "edges",
        "Eine Verbindung darf nicht doppelt vorkommen.",
        duplicateEdges.map((key) => {
          const edge = graph.edges.find((candidate) => `${candidate.from}->${candidate.to}` === key);
          return edge ? displayEdge(edge.from, edge.to) : "Verbindung";
        }).join(", "),
      );
    }
    if (ErrorChecker.hasDirectedCycle(graph, nodeIds)) {
      this.error(
        "riddle_graph",
        "edges",
        "Der Spielablauf enthält einen Kreis.",
        "Entferne mindestens eine Verbindung aus dem Kreis.",
      );
    }

    this.checkGraphReachability(graph, nodeIds, nodeNames);
  }

  private checkGraphReachability(
    graph: RiddleGraph,
    nodeIds: Set<string>,
    nodeNames: ReadonlyMap<string, string>,
  ) {
    const startNodeId = graph.nodes.find((node) => node.kind === "start")?.id;
    const endNodeId = graph.nodes.find((node) => node.kind === "end")?.id;
    if (startNodeId === undefined) return;

    const outgoing = new Map<string, string[]>();
    const incoming = new Map<string, string[]>();
    for (const edge of graph.edges) {
      if (!nodeIds.has(edge.from) || !nodeIds.has(edge.to)) continue;
      outgoing.set(edge.from, [...(outgoing.get(edge.from) ?? []), edge.to]);
      incoming.set(edge.to, [...(incoming.get(edge.to) ?? []), edge.from]);
    }

    const reachable = ErrorChecker.collectReachable(startNodeId, outgoing);

    if (endNodeId !== undefined && !reachable.has(endNodeId)) {
      this.error(
        "riddle_graph",
        "edges",
        "Der Endpunkt ist vom Start aus nicht erreichbar.",
        "Es fehlt eine Verbindung zwischen Start und Ende.",
      );
    }

    const unreachable = [...nodeIds].filter((id) => !reachable.has(id) && id !== endNodeId);
    if (unreachable.length > 0) {
      this.error(
        "riddle_graph",
        "nodes",
        "Es gibt Schritte, die vom Start aus nicht erreichbar sind.",
        unreachable.map((id) => nodeNames.get(id) ?? "Unbekannter Schritt").join(", "),
      );
    }

    if (endNodeId === undefined) return;

    // Steps that no longer lead to the exit leave the adventure in a dead end.
    const leadingToEnd = ErrorChecker.collectReachable(endNodeId, incoming);
    const deadEnds = [...nodeIds].filter((id) => reachable.has(id) && !leadingToEnd.has(id));
    if (deadEnds.length > 0) {
      this.error(
        "riddle_graph",
        "edges",
        "Es gibt Schritte, von denen aus der Endpunkt nicht erreichbar ist.",
        deadEnds.map((id) => nodeNames.get(id) ?? "Unbekannter Schritt").join(", "),
      );
    }
  }

  private static hasDirectedCycle(graph: RiddleGraph, nodeIds: Set<string>): boolean {
    const incomingCount = new Map([...nodeIds].map((id) => [id, 0]));
    const outgoing = new Map<string, string[]>();
    for (const edge of graph.edges) {
      if (!nodeIds.has(edge.from) || !nodeIds.has(edge.to)) continue;
      outgoing.set(edge.from, [...(outgoing.get(edge.from) ?? []), edge.to]);
      incomingCount.set(edge.to, (incomingCount.get(edge.to) ?? 0) + 1);
    }
    const queue = [...nodeIds].filter((id) => incomingCount.get(id) === 0);
    let visited = 0;
    while (queue.length > 0) {
      const current = queue.shift() as string;
      visited += 1;
      for (const next of outgoing.get(current) ?? []) {
        const remaining = (incomingCount.get(next) ?? 0) - 1;
        incomingCount.set(next, remaining);
        if (remaining === 0) queue.push(next);
      }
    }
    return visited !== nodeIds.size;
  }

  /** Breadth-first traversal of the given adjacency map, starting at `startId`. */
  private static collectReachable(startId: string, adjacency: Map<string, string[]>) {
    const reached = new Set<string>();
    const queue = [startId];
    while (queue.length > 0) {
      const current = queue.shift() as string;
      if (reached.has(current)) continue;
      reached.add(current);
      queue.push(...(adjacency.get(current) ?? []));
    }
    return reached;
  }
  //#endregion
}
