import type { DeerProject } from "./DeerSchema";
import type { AssetStoragePort, StoredAssetFile } from "./AssetStorage";
import {
  assertWizardDraft,
  DraftReloadRequiredError,
  type DraftStoragePort,
  type DraftSummary,
} from "./DraftStorage";
import { parseProjectValidationReport, type ProjectValidationReport } from "./ProjectValidationReport";
import { draftRevision, type DraftRevision, type WizardDraft } from "./WizardDraft";

export interface FinalizeResult {
  revision: DraftRevision;
  candidateHash: string;
  report: ProjectValidationReport;
  seed: number | null;
  finalizedAt: string | null;
  projectDirectory: string | null;
  deerSha256: string | null;
}

export interface FinalizationIdentity {
  seed: number;
  finalizedAt: string;
  projectDirectory: string;
  deerSha256: string;
}

export interface PackageRequest extends FinalizationIdentity {
  revision: DraftRevision;
}

export interface PackageResult extends FinalizationIdentity {
  revision: DraftRevision;
  jarPath: string;
  jarSha256: string;
}

export interface FinalizationStatus {
  revision: DraftRevision;
  status: "not-finalized" | "invalid" | "finalized" | "ready";
  seed: number | null;
  finalizedAt: string | null;
  projectDirectory: string | null;
  deerSha256: string | null;
  jarPath: string | null;
  jarSha256: string | null;
}

export interface ValidationResult {
  revision: DraftRevision;
  candidateHash: string;
  report: ProjectValidationReport;
}

export interface WizardHostPort {
  readonly native: boolean;
  chooseProjectDirectory(): Promise<string | null>;
  validate(draftId: string, revision: DraftRevision, candidateHash: string, project: DeerProject, uploads: Record<string, string>): Promise<ValidationResult>;
  finalize(draftId: string, revision: DraftRevision, candidateHash: string, project: DeerProject, uploads: Record<string, string>, projectDirectory: string): Promise<FinalizeResult>;
  package(draftId: string, request: PackageRequest): Promise<PackageResult>;
  finalizationStatus(draftId: string): Promise<FinalizationStatus>;
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const isSha256 = (value: unknown): value is string =>
  typeof value === "string" && /^[0-9a-f]{64}$/.test(value);

function errorMessage(status: number, value: unknown, action: string): string {
  const code = isRecord(value) && typeof value.code === "string" ? value.code : null;
  if (status === 409 && code === "PROJECT_DIRECTORY_OWNED") {
    return "Dieser Zielordner gehört bereits zu einem anderen Spiel. Wähle bitte einen anderen Ordner.";
  }
  if (status === 409 && code === "PROJECT_OWNERSHIP_UNAVAILABLE") {
    return "Der Zielordner kann diesem Spiel gerade nicht sicher zugeordnet werden. Deine Eingaben bleiben erhalten. Wähle einen anderen Ordner oder versuche es erneut.";
  }
  if (status === 409 && code === "FINALIZATION_IDENTITY_MISMATCH") {
    return "Die gespeicherte Spielversion hat sich geändert. Deine Eingaben bleiben erhalten. Erstelle das Spiel erneut, bevor du die Spieldatei erzeugst.";
  }
  return `${action} ist fehlgeschlagen (${status}).`;
}

async function responseJson(response: Response, action: string): Promise<unknown> {
  if (!response.ok) {
    let value: unknown;
    try { value = await response.json(); } catch { value = null; }
    const code = isRecord(value) && typeof value.code === "string" ? value.code : null;
    if (response.status === 409 && code === "REVISION_CONFLICT") {
      throw new DraftReloadRequiredError("revision-conflict");
    }
    if (response.status === 409 && code === "FINALIZATION_RECOVERED") {
      throw new DraftReloadRequiredError("finalization-recovered");
    }
    throw new Error(errorMessage(response.status, value, action));
  }
  if (!response.headers.get("content-type")?.toLowerCase().includes("application/json")) {
    throw new Error(`${action} hat eine ungültige Antwort geliefert.`);
  }
  try {
    return await response.json();
  } catch {
    throw new Error(`${action} hat eine unlesbare Antwort geliefert.`);
  }
}

async function apiJson(path: string, init: RequestInit | undefined, action: string) {
  return responseJson(await fetch(`/api/v1${path}`, init), action);
}

export async function detectNativeHost(): Promise<boolean> {
  try {
    const response = await fetch("/api/v1/status", { headers: { Accept: "application/json" } });
    const value = await responseJson(response, "Die Host-Erkennung");
    return isRecord(value) && Object.keys(value).length === 2
      && value.apiVersion === "1" && value.mode === "native";
  } catch {
    return false;
  }
}

export class NativeDraftStorage implements DraftStoragePort {
  async list(): Promise<DraftSummary[]> {
    const value = await apiJson("/drafts", undefined, "Das Laden der Entwürfe");
    if (!Array.isArray(value)) throw new Error("Die Entwurfsliste ist ungültig.");
    return value.map((entry) => {
      if (!isRecord(entry) || typeof entry.draftId !== "string" || typeof entry.title !== "string"
        || (entry.savedAt !== undefined && typeof entry.savedAt !== "string")) {
        throw new Error("Die Entwurfsliste enthält einen ungültigen Eintrag.");
      }
      return { draftId: entry.draftId, title: entry.title, ...(entry.savedAt ? { savedAt: entry.savedAt } : {}) };
    });
  }

  async load(draftId: string): Promise<WizardDraft | null> {
    const response = await fetch(`/api/v1/drafts/${encodeURIComponent(draftId)}`);
    if (response.status === 404) return null;
    const value = await responseJson(response, "Das Öffnen des Entwurfs");
    assertWizardDraft(value);
    return structuredClone(value);
  }

  async save(draft: WizardDraft): Promise<WizardDraft> {
    const value = await apiJson(`/drafts/${encodeURIComponent(draft.draftId)}`, {
      method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(draft),
    }, "Das Speichern des Entwurfs");
    assertWizardDraft(value);
    return structuredClone(value);
  }
}

export class NativeAssetStorage implements AssetStoragePort {
  async putAssetFile(draftId: string, file: File): Promise<string> {
    const value = await apiJson(
      `/drafts/${encodeURIComponent(draftId)}/uploads?name=${encodeURIComponent(file.name)}`,
      { method: "POST", headers: { "Content-Type": file.type }, body: file },
      "Das Speichern der Datei",
    );
    if (!isRecord(value) || typeof value.storageKey !== "string" || !/^[0-9a-f]{64}$/.test(value.storageKey)
      || typeof value.originalName !== "string") throw new Error("Die gespeicherte Datei wurde nicht bestätigt.");
    return value.storageKey;
  }

  async getAssetFile(draftId: string, storageKey: string): Promise<StoredAssetFile | null> {
    const response = await fetch(
      `/api/v1/drafts/${encodeURIComponent(draftId)}/uploads/${encodeURIComponent(storageKey)}`,
    );
    if (response.status === 404) return null;
    if (!response.ok) throw new Error(`Die Dateivorschau ist fehlgeschlagen (${response.status}).`);
    const blob = await response.blob();
    return { id: `${draftId}:${storageKey}`, draftId, storageKey, name: storageKey, mediaType: blob.type, blob };
  }

  async listAssetIds(draftId: string): Promise<string[]> {
    const value = await apiJson(`/drafts/${encodeURIComponent(draftId)}/uploads`, undefined, "Das Prüfen der Dateien");
    if (!isRecord(value) || !Array.isArray(value.storageKeys)
      || !value.storageKeys.every((key) => typeof key === "string" && /^[0-9a-f]{64}$/.test(key))) {
      throw new Error("Die Dateiliste ist ungültig.");
    }
    return value.storageKeys as string[];
  }
}

export class NativeWizardHost implements WizardHostPort {
  readonly native = true;

  async chooseProjectDirectory(): Promise<string | null> {
    const response = await fetch("/api/v1/choose-project-directory", { method: "POST" });
    if (response.status === 204) return null;
    const value = await responseJson(response, "Die Ordnerauswahl");
    if (!isRecord(value) || typeof value.projectDirectory !== "string" || value.projectDirectory.length === 0) {
      throw new Error("Der gewählte Ordner wurde nicht bestätigt.");
    }
    return value.projectDirectory;
  }

  async validate(draftId: string, revision: DraftRevision, candidateHash: string, project: DeerProject, uploads: Record<string, string>) {
    const value = await apiJson(`/drafts/${encodeURIComponent(draftId)}/validate`, {
      method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ revision, candidateHash, project, uploads }),
    }, "Die vollständige Prüfung");
    if (!isRecord(value) || !Number.isSafeInteger(value.revision) || (value.revision as number) < 0
      || !isSha256(value.candidateHash) || !("report" in value)) {
      throw new Error("Die vollständige Prüfung hat eine ungültige Antwort geliefert.");
    }
    return {
      revision: draftRevision(value.revision as number),
      candidateHash: value.candidateHash,
      report: parseProjectValidationReport(value.report),
    };
  }

  async finalize(draftId: string, revision: DraftRevision, candidateHash: string, project: DeerProject, uploads: Record<string, string>, projectDirectory: string) {
    const value = await apiJson(`/drafts/${encodeURIComponent(draftId)}/finalize`, {
      method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ revision, candidateHash, project, uploads, projectDirectory }),
    }, "Das Erstellen des Spiels");
    if (!isRecord(value) || !("report" in value)
      || !Number.isSafeInteger(value.revision) || (value.revision as number) < 0
      || !isSha256(value.candidateHash)
      || (value.seed !== null && (!Number.isSafeInteger(value.seed) || (value.seed as number) < 0))
      || (value.finalizedAt !== null
        && (typeof value.finalizedAt !== "string" || value.finalizedAt.length === 0))
      || (value.projectDirectory !== null
        && (typeof value.projectDirectory !== "string" || value.projectDirectory.length === 0))
      || (value.deerSha256 !== null
        && (typeof value.deerSha256 !== "string" || !/^[0-9a-f]{64}$/.test(value.deerSha256)))) {
      throw new Error("Das Erstellen des Spiels hat eine ungültige Antwort geliefert.");
    }
    const report = parseProjectValidationReport(value.report);
    const identityComplete = value.seed !== null && value.finalizedAt !== null
      && value.projectDirectory !== null && value.deerSha256 !== null;
    const identityAbsent = value.seed === null && value.finalizedAt === null
      && value.projectDirectory === null && value.deerSha256 === null;
    if ((report.valid && !identityComplete) || (!report.valid && !identityAbsent)) {
      throw new Error("Das Erstellen des Spiels hat eine widersprüchliche Antwort geliefert.");
    }
    return {
      revision: draftRevision(value.revision as number),
      candidateHash: value.candidateHash,
      report,
      seed: value.seed as number | null,
      finalizedAt: value.finalizedAt as string | null,
      projectDirectory: value.projectDirectory as string | null,
      deerSha256: value.deerSha256 as string | null,
    };
  }

  async package(draftId: string, request: PackageRequest) {
    const value = await apiJson(`/drafts/${encodeURIComponent(draftId)}/package`, {
      method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(request),
    }, "Das Erstellen der Spieldatei");
    if (!isRecord(value) || !Number.isSafeInteger(value.revision) || (value.revision as number) < 0
      || !Number.isSafeInteger(value.seed) || (value.seed as number) < 0
      || typeof value.finalizedAt !== "string" || value.finalizedAt.length === 0
      || typeof value.projectDirectory !== "string" || value.projectDirectory.length === 0
      || !isSha256(value.deerSha256)
      || typeof value.jarPath !== "string" || value.jarPath.length === 0
      || !isSha256(value.jarSha256)) {
      throw new Error("Die Spieldatei wurde nicht bestätigt.");
    }
    return {
      revision: draftRevision(value.revision as number),
      seed: value.seed as number,
      finalizedAt: value.finalizedAt,
      projectDirectory: value.projectDirectory,
      deerSha256: value.deerSha256,
      jarPath: value.jarPath,
      jarSha256: value.jarSha256,
    };
  }

  async finalizationStatus(draftId: string) {
    const value = await apiJson(
      `/drafts/${encodeURIComponent(draftId)}/finalization-status`,
      undefined,
      "Das Prüfen der erstellten Spieldatei",
    );
    const validStatus = isRecord(value)
      && ["not-finalized", "invalid", "finalized", "ready"].includes(String(value.status))
      && Number.isSafeInteger(value.revision) && (value.revision as number) >= 0
      && (value.seed === null || (Number.isSafeInteger(value.seed) && (value.seed as number) >= 0))
      && (value.finalizedAt === null || (typeof value.finalizedAt === "string" && value.finalizedAt.length > 0))
      && (value.projectDirectory === null || (typeof value.projectDirectory === "string" && value.projectDirectory.length > 0))
      && (value.deerSha256 === null || isSha256(value.deerSha256))
      && (value.jarPath === null || (typeof value.jarPath === "string" && value.jarPath.length > 0))
      && (value.jarSha256 === null || isSha256(value.jarSha256));
    if (!validStatus) throw new Error("Der Status der Spieldatei ist ungültig.");
    const identityComplete = value.seed !== null && value.finalizedAt !== null
      && value.projectDirectory !== null && value.deerSha256 !== null;
    const identityPresent = value.seed !== null || value.finalizedAt !== null
      || value.projectDirectory !== null || value.deerSha256 !== null;
    const jarComplete = value.jarPath !== null && value.jarSha256 !== null;
    if ((value.status === "not-finalized" && (identityPresent || jarComplete))
      || ((value.status === "finalized" || value.status === "ready") && !identityComplete)
      || (value.status === "finalized" && jarComplete)
      || (value.status === "ready" && !jarComplete)
      || ((value.jarPath === null) !== (value.jarSha256 === null))) {
      throw new Error("Der Status der Spieldatei ist widersprüchlich.");
    }
    return { ...value, revision: draftRevision(value.revision as number) } as FinalizationStatus;
  }
}

export class BrowserWizardHost implements WizardHostPort {
  readonly native = false;
  private unavailable(): never { throw new Error("Diese Funktion ist nur in der lokal gestarteten Wizard-Anwendung verfügbar."); }
  async chooseProjectDirectory(): Promise<string | null> { return this.unavailable(); }
  async validate(): Promise<ValidationResult> { return this.unavailable(); }
  async finalize(): Promise<FinalizeResult> { return this.unavailable(); }
  async package(): Promise<PackageResult> { return this.unavailable(); }
  async finalizationStatus(): Promise<FinalizationStatus> { return this.unavailable(); }
}
