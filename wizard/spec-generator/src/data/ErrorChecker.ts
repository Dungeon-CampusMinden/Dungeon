import type { AnyRiddle, Asset, AssetMediaType, DeerSchema, RiddleGraph, Surface } from "./DeerSchema";
import { getBundledAssetPaths, isBundledAssetPath } from "@/components/assets/assetPaths";

export type IssueSeverity = "info" | "warning" | "error";

export interface Issue {
  description: string;
  details?: string;
  severity: IssueSeverity;
}

export type TabIssues = Record<string, Issue[]>;

export type IssueReport = Record<string, TabIssues>;

export const VALIDATED_TAB_IDS = [
  "metadata",
  "scenario",
  "session",
  "surfaces",
  "assets",
  "riddles",
  "riddle_graph",
] as const;

export type ValidatedTabId = (typeof VALIDATED_TAB_IDS)[number];

export interface ErrorCheckerContext {
  storedAssetIds: Set<string>;
}

const THEME_IDS = ["default"];
const SURFACE_KINDS: Surface["kind"][] = ["world", "container", "keypad", "door"];
const RIDDLE_TYPES: AnyRiddle["type"][] = ["collection", "input"];
const RIDDLE_DIFFICULTIES = ["easy", "medium", "hard"];
const RESOURCE_KINDS = ["inline_text", "asset"];
const RESOURCE_AVAILABILITIES = ["visible_in_level", "inside_container"];
const RESOURCE_PURPOSES = ["clue", "context", "instruction", "decoy"];
const COLLECTION_SOURCE_KINDS = ["container", "world_object"];
const ASSET_MEDIA_TYPES: AssetMediaType[] = [
  "image/png",
  "image/jpeg",
  "text/plain",
  "audio/wav",
  "font/ttf",
];

const SEVERITY_ORDER: Record<IssueSeverity, number> = { info: 0, warning: 1, error: 2 };

const isBlank = (value: unknown) => typeof value !== "string" || value.trim() === "";

export class ErrorChecker {
  private readonly context: ErrorCheckerContext;
  private report: IssueReport = {};

  constructor(context: ErrorCheckerContext = { storedAssetIds: new Set() }) {
    this.context = context;
  }

  check(deerSchema: DeerSchema): IssueReport {
    this.report = Object.fromEntries(VALIDATED_TAB_IDS.map((tabId) => [tabId, {}])) as IssueReport;

    this.checkMetadata(deerSchema);
    this.checkScenario(deerSchema);
    this.checkSession(deerSchema);
    this.checkSurfaces(deerSchema);
    this.checkAssets(deerSchema);
    this.checkRiddles(deerSchema);
    this.checkRiddleGraph(deerSchema);

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
      this.error(tabId, field, description, `Ungültiger Wert: "${value}".`);
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
  private checkMetadata(deerSchema: DeerSchema) {
    const { metadata, learningDesign } = deerSchema;

    this.requireText("metadata", "id", metadata.id, "Das Abenteuer hat keine Id.");
    this.requireText("metadata", "title", metadata.title, "Der Titel darf nicht leer sein.");
    this.requireText("metadata", "locale", metadata.locale, "Es ist keine Sprache gesetzt.");
    this.requireText(
      "metadata",
      "description",
      metadata.description,
      "Die Beschreibung darf nicht leer sein.",
    );
    this.requireText("metadata", "author", metadata.author, "Der Autor darf nicht leer sein.");

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
        this.error("metadata", "objectives", "Lernziele haben doppelte Ids.", duplicates.join(", "));
      }
    }

    this.requireTexts(
      "metadata",
      "debriefPrompts",
      learningDesign.debriefPrompts,
      "Es muss mindestens eine Debrief-Frage geben.",
      "Debrief-Fragen dürfen nicht leer sein.",
    );
  }
  //#endregion

  //#region Scenario
  private checkScenario(deerSchema: DeerSchema) {
    const { scenario } = deerSchema;

    this.requireOption(
      "scenario",
      "themeId",
      scenario.themeId,
      THEME_IDS,
      "Es muss ein gültiges Thema ausgewählt werden.",
    );
    this.requireText("scenario", "playerRole", scenario.playerRole, "Die Spielerrolle darf nicht leer sein.");
    this.requireText("scenario", "premise", scenario.premise, "Die Prämisse darf nicht leer sein.");
    this.requireText("scenario", "mission", scenario.mission, "Die Mission darf nicht leer sein.");

    this.requireTexts(
      "scenario",
      "introTexts",
      scenario.introTexts,
      "Es muss mindestens einen Intro-Text geben.",
      "Intro-Texte dürfen nicht leer sein.",
    );
    this.requireTexts(
      "scenario",
      "successTexts",
      scenario.successTexts,
      "Es muss mindestens einen Text für den erfolgreichen Abschluss geben.",
      "Texte für den erfolgreichen Abschluss dürfen nicht leer sein.",
    );
    this.requireTexts(
      "scenario",
      "failureTexts",
      scenario.failureTexts,
      "Es muss mindestens einen Text für den Misserfolg geben.",
      "Texte für den Misserfolg dürfen nicht leer sein.",
    );
  }
  //#endregion

  //#region Session
  private checkSession(deerSchema: DeerSchema) {
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
    this.requireOption("session", "time", session.time.limitMode, ["hard"], "Ungültiger Zeitlimit-Modus.");
  }
  //#endregion

  //#region Surfaces
  private checkSurfaces(deerSchema: DeerSchema) {
    const { surfaces } = deerSchema;

    if (surfaces.length === 0) {
      this.error("surfaces", "surfaces", "Es muss mindestens einen Ort geben.");
      return;
    }

    const duplicates = ErrorChecker.findDuplicates(surfaces.map((surface) => surface.id));
    if (duplicates.length > 0) {
      this.error("surfaces", "surfaces", "Es gibt Orte mit doppelten Ids.", duplicates.join(", "));
    }

    for (const surface of surfaces) {
      const field = `surface:${surface.id}`;
      this.requireText("surfaces", field, surface.title, "Der Name des Ortes darf nicht leer sein.");
      this.requireOption(
        "surfaces",
        field,
        surface.kind,
        SURFACE_KINDS,
        `Der Ort "${surface.title}" hat keine gültige Art.`,
      );
    }
  }
  //#endregion

  //#region Assets
  private checkAssets(deerSchema: DeerSchema) {
    const { assets } = deerSchema;

    const duplicates = ErrorChecker.findDuplicates(assets.map((asset) => asset.id));
    if (duplicates.length > 0) {
      this.error("assets", "assets", "Es gibt Dateien mit doppelten Ids.", duplicates.join(", "));
    }

    for (const asset of assets) {
      this.checkAsset(asset);
    }
  }

  private checkAsset(asset: Asset) {
    const field = `asset:${asset.id}`;
    const name = asset.path || asset.id;

    if (isBlank(asset.path)) {
      this.error("assets", field, `Die Datei "${asset.id}" hat keinen Pfad.`);
    } else if (isBundledAssetPath(asset.path)) {
      if (!getBundledAssetPaths().has(asset.path)) {
        this.error(
          "assets",
          field,
          `Die mitgelieferte Datei "${name}" existiert nicht.`,
          `Unbekannter Pfad: "${asset.path}".`,
        );
      }
    } else if (!this.context.storedAssetIds.has(asset.id)) {
      this.error(
        "assets",
        field,
        `Der Inhalt der Datei "${name}" wurde nicht gefunden.`,
        "Die hochgeladene Datei ist nicht mehr im Browser-Speicher vorhanden.",
      );
    }

    this.requireOption(
      "assets",
      field,
      asset.mediaType,
      ASSET_MEDIA_TYPES,
      `Die Datei "${name}" hat kein unterstütztes Format.`,
    );

    // bundled assets bring their own license, only uploaded files need one from the user.
    if (!isBundledAssetPath(asset.path) && isBlank(asset.source?.license)) {
      this.warning(
        "assets",
        field,
        `Für die Datei "${name}" ist keine Lizenz angegeben.`,
        "Eigene Dateien sollten eine Lizenz besitzen.",
      );
    }
  }
  //#endregion

  //#region Riddles
  private checkRiddles(deerSchema: DeerSchema) {
    const { riddles } = deerSchema;

    if (riddles.length === 0) {
      this.error("riddles", "riddles", "Es muss mindestens ein Rätsel geben.");
      return;
    }

    const duplicates = ErrorChecker.findDuplicates(riddles.map((riddle) => riddle.id));
    if (duplicates.length > 0) {
      this.error("riddles", "riddles", "Es gibt Rätsel mit doppelten Ids.", duplicates.join(", "));
    }

    const surfaceIds = new Set(deerSchema.surfaces.map((surface) => surface.id));
    const assetIds = new Set(deerSchema.assets.map((asset) => asset.id));
    const objectiveIds = new Set(deerSchema.learningDesign.objectives.map((objective) => objective.id));

    for (const riddle of riddles) {
      this.checkRiddle(riddle, surfaceIds, assetIds, objectiveIds);
    }
  }

  private checkRiddle(
    riddle: AnyRiddle,
    surfaceIds: Set<string>,
    assetIds: Set<string>,
    objectiveIds: Set<string>,
  ) {
    const field = `riddle:${riddle.id}`;
    const name = riddle.title || riddle.id;

    this.requireText("riddles", field, riddle.title, "Der Titel des Rätsels darf nicht leer sein.");
    this.requireOption(
      "riddles",
      field,
      riddle.type,
      RIDDLE_TYPES,
      `Das Rätsel "${name}" hat keine gültige Art.`,
    );
    this.requireOption(
      "riddles",
      field,
      riddle.difficulty,
      RIDDLE_DIFFICULTIES,
      `Das Rätsel "${name}" hat keinen gültigen Schwierigkeitsgrad.`,
    );
    this.requireText(
      "riddles",
      field,
      riddle.playerFacingTask,
      `Die Aufgabenstellung von "${name}" darf nicht leer sein.`,
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
        unknownObjectives.join(", "),
      );
    }
    if (riddle.learningObjectiveIds.length === 0) {
      this.warning("riddles", field, `Das Rätsel "${name}" ist keinem Lernziel zugeordnet.`);
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
    }

    for (const resource of riddle.resources) {
      this.requireText("riddles", field, resource.title, `Ein Material von "${name}" hat keinen Titel.`);
      this.requireOption(
        "riddles",
        field,
        resource.kind,
        RESOURCE_KINDS,
        `Ein Material von "${name}" hat keine gültige Art.`,
      );
      this.requireOption(
        "riddles",
        field,
        resource.availability,
        RESOURCE_AVAILABILITIES,
        `Ein Material von "${name}" hat keine gültige Verfügbarkeit.`,
      );
      this.requireOption(
        "riddles",
        field,
        resource.purpose,
        RESOURCE_PURPOSES,
        `Ein Material von "${name}" hat keinen gültigen Zweck.`,
      );

      if (resource.kind === "inline_text") {
        this.requireText(
          "riddles",
          field,
          resource.text,
          `Das Material "${resource.title || resource.id}" hat keinen Text.`,
        );
      } else if (resource.kind === "asset") {
        if (isBlank(resource.assetId)) {
          this.error(
            "riddles",
            field,
            `Für das Material "${resource.title || resource.id}" ist keine Datei ausgewählt.`,
          );
        } else if (!assetIds.has(resource.assetId)) {
          this.error(
            "riddles",
            field,
            `Das Material "${resource.title || resource.id}" verweist auf eine unbekannte Datei.`,
            `Unbekannte Datei-Id: "${resource.assetId}".`,
          );
        }
      }
    }

    if (riddle.type === "collection") {
      const { parameters } = riddle;
      this.checkSurfaceReference("riddles", field, parameters.surfaceId, surfaceIds, name);
      this.requireOption(
        "riddles",
        field,
        parameters.sourceKind,
        COLLECTION_SOURCE_KINDS,
        `Das Rätsel "${name}" hat keine gültige Fundquelle.`,
      );
      this.requireOption(
        "riddles",
        field,
        parameters.rewardMode,
        ["find_resource"],
        `Das Rätsel "${name}" hat keine gültige Belohnungsart.`,
      );

      if (parameters.resourceIds.length === 0) {
        this.error("riddles", field, `Das Rätsel "${name}" muss mindestens ein Material zum Finden haben.`);
      }
      const resourceIds = new Set(riddle.resources.map((resource) => resource.id));
      const unknownResources = parameters.resourceIds.filter((id) => !resourceIds.has(id));
      if (unknownResources.length > 0) {
        this.error(
          "riddles",
          field,
          `Das Rätsel "${name}" verweist auf unbekannte Materialien.`,
          unknownResources.join(", "),
        );
      }
    } else if (riddle.type === "input") {
      const { parameters } = riddle;
      this.checkSurfaceReference("riddles", field, parameters.surfaceId, surfaceIds, name);
      this.requireOption(
        "riddles",
        field,
        parameters.inputMode,
        ["numeric"],
        `Das Rätsel "${name}" hat keine gültige Eingabeart.`,
      );

      if (isBlank(parameters.answer)) {
        this.error("riddles", field, `Für das Rätsel "${name}" ist keine Lösung hinterlegt.`);
      } else if (parameters.inputMode === "numeric" && !/^\d+$/.test(parameters.answer)) {
        this.error(
          "riddles",
          field,
          `Die Lösung von "${name}" darf nur Ziffern enthalten.`,
          `Aktuelle Lösung: "${parameters.answer}".`,
        );
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
      this.error(tabId, field, `Für das Rätsel "${riddleName}" ist kein Ort ausgewählt.`);
    } else if (!surfaceIds.has(surfaceId)) {
      this.error(
        tabId,
        field,
        `Das Rätsel "${riddleName}" verweist auf einen unbekannten Ort.`,
        `Unbekannte Ort-Id: "${surfaceId}".`,
      );
    }
  }
  //#endregion

  //#region Riddle graph
  private checkRiddleGraph(deerSchema: DeerSchema) {
    const graph = deerSchema.riddleGraph;
    const nodeIds = new Set(graph.nodes.map((node) => node.id));
    const riddleIds = new Set(deerSchema.riddles.map((riddle) => riddle.id));
    const surfaceIds = new Set(deerSchema.surfaces.map((surface) => surface.id));

    const duplicates = ErrorChecker.findDuplicates(graph.nodes.map((node) => node.id));
    if (duplicates.length > 0) {
      this.error("riddle_graph", "nodes", "Es gibt Schritte mit doppelten Ids.", duplicates.join(", "));
    }

    if (!nodeIds.has(graph.startNodeId)) {
      this.error("riddle_graph", "startNodeId", "Der Startpunkt des Spielablaufs fehlt.");
    }
    if (!nodeIds.has(graph.endNodeId)) {
      this.error("riddle_graph", "endNodeId", "Der Endpunkt des Spielablaufs fehlt.");
    }

    for (const node of graph.nodes) {
      if (node.kind === "riddle" && !riddleIds.has(node.riddleId)) {
        this.error(
          "riddle_graph",
          "nodes",
          "Ein Schritt verweist auf ein unbekanntes Rätsel.",
          `Unbekannte Rätsel-Id: "${node.riddleId}".`,
        );
      }
      if (node.kind === "end" && !surfaceIds.has(node.surfaceId)) {
        this.error(
          "riddle_graph",
          "nodes",
          "Der Endpunkt verweist auf einen unbekannten Ort.",
          `Unbekannte Ort-Id: "${node.surfaceId}".`,
        );
      }
    }

    const usedRiddleIds = new Set(
      graph.nodes.filter((node) => node.kind === "riddle").map((node) => node.riddleId),
    );
    const unusedRiddles = [...riddleIds].filter((id) => !usedRiddleIds.has(id));
    if (unusedRiddles.length > 0) {
      this.warning(
        "riddle_graph",
        "nodes",
        "Es gibt Rätsel, die im Spielablauf nicht vorkommen.",
        unusedRiddles.join(", "),
      );
    }

    for (const edge of graph.edges) {
      if (!nodeIds.has(edge.from) || !nodeIds.has(edge.to)) {
        this.error(
          "riddle_graph",
          "edges",
          "Eine Verbindung verweist auf einen unbekannten Schritt.",
          `${edge.from} → ${edge.to}`,
        );
      } else if (edge.from === edge.to) {
        this.error("riddle_graph", "edges", "Eine Verbindung zeigt auf sich selbst.", edge.from);
      }
    }

    this.checkGraphReachability(graph, nodeIds);
  }

  private checkGraphReachability(graph: RiddleGraph, nodeIds: Set<string>) {
    if (!nodeIds.has(graph.startNodeId)) return;

    const outgoing = new Map<string, string[]>();
    for (const edge of graph.edges) {
      if (!nodeIds.has(edge.from) || !nodeIds.has(edge.to)) continue;
      outgoing.set(edge.from, [...(outgoing.get(edge.from) ?? []), edge.to]);
    }

    const reachable = new Set<string>();
    const queue = [graph.startNodeId];
    while (queue.length > 0) {
      const current = queue.shift() as string;
      if (reachable.has(current)) continue;
      reachable.add(current);
      queue.push(...(outgoing.get(current) ?? []));
    }

    if (nodeIds.has(graph.endNodeId) && !reachable.has(graph.endNodeId)) {
      this.error(
        "riddle_graph",
        "edges",
        "Der Endpunkt ist vom Start aus nicht erreichbar.",
        "Es fehlt eine Verbindung zwischen Start und Ende.",
      );
    }

    const unreachable = [...nodeIds].filter((id) => !reachable.has(id) && id !== graph.endNodeId);
    if (unreachable.length > 0) {
      this.warning(
        "riddle_graph",
        "nodes",
        "Es gibt Schritte, die vom Start aus nicht erreichbar sind.",
        unreachable.join(", "),
      );
    }
  }
  //#endregion
}
