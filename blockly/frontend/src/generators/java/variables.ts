import * as Blockly from "blockly";
import { javaGenerator, Order } from "../java.ts";

export function set_number(block: Blockly.Block, generator: Blockly.Generator) {
  const variable_id = block.getFieldValue("VAR");
  const variable_name =
    Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName();

  const field_value = generator.valueToCode(block, "VALUE", Order.NONE);

  if (field_value) {
    javaGenerator.variables.set(variable_name!, Number(field_value));
    return `${variable_name} = ${field_value};`;
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
    Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName();

  const code = String(javaGenerator.variables.get(variable_name!));

  return [code, Order.NONE];
}

export function expression(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const input_a = generator.valueToCode(block, "INPUT_A", Order.NONE);
  const input_b = generator.valueToCode(block, "INPUT_B", Order.NONE);
  const op = block.getFieldValue("OPERATOR");

  const code = input_a + ' ' + op + ' ' + input_b;

  return [code, Order.NONE];
}

export function set_number_expression(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const value = generator.valueToCode(block, "VALUE", Order.NONE);

  const variable_id = block.getFieldValue("VAR");
    const variable_name = Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName() ?? "UNKNOWN";

  if (value) {
      javaGenerator.variables.set(variable_name, variable_name);
      return `int ${variable_name} = ${value};`;
    }
  return null;
}

export function get_variable(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const variable_id = block.getFieldValue("VAR");
  const variable_name =
      Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName();

  return [variable_name, Order.NONE];
}
