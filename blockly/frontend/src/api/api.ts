import {config} from "../config.ts";

interface ApiResponse {
  data: string;
  error: string;
}

class Api {
  public async post(endpoint: string, text: string = "", ignoreError:boolean = false): Promise<Response> {
    const url = new URL(config.API_URL + endpoint);

    try {
      return await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "text/plain",
        },
        body: text,
      });
    } catch (error) {
      if (!ignoreError) {
        alert("Fehler beim Senden der Anfrage");
        console.error("Fehler beim Senden der Anfrage", error);
      }
      return new Response(null, { status: 500 });
    }
  }
}

const api = new Api();

/**
 * Handle the response from the server
 *
 * This function checks if the response from the server was ok or
 * the program got interrupted. If the response was not ok, an
 * error message is displayed on the current block.
 *
 * @param response The response from the server
 * @returns true if the response was ok, false otherwise
 */
const handleResponse = async (response: Response | null): Promise<ApiResponse> => {
  if (response == null || !response.ok) {
    const errorMessage = await response?.text() || "Fehler beim Abrufen der Antwort vom Server";
    return {
      data: "",
      error: errorMessage,
    }
  }
  // Status 205 means program was interrupted
  if (response.status === 205) {
    console.info("Programm unterbrochen!");
    return {
      data: "",
      error: "Programm unterbrochen!",
    }
  }
  return {
    data: await response.text(),
    error: "",
  }
}

/**
 * Call the reset route of the server
 *
 * <p> This route is used to reset the game (current level)
 *
 * @returns true if the game was successfully reset, false otherwise
 */
export const call_reset_route = async (): Promise<boolean> => {
  const reset_response = await api.post("reset");
  const response = await handleResponse(reset_response);
  if (response.error !== "") {
    console.error("Fehler beim Zurücksetzen des Spiels", response);
  }

  return response.error === "";
}

/**
 * Call the clear route of the server
 *
 * <p> This route is used to reset all variables and values
 *
 * @returns true if the values were successfully cleared, false otherwise
 */
export const call_clear_route = async () => {
  const clear_response = await api.post("clear");
  if (!clear_response.ok) {
    console.error("Fehler beim Zurücksetzen der Werte", clear_response);
  }
  return clear_response.ok
}

/**
 * Call the level route of the server
 *
 * <p> This route is used to get all available levels from the server.
 *
 * @Returns A list of all levels.
 */
export const call_levels_route = async () => {
  const level_response = await api.post("levels", "", true);
  const response = await handleResponse(level_response);
  if (response.error !== "") {
    console.error("Fehler beim Abrufen der Level", response);
    return [];
  }
  return response.data.split("\n");
}

interface Level {
  name: string;
  block_blocks?: string[];
}

/**
 * Call the level route of the server
 *
 * <p> This route is used to get the level from the server and the block blocks for blockly.
 *
 * @param levelName If given, the Server will change the level to this level, before returning the level.
 *
 * @Returns The current level or the level list, with the block blocks
  */
export const call_level_route = async (levelName = ""): Promise<Level> => {
  const url = "level" + (levelName ? `?levelName=${levelName}` : "");
  const level_response = await api.post(url, "", true);
  const response = await handleResponse(level_response);
  if (response.error !== "") {
    console.error("Fehler beim Abrufen des Levels", response);
    return {
      name: "",
      block_blocks: [],
    }
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
 * <p> This route is used to send the full program code to the server
 *
 * @param code The full program code to send to the server
 *
 * @returns true if the program was successfully started, false otherwise
 */
export const call_code_route = async (code: string): Promise<boolean> => {
  console.log("Call Code route: ", code);
  const code_response = await api.post(`code`, code);
  const response = await handleResponse(code_response);
  if (response.error !== "" && response.error !== "Programm unterbrochen!") {
    console.error("Fehler beim Ausführen des Programms", response);
  }
  return response.error === "";
};

/**
 * Checks the current execution status of the program code on the server.
 *
 * <p> This function sends a request to the `/status` endpoint on the server to determine whether
 * the code is still running, has completed execution, or if an error occurred during the process.
 *
 * @returns `"running"` – if the code is currently being executed,
 *          `"completed"` – if the code has finished executing,
 *          `"error"` – if a server error occurred or an unexpected response was received.
 */
export const call_code_status_route = async (): Promise<"running" | "completed" | "error"> => {
  const status_response = await api.post("status");
  const response = await handleResponse(status_response);
  if (response.error !== "" && response.error !== "Programm unterbrochen!") {
    console.error("Fehler beim Ausführen des Programms", response);
    return "error";
  }

  const status = response.data.trim();

  if (status === "running" || status === "completed") {
    return status;
  }

  console.warn("Unerwartete Antwort vom Server:", status);
  return "error";
}
