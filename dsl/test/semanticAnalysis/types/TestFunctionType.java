package semanticAnalysis.types;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestFunctionType {
    @Test
    public void equality() {
        FunctionType type1 = new FunctionType(BuiltInType.noType, new ArrayList<>());
        FunctionType type2 = new FunctionType(BuiltInType.noType, new ArrayList<>());

        Assert.assertEquals(type1, type2);
    }

    @Test
    public void inequalityRetType() {
        FunctionType type1 = new FunctionType(BuiltInType.intType, new ArrayList<>());
        FunctionType type2 = new FunctionType(BuiltInType.noType, new ArrayList<>());

        Assert.assertNotEquals(type1, type2);
    }

    @Test
    public void equalityParam() {
        FunctionType type1 =
                new FunctionType(
                        BuiltInType.intType,
                        new ArrayList<>(List.of(new IType[] {BuiltInType.intType})));
        FunctionType type2 =
                new FunctionType(
                        BuiltInType.intType,
                        new ArrayList<>(List.of(new IType[] {BuiltInType.intType})));

        Assert.assertEquals(type1, type2);
    }

    @Test
    public void inequalityDifferentParamCounts() {
        FunctionType type1 =
                new FunctionType(
                        BuiltInType.noType,
                        new ArrayList<>(List.of(new IType[] {BuiltInType.intType})));
        FunctionType type2 =
                new FunctionType(
                        BuiltInType.noType,
                        new ArrayList<>(
                                List.of(new IType[] {BuiltInType.intType, BuiltInType.intType})));

        Assert.assertNotEquals(type1, type2);
    }

    @Test
    public void inequalityDifferentParams() {
        FunctionType type1 =
                new FunctionType(
                        BuiltInType.intType,
                        new ArrayList<>(
                                List.of(new IType[] {BuiltInType.intType, BuiltInType.floatType})));
        FunctionType type2 =
                new FunctionType(
                        BuiltInType.intType,
                        new ArrayList<>(
                                List.of(new IType[] {BuiltInType.intType, BuiltInType.intType})));

        Assert.assertNotEquals(type1, type2);
    }
}
