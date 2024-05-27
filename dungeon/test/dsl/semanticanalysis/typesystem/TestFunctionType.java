package dsl.semanticanalysis.typesystem;

import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import dsl.semanticanalysis.typesystem.typebuilding.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestFunctionType {
  @Before
  public void clearTypeFactory() {
    TypeFactory.INSTANCE.clear();
  }

  @Test
  public void equality() {
    FunctionType type1 = TypeFactory.INSTANCE.functionType(BuiltInType.noType);
    FunctionType type2 = TypeFactory.INSTANCE.functionType(BuiltInType.noType);

    Assert.assertEquals(type1, type2);
  }

  @Test
  public void inequalityRetType() {
    FunctionType type1 = TypeFactory.INSTANCE.functionType(BuiltInType.intType);
    FunctionType type2 = TypeFactory.INSTANCE.functionType(BuiltInType.noType);

    Assert.assertNotEquals(type1, type2);
  }

  @Test
  public void equalityParam() {
    FunctionType type1 =
        TypeFactory.INSTANCE.functionType(BuiltInType.intType, BuiltInType.intType);
    FunctionType type2 =
        TypeFactory.INSTANCE.functionType(BuiltInType.intType, BuiltInType.intType);

    Assert.assertEquals(type1, type2);
  }

  @Test
  public void inequalityDifferentParamCounts() {
    FunctionType type1 = TypeFactory.INSTANCE.functionType(BuiltInType.noType, BuiltInType.intType);
    FunctionType type2 =
        TypeFactory.INSTANCE.functionType(
            BuiltInType.noType, BuiltInType.intType, BuiltInType.intType);

    Assert.assertNotEquals(type1, type2);
  }

  @Test
  public void inequalityDifferentParams() {
    FunctionType type1 =
        TypeFactory.INSTANCE.functionType(
            BuiltInType.intType, BuiltInType.intType, BuiltInType.floatType);
    FunctionType type2 =
        TypeFactory.INSTANCE.functionType(
            BuiltInType.intType, BuiltInType.intType, BuiltInType.intType);

    Assert.assertNotEquals(type1, type2);
  }
}
