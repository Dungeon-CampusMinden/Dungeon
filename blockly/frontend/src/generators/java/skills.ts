import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function interact(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "Hero.interact();";
}

export function fireball(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "Hero.shootFireball();";
}

export function wait(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "Hero.rest();";
}

export function use(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  return "Hero.interact(" + dir + ");";
}

export function push(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "Hero.push();";
}

export function pull(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "Hero.pull();";
}

export function pickup(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "Hero.pickup();";
}

export function drop_item(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const item = generator.valueToCode(block, "ITEM", Order.NONE);
  return "Hero.dropItem(" + item + ");";
}

