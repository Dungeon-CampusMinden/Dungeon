import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function fireball(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "hero.shootFireball();";
}

export function shoot_blue_portal(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "hero.shootBluePortal();";
}

export function shoot_green_portal(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "hero.shootGreenPortal();";
}

export function wait(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "hero.rest();";
}

export function use(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const dir = generator.valueToCode(block, "DIRECTION", Order.NONE);
  return "hero.interact(" + dir + ");";
}

export function push(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "hero.push();";
}

export function pull(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "hero.pull();";
}

export function pickup(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "hero.pickup();";
}

export function drop_item(
  block: Blockly.Block,
  generator: Blockly.Generator
) {
  const item = generator.valueToCode(block, "ITEM", Order.NONE);
  return "hero.dropItem(" + item + ");";
}

