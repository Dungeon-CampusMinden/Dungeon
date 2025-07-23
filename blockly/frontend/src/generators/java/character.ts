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
  const ALLOWED_DIRECTIONS = ["left", "right"]
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  if (dir === "") return "";
  // if not allowed, shows error and deconnects the input
  if (!ALLOWED_DIRECTIONS.includes(dir.substring(1, dir.length - 1))) { // ignore the quotes
    block.setWarningText("Ungültige Richtung. Erlaubt sind: " + ALLOWED_DIRECTIONS.join(", "));
    block.getInput("DIRECTION")?.connection?.disconnect()
    return "";
  }
  block.setWarningText(null);
  let code = "hero.rotate";
  if(dir.substring(1, dir.length - 1) === "links") {
    code = code + "(Direction.LEFT);"
  }else if(dir.substring(1, dir.length - 1) === "rechts") {
    code = code + "(Direction.RIGHT);"
  } else {
    return "";
  }
  return code;
}

export function goToExit(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "hero.moveToExit();";
}

