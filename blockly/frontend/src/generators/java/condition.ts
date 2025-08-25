import * as Blockly from "blockly";
import { Order } from "../java.ts";

export function logic_boolean(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = block.getFieldValue("BOOL") === "TRUE" ? "true" : "false";
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
  const code = '!' + input_a;
  return [code, Order.NONE];
}

export function logic_wall_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "hero.isNearTile(LevelElement.WALL, " + dir + ")";
  return [code, Order.NONE];
}

export function logic_floor_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "hero.isNearTile(LevelElement.FLOOR, " + dir + ")";
  return [code, Order.NONE];
}

export function logic_pit_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "hero.isNearTile(LevelElement.PIT, " + dir + ")";
  return [code, Order.NONE];
}

export function logic_monster_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "hero.isNearComponent(AIComponent.class, " + dir + ")";
  return [code, Order.NONE];
}
export function logic_switch_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "hero.isNearComponent(LeverComponent.class, " + dir + ")";
  return [code, Order.NONE];
}

export function logic_breadcrumbs_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "hero.isNearComponent(BreadcrumbComponent.class, " + dir + ")";
  return [code, Order.NONE];
}

export function logic_clover_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "hero.isNearComponent(CloverComponent.class, " + dir + ")";
  return [code, Order.NONE];
}

export function logic_active_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "hero.active(" + dir + ")";
  return [code, Order.NONE];
}

export function logic_bossView_direction(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  const code = "hero.checkBossViewDirection(" + dir + ")";
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
      "if (" +
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
    code += "else {\n" + branchCode + "\n}";
  }
  return code;
}

export const controls_ifelse = controls_if;

export function switch_case(block: Blockly.Block, generator: Blockly.Generator) {
  const switch_expr = generator.valueToCode(block, "SWITCH", Order.NONE);
  const cases_code = generator.statementToCode(block, "CASES");
  const code = "switch (" + switch_expr + ") {\n" + cases_code + "\n}";
  return code;
}

export function case_block(block: Blockly.Block, generator: Blockly.Generator) {
  const case_value = generator.valueToCode(block, "CASE", Order.NONE);
  let do_code = generator.statementToCode(block, "DO");
  do_code = generator.prefixLines(do_code, generator.INDENT);
  return `case ${case_value}:\n${do_code}\nbreak;`;
}

export function default_block(block: Blockly.Block, generator: Blockly.Generator) {
  let do_code = generator.statementToCode(block, "DO");
  do_code = generator.prefixLines(do_code, generator.INDENT);
  return `default:\n${do_code}`;
}
