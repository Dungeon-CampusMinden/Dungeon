import { config } from "../config.ts";

export class Api {
  public async post(endpoint: string, text: string = ""): Promise<Response> {
    const url = new URL(config.API_URL + endpoint);

    try {
      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "text/plain",
        },
        body: text,
      });
      return response;
    } catch (error) {
      alert("Fehler beim Senden der Anfrage");
      console.error("Fehler beim Senden der Anfrage", error);
      return new Response(null, { status: 500 });
    }
  }
}
