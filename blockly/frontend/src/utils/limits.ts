import * as Blockly from "blockly";
import {config} from "../config.ts";
import {toolbox} from "../toolbox.ts";

const blockCounts: Record<string, number> = {};

/**
 * Increases the count for a newly added block type.
 * @param block - The Blockly block that was added.
 */
function addBlock(block: Blockly.serialization.blocks.State): void {
  const type = block.type;
  blockCounts[type] = (blockCounts[type] ?? 0) + 1;
}

/**
 * Decreases the count for a removed block type.
 * @param block - The Blockly block state that was removed.
 */
function removeBlock(block: Blockly.serialization.blocks.State): void {
  const type = block.type;
  if (type in blockCounts) {
    blockCounts[type]--;
  }
}

/**
 * Recursively retrieves all sub-blocks of a given block.
 * @param block - The Blockly block to retrieve sub-blocks from.
 * @returns An array of all sub-blocks.
 */
function getAllSubBlocks(block: Blockly.serialization.blocks.State) {
  const blocks = [block];
  if (block.inputs !== undefined) {
    for (const inputBlock of Object.values(block.inputs)) {
      blocks.push(...getAllSubBlocks(inputBlock.block!));
    }
  }
  if (block.next !== undefined) {
    blocks.push(...getAllSubBlocks(block.next.block!));
  }

  return blocks;
}

/**
 * Increases the count for a newly added block type.
 * @param block - The Blockly block that was added.
 */
export function registerBlockAdded(block: Blockly.serialization.blocks.State): void {
  getAllSubBlocks(block).forEach(addBlock);
}

/**
 * Decreases the count for a removed block type.
 * @param block - The Blockly block state that was removed.
 */
export function registerBlockRemoved(block: Blockly.serialization.blocks.State): void {
  getAllSubBlocks(block).forEach(removeBlock);
}

/**
 * Retrieves a list of limit rules that have been reached based on the current block counts.
 * @returns An array of objects containing the rule name and its corresponding regex.
 */
function getReachedLimitRules(): { rule: string; regex: RegExp }[] {
  return Object.keys(config.LIMITS).map(rule => ({
    rule,
    regex: new RegExp(`^${rule}$`)
  }));
}

/**
 * Extracts all block items from the toolbox, including nested categories.
 * @param items - The toolbox item information array.
 * @returns An array of block information objects.
 */
function extractBlockItems(items: Blockly.utils.toolbox.ToolboxItemInfo[]): Blockly.utils.toolbox.BlockInfo[] {
  const result: Blockly.utils.toolbox.BlockInfo[] = [];
  const stack: Blockly.utils.toolbox.ToolboxItemInfo[] = [...items];

  while (stack.length) {
    const item = stack.pop()! as Blockly.utils.toolbox.StaticCategoryInfo;
    if (item.kind === "block") {
      result.push(item);
    } else if (item.kind === "category" && item.contents) {
      stack.push(...item.contents);
    }
  }

  return result;
}

/**
 * Updates the enabled state of each block item based on the reached limit rules.
 * @param blocks - The array of block information objects to update.
 */
function applyDisablingToBlocks(blocks: Blockly.utils.toolbox.BlockInfo[]): void {
  const compiledRules = getReachedLimitRules().filter(({rule, regex}) => {
    for (const type in blockCounts) {
      if (regex.test(type) && blockCounts[type] >= config.LIMITS[rule]) {
        return true;
      }
    }
    return false;
  });

  for (const blockItem of blocks) {
    blockItem.enabled = true;
    delete blockItem.disabledReasons;
    for (const {rule, regex} of compiledRules) {
      if (regex.test(blockItem.type!)) {
        blockItem.enabled = false;
        blockItem.disabledReasons = [`Limit ${rule} reached`];
        break;
      }
    }
  }
}

/**
 * Refreshes the toolbox in the given workspace based on the current block counts and reached limits.
 * @param workspace - The Blockly workspace where the toolbox should be updated.
 */
export function refreshToolbox(workspace: Blockly.WorkspaceSvg): void {
  const toolboxInfo = toolbox as Blockly.utils.toolbox.ToolboxInfo;
  const blockItems = extractBlockItems(toolboxInfo.contents);
  applyDisablingToBlocks(blockItems);
  workspace.updateToolbox(toolbox);
}
