package dsl.interpreter.mockecs;

import dsl.annotation.DSLTypeAdapter;
import dsl.annotation.DSLTypeMember;

/** WTF? . */
public class ExternalTypeBuilderMultiParam {

  /**
   * WTF? .
   *
   * @param n foo
   * @param str foo
   * @return foo
   */
  @DSLTypeAdapter
  public static ExternalType buildExternalType(
      @DSLTypeMember(name = "number") int n, @DSLTypeMember(name = "string") String str) {
    return new ExternalType(n, 12, str);
  }
}
