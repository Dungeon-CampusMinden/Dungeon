import dgir.core.Dialect;
import dgir.core.debug.Location;
import dgir.core.ir.Attribute;
import dgir.dialect.builtin.BuiltinAttrs;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.builtin.BuiltinTypes.IntegerT;
import static dgir.dialect.func.FuncOps.*;
import static dgir.dialect.func.FuncTypes.FuncType;
import static dgir.dialect.io.IoOps.PrintOp;
import static dgir.dialect.str.StrTypes.StringT;
import static org.junit.jupiter.api.Assertions.*;

public class BuiltinTests {
  private static final Location LOC = Location.UNKNOWN;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
  }

  @Test
  public void emptyProgramOp() {
    ProgramOp programOp = new ProgramOp(LOC);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void simpleProgramOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var textOp = funcMainOp.addOperation(new ConstantOp(LOC, "Hello World!"), 0);
    var numberTextOP = funcMainOp.addOperation(new ConstantOp(LOC, 42), 0);

    funcMainOp.addOperation(new PrintOp(LOC, textOp.getResult(), numberTextOP.getResult()), 0);
    funcMainOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void functionCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var fooFuncOp =
        programOp.addOperation(
            new FuncOp(LOC, "foo", FuncType.of(List.of(StringT.INSTANCE), StringT.INSTANCE)));
    {
      fooFuncOp.addOperation(new ReturnOp(LOC, fooFuncOp.getArgument(0).orElseThrow()), 0);
    }

    {
      var helloWorldTextOp = funcMainOp.addOperation(new ConstantOp(LOC, "Hello World!"), 0);
      var funcCallOp =
          funcMainOp.addOperation(new CallOp(LOC, fooFuncOp, helloWorldTextOp.getResult()), 0);
      funcMainOp.addOperation(new PrintOp(LOC, funcCallOp.getOutputValue().orElseThrow()), 0);
      funcMainOp.addOperation(new ReturnOp(LOC), 0);
    }

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  /**
   * Checks whether values which are written to multiple types are serialized and deserialized
   * correctly.
   */
  @Test
  public void overrideValue() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var constOp = funcMainOp.addOperation(new ConstantOp(LOC, 42), 0);

    var secondConstOp =
        funcMainOp.addOperation(new ConstantOp(LOC, 100).setOutputValue(constOp.getResult()), 0);

    funcMainOp.addOperation(new PrintOp(LOC, secondConstOp.getOutputValue().orElseThrow()), 0);
    funcMainOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  /**
   * Checks whether an incorrect program with a function without terminator is correctly rejected by
   * the verifier.
   */
  @Test
  public void missingTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  /** Checks that signed integer attributes preserve signed values across JSON round-trip. */
  @Test
  public void signedIntegerRoundTrip() {
    Attribute original = new BuiltinAttrs.IntegerAttribute(-1, IntegerT.INT8);

    String json = TestUtils.mapper.writeValueAsString(original);
    Attribute parsed = TestUtils.mapper.readValue(json, Attribute.class);

    assertTrue(parsed instanceof BuiltinAttrs.IntegerAttribute);
    BuiltinAttrs.IntegerAttribute parsedInteger = (BuiltinAttrs.IntegerAttribute) parsed;
    assertEquals("int8", parsedInteger.getType().getParameterizedIdent());
    assertEquals(-1, parsedInteger.getValue().byteValue());
  }

  /** Checks that unsigned integer attributes keep the expected unsigned bit pattern. */
  @Test
  public void unsignedIntegerStorage() {
    BuiltinAttrs.IntegerAttribute value = new BuiltinAttrs.IntegerAttribute(255, IntegerT.UINT8);

    assertEquals("uint8", value.getType().getParameterizedIdent());
    assertEquals(255, Byte.toUnsignedInt(value.getValue().byteValue()));
  }

  /** Checks implicit narrowing and bool normalization done by IntegerAttribute conversion. */
  @Test
  public void implicitIntegerCasting() {
    BuiltinAttrs.IntegerAttribute narrowed = new BuiltinAttrs.IntegerAttribute(300, IntegerT.INT8);
    BuiltinAttrs.IntegerAttribute normalizedBool =
        new BuiltinAttrs.IntegerAttribute(42, IntegerT.BOOL);

    assertEquals("int8", narrowed.getType().getParameterizedIdent());
    assertEquals(44, narrowed.getValue().byteValue());

    assertEquals("int1", normalizedBool.getType().getParameterizedIdent());
    assertEquals(1, normalizedBool.getValue().byteValue());
  }
}
