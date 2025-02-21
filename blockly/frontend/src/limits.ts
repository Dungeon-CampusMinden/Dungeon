import * as Blockly from "blockly";
import {config} from "./config.ts";
import {toolbox} from "./toolbox.ts";

const counts: Record<string, number> = {};

export function onNewBlock(block: Blockly.Block) {
  const blockType = block.type;
  if (blockType in counts) {
    counts[blockType]++;
  } else {
    counts[blockType] = 1;
  }
}

export function onDeleteBlock(block: Blockly.serialization.blocks.State) {
  const blockType = block.type;
  if (blockType in counts) {
    counts[blockType]--;
  }
}

function getAllReachedLimitRules() {
  const rules: string[] = [];
  for (const key in config.LIMITS) {
    const regex = new RegExp(`^${key}$`);
    for (const blockType in counts) {
      if (regex.test(blockType) && counts[blockType] >= config.LIMITS[key]) {
        rules.push(key);
      }
    }
  }
  return rules;
}


export function updateToolbox(workspace: Blockly.WorkspaceSvg) {
  const allReachedLimitRules = getAllReachedLimitRules();

  // get alls blocks in toolbox
  const blocks: Blockly.utils.toolbox.BlockInfo[] = [];
  // checking every category/subcategory etc.
  const queue: Blockly.utils.toolbox.ToolboxItemInfo[] = Array.from((toolbox as Blockly.utils.toolbox.ToolboxInfo).contents);
  while (queue.length > 0) {
    const item = queue.shift() as Blockly.utils.toolbox.StaticCategoryInfo;
    if (item === undefined) continue;
    if (item.kind === "category") {
      queue.push(...(item.contents));
    } else if (item.kind === "block") {
      blocks.push(item);
    }
  }

  // disable blocks if limit is reached
  for (const item of blocks) {
    item.enabled = true;
    delete item.disabledReasons
    for (const rule of allReachedLimitRules) {
      const regex = new RegExp(`^${rule}$`);
      item.enabled = !regex.test(item.type!);
      if (!item.enabled) {
        item.disabledReasons = [`Limit ${rule} reached`];
        break;
      }
    }
  }

  workspace.updateToolbox(toolbox);
}
