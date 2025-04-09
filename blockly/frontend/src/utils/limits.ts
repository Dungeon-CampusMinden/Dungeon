import * as Blockly from "blockly";
import {config} from "../config.ts";
import {toolbox} from "../toolbox.ts";
import {blockElementsFromToolbox, getAllBlocksFromToolboxDefinition} from "./workspace.ts";

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

function getElementsToDisable(blockList: Blockly.utils.toolbox.BlockInfo[]): string[] {
  const compiledRules = getReachedLimitRules().filter(({ rule, regex }) =>
    Object.entries(blockCounts).some(([type, count]) => regex.test(type) && count >= config.LIMITS[rule])
  );

  return blockList
    .filter(blockItem => compiledRules.some(({ regex }) => regex.test(blockItem.type!)))
    .map(blockItem => blockItem.type!);
}

/**
 * Refreshes the toolbox in the given workspace based on the current block counts and reached limits.
 * @param workspace - The Blockly workspace where the toolbox should be updated.
 */
export function refreshToolbox(workspace: Blockly.WorkspaceSvg): void {
  const allBlocks = getAllBlocksFromToolboxDefinition(toolbox)
  blockElementsFromToolbox(toolbox, getElementsToDisable(allBlocks), "Limit Reached");
  workspace.updateToolbox(toolbox);
}
