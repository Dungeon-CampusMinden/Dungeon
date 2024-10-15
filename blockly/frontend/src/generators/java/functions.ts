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

  javaGenerator.variables.set(funcName!, funcName + "()");
  return "public void " + funcName + "() {\n" + funcBody + "\n}";

}

export function func_call(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const funcName = generator.valueToCode(block, "FUNC_NAME", Order.NONE);

  const code = funcName + "();";
  return code;
}

