package syntaxHighlighting;

/**
 * The semantic token type that this server announces and the client may use to define a color of a
 * token. Predefined tokens are listed here <a
 * href="https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#textDocument_semanticTokens">...</a>
 */
public enum SemanticTokenType {
  /** Semantic token type of variable. */
  variable,
  /** Semantic token type of keyword. */
  keyword,
  /** Semantic token type of string. */
  string,
  /** Semantic token type of number. */
  number,
  /** Semantic token type of type. */
  type
}
