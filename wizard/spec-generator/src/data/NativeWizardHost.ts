import type { DeerSchema } from "./DeerSchema";
import { parseProjectValidationReport, type ProjectValidationReport } from "./ProjectValidationReport";

export interface CustomAssetPayload { path: string; bytesBase64: string; }
export interface ProductionRequest { project: DeerSchema; customAssets: CustomAssetPayload[]; }
export type PackageResult =
  | { kind: "invalid"; report: ProjectValidationReport }
  | { kind: "ready"; jar: Blob };

export interface WizardHostPort {
  readonly native: boolean;
  validate(request: ProductionRequest): Promise<ProjectValidationReport>;
  package(request: ProductionRequest): Promise<PackageResult>;
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

function errorMessage(status: number, value: unknown, action: string): string {
  void value;
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

  async validate(request: ProductionRequest): Promise<ProjectValidationReport> {
    const value = await apiJson("/validate", request, "Die vollständige Prüfung");
    if (!isRecord(value) || !("report" in value)) {
      throw new Error("Die vollständige Prüfung hat eine ungültige Antwort geliefert.");
    }
    return parseProjectValidationReport(value.report);
  }

  async package(request: ProductionRequest): Promise<PackageResult> {
    const response = await fetch("/api/v1/package", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/java-archive, application/json",
      },
      body: JSON.stringify(request),
    });
    const contentType = response.headers.get("content-type")?.toLowerCase() ?? "";
    if (response.ok && contentType.includes("application/java-archive")) {
      return { kind: "ready", jar: await response.blob() };
    }
    const value = await responseJson(response, "Das Erstellen der Spieldatei");
    if (!isRecord(value) || !("report" in value)) {
      throw new Error("Das Erstellen der Spieldatei hat eine ungültige Antwort geliefert.");
    }
    return { kind: "invalid", report: parseProjectValidationReport(value.report) };
  }
}

export class BrowserWizardHost implements WizardHostPort {
  readonly native = false;
  private unavailable(): never {
    throw new Error("Diese Funktion ist nur in der lokal gestarteten Wizard-Anwendung verfügbar.");
  }
  async validate(): Promise<ProjectValidationReport> { return this.unavailable(); }
  async package(): Promise<PackageResult> { return this.unavailable(); }
}
