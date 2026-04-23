import * as Blockly from "blockly";
import {Order} from "../java.ts";

export function interact(_block: Blockly.Block, _generator: Blockly.Generator) {
  return "interagieren();";
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

export function projectile_fireball(_block: Blockly.Block, _generator: Blockly.Generator) {
  return ["fireball", Order.ATOMIC];
}

export function projectile_portal(block: Blockly.Block, generator: Blockly.Generator) {
  const color = generator.valueToCode(block, "PORTAL_COLOR", Order.NONE);
  return ["portal_" + color, Order.ATOMIC];
}

export function portal_blue(_block: Blockly.Block, _generator: Blockly.Generator) {
  return ["blue", Order.ATOMIC];
}

export function portal_green(_block: Blockly.Block, _generator: Blockly.Generator) {
  return ["green", Order.ATOMIC];
}

export function schiessen(block: Blockly.Block, generator: Blockly.Generator) {
  const projectile = generator.valueToCode(block, "PROJECTILE", Order.NONE);
  if (projectile === "fireball") return "hero.shootFireball();";
  if (projectile === "portal_blue") return "hero.shootBluePortal();";
  if (projectile === "portal_green") return "hero.shootGreenPortal();";
  return "";
}

