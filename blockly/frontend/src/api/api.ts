import {config} from "../config.ts";

export interface ApiResult<T = string> {
  data: T;
  error: string;
  status: number;
  ok: boolean;
  interrupted: boolean;
}

export type CodeStatus = "running" | "completed" | "error";
export type ExecutionStatus = CodeStatus | "paused" | "stepping";

export interface Level {
  name: string;
  block_blocks?: string[];
}

const DEFAULT_HEADERS = {
  "Content-Type": "text/plain",
};

// Toggle this to see request/response logging while debugging network calls.
const DEBUG_NETWORK = true;

class ApiClient {
  private readonly baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl : `${baseUrl}/`;
  }

  private buildUrl(endpoint: string): URL {
    return new URL(endpoint, this.baseUrl);
  }

  /**
   * Sends a POST request with a plain-text body.
   *
   * @param endpoint The endpoint appended to {@link config.API_URL}.
   * @param text Optional request body; defaults to an empty string.
   * @param ignoreError When true, suppresses user-facing alerts on network errors.
   * @param signal Optional abort signal for callers that want to cancel a request.
   * @param query Optional query parameters appended to the request URL. Multiquery parameters are supported ({ tag: "a", page: 2, sort }
   */
  public async post(
    endpoint: string,
    text = "",
    ignoreError = false,
    signal?: AbortSignal,
    query?: Record<string, string | number | boolean | null | undefined>,
  ): Promise<Response | null> {
    const url = this.buildUrl(endpoint);
    if (query) {
      Object.entries(query).forEach(([key, value]) => {
        if (value === undefined || value === null) return;
        const values = Array.isArray(value) ? value : [value];
        values.forEach((v) => url.searchParams.append(key, String(v)));
      });
    }

    try {
      if (DEBUG_NETWORK) {
        console.debug("[api] ->", endpoint, text, query);
        console.debug("[api] ->", url.toString());
      }
      return await fetch(url, {
        method: "POST",
        headers: DEFAULT_HEADERS,
        body: text,
        signal,
      });
    } catch (error) {
      if (!ignoreError) {
        alert("Fehler beim Senden der Anfrage");
        console.error("Fehler beim Senden der Anfrage", error);
      } else if (DEBUG_NETWORK) {
        console.debug("[api] network error (ignored)", error);
      }
      return null;
    }
  }

  /**
   * Normalizes a fetch {@link Response} into a structured {@link ApiResult}.
   *
   * <p> Handles status 205 (program interrupted) and non-OK responses uniformly so
   * callers receive a predictable shape.</p>
   *
   * @param response The fetch response or null when the request failed.
   * @returns A normalized {@link ApiResult} describing the server reply.
   */
  public async toResult(response: Response | null): Promise<ApiResult<string>> {
    if (response == null) {
      return {
        data: "",
        error: "Fehler beim Abrufen der Antwort vom Server",
        status: 0,
        ok: false,
        interrupted: false,
      };
    }

    const status = response.status;

    if (status === 205) {
      if (DEBUG_NETWORK) {
        console.debug("[api] <- 205 Programm unterbrochen!");
      }
      return {
        data: "",
        error: "Programm unterbrochen!",
        status: status,
        ok: false,
        interrupted: true,
      };
    }

    if (!response.ok) {
      const errorMessage = await this.safeText(response) || "Fehler beim Abrufen der Antwort vom Server";
      if (DEBUG_NETWORK) {
        console.debug("[api] <-", status, errorMessage);
      }
      return {
        data: "",
        error: errorMessage,
        status: status,
        ok: false,
        interrupted: false,
      };
    }

    const data = await response.text();
    if (DEBUG_NETWORK) {
      console.debug("[api] <-", status, data);
    }
    return {
      data: data,
      error: "",
      status: status,
      ok: true,
      interrupted: false,
    };
  }

  private async safeText(response: Response): Promise<string> {
    try {
      return await response.text();
    } catch (err) {
      if (DEBUG_NETWORK) {
        console.debug("[api] failed to read response text", err);
      }
      return "";
    }
  }

  public async get(
    endpoint: string,
    ignoreError = false,
    signal?: AbortSignal,
    query?: Record<string, string | number | boolean | null | undefined>,
  ): Promise<Response | null> {
    const url = this.buildUrl(endpoint);
    if (query) {
      Object.entries(query).forEach(([key, value]) => {
        if (value === undefined || value === null) return;
        const values = Array.isArray(value) ? value : [value];
        values.forEach((v) => url.searchParams.append(key, String(v)));
      });
    }

    try {
      if (DEBUG_NETWORK) {
        console.debug("[api] ->", endpoint, query);
        console.debug("[api] ->", url.toString());
      }
      return await fetch(url, {
        method: "GET",
        headers: DEFAULT_HEADERS,
        signal,
      });
    } catch (error) {
      if (!ignoreError) {
        alert("Fehler beim Senden der Anfrage");
        console.error("Fehler beim Senden der Anfrage", error);
      } else if (DEBUG_NETWORK) {
        console.debug("[api] network error (ignored)", error);
      }
      return null;
    }
  }
}

const apiClient = new ApiClient(config.API_URL);

/**
 * Handle the response from the server
 *
 * <p> This function checks if the response from the server was ok or
 * the program got interrupted. If the response was not ok, an
 * error message is displayed. </p>
 *
 * @param response The response from the server
 * @returns The normalized server reply
 */
const handleResponse = async (response: Response | null): Promise<ApiResult<string>> => {
  return apiClient.toResult(response);
}

/**
 * Call the reset route of the server
 *
 * <p> This route is used to reset the game (current level)</p>
 *
 * @returns true if the game was successfully reset, false otherwise
 */
export const call_reset_route = async (): Promise<boolean> => {
  const reset_response = await apiClient.post("reset");
  const response = await handleResponse(reset_response);
  if (response.error !== "") {
    console.error("Fehler beim Zurücksetzen des Spiels", response);
  }

  return response.error === "";
}

/**
 * Call the clear route of the server
 *
 * <p> This route is used to reset all variables and values</p>
 *
 * @returns true if the values were successfully cleared, false otherwise
 */
export const call_clear_route = async () => {
  const clear_response = await apiClient.post("clear");
  const response = await handleResponse(clear_response);
  if (!response.ok) {
    console.error("Fehler beim Zurücksetzen der Werte", response);
  }
  return response.ok;
}

/**
 * Call the level route of the server
 *
 * <p> This route is used to get all available levels from the server.</p>
 *
 * @Returns A list of all levels.
 */
export const call_levels_route = async () => {
  const level_response = await apiClient.get("levels", true);
  const response = await handleResponse(level_response);
  if (response.error !== "") {
    console.error("Fehler beim Abrufen der Level", response);
    return [] as string[];
  }
  return response.data.split("\n");
}

/**
 * Call the level route of the server
 *
 * <p> This route is used to get the level from the server and the blocked blocks for blockly.</p>
 *
 * @param levelName If given, the Server will change the level to this level, before returning the level.
 *
 * @Returns The current level or the level list, with the block blocks
 */
export const call_level_route = async (levelName = ""): Promise<Level> => {
  const level_response = await apiClient.get("level", true, undefined, levelName ? {levelName: levelName} : undefined);
  const response = await handleResponse(level_response);
  if (response.error !== "") {
    console.error("Fehler beim Abrufen des Levels", response);
    return {
      name: "",
      block_blocks: [],
    };
  }
  const data = response.data.split(" ");
  return {
    name: data[0],
    block_blocks: data.length > 1 ? data.slice(1) : undefined,
  };
}

/**
 * Call the code route of the server
 *
 * <p> This route is used to send the full program code to the server</p>
 *
 * @param code The full program code to send to the server
 * @param sessionId Optional session identifier when the backend supports multiple sessions.
 * @returns true if the program was successfully started, false otherwise
 */
export const call_code_route = async (code: string, sessionId?: string): Promise<boolean> => {
  console.log("Call Code route: \n", code);
  const code_response = await apiClient.post("code", code, false, undefined, sessionId ? {sessionId: sessionId} : undefined);
  const response = await handleResponse(code_response);
  if (response.error !== "" && response.error !== "Programm unterbrochen!") {
    console.error("Fehler beim Ausführen des Programms", response);
  }
  return response.error === "";
};

/**
 * Advance execution by a single step.
 *
 * <p> This helper is future-facing for step-by-step execution via a custom IR.
 * If the backend does not support stepping yet, a 404/500 is treated as an error
 * and can be surfaced to the UI.</p>
 *
 * @param sessionId Optional session identifier when the backend supports multiple sessions.
 * @returns The normalized server reply for the step attempt.
 */
export const call_code_step_route = async (sessionId?: string): Promise<ApiResult<string>> => {
  const step_response = await apiClient.post("step", "", false, undefined, sessionId ? {sessionId: sessionId} : undefined);
  const response = await handleResponse(step_response);
  if (response.error !== "" && response.error !== "Programm unterbrochen!") {
    console.error("Fehler beim schrittweisen Ausführen des Programms", response);
  }
  return response;
};

/**
 * Checks the current execution status of the program code on the server.
 *
 * <p> This function sends a request to the `/status` endpoint on the server to determine whether
 * the code is still running, has completed execution, or if an error occurred during the process.</p>
 *
 * @param sessionId Optional session identifier when the backend supports multiple sessions.
 *
 * @returns `"running"` – if the code is currently being executed,
 *          `"completed"` – if the code has finished executing,
 *          `"paused"` / `"stepping"` – if the backend supports and reports step mode,
 *          `"error"` – if a server error occurred or an unexpected response was received.
 */
export const call_code_status_route = async (sessionId?: string): Promise<ExecutionStatus> => {
  const status_response = await apiClient.get("status", false, undefined, sessionId ? {sessionId: sessionId} : undefined);
  const response = await handleResponse(status_response);
  if (response.error !== "" && response.error !== "Programm unterbrochen!") {
    console.error("Fehler beim Ausführen des Programms", response);
    return "error";
  }

  const status = response.data.trim();

  if (status === "running" || status === "completed" || status === "paused" || status === "stepping") {
    return status;
  }

  console.warn("Unerwartete Antwort vom Server:", status);
  return "error";
}
