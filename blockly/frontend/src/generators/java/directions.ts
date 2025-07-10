import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function direction_up(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"up\"", Order.ATOMIC];
}

export function direction_down(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"down\"", Order.ATOMIC];
}

export function direction_left(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"left\"", Order.ATOMIC];
}

export function direction_right(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"right\"", Order.ATOMIC];
}

export function direction_here(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"none\"", Order.ATOMIC];
}

