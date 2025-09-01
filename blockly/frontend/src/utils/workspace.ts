import * as Blockly from "blockly";
import {javaGenerator} from "../generators/java.ts";
import {sleep} from "./utils.ts";
import {
  call_clear_route, call_level_route,
  call_reset_route,
  call_code_route,
  call_code_status_route
} from "../api/api.ts";
import {completeLevel, getCurrentLevel} from "./level.ts";
let startBlock: Blockly.Block | null = null;
export let currentBlock: Blockly.Block | null = null;

export const getStartBlock = (workspace: Blockly.Workspace) => {
  if (startBlock !== null && !startBlock.isDeadOrDying()) {
    return startBlock;
  }

  const allBlocks = workspace.getAllBlocks();
  for (let i = 0; i < allBlocks.length; i++) {
    const block = allBlocks[i];
    if (block.type == "start" && !block.isDeadOrDying()) {
      startBlock = block;
      return block;
    }
  }
  return null;
}

export const getAllBlocksFromToolboxDefinition = (toolbox: unknown) => {
  const blocks: FlyoutItem[] = [];
  // @ts-expect-error We don't know the correct type of toolbox
  for (const content of (toolbox.contents as FlyoutItem[])) {
    if (content.kind === "category") {
      blocks.push(...getAllBlocksFromCategory(content));
    } else if (content.kind === "block") {
      blocks.push(content);
    }
  }
  return blocks;
}

export const getAllCategoriesFromToolboxDefinition = (toolbox: unknown) => {
  const categories: FlyoutItem[] = [];
  // @ts-expect-error We don't know the correct type of toolbox
  for (const content of (toolbox.contents as FlyoutItem[])) {
    if (content.kind === "category") {
      categories.push(content);
      categories.push(...getAllCategoriesFromToolboxDefinition(content));
    }
  }
  return categories;
}

interface FlyoutItem {
  kind: string;
  name: string;
  type: string;
  contents?: FlyoutItem[];
  disabledReasons?: string[];
  hidden?: string;
}

export const resetBlocksAndCategories = (allBlocks: FlyoutItem[], allCategories: FlyoutItem[], blockReason:string)=> {
  // Enable all blocks
  allBlocks.forEach((block) => {
    block["disabledReasons"] = (block["disabledReasons"] || []).filter((reason: string) => reason !== blockReason);
  });

  // Enable all categories
  allCategories.forEach((category) => {
    category["disabledReasons"] = (category["disabledReasons"] || []).filter((reason: string) => reason !== blockReason);
    if (category["disabledReasons"]?.length === 0) {
      category["hidden"] = "false"
    }
  });
}

export const blockElementsFromToolbox = (toolbox: unknown, blockedElements: string[], reason:string)=> {
  const allBlocks = getAllBlocksFromToolboxDefinition(toolbox);
  const allCategories = getAllCategoriesFromToolboxDefinition(toolbox);

  // Reset all blocks and categories to enabled state
  resetBlocksAndCategories(allBlocks, allCategories, reason);

  if (blockedElements.length === 0) return;
  // Create regex to match blocked elements
  const blockedItemsRegex = new RegExp(`^${blockedElements.join("|")}$`);

  // Disable blocked categories
  const blockedCategories = allCategories.filter(
    (category) => blockedItemsRegex.test(category.name)
  );
  blockedCategories.forEach((category) => {
    category["hidden"] = "true";
    category["disabledReasons"] = [
      ...(category["disabledReasons"] || []),
      reason
    ];
  });

  // Disable blocked blocks
  const blockedBlocks = allBlocks.filter(
    block => blockedItemsRegex.test(block.type)
  );
  blockedBlocks.forEach((blockedBlock) => {
    blockedBlock["disabledReasons"] = [
      ...(blockedBlock["disabledReasons"] || []),
      reason
    ];
  });
}

const getAllBlocksFromCategory = (category: unknown) => {
  const blocks: FlyoutItem[] = [];
  // @ts-expect-error We don't know the correct type of category
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

/**
 * Display an error message on a block
 * @param block The block to display the error on
 * @param error The error message to display
 */
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
  resetBtn: HTMLButtonElement;
}

/**
 * Set up the buttons for the workspace
 *
 * <p> Each button has its own function that is called when the button is clicked.
 * The start button starts the program and the reset button resets the program.
 *
 * <p> The buttons are disabled while the program is running to prevent multiple clicks.
 * The reset button is always enabled.
 *
 * @param workspace The workspace to set up the buttons for
 */
export const setupButtons = (workspace: Blockly.WorkspaceSvg) => {
  const buttons: Buttons = {
    startBtn: document.getElementById("startBtn") as HTMLButtonElement,
    resetBtn: document.getElementById("resetBtn") as HTMLButtonElement
  };
  const delayInput = document.getElementById("delay") as HTMLInputElement;

  if (buttons.startBtn === null || buttons.resetBtn === null) {
    throw new Error("Buttons not found");
  }
  if (delayInput === null) {
    throw new Error("Delay input not found");
  }

  setupStartButton(buttons, workspace, delayInput);
  setupResetButton(buttons, workspace);

  return buttons;
}

const setupStartButton = (buttons: Buttons, workspace: Blockly.WorkspaceSvg, delayInput: HTMLInputElement) => {
  buttons.startBtn.addEventListener("click", async () => {
    buttons.startBtn.disabled = true;

    buttons.resetBtn.click();
    //workaround race condition #1887
    await sleep(0.5);
    let sleepingTimeStr = delayInput.value;
    if (sleepingTimeStr === "") {
      sleepingTimeStr = "1";
    }
    const sleepingTime = Number(sleepingTimeStr);
    if (isNaN(sleepingTime)) {
      // Enable button again
      buttons.startBtn.disabled = false;
      throw new Error("Die konfigurierte Verzögerung muss eine Zahl sein");
    }

    workspace.highlightBlock(null);
    currentBlock = getStartBlock(workspace);

    // collect all code snippets
    const codeSnippets: string[] = [];
    while (currentBlock !== null) {
      // Highlight current block
      workspace.highlightBlock(currentBlock.id);

      // Skip the start block itself
      if (currentBlock.type !== "start") {
        const snippet = javaGenerator.blockToCode(currentBlock, true) as string;
        codeSnippets.push(snippet.trim());
      }

      currentBlock = currentBlock.getNextBlock();
    }

    // send the full program in a single request
    const fullProgram = codeSnippets.join("\n");

    const apiResponse = await call_code_route(fullProgram);
    if (!apiResponse) {
      await call_clear_route();
      workspace.highlightBlock(null);
      buttons.startBtn.disabled = false;
      return;
    }

    //wait for the full programm code to run
    let status = await call_code_status_route();
    let codeRunning = true;
    while (codeRunning) {
      if (status === "running") {
        console.log("code still running");
        await sleep(sleepingTime);
      } else if (status === "completed") {
        console.log("code execution completed");
        codeRunning = false;
      } else if (status === "error"){
        console.error("Error while running code: " + codeRunning);
        codeRunning = false;
      }

      status = await call_code_status_route();
    }

    // Check if we reach next level
    await sleep(1);
    const levelResponse = await call_level_route();
    if (getCurrentLevel() !== levelResponse.name) {
      completeLevel();
    }


    workspace.highlightBlock(null);
    // Enable button again
    buttons.startBtn.disabled = false;
  });
}

const setupResetButton = (buttons: Buttons, workspace: Blockly.WorkspaceSvg) => {
  buttons.resetBtn.addEventListener("click", async () => {
    clearAllWarnings(workspace);
    // Reset code highlighting
    workspace.highlightBlock(null);
    // Reset currentBlock for step button
    currentBlock = getStartBlock(workspace);
    await call_reset_route();
  });
}

/**
 * Place the default start block in the workspace
 *
 * <p> This function creates a new block of type "start" and places it in the workspace.
 * The block is not deletable and is rendered immediately. Also, the currentBlock variable is set to the new block.
 *
 * @param workspace The workspace to place the block in
 */
export const placeDefaultStartBlock = (workspace: Blockly.WorkspaceSvg) => {
  const startBlock = workspace.newBlock("start");
  startBlock.initSvg();
  startBlock.render();
  startBlock.setDeletable(false);
  currentBlock = startBlock;
}
