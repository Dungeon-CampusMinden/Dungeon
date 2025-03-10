import * as Blockly from "blockly";

export function move(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const dir = block.getFieldValue("DIRECTION");

  return "gehe(\"" + dir + "\");\n";
}
