import { config } from "../config.ts";

export class Api {
  public async post(endpoint: string, text: string = ""): Promise<Response> {
    const url = new URL(config.API_URL + endpoint);

    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "text/plain",
      },
      body: text,
    });

    if (!response.ok) {
      throw new Error("HTTP error " + response.status);
    }

    return response;
  }
}
