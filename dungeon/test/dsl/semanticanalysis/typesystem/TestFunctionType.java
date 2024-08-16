package dsl.semanticanalysis.typesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import org.junit.jupiter.api.Test;

/** WTF? . */
public class TestFunctionType {
  /** WTF? . */
  @Test
  public void equality() {
    FunctionType type1 = new FunctionType(BuiltInType.noType);
    FunctionType type2 = new FunctionType(BuiltInType.noType);

    assertEquals(type1, type2);
  }

  /** WTF? . */
  @Test
  public void inequalityRetType() {
    FunctionType type1 = new FunctionType(BuiltInType.intType);
    FunctionType type2 = new FunctionType(BuiltInType.noType);

    assertNotEquals(type1, type2);
  }

  /** WTF? . */
  @Test
  public void equalityParam() {
    FunctionType type1 = new FunctionType(BuiltInType.intType, BuiltInType.intType);
    FunctionType type2 = new FunctionType(BuiltInType.intType, BuiltInType.intType);

    assertEquals(type1, type2);
  }

  /** WTF? . */
  @Test
  public void inequalityDifferentParamCounts() {
    FunctionType type1 = new FunctionType(BuiltInType.noType, BuiltInType.intType);
    FunctionType type2 =
        new FunctionType(BuiltInType.noType, BuiltInType.intType, BuiltInType.intType);

    assertNotEquals(type1, type2);
  }

  /** WTF? . */
  @Test
  public void inequalityDifferentParams() {
    FunctionType type1 =
        new FunctionType(BuiltInType.intType, BuiltInType.intType, BuiltInType.floatType);
    FunctionType type2 =
        new FunctionType(BuiltInType.intType, BuiltInType.intType, BuiltInType.intType);

    assertNotEquals(type1, type2);
  }
}
