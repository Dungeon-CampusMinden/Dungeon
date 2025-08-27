package lispy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lispy.ast.*;
import org.junit.jupiter.api.Test;

class ParserTest {

  @Test
  public void testNumber() {
    // given
    Program res = Program.of(new NumberLiteral(42));

    // when
    Program p = Parser.parseString(" 42 ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testString() {
    // given
    Program res = Program.of(new StringLiteral("wuppieFluppie"));

    // when
    Program p = Parser.parseString(" \"wuppieFluppie\" ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testBooleanTrue() {
    // given
    Program res = Program.of(new BoolLiteral(true));

    // when
    Program p = Parser.parseString(" true ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testBooleanFalse() {
    // given
    Program res = Program.of(new BoolLiteral(false));

    // when
    Program p = Parser.parseString(" false ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testSymbol() {
    // given
    Program res = Program.of(new SymbolExpr("wuppie"));

    // when
    Program p = Parser.parseString(" wuppie ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testListID() {
    // given
    Program res = Program.of(ListExpr.of(new SymbolExpr("wuppie"), new NumberLiteral(42)));

    // when
    Program p = Parser.parseString(" (wuppie, 42 ) ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testListOP() {
    // given
    Program res =
        Program.of(ListExpr.of(new SymbolExpr("+"), new NumberLiteral(42), new NumberLiteral(7)));

    // when
    Program p = Parser.parseString(" (+ 42 7) ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testListIdBad() {
    // given

    // when, then
    assertThrows(RuntimeException.class, () -> Parser.parseString(" (\"wuppie\", 42 "));
  }

  @Test
  public void testComplexLine() {
    // given
    Program res =
        Program.of(
            ListExpr.of(
                new SymbolExpr("if"),
                ListExpr.of(new SymbolExpr("<"), new NumberLiteral(1), new NumberLiteral(2)),
                ListExpr.of(
                    new SymbolExpr("do"),
                    ListExpr.of(new SymbolExpr("print"), new StringLiteral("true")),
                    ListExpr.of(new SymbolExpr("print"), new StringLiteral("WUPPIE"))),
                ListExpr.of(new SymbolExpr("print"), new BoolLiteral(false))));

    // when
    Program p =
        Parser.parseString(" (if (< 1 2) (do (print \"true\") (print \"WUPPIE\")) (print false)) ");

    // then
    assertEquals(res, p);
  }
}
