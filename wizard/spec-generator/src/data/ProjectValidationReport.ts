import type { WizardDraft } from "./WizardDraft";
import type { Issue, IssueReport } from "./ErrorChecker";
import { VALIDATED_TAB_IDS, type ValidatedTabId } from "./Tabs";

export type ProductionIssueSeverity = "error" | "warning";

export interface ProductionValidationIssue {
  severity: ProductionIssueSeverity;
  messageKey: KnownValidationMessageKey;
  path: string;
  entity: { kind: string; id: string } | null;
  relatedPaths: string[];
}

export interface ProjectValidationReport {
  valid: boolean;
  issues: ProductionValidationIssue[];
}

const ISSUE_CODES = new Set([
  "ASSET_CONTENT_MISMATCH", "ASSET_DECLARED_UNUSED", "ASSET_FILE_UNREFERENCED",
  "ASSET_HASH_MISMATCH", "ASSET_MISSING", "ASSET_PATH_DUPLICATE", "ASSET_PATH_UNSAFE",
  "FORMAT_VERSION_UNSUPPORTED", "GRAPH_CYCLE", "GRAPH_EDGE_INVALID",
  "GRAPH_NODE_NO_PATH_TO_END", "GRAPH_NODE_UNREACHABLE", "GRAPH_PROFILE_INVALID",
  "GRAPH_RIDDLE_UNREACHABLE", "ID_DUPLICATE", "INPUT_CHANGED_DURING_RUN",
  "INPUT_DEER_TOO_LARGE", "INPUT_DEER_UNREADABLE", "INPUT_PROJECT_INVALID", "INPUT_UTF8_BOM",
  "INPUT_UTF8_INVALID", "INTERNAL_ERROR", "JSON_DUPLICATE_KEY", "JSON_PARSE_INVALID",
  "JSON_UNICODE_INVALID", "PLAYER_COUNT_INVALID", "REFERENCE_UNKNOWN",
  "RUNNER_CAPACITY_EXCEEDED", "SCHEMA_INVALID", "SURFACE_CARDINALITY_INVALID",
  "SURFACE_INCOMPATIBLE", "SURFACE_OWNERSHIP_INVALID", "TEXT_LONG",
]);

const PHASES = new Set(["input", "schema", "references", "graph", "capability", "assets", "feasibility"]);
const SHA256 = /^[0-9a-f]{64}$/;
const POINTER = /^(?:\/(?:[^~/]|~[01])*)*$/;

const MESSAGES = {
  "validation.assets.content_mismatch": "Eine eigene Datei passt nicht zu den Angaben im Entwurf.",
  "validation.assets.directory_unreadable": "Die eigenen Dateien konnten nicht vollständig gelesen werden.",
  "validation.assets.directory_unsafe": "Der Ordner mit eigenen Dateien kann nicht sicher verwendet werden.",
  "validation.assets.file_unreferenced": "Eine nicht verwendete Datei wurde dem Spiel hinzugefügt.",
  "validation.assets.hash_mismatch": "Eine eigene Datei hat sich unerwartet verändert.",
  "validation.assets.image_capacity_exceeded": "Ein Bild ist für das Spiel zu groß.",
  "validation.assets.missing": "Eine benötigte eigene Datei fehlt.",
  "validation.assets.path_duplicate": "Dieselbe eigene Datei ist mehrfach eingetragen.",
  "validation.assets.path_unsafe": "Eine eigene Datei hat einen nicht unterstützten Namen.",
  "validation.runner.capacity_exceeded": "Der Entwurf ist für die aktuelle Spielversion zu umfangreich.",
  "validation.input.changed_during_read": "Der Entwurf wurde während der Prüfung verändert. Bitte prüfe ihn erneut.",
  "validation.input.deer_not_regular": "Die gespeicherte Projektdatei kann nicht verwendet werden.",
  "validation.input.deer_too_large": "Der Entwurf ist für die Prüfung zu groß.",
  "validation.input.deer_unreadable": "Der Entwurf konnte nicht gelesen werden.",
  "validation.input.format_version_unsupported": "Der Entwurf gehört zu einer nicht unterstützten Spielversion.",
  "validation.input.json_duplicate_key": "Der gespeicherte Entwurf enthält widersprüchliche Angaben.",
  "validation.input.json_parse_invalid": "Der gespeicherte Entwurf ist beschädigt.",
  "validation.input.json_unicode_invalid": "Ein Text enthält nicht unterstützte Zeichen.",
  "validation.input.project_link_or_not_directory": "Die Spieldateien konnten nicht vorbereitet werden.",
  "validation.input.project_unreadable": "Die Spieldateien konnten nicht gelesen werden.",
  "validation.input.utf8_bom": "Der gespeicherte Entwurf hat ein nicht unterstütztes Textformat.",
  "validation.input.utf8_invalid": "Der gespeicherte Entwurf enthält ungültige Textzeichen.",
  "validation.schema.root_object": "Der gespeicherte Entwurf ist unvollständig.",
  "validation.schema.invalid": "Eine Angabe im Entwurf ist ungültig oder fehlt.",
  "validation.derivation.failed": "Das Spiel konnte aus dem Entwurf nicht aufgebaut werden.",
  "validation.internal_error": "Die Spielprüfung konnte nicht abgeschlossen werden.",
  "validation.assets.declared_unused": "Eine eingetragene Datei wird im Spiel nicht verwendet.",
  "validation.capability.exit_surface_incompatible": "Der Ausgang ist nicht passend eingerichtet.",
  "validation.capability.player_count_invalid": "Die gewählte Anzahl Spielender wird nicht unterstützt.",
  "validation.capability.surface_cardinality_invalid": "Ein Ort oder Gerät wird nicht in der benötigten Anzahl verwendet.",
  "validation.capability.surface_incompatible": "Ein Rätsel verwendet einen unpassenden Ort oder ein unpassendes Gerät.",
  "validation.capability.surface_ownership_invalid": "Ein Ort oder Gerät ist mehreren Rätseln zugeordnet.",
  "validation.feasibility.capacity_exceeded": "Der Entwurf ist für einen einzelnen Spielraum zu umfangreich.",
  "validation.feasibility.text_long": "Ein Text ist sehr lang und könnte im Spiel schlecht lesbar sein.",
  "validation.graph.cycle": "Der Spielablauf enthält einen Kreis.",
  "validation.graph.edge_invalid": "Eine Verbindung im Spielablauf ist ungültig.",
  "validation.graph.node_no_path_to_end": "Von einem Schritt führt kein Weg zum Spielende.",
  "validation.graph.node_unreachable": "Ein Schritt kann vom Start aus nicht erreicht werden.",
  "validation.graph.profile_invalid": "Der Spielablauf kann so nicht verwendet werden.",
  "validation.graph.riddle_binding_invalid": "Ein Rätsel ist im Spielablauf nicht richtig eingebunden.",
  "validation.references.asset_unknown": "Ein Rätsel verweist auf eine unbekannte Datei.",
  "validation.references.graph_node_unknown": "Der Spielablauf verweist auf einen unbekannten Schritt.",
  "validation.references.id_duplicate": "Ein Element ist mehrfach angelegt.",
  "validation.references.information_source_unknown": "Eine Eingabe verweist auf unbekanntes Material.",
  "validation.references.riddle_unknown": "Der Spielablauf verweist auf ein unbekanntes Rätsel.",
  "validation.references.surface_unknown": "Ein Rätsel verweist auf einen unbekannten Ort.",
} as const;

export type KnownValidationMessageKey = keyof typeof MESSAGES;

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);
const hasExactKeys = (value: Record<string, unknown>, keys: string[]) =>
  Object.keys(value).length === keys.length && keys.every((key) => key in value);
const isScalar = (value: unknown) =>
  typeof value === "string" || typeof value === "boolean"
  || (typeof value === "number" && Number.isFinite(value));

export function parseProjectValidationReport(value: unknown): ProjectValidationReport {
  if (!isRecord(value) || !hasExactKeys(value, [
    "valid", "runnerVersion", "rawDeerSha256", "hostInputSha256", "issues",
  ])) throw new Error("Die Antwort der vollständigen Prüfung ist ungültig.");
  if (typeof value.valid !== "boolean" || typeof value.runnerVersion !== "string"
    || value.runnerVersion.length === 0
    || (value.rawDeerSha256 !== null && (typeof value.rawDeerSha256 !== "string" || !SHA256.test(value.rawDeerSha256)))
    || (value.hostInputSha256 !== null && (typeof value.hostInputSha256 !== "string" || !SHA256.test(value.hostInputSha256)))
    || !Array.isArray(value.issues)) {
    throw new Error("Die Antwort der vollständigen Prüfung ist ungültig.");
  }
  const issues = value.issues.map((raw): ProductionValidationIssue => {
    if (!isRecord(raw) || !hasExactKeys(raw, [
      "severity", "phase", "code", "messageKey", "arguments", "path", "entity", "relatedPaths",
    ]) || (raw.severity !== "error" && raw.severity !== "warning")
      || typeof raw.phase !== "string" || !PHASES.has(raw.phase)
      || typeof raw.code !== "string" || !ISSUE_CODES.has(raw.code)
      || typeof raw.messageKey !== "string" || !(raw.messageKey in MESSAGES)
      || !isRecord(raw.arguments) || !Object.values(raw.arguments).every(isScalar)
      || typeof raw.path !== "string" || !POINTER.test(raw.path)
      || !Array.isArray(raw.relatedPaths)
      || !raw.relatedPaths.every((path) => typeof path === "string" && POINTER.test(path))
      || new Set(raw.relatedPaths).size !== raw.relatedPaths.length) {
      throw new Error("Die Antwort der vollständigen Prüfung enthält eine unbekannte Meldung.");
    }
    let entity: ProductionValidationIssue["entity"] = null;
    if (raw.entity !== null) {
      if (!isRecord(raw.entity) || !hasExactKeys(raw.entity, ["kind", "id"])
        || typeof raw.entity.kind !== "string" || raw.entity.kind.length === 0
        || typeof raw.entity.id !== "string" || raw.entity.id.length === 0) {
        throw new Error("Die Antwort der vollständigen Prüfung enthält eine ungültige Zuordnung.");
      }
      entity = { kind: raw.entity.kind, id: raw.entity.id };
    }
    return {
      severity: raw.severity,
      messageKey: raw.messageKey as KnownValidationMessageKey,
      path: raw.path,
      entity,
      relatedPaths: raw.relatedPaths as string[],
    };
  });
  if (value.valid !== !issues.some((issue) => issue.severity === "error")) {
    throw new Error("Die Antwort der vollständigen Prüfung ist widersprüchlich.");
  }
  return { valid: value.valid, issues };
}

function issueTab(issue: ProductionValidationIssue, draft: WizardDraft): ValidatedTabId | "review" {
  const id = issue.entity?.id;
  const path = [issue.path, ...issue.relatedPaths].join("/");
  if (issue.messageKey === "validation.graph.riddle_binding_invalid") return "riddle_graph";
  if (issue.messageKey === "validation.capability.exit_surface_incompatible") return "game_end";
  if (issue.messageKey === "validation.references.surface_unknown"
    && issue.entity?.kind === "graph_node"
    && path.includes("riddleGraph")
    && path.includes("surfaceId")) return "game_end";
  if (path.includes("riddleGraph")) return "riddle_graph";
  if (path.includes("successText") || path.includes("failureText") || path.includes("debriefPrompts")) {
    return "game_end";
  }
  const riddle = id ? draft.project.riddles.find((entry) => entry.id === id) : undefined;
  if (riddle) return "riddles";
  const asset = id ? draft.project.assets.find((entry) => entry.id === id) : undefined;
  if (asset) return "assets";
  if (id) {
    const isExitSurface = draft.project.riddleGraph.nodes.some(
      (node) => node.kind === "end" && node.surfaceId === id,
    );
    if (isExitSurface) return "game_end";
    const isRiddleSurface = draft.project.riddles.some((entry) =>
      entry.informationSources.some((source) => source.surfaceId === id)
      || entry.inputs.some((input) => input.type === "numeric" && input.surfaceId === id),
    );
    if (isRiddleSurface) return "riddles";
  }
  if (path.includes("riddles")) return "riddles";
  if (path.includes("assets")) return "assets";
  if (path.includes("scenario")) return "scenario";
  if (path.includes("session")) return "session";
  if (path.includes("learningDesign") || path.includes("metadata")) return "metadata";
  return "review";
}

function issueArea(tabId: ValidatedTabId | "review", issue: ProductionValidationIssue, draft: WizardDraft) {
  const riddle = issue.entity?.id
    ? draft.project.riddles.find((entry) => entry.id === issue.entity?.id)
    : undefined;
  if (riddle) return `Rätsel „${riddle.title.trim() || "Unbenannt"}“`;
  return {
    metadata: "Eckdaten und Lernziele",
    scenario: "Geschichte",
    session: "Spieleinstellungen",
    assets: "Eigene Bilder und Dateien",
    riddles: "Rätsel",
    riddle_graph: "Spielablauf",
    game_end: "Spiel-Ende",
    review: undefined,
  }[tabId];
}

export function productionIssueReport(report: ProjectValidationReport, draft: WizardDraft): IssueReport {
  const result: IssueReport = Object.fromEntries(
    [...VALIDATED_TAB_IDS, "review"].map((tabId) => [tabId, {}]),
  );
  report.issues.forEach((issue, index) => {
    const tabId = issueTab(issue, draft);
    const area = issueArea(tabId, issue, draft);
    const localized: Issue = {
      severity: issue.severity,
      description: MESSAGES[issue.messageKey],
      ...(area ? { details: `Bereich: ${area}` } : {}),
    };
    result[tabId][`production:${index}`] = [localized];
  });
  return result;
}
