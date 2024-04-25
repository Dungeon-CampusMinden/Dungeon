package dsl.interpreter.mockecs;

import dsl.annotation.DSLTypeAdapter;

/** WTF? . */
public class ExternalTypeBuilder {

  /**
   * WTF? .
   *
   * @param str foo
   * @return foo
   */
  @DSLTypeAdapter
  public static ExternalType buildExternalType(String str) {
    return new ExternalType(42, 12, str);
  }
}
