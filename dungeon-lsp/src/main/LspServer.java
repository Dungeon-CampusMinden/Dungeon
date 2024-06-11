package main;

import static main.Helpers.getMethodName;
import static main.Main.LOGGER;

import dsl.antlr.ParseTracerForTokenType;
import dsl.antlr.TreeUtils;
import dsl.error.ErrorListener;
import dsl.error.ErrorRecord;
import dsl.error.ErrorStrategy;
import dsl.helper.ProfilingTimer;
import dsl.neo4j.Neo4jConnect;
import dsl.parser.ast.*;
import dsl.programmanalyzer.ProgrammAnalyzer;
import dsl.programmanalyzer.Relate;
import dsl.programmanalyzer.RelationshipRecorder;
import dsl.runtime.callable.ExtensionMethod;
import dsl.runtime.callable.NativeMethod;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.PropertySymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.*;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
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
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

public class LspServer
    implements LanguageServer, LanguageClientAware, TextDocumentService, WorkspaceService {
  private final List<LanguageClient> clients = new ArrayList<>();
  private final Socket socket;
  private final Future<Void> serverListening;
  private Session session;
  private final DBUpdater dbUpdater = new DBUpdater();
  private final DBAccessor dbAccessor;

  private ProgrammAnalyzer programmAnalyzer = new ProgrammAnalyzer(false);
  private GameEnvironment environment;
  private Driver neo4jDriver;
  private SessionFactory sessionFactory;
  // TODO: use ReentrantReadWriteLock
  private ReentrantLock dbMutex = new ReentrantLock();
  private CountDownLatch dbLatch = new CountDownLatch(1);
  private ReentrantLock docVersionMutex = new ReentrantLock();
  private HashMap<String, Long> documentDiagnosticVersionMap = new HashMap<>();
  private ReentrantLock documentCalculationsMutex = new ReentrantLock();
  private HashMap<String, CompletableFuture<Void>> documentAnalysisFutures = new HashMap<>();

  private final PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
  private final ExecutorService EXECUTOR =
      new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, this.queue);

  public LspServer(int socketPort) throws IOException {
    this.neo4jDriver = Neo4jConnect.openConnection();

    // purge database
    var rawSession = this.neo4jDriver.session();
    rawSession.run("match (n) detach delete n");
    rawSession.close();

    this.sessionFactory = Neo4jConnect.getSessionFactory(neo4jDriver);
    this.session = sessionFactory.openSession();
    this.session.clear();
    // DeletionEventListener listener = new DeletionEventListener();
    // this.session.register(listener);

    this.dbAccessor = new DBAccessor(session);

    this.socket = new Socket("127.0.0.1", socketPort);
    Launcher<LanguageClient> launcher;
    launcher =
        LSPLauncher.createServerLauncher(
            this, this.socket.getInputStream(), this.socket.getOutputStream());
    this.clients.add(launcher.getRemoteProxy());
    this.serverListening = launcher.startListening();
    LOGGER.info("Started listening");

    this.dbUpdater.initializeDB();
  }

  public <V> CompletableFuture<V> runAsync(Task<V> task) {
    CompletableFuture<V> result = new CompletableFuture<>();

    class Job implements Runnable, Comparable<Job>, CompletableFuture.AsynchronousCompletionTask {
      public void run() {
        try {
          if (!result.isDone()) {
            result.complete(task.call());
          }
        } catch (Throwable t) {
          result.completeExceptionally(t);
        }
      }

      private Task.Priority priority() {
        return task.getPriority();
      }

      public int compareTo(Job o) {
        return priority().compareTo(o.priority());
      }
    }

    LOGGER.info("Submitting task " + task);
    LOGGER.info("Queue: " + Arrays.toString(this.queue.toArray()));
    this.EXECUTOR.execute(new Job());
    return result;
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
              // completionOptions.setResolveProvider(true);
              completionOptions.setResolveProvider(false);

              /*
               * see https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#completionItemLabelDetails
               *
               * The server supports additional details for a completion item label
               *
               * TODO: currently not used, let's see about that in the future.
               */
              // CompletionItemOptions completionItemOptions = new CompletionItemOptions();
              // completionItemOptions.set

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

              // custom commands
              ExecuteCommandOptions executeCommandOptions = new ExecuteCommandOptions();
              executeCommandOptions.setCommands(List.of("dungeon-lsp/patternCompletion"));
              serverCapabilities.setExecuteCommandProvider(executeCommandOptions);

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

  private CompletionItemKind symbolToCompletionItemKind(Symbol symbol) {
    if (symbol instanceof PropertySymbol) {
      return CompletionItemKind.Property;
    } else if (symbol instanceof FunctionSymbol) {
      return CompletionItemKind.Function;
    } else if (symbol instanceof ExtensionMethod || symbol instanceof NativeMethod) {
      return CompletionItemKind.Method;
    } else if (symbol instanceof EnumType) {
      return CompletionItemKind.Enum;
    } else if (symbol instanceof IType) {
      return CompletionItemKind.Class;
    } else {
      return CompletionItemKind.Variable;
    }
  }

  private String resolveTypeRestriction(
      Node matchedNode, Node restrictingNode, String restrictionType) {
    switch (restrictionType) {
      case "PropertyDefinition":
        // TODO: Problem -> no alternative for incomplete definitions..
        break;
      case "ExpressionList":
        // Note: this may either be in a function call, or in a definition of list or set value
        // -> question: is that even matched correctly?
        break;
      case "ReturnStmt":
        {
          var result =
              session.query(
                  """
          // resolve return stmt type restriction
          match (n:AstNode) where n.idx=$returnNodeIdx
          match (n)-[:CHILD_OF*]->(funcNode:FuncDefNode)-[:CREATES]->(funcSym:ScopedSymbol)-[:OF_TYPE]->(funcType:FunctionType)
          optional match (funcType)-[:RETURN_TYPE]->(returnType:IType)
          //return n, funcNode, funcSym, funcType, returnType
          return returnType
          """,
                  Map.of("returnNodeIdx", restrictingNode.getId()));
          var iter = result.iterator();
          if (iter.hasNext()) {
            var map = iter.next();
            var type = map.get("returnType");
            if (type != null) {
              return ((IType) type).getName();
            }
          }
          return "";
        }
      case "Assignment":
        {
          // we need to check, whether the matched node (for which the completion was triggered) is
          // on the lhs or the rhs of the assignment
          var lhsOrRhs =
              session.query(
                  """
          match (parentNode:AstNode) where parentNode.internalId=$restrictingNodeIdx
          match (parentNode)-[childEdge:PARENT_OF]->(directChild:AstNode)
          call {
            with directChild
            match (directChild) where directChild.internalId=$matchedNodeIdx return directChild as child
            union
            with directChild
            match (child)-[:CHILD_OF*]->(directChild) where child.internalId=$matchedNodeIdx return child
          }
          return child.internalId as childIdx, child, childEdge.idx as edgeIdx
          """,
                  Map.of(
                      "restrictingNodeIdx",
                      restrictingNode.getId(),
                      "matchedNodeIdx",
                      matchedNode.getId()));
          var iter = lhsOrRhs.iterator();
          if (!iter.hasNext()) {
            return "";
          }
          var result = lhsOrRhs.iterator().next();

          var lhsOrRhsIdx = (Long) result.get("edgeIdx");

          String query;
          if (lhsOrRhsIdx == 0L) {
            // lhs -> get rhs type and use that as a restriction
            // two steps: if the other child is an error node, there is no valid id and the matched
            // id was matched on
            // the other side of assignment
            query =
                """
            match (parentNode:AstNode) where parentNode.internalId=$restrictingNodeIdx
            match (parentNode)-[childEdge:PARENT_OF]->(directChild:AstNode) where childEdge.idx=1
            //match (directChild)-[:REFERENCES]->(symbol:Symbol)-[:OF_TYPE]->(type:IType)
            //return directChild, symbol, type
            return directChild
            //limit 1
            """;
          } else {
            // rhs -> get lhs type and use that as a restriction
            query =
                """
            match (parentNode:AstNode) where parentNode.internalId=$restrictingNodeIdx
            match (parentNode)-[childEdge:PARENT_OF]->(directChild:AstNode) where childEdge.idx=0
            //match (directChild)-[:REFERENCES]->(symbol:Symbol)-[:OF_TYPE]->(type:IType)
            //return directChild, symbol, type
            return directChild
            //return type
            limit 1
            """;
          }

          var directChild =
              session.queryForObject(
                  Node.class, query, Map.of("restrictingNodeIdx", restrictingNode.getId()));

          // TODO: handle error node
          if (directChild.type == Node.Type.ErrorNode) {
            // get
            query =
                """
              match (parentNode:AstNode) where parentNode.internalId=$restrictingNodeIdx
              match (parentNode)-[childEdge:PARENT_OF]->(directChild:AstNode) where childEdge.idx=$edgeIdx
              match (directChild)-[:REFERENCES]->(symbol:Symbol)-[:OF_TYPE]->(type:IType)
              //return directChild, symbol, type
              //return directChild
              return type
              limit 1
              """;
            var otherSideType =
                session.queryForObject(
                    IType.class,
                    query,
                    Map.of("restrictingNodeIdx", restrictingNode.getId(), "edgeIdx", lhsOrRhsIdx));
            return otherSideType.getName();

          } else {
            // use the type of the direct child
            query =
                """
              match (directChild:AstNode) where directChild.internalId=$childInternalId
              match (directChild)-[:REFERENCES]->(symbol:Symbol)-[:OF_TYPE]->(type:IType)
              return type
              limit 1
              """;
            var otherSideType =
                session.queryForObject(
                    IType.class, query, Map.of("childInternalId", directChild.getId()));
            return otherSideType.getName();
          }
        }
      default:
        break;
    }
    return "";
  }

  // region TextDocument Service
  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
      CompletionParams completionParams) {
    LOGGER.entering(this.getClass().getName(), getMethodName());
    LOGGER.info("Param: '" + completionParams + "'");

    var task =
        new Task<Either<List<CompletionItem>, CompletionList>>(
            () -> {
              boolean isIncomplete = false;
              List<CompletionItem> items = new ArrayList<>();
              try {
                // resolve completion context
                var context = completionParams.getContext();
                var position = completionParams.getPosition();
                var nodeMap = dbAccessor.resolveContextToNearestAstNode(position);

                var astNode = (Node) nodeMap.get("n");
                var sfr = (SourceFileReference) nodeMap.get("nearestSfr");
                var parentNode = (Node) nodeMap.get("parent");
                var parentSfr = (SourceFileReference) nodeMap.get("parentSfr");
                var typeRestrictionContext = (Node) nodeMap.get("typeRestrictingContext");
                String restrictionType = (String) nodeMap.get("restrictionType");

                // resolve restriction type (based on type of restriction)
                String typeRestriction = "";
                if (typeRestrictionContext != Node.NONE) {
                  // TODO: this means, that the types of the resolved symbols need to be assignable
                  // to the typeRestriction
                  //  -> the notion of 'assignable' is not really a thing right now...
                  typeRestriction =
                      resolveTypeRestriction(astNode, typeRestrictionContext, restrictionType);
                }

                String matchedText;
                if (astNode == Node.NONE) {
                  var map = dbAccessor.sparseContextResolving(position);
                  astNode = (Node) map.get("n");
                  sfr = (SourceFileReference) map.get("nearestSfr");
                  parentNode = (Node) map.get("parent");
                  parentSfr = (SourceFileReference) map.get("parentSfr");
                  typeRestrictionContext = (Node) map.get("typeRestrictingContext");
                  restrictionType = (String) map.get("restrictionType");
                  matchedText = (String) map.get("matchedText");
                  // TODO: test
                }

                if (astNode == Node.NONE) {
                  throw new RuntimeException("No AST node matched!");
                }

                List<RankedSymbol> rankedSymbols = new ArrayList<>();

                String triggerCharacter = context.getTriggerCharacter();
                // TODO: complete all cases!
                if (triggerCharacter == null || triggerCharacter.isEmpty()) {
                  // the completion request was triggered by normal typing without special trigger
                  // character
                  // which means that we generate completion items for the nearest scope
                  if (parentNode.type == Node.Type.MemberAccess) {
                    LOGGER.info("Completion, no explicit trigger, context: member access");

                    // get symbols from scope of lhs of member access
                    String prefix;
                    Position startPosition;

                    // need to clarify, if the returned id is lhs or rhs of the member access node
                    var childIdxs = dbAccessor.getChildIdxsOfMemberAccess(parentNode);
                    if (childIdxs.getFirst().equals(astNode.getId())) {
                      // astNode is the lhs of the member access
                      prefix = "";
                      // position should be the end line of member access
                      startPosition =
                          new Position(parentSfr.getEndLine(), parentSfr.getEndColumn() + 1);
                    } else {
                      prefix =
                          astNode.type == Node.Type.Identifier ? ((IdNode) astNode).getName() : "";
                      startPosition = new Position(sfr.getStartLine(), sfr.getStartColumn());
                    }

                    rankedSymbols =
                        dbAccessor.getSymbolsInScopeOfLhsIdentifierWithPrefix(
                            parentNode, prefix, typeRestriction);

                    symbolsToCompletionItems(items, position, rankedSymbols, prefix, startPosition);
                  } else if (parentNode.type == Node.Type.Assignment) {
                    LOGGER.info("Completion, no explicit trigger, context: assignment");
                    // TODO: should enable fallback, if lhs (or rhs) has no type

                    Position startPosition;
                    String prefix;
                    var childIdxs = dbAccessor.getChildIdxsOfNode(parentNode);
                    if (childIdxs.getFirst().equals(astNode.getId())) {
                      // astNode is the lhs of the member access
                      prefix = "";
                      // position should be the end line of member access
                      startPosition =
                          new Position(parentSfr.getEndLine(), parentSfr.getEndColumn() + 1);
                    } else {
                      prefix =
                          astNode.type == Node.Type.Identifier ? ((IdNode) astNode).getName() : "";
                      startPosition = new Position(sfr.getStartLine(), sfr.getStartColumn());
                    }

                    rankedSymbols =
                        dbAccessor.getAllSymbolsOfTypeInScopeAndParentScopes(
                            astNode, typeRestriction, prefix);

                    symbolsToCompletionItems(items, position, rankedSymbols, prefix, startPosition);
                  } else {
                    // get symbols from parent scope and all it's parent scopes
                    String prefix =
                        astNode.type == Node.Type.Identifier ? ((IdNode) astNode).getName() : "";
                    rankedSymbols =
                        dbAccessor.getAllSymbolsInScopeAndParentScopes(
                            astNode, typeRestriction, prefix); // TODO: type restriction

                    Position startPosition = new Position(sfr.getStartLine(), sfr.getStartColumn());

                    symbolsToCompletionItems(items, position, rankedSymbols, prefix, startPosition);
                  }
                } else {
                  if (triggerCharacter.equals(".")) {
                    LOGGER.info("Completion, dot trigger");
                    // member access, treat node as scope
                    rankedSymbols =
                        dbAccessor.getSymbolsInScopeOfIdentifier(astNode, typeRestriction);

                    for (var rankedSymbol : rankedSymbols) {
                      // TODO: documentation
                      var symbol = rankedSymbol.symbol();
                      CompletionItem item = new CompletionItem(symbol.getName());
                      CompletionItemKind kind = symbolToCompletionItemKind(symbol);
                      item.setKind(kind);
                      if (!kind.equals(CompletionItemKind.Class)) {
                        // item.setDetail("type: " + rankedSymbol.symbolType().getName());
                        var details = new CompletionItemLabelDetails();
                        // details.setDetail("type: " + rankedSymbol.symbolType().getName());
                        details.setDescription("  " + rankedSymbol.symbolType().getName());
                        item.setLabelDetails(details);
                      }

                      TextEdit textEdit =
                          new TextEdit(new Range(position, position), symbol.getName());
                      item.setTextEdit(Either.forLeft(textEdit));
                      item.setSortText("00" + (9 - rankedSymbol.ranking()));

                      items.add(item);
                    }
                  }
                }
                LOGGER.info("Found " + rankedSymbols.size() + " symbols for completion request!");

              } catch (Exception ex) {
                LOGGER.severe(ex.toString());
              }

              boolean setList = true;
              if (setList) {
                CompletionList completionList = new CompletionList(items);
                completionList.setIsIncomplete(false);
                return Either.forRight(completionList);
              } else {
                return Either.forLeft(items);
              }
            },
            Task.Priority.LOW,
            "Completion " + completionParams);
    return runAsync(task);
  }

  private void symbolsToCompletionItems(
      List<CompletionItem> items,
      Position position,
      List<RankedSymbol> rankedSymbols,
      String prefix,
      Position startPosition) {
    for (var rankedSymbol : rankedSymbols) {
      var symbol = rankedSymbol.symbol();
      CompletionItem item = new CompletionItem(symbol.getName());
      CompletionItemKind kind = symbolToCompletionItemKind(symbol);
      item.setKind(kind);
      item.setLabel("Label: " + symbol.getName());
      if (!kind.equals(CompletionItemKind.Class)) {
        // item.setDetail("type: " + rankedSymbol.symbolType().getName());
        var details = new CompletionItemLabelDetails();
        // details.setDetail("type: " + rankedSymbol.symbolType().getName());
        details.setDescription("  " + rankedSymbol.symbolType().getName());
        item.setLabelDetails(details);
      }
      // start
      TextEdit textEdit = new TextEdit(new Range(startPosition, position), symbol.getName());
      item.setTextEdit(Either.forLeft(textEdit));
      item.setSortText("00" + (9 - rankedSymbol.ranking()));

      items.add(item);
    }
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

    LOGGER.info("ANALYSE FILE - LOCKING CALCULATIONS LOCK");
    documentCalculationsMutex.lock();

    if (this.documentAnalysisFutures.containsKey(uri)) {
      var previousCalculation = this.documentAnalysisFutures.get(uri);
      if (previousCalculation.isDone() && !previousCalculation.isCancelled()) {
        this.documentAnalysisFutures.remove(uri);
      } else {
        if (dbUpdater.isRunning()) { // db update is in progress
          dbUpdater.stop();

          LOGGER.info("INTERRUPTING ANALYSIS - AWAITING LATCH");
          // dbMutex.lock(); // wait for updater to stop
          // LOGGER.info("INTERRUPTING ANALYSIS - UNLOCKING DB");
          // dbMutex.unlock();
          // TODO: wait for dbUpdater to actually to stop, how to do this in simple way?
          //  dbMutex is fine for now...
          try {
            dbLatch.await();
          } catch (InterruptedException exception) {
            LOGGER.severe("Interruption while awaiting latch");
          }
          LOGGER.info("INTERRUPTING ANALYSIS - PASSED LATCH");
        }
        LOGGER.warning("Interrupting previous analysis!");
        previousCalculation.cancel(true);

        this.documentAnalysisFutures.remove(uri);
      }
      dbUpdater.reset();
    }

    var task =
        new Task<Void>(
            () -> {
              dbLatch = new CountDownLatch(1);
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

                dbUpdater.updateDB(text, uri);

                this.documentDiagnosticVersionMap.compute(uri, (k, versionIdx) -> versionIdx + 1L);
                newVersion = this.documentDiagnosticVersionMap.get(uri);
                ErrorRecord.setDocumentVersion(newVersion);
                LOGGER.info("Updated document version POST UPDATE: [" + newVersion + "]");

                List<Diagnostic> diagnostics = computeDiagnostics(uri);
                publishDiagnostics(uri, diagnostics);

                // this.docVersionMutex.unlock();
              } catch (InterruptedException ignored) {

              } catch (Exception other) {
                LOGGER.severe(other.getMessage());
              } finally
               {
                LOGGER.info("Counting down latch");
                dbLatch.countDown();
              }
              return null;
            },
            Task.Priority.HIGH,
            "ANALYZE FILE " + uri);

    CompletableFuture<Void> analyzeDocumentFuture = runAsync(task);

    this.documentAnalysisFutures.put(uri, analyzeDocumentFuture);
    LOGGER.info("ANALYSE FILE - UNLOCKING CALCULATIONS LOCK");
    documentCalculationsMutex.unlock();
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

    // TODO: direction!!
    private void saveRelationship(dsl.programmanalyzer.Relationship relationship) {
      String name = relationship.name();
      List<Long> endPointIds = relationship.endIdxs();
      StringBuilder propertyBuilder = new StringBuilder("{");
      var propIterator = relationship.getProperties().entrySet().iterator();
      boolean addedProperties = false;
      for (int i = 0; i < relationship.getProperties().size(); i++) {
        var entry = propIterator.next();
        var propertyName = entry.getKey();
        var propertyValue = entry.getValue();
        propertyBuilder.append(propertyName).append(":").append(propertyValue);
        if (i < relationship.getProperties().size() - 1) {
          propertyBuilder.append(", ");
        }
        addedProperties = true;
      }

      String edgeString =
          relationship.direction() == Relate.Direction.OUTGOING
              ? "(start)-[e:" + name + "]->(end)"
              : "(end)-[e:" + name + "]->(start)";

      if (endPointIds.size() > 1 || relationship.forceIdxProperty()) {
        if (addedProperties) {
          propertyBuilder.append(", idx: %d").append("}");
        } else {
          propertyBuilder.append("idx: %d").append("}");
        }
        for (int i = 0; i < endPointIds.size(); i++) {
          String propertiesString = String.format(propertyBuilder.toString(), i);
          try {
            /*try {
              // check
              String checkQuery =
                "match (start) where start.internalId=$startId "
                  + "match (end) where end.internalId=$endId "
                  + "return start, end";
              var result =
                session.query(
                  checkQuery,
                  Map.of("startId", relationship.startId(), "endId", endPointIds.get(i)));
              int resultCount = 0;
              for (Map<String, Object> stringObjectMap : result) {
                resultCount++;
              }
              if (resultCount == 0) {
                boolean b = true;
              }
            } catch (Exception ex) {
              boolean b = true;
            }*/

            // add idx
            String query =
                "match (start:Relatable) where start.internalId=$startId "
                    + "match (end:Relatable) where end.internalId=$endId "
                    + "create "
                    + edgeString
                    + " SET e = "
                    + propertiesString;
            var startId = relationship.startId();
            var endId = endPointIds.get(i);
            session.query(query, Map.of("startId", startId, "endId", endId));
          } catch (CypherException ex) {
            LOGGER.severe(ex.getMessage());
          }
        }
      } else if (endPointIds.size() == 1) {
        propertyBuilder.append("}");
        // add simple relationship
        try {
          String query =
              "match (start:Relatable) where start.internalId=$startId match (end:Relatable)"
                  + " where end.internalId=$endId "
                  + "create "
                  + edgeString
                  + " SET e = "
                  + propertyBuilder;
          session.query(
              query, Map.of("startId", relationship.startId(), "endId", endPointIds.getFirst()));
        } catch (CypherException ex) {
          LOGGER.severe(ex.getMessage());
        }
      }
    }

    private void initializeDB() {
      this.isRunning = true;
      LOGGER.info("INITIALIZE - LOCKING DB");
      dbMutex.lock();
      // var tx = session.beginTransaction(Transaction.Type.READ_WRITE);
      try {
        session.query("match (n) detach delete n", Map.of());
        session.clear();
        session.purgeDatabase();
        // tx.commit();
        // tx.close();
        session.query(
            "CREATE INDEX internal_id_index IF NOT EXISTS FOR (n:Relatable) ON (n.internalId)",
            Map.of());

        // tx = session.beginTransaction(Transaction.Type.READ_WRITE);
        var insanityCheck = session.query("match (n) return n", Map.of());
        session.save(Scope.NULL, 1);
        // tx.commit();

        var tx = session.beginTransaction(Transaction.Type.READ_WRITE);
        var globalScope = programmAnalyzer.getEnvironment().getGlobalScope();
        session.save(globalScope);
        tx.commit();

        tx = session.beginTransaction(Transaction.Type.READ_WRITE);
        var globalSymbols = globalScope.getSymbols();
        session.save(globalSymbols);

        var types = TypeFactory.INSTANCE.getTypes();
        session.save(types);
        tx.commit();

        tx = session.beginTransaction(Transaction.Type.READ_WRITE);
        RelationshipRecorder.instance.processObjectsToPersist();
        var objectsToPersist = RelationshipRecorder.instance.getObjectsToPersist();
        session.save(objectsToPersist);
        tx.commit();

        RelationshipRecorder.instance.processRelationships();
        var relationships = RelationshipRecorder.instance.get();
        relationships.forEach(this::saveRelationship);

        LOGGER.info("Finished initializing database!");
      } catch (Exception ex) {
        boolean b = true;
        LOGGER.severe(ex.getMessage());
      } finally {
        // tx.close();
        LOGGER.info("INITIALIZE - UNLOCKING DB");
        dbMutex.unlock();
        this.isRunning = false;
      }
    }

    private void updateDB(String text, String uri) throws InterruptedException {
      this.isRunning = true;
      throwIfStop();
      LOGGER.info("UPDATE DB - LOCKING DB");
      dbMutex.lock();

      ProgrammAnalyzer.AnalyzedProgramComplete analyzedProgram;
      analyzedProgram = programmAnalyzer.analyze(text, uri);

      // LOGGER.fine(getPrettyPrintedParseTree(text, new GameEnvironment()));

      boolean interrupted = false;
      var tx = session.beginTransaction(Transaction.Type.READ_WRITE);
      try {
        LOGGER.info("Updating program database...");
        throwIfStop();

        // non-interuptable!
        // TODO: for delta based update, this needs to change!!
        // session.deleteAll(ParentOf.class);
        session.deleteAll(Node.class);
        session.deleteAll(ErrorRecord.class);
        session.deleteAll(SourceFileReference.class);

        // delete only in file scope
        for (var parsedFile : analyzedProgram.parsedFiles()) {
          var result =
              session.query(
                  """
              match (f:FileScope)-[:OF_FILE]->(file:ParsedFile) where file.pathString=$pathString
              match (g:Groum)-[:FILE_SCOPE]->(f)
              call {
                  with f
                  call {
                      with f
                      return f as scope
                      union
                      with f
                      match (scope:IScope)-[:PARENT_SCOPE|IN_SCOPE*]->(f) return scope
                  }
                  optional match (symbol:Symbol)-[:IN_SCOPE]->(scope)
                  // return distinct scope, symbol
                  detach delete scope, symbol
              }
              call {
                  with g
                  match (g)-[:NODES]->(n:GroumNode)
                  detach delete n
              }
              detach delete g
              """,
                  Map.of("pathString", parsedFile.pathString));
        }

        throwIfStop();

        tx.commit();

        tx = session.beginTransaction(Transaction.Type.READ_WRITE);

        ProfilingTimer.Unit unit = ProfilingTimer.Unit.milli;
        HashMap<String, Long> times = new HashMap<>();
        // try (ProfilingTimer timer = new ProfilingTimer("AST", times, unit)) {
        // save ast in db
        for (var parsedFile : analyzedProgram.parsedFiles()) {
          var ast = parsedFile.rootASTNode();
          throwIfStop();
          session.save(parsedFile);
          // TODO: this does not work, because objects are not stored transitively..
          session.save(ast);
        }

        throwIfStop();
        session.save(analyzedProgram.symboltable().getScopes());

        throwIfStop();
        session.save(analyzedProgram.groum());

        var nodes = analyzedProgram.groum().nodes();
        session.save(nodes);

        var edges = analyzedProgram.groum().edges();
        edges.forEach(e -> RelationshipRecorder.instance.translateRelationshipEntity(e));

        throwIfStop();
        // TODO: could this be done in ProgramAnalyzer? does this really have to be done here?
        RelationshipRecorder.instance.processObjectsToPersist();
        var objectsToPersist = RelationshipRecorder.instance.getObjectsToPersist();
        session.save(objectsToPersist);
        // tx.commit();

        // tx = session.beginTransaction(Transaction.Type.READ_WRITE);
        RelationshipRecorder.instance.processRelationships();
        var relationships = RelationshipRecorder.instance.get();
        for (var relationShip : relationships) {
          throwIfStop();
          saveRelationship(relationShip);
        }
        tx.commit();

        LOGGER.info("Finished updating database!");
      } catch (InterruptedException interrupt) {
        LOGGER.info("Database update was interrupted");
        tx.rollback();
        interrupted = true;
      } catch (Exception ex) {
        LOGGER.severe(ex.getMessage());
      } finally {
        tx.close();
        LOGGER.info("UPDATE DB - UNLOCKING DB");
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
          match (n:AstNode)-[:HAS_ERROR_RECORD]->(e:ErrorRecord) return distinct e
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
