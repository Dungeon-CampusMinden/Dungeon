import * as Blockly from "blockly";

export function interact(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "interagieren();";
}

export function fireball_up(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return "feuerballOben();";
}

export function fireball_down(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return "feuerballUnten();";
}
export function fireball_left(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return "feuerballLinks();";
}
export function fireball_right(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return "feuerballRechts();";
}
