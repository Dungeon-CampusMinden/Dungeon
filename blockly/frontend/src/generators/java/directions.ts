import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function direction_up(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["Direction.UP", Order.ATOMIC];
}

export function direction_down(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["Direction.DOWN", Order.ATOMIC];
}

export function direction_left(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["Direction.LEFT", Order.ATOMIC];
}

export function direction_right(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["Direction.RIGHT", Order.ATOMIC];
}

export function direction_here(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["Direction.NONE", Order.ATOMIC];
}

