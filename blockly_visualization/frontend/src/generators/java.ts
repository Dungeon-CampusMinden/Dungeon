import * as Blockly from "blockly";
import * as start from "./java/start.ts";
import * as variables from "./java/variables.ts";
import * as character from "./java/character.ts";
import * as loops from "./java/loops.ts";
import * as logic from "./java/condition.ts";
import * as skills from "./java/skills.ts";

class JavaGenerator extends Blockly.Generator {
  public variables: Map<string, number>;

  constructor() {
    super("JAVA");

    this.variables = new Map();
  }

  protected scrub_(
    block: Blockly.Block,
    code: string,
    thisOnly?: boolean | undefined
  ): string {
    const nextBlock =
      block.nextConnection && block.nextConnection.targetBlock();

    if (block.type === "start") {
      return code + javaGenerator.blockToCode(nextBlock);
    }

    if (nextBlock && !thisOnly) {
      return code + "\n" + javaGenerator.blockToCode(nextBlock);
    }

    return code;
  }
}

export const javaGenerator = new JavaGenerator();

Object.assign(
  javaGenerator.forBlock,
  start,
  variables,
  character,
  loops,
  logic,
  skills
);

export const Order = {
  ATOMIC: 0,
  MEMBER: 1, // . []
  FUNCTION_CALL: 1, // ()
  INCREMENT: 2, // ++
  DECREMENT: 2, // --
  UNARY_PLUS: 3, // +
  UNARY_MINUS: 3, // -
  UNARY_LOGICAL_NOT: 3, // !
  UNARY_BITWISE_NOT: 3, // ~
  UNARY_PRE_INCREMENT: 3, // ++
  UNARY_PRE_DECREMENT: 3, // --
  CAST: 4, // cast
  NEW: 4, // new
  MULTIPLICATIVE: 5, // * / %
  ADDITIVE: 6, // + -
  STRING_CONCAT: 6, // +
  SHIFT: 7, // << >> >>>
  RELATIONAL: 8, // < > <= >= instanceof
  EQUALITY: 9, // == !=
  BITWISE_AND: 10, // &
  BITWISE_XOR: 11, // ^
  BITWISE_OR: 12, // |
  LOGICAL_AND: 13, // &&
  LOGICAL_OR: 14, // ||
  TERNARY: 15, // ?:
  ASSIGNMENT: 16, // = += -= *= /= %= &= ^= |= <<= >>= >>>=
  LAMBDA: 17, // ->
  NONE: 99,
} as const;
