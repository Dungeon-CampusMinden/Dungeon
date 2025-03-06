import * as Blockly from "blockly";

export function interact(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "interagieren();";
}

export function fireball_direction(
  block: Blockly.Block,
  _generator: Blockly.Generator
) {
  const dir = block.getFieldValue("DIRECTION");
  if (dir === null) {
    //throw new Error("Direction is null");
  }
  return "feuerball(\""+ dir + "\");";
}
