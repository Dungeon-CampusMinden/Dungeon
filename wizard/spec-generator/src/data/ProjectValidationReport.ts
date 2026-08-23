import type { WizardDraft } from "./WizardDraft";
import type { Issue, IssueReport } from "./ErrorChecker";
import { VALIDATED_TAB_IDS, type ValidatedTabId } from "./Tabs";

export type ProductionIssueSeverity = "error" | "warning";
export type ProductionValidationArgument = string | number | boolean;

export interface ProductionValidationIssue {
  severity: ProductionIssueSeverity;
  messageKey: KnownValidationMessageKey;
  arguments: Record<string, ProductionValidationArgument>;
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
  "validation.capability.surface_cardinality_invalid": "Der Spielraum und sein Ausgang konnten nicht eindeutig vorbereitet werden.",
  "validation.capability.surface_incompatible": "Material oder Eingabe eines Rätsels ist nicht passend eingerichtet.",
  "validation.capability.surface_ownership_invalid": "Eine Material- oder Eingabezuordnung gehört nicht genau zu einem Rätsel.",
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
  "validation.references.surface_unknown": "Eine Verknüpfung zu Material, Eingabe oder Spielende fehlt.",
} as const;

export type KnownValidationMessageKey = keyof typeof MESSAGES;

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);
const hasExactKeys = (value: Record<string, unknown>, keys: string[]) =>
  Object.keys(value).length === keys.length && keys.every((key) => key in value);
const isScalar = (value: unknown): value is ProductionValidationArgument =>
  typeof value === "string" || typeof value === "boolean"
  || (typeof value === "number" && Number.isFinite(value));
const hasScalarValues = (value: unknown): value is Record<string, ProductionValidationArgument> =>
  isRecord(value) && Object.values(value).every(isScalar);

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
      || !hasScalarValues(raw.arguments)
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
      arguments: { ...raw.arguments },
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

const capacityKinds: Record<string, { singular: string; plural: string }> = {
  riddles: { singular: "Rätsel", plural: "Rätsel" },
  graph_edges: { singular: "Verbindung im Spielablauf", plural: "Verbindungen im Spielablauf" },
  resources: { singular: "Material", plural: "Materialien" },
  hints: { singular: "Hinweis", plural: "Hinweise" },
};

const runnerDimensions: Record<string, { singular: string; plural: string }> = {
  referencedAssets: { singular: "verwendetes Bild", plural: "verwendete Bilder" },
  assetDirectoryEntries: { singular: "eigene Datei", plural: "eigene Dateien" },
};

const countMessage = (
  arguments_: Record<string, ProductionValidationArgument>,
  labels: Record<string, { singular: string; plural: string }>,
  discriminator: "kind" | "dimension",
): string | null => {
  const actual = arguments_.actual;
  const limit = arguments_.limit;
  const key = arguments_[discriminator];
  if (typeof actual !== "number" || typeof limit !== "number" || typeof key !== "string") return null;
  const label = labels[key];
  if (label === undefined) return null;
  return `Der Entwurf enthält ${actual.toLocaleString("de-DE")} ${actual === 1 ? label.singular : label.plural}. Höchstens ${limit.toLocaleString("de-DE")} sind möglich.`;
};

const formatBytes = (bytes: number): string => {
  if (bytes < 1024) return `${bytes.toLocaleString("de-DE")} Byte`;
  const units = ["KiB", "MiB", "GiB"];
  let value = bytes / 1024;
  let unit = units[0];
  for (let index = 1; index < units.length && value >= 1024; index++) {
    value /= 1024;
    unit = units[index];
  }
  const formatted = `${value.toLocaleString("de-DE", { maximumFractionDigits: 1 })} ${unit}`;
  return Number.isInteger(value)
    ? formatted
    : `${formatted} (${bytes.toLocaleString("de-DE")} Byte)`;
};

function issueDescription(issue: ProductionValidationIssue): string {
  const arguments_ = issue.arguments;
  if (issue.messageKey === "validation.feasibility.capacity_exceeded") {
    return countMessage(arguments_, capacityKinds, "kind") ?? MESSAGES[issue.messageKey];
  }
  if (issue.messageKey === "validation.runner.capacity_exceeded") {
    const count = countMessage(arguments_, runnerDimensions, "dimension");
    if (count !== null) return count;
    const limit = arguments_.limit;
    if (typeof limit === "number"
      && (arguments_.dimension === "assetBytes" || arguments_.dimension === "referencedAssetBytes")) {
      return `Die eigenen Dateien sind für die aktuelle Spielversion zu groß. Erlaubt sind höchstens ${formatBytes(limit)}.`;
    }
  }
  if (issue.messageKey === "validation.input.deer_too_large") {
    const actual = arguments_.actual;
    const limit = arguments_.limit;
    if (typeof actual === "number" && typeof limit === "number") {
      return `Der Entwurf ist ${formatBytes(actual)} groß. Für die Prüfung sind höchstens ${formatBytes(limit)} möglich.`;
    }
  }
  if (issue.messageKey === "validation.assets.image_capacity_exceeded") {
    const { width, height, maxDimension, maxPixels } = arguments_;
    if (typeof width === "number" && typeof height === "number"
      && typeof maxDimension === "number" && typeof maxPixels === "number") {
      return `Das Bild ist ${width.toLocaleString("de-DE")} × ${height.toLocaleString("de-DE")} Pixel groß. Erlaubt sind höchstens ${maxDimension.toLocaleString("de-DE")} Pixel pro Seite und ${maxPixels.toLocaleString("de-DE")} Pixel insgesamt.`;
    }
  }
  if (issue.messageKey === "validation.feasibility.text_long") {
    const actual = arguments_.actual;
    const limit = arguments_.limit;
    if (typeof actual === "number" && typeof limit === "number") {
      return `Der Text hat ${actual.toLocaleString("de-DE")} Zeichen. Für gute Lesbarkeit werden höchstens ${limit.toLocaleString("de-DE")} empfohlen.`;
    }
  }
  return MESSAGES[issue.messageKey];
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
      description: issueDescription(issue),
      ...(area ? { details: `Bereich: ${area}` } : {}),
    };
    result[tabId][`production:${index}`] = [localized];
  });
  return result;
}
