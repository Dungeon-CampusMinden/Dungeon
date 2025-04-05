import * as Blockly from "blockly";
import {javaGenerator} from "../generators/java.ts";
import {sleep} from "./utils.ts";
import * as VariableListUtils from "./variableList.ts";
import {
  call_clear_route,
  call_reset_route,
  call_start_route,
  call_variables_route
} from "../api/api.ts";
import {FlyoutItem} from "blockly/core/flyout_base";

export let currentBlock: Blockly.Block | null = null;

export const getStartBlock = (workspace: Blockly.Workspace) => {
  const allBlocks = workspace.getAllBlocks();
  for (let i = 0; i < allBlocks.length; i++) {
    const block = allBlocks[i];
    if (block.type == "start") {
      return block;
    }
  }
  return null;
}

export const getAllBlocksFromToolboxDefinition = (toolbox: unknown) => {
  const blocks: FlyoutItem[] = [];
  // @ts-ignore We don't know the correct type of toolbox
  for (const content of (toolbox.contents as FlyoutItem[])) {
    if (content.kind === "category") {
      blocks.push(...getAllBlocksFromCategory(content));
    } else if (content.kind === "block") {
      blocks.push(content);
    }
  }
  return blocks;
}

const getAllBlocksFromCategory = (category: unknown) => {
  const blocks: FlyoutItem[] = [];
  // @ts-ignore We don't know the correct type of category
  for (const block of (category.contents as FlyoutItem[])) {
    if (block.kind === "block") {
      blocks.push(block);
    } else if (block.kind === "category") {
      blocks.push(...getAllBlocksFromCategory(block));
    }
  }
  return blocks;
}

/**
 * Clear all warnings from all blocks in the workspace
 * @param workspace The workspace to clear warnings from
 */
export const clearAllWarnings = (workspace: Blockly.Workspace) => {
  const allBlocks = workspace.getAllBlocks();
  for (let i = 0; i < allBlocks.length; i++) {
    const block = allBlocks[i];
    block.setWarningText(null);
  }
}

export const displayErrorOnBlock = (block: Blockly.Block | null, error: string) => {
  if (block === null) {
    console.error("Cannot display error on block: block is null");
    console.error("Error message: ", error);
    return;
  }

  const errorArray = error.split("Fehlermeldung: ");
  let errorText;
  if (errorArray.length > 1) {
    errorText = errorArray[1];
  } else {
    errorText = error;
  }
  block.setWarningText(errorText);
}

interface Buttons {
  startBtn: HTMLButtonElement;
  stepBtn: HTMLButtonElement;
  resetBtn: HTMLButtonElement;
}

/**
 * Set up the buttons for the workspace
 *
 * <p> Each button has its own function that is called when the button is clicked.
 * The start button starts the program, the step button executes one step and the reset button resets the program.
 *
 * <p> The buttons are disabled while the program is running to prevent multiple clicks.
 * The start button is disabled while the step button is enabled and vice versa.
 * The reset button is always enabled.
 *
 * @param workspace The workspace to set up the buttons for
 */
export const setupButtons = (workspace: Blockly.WorkspaceSvg) => {
  const buttons: Buttons = {
    startBtn: document.getElementById("startBtn") as HTMLButtonElement,
    stepBtn: document.getElementById("stepBtn") as HTMLButtonElement,
    resetBtn: document.getElementById("resetBtn") as HTMLButtonElement
  };
  const delayInput = document.getElementById("delay") as HTMLInputElement;

  if (buttons.startBtn === null || buttons.stepBtn === null || buttons.resetBtn === null) {
    throw new Error("Buttons not found");
  }
  if (delayInput === null) {
    throw new Error("Delay input not found");
  }

  setupStartButton(buttons, workspace, delayInput);
  setupStepButton(buttons, workspace);
  setupResetButton(buttons, workspace);

  return buttons;
}

const setupStartButton = (buttons: Buttons, workspace: Blockly.WorkspaceSvg, delayInput: HTMLInputElement) => {
  buttons.startBtn.addEventListener("click", async () => {
    buttons.resetBtn.click();

    let sleepingTimeStr = delayInput.value;
    if (sleepingTimeStr === "") {
      sleepingTimeStr = "1";
    }
    const sleepingTime = Number(sleepingTimeStr);
    if (isNaN(sleepingTime)) {
      throw new Error("Die konfigurierte Verzögerung muss eine Zahl sein");
    }

    buttons.startBtn.disabled = true;
    buttons.stepBtn.disabled = true;
    workspace.highlightBlock(null);
    currentBlock = getStartBlock(workspace);
    let first = true;
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

      const apiResponse = await call_start_route(currentCode as string, currentBlock, first);
      first = false;
      if (!apiResponse) break;

      await call_variables_route();

      // Get next block and sleep x seconds
      currentBlock = currentBlock.getNextBlock();
      await sleep(sleepingTime);
    }

    // Check if we reach next level
    await sleep(1);
    //await updateLevelList();

    // Reset values in backend
    await call_clear_route();

    workspace.highlightBlock(null);
    // Enable button again
    buttons.startBtn.disabled = false;
    buttons.stepBtn.disabled = false;
  });
}

const setupStepButton = (buttons: Buttons, workspace: Blockly.WorkspaceSvg) => {
  buttons.stepBtn.addEventListener("click", async () => {
      clearAllWarnings(workspace);

      if (currentBlock === null) {
        workspace.highlightBlock(null);
        currentBlock = getStartBlock(workspace)
        // Reset values in backend
        call_clear_route();
        return;
      }

      // Highlight current block
      workspace.highlightBlock(currentBlock.id);

      // Do nothing except highlighting on start block
      if (currentBlock.type === "start") {
        currentBlock = currentBlock.getNextBlock();
        return;
      }
      let first = currentBlock.getParent()?.type === "start";

      // Disable buttons
      buttons.stepBtn.disabled = true;
      buttons.startBtn.disabled = true;
      // Get code of the current block
      const currentCode = javaGenerator.blockToCode(currentBlock, true);
      // Send code to server
      const response = await call_start_route(currentCode as string, currentBlock, first);
      if (!response) {
        currentBlock = getStartBlock(workspace);
        await call_clear_route();
        workspace.highlightBlock(null);
      }

      await call_variables_route();

      // Enable button again
      buttons.startBtn.disabled = false;
      buttons.stepBtn.disabled = false;

      // Get next block. Current block may be null if program was interrupted
      if (currentBlock) {
        currentBlock = currentBlock.getNextBlock();
      }
    });
}

const setupResetButton = (buttons: Buttons, workspace: Blockly.WorkspaceSvg) => {
  buttons.resetBtn.addEventListener("click", async () => {
    clearAllWarnings(workspace);
    // Reset code highlighting
    workspace.highlightBlock(null);
    // Reset currentBlock for step button
    currentBlock = getStartBlock(workspace);
    VariableListUtils.resetVariables();
    await call_reset_route();
  });
}

export const placeDefaultStartBlock = (workspace: Blockly.WorkspaceSvg) => {
  const startBlock = workspace.newBlock("start");
  startBlock.initSvg();
  startBlock.render();
  startBlock.setDeletable(false);
  currentBlock = startBlock;
}
