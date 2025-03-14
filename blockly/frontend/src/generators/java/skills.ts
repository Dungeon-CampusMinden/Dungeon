import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function interact(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "interagieren();";
}

export function fireball_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  return "feuerball(" + dir + ");";
}

export function wait(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "warte();";
}
