import {config} from "../config.ts";
import {displayErrorOnBlock} from "../utils/workspace.ts";
import * as VariableListUtils from "../utils/variableList.ts";
import {Block} from "blockly";

interface ApiResponse {
  data: string;
  error: string;
}

class Api {
  public async post(endpoint: string, text: string = ""): Promise<Response> {
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
      alert("Fehler beim Senden der Anfrage");
      console.error("Fehler beim Senden der Anfrage", error);
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
const handleResponse = async (response: Response): Promise<ApiResponse> => {
  if (!response.ok) {
    const errorMessage = await response.text();
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
 * Call the start route of the server
 *
 * <p> This route is used to start the program and send the code to the server.
 *
 * @param code The code to send to the server
 * @param currentBlock The current block to highlight
 * @param first_step true if this is the first step, false otherwise. This is to check if were sending a new program or not.
 *
 * @returns true if the program was successfully started, false otherwise
 */
export const call_start_route = async (code: string, currentBlock: Block, first_step: boolean = true) => {
  const start_response = await api.post(`start?first=${first_step}`, code);
  const response = await handleResponse(start_response);
  if (response.error !== "" && response.error !== "Programm unterbrochen!") {
    displayErrorOnBlock(currentBlock, response.error);
  }
  return response.error === "";
}

/**
 * Call the variables route of the server
 *
 * <p> This route is used to get the variables from the server and update the variable list.
 *
 * @returns true if the variables were successfully retrieved, false otherwise
 */
export const call_variables_route = async (): Promise<boolean> => {
  const var_response = await api.post("variables");
  const response = await handleResponse(var_response);
  if (response.error !== "") {
    console.error("Fehler beim Abrufen der Variablen", response);
    return false;
  }
  const variableData = response.data;
  variableData.split("\n").forEach((line) => {
    const [name, value] = line.split("=");
    VariableListUtils.updateVariable(name, value);
  });
  return true;
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
}
