package semanticanalysis.types;

import org.junit.Assert;
import org.junit.Test;

public class TestFunctionType {
    @Test
    public void equality() {
        FunctionType type1 = new FunctionType(BuiltInType.noType);
        FunctionType type2 = new FunctionType(BuiltInType.noType);

        Assert.assertEquals(type1, type2);
    }

    @Test
    public void inequalityRetType() {
        FunctionType type1 = new FunctionType(BuiltInType.intType);
        FunctionType type2 = new FunctionType(BuiltInType.noType);

        Assert.assertNotEquals(type1, type2);
    }

    @Test
    public void equalityParam() {
        FunctionType type1 = new FunctionType(BuiltInType.intType, BuiltInType.intType);
        FunctionType type2 = new FunctionType(BuiltInType.intType, BuiltInType.intType);

        Assert.assertEquals(type1, type2);
    }

    @Test
    public void inequalityDifferentParamCounts() {
        FunctionType type1 = new FunctionType(BuiltInType.noType, BuiltInType.intType);
        FunctionType type2 =
                new FunctionType(BuiltInType.noType, BuiltInType.intType, BuiltInType.intType);

        Assert.assertNotEquals(type1, type2);
    }

    @Test
    public void inequalityDifferentParams() {
        FunctionType type1 =
                new FunctionType(BuiltInType.intType, BuiltInType.intType, BuiltInType.floatType);
        FunctionType type2 =
                new FunctionType(BuiltInType.intType, BuiltInType.intType, BuiltInType.intType);

        Assert.assertNotEquals(type1, type2);
    }
}
