package syntaxHighlighting;

import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensServerFull;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;

/**
 * Factory for the {@code SemanticTokensWithRegistrationOptions} necessary to register the server's
 * semantic token capabilities to provide syntax highlighting.
 */
public class SemanticTokenProviderOptionsFactory {
  /**
   * Creates the {@code SemanticTokensWithRegistrationOptions} necessary to register the server's
   * semantic token capabilities to provide syntax highlighting.
   *
   * @return the {@code SemanticTokensWithRegistrationOptions} necessary to register the server's
   *     semantic token capabilities to provide syntax highlighting
   */
  public static SemanticTokensWithRegistrationOptions create() {
    SemanticTokensWithRegistrationOptions options = new SemanticTokensWithRegistrationOptions();
    SemanticTokensServerFull serverFull = new SemanticTokensServerFull();
    serverFull.setDelta(false);
    options.setFull(serverFull);
    options.setRange(false);
    options.setLegend(
        new SemanticTokensLegend(
            EnumNamesUtil.getNamesInDeclarationOrder(SemanticTokenType.class),
            EnumNamesUtil.getNamesInDeclarationOrder(SemanticTokenModifier.class)));
    return options;
  }
}
