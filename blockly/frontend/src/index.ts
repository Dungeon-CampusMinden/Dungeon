import * as Blockly from "blockly";
import * as De from "blockly/msg/de";
import {blocks} from "./blocks/dungeon.ts";
import {javaGenerator} from "./generators/java.ts";
import {load, save} from "./serialization.ts";
import {toolbox} from "./toolbox.ts";
import {config} from "./config.ts";
import "./style.css";
import * as LimitUtils from "./utils/limits.ts";
import {
  blockElementsFromToolbox,
  getStartBlock,
  placeDefaultStartBlock,
  setupButtons
} from "./utils/workspace.ts";
import {getCurrentLevel, LevelChangedEvent, setupLevelSelector, updateLevelList} from "./utils/level.ts";

Blockly.setLocale(De as any); // eslint-disable-line @typescript-eslint/no-explicit-any

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

if (!workspace) {
  throw new Error("No workspace available");
}

setupButtons(workspace);

// Disable all blocks that aren't connected to the start block.
workspace.addChangeListener(Blockly.Events.disableOrphans);

// Disable the toolbox flyout auto-close
const toolboxFlyout = workspace.getToolbox()?.getFlyout();
if (toolboxFlyout) {
  toolboxFlyout.autoClose = false;
}

// Level Selector
const levelSelector = setupLevelSelector();
levelSelector.addEventListener("levelChanged", (event) => {
  const levelChangedEvent = (event as CustomEvent).detail as LevelChangedEvent;

  // Save current workspace state with old level name
  save(workspace, levelChangedEvent.oldLevelName);

  // Configure blocks and categories based on level restrictions
  blockElementsFromToolbox(toolbox, levelChangedEvent.blockedElements, "Not available in Level");

  // Load new level and initialize workspace
  load(workspace, levelChangedEvent.newLevelName);
  updateCodeDiv();
  workspace.scrollCenter();

  // Check for start block and add if missing
  const startBlock = getStartBlock(workspace);
  if (!startBlock) {
    placeDefaultStartBlock(workspace);
  }

  // Update toolbox
  workspace.updateToolbox(toolbox);
  workspace.getToolbox()?.refreshSelection();
});

// This function resets the code and output divs, shows the
// generated code from the workspace, and evals the code.
// In a real application, you probably shouldn't use `eval`.
let code = "";
const updateCodeDiv = () => {
  code = javaGenerator.workspaceToCode(Blockly.getMainWorkspace());
  if (codeDiv) {
    codeDiv.textContent = code;
  }
};
updateCodeDiv();

// Every time the workspace changes state, save the changes to storage.
workspace.addChangeListener((e: Blockly.Events.Abstract) => {
  // UI events are things like scrolling, zooming, etc.
  // No need to save after one of these.
  if (e.isUiEvent) return;
  save(workspace, getCurrentLevel());
});

// Whenever the workspace changes meaningfully, run the code again.
workspace.addChangeListener((e: Blockly.Events.Abstract) => {
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
  updateCodeDiv();
});

// Limiting Blocks Logic
workspace.addChangeListener((e: Blockly.Events.Abstract) => {
  let update = false;
  if (e.type == Blockly.Events.BLOCK_CREATE) {
    const newBlockId = (e as Blockly.Events.BlockCreate).blockId;
    if (newBlockId === undefined) return;
    const newBlock = workspace.getBlockById(newBlockId);
    if (newBlock === null) return;
    LimitUtils.registerBlockAdded(newBlock as unknown as Blockly.serialization.blocks.State);
    update = true;
  } else if (e.type == Blockly.Events.BLOCK_DELETE) {
    const oldBlockState = (e as Blockly.Events.BlockDelete).oldJson;
    if (oldBlockState === undefined) return;
    LimitUtils.registerBlockRemoved(oldBlockState);
    update = true;
  }
  // Enable/disable affected blocks
  if (update) {
    LimitUtils.refreshToolbox(workspace);
    workspace.getToolbox()?.refreshSelection();
  }
});

// Prevent restoring multiple start blocks
workspace.addChangeListener(async (e: Blockly.Events.Abstract) => {
  if (e.type !== Blockly.Events.BLOCK_CREATE) return;

  const startBlock = getStartBlock(workspace);
  if (!startBlock) return; // no start block, so we allow any block to be created

  const newStartBlocks = (e as Blockly.Events.BlockCreate).ids
    ?.map(id => workspace.getBlockById(id))
    .filter(block => block?.type === "start") ?? [];

  if (newStartBlocks.length === 0) return;

  const extraStartBlocks = newStartBlocks.filter(block => block?.id !== startBlock.id);
  if (extraStartBlocks.length > 1) { // More than one extra start block, prevent adding at all (should never happen)
    extraStartBlocks.forEach(block => block?.dispose());
    return;
  }

  if (extraStartBlocks.length === 1) { // delete the old start block, so we can restore the new one
    workspace.removeBlockById(startBlock.id);
    startBlock.dispose();
  }
});

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

updateLevelList().then(() => {
  load(workspace, getCurrentLevel()); // load initial state if available
  updateCodeDiv();
});

placeDefaultStartBlock(workspace);
workspace.scrollCenter(); // centering workspace
