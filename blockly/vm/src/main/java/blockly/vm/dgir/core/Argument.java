package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.Type;

/**
 * Argument of a block. Can be used to declare a typed variable that gets defined for a block.
 * E.g.: func.func main(int32 x, int32 y) // x and y are typed arguments
 */
public class Argument {
  public String ident;
  public Type type;
}
