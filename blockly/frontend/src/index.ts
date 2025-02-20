import * as Blockly from "blockly";
import * as De from "blockly/msg/de";
import { blocks } from "./blocks/dungeon.ts";
import { javaGenerator } from "./generators/java.ts";
import { save, load } from "./serialization.ts";
import { toolbox } from "./toolbox.ts";
import { Api } from "./api/api.ts";
import { config } from "./config.ts";
import "./style.css";

Blockly.setLocale(De as any);

// Register the blocks and generator with Blockly
Blockly.common.defineBlocks(blocks);

// Set up UI elements and inject Blockly
const codeDiv = document.getElementById("generatedCode")?.firstChild;
const blocklyDiv = document.getElementById("blockly");
const workspace =
  blocklyDiv &&
  Blockly.inject(blocklyDiv, {
    toolbox: toolbox,
    trashcan: true,
    zoom: {
      controls: true,
      wheel: true,
      startScale: 1.0,
      maxScale: 3,
      minScale: 0.3,
      scaleSpeed: 1.2,
    },
  });
const api = new Api();
const startBtn = document.getElementById("startBtn") as HTMLButtonElement;
const delay = document.getElementById("delay") as HTMLInputElement;
const stepBtn = document.getElementById("stepBtn") as HTMLButtonElement;
const resetBtn = document.getElementById("resetBtn") as HTMLButtonElement;

// Disable all blocks that aren't connected to the start block.
if (workspace !== null) {
  workspace.addChangeListener(Blockly.Events.disableOrphans);
  workspace.registerButtonCallback("createVariable", () => {
    Blockly.Variables.createVariableButtonHandler(workspace);
  });
} else {
  throw new Error("No workspace available");
}


// This function resets the code and output divs, shows the
// generated code from the workspace, and evals the code.
// In a real application, you probably shouldn't use `eval`.
let code = "";
const runCode = () => {
  code = javaGenerator.workspaceToCode(Blockly.getMainWorkspace());
  if (codeDiv) {
    codeDiv.textContent = code;
  }
};

if (workspace) {
  // Load the initial state from storage and run the code.
  load(workspace);
  runCode();

  // Every time the workspace changes state, save the changes to storage.
  // @ts-ignore
  workspace.addChangeListener((e: Blockly.Events.UiBase) => {
    // UI events are things like scrolling, zooming, etc.
    // No need to save after one of these.
    if (e.isUiEvent) return;
    save(workspace);
  });

  // Whenever the workspace changes meaningfully, run the code again.
  // @ts-ignore
  workspace.addChangeListener((e: Blockly.Events.UiBase) => {
    // Don't run the code when the workspace finishes loading; we're
    // already running it once when the application starts.
    // Don't run the code during drags; we might have invalid state.
    if (
      e.isUiEvent ||
      e.type == Blockly.Events.FINISHED_LOADING ||
      workspace.isDragging()
    ) {
      return;
    }
    runCode();
  });
}

async function call_clear_route(){
  const clear_response = await api.post("clear");
  if (!clear_response.ok) {
    console.error("Fehler beim Zurücksetzen der Werte", clear_response);
  }
}

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));
if (startBtn) {
  startBtn.addEventListener("click", async () => {
    let sleepingTimeStr = delay.value;
    if (sleepingTimeStr === "") {
      sleepingTimeStr = "1";
    }
    const sleepingTime = Number(sleepingTimeStr);
    if (isNaN(sleepingTime)) {
      alert("Die konfigurierte Verzögerung muss eine Zahl sein");
      return;
    }

    startBtn.disabled = true;
    stepBtn.disabled = true;
    workspace.highlightBlock(null);
    let currentBlock = getStartBlock(workspace);
    while (currentBlock !== null) {
      // Highlight current block
      if (currentBlock) {
        workspace.highlightBlock(currentBlock.id);
      }
      // Do nothing except highlighting on start block
      if (currentBlock.type === "start") {
        currentBlock = currentBlock.getNextBlock();
        continue;
      }

      // Get code of the current block
      const currentCode = javaGenerator.blockToCode(currentBlock, true);

      const response = await api.post("start", currentCode as string);

      // Check if response was not ok
      if (!response.ok) {
        currentBlock = null;
        const text = await response.text();
        alert("Bei der Ausführung des Programms ist ein Fehler aufgetreten.\n" + text );
        continue;
      }
      // Status 205 means program was interrupted
      if (response.status === 205) {
        currentBlock = null;
        console.log("Programm unterbrochen!");
        continue;
      }

      // Get next block and sleep x seconds
      currentBlock = currentBlock.getNextBlock();
      await sleep(sleepingTime * 1000);
    }
    // Reset values in backend
    call_clear_route();

    workspace.highlightBlock(null);
    // Enable button again
    startBtn.disabled = false;
    stepBtn.disabled = false;
  });
}

function getStartBlock(workspace: Blockly.Workspace) {
  const allBlocks = workspace.getAllBlocks();
  for (let i = 0; i < allBlocks.length; i++) {
      const block = allBlocks[i];
      if (block.type == "start") {
        return block;
      }
  }
  return null;
}

const startBlock = getStartBlock(workspace);
let currentBlock = startBlock;

if (stepBtn !== null) {
  workspace.highlightBlock(null);
  stepBtn.addEventListener("click", async () => {
      if (currentBlock === null) {
        alert("Alle Schritte ausgeführt.");
        workspace.highlightBlock(null);
        currentBlock = startBlock;
        // Reset values in backend
        call_clear_route();
        return;
      }
      // Highlight current block
      if (currentBlock) {
          workspace.highlightBlock(currentBlock.id);
      }
      // Do nothing except highlighting on start block
      if (currentBlock.type === "start") {
        currentBlock = currentBlock.getNextBlock();
        return;
      }
      // Disable button
      stepBtn.disabled = true;
      startBtn.disabled = true;
      // Get code of the current block
      const currentCode = javaGenerator.blockToCode(currentBlock, true);
      // Send code to server
      const response = await api.post("start", currentCode as string);

      // Check if response was not ok
      if (!response.ok) {
        currentBlock = startBlock;
        workspace.highlightBlock(null);

        alert("Bei der Ausführung des Programms ist ein Fehler aufgetreten.");
        console.error("Fehler beim Ausführen des Codes", response);

        call_clear_route();
      }
      // Status 205 means program was interrupted
      if (response.status === 205) {
        currentBlock = startBlock;
        workspace.highlightBlock(null);
        console.log("Programm unterbrochen!");
        call_clear_route();
      }
      // Enable button again
      startBtn.disabled = false;
      stepBtn.disabled = false;

    // Get next block. Current block may be null if program was interrupted
    if (currentBlock) {
      currentBlock = currentBlock.getNextBlock();
    }
  });
}

if (resetBtn) {
  resetBtn.addEventListener("click", async () => {
    // Reset code highlighting
    workspace.highlightBlock(null);
    // Reset currentBlock for step button
    currentBlock = startBlock;
    await api.post("reset");
  });
}

if (config.HIDE_GENERATED_CODE) {
  const generatedCodeDiv = document.getElementById("generatedCode");
  if (generatedCodeDiv) {
    generatedCodeDiv.style.visibility = "hidden";
  }
}

if (config.HIDE_RESPONSE_INFO) {
  const responseDiv = document.getElementById("response");
  if (responseDiv) {
    responseDiv.style.visibility = "hidden";
  }
}

workspace.clear(); // clearing workspace
workspace.scrollCenter(); // centering workspace

if (workspace) { // placing default start block
  const startBlock = workspace.newBlock("start");
  startBlock.initSvg();
  startBlock.render();
  startBlock.setDeletable(false);
}