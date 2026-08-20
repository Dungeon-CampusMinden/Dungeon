import type { DeerSchema } from "./DeerSchema";
import { parseProjectValidationReport, type ProjectValidationReport } from "./ProjectValidationReport";

export interface CustomAssetPayload { path: string; bytesBase64: string; }
export interface ProductionRequest { project: DeerSchema; customAssets: CustomAssetPayload[]; }
export interface FinalizeRequest extends ProductionRequest { projectDirectory: string; }
export interface PackageResult { report: ProjectValidationReport; jarPath: string | null; }

export interface WizardHostPort {
  readonly native: boolean;
  chooseProjectDirectory(): Promise<string | null>;
  validate(request: ProductionRequest): Promise<ProjectValidationReport>;
  finalize(request: FinalizeRequest): Promise<ProjectValidationReport>;
  package(projectDirectory: string, projectId: string): Promise<PackageResult>;
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

function errorMessage(status: number, value: unknown, action: string): string {
  const code = isRecord(value) && typeof value.code === "string" ? value.code : null;
  if (status === 409 && code === "PROJECT_DIRECTORY_CONFLICT") {
    return "Dieser Zielordner gehört bereits zu einem anderen Spiel. Wähle bitte einen anderen Ordner.";
  }
  if (status === 409) {
    return "Der Zielordner kann für dieses Spiel nicht verwendet werden. Deine Eingaben bleiben erhalten.";
  }
  return `${action} ist fehlgeschlagen (${status}).`;
}

async function responseJson(response: Response, action: string): Promise<unknown> {
  let value: unknown;
  try { value = await response.json(); } catch { value = null; }
  if (!response.ok) throw new Error(errorMessage(response.status, value, action));
  if (!response.headers.get("content-type")?.toLowerCase().includes("application/json")) {
    throw new Error(`${action} hat eine ungültige Antwort geliefert.`);
  }
  return value;
}

async function apiJson(path: string, body: unknown, action: string): Promise<unknown> {
  return responseJson(await fetch(`/api/v1${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  }), action);
}

export async function detectNativeHost(): Promise<boolean> {
  try {
    const response = await fetch("/api/v1/status", { headers: { Accept: "application/json" } });
    const value = await responseJson(response, "Die Host-Erkennung");
    return isRecord(value) && Object.keys(value).length === 2
      && value.apiVersion === "1" && value.mode === "native";
  } catch { return false; }
}

export class NativeWizardHost implements WizardHostPort {
  readonly native = true;

  async chooseProjectDirectory(): Promise<string | null> {
    const response = await fetch("/api/v1/choose-project-directory", { method: "POST" });
    if (response.status === 204) return null;
    const value = await responseJson(response, "Die Ordnerauswahl");
    if (!isRecord(value) || typeof value.projectDirectory !== "string" || !value.projectDirectory) {
      throw new Error("Der gewählte Ordner wurde nicht bestätigt.");
    }
    return value.projectDirectory;
  }

  async validate(request: ProductionRequest): Promise<ProjectValidationReport> {
    const value = await apiJson("/validate", request, "Die vollständige Prüfung");
    if (!isRecord(value) || !("report" in value)) {
      throw new Error("Die vollständige Prüfung hat eine ungültige Antwort geliefert.");
    }
    return parseProjectValidationReport(value.report);
  }

  async finalize(request: FinalizeRequest): Promise<ProjectValidationReport> {
    const value = await apiJson("/finalize", request, "Das Erstellen des Spiels");
    if (!isRecord(value) || !("report" in value)) {
      throw new Error("Das Erstellen des Spiels hat eine ungültige Antwort geliefert.");
    }
    return parseProjectValidationReport(value.report);
  }

  async package(projectDirectory: string, projectId: string): Promise<PackageResult> {
    const value = await apiJson("/package", { projectDirectory, projectId }, "Das Erstellen der Spieldatei");
    if (!isRecord(value) || !("report" in value)
      || (value.jarPath !== null && typeof value.jarPath !== "string")) {
      throw new Error("Die Spieldatei wurde nicht bestätigt.");
    }
    return { report: parseProjectValidationReport(value.report), jarPath: value.jarPath as string | null };
  }
}

export class BrowserWizardHost implements WizardHostPort {
  readonly native = false;
  private unavailable(): never {
    throw new Error("Diese Funktion ist nur in der lokal gestarteten Wizard-Anwendung verfügbar.");
  }
  async chooseProjectDirectory(): Promise<string | null> { return this.unavailable(); }
  async validate(): Promise<ProjectValidationReport> { return this.unavailable(); }
  async finalize(): Promise<ProjectValidationReport> { return this.unavailable(); }
  async package(): Promise<PackageResult> { return this.unavailable(); }
}
