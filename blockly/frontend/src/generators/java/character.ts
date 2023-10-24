import * as Blockly from "blockly";
import { Order } from "../java.ts";

export function move_up(block: Blockly.Block, _generator: Blockly.Generator) {
  const amount: number = block.getFieldValue("amount");
  let code = "";

  for (let i = 0; i < amount; i++) {
    if (i === amount - 1) {
      code += "oben();";
      break;
    }
    code += "oben();\n";
  }

  return code;
}

export function move_up_var(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const amount: number = Number(
    generator.valueToCode(block, "amount", Order.NONE)
  );
  let code = "";

  for (let i = 0; i < amount; i++) {
    if (i === amount - 1) {
      code += "oben();";
      break;
    }
    code += "oben();\n";
  }

  return code;
}

export function move_down(block: Blockly.Block, _generator: Blockly.Generator) {
  const amount: number = block.getFieldValue("amount");
  let code = "";

  for (let i = 0; i < amount; i++) {
    if (i === amount - 1) {
      code += "unten();";
      break;
    }
    code += "unten();\n";
  }

  return code;
}

export function move_down_var(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const amount: number = Number(
    generator.valueToCode(block, "amount", Order.NONE)
  );
  let code = "";

  for (let i = 0; i < amount; i++) {
    if (i === amount - 1) {
      code += "unten();";
      break;
    }
    code += "unten();\n";
  }

  return code;
}

export function move_left(block: Blockly.Block, _generator: Blockly.Generator) {
  const amount: number = block.getFieldValue("amount");
  let code = "";

  for (let i = 0; i < amount; i++) {
    if (i === amount - 1) {
      code += "links();";
      break;
    }
    code += "links();\n";
  }

  return code;
}

export function move_left_var(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const amount: number = Number(
    generator.valueToCode(block, "amount", Order.NONE)
  );
  let code = "";

  for (let i = 0; i < amount; i++) {
    if (i === amount - 1) {
      code += "links();";
      break;
    }
    code += "links();\n";
  }

  return code;
}

export function move_right(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const amount: number = block.getFieldValue("amount");
  let code = "";

  for (let i = 0; i < amount; i++) {
    if (i === amount - 1) {
      code += "rechts();";
      break;
    }
    code += "rechts();\n";
  }

  return code;
}

export function move_right_var(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const amount: number = Number(
    generator.valueToCode(block, "amount", Order.NONE)
  );
  let code = "";

  for (let i = 0; i < amount; i++) {
    if (i === amount - 1) {
      code += "rechts();";
      break;
    }
    code += "rechts();\n";
  }

  return code;
}
