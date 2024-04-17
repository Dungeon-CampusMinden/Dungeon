package main;

import static main.Helpers.getMethodName;
import static main.Main.LOGGER;

import dsl.antlr.ParseTracerForTokenType;
import dsl.antlr.TreeUtils;
import dsl.error.ErrorListener;
import dsl.error.ErrorRecord;
import dsl.error.ErrorRecordFactory;
import dsl.error.ErrorStrategy;
import dsl.helper.ProfilingTimer;
import dsl.interpreter.DSLInterpreter;
import dsl.neo4j.Neo4jConnect;
import dsl.parser.ast.*;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.Symbol;
import entrypoint.DungeonConfig;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.*;
import org.neo4j.driver.Driver;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

public class LspServer
    implements LanguageServer, LanguageClientAware, TextDocumentService, WorkspaceService {
  private final List<LanguageClient> clients = new ArrayList<>();
  private final Socket socket;
  private final Future<Void> serverListening;
  private final Session session;
  private final DBUpdater dbUpdater = new DBUpdater();

  private GameEnvironment environment;
  private Driver neo4jDriver;
  private SessionFactory sessionFactory;
  private ReentrantLock dbMutex = new ReentrantLock();
  private ReentrantLock docVersionMutex = new ReentrantLock();
  private HashMap<String, Long> documentDiagnosticVersionMap = new HashMap<>();
  private ReentrantLock documentCalculationsMutex = new ReentrantLock();
  private HashMap<String, CompletableFuture<Void>> documentAnalysisFutures = new HashMap<>();

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
    this.clients.add(launcher.getRemoteProxy());
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

              // DiagnosticRegistrationOptions diagnosticOptions = new
              // DiagnosticRegistrationOptions();
              // diagnosticOptions.setId("DungeonDSL Diagnostics ID");
              // diagnosticOptions.setIdentifier("DungeonDSL Diagnostics identifier");

              // // specifies, whether the language has inter-file dependencies, which means that
              // // editing code in one file can lead to a diagnostic in another file
              // diagnosticOptions.setInterFileDependencies(true);
              // serverCapabilities.setDiagnosticProvider(diagnosticOptions);

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

  // TODO: add version
  public void publishDiagnostics(String uri, List<Diagnostic> diagnostics) {
    // Diagnostics notifications are sent from the server to the client to signal results of
    // validation runs.
    //
    // Diagnostics are “owned” by the server so it is the server’s responsibility to clear them if
    // necessary.
    // The following rule is used for VS Code servers that generate diagnostics:
    //
    // - if a language is single file only (for example HTML) then diagnostics are cleared by the
    // server when
    //   the file is closed. Please note that open / close events don’t necessarily reflect what the
    // user sees
    //   in the user interface.
    //   These events are ownership events. So with the current version of the specification it is
    // possible that
    //   problems are not cleared although the file is not visible in the user interface since the
    // client
    //   has not closed the file yet.
    // - if a language has a project system (for example C#) diagnostics are not cleared when a file
    // closes.
    //   When a project is opened all diagnostics for all files are recomputed (or read from a
    // cache).
    //
    // When a file changes it is the server’s responsibility to re-compute diagnostics and push them
    // to the
    // client. If the computed set is empty it has to push the empty array to clear former
    // diagnostics.
    // Newly pushed diagnostics always replace previously pushed diagnostics. There is no merging
    // that
    // happens on the client side.

    LOGGER.info("Publishing [" + diagnostics.size() + "]diagnostics for file: " + uri);
    var client = this.clients.get(0);
    PublishDiagnosticsParams params = new PublishDiagnosticsParams();
    params.setDiagnostics(diagnostics);
    params.setUri(uri);
    // params.setVersion(version);
    client.publishDiagnostics(params);
  }

  public void analyzeFile(String uri, String text) {
    Long version;
    if (!this.documentDiagnosticVersionMap.containsKey(uri)) {
      this.documentDiagnosticVersionMap.put(uri, 0L);
      version = 0L;
      ErrorRecord.setDocumentVersion(version);
    } else {
      version = this.documentDiagnosticVersionMap.get(uri);
    }

    LOGGER.info("Document version: [" + version + "]");

    documentCalculationsMutex.lock();

    if (this.documentAnalysisFutures.containsKey(uri)) {
      var previousCalculation = this.documentAnalysisFutures.get(uri);
      if (previousCalculation.isDone() && !previousCalculation.isCancelled()) {
        this.documentAnalysisFutures.remove(uri);
      } else {
        if (dbUpdater.isRunning()) { // db update is in progress
          dbUpdater.stop();
          dbMutex.lock(); // wait for updater to stop
          dbMutex.unlock();
          // TODO: wait for dbUpdater to actually to stop, how to do this in simple way?
          //  dbMutex is fine for now...
        }
        LOGGER.warning("Interrupting previous analysis!");
        previousCalculation.cancel(true);

        this.documentAnalysisFutures.remove(uri);
      }
      dbUpdater.reset();
    }
    dbMutex.lock();

    // TODO: should make this cancelable!
    // TODO: more testing required....
    CompletableFuture<Void> analyzeDocumentFuture =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                // this is a problem, because the docVersionMutex won't be unlocked, if the future
                // gets cancelled
                // ...but this isn't a problem anymore, bzw. the mutex isnt' needed anymore, because
                // the whole computation
                // of the diagnostics is performed sequentially after update of the database..
                // future changes will likely require modifications, but just make it work for now
                // this.docVersionMutex.lock();

                this.documentDiagnosticVersionMap.compute(uri, (k, versionIdx) -> versionIdx + 1L);
                Long newVersion = this.documentDiagnosticVersionMap.get(uri);
                ErrorRecord.setDocumentVersion(newVersion);
                LOGGER.info("Updated document version PRE UPDATE: [" + newVersion + "]");

                dbUpdater.updateDB(text);

                this.documentDiagnosticVersionMap.compute(uri, (k, versionIdx) -> versionIdx + 1L);
                newVersion = this.documentDiagnosticVersionMap.get(uri);
                ErrorRecord.setDocumentVersion(newVersion);
                LOGGER.info("Updated document version POST UPDATE: [" + newVersion + "]");

                List<Diagnostic> diagnostics = computeDiagnostics(uri);
                publishDiagnostics(uri, diagnostics);

                // this.docVersionMutex.unlock();
              } catch (InterruptedException ignored) {
              }
              return null;
            });

    this.documentAnalysisFutures.put(uri, analyzeDocumentFuture);
    documentCalculationsMutex.unlock();

    dbMutex.unlock();
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    // LOGGER.info("Param: '" + didOpenTextDocumentParams + "'");
    String text = didOpenTextDocumentParams.getTextDocument().getText();
    String uri = didOpenTextDocumentParams.getTextDocument().getUri();

    analyzeFile(uri, text);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    // LOGGER.info("Param: '" + didChangeTextDocumentParams + "'");

    String text = didChangeTextDocumentParams.getContentChanges().get(0).getText();
    String uri = didChangeTextDocumentParams.getTextDocument().getUri();

    analyzeFile(uri, text);
    // TODO:
    //  var client = this.clients.get(0);
    //  PublishDiagnosticsParams params = new PublishDiagnosticsParams();
    //  params.set
    //  client.publishDiagnostics();
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

  private class DBUpdater {
    private boolean stop;
    private boolean isRunning;

    public DBUpdater() {}

    public void stop() {
      this.stop = true;
    }

    public boolean isRunning() {
      return this.isRunning;
    }

    public void reset() {
      this.stop = false;
      this.isRunning = false;
    }

    private void throwIfStop() throws InterruptedException {
      if (this.stop) {
        throw new InterruptedException();
      }
    }

    private void updateDB(String text) throws InterruptedException {
      this.isRunning = true;
      dbMutex.lock();

      // TODO: this is a temporary solution to the permanence problem of error nodes and error
      // records..
      ErrorRecordFactory.instance.clear();

      DSLInterpreter interpreter = new DSLInterpreter();
      interpreter.setTrace(false);
      try {
        // TODO: temporary test...
        RelationshipRecorder.instance.clear();

        DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(text);
      } catch (Exception ex) {
        // not relevant right now
      }

      LOGGER.fine(getPrettyPrintedParseTree(text, new GameEnvironment()));

      boolean interrupted = false;
      var tx = session.beginTransaction(Transaction.Type.READ_WRITE);
      try {
        var env = interpreter.getRuntimeEnvironment();
        var fileScope = env.getFileScopes().get(null);
        var parsedFile = fileScope.file();
        var ast = parsedFile.rootASTNode();
        var symTable = env.getSymbolTable();
        var nodeRelationShips = RelationshipRecorder.instance.get();

        // session.query("MATCH (n) DETACH DELETE n", Map.of());

        LOGGER.info("Updating program database...");
        throwIfStop();
        // non-interuptable!
        session.deleteAll(ParentOf.class);
        session.deleteAll(Node.class);
        session.deleteAll(Symbol.class);
        session.deleteAll(IScope.class);
        session.deleteAll(ErrorRecord.class);
        throwIfStop();

        ProfilingTimer.Unit unit = ProfilingTimer.Unit.milli;
        HashMap<String, Long> times = new HashMap<>();
        // try (ProfilingTimer timer = new ProfilingTimer("AST", times, unit)) {
        // save ast in db
        throwIfStop();
        session.save(ast);

        throwIfStop();
        session.save(nodeRelationShips);
        // }
        // try (ProfilingTimer timer = new ProfilingTimer("SYMBOL CREATIONS", times, unit)) {
        throwIfStop();
        session.save(symTable.getSymbolCreations());
        // }
        // try (ProfilingTimer timer = new ProfilingTimer("SYMBOL REFERENCES", times, unit)) {
        throwIfStop();
        session.save(symTable.getSymbolReferences());
        // }
        // try (ProfilingTimer timer = new ProfilingTimer("GLOBAL SCOPE", times, unit)) {
        throwIfStop();
        session.save(symTable.globalScope());
        // }

        var filScopes = env.getFileScopes().entrySet();
        for (var entry : filScopes) {
          var scope = entry.getValue();
          // try (ProfilingTimer timer = new ProfilingTimer("SCOPE " + scope.getName(), times,
          // unit)) {
          throwIfStop();
          session.save(scope);
          // }
        }
        tx.commit();
        LOGGER.info("Finished updating database!");
      } catch (InterruptedException interrupt) {
        LOGGER.info("Database update was interrupted");
        tx.rollback();
        interrupted = true;
      } catch (Exception ex) {
        boolean b = true;
      } finally {
        tx.close();
        dbMutex.unlock();
        this.isRunning = false;
      }
      if (interrupted) {
        throw new InterruptedException();
      }
    }
  }

  private List<Diagnostic> computeDiagnostics(String fileUri) {
    Long documentVersion = this.documentDiagnosticVersionMap.get(fileUri);

    ArrayList<Node> nodesWithErrorRecord = new ArrayList<>();
    ArrayList<ErrorRecord> errorRecords = new ArrayList<>();
    ArrayList<ASTErrorNode> errorNodes = new ArrayList<>();

    // fetch all necessary data from database while ensuring that the database was not updated in
    // the meantime
    LOGGER.info("Fetching data from database with document version [" + documentVersion + "]...");
    Result queryResult;
    try {
      LOGGER.info("Getting nodes with error record from database...");
      queryResult =
          session.query(
              """
          match (n:Node)-[:HAS_ERROR_RECORD]->(e:ErrorRecord) return distinct e
          """,
              Map.of());
      // queryResult.queryResults().forEach(r -> nodesWithErrorRecord.add((Node) r.get("n")));
      queryResult.queryResults().forEach(r -> errorRecords.add((ErrorRecord) r.get("e")));
    } catch (ClassCastException ex) {
      boolean b = true;
    } catch (CompletionException compEx) {
      boolean b = true;
    } finally {
      LOGGER.info("Finished getting nodes with error record!");
    }

    /*try {
      LOGGER.info("Getting ASTErrorNodes from database...");
      var nodesWithErrorChild =
        session.query(
          """
            match (n:ASTErrorNode)-[:CHILD_OF]->(p:Node) return n,p
            """,
          Map.of());

      nodesWithErrorChild
        .queryResults()
        .forEach(r -> errorNodes.add((ASTErrorNode) r.get("n")));
    } finally {
      LOGGER.info("Finished getting ASTErrorNodes from database!");
    }*/

    // get document version after fetching; if this version is different from the pre fetch version,
    // fetch the data again!
    LOGGER.info("Finished fetching data from database!");

    // compute diagnostics
    List<Diagnostic> diagnostics = new ArrayList<>();

    for (var errorRecord : errorRecords) {
      Position start;
      Position end;
      var line = errorRecord.line();
      var charPosition = errorRecord.charPositionInLine();
      start = new Position(line - 1, charPosition);
      if (errorRecord.offendingSymbol() != null) {
        var offendingSymbol = errorRecord.offendingSymbol();
        boolean b = true;
      }

      end = new Position(line - 1, charPosition);

      Range range = new Range(start, end);

      String errorType = errorRecord.errorType().toString();
      String internalMsg = errorRecord.msg() != null ? errorRecord.msg() : "";
      String msg = String.format("Error of type '%s' - '%s'", errorType, internalMsg);

      // String msg = errorRecord.msg() != null ? errorRecord.msg() : "LEXER ERROR!";
      Diagnostic diagnostic =
          new Diagnostic(range, msg, DiagnosticSeverity.Error, "Source: DungeonDSL LSP Server");
      diagnostics.add(diagnostic);
    }

    // nodes with error records
    for (var node : nodesWithErrorRecord) {
      // get range for error
      Position start = new Position(0, 0);
      Position end = new Position(0, 0);
      boolean setStart = false;
      /*if (node instanceof ASTOffendingSymbol) {
        // get parent
        var parent = node.getParent();
        // get all children before current node
        for (var child : parent.getChildren()) {
          if (!setStart) {
            if (child.getChildren().size() == 0) { // no children, likely terminal node
              int line = child.getSourceFileReference().getLine() - 1;
              int column = child.getSourceFileReference().getColumn();
              start.setLine(line);
              start.setCharacter(column);
              setStart = true;
              break;
            } else {
              // recursively go down the children of child
              Node currentNode = child;
              while (currentNode.getChildren().size() > 0) {
                currentNode = currentNode.getChild(0);
              }
              int line = currentNode.getSourceFileReference().getLine() - 1;
              int column = currentNode.getSourceFileReference().getColumn();
              start.setLine(line);
              start.setCharacter(column);
              setStart = true;
              break;
            }
          }
        }
      }*/

      var errorRecord = node.getErrorRecord();
      if (errorRecord == null) {
        continue;
      }

      if (!setStart) {
        var line = errorRecord.line();
        var charPosition = errorRecord.charPositionInLine();
        start = new Position(line - 1, charPosition);
        if (errorRecord.offendingSymbol() != null) {
          var offendingSymbol = errorRecord.offendingSymbol();
          boolean b = true;
        }
      }

      var line = errorRecord.line();
      var charPosition = errorRecord.charPositionInLine();
      end = new Position(line - 1, charPosition);

      Range range = new Range(start, end);
      String msg = errorRecord.msg() != null ? errorRecord.msg() : "LEXER ERROR!";
      Diagnostic diagnostic =
          new Diagnostic(range, msg, DiagnosticSeverity.Error, "Source: DungeonDSL LSP Server");
      diagnostics.add(diagnostic);
    }

    // get Parsing errors (ASTErrorNode)
    for (var node : errorNodes) {
      var internalErrorNode = node.internalErrorNode();
      if (internalErrorNode == null) {
        boolean b = true;
      }

      if (node.getErrorRecord() == null) {
        boolean b = true;
      } else {
        var errorRecord = node.getErrorRecord();
        // var sourceInterval = internalErrorNode.getSourceInterval();
        // to use source interval, we need the lexer output.. i guess
        // var symbol = internalErrorNode.getSymbol();
        // var tokenSource = symbol.getTokenSource();
        int line = errorRecord.line() - 1; // symbol.getLine() - 1;
        int charInLine = errorRecord.charPositionInLine(); // symbol.getCharPositionInLine();

        // get range for error
        Position start = new Position(line, charInLine);
        Position end = new Position(line, charInLine);

        Range range = new Range(start, end);
        String msg = errorRecord.msg(); // internalErrorNode.toString();
        Diagnostic diagnostic =
            new Diagnostic(range, msg, DiagnosticSeverity.Error, "Source: DungeonDSL LSP Server");
        diagnostics.add(diagnostic);
      }
    }
    return diagnostics;
  }

  @Override
  // NOTE: not used in current implementation
  public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
    LOGGER.entering(this.getClass().getName(), getMethodName());

    LOGGER.info("DIAGNOSTIC");
    LOGGER.info("Identifier: '" + params.getIdentifier() + "'");
    LOGGER.info("TextDocumentIdentifier: '" + params.getTextDocument() + "'");
    LOGGER.info("PreviousResultId: '" + params.getPreviousResultId() + "'");

    return CompletableFuture.supplyAsync(
        () -> {
          List<Diagnostic> diagnostics = computeDiagnostics(params.getTextDocument().getUri());

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

  // helpers

  public static String getPrettyPrintedParseTree(
      String program, IEnvironment environment, boolean trace) {
    ErrorListener el = new ErrorListener();
    var stream = CharStreams.fromString(program);
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, environment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);
    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, environment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    if (trace) {
      ParseTracerForTokenType ptftt = new ParseTracerForTokenType(parser);
      parser.addParseListener(ptftt);
    }
    parser.setTrace(trace);
    var programParseTree = parser.program();
    return TreeUtils.toPrettyTree(
        programParseTree, java.util.Arrays.stream(parser.getRuleNames()).toList());
  }

  public static String getPrettyPrintedParseTree(String program, IEnvironment environment) {
    return getPrettyPrintedParseTree(program, environment, false);
  }
}
