import * as Blockly from "blockly";
import { Order } from "../java.ts";

export function var_array(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {

  const variable_id = block.getFieldValue("VAR");
  const variable_name = Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName();

  const input_a = block.getFieldValue("INPUT_A");

  const code = 'int[] ' + variable_name + ' = new int[' + input_a + '];'
  return code;
}

export function array_set(
  block: Blockly.Block,
  generator: Blockly.Generator
) {

  const variable_id = block.getFieldValue("VAR");
  const variable_name = Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName();

  const input_index = generator.valueToCode(block, "INPUT_INDEX", Order.NONE);
  const input_value = generator.valueToCode(block, "INPUT_VALUE", Order.NONE);

  const code = variable_name + '[' + input_index + '] = ' + input_value + ';'
  return code;
}

export function array_get(
  block: Blockly.Block,
  generator: Blockly.Generator
) {

  const variable_id = block.getFieldValue("VAR");
  const variable_name = Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName();

  const input_index = generator.valueToCode(block, "INPUT_INDEX", Order.NONE);

  const code = variable_name + '[' + input_index + ']';
  return [code, Order.NONE];
}

export function array_length(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {

  const variable_id = block.getFieldValue("VAR");
  const variable_name = Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName();

  const code = variable_name + '.length';
  return [code, Order.NONE];
}


