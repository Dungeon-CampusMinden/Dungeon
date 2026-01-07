import * as Blockly from "blockly";

export function start(block: Blockly.Block, generator: Blockly.Generator) {
  const bodyBlock = block.getInputTargetBlock("DO");
  const body = bodyBlock
    ? generator.prefixLines(
        generator.blockToCode(bodyBlock) as string,
        generator.INDENT
      )
    : "";
  return "public static void execute(BlocklyCommands hero) {\n" + body + "\n}";
}
