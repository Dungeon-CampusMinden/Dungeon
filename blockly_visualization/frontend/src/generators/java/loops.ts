import * as Blockly from "blockly";
import { Order } from "../java.ts";

export function repeat(block: Blockly.Block, generator: Blockly.Generator) {
  let code = "";
  const times = Number(generator.valueToCode(block, "TIMES", Order.NONE));

  if (times) {
    for (let i = 0; i < times; i++) {
      code += generator.blockToCode(block.getInputTargetBlock("DO"));
      if (i !== times - 1) {
        code += "\n";
      }
    }
  } else {
    return "";
  }
  return code;
}

export function repeat_number(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = String(block.getFieldValue("REPEAT_NUMBER"));

  return [code, Order.NONE];
}
