import * as Blockly from "blockly";
import { Order } from "../java.ts";

export function repeat(block: Blockly.Block, generator: Blockly.Generator) {
  const times = generator.valueToCode(block, "TIMES", Order.NONE);

  const repeat_body = generator.prefixLines(
    generator.blockToCode(block.getInputTargetBlock("DO")) as string,
    generator.INDENT
  );

  const code = "wiederhole " + times + " Mal{\n" + repeat_body + "\n}";
  return code;
}

export function while_loop(block: Blockly.Block, generator: Blockly.Generator) {
  const condition = generator.valueToCode(block, "CONDITION", Order.NONE);

  const while_body = generator.prefixLines(
    generator.blockToCode(block.getInputTargetBlock("DO")) as string,
    generator.INDENT
  );
  const code = "solange (" + condition + ") {\n" + while_body + "\n}";

  return code;
}

export function repeat_number(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = String(block.getFieldValue("REPEAT_NUMBER"));

  return [code, Order.NONE];
}
