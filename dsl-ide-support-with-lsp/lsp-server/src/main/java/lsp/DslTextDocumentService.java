package lsp;

import antlrListener.AntlrListener;
import antlr_gen.AntlrGrammarLexer;
import antlr_gen.AntlrGrammarParser;
import autocompletion.CompletionItemQuery;
import identifiers.GoToResolver;
import identifiers.IdentifierDiagnosticsQuery;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import syntaxHighlighting.SemanticTokenTransformer;

/** Implements {@code TextDocumentService} to provide capabilities on text documents. */
public final class DslTextDocumentService implements TextDocumentService {
  private final ClientLogger clientLogger = ClientLogger.getInstance();
  private LanguageClient languageClient;
  private final Dictionary<String, DocumentInformation> documentInformationByUri =
      new Hashtable<>();

  /**
   * Initializes this class.
   *
   * @param languageClient the client this server connected to.
   */
  public void initialize(LanguageClient languageClient) {
    this.languageClient = languageClient;
  }

  @Override
  public void didOpen(final DidOpenTextDocumentParams didOpenTextDocumentParams) {
    String fileUri = didOpenTextDocumentParams.getTextDocument().getUri();
    this.clientLogger.logMessage("text/didOpen fileUri: " + fileUri);
    try {
      String fileContent = Files.readString(Paths.get(URI.create(fileUri)));
      documentInformationByUri.put(fileUri, new DocumentInformation(fileContent));
    } catch (IOException e) {
      this.clientLogger.logMessage("error reading file: " + fileUri + "\n" + e.getMessage());
    }
  }

  @Override
  public void didChange(final DidChangeTextDocumentParams didChangeTextDocumentParams) {
    String fileUri = didChangeTextDocumentParams.getTextDocument().getUri();
    this.clientLogger.logMessage("text/didChange fileUri: " + fileUri);
    String newDocumentContentWhenSyncModeIsFull =
        didChangeTextDocumentParams.getContentChanges().getFirst().getText();
    documentInformationByUri.get(fileUri).updateFileContent(newDocumentContentWhenSyncModeIsFull);
  }

  @Override
  public void didClose(final DidCloseTextDocumentParams didCloseTextDocumentParams) {
    String fileUri = didCloseTextDocumentParams.getTextDocument().getUri();
    this.clientLogger.logMessage("text/didClose fileUri: " + fileUri);
    documentInformationByUri.remove(fileUri);
    languageClient.publishDiagnostics(new PublishDiagnosticsParams(fileUri, List.of()));
  }

  @Override
  public void didSave(final DidSaveTextDocumentParams didSaveTextDocumentParams) {
    String fileUri = didSaveTextDocumentParams.getTextDocument().getUri();
    this.clientLogger.logMessage("text/didSave fileUri: " + fileUri);
  }

  @Override
  public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
      definition(DefinitionParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          String uri = params.getTextDocument().getUri();
          DocumentInformation documentInformation = documentInformationByUri.get(uri);
          return Either.forLeft(
              GoToResolver.resolveDefinition(
                  uri,
                  params.getPosition(),
                  documentInformation.getDefinitionIdCollector().getCollectedIdentifiersRanges(),
                  documentInformation.getUsageIdCollector().getCollectedIdentifiersRanges()));
        });
  }

  @Override
  public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          String uri = params.getTextDocument().getUri();
          DocumentInformation documentInformation = documentInformationByUri.get(uri);
          return GoToResolver.resolveUsages(
              uri,
              params.getPosition(),
              documentInformation.getDefinitionIdCollector().getCollectedIdentifiersRanges(),
              documentInformation.getUsageIdCollector().getCollectedIdentifiersRanges());
        });
  }

  @Override
  public CompletableFuture<SemanticTokens> semanticTokensFull(
      SemanticTokensParams semanticTokensParams) {
    String fileUri = semanticTokensParams.getTextDocument().getUri();
    this.clientLogger.logMessage("text/semanticTokensFull fileUri: " + fileUri);
    return CompletableFuture.supplyAsync(
        () -> parseFileToSemanticTokensAndPublishDiagnostics(fileUri));
  }

  private SemanticTokens parseFileToSemanticTokensAndPublishDiagnostics(String fileUri) {
    DocumentInformation documentInformation = documentInformationByUri.get(fileUri);
    if (documentInformation == null) {
      return new SemanticTokens();
    }
    ArrayList<Diagnostic> diagnostics = new ArrayList<>();
    SemanticTokenTransformer semanticTokenTransformer = new SemanticTokenTransformer();

    parseFileIntoSemanticTokenTransformerAndCollectDiagnostics(
        documentInformation, semanticTokenTransformer, diagnostics);

    languageClient.publishDiagnostics(new PublishDiagnosticsParams(fileUri, diagnostics));
    return new SemanticTokens(semanticTokenTransformer.getResult());
  }

  private void parseFileIntoSemanticTokenTransformerAndCollectDiagnostics(
      DocumentInformation documentInformation,
      SemanticTokenTransformer semanticTokenTransformer,
      ArrayList<Diagnostic> diagnostics) {
    BaseErrorListener errorListener =
        new BaseErrorListener() {
          @Override
          public void syntaxError(
              Recognizer<?, ?> recognizer,
              Object offendingSymbol,
              int lineStartingAt1,
              int charPositionInLine,
              String msg,
              RecognitionException e) {
            int lengthOfWrongText = 1;
            if (e != null
                && e.getOffendingToken() != null
                && e.getOffendingToken().getText() != null) {
              lengthOfWrongText = e.getOffendingToken().getText().length();
            } else if (offendingSymbol instanceof CommonToken commonToken) {
              lengthOfWrongText = commonToken.getText().length();
            }

            diagnostics.add(
                new Diagnostic(
                    new Range(
                        new Position(lineStartingAt1 - 1, charPositionInLine),
                        new Position(lineStartingAt1 - 1, charPositionInLine + lengthOfWrongText)),
                    msg));
          }
        };

    CharStream charStreamOfGivenFilePath =
        CharStreams.fromString(documentInformation.getFileContent());

    Lexer lexer = new AntlrGrammarLexer(charStreamOfGivenFilePath);
    lexer.removeErrorListeners();
    lexer.addErrorListener(errorListener);

    AntlrGrammarParser parser = new AntlrGrammarParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);

    try {
      ParseTree tree = parser.start();
      ParseTreeWalker.DEFAULT.walk(
          new AntlrListener(semanticTokenTransformer, documentInformation), tree);
      diagnostics.addAll(
          IdentifierDiagnosticsQuery.getDiagnostics(
              documentInformation.getDefinitionIdCollector().getCollectedIdentifiersRanges(),
              documentInformation.getUsageIdCollector().getCollectedIdentifiersRanges()));
    } catch (RecognitionException recognitionException) {
      Token offendingToken = recognitionException.getOffendingToken();
      if (offendingToken != null) {
        int zeroBasedLine = offendingToken.getLine() - 1;
        diagnostics.add(
            new Diagnostic(
                new Range(
                    new Position(zeroBasedLine, offendingToken.getCharPositionInLine()),
                    new Position(
                        zeroBasedLine,
                        offendingToken.getCharPositionInLine()
                            + offendingToken.getText().length())),
                recognitionException.getMessage()));
      }
    }
  }

  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
      final CompletionParams completionParams) {
    return CompletableFuture.supplyAsync(
        () -> {
          this.clientLogger.logMessage("text/completion");
          Position position = completionParams.getPosition();
          DocumentInformation documentInformation =
              documentInformationByUri.get(completionParams.getTextDocument().getUri());
          if (documentInformation == null) {
            return Either.forLeft(new ArrayList<>());
          }
          List<CompletionItem> completionItems =
              CompletionItemQuery.fetchCompletionItems(
                  position,
                  documentInformation.getFileContent(),
                  documentInformation
                      .getDefinitionIdCollector()
                      .getCollectedIdentifiersRanges()
                      .keySet());
          return Either.forLeft(completionItems);
        });
  }
}
