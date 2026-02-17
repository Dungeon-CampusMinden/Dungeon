import * as Blockly from "blockly";
import { javaGenerator, Order } from "../java.ts";

export function set_number(block: Blockly.Block, generator: Blockly.Generator) {
  const variable_id = block.getFieldValue("VAR");
  const variable_name =
    Blockly.getMainWorkspace()?.getVariableMap().getVariableById(variable_id)?.getName();

  const field_value = generator.valueToCode(block, "VALUE", Order.NONE);

  if (field_value) {
    javaGenerator.variables.set(variable_name!, Number(field_value));
    console.log(`${variable_name} = ${field_value};`)

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

export const checkIfVariablesAreDeclared = (codeLines : string[]) => {

// Globaler Scope
  const scopes: Set<string>[] = [new Set<string>()];
  let message = "";
  for (let line of codeLines) {
    line = line.trim();

    // Variablen deklarieren (nur let, var, const - C-Typen kannst du hinzufügen)
    const matchDecl = line.match(/(let|var|const|int)\s+([a-zA-Z_][a-zA-Z0-9_]*)/);
    if (matchDecl) {
      const varName = matchDecl[2];
      scopes[scopes.length - 1].add(varName);
      continue;
    }

    // Variablen zuweisen
    const matchAssign = line.match(/^([a-zA-Z_][a-zA-Z0-9_]*)\s*=/);
    if (matchAssign) {
      const varName = matchAssign[1];
      // Prüfen, ob Variable in einem Scope existiert
      const exists = scopes.some(scope => scope.has(varName));
      if (!exists) {
        message = `Fehler: Variable '${varName}' wurde nicht erstellt!`
        console.log(message);
      }
    }
  }
  return message;
}

