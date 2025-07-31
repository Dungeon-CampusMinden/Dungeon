import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function move(
  _block: Blockly.Block,
  _generator: Blockly.Generator
) {
  return "hero.move();";
}

export function rotate(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const ALLOWED_DIRECTIONS = ["Direction.LEFT", "Direction.RIGHT"]
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  if (dir === "") return "";
  // if not allowed, shows error and disconnects the input
  if(!ALLOWED_DIRECTIONS.includes(dir)) {
    block.setWarningText("Ungültige Richtung. Erlaubt sind: links, rechts");
    block.getInput("DIRECTION")?.connection?.disconnect()
    return "";
  }
  block.setWarningText(null);
  return "hero.rotate(" + dir + ");";
}

export function goToExit(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "hero.moveToExit();";
}

