package main;

import static main.Helpers.getMethodName;
import static main.Main.LOGGER;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;

import dsl.helper.ProfilingTimer;
import dsl.interpreter.DSLInterpreter;
import dsl.neo4j.Neo4jConnect;
import dsl.parser.DungeonASTConverter;
import dsl.parser.ast.Node;
import dsl.semanticanalysis.environment.GameEnvironment;
import entrypoint.DungeonConfig;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.*;
import org.junit.Assert;
import org.neo4j.driver.Driver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class LspServer
    implements LanguageServer, LanguageClientAware, TextDocumentService, WorkspaceService {
  private final List<LanguageClient> clients = new ArrayList<>();
  private final Socket socket;
  private final Future<Void> serverListening;
  private final Session session;
  private GameEnvironment environment;
  private Driver neo4jDriver;
  private SessionFactory sessionFactory;

  public LspServer(int socketPort) throws IOException {
    this.environment = new GameEnvironment();
    // TODO: Neo4j Connection
    this.neo4jDriver = Neo4jConnect.openConnection();
    this.sessionFactory = Neo4jConnect.getSessionFactory(neo4jDriver);
    this.session = sessionFactory.openSession();

    this.socket = new Socket("127.0.0.1", socketPort);
    Launcher<LanguageClient> launcher;
    launcher =
        LSPLauncher.createServerLauncher(
            this, this.socket.getInputStream(), this.socket.getOutputStream());
    this.serverListening = launcher.startListening();
    LOGGER.info("Started listening");
  }

  public Future<Void> getServerListening() {
    return serverListening;
  }

  // as per
  // https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#initialize
  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
    LOGGER.info("initialize");

    // TODO: welche informationen muss der Server hier speichern oder abfragen?
    var options = initializeParams.getInitializationOptions();
    var caps = initializeParams.getCapabilities();
    var clientInfo = initializeParams.getClientInfo();
    var workSpaceFolders = initializeParams.getWorkspaceFolders();

    // TODO: welche informationen muss der Server hier an Client zurückmelden?
    //  - [x] The capabilities the server provides (ServerCapabilities)
    //      - [x] required:
    //          - [x] positionEncoding (falls nicht angegeben, utf-16) (as is)
    //          - [x] TextDocumentSync
    //          - [x] completionProvider
    //          - [x] diagnosticProvider (the server has support for pull model diagnostics)?
    //          - [x] workspace-specific:
    //              - [x] workspaceFolders?
    //              - [x] fileoperations (didCreate, willCreate, didRename, willRename, didDelete,
    // willDelete)
    //      - [ ] nice to have:
    //          - [ ] inlayHintsProvider?!
    //          - [ ] semanticTokensProvider?!
    //          - [ ] workspaceSymbolProvider?!
    //      - [ ] don't know:
    //          - [ ] documentHighlightProvider?
    //          - [ ] documentSymbolProvider?
    //          - [ ] colorProvider?
    //          - [ ] renameProvider?
    //  - [x] ServerInfo (name of the server; version of the server)

    CompletableFuture<InitializeResult> result =
        CompletableFuture.supplyAsync(
            () -> {
              ServerCapabilities serverCapabilities = new ServerCapabilities();

              /*
               * see https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#textDocument_synchronization
               *
               * Servers cannot opt out of text document sync capabilities, which includes handling
               * textDocument/didOpen, textDocument/didChange and textDocument/didClose notifications.
               *
               * The TextDocumentSyncKind determines, how textDocument/didChange is performed.
               * - TextDocumentSyncKind.Full: The full contents of the changed
               *   document is transmitted with the didChange notification.
               * - TextDocumentSyncKind.Incremental: Only the incremental changes
               *   of the document are transmitted with the didChange notification.
               *
               * For the sake of simplicity we use full synchronization in the first implementation.
               */
              serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

              /*
               * see https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#completionOptions
               */
              CompletionOptions completionOptions = new CompletionOptions();

              /*
               * Additional trigger characters (appart from [a-zA-Z]) on which the completion is triggered
               */
              completionOptions.setTriggerCharacters(List.of(".", ",", "("));

              /*
               * List of all possible characters that commit a completion.
               *
               * TODO: currently not used, let's see about that in the future.
               */
              // completionOptions.setAllCommitCharacters();

              /*
               * The server provides support to resolve additional information for a completion item.
               *
               * TODO: how does this affect completion requests?
               */
              completionOptions.setResolveProvider(true);

              /*
               * see https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#completionItemLabelDetails
               *
               * The server supports additional details for a completion item label
               *
               * TODO: currently not used, let's see about that in the future.
               */
              // CompletionItemOptions completionItemOptions = new CompletionItemOptions();
              // completionOptions.setCompletionItem();

              serverCapabilities.setCompletionProvider(completionOptions);

              /*
               * see https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#textDocument_pullDiagnostics
               *
               * Specifies, whether the server supports pull-based diagnostic calculations.
               * Normally, the server pushes diagnostics to the client, which leaves it to the
               * server to calculate diagnostics when it fits best for the server.
               * This leads to the problem, that the server may not prioritize the file, which is currently
               * edited by the user. To remedy that, the client can pull the diagnostics for a file.
               *
               * TODO: if this is a good idea remains to be seen. For now, just enable it and see...
               */

              DiagnosticRegistrationOptions diagnosticOptions = new DiagnosticRegistrationOptions();
              diagnosticOptions.setId("DungeonDSL Diagnostics ID");
              diagnosticOptions.setIdentifier("DungeonDSL Diagnostics identifier");

              // specifies, whether the language has inter-file dependencies, which means that
              // editing code in one file can lead to a diagnostic in another file
              diagnosticOptions.setInterFileDependencies(true);
              serverCapabilities.setDiagnosticProvider(diagnosticOptions);

              /*
               * see
               */

              WorkspaceServerCapabilities workspaceServerCapabilities =
                  new WorkspaceServerCapabilities();

              /*
               * see https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#workspace_workspaceFolders
               *
               * Relates to projects with multiple roots, we don't support that right now.
               */
              WorkspaceFoldersOptions workspaceFoldersOptions = new WorkspaceFoldersOptions();
              workspaceFoldersOptions.setSupported(false);
              workspaceServerCapabilities.setWorkspaceFolders(workspaceFoldersOptions);
              serverCapabilities.setWorkspace(workspaceServerCapabilities);

              /*
               * see https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#initialize
               * (in ServerCapabilities)
               *
               * Used to specify for which file related operations the server wants to be notified.
               * For each file operation filters can be applied,
               * see https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#fileOperationRegistrationOptions
               *
               * TODO: activate all of them for now, let's see which we actually need in the future...
               */
              FileOperationsServerCapabilities fileOperationsCapabilities =
                  new FileOperationsServerCapabilities();
              fileOperationsCapabilities.setDidCreate(new FileOperationOptions());
              fileOperationsCapabilities.setDidDelete(new FileOperationOptions());
              fileOperationsCapabilities.setDidRename(new FileOperationOptions());
              fileOperationsCapabilities.setWillCreate(new FileOperationOptions());
              fileOperationsCapabilities.setWillDelete(new FileOperationOptions());
              fileOperationsCapabilities.setWillRename(new FileOperationOptions());
              workspaceServerCapabilities.setFileOperations(fileOperationsCapabilities);

              ServerInfo serverInfo = new ServerInfo("DungeonDSL LSP Server", "0.0.1");
              return new InitializeResult(serverCapabilities, serverInfo);
            });

    return result;
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    LOGGER.info("shutdown");
    return null;
  }

  @Override
  public void exit() {
    LOGGER.info("Exitting server");
    this.serverListening.cancel(true);
    try {
      this.socket.close();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Exception", e);
    }
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    LOGGER.info("getTextDocumentService");
    return this;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    LOGGER.info("getWorkspaceService");
    return this;
  }

  @Override
  public void connect(LanguageClient languageClient) {
    LOGGER.info("connect");
    clients.add(languageClient);
  }

  // region TextDocument Service
  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
      CompletionParams position) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    LOGGER.info("Param: '" + position + "'");

    return CompletableFuture.supplyAsync(
        () -> {
          List<CompletionItem> items = new ArrayList<>();

          // TODO: this is heavy
          CompletionItem item = new CompletionItem("HELLO, THIS IS MY LABEL");
          item.setData("ARBITRARY DATA OBJECT");
          item.setDetail("DETAIL");
          item.setDocumentation("DOCUMENTATION");
          item.setKind(CompletionItemKind.Text);
          item.setFilterText("Filter text");
          item.setInsertText("INSERT TEXT");
          item.setInsertTextMode(InsertTextMode.AsIs);
          item.setLabel("LABEL");
          item.setSortText("SORT TEXT");
          items.add(item);

          CompletionList completionList = new CompletionList(items);
          return Either.forRight(completionList);
        });
  }

  @Override
  public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    LOGGER.info("Param: '" + unresolved + "'");

    return CompletableFuture.supplyAsync(
        () -> {
          unresolved.setSortText("THIS IS NOW RESOLVED [SORT TEXT]");
          unresolved.setLabel("THIS IS NOW RESOLVED [LABEL]");
          unresolved.setFilterText("THIS IS NOW RESOLVED [FILTER TEXT]");
          return unresolved;
        });
  }

  @Override
  public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(
      DocumentHighlightParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.documentHighlight(params);
  }

  @Override
  public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
      DocumentSymbolParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.documentSymbol(params);
  }

  @Override
  public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.rename(params);
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    LOGGER.info("Param: '" + didOpenTextDocumentParams + "'");
    String text = didOpenTextDocumentParams.getTextDocument().getText();

    DSLInterpreter interpreter = new DSLInterpreter();
    try {
      DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(text);
    } catch (Exception ex) {
      // not relevant right now
    }

    var env = interpreter.getRuntimeEnvironment();
    var fileScope = env.getFileScopes().get(null);
    var parsedFile = fileScope.file();
    var ast = parsedFile.rootASTNode();
    var symTable = env.getSymbolTable();

    session.query("MATCH (n) DETACH DELETE n", Map.of());

    ProfilingTimer.Unit unit = ProfilingTimer.Unit.milli;
    HashMap<String, Long> times = new HashMap<>();
    try (ProfilingTimer timer = new ProfilingTimer("AST", times, unit)) {
      // save ast in db
      session.save(ast);
    }
    try (ProfilingTimer timer = new ProfilingTimer("SYMBOL CREATIONS", times, unit)) {
      session.save(symTable.getSymbolCreations());
    }
    try (ProfilingTimer timer = new ProfilingTimer("SYMBOL REFERENCES", times, unit)) {
      session.save(symTable.getSymbolReferences());
    }
    try (ProfilingTimer timer = new ProfilingTimer("GLOBAL SCOPE", times, unit)) {
      session.save(symTable.globalScope());
    }

    var filScopes = env.getFileScopes().entrySet();
    for (var entry : filScopes) {
      var scope = entry.getValue();
      try (ProfilingTimer timer = new ProfilingTimer("SCOPE " + scope.getName(), times, unit)) {
        session.save(scope);
      }
    }
  }

  @Override
  public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    LOGGER.info("Param: '" + didChangeTextDocumentParams + "'");
  }

  @Override
  public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    LOGGER.info("Param: '" + didCloseTextDocumentParams + "'");
  }

  @Override
  public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    LOGGER.info("Param: '" + didSaveTextDocumentParams + "'");
  }

  @Override
  public void willSave(WillSaveTextDocumentParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    LOGGER.info("Param: '" + params + "'");
  }

  @Override
  public CompletableFuture<List<TextEdit>> willSaveWaitUntil(WillSaveTextDocumentParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.willSaveWaitUntil(params);
  }

  @Override
  public CompletableFuture<List<ColorInformation>> documentColor(DocumentColorParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.documentColor(params);
  }

  @Override
  public CompletableFuture<List<ColorPresentation>> colorPresentation(
      ColorPresentationParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.colorPresentation(params);
  }

  @Override
  public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>>
      prepareRename(PrepareRenameParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.prepareRename(params);
  }

  @Override
  public CompletableFuture<List<SelectionRange>> selectionRange(SelectionRangeParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.selectionRange(params);
  }

  @Override
  public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.semanticTokensFull(params);
  }

  @Override
  public CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> semanticTokensFullDelta(
      SemanticTokensDeltaParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.semanticTokensFullDelta(params);
  }

  @Override
  public CompletableFuture<SemanticTokens> semanticTokensRange(SemanticTokensRangeParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return TextDocumentService.super.semanticTokensRange(params);
  }

  @Override
  public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());

    LOGGER.info("Identifier: '" + params.getIdentifier() + "'");
    LOGGER.info("TextDocumentIdentifier: '" + params.getTextDocument() + "'");
    LOGGER.info("PreviousResultId: '" + params.getPreviousResultId() + "'");

    return CompletableFuture.supplyAsync(
        () -> {
          var nodesWithErrorRecord =
            session.query(
              """
            match (n:Node {hasErrorRecord:TRUE}) return n
            """,
              Map.of());

          ArrayList<Node> nodes = new ArrayList<>();
          nodesWithErrorRecord.queryResults().forEach(r -> nodes.add((Node) r.get("n")));

          // TODO: translate diagnostics

          List<Diagnostic> diagnostics = new ArrayList<>();

          Range range = new Range(new Position(0, 0), new Position(0, 1));
          Diagnostic diagnostic =
              new Diagnostic(
                  range,
                  "THIS IS A TEST DIAGNOSTIC, param identifier was '"
                      + params.getIdentifier()
                      + "'",
                  DiagnosticSeverity.Error,
                  "Source: DungeonDSL LSP Server");
          diagnostics.add(diagnostic);

          RelatedFullDocumentDiagnosticReport fullReport =
              new RelatedFullDocumentDiagnosticReport(diagnostics);
          // RelatedUnchangedDocumentDiagnosticReport unchangedReport = new
          // RelatedUnchangedDocumentDiagnosticReport("idunno");
          return new DocumentDiagnosticReport(fullReport);
        });
  }

  // endregion

  // region WorkspaceService

  @Override
  public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return WorkspaceService.super.executeCommand(params);
  }

  @Override
  public CompletableFuture<
          Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>>
      symbol(WorkspaceSymbolParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return WorkspaceService.super.symbol(params);
  }

  @Override
  public CompletableFuture<WorkspaceSymbol> resolveWorkspaceSymbol(
      WorkspaceSymbol workspaceSymbol) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return WorkspaceService.super.resolveWorkspaceSymbol(workspaceSymbol);
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
  }

  @Override
  public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    WorkspaceService.super.didChangeWorkspaceFolders(params);
  }

  @Override
  public CompletableFuture<WorkspaceEdit> willCreateFiles(CreateFilesParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return WorkspaceService.super.willCreateFiles(params);
  }

  @Override
  public void didCreateFiles(CreateFilesParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    WorkspaceService.super.didCreateFiles(params);
  }

  @Override
  public CompletableFuture<WorkspaceEdit> willRenameFiles(RenameFilesParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return WorkspaceService.super.willRenameFiles(params);
  }

  @Override
  public void didRenameFiles(RenameFilesParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    WorkspaceService.super.didRenameFiles(params);
  }

  @Override
  public CompletableFuture<WorkspaceEdit> willDeleteFiles(DeleteFilesParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return WorkspaceService.super.willDeleteFiles(params);
  }

  @Override
  public void didDeleteFiles(DeleteFilesParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    WorkspaceService.super.didDeleteFiles(params);
  }

  @Override
  public CompletableFuture<WorkspaceDiagnosticReport> diagnostic(WorkspaceDiagnosticParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    return WorkspaceService.super.diagnostic(params);
  }

  // endregion
}
