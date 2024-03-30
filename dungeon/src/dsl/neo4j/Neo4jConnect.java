package dsl.neo4j;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import entrypoint.DungeonConfig;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;

public class Neo4jConnect {
  public static void main(String[] args) {
    // main
    System.out.println("Hello, World!");


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
    final String dbUri = "neo4j://localhost";
    final String dbUser = "neo4j";
    final String dbPassword = "asdf1234";
    try (var driver = GraphDatabase.driver(dbUri, AuthTokens.basic(dbUser, dbPassword))) {
      driver.verifyConnectivity();
      Driver ogmDriver = new BoltDriver(driver);
      try {
        var sessionFactory = new SessionFactory(ogmDriver, "dsl.parser.ast");
        var session = sessionFactory.openSession();

        // clean up db
        session.query("MATCH (n) DETACH DELETE n", Map.of());

        // save ast in db
        session.save(ast);

        // get ast root node back from db
        var root =
            session.queryForObject(
                Node.class, "MATCH (n:Node {type:$type}) RETURN n", Map.of("type", "Program"));

        var mismatches = matchAST(ast, root);
        assert mismatches.isEmpty();

        sessionFactory.close();
      } catch (Exception ex) {
        boolean b = true;
      }
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
        "Nodes do not match, expected type: '" + expectedNode.type + "' given type: '" + givenNode.type + "'";
      mismatches.add(msg);
    }

    //  specific content match
    boolean specificMatch;
    switch (expectedNode.type) {
      case Identifier: {
        IdNode expected = (IdNode) expectedNode;
        IdNode given = (IdNode) givenNode;
        specificMatch = expected.getName().equals(given.getName());
        specificMatch &= expected.getSourceFileReference().equals(given.getSourceFileReference());
        if (!specificMatch) {
          String msg =
            "Node values do not match, expected value: '" + expected.getName() + "' given value: '" + given.getName() + "'";
          mismatches.add(msg);
        }
        break;
      }
      case Bool: {
        BoolNode expected = (BoolNode) expectedNode;
        BoolNode given = (BoolNode) givenNode;
        specificMatch = expected.getValue() == given.getValue();
        specificMatch &= expected.getSourceFileReference().equals(given.getSourceFileReference());
        if (!specificMatch) {
          String msg =
            "Node values do not match, expected value: '" + expected.getValue() + "' given value: '" + given.getValue()
              + "'";
          mismatches.add(msg);
        }
        break;
      }
      case Number: {
        NumNode expected = (NumNode) expectedNode;
        NumNode given = (NumNode) givenNode;
        specificMatch = expected.getValue() == given.getValue();
        specificMatch &= expected.getSourceFileReference().equals(given.getSourceFileReference());
        if (!specificMatch) {
          String msg =
            "Node values do not match, expected value: '" + expected.getValue() + "' given value: '" + given.getValue() + "'";
          mismatches.add(msg);
        }
        break;
      }
      case DecimalNumber: {
        DecNumNode expected = (DecNumNode) expectedNode;
        DecNumNode given = (DecNumNode) givenNode;
        specificMatch = expected.getValue() == given.getValue();
        specificMatch &= expected.getSourceFileReference().equals(given.getSourceFileReference());
        if (!specificMatch) {
          String msg =
            "Node values do not match, expected value: '" + expected.getValue() + "' given value: '" + given.getValue() + "'";
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

    for (int i = 0; i < expectedNode.getChildren().size() && i < givenNode.getChildren().size(); i++) {
      var expectedChild = expectedNode.getChild(i);
      var childToCheck = givenNode.getChild(i);
      List<String> msgs = matchAST(expectedChild, childToCheck);
      mismatches.addAll(msgs);
    }

    return mismatches;
  }
}
