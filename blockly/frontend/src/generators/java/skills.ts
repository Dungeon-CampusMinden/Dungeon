import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function interact(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "interagieren();";
}

export function fireball(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "feuerball();";
}

export function wait(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "warte();";
}

export function use(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "benutzen();";
}

export function push(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "schieben();";
}

export function pull(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "ziehen();";
}

export function pickup(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "aufsammeln();";
}

export function drop_item(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const item = generator.valueToCode(block, "ITEM", Order.NONE);
  return "fallen_lassen(" + item + ");";
}

