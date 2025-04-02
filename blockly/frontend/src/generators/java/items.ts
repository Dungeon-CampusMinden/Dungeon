import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function item_breadcrumbs(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"Brotkrumen\"", Order.ATOMIC];
}

export function item_clover(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return ["\"Kleeblatt\"", Order.ATOMIC];
}


