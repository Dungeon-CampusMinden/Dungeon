import * as Blockly from "blockly";
import { Order } from "../java.ts";

export function logic_boolean(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = block.getFieldValue("BOOL") === "TRUE" ? "wahr" : "falsch";
  return [code, Order.NONE];
}

export function logic_operator(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const input_a = generator.valueToCode(block, "CONDITION_A", Order.NONE);
  const input_b = generator.valueToCode(block, "CONDITION_B", Order.NONE);
  const operator = block.getFieldValue("LOGIC_OPERATOR");
  const code = '(' + input_a + ' ' + operator + ' ' + input_b + ')';
  return [code, Order.NONE];
}

export function usual_condition(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const input_a = generator.valueToCode(block, "INPUT_A", Order.NONE);
  const input_b = generator.valueToCode(block, "INPUT_B", Order.NONE);
  const operator = block.getFieldValue("OPERATOR");
  const code = input_a + ' ' + operator + ' ' + input_b;
  return [code, Order.NONE];
}

export function not_condition(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const input_a = generator.valueToCode(block, "INPUT_A", Order.NONE);
  const code = 'nicht ' + input_a;
  return [code, Order.NONE];
}

export function logic_wall_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "naheWand(" + dir + ")";
  return [code, Order.NONE];
}

export function controls_if(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  let n = 0;
  let code = "";

  if (generator.STATEMENT_PREFIX) {
    code += generator.injectId(generator.STATEMENT_PREFIX, block);
  }

  do {
    const conditionCode =
      generator.valueToCode(block, "IF" + n, Order.NONE) || "falsch";
    let branchCode = generator.statementToCode(block, "DO" + n);
    if (generator.STATEMENT_SUFFIX) {
      branchCode =
        generator.prefixLines(
          generator.injectId(generator.STATEMENT_SUFFIX, block),
          generator.INDENT
        ) + branchCode;
    }
    code +=
      (n > 0 ? " else " : "") +
      "falls (" +
      conditionCode +
      ") {\n" +
      branchCode +
      "\n}";
    n++;
  } while (block.getInput("IF" + n));

  if (block.getInput("ELSE") || generator.STATEMENT_SUFFIX) {
    let branchCode = generator.statementToCode(block, "ELSE");
    if (generator.STATEMENT_SUFFIX) {
      branchCode =
        generator.prefixLines(
          generator.injectId(generator.STATEMENT_SUFFIX, block),
          generator.INDENT
        ) + branchCode;
    }
    code += "sonst {\n" + branchCode + "\n}";
  }
  return code;
}

export const controls_ifelse = controls_if;
