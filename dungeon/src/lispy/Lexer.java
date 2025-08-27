package lispy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;
import lispy.token.Token;
import lispy.token.Type;

/** Lexer (recursive descent). */
public class Lexer {
  private String input;
  private int index = 0;

  private Lexer(String input) {
    this.input = input;
  }

  /**
   * Create a Lexer from String.
   *
   * @param source source (string)
   * @return new lexer
   */
  public static Lexer from(String source) {
    return new Lexer(Objects.requireNonNull(source));
  }

  /**
   * Create a Lexer from File.
   *
   * @param path source (file)
   * @return new lexer
   * @throws IOException when encountering issues while file handling
   */
  public static Lexer from(Path path) throws IOException {
    return new Lexer(Files.readString(path, StandardCharsets.UTF_8));
  }

  /**
   * Read the next token.
   *
   * @return next token
   */
  public Token nextToken() {
    while (!eof()) {
      char c = peek();
      switch (c) {
        case '(' -> {
          consume();
          return Token.of(Type.LPAREN, "(");
        }
        case ')' -> {
          consume();
          return Token.of(Type.RPAREN, ")");
        }
        case '+', '-', '*', '/', '=', '>', '<' -> {
          consume();
          return Token.of(Type.OP, String.valueOf(c));
        }
        case '"' -> {
          consume();
          String s = readStringLiteral();
          return Token.of(Type.STRING, s);
        }
        case ';' -> skipComments();
        case ' ', ',', '\t', '\n', '\r' -> skipWhitespace();
        default -> {
          if (isDigit(c)) {
            String num = readWhile(Lexer::isDigit);
            return Token.of(Type.NUMBER, num);
          }
          if (isLetter(c)) {
            String id = readWhile(Lexer::isLetterOrDigit);
            return switch (id) {
              case "true" -> Token.of(Type.TRUE, id);
              case "false" -> Token.of(Type.FALSE, id);
              default -> Token.of(Type.ID, id);
            };
          }
          throw error("unexpected character '" + c + "'");
        }
      }
    }
    return Token.of(Type.EOF, "<EOF>");
  }

  private boolean eof() {
    return index >= input.length();
  }

  private char peek() {
    return (index < input.length()) ? input.charAt(index) : '\0';
  }

  private char peekNext() {
    return (index + 1 < input.length()) ? input.charAt(index + 1) : '\0';
  }

  private void consume() {
    index++;
  }

  private boolean match(char c) {
    if (peekNext() == c) {
      consume();
      return true;
    }
    return false;
  }

  private String readWhile(Predicate<Character> pred) {
    StringBuilder sb = new StringBuilder();
    while (!eof() && pred.test(peek())) {
      sb.append(peek());
      consume();
    }
    return sb.toString();
  }

  private String readStringLiteral() {
    String s = readWhile(c -> c != '"' && c != '\n' && c != '\r');

    if (peek() == '"') consume(); // closing '"'
    else if (peek() == '\n' || peek() == '\r')
      throw error("string not terminated (found line end before matching '\"')");
    else if (peek() == '\0') throw error("string not terminated (found EOF before matching '\"')");

    return s;
  }

  private void skipComments() {
    if (match(';')) readWhile(c -> c != '\n' && c != '\r');
  }

  private void skipWhitespace() {
    readWhile(c -> c == ' ' || c == ',' || c == '\t' || c == '\n' || c == '\r');
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isLetter(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  private static boolean isLetterOrDigit(char c) {
    return isLetter(c) || isDigit(c);
  }

  private static RuntimeException error(String msg) {
    return new RuntimeException(msg);
  }
}
