package lispy.token;

/**
 * Token.
 *
 * @param type type
 * @param lexeme lexeme
 */
public record Token(Type type, String lexeme) {
  /**
   * create a new token.
   *
   * @param type type
   * @param lexeme lexeme
   * @return new token
   */
  public static Token of(Type type, String lexeme) {
    return new Token(type, lexeme);
  }
}
