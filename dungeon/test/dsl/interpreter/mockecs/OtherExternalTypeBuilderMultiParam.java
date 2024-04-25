package dsl.interpreter.mockecs;

import dsl.annotation.DSLTypeAdapter;
import dsl.annotation.DSLTypeMember;

/** WTF? . */
public class OtherExternalTypeBuilderMultiParam {
  /**
   * WTF? .
   *
   * @param n foo
   * @param str foo
   * @return foo
   */
  @DSLTypeAdapter(name = "external_type")
  public static OtherExternalType buildExternalType(
      @DSLTypeMember(name = "number") int n, @DSLTypeMember(name = "string") String str) {
    return new OtherExternalType(n, 12, str);
  }
}
