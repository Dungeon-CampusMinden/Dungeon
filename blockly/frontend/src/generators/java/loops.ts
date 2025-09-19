import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function repeat(block: Blockly.Block, generator: Blockly.Generator) {
  const times = generator.valueToCode(block, "TIMES", Order.NONE);
  // total amount of loops in whole workspace

  const repeat_body = generator.prefixLines(
    generator.blockToCode(block.getInputTargetBlock("DO")) as string,
    generator.INDENT
  );

  // create a variable name that is unique to this block (java var can only contain letters, _)
  const repeat_var = block.id.replace(/[^a-zA-Z_]/g, '');
  return "for(int " + repeat_var + " = 0; " + repeat_var + " < " + times + "; " + repeat_var + "++) {\n" + repeat_body + "\n}";
}

export function while_loop(block: Blockly.Block, generator: Blockly.Generator) {
  const condition = generator.valueToCode(block, "CONDITION", Order.NONE);

  const while_body = generator.prefixLines(
    generator.blockToCode(block.getInputTargetBlock("DO")) as string,
    generator.INDENT
  );
  const code = "while (" + condition + ") {\n" + while_body + "\n}";

  return code;
}

export function repeat_number(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = String(block.getFieldValue("REPEAT_NUMBER"));

  return [code, Order.NONE];
}
