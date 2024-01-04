package dsl.interpreter.mockecs;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeAdapter;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeMember;

public class OtherExternalTypeBuilderMultiParam {
  @DSLTypeAdapter(name = "external_type")
  public static OtherExternalType buildExternalType(
      @DSLTypeMember(name = "number") int n, @DSLTypeMember(name = "string") String str) {
    return new OtherExternalType(n, 12, str);
  }
}
