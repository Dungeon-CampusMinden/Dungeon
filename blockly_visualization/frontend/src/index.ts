import * as Blockly from "blockly";
import * as De from "blockly/msg/de";
import { blocks } from "./blocks/dungeon.ts";
import { javaGenerator } from "./generators/java.ts";
import { save, load } from "./serialization.ts";
import { toolbox } from "./toolbox.ts";
import { Api } from "./api/api.ts";
import { config } from "./config.ts";
import "./style.css";
import { Character } from "./character/character.ts";

Blockly.setLocale(De);

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
  });
const api = new Api();
const startBtn = document.getElementById("startBtn") as HTMLButtonElement;
const delay = document.getElementById("delay");
const stepBtn = document.getElementById("stepBtn") as HTMLButtonElement;
const resetBtn = document.getElementById("resetBtn") as HTMLButtonElement;
const responseStatusDiv = document.getElementById("responseStatus");
const characterPositionDiv = document.getElementById("characterPosition");
const character = new Character();

// Disable all blocks that aren't connected to the start block.
if (workspace) {
  workspace.addChangeListener(Blockly.Events.disableOrphans);
  workspace.registerButtonCallback("createVariable", () => {
    Blockly.Variables.createVariableButtonHandler(workspace);
  });
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

function isNumber(str) {
  if (typeof str != "string") return false // we only process strings!
  // could also coerce to string: str = ""+str
  return !isNaN(str) && !isNaN(parseFloat(str))
}

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));
if (startBtn) {
  startBtn.addEventListener("click", async () => {
    var sleepingTime = delay.value;
    console.log(sleepingTime);
    if (sleepingTime === "") {
      sleepingTime = "1";
    }
    if (!isNumber(sleepingTime)) {
      alert("Die konfigurierte Verzögerung muss eine Zahl sein");
      return;
    }
    startBtn.disabled = true;
    workspace.highlightBlock(null);
    var startBlock = getStartBlock();
    var currentBlock = startBlock;
    while (currentBlock !== null) {
      // Highlight current block
      if (currentBlock) {
          workspace.highlightBlock(currentBlock.id);
      }
      // Do nothing except highlighting on start block
      if (currentBlock.type !== "start") {
        // Disable button

        // Get code of the current block
        var currentCode = javaGenerator.blockToCode(currentBlock, true);
        try {
          const response = await api.post("start", currentCode);
          const status = response.status;
          const text = await response.text();
          if (text) {
            character.setPosition(text);
            characterPositionDiv!.textContent = `Character position: x=${
              character.getPosition()?.x
            }, y=${character.getPosition()?.y}`;
          }
          responseStatusDiv!.textContent = `HTTP response status: ${status.toString()}`;
          console.log(status.toString());
        } catch (error) {
          if (error instanceof Error) {
            responseStatusDiv!.textContent = `HTTP response status: ${error.message}`;
          }
        } finally {

        }
      }
      // Get next block
      currentBlock = currentBlock.getNextBlock();
      await sleep(sleepingTime * 1000);
    }
    workspace.highlightBlock(null);
    // Enable button again
    startBtn.disabled = false;
  });
}

function getStartBlock() {
  var allBlocks = workspace.getAllBlocks();
  for (var i = 0; i < allBlocks.length; i++) {
      var block = allBlocks[i];
      if (block.type == "start") {
        return block;
      }
  }
  return null;
}

var startBlock = getStartBlock();
var currentBlock = startBlock;

if (stepBtn) {
  workspace.highlightBlock(null);
  stepBtn.addEventListener("click", async () => {
      if (currentBlock === null) {
        alert("Alle Schritte ausgeführt.");
        workspace.highlightBlock(null);
        currentBlock = startBlock;
        return;
      }
      // Highlight current block
      if (currentBlock) {
          workspace.highlightBlock(currentBlock.id);
      }
      // Do nothing except highlighting on start block
      if (currentBlock.type !== "start") {
        // Disable button
        stepBtn.disabled = true;
        // Get code of the current block
        var currentCode = javaGenerator.blockToCode(currentBlock, true);
        try {
          const response = await api.post("start", currentCode);
          const status = response.status;
          const text = await response.text();
          if (text) {
            character.setPosition(text);
            characterPositionDiv!.textContent = `Character position: x=${
              character.getPosition()?.x
            }, y=${character.getPosition()?.y}`;
          }
          responseStatusDiv!.textContent = `HTTP response status: ${status.toString()}`;
          console.log(status.toString());
        } catch (error) {
          if (error instanceof Error) {
            responseStatusDiv!.textContent = `HTTP response status: ${error.message}`;
          }
        } finally {
          // Enable button again
          stepBtn.disabled = false;
        }
      }
      // Get next block
      currentBlock = currentBlock.getNextBlock();
  });
}

if (resetBtn) {
  resetBtn.addEventListener("click", async () => {
    // Reset code highlighting
    workspace.highlightBlock(null);
    // Reset currentBlock for step button
    currentBlock = startBlock;
    try {
      const response = await api.post("reset");
      const status = response.status;
      const text = await response.text();
      if (text) {
        character.setPosition(text);
        characterPositionDiv!.textContent = `Character position: x=${
          character.getPosition()?.x
        }, y=${character.getPosition()?.y}`;
      }
      responseStatusDiv!.textContent = `HTTP response status: ${status.toString()}`;
    } catch (error) {
      if (error instanceof Error) {
        responseStatusDiv!.textContent = `HTTP response status: ${error.message}`;
      }
    }
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