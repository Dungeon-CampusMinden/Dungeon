import * as Blockly from "blockly";
import { Order } from "../java.ts";

export function logic_boolean(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = block.getFieldValue("BOOL") === "TRUE" ? "wahr" : "falsch";
  return [code, Order.NONE];
}

export function logic_wall(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = "naheWand()";
  return [code, Order.NONE];
}

export function logic_wall_up(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = "WandOben()";
  return [code, Order.NONE];
}

export function logic_wall_down(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = "WandUnten()";
  return [code, Order.NONE];
}

export function logic_wall_left(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = "WandLinks()";
  return [code, Order.NONE];
}

export function logic_wall_right(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const code = "WandRechts()";
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
