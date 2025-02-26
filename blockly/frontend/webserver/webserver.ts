import { serveFile } from "jsr:@std/http/file-server";

const BASE_DIR = "/content";
const PORT = 8080;

const handler = async (request: Request): Promise<Response> => {
    const url = new URL(request.url);
    let filepath = decodeURIComponent(url.pathname);
    if (filepath === "/") {
        filepath = "/index.html";
    }

    try {
        return await serveFile(request, `.${BASE_DIR + filepath}`);
    } catch {
        return new Response("404: Not Found", { status: 404 });
    }
};

const openLink = (url: string) => {
    const os = Deno.build.os;
    if (os === "windows") {
        return new Deno.Command("cmd", { args: ["/c", "start", url] }).output();
    } else if (os === "darwin") {
        return new Deno.Command("open", { args: [url] }).output();
    } else if (os === "linux") {
        return new Deno.Command("xdg-open", { args: [url] }).output();
    }
    console.error("Unsupported OS");
    return null;
};

const openJar = () => {
    const jarPath = `${Deno.cwd() + BASE_DIR}/blockly.jar`;
    const _blocklyClientProcess = new Deno.Command("java", {
        args: [
            "-jar",
             jarPath
        ],
    });
    _blocklyClientProcess.output().then(({ code }) => {
        Deno.exit(code);
    }).catch((err) => {
        console.error("Failed to start Blockly Client process", err);
    });
}

try {
    openJar();
} catch (err) {
    console.error("Failed to start Blockly Client process", err);
}
Deno.serve({ port: PORT, handler, onListen: () => {
    console.log(`Access the Blockly client at http://localhost:${PORT}`)
    // opening the link
    openLink(`http://localhost:${PORT}`);
} });
