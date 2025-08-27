package lispy;

import static org.junit.jupiter.api.Assertions.*;

import lispy.ast.*;
import lispy.values.Value;
import org.junit.jupiter.api.Test;

class InterpreterTest {

  @Test
  public void testNumber() {
    // given: 42
    Program p = Program.of(new NumberLiteral(42));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("42", Value.pretty(res));
  }

  @Test
  public void testString() {
    // given: "wuppieFluppie"
    Program p = Program.of(new StringLiteral("wuppieFluppie"));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("\"wuppieFluppie\"", Value.pretty(res));
  }

  @Test
  public void testBooleanTrue() {
    // given: true
    Program p = Program.of(new BoolLiteral(true));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("true", Value.pretty(res));
  }

  @Test
  public void testBooleanFalse() {
    // given: false
    Program p = Program.of(new BoolLiteral(false));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("false", Value.pretty(res));
  }

  @Test
  public void testSymbol() {
    // given: wuppie
    Program p = Program.of(new SymbolExpr("wuppie"));

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.evaluate(p));
  }

  @Test
  public void testList() {
    // given: (list 42 7 "wuppie")
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("list"),
                new NumberLiteral(42),
                new NumberLiteral(7),
                new StringLiteral("wuppie")));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("(42 7 \"wuppie\")", Value.pretty(res));
  }

  @Test
  public void testListHead() {
    // given: (head (list 42 7 "wuppie"))
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("head"),
                ListExpr.of(
                    new SymbolExpr("list"),
                    new NumberLiteral(42),
                    new NumberLiteral(7),
                    new StringLiteral("wuppie"))));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("42", Value.pretty(res));
  }

  @Test
  public void testListTail() {
    // given: (tail (list 42 7 "wuppie"))
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("tail"),
                ListExpr.of(
                    new SymbolExpr("list"),
                    new NumberLiteral(42),
                    new NumberLiteral(7),
                    new StringLiteral("wuppie"))));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("(7 \"wuppie\")", Value.pretty(res));
  }

  @Test
  public void testListCons() {
    // given: (cons true (tail (list 42 7 "wuppie")))
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("cons"),
                new BoolLiteral(true),
                ListExpr.of(
                    new SymbolExpr("tail"),
                    ListExpr.of(
                        new SymbolExpr("list"),
                        new NumberLiteral(42),
                        new NumberLiteral(7),
                        new StringLiteral("wuppie")))));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("(true 7 \"wuppie\")", Value.pretty(res));
  }

  @Test
  public void testIfTrue() {
    // given: (if (< 1 2) (print "true") (print "WUPPIE"))
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("if"),
                ListExpr.of(new SymbolExpr("<"), new NumberLiteral(1), new NumberLiteral(2)),
                ListExpr.of(new SymbolExpr("print"), new StringLiteral("true")),
                ListExpr.of(new SymbolExpr("print"), new StringLiteral("WUPPIE"))));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("\"true\"", Value.pretty(res));
  }

  @Test
  public void testIfFalse() {
    // given: (if (> 1 2) (print "true") (print "WUPPIE"))
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("if"),
                ListExpr.of(new SymbolExpr(">"), new NumberLiteral(1), new NumberLiteral(2)),
                ListExpr.of(new SymbolExpr("print"), new StringLiteral("true")),
                ListExpr.of(new SymbolExpr("print"), new StringLiteral("WUPPIE"))));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("\"WUPPIE\"", Value.pretty(res));
  }

  @Test
  public void testLetSymbol() {
    // given: (let wuppie 5) (print wuppie)
    Program p =
        Program.of(
            ListExpr.of(new SymbolExpr("let"), new SymbolExpr("wuppie"), new NumberLiteral(5)),
            ListExpr.of(new SymbolExpr("print"), new SymbolExpr("wuppie")));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("5", Value.pretty(res));
  }

  @Test
  public void testLetFnPrint() {
    // given: (let wuppie 5) (let a 10) (let (foo a b) (print (+ a b wuppie)))
    Program p =
        Program.of(
            ListExpr.of(new SymbolExpr("let"), new SymbolExpr("wuppie"), new NumberLiteral(5)),
            ListExpr.of(new SymbolExpr("let"), new SymbolExpr("a"), new NumberLiteral(10)),
            ListExpr.of(
                new SymbolExpr("let"),
                ListExpr.of(new SymbolExpr("foo"), new SymbolExpr("a"), new SymbolExpr("b")),
                ListExpr.of(
                    new SymbolExpr("print"),
                    ListExpr.of(
                        new SymbolExpr("+"),
                        new SymbolExpr("a"),
                        new SymbolExpr("b"),
                        new SymbolExpr("wuppie")))));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("<fn foo>", Value.pretty(res));
  }

  @Test
  public void testLetFnEval() {
    // given:
    // (let wuppie 5) (let a 10)
    // (let (foo a b) (print (+ a b wuppie))
    // (let c (foo 1 100))
    // (print c) (print wuppie) (print a))
    Program p =
        Program.of(
            ListExpr.of(new SymbolExpr("let"), new SymbolExpr("wuppie"), new NumberLiteral(5)),
            ListExpr.of(new SymbolExpr("let"), new SymbolExpr("a"), new NumberLiteral(10)),
            ListExpr.of(
                new SymbolExpr("let"),
                ListExpr.of(new SymbolExpr("foo"), new SymbolExpr("a"), new SymbolExpr("b")),
                ListExpr.of(
                    new SymbolExpr("print"),
                    ListExpr.of(
                        new SymbolExpr("+"),
                        new SymbolExpr("a"),
                        new SymbolExpr("b"),
                        new SymbolExpr("wuppie")))),
            ListExpr.of(
                new SymbolExpr("let"),
                new SymbolExpr("c"),
                ListExpr.of(new SymbolExpr("foo"), new NumberLiteral(1), new NumberLiteral(100))),
            ListExpr.of(new SymbolExpr("print"), new SymbolExpr("c")),
            ListExpr.of(new SymbolExpr("print"), new SymbolExpr("wuppie")),
            ListExpr.of(new SymbolExpr("print"), new SymbolExpr("a")));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("10", Value.pretty(res));
  }

  @Test
  public void testLetFnArity() {
    // given:
    // (let (foo a b) (print (+ a b wuppie))
    // (let c (foo 1))
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("let"),
                ListExpr.of(new SymbolExpr("foo"), new SymbolExpr("a"), new SymbolExpr("b")),
                ListExpr.of(
                    new SymbolExpr("print"),
                    ListExpr.of(
                        new SymbolExpr("+"),
                        new SymbolExpr("a"),
                        new SymbolExpr("b"),
                        new SymbolExpr("wuppie")))),
            ListExpr.of(
                new SymbolExpr("let"),
                new SymbolExpr("c"),
                ListExpr.of(new SymbolExpr("foo"), new NumberLiteral(1))));

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.evaluate(p));
  }

  @Test
  public void testBuildInPlus() {
    // given: (+ 1 10 100 1000)
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("+"),
                new NumberLiteral(1),
                new NumberLiteral(10),
                new NumberLiteral(100),
                new NumberLiteral(1000)));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("1111", Value.pretty(res));
  }

  @Test
  public void testBuildInMinus() {
    // given: (- 100 10 1)
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("-"),
                new NumberLiteral(100),
                new NumberLiteral(10),
                new NumberLiteral(1)));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("89", Value.pretty(res));
  }

  @Test
  public void testBuildInMult() {
    // given: (* 100 10 2)
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("*"),
                new NumberLiteral(100),
                new NumberLiteral(10),
                new NumberLiteral(2)));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("2000", Value.pretty(res));
  }

  @Test
  public void testBuildInDiv() {
    // given: (/ 100 10 2)
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("/"),
                new NumberLiteral(100),
                new NumberLiteral(10),
                new NumberLiteral(2)));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("5", Value.pretty(res));
  }

  @Test
  public void testRecursion() {
    // given: (let (fac n) (if (< n 2) 1 (* n (fac (- n 1))))) (fac 5)
    Program p =
        Program.of(
            ListExpr.of(
                new SymbolExpr("let"),
                ListExpr.of(new SymbolExpr("fac"), new SymbolExpr("n")),
                ListExpr.of(
                    new SymbolExpr("if"),
                    ListExpr.of(new SymbolExpr("<"), new SymbolExpr("n"), new NumberLiteral(2)),
                    new NumberLiteral(1),
                    ListExpr.of(
                        new SymbolExpr("*"),
                        new SymbolExpr("n"),
                        ListExpr.of(
                            new SymbolExpr("fac"),
                            ListExpr.of(
                                new SymbolExpr("-"), new SymbolExpr("n"), new NumberLiteral(1)))))),
            ListExpr.of(new SymbolExpr("fac"), new NumberLiteral(5)));

    // when
    Value res = Interpreter.evaluate(p);

    // then
    assertEquals("120", Value.pretty(res));
  }
}
