package lsp;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import syntaxHighlighting.SemanticTokenProviderOptionsFactory;

/**
 * This class is the central point of the lsp4j implementation. It manages client connections and
 * provides Services.
 */
public final class DslLanguageServer implements LanguageServer, LanguageClientAware {

  private final DslTextDocumentService textDocumentService = new DslTextDocumentService();
  private final WorkspaceService workspaceService = new DslWorkSpaceService();
  private int exitCode = 1;

  @Override
  public CompletableFuture<InitializeResult> initialize(final InitializeParams initializeParams) {
    return CompletableFuture.supplyAsync(
        () -> {
          ServerCapabilities serverCapabilities = new ServerCapabilities();
          serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
          serverCapabilities.setDefinitionProvider(true);
          serverCapabilities.setReferencesProvider(true);
          serverCapabilities.setSemanticTokensProvider(
              SemanticTokenProviderOptionsFactory.create());
          serverCapabilities.setCompletionProvider(new CompletionOptions());
          return new InitializeResult(serverCapabilities);
        });
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    exitCode = 0;
    return CompletableFuture.supplyAsync(Object::new);
  }

  @Override
  public void exit() {
    System.exit(exitCode);
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return this.textDocumentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return this.workspaceService;
  }

  @Override
  public void connect(final LanguageClient languageClientToConnectTo) {
    textDocumentService.initialize(languageClientToConnectTo);
    ClientLogger.getInstance().initialize(languageClientToConnectTo);
  }
}
