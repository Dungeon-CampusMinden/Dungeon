import * as Blockly from "blockly";
import { javaGenerator, Order } from "../java.ts";

export function func_def(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const variable_id = block.getFieldValue("FUNC_NAME");
  const funcName = Blockly.getMainWorkspace()?.getVariableById(variable_id)?.name;

  const funcBody = generator.prefixLines(
      generator.blockToCode(block.getInputTargetBlock("DO")),
      generator.INDENT
    );

  const code = "public void " + funcName + "() {\n" + funcBody + "\n}";
  return code;
}

