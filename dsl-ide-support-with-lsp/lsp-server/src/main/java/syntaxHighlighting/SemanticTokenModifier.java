package syntaxHighlighting;

/**
 * The semantic token modifiers that this server announces and the client may use to define a color
 * of a token. Predefined modifiers are listed here <a
 * href="https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#textDocument_semanticTokens">...</a>
 */
public enum SemanticTokenModifier {
  /**
   * Modifier that shows that this is the definition of an identifier and not just a (forward)
   * declaration.
   */
  definition
}
