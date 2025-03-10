import * as Blockly from "blockly";
import { Order } from "../java.ts";

export function move(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const amount: number = Number(
    generator.valueToCode(block, "amount", Order.NONE)
  );
  const dir = block.getFieldValue("DIRECTION");
  let code = "";

  for (let i = 0; i < amount; i++) {
    code += "gehe(\"" + dir + "\");" + (i < amount - 1 ? "\n" : "");
  }

  return code;
}
