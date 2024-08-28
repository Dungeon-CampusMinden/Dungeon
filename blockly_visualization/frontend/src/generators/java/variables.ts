import * as Blockly from "blockly";
import { javaGenerator, Order } from "../java.ts";

export function set_number(block: Blockly.Block, generator: Blockly.Generator) {
  const variable_id = block.getFieldValue("VAR");
  const variable_name =
    Blockly.getMainWorkspace()?.getVariableById(variable_id)?.name;

  const field_value = generator.valueToCode(block, "VALUE", Order.NONE);

  if (field_value) {
    javaGenerator.variables.set(variable_name!, Number(field_value));
    return `int ${variable_name} = ${field_value};`;
  } else {
    return null;
  }
}

export function var_number(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = String(block.getFieldValue("VAR_NUMBER"));

  return [code, Order.NONE];
}

export function get_number(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const variable_id = block.getFieldValue("VAR");
  const variable_name =
    Blockly.getMainWorkspace()?.getVariableById(variable_id)?.name;

  const code = String(javaGenerator.variables.get(variable_name!));

  return [code, Order.NONE];
}
