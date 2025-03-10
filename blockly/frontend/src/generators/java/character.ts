import * as Blockly from "blockly";
import { Order } from "../java.ts";

export function move(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);

  return "gehe(" + dir + ");";
}
