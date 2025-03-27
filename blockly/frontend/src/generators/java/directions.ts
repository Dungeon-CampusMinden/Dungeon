import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function direction_up(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"oben\"", Order.ATOMIC];
}

export function direction_down(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"unten\"", Order.ATOMIC];
}

export function direction_left(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"links\"", Order.ATOMIC];
}

export function direction_right(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"rechts\"", Order.ATOMIC];
}

export function direction_here(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"hier\"", Order.ATOMIC];
}

