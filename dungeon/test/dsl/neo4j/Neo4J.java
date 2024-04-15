package dsl.neo4j;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.*;
import dsl.runtime.callable.ICallable;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.ListType;
import dsl.semanticanalysis.typesystem.typebuilding.type.MapType;
import dsl.semanticanalysis.typesystem.typebuilding.type.SetType;
import entrypoint.DungeonConfig;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class Neo4J {
  @Test
  @Ignore
  public void testDBCallableConsistency() {
    String program =
        """
        assign_task t1 {
            description: "Task1",
            solution: <["a", "b"], ["c", "d"], ["y", "x"], ["c", "hallo"], [_, "world"], ["!", _]>
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        entity_type chest_type {
            inventory_component {},
            draw_component {
                path: "objects/treasurechest"
            },
            hitbox_component {},
            position_component{},
            interaction_component{},
            task_content_component{}
        }

        item_type scroll_type {
            display_name: "A scroll",
            description: "Please read me",
            texture_path: "items/book/wisdom_scroll.png"
        }

        fn build_task(assign_task t) -> entity<><> {
            var return_set : entity<><>;
            var room_set : entity<>;

            var solution_map : [element -> element<>];
            solution_map = t.get_solution();

            // instantiate chests
            for element key in solution_map.get_keys() {
                if key.is_empty() {
                    // skip
                } else {
                    // if this variable is declared outside of the for-loop,
                    // it is not correctly placed in the set, because the internal
                    // Value will be still the same Object (with the same HashCode!!)
                    var chest : entity;
                    chest = instantiate(chest_type);
                    chest.task_content_component.content = key;
                    room_set.add(chest);
                }
            }

            var item : quest_item;
            // instantiate all answer elements as scrolls
            for element<> element_set in solution_map.get_elements() {
                for element element in element_set {
                    if element.is_empty() {
                        // skip
                    } else {
                        print(element);
                        item = build_quest_item(scroll_type, element);
                        place_quest_item(item, room_set);
                    }
                }
            }

            return_set.add(room_set);
            return return_set;
        }
        """;
    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    var env = interpreter.getRuntimeEnvironment();
    var fileScope = env.getFileScopes().get(null);
    var parsedFile = fileScope.file();
    var ast = parsedFile.rootASTNode();
    var symTable = env.getSymbolTable();

    // URI examples: "neo4j://localhost", "neo4j+s://xxx.databases.neo4j.io"
    try (var driver = Neo4jConnect.openConnection()) {
      var sessionFactory = Neo4jConnect.getSessionFactory(driver);
      var session = sessionFactory.openSession();

      // clean up db
      session.query("MATCH (n) DETACH DELETE n", Map.of());

      // save ast in db
      session.save(ast);

      session.save(symTable.getSymbolCreations());
      session.save(symTable.getSymbolReferences());
      session.save(symTable.globalScope());
      var filScopes = env.getFileScopes().entrySet();
      for (var entry : filScopes) {
        var scope = entry.getValue();
        session.save(scope);
      }

      // get ast root node back from db
      var root =
          session.queryForObject(
              Node.class, "MATCH (n:Node {type:$type}) RETURN n", Map.of("type", "Program"));

      var mismatches = matchAST(ast, root);
      Assert.assertTrue(mismatches.isEmpty());

      ArrayList<Symbol> callableList = new ArrayList<>();
      getCallableWithType(env.getGlobalScope(), callableList);
      getCallableWithType(env.getFileScope(null), callableList);

      var symbols = session.query("MATCH (n:Symbol {symbolType:\"Callable\"}) return n", Map.of());

      var results = symbols.queryResults();
      ArrayList<Symbol> list = new ArrayList<>();
      results.forEach(m -> list.add((Symbol) m.get("n")));

      HashSet<Symbol> distinctCallablesFromDB = new HashSet<>(list);
      HashSet<Symbol> distinctCallablesFromInternalRep = new HashSet<>(callableList);

      List<Symbol> missingSymbols = new ArrayList<>();
      for (var symbol : distinctCallablesFromDB) {
        if (!distinctCallablesFromInternalRep.contains(symbol)) {
          missingSymbols.add(symbol);
        }
      }
      Assert.assertTrue(missingSymbols.isEmpty());

      sessionFactory.close();
    }
  }

  @Test
  public void testDBGetErrorNode() {
    String program =
      """
  assign_task t1 {
      description: "Task1",
      solution: <["a", "b"]>
  }

  graph g {
      t1 -> (; // broken definition
  }

  dungeon_config c {
      dependency_graph: g
  }
  """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    DSLInterpreter interpreter = new DSLInterpreter();
    try {
      DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    } catch (RuntimeException ex) {
      // program contains error, won't be able to create quest config
    }

    var env = interpreter.getRuntimeEnvironment();
    var fileScope = env.getFileScopes().get(null);
    var parsedFile = fileScope.file();
    var ast = parsedFile.rootASTNode();
    var symTable = env.getSymbolTable();

    // URI examples: "neo4j://localhost", "neo4j+s://xxx.databases.neo4j.io"
    try (var driver = Neo4jConnect.openConnection()) {
      var sessionFactory = Neo4jConnect.getSessionFactory(driver);
      var session = sessionFactory.openSession();

      // clean up db
      session.query("MATCH (n) DETACH DELETE n", Map.of());

      // save ast in db
      session.save(ast);

      session.save(symTable.getSymbolCreations());
      session.save(symTable.getSymbolReferences());
      session.save(symTable.globalScope());
      var filScopes = env.getFileScopes().entrySet();
      for (var entry : filScopes) {
        var scope = entry.getValue();
        session.save(scope);
      }

      // get ast root node back from db
      var graphNodeReferenceAssignDef =
        session.queryForObject(
          Node.class,
          """
        match
        (n:Node {type:"Identifier", name:"t1"})-[:REFERENCES]->(s:Symbol {name:"t1"}),
        (o:Node {type:"ObjectDefinition"})-[:CREATES]->(s),
        (t:IType {name:"assign_task"})<-[:OF_TYPE]-(s)
        return n
        """,
          Map.of());
      Assert.assertNotEquals(null, graphNodeReferenceAssignDef);

      var dungeonConfigRefGraph =
        session.query(
          """
        match
        (n:Node {type:"Identifier", name:"g"})-[:REFERENCES]->(s:Symbol {name:"g"}),
        (o:Node {type:"DotDefinition"})-[:CREATES]->(s)
        return n
        """,
          Map.of());
      Assert.assertNotEquals(null, dungeonConfigRefGraph);

      var topLevelNodesWithErrorChildren =
        session.query(
          """
        match (n:Node {hasErrorChild:TRUE}) return n
        """,
          Map.of());

      ArrayList<Node> list = new ArrayList<>();
      topLevelNodesWithErrorChildren.queryResults().forEach(r -> list.add((Node) r.get("n")));

      Assert.assertEquals(2, list.size());

      var nodesWithErrorRecord =
        session.query(
          """
        match (n:Node {hasErrorRecord:TRUE}) return n
        """,
          Map.of());

      ArrayList<Node> nodes = new ArrayList<>();
      nodesWithErrorRecord.queryResults().forEach(r -> nodes.add((Node) r.get("n")));
      Assert.assertEquals(1, nodes.size());

      /*(var specificErrorChildren =
       session.query("""
           match (n:Node {hasErrorChild:TRUE}) return n
           """, Map.of());

      */

      sessionFactory.close();
    }
  }

  @Test
  public void testDBNodeRelationShips() {
    String program =
        """
    assign_task t1 {
        description: "Task1",
        solution: <["a", "b"]>
    }

    graph g {
        t1 -> t2;
    }

    fn test() -> {
      var x = y;
      var u = w;
    }

    dungeon_config c {
        dependency_graph: g
    }
    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    DSLInterpreter interpreter = new DSLInterpreter();
    try {
      RelationshipRecorder.instance.clear();
      DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    } catch (RuntimeException ex) {
      // program contains error, won't be able to create quest config
    }

    var env = interpreter.getRuntimeEnvironment();
    var fileScope = env.getFileScopes().get(null);
    var parsedFile = fileScope.file();
    var ast = parsedFile.rootASTNode();
    var symTable = env.getSymbolTable();
    var nodeRelationShips = RelationshipRecorder.instance.get();

    // URI examples: "neo4j://localhost", "neo4j+s://xxx.databases.neo4j.io"
    try (var driver = Neo4jConnect.openConnection()) {
      var sessionFactory = Neo4jConnect.getSessionFactory(driver);
      var session = sessionFactory.openSession();

      // clean up db
      session.query("MATCH (n) DETACH DELETE n", Map.of());

      // save ast in db
      session.save(ast);
      session.save(nodeRelationShips);

      //session.save(symTable.getSymbolCreations());
      //session.save(symTable.getSymbolReferences());
      //session.save(symTable.globalScope());
      /*var filScopes = env.getFileScopes().entrySet();
      for (var entry : filScopes) {
        var scope = entry.getValue();
        session.save(scope);
      }*/

      var nodesOnRhsOfVarDecl =
          session.query(
              """
            match (n:IdNode)<-[:PARENT_OF{idx:1}]-(v:VarDeclNode) return n
            """,
              Map.of());

      ArrayList<Node> nodesOnRhs = new ArrayList<>();
      nodesOnRhsOfVarDecl.queryResults().forEach(r -> nodesOnRhs.add((Node) r.get("n")));
      Assert.assertEquals(2, nodesOnRhs.size());

      var nameSet = nodesOnRhs.stream().map(n -> ((IdNode)n).getName()).collect(Collectors.toSet());
      Assert.assertTrue(nameSet.contains("y"));
      Assert.assertTrue(nameSet.contains("w"));

      // test for persistence of child-relationships
      var varDecls =
        session.query(
          """
        match (n:IdNode)<-[:PARENT_OF{idx:1}]-(v:VarDeclNode) return distinct v
        """,
          Map.of());

      ArrayList<Node> varDeclNodes = new ArrayList<>();
      varDecls.queryResults().forEach(r -> varDeclNodes.add((Node) r.get("v")));
      Assert.assertEquals(2, varDeclNodes.size());

      for (var node : varDeclNodes) {
        Assert.assertEquals(2, node.getChildren().size());
        var firstChild = (IdNode)node.getChild(0);
        var secondChild = (IdNode)node.getChild(1);
        if (firstChild.getName().equals("x")) {
          Assert.assertEquals("y", secondChild.getName());
        }
        if (firstChild.getName().equals("u")) {
          Assert.assertEquals("w", secondChild.getName());
        }
      }
      // TODO: assert on children

      sessionFactory.close();
    }
  }

  private static List<String> matchAST(Node expectedNode, Node givenNode) {
    List<String> mismatches = new ArrayList<>();

    boolean match = expectedNode.type.equals(givenNode.type);
    match &= expectedNode.hasErrorChild() == givenNode.hasErrorChild();
    boolean errorRecordMatch;
    try {
      errorRecordMatch =
          expectedNode.getErrorRecord() == null && givenNode.getErrorRecord() == null
              || expectedNode.getErrorRecord().equals(givenNode.getErrorRecord());
    } catch (NullPointerException ex) {
      errorRecordMatch = false;
    }
    match &= errorRecordMatch;

    if (!match) {
      String msg =
          "Nodes do not match, expected type: '"
              + expectedNode.type
              + "' given type: '"
              + givenNode.type
              + "'";
      mismatches.add(msg);
    }

    //  specific content match
    boolean specificMatch;
    switch (expectedNode.type) {
      case Identifier:
        {
          IdNode expected = (IdNode) expectedNode;
          IdNode given = (IdNode) givenNode;
          specificMatch = expected.getName().equals(given.getName());
          specificMatch &= expected.getSourceFileReference().equals(given.getSourceFileReference());
          if (!specificMatch) {
            String msg =
                "Node values do not match, expected value: '"
                    + expected.getName()
                    + "' given value: '"
                    + given.getName()
                    + "'";
            mismatches.add(msg);
          }
          break;
        }
      case Bool:
        {
          BoolNode expected = (BoolNode) expectedNode;
          BoolNode given = (BoolNode) givenNode;
          specificMatch = expected.getValue() == given.getValue();
          specificMatch &= expected.getSourceFileReference().equals(given.getSourceFileReference());
          if (!specificMatch) {
            String msg =
                "Node values do not match, expected value: '"
                    + expected.getValue()
                    + "' given value: '"
                    + given.getValue()
                    + "'";
            mismatches.add(msg);
          }
          break;
        }
      case Number:
        {
          NumNode expected = (NumNode) expectedNode;
          NumNode given = (NumNode) givenNode;
          specificMatch = expected.getValue() == given.getValue();
          specificMatch &= expected.getSourceFileReference().equals(given.getSourceFileReference());
          if (!specificMatch) {
            String msg =
                "Node values do not match, expected value: '"
                    + expected.getValue()
                    + "' given value: '"
                    + given.getValue()
                    + "'";
            mismatches.add(msg);
          }
          break;
        }
      case DecimalNumber:
        {
          DecNumNode expected = (DecNumNode) expectedNode;
          DecNumNode given = (DecNumNode) givenNode;
          specificMatch = expected.getValue() == given.getValue();
          specificMatch &= expected.getSourceFileReference().equals(given.getSourceFileReference());
          if (!specificMatch) {
            String msg =
                "Node values do not match, expected value: '"
                    + expected.getValue()
                    + "' given value: '"
                    + given.getValue()
                    + "'";
            mismatches.add(msg);
          }
          break;
        }
      default:
        break;
    }

    if (expectedNode.getChildren().size() != givenNode.getChildren().size()) {
      // check for don't care node
      String msg =
          String.format(
              "Childcount of expected node '%s'(%x) and given node '%s'(%x) is different!",
              expectedNode,
              expectedNode.getChildren().size(),
              givenNode,
              givenNode.getChildren().size());
      mismatches.add(msg);
    }

    for (int i = 0;
        i < expectedNode.getChildren().size() && i < givenNode.getChildren().size();
        i++) {
      var expectedChild = expectedNode.getChild(i);
      var childToCheck = givenNode.getChild(i);
      List<String> msgs = matchAST(expectedChild, childToCheck);
      mismatches.addAll(msgs);
    }

    return mismatches;
  }

  static int countCallables(IScope scope, int count) {
    var symbols = scope.getSymbols();
    for (var symbol : symbols) {
      if (symbol instanceof ICallable) {
        count++;
      }
      if (symbol instanceof IScope childScope) {
        count += countCallables(childScope, 0);
      }
    }
    return count;
  }

  static int countSymbolsWithCallableType(IScope scope, int count) {
    var symbols = scope.getSymbols();
    for (var symbol : symbols) {
      if (symbol.getSymbolType().equals(Symbol.SymbolType.Callable)) {
        count++;
      }
      if (symbol instanceof IScope childScope) {
        count += countSymbolsWithCallableType(childScope, 0);
      }
    }
    return count;
  }

  static ArrayList<Symbol> getCallableWithType(IScope scope, ArrayList<Symbol> callables) {
    var symbols = scope.getSymbols();
    for (var symbol : symbols) {
      if (symbol.getSymbolType().equals(Symbol.SymbolType.Callable)) {
        callables.add(symbol);
      }
      if (symbol instanceof ListType listType
          || symbol instanceof SetType setType
          || symbol instanceof MapType) {
        boolean b = true;
      }
      if (symbol.getName().equals("task_content<>") && symbol instanceof SetType) {
        boolean b = true;
      }
      if (symbol instanceof IScope childScope) {
        getCallableWithType(childScope, callables);
      }
    }
    return callables;
  }
}
