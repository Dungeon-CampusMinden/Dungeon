import * as Blockly from "blockly";
import { javaGenerator, Order } from "../java.ts";

export function func_def(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const variable_id = block.getFieldValue("FUNC_NAME");
  const funcName = Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName();

  const funcBody = generator.prefixLines(
      generator.blockToCode(block.getInputTargetBlock("DO")) as string,
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

