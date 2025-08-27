package lispy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import lispy.token.Token;
import lispy.token.Type;
import org.junit.jupiter.api.Test;

class LexerTest {

  @Test
  public void testSimpleLParen() {
    // given
    Lexer l = Lexer.from("(");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.LPAREN, "("), t);
  }

  @Test
  public void testSimpleRParen() {
    // given
    Lexer l = Lexer.from(")");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.RPAREN, ")"), t);
  }

  @Test
  public void testSimpleTrue() {
    // given
    Lexer l = Lexer.from("true");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.TRUE, "true"), t);
  }

  @Test
  public void testSimpleFalse() {
    // given
    Lexer l = Lexer.from("false");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.FALSE, "false"), t);
  }

  @Test
  public void testSimpleID() {
    // given
    Lexer l = Lexer.from("foBaa2poo");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.ID, "foBaa2poo"), t);
  }

  @Test
  public void testSimpleNumber() {
    // given
    Lexer l = Lexer.from("12345");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.NUMBER, "12345"), t);
  }

  @Test
  public void testSimpleOpPlus() {
    // given
    Lexer l = Lexer.from("+");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.OP, "+"), t);
  }

  @Test
  public void testSimpleOpMinus() {
    // given
    Lexer l = Lexer.from("-");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.OP, "-"), t);
  }

  @Test
  public void testSimpleOpMult() {
    // given
    Lexer l = Lexer.from("*");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.OP, "*"), t);
  }

  @Test
  public void testSimpleOpDiv() {
    // given
    Lexer l = Lexer.from("/");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.OP, "/"), t);
  }

  @Test
  public void testSimpleOpAssign() {
    // given
    Lexer l = Lexer.from("=");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.OP, "="), t);
  }

  @Test
  public void testSimpleOpGt() {
    // given
    Lexer l = Lexer.from(">");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.OP, ">"), t);
  }

  @Test
  public void testSimpleOpLt() {
    // given
    Lexer l = Lexer.from("<");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.OP, "<"), t);
  }

  @Test
  public void testSimpleString() {
    // given
    Lexer l = Lexer.from("\" foo bar wuppie fluppie  \"");

    // when
    Token t = l.nextToken();

    // then
    assertEquals(Token.of(Type.STRING, " foo bar wuppie fluppie  "), t);
  }

  @Test
  public void testDefectString() {
    // given
    Lexer l = Lexer.from("\" foo bar wuppie fluppie  ");

    // when, then
    assertThrows(RuntimeException.class, l::nextToken);
  }

  @Test
  public void testComment() {
    // given
    Lexer l = Lexer.from("< ;; foo bar wuppie");

    // when
    Token t1 = l.nextToken();
    Token t2 = l.nextToken();

    // then
    assertEquals(Token.of(Type.OP, "<"), t1);
    assertEquals(Type.EOF, t2.type());
  }

  @Test
  public void testComplexLine() {
    // given
    Lexer l = Lexer.from("(if (< 1 2) (do (print \"true\") (print \"WUPPIE\")) (print \"false\"))");

    // when
    List<Token> tokactual = new ArrayList<>();
    Token t = l.nextToken();
    while (t.type() != Type.EOF) {
      tokactual.add(t);
      t = l.nextToken();
    }

    // then
    List<Token> tokexpected =
        List.of(
            Token.of(Type.LPAREN, "("),
            Token.of(Type.ID, "if"),
            Token.of(Type.LPAREN, "("),
            Token.of(Type.OP, "<"),
            Token.of(Type.NUMBER, "1"),
            Token.of(Type.NUMBER, "2"),
            Token.of(Type.RPAREN, ")"),
            Token.of(Type.LPAREN, "("),
            Token.of(Type.ID, "do"),
            Token.of(Type.LPAREN, "("),
            Token.of(Type.ID, "print"),
            Token.of(Type.STRING, "true"),
            Token.of(Type.RPAREN, ")"),
            Token.of(Type.LPAREN, "("),
            Token.of(Type.ID, "print"),
            Token.of(Type.STRING, "WUPPIE"),
            Token.of(Type.RPAREN, ")"),
            Token.of(Type.RPAREN, ")"),
            Token.of(Type.LPAREN, "("),
            Token.of(Type.ID, "print"),
            Token.of(Type.STRING, "false"),
            Token.of(Type.RPAREN, ")"),
            Token.of(Type.RPAREN, ")"));

    assertEquals(tokexpected, tokactual);
  }
}
